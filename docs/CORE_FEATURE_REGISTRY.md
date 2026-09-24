# AYNVORA Core Feature Registry

Version: 1.0 (Phase 7.4 — Core Product Feature Foundation)

## Purpose

The Core Feature Registry serves as the canonical architectural manifest defining AYNVORA's
first-class product domains. Each product domain defines its explicit boundaries, Clean Architecture
package ownership, persistence policy, analytics hygiene, localization namespaces, and cross-domain
data dependencies.

---

## 1. Feature Registry & Status Overview

| #  | Domain ID        | Domain Title                  | Architectural Tier / Primary Package                                                         | Status            | Primary Responsibility                                                                                                                                                |
|----|------------------|-------------------------------|----------------------------------------------------------------------------------------------|-------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | `ASTROLOGY`      | Vedic Astrology               | `:astro-engine`, `com.aynvora.core.models`, `:aynvora-data`                                  | `IMPLEMENTED`     | Pure deterministic astronomical calculations, Vargas, Shadbala, Ashtakavarga, Pinda.                                                                                  |
| 2  | `PALMISTRY`      | Hastrekha / Palmistry         | `com.aynvora.core.palmistry`, `com.aynvora.data.palmistry`                                   | `FOUNDATION_ONLY` | Camera/hand image capture reference, palm lines/mounts domain model, traditional rule contracts. On-device default.                                                   |
| 3  | `GEMSTONE`       | Gemstone / Ratna              | `com.aynvora.core.gemstone`, `com.aynvora.data.gemstone`                                     | `FOUNDATION_ONLY` | Gemstone catalog, user wearing inventory, certificate OCR/scale evidence provenance, ethical recommendation contracts.                                                |
| 4  | `GITA`           | Bhagavad Gita                 | `com.aynvora.core.gita`, `com.aynvora.data.gita`                                             | `FOUNDATION_ONLY` | Edition-aware Sanskrit shloka text, transliteration, authentic translations, and commentary provenance.                                                               |
| 5  | `GARUDA_PURAN`   | Garuda Puran                  | `com.aynvora.core.garudapuran`, `com.aynvora.data.garudapuran`, `com.aynvora.ui.garudapuran` | `FOUNDATION_ONLY` | Typed, source-gated offline content/report pipeline. No scripture corpus or content pack is bundled; topics remain unavailable until a reviewed package is installed. |
| 6  | `LAL_KITAB`      | Lal Kitab                     | `com.aynvora.core.lalkitab`, `com.aynvora.data.lalkitab`                                     | `FOUNDATION_ONLY` | Separate traditional ruleset with explicit conditions, non-fear-based traditional remedies, and rule provenance.                                                      |
| 7  | `TAROT`          | Tarot Reflection              | `com.aynvora.core.tarot`, `com.aynvora.data.tarot`, `com.aynvora.ui.tarot`                   | `IMPLEMENTED`     | Contemplative reflection tool, 78-card standard deck, offline Room storage, random draw engine, non-predictive framing.                                               |
| 8  | `AI_ASSISTANT`   | On-Device AI Chat             | `com.aynvora.core.ai`, `com.aynvora.data.ai`                                                 | `FOUNDATION_ONLY` | Local small-model assistant orchestrator, structured domain tool calling router, strict provenance, non-hallucination guardrails.                                     |
| 9  | `DAILY_GUIDANCE` | Daily Practice & Guidance     | `com.aynvora.core.guidance`, `com.aynvora.data.guidance`                                     | `FOUNDATION_ONLY` | Personalized morning routine, daytime focus, evening reflection, night relaxation, multiplatform scheduling.                                                          |
| 10 | `WALLPAPER`      | Personalized Wallpaper Studio | `com.aynvora.core.wallpaper`, `com.aynvora.data.wallpaper`                                   | `FOUNDATION_ONLY` | Device-aware visual prompt builder incorporating user Rashi/Nakshatra, copy/share handoff to user-authorized external generator.                                      |
| 11 | `NUMEROLOGY`     | Numerology                    | `com.aynvora.core.numerology`                                                                | `FOUNDATION_ONLY` | First-class domain; model contracts only, no calculation engine.                                                                                                      |
| 12 | `RUDRAKSHA`      | Rudraksha                     | `com.aynvora.core.rudraksha`                                                                 | `FOUNDATION_ONLY` | First-class domain; provenance-aware contracts only, no recommendation engine.                                                                                        |
| 13 | `JADI`           | Jadi / Sacred Roots           | `com.aynvora.core.jadi`                                                                      | `FOUNDATION_ONLY` | First-class domain; source and catalog contracts only, no recommendation engine.                                                                                      |
| 14 | `YANTRA`         | Yantra                        | `com.aynvora.core.yantra`                                                                    | `FOUNDATION_ONLY` | First-class domain; tradition/source contracts only, no recommendation engine.                                                                                        |

