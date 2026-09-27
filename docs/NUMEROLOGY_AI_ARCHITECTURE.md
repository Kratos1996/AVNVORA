# AYNVORA Numerology AI Explanation Architecture

Version: 1.1 (Phase 10.7 Hardened)  
Status: Production Hardened & Regression Verified  
Engines: `GroundedSlmNumerologyExplanationEngine`, `DeterministicNumerologyExplanationEngine`

---

## 1. Core Principle: Explanation Layer, Not Numerical Authority

AYNVORA's on-device SLM models (e.g. Qwen2.5-Instruct 0.5B/1.5B GGUF) serve strictly as an *
*explanatory and conversational synthesis layer**:

- **Strict Boundary**: The AI layer **never** calculates, re-calculates, or alters any numbers.
- **Authoritative Provenance**: Numbers (Life Path, Moolank, Bhagyank, Name Number, Soul Urge,
  Personality, Lo Shu Grid, Nine Star Ki, Gematria, Abjad, Katapayadi, Tarot Birth Cards) are
  computed solely by `NumerologyCalculationEngine` and `NumerologyReductionEngine`.
- **Zero Recalculation**: If a user prompts the AI to change their number or recalculate it, the
  prompt defense and `NumerologyAiOutputValidator` forcefully reject the claim.

---

## 2. Grounding Flow & Provenance Architecture

```
User Input (Birth Date, Name, Tradition Selection)
       │
       ▼
NumerologyCalculationEngine (Deterministic Calculation)
       │  - Reduction, Master Number Rules, Planetary Rulers, Traces
       ▼
NumerologyFeatureDataConnector
       │  - Builds NumerologyAiGroundingContext
       │  - Gathers AiEvidence nodes (AiPrivacyClass.STRICT_LOCAL_ONLY)
       │  - Attaches calculation traces and authoritative source provenance
       ▼
Prompt Injection Defense & Context Packaging
       │  - NumerologyAiPromptTemplate
       │  - Isolates untrusted user prompt inside <untrusted_user_question>
       ▼
GroundedSlmNumerologyExplanationEngine
   ├──> LocalNativeInferenceEngine (On-Device SLM execution)
   │       └──> NumerologyAiOutputValidator (Phase 10.7 Hardened)
   │               - Rejects fatalism, medical, financial, and scientific claims
   │               - Rejects recalculation overrides ("i calculated", "overriding the calculation")
   │               - Rejects invented sources ("secret book", "unverified modern channel")
   │               - Rejects personality analysis on non-personality systems
   │               - Enforces number preservation & tradition isolation
   └──> DeterministicNumerologyExplanationEngine
           - 100% offline, source-grounded fallback triggered automatically on:
             * Model not downloaded / unallocated
             * Inference failure or timeout
             * Validation rejection
             * Locale unsupported by local SLM (preserves authentic localized strings)
       │
       ▼
NumerologyAiResponse & NumerologyConversationState
       │  - Rendered in NumerologyAiConversationModal
       │  - Zero PII Event Telemetry dispatched to Event SDK
```

---

## 3. Strict Tradition Isolation & Canonical Ruleset Identities

AYNVORA maintains single canonical ruleset identities across calculation, interpretation,
EvidenceGraph, AI grounding, UI, reports, and analytics:

| Canonical Ruleset ID             | Tradition           | Primary Source Citation                                         | Non-Personality System |
|----------------------------------|---------------------|-----------------------------------------------------------------|------------------------|
| `PYTHAGOREAN_WESTERN_V1`         | Pythagorean Western | Matthew Oliver Goodwin, 'Numerology: The Complete Guide' (1981) | No                     |
| `CHALDEAN_CHEIRO_V1`             | Chaldean            | Cheiro (William John Warner), 'Cheiro's Book of Numbers' (1926) | No                     |
| `INDIAN_ANK_JYOTISH_V1`          | Indian Ank Jyotish  | Harish Johari (1990) & Pandit Sethuraman (1954)                 | No                     |
| `TAMIL_VEDIC_V1`                 | Tamil Vedic         | Pandit Sethuraman, 'Science of Fortune' (1954)                  | No                     |
| `LO_SHU_CLASSICAL_V1`            | Lo Shu Magic Square | I Ching (Book of Changes) & Classical Han Dynasty Lo Shu        | No                     |
| `CHINESE_NINE_STAR_KI_V1`        | Nine Star Ki        | Master Takashi Yoshikawa, 'The Ki' (1984)                       | No                     |
| `HEBREW_GEMATRIA_STANDARD_V1`    | Hebrew Gematria     | Sefer Yetzirah & Classical Hebrew Alphanumeric Tradition        | Yes                    |
| `ARABIC_ABJAD_MASHRIQI_V1`       | Arabic Abjad        | Classical Semitic / Mashriqi Abjad Alphanumeric Ordering        | Yes                    |
| `INDIAN_KATAPAYADI_VARARUCHI_V1` | Indian Katapayadi   | Vararuchi (Chandra-Vakyani) & Sadratnamala                      | Yes                    |
| `AGRIPPAN_LATIN_V1`              | Agrippan Latin      | Heinrich Cornelius Agrippa, 'De Occulta Philosophia' (1533)     | No                     |
| `TAROT_BIRTH_CARD_V1`            | Tarot Birth Cards   | Mary K. Greer, 'Who Are You in the Tarot?' (1984)               | No                     |

**Enforcement**:

- Non-personality systems (Gematria, Abjad, Katapayadi) explicitly present notice banners and reject
  any attempt to generate personality horoscopes.
- Cross-tradition comparisons (`CROSS_TRADITION_COMPARISON`) output clearly distinguished, labeled
  sections and never produce synthetic "universal blended" meanings.

---

## 4. Report Authority vs. AI Reflection Boundary

Canonical report authority in AYNVORA is exclusively composed of:

1. `NumerologyResult` (mathematical calculation)
2. `NumerologyCalculationTrace` (reduction steps)
3. `NumerologyInterpretationPackage` (curated human tradition literature)
4. Authoritative Source Citation

AI-generated prose is **never** canonical report authority. When presented in report contexts, it is
explicitly branded as **"AI Reflection"** or **"Personal Synthesis"**, remaining visually and
structurally partitioned from traditional interpretations and calculations.

---

## 5. Native Runtime vs. Simulation Distinction

AYNVORA strictly distinguishes runtime environments:

- `NATIVE_RUNTIME_VERIFIED`: Local on-device SLM binary running via native inference library on
  physical Android or iOS device hardware with loaded model weights.
- `SIMULATED_RUNTIME_VERIFIED`: Validated with simulated engine (`LocalSimulationEngine`) honoring
  inference latency, memory quotas, and streaming token semantics.
- `TEST_HARNESS_VERIFIED`: Deterministic and mock-driven JVM unit/integration test suite executing
  automated verification contracts.

---

## 6. Privacy, Security & Zero PII Analytics

- **Offline-Only Execution**: Zero network requests or cloud AI endpoints are contacted.
- **Privacy Classification**: All numerological evidence is tagged with
  `AiPrivacyClass.STRICT_LOCAL_ONLY`.
- **Zero PII Telemetry**:
    - Telemetry payloads sent via `AynvoraEvent` and bridged to `AnalyticsEvent` contain only safe
      technical metadata: `ruleset_id`, `question_category`, `status`, `fallback_used`,
      `sanitized_error_code`, `response_time_ms`.
    - User names, birth dates, user question text, and AI response text are strictly excluded from
      telemetry.
