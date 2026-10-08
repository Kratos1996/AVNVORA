# Custom UI & Headless SDK Integration Guide

AYNVORA SDK can be embedded into third-party apps without using the official Compose presentation layer (`:ui`).

## Option A: Full Official UI Integration
Used when the host application wants the full, out-of-the-box Compose Multiplatform experience:

```kotlin
// In host app (e.g., androidApp or desktopApp)
dependencies {
    implementation(project(":aynvora-sdk"))
    implementation(project(":ui"))
}

// In Compose view:
AynvoraApp()
```

---

## Option B: Headless SDK Integration (Custom UI / Cross-Platform)
Used when building a custom user interface in:
- Native Android Views (XML / Jetpack Compose with custom design system)
- Native iOS SwiftUI
- Flutter via platform channels
- React Native via native bridge
- Web / KMP Wasm / Desktop CLI

```kotlin
// Only depend on the SDK
dependencies {
    implementation(project(":aynvora-sdk"))
    // NO dependency on :ui or :design-system
}

class CustomAppPresenter(val sdk: Aynvora = Aynvora.create()) {
    
    suspend fun onUserCalculated(birthDate: String, birthTime: String, lat: Double, lon: Double) {
        val request = AynvoraEventRequest(
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "CALCULATE_CHART",
            payloadJson = """
                {
                    "dateOfBirth": "$birthDate",
                    "timeOfBirth": "$birthTime",
                    "location": { "latitude": $lat, "longitude": $lon }
                }
            """.trimIndent()
        )
        
        val response = sdk.dispatch(request)
        if (response.isSuccess) {
            // Render custom charts using response.resultJson
            myCustomRenderer.render(response.resultJson)
        }
    }
}
```
