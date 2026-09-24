# Phase 8.2 — Astrology Reference Parity

## Result

The JKR source has been located
at [docs/JKR_#JKR-117480_ishant_sharma_1787393982115.pdf](JKR_#JKR-117480_ishant_sharma_1787393982115.pdf),
read, and used for source-page comparisons. The work closes the applicable calculation paths while
retaining ambiguity labels where the report does not state a convention or contradicts itself. This
phase adds no product feature.

## Calculation changes and evidence

- Corrected the JKR birth JD to 2450275.357639 for 10 Jul 1996 20:35 UT.
- Added `ReferenceComparisonStatus` with the seven requested values and a contract test.
- Added a live JKR chart comparison for nine classical planets. Sign/nakshatra/pada/motion match
  9/9; combustion matches the reported Mercury state. Longitude precision and five dignity
  relationship labels remain ambiguous for documented reasons.
- Compared the 16 Varga transformations against 160 JKR p.110 sign cells: 160 exact sign matches,
  using p.6 longitudes as inputs. JKR's outer-planet cells (48) are unsupported by the classical
  body model.
- Implemented named `SRIPATI_CHALIT_V1` quadrants, Bhava midpoints/sandhis, and planet-house
  assignment. JKR p.7 first four midpoints match within 0.2°. Remaining row and occupancy
  interpretation stays ambiguous.
- Added solar-day sunrise/sunset and a local-sunrise Vara profile with explicit coordinates and
  fixed UTC offset. JKR sunrise/sunset compare within 6 minutes. The engine reports polar sunless
  days as unsupported; DST-aware zone rules are not included.
- Corrected a stale Dasha test date and now assert active Rahu/Mercury/Mars in September 2026. JKR's
  printed boundary dates conflict across pages, so no exact boundary claim is made.

Detailed row classifications and page references are
in [REFERENCE_VALIDATION_JKR.md](REFERENCE_VALIDATION_JKR.md). Prediction semantics and `traceWhy()`
were inspected; no prediction rules were altered. Transit vectors are absent from the report, so
transit parity remains unsupported.

## Accounting

`JkrReferenceGoldenTest.kt` contains **31** executable tests (original 27 plus four live
reference/behavior tests). Category totals: birth 1, planet/status 6, Panchang 8, Dasha 6, dosha 2,
Varga 4, numerology record 1, Chalit 2, yoga 1. The suite count is not a count of calculation
vectors. Full Gradle test and build totals below are recorded from the final run.

## Governance

Rules 67–72 in `00_MASTER_RULES.md` require explicit status classifications, named house profiles,
source-backed vectors, no overfitting, full Varga evidence, and preservation of first-class feature
registry entries. Numerology, Rudraksha, Jadi, and Yantra remain `FOUNDATION_ONLY`; roadmap/registry
entries remain intact.

## Remaining limits

- Planetary longitudes use a different or undocumented ephemeris model; JKR values are rounded to
  arcseconds.
- Five dignity labels are convention-dependent and unresolved.
- JKR Chalit table row alignment/planet placements need clarification for full occupancy
  certification.
- Dasha source pages disagree on boundary dates; timezone and time-of-day display conventions are
  unspecified.
- No transit vectors; no IANA DST support for local-sunrise Vara.
- iOS build cannot be established from this checkout because it has Swift sources but no Xcode
  project/workspace or iOS Gradle target.

## Build and regression record

Command:
`./gradlew test :astro-engine:jvmTest :aynvora-core:jvmTest :aynvora-data:jvmTest :aynvora-localization:jvmTest :androidApp:assembleDebug :desktopApp:compileKotlinJvm` —
**BUILD SUCCESSFUL**. XML test reports: 404 tests, 0 failures, 0 errors, 0 skipped (`astro-engine`
188, `aynvora-core` 102, `aynvora-data` 34, `aynvora-localization` 67, `design-system` 13). Android
debug assembly and Desktop JVM compilation completed. Prediction regression, including `traceWhy()`,
passed within the core suite. iOS remains `PARTIALLY_VERIFIED`; this checkout lacks an Xcode
project/workspace and iOS Gradle target.
