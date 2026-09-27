# AYNVORA Unified Event Architecture

Version: 2.0 (Phase 9.1 — Hardened & Audited)

## 1. Architectural Philosophy

The AYNVORA Unified Event-Driven App SDK establishes an architecture-wide reactive event foundation
for all user interactions, one-shot UI commands, navigation transitions, and telemetry.

### Unidirectional Event Pipeline

```
USER INTERACTION
  ↓
BASE CLICK LISTENER / EVENT DISPATCHER (aynvoraClickable / LocalAynvoraEventDispatcher)
  ↓
BASE VIEWMODEL onEvent() (AynvoraBaseViewModel)
  ↓
EVENT GUARD & IDEMPOTENCY (AynvoraEventGuard & AynvoraEventDeduplicator)
  ↓
PRIVATE EVENT HANDLER (handleEvent)
  ↓
BUSINESS ACTION / USE CASE EXECUTION
  ↓
STATE UPDATE                     ONE-SHOT EFFECT
(Persistent StateFlow<State>)    (Channel<AynvoraEffect>)
                                  ↓
                                 NAVIGATION / DIALOG / SHEET / SNACKBAR / SHARE
  ↓
ANALYTICS OBSERVER BRIDGE (AynvoraEventAnalyticsBridge)
  ↓ (Strict allowlist & AnalyticsSafePayload)
CENTRAL ANALYTICS TRACKER
```

## 2. Responsibilities

- **UI Composables**: Render persistent `State`, emit typed `AynvoraEvent`s, observe one-shot
  `AynvoraEffect`s. Zero business logic, zero direct repository calls, zero direct navigation
  decisions.
- **Base Click Listener (`aynvoraClickable`)**: Wraps Jetpack Compose / Multiplatform gestures,
  debounces double-taps according to `AynvoraDeduplicationPolicy`, preserves accessibility `Role`
  and semantics, and dispatches to `LocalAynvoraEventDispatcher`.
- **ViewModels (`AynvoraBaseViewModel`)**: Expose read-only `uiState`, accept public
  `onEvent(event)`, execute thread-safe private `handleEvent(event)`, update state atomically, and
  emit single-delivery effects.
- **Event Guard (`StandardAynvoraEventGuard`)**: Validates event structure, validates payloads,
  enforces permission boundaries, and rejects forged inputs before handler execution.
- **Deduplicator (`DefaultAynvoraEventDeduplicator`)**: Prevents rapid double-tap, accidental
  duplicate submission, and re-entrant deletion/download using monotonic timestamp windows and
  thread-safe mutex locks.
- **Analytics Bridge (`AynvoraEventAnalyticsBridge`)**: Non-intrusively observes completed events,
  passes them through `AnalyticsSafePayload` sanitization and `DefaultAynvoraEventAnalyticsMapper`,
  and records authorized analytics without blocking UI or failing business operations.

## 3. Phase 9.2 Route-Level Event & Callback Elimination Rules

1. **Route-Level Business Actions**: All business actions originating from Route-level composables (
   e.g. `PalmistryRoute`, `TarotRoute`, `GarudaPuranRoute`) must flow through
   `viewModel.onEvent(...)`.
2. **Zero Direct Business Repository Mutations**: Composables are prohibited from directly invoking
   repository or data source mutations (`saveSession`, `recordFeedback`, `updateQuestion`, etc.).
   All repository mutations must occur within ViewModel private handlers or domain use cases.
3. **Observer-Driven Lifecycle & Action Telemetry**: All business action and screen lifecycle
   analytics must be observer-driven via `AynvoraEventAnalyticsBridge`. Direct `analytics.track()`
   calls from UI composables are prohibited for business flows.
4. **Presentation-Only Callbacks Permitted**: Truly presentation-only callbacks (e.g. `onBack`,
   `onClose`, local dialog dismiss, local tab selection, list scroll animations) remain permitted
   when they do not perform domain mutations or telemetry side effects.
5. **Private Event Handlers**: Feature ViewModels must maintain
   `private suspend fun handleEvent(...)` registered via `registerEventHandler(::handleEvent)` in
   constructor/init. UI cannot access private handlers directly.
6. **Mandatory Event Registration**: Every dispatched event must be registered in
   `AynvoraEventRegistry` with explicit payload type ownership, feature scoping, and optional
   analytics mapping prior to dispatch. Unregistered events are rejected at the EventGuard.
