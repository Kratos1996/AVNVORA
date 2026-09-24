# Phase 5.6 — Temporal & Motional Bala Completion

## 1. Scope & Objective
Phase 5.6 completes the deferred mathematical calculations of classical **Shadbala** (Six-Fold Planetary Strengths) for the Parashara tradition (`PARASHARA_CLASSICAL_V1`).

Building upon the Sthana Bala, Dig Bala, and Naisargika Bala foundations established in Phase 5.5, Phase 5.6 completes:
1. **Kala Bala** (Temporal Strength) with all classical subcomponents:
   - Nathonnatha Bala (Diurnal / Nocturnal Strength)
   - Paksha Bala (Fortnight / Lunar Phase Strength)
   - Tribhaga Bala (Three-part Day/Night Lord Strength)
   - Vara Bala (Weekday Lord Strength)
   - Hora Bala (Planetary Hour Lord Strength)
   - Masa Bala (Month Lord Strength)
   - Varsha Bala (Year Lord Strength)
   - Ayana Bala (Equatorial Declination Solstitial Strength)
   - Yuddha Bala (Planetary War Strength / Reduction)
2. **Chesta Bala** (Motional Strength):
   - Classical motion-based calculation per BPHS Ch. 28, Sloka 21
   - Sun Chesta Bala = Sun Ayana Bala
   - Moon Chesta Bala = Moon Paksha Bala
   - Tara Grahas (Mars, Mercury, Jupiter, Venus, Saturn): Chesta Kendra elongation arc reduction ($K / 3.0 \in [0.0, 60.0]$ Virupas), with Retrograde (`VAKRA`) receiving full $60.0$ Virupas
   - Motion state categorization (Vakra, Vikala, Chara, Manda, Sama)
3. **Drik Bala** (Aspectual Strength):
   - Classical aspect curve reduction across angular separations $[30^\circ, 300^\circ]$
   - Special full aspects: Mars (4th & 8th houses), Jupiter (5th & 9th houses), Saturn (3rd & 10th houses)
   - Signed aspect contributions (Benefics positive, Malefics negative) with 1/4th factor reduction
4. **Shadbala Completeness Gate**:
   - For all 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn), all 6 components are fully evaluated.
   - Status transitions from `PARTIAL_FOUNDATION` to `COMPLETE` (`isComplete = true`).
   - `totalVirupas` equals the exact IEEE 754 64-bit sum of all 6 components.
   - `totalRupas` equals `totalVirupas / 60.0`.
   - Lunar nodes (Rahu and Ketu) remain strictly `UNSUPPORTED` per classical Parashara doctrine (`isComplete = false`, `totalVirupas = null`).

All logic is 100% offline, deterministic, and facts-only (no predictions, no psychological labels, no remediation).

---

## 2. Source-and-Input Audit

### Primary Classical Sources
1. **Brihat Parashara Hora Shastra (BPHS)**:
   - Chapters 27 & 28: *Evaluation of Strengths of Planets (Shadbala)*.
   - English translation by R. Santhanam (Ranjan Publications, New Delhi, 1984).
   - English translation by Girish Chand Sharma (Sagar Publications, New Delhi, 1995).
2. **Graha and Bhava Balas**:
   - Dr. B.V. Raman (Raman Publications / UBS Publishers, Bangalore/New Delhi, 13th ed.).
   - Standard mathematical manual for classical Graha Bala calculation.

