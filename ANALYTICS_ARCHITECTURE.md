# AYNVORA Centralized Analytics Architecture

## Architecture & Privacy Invariants

1. **No Direct Third-Party SDK Imports in Engines**:
   - Engines NEVER import Firebase Analytics, Mixpanel, or PostHog.
   - All engines are pure Kotlin modules emitting privacy-safe events through the central Core event router.
2. **Central Middleware Interception**:
   - Every `AynvoraEvent` and `AynvoraEventResponse` dispatched via `AynvoraEventRouter` automatically records duration, status, capability, and engine version.
3. **Strict Biometric & PII Privacy**:
   - For Palmistry: Analytics **never** transmits palm images, raw pixels, hand landmarks, or biometric line coordinates.
   - For Astrology: Analytics **never** transmits user names, exact birth moments, or coordinates.
   - Only coarse technical events (`PALMISTRY_ANALYZED`, `durationMs`, `validationStatus`) are reported.

## Data Flow

```mermaid
graph LR
    Engine[Feature Engine] --> Event[AynvoraEventResponse]
    Event --> Router[AynvoraEventRouter]
    Router --> Adapter[AnalyticsTracker Bridge]
    Adapter --> HostAnalytics[Host / Firebase Analytics]
```
