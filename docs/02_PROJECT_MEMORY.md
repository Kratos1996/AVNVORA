# AYNVORA Project Memory
Version: 1.0

## Locked Brand
- Brand: AYNVORA
- Tagline: Ancient Wisdom. Clearer Choices.
- Display font: Cormorant Garamond
- UI font: Inter
- Indian script family: Noto Sans
- Primary gold: #C9A227
- Cosmic black: #080B14
- Celestial blue: #6B8CFF

## Locked Engineering Direction
- Kotlin Multiplatform + Compose Multiplatform
- Offline-first core (Room KMP local persistence as source of truth for content)
- Clean Architecture mandatory for every feature: Presentation → Domain ← Data
- Deterministic astrology engine (:astro-engine) completely decoupled and analytics-independent
- Firebase Analytics decoupled via domain AnalyticsTracker & AnalyticsConsentManager (AndroidApp implementation, NoOp on Desktop/iOS/tests)
- Shared business/domain logic
- Reusable design system
- Secure SDK licensing and entitlements
- Deterministic astrology engine separated from AI/explanation
- Tarot feature domain: independent contemplative tool, zero astro coupling, offline-first Room KMP,
  privacy-safe analytics
- Canonical Clean Architecture: Presentation (`:ui`) → Domain (`:aynvora-core`, `:astro-engine`) ←
  Data (`:aynvora-data`)
- Use cases in Domain (`com.aynvora.core.usecase`), Repository contracts in Domain (
  `com.aynvora.core.repository`), Repository implementations in Data (
  `com.aynvora.data.repository`), and Room persistence entities/DAOs strictly in Data (
  `com.aynvora.data.database`)
- Project-wide Dependency Injection: Koin Multiplatform (`coreDomainModule`, `coreDataModule`,
  `uiModule`, `aynvoraAppModules`) configured and bootstrapped across desktop and mobile
- Mandatory workflow rule: Always check and utilize global skills (e.g. `compose-multiplatform`)
  before beginning work
- Phase 7.4 Core Product Feature Foundation: Established the 10 first-class product domains (
  Astrology, Palmistry, Gemstones, Gita, Garuda Puran, Lal Kitab, Tarot, On-Device AI, Daily
  Guidance, Wallpaper Studio) with clean domain contracts, AI tool registries, privacy
  classifications, and typed feature availability.
- Phase 7.5 Core Intelligence & Multi-Feature Orchestration: Established
  `com.aynvora.core.intelligence` containing `MultiFeatureOrchestrator`, `EvidenceGraph`,
  `FeatureCapabilityRegistry`, `DataSufficiencyValidator`, multi-tradition conflict handling,
  provenance preservation, and on-device SLM tool routing contracts without direct database access.
- Phase 8.0 Astrology Predictive Core & Timing Engine: Implemented deterministic Vimshottari Dasha (
  120-yr, MD/AD/PD), Planetary Transits (Gochara, timeline, ingress, retrograde), and Five-Limb
  Classical Panchang (Tithi, Nakshatra, Yoga, Karana, Vara) in `:astro-engine`. Established
  `com.aynvora.core.astrology.prediction` in `:aynvora-core` featuring classical Parashara rule
  evaluation, life topic taxonomy (13 topics), multi-signal convergence, bounded timing window
  generator, and Prediction Evidence Graph with `traceWhy` backward traceability. Expanded Public
  SDK APIs (`calculateDasha`, `calculateTransit`, `calculatePanchang`) and registered
  `AstrologyPredictionEngine` in Koin DI.
- Phase 8.2 reference audit: the JKR PDF is checked in at
  `docs/JKR_#JKR-117480_ishant_sharma_1787393982115.pdf`. `JkrReferenceGoldenTest.kt` now has 31
  methods; these are test methods, not 31 independent calculations. Nine core planets are compared
  live, and all 160 classical-body/Lagna cells across 16 Vargas match JKR signs when using the
  source longitudes. Longitudes/dignities and Dasha boundary disagreements remain classified as
  ambiguous. `WHOLE_SIGN_V1`, `EQUAL_HOUSE_V1`, and implemented `SRIPATI_CHALIT_V1` are distinct
  profiles. Sunrise Vara is implemented for fixed offsets; solar times use an explicit
  approximation. Transit vectors and complete Chalit occupancy certification remain
  unsupported/ambiguous. See `REFERENCE_VALIDATION_JKR.md`. Fourteen first-class domains are
  registered; Numerology, Rudraksha, Jadi, and Yantra remain `FOUNDATION_ONLY`.
