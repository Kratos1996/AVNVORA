# Calculation Contract V1

**Status:** Contract baseline created; selected behavior is verified by existing tests, while multiple convention, date-range, provenance and platform questions remain partial or unverified.
**Contract version:** `V1`
**Authority:** `astro-engine` is the single calculation authority.

## 1. Authority and AI boundary

`astro-engine` is the only authority for astronomical and astrological numeric facts. `aynvora-core` exposes the SDK contract and delegates chart calculations to `astro-engine`; UI, repositories and AI must not duplicate or adjust calculation formulas.

AI may explain, summarize, synthesize and connect structured, validated engine or knowledge results. AI must not calculate or infer planetary positions, houses, Nakshatra/Pada, Dasha periods, Panchang values, aspects, Varga placements or other numeric facts; override an engine result; silently repair it; or state unsupported results as facts. Missing or unsupported evidence remains missing or unsupported.

Calculation changes require an explicit profile/ruleset version change or a documented correction with reference evidence and regression vectors. Language, UI, platform, device locale and AI request must never change the selected calculation convention.

## 2. Current public calculation inventory

Inputs and outputs below are immutable Kotlin data classes/enums where represented by SDK models. `AynvoraSdk` is the facade; calculations delegate through `AstroEngineAdapter` to `AynvoraAstroEngine`.

| Public operation | Input | Output / facts | Current contract and limits |
|---|---|---|---|
| `calculateChart(ChartRequest)` | `BirthData` (civil date/time, latitude/longitude, timezone ID) plus `CalculationConfig` | Engine version/status, model label, Julian Day, ayanamsa, Lagna, 12 houses and occupancy, 9-body positions, aspects, motion/combustion states, requested Vargas, dignity/relationships, Shadbala, Ashtakavarga/Shodhana/Pindas | Main complete result. Defaults: `STANDARD_VEDIC`, `LAHIRI_CHITRAPAKSHA`, `EQUAL_HOUSE`, `PARASHARA_CLASSICAL_V1`. Unsupported profile/convention combinations must fail explicitly; actual enum/registry mismatches are listed below. |
| `calculateDivisionalChart(s)` | Same chart request plus one or more `DivisionalChart` IDs | Varga placements for supported D1, D2, D3, D4, D7, D9, D10, D12, D16, D20, D24, D27, D30, D40, D45, D60 | Parashara ruleset ID is reported. JKR Phase 8.2 reports 160/160 sign-cell matches for 16 Vargas using its cited inputs; this does not certify all schools or all longitudes. |
| `calculateDignities` | Chart request and optional Varga | Sign dignity facts (own sign, exaltation/debilitation, Moolatrikona and related defined labels) | Classical rule implementation; source and some boundary/label mapping ambiguities remain. |
| `calculateRelationships` | Chart request and optional Varga | Planetary relationship classifications | Rule-based; label parity remains partial for convention-dependent JKR values. |
| `calculateShadbala` | Chart request | Six-fold strength component results and completeness metadata | Component implementation exists. Per-component omissions/unsupported conditions must remain explicit; see the Shadbala phase report. |
| `calculateAshtakavarga` | Chart request | Bhinnashtakavarga and Sarvashtakavarga | Parashara classical ruleset default. |
| `calculateShodhitaAshtakavarga` | Chart request | Trikona/Ekadhipatya Shodhana and reduced SAV | Parashara classical ruleset default. |
| `calculateAshtakavargaPinda` | Chart request | Rashi, Graha and Shodhya Pinda outputs | Depends on Ashtakavarga and Shodhana input. |
| `calculateDasha` | Birth Julian Day, Moon sidereal longitude, Antardasha/Pratyantardasha flags | `VimshottariDashaTimeline` (Mahadasha, optional Antardasha/Pratyantardasha) | `PARASHARA_VIMSHOTTARI_120_V1`; uses 365.25 days per Dasha year. Printed reference boundary dates conflict; exact boundary certification is partial. |
| `calculateTransit` | Julian Day and ayanamsa string | Snapshot of the same 9 bodies and current motion | Default `LAHIRI_CHITRAPAKSHA`; model is analytical, not Swiss/JPL data. Transit reference vectors are not present in the JKR source. |
| `calculatePanchang` | Julian Day and ayanamsa string | Tithi, Nakshatra, Yoga, Karana, Vara and related 5-limb values | Default Lahiri. Vara has `CIVIL_UTC` and `LOCAL_SUNRISE` paths; sunrise needs coordinates and the local-sunrise profile. No worldwide holiday data is part of this calculation. |