### Input Mapping & Astronomical Primitives
| Input / Parameter | Source Layer | Units / Coordinate System | Reference / Convention |
| :--- | :--- | :--- | :--- |
| `tropicalLongitude` | `:astro-engine` (`BodyPosition`) | Degrees $[0.0^\circ, 360.0^\circ)$ | Equatorial/Ecliptic J2000.0 apparent |
| `siderealLongitude` | `:astro-engine` (`BodyPosition`) | Degrees $[0.0^\circ, 360.0^\circ)$ | Lahiri Chitra Paksha Ayanamsa |
| `isRetrograde` | `:astro-engine` (`BodyPosition`) | Boolean (`true` if $d\lambda/dt < 0$) | Finite difference longitudinal rate |
| `longitudeSpeed` | `:astro-engine` (`BodyPosition`) | Degrees / day | Apparent motion rate |
| `obliquityDegrees` | `:astro-engine` (`LagnaResult`) | Degrees ($\approx 23.439^\circ$) | Meeus Mean Obliquity of Ecliptic $\epsilon$ |
| `declination` $\delta$ | Calculated (`ShadbalaCalculator`) | Degrees $[-23.5^\circ, +23.5^\circ]$ | $\arcsin(\sin\epsilon \sin\lambda_{\text{tropical}})$ |
| `julianDay` | `:astro-engine` (`AstroEngine`) | Days (UT) | Meeus Gregorian algorithm |
| `birthHour` | `:aynvora-core` (`BirthData`) | Integer $[0, 23]$ | Local solar / standard civil hour |
| `houseCusps` | `:astro-engine` (`HousesResult`) | Degrees $[0.0^\circ, 360.0^\circ)$ | Whole Sign / Equal house boundaries |

---

## 3. Mathematical Formulations

### 3.1 Kala Bala Subcomponents (`ASTRO-R36`)

1. **Nathonnatha Bala (Diurnal / Nocturnal Strength)** (BPHS Ch. 28, Slokas 15–16):
   - Sun distance from Midheaven (10th cusp) vs Nadir (4th cusp).
   - Distance $\theta \in [0^\circ, 180^\circ]$ from midnight/nadir:
     $$\text{fraction} = \frac{\theta}{180.0}$$
   - Midnight planets (Moon, Mars, Saturn):
     $$\text{Virupas} = (1.0 - \text{fraction}) \times 60.0$$
   - Midday planets (Sun, Jupiter, Venus):
     $$\text{Virupas} = \text{fraction} \times 60.0$$
   - Mercury: Always receives full $60.0$ Virupas.

2. **Paksha Bala (Lunar Fortnight Strength)** (BPHS Ch. 28, Slokas 16–17):
   - Elongation $\Delta = (\lambda_{\text{Moon}} - \lambda_{\text{Sun}} + 360^\circ) \bmod 360^\circ$.
   - Benefic base:
     $$\text{Virupas}_{\text{benefic}} = \begin{cases} \frac{\Delta}{3.0} & \text{if } \Delta \le 180^\circ \\ \frac{360^\circ - \Delta}{3.0} & \text{if } \Delta > 180^\circ \end{cases}$$
   - Benefics (Jupiter, Venus, benefic Mercury, Moon): receive $\text{Virupas}_{\text{benefic}}$.
   - Malefics (Sun, Mars, Saturn): receive $60.0 - \text{Virupas}_{\text{benefic}}$.

3. **Tribhaga Bala (Three-part Day/Night Strength)** (BPHS Ch. 28, Sloka 17):
   - Day is divided into 3 equal portions:
     - Part 1 (Houses 12, 11): Mercury receives $60.0$ Virupas.
     - Part 2 (Houses 10, 9): Sun receives $60.0$ Virupas.
     - Part 3 (Houses 8, 7): Saturn receives $60.0$ Virupas.
   - Night is divided into 3 equal portions:
     - Part 1 (Houses 6, 5): Moon receives $60.0$ Virupas.
     - Part 2 (Houses 4, 3): Venus receives $60.0$ Virupas.
     - Part 3 (Houses 2, 1): Mars receives $60.0$ Virupas.
   - Jupiter: Always receives full $60.0$ Virupas as Guru/preceptor of devas.

4. **Vara Bala (Weekday Lord Strength)** (BPHS Ch. 28, Sloka 18):
   - Day lord calculated from Julian Day number:
     $$\text{dayOfWeek} = (\lfloor JD + 1.5 \rfloor) \bmod 7$$
     where $0 = \text{Sunday (Sun)}$, $1 = \text{Monday (Moon)}$, $2 = \text{Tuesday (Mars)}$, $3 = \text{Wednesday (Mercury)}$, $4 = \text{Thursday (Jupiter)}$, $5 = \text{Friday (Venus)}$, $6 = \text{Saturday (Saturn)}$.
   - The lord of the day of birth receives $45.0$ Virupas ($0.75$ Rupas). Other bodies receive $0.0$.

