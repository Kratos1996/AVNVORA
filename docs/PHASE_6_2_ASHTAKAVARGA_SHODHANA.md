# Phase 6.2 — Ashtakavarga Shodhana

## 1. Scope & Objective
Phase 6.2 implements deterministic, ruleset-versioned **Ashtakavarga Shodhana** (Classical Reductions) and its public SDK integration for the Parashara tradition (`PARASHARA_CLASSICAL_V1`).

Ashtakavarga Shodhana comprises the two classical mathematical reduction processes applied to raw Bhinnashtakavarga (BAV) tables:
1. **Trikona Shodhana** (Triplicity Reduction / Reduction to Triangles)
2. **Ekadhipatya Shodhana** (Dual-Ownership Reduction / Reduction to Single Lordship)
3. **Shodhita Sarvashtakavarga** (Reduced SAV Aggregation)

Strict facts-only principle: no transit predictions, auspiciousness scoring for human action, Pinda calculations (deferred to subsequent phases), or life predictions.

---

## 2. Source Audit Gate & Classical References

The mathematical reduction rules have been verified against authoritative classical treatises:

1. **Brihat Parashara Hora Shastra (BPHS)**:
   - Chapter 73: *Trikona Shodhana* (Reduction of Triplicities).
   - Chapter 74: *Ekadhipatya Shodhana* (Reduction for Dual Ownership).
   - English translation by R. Santhanam (Ranjan Publications, New Delhi, 1984).
   - English translation by Girish Chand Sharma (Sagar Publications, New Delhi, 1995).
2. **The Ashtakavarga System of Direction**:
   - Dr. B.V. Raman (Raman Publications / UBS Publishers, Bangalore, 14th ed.).
   - Chapter 4: "Trikona Sodhana or Reduction to Triangles".
   - Chapter 5: "Ekadhipatya Sodhana or Reduction to Single Lordship".
3. **Phaladeepika**:
   - Mantreswara, Chapter 24, Slokas 10–15.
4. **Jataka Parijata**:
   - Vaidyanatha Dikshita, Chapter 10.

### 2.1 Order of Application
The reduction order is strictly sequential:
1. **First**: Trikona Shodhana is applied to the raw 12-sign figures of each planet's BAV.
2. **Second**: Ekadhipatya Shodhana is applied strictly to the figures resulting from Trikona Shodhana.
*(BPHS Ch. 74 Sloka 1; Raman Ch. 5, p. 38)*

### 2.2 Eligible Tables & SAV Derivation
- Reductions are applied individually to each of the 7 classical planetary BAV tables (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn).
- In classical Parashara Ashtakavarga, reductions are **not** applied directly to the unreduced SAV. Instead, after the 7 BAV tables have undergone Trikona and Ekadhipatya Shodhana, **Shodhita Sarvashtakavarga** is derived by summing the 7 Shodhita BAV charts sign by sign:
  $$\text{ShodhitaSAV}[s] = \sum_{P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}} \text{ShodhitaBAV}_P[s]$$
*(BPHS Ch. 74; Raman Ch. 5, p. 43)*

---

## 3. Implemented Mathematical Reduction Rules

### 3.1 Trikona Shodhana (`ASTRO-R42`)
The 12 zodiac signs are partitioned into 4 triplicities (*Trikonas*), spaced 120° (trines) apart:
- **Agni (Fire)**: Mesha (0, Aries), Simha (4, Leo), Dhanus (8, Sagittarius)
- **Prithvi (Earth)**: Vrishabha (1, Taurus), Kanya (5, Virgo), Makara (9, Capricorn)
- **Vayu (Air)**: Mithuna (2, Gemini), Tula (6, Libra), Kumbha (10, Aquarius)
- **Jala (Water)**: Karka (3, Cancer), Vrishchika (7, Scorpio), Meena (11, Pisces)

For each triplicity $(s_0, s_1, s_2)$ with bindu counts $(v_0, v_1, v_2)$:
1. **All 3 zeros**: $(0, 0, 0) \to (0, 0, 0)$.
2. **Exactly 2 zeros**: The third non-zero figure is also reduced to 0 $\implies (0, 0, 0)$.
3. **Exactly 1 zero**: No reduction is made in this triplicity. The two non-zero figures remain unchanged.
4. **All 3 non-zero**:
   - *All three equal*: All three are reduced to 0: $(b, b, b) \to (0, 0, 0)$.
   - *Unequal figures*: Subtract the minimum figure $m = \min(v_0, v_1, v_2)$ from all three signs: $v_i' = v_i - m$.

