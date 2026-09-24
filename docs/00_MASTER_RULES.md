# AYNVORA Master Rules
Version: 2.0 (Phase 7.0 — Clean Architecture + Analytics + Offline-First)

## Purpose
This document is the highest-level engineering and product governance rulebook for AYNVORA. Every contributor, developer, designer, automation, and AI coding agent must follow it.

## Non-Negotiable Rules
1. Read the relevant AYNVORA specification before modifying code.
2. Never bypass architecture for speed.
3. Reuse existing functionality before creating duplicate functionality.
4. Reuse approved UI components before creating one-off UI.
5. Never invent colors, typography, spacing, radius, motion, icons, or visual patterns outside the Design System.
6. Astro calculations belong to the Astro Engine, never inside UI code.
7. Never fabricate astrology results, calculation progress, confidence, or sources.
8. Never store secrets, private keys, tokens, or credentials in source control.
9. Security-sensitive changes require threat analysis and tests.
10. Minimize collection and retention of personal data.
11. Common KMP code is preferred unless platform-specific behavior is genuinely required.
12. Business/domain rules must not be duplicated across platforms.
13. Every interactive feature must define loading, empty, error, disabled and success states where applicable.
14. Every user-facing feature must meet accessibility requirements.
15. New dependencies require a documented reason and security/license review.
16. Do not make breaking public API changes without versioning and migration notes.
17. Do not silently change astrology rules or calculation conventions.
18. Traditional interpretations must be distinguishable from deterministic calculated facts.
19. Never use fear, false certainty, or manipulative engagement as a product mechanism.
20. If an existing abstraction is insufficient, extend it before creating a parallel abstraction.
21. Tests are required for deterministic domain logic and security-critical behavior.
22. Documentation must be updated when architecture, public APIs, security rules, or design tokens change.
23. Never claim a build, test, platform, or security control is verified unless it was actually verified.
24. Preserve backward compatibility where contractually required by the SDK.
25. When requirements conflict, follow: security/privacy → architecture → Style Guide → feature requirement → convenience.

## Phase 7.0 Permanent Governance Rules (Clean Architecture + Analytics + Offline-First)

26. **Clean Architecture is mandatory for every current and future AYNVORA feature.**
    Dependencies must point inward: Presentation → Domain ← Data.
    Domain (`:aynvora-core`, `:astro-engine`) must remain independent of Android, iOS, Compose, Room, Firebase, and all platform APIs.
    Repository interfaces belong in Domain. Repository implementations belong in Data.
    Platform-specific code must be isolated behind platform interfaces.

27. **Firebase Analytics is mandatory for all applicable current and future AYNVORA user-facing functionality.**
    New phases must audit existing analytics infrastructure and integrate meaningful, privacy-safe analytics
    without introducing Firebase dependencies into Domain or the deterministic astrology engine.
    Analytics must be decoupled via `AnalyticsTracker` and `AnalyticsEvent` abstractions in `:aynvora-core`.
    Firebase SDK imports are confined to `androidApp` only.

28. **The deterministic astrology engine must remain completely analytics-independent.**
    `:astro-engine` must have zero dependency on Firebase, analytics, or platform APIs.
    Analytics must never affect calculation inputs, outputs, precision, determinism, or performance.
    The identical calculation result must be produced whether analytics is enabled, disabled, or unavailable.

29. **Offline-first architecture is mandatory.**
    All applicable AYNVORA features must operate completely without network connectivity.
    Room KMP is the local source of truth for content, sync metadata, and structured data.
    Network is used only for content synchronization — never as a direct UI data source.
    A network failure must never destroy or corrupt existing local data.

30. **Content updates must be validated before committing to the local database.**
    Sync operations must be transactional. Partial or unverified content must never replace verified local content.
    A `ContentVerifier` implementation (not a stub) is required before any production content release.
    Signing keys must not be committed to source control.

31. **Analytics consent must be obtained before any analytics data is collected.**
    Default consent state is PENDING — analytics must be suppressed until the user grants consent.
    `ConsentAwareAnalyticsTracker` must gate all analytics calls behind `AnalyticsConsentManager`.
    Firebase must not be assumed available before initialization and consent are satisfied.

