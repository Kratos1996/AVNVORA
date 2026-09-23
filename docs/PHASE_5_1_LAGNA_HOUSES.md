# Phase 5.1 — Lagna + House / Bhava Engine Foundation

## 1. Scope
Phase 5.1 establishes a production-quality, deterministic, offline-first astronomical calculation engine for:
- Ascendant (Lagna) and Midheaven (MC) geocentric ecliptic longitudes.
- Sidereal conversion of Lagna via pluggable Ayanamsa models (Lahiri Chitra Paksha).
- Lagna Rashi (0..11), Nakshatra (0..26), and Pada (1..4) allocation.
- Pluggable House (Bhava) system architecture.
- Supported House Systems:
  - **Whole Sign** (Rashi Bhava)
  - **Equal House** (30° divisions from Ascendant)
  - **Placidus** (Designated unsupported; explicit configuration failure without silent approximation)
- Unambiguous $[start, end)$ boundary handling and $360^\circ$ wraparound arithmetic.
- Planet-to-house occupancy mapping across all 9 supported bodies (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn, Rahu, Ketu).
- Public SDK facade decoupling via `:aynvora-core`.

Strictly **out of scope**: Interpretations, predictions, house lord relationships, yogas, dashas, divisional charts (D9, etc.), and UI screens.

---

## 2. Lagna (Ascendant) Astronomical Calculation
The Ascendant is the ecliptic longitude intersecting the eastern horizon at the observer's exact geographic coordinates $(\phi, \lambda_{\text{geo}})$ and calculation instant (Julian Day $JD$).

### Astronomical Formulas (Meeus Ch. 12 & 22)
1. **Greenwich Mean Sidereal Time (GMST $\theta_0$)**:
   $$\theta_0 = 280.46061837 + 360.98564736629 \times (JD - 2451545.0) + 0.000387933 \times T^2 - \frac{T^3}{38710000.0} \pmod{360^\circ}$$
2. **Local Sidereal Time (LST $\theta$)**:
   $$\theta = \text{normalizeDegrees}(\theta_0 + \lambda_{\text{geo}})$$
   (where $\lambda_{\text{geo}}$ is positive East, negative West).
3. **Mean Obliquity of the Ecliptic ($\varepsilon$)**:
   $$\varepsilon = 23.43929111 - 0.013004167 \times T - 0.0000001639 \times T^2 + 0.0000005036 \times T^3 \pmod{360^\circ}$$
4. **Geocentric Tropical Ascendant ($\lambda_{\text{Asc}}$)**:
   $$y = \cos \theta$$
   $$x = -\sin \theta \cos \varepsilon - \tan \phi \sin \varepsilon$$
   $$\lambda_{\text{Asc, trop}} = \text{atan2Deg}(y, x) \pmod{360^\circ}$$
5. **Midheaven ($\lambda_{\text{MC}}$)**:
   $$\lambda_{\text{MC, trop}} = \text{atan2Deg}(\sin \theta, \cos \theta \cos \varepsilon) \pmod{360^\circ}$$

---

## 3. Sidereal Conversion & Ayanamsa
Tropical longitudes are transformed to the Sidereal reference frame using the selected `AyanamsaCalculator`:
$$\lambda_{\text{Asc, sid}} = \text{normalizeDegrees}(\lambda_{\text{Asc, trop}} - \text{ayanamsaDegrees})$$
$$\lambda_{\text{MC, sid}} = \text{normalizeDegrees}(\lambda_{\text{MC, trop}} - \text{ayanamsaDegrees})$$

For the default **Lahiri (Chitra Paksha)** convention:
$$\text{Ayanamsa}_{\text{Lahiri}} = 23.85709222^\circ + 1.39697128^\circ \times T + 0.00030878^\circ \times T^2$$

---

## 4. Lagna Rashi, Nakshatra, and Pada
Using `ZodiacCalculator`:
- **Rashi Index**: $\lfloor \lambda_{\text{Asc, sid}} / 30.0^\circ \rfloor \in [0..11]$
- **Degree in Rashi**: $\lambda_{\text{Asc, sid}} - (\text{rashiIndex} \times 30.0^\circ)$
- **Nakshatra Index**: $\lfloor \lambda_{\text{Asc, sid}} / (40.0^\circ / 3.0) \rfloor \in [0..26]$
- **Degree in Nakshatra**: $\lambda_{\text{Asc, sid}} - (\text{nakshatraIndex} \times 13.333333^\circ)$
- **Pada**: $\lfloor \text{degreeInNakshatra} / 3.333333^\circ \rfloor + 1 \in [1..4]$

