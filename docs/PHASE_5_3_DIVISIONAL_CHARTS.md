# Phase 5.3 — Divisional Charts (Varga Engine Foundation)

## 1. Scope
Phase 5.3 establishes a production-quality, deterministic, offline-first calculation framework for Vedic divisional charts (Shodashavargas) within `:astro-engine` and exposes them through `:aynvora-core`.

The framework calculates pure astronomical and astrological **FACTS** only. Astrological interpretations, predictions, character profiling, and marital/career readings are strictly omitted.

---

## 2. Varga Architecture
The divisional chart architecture follows a clean, decoupled pipeline:
```
Planetary & Lagna Sidereal Positions (Phase 4 & 5.1)
                       ↓
               Varga Engine Facade
                       ↓
          Varga Registry & Metadata
                       ↓
   Profile & Ruleset (PARASHARA_CLASSICAL_V1)
                       ↓
       Varga Calculation Strategy (D1..D60)
                       ↓
          Divisional Factual Result
                       ↓
         Public SDK (:aynvora-core)
```
- **Single Coordinated Pass**: Calculates astronomical ephemeris coordinates once; all requested Vargas are derived without recalculating astronomy.
- **Zero Redundant Work**: Consumers can compute single charts (e.g. D9), subsets (e.g. D1, D9, D10), or all 16 charts on demand.

---

## 3. Varga Registry
The `VargaRegistry` object centralizes metadata, rule identifiers, traditional names, and division counts for all 16 classical Shodashavargas.

---

## 4. Varga Metadata
Each Varga contains immutable factual metadata (`VargaMetadata`):
- `chart`: `DivisionalChart` (enum)
- `divisionNumber`: Int (1, 2, 3, 4, 7, 9, 10, 12, 16, 20, 24, 27, 30, 40, 45, 60)
- `traditionalName`: String ("Rashi", "Hora", "Drekkana", etc. as internal technical identifiers)
- `ruleId`: String ("ASTRO-R09" through "ASTRO-R24")
- `referenceSource`: Classical text citation
- `isSupported`: Boolean flag
- `description`: Technical summary of chart division

---

## 5. Calculation Contract
The generic calculation contract is defined in `VargaEngine`:
```kotlin
interface VargaEngine {
    fun calculate(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        chart: DivisionalChart,
        profile: VargaProfile = VargaProfile.DEFAULT,
    ): VargaChartResult

    fun calculateMultiple(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        charts: Set<DivisionalChart>,
        profile: VargaProfile = VargaProfile.DEFAULT,
    ): Map<DivisionalChart, VargaChartResult>
}
```
Each individual chart implements `VargaCalculationStrategy`:
```kotlin
interface VargaCalculationStrategy {
    val chart: DivisionalChart
    fun calculate(siderealLongitude: Double): VargaDivisionResult
}
```

---

## 6. Profile / Ruleset Model
Different astrological traditions use variant divisional schemes. The framework models this cleanly via `VargaProfile`:
- `rulesetId`: String (Default: `"PARASHARA_CLASSICAL_V1"`)
- `tradition`: String ("Brihat Parashara Hora Shastra")
- `rulesetVersion`: String ("1.0")
- `supportedCharts`: Set<DivisionalChart> (Set of allowed charts under the ruleset)

If a request specifies an unknown ruleset or an unsupported chart, the engine deterministically throws `UnsupportedOperationException`, which maps to `AynvoraResult.Failure.UnsupportedConfiguration` at the SDK boundary.

---

## 7. D1 — Rashi Chart (ASTRO-R09)
- **Divisions**: 1 division of $30.0^\circ$ per sign.
- **Rule**: Direct projection. $\text{resultingRashi} = S$. Reuses base Nirayana coordinates.

---

## 8. D2 — Hora Chart (ASTRO-R10)
- **Divisions**: 2 divisions of $15.0^\circ$ per sign.
- **Tradition**: Brihat Parashara Hora Shastra Ch. 6, slokas 5-6 (Parashara Sun/Moon Hora).
- **Odd Signs** ($S \bmod 2 == 0$): $[0^\circ, 15^\circ) \implies$ Leo (4, Sun); $[15^\circ, 30^\circ) \implies$ Cancer (3, Moon).
- **Even Signs** ($S \bmod 2 == 1$): $[0^\circ, 15^\circ) \implies$ Cancer (3, Moon); $[15^\circ, 30^\circ) \implies$ Leo (4, Sun).