Every public astrology calculation result carries immutable `CalculationMetadata`: full `ChartResult`, the `AynvoraResult.Success.calculationMetadata` envelope for chart-derived SDK results (Varga, dignity, relationship, Shadbala, Ashtakavarga and Pinda), `VimshottariDashaTimeline`, `TransitSnapshot`, `TransitTimeline`, and `PanchangSnapshot`. Metadata records V1, a ruleset/profile ID, engine/model/source identity, nullable data version, conservative redistribution status, and applicable conventions. Analytical calculators use a null data version because no external versioned ephemeris file is used. `TransitTimeline` carries the same metadata as its snapshots. Metadata is descriptive and does not modify numeric outputs.

## 3. Inputs, time, coordinates and precision

### Chart request

- Civil date: year `1..9999`, month `1..12`, day `1..31`; time: `00:00:00..23:59:59`.
- Geographic latitude: `-90..90` degrees; longitude: `-180..180` degrees. Lagna/house and local-sunrise calculations require location; the geocentric planetary longitude algorithms do not use birthplace coordinates.
- Timezone: required non-empty string. `TimeNormalizer` accepts UTC/GMT/Z, numeric offsets, and the curated regional IDs/abbreviations listed below. It is **not** an IANA tzdb implementation. Unknown IDs are rejected.
- UTC civil time is converted to Julian Day using the Gregorian formula documented as Jean Meeus, *Astronomical Algorithms*, 2nd ed., Ch. 7. The engine uses that Julian Day in polynomial/analytical formulae. A separate UT-to-TT/Delta-T conversion is not present in the inspected path.
- The arithmetic uses the proleptic Gregorian calendar for years `1..9999`. Invalid month/day combinations are rejected; historical Julian/Gregorian adoption is not modeled.
- Timezone behavior is exactly the curated resolver behavior, not a claim of historical local-time correctness. For New York-style US rules, fold hour `01:xx` on the first Sunday in November resolves to the DST offset (the earlier occurrence); the spring gap hour `02:xx` on the second Sunday in March is accepted with the post-change DST offset although that wall time does not exist. No fold choice is exposed. Europe rules compare local hour to `01:00` on the last Sundays of March/October. Sydney uses a coarse October-through-March seasonal rule with no transition-day check. These are deterministic approximations; historical and future transition fidelity is **NOT_VERIFIED**.

#### Curated timezone identifiers and rules

- Fixed offsets/IDs: `UTC`, `GMT`, `Z`, and signed offsets with optional `UTC`/`GMT` or `Etc/` prefix as parsed by `TimeNormalizer`. The current parser does not enforce a canonical IANA offset range.
- Fixed regional IDs: `Asia/Kolkata`, `Asia/Calcutta`, `Asia/Colombo`, `Asia/Kathmandu`, `Asia/Katmandu`, `Asia/Dhaka`, `Asia/Dacca`, `Asia/Karachi`, `Asia/Dubai`, `Asia/Tokyo`, `Asia/Singapore`, `Asia/Hong_Kong`, `Asia/Shanghai`, `Asia/Bangkok`, `America/Phoenix`, `Pacific/Honolulu`.
- Rule-based US IDs/aliases: New York/Eastern/EST/EDT; Chicago/Central/CST/CDT; Denver/Mountain/MST/MDT; Los Angeles/Pacific/PST/PDT; Anchorage. Applies current US second-Sunday-March / first-Sunday-November hour rules to all years.
- Rule-based European IDs/aliases: London/WEP; Paris, Berlin, Rome, Madrid, Amsterdam/CET/CEST; manual last-Sunday-March/October rules.
- Australia: Sydney/AEST/AEDT use a month-only seasonal offset. `IST` is treated as India fixed offset. Ambiguous abbreviations are resolved using these specific mappings, not a global abbreviation database.
- Supported transition history/future range: **NOT_VERIFIED**. Unsupported IDs throw `IllegalArgumentException`. Fold/gap inputs are accepted with one deterministic offset rule; invalid Gregorian dates throw. Offset rollover updates the civil date before Julian Day conversion.

