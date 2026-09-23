# AYNVORA API Contracts
Version: 1.0

## Public API
Public APIs must be small, documented and stable.

## Rules
- Prefer immutable request/result models.
- Use explicit versioning for breaking changes.
- Do not expose internal implementation types unnecessarily.
- Validate inputs at API boundaries.
- Define error/result states rather than throwing uncontrolled exceptions for expected domain conditions.

## SDK Entry Point (`aynvora-core`)

The public SDK boundary is formalized in `:aynvora-core`. Consumers (UI, application layer, external clients) must never interact directly with internal modules such as `:astro-engine`.

### Entry Point Contract
```kotlin
val sdk = Aynvora.create()

val result: AynvoraResult<ChartResult> = sdk.calculateChart(
    BirthData(
        date = BirthDate(year = 1995, month = 5, day = 21),
        time = BirthTime(hour = 14, minute = 30, second = 0),
        place = BirthPlace(
            name = "Varanasi, India",
            coordinates = Coordinates(latitude = 25.3176, longitude = 82.9739),
            timezoneId = "Asia/Kolkata"
        )
    )
)
```

### Capability Discovery
```kotlin
val metadata: EngineMetadata = sdk.getMetadata()
```

### Deterministic Result Contract
All operations return `AynvoraResult<T>`:
- `AynvoraResult.Success(value, metadata)`
- `AynvoraResult.Failure`:
  - `InvalidInput(field, message)`
  - `UnsupportedConfiguration(message)`
  - `CalculationFailure(code, message)`
  - `InternalFailure(message)`

No unhandled exceptions are thrown across the public SDK boundary for expected domain conditions.

