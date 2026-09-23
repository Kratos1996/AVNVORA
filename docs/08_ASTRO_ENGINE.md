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

## Calculation Domains Implemented (Phase 4)
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

## Determinism & Offline Operation
- **100% Deterministic**: Identical inputs produce identical outputs bit-for-bit.
- **100% Offline**: Zero network calls, zero web APIs, zero platform-specific C-library binaries.
- **No Fake Data**: All planetary degrees represent real astronomical calculations. Unsupported ayanamsa conventions are rejected explicitly with `UnsupportedConfiguration`.