### Coordinates and number handling

- Position outputs are geocentric ecliptic longitudes in decimal degrees, with tropical and sidereal values. Longitudes are normalized to `[0°, 360°)`; sign, Nakshatra and Pada intervals are lower-inclusive / upper-exclusive in the boundary tests.
- Results retain `Double` values. The engine does not define one global output-rounding rule; formatting/rounding belongs to presentation and must not be fed back into calculations. JKR printed longitudes are rounded to arcseconds, so tests use the documented half-arcsecond tolerance where applicable.
- `ACCURACY_ENVELOPE = NOT_VERIFIED`. Accepted civil input years `1..9999` are not an astronomical validity range. The defensible evidence range is only the discrete cited test vectors (Meeus examples/J2000 and the dated JKR sample); continuous or epoch-wide accuracy is **NOT_VERIFIED**. Present evidence does not justify a broader supported scientific range.
- No bundled ephemeris data file was found under `astro-engine` or the repository's ephemeris-named asset search. The engine computes from formulas/coefficients in Kotlin source.

## 4. Ephemeris and source audit

| Component | Implementation/source found | Version/data package | License evidence in repository | Redistribution status |
|---|---|---|---|---|
| Sun longitude | `astro-engine/.../planets/SunCalculator.kt`; comments cite Meeus, *Astronomical Algorithms*, Ch. 25; apparent longitude corrections are implemented in source | No independent source commit or data-file version | No license grant for a commercial redistribution package recorded in the inspected repo | **COMMERCIAL_REDISTRIBUTION_STATUS = NOT_VERIFIED** |
| Moon longitude | `.../planets/MoonCalculator.kt`; Meeus Ch. 47 principal arguments/periodic terms plus selected planetary and nutation terms; source comment estimates about 1–2 arcminutes | Formula subset is in source; no full ELP-2000/82 coefficient data package/version discovered | No commercial redistribution grant recorded | **NOT_VERIFIED** |
| Mercury–Saturn | `.../planets/PlanetaryCalculator.kt`; code contains heliocentric Keplerian orbital elements with secular terms; comments cite Simon et al. (1994), Meeus Chs. 31–33 and major Jupiter/Saturn perturbations | Coefficients compiled in source; no versioned external ephemeris file | No commercial redistribution grant recorded | **NOT_VERIFIED** |
| Rahu/Ketu | `.../planets/LunarNodesCalculator.kt`; mean ascending node per Meeus Ch. 47; Ketu is exactly 180° opposite Rahu | Analytical formula in source; no data package | No commercial redistribution grant recorded | **NOT_VERIFIED** |
| Lahiri ayanamsha | `.../ayanamsa/AyanamsaCalculator.kt`; polynomial anchored at J2000 and a stated Lahiri value/rate | Formula in source; no data package | No commercial redistribution grant recorded | **NOT_VERIFIED** |
| Julian Day / sidereal time | `.../time/JulianDay.kt`, `SiderealTimeCalculator.kt`; comments cite Meeus Chs. 7, 12 and 22 | Formulae in source | No commercial redistribution grant recorded | **NOT_VERIFIED** |

The public result's existing `calculationModel = "MEEUS_VSOP87"` is preserved for compatibility, but inspected `PlanetaryCalculator` implements orbital-element/Keplerian calculations and does not establish that a full VSOP87 coefficient series is present. Treat that field as a **legacy model label with unverified exactness**, not as proof of a VSOP87 implementation. This contract does not rename it or change output values.

No third-party ephemeris binary or coefficient data license was found in the inspected repository paths. This absence is not commercial permission. No license is declared verified by this audit. A commercial release is blocked on a documented rights review for the actual implementation, source-derived coefficients, dependencies and distribution model.

## 5. Frozen conventions for this contract

These IDs describe current behavior; they do not imply that every option in the public enums is implemented.

