# AYNVORA Feature Access & Entitlements Guide

This guide describes how feature access states are categorized, verified, and controlled at runtime.

---

## 1. Feature Access State Taxonomy

| FeatureAccessState | Meaning | Behavior |
|---|---|---|
| `NOT_INCLUDED` | The engine module was not provided at SDK initialization or is not packaged in the app binary. | Dispatch blocked immediately. Returns `AynvoraStatus.FEATURE_NOT_INCLUDED`. |
| `DISABLED` | The feature module is physically included, but the user or admin disabled it in settings. | Blocked. Returns `AynvoraStatus.FEATURE_DISABLED`. Data is preserved; no native/model memory is loaded. |
| `LOCKED` | The feature requires a paid subscription or higher entitlement tier. | Blocked. Returns `AynvoraStatus.FEATURE_LOCKED`. |
| `NOT_READY` | Feature is under active implementation. | Blocked. Returns `AynvoraStatus.FEATURE_NOT_READY`. |
| `RESEARCH_ONLY` | Experimental/research mode only. | Blocked in production configurations. |
| `UNSUPPORTED` | Not supported on the current target platform. | Blocked. Returns `AynvoraStatus.FEATURE_UNSUPPORTED`. |
| `AVAILABLE` / `VERIFIED` / `PRODUCTION` | Fully operational. | Execution proceeds to engine. |

---

## 2. Dynamic Settings Updating

```kotlin
val currentSettings = sdk.getSettings()

// Disable Palmistry dynamically without uninstalling modules
val updated = currentSettings.copy(
    palmistry = FeatureAccessState.DISABLED
)

sdk.updateSettings(updated)

// Immediate effect:
// sdk.palmistry.analyze(...) -> AynvoraResult.Error(FEATURE_DISABLED)
```