### Future report, localization, capability and analytics taxonomy

Reserve `numerology`, `rudraksha`, `jadi`, and `yantra` namespaces in future report schemas and
localized key planning. Intelligence capabilities and analytics event names for these domains are
registered, but remain metadata only; they do not imply calculation engines. Keep all four feature
statuses `FOUNDATION_ONLY` until their engines exist.

---

## 2. Canonical Domain Boundaries & Contracts

### 1. Astrology (`ASTROLOGY`)

- **Engine Authority**: `:astro-engine` remains the pure calculation engine. Zero Compose, UI, Room,
  Firebase, or AI dependencies.
- **Formulas**: Deterministic Ephemeris, D1..D60 divisional charts, Shadbala, Ashtakavarga, and
  Shodhya Pinda calculations.
- **Contract Boundary**: Exposes immutable domain results through `AynvoraSdk` facade.

### 2. Palmistry / Hastrekha (`PALMISTRY`)

- **Domain Models**: `PalmSession`, `HandImageReference`, `PalmRegion`, `PalmLine`, `PalmFinding`,
  `PalmAnalysis`, `PalmistryRuleSet`, `PalmistryAnalysisResult`.
- **Privacy Boundary**: Local-only by default. Hand images must NEVER be transmitted to analytics or
  cloud storage without explicit opt-in consent.
- **Ethical Gate**: Non-medical; strictly avoids deterministic lifespan or health guarantees.

### 3. Gemstone / Ratna (`GEMSTONE`)

- **Domain Models**: `Gemstone`, `GemstoneType`, `GemstoneCertificate`, `GemstoneEvidence`,
  `GemstoneInventoryItem`, `GemstoneWearingContext`, `GemstoneEvaluation`, `GemstoneRecommendation`,
  `GemstoneProvenance`.
- **Evidence Integrity**: Camera visual inspection is modeled as physical observation evidence, not
  proof of authenticity or weight.
- **Inventory Awareness**: Recommendations must evaluate currently worn gemstones (
  `GemstoneWearingContext`) to prevent contraindications.

### 4. Bhagavad Gita (`GITA`)

- **Domain Models**: `GitaChapter`, `GitaVerse`, `SanskritText`, `Transliteration`, `Translation`,
  `Commentary`, `ThemeTag`, `SourceEdition`, `GitaProvenance`.
- **Integrity**: Original Sanskrit verses are kept strictly distinct from edition-specific
  translations and commentaries.
- **Separation**: Wisdom and philosophical contemplation domain — not deterministic future
  prediction.

### 5. Garuda Puran (`GARUDA_PURAN`)

- **Domain Models**: `GarudaPuranTopicId`, `GarudaPuranSection`, `GarudaPuranReference`,
  `GarudaPuranSourceEdition`, `GarudaPuranText`, `GarudaPuranInterpretation`, `GarudaPuranPractice`,
  `GarudaPuranContentItem`, and typed availability reasons.
- **Source status**: No Garuda scripture, translation, citation set, or installed content pack is
  present in the repository. Topic IDs are taxonomy only; available content is empty until an
  authorized, reviewed package is supplied.
- **Corpus isolation**: Separate from astrology and Lal Kitab. Only explicit within-source
  references are represented in the EvidenceGraph; no cross-feature rule is inferred.
- **Product status**: `FOUNDATION_ONLY`; the content/report pipeline does not promote the product to
  available.

### 6. Lal Kitab (`LAL_KITAB`)

