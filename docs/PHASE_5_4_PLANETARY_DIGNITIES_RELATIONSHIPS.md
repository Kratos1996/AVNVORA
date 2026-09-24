# Phase 5.4 — Planetary Dignities + Planetary Relationships Engine

## 1. Scope & Objective
Phase 5.4 delivers a deterministic, offline-first, facts-only calculation engine for traditional Vedic planetary dignities and planetary relationships. It exposes stable, immutable results via `:aynvora-core` while keeping all mathematical calculations encapsulated within `:astro-engine`.

Calculations are strictly factual: no psychological assessments, predictions, fate interpretations, life advice, or LLM-generated texts are included.

---

## 2. Selected Tradition and Classical Rule Sources
- **Tradition**: Classical Parashara System (`PARASHARA_CLASSICAL_V1`).
- **Primary Source**: *Brihat Parashara Hora Shastra* (BPHS), Chapter 3 ("Planetary Characters and Descriptions"):
  - **Sign Ownership (Swakshetra)**: BPHS Ch. 3, Slokas 8–10 (Rule `ASTRO-R25`).
  - **Exaltation (Uchcha) & Debilitation (Neecha)**: BPHS Ch. 3, Slokas 49–51 (Rule `ASTRO-R26`).
  - **Moolatrikona & Boundary Degrees**: BPHS Ch. 3, Slokas 52–54 (Rule `ASTRO-R27`).
  - **Natural Relationships (Naisargika Maitri)**: BPHS Ch. 3, Slokas 55–58 (Rule `ASTRO-R28`).
  - **Temporary Relationships (Tatkalika Maitri)**: BPHS Ch. 3, Sloka 59 (Rule `ASTRO-R29`).
  - **Five-Fold Combined Relationships (Panchadha Maitri)**: BPHS Ch. 3, Sloka 60 (Rule `ASTRO-R30`).
  - **Residential Sign Dignity**: Compound relationship to sign dispositor/lord (Rule `ASTRO-R31`).

---

## 3. Implemented vs. Unsupported Rules

### Implemented Rules:
1. **Planetary Sign Ownership (`ASTRO-R25`)**:
   - Sun: Leo (index 4)
   - Moon: Cancer (index 3)
   - Mars: Aries (index 0), Scorpio (index 7)
   - Mercury: Gemini (index 2), Virgo (index 5)
   - Jupiter: Sagittarius (index 8), Pisces (index 11)
   - Venus: Taurus (index 1), Libra (index 6)
   - Saturn: Capricorn (index 9), Aquarius (index 10)
2. **Exaltation and Debilitation (`ASTRO-R26`)**:
   - Exact Parama Uchcha (deep exaltation) and Parama Neecha (deep debilitation) degrees.
   - Dual-dignity sign degree constraints:
     - Taurus (Moon): $[0^\circ, 3^\circ)$ is Exaltation, $[3^\circ, 30^\circ)$ is Moolatrikona.
     - Virgo (Mercury): $[0^\circ, 15^\circ)$ is Exaltation, $[15^\circ, 20^\circ)$ is Moolatrikona, $[20^\circ, 30^\circ)$ is Own Sign.
3. **Moolatrikona Degrees (`ASTRO-R27`)**:
   - Strict half-open degree intervals $[start, end)$:
     - Sun in Leo: $[0.0^\circ, 20.0^\circ)$
     - Moon in Taurus: $[3.0^\circ, 30.0^\circ)$
     - Mars in Aries: $[0.0^\circ, 12.0^\circ)$
     - Mercury in Virgo: $[15.0^\circ, 20.0^\circ)$
     - Jupiter in Sagittarius: $[0.0^\circ, 10.0^\circ)$
     - Venus in Libra: $[0.0^\circ, 15.0^\circ)$
     - Saturn in Aquarius: $[0.0^\circ, 20.0^\circ)$
4. **Natural Relationships (`ASTRO-R28`)**:
   - Complete $7 \times 7$ directional matrix reflecting intentional asymmetry (e.g. Moon considers Mercury a Friend, while Mercury considers Moon an Enemy).
5. **Temporary Relationships (`ASTRO-R29`)**:
   - Relative sign distance $H = (S_{\text{target}} - S_{\text{source}} + 12) \bmod 12 + 1$.
   - $\{2, 3, 4, 10, 11, 12\} \implies \text{FRIEND}$.
   - $\{1, 5, 6, 7, 8, 9\} \implies \text{ENEMY}$ (same sign $H=1$ is an Enemy).
