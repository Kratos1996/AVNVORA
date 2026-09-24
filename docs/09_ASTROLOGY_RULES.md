# AYNVORA Astrology Rules
Version: 1.3 (Phase 5.2 Implemented Rules)

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

### ASTRO-R07: Angular Separation & Major Planetary Aspects
- **Geometry**: Shortest circular distance along the ecliptic:
  $\text{separation}(A, B) = \min(|A - B| \bmod 360^\circ, 360^\circ - (|A - B| \bmod 360^\circ)) \in [0.0^\circ, 180.0^\circ]$.
- **Aspect Definitions & Default Orbs**:
  - Conjunction ($0^\circ$): Orb $\le 8.0^\circ$
  - Sextile ($60^\circ$): Orb $\le 6.0^\circ$
  - Square ($90^\circ$): Orb $\le 7.0^\circ$
  - Trine ($120^\circ$): Orb $\le 8.0^\circ$
  - Opposition ($180^\circ$): Orb $\le 8.0^\circ$
- **Pairs**: Distinct unordered pairs only $(A < B)$, deduplicated and deterministically ordered.

### ASTRO-R08: Planetary Combustion (Asta)
- **Tradition**: Surya Siddhanta / Brihat Parashara Hora Shastra classical solar proximity thresholds:
  - Moon: $\le 12.0^\circ$
  - Mars: $\le 17.0^\circ$
  - Mercury: $\le 14.0^\circ$ (Direct), $\le 12.0^\circ$ (Retrograde)
  - Jupiter: $\le 11.0^\circ$
  - Venus: $\le 10.0^\circ$ (Direct), $\le 8.0^\circ$ (Retrograde)
  - Saturn: $\le 15.0^\circ$
  - Sun, Rahu, Ketu: `NOT_APPLICABLE` (Sun cannot be combust; shadow nodes are exempt from solar combustion).

## Divisional Charts (Vargas) — Brihat Parashara Hora Shastra (BPHS Ch. 6)
Ruleset ID: `PARASHARA_CLASSICAL_V1`

### ASTRO-R09: D1 Rashi Chart (Base Chart)
- **Tradition**: BPHS Ch. 6, slokas 3-4.
- **Divisions**: 1 division of $30.0^\circ$ per sign.
- **Sign Mapping**: $\text{resultingRashi} = S$.
- **Interval**: $[0.0^\circ, 30.0^\circ)$.

### ASTRO-R10: D2 Hora Chart
- **Tradition**: BPHS Ch. 6, slokas 5-6 (Parashara Sun/Moon Hora).
- **Divisions**: 2 divisions of $15.0^\circ$ per sign.
- **Intervals**: $[0.0^\circ, 15.0^\circ)$ (division 0), $[15.0^\circ, 30.0^\circ)$ (division 1).
- **Odd Signs** ($S \bmod 2 == 0$, Aries, Gemini, Leo, etc.):
  - Division 0: Sun $\implies$ Leo (4).
  - Division 1: Moon $\implies$ Cancer (3).
- **Even Signs** ($S \bmod 2 == 1$, Taurus, Cancer, Virgo, etc.):
  - Division 0: Moon $\implies$ Cancer (3).
  - Division 1: Sun $\implies$ Leo (4).

### ASTRO-R11: D3 Drekkana Chart
- **Tradition**: BPHS Ch. 6, slokas 7-8.
- **Divisions**: 3 equal divisions of $10.0^\circ$ per sign.
- **Intervals**: $[0.0^\circ, 10.0^\circ)$ (div 0), $[10.0^\circ, 20.0^\circ)$ (div 1), $[20.0^\circ, 30.0^\circ)$ (div 2).
- **Sign Mapping**:
  - Division 0: 1st from sign $\implies S$.
  - Division 1: 5th from sign $\implies (S + 4) \bmod 12$.
  - Division 2: 9th from sign $\implies (S + 8) \bmod 12$.
  - General formula: $\text{resultingRashi} = (S + 4 \times k) \bmod 12$ for $k \in \{0, 1, 2\}$.