- **Domain Models**: `LalKitabRuleset`, `LalKitabRule`, `LalKitabCondition`, `LalKitabRemedy`,
  `LalKitabSource`, `LalKitabResult`.
- **Ruleset Independence**: Strictly isolated from classical `PARASHARA_CLASSICAL_V1` rulesets.

### 7. Tarot (`TAROT`)

- **Domain Models**: `TarotCard`, `TarotDeck`, `TarotSpread`, `TarotReading`, `TarotCardContent`,
  `TarotCardDraw`.
- **Boundary**: Completely decoupled from `:astro-engine` and planetary positions. Employs
  abstracted `TarotRandomSource`.

### 8. On-Device AI Chat / Local Assistant (`AI_ASSISTANT`)

- **Domain Models**: `AiAssistant`, `AiMessage`, `AiResponse`, `AiTool`, `AiToolCall`,
  `AiToolResult`, `AiProvenance`, `AiModelInfo`.
- **Authority Rule**: The AI is strictly an **explainer and tool orchestrator**, never the
  calculation authority.
- **Tool Protocol**: All domain facts (astrological charts, Panchang, Gita verses) are retrieved via
  typed domain tools (`AiTool`) with explicit provenance.

### 9. Daily Guidance / Practices (`DAILY_GUIDANCE`)

- **Domain Models**: `DailyGuidance`, `MorningRoutine`, `DayAction`, `EveningReflection`,
  `NightRoutine`, `PracticeRecommendation`, `DailyGuidanceContext`.
- **Experience Layer**: Harmonizes Panchang, user goals, and Gita reflection into actionable daily
  routines with user-controlled reminders.

### 10. Personalized Wallpaper Studio (`WALLPAPER`)

- **Domain Models**: `WallpaperRequest`, `WallpaperPrompt`, `DeviceProfile`, `ScreenDimensions`,
  `SafeAreas`, `VisualTheme`, `WallpaperStyle`.
- **Execution Boundary**: Formulates device-tailored high-fidelity generation prompts based on
  astrological tokens (Rashi, Nakshatra) for user-authorized external handoff.

---

## 3. Cross-Domain Dependency Matrix

```
       ASTRO  PALM  GEM  GITA  GARUDA  LAL_KITAB  TAROT  AI_ASST  GUIDANCE  WALLPAPER
ASTRO    -      No   No   No     No       No       No      No        No        No
PALM    Req*    -    No   No     No       No       No      No        No        No
GEM     Req*    No   -    No     No       No       No      No        No        No
GITA     No     No   No   -      No       No       No      No        No        No
GARUDA   No     No   No   No     -        No       No      No        No        No
LAL_KIT Req*    No   No   No     No       -        No      No        No        No
TAROT    No     No   No   No     No       No       -       No        No        No
AI_ASST Req*   Req* Req* Req*   Req*     Req*     Req*     -        Req*      Req*
GUIDANCE Req*   No  Req* Req*    No       No       No      No        -         No
WALLPAP Req*    No   No   No     No       No       No      No        No        -
```

*`Req*`: Feature may consume typed public domain models from the target feature via DI/contracts.
Never accesses internal data/database layers.

---

## 4. Privacy & Data Classification

| Data Category            | Examples                                                                                         | Default Storage                          | Transmission Policy                                                           |
|--------------------------|--------------------------------------------------------------------------------------------------|------------------------------------------|-------------------------------------------------------------------------------|
| **Sensitive Local Data** | Birth details, hand images, AI chat history, personal goals, family members, journal notes       | Local SQLite / Encrypted Mutex File      | Strict local-only. Never sent to analytics. Opt-in only for encrypted backup. |
| **Public Wisdom Corpus** | Bhagavad Gita verses, Garuda Puran text, Lal Kitab traditional rules, Tarot cards & descriptions | Local Room KMP Read-only Cache           | Seeded offline; synchronized via content packs with SHA-256 verifier.         |
| **Analytics Metadata**   | Feature opened, tool invoked, calculation completed, duration, screen viewed                     | Firebase Analytics (Android host) / NoOp | Strictly consent-gated. Zero PII, zero astrology inputs/outputs.              |
