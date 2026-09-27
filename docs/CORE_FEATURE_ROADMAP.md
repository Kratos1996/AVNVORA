# AYNVORA Core Feature Product Roadmap

Version: 1.1 (Phase 8.1 — Reference Validation + 14-Domain Registration)

## Purpose

This document provides the authoritative delivery status, architecture readiness, dependency matrix,
and future implementation plan for all 14 first-class core product domains in AYNVORA.

---

## 1. Feature Status Summary

| Feature                                   | Foundation Status | Implementation Status   | Data Requirements                                                                                                                               | Analytics Status                                 | Dedicated Target Phase                                   |
|-------------------------------------------|-------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------|----------------------------------------------------------|
| **1. Vedic Astrology**                    | `IMPLEMENTED`     | `IMPLEMENTED`           | Ephemeris, coordinates, math                                                                                                                    | Consent-gated                                    | Active Core / Phase 8.2 parity audit; JKR parity partial |
| **2. Tarot Reflection**                   | `VERIFIED`        | `IMPLEMENTED`           | Room 78-card deck, local readings                                                                                                               | Consent-gated                                    | Phase 7.1 / 7.2                                          |
| **3. Numerology**                         | `VERIFIED`        | `FOUNDATION_ONLY`       | Birth date, full name                                                                                                                           | No PII events                                    | Phase 8.9                                                |
| **4. Palmistry / Hastrekha**              | `VERIFIED`        | `IMPLEMENTED`           | Local hand camera/gallery image (`PalmImageSource`)                                                                                             | Local-only privacy                               | Phase 8.10 complete; full production experience          |
| **5. Gemstone / Navaratna**               | `VERIFIED`        | `FOUNDATION_ONLY`       | Wearing inventory, Lab OCR                                                                                                                      | No PII events                                    | Phase 8.2                                                |
| **6. Bhagavad Gita**                      | `VERIFIED`        | `FOUNDATION_ONLY`       | Sanskrit shlokas, public domain translations                                                                                                    | Content viewed                                   | Phase 8.3                                                |
| **7. Garuda Puran**                       | `VERIFIED`        | `PARTIALLY_IMPLEMENTED` | Reviewed source package `garuda-content-wood-1911-en-v1` (16 chapters) installed for English; Hindi unavailable under Option B                  | Topic viewed (topic ID only)                     | Phase 8.4B complete; English Saroddhara active offline   |
| **8. Lal Kitab Tradition**                | `VERIFIED`        | `FOUNDATION_ONLY`       | 1939-1952 traditional rules                                                                                                                     | Rule explored                                    | Phase 8.5                                                |
| **9. On-Device AI Platform & Runtime**    | `VERIFIED`        | `PARTIALLY_IMPLEMENTED` | Native SLM weights (Qwen2.5 0.5B/1.5B GGUF), native runtime driver, 64-bit ABI configuration, staging/rollback lifecycle, dynamic RAM safeguard | Technical lifecycle metrics only (no PII/prompt) | Phase 8.6, 8.7 & 8.8 completed                           |
| **10. Daily Guidance & Practice**         | `VERIFIED`        | `FOUNDATION_ONLY`       | Sunrise/Panchang, Gita reflection                                                                                                               | Routine started                                  | Phase 8.7                                                |
| **11. Wallpaper Studio**                  | `VERIFIED`        | `FOUNDATION_ONLY`       | Device profiles, Rashi/Nakshatra tokens                                                                                                         | Prompt generated                                 | Phase 8.8                                                |
| **12. Rudraksha**                         | `VERIFIED`        | `FOUNDATION_ONLY`       | Mukhi classification, tradition source                                                                                                          | Feature opened                                   | Phase 8.10                                               |
| **13. Jadi / Sacred Roots**               | `VERIFIED`        | `FOUNDATION_ONLY`       | Root catalog, source text                                                                                                                       | Feature opened                                   | Phase 8.11                                               |
| **14. Yantra**                            | `VERIFIED`        | `FOUNDATION_ONLY`       | Yantra type, tradition source                                                                                                                   | Feature opened                                   | Phase 8.12                                               |
| **15. Core Intelligence & Orchestration** | `VERIFIED`        | `IMPLEMENTED`           | Multi-source evidence, sufficiency validator, conflict preservation                                                                             | Query metrics                                    | Phase 7.5                                                |
| **16. Unified Event-Driven App SDK**      | `VERIFIED`        | `IMPLEMENTED`           | Typed immutable events, UI decoupling, navigation effects, security guard, idempotency, design system integration, central analytics bridge     | Centralized event observer bridge (zero PII)     | Phase 9.0 complete                                       |

