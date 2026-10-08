# AYNVORA Engine Implementation Guide

## Engine Contract

Every engine in AYNVORA implements the `AynvoraFeatureEngine` interface from `:aynvora-contracts`:

```kotlin
interface AynvoraFeatureEngine {
    val featureId: AynvoraFeatureId
    val version: String
    val capabilities: Set<AynvoraFeatureCapability>

    suspend fun handle(event: AynvoraEvent): AynvoraEventResponse
}
```

## Rules for Engine Developers

1. **Zero UI Dependencies**:
   - Never import `androidx.compose.*`, `android.view.*`, or `android.content.Context`.
   - Never include localized display strings like `"Right Hand"` or `"Galat haath"`. Use language-neutral enums and tokens.
2. **Zero Cross-Engine Dependencies**:
   - An engine cannot depend on another engine module in Gradle or Kotlin imports.
   - If an engine requires planetary concepts (e.g., Gemstones or Rudraksha mapping to Grahas), use shared enum `CelestialBody` from `:aynvora-contracts`.
3. **JSON-First Request & Result**:
   - Define `@Serializable` request and result classes within your engine.
   - `handle(event)` decodes `event.payloadJson`, performs calculations, and serializes the result into `AynvoraEventResponse.success(...)`.
4. **Deterministic Calculation & Provenance**:
   - Engine results should attach `AynvoraProvenance` indicating `engineId`, `engineVersion`, `calculationVersion`, and deterministic flag.
5. **Truthful Readiness Status**:
   - If an engine is foundation-only or under research, return `AynvoraStatus.FEATURE_NOT_READY` or `FEATURE_UNSUPPORTED`. Never return mock or fake calculations.

## How to Add a New Engine

1. Create module directory under `engines/<feature-name>-engine`.
2. Define `build.gradle.kts` targeting KMP (`commonMain`, `jvm`, optional `androidLibrary`) with `api(project(":aynvora-contracts"))`.
3. Include project in `settings.gradle.kts`.
4. Add feature identifier to `AynvoraFeatureId` in `:aynvora-contracts`.
5. Implement `AynvoraFeatureEngine` in `<feature-name>-engine`.
6. Register the engine in `AynvoraEngineRegistry` in `:aynvora-core`.
7. Add a typed client adapter in `aynvora-sdk`.
