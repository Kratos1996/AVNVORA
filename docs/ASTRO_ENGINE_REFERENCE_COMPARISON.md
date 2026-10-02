# Astro engine reference comparison

**Purpose:** engineering capability and validation inventory, not an accuracy ranking.
**Reference provider:** VedAstro repository commit `fcb4dede360372545eb244c53e9a80ec3510e194` ([audit](REFERENCES/VEDASTRO_REFERENCE.md)).
**Authority:** AYNVORA `astro-engine` remains the only production calculation authority. VedAstro results, when later collected, must be labeled `REFERENCE_ONLY`.

## Capability and validation matrix

| Feature | AYNVORA status | VedAstro source status | Reference implementation locations | AYNVORA adoption decision | Independent validation required |
|---|---|---|---|---|---|
| Planetary positions | PARTIAL: analytical Sun/Moon/Mercury–Saturn/mean nodes; limited cited vectors; no continuous validated epoch bound | Calculators and SwissEphNet usage are present | AYNVORA `astro-engine/.../planets/*Calculator.kt`; VedAstro `Library/Logic/Calculate/Core.cs`, `Library.csproj` | Keep AYNVORA formula provider; compare only after profile/time alignment | Yes: cited Meeus examples/J2000 are partial; add traceable independent multi-epoch vectors |
| Houses | PARTIAL: Equal and Whole Sign implemented; public Placidus unsupported; Sripati internal validation partial | House and chart pathways exist | AYNVORA `houses/HouseSystem.kt`; VedAstro `Core.cs`, `ChartType.cs`, chart factories | Do not coerce systems or copy house outputs | Yes: house cusps and assignment boundaries |
| Lagna | PARTIAL: existing deterministic implementation with selected tests | Core calculation path exists | AYNVORA `lagna/LagnaCalculator.kt`; VedAstro `Core.cs` | Preserve contract profile and input provenance | Yes: independent sidereal-time/ascendant cases across latitudes |
| Nakshatra and Pada | VERIFIED for coded interval mapping; longitude accuracy remains partial | Constellation data and calculators exist | AYNVORA `zodiac/ZodiacCalculator.kt`; VedAstro `Core.cs`, `Library/Data/Constellation.cs` | Do not adopt reference values as truth | Yes: boundary tests against an independent ephemeris |
| Varga | VERIFIED for current registered Parashara strategies and cited 160-cell JKR sample; school-wide parity partial | Varga helper and chart types exist | AYNVORA `varga/VargaCalculationStrategy.kt`; VedAstro `Vargas.cs`, chart factories | Retain named AYNVORA strategy; compare each varga and convention | Yes: each requested division and cusp/boundary vector |
| Dasha | PARTIAL: Vimshottari Maha/Antar/Pratyantar; 365.25-day convention; request-based facade added | Nested Dasa periods/current Dasa APIs exist | AYNVORA `dasha/VimshottariDashaCalculator.kt`; VedAstro `VimshottariDasa.cs`, `DasaEvent.cs` | Keep AYNVORA ruleset and interval boundary; no formula copying | Yes: traceable fixtures and boundary-date comparison |
| Transit / Gochara | PARTIAL: requested instant works; outputs analytical positions; natal interaction support limited | Transit/time-range event chart APIs exist | AYNVORA `transit/TransitCalculator.kt`, core request model; VedAstro `EventsChart.cs`, `EventManager.cs`, `EventsChartAPI.cs` | Preserve request's explicit datetime and profile | Yes: timestamp-aligned longitude and interaction vectors |
| Panchang | PARTIAL: five limb values and local-sunrise pathway; source/calendar edge cases remain | Panchang/day data and solar-event SwissEph path exist | AYNVORA `panchang/PanchangCalculator.kt`; VedAstro `Core.cs`, `PanchangaTable.cs` | No borrowed values; keep vara convention explicit | Yes: independently sourced local-date, sunrise and boundary fixtures |
| Bhava Chalit | AMBIGUOUS: no verified cusp convention in current snapshot UI; correctly refuses a substitute chart | `BhavaChalit` type and house code exist | AYNVORA `houses/HouseSystem.kt`, Chalit feature; VedAstro `ChartType.cs`, `Core.cs`, chart factories | Do not enable until a convention and fixtures are frozen | Yes: cusps, house assignment and cusp-boundary tests |
| Shadbala | PARTIAL: components implemented, but complete component status/provenance parity not certified | Strength calls and related types are present; full component audit not established | AYNVORA `shadbala/ShadbalaCalculator.kt`; VedAstro `Core.cs`, `Muhurtha.cs`, `HouseSubStrength.cs` | Never convert missing components to zero; no rule copy | Yes: all six components with intermediate values |
| Ashtakavarga | PARTIAL: BAV/SAV, Shodhana and Pinda code/tests exist; full independent reference not yet attached | BAV/SAV and Prastaraka code/data types exist | AYNVORA `ashtakavarga/*`; VedAstro `Ashtakavarga.cs`, `Bhinnashtakavarga.cs`, `Sarvashtakavarga.cs`, `Prastaraka.cs` | Retain AYNVORA ruleset; compare intermediate matrices | Yes: planet/sign rows and transformed Shodhana/Pinda values |
| Yogas | PARTIAL: current general prediction/yoga coverage is not an exhaustive catalog | Horoscope XML and named calculator hooks exist | AYNVORA `aynvora-core/.../astrology/prediction/`; VedAstro `HoroscopeDataList.xml`, `HoroscopeName.cs`, calculator attributes | Use event/data separation only; no source text reuse | Yes: each rule needs a named authoritative edition and fixtures |
| KP | UNSUPPORTED in this phase | KP-specific source file exists; complete behavior not audited | VedAstro `CalculateKP-ORI.cs`; AYNVORA public KP convention currently rejected | Do not implement under this phase and do not add KP source | Yes, before future implementation |
| Varshaphal | NOT_VERIFIED | No verified core calculator was located in the inspected paths | VedAstro repository search audit | Excluded by phase scope | Yes, if future source path is found |
| Prashna | NOT_VERIFIED | No verified question-chart calculator was located in inspected paths | VedAstro repository search audit | Excluded by phase scope | Yes, if future source path is found |
| Prediction / event engine | PARTIAL: AYNVORA has a current prediction engine and new event metadata structures; rules are not yet fully data-driven/source-complete | XML data, event identity, delegates, time ranges and event chart source exist | AYNVORA `astrology/prediction/*`, new `AstroEventModels.kt`; VedAstro `EventDataList.xml`, `EventGenerator.cs`, `EventManager.cs`, `TimeRange.cs` | Adopt typed separation pattern only; no event/rule text copied | Yes: rule-by-rule source provenance and deterministic condition tests |

