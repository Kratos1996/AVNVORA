# Phase: Analytics Foundation & Full Project Migration

## Status: COMPLETE

---

## Objective

Introduce Firebase Analytics across the existing AYNVORA project with a clean,
architecture-compliant abstraction. Establish permanent rules so every future feature
also integrates analytics correctly and safely.

---

## Architecture

```
androidApp                          ← FirebaseAnalyticsTracker (platform impl)
    │
    └── implements ──────────────► AnalyticsTracker (interface, aynvora-core)
                                        │
                              consumed by DefaultAynvoraSdk
                                        │
                              tracks AnalyticsEvent (sealed class, aynvora-core)
```

**Dependency direction**: `androidApp → aynvora-core`. Firebase SDK never leaks
into `aynvora-core`, `astro-engine`, or any shared module.

---

## Files Created / Modified

| File | Change |
|------|--------|
| `aynvora-core/.../analytics/AnalyticsEvent.kt` | Created — complete PII-free event catalog |
| `aynvora-core/.../analytics/AnalyticsTracker.kt` | Created — interface + `NoOpAnalyticsTracker` |
| `aynvora-core/.../AynvoraSdk.kt` | Modified — `analyticsTracker` param, instrumented all entry points |
| `androidApp/.../analytics/FirebaseAnalyticsTracker.kt` | Created — Firebase platform impl |
| `androidApp/.../MainActivity.kt` | Modified — wired tracker, fires `AppOpened` |

---

## AnalyticsEvent Catalog

All events are guaranteed PII-free by construction:

| Event Name | Class | Description |
|------------|-------|-------------|
| `app_opened` | `AppOpened` | App session start |
| `chart_calculation_requested` | `ChartCalculationRequested` | Natal chart calc start |
| `chart_calculation_succeeded` | `ChartCalculationSucceeded` | Natal chart success + duration |
| `chart_calculation_failed` | `ChartCalculationFailed` | Natal chart failure + error code |
| `divisional_chart_requested` | `DivisionalChartRequested` | Varga calc start |
| `divisional_chart_succeeded` | `DivisionalChartSucceeded` | Varga success + duration |
| `divisional_chart_failed` | `DivisionalChartFailed` | Varga failure |
| `shadbala_calculation_requested` | `ShadbalaCalculationRequested` | Shadbala start |
| `shadbala_calculation_succeeded` | `ShadbalaCalculationSucceeded` | Shadbala success |
| `shadbala_calculation_failed` | `ShadbalaCalculationFailed` | Shadbala failure |
| `ashtakavarga_calculation_requested` | `AshtakavargaCalculationRequested` | AVG start |
| `ashtakavarga_calculation_succeeded` | `AshtakavargaCalculationSucceeded` | AVG success |
| `ashtakavarga_calculation_failed` | `AshtakavargaCalculationFailed` | AVG failure |
| `shodhana_calculation_requested` | `ShodhanaCalculationRequested` | Shodhana start |
| `shodhana_calculation_succeeded` | `ShodhanaCalculationSucceeded` | Shodhana success |
| `pinda_calculation_requested` | `PindaCalculationRequested` | Pinda start |
| `pinda_calculation_succeeded` | `PindaCalculationSucceeded` | Pinda success |
| `birth_profile_saved` | `BirthProfileSaved` | Profile saved (no content) |
| `birth_profile_deleted` | `BirthProfileDeleted` | Profile deleted |
| `chart_saved` | `ChartSaved` | Saved chart created |
| `chart_deleted` | `ChartDeleted` | Saved chart deleted |
| `preference_changed` | `PreferenceChanged` | Preference key changed (no value) |
| `theme_toggled` | `ThemeToggled` | Dark/light toggle |
| `sdk_error_observed` | `SdkErrorObserved` | Unhandled SDK error (sanitized code) |

---

## Privacy Contract

- **No PII**: Birth date/time, birth coordinates, names, chart results, and raw
  exception messages are strictly prohibited from all event params.
- **Error sanitization**: `AynvoraResult.Failure` subtypes are mapped to opaque
  string codes (`invalid_input`, `calculation_failure`, etc.) — never raw messages.
- **Duration only**: Timing params (`duration_ms`) measure wall-clock milliseconds
  for performance telemetry only — no user-identifying timing patterns.
- **Analytics never fails loudly**: `FirebaseAnalyticsTracker.track()` wraps all
  Firebase calls in `try/catch`; exceptions are swallowed silently.

---

## Platform Coverage

| Platform | Implementation |
|----------|----------------|
| Android | `FirebaseAnalyticsTracker` (Firebase Analytics) |
| iOS | `NoOpAnalyticsTracker` (stub — extend when native Firebase wrapper added) |
| JVM Desktop | `NoOpAnalyticsTracker` |
| Tests | `NoOpAnalyticsTracker` |

---

## Governance Rules (Permanent)

1. All new user-facing SDK entry points MUST emit `*_requested` and `*_succeeded`/`*_failed` events.
2. No event MUST contain PII. Reviewers MUST reject events with personal data.
3. Firebase import MUST be confined to `androidApp`. Never allowed in shared modules.
4. `AnalyticsTracker` is the only interface callers may reference from shared code.
5. New analytics events MUST be added to `AnalyticsEvent.kt` in `aynvora-core`.
6. `NoOpAnalyticsTracker` remains the default for all non-Android contexts.
7. Update this document when new events or platforms are added.

---

## Measurement Gaps (Deferred)

- iOS Firebase Analytics (pending native wrapper or `expect/actual` integration)
- Repository-layer events (profile save/delete) — wiring deferred to feature screens
- Deep user journey funnels (pending screen navigation implementation)
