# Phase 5.5 — Shadbala Engine Foundation

## 1. Scope & Objective
Phase 5.5 implements a deterministic, offline-first, facts-only calculation foundation for classical **Shadbala** (Six-Fold Planetary Strengths) according to the Parashara tradition (`PARASHARA_CLASSICAL_V1`).

Shadbala comprises six primary strength components:
1. **Sthana Bala** (Positional Strength)
2. **Dig Bala** (Directional Strength)
3. **Kala Bala** (Temporal Strength)
4. **Chesta Bala** (Motional Strength)
5. **Naisargika Bala** (Natural Strength)
6. **Drik Bala** (Aspectual Strength)

All mathematical calculations are encapsulated within `:astro-engine`. Stable, immutable, decoupled public domain models and facade methods are exposed through `:aynvora-core`.

Strict facts-only principle: no predictions, auspiciousness ratings, psychological interpretations, remedial advice, or natural-language conclusions are produced.

---

## 2. Selected Tradition and Classical Rule Sources
- **Tradition**: Classical Parashara System (`PARASHARA_CLASSICAL_V1`).
- **Primary Source Editions**:
  - *Brihat Parashara Hora Shastra* (BPHS), Vol. 1, Chapters 27–28 (Evaluation of Strengths), translated by R. Santhanam (Ranjan Publications, New Delhi).
  - *Brihat Parashara Hora Shastra*, Chapters 27–28, translated by Girish Chand Sharma (Sagar Publications).
  - *Graha and Bhava Balas*, Dr. B.V. Raman (UBS Publishers / Raman Publications), standard reference work on classical mathematical Shadbala.
- **Rule IDs Cataloged in `docs/09_ASTROLOGY_RULES.md`**:
  - `ASTRO-R32`: Naisargika Bala (Natural Strength, BPHS Ch. 28, slokas 13–14)
  - `ASTRO-R33`: Dig Bala (Directional Strength, BPHS Ch. 28, slokas 7–8)
  - `ASTRO-R34`: Sthana Bala (Positional Strength, BPHS Ch. 28, slokas 2–12)
    - `ASTRO-R34A`: Uchcha Bala (Exaltation Strength)
    - `ASTRO-R34B`: Saptavargaja Bala (Divisional Friendship Strength across 7 Vargas)
    - `ASTRO-R34C`: Ojhayugmarasyamsa Bala (Odd/Even Sign & Navamsa Placement)
    - `ASTRO-R34D`: Kendra Bala (Angular House Placement)
    - `ASTRO-R34E`: Drekkana Bala (Decanate Gender Placement)
  - `ASTRO-R35`: Chesta Bala Foundation (Motional Strength, BPHS Ch. 28, slokas 19–21)
  - `ASTRO-R36`: Kala Bala Foundation (Temporal Strength, BPHS Ch. 28, slokas 15–18)
  - `ASTRO-R37`: Drik Bala Foundation (Aspectual Strength, BPHS Ch. 28, slokas 22–24)
  - `ASTRO-R38`: Shadbala Completeness and Auditability Gate

---

## 3. Implemented vs. Deferred Components

### Fully Implemented Components:
1. **Naisargika Bala (`ASTRO-R32`)**:
   - Luminosity-proportional fixed Virupas:
     - Sun: $60.0$ Virupas ($1.0$ Rupa, Rank 1)
     - Moon: $60 \times \frac{6}{7} \approx 51.42857$ Virupas ($0.85714$ Rupas, Rank 2)
     - Venus: $60 \times \frac{5}{7} \approx 42.85714$ Virupas ($0.71429$ Rupas, Rank 3)
     - Jupiter: $60 \times \frac{4}{7} \approx 34.28571$ Virupas ($0.57143$ Rupas, Rank 4)
     - Mercury: $60 \times \frac{3}{7} \approx 25.71429$ Virupas ($0.42857$ Rupas, Rank 5)
     - Mars: $60 \times \frac{2}{7} \approx 17.14286$ Virupas ($0.28571$ Rupas, Rank 6)
     - Saturn: $60 \times \frac{1}{7} \approx 8.57143$ Virupas ($0.14286$ Rupas, Rank 7)
   - Total classical sum across all 7 planets: $240.0$ Virupas ($4.0$ Rupas).

2. **Dig Bala (`ASTRO-R33`)**:
   - Directional strength derived from angular distance to the classical power point:
     - East (1st House Cusp / Lagna): Mercury, Jupiter
     - South (10th House Cusp / Midheaven): Sun, Mars
     - West (7th House Cusp / Descendant): Saturn
     - North (4th House Cusp / Nadir): Moon, Venus
   - Zero point is at $180^\circ$ opposition to the powerful point.
   - Arc distance $\theta \in [0.0^\circ, 180.0^\circ]$ from zero point:
     $$\text{Virupas} = \frac{\theta}{3.0} \in [0.0, 60.0]$$
     $$\text{Rupas} = \frac{\text{Virupas}}{60.0} \in [0.0, 1.0]$$