| Area | V1 behavior / profile ID | Status and notes |
|---|---|---|
| Primary profile | `STANDARD_VEDIC` | Implemented and SDK-tested. `SURYA_SIDDHANTA` and `DRIG_GANITA` now return `UnsupportedConfiguration`; no distinct calculation path is present. |
| Zodiac | Tropical intermediate + sidereal output | Calculators produce tropical/apparent longitude and subtract the selected ayanamsha for sidereal longitude. |
| Ayanamsha | `LAHIRI_CHITRAPAKSHA` default; `TROPICAL` zero-offset option | Lahiri and tropical are implemented. `RAMAN` and `KRISHNAMURTI_KP` are rejected by resolver/SDK. |
| House system | `EQUAL_HOUSE` default; `WHOLE_SIGN` | Both are implemented. Internal engine `SRIPATI_CHALIT_V1` is implemented but only partially verified against JKR; no matching public enum value currently selects it. `SHRIPATI_PORPHYRY` is not assumed to alias Sripati. Public `PLACIDUS` resolves to an unsupported placeholder. |
| Bodies | `SUN`, `MOON`, Mercury, Venus, Mars, Jupiter, Saturn, Rahu, Ketu | Nine bodies. Uranus, Neptune and Pluto are not in the public `BodyId` calculation set. |
| Nodes | `MEAN_NODE_V1` | Rahu is mean ascending lunar node; Ketu is exactly opposite; both are marked retrograde by rule. No true-node profile is established. |
| Retrograde | Finite-difference apparent longitude | Planetary code estimates daily motion by evaluating a short forward time step (`0.002` day for Mercury–Saturn; `0.001` day for Moon); retrograde iff normalized signed delta is negative. Station behavior/tolerance is not separately frozen. Mean nodes always retrograde. |
| Combustion | `PARASHARA_CLASSICAL_V1` thresholds | Moon 12°, Mars 17°, Mercury 14° direct/12° retrograde, Jupiter 11°, Venus 10° direct/8° retrograde, Saturn 15°; inclusive threshold (`separation <= threshold`). Sun and nodes are not applicable. |
| Aspects | `STANDARD_MAJOR` | Conjunction 0°/8° orb, sextile 60°/6°, square 90°/7°, trine 120°/8°, opposition 180°/8°; applied to sidereal longitudes; lunar nodes included by default. This is the code's current aspect profile, not a claim that every Vedic aspect school is represented. |
| Dignity / relationship | `PARASHARA_CLASSICAL_V1` labels | Rule data is in `PlanetaryDignityCalculator` and relationship calculator. JKR label discrepancies remain ambiguous; this contract does not choose a new interpretive convention. |
| Varga | `PARASHARA_CLASSICAL_V1` | 16 chart strategies are registered. Each has strategy-specific sign mapping and boundary behavior; a profile must accompany future alternatives. JKR sign-cell validation was 160/160 for its checked sample only. |
| Dasha | `PARASHARA_VIMSHOTTARI_120_V1` | 120-year Vimshottari sequence; year length 365.25 days; period intervals `[startJD,endJD)`. `calculateDasha` accepts a supplied Moon sidereal longitude, so caller must use the same profile and must not round it. |
| Panchang | `PANCHANG_CLASSICAL_V1` (contract alias) | Tithi from Moon–Sun sidereal elongation; Nakshatra from Moon sidereal longitude; Yoga from summed sidereal longitudes; Karana from half-Tithi; Vara by explicit `CIVIL_UTC` or `LOCAL_SUNRISE` choice. Local sunrise requires location. The alias is contract documentation; no new engine selector is added in V1. |
| Timezone | `CURATED_OFFSET_RULES_V1` | Fixed offsets and curated/manual DST approximations; not IANA historical data. Folds use one implicit offset and gaps are accepted with the resolver's offset; neither is explicitly disambiguated. |

### Existing option mismatches that must remain visible

- Public `AyanamsaConvention` lists Raman and Krishnamurti KP, but `AyanamsaCalculator` explicitly rejects them.
- Public `HouseSystem` lists Placidus and `SHRIPATI_PORPHYRY`; `SHRIPATI_PORPHYRY` is not the registered ID `SRIPATI_CHALIT_V1`, and the Placidus implementation throws unsupported.
- Public calculation profile enum lists `SURYA_SIDDHANTA` and `DRIG_GANITA`; the SDK explicitly rejects both rather than calculating them as Standard Vedic.
- Keep these values unsupported until explicit mappings, implementation and reference vectors exist. Do not silently coerce them to the defaults.

## 6. Supported calculations: detailed contract summary

