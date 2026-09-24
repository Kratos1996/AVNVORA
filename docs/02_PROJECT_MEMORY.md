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