### 3.2 Ekadhipatya Shodhana (`ASTRO-R43`)
Applied strictly to the figures resulting from Trikona Shodhana.

#### Dual-Ownership Pairs:
- Mars: Mesha (0, Aries) & Vrishchika (7, Scorpio)
- Venus: Vrishabha (1, Taurus) & Tula (6, Libra)
- Mercury: Mithuna (2, Gemini) & Kanya (5, Virgo)
- Jupiter: Dhanus (8, Sagittarius) & Meena (11, Pisces)
- Saturn: Makara (9, Capricorn) & Kumbha (10, Aquarius)

#### Single-Ownership Exemption:
- Moon: Karka (3, Cancer) — exempt from Ekadhipatya Shodhana.
- Sun: Simha (4, Leo) — exempt from Ekadhipatya Shodhana.
These two signs retain their figures exactly as left by Trikona Shodhana.

#### Planetary Occupancy Rule:
- A sign is occupied if it contains at least one of the 7 classical physical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn) in the birth chart (D1).
- In classical Parashara Ashtakavarga, Rahu and Ketu do NOT own signs and do NOT count as occupying planets. Lagna does not count as a planet.

#### Reduction Rules for Each Dual-Ownership Pair $(S_1, S_2)$ with Figures $(v_1, v_2)$:
1. **Rule 1 (Zero Exemption)**: If either sign has 0 bindus ($v_1 = 0$ or $v_2 = 0$), no reduction is made. Both retain their figures.
2. **Rule 2 (Both Signs Occupied)**: If both signs are occupied by at least one classical planet, no reduction is made. Both retain their figures.
3. **Rule 3 (Both Signs Unoccupied)**:
   - *(a) Equal figures ($v_1 == v_2$)*: Both are reduced to 0.
   - *(b) Unequal figures ($v_1 \ne v_2$)*: The larger figure is reduced to the smaller figure (both become $\min(v_1, v_2)$).
4. **Rule 4 (One Sign Occupied, One Sign Unoccupied)**:
   Let $v_{\text{occ}}$ be the occupied sign's figure, and $v_{\text{unocc}}$ the unoccupied sign's figure.
   The occupied sign always retains its figure ($v_{\text{occ}}$ unchanged).
   - *(a) If $v_{\text{occ}} \ge v_{\text{unocc}}$*: The unoccupied sign's figure is eliminated (becomes 0).
   - *(b) If $v_{\text{occ}} < v_{\text{unocc}}$*: The unoccupied sign's figure is made equal to the occupied figure ($v_{\text{unocc}} \leftarrow v_{\text{occ}}$).

### 3.3 Shodhita Sarvashtakavarga Aggregation (`ASTRO-R44`)
- Derived by summing the 7 classical planets' Shodhita BAV charts sign by sign:
  $$\text{ShodhitaSAV}[s] = \sum_{P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}} \text{ShodhitaBAV}_P[s]$$
- Monotonic non-increasing invariant:
  $$\text{Raw } 337 \ge \sum \text{TrikonaSAV} \ge \sum \text{ShodhitaSAV} \ge 0$$

---

## 4. Architecture & Data Structures

### 4.1 Internal Engine Models (`astro-engine`)
- `ShodhitaBhinnashtakavargaSignScore`: `rashiIndex`, `rashiName`, `rawBindus`, `trikonaReducedBindus`, `ekadhipatyaReducedBindus`, `shodhitaBindus`.
- `ShodhitaBhinnashtakavargaChart`: `targetBody`, `rulesetId`, `signScores`, `rawTotalBindus`, `trikonaTotalBindus`, `shodhitaTotalBindus`.
- `ShodhitaSarvashtakavargaSignScore`: `rashiIndex`, `rashiName`, `rawTotalBindus`, `trikonaTotalBindus`, `shodhitaTotalBindus`, `planetShodhitaBindus`.
- `ShodhitaSarvashtakavargaChart`: `rulesetId`, `signScores`, `grandTotalRawBindus` (337), `grandTotalTrikonaBindus`, `grandTotalShodhitaBindus`.
- `ShodhitaAshtakavargaResult`: `rulesetId`, `shodhitaBhinnashtakavarga`, `shodhitaSarvashtakavarga`, `completeness`, `unsupportedBodies`.
- Added backward-compatible `shodhana: ShodhitaAshtakavargaResult? = null` to `AshtakavargaResult`.
- Added `shodhitaAshtakavarga: ShodhitaAshtakavargaResult? = null` to `CalculationResult`.