---

## 2. Detailed Domain Roadmaps

The Numerology, Rudraksha, Jadi, and Yantra rows are permanent first-class registry entries at
`FOUNDATION_ONLY`. Their capability metadata and event taxonomy do not imply an implemented
calculation engine. Future report schemas and localization keys reserve matching domain namespaces.

### 1. Vedic Astrology

- **Current Status**: `IMPLEMENTED`; JKR reference validation is partial, not full-profile verified.
- **Architecture**: Pure calculation engine (`:astro-engine`) exposed via `AynvoraSdk` facade.
  Prediction interpretation, rule engine, multi-signal convergence, and timing window generators in
  `:aynvora-core`.
- **Delivered in Phase 8.0**:
    - Vimshottari Dasha Engine (120-year cycle, Mahadasha, Antardasha, Pratyantardasha balance).
    - Planetary Transit Engine (Gochara snapshots, timelines, ingresses, retrogrades).
    - Five-Limb Classical Panchang Engine (Tithi, Nakshatra, Yoga, Karana, Vara).
    - Classical Rule Evaluation Engine (BPHS Yogakaraka, Dhana Yoga, Gajakesari Yoga, Phaladeepika
      transits).
    - Life Topic Taxonomy (13 domains) & Bounded Timing Window Generator.
    - Prediction Evidence Graph with `traceWhy` backward traceability.
- **Reference validation**: see [REFERENCE_VALIDATION_JKR.md](REFERENCE_VALIDATION_JKR.md). The JKR
  PDF is present under `docs/`; calculation-level matches, ambiguities, and unsupported cases are
  tracked there.

### 2. Palmistry / Hastrekha

- **Current Status**: `IMPLEMENTED` (Phase 8.10 complete)
- **Delivered in Phase 8.10**:
    - Real image input abstraction (`PalmImageSource`) & deterministic quality validator (
      `ImageQualityAssessment`).
    - Deterministic gradient & contour analysis engine (`PalmImageAnalysisEngine`) for Life, Head,
      Heart, Fate lines and Palm Shape.
    - Honest capability boundaries (`PalmistryAnalysisCapabilities`) marking unmeasured lines as
      `NOT_DETECTED`/`UNSUPPORTED`.
    - Classical Samudrika Shastra bilingual interpretations (`PalmistryContentPackage`) in English
      and Hindi.
    - Grounded on-device AI explanation (`GroundedSlmPalmistryExplanationEngine`) with fallback (
      `DeterministicPalmistryExplanationEngine`).
    - Evidence-bounded follow-up Q&A (`PalmQuestionEngine`) with `INSUFFICIENT_EVIDENCE` checks.
    - Reading timeline, multi-tier feedback (1-5 stars, per-answer, per-feature), and anonymous
      `AiImprovementSignal`.
    - Report Engine (`PalmistryReportGenerator`) & PDF export integration.
    - Compose Multiplatform 10-screen UI (`PalmistryRoute.kt`) with top-right language switcher
      preserving state.
    - Root navigation hook in `AynvoraApp.kt`.
- **Privacy Lock**: Strict on-device default. Zero image transmission to cloud or analytics.
  User-deletable sessions.

### 3. Gemstone / Navaratna

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.2)
- **Domain Contracts**: `GemstoneType`, `GemstoneInventoryItem`, `GemstoneWearingContext`,
  `GemstoneCertificate`, `GemstoneRecommendation`.
- **Integrity Lock**: Camera inspection is treated as physical evidence, not absolute proof of
  purity/weight. Recommendations evaluate existing worn gems to avoid planetary conflict.

### 4. Bhagavad Gita

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.3)
- **Domain Contracts**: `GitaChapter`, `GitaVerse`, `GitaSourceEdition`, `GitaRepository`.
- **Implementation**: Starter Nishkama Karma verses seeded in `InMemoryGitaRepository`.
- **Integrity Lock**: Original Sanskrit verses kept strictly distinct from commentary and
  translations.