| Calculation | Coordinate/system assumptions | Time/location | Precision/rounding and boundaries | Reference/tests | Contract status |
|---|---|---|---|---|---|
| Sun and planetary longitude | Geocentric apparent tropical ecliptic longitude; sidereal conversion subtracts selected ayanamsha | JD from UTC civil normalization; place does not affect geocentric position | Decimal `Double`; normalized `[0,360)`; no end-to-end precision bound or validated epoch range | Meeus worked examples in `PlanetaryGoldenTest`, J2000 checks, JKR chart comparison | **PARTIAL**; source model identifiable, external full-range accuracy and licensing not verified |
| Moon longitude | Geocentric apparent tropical ecliptic longitude; selected terms and corrections | JD from UTC; no birthplace coordinate dependency | Decimal `Double`; normalized; code comment estimates 1–2 arcminutes, not a complete independent accuracy certification | Meeus/J2000 and JKR checks | **PARTIAL** |
| Nodes | Mean ascending node + exact opposition | JD from UTC | Ketu = normalize(Rahu + 180°); node direction forced retrograde | J2000 and 100-epoch opposition property tests | **VERIFIED** for implemented invariant; mean-vs-true node is a frozen convention |
| Lagna / houses | Tropical ascendant from sidereal time and obliquity; sidereal conversion; selected house profile | Requires UTC JD and latitude/longitude; house systems differ | Degree output; house interval behavior and high-latitude degeneracy need profile-specific handling | Lagna and house tests; JKR Sripati first four midpoints within 0.2° | **PARTIAL**; full Chalit occupancy and high-latitude support not certified |
| Zodiac / Nakshatra / Pada | Sidereal longitude after ayanamsha; equal 12 signs, 27 Nakshatras, 4 Padas each in current engine | No place dependency after longitude exists | `[start,end)`; 360° normalizes to 0° | `ZodiacPropertyTest`; JKR 9/9 sign/Nakshatra/Pada labels | **VERIFIED** for coded boundaries; external longitude accuracy remains partial |
| Varga | Transform sidereal source longitude under named Parashara strategy | Same chart time; no extra place input beyond Lagna transformation | Division intervals `[start,end)`; 360° normalization tested; no display rounding in transform | Varga boundary tests; JKR 160/160 checked sign cells | **VERIFIED** for tested strategies/vectors; school-wide parity not claimed |
| Dasha | Moon sidereal longitude selects Nakshatra lord and balance | Input birth JD and unrounded sidereal Moon longitude | Half-open JD periods; 365.25-day year; printed reference dates disagree at boundaries | Dasha tests plus JKR active period test; JKR source conflict logged | **PARTIAL / AMBIGUOUS** at exact external date boundaries |
| Transit | Same body model and sidereal profiles as natal positions | Input JD; no location for longitude snapshot | Timeline sampling step is caller supplied; no independent transit golden vectors found in JKR material | Determinism/range tests | **PARTIAL** |
| Panchang | Tithi, Nakshatra, Yoga, Karana from current sidereal solar/lunar coordinates; Vara profile explicit | Input JD; `LOCAL_SUNRISE` additionally requires coordinates and offset | Angular bucket boundaries implemented; day boundary differs by Vara profile; polar sunless days unsupported in solar-day code | Panchang tests; JKR sunrise/sunset comparison within 6 minutes at one case | **PARTIAL**; regional authority and broad date/location validation absent |
| Aspects / dignity / relationship / states | Named current profiles above, primarily sidereal positions | Derived from chart facts | Aspect threshold includes `orb <= allowedOrb + 1e-9`; combustion includes threshold. Other class-specific boundaries are code-defined. | Dedicated tests and JKR comparison | **PARTIAL** where external traditional labels differ or sources are ambiguous |
| Shadbala / Ashtakavarga | `PARASHARA_CLASSICAL_V1` rulesets | Derived from chart/time inputs | Rule/component specific; no single global rounding policy | Existing component, parity and SDK tests | **PARTIAL** until every component, exception and result is validated against independent vectors |

## 7. Boundary and exception matrix

Status labels reflect present code and cited evidence; `PASS` applies only to the stated invariant/tested slice.

