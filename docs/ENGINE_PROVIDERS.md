# AYNVORA Engine Provider Contract & Registry Architecture

Every feature calculation engine in AYNVORA connects to the Core Event Router through the `AynvoraEngineProvider` interface.

---

## 1. Engine Provider Interface

```kotlin
interface AynvoraEngineProvider {
    val featureId: AynvoraFeatureId
    val engineId: String
    val engineVersion: String
    val capabilities: Set<AynvoraFeatureCapability>
    val minimumSdkVersion: String get() = "10.46.0"

    fun create(): AynvoraFeatureEngine
}
```

Key characteristics:
1. **Lightweight Metadata**: Calling `featureId`, `engineId`, `engineVersion`, or `capabilities` does not instantiate the heavy calculation engine.
2. **Lazy Instantiation**: `create()` is only invoked by the router upon receipt of the first event for that feature.
3. **Physical Decoupling**: Providers live inside each feature engine artifact (e.g. `AstroEngineProvider` in `:astro-engine`, `PalmEngineProvider` in `:palmistry-engine`).

---

## 2. Engine Registry Lifecycles

The `AynvoraEngineRegistry` tracks each engine through strict states:

- `NOT_INCLUDED`: Engine provider was not included in `AynvoraConfig`.
- `REGISTERED`: Engine provider registered with the registry; engine instance not yet created.
- `READY`: Engine instance created and validated.
- `DISABLED`: Engine registered, but temporarily disabled by consumer settings (`AynvoraFeatureSettings`).
- `LOCKED`: Engine requires higher subscription/entitlement tier.
- `UNSUPPORTED`: Engine cannot run on the current platform/OS.
- `FAILED`: Engine initialization failed.

---

## 3. Creating a Custom Engine Provider

External developers can register custom feature engines into the SDK pipeline:

```kotlin
class CustomEngineProvider : AynvoraEngineProvider {
    override val featureId = AynvoraFeatureId.CUSTOM
    override val engineId = "com.example.custom"
    override val engineVersion = "1.0.0"
    override val capabilities = setOf(AynvoraFeatureCapability("CUSTOM_EVAL", "Custom Evaluation", true, true))

    override fun create(): AynvoraFeatureEngine = CustomEngine()
}
```
