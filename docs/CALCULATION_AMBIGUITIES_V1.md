# Calculation ambiguity register V1

This register records deterministic engine behavior separately from reference agreement. It does
not resolve traditional disagreements without source evidence.

| Case | Current behavior | Reference evidence | Status | Reason |
|---|---|---|---|---|
| JKR Chalit occupancy | Internal `SRIPATI_CHALIT_V1` computes 12 midpoint-based houses; degeneracy/reversed quadrants throw. The public `HouseSystem` enum has no matching selector. | Existing JKR comparisons cover four midpoints; full row/planet occupancy alignment is unresolved. | **AMBIGUOUS** | Reference table interpretation is insufficient to certify every occupancy. |
| Dignity labels | Current Parashara rule tables return deterministic labels. | JKR comparisons differ for five convention-sensitive dignity labels; cited passages do not establish one shared rule set. | **AMBIGUOUS** | No sourced convention resolution. |
| Dasha boundary dates | Period intervals are half-open `[startJD,endJD)` and deterministic. | JKR pages reportedly give conflicting printed boundary dates; see `REFERENCE_VALIDATION_JKR.md`. | **AMBIGUOUS** | Printed dates conflict and are not used to override the implemented interval convention. |
| Longitude bucket edges | Longitudes normalize to `[0,360)`; sign buckets are lower-inclusive/upper-exclusive. | Mathematical boundary tests, including 0°/360°, cover the implemented rule. | **RESOLVED** for code contract | Deterministic interval semantics are tested; this does not certify source longitude accuracy. |
| House boundary occupancy | Current calculators use deterministic half-open angular intervals; unsupported Placidus throws. | Whole/Equal rules are internally tested; JKR Chalit occupancy remains incomplete. | **AMBIGUOUS** for Chalit parity; **RESOLVED** for tested code intervals | Engine determinism is distinct from full JKR parity. |
| Nakshatra/Pada edges | 27 equal Nakshatra intervals and four equal Padas, lower-inclusive/upper-exclusive. | `ZodiacPropertyTest` covers exact boundaries and 360° normalization. | **RESOLVED** for code contract | Mathematical interval behavior has direct tests. |
| Varga boundaries | Registered Parashara strategy boundaries map deterministically with half-open division intervals. | `VargaEngineTest` and the limited cited JKR vectors cover selected cases. | **RESOLVED** for tested strategy behavior; broader school parity **NOT_VERIFIED** | No alternative school is implied by current rule IDs. |
| DST fold/gap local times | Curated resolver selects one offset; New York fold maps to DST offset and gap time is accepted with DST offset. No caller choice exists. | Behavior is specified by source rules and pinned by deterministic tests, not an independent tzdb comparison. | **AMBIGUOUS** | The API cannot express which fold occurrence is intended and gap values do not denote real local instants. |
| Polar sunrise/sunset | Local-sunrise Panchang requires a solar event; sunless cases fail. | Existing `PanchangSolarDayTest` exercises supported solar-day cases, not all polar latitudes/dates. | **UNSUPPORTED** on sunless days; broader polar range **NOT_VERIFIED** | No fallback day boundary is invented. |

Each case remains deterministic where the engine accepts the input. `AMBIGUOUS` means reference
agreement is unresolved; it does not mean the engine produces random values.
