# AYNVORA AI Runtime Performance & Resource Metrics

## 1. Latency & Resource Observation Metrics

AYNVORA tracks fine-grained runtime telemetry per inference cycle through `AiInferenceDiagnostics`:

| Metric Name            | Unit   | Target / Typical Range           | Description                                                  |
|:-----------------------|:-------|:---------------------------------|:-------------------------------------------------------------|
| `coldLoadDurationMs`   | ms     | 800 - 2,500 ms                   | Time to map and verify model weights into RAM on first load. |
| `warmLoadDurationMs`   | ms     | < 50 ms                          | Latency when model is already memory-resident.               |
| `inferenceDurationMs`  | ms     | 1,200 - 4,500 ms                 | Wall-clock execution time for explanation generation.        |
| `tokensPerSecond`      | tok/s  | 12 - 35 tok/s (device dependent) | Model generation throughput.                                 |
| `allocatedMemoryBytes` | bytes  | 550 MB - 1.4 GB                  | Total memory utilized by model weights + KV cache.           |
| `contextTokensUsed`    | tokens | 120 - 450 tokens                 | Context window utilization for Tarot evidence.               |

## 2. Dynamic RAM Safeguard Formula

Prior to invoking native model initialization:
$$\text{Max Allowed Memory} = \text{Available System RAM} \times 0.70$$
If model size + KV cache + 64 MB exceeds this threshold, loading is rejected immediately with
`AiRuntimeErrorCode.INSUFFICIENT_RUNTIME_MEMORY`.

## 3. Cold vs Warm Performance Observations

- **Cold Load**: Evaluates checksum, allocates KV cache buffers, and initializes tensor operations.
- **Warm Load**: Bypasses initialization; reuses existing resident context and weight pointers.
- **Unload**: Invoking `unloadModel()` triggers native handle deallocation and explicit garbage
  collection hint, returning memory to the OS.