32. **Tarot must remain an independent, reflection-oriented feature domain.**
    Tarot is strictly for relaxation, introspection, and personal reflection.
    Tarot must never make supernatural claims, guarantee future events, or offer medical, legal, or
    financial advice.
    Tarot must remain completely decoupled from `:astro-engine` and astrological chart calculations.
    Random card draws must use an abstracted `TarotRandomSource` and must never depend on birth data
    or planetary positions.
    Tarot reading history and analytics must never collect or persist private user questions or
    journal text.

## Phase 7.3 Permanent Rules (Repository Architecture & Clean Architecture Standardization)

33. **Every AYNVORA module and package must have one clearly defined architectural responsibility.**
    - Presentation / UI (`:ui`, application hosts): screens, viewmodels, UI components, navigation.
    - Domain (`:aynvora-core`, `:astro-engine`): domain models, repository interfaces, use cases,
      analytics abstraction.
    - Data (`:aynvora-data`): Room entities, DAOs, database construction, storage drivers,
      repository implementations.
    - Platform hosts (`androidApp`, `desktopApp`, `iosApp`): platform boot, Firebase integration, OS
      drivers.

34. **Dependency direction must point inward strictly: Presentation → Domain ← Data.**
    Domain contracts are independent of framework infrastructure. Presentation and Data depend on
    Domain.
    Data depends on external infrastructure (Room, SQLite, Network, Storage).

35. **Domain Use Cases live in Domain (`com.aynvora.core.usecase`), Repository contracts in
    Domain (`com.aynvora.core.repository`), and Repository Implementations in
    Data (`com.aynvora.data.repository`).**
    Models representing domain entities belong in Domain (`com.aynvora.core.models` or feature
    domains like `com.aynvora.core.tarot`).
    Data-layer persistence models and Room entities belong in Data (
    `com.aynvora.data.database.entity` and `com.aynvora.data.entity`).

36. **The Astrology Engine (`:astro-engine`) remains pure math, physics, and astronomical
    calculation.**
    No Compose, Room, Android, iOS, Firebase, network, or UI dependencies are permitted in
    `:astro-engine`.

37. **Analytics is infrastructure.**
    Firebase Analytics SDK is confined to `androidApp` platform host.
    Domain and Presentation interact with analytics strictly via `AnalyticsTracker` and
    `AnalyticsEvent` domain abstractions.

38. **Room is Data-layer infrastructure and must never leak into Domain or Presentation.**
    Room entities, DAOs, and database builders are strictly internal to `:aynvora-data` and platform
    setup roots.

39. **Feature domains remain cleanly decoupled.**
    Tarot, Astrology, Garud Puran, Tantra, and future feature domains must maintain distinct
    namespaces and boundaries.
    Tarot must never import `:astro-engine` or depend on astrological calculations.

40. **Every future feature must define its Presentation, Domain, Data, persistence, analytics, and
    tests before implementation.**

41. **Files must live in the package corresponding to their architectural responsibility.**
    No arbitrary placement based on convenience.

42. **Implementations must reuse existing abstractions rather than duplicating repositories,
    database drivers, analytics, or sync logic.**

43. **Global Skills First Rule.**
    Always inspect and invoke applicable global skills (such as `compose-multiplatform`,
    `test-driven-development`, etc.) before beginning implementation or architecture work. Coding
    agents and developers must strictly adhere to the patterns and best practices defined in those
    skills.

44. **Koin Dependency Injection Rule.**
    Koin is the official, project-wide Dependency Injection framework for AYNVORA across all Kotlin
    Multiplatform and Compose Multiplatform modules:
    - Domain dependencies and use cases are registered in `coreDomainModule` (`:aynvora-core`).
    - Storage drivers, database DAOs, and repository implementations are registered in
      `coreDataModule` (`:aynvora-data`).
    - Presentation dependencies are aggregated in `uiModule` and `aynvoraAppModules` (`:ui`).
    - Host applications (`androidApp`, `desktopApp`, `iosApp`) bootstrap the DI graph using
      `startKoin` with platform-specific module overrides where necessary.
    - ViewModels and composable dependencies must be resolved via Koin (`koinViewModel()`,
      `koinInject()`).

