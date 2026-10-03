# AYNVORA UI Backend Contract (Phase 10.31)

**Audit Date:** October 3, 2026  
**Auditor:** Antigravity Autonomous Systems Engineering & Compliance  
**Standard:** Strict Zero-Fabrication, Source-First Provenance, UI-Ready Headless SDK Contract  

---

## 1. Architectural Invariant: Zero UI Calculation

The user interface layer (Compose Multiplatform / Native UI) is strictly presentation-only. Under no circumstances may a UI component or ViewModel calculate astronomical positions, house cusps, Vimshottari fractional periods, or astrological relationships. All data is requested from and emitted by the headless `AynvoraSdk` or restored from a cached `KundaliSnapshot`.

---

## 2. Screen-by-Screen Data Source Contract

| UI Screen | Backend Source Contract | Emitted Data Model / Contract | State Representation | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **AstrologyHome** | `SavedChartRepository` via `AynvoraSdk.getSnapshot()` | `SavedKundaliSnapshot`, `List<SavedChart>` | `UiDataState<List<SavedChart>>` | Displays profile list sorted by last-open; loads snapshot JSON without re-computing |
| **KundaliChartScreen** | `AstroChartBuilder.fromSnapshot()` | `AstroChart`, `ChartRenderModel` | `UiDataState<ChartRenderModel>` | House-owned contract; `PlanetLayoutEngine` detects close conjunction collision hints |
| **DivisionalChartsScreen** | `AynvoraSdk.calculateDivisionalChart()` | `DivisionalChartResult`, `AstroChart` | `UiDataState<AstroChart>` | Renders D1 through D60; 12 houses strictly owned |
| **DashaScreen** | `AynvoraSdk.calculateDasha()` | `VimshottariDashaTimeline` | `UiDataState<VimshottariDashaTimeline>` | 3-tier Mahadasha, Antardasha, Pratyantardasha timeline |
| **VarshaphalScreen** | `AynvoraSdk.calculateVarshaphal()` | `VarshaphalResult` | `UiDataState<VarshaphalResult>` | Annual solar return chart, Muntha, Varsheshwara, Sahams, Mudda Dasha |
| **KPAnalysisScreen** | `AynvoraSdk.calculateFeatures(setOf("astro_kp_249"))` | `KPResult`, `KPSubdivision` | `UiDataState<KPResult>` | 249 Sub-division table, Placidus cusps, Level A-D 4-fold significators |
| **JaiminiScreen** | `AynvoraSdk.calculateJaimini()` | `JaiminiResult`, `JaiminiCharaDashaResult` | `UiDataState<JaiminiResult>` | 7 Chara Karakas, Arudhas (AL, UL), Karakamsha, Rashi Aspects, Chara Dasha |
| **PrashnaScreen** | `PrashnaEngine.evaluateCore()` | `PrashnaCoreResult` | `UiDataState<PrashnaCoreResult>` | Intent classification, house significations, KP 1-249 seed resolution |
| **MuhurtaScreen** | `AynvoraSdk.calculateMuhurta()` | `MuhurtaResult` | `UiDataState<MuhurtaResult>` | Chaldean Horas, Day/Night Choghadiyas, Rahu Kalam, Yamaganda |
| **CompatibilityScreen** | `AynvoraSdk.calculateCompatibility()` | `CompatibilityResult` | `UiDataState<CompatibilityResult>` | Ashtakoota 36 Gunas, South Indian 10-Poruthams |
| **YogaDoshaScreen** | `AynvoraSdk.calculateYoga()`, `calculateDosha()` | `YogaDoshaResult` | `UiDataState<YogaDoshaResult>` | Classical BPHS yogas, Dosha identification, and Bhanga cancellation |
| **UpagrahaScreen** | `AynvoraSdk.calculateUpagraha()` | `UpagrahaResult` | `UiDataState<UpagrahaResult>` | Aprakash Grahas (Dhuma, Vyatipata, Parivesha, Indrachapa, Upaketu) |
| **LocationPickerModal** | `OfflineLocationCatalog` | `CanonicalCountry`, `CanonicalState`, `CanonicalLocation` | `UiDataState<List<CanonicalLocation>>` | 100% offline gazetteer; retains lat, lng, timezone, unique IDs |
| **AiChatScreen** | `AynvoraAiToolExecutor` + `PromptBuilder` | `PageContext` + `AstroToolRegistry` + `EvidenceBundle` | `UiDataState<AynvoraAiResponse>` | Grounded AI reflection citing deterministic engine facts |
| **ReportViewerScreen** | `ReportEngine.generate()` | `ReportDocument` | `UiDataState<ReportDocument>` | Renders PDF/JSON structured report without recalculating astrology |
| **ResearchOnlyDisclaimer**| `FeatureCapabilityRegistry` | `FeatureCapabilitySpecification`, `VastuCapability` | `UiDataState.ResearchOnly` | Explains classical research boundaries for Lal Kitab and Vastu |

---

## 3. Canonical UI State Lifecycle Contract

All future UI ViewModels implement `UiDataState<T>`:
```kotlin
sealed interface UiDataState<out T> {
    data object Loading : UiDataState<Nothing>
    data class Content<out T>(val data: T) : UiDataState<T>
    data object Empty : UiDataState<Nothing>
    data class Error(val error: AynvoraStructuredError) : UiDataState<Nothing>
    data class Unsupported(val featureId: String, val message: String) : UiDataState<Nothing>
    data class ResearchOnly(val featureId: String, val disclaimer: String) : UiDataState<Nothing>
    data class Offline<out T>(val cachedData: T? = null, val message: String) : UiDataState<T>
}
```

---

## 4. UI-Facing Structured Error Mapping

Errors emitted to the UI use `AynvoraErrorCode`:
- `INVALID_BIRTH_DATA`: Invalid date/time format or nonexistent calendar instant.
- `INVALID_LOCATION`: Coordinates out of range or unresolvable city.
- `TIMEZONE_UNAVAILABLE`: Timezone identifier missing or unrecognized.
- `CALCULATION_UNSUPPORTED`: Engine capability unavailable for requested profile.
- `TRADITION_UNSUPPORTED`: Requested feature not available in selected tradition.
- `SOURCE_UNVERIFIED`: Classical citation unverified or missing provenance.
- `RESEARCH_ONLY`: Feature restricted to academic/research study (Lal Kitab, Vastu).
- `MODEL_UNAVAILABLE`: Local SLM weights not loaded on device.
- `PERSISTENCE_ERROR`: Local SQLite or JSON cache read/write failure.
- `MIGRATION_ERROR`: Stored schema version upgrade failure.
- `RESOURCE_CONSTRAINT`: Insufficient memory/threads for requested operation.
