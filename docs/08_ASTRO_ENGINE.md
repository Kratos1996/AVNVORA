# AYNVORA Astro Engine
Version: 1.1 (Phase 4 Real Astronomical Engine)

## Purpose
Provide mathematically deterministic, testable astronomical and astrological calculations independent of UI, persistence, and external networks.

## Architecture
The Astro Engine operates as a standalone Kotlin Multiplatform calculation pipeline:
```
Time Normalization (Civil Time + Timezone -> UTC -> Julian Day)
        ↓
Astronomical Time Parameter (Julian centuries T from J2000.0)
        ↓
Planetary Coordinate Solvers (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn, Rahu, Ketu)
        ↓
Ayanamsa Calculator (Official Lahiri Chitra Paksha / Tropical)
        ↓
Tropical to Sidereal Coordinate Transformation
        ↓
Zodiac Allocator (12 Rashis, 27 Nakshatras, 4 Padas per Nakshatra)
        ↓
Motion & Velocity Analyzer (Apparent Retrograde State)
```

## Calculation Domains Implemented
- **Time Normalization**: Date/time conversion to UTC instant across global IANA timezones and DST rules.
- **Julian Day**: Meeus Gregorian algorithm and Julian centuries $T$.
- **Geocentric Apparent Ecliptic Longitudes**:
  - Sun (Meeus Ch. 25: equation of center, nutation, aberration).
  - Moon (Meeus Ch. 47 / ELP-2000/82: 30+ periodic perturbation terms, evection, variation, annual equation).
  - Planets: Mercury, Venus, Mars, Jupiter, Saturn (Simon et al. 1994 Keplerian elements, secular variations, light-time correction, Jupiter-Saturn Great Inequality).
  - Lunar Nodes: Mean Rahu and Ketu with exact $180^\circ$ mathematical opposition.
- **Ayanamsa**: Official Government of India / Indian Astronomical Ephemeris Lahiri (Chitra Paksha) formulation.
- **Sidereal Conversion**: Angular normalization strictly within $[0^\circ, 360^\circ)$.
- **Rashi**: 12 equal $30^\circ$ signs with degrees within sign.
- **Nakshatra & Pada**: 27 Nakshatras ($13^\circ 20'$) and 4 Padas ($3^\circ 20'$) with deterministic boundary arithmetic.
- **Retrograde State**: Finite difference apparent longitudinal velocity $d\lambda/dt < 0$.
- **Lagna & Houses (Phase 5.1)**: Spherical horizon-ecliptic ascendant calculation, whole sign and equal house divisions.
- **Aspects & Planet States (Phase 5.2)**: Shortest circular pairwise separation, classical aspects, combustion thresholds.
- **Divisional Charts / Vargas (Phase 5.3)**: Classical Shodashavargas (D1, D2, D3, D4, D7, D9, D10, D12, D16, D20, D24, D27, D30, D40, D45, D60) per Brihat Parashara Hora Shastra Ch. 6, preserving 64-bit IEEE 754 precision and $[start, end)$ boundary intervals.
- **Planetary Dignities & Relationships (Phase 5.4)**: Classical Parashara dignity classifications (Exaltation with deep degrees, Debilitation with deep degrees, Moolatrikona half-open intervals, Swakshetra Own Sign, and Residential Sign Lord Dignities). Natural directional relationships (Naisargika Maitri, BPHS Ch. 3 slokas 55-58), temporary relative sign relationships (Tatkalika Maitri, BPHS Ch. 3 sloka 59), and five-fold combined relationships (Panchadha Maitri, BPHS Ch. 3 sloka 60) across D1 and all supported divisional charts. Nodes (Rahu/Ketu) explicitly marked NOT_APPLICABLE.
- **Shadbala Engine Foundation (Phase 5.5)**: Deterministic evaluation of classical strengths per *Brihat Parashara Hora Shastra*, Chapters 27–28 (`PARASHARA_CLASSICAL_V1`).
  - **Naisargika Bala (Natural Strength, ASTRO-R32)**: Permanent luminosity-proportional Virupas (Sun=60, Moon=51 3/7, Venus=42 6/7, Jupiter=34 2/7, Mercury=25 5/7, Mars=17 1/7, Saturn=8 4/7; Total = 240 Virupas / 4 Rupas).
  - **Dig Bala (Directional Strength, ASTRO-R33)**: Angular distance from zero point to powerful point (Lagna for Jupiter/Mercury, 10th for Sun/Mars, 7th for Saturn, 4th for Moon/Venus), $\text{Virupas} = \text{arc} / 3.0 \in [0.0, 60.0]$.
  - **Sthana Bala (Positional Strength, ASTRO-R34)**: Sum of 5 classical subcomponents:
    - *Uchcha Bala (ASTRO-R34A)*: Distance from deep debilitation point divided by $3.0 \in [0.0, 60.0]$ Virupas.
    - *Saptavargaja Bala (ASTRO-R34B)*: Friendship dignity weights across the 7 classical Vargas (D1, D2, D3, D7, D9, D12, D30).
    - *Ojhayugmarasyamsa Bala (ASTRO-R34C)*: Odd/even sign and navamsa placement ($15.0$ Virupas per matching sign/navamsa).
    - *Kendra Bala (ASTRO-R34D)*: Angular placement (Kendra=60, Panaphara=30, Apoklima=15 Virupas).
    - *Drekkana Bala (ASTRO-R34E)*: Decanate placement ($15.0$ Virupas for matching decanate/gender).
