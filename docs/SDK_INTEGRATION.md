# AYNVORA SDK Integration Guide (Phase 10.46)

AYNVORA is a production-grade, modular Kotlin Multiplatform SDK for astrology, palmistry, numerology, tarot, gemstone recommendations, sacred scriptures, AI grounding, and composite report synthesis.

Starting with Phase 10.46, AYNVORA is structured as a **fully distributable, engine-first modular library** published to Maven with strict dependency isolation. Consumers do NOT need to import or bundle the entire application suite; applications declare dependencies on only the specific feature engines they require.

---

## 1. Architectural Highlights

- **Zero Monolithic Lock-In**: Import only the engines you need.
- **Physical Feature Exclusion**: Excluding an engine module physically omits its native libraries, vision models, calculation tables, and classes from your binary.
- **Dual API Paradigms**:
  - **Mode A (Strongly Typed)**: High-level Kotlin types, safety, compile-time autocomplete.
  - **Mode B (Headless / Raw JSON)**: Decoupled event-driven JSON payloads for headless services, REST/gRPC wrappers, or custom UI engines.
- **Lazy Engine Loading**: Engines remain uninstantiated until their first event dispatch.
- **Centralized Event Router**: Thread-safe registry, lifecycle management (`REGISTERED` -> `READY` -> `RUNNING` -> `IDLE` -> `RELEASED`), and access control.

---

## 2. Choosing Your Integration Mode

### Scenario A: Astrology-Only Application

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.aynvora:aynvora-sdk:10.46.0")
    implementation("com.aynvora:astro-engine:10.46.0")
}
```

```kotlin
import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraResult
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig

val sdk = Aynvora.create(
    AynvoraConfig(
        engineProviders = listOf(AstroEngineProvider())
    )
)

val result = sdk.astrology.calculate(
    AstrologyRequest(
        name = "Aryabhata",
        dateOfBirth = "1992-04-14",
        timeOfBirth = "06:15:00",
        location = AstrologyLocation(latitude = 28.6139, longitude = 77.2090),
    )
)

when (result) {
    is AynvoraResult.Success -> {
        println("Lagna: ${result.value.lagnaSign}, Julian Day: ${result.value.julianDay}")
    }
    is AynvoraResult.Error -> {
        println("Error: ${result.error.message}")
    }
}
```

*Note: Calling `sdk.palmistry.analyze(...)` in an Astrology-only build returns `AynvoraStatus.FEATURE_NOT_INCLUDED`.*

---

### Scenario B: Palmistry-Only Application

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.aynvora:aynvora-sdk:10.46.0")
    implementation("com.aynvora:palmistry-engine:10.46.0")
}
```

```kotlin
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig

val sdk = Aynvora.create(
    AynvoraConfig(
        engineProviders = listOf(PalmEngineProvider())
    )
)

val result = sdk.palmistry.analyze(
    PalmistryRequest(
        selectedHand = HandType.RIGHT,
        widthPx = 640,
        heightPx = 480,
    )
)
```

*Note: Completely independent; does NOT require an Astrology birth chart.*

---

### Scenario C: Multi-Feature Application (Astrology + Palmistry + AI + Report)

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.aynvora:aynvora-sdk:10.45.0")
    implementation("com.aynvora:astro-engine:10.45.0")
    implementation("com.aynvora:palmistry-engine:10.45.0")
    implementation("com.aynvora:ai-engine:10.45.0")
    implementation("com.aynvora:report-engine:10.45.0")
}
```

```kotlin
val sdk = Aynvora.create(
    AynvoraConfig(
        engineProviders = listOf(
            AstroEngineProvider(),
            PalmEngineProvider(),
            AiEngineProvider(),
            ReportEngineProvider(),
        )
    )
)
```

---

## 3. Official Bundled Mode (AYNVORA First-Party Apps)

When all 13 official engine modules are on the classpath:

```kotlin
// Default configuration registers all available bundled engines
val sdk = Aynvora.create()
```
