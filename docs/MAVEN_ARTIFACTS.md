# AYNVORA Maven Artifacts & Distribution Model (Phase 10.46)

AYNVORA publishes 20 modular Kotlin Multiplatform (JVM and Android AAR) artifacts under the Maven group `com.aynvora`. Each artifact is built with strict boundary enforcement, independent lifecycle, and complete physical exclusion of unneeded feature resources.

---

## 1. Publication Coordinates

All artifacts share the unified version `10.46.0` (or future semantic release versions).

| Category | Artifact ID | Description | Targets |
| :--- | :--- | :--- | :--- |
| **Core** | `aynvora-contracts` | Core event types, status enums, provenance, feature contracts | JVM, Android AAR |
| **Core** | `aynvora-core` | Central Event Router, Engine Registry, Access Controller | JVM, Android AAR |
| **Core** | `aynvora-sdk` | Public API facade, typed adapters, AynvoraConfig builder | JVM, Android AAR |
| **Engine** | `astro-engine` | High-precision Swiss Ephemeris / Vedic calculation engine | JVM, Android AAR |
| **Engine** | `palmistry-engine` | Classical Palmistry line/mount analysis engine | JVM, Android AAR |
| **Engine** | `numerology-engine` | Multi-tradition Chaldean / Pythagorean / Indian numerology | JVM, Android AAR |
| **Engine** | `tarot-engine` | Multi-spread Tarot draw and interpretation engine | JVM, Android AAR |
| **Engine** | `gemstone-engine` | Anukul / Graha Gemstone recommendation engine | JVM, Android AAR |
| **Engine** | `gita-engine` | Srimad Bhagavad Gita verse semantic lookup | JVM, Android AAR |
| **Engine** | `garuda-puran-engine`| Garuda Puran karma / post-mortem doctrine engine | JVM, Android AAR |
| **Engine** | `rudraksha-engine` | Mukhi recommendation engine | JVM, Android AAR |
| **Engine** | `jadi-engine` | Herbal roots (Vanaushadhi) astrological engine | JVM, Android AAR |
| **Engine** | `yantra-engine` | Geometric Vedic Yantra synthesis engine | JVM, Android AAR |
| **Engine** | `guidance-engine` | Synthesized holistic remediation engine | JVM, Android AAR |
| **Engine** | `ai-engine` | On-device Qwen GGUF runtime / evidence grounding | JVM, Android AAR |
| **Engine** | `report-engine` | Cross-engine PDF & export report synthesizer | JVM, Android AAR |
| **Data** | `aynvora-data` | Room/SQLite offline persistence & cache layer | JVM, Android AAR |
| **Localization** | `aynvora-localization`| Multilingual token-to-string dictionary resolver | JVM, Android AAR |
| **Presentation** | `design-system` | Core Compose Multiplatform design tokens & components | JVM, Android AAR |
| **Presentation** | `ui` | Official Compose Multiplatform UI screens | JVM, Android AAR |

---

## 2. Resolving from MavenLocal

To consume published artifacts locally during evaluation or integration testing:

```kotlin
// settings.gradle.kts or build.gradle.kts
repositories {
    mavenLocal()
    mavenCentral()
    google()
}
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.aynvora:aynvora-sdk:10.46.0")
    implementation("com.aynvora:astro-engine:10.46.0")
}
```

---

## 3. Physical Artifact Isolation & Inspection Audit

Every artifact has been verified using `jar -tf` and AAR inspection:

1. **`astro-engine-jvm-10.46.0.jar`**:
   - Contains: `com/aynvora/astro/**`
   - ZERO Palmistry classes (`com/aynvora/palmistry/**` = 0)
   - ZERO Tarot classes (`com/aynvora/tarot/**` = 0)
   - ZERO Numerology classes (`com/aynvora/numerology/**` = 0)
   - ZERO UI / Compose dependencies

2. **`palmistry-engine-jvm-10.46.0.jar`**:
   - Contains: `com/aynvora/palmistry/**`
   - ZERO Astrology classes (`com/aynvora/astro/**` = 0)
   - ZERO Ephemeris or planetary data tables

3. **`ai-engine-jvm-10.46.0.jar`**:
   - Contains: `com/aynvora/ai/**`
   - Isolated native llama wrappers and grounding logic
   - ZERO feature calculations (Astrology/Palmistry/Tarot are NOT included)

4. **`report-engine-jvm-10.46.0.jar`**:
   - Contains: `com/aynvora/report/**`
   - Consumes raw JSON strings from other engines
   - ZERO feature calculation engines packaged
