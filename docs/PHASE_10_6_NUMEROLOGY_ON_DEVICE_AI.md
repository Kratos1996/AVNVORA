# Phase 10.6: Numerology Conversational On-Device AI

## 1. Overview & Objectives

Phase 10.6 implements the conversational, on-device AI explanation layer for AYNVORA Numerology
across all 12 supported tradition rulesets.

### Key Tenets

1. **Explanation Layer, Never Authority**: AI does not calculate, recalculate, or alter numbers.
   Numbers are sourced exclusively from the deterministic calculation engine.
2. **Multi-Tradition Semantic Isolation**: Rulesets are strictly isolated; non-personality systems (
   Gematria, Abjad, Katapayadi) do not generate personality horoscopes.
3. **Multilingual Support**: Supports all 11 project locales (`en`, `hi`, `ar` [RTL], `bn`, `gu`,
   `mr`, `pa`, `ta`, `te`, `kn`, `ml`).
4. **Deterministic Fallback**: 100% offline, source-grounded fallback ensures full functionality
   without downloaded SLM models or on inference failure.
5. **Zero PII Analytics**: Telemetry records only technical performance metadata (`ruleset_id`,
   `category`, `status`, `fallback_used`), with no personal data or conversational text logged.
6. **Strict Scope Enforcement**: Gemstone, Rudraksha, Yantra, and Jadi features are completely
   deferred to future phases.

---

## 2. Components Implemented

### Domain & Engine (`aynvora-core`)

- `NumerologyAiModels.kt`: Data contracts for AI capabilities, grounding contexts, responses,
  conversation state, and the `NumerologyExplanationEngine` interface.
- `NumerologyFeatureDataConnector.kt`: Extracts verified calculation results, calculation traces,
  and localized interpretations into `AiEvidence` nodes.
- `NumerologyAiPromptTemplate.kt`: Constructs system and user prompts enforcing the 17 core rules,
  including prompt injection defense via `<untrusted_user_question>`.
- `NumerologyAiOutputValidator.kt`: Enforces safety boundaries (rejects fatalism, medical,
  financial, or scientific claims; rejects personality predictions on non-personality traditions;
  checks number preservation).
- `DeterministicNumerologyExplanationEngine.kt`: Provides instant, fully grounded, 100% offline
  explanations.
- `GroundedSlmNumerologyExplanationEngine.kt`: Integrates with `AiInferenceEngine` for on-device SLM
  execution, automatically falling back to the deterministic engine when needed.

### Event SDK & Analytics (`aynvora-core`, `ui`)

- Added 7 typed numerology AI events to `AynvoraEvent.kt`:
    - `NumerologyAiExplanationRequested`
    - `NumerologyAiExplanationCompleted`
    - `NumerologyAiExplanationFailed`
    - `NumerologyAiFallbackTriggered`
    - `NumerologyAiValidationFailed`
    - `NumerologyAiConversationStarted`
    - `NumerologyAiConversationCleared`
- Registered in `AynvoraEventRegistry.kt`.
- Bridged to `AnalyticsEvent` in `AynvoraEventAnalyticsBridge.kt` with zero PII attributes.

### UI Integration (`ui`)

- `NumerologyUiState.kt`: Integrated conversation state, loading flags, error message, and text
  input state.
- `NumerologyUiEvent.kt`: Added events for typing, submitting questions, and clearing conversations.
- `NumerologyViewModel.kt`: Wired to `aiExplanationEngine`, handling lifecycle, async dispatching,
  and error handling.
- `NumerologyRoute.kt`: Replaced placeholder modal with `NumerologyAiConversationModal`:
    - Contemplative disclosure notice
    - Quick-prompt category chips
    - Formatted chat bubbles with source citations and evidence badges
    - Input field with clear history action

### Test Suite (`aynvora-core`, `ui`)

- 8 comprehensive test suites verifying grounding, fallback, adversarial injection defense, ruleset
  isolation, locale fidelity, and ViewModel state transitions.