---

## 5. Pluggable House System Architecture
House calculation is decoupled via the `HouseSystemCalculator` interface:
```kotlin
interface HouseSystemCalculator {
    val systemName: String
    fun calculate(input: HouseCalculationInput): HouseCalculationResult
}
```

### Supported House Systems
1. **Whole Sign (`WHOLE_SIGN`)**:
   - House 1 equals the full $30^\circ$ Rashi containing the Sidereal Ascendant.
   - House $h \in [1..12]$ occupies sign index $(\text{lagnaRashiIndex} + h - 1) \bmod 12$.
   - Start longitude $= \text{signIndex} \times 30.0^\circ$.
   - End longitude $= (\text{signIndex} + 1) \times 30.0^\circ$.
2. **Equal House (`EQUAL_HOUSE`)**:
   - House 1 cusp begins at $\lambda_{\text{Asc, sid}}$.
   - House $h \in [1..12]$ cusp $= \text{normalizeDegrees}(\lambda_{\text{Asc, sid}} + (h - 1) \times 30.0^\circ)$.
   - Interval: $[\text{cusp}_h, \text{cusp}_{h+1})$.
3. **Placidus (`PLACIDUS`)**:
   - In accordance with Master Rules against approximate or faked calculations, Placidus semi-arc calculations are explicitly marked unsupported and return `AynvoraResult.Failure.UnsupportedConfiguration`.

---

## 6. Planet-to-House Mapping & Boundary Handling
- All boundary intervals are defined as half-open $[start, end)$.
- Every sidereal planetary coordinate $\lambda_p \in [0.0^\circ, 360.0^\circ)$ maps deterministically to exactly one house $h \in [1..12]$.
- **Whole Sign Mapping**:
  $$\text{houseNumber} = ((\lfloor \lambda_p / 30.0 \rfloor - \text{lagnaRashiIndex} + 12) \bmod 12) + 1$$
- **Equal House Mapping**:
  $$\Delta = \text{normalizeDegrees}(\lambda_p - \lambda_{\text{Asc, sid}})$$
  $$\text{houseNumber} = \lfloor \Delta / 30.0 \rfloor + 1$$
- **Rahu / Ketu**: Rahu and Ketu maintain exact $180^\circ$ separation, placing them in opposite houses ($h$ and $(h + 5) \bmod 12 + 1$).

---

## 7. Polar & Extreme Latitude Safety
- At latitudes $|\phi| \ge 89.99^\circ$, coordinate inputs are clamped safely to avoid infinite tangent singularities.
- Calculations return valid, finite numbers without crashing, freezing, or allocating platform-specific memory.

---

## 8. Public SDK Facade
The public SDK in `:aynvora-core` exposes:
- `ChartResult.lagna`: `LagnaDetails` (tropical & sidereal longitudes, Rashi position, Nakshatra position, LST, obliquity, MC).
- `ChartResult.houses`: `List<HouseDetails>` (house number 1..12, system, cusp, start, end, Rashi position).
- `PlanetaryPosition.houseNumber`: House occupancy (1..12) for each celestial body.

---

## 9. Localization Decoupling
Calculations produce pure mathematical structures (indices 0..11, 0..26, 1..4, 1..12, degrees). Localization layers (`:aynvora-localization`) format these models for presentation without affecting calculation determinism.

---

## 10. Verification & Test Results
- **Unit Tests**: All tests in `:astro-engine:jvmTest`, `:aynvora-core:jvmTest`, `:aynvora-data:jvmTest`, `:design-system:jvmTest`, `:aynvora-localization:jvmTest` pass.
- **Android App**: `./gradlew :androidApp:assembleDebug` builds successfully.
- **Desktop App**: `./gradlew :desktopApp:packageDistributionForCurrentOS` builds successfully.
- **iOS KMP Targets**: `:compileKotlinIosArm64` builds cleanly across all modules.

---

## 11. Reference Sources
- Jean Meeus, *Astronomical Algorithms*, 2nd Edition (Chapters 12, 14, 22, 25, 47).
- Indian Astronomical Ephemeris & Calendar Reform Committee (1955).
- VSOP87 & Simon et al. (1994) planetary theories.

---

## 12. Known Limitations & Deferred Work
- **Placidus House System**: Deferred to future phase pending full iterative quadrant implementation with polar circle bounds checks.
- **Divisional Charts (Vargas)**: D9 Navamsha, D10 Dashamsha, etc. deferred to Phase 5.2.
- **Astrological Yogas, Dashas, and Interpretation**: Deferred to subsequent phases.
