# AYNVORA Centralized Event-to-Analytics Bridge

Version: 1.0 (Phase 9.0)

## 1. Zero Direct UI Analytics Tracking

In the legacy architecture, individual UI callbacks manually called
`analyticsTracker.track(AnalyticsEvent.*)`.
Under Phase 9.0:

- UI emits typed `AynvoraEvent`.
- Central `AynvoraEventAnalyticsBridge` observes events dispatched through `AynvoraEventDispatcher`.
- `AynvoraEventAnalyticsMapper` maps approved events to typed `AnalyticsEvent`s.

## 2. Privacy Allowlist & Forbidden Data Rules

`AnalyticsSafePayload` enforces zero-PII transmission:

- **Forbidden Fields & Keywords**:
    - `password`, `token`, `secret`, `credential`
    - `birth_date`, `birth_time`, `latitude`, `longitude`
    - `palm_image`, `image_bytes`
    - `prompt`, `ai_output`
    - `user_question`, `question_text`, `private_note`
- Events containing forbidden strings are silently discarded from the analytics pipeline.
- No arbitrary event payloads are ever serialized or dumped into analytics telemetry.

## 3. Failure Insulation

Analytics is strictly an observational side-effect. Any network, disk, or runtime exception inside
`AnalyticsTracker` is isolated inside a try-catch boundary in `AynvoraEventAnalyticsBridge`.
Analytics failures **never** abort business actions, state changes, or user navigation.
