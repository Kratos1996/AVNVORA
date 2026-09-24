# JKR Reference Validation

## Source and case

The full 183-page report is checked in
at [JKR PDF](JKR_#JKR-117480_ishant_sharma_1787393982115.pdf). Relevant extracted pages: p.2 birth
details/Panchang, p.6 planetary positions, p.7 Chalit, p.110 Shodashvarga, p.127 Vimshottari table.
Birth input is 11 Jul 1996 02:05 IST, Bikaner (28.0167 N, 73.3167 E), Lahiri. Page 2 gives 10 Jul
1996 20:35 UT; the corresponding JD is 2450275.357639. This corrects a stale value that was 12 hours
late.

## Golden coverage and statuses

`JkrReferenceGoldenTest.kt` currently has **31** executable test methods. The original 27-method
accounting has four additions: a live nine-planet comparison, a live complete Varga comparison,
local-sunrise behavior, and a Sripati midpoint comparison. This count is methods, not independent
calculations. There are separate contract and edge tests in `ReferenceComparisonStatusTest`,
`PanchangSolarDayTest`, and `HouseSystemTest`.

Current category counts (31 total): birth basics 1; planet/status 6; Panchang 8; Dasha 6; dosha
records 2; Varga 4; numerology record 1; Chalit 2; yoga record 1. A transparent vector inventory is:
one birth case, 160 Varga sign cells, nine classical planetary rows with multiple compared fields
each, seven Panchang items including sunrise and sunset, and four directly checked Chalit midpoints.
Do not add these heterogeneous rows into a misleading single count.

Use the seven `ReferenceComparisonStatus` values: `EXACT_MATCH`, `TOLERANCE_MATCH`,
`PROFILE_DIFFERENCE`, `ROUNDING_DIFFERENCE`, `CALCULATION_ERROR`, `UNSUPPORTED`, and
`REFERENCE_AMBIGUITY`. A tolerance result must name its tolerance and reason. Do not count
unsupported or ambiguous rows as matches.

| Domain                    | Comparison and outcome                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Core planets, JKR p.6     | Live chart agrees on sign, nakshatra, pada, motion, and Mercury combustion for 9/9 classical bodies. Longitudes are rounded in JKR; all nine deltas exceed half an arc-second (printed rounding bound), so 0 are exact and 0 qualify as rounding-only tolerance matches. JKR does not specify its ephemeris implementation, so all nine are `REFERENCE_AMBIGUITY`. Dignity labels match 4/9; Sun/Mars/Saturn/Rahu/Ketu differ under the engine's relationship conventions and remain `REFERENCE_AMBIGUITY`.                                                                                                                                                                                                                                                    |
| Vargas, JKR p.110         | All 16 charts × Lagna and 9 classical bodies (160 sign cells) match exactly when seeded with the p.6 sidereal longitudes and JKR's 23.8° Lahiri value. This validates the chart transformations against the table, not the upstream ephemeris. Uranus, Neptune, Pluto are recorded in JKR but unsupported by `BodyId` (48 cells).                                                                                                                                                                                                                                                                                                                                                                                                                              |
| Sripati Chalit, JKR p.7   | Distinct `SRIPATI_CHALIT_V1` implementation; it is not Equal House. The first four printed Bhava midpoints agree within 0.2° (JKR prints arcminutes and engine input is rounded). All nine claimed house placements (eight shifts, one unchanged) are `REFERENCE_AMBIGUITY`: the p.7 Start/Cusp/End row alignment and planet annotations conflict with the printed midpoint values, so occupancy is not counted as a match.                                                                                                                                                                                                                                                                                                                                    |
| Dasha, JKR pp.2, 4–5, 127 | Birth lord and sequence are checked. Active Rahu/Mercury/Mars periods match at an interior date in September 2026. Compared to JKR p.127 dates interpreted as 00:00 UTC, engine boundaries are: Rahu MD start +2.3457 days (22 May 2017), end +2.8457 days (22 May 2035). These are exact differences under that interpretation; the proper result remains `REFERENCE_AMBIGUITY` because JKR gives date-only endpoints and unspecified calendar/year/timezone rules. Birth balance computes as 3.869235 years versus the displayed 3y 10m 12d; its day conversion also depends on month/year convention. p.4 gives Mercury AD end 21 Nov 2027 while p.127 gives 30 Dec 2027 (39-day conflict); p.4 and p.5 differ by one day on Mars PD end (6 vs 7 Oct 2026). |
| Panchang, JKR p.2         | Tithi, local-sunrise Vara, Yoga, Karana, Nakshatra and sunrise/sunset are exercised. Sunrise and sunset use the engine's apparent solar-center altitude −0.833° approximation; times are accepted within 6 minutes (`TOLERANCE_MATCH`). `LOCAL_SUNRISE` uses coordinates and a fixed UTC offset; IANA DST transitions are not modeled. Civil UTC weekday is a different profile (`PROFILE_DIFFERENCE`). Polar sunless days report unsupported.                                                                                                                                                                                                                                                                                                                 |
| Transits                  | No dated transit-position/ingress/station vectors were found in the report. JKR prose mentions transits but does not provide comparable vectors; status `UNSUPPORTED`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |

Absolute sidereal longitude deltas (AYNVORA minus the rounded JKR p.6 value): Sun 0.006878°, Moon
0.000962°, Mars 0.010217°, Mercury 0.007408°, Jupiter 0.060552°, Venus 0.004514°, Saturn 0.146219°,
Rahu 0.003933°, Ketu 0.003933°. The half-arcsecond rounding bound is 0.000139°; unexplained
ephemeris/model differences are recorded as ambiguity rather than accepted under a broad tolerance.

### Panchang limb-by-limb result (JKR p.2)

| Limb      | JKR              | AYNVORA result/profile                                                                  | Status                                               |
|-----------|------------------|-----------------------------------------------------------------------------------------|------------------------------------------------------|
| Tithi     | Krishna Ekadashi | Krishna Ekadashi                                                                        | `EXACT_MATCH`                                        |
| Vara      | Wednesday        | Wednesday with `LOCAL_SUNRISE` at 28.0167 N, 73.3167 E, UTC+05:30; fixed-offset profile | `EXACT_MATCH` label; civil UTC is a separate profile |
| Nakshatra | Krittika         | Krittika                                                                                | `EXACT_MATCH`                                        |
| Yoga      | Shula            | Shoola (same limb, transliteration spelling normalized)                                 | `EXACT_MATCH`                                        |
| Karana    | Balava           | Balava                                                                                  | `EXACT_MATCH`                                        |
| Sunrise   | 05:47:55 local   | Solar-center altitude −0.833° approximation                                             | `TOLERANCE_MATCH`, ±6 min                            |
| Sunset    | 19:36:21 local   | Solar-center altitude −0.833° approximation                                             | `TOLERANCE_MATCH`, ±6 min                            |

## Reproducibility notes

The JKR PDF is the source for the extracted table values; the tests cite page numbers. A source
report is not mathematical authority for undocumented conventions. Keep unexplained ephemeris,
dignity-rule, and table-layout differences classified as ambiguity until the report or convention
resolves them. Existing Dasha boundary tolerances are not exact matches. Modern planets remain
outside the classical engine scope.