5. **Hora Bala (Planetary Hour Lord Strength)** (BPHS Ch. 28, Sloka 18):
   - Classical Chaldean order from sunrise: Sun, Venus, Mercury, Moon, Saturn, Jupiter, Mars.
   - The planet ruling the birth hora receives $60.0$ Virupas ($1.0$ Rupa). Other bodies receive $0.0$.

6. **Masa Bala (Solar Month Lord Strength)** (BPHS Ch. 28, Sloka 18):
   - Lord of the Sun's current sidereal Rashi sign receives $30.0$ Virupas ($0.5$ Rupa). Other bodies receive $0.0$.

7. **Varsha Bala (Solar Year Lord Strength)** (BPHS Ch. 28, Sloka 18):
   - Planetary lord of the first day of the Jovian/solar year receives $15.0$ Virupas ($0.25$ Rupa). Other bodies receive $0.0$.

8. **Ayana Bala (Declination Strength)** (BPHS Ch. 28, Slokas 19–20, Raman Ch. 4):
   - Declination $\delta$ calculated from tropical longitude $\lambda_{\text{trop}}$ and mean obliquity $\epsilon$:
     $$\delta = \arcsin(\sin\epsilon \sin\lambda_{\text{trop}})$$
   - Northern-favouring planets (Sun, Mars, Jupiter, Venus):
     $$\text{Virupas} = (24.0 + \delta) \times 1.25 \in [0.0, 60.0]$$
   - Southern-favouring planets (Moon, Saturn):
     $$\text{Virupas} = (24.0 - \delta) \times 1.25 \in [0.0, 60.0]$$
   - Mercury (Neutral): $30.0$ Virupas.

9. **Yuddha Bala (Planetary War Strength)** (BPHS Ch. 28, Sloka 21):
   - Applicable only to the five Tara grahas (Mars, Mercury, Jupiter, Venus, Saturn) when angular separation $< 1.0^\circ$.
   - The victor gains the difference between their Sthana/Dig/Chesta Balas, while the defeated planet loses that amount. When no planetary war occurs, $0.0$ Virupas.

- **Total Kala Bala**: Exact sum of the 9 subcomponents. $\text{Rupas} = \frac{\text{Total Virupas}}{60.0}$.

---

### 3.2 Chesta Bala (`ASTRO-R35`)

Per *Brihat Parashara Hora Shastra*, Chapter 28, Sloka 21 and Dr. B.V. Raman, *Graha and Bhava Balas*, Chapter 5:
- **Sun Chesta Bala**: Identical to its **Ayana Bala** ($\text{Virupas}_{\text{Ayana}}$).
- **Moon Chesta Bala**: Identical to its **Paksha Bala** ($\text{Virupas}_{\text{Paksha}}$).
- **Tara Grahas** (Mars, Mercury, Jupiter, Venus, Saturn):
  - If retrograde (`isRetrograde == true`): Receives full $60.0$ Virupas (`VAKRA` state).
  - Otherwise, calculated via **Chesta Kendra** elongation arc $K$:
    $$K = |\lambda_{\text{planet}} - \lambda_{\text{Sun}}|$$
    $$\text{arc} = \begin{cases} K & \text{if } K \le 180^\circ \\ 360^\circ - K & \text{if } K > 180^\circ \end{cases}$$
    $$\text{Virupas} = \frac{\text{arc}}{3.0} \in [0.0, 60.0]$$
    $$\text{Rupas} = \frac{\text{Virupas}}{60.0} \in [0.0, 1.0]$$
- **Motion Categories**:
  - `VAKRA`: Retrograde motion ($60.0$ Virupas)
  - `VIKALA`: Stationary motion ($15.0$ Virupas)
  - `MANDA`: Slower than average daily speed ($15.0$ Virupas)
  - `SAMA`: Average daily speed ($30.0$ Virupas)
  - `CHARA`: Faster than average daily speed ($45.0$ Virupas)