| Case | Status | Evidence / limitation |
|---|---|---|
| Longitude normalization at 0°/360° and sign edges | **PASS** | `ZodiacPropertyTest`; longitudes use half-open intervals. |
| Nakshatra/Pada exact bucket edges | **PASS** | `ZodiacPropertyTest` includes Ashwini/Pada and Bharani boundaries. |
| Varga division edges and 360° normalization | **PASS** | `VargaEngineTest` boundary cases for listed strategy mappings. |
| Dasha internal containment | **PASS** for code interval | `DashaPeriod.contains` is start-inclusive/end-exclusive; external printed boundary dates remain disputed. |
| Dasha source-date boundary parity | **AMBIGUOUS** | JKR pages reportedly conflict on exact dates; no forced match. |
| Chalit/Sripati midpoints | **PARTIAL** | First four JKR midpoints within 0.2°; remaining row/occupancy interpretation unresolved. |
| House cusp/boundary/high-latitude behavior | **PARTIAL** | Whole/Equal interval tests exist; Placidus is unsupported; degenerate/reversed Sripati quadrants throw; high-latitude range not certified. |
| Timezone fixed offsets and date rollover | **PASS** for tested cases | `JulianDayTest` covers offsets and cross-midnight normalization. |
| Historical timezone/DST transitions | **NOT_VERIFIED** | Hand-coded resolver; no versioned tzdb or broad historical vectors. |
| DST fold/gap mapping | **PARTIAL / AMBIGUOUS** | One implicit offset is selected; no fold selector. Gap inputs are accepted, not rejected. Tests lock only current New York resolver behavior. |
| Calendar date validation | **PASS** for proleptic Gregorian validity | Month-specific day validity and leap years are checked by `TimeNormalizer` and `JulianDay`; historical calendar adoption is not modeled. |
| Planetary sign-transition dates / retrograde stations | **PARTIAL** | Tests check selected positions/states; no broad independently sourced transition/station vector suite. |
| Panchang day/Tithi boundary | **PARTIAL** | Rule-level buckets exist; broad authoritative date/location vectors and cross-school convention matrix are missing. |
| Polar sunrise/sunset | **UNSUPPORTED** on sunless days | Solar-day code reports unsupported where polar daylight is absent. |
| Longitude accuracy over all accepted years | **NOT_VERIFIED** | Input range is not a certified ephemeris validity range; no full-range truth set or error envelope. |

Expanded descriptions and exact reasons are in [CALCULATION_AMBIGUITIES_V1.md](CALCULATION_AMBIGUITIES_V1.md).

## 8. Existing reference vectors and test coverage

No expected astronomical values were invented in this phase. Existing sources/tests remain the source of golden values:

- `astro-engine/src/commonTest/kotlin/com/aynvora/astro/JkrReferenceGoldenTest.kt`: JKR-117480 reference case; source pages cited in test. Earlier Phase 8.2 report records 31 executable tests and 9/9 classical body sign/Nakshatra/Pada matches; JKR values are not independently verified from the PDF unless the cited page is available.
- `PlanetaryGoldenTest.kt`: Meeus examples/J2000 sanity checks, mean nodes and Lahiri epoch value.
- `JulianDayTest.kt`: Meeus JD example, fixed offsets and civil-date rollover.
- `JulianDayTest.kt`: added proleptic Gregorian invalid-date checks and deterministic New York DST fold/gap mapping. These values characterize the resolver's rules, not independent astronomy or IANA parity.
- `ZodiacPropertyTest.kt`, `VargaEngineTest.kt`: sign/Nakshatra/Pada/Varga mathematical boundaries.
- `HouseSystemTest.kt`, `LagnaCalculatorTest.kt`: house/Lagna behavior, including Placidus unsupported case.
- `DashaTransitPanchangTest.kt`, `PanchangSolarDayTest.kt`: selected deterministic period, transit and Panchang checks.
- `PlanetStateCalculatorTest.kt`, `AspectCalculatorTest.kt`, `DignityRelationshipTest.kt`: state/aspect/dignity behaviors.
- Shadbala and Ashtakavarga component tests plus `aynvora-core` SDK integration tests.

Shared parity preparation uses `astro-engine/src/commonTest`: the fixtures and tests are in Kotlin Multiplatform `commonTest`, with Android and JVM targets declared by `astro-engine/build.gradle.kts`. The same shared suite can be wired to an iOS target later; this phase does not fabricate one. No standalone versioned cross-platform vector exchange file exists yet. A future vector set must preserve source URL/page, convention ID, units, tolerance, attribution/license review and input provenance for every expected value.

