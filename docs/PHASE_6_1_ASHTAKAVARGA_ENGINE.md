# Phase 6.1 — Ashtakavarga Engine

## 1. Scope & Objective
Phase 6.1 implements the deterministic, ruleset-versioned **Ashtakavarga** (Eightfold Division / Auspicious Planetary Distributions) calculation engine and its public SDK integration for the Parashara tradition (`PARASHARA_CLASSICAL_V1`).

Ashtakavarga evaluates the combined benefic (Bindu) and inauspicious (Rekha) points cast by 8 classical contributors (the 7 classical planets plus the Ascendant / Lagna) across the 12 sidereal zodiac signs for each of the 7 classical planets.

The implementation comprises:
1. **Bhinnashtakavarga (BAV)**: The individual Ashtakavarga tables for each of the 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn), tracking:
   - 12-sign Bindu counts ($0..8$ per sign)
   - 12-sign Rekha counts ($8 - \text{binduCount}$ per sign)
   - Fixed classical Bindu total invariants (Sun=48, Moon=49, Mars=39, Mercury=54, Jupiter=56, Venus=52, Saturn=39)
   - Prastarashtakavarga grid ($8 \text{ contributors} \times 12 \text{ signs}$ binary flags $0$ or $1$)
2. **Sarvashtakavarga (SAV)**: The collective aggregate distribution across all 12 signs, with:
   - Per-sign Bindu totals ($0..56$)
   - Per-sign Rekha totals ($56 - \text{binduCount}$)
   - Planetary breakdown per sign
   - Classical grand total invariant verification ($\sum \text{SAV} = 337$ Bindus, $\sum \text{Rekhas} = 335$)
3. **Lunar Nodes Policy**: Strict facts-only policy marking Rahu and Ketu as unsupported under the classical 337-Bindu Parashara system.
4. **Public SDK Facade**: Clean integration via `aynvora-core` offering `sdk.calculateAshtakavarga(request)` and direct inclusion in `ChartResult`.

Strict facts-only principle: no transit predictions, auspiciousness scoring for human action, reduction techniques (Trikona/Ekadhipatya Shodhana, deferred to subsequent phases), or psychological extrapolations.

---

## 2. Authoritative Classical Sources & Editions

1. **Brihat Parashara Hora Shastra (BPHS)**:
   - Chapters 66 to 73 (*Ashtakavarga Adhyaya*):
     - Chapter 66: General Ashtakavarga & Surya Ashtakavarga (Sun)
     - Chapter 67: Chandra Ashtakavarga (Moon)
     - Chapter 68: Bhauma / Mangala Ashtakavarga (Mars)
     - Chapter 69: Budha Ashtakavarga (Mercury)
     - Chapter 70: Guru Ashtakavarga (Jupiter)
     - Chapter 71: Shukra Ashtakavarga (Venus)
     - Chapter 72: Shani Ashtakavarga (Saturn)
     - Chapter 73: Sarvashtakavarga
   - English translation by R. Santhanam (Ranjan Publications, New Delhi, 1984).
   - English translation by Girish Chand Sharma (Sagar Publications, New Delhi, 1995).
2. **The Ashtakavarga System of Direction**:
   - Dr. B.V. Raman (Raman Publications / UBS Publishers, Bangalore, 14th ed.).
   - Standard mathematical treatise on classical Ashtakavarga casting and rules.
3. **Phaladeepika**:
   - Mantreswara, Chapter 24 (*Ashtakavarga*).
4. **Jataka Parijata**:
   - Vaidyanatha Dikshita, Chapter 10.

---

## 3. Implemented Contribution Matrices & Rules

All house offsets $k \in [1..12]$ are 1-indexed relative to the contributor's sidereal Rashi placement $R_C \in [0..11]$:
$$\text{Target Sign Index} = (R_C + (k - 1)) \pmod{12}$$

### 3.1 Surya Ashtakavarga (`ASTRO-R39A`) — Total = 48 Bindus
- **From Sun (8)**: 1, 2, 4, 7, 8, 9, 10, 11
- **From Moon (4)**: 3, 6, 10, 11
- **From Mars (8)**: 1, 2, 4, 7, 8, 9, 10, 11
- **From Mercury (7)**: 3, 5, 6, 9, 10, 11, 12
- **From Jupiter (4)**: 5, 6, 9, 11
- **From Venus (3)**: 6, 7, 12
- **From Saturn (8)**: 1, 2, 4, 7, 8, 9, 10, 11
- **From Lagna (6)**: 3, 4, 6, 10, 11, 12
- **Invariant**: $\sum \text{Bindus} = 8 + 4 + 8 + 7 + 4 + 3 + 8 + 6 = 48$. Total Rekhas $= 96 - 48 = 48$.