### ASTRO-R12: D4 Chaturthamsa (Turyamsa)
- **Tradition**: BPHS Ch. 6, sloka 9.
- **Divisions**: 4 equal divisions of $7.5^\circ$ ($7^\circ 30'$) per sign.
- **Sign Mapping**: Kendras (1st, 4th, 7th, 10th from sign):
  - $\text{resultingRashi} = (S + 3 \times k) \bmod 12$ for $k \in \{0, 1, 2, 3\}$.

### ASTRO-R13: D7 Saptamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 10-11.
- **Divisions**: 7 equal divisions of $30.0^\circ / 7 \approx 4.285714^\circ$ ($4^\circ 17' 8.57''$) per sign.
- **Odd Signs** ($S \bmod 2 == 0$): starts from sign itself $\implies (S + k) \bmod 12$ for $k \in [0..6]$.
- **Even Signs** ($S \bmod 2 == 1$): starts from 7th sign from it $\implies (S + 6 + k) \bmod 12$ for $k \in [0..6]$.

### ASTRO-R14: D9 Navamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 12-14.
- **Divisions**: 9 equal divisions of $3^\circ 20' = 3.333333^\circ$ per sign (equivalent to 1 Nakshatra Pada; 108 in total zodiac).
- **Elemental Triplicity Starting Signs**:
  - Fiery signs ($S \in \{0, 4, 8\}$, Aries, Leo, Sagittarius): starts from Aries (0).
  - Earthy signs ($S \in \{1, 5, 9\}$, Taurus, Virgo, Capricorn): starts from Capricorn (9).
  - Airy signs ($S \in \{2, 6, 10\}$, Gemini, Libra, Aquarius): starts from Libra (6).
  - Watery signs ($S \in \{3, 7, 11\}$, Cancer, Scorpio, Pisces): starts from Cancer (3).
- **Sign Mapping**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12 \equiv \lfloor \lambda_{\text{sidereal}} / (40.0 / 12.0) \rfloor \bmod 12$ for $k \in [0..8]$.

### ASTRO-R15: D10 Dasamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 15-16.
- **Divisions**: 10 equal divisions of $3.0^\circ$ per sign.
- **Odd Signs** ($S \bmod 2 == 0$): starts from sign itself $\implies (S + k) \bmod 12$ for $k \in [0..9]$.
- **Even Signs** ($S \bmod 2 == 1$): starts from 9th sign from it $\implies (S + 8 + k) \bmod 12$ for $k \in [0..9]$.

### ASTRO-R16: D12 Dwadasamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 17-18.
- **Divisions**: 12 equal divisions of $2.5^\circ$ ($2^\circ 30'$) per sign.
- **Sign Mapping**: Counted sequentially from sign itself $\implies (S + k) \bmod 12$ for $k \in [0..11]$.

### ASTRO-R17: D16 Shodasamsa (Kalamsa) Chart
- **Tradition**: BPHS Ch. 6, slokas 19-21.
- **Divisions**: 16 equal divisions of $1.875^\circ$ ($1^\circ 52' 30''$) per sign.
- **Sign Mobility Starting Signs**:
  - Movable (Chara: $S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed (Sthira: $S \in \{1, 4, 7, 10\}$): starts from Leo (4).
  - Dual (Dwisvabhava: $S \in \{2, 5, 8, 11\}$): starts from Sagittarius (8).
- **Sign Mapping**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$ for $k \in [0..15]$.

### ASTRO-R18: D20 Vimsamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 22-23.
- **Divisions**: 20 equal divisions of $1.5^\circ$ ($1^\circ 30'$) per sign.
- **Sign Mobility Starting Signs**:
  - Movable ($S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed ($S \in \{1, 4, 7, 10\}$): starts from Sagittarius (8).
  - Dual ($S \in \{2, 5, 8, 11\}$): starts from Leo (4).
- **Sign Mapping**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$ for $k \in [0..19]$.

### ASTRO-R19: D24 Chaturvimsamsa (Siddhamsa) Chart
- **Tradition**: BPHS Ch. 6, slokas 24-25.
- **Divisions**: 24 equal divisions of $1.25^\circ$ ($1^\circ 15'$) per sign.
- **Odd Signs** ($S \bmod 2 == 0$): starts from Leo (4) $\implies (4 + k) \bmod 12$ for $k \in [0..23]$.
- **Even Signs** ($S \bmod 2 == 1$): starts from Cancer (3) $\implies (3 + k) \bmod 12$ for $k \in [0..23]$.

### ASTRO-R20: D27 Bhamsa (Saptavimsamsa / Nakshatramsa) Chart
- **Tradition**: BPHS Ch. 6, slokas 26-27.
- **Divisions**: 27 equal divisions of $30.0^\circ / 27 = 10.0^\circ / 9 \approx 1.111111^\circ$ ($1^\circ 6' 40''$) per sign.
- **Elemental Triplicity Starting Signs**:
  - Fiery signs ($S \in \{0, 4, 8\}$): starts from Aries (0).
  - Earthy signs ($S \in \{1, 5, 9\}$): starts from Cancer (3).
  - Airy signs ($S \in \{2, 6, 10\}$): starts from Libra (6).
  - Watery signs ($S \in \{3, 7, 11\}$): starts from Capricorn (9).
- **Sign Mapping**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$ for $k \in [0..26]$.

### ASTRO-R21: D30 Trimsamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 28-31.
- **Divisions**: 5 UNEQUAL degree divisions ruled by 5 non-luminary planets (Mars, Saturn, Jupiter, Mercury, Venus).
- **Odd Signs** ($S \bmod 2 == 0$):
  - $[0.0^\circ, 5.0^\circ)$ (span $5^\circ$): Mars $\implies$ Aries (0).
  - $[5.0^\circ, 10.0^\circ)$ (span $5^\circ$): Saturn $\implies$ Aquarius (10).
  - $[10.0^\circ, 18.0^\circ)$ (span $8^\circ$): Jupiter $\implies$ Sagittarius (8).
  - $[18.0^\circ, 25.0^\circ)$ (span $7^\circ$): Mercury $\implies$ Gemini (2).
  - $[25.0^\circ, 30.0^\circ)$ (span $5^\circ$): Venus $\implies$ Libra (6).
- **Even Signs** ($S \bmod 2 == 1$):
  - $[0.0^\circ, 5.0^\circ)$ (span $5^\circ$): Venus $\implies$ Taurus (1).
  - $[5.0^\circ, 12.0^\circ)$ (span $7^\circ$): Mercury $\implies$ Virgo (5).
  - $[12.0^\circ, 20.0^\circ)$ (span $8^\circ$): Jupiter $\implies$ Pisces (11).
  - $[20.0^\circ, 25.0^\circ)$ (span $5^\circ$): Saturn $\implies$ Capricorn (9).
  - $[25.0^\circ, 30.0^\circ)$ (span $5^\circ$): Mars $\implies$ Scorpio (7).

### ASTRO-R22: D40 Khavedamsa (Swavedamsa) Chart
- **Tradition**: BPHS Ch. 6, slokas 32-33.
- **Divisions**: 40 equal divisions of $0.75^\circ$ ($45'$) per sign.
- **Odd Signs** ($S \bmod 2 == 0$): starts from Aries (0) $\implies (0 + k) \bmod 12$ for $k \in [0..39]$.
- **Even Signs** ($S \bmod 2 == 1$): starts from Libra (6) $\implies (6 + k) \bmod 12$ for $k \in [0..39]$.

### ASTRO-R23: D45 Akshavedamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 34-35.
- **Divisions**: 45 equal divisions of $30.0^\circ / 45 = 2/3^\circ = 0.666667^\circ$ ($40'$) per sign.
- **Sign Mobility Starting Signs**:
  - Movable ($S \in \{0, 3, 6, 9\}$): starts from Aries (0).
  - Fixed ($S \in \{1, 4, 7, 10\}$): starts from Leo (4).
  - Dual ($S \in \{2, 5, 8, 11\}$): starts from Sagittarius (8).
- **Sign Mapping**: $\text{resultingRashi} = (\text{startSign}(S) + k) \bmod 12$ for $k \in [0..44]$.

### ASTRO-R24: D60 Shashtiamsa Chart
- **Tradition**: BPHS Ch. 6, slokas 36-42 & Dr. B. V. Raman, *A Manual of Hindu Astrology* Ch. 9 / Jataka Parijata Ch. 1.
- **Divisions**: 60 equal divisions of $0.5^\circ$ ($30'$) per sign. High boundary sensitivity.
- **Sign Mapping**: Cyclic counting from sign itself $\implies \text{resultingRashi} = (S + k) \bmod 12$ for $k = \lfloor \text{degreeInSign} / 0.5 \rfloor \in [0..59]$.
- **Boundary Handling**: Strict half-open intervals $[k \times 0.5^\circ, (k + 1) \times 0.5^\circ)$ with IEEE 754 64-bit precision preservation.

## Planetary Dignities & Relationships — Brihat Parashara Hora Shastra (BPHS Ch. 3)
Ruleset ID: `PARASHARA_CLASSICAL_V1`

### ASTRO-R25: Planetary Sign Ownership (Swakshetra)
- **Tradition**: BPHS Ch. 3, slokas 21-22 & 49-50.
- **Lords of Signs**:
  - Sun (Surya): Leo (Simha, 4)
  - Moon (Chandra): Cancer (Karka, 3)
  - Mars (Mangala): Aries (Mesha, 0), Scorpio (Vrishchika, 7)
  - Mercury (Budha): Gemini (Mithuna, 2), Virgo (Kanya, 5)
  - Jupiter (Guru): Sagittarius (Dhanu, 8), Pisces (Meena, 11)
  - Venus (Shukra): Taurus (Vrishabha, 1), Libra (Tula, 6)
  - Saturn (Shani): Capricorn (Makara, 9), Aquarius (Kumbha, 10)
  - Rahu & Ketu: Shadow nodes (Chaya Grahas); no undisputed canonical rulership in BPHS $\implies$ `NOT_APPLICABLE`.

### ASTRO-R26: Planetary Exaltation (Uchcha) and Debilitation (Neecha)
- **Tradition**: BPHS Ch. 3, slokas 49-50.
- **Exaltation Sign & Deep Degree (Parama Uchcha)**:
  - Sun: Aries (0) at $10.0^\circ$
  - Moon: Taurus (1) at $3.0^\circ$
  - Mars: Capricorn (9) at $28.0^\circ$
  - Mercury: Virgo (5) at $15.0^\circ$
  - Jupiter: Cancer (3) at $5.0^\circ$
  - Venus: Pisces (11) at $27.0^\circ$
  - Saturn: Libra (6) at $20.0^\circ$
- **Debilitation Sign & Deep Degree (Parama Neecha)** (Exact $180^\circ$ opposite sign):
  - Sun: Libra (6) at $10.0^\circ$
  - Moon: Scorpio (7) at $3.0^\circ$
  - Mars: Cancer (3) at $28.0^\circ$
  - Mercury: Pisces (11) at $15.0^\circ$
  - Jupiter: Capricorn (9) at $5.0^\circ$
  - Venus: Virgo (5) at $27.0^\circ$
  - Saturn: Aries (0) at $20.0^\circ$
  - Rahu & Ketu: `NOT_APPLICABLE`.

### ASTRO-R27: Planetary Moolatrikona Degrees
- **Tradition**: BPHS Ch. 3, slokas 51-54.
- **Half-open Degree Intervals**:
  - Sun in Leo (4): $[0.0^\circ, 20.0^\circ) \implies$ Moolatrikona; $[20.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Moon in Taurus (1): $[0.0^\circ, 3.0^\circ) \implies$ Exaltation; $[3.0^\circ, 30.0^\circ) \implies$ Moolatrikona.
  - Mars in Aries (0): $[0.0^\circ, 12.0^\circ) \implies$ Moolatrikona; $[12.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Mercury in Virgo (5): $[0.0^\circ, 15.0^\circ) \implies$ Exaltation; $[15.0^\circ, 20.0^\circ) \implies$ Moolatrikona; $[20.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Jupiter in Sagittarius (8): $[0.0^\circ, 10.0^\circ) \implies$ Moolatrikona; $[10.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Venus in Libra (6): $[0.0^\circ, 15.0^\circ) \implies$ Moolatrikona; $[15.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Saturn in Aquarius (10): $[0.0^\circ, 20.0^\circ) \implies$ Moolatrikona; $[20.0^\circ, 30.0^\circ) \implies$ Own Sign.
  - Rahu & Ketu: `NOT_APPLICABLE`.

### ASTRO-R28: Natural Planetary Relationships (Naisargika Maitri)
- **Tradition**: BPHS Ch. 3, slokas 55-58.
- **Directional Relationship Matrix**:
  - Sun: Friends = Moon, Mars, Jupiter; Neutral = Mercury; Enemies = Venus, Saturn.
  - Moon: Friends = Sun, Mercury; Neutral = Mars, Jupiter, Venus, Saturn; Enemies = None.
  - Mars: Friends = Sun, Moon, Jupiter; Neutral = Venus, Saturn; Enemies = Mercury.
  - Mercury: Friends = Sun, Venus; Neutral = Mars, Jupiter, Saturn; Enemies = Moon.
  - Jupiter: Friends = Sun, Moon, Mars; Neutral = Saturn; Enemies = Mercury, Venus.
  - Venus: Friends = Mercury, Saturn; Neutral = Mars, Jupiter; Enemies = Sun, Moon.
  - Saturn: Friends = Mercury, Venus; Neutral = Jupiter; Enemies = Sun, Moon, Mars.
  - Rahu & Ketu: `NOT_APPLICABLE`.

### ASTRO-R29: Temporary Planetary Relationships (Tatkalika Maitri)
- **Tradition**: BPHS Ch. 3, sloka 59.
- **Relative Sign Distance**: $H = (S_{\text{target}} - S_{\text{source}} + 12) \bmod 12 + 1$.
- **Rule**:
  - $H \in \{2, 3, 4, 10, 11, 12\} \implies$ `FRIEND` (Tatkalika Mitra).
  - $H \in \{1, 5, 6, 7, 8, 9\} \implies$ `ENEMY` (Tatkalika Shatru).
- **Properties**: Same-sign occupancy ($H = 1$) is temporary enmity; symmetric pairwise relationship.

### ASTRO-R30: Five-fold Combined Relationships (Panchadha Maitri)
- **Tradition**: BPHS Ch. 3, sloka 60.
- **Synthesis**:
  - Friend (+1) + Temporary Friend (+1) = `GREAT_FRIEND` (Adhi Mitra)
  - Friend (+1) + Temporary Enemy (-1) = `NEUTRAL` (Sama)
  - Neutral (0) + Temporary Friend (+1) = `FRIEND` (Mitra)
  - Neutral (0) + Temporary Enemy (-1) = `ENEMY` (Shatru)
  - Enemy (-1) + Temporary Friend (+1) = `NEUTRAL` (Sama)
  - Enemy (-1) + Temporary Enemy (-1) = `GREAT_ENEMY` (Adhi Shatru)

### ASTRO-R31: Sign Placement Dignity
- **Evaluation**: Evaluated for any planetary placement in sign $S$ and degree $d$ (applicable in D1 and any divisional chart).
- **Classification Hierarchy**:
  1. `EXALTATION`: In Exaltation sign (respecting Moolatrikona/Own-sign bounds in Taurus and Virgo).
  2. `DEBILITATION`: In Debilitation sign.
  3. `MOOLATRIKONA`: In Moolatrikona sign within $[0^\circ, \text{endDeg})$.
  4. `OWN_SIGN`: In Own sign (or outside Moolatrikona span).
  5. Other signs: Classified according to Compound Relationship to the sign lord $L = \text{signLord}(S)$:
     - `GREAT_FRIEND_SIGN`
     - `FRIEND_SIGN`
     - `NEUTRAL_SIGN`
     - `ENEMY_SIGN`
     - `GREAT_ENEMY_SIGN`

---

## Phase 5.5: Classical Shadbala Engine Rules

### ASTRO-R32: Naisargika Bala (Natural Strength)
- **Tradition & Source**: Brihat Parashara Hora Shastra (BPHS), Chapter 28, Slokas 13–14.
- **Rule**: Fixed intrinsic strength proportional to the natural brilliance/luminosity of the seven traditional Grahas:
  - Sun: $60.0 \times \frac{7}{7} = 60.0000$ Virupas ($1.0000$ Rupa)
  - Moon: $60.0 \times \frac{6}{7} \approx 51.4286$ Virupas ($0.8571$ Rupa)
  - Venus: $60.0 \times \frac{5}{7} \approx 42.8571$ Virupas ($0.7143$ Rupa)
  - Jupiter: $60.0 \times \frac{4}{7} \approx 34.2857$ Virupas ($0.5714$ Rupa)
  - Mercury: $60.0 \times \frac{3}{7} \approx 25.7143$ Virupas ($0.4286$ Rupa)
  - Mars: $60.0 \times \frac{2}{7} \approx 17.1429$ Virupas ($0.2857$ Rupa)
  - Saturn: $60.0 \times \frac{1}{7} \approx 8.5714$ Virupas ($0.1429$ Rupa)
  - Rahu & Ketu: `NOT_APPLICABLE` ($0.0$ Virupas).

### ASTRO-R33: Dig Bala (Directional Strength)
- **Tradition & Source**: BPHS Chapter 28, Slokas 7–8.
- **Powerful Points (Dig Bala Kendra)**:
  - Jupiter & Mercury: 1st house cusp (East / Lagna).
  - Sun & Mars: 10th house cusp (South / Midheaven / MC).
  - Saturn: 7th house cusp (West / Descendant).
  - Moon & Venus: 4th house cusp (North / Nadir / IC).
- **Powerless Points (Nirbala Points)**: Exactly $180^\circ$ opposite the powerful cusp.
- **Formula**:
  Let $\lambda_{\text{planet}}$ be the sidereal longitude of the Graha, and $\lambda_{\text{zero}}$ be its Nirbala point longitude.
  $$\Delta = |\lambda_{\text{planet}} - \lambda_{\text{zero}}| \pmod{360^\circ}$$
  $$\text{If } \Delta > 180^\circ, \quad \Delta = 360^\circ - \Delta$$
  $$\text{Dig Bala (Virupas)} = \frac{\Delta^\circ}{3.0} = \frac{\Delta^\circ}{180^\circ} \times 60 \text{ Virupas}$$
  Range: $[0.0, 60.0]$ Virupas ($[0.0, 1.0]$ Rupa).

### ASTRO-R34: Sthana Bala (Positional Strength)
- **Tradition & Source**: BPHS Chapter 28, Slokas 2–12.
- **Component Balas**:
  1. **ASTRO-R34A: Uchcha Bala (Exaltation Strength)**:
     - Distance from deep debilitation point $\lambda_{\text{Neecha}}$ (ASTRO-R26).
     - $\Delta = |\lambda_{\text{planet}} - \lambda_{\text{Neecha}}| \pmod{360^\circ}$; if $\Delta > 180^\circ, \Delta = 360^\circ - \Delta$.
     - $\text{Uchcha Bala (Virupas)} = \frac{\Delta^\circ}{3.0}$. (Range: $0.0$ to $60.0$ Virupas).
  2. **ASTRO-R34B: Saptavargaja Bala (Seven Divisional Charts Strength)**:
     - Evaluated across the 7 classical Parashara divisions: D1, D2, D3, D7, D9, D12, D30.
     - In each Varga, the residential dignity (ASTRO-R31) yields:
       - Moolatrikona / Exaltation: $45.0$ Virupas
       - Own Sign (Swakshetra): $30.0$ Virupas
       - Great Friend Sign (Adhi Mitra): $20.0$ Virupas
       - Friend Sign (Mitra): $15.0$ Virupas
       - Neutral Sign (Sama): $10.0$ Virupas
       - Enemy Sign (Shatru): $4.0$ Virupas
       - Great Enemy Sign (Adhi Shatru): $2.0$ Virupas
       - Debilitation: $0.0$ Virupas
     - $\text{Saptavargaja Bala} = \sum_{v \in \text{Saptavargas}} \text{Virupas}(v)$. (Range: $0.0$ to $315.0$ Virupas).
  3. **ASTRO-R34C: Ojhayugmarasyamsa Bala (Odd/Even Sign & Navamsa Strength)**:
     - Evaluated in D1 (Rashi) and D9 (Navamsa):
       - Female planets (Moon, Venus) in even signs (Taurus, Cancer, Virgo, Scorpio, Capricorn, Pisces): $15.0$ Virupas each (max $30.0$ Virupas).
       - Male and neutral planets (Sun, Mars, Jupiter, Mercury, Saturn) in odd signs (Aries, Gemini, Leo, Libra, Sagittarius, Aquarius): $15.0$ Virupas each (max $30.0$ Virupas).
  4. **ASTRO-R34D: Kendra Bala (Angular House Strength)**:
     - House occupied (1..12):
       - Kendra (Houses 1, 4, 7, 10): $60.0$ Virupas
       - Panaphara (Houses 2, 5, 8, 11): $30.0$ Virupas
       - Apoklima (Houses 3, 6, 9, 12): $15.0$ Virupas
  5. **ASTRO-R34E: Drekkana Bala (Decanate Portion Strength)**:
     - Degree in sign $[0^\circ, 30^\circ)$:
       - 1st Drekkana $[0.0^\circ, 10.0^\circ)$: Male planets (Sun, Mars, Jupiter) receive $15.0$ Virupas; others $0.0$.
       - 2nd Drekkana $[10.0^\circ, 20.0^\circ)$: Neutral planets (Mercury, Saturn) receive $15.0$ Virupas; others $0.0$.
       - 3rd Drekkana $[20.0^\circ, 30.0^\circ)$: Female planets (Moon, Venus) receive $15.0$ Virupas; others $0.0$.
  - **Total Sthana Bala**:
    $$\text{Sthana Bala} = \text{Uchcha} + \text{Saptavargaja} + \text{Ojhayugmarasyamsa} + \text{Kendra} + \text{Drekkana}$$

### ASTRO-R35: Chesta Bala (Motional Strength Completion)
- **Tradition & Source**: BPHS Chapter 28, Slokas 19–21; Raman *Graha and Bhava Balas* Ch. 4.
- **Rule**:
  - **Sun & Moon**:
    - Sun's Chesta Bala equals its Ayana Bala (BPHS Ch. 28, Sloka 21).
    - Moon's Chesta Bala equals its Paksha Bala (BPHS Ch. 28, Sloka 21).
  - **Five Tara Grahas (Mars, Mercury, Jupiter, Venus, Saturn)**:
    - **Chesta Kendra Elongation Arc**:
      - Elongation difference between planet and Seeghrocha (Sun for superior planets Mars, Jupiter, Saturn; elongation point for inferior planets Mercury, Venus):
        $$\Delta = |\lambda_{\text{planet}} - \lambda_{\text{Seeghrocha}}| \pmod{360^\circ}$$
        $$\text{Chesta Kendra } K = \begin{cases} \Delta & \text{if } \Delta \le 180^\circ \\ 360^\circ - \Delta & \text{if } \Delta > 180^\circ \end{cases}$$
        $$\text{Chesta Bala (Virupas)} = \frac{K}{3.0} \in [0.0, 60.0]$$
    - **Vakra Motion**: Retrograde planets ($d\lambda/dt < 0$) receive full $60.0$ Virupas (BPHS Ch. 28, Sloka 19).
    - **Motion Classification**:
      - `VAKRA`: Retrograde ($60.0$ Virupas).
      - `VIKALA`: Stationary ($|d\lambda/dt| < 0.05^\circ/\text{day}$).
      - `CHARA`: Fast direct motion ($d\lambda/dt > \text{mean}$).
      - `MANDA`: Slow direct motion ($0 < d\lambda/dt < \text{mean}$).
      - `SAMA`: Normal mean direct motion.

### ASTRO-R36: Kala Bala (Temporal Strength Completion)
- **Tradition & Source**: BPHS Chapter 28, Slokas 14–18; Raman *Graha and Bhava Balas* Ch. 3.
- **Component Sub-balas**:
  1. **Nathonnatha Bala (Diurnal / Nocturnal Strength)**:
     - Angular distance $\theta \in [0^\circ, 180^\circ]$ of Sun from Nadir (4th house cusp / midnight point).
     - Diurnal planets (Sun, Jupiter, Venus): $\frac{\theta}{3.0} \in [0.0, 60.0]$ Virupas.
     - Nocturnal planets (Moon, Mars, Saturn): $\frac{180.0 - \theta}{3.0} \in [0.0, 60.0]$ Virupas.
     - Mercury: $60.0$ Virupas continuously day and night.
  2. **Paksha Bala (Lunar Phase Strength)**:
     - Lunar elongation $\Delta = (\lambda_{\text{Moon}} - \lambda_{\text{Sun}} + 360^\circ) \pmod{360^\circ}$.
     - Benefic Paksha Bala $= \frac{\Delta}{3.0}$ (Shukla) or $\frac{360^\circ - \Delta}{3.0}$ (Krishna).
     - Malefic Paksha Bala $= 60.0 - \text{Benefic Paksha Bala}$.
     - Jupiter, Venus, Mercury get Benefic Paksha Bala; Moon gets $2 \times \text{Benefic Paksha Bala}$ (max 60); Sun, Mars, Saturn get Malefic Paksha Bala.
  3. **Tribhaga Bala (Three Parts of Day & Night)**:
     - Day (Sun in houses 7..12): Part 1 (H11, 12) = Mercury ($60.0$); Part 2 (H9, 10) = Sun ($60.0$); Part 3 (H7, 8) = Saturn ($60.0$).
     - Night (Sun in houses 1..6): Part 1 (H5, 6) = Moon ($60.0$); Part 2 (H3, 4) = Venus ($60.0$); Part 3 (H1, 2) = Mars ($60.0$).
     - Jupiter receives $60.0$ Virupas at all times. Non-rulers receive $0.0$.
  4. **Vara Bala (Lord of the Day)**:
     - Day of birth lord: Sunday = Sun, Monday = Moon, Tuesday = Mars, Wednesday = Mercury, Thursday = Jupiter, Friday = Venus, Saturday = Saturn.
     - Day lord receives $45.0$ Virupas ($0.75$ Rupas). Others receive $0.0$.
  5. **Hora Bala (Lord of the Hour)**:
     - Chaldean order hora ruler from sunrise/day-start receives $60.0$ Virupas ($1.0$ Rupa). Others receive $0.0$.
  6. **Masa Bala (Lord of the Month)**:
     - Lord of Sun's transit sign receives $30.0$ Virupas ($0.5$ Rupa). Others receive $0.0$.
  7. **Varsha Bala (Lord of the Year)**:
     - Lord of the astrological year receives $15.0$ Virupas ($0.25$ Rupa). Others receive $0.0$.
  8. **Ayana Bala (Declination Strength)**:
     - Declination $\delta = \arcsin(\sin\epsilon \sin\lambda_{\text{tropical}}) \in [-24^\circ, +24^\circ]$.
     - Sun, Mars, Jupiter, Venus: $\frac{24^\circ + \delta}{48^\circ} \times 60 = (24.0 + \delta) \times 1.25$ Virupas.
     - Moon, Saturn: $\frac{24^\circ - \delta}{48^\circ} \times 60 = (24.0 - \delta) \times 1.25$ Virupas.
     - Mercury: $(24.0 + \delta) \times 1.25$ Virupas.
  9. **Yuddha Bala (Planetary War Strength)**:
     - Evaluated for conjunctions within $1.0^\circ$ among the 5 Tara Grahas. If no war, $0.0$.
  - **Total Kala Bala**:
    $$\text{Kala Bala} = \text{Nathonnatha} + \text{Paksha} + \text{Tribhaga} + \text{Vara} + \text{Hora} + \text{Masa} + \text{Varsha} + \text{Ayana} + \text{Yuddha}$$

### ASTRO-R37: Drik Bala (Aspectual Strength Completion)
- **Tradition & Source**: BPHS Chapter 28, Slokas 22–24; Raman *Graha and Bhava Balas* Ch. 6.
- **Rule**:
  - For target planet $\lambda_T$ and aspecting planet $\lambda_A$, separation $\theta = (\lambda_T - \lambda_A + 360^\circ) \pmod{360^\circ}$.
  - General aspect value curve in Virupas:
    - $\theta \in [0^\circ, 30^\circ)$: $0.0$
    - $\theta \in [30^\circ, 60^\circ)$: $\frac{\theta - 30^\circ}{2.0} \in [0.0, 15.0]$
    - $\theta \in [60^\circ, 90^\circ)$: $15.0 + (\theta - 60^\circ) \in [15.0, 45.0]$
    - $\theta \in [90^\circ, 120^\circ)$: $45.0 - \frac{\theta - 90^\circ}{2.0} \in [45.0, 30.0]$
    - $\theta \in [120^\circ, 150^\circ)$: $30.0 - (\theta - 120^\circ) \in [30.0, 0.0]$
    - $\theta \in [150^\circ, 180^\circ)$: $(\theta - 150^\circ) \times 2.0 \in [0.0, 60.0]$
    - $\theta \in [180^\circ, 300^\circ)$: $\frac{300^\circ - \theta}{2.0} \in [60.0, 0.0]$
    - $\theta \ge 300^\circ$: $0.0$
  - Special aspects: Mars (4th, 8th), Jupiter (5th, 9th), Saturn (3rd, 10th) receive classical $60.0$ Virupas full aspect bonuses.
  - Benefic aspects add strength ($+ \text{Drishti}$); Malefic aspects subtract strength ($- \text{Drishti}$).
  - Net aspectual strength:
    $$\text{Drik Bala} = \frac{\sum \text{Benefic Drishti} - \sum \text{Malefic Drishti}}{4.0} \text{ Virupas}$$

### ASTRO-R38: Shadbala Completeness and Auditability Gate
- **Rule**:
  - Total Shadbala score is produced when all six component Balas (Sthana, Dig, Kala, Chesta, Naisargika, Drik) are fully evaluated:
    $$\text{Total Virupas} = \text{Sthana} + \text{Dig} + \text{Kala} + \text{Chesta} + \text{Naisargika} + \text{Drik}$$
    $$\text{Total Rupas} = \frac{\text{Total Virupas}}{60.0}$$
    - `completeness = COMPLETE`
    - `isComplete = true`
    - `totalVirupas = totalVirupas`
    - `totalRupas = totalRupas`
    - `deferredComponents = emptyList()`
  - If any required component is missing or uncomputable:
    - `completeness = PARTIAL_FOUNDATION`
    - `isComplete = false`
    - `totalVirupas = null`
    - `totalRupas = null`
    - `deferredComponents` lists the missing components.
  - For Lunar Nodes (Rahu / Ketu):
    - Classical Parashara Shadbala applies strictly to the 7 physical Grahas.
    - `completeness = UNSUPPORTED`
    - `isComplete = false`
    - `totalVirupas = null`, `totalRupas = null`

---

## 7. Ashtakavarga Rules (Phase 6.1)

### ASTRO-R39: Ashtakavarga Foundations & Conventions (`PARASHARA_CLASSICAL_V1`)
- **Tradition & Source**:
  - *Brihat Parashara Hora Shastra* (BPHS), Chapters 66–73 (Santhanam and Sharma editions).
  - Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapters 1–3.
- **Scope & Conventions**:
  - Evaluation of eightfold benefic and inauspicious planetary distributions across the 12 sidereal signs ($0..11$, Mesha to Meena).
  - Eight Contributors: Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, and Lagna (Ascendant).
  - Seven Target Planetary Tables (Bhinnashtakavarga): Sun, Moon, Mars, Mercury, Jupiter, Venus, and Saturn.
  - Sign-relative offset calculation:
    - If contributor is in sign $R_C \in [0..11]$ and contributes at classical 1-indexed relative house offset $k \in [1..12]$:
      $$\text{Target Sign Index} = (R_C + (k - 1)) \pmod{12}$$
  - Bindu / Rekha Convention:
    - Auspicious point contributed $= 1$ Bindu.
    - Inauspicious point $= 1$ Rekha ($8 - \text{binduCount}$ per sign in BAV).
  - Classical Bindu totals invariant across 12 signs for each BAV table:
    - Sun: 48 Bindus
    - Moon: 49 Bindus
    - Mars: 39 Bindus
    - Mercury: 54 Bindus
    - Jupiter: 56 Bindus
    - Venus: 52 Bindus
    - Saturn: 39 Bindus
    - Sarvashtakavarga Grand Total: $48 + 49 + 39 + 54 + 56 + 52 + 39 = 337$ Bindus.

### ASTRO-R39A: Surya Ashtakavarga (Sun's BAV — 48 Bindus)
- **Source**: BPHS Ch. 66; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 1, 2, 4, 7, 8, 9, 10, 11 (8 points)
  - From Moon: 3, 6, 10, 11 (4 points)
  - From Mars: 1, 2, 4, 7, 8, 9, 10, 11 (8 points)
  - From Mercury: 3, 5, 6, 9, 10, 11, 12 (7 points)
  - From Jupiter: 5, 6, 9, 11 (4 points)
  - From Venus: 6, 7, 12 (3 points)
  - From Saturn: 1, 2, 4, 7, 8, 9, 10, 11 (8 points)
  - From Lagna: 3, 4, 6, 10, 11, 12 (6 points)
- Total Bindus $= 8 + 4 + 8 + 7 + 4 + 3 + 8 + 6 = 48$.

### ASTRO-R39B: Chandra Ashtakavarga (Moon's BAV — 49 Bindus)
- **Source**: BPHS Ch. 67; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 3, 6, 7, 8, 10, 11 (6 points)
  - From Moon: 1, 3, 6, 7, 10, 11 (6 points)
  - From Mars: 2, 3, 5, 6, 9, 10, 11 (7 points)
  - From Mercury: 1, 3, 4, 5, 7, 8, 10, 11 (8 points)
  - From Jupiter: 1, 4, 7, 8, 10, 11, 12 (7 points)
  - From Venus: 3, 4, 5, 7, 9, 10, 11 (7 points)
  - From Saturn: 3, 5, 6, 11 (4 points)
  - From Lagna: 3, 6, 10, 11 (4 points)
- Total Bindus $= 6 + 6 + 7 + 8 + 7 + 7 + 4 + 4 = 49$.

### ASTRO-R39C: Mangala Ashtakavarga (Mars' BAV — 39 Bindus)
- **Source**: BPHS Ch. 68; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 3, 5, 6, 10, 11 (5 points)
  - From Moon: 3, 6, 11 (3 points)
  - From Mars: 1, 2, 4, 7, 8, 10, 11 (7 points)
  - From Mercury: 3, 5, 6, 11 (4 points)
  - From Jupiter: 6, 10, 11, 12 (4 points)
  - From Venus: 6, 8, 11, 12 (4 points)
  - From Saturn: 1, 4, 7, 8, 9, 10, 11 (7 points)
  - From Lagna: 1, 3, 6, 10, 11 (5 points)
- Total Bindus $= 5 + 3 + 7 + 4 + 4 + 4 + 7 + 5 = 39$.

### ASTRO-R39D: Budha Ashtakavarga (Mercury's BAV — 54 Bindus)
- **Source**: BPHS Ch. 69; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 5, 6, 9, 11, 12 (5 points)
  - From Moon: 2, 4, 6, 8, 10, 11 (6 points)
  - From Mars: 1, 2, 4, 7, 8, 9, 10, 11 (8 points)
  - From Mercury: 1, 3, 5, 6, 9, 10, 11, 12 (8 points)
  - From Jupiter: 6, 8, 11, 12 (4 points)
  - From Venus: 1, 2, 3, 4, 5, 8, 9, 11 (8 points)
  - From Saturn: 1, 2, 4, 7, 8, 9, 10, 11 (8 points)
  - From Lagna: 1, 2, 4, 6, 8, 10, 11 (7 points)
- Total Bindus $= 5 + 6 + 8 + 8 + 4 + 8 + 8 + 7 = 54$.

### ASTRO-R39E: Guru Ashtakavarga (Jupiter's BAV — 56 Bindus)
- **Source**: BPHS Ch. 70; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 1, 2, 3, 4, 7, 8, 9, 10, 11 (9 points)
  - From Moon: 2, 5, 7, 9, 11 (5 points)
  - From Mars: 1, 2, 4, 7, 8, 10, 11 (7 points)
  - From Mercury: 1, 2, 4, 5, 6, 9, 10, 11 (8 points)
  - From Jupiter: 1, 2, 3, 4, 7, 8, 10, 11 (8 points)
  - From Venus: 2, 5, 6, 9, 10, 11 (6 points)
  - From Saturn: 3, 5, 6, 12 (4 points)
  - From Lagna: 1, 2, 4, 5, 6, 7, 9, 10, 11 (9 points)
- Total Bindus $= 9 + 5 + 7 + 8 + 8 + 6 + 4 + 9 = 56$.

### ASTRO-R39F: Shukra Ashtakavarga (Venus' BAV — 52 Bindus)
- **Source**: BPHS Ch. 71; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 8, 11, 12 (3 points)
  - From Moon: 1, 2, 3, 4, 5, 8, 9, 11, 12 (9 points)
  - From Mars: 3, 5, 6, 9, 11, 12 (6 points)
  - From Mercury: 3, 5, 6, 9, 11 (5 points)
  - From Jupiter: 5, 8, 9, 10, 11 (5 points)
  - From Venus: 1, 2, 3, 4, 5, 8, 9, 10, 11 (9 points)
  - From Saturn: 3, 4, 5, 8, 9, 10, 11 (7 points)
  - From Lagna: 1, 2, 3, 4, 5, 8, 9, 11 (8 points)
- Total Bindus $= 3 + 9 + 6 + 5 + 5 + 9 + 7 + 8 = 52$.

### ASTRO-R39G: Shani Ashtakavarga (Saturn's BAV — 39 Bindus)
- **Source**: BPHS Ch. 72; Raman Ch. 2.
- **Contributions (1-indexed from contributor)**:
  - From Sun: 1, 2, 4, 7, 8, 10, 11 (7 points)
  - From Moon: 3, 6, 11 (3 points)
  - From Mars: 3, 5, 6, 10, 11, 12 (6 points)
  - From Mercury: 6, 8, 9, 10, 11, 12 (6 points)
  - From Jupiter: 5, 6, 11, 12 (4 points)
  - From Venus: 6, 11, 12 (3 points)
  - From Saturn: 3, 5, 6, 11 (4 points)
  - From Lagna: 1, 3, 4, 6, 10, 11 (6 points)
- Total Bindus $= 7 + 3 + 6 + 6 + 4 + 3 + 4 + 6 = 39$.

### ASTRO-R40: Sarvashtakavarga Aggregation (SAV Grand Total — 337 Bindus)
- **Source**: BPHS Ch. 73; Raman Ch. 3.
- **Rule**:
  - For each sign $s \in [0..11]$:
    $$\text{SAV}[s] = \sum_{P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}} \text{BAV}_P[s]$$
  - Per-sign Rekhas:
    $$\text{SAV\_Rekhas}[s] = 56 - \text{SAV}[s]$$
  - Grand Invariant Check:
    $$\sum_{s=0}^{11} \text{SAV}[s] = 337 \text{ Bindus}$$
    $$\sum_{s=0}^{11} \text{SAV\_Rekhas}[s] = 672 - 337 = 335 \text{ Rekhas}$$

### ASTRO-R41: Lunar Nodes (Rahu/Ketu) Ashtakavarga Policy
- **Rule**:
  - In classical Parashara Ashtakavarga, Rahu and Ketu do not participate as contributors or targets in the classical 337-Bindu Sarvashtakavarga.
  - Any request evaluating Rahu or Ketu BAV returns `completeness = UNSUPPORTED`, with null sign scores and explicit unsupported reason.

## 8. Ashtakavarga Shodhana Rules (Phase 6.2)

### ASTRO-R42: Trikona Shodhana (Triplicity Reduction)
- **Ruleset**: `PARASHARA_CLASSICAL_V1`.
- **Sources**: *Brihat Parashara Hora Shastra* (BPHS), Chapter 73; Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapter 4.
- **Triplicity Groups**:
  1. Fire (*Agni*): Mesha (0, Aries), Simha (4, Leo), Dhanus (8, Sagittarius)
  2. Earth (*Prithvi*): Vrishabha (1, Taurus), Kanya (5, Virgo), Makara (9, Capricorn)
  3. Air (*Vayu*): Mithuna (2, Gemini), Tula (6, Libra), Kumbha (10, Aquarius)
  4. Water (*Jala*): Karka (3, Cancer), Vrishchika (7, Scorpio), Meena (11, Pisces)
- **Reduction Logic**:
  For each triplicity $(s_0, s_1, s_2)$ with bindu counts $(v_0, v_1, v_2)$:
  1. **All 3 zeros**: $(0, 0, 0) \to (0, 0, 0)$.
  2. **Exactly 2 zeros**: The third non-zero figure is also reduced to 0 $\implies (0, 0, 0)$.
  3. **Exactly 1 zero**: No reduction is made in this triplicity. The two non-zero figures remain unchanged.
  4. **All 3 non-zero**:
     - *All three equal*: All three are reduced to 0: $(b, b, b) \to (0, 0, 0)$.
     - *Unequal figures*: Subtract the minimum figure $m = \min(v_0, v_1, v_2)$ from all three signs: $v_i' = v_i - m$.

### ASTRO-R43: Ekadhipatya Shodhana (Dual-Ownership Reduction)
- **Ruleset**: `PARASHARA_CLASSICAL_V1`.
- **Sources**: *Brihat Parashara Hora Shastra* (BPHS), Chapter 74; Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapter 5.
- **Pre-requisite**: Applied strictly *after* Trikona Shodhana to the figures resulting from Trikona Shodhana.
- **Dual-Ownership Pairs**:
  - Mars: Mesha (0, Aries) & Vrishchika (7, Scorpio)
  - Venus: Vrishabha (1, Taurus) & Tula (6, Libra)
  - Mercury: Mithuna (2, Gemini) & Kanya (5, Virgo)
  - Jupiter: Dhanus (8, Sagittarius) & Meena (11, Pisces)
  - Saturn: Makara (9, Capricorn) & Kumbha (10, Aquarius)
- **Single-Ownership Exemption**:
  - Moon: Karka (3, Cancer) — exempt, figure remains unchanged.
  - Sun: Simha (4, Leo) — exempt, figure remains unchanged.
- **Planetary Occupancy Rule**:
  - A sign is occupied if it contains at least one of the 7 classical physical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn) in D1.
  - Rahu and Ketu do NOT own signs and do NOT count as occupying planets. Lagna does not count as a planet.
- **Reduction Rules for Each Pair $(S_1, S_2)$ with Figures $(v_1, v_2)$**:
  1. **Zero Exemption**: If either sign has 0 bindus ($v_1 = 0$ or $v_2 = 0$), no reduction is made. Both retain their figures.
  2. **Both Occupied**: If both signs are occupied by at least one classical planet, no reduction is made.
  3. **Both Unoccupied**:
     - *If equal ($v_1 == v_2$)*: Both are reduced to 0.
     - *If unequal ($v_1 \ne v_2$)*: The larger figure is reduced to the smaller figure (both become $\min(v_1, v_2)$).
  4. **One Occupied ($v_{\text{occ}}$), One Unoccupied ($v_{\text{unocc}}$)**:
     - The occupied sign retains its figure ($v_{\text{occ}}$ unchanged).
     - *If $v_{\text{occ}} \ge v_{\text{unocc}}$*: The unoccupied sign's figure is removed (becomes 0).
     - *If $v_{\text{occ}} < v_{\text{unocc}}$*: The unoccupied sign's figure is reduced to the occupied figure ($v_{\text{unocc}} \leftarrow v_{\text{occ}}$).

### ASTRO-R44: Shodhita Sarvashtakavarga Aggregation
- **Sources**: BPHS Ch. 74; Raman Ch. 5.
- **Rule**:
  - Shodhita Sarvashtakavarga is derived by summing the 7 classical planets' Shodhita BAV charts sign by sign:
    $$\text{ShodhitaSAV}[s] = \sum_{P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}} \text{ShodhitaBAV}_P[s]$$
  - Grand total reduced bindus:
    $$\text{GrandTotalShodhitaBindus} = \sum_{s=0}^{11} \text{ShodhitaSAV}[s]$$
  - Invariant:
    $$\text{GrandTotalRawBindus (337)} \ge \text{GrandTotalTrikonaBindus} \ge \text{GrandTotalShodhitaBindus} \ge 0$$

## 9. Ashtakavarga Pinda Rules (Phase 6.3)

### ASTRO-R45: Rashi Pinda (Sign Multiplication)
- **Ruleset**: `PARASHARA_CLASSICAL_V1`.
- **Sources**: *Brihat Parashara Hora Shastra* (BPHS), Chapter 74/75; Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapter 6 ("Pindas"); *Phaladeepika*, Chapter 24; *Jataka Parijata*, Chapter 10.
- **Intermediate Data Source**: Strictly computed from **Shodhita Bhinnashtakavarga** (after Trikona Shodhana and Ekadhipatya Shodhana).
- **Rasi Gunakaras (Sign Multipliers)**:
  - Mesha (Aries, index 0): 7
  - Vrishabha (Taurus, index 1): 10
  - Mithuna (Gemini, index 2): 8
  - Karka (Cancer, index 3): 4
  - Simha (Leo, index 4): 10
  - Kanya (Virgo, index 5): 5
  - Tula (Libra, index 6): 7
  - Vrishchika (Scorpio, index 7): 8
  - Dhanus (Sagittarius, index 8): 9
  - Makara (Capricorn, index 9): 5
  - Kumbha (Aquarius, index 10): 11
  - Meena (Pisces, index 11): 12
- **Formula**:
  For each classical planet $P \in \{\text{Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn}\}$:
  $$\text{RasiPinda}_P = \sum_{s=0}^{11} \left( \text{ShodhitaBAV}_P[s] \times \text{RasiGunakara}[s] \right)$$

### ASTRO-R46: Graha Pinda (Planetary Multiplication)
- **Ruleset**: `PARASHARA_CLASSICAL_V1`.
- **Sources**: BPHS Ch. 74/75; Raman Ch. 6; *Phaladeepika* Ch. 24.
- **Graha Gunakaras (Planetary Multipliers)**:
  - Sun: 5
  - Moon: 5
  - Mars: 8
  - Mercury: 5
  - Jupiter: 10
  - Venus: 7
  - Saturn: 5
- **Exclusions**: Rahu and Ketu have no Gunakaras and do not participate.
- **Formula**:
  For each classical planet $P$:
  $$\text{GrahaPinda}_P = \sum_{G \in \{\text{7 planets}\}} \left( \text{ShodhitaBAV}_P[R_G] \times \text{GrahaGunakara}(G) \right)$$
  where $R_G \in [0, 11]$ is the sign occupied by planet $G$ in D1 (Rashi chart).
  If multiple planets occupy the same sign $R_G$, each planet's Gunakara is multiplied by the bindus in that sign and summed.

### ASTRO-R47: Shodhya Pinda (Composite Reduction Pinda)
- **Ruleset**: `PARASHARA_CLASSICAL_V1`.
- **Sources**: BPHS Ch. 74/75; Raman Ch. 6.
- **Formula**:
  $$\text{ShodhyaPinda}_P = \text{RasiPinda}_P + \text{GrahaPinda}_P$$
- **Invariant**:
  $$\text{ShodhyaPinda}_P \ge 0 \quad \forall P$$

### ASTRO-R48: Ashtakavarga Aggregate Pindas
- **Formula**:
  $$\text{TotalRasiPinda} = \sum_{P} \text{RasiPinda}_P$$
  $$\text{TotalGrahaPinda} = \sum_{P} \text{GrahaPinda}_P$$
  $$\text{TotalShodhyaPinda} = \sum_{P} \text{ShodhyaPinda}_P = \text{TotalRasiPinda} + \text{TotalGrahaPinda}$$