6. **Five-fold Combined Relationships (`ASTRO-R30`)**:
   - Friend + Friend = `GREAT_FRIEND` (+2)
   - Friend + Enemy = `NEUTRAL` (0)
   - Neutral + Friend = `FRIEND` (+1)
   - Neutral + Enemy = `ENEMY` (-1)
   - Enemy + Friend = `NEUTRAL` (0)
   - Enemy + Enemy = `GREAT_ENEMY` (-2)
7. **Residential Compound Sign Dignity (`ASTRO-R31`)**:
   - Evaluates a planet's dignity in any sign: Exaltation $\to$ Debilitation $\to$ Moolatrikona $\to$ Own Sign $\to$ Compound relationship to Sign Lord (`GREAT_FRIEND_SIGN`, `FRIEND_SIGN`, `NEUTRAL_SIGN`, `ENEMY_SIGN`, `GREAT_ENEMY_SIGN`).
8. **Divisional Chart Integration**:
   - Full support for evaluating dignities and relationships across D1 and all 16 classical Parashara divisional charts (D2, D3, D4, D7, D9, D10, D12, D16, D20, D24, D27, D30, D40, D45, D60) without re-calculating ephemerides.

### Explicitly Excluded / Unsupported:
- **Rahu & Ketu Dignity/Relationships**: BPHS Chapter 3 defines classical dignities and friendships strictly for the 7 physical Grahas. Under `PARASHARA_CLASSICAL_V1`, nodes return `DignityType.NOT_APPLICABLE`, `NaturalRelationshipType.NOT_APPLICABLE`, and `TemporaryRelationshipType.NOT_APPLICABLE`.
- Non-Parashara traditions (e.g., Jaimini, Western, Tajika) or modern outer planets (Uranus, Neptune, Pluto) are explicitly unsupported and return deterministic `UnsupportedConfiguration`.

---

## 4. Public SDK API (`:aynvora-core`)
```kotlin
// Public API facade methods on AynvoraSdk:
suspend fun calculateDignities(
    request: ChartRequest,
    chart: DivisionalChart = DivisionalChart.D1,
): AynvoraResult<List<PlanetaryDignity>>

suspend fun calculateRelationships(
    request: ChartRequest,
    chart: DivisionalChart = DivisionalChart.D1,
): AynvoraResult<List<PlanetaryRelationship>>
```
In addition, `ChartResult` now automatically includes:
- `planetaryDignities: List<PlanetaryDignity>` (evaluated for D1 by default)
- `planetaryRelationships: List<PlanetaryRelationship>` (evaluated for D1 by default)

---

## 5. Architectural Quality and Decoupling
- **Dependency Direction**: UI / App $\to$ `:aynvora-core` $\to$ `:astro-engine`.
- **Localization Isolation**: All internal calculations return numerical indices, degrees, and locale-independent enums (`DignityType`, `NaturalRelationshipType`, etc.). No localized or presentation strings exist in `:astro-engine`.
- **Precision Preservation**: Calculations use unrounded 64-bit IEEE 754 floating-point coordinates and exact half-open degree intervals $[start, end)$.

---

## 6. Verification Summary
- Unit and property test suites added:
  - `astro-engine/src/commonTest/kotlin/com/aynvora/astro/dignity/DignityRelationshipTest.kt`
  - `aynvora-core/src/commonTest/kotlin/com/aynvora/core/DignityRelationshipSdkTest.kt`
- Clean builds and test execution:
  - `:astro-engine:jvmTest` — PASSED
  - `:aynvora-core:jvmTest` — PASSED
  - `:aynvora-data:jvmTest` — PASSED
  - `:design-system:jvmTest` — PASSED
  - `:aynvora-localization:jvmTest` — PASSED
  - `./gradlew test` — PASSED
  - `./gradlew :androidApp:assembleDebug` — PASSED
  - `./gradlew :desktopApp:packageDistributionForCurrentOS` — PASSED
  - `:astro-engine:compileKotlinIosArm64`, `:aynvora-core:compileKotlinIosArm64`, `:aynvora-localization:compileKotlinIosArm64` — PASSED