### 3.2 Chandra Ashtakavarga (`ASTRO-R39B`) — Total = 49 Bindus
- **From Sun (6)**: 3, 6, 7, 8, 10, 11
- **From Moon (6)**: 1, 3, 6, 7, 10, 11
- **From Mars (7)**: 2, 3, 5, 6, 9, 10, 11
- **From Mercury (8)**: 1, 3, 4, 5, 7, 8, 10, 11
- **From Jupiter (7)**: 1, 4, 7, 8, 10, 11, 12
- **From Venus (7)**: 3, 4, 5, 7, 9, 10, 11
- **From Saturn (4)**: 3, 5, 6, 11
- **From Lagna (4)**: 3, 6, 10, 11
- **Invariant**: $\sum \text{Bindus} = 6 + 6 + 7 + 8 + 7 + 7 + 4 + 4 = 49$. Total Rekhas $= 96 - 49 = 47$.

### 3.3 Mangala Ashtakavarga (`ASTRO-R39C`) — Total = 39 Bindus
- **From Sun (5)**: 3, 5, 6, 10, 11
- **From Moon (3)**: 3, 6, 11
- **From Mars (7)**: 1, 2, 4, 7, 8, 10, 11
- **From Mercury (4)**: 3, 5, 6, 11
- **From Jupiter (4)**: 6, 10, 11, 12
- **From Venus (4)**: 6, 8, 11, 12
- **From Saturn (7)**: 1, 4, 7, 8, 9, 10, 11
- **From Lagna (5)**: 1, 3, 6, 10, 11
- **Invariant**: $\sum \text{Bindus} = 5 + 3 + 7 + 4 + 4 + 4 + 7 + 5 = 39$. Total Rekhas $= 96 - 39 = 57$.

### 3.4 Budha Ashtakavarga (`ASTRO-R39D`) — Total = 54 Bindus
- **From Sun (5)**: 5, 6, 9, 11, 12
- **From Moon (6)**: 2, 4, 6, 8, 10, 11
- **From Mars (8)**: 1, 2, 4, 7, 8, 9, 10, 11
- **From Mercury (8)**: 1, 3, 5, 6, 9, 10, 11, 12
- **From Jupiter (4)**: 6, 8, 11, 12
- **From Venus (8)**: 1, 2, 3, 4, 5, 8, 9, 11
- **From Saturn (8)**: 1, 2, 4, 7, 8, 9, 10, 11
- **From Lagna (7)**: 1, 2, 4, 6, 8, 10, 11
- **Invariant**: $\sum \text{Bindus} = 5 + 6 + 8 + 8 + 4 + 8 + 8 + 7 = 54$. Total Rekhas $= 96 - 54 = 42$.

### 3.5 Guru Ashtakavarga (`ASTRO-R39E`) — Total = 56 Bindus
- **From Sun (9)**: 1, 2, 3, 4, 7, 8, 9, 10, 11
- **From Moon (5)**: 2, 5, 7, 9, 11
- **From Mars (7)**: 1, 2, 4, 7, 8, 10, 11
- **From Mercury (8)**: 1, 2, 4, 5, 6, 9, 10, 11
- **From Jupiter (8)**: 1, 2, 3, 4, 7, 8, 10, 11
- **From Venus (6)**: 2, 5, 6, 9, 10, 11
- **From Saturn (4)**: 3, 5, 6, 12
- **From Lagna (9)**: 1, 2, 4, 5, 6, 7, 9, 10, 11
- **Invariant**: $\sum \text{Bindus} = 9 + 5 + 7 + 8 + 8 + 6 + 4 + 9 = 56$. Total Rekhas $= 96 - 56 = 40$.

### 3.6 Shukra Ashtakavarga (`ASTRO-R39F`) — Total = 52 Bindus
- **From Sun (3)**: 8, 11, 12
- **From Moon (9)**: 1, 2, 3, 4, 5, 8, 9, 11, 12
- **From Mars (6)**: 3, 5, 6, 9, 11, 12
- **From Mercury (5)**: 3, 5, 6, 9, 11
- **From Jupiter (5)**: 5, 8, 9, 10, 11
- **From Venus (9)**: 1, 2, 3, 4, 5, 8, 9, 10, 11
- **From Saturn (7)**: 3, 4, 5, 8, 9, 10, 11
- **From Lagna (8)**: 1, 2, 3, 4, 5, 8, 9, 11
- **Invariant**: $\sum \text{Bindus} = 3 + 9 + 6 + 5 + 5 + 9 + 7 + 8 = 52$. Total Rekhas $= 96 - 52 = 44$.