### 5. Garuda Puran

- **Current Status**: `PARTIALLY_IMPLEMENTED` (Phase 8.4B completed: primary approved package
  `garuda-content-wood-1911-en-v1`
  ingested with SHA-256 `4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5`; all 16
  chapters covered across 10 typed topics;
  secondary cross-reference with Dutt 1908 in `GarudaSourceComparisonRegistry`; report engine
  integrated; Hindi handled via Option B as `CONTENT_UNAVAILABLE`).
- **Domain Contracts**: `GarudaPuranContentItem`, `GarudaPuranSourceEdition`,
  `GarudaPuranReference`, `GarudaPuranEvidenceGraphFactory`, `GarudaPuranRepository`,
  `GarudaSourceManifest`, `GarudaSource`, `GarudaEdition`, `GarudaSourceLicense`,
  `GarudaChapter`, `GarudaVerse`, `GarudaChecksumVerifier`, and `GarudaContentIngestionPipeline`.
- **Source Research (Phase 8.4A/B)**: 7 candidate sources cataloged. Ingested Ernest Wood 1911 as
  primary approved edition for verified jurisdictions (`PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS`).
  Manmatha Nath Dutt 1908 integrated as secondary comparative source. Gita Press Code 1416, MLBD
  1978,
  GRETIL, and SanskritDocuments strictly maintained as `REFERENCE_ONLY_NON_DISTRIBUTABLE`.
- **Ethical lock**: Traditional teachings remain labelled as scriptural/traditional. Never use them
  for fear-based prognostication or as medical, legal, financial, or scientific claims. Zero
  AI-generated verses.
- **Next dependency**: Future independent unencumbered Hindi translation authoring/verification. See
  `docs/garuda-puran/`.

### 6. Lal Kitab Tradition

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.5)
- **Domain Contracts**: `LalKitabRule`, `LalKitabRemedy`, `LalKitabSource`, `LalKitabRepository`.
- **Separation Lock**: Completely isolated from classical Parashara rules. Strictly non-harm folk
  remedies.

### 7. Tarot Reflection

- **Current Status**: `IMPLEMENTED` & `VERIFIED`
- **Architecture**: 78 canonical cards seeded in Room KMP, offline single/three-card spreads,
  non-predictive ethical disclaimer.

### 8. On-Device AI Platform & Runtime

- **Current Status**: `PARTIALLY_IMPLEMENTED` (Phase 8.6, 8.7 & 8.8 completed)
- **Domain Contracts**: `AiDeviceCapabilityDetector`, `AiModelSelector`, `AiModelLifecycleManager`,
  `AiInferenceEngine`, `LocalNativeInferenceEngine`, `LlamaNativeRuntimeDriver`, `NativeAiRuntime`,
  `TarotFeatureDataConnector`, `AiContextBuilder`, `AiOutputValidator`,
  `GroundedSlmTarotExplanationEngine`.
- **Runtime Features**: Real GGUF model download/verification (SHA-256), atomic install, rollback on
  failed
  checksum, dynamic RAM checking, context token overflow protection, native request timeout and
  cancellation,
  native resource cleanup, 64-bit Android ABI configuration, offline-only inference (zero network
  calls),
  zero-configuration consumer UX, developer diagnostics screen.
- **Authority Lock**: Explainer and tool orchestrator only; never calculation authority. Zero direct
  Room or Astro Engine internal access. Authoritative Tarot readings and EvidenceGraph are
  immutable.

### 9. Daily Guidance & Practice

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.7)
- **Domain Contracts**: `MorningRoutine`, `NightRoutine`, `DailyGuidance`, `DailyGuidanceContext`,
  `DailyGuidanceRepository`.
- **Scheduling**: Multiplatform lifecycle-aware scheduler. Does not depend on guaranteed background
  push notifications.

### 10. Personalized Wallpaper Studio

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.8)
- **Domain Contracts**: `WallpaperRequest`, `WallpaperPrompt`, `DeviceProfile`, `SafeAreas`,
  `WallpaperPromptBuilder`.
- **Implementation**: `DefaultWallpaperPromptBuilder` operational with device safe area and aspect
  ratio calculations.
- **Handoff Lock**: Copy/Share to user-authorized external generator. Never claims to generate
  images internally without an on-device rendering engine.
