# AYNVORA Local Offline Inference & Network Access Audit

## 1. Zero-Network Principle

AYNVORA On-Device AI operates under a strict offline contract:

- Inference **never** makes network requests.
- No telemetry, analytics, or background services transmit inference prompts, card selections, or
  generated explanations.
- Disabling device internet connectivity has zero effect on model loading or inference generation.

## 2. Network Access Audit of `AiInferenceEngine.generate()`

A static code path audit of `LocalNativeInferenceEngine.generate()` confirms that no networking
libraries or network calls are invoked:

```
LocalNativeInferenceEngine.generate(request)
  │
  ├─ Network client check:
  │    • Ktor HttpClient: NOT USED
  │    • Retrofit / OkHttp: NOT USED
  │    • Firebase AI / Vertex / Cloud ML: NOT USED
  │    • java.net.HttpURLConnection / Socket: NOT USED
  │    • Remote Tokenizer / Web Sockets: NOT USED
  │
  ├─ Internal Path:
  │    1. Validates local model file on device disk (AiModelStorageRepository).
  │    2. Checks dynamic RAM headroom (calculateSafeRamAllocation).
  │    3. Verifies context token budget (contextTokens <= variant.contextLength).
  │    4. Loads weights directly from local storage.
  │    5. Computes token generation locally on CPU/NPU.
  │    6. Validates generated text via AiOutputValidator.
  │
  └─ Offline Verification Result:
       • OFFLINE INFERENCE VERIFIED: All inference pipelines run completely offline without internet.
```

## 3. Evidence Privacy Boundary

The Tarot feature passes data to the AI runtime strictly via `TarotFeatureDataConnector` and
`AiContextBuilder`. The context builder creates an `AiContext` with
`privacyTier = AiPrivacyTier.STRICT_LOCAL_ONLY`. Any attempt to serialize or route this context to a
remote endpoint is prohibited by architectural contract.
