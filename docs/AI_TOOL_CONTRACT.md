# AI TOOL CONTRACT & ROUTER SPECIFICATION

## 1. Core Principle

The on-device SLM / AI Assistant is strictly decoupled from storage, calculation engines, and
personal records.
AI interacts with AYNVORA features solely through the typed `AiTool` interface.

```kotlin
interface AiTool {
    val name: String
    val description: String
    val parametersJsonSchema: String
    suspend fun execute(argumentsJson: String): AynvoraResult<AiToolResult>
}
```

---

## 2. Tool Result & Provenance

Every tool execution must return structured, validated output accompanied by an immutable
`AiProvenance` descriptor:

```kotlin
data class AiToolResult(
    val callId: String,
    val toolName: String,
    val resultJson: String,
    val provenance: AiProvenance,
    val isSuccess: Boolean = true,
)

data class AiProvenance(
    val sourceDomain: String,                 // e.g. "ASTROLOGY", "GITA", "TAROT"
    val calculationRulesetOrEdition: String, // e.g. "PARASHARA_CLASSICAL_V1", "VEDANTA_STANDARD"
    val verifiedTimestampEpochMs: Long,
    val isRetrievedFact: Boolean = true,
)
```

---

## 3. Currently Registered Tools

| Tool Name                   | Domain         | Description                                                      | Parameter Schema                                                  | Status            |
|:----------------------------|:---------------|:-----------------------------------------------------------------|:------------------------------------------------------------------|:------------------|
| `calculateBirthChart`       | `ASTROLOGY`    | Deterministic birth chart calculations via `AynvoraSdk`          | `year`, `month`, `day`, `hour`, `minute`, `latitude`, `longitude` | **IMPLEMENTED**   |
| `searchGita`                | `GITA`         | Authentic verse and Sanskrit text retrieval via `GitaRepository` | `chapter`, `verse`, `language`                                    | **IMPLEMENTED**   |
| `drawTarotCard`             | `TAROT`        | Single or spread card draws via `PerformTarotReadingUseCase`     | `spreadId`, `allowReversed`                                       | **IMPLEMENTED**   |
| `buildWallpaperPrompt`      | `WALLPAPER`    | Sacred art prompt builder via `WallpaperPromptBuilder`           | `rashiIndex`, `nakshatraIndex`, `theme`, `deviceProfile`          | **IMPLEMENTED**   |
| `evaluateGemstoneInventory` | `GEMSTONE`     | Verified user inventory tracking (Foundation)                    | `stoneType`, `userContext`                                        | *FOUNDATION_ONLY* |
| `analyzePalmLines`          | `PALMISTRY`    | Line detection and mount findings (Foundation)                   | `handType`, `imageGuidance`                                       | *FOUNDATION_ONLY* |
| `retrieveLalKitabRule`      | `LAL_KITAB`    | Folk house rules and remedies (Foundation)                       | `planet`, `house`                                                 | *FOUNDATION_ONLY* |
| `retrieveGarudaChapter`     | `GARUDA_PURAN` | Transition and funeral wisdom (Foundation)                       | `chapterNumber`                                                   | *FOUNDATION_ONLY* |

---

## 4. Architectural Invariants

1. **AI Cannot Hallucinate Missing Data**: If a tool returns an error or no match, the AI must
   report the data as unavailable.
2. **AI Cannot Bypass Permissions**: Sensitive user context (birth data, palm images, personal
   reflections) cannot be passed to unauthorized tools.
3. **No Direct Room DAO Access**: Tools invoke public domain SDK facades or domain use cases, never
   Room DAOs or internal database entities.
