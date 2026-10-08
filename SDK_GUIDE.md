# AYNVORA SDK Developer Guide

## Overview

The `aynvora-sdk` module is the public boundary for all applications interacting with the platform.
It supports two primary consumption paradigms:

1. **Mode A — Strongly Typed APIs**: For Kotlin Multiplatform and Android consumers wanting typesafe models.
2. **Mode B — Raw Event / JSON API**: For headless, cross-platform bridges (React Native, Flutter, WebAssembly, CLI, REST server).

## Initializing the SDK

```kotlin
val sdk = Aynvora.create(
    AynvoraConfig(
        settings = AynvoraFeatureSettings.DEFAULT,
        analyticsTracker = CustomAnalyticsTracker(),
    )
)
```

---

## Mode A: Typed API Example

```kotlin
// Astrology calculation
val astroRequest = AstrologyRequest(
    name = "Aryabhata",
    dateOfBirth = "1990-05-15",
    timeOfBirth = "14:30:00",
    location = AstrologyLocation(
        city = "New Delhi",
        latitude = 28.6139,
        longitude = 77.2090,
        timezone = 5.5,
    ),
)
val astroResult: AynvoraResult<AstrologyResult> = sdk.astrology.calculate(astroRequest)

// Numerology calculation
val numRequest = NumerologyRequest(
    birthDay = 15,
    birthMonth = 5,
    birthYear = 1990,
    fullName = "Alexander",
)
val numResult = sdk.numerology.calculate(numRequest)

// Tarot Draw
val tarotRequest = TarotRequest(spreadId = "three_card_timeline")
val tarotResult = sdk.tarot.draw(tarotRequest)
```

---

## Mode B: Raw Event / JSON API Example

```kotlin
val rawRequest = AynvoraEventRequest(
    featureId = AynvoraFeatureId.ASTROLOGY,
    eventType = "CALCULATE_CHART",
    payloadJson = """
        {
            "name": "Headless User",
            "dateOfBirth": "1995-10-25",
            "timeOfBirth": "08:15:00",
            "location": {
                "latitude": 19.0760,
                "longitude": 72.8777,
                "timezone": 5.5
            }
        }
    """.trimIndent(),
    requestId = "req_1001"
)

val response: AynvoraEventResponse = sdk.dispatch(rawRequest)

if (response.isSuccess) {
    println("Calculated JSON: ${response.resultJson}")
} else {
    println("Error: ${response.status} - ${response.error?.details}")
}
```

---

## Dynamic Feature Gating

```kotlin
// Dynamically disable Palmistry without deleting user data:
val updatedSettings = sdk.getSettings().withFeatureState(
    AynvoraFeatureId.PALMISTRY,
    FeatureAccessState.DISABLED
)
sdk.updateSettings(updatedSettings)

// Subsequent calls will return AynvoraStatus.FEATURE_DISABLED
```