## Phase 7.4 Permanent Rules (Core Feature Foundation & Personal Astro OS)

45. **Canonical Core Feature Registry.**
    AYNVORA permanently defines 10 first-class product domains:
    1. Astrology (Vedic calculations, Vargas, Shadbala, Ashtakavarga, Pinda)
    2. Palmistry / Hastrekha (on-device hand capture, palm findings, non-medical reflection)
    3. Gemstone / Navaratna (wearing inventory, certificate OCR provenance, planetary balance)
    4. Bhagavad Gita (Sanskrit shlokas, authentic translations, Nishkama Karma reflection)
    5. Garuda Puran (transition wisdom, funeral contemplation, ethical preservation)
    6. Lal Kitab (independent traditional ruleset, folk remedies, distinct from Parashara)
    7. Tarot (contemplative archetypes, offline Room storage, random draw, non-predictive)
    8. On-Device AI Chat (local small language model orchestrating structured domain tools)
    9. Daily Guidance & Practice (sunrise routines, daily action, night contemplation)
    10. Personalized Wallpaper Studio (device-tailored sacred art prompts with safe areas)
        Every future feature must extend this registry rather than inventing arbitrary packages or
        modules.

46. **On-Device AI is Explainer and Tool Orchestrator, Never Calculation Authority.**
    The AI Assistant must never directly query Room databases or invent astrology calculations,
    Tithi, degrees, gemstone weights, or Gita shlokas. All domain facts must be retrieved via typed
    tools (`AiTool`) with explicit `AiProvenance`. The assistant must explicitly state when
    information is unavailable.

47. **Wallpaper External Handoff Requires Explicit User Action.**
    Wallpaper Studio generates device-tailored visual prompts. AYNVORA must never claim to generate
    the image itself unless an on-device rendering engine is present, and must never automatically
    transmit user prompt data to external cloud services without explicit, user-initiated copy or
    share action.

48. **Core Feature Privacy Classification.**
    - Sensitive Local Data (birth records, hand images, chat history, family data, private goals)
      must remain local-only by default and must never be transmitted to analytics or cloud storage
      without explicit opt-in consent.
    - Public Wisdom Corpus (Gita verses, Garuda Puran, Lal Kitab rules, Tarot cards) is seeded
      locally and verified via checksums.
    - Analytics is strictly consent-gated and must never collect personal astrological data, hand
      images, chat text, or user reflections.

## Phase 7.5 Permanent Rules (Core Intelligence & Multi-Feature Orchestration)

49. **Independent Feature Rule.**
    Every AYNVORA core feature must be independently usable without mandatory dependencies on
    unrelated features. Astrology must calculate charts without Tarot; Gita must search verses
    without Astrology; Gemstones must track inventory without Tarot.

50. **Controlled Cross-Feature Orchestration Rule.**
    Core features may be combined only through the dedicated orchestration layer (
    `MultiFeatureOrchestrator` in `com.aynvora.core.intelligence`). Feature domain implementations
    must NEVER call one another's internal implementations or repositories directly.

51. **Evidence-Based Synthesis Rule.**
    Combined analysis must be based on structured evidence (`EvidenceBundle`, `EvidenceItem`,
    `EvidenceCategory`), not hidden feature coupling. Explicit distinctions must be maintained
    between `FACT`, `DERIVED_FACT`, `TRADITIONAL_RULE`, `INTERPRETATION`, and `USER_CONTEXT`.

52. **Traceable Provenance Rule.**
    Every evidence-producing tool must preserve source, tradition, ruleset/edition, engine version,
    timestamp, and timezone provenance. Provenance must survive through tool execution,
    orchestration, AI explanation, and UI presentation without being lost.

53. **AI Boundary & Non-Authority Rule.**
    The on-device SLM/AI Assistant may select tools and summarize verified results, but must never
    become the calculation, truth, or database authority. AI must never query Room DAOs directly or
    invent planetary positions, verses, or gemstone metrics.

54. **Prediction As Interpretation Rule.**
    Future-looking output generated by astrology, dasha, transit, or tarot is classified as
    traditional/astrological interpretation (`TRADITIONAL_INTERPRETATION`, `FUTURE_TIMING_WINDOW`,
    `CONDITIONAL_GUIDANCE`), never as guaranteed factual event predictions.