3. **Sthana Bala (`ASTRO-R34`)** (Full 5-subcomponent classical sum):
   - **Uchcha Bala (`ASTRO-R34A`)**: Distance $\Delta$ from Parama Neecha (deep debilitation point) divided by $3.0$:
     $$\text{Virupas} = \frac{\Delta}{3.0} \in [0.0, 60.0]$$
     (At Parama Uchcha, $\Delta = 180^\circ \implies 60.0$ Virupas; at Parama Neecha, $\Delta = 0^\circ \implies 0.0$ Virupas).
   - **Saptavargaja Bala (`ASTRO-R34B`)**: Evaluates compound relationship dignity across the 7 classical Saptavargas (D1 Rashi, D2 Hora, D3 Drekkana, D7 Saptamsa, D9 Navamsa, D12 Dwadasamsa, D30 Trimsamsa):
     - Moolatrikona / Exaltation: $45.0$ Virupas
     - Own Sign (Swakshetra): $30.0$ Virupas
     - Great Friend (Adhi Mitra): $20.0$ Virupas
     - Friend (Mitra): $15.0$ Virupas
     - Neutral (Sama): $10.0$ Virupas
     - Enemy (Shatru): $4.0$ Virupas
     - Great Enemy (Adhi Shatru): $2.0$ Virupas
     - Debilitation: $0.0$ Virupas
   - **Ojhayugmarasyamsa Bala (`ASTRO-R34C`)**:
     - Female planets (Moon, Venus) in even Rashi ($15.0$ Virupas) and even Navamsa ($15.0$ Virupas).
     - Male & neutral planets (Sun, Mars, Jupiter, Mercury, Saturn) in odd Rashi ($15.0$ Virupas) and odd Navamsa ($15.0$ Virupas).
   - **Kendra Bala (`ASTRO-R34D`)**:
     - Kendra houses (1, 4, 7, 10): $60.0$ Virupas.
     - Panaphara houses (2, 5, 8, 11): $30.0$ Virupas.
     - Apoklima houses (3, 6, 9, 12): $15.0$ Virupas.
   - **Drekkana Bala (`ASTRO-R34E`)**:
     - 1st Decanate $[0^\circ, 10^\circ)$: Male planets (Sun, Mars, Jupiter) get $15.0$ Virupas.
     - 2nd Decanate $[10^\circ, 20^\circ)$: Neutral planets (Mercury, Saturn) get $15.0$ Virupas.
     - 3rd Decanate $[20^\circ, 30^\circ)$: Female planets (Moon, Venus) get $15.0$ Virupas.
   - Total Sthana Bala Virupas = $\sum \text{subcomponents}$. Total Rupas = $\frac{\text{Total Virupas}}{60.0}$.

### Foundational / Partially Deferred Components:
4. **Kala Bala (`ASTRO-R36`)**:
   - Implemented foundation: **Paksha Bala** calculated from elongation $\Delta = (\lambda_{\text{Moon}} - \lambda_{\text{Sun}} + 360^\circ) \bmod 360^\circ$:
     - Benefics: $\text{Virupas} = \frac{\Delta}{3.0}$ (Shukla) or $\frac{360^\circ - \Delta}{3.0}$ (Krishna).
     - Malefics: $60.0 - \text{Benefic Virupas}$.
   - Deferred subcomponents:
     - `NATHONNATHA_BALA` (Diurnal/Nocturnal strength requiring exact local apparent sunrise/sunset epochs)
     - `TRIBHAGA_BALA` (Day/night three-part partitioning)
     - `VARSHA_MASA_DINA_HORA_BALA` (Lords of Year, Month, Day, and Planetary Hour, requiring Ahargana epoch tracking)
     - `AYANA_BALA` (Equinoctial declination strength requiring 3D equatorial declination)
     - `YUDDHA_BALA` (Planetary war adjustment)

5. **Chesta Bala (`ASTRO-R35`)**:
   - Implemented foundation: Retrograde indicator, apparent daily longitudinal motion tracking ($d\lambda/dt$).
   - Deferred subcomponents:
     - `CHESTA_KENDRA_REDUCTION` (8-stage motion categorization using mean astronomical longitude difference and speed ratio matrices).

6. **Drik Bala (`ASTRO-R37`)**:
   - Implemented foundation: Planetary aspect capability integration.
   - Deferred subcomponents:
     - `GRAHA_DRISHTI_VIRUPA_REDUCTION` (Parashara quarter-aspect strength matrix).

---

## 4. Completeness and Auditability Gate (`ASTRO-R38`)
To prevent silent approximations or partial figures being misinterpreted as total classical Shadbala scores, a strict gate is enforced:
- If ANY required component is partial or deferred:
  - `isComplete = false`
  - `totalVirupas = null`
  - `totalRupas = null`
  - `completeness = ShadbalaCompleteness.PARTIAL_FOUNDATION`
  - `deferredComponents` lists the exact subcomponents deferred.
- **Rahu & Ketu**: Classical Shadbala in BPHS applies strictly to the 7 physical Grahas. Under `PARASHARA_CLASSICAL_V1`:
  - `completeness = ShadbalaCompleteness.UNSUPPORTED`
  - `isComplete = false`
  - `totalVirupas = null`, `totalRupas = null`
  - All component virupas are $0.0$.

