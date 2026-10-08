# AYNVORA Typed API Reference

The AYNVORA Typed API offers idiomatic Kotlin interfaces with strong typing, auto-completion, and compile-time correctness, while internally utilizing the canonical event router pipeline.

---

## 1. Initializing the SDK with Typed Clients

```kotlin
val sdk = Aynvora.create(
    AynvoraConfig(
        engineProviders = listOf(
            AstroEngineProvider(),
            PalmEngineProvider(),
            AiEngineProvider(),
            ReportEngineProvider()
        )
    )
)
```

---

## 2. Available Typed Clients

| Client Property | Feature | Primary Method | Request Type | Result Type |
| :--- | :--- | :--- | :--- | :--- |
| `sdk.astrology` | Vedic Astrology | `calculate(request)` | `AstrologyRequest` | `AstrologyResult` |
| `sdk.palmistry` | Palmistry | `analyze(request)` | `PalmistryRequest` | `PalmistryResult` |
| `sdk.ai` | AI Grounding | `ask(request)` | `AiGroundingRequest` | `AiGroundingResult` |
| `sdk.report` | Report Synthesis| `generate(request)` | `ReportGenerateRequest`| `ReportResult` |

---

## 3. Unified Error Handling

All typed clients return `AynvoraResult<T>`:

```kotlin
val result = sdk.astrology.calculate(request)

when (result) {
    is AynvoraResult.Success -> {
        val chart = result.value
        println("Lagna: ${chart.lagnaSign}")
    }
    is AynvoraResult.Error -> {
        println("Status: ${result.error.status}")
        println("Message: ${result.error.message}")
    }
}
```

If the underlying engine was not provided in `AynvoraConfig`, `result.error.status` will be `AynvoraStatus.FEATURE_NOT_INCLUDED`.