55. **Conflict Preservation Rule.**
    When multiple traditions or rulesets produce conflicting insights (e.g. Parashara vs Lal Kitab),
    the orchestrator must preserve and display both factors with `CONFLICTING_RULES` status.
    Conflicting factors must NEVER be silently overwritten or filtered to produce a "nicer" answer.

56. **Data Sufficiency Rule.**
    Before executing domain calculations, the orchestrator must validate data sufficiency (
    `DataSufficiencyValidator`). Missing required inputs must result in an explicit
    `INSUFFICIENT_DATA` response; default or fabricated values must never be synthesized.

57. **Personal Context Privacy Rule.**
    User context (birth details, coordinates, active gemstones, personal goals) remains strictly
    local-only by default. Personal context is passed only to approved domain tools through typed
    contracts.

58. **Orchestration Analytics Hygiene Rule.**
    Analytics events for cross-feature queries (`intelligence_query_started`,
    `intelligence_query_completed`, etc.) must carry only high-level sanitized identifiers (query
    IDs, counts, statuses) and must NEVER contain user question text, chart data, or personal
    reflections.

## Phase 8.0 Permanent Rules (Astrology Prediction Core & Timing Engine)

59. **Prediction Evidence Rule.**
    No future-looking astrology output or timing window may be generated without structured backing
    evidence. Every prediction must trace to observable astronomical chart factors, Dasha periods,
    and transit positions.

60. **Rule Provenance Rule.**
    Every predictive astrological rule must declare its tradition, canonical source title,
    chapter/verse citation, engine version, and unique rule ID. Rules must not be invented without
    source citation.

61. **Timing Window Rule.**
    Astrological timing outputs must be expressed as bounded date/time windows backed by convergence
    of Dasha and transit triggers. Spurious minute-level certainty for life events is prohibited.

62. **Conflict Visibility Rule.**
    Contradictory or modifying astrological indications (such as planetary cancellations,
    Neechabhanga, or tradition conflicts) must remain visible in the evidence graph. The engine must
    never suppress unfavorable or discordant factors.

63. **Interpretation Fidelity Rule.**
    Astrological interpretation templates may only elucidate factors present in the structured
    evidence bundle. Explanations must never introduce external ungrounded facts or promise
    guaranteed material outcomes.

64. **AI/SLM Calculation Bar Rule.**
    The on-device SLM may format, translate, and explain verified predictive evidence graphs, but
    must never perform astronomical calculations, Dasha timelines, or Panchang evaluations directly.

65. **Multi-Feature Extension Rule.**
    External core feature tools (Palmistry, Gemstones, Gita, Lal Kitab, Tarot) may contribute
    corroborating evidence to guidance synthesis only through typed domain contracts, never through
    direct internal modification of the astrology calculation engine.

66. **Outcome Verification Lab Rule.**
    Internal QA and verification tracking for predictive rules must record exact input parameters,
    calculation versions, and rule matches without fabricating or marketing an ungrounded "accuracy
    percentage".

## Phase 8.2 Permanent Rules (Reference Validation Governance)

67. **Exactness Classification Rule.** Exact and tolerance matches are separate statuses. Every
    tolerance match records its numeric tolerance and justification.
68. **House System Rule.** Every house result identifies its explicit house-system profile. Results
    from different systems are never compared as equivalent.
69. **Golden Test Rule.** Reference tests contain expected values, calculation inputs/profile, and a
    precise source/page citation. Test counts distinguish executable tests from calculation
    coverage.
70. **No Overfitting Rule.** Do not change a formula solely to reproduce a single third-party
    report. Formula changes require a recognized source/convention and regression coverage.
71. **Varga Validation Rule.** Every supported divisional chart needs independent reference coverage
    before it can be described as fully reference-validated.
72. **Feature Registry Rule.** Numerology, Rudraksha, Jadi, and Yantra remain first-class core
    domains and remain `FOUNDATION_ONLY` until a genuinely implemented engine exists.

## Change Rule
If a requested feature conflicts with a rule, stop and document the conflict before implementation.