- Phase 8.4 Garuda Puran: typed topic/section/reference/edition/text/interpretation/practice
  contracts, an offline adapter over the existing approved-content store, source-backed evidence
  graph, report generator, English/Hindi localization, reusable Compose route, privacy-safe
  analytics, and orchestrator integration are implemented. Repository inventory confirmed there is
  no Garuda Puran source text, translation, citation set, or installed pack; test-only fixtures
  contain synthetic text and references. All real topics therefore remain unavailable and the
  product status remains `ComingSoon` until a reviewed source package is installed. The shared
  content sync and stub verifier are not production integrity verification. See
  `GARUDA_PURAN_ARCHITECTURE.md`.

## Documentation Authority
00_MASTER_RULES.md is the highest-level engineering rulebook. More specific documents govern their own domains.

Phase 8.2 governance: comparison statuses explicitly separate exact matches from tolerance matches;
house profiles are not interchangeable; reference coverage must be independently backed per
calculation and per Varga. See `PHASE_8_2_ASTROLOGY_REFERENCE_PARITY.md`. Numerology, Rudraksha,
Jadi, and Yantra remain first-class domains at `FOUNDATION_ONLY` status.

Phase 8.4 governance: Garuda Puran text, meanings, interpretations, practices, and references must
arrive in a reviewed approved package. No scripture or citation may be generated from model
knowledge. A schema/report pipeline is not evidence that the source corpus is available. Traditional
statements are not scientific claims. The reusable route delegates report viewing and export to the
shared Report Engine; the current root host still has no feature navigation graph.

Phase 8.4A governance: Garuda Puran source acquisition and rights verification. Investigated 7
candidate
sources (Summary PDF, Gita Press Code 1416, GRETIL 1906, SanskritDocuments, Wood 1911, Dutt 1908,
MLBD 1978). Established strict rule: no copyrighted translations into the shipped SDK. Sources 5 (
Wood 1911)
and 6 (Dutt 1908) classified as jurisdiction-eligible for distribution (
`PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS`).
Sources 2 (Gita Press), 3 (GRETIL), 4 (SanskritDocs), and 7 (MLBD) classified
`REFERENCE_ONLY_NON_DISTRIBUTABLE`.
Source 1 classified `LICENSE_UNCERTAIN`. Typed source metadata (`GarudaSourceManifest`,
`GarudaSource`),
normalized models (`GarudaChapter`, `GarudaVerse`), and FIPS 180-4 SHA-256 verifier implemented.

Phase 8.4B governance: Targeted public-domain content ingestion, editorial verification & real
report integration.
Ingested primary approved package `garuda-content-wood-1911-en-v1` (Ernest Wood & S.V. Subrahmanyam,
1911) with
verified SHA-256 `4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5`. Packaged 23
visually verified,
curated passages across all 16 chapters of the Saroddhara, mapped to all 10 typed topics (
`GarudaPuranTopicId`).
Established secondary cross-reference with Manmatha Nath Dutt (1908) with SHA-256
`2313f4e0472e3ec9a97b46cb47e6d74a675ba4d7da150214d4759da94960319e`
in `GarudaSourceComparisonRegistry`. Selected Option B for Hindi: kept `CONTENT_UNAVAILABLE` with
reason
`NO_APPROVED_CONTENT_FOR_LANGUAGE` with strictly zero fallback to copyrighted Gita Press text.
End-to-end report
integration active through `GarudaPuranReportGenerator` -> `ReportDocument` -> `ReportViewer` ->
`ReportPdfGenerator`.
25-point real-fixture test suite verified. Capability status promoted to `PARTIALLY_IMPLEMENTED`.
See `docs/garuda-puran/`.

Phase 8.5 governance: Tarot Complete Production Experience.

- Packaged full 78-card Rider-Waite-Smith deck (22 Major + 56 Minor Arcana + 1 card back) with
  SHA-256 verified assets in resources.
- Asset metadata, manifests, and deck packaging governed under Public Domain (US / Berne 70+ pma,
  Pamela Colman Smith d. 1951 / A.E. Waite d. 1942).
- Strictly non-predictive, contemplative framing enforced across all screens: Mandatory Disclaimer
  gate (`TarotDisclaimer`), Home, Deck Selection, Single-card and Three-card spreads, Card Browser,
  Card Detail, Card Reveal (Upright/Reversed), Reading Result, and Reading History.
