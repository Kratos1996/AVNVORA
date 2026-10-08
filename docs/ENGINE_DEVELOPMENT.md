# AYNVORA Engine Development Guide

This guide establishes the architectural rules and SPI requirements for contributing a new feature engine to the AYNVORA platform.

---

## 1. Engine SPI: `AynvoraFeatureEngine`

Every engine must implement `AynvoraFeatureEngine` from `:aynvora-contracts`:

```kotlin
interface AynvoraFeatureEngine {
    val featureId: AynvoraFeatureId
    val version: String
    val supportedEventTypes: Set<String>

    suspend fun handleEvent(event: AynvoraEvent): AynvoraEventResponse
}
```

---

## 2. Engine Provider SPI: `AynvoraEngineProvider`

To support lazy initialization and dependency isolation, engines must provide an `AynvoraEngineProvider`:

```kotlin
class MyFeatureEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.MY_FEATURE

    override fun create(): AynvoraFeatureEngine {
        return MyFeatureEngine()
    }
}
```

The engine is only constructed when the first event for `MY_FEATURE` is dispatched through `AynvoraEventRouter`.

---

## 3. Engine Rules & Invariants

1. **Deterministic Calculations**:
   Given identical inputs, the calculation facts must always be identical regardless of locale or environment.

2. **No UI Imports**:
   Engines must never depend on UI frameworks (Compose, Skiko, Android Views).

3. **No Direct Engine Imports**:
   Engines must never directly import classes from other feature engines. If an engine requires outputs from another engine (such as AI grounding or Report synthesis), they must be passed via standard JSON evidence payloads.

4. **Resource Lifecycle**:
   For memory- or model-heavy engines, implement release logic to free buffers, native tensors, or caches when `release(featureId)` is invoked.
