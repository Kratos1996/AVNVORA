# AYNVORA Core Feature Product Roadmap

Version: 1.1 (Phase 8.1 — Reference Validation + 14-Domain Registration)

## Purpose

This document provides the authoritative delivery status, architecture readiness, dependency matrix,
and future implementation plan for all 14 first-class core product domains in AYNVORA.

---

## 1. Feature Status Summary

| Feature                                   | Foundation Status | Implementation Status | Data Requirements                                                                                   | Analytics Status             | Dedicated Target Phase                                   |
|-------------------------------------------|-------------------|-----------------------|-----------------------------------------------------------------------------------------------------|------------------------------|----------------------------------------------------------|
| **1. Vedic Astrology**                    | `IMPLEMENTED`     | `IMPLEMENTED`         | Ephemeris, coordinates, math                                                                        | Consent-gated                | Active Core / Phase 8.2 parity audit; JKR parity partial |
| **2. Tarot Reflection**                   | `VERIFIED`        | `IMPLEMENTED`         | Room 78-card deck, local readings                                                                   | Consent-gated                | Phase 7.1 / 7.2                                          |
| **3. Numerology**                         | `VERIFIED`        | `FOUNDATION_ONLY`     | Birth date, full name                                                                               | No PII events                | Phase 8.9                                                |
| **4. Palmistry / Hastrekha**              | `VERIFIED`        | `FOUNDATION_ONLY`     | Local hand camera image                                                                             | Local-only privacy           | Phase 8.1                                                |
| **5. Gemstone / Navaratna**               | `VERIFIED`        | `FOUNDATION_ONLY`     | Wearing inventory, Lab OCR                                                                          | No PII events                | Phase 8.2                                                |
| **6. Bhagavad Gita**                      | `VERIFIED`        | `FOUNDATION_ONLY`     | Sanskrit shlokas, public domain translations                                                        | Content viewed               | Phase 8.3                                                |
| **7. Garuda Puran**                       | `VERIFIED`        | `FOUNDATION_ONLY`     | Reviewed source-edition package and selected-language translations; none bundled in this repository | Topic viewed (topic ID only) | Phase 8.4 pipeline implemented; corpus pending           |
| **8. Lal Kitab Tradition**                | `VERIFIED`        | `FOUNDATION_ONLY`     | 1939-1952 traditional rules                                                                         | Rule explored                | Phase 8.5                                                |
| **9. On-Device AI Assistant**             | `VERIFIED`        | `FOUNDATION_ONLY`     | Local small LLM weights (Qwen3 0.6B/1.7B)                                                           | Tool call metrics            | Phase 8.6                                                |
| **10. Daily Guidance & Practice**         | `VERIFIED`        | `FOUNDATION_ONLY`     | Sunrise/Panchang, Gita reflection                                                                   | Routine started              | Phase 8.7                                                |
| **11. Wallpaper Studio**                  | `VERIFIED`        | `FOUNDATION_ONLY`     | Device profiles, Rashi/Nakshatra tokens                                                             | Prompt generated             | Phase 8.8                                                |
| **12. Rudraksha**                         | `VERIFIED`        | `FOUNDATION_ONLY`     | Mukhi classification, tradition source                                                              | Feature opened               | Phase 8.10                                               |
| **13. Jadi / Sacred Roots**               | `VERIFIED`        | `FOUNDATION_ONLY`     | Root catalog, source text                                                                           | Feature opened               | Phase 8.11                                               |
| **14. Yantra**                            | `VERIFIED`        | `FOUNDATION_ONLY`     | Yantra type, tradition source                                                                       | Feature opened               | Phase 8.12                                               |
| **15. Core Intelligence & Orchestration** | `VERIFIED`        | `IMPLEMENTED`         | Multi-source evidence, sufficiency validator, conflict preservation                                 | Query metrics                | Phase 7.5                                                |

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

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.1)
- **Domain Contracts**: `PalmFinding`, `HandImageReference`, `PalmLineFinding`, `PalmMountFinding`,
  `PalmistryAnalysisResult`.
- **Privacy Lock**: Strict on-device default. Zero image transmission to cloud or analytics.
- **Dependencies**: Camera permission, on-device contour/pose extraction.

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

- **Current Status**: `FOUNDATION_ONLY` (Phase 8.4 source-gated pipeline implemented; no corpus
  available)
- **Domain Contracts**: `GarudaPuranContentItem`, `GarudaPuranSourceEdition`,
  `GarudaPuranReference`, `GarudaPuranEvidenceGraphFactory`, and `GarudaPuranRepository`.
- **Source inventory**: There are no source passages, translations, canonical citations, or
  installed Garuda Puran package in the repository. The roadmap's former Saroddhara reference was a
  content requirement, not shipped data.
- **Ethical lock**: Traditional teachings remain labelled as scriptural/traditional. Never use them
  for fear-based prognostication or as medical, legal, financial, or scientific claims.
- **Next dependency**: Supply and review an authorized, versioned source package before marking any
  topic available. See `GARUDA_PURAN_ARCHITECTURE.md`.

### 6. Lal Kitab Tradition

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.5)
- **Domain Contracts**: `LalKitabRule`, `LalKitabRemedy`, `LalKitabSource`, `LalKitabRepository`.
- **Separation Lock**: Completely isolated from classical Parashara rules. Strictly non-harm folk
  remedies.

### 7. Tarot Reflection

- **Current Status**: `IMPLEMENTED` & `VERIFIED`
- **Architecture**: 78 canonical cards seeded in Room KMP, offline single/three-card spreads,
  non-predictive ethical disclaimer.

### 8. On-Device AI Assistant

- **Current Status**: `FOUNDATION_ONLY` (Target: Phase 8.6)
- **Domain Contracts**: `AiAssistant`, `AiTool`, `AiToolCall`, `AiToolResult`, `AiProvenance`,
  `AiModelInfo`, `AiToolRegistry`.
- **Tool Implementations**: `CalculateBirthChartTool` (wired to `AynvoraSdk`), `SearchGitaTool` (
  wired to `GitaRepository`).
- **Authority Lock**: Explainer and tool orchestrator only; never calculation authority. Zero direct
  Room or Astro Engine internal access.

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
