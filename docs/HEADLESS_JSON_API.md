# AYNVORA Headless / Raw JSON API Guide

AYNVORA provides a headless mode (Mode B) enabling applications to interact via canonical JSON events without pulling any Compose, Android, or desktop UI dependencies.

---

## 1. Dispatching Events via JSON

```kotlin
val sdk = Aynvora.create(
    AynvoraConfig(
        engineProviders = listOf(AstroEngineProvider())
    )
)

val requestJson = """
{
    "name": "Aryabhata",
    "dateOfBirth": "1992-04-14",
    "timeOfBirth": "06:15:00",
    "location": {
        "latitude": 28.6139,
        "longitude": 77.2090,
        "timezone": 5.5,
        "city": "New Delhi",
        "country": "India"
    }
}
""".trimIndent()

val response = sdk.dispatch(
    AynvoraEventRequest(
        featureId = AynvoraFeatureId.ASTROLOGY,
        eventType = "CALCULATE_CHART",
        payloadJson = requestJson
    )
)

if (response.status == AynvoraStatus.SUCCESS) {
    println("Result JSON: ${response.resultJson}")
} else {
    println("Failure: ${response.status} - ${response.error?.message}")
}
```

---

## 2. Response Status Values

- `SUCCESS`: Event handled and calculation succeeded.
- `FEATURE_NOT_INCLUDED`: The engine module was not provided to `AynvoraConfig` / is not on the classpath.
- `FEATURE_DISABLED`: Feature included in binary, but disabled via user settings or subscription flags.
- `FEATURE_LOCKED`: Feature requires higher entitlement tier.
- `FEATURE_NOT_READY`: Feature implementation under development.
- `INVALID_REQUEST`: Payload missing required fields or malformed JSON.
- `ENGINE_ERROR`: Internal computation error within the feature engine.
