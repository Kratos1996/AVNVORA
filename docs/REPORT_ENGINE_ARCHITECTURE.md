# AYNVORA Report Engine

Phase 8.3 introduces one structured report path for deterministic calculations, in-app rendering,
and PDF export.

## Modules and flow

`:ui` calls the report use case in `:aynvora-core`. The use case asks `AynvoraSdk` for already
implemented chart, Panchang, and Dasha results; the registered `KundaliReportGenerator` maps those
results and matching-locale evidence into one `ReportDocument`. `ReportViewer` and the platform
`ReportPdfGenerator` both consume that same document. Renderers do not call `AstroEngine` and do not
calculate or infer report data.

The report domain in `com.aynvora.core.report` contains typed models, status/error codes, generator
registry, type catalog, and PDF/share contracts. Android and JVM implementations live in `:ui`
platform source sets. The domain has no Android, Compose, filesystem, Firebase, or network
dependencies.

## Types and extension

`ReportTypeRegistry` exposes the twelve independent built-in types: Kundali, Gemstone, Numerology,
Rudraksha, Jadi, Yantra, Palmistry, Tarot, Gita, Lal Kitab, Garuda Puran, and Daily Guidance.
`ReportGeneratorRegistry` maps a report type to its own generator. Add a report by defining its
typed input, generator, localized title/content keys, and registration; do not add its content rules
to `KundaliReportGenerator`.

`ReportFeatureStatus` is derived from `CanonicalCoreFeatures`. Kundali is calculated from the SDK.
Garuda Puran has a registered source-backed generator, but report preparation requires non-empty
approved local content and returns a typed unavailable result while no corpus is installed. Other
features still at `ComingSoon` or configuration-required status return `FOUNDATION_ONLY` and a
localized reason; they do not receive invented calculations.

## Kundali data coverage

Included when the chart result contains the relevant structured values: birth details, Panchang,
ascendant, Moon sign, Nakshatra, planetary positions, house placements, motion/retrograde,
combustion, aspects, dignities, relationships, Shadbala, Ashtakavarga, Shodhana, requested Vargas,
the complete returned Dasha hierarchy, matching-language evidence, calculation provenance, and
limitations. Each included section has a typed source and calculation reference. The viewer and PDF
expose calculation/source IDs, profiles, versions, and reference status. No empty calculated section
is filled with placeholder values.

Transits need an explicit transit epoch, Vargas need to be requested, and
Yogas/Dosha/timing/predictions need calculation or interpretation evidence. Until those inputs
exist, the report records omitted sections with typed reason codes, localized section titles, and
localized explanations. Reference status is `NOT_ASSESSED` unless a specific comparison has
established another status. The JKR document is a structural benchmark and existing astrology golden
tests remain the calculation validation source; report output does not upgrade JKR parity claims.

The document version includes schema, calculation-engine, and content versions, plus language and
generation time. Raw profile, ruleset, calculation, and source identifiers remain unchanged for
auditability. Evidence graph summaries are included only when their provenance locale matches the
selected report language.

## Localization and offline operation

`AynvoraReportTextResolver` adapts the bundled English and Hindi translation tables and
`LocaleFormatter` to the domain text contract. Report titles, labels, state messages, section
omission reasons, disclaimers, Panchang limbs, chart labels, and supported calculation status names
have catalog entries in both languages. Date/time formatting is deterministic and the generation
timestamp is shown in UTC. No report-generation or display step requires network access.

## Viewer and PDF/share

`ReportRoute` is the complete route for a `ReportGenerationRequest`: loading/unavailable states,
deterministic generation, `ReportViewer`, PDF generation, and sharing/export. The host provides the
SDK, selected-language resolver, and platform services. `KundaliReportRoute` remains a compatibility
wrapper. `ReportViewer` includes birth summary, jump-to-section navigation, collapsible reusable
sections, tables, metrics, interpretations, evidence/provenance, metadata, and disclaimer.

`GarudaPuranRoute` is a reusable topic/catalog entry route. It reads through domain use cases and
delegates generation, viewer, PDF, and share to the same `ReportRoute` and Report Engine. It has no
AI dependency and produces no report until an approved, selected-language source package supplies
content. The root app is currently only a scaffold and has no feature NavHost; host integration is
still required to expose feature routes through app navigation.

- Android uses `android.graphics.pdf.PdfDocument`. Sharing writes to app cache and gives external
  apps a cache-scoped `FileProvider` URI.
- Desktop JVM uses a dependency-free PDF writer and Java2D pages, preferring the bundled Noto Sans
  font for Hindi glyph coverage. Pages are rasterized; generated PDFs are image-based and text is
  not selectable/searchable.
- The iOS Swift host has no configured Kotlin/Native Gradle target or bridge for this report route
  in the current project. It can bind `UnsupportedReportPdfGenerator` and
  `UnsupportedReportShareService` to surface `PLATFORM_UNSUPPORTED`; native iOS PDF/share support
  remains unimplemented and unverified.

PDF rendering reads only the `ReportDocument` and bundled translations. It does not use Firebase or
network services. PDF output is returned as bytes; share/export implementations manage temporary
files. Report binaries are not persisted to Room.

## Analytics and privacy

Report events use the existing `AnalyticsTracker` abstraction: `report_opened`, `report_generated`,
`report_pdf_generated`, `report_shared`, and `report_pdf_failed`. Parameters are limited to built-in
report type IDs (extensions collapse to `custom`) and validated stable error codes. Birth
date/time/place, identity, chart results, evidence, report content, and prediction text are
excluded. Android can route the abstraction to Firebase; desktop and tests can use
`NoOpAnalyticsTracker`.

## Verification boundary

Domain and localization common code have JVM tests; the desktop JVM PDF renderer has a smoke test
checking a valid PDF envelope and the shared document-to-lines mapping. Android compilation
validates its renderer and provider wiring. This repository currently has no enabled iOS Gradle
target, so no iOS compilation or native PDF claim is made.