---

## 9. D3 — Drekkana Chart (ASTRO-R11)
- **Divisions**: 3 divisions of $10.0^\circ$ per sign.
- **Tradition**: BPHS Ch. 6, slokas 7-8.
- **Rule**: 1st part $\implies$ sign itself ($S$); 2nd part $\implies$ 5th sign ($(S + 4) \bmod 12$); 3rd part $\implies$ 9th sign ($(S + 8) \bmod 12$).
- **Formula**: $\text{resultingRashi} = (S + 4 \times k) \bmod 12$ for $k \in \{0, 1, 2\}$.

---

## 10. D4 — Chaturthamsa / Turyamsa (ASTRO-R12)
- **Divisions**: 4 divisions of $7.5^\circ$ ($7^\circ 30'$) per sign.
- **Tradition**: BPHS Ch. 6, sloka 9.
- **Rule**: Kendras from sign (1st, 4th, 7th, 10th).
- **Formula**: $\text{resultingRashi} = (S + 3 \times k) \bmod 12$ for $k \in \{0, 1, 2, 3\}$.

---

## 11. D7 — Saptamsa Chart (ASTRO-R13)
- **Divisions**: 7 divisions of $30.0^\circ / 7 \approx 4.285714^\circ$ ($4^\circ 17' 8.57''$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 10-11.
- **Odd Signs**: Counted from sign itself $\implies (S + k) \bmod 12$.
- **Even Signs**: Counted from 7th sign from it $\implies (S + 6 + k) \bmod 12$.

---

## 12. D9 — Navamsa Chart (ASTRO-R14)
- **Divisions**: 9 divisions of $3^\circ 20' = 3.333333^\circ$ per sign (108 Navamsas across the zodiac, matching the 108 Nakshatra Padas).
- **Tradition**: BPHS Ch. 6, slokas 12-14.
- **Elemental Starting Signs**:
  - Fiery signs (Aries, Leo, Sagittarius; $S \in \{0, 4, 8\}$): starts from Aries (0).
  - Earthy signs (Taurus, Virgo, Capricorn; $S \in \{1, 5, 9\}$): starts from Capricorn (9).
  - Airy signs (Gemini, Libra, Aquarius; $S \in \{2, 6, 10\}$): starts from Libra (6).
  - Watery signs (Cancer, Scorpio, Pisces; $S \in \{3, 7, 11\}$): starts from Cancer (3).
- **Mathematical Identity**: $\text{navamsaGlobalIndex} = \lfloor \lambda_{\text{sidereal}} / (40.0 / 12.0) \rfloor \bmod 12$.

---

## 13. D10 — Dasamsa Chart (ASTRO-R15)
- **Divisions**: 10 divisions of $3.0^\circ$ per sign.
- **Tradition**: BPHS Ch. 6, slokas 15-16.
- **Odd Signs**: Counted from sign itself $\implies (S + k) \bmod 12$.
- **Even Signs**: Counted from 9th sign from it $\implies (S + 8 + k) \bmod 12$.

---

## 14. D12 — Dwadasamsa Chart (ASTRO-R16)
- **Divisions**: 12 divisions of $2.5^\circ$ ($2^\circ 30'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 17-18.
- **Rule**: Direct sequential progression from sign itself $\implies (S + k) \bmod 12$ for $k \in [0..11]$.

---

## 15. D16 — Shodasamsa / Kalamsa (ASTRO-R17)
- **Divisions**: 16 divisions of $1.875^\circ$ ($1^\circ 52' 30''$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 19-21.
- **Mobility Starting Signs**:
  - Movable (Chara, $S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed (Sthira, $S \in \{1, 4, 7, 10\}$): starts from Leo (4).
  - Dual (Dwisvabhava, $S \in \{2, 5, 8, 11\}$): starts from Sagittarius (8).
- **Formula**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$.

---

## 16. D20 — Vimsamsa Chart (ASTRO-R18)
- **Divisions**: 20 divisions of $1.5^\circ$ ($1^\circ 30'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 22-23.
- **Mobility Starting Signs**:
  - Movable ($S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed ($S \in \{1, 4, 7, 10\}$): starts from Sagittarius (8).
  - Dual ($S \in \{2, 5, 8, 11\}$): starts from Leo (4).
- **Formula**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$.

---

## 17. D24 — Chaturvimsamsa / Siddhamsa (ASTRO-R19)
- **Divisions**: 24 divisions of $1.25^\circ$ ($1^\circ 15'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 24-25.
- **Odd Signs**: starts from Leo (4) $\implies (4 + k) \bmod 12$.
- **Even Signs**: starts from Cancer (3) $\implies (3 + k) \bmod 12$.

---

## 18. D27 — Bhamsa / Nakshatramsa / Saptavimsamsa (ASTRO-R20)
- **Divisions**: 27 divisions of $30.0^\circ / 27 = 10.0^\circ / 9 \approx 1.111111^\circ$ ($1^\circ 6' 40''$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 26-27.
- **Triplicity Starting Signs**:
  - Fiery ($S \in \{0, 4, 8\}$): starts from Aries (0).
  - Earthy ($S \in \{1, 5, 9\}$): starts from Cancer (3).
  - Airy ($S \in \{2, 6, 10\}$): starts from Libra (6).
  - Watery ($S \in \{3, 7, 11\}$): starts from Capricorn (9).
- **Formula**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$.

---

## 19. D30 — Trimsamsa Chart (ASTRO-R21)
- **Divisions**: 5 UNEQUAL degree spans ruled by the 5 non-luminary planets (Mars, Saturn, Jupiter, Mercury, Venus).
- **Tradition**: BPHS Ch. 6, slokas 28-31.
- **Odd Signs**:
  - $[0.0^\circ, 5.0^\circ)$ ($5^\circ$ span): Mars $\implies$ Aries (0).
  - $[5.0^\circ, 10.0^\circ)$ ($5^\circ$ span): Saturn $\implies$ Aquarius (10).
  - $[10.0^\circ, 18.0^\circ)$ ($8^\circ$ span): Jupiter $\implies$ Sagittarius (8).
  - $[18.0^\circ, 25.0^\circ)$ ($7^\circ$ span): Mercury $\implies$ Gemini (2).
  - $[25.0^\circ, 30.0^\circ)$ ($5^\circ$ span): Venus $\implies$ Libra (6).
- **Even Signs**:
  - $[0.0^\circ, 5.0^\circ)$ ($5^\circ$ span): Venus $\implies$ Taurus (1).
  - $[5.0^\circ, 12.0^\circ)$ ($7^\circ$ span): Mercury $\implies$ Virgo (5).
  - $[12.0^\circ, 20.0^\circ)$ ($8^\circ$ span): Jupiter $\implies$ Pisces (11).
  - $[20.0^\circ, 25.0^\circ)$ ($5^\circ$ span): Saturn $\implies$ Capricorn (9).
  - $[25.0^\circ, 30.0^\circ)$ ($5^\circ$ span): Mars $\implies$ Scorpio (7).

---

## 20. D40 — Khavedamsa / Swavedamsa (ASTRO-R22)
- **Divisions**: 40 divisions of $0.75^\circ$ ($45'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 32-33.
- **Odd Signs**: starts from Aries (0) $\implies (0 + k) \bmod 12$.
- **Even Signs**: starts from Libra (6) $\implies (6 + k) \bmod 12$.

---

## 21. D45 — Akshavedamsa Chart (ASTRO-R23)
- **Divisions**: 45 divisions of $30.0^\circ / 45 = 2/3^\circ \approx 0.666667^\circ$ ($40'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 34-35.
- **Mobility Starting Signs**:
  - Movable ($S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed ($S \in \{1, 4, 7, 10\}$): starts from Leo (4).
  - Dual ($S \in \{2, 5, 8, 11\}$): starts from Sagittarius (8).
- **Formula**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$.

---

## 22. D60 — Shashtiamsa Chart (ASTRO-R24)
- **Divisions**: 60 divisions of $0.5^\circ$ ($30'$) per sign.
- **Tradition**: BPHS Ch. 6, slokas 36-42 & Dr. B. V. Raman, *A Manual of Hindu Astrology* Ch. 9 / Jataka Parijata Ch. 1.
- **Sign Mapping**: Cyclic counting from sign itself $\implies (S + k) \bmod 12$ for $k = \lfloor \text{degreeInSign} / 0.5 \rfloor \in [0..59]$.
- **Boundary Sensitivity**: Strict IEEE 754 64-bit precision preservation with half-open intervals $[k \times 0.5^\circ, (k + 1) \times 0.5^\circ)$.

---

## 23. Implemented vs. Unsupported
- **Implemented**: All 16 classical Shodashavargas (D1, D2, D3, D4, D7, D9, D10, D12, D16, D20, D24, D27, D30, D40, D45, D60) are fully implemented under the authoritative `PARASHARA_CLASSICAL_V1` profile.
- **Unsupported**: Variant traditions (e.g. Parivritti Hora, Somnath Drekkana, Jaimini D30) are not configured by default and deterministically produce `AynvoraResult.Failure.UnsupportedConfiguration` without silent fallback or approximation.

---

## 24. Rule Sources
All rules are grounded directly in classical literature:
- *Brihat Parashara Hora Shastra* (BPHS), Chapter 6: The Sixteen Vargas.
- Dr. B. V. Raman, *A Manual of Hindu Astrology*, Chapter 9: The Sixteen Vargas.
- *Jataka Parijata*, Adhyaya 1.
- *Phaladeepika*, Chapter 3.

---

## 25. Precision
- Internal coordinate transformations operate on raw 64-bit IEEE 754 `Double` values.
- Degrees are never rounded prematurely or converted to display strings during calculations.

---

## 26. Boundaries
All divisional spans use strict half-open intervals:
$$[start, end)$$
At $0.0^\circ$, division index is $0$. At exact boundaries (e.g. $10.0^\circ$ in D3), the position advances deterministically to the next division. Clamping safely prevents array overflow for edge values such as $29.999999^\circ$ and $360.0^\circ$.

---

## 27. Rahu / Ketu Handling
Rahu and Ketu maintain exact $180^\circ$ astronomical opposition. In symmetric harmonic charts such as D1 and D9, this guarantees that Rahu and Ketu reside in exact opposite signs ($6$ signs apart).

---

## 28. Lagna in Varga
Lagna sidereal longitude is passed into the same strategy without code duplication. `VargaPosition` marks Lagna explicitly with `isLagna = true` and `bodyId = null`.

---

## 29. Public SDK
Exposed through `:aynvora-core`:
- `sdk.calculateDivisionalChart(request, chart)`: Single Varga evaluation.
- `sdk.calculateDivisionalCharts(request, charts)`: Multiple Varga coordinated evaluation.
- `CalculationConfig.requestedDivisionalCharts`: Pre-configured evaluation embedded in `ChartResult.divisionalCharts`.

---

## 30. Localization Separation
The calculation engine emits strongly typed Kotlin enums (`DivisionalChart`, `Rashi`, `CelestialBody`) and mathematical doubles. Localization layers (`:aynvora-localization`) format presentation text independently.

---

## 31. Testing
- Dedicated test suite `VargaEngineTest` (30+ comprehensive test methods in `:astro-engine`).
- SDK integration suite `VargaSdkIntegrationTest` in `:aynvora-core`.
- Property tests verifying that all resulting signs fall in $0..11$ and longitudes in $[0^\circ, 360^\circ)$.
- Deterministic repeatability tests (identical results over multiple runs).
- Regression tests confirming Phase 4, 5.1, and 5.2 integrity.

---

## 32. Performance
- Planetary ephemeris is solved only once per birth instant.
- Divisional calculations perform fast, zero-allocation modular arithmetic.

---

## 33. Known Limitations
- Shashtiamsa (D60) deity names and auspicious/inauspicious classifications are deferred to future interpretation/dignity modules.
- Non-Parashari traditional variants (e.g. Parivritti, Somnath) are designated unsupported under `PARASHARA_CLASSICAL_V1`.

---

## 34. Deferred Work
- Divisional chart dignities, varga aspectation, and varga-based strength (Vimsopaka Bala).
- Astrological interpretations (e.g. D9 marriage readings, D10 career readings).

---

## 35. Next Phase
Phase 5.4 — Astrological Dignities & Planetary Relationships (Exaltation, Debilitation, Moolatrikona, Own Sign, Natural & Temporal Friends/Enemies).