---

### 3.3 Drik Bala (`ASTRO-R37`)

Per *Brihat Parashara Hora Shastra*, Chapter 28, Slokas 22–24 and Dr. B.V. Raman, Chapter 7:
- For every aspecting body $A$ on aspected body $B$, separation $\theta = (\lambda_B - \lambda_A + 360^\circ) \bmod 360^\circ$.
- Base aspect strength:
  - $\theta \in [30^\circ, 60^\circ)$: $\frac{\theta - 30^\circ}{2.0}$
  - $\theta \in [60^\circ, 90^\circ)$: $15.0 + (\theta - 60^\circ)$
  - $\theta \in [90^\circ, 120^\circ)$: $45.0 - \frac{\theta - 90^\circ}{2.0}$
  - $\theta \in [120^\circ, 150^\circ)$: $30.0 - (\theta - 120^\circ)$
  - $\theta \in [150^\circ, 180^\circ)$: $(\theta - 150^\circ) \times 2.0$ ($60.0$ Virupas at exact opposition)
  - $\theta \in [180^\circ, 300^\circ)$: $\frac{300^\circ - \theta}{2.0}$
  - Outside $[30^\circ, 300^\circ]$: $0.0$ Virupas.
- Special full aspect additions:
  - **Mars**: 4th house ($[90^\circ, 120^\circ]$) and 8th house ($[210^\circ, 240^\circ]$) $\implies +15.0$ Virupas.
  - **Jupiter**: 5th house ($[120^\circ, 150^\circ]$) and 9th house ($[240^\circ, 270^\circ]$) $\implies +30.0$ Virupas.
  - **Saturn**: 3rd house ($[60^\circ, 90^\circ]$) and 10th house ($[270^\circ, 300^\circ]$) $\implies +45.0$ Virupas.
- Directional / Benefic Weighting:
  - Benefics (Jupiter, Venus, benefic Mercury, Moon): contribute $+ \text{Virupas}$ (Drishti).
  - Malefics (Sun, Mars, Saturn): contribute $- \text{Virupas}$ (Vedha / adverse aspect).
- Reduction factor:
  $$\text{Drik Bala Virupas} = \frac{\text{beneficAspectVirupas} - \text{maleficAspectVirupas}}{4.0}$$
  $$\text{Drik Bala Rupas} = \frac{\text{Virupas}}{60.0}$$

---

### 3.4 Completeness Gate (`ASTRO-R38`)

- **Gate Conditions**:
  - The 7 classical grahas (`SUN`, `MOON`, `MARS`, `MERCURY`, `JUPITER`, `VENUS`, `SATURN`) have all 6 Shadbala components fully evaluated with non-null, finite values.
  - Result:
    - `completeness = ShadbalaCompleteness.COMPLETE`
    - `isComplete = true`
    - `totalVirupas = sthana + dig + kala + chesta + naisargika + drik`
    - `totalRupas = totalVirupas / 60.0`
    - `unsupportedComponents = emptyList()`
- **Lunar Nodes Policy**:
  - Rahu and Ketu are mathematical shadow intersections (Chhaya Grahas) without physical mass, disc, or latitude/declination.
  - Per classical Parashara doctrine, Shadbala is not evaluated for Rahu and Ketu.
  - Result:
    - `completeness = ShadbalaCompleteness.UNSUPPORTED`
    - `isComplete = false`
    - `totalVirupas = null`
    - `totalRupas = null`
    - `unsupportedComponents = ["SHADBALA_NOT_APPLICABLE_FOR_NODES"]`

---

## 4. Source-Based Reference Tests

The test suite in `ShadbalaCalculatorTest.kt` verifies every formula and boundary condition against classical reference cases:

