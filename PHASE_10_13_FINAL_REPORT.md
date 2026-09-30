# Phase 10.13 Final Report

**Disposition: PARTIAL.** Calculation Contract V1 and chart-result provenance metadata are implemented. Calculation algorithms and conventions were not changed. Commercial redistribution, full profile support, broad boundary coverage and iOS parity remain unverified or unsupported, so this phase does not certify commercial or production readiness.

## 1. Calculation inventory

The public `AynvoraSdk` exposes natal chart, one/many Vargas, dignity, relationships, Shadbala, Ashtakavarga, Shodhita Ashtakavarga, Ashtakavarga Pindas, Vimshottari Dasha, transit snapshots, Panchang snapshots, numerology and engine metadata. `docs/CALCULATION_CONTRACT_V1.md` inventories the astrology operations with inputs, outputs, profiles, time/location assumptions, precision limits, source/test references and open boundaries.

## 2. Contract and contract types

- Created `docs/CALCULATION_CONTRACT_V1.md`.
- Added immutable `CalculationMetadata` to complete `ChartResult` outputs with contract version `V1`, profile ID, engine/model labels, analytical source ID, nullable data version and redistribution status.
- Adapter populates this metadata; SDK facade test verifies its values.
- Direct Dasha, transit and Panchang results still lack the same metadata object. This is a documented remaining gap.

## 3. Ephemeris/data sources

The inspected engine uses analytical formulas in Kotlin source: Meeus solar/lunar/JD/sidereal-time formulae; Simon/Meeus orbital-element calculations for Mercury–Saturn; a mean lunar node formula; and a Lahiri polynomial. No ephemeris data file, external coefficient package version or independently recorded commit was found in the repository paths reviewed. `MEEUS_VSOP87` remains the legacy result label, but inspected planetary code does not establish that a complete VSOP87 series is used.

## 4. License and redistribution evidence

No commercial redistribution grant was found in the repository for the implemented source/coefficient material or a bundled ephemeris package. **`COMMERCIAL_REDISTRIBUTION_STATUS = NOT_VERIFIED`.** Absence of evidence was not treated as permission; this report makes no legal conclusion.

## 5. Frozen conventions

Documented current defaults/IDs include `STANDARD_VEDIC`, `LAHIRI_CHITRAPAKSHA`, `EQUAL_HOUSE`, Parashara Varga/Ashtakavarga, mean nodes, current combustion thresholds, `STANDARD_MAJOR` aspects, Vimshottari 120-year Dasha, Panchang Vara modes and the current curated timezone resolver. No convention or numerical behavior was changed.

## 6. Known ambiguous/unsupported cases

- Raman and Krishnamurti ayanamsha values appear in a public enum but are rejected by the calculation resolver.
- Surya Siddhanta/Drig Ganita profiles exist as enum values, but distinct calculation paths were not established.
- Placidus currently throws unsupported; `SHRIPATI_PORPHYRY` is not the engine profile ID `SRIPATI_CHALIT_V1`.
- JKR Chalit occupancy and some dignity labels remain ambiguous; printed Dasha boundary dates conflict.
- Timezone conversion is curated/manual, not full IANA tzdb; DST fold/gap policies and month-specific date validation need work.
- Polar sunless-day calculation is unsupported. No complete supported ephemeris epoch range or error envelope is declared.
- Dasha, Transit and Panchang result metadata remains partial.

## 7. Golden vectors and tests

No new astronomical expected values were invented. Existing source-traceable Meeus, JKR and mathematical boundary vectors were cataloged in the contract; the SDK chart test was expanded to assert contract/source metadata. Shared common tests already cover JD/timezone rollover, zodiac/Nakshatra/Pada boundaries, Varga mapping boundaries, nodes, Dasha, Panchang, houses/Lagna, aspects, states, dignity, Shadbala and Ashtakavarga. Additional independent vectors are still needed for transit/stations, broad timezone transitions and epoch/location coverage.

## 8. Test results

- `./gradlew test` — **BUILD SUCCESSFUL**. This root task did not run the `astro-engine` and `aynvora-core` JVM test suites, so they were run explicitly.
- `./gradlew :astro-engine:jvmTest :aynvora-core:jvmTest :desktopApp:assemble :androidApp:assembleDebug` — **BUILD SUCCESSFUL**.
- Existing warnings include the Android host-test source-set configuration notice and unrelated Kotlin test-code warnings; no failures were reported.
- `git diff --check` — **passed**.

## 9. Build results

- Desktop `:desktopApp:assemble` — **passed**.
- Android `:androidApp:assembleDebug` — **passed**.
- No new platform target was created.

## 10. AI calculation-authority boundary

Updated `docs/AI_TOOL_CONTRACT.md`: AI must consume typed calculation evidence from `AynvoraSdk`/`astro-engine`, preserve profile and provenance, and may explain or synthesize but must not calculate, round/correct, override or invent astrology facts. No AI integration was added.

## 11. Platform status

- Android: application and KMP Android targets exist; build passed. Full Android/Desktop parity was not claimed.
- Desktop: JVM target exists; build passed. Cross-platform parity was not independently certified.
- iOS: no iOS Gradle target or Xcode project/workspace exists in this checkout. **NOT_VERIFIED**; no fake iOS support was added.

## 12. Remaining P0 blockers

1. Documented commercial redistribution/license decision for the actual calculation implementation and dependencies.
2. Supported epoch range and quantified accuracy validated against independent source vectors.
3. Explicit implementation/mapping decisions for unsupported enum/profile options.
4. Full versioned timezone database behavior or explicit supported-ID/range constraints and DST fold/gap policy.
5. Source resolution or continued `AMBIGUOUS`/`UNSUPPORTED` classification for JKR Chalit, dignity and Dasha disagreements.
6. Additional sourced golden vectors and actual platform parity runs; iOS remains out of the current checkout.
7. Versioned metadata on all public calculation result types, including direct Dasha, transit and Panchang returns.

**Phase 10.13 stops here.** No final AI integration or unrelated product feature work was started.