## Reference adapter and result schema

`AstroReferenceComparison` provides fixture ID, input, calculation profile, ayanamsha, house system, provider names, values, difference, tolerance, status, explanation and source references. The `authority` field defaults to `REFERENCE_ONLY`. Supported statuses are exactly `EXACT_MATCH`, `WITHIN_TOLERANCE`, `MISMATCH`, `AMBIGUOUS`, `UNSUPPORTED`, and `NOT_VERIFIED`.

The schema is present, but a live VedAstro client, normalized nine-planet response parser and differential fixture runner are **PARTIAL / NOT_VERIFIED**. No network API has been made a production dependency. Existing AYNVORA JKR/Meeus fixtures remain their existing provenance; they are not silently converted into VedAstro fixture values.

## Differences and numerical coverage

No AYNVORA-versus-VedAstro numeric output fixture was collected in this phase. Therefore:

- fixtures collected: **0**; comparisons executed: **0**
- exact matches: **0 observed** (no comparisons; accuracy remains **NOT_VERIFIED**)
- within-tolerance matches: **0 observed** (no comparisons; accuracy remains **NOT_VERIFIED**)
- mismatches: **0 observed** (no comparisons; no mismatch category assigned)
- ambiguous cases: **0 observed** (no comparisons)
- unsupported comparisons: **0 executed** (feature support inventory is separate)
- input-equivalent cases: **0 checked**
- input-not-equivalent cases: **0 checked**
- 1000-fixture accuracy lab: **NOT_STARTED**; fixtures will not be fabricated to meet a count

Before comparing, align civil time conversion, timezone version/fold choice, UTC/TT treatment, location coordinates, ayanamsha, mean/true node, house system, rule tradition, interval inclusivity and rounding. Record every unresolved convention difference rather than changing AYNVORA to force agreement.

## Current AYNVORA evidence and limits

- `docs/CALCULATION_CONTRACT_V1.md` names calculation authority, assumptions, currently accepted IDs and known support limits.
- `astro-engine/src/commonTest/kotlin/com/aynvora/astro/JkrReferenceGoldenTest.kt`, `PlanetaryGoldenTest.kt`, and `JulianDayTest.kt` contain existing traceable test vectors; these are sparse vectors, not a broad accuracy envelope.
- Phase 10.16E adds first-class provider metadata while leaving the analytical formulas and output values unchanged.
- The bundled `design-system/.../locations.tsv` has SHA-256 `b9ef998d8772e66f8f03314f1ad843f2b7d2ab6366eb0e7933a95463c1ae18aa`; upstream source/license are not verified. It must not be treated as authoritative until that provenance is established.
- Time conversion uses curated rules, not a versioned IANA tzdb. Gaps/folds are rejected for ambiguous normalization where implemented; broader historical coverage is not verified.