1. **Ayana Bala**:
   - Sun at Tropical $90^\circ$ (Summer Solstice, $\delta = +23.44^\circ$): $(24 + 23.44) \times 1.25 = 59.3$ Virupas.
   - Sun at Tropical $270^\circ$ (Winter Solstice, $\delta = -23.44^\circ$): $(24 - 23.44) \times 1.25 = 0.7$ Virupas.
   - Moon at Northern Solstice: receives minimum Ayana Bala ($0.7$ Virupas).
   - Moon at Southern Solstice: receives maximum Ayana Bala ($59.3$ Virupas).
   - Mercury: consistently yields $30.0$ Virupas.

2. **Nathonnatha Bala**:
   - Sun at Midday (10th house cusp): Sun, Jupiter, Venus receive $60.0$ Virupas; Moon, Mars, Saturn receive $0.0$ Virupas.
   - Midnight (4th house cusp): Moon, Mars, Saturn receive $60.0$ Virupas; Sun, Jupiter, Venus receive $0.0$ Virupas.
   - Mercury: consistently yields $60.0$ Virupas regardless of time.

3. **Tribhaga Bala**:
   - 1st part of day: Mercury receives $60.0$ Virupas.
   - 2nd part of day: Sun receives $60.0$ Virupas.
   - 3rd part of day: Saturn receives $60.0$ Virupas.
   - 1st part of night: Moon receives $60.0$ Virupas.
   - 2nd part of night: Venus receives $60.0$ Virupas.
   - 3rd part of night: Mars receives $60.0$ Virupas.
   - Jupiter: consistently receives $60.0$ Virupas in all parts.

4. **Chesta Bala**:
   - Retrograde planets (`isRetrograde = true`): verified to yield $60.0$ Virupas (`VAKRA`).
   - Direct planets: verified to yield elongation $/ 3.0$ Virupas.
   - Sun: verified to equal its Ayana Bala.
   - Moon: verified to equal its Paksha Bala.

5. **Drik Bala**:
   - Exact opposition ($180^\circ$): verified to receive $60.0$ Virupas.
   - Special full aspects: Mars 4th/8th, Jupiter 5th/9th, Saturn 3rd/10th verified.
   - Benefic vs Malefic signed weighting with $1/4$ factor verified.

6. **Completeness Gate**:
   - Classical 7 bodies: verified to produce `COMPLETE`, `isComplete == true`, `totalVirupas != null`, `totalRupas == totalVirupas / 60.0`.
   - Nodes (Rahu/Ketu): verified to produce `UNSUPPORTED`, `isComplete == false`, `totalVirupas == null`.

---

## 5. Public SDK & Metadata Integration

- Internal models in `:astro-engine` (`KalaBalaPosition`, `ChestaBalaPosition`, `DrikBalaPosition`) map cleanly to public models in `:aynvora-core` (`KalaBala`, `ChestaBala`, `DrikBala`).
- `EngineMetadata.supportedDomains` catalogs all newly supported Phase 5.6 domains:
  - `KALA_BALA`, `CHESTA_BALA`, `DRIK_BALA`, `NATHONNATHA_BALA`, `PAKSHA_BALA`, `TRIBHAGA_BALA`, `VARA_BALA`, `HORA_BALA`, `MASA_BALA`, `VARSHA_BALA`, `AYANA_BALA`, `YUDDHA_BALA`, `CHESTA_KENDRA`, `DRISHTI_BALA`.
- `AynvoraResult.Success` guarantees non-null `totalVirupas` for the 7 classical planets.

---

## 6. Verification Status

All commands executed and verified with zero errors and zero warnings:
- `./gradlew clean`: SUCCESS
- `./gradlew :astro-engine:jvmTest`: 34 tests passed, 0 failed
- `./gradlew :aynvora-core:jvmTest`: 21 tests passed, 0 failed
- `./gradlew test`: All unit test suites passed across all subprojects
- `./gradlew :androidApp:assembleDebug`: Android build passed
- `./gradlew :desktopApp:packageDistributionForCurrentOS`: Desktop packaging passed
- `./gradlew :astro-engine:compileKotlinIosArm64 :aynvora-core:compileKotlinIosArm64 :aynvora-localization:compileKotlinIosArm64`: iOS compilation passed