- **Temporal & Motional Bala Completion (Phase 5.6)**: Complete classical calculations for remaining Shadbala components:
  - **Kala Bala (Temporal Strength, ASTRO-R36)**: Sum of 9 classical subcomponents:
    - *Nathonnatha Bala*: Midday/midnight diurnal-nocturnal strength based on Sun distance from Nadir/Midheaven.
    - *Paksha Bala*: Fortnight strength based on lunar elongation from Sun.
    - *Tribhaga Bala*: Day/night three-part lord allocations (Mercury, Sun, Saturn / Moon, Venus, Mars; Jupiter full 60).
    - *Vara Bala*: Weekday lord allocation ($45.0$ Virupas).
    - *Hora Bala*: Planetary hour lord allocation ($60.0$ Virupas).
    - *Masa Bala*: Solar month lord allocation ($30.0$ Virupas).
    - *Varsha Bala*: Jovian/solar year lord allocation ($15.0$ Virupas).
    - *Ayana Bala*: Solstitial strength from 3D equatorial declination $\delta = \arcsin(\sin\epsilon \sin\lambda_{\text{tropical}})$.
    - *Yuddha Bala*: Planetary war adjustment for Tara grahas within $1.0^\circ$ separation.
  - **Chesta Bala (Motional Strength, ASTRO-R35)**: Motional strength per BPHS Ch. 28, Sloka 21:
    - Sun Chesta Bala equals its Ayana Bala.
    - Moon Chesta Bala equals its Paksha Bala.
    - Tara Grahas: Chesta Kendra elongation arc reduction ($K / 3.0 \in [0.0, 60.0]$ Virupas), with Retrograde (`VAKRA`) receiving full $60.0$ Virupas.
    - Motion categorization: VAKRA, VIKALA, CHARA, MANDA, SAMA.
  - **Drik Bala (Aspectual Strength, ASTRO-R37)**: Aspectual strength curve over $[30^\circ, 300^\circ]$, special aspect bonuses (Mars 4th/8th, Jupiter 5th/9th, Saturn 3rd/10th), signed Benefic (+) vs Malefic (-) weighting, reduced by $\frac{1}{4}$ factor.
  - **Completeness & Auditability Gate (ASTRO-R38)**: Complete evaluation: for the 7 classical planets, all 6 components evaluate to complete non-null totals (`completeness = COMPLETE`, `isComplete = true`, `totalVirupas = sum(6 components)`, `totalRupas = totalVirupas / 60.0`). Rahu and Ketu remain strictly `UNSUPPORTED`.
