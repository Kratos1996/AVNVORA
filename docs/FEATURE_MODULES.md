# AYNVORA Feature Engine Modules Inventory

This document details the modular engines available in the AYNVORA platform, their inputs, outputs, calculation determinism, and optionality.

---

## 1. Engine Modules Overview

| Engine Module | Feature ID | Calculation Engine | Primary Event | Output Type |
|---|---|---|---|---|
| `:astro-engine` | `ASTROLOGY` | Sidereal Ephemeris / JKR Parity | `CALCULATE_CHART` | `AstrologyResult` |
| `:palmistry-engine` | `PALMISTRY` | Hand Landmark & Crease Analysis | `ANALYZE_HAND` | `PalmistryResult` |
| `:numerology-engine` | `NUMEROLOGY` | Pythagorean, Chaldean, Vedic, Tamil, Kabbalah, Chinese | `CALCULATE_PROFILE` | `NumerologyResult` |
| `:tarot-engine` | `TAROT` | 78-Card Rider-Waite-Smith Engine | `DRAW_CARDS` | `TarotResult` |
| `:gemstone-engine` | `GEMSTONES` | Planetary Gemstone Recommendation | `RECOMMEND_GEMSTONE` | `GemstoneResult` |
| `:gita-engine` | `GITA` | 700 Bhagavad Gita Shlokas & Cross-Reference | `LOOKUP_VERSE` | `GitaResult` |
| `:garuda-puran-engine` | `GARUDA_PURAN` | Garuda Purana Eschatology & Guidance | `QUERY_TOPIC` | `GarudaPuranResult` |
| `:rudraksha-engine` | `RUDRAKSHA` | 1-21 Mukhi Bead Matching | `RECOMMEND_BEAD` | `RudrakshaResult` |
| `:jadi-engine` | `JADI` | Herbal Root Substitutes (Astro-Botanical) | `RECOMMEND_ROOT` | `JadiResult` |
| `:yantra-engine` | `YANTRA` | Sacred Geometry & Planetary Yantras | `RECOMMEND_YANTRA` | `YantraResult` |
| `:guidance-engine` | `DAILY_GUIDANCE` | Synthesis & Daily Recommendations | `GET_GUIDANCE` | `GuidanceResult` |
| `:ai-engine` | `AI_ASSISTANT` | Evidence Grounding (Qwen-2.5-1.5B GGUF) | `ASK_QUESTION` | `AiGroundingResponse` |
| `:report-engine` | `REPORT_SYNTHESIS` | Multi-Source Astrological/Hand Synthesis | `SYNTHESIZE_REPORT`| `ReportResult` |

---

## 2. Dependency Rules

1. **No Engine-to-Engine Coupling**:
   - `:astro-engine` does NOT depend on `:palmistry-engine`.
   - `:palmistry-engine` does NOT depend on `:astro-engine`.
   - `:ai-engine` does NOT depend on any calculation engines. It consumes grounding JSON evidence.
   - `:report-engine` does NOT invoke engines. It receives pre-computed feature JSON outputs.

2. **No UI or Presentation in Engines**:
   Engines do not import Jetpack Compose, Compose Multiplatform, Skiko, or UI themes.

3. **No Third-Party Analytics SDKs in Engines**:
   Engines emit canonical events through the SDK event router. No Firebase, Mixpanel, or Amplitude SDKs are included.

4. **Zero Training Modification**:
   - `FINE_TUNING = TRAINING_PREPARED`
   - Production model: `qwen2.5-1.5b-instruct-q5_k_m.gguf`
   - Model modifications: NONE