### 4.2 Public SDK Models (`aynvora-core`)
- Public domain models mirror the engine types with decoupled architecture (`Rashi`, `CelestialBody`, `AshtakavargaCompleteness`).
- `ChartResult` contains `val shodhitaAshtakavarga: ShodhitaAshtakavargaResult? = null`.
- Public facade: `sdk.calculateShodhitaAshtakavarga(request): AynvoraResult<ShodhitaAshtakavargaResult>`.
- Domain capabilities exposed:
  - `TRIKONA_SHODHANA`
  - `EKADHIPATYA_SHODHANA`
  - `SHODHITA_ASHTAKAVARGA`

---

## 5. Independent Reference Tests & Validation

1. **Trikona Shodhana Boundary Tests**:
   - Equal values in Fire triplicity (all 3 reduce to 0).
   - Unequal non-zero values in Earth triplicity (subtract minimum).
   - Two equal and one smaller in Air triplicity (subtract minimum).
   - Two equal and one larger in Water triplicity (subtract minimum).
   - Exactly one zero (no reduction made in triplicity).
   - Exactly two zeros (third sign also becomes 0).
   - All three zeros (all remain 0).
2. **Ekadhipatya Shodhana Boundary Tests**:
   - Zero exemption rule: if either sign is 0, no reduction is made.
   - Both signs occupied: both retain their figures.
   - Both signs unoccupied and equal: both become 0.
   - Both signs unoccupied and unequal: larger figure becomes equal to smaller.
   - One occupied, one unoccupied ($v_{\text{occ}} \ge v_{\text{unocc}}$): unoccupied becomes 0.
   - One occupied, one unoccupied ($v_{\text{occ}} < v_{\text{unocc}}$): unoccupied becomes $v_{\text{occ}}$.
   - Cancer and Leo single-lordship exemption: strictly preserved.
   - Node non-occupancy: Rahu and Ketu do not count as occupying planets.
3. **Classical Worked Horoscope Reference Test (Dr. B.V. Raman Example)**:
   - Full chart from Raman's *The Ashtakavarga System of Direction*, Chapters 2–5.
   - Verified that Surya BAV raw figures `[4, 3, 2, 5, 5, 4, 4, 2, 3, 5, 7, 4]` (sum 48) reduce under Trikona Shodhana to `[1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 5, 2]` (sum 18) and under Ekadhipatya Shodhana to `[1, 0, 0, 3, 2, 1, 2, 0, 0, 2, 2, 2]` (sum 15), matching classical literature.
   - Verified monotonic non-increasing property for all 7 planets across all 12 signs ($raw \ge trikona \ge shodhita \ge 0$).
   - Verified sign-by-sign SAV equality against the column sum of the 7 Shodhita BAVs.
4. **Immutability & Non-mutation**:
   - Verified that raw Phase 6.1 BAV charts and SAV totals remain 100% intact before and after Shodhana.
5. **Determinism**:
   - Calling calculations repeatedly with identical inputs yields bit-for-bit identical results.

---

## 6. Verification Command Results

All build and test verification tasks executed cleanly with zero errors:

| Build Command | Status | Result / Execution Time |
| :--- | :--- | :--- |
| `./gradlew clean` | **SUCCESS** | Cleaned workspace (19 executed, 9 up-to-date, 1s) |
| `./gradlew :astro-engine:jvmTest --rerun-tasks` | **SUCCESS** | 144 unit & reference tests passed (5s) |
| `./gradlew :aynvora-core:jvmTest --rerun-tasks` | **SUCCESS** | 42 SDK tests passed (7s) |
| `./gradlew test` | **SUCCESS** | All multiplatform test suites passed (6s) |
| `./gradlew :androidApp:assembleDebug` | **SUCCESS** | Android Debug APK assembled successfully (3s) |
| `./gradlew :desktopApp:packageDistributionForCurrentOS` | **SUCCESS** | Desktop distribution packaged successfully (1s) |
| `./gradlew :astro-engine:compileKotlinIosArm64 :aynvora-core:compileKotlinIosArm64 :aynvora-localization:compileKotlinIosArm64` | **SUCCESS** | iOS ARM64 KMP compilation passed (6s) |

---

## 7. Known Limitations & Scope Boundaries

- **Ruleset**: Strictly `PARASHARA_CLASSICAL_V1`.
- **Rahu/Ketu**: Explicitly excluded from sign rulership and occupancy checks in classical 337-Bindu SAV.
- **Pinda Reductions**: Rashi Pinda, Graha Pinda, and Shodya Pinda are deferred to Phase 6.3.
- **Strict Stop Condition**: No Yogas, Dashas, Bhava Bala, transits, predictions, or UI features have been implemented.
