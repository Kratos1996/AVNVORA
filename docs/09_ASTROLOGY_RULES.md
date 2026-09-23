# AYNVORA Astrology Rules
Version: 1.1 (Phase 4 Implemented Rules)

## Implemented Rule Registry

### ASTRO-R01: Tropical to Sidereal Conversion (Lahiri Chitra Paksha)
- **Tradition / Standard**: Calendar Reform Committee (Govt. of India, 1955), Indian Astronomical Ephemeris.
- **Formula**: $\lambda_{\text{sidereal}} = \text{normalize}(\lambda_{\text{tropical}} - \text{Ayanamsa}_{\text{Lahiri}})$.
- **Epoch**: J2000.0 offset = $23^\circ 51' 25.532''$, precession rate = $5029.0966''$ / century.
- **Output**: Sidereal ecliptic longitude normalized to $[0.0^\circ, 360.0^\circ)$.

### ASTRO-R02: 12 Rashi (Zodiac Sign) Division
- **Tradition**: Standard Vedic Sidereal Zodiac (Nirayana).
- **Rule**: Sidereal circle of $360^\circ$ is divided into 12 equal signs of $30^\circ$ each.
- **Signs**: 0: Aries (Mesha), 1: Taurus (Vrishabha), 2: Gemini (Mithuna), 3: Cancer (Karka), 4: Leo (Simha), 5: Virgo (Kanya), 6: Libra (Tula), 7: Scorpio (Vrishchika), 8: Sagittarius (Dhanu), 9: Capricorn (Makara), 10: Aquarius (Kumbha), 11: Pisces (Meena).
- **Calculation**: $\text{signIndex} = \lfloor \lambda_{\text{sidereal}} / 30.0 \rfloor$, $\text{degreeInSign} = \lambda_{\text{sidereal}} - (\text{signIndex} \times 30.0)$.

### ASTRO-R03: 27 Nakshatra Division
- **Tradition**: Vedic Lunar Mansions.
- **Rule**: $360^\circ$ sidereal circle is divided into 27 equal Nakshatras of $13^\circ 20' = 13.333333^\circ$ each.
- **Sequence**: Ashwini (0) to Revati (26).
- **Calculation**: $\text{nakshatraIndex} = \lfloor \lambda_{\text{sidereal}} / (40.0 / 3.0) \rfloor$, $\text{degreeInNakshatra} = \lambda_{\text{sidereal}} - (\text{nakshatraIndex} \times (40.0 / 3.0))$.

### ASTRO-R04: 4 Padas per Nakshatra
- **Tradition**: Vedic Nakshatra Pada division.
- **Rule**: Each Nakshatra contains 4 equal quarters (Padas) of $3^\circ 20' = 3.333333^\circ$ each.
- **Output**: Pada integer in range 1..4. Boundary values clamp safely to $[1..4]$.

### ASTRO-R05: Lunar Node Opposition (Rahu & Ketu)
- **Tradition**: Classical Jyotish (Surya Siddhanta, Brihat Parashara Hora Shastra).
- **Rule**: Rahu is the North Lunar Node; Ketu is the South Lunar Node. Ketu is mathematically required to maintain exact $180^\circ$ opposition to Rahu:
  $\lambda_{\text{Ketu}} = (\lambda_{\text{Rahu}} + 180^\circ) \bmod 360^\circ$.

### ASTRO-R06: Apparent Retrograde Motion (Vakra)
- **Condition**: Daily motion rate of apparent geocentric ecliptic longitude:
  $\text{rate} = \frac{d\lambda}{dt}$.
- **Rule**: Planet is retrograde when $\text{rate} < 0$.
- **Exceptions**:
  - Sun and Moon: Always direct ($\text{isRetrograde} = \text{false}$).
  - Mean Rahu and Ketu: Always retrograde ($\text{isRetrograde} = \text{true}$).
  - Mercury, Venus, Mars, Jupiter, Saturn: Determined from apparent longitudinal velocity.