- Structured `TarotReportGenerator` integrated with Report Engine (`ReportType.TAROT`), PDF
  generation, and sharing services.
- Local explanation powered by `TarotExplanationEngine` (deterministic, grounded, non-predictive).
  Immutable `EvidenceGraph` provenance attached to every reading.
- Room database strictly persists card metadata and asset paths; zero binary images in DB.
- Full Compose Multiplatform UI implemented in `TarotRoute.kt` and wired into `AynvoraApp.kt`. Tests
  pass across `:aynvora-core` and `:aynvora-data`.

Phase 8.6 & 8.7 governance: On-Device AI Platform Foundation & Production Runtime Hardening.

- Architecture: Zero-configuration consumer UX ("Download AYNVORA AI") driven internally by
  `AiDeviceCapabilityDetector` and `AiModelSelector`.
- Models: Qwen2.5-Instruct family (0.5B / 1.5B; Q4_K_M, Q5_K_M, Q8_0) licensed under Apache-2.0,
  SHA-256 verified, GGUF format.
- Lifecycle: `AiModelLifecycleManager` handles staging, SHA-256 checksum verification, atomic
  promotion, atomic update, rollback to previous model on failure, and clean deletion without silent
  re-downloads.
- Runtime Hardening: Dynamic RAM safety calculation before loading (`calculateSafeRamAllocation`),
  context length bounding against model limit, execution timeout (`withTimeoutOrNull`), cancellation
  tracking, and cold vs warm load tracking.
- Local Offline Execution: 100% offline verification; zero network calls (no Ktor, Retrofit, or
  Cloud ML) in `AiInferenceEngine.generate()`.
- Data Grounding & Safety: `TarotFeatureDataConnector` connects authoritative readings;
  `AiOutputValidator` detects and rejects medical claims, financial guarantees, fatalistic
  predictions, and empty responses, falling back infallibly to deterministic templates.
- Technical Diagnostics: Raw hardware metrics separated into developer-only `AiDiagnosticScreen`.
  Consumer `AiSetupCard` exposes strictly consumer-friendly zero-config status.
- Analytics Hygiene: 8 technical lifecycle events added without storing or transmitting user
  prompts, questions, responses, or private evidence.

Phase 8.8 governance: Real Native On-Device AI Runtime Integration & Verification.

- Runtime Engine: Completed transition from simulation to real native runtime architecture with
  `NativeAiRuntime`, `LlamaNativeRuntimeDriver`, and `LocalNativeInferenceEngine` bound in
  production DI (`coreDomainModule.kt`).
- Isolation Principle: Isolated test simulation into `LocalSimulationEngine` for non-native CI unit
  testing. The production engine strictly emits `AiExecutionMode.REAL_MODEL_INFERENCE` and never
  falls back to simulation.
- Native Error Mapping: 9 typed native error codes added (`NATIVE_LIBRARY_LOAD_FAILED`,
  `NATIVE_MODEL_LOAD_FAILED`, `NATIVE_CONTEXT_CREATE_FAILED`, `NATIVE_OUT_OF_MEMORY`,
  `NATIVE_INFERENCE_FAILED`, `NATIVE_CANCELLED`, `NATIVE_INVALID_HANDLE`,
  `NATIVE_RUNTIME_UNAVAILABLE`, `UNSUPPORTED_ARCHITECTURE`).
- Native Resource Safety: Native handles tracked in an allocation registry; unloads trigger native
  release, preventing double-free, dangling pointers, and memory leaks.
- Android Packaging: Configured 64-bit ABI filters (`arm64-v8a`, `x86_64`) in
  `androidApp/build.gradle.kts`. `:androidApp:assembleDebug` builds cleanly and produces the debug
  APK.
- Tarot Real Integration: Tarot reading -> `TarotFeatureDataConnector` -> `EvidenceGraph` ->
  `AiContextBuilder` -> `AiRequest` -> `LocalNativeInferenceEngine` -> `AiOutputValidator` verified
  with preserved provenance and zero authority mutation.
- Native Runtime Verification Suite: 11 tests in `AiNativeRuntimeVerificationTest` covering format
  validation, dynamic RAM safety, cold/warm load, native cancellation, context overflow, resource
  cleanup, and clean deterministic fallback.