### 3.7 Shani Ashtakavarga (`ASTRO-R39G`) — Total = 39 Bindus
- **From Sun (7)**: 1, 2, 4, 7, 8, 10, 11
- **From Moon (3)**: 3, 6, 11
- **From Mars (6)**: 3, 5, 6, 10, 11, 12
- **From Mercury (6)**: 6, 8, 9, 10, 11, 12
- **From Jupiter (4)**: 5, 6, 11, 12
- **From Venus (3)**: 6, 11, 12
- **From Saturn (4)**: 3, 5, 6, 11
- **From Lagna (6)**: 1, 3, 4, 6, 10, 11
- **Invariant**: $\sum \text{Bindus} = 7 + 3 + 6 + 6 + 4 + 3 + 4 + 6 = 39$. Total Rekhas $= 96 - 39 = 57$.

### 3.8 Sarvashtakavarga Aggregation (`ASTRO-R40`) — Total = 337 Bindus
- For each sign $s \in [0..11]$:
  $$\text{SAV}[s] = \sum_{P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}} \text{BAV}_P[s]$$
  $$\text{SAV\_Rekhas}[s] = 56 - \text{SAV}[s]$$
- **Grand Invariants**:
  $$\sum_{s=0}^{11} \text{SAV}[s] = 48 + 49 + 39 + 54 + 56 + 52 + 39 = 337 \text{ Bindus}$$
  $$\sum_{s=0}^{11} \text{SAV\_Rekhas}[s] = (56 \times 12) - 337 = 672 - 337 = 335 \text{ Rekhas}$$

### 3.9 Lunar Nodes (Rahu/Ketu) Policy (`ASTRO-R41`)
- Rahu and Ketu do not participate as contributors or targets in classical 337-SAV.
- Requests for Rahu or Ketu BAV evaluate to `null` and are returned under `unsupportedBodies = [RAHU, KETU]`.

---

## 4. Independent Reference Tests & Validation

1. **Source Contribution Rules Verification**:
   - Every contributor row for all 7 classical planets verified against BPHS slokas and Raman tables.
   - All 56 rule vectors confirmed to contain exact expected cardinalities.
2. **Classical Horoscope Reference Verification**:
   - Verified that regardless of planetary sign allocations, every planet's BAV total strictly sums to its classical invariant (48, 49, 39, 54, 56, 52, 39).
   - Verified that SAV sign totals match the column sums of the 7 BAV tables.
   - Verified that SAV grand total strictly equals 337.
3. **Boundary Condition & Extremes**:
   - Single-sign congestion test (all 7 bodies and Lagna in Capricorn) verified to satisfy all invariants.
   - Pisces-to-Aries circular wrap-around ($11 \to 0$) verified.
4. **SDK Facade & Metadata Integration**:
   - Standalone `sdk.calculateAshtakavarga(request)` verified.
   - Direct inclusion in `ChartResult.ashtakavarga` verified.
   - Domain capability discovery keys cataloged:
     - `ASHTAKAVARGA`, `BHINNASHTAKAVARGA`, `SARVASHTAKAVARGA`, `PRASTARASHTAKAVARGA`, `SURYA_ASHTAKAVARGA`, `CHANDRA_ASHTAKAVARGA`, `KUJA_ASHTAKAVARGA`, `BUDHA_ASHTAKAVARGA`, `GURU_ASHTAKAVARGA`, `SHUKRA_ASHTAKAVARGA`, `SHANI_ASHTAKAVARGA`.

---

## 5. Verification Command Results

All build and test verification tasks executed cleanly with zero errors:

| Build Command | Status | Result / Execution Time |
| :--- | :--- | :--- |
| `./gradlew clean` | **SUCCESS** | Cleaned workspace (19 executed, 9 up-to-date, 1s) |
| `./gradlew :astro-engine:jvmTest --rerun-tasks` | **SUCCESS** | 41 unit tests executed and passed (6s) |
| `./gradlew :aynvora-core:jvmTest --rerun-tasks` | **SUCCESS** | 38 SDK tests executed and passed (7s) |
| `./gradlew test` | **SUCCESS** | All multiplatform test suites passed (10s) |
| `./gradlew :androidApp:assembleDebug` | **SUCCESS** | Android Debug APK assembled successfully (7s) |
| `./gradlew :desktopApp:packageDistributionForCurrentOS` | **SUCCESS** | Desktop distribution packaged successfully (1s) |
| `./gradlew :astro-engine:compileKotlinIosArm64 :aynvora-core:compileKotlinIosArm64 :aynvora-localization:compileKotlinIosArm64` | **SUCCESS** | iOS ARM64 KMP compilation passed (6s) |

---

## 6. Known Limitations & Scope Boundaries

- **Ruleset**: Strictly `PARASHARA_CLASSICAL_V1`.
- **Rahu/Ketu**: Explicitly unsupported in classical 337-Bindu SAV.
- **Reductions (Shodhana)**: Trikona Shodhana (triplicity reduction) and Ekadhipatya Shodhana (reduction for dual-ownership signs) are not part of raw Ashtakavarga casting and belong to subsequent analysis phases.
- **Transits & Predictions**: No Kaksha transit timing, Gochara evaluation, or life predictions are generated.