## Governance and blockers

VedAstro code is MIT-licensed at the pinned repository commit, but AYNVORA reused none. Source texts/rule datasets and SwissEphNet/SWISSEPH require separate rights and provenance review. AYNVORA's existing analytical provider declares redistribution rights `NOT_VERIFIED`; this phase does not clear them. Swiss Ephemeris remains a candidate/reference only.

## Phase 10.16F acceptance update

This update preserves the Phase 10.16D/E work already present in the working tree. The changes and evidence below are limited to this phase's acceptance hardening; no production calculations use VedAstro.

| Acceptance area | Current result | Evidence and remaining gap |
|---|---|---|
| Reference adapter and normalized inputs | PARTIAL | `AstroReferenceInput`, mismatch fields and `INPUT_NOT_EQUIVALENT` gate are present. No pinned-reference process/client was run; the local environment did not have `dotnet`. Numeric comparison totals remain NOT_VERIFIED. |
| Canonical location APIs | PARTIAL | SDK exposes country, state and city search plus city resolution through an optional `OfflineLocationCatalog`; Android loads its bundled TSV. Resolved identity, coordinates, timezone, dataset version and provenance now flow through the internal engine birth input and location feature into snapshot/JSON/Room. Duplicate-name ambiguity and SDK-to-pipeline field transport have tests. Bundled data lineage/license and geographic completeness remain unverified. |
| Timezone handling | PARTIAL | Supported curated IDs/offsets report `AYNVORA_CURATED_RULES_V1`; unknown IDs are rejected by the capability query. This is not an IANA tzdb release and does not establish broad historical/DST coverage. |
| Room migration | IMPLEMENTED, PARTIAL DEVICE EVIDENCE | Database v3 has explicit 1→2 and 2→3 migrations, app builders register them, and destructive fallback is removed. v2→v3 schema compiles. On 2026-10-01, the debug APK was installed over the existing Galaxy S23 Ultra app with `adb install -r`; Android resumed `MainActivity`, and the existing app database and WAL remained present. The database's pre-upgrade Room version was not captured, so this does not prove that a v2→v3 migration ran. Automated migration execution against a known v2 fixture remains pending. |
| Saved snapshot reuse | PARTIAL | Public `getSnapshot` reads and decodes persisted data without invoking calculation APIs; Room-backed saved-chart repository persists snapshot metadata/payload. Repository read counts are tested, but no instrumentation proves zero feature-engine invocations across process restart/reopen. Older snapshots return migration-required and future versions return unsupported. |
| Consistency checks | EXPANDED | Snapshot validation now checks chart/placement/state consistency, divisional ascendants, recursive Dasha ranges, transit body and timestamp consistency, and birth provenance against the natal chart. These are invariants, not independent accuracy proof. |
| Dasha interval correctness | FIXED, UNIT COVERAGE ADDED | Antardasha spans now scale against the actual Mahadasha duration, including the birth balance period. The core snapshot invariant exposed child periods spilling past that shortened parent; tests assert containment and endpoints. |
| Cross-platform/performance acceptance | PARTIAL | The full Phase F command matrix passed: engine/core/data/UI JVM tests, aggregate `test`, desktop `assemble`, and Android `assembleDebug`. A Galaxy S23 Ultra (`SM-S918B`, Android 16/API 36) was attached on 2026-10-01; the APK installed over the existing app and `MainActivity` resumed. The visible screen was the Vedic birth-details form. Saved-chart screen-by-screen/reopen validation against a generated snapshot was not performed. No performance timings or engine invocation counters were captured. |

Additional acceptance limits: timezone tests cover fixed offsets plus the current New York DST gap/fold policy, but there is no full historical IANA fixture set. Duplicate city names are preserved and tested as ambiguous by the catalog API. No independently sourced Dasha, requested-date Transit, or Panchang reference fixture was added because no vetted reference values were available in this phase. Room's migration path compiles and exports schema v3, but has not been executed against a fixture database. `MIGRATE` is reserved in the open status model; there is no snapshot JSON converter yet, so older versions correctly return `MIGRATION_REQUIRED` rather than being recalculated.

Do not report engine parity or production-grade reference accuracy from this phase. Completing those claims still requires a runnable pinned VedAstro adapter with equivalent inputs, source-backed numeric fixtures, verified canonical-location dataset provenance, broader timezone rules, a real v2 Room migration test, and instrumentation proving cached reopen performs zero recalculations.