- **Ashtakavarga Engine (Phase 6.1)**: Classical Ashtakavarga system per *Brihat Parashara Hora Shastra*, Chapters 66–73 and Dr. B.V. Raman's *Ashtakavarga System of Direction* (`PARASHARA_CLASSICAL_V1`).
  - **Bhinnashtakavarga (BAV, ASTRO-R39A-G)**: Individual 8-contributor auspicious distribution tables for the 7 classical planets (Sun=48, Moon=49, Mars=39, Mercury=54, Jupiter=56, Venus=52, Saturn=39 Bindus).
  - **Sarvashtakavarga (SAV, ASTRO-R40)**: Aggregate 12-sign distribution across all 7 planets with strict mathematical invariant check ($\sum \text{SAV} = 337$ Bindus, $\sum \text{Rekhas} = 335$).
  - **Prastarashtakavarga Matrix**: Complete $8 \times 12$ binary allocation grid (0 or 1) mapping contributor-to-sign benefic points.
  - **Node Policy (ASTRO-R41)**: Rahu and Ketu do not participate in classical 337-Bindu Ashtakavarga and return `UNSUPPORTED`.
- **Ashtakavarga Shodhana (Phase 6.2)**: Classical dual reductions per *Brihat Parashara Hora Shastra*, Chapters 73–74 and Dr. B.V. Raman's *Ashtakavarga System of Direction*, Chapters 4–5 (`PARASHARA_CLASSICAL_V1`).
  - **Trikona Shodhana (ASTRO-R42)**: Triplicity reduction across Agni (0,4,8), Prithvi (1,5,9), Vayu (2,6,10), and Jala (3,7,11) groups, handling equal values, zero boundaries, and minimum subtraction.
  - **Ekadhipatya Shodhana (ASTRO-R43)**: Dual-lordship reductions for Mars (0,7), Venus (1,6), Mercury (2,5), Jupiter (8,11), and Saturn (9,10) based on classical D1 planetary occupancy, zero exemption, and unoccupied sign rules. Cancer (3) and Leo (4) are strictly exempt.
  - **Shodhita Sarvashtakavarga (ASTRO-R44)**: Derivation of the reduced collective distribution summing the 7 Shodhita BAV charts sign by sign, preserving monotonic non-increasing bounds ($\text{Raw } 337 \ge \text{Trikona} \ge \text{Shodhita} \ge 0$).
- **Ashtakavarga Pinda Calculations (Phase 6.3)**: Classical Pinda computations per *Brihat Parashara Hora Shastra*, Chapters 74–75 and Dr. B.V. Raman's *Ashtakavarga System of Direction*, Chapter 6 (`PARASHARA_CLASSICAL_V1`).
  - **Rashi Pinda (ASTRO-R45)**: Product of Shodhita BAV bindus in each sign multiplied by classical Rasi Gunakaras [7, 10, 8, 4, 10, 5, 7, 8, 9, 5, 11, 12] for signs 0..11, summed across 12 signs.
  - **Graha Pinda (ASTRO-R46)**: Product of Shodhita BAV bindus in the sign occupied by each planet multiplied by classical Graha Gunakaras (Sun: 5, Moon: 5, Mars: 8, Mercury: 5, Jupiter: 10, Venus: 7, Saturn: 5), summed across all 7 classical planets.
  - **Shodhya Pinda (ASTRO-R47)**: Aggregate composite pinda for each planet: $\text{ShodhyaPinda} = \text{RasiPinda} + \text{GrahaPinda}$.
  - **Aggregate Totals & Invariants (ASTRO-R48)**: Strict mathematical verification of total Rashi Pinda, total Graha Pinda, and total Shodhya Pinda across all 7 classical planets, preserving non-negative integer bounds and immutability.

## Determinism & Offline Operation
- **100% Deterministic**: Identical inputs produce identical outputs bit-for-bit.
- **100% Offline**: Zero network calls, zero web APIs, zero platform-specific C-library binaries.
- **No Fake Data**: All planetary degrees represent real astronomical calculations. Unsupported ayanamsa conventions or varga profiles are rejected explicitly with `UnsupportedConfiguration`.