## 9. Versioning and result traceability

- Calculation Contract V1 ID: `V1`.
- Every public astrology result listed in §2 includes immutable `CalculationMetadata`; chart conventions, Dasha rules/year length, transit ayanamsha, and Panchang ayanamsha/Vara convention are retained.
- Current engine version in inspected source: `0.3.0`; adapter metadata build label: `astro-b3`. These are source constants, not a signed release identity.
- Current analytical source ID: `ANALYTICAL_MEEUS_SIMON_FORMULAE`. External ephemeris data version is null because no such versioned data asset was found.
- Commercial redistribution status is `NOT_VERIFIED`.
- The AI `calculateBirthChart` tool retains chart `CalculationMetadata` as a typed result field. This preserves deterministic provenance and does not make AI a calculation authority.
- Metadata is descriptive and must never alter numeric calculation results.

## 10. Platform execution status

| Platform | Current repository target | Shared calculation code | Executed parity evidence |
|---|---|---|---|
| Android | `androidApp` plus Android KMP library targets | Yes | Android build/device checks exist in prior reports; no claim of complete Android vs Desktop numerical parity from this phase. |
| Desktop | `desktopApp` JVM target | Yes | JVM tests/build available; no claim of cross-platform parity solely from JVM results. |
| iOS | No iOS Gradle target or Xcode project/workspace found in current checkout | Shared `commonMain` is designed for reuse but not built as iOS here | **NOT_VERIFIED**; do not claim iOS support or parity. |

## 11. Remaining P0 blockers

1. Obtain and record a legal/commercial redistribution decision for the analytical source implementation, formula coefficients, dependencies and any future ephemeris assets. Until then, status remains `COMMERCIAL_REDISTRIBUTION_STATUS = NOT_VERIFIED`.
2. Establish a defensible calculation-model identity. Reconcile the legacy `MEEUS_VSOP87` label with the actual orbital-element code only through a separately reviewed, versioned compatibility change; do not alter V1 calculations silently.
3. Declare a supported epoch range and quantitative error budget using independent reference vectors across the range; current civil input acceptance is not an accuracy claim.
4. Continue to expose enum/profile mismatches accurately: Raman/KP, Surya Siddhanta/Drig Ganita, Placidus and public Sripati naming remain unsupported or not selectable and must continue to fail explicitly.
5. Replace/upgrade curated timezone rules with versioned IANA tzdb behavior or explicitly constrain supported birth epochs/IDs and define gap/fold policy.
6. Complete unresolved JKR Chalit occupancy, dignity labels and Dasha date boundaries with source review or leave them `AMBIGUOUS`/`UNSUPPORTED`.
7. Add externally sourced golden vectors for transit/stations, calendar/Panchang boundaries, timezone transitions and broad place/epoch coverage. Keep source citations and tolerances attached. Existing tests verify deterministic rules but do not establish a scientific accuracy bound.
9. Run actual multi-platform parity when iOS is separately configured. Until then status remains `NOT_VERIFIED`.

## Source map

- Public facade and immutable result models: `aynvora-core/src/commonMain/kotlin/com/aynvora/core/AynvoraSdk.kt`, `.../models/BirthData.kt`, `ChartRequest.kt`, `CalculationConfig.kt`, `ChartResult.kt`; shared provenance is `astro-engine/src/commonMain/kotlin/com/aynvora/astro/provenance/CalculationMetadata.kt`.
- Engine: `astro-engine/src/commonMain/kotlin/com/aynvora/astro/AstroEngine.kt` and calculation packages `planets`, `time`, `ayanamsa`, `houses`, `zodiac`, `varga`, `dasha`, `transit`, `panchang`, `aspects`, `states`, `dignity`, `relationship`, `shadbala`, `ashtakavarga`.
- Adapter: `aynvora-core/src/commonMain/kotlin/com/aynvora/core/internal/AstroEngineAdapter.kt`.
- JKR findings: `docs/PHASE_8_2_ASTROLOGY_REFERENCE_PARITY.md`, `docs/REFERENCE_VALIDATION_JKR.md`, and `astro-engine/src/commonTest/kotlin/com/aynvora/astro/JkrReferenceGoldenTest.kt`.
- AI/tool boundary: `docs/AI_TOOL_CONTRACT.md`.