---

## 5. Units, Precision, and Mathematical Policies
- **Units**:
  - Primary unit: **Virupas** (classical points, equivalent to Shashtiamsas of strength).
  - Secondary unit: **Rupas** ($1\text{ Rupa} = 60\text{ Virupas}$).
- **Precision**: 64-bit IEEE 754 double precision (`Double`). No premature rounding in internal calculators.
- **Angular Normalization**: Shortest circular arc distances strictly within $[0^\circ, 180^\circ]$ for Dig Bala, and $[0^\circ, 360^\circ)$ for longitudinal calculations.

---

## 6. Public SDK API (`:aynvora-core`)
```kotlin
// Public SDK Facade method on AynvoraSdk:
suspend fun calculateShadbala(
    request: ChartRequest,
): AynvoraResult<List<PlanetaryShadbala>>

// Direct inclusion in ChartResult:
val chartResult = sdk.calculateChart(chartRequest)
if (chartResult is AynvoraResult.Success) {
    val shadbala: List<PlanetaryShadbala> = chartResult.value.shadbala
}
```

### Models Exposed in `:aynvora-core`:
- `enum class ShadbalaCompleteness { COMPLETE, PARTIAL_FOUNDATION, UNSUPPORTED }`
- `data class SthanaBala(uchchaBalaVirupas, saptavargajaBalaVirupas, ojhayugmarasyamsaBalaVirupas, kendraBalaVirupas, drekkanaBalaVirupas, totalVirupas, totalRupas, isEvaluated)`
- `data class DigBala(powerfulPointDegrees, zeroPointDegrees, arcDegrees, virupas, rupas, isEvaluated)`
- `data class NaisargikaBala(virupas, rupas, rank, isEvaluated)`
- `data class KalaBala(pakshaBalaVirupas, isEvaluated, deferredSubcomponents)`
- `data class ChestaBala(isRetrograde, dailyMotionDegrees, virupas, isEvaluated, deferredSubcomponents)`
- `data class DrikBala(virupas, isEvaluated, deferredSubcomponents)`
- `data class PlanetaryShadbala(body, sthanaBala, digBala, naisargikaBala, kalaBala, chestaBala, drikBala, completeness, isComplete, totalVirupas, totalRupas, deferredComponents, rulesetId)`

---

## 7. Tests & Independent Validation

### Test Suites Added:
1. `astro-engine/src/commonTest/kotlin/com/aynvora/astro/shadbala/ShadbalaCalculatorTest.kt`:
   - **Reference Tests**:
     - Naisargika Bala exact Virupa ratios for all 7 planets ($60, 51\frac{3}{7}, 42\frac{6}{7}, 34\frac{2}{7}, 25\frac{5}{7}, 17\frac{1}{7}, 8\frac{4}{7}$) matching BPHS Ch. 28, slokas 13–14 and Raman.
     - Dig Bala exact powerful point yield ($60.0$ Virupas) and zero point yield ($0.0$ Virupas).
     - Uchcha Bala exact Parama Uchcha ($60.0$ Virupas) and Parama Neecha ($0.0$ Virupas).
     - Kendra Bala house tier scores ($60.0, 30.0, 15.0$ Virupas).
     - Drekkana Bala decanate gender scores ($15.0$ Virupas).
     - Ojhayugmarasyamsa Bala odd/even Rashi/Navamsa scores ($15.0, 30.0$ Virupas).
   - **Consistency Tests**:
     - Saptavargaja Bala evaluation across the 7 divisional charts.
     - Sthana Bala total exact summation of 5 subcomponents.
     - Completeness gate validation: `totalVirupas == null` when components are deferred.
     - Rahu / Ketu unsupported gate.
     - Determinism and repeatability across multiple runs.
2. `aynvora-core/src/commonTest/kotlin/com/aynvora/core/ShadbalaSdkTest.kt`:
   - Public SDK `calculateShadbala` and `calculateChart` integration.
   - Metadata capability discovery (`SHADBALA`, `STHANA_BALA`, `DIG_BALA`, `NAISARGIKA_BALA`, etc.).
   - Error handling and invalid input rejection.

---

## 8. Verification Results
- `./gradlew :astro-engine:jvmTest`: Passed (115 tests, 0 failed).
- `./gradlew :aynvora-core:jvmTest`: Passed (34 tests, 0 failed).
- Full `./gradlew test`: Passed (all module test suites green).
- `./gradlew :androidApp:assembleDebug`: Verified compile and assemble.
- Multiplatform iOS compilation: Verified.

---

## 9. Known Limitations & Next Steps
- **Limitations**: Total Shadbala is intentionally withheld (`null`) until Phase 5.6 or later, when full astronomical time models for Ayana Bala (3D declination) and mean motion ratio reductions for Chesta Bala are implemented.
- **Next Phase**: Phase 5.6 (Complete Temporal & Motional Bala or Ashtakavarga Engine Foundation).
