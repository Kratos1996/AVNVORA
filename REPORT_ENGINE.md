# AYNVORA Report Engine Architecture

## Architectural Isolation
The Report Engine (`engines/report-engine/`) is completely separated from feature engines:
- It **does not calculate** astrology charts, palm landmarks, tarot spreads, or numerology cycles.
- It receives pre-calculated feature JSON outputs and aggregates them into structured, beautiful reports (PDF / HTML / exportable formats).

## Generation Flow

```mermaid
sequenceDiagram
    participant App as External App / UI
    participant SDK as Aynvora SDK
    participant Astro as Astro Engine
    participant Report as Report Engine

    App->>SDK: sdk.astrology.calculate(request)
    SDK->>Astro: CALCULATE_CHART event
    Astro-->>SDK: Astrology Result JSON
    
    App->>SDK: sdk.report.generate(ReportGenerateRequest(featureResults = {astrology: JSON}))
    SDK->>Report: GENERATE_REPORT event
    Report-->>SDK: Report Result (PDF bytes / artifact reference)
    SDK-->>App: AynvoraResult<ReportResult>
```

## Report Request Contract

```kotlin
@Serializable
data class ReportGenerateRequest(
    val reportType: String, // e.g. "KUNDALI_COMPREHENSIVE", "PALMISTRY_INSIGHT"
    val subjectName: String,
    val featureResults: Map<String, String> = emptyMap(), // Keyed by feature token
    val requestedSections: List<String> = emptyList(),
    val languageCode: String = "en",
)
```
