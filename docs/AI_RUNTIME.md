# AYNVORA On-Device AI Runtime Architecture

## 1. Overview & Principle

AYNVORA AI runtime executes small language models (SLMs) strictly on-device, prioritizing privacy,
deterministic safety boundaries, and predictable resource consumption.

Under no circumstances does the AI system:

- Access SQLite/Room DAOs directly.
- Modify authoritative Tarot spreads, cards, positions, or provenance metadata.
- Make outbound network requests for inference.
- Return unverified or fatalistic claims to the user.

## 2. Component Architecture

```
[Tarot Reading / Evidence]
         │
         ▼
[TarotFeatureDataConnector] ──> [AiContextBuilder]
                                        │
                                        ▼
                                 [AiRequest]
                                        │
                         ┌──────────────┴──────────────┐
                         ▼                             ▼
             [Real Model Installed?]               [No Model]
                    │                                  │
         ┌──────────┴──────────┐                       │
       [Yes]                  [No]                     │
         │                     │                       │
         ▼                     │                       │
[Dynamic RAM Check]            │                       │
         │                     │                       │
  ┌──────┴──────┐              │                       │
[Safe]       [OOM Risk]        │                       │
  │             │              │                       │
  ▼             ▼              ▼                       │
[LocalNativeEngine] ──> [Deterministic Engine] <───────┘
  │                                │
  ▼                                ▼
[Native llama.cpp JNI]             │
  │                                │
  ▼                                │
[Raw Generation]         [Authoritative Text]
  │                                │
  ▼                                │
[AiOutputValidator]                │
  │                                │
  ├─ Valid? ───────────────────────┼──> [AiResponse (REAL_MODEL_INFERENCE)]
  └─ Infallible Fallback ──────────┘──> [AiResponse (DETERMINISTIC_FALLBACK)]
```

## 3. Native Runtime Driver: `LlamaNativeRuntimeDriver`

Phase 8.8 replaces simulation with the actual native GGUF runtime driver:

- **Contract**: Implements `NativeAiRuntime` interface (`isAvailable()`, `loadModel()`,
  `generate()`, `cancel()`, `releaseModel()`).
- **Target Engine**: `llama.cpp` native shared libraries (`libllama.so` on Android).
- **Target ABIs**: Modern 64-bit architectures (`arm64-v8a`, `x86_64`). 32-bit legacy ABIs are
  filtered out to prevent address space exhaustion.
- **Model Format**: Validates `.gguf` extension and GGUF binary format before invoking native
  loader.
- **Handle Lifecycle**: Native model handles are tracked in an internal allocation registry. Model
  unloads explicitly trigger native handle deallocation, preventing native memory leaks, dangling
  pointers, and double-free exceptions.

## 4. Production vs Testing Separation

- **Production Engine (`LocalNativeInferenceEngine`)**: Bound in `coreDomainModule.kt`. Execution
  mode is strictly `AiExecutionMode.REAL_MODEL_INFERENCE`.
    - Under no circumstances does `LocalNativeInferenceEngine` silently fall back to simulation if
      native execution fails.
    - Native runtime errors map directly to typed `AiRuntimeErrorCode` and fail immediately to
      `AynvoraResult.Failure`.
    - The consuming layer (`GroundedSlmTarotExplanationEngine`) transparently handles failures by
      invoking `DeterministicTarotExplanationEngine`, attributing fallback clearly (
      `fallbackUsed = true`).
- **Testing Engine (`LocalSimulationEngine`)**: Isolated test engine with
  `AiExecutionMode.LOCAL_SIMULATION` used exclusively for non-native unit tests and CI mock
  harnesses.

## 5. Typed Native Error Mapping

Raw native C++ crashes are prevented; native errors are strictly mapped into typed domain error
codes:

- `NATIVE_LIBRARY_LOAD_FAILED`: Native shared library could not be loaded into the process.
- `NATIVE_MODEL_LOAD_FAILED`: Model weight parsing or tensor context creation failed.
- `NATIVE_CONTEXT_CREATE_FAILED`: Insufficient memory to allocate KV cache context.
- `NATIVE_OUT_OF_MEMORY`: Native heap memory exhausted during generation.
- `NATIVE_INFERENCE_FAILED`: Generation loop encountered unexpected tensor exception.
- `NATIVE_CANCELLED`: Native token generation interrupted by user or request cancellation.
- `NATIVE_INVALID_HANDLE`: Stale or unregistered native model pointer.
- `NATIVE_RUNTIME_UNAVAILABLE`: Runtime binary not linked or unsupported on host ABI.
- `UNSUPPORTED_ARCHITECTURE`: Host CPU does not support 64-bit tensor instructions.

## 6. Dynamic Memory Safety Check

Prior to loading a model into memory, `LocalNativeInferenceEngine` computes the safe runtime
allocation:

$$\text{Required Bytes} = \text{Model Size} + (\text{Max Context Tokens} \times 128 \text{ bytes KV Cache}) + \text{Runtime Overhead (64 MB)}$$
$$\text{Safe Limit} = \text{Available System RAM} \times 0.70$$

If $\text{Required Bytes} > \text{Safe Limit}$, the engine aborts loading with
`AiRuntimeErrorCode.INSUFFICIENT_RUNTIME_MEMORY` and routes immediately to deterministic fallback,
avoiding OS-level low-memory killer (LMK) termination.

## 7. Context Overflow & Timeout Protection

- **Context Length Bounding**: Prompts are constrained to the model's safe token budget. If prompt
  token count exceeds the variant's `contextLength`, the request fails with
  `AiRuntimeErrorCode.CONTEXT_OVERFLOW`.
- **Inference Timeout**: Every inference call is wrapped in a hard timeout (
  `withTimeoutOrNull(timeoutMs)`). If execution stalls, the engine cancels native execution and
  returns `AiRuntimeErrorCode.INFERENCE_TIMEOUT`.
- **Cancellation**: Requests track active request IDs. Cancellation via coroutine cancellation or
  explicit request abort terminates generation cleanly via `nativeRuntime.cancel(handle)` without
  leaving orphaned native handles or zombie background threads.

## 8. Execution Modes & Diagnostics

Every `AiResponse` explicitly states its `AiExecutionMode`:

- `REAL_MODEL_INFERENCE`: Generated locally by the installed model.
- `DETERMINISTIC_FALLBACK`: Rule-based classical template generation when model is uninstalled,
  memory-constrained, or output fails validation.
- `LOCAL_SIMULATION`: In-memory development mock for non-native environments.

Diagnostics tracked per inference:

- `coldLoadDurationMs`: Initial weight loading time.
- `warmLoadDurationMs`: Latency when weights are already cached in RAM.
- `inferenceDurationMs`: Wall-clock generation duration.
- `tokensPerSecond`: Generation throughput.
- `allocatedMemoryBytes`: Memory occupied by model weights and context buffer.
- `contextTokensUsed`: Token count consumed by prompt.
