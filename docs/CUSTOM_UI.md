# AYNVORA Custom UI Integration Guide

The official AYNVORA UI module (`:ui`) is completely optional. External developers are free to implement their own custom user interfaces in SwiftUI, Jetpack Compose, Flutter, React Native, or WebAssembly while consuming the headless SDK.

---

## 1. Principles of Custom UI Integration

1. **Direct Event Consumption**:
   The custom UI layer communicates with `Aynvora` via either typed clients or the raw JSON dispatch interface.

2. **No Direct Engine Imports**:
   Custom UI never directly references engine classes (`AstroFeatureEngine`, `PalmFeatureEngine`, etc.). This guarantees that engine refactoring or model upgrades never break the UI presentation.

3. **Presentation-Side Localization**:
   Engines return technical identifiers and enums (e.g., `ARIES`, `RIGHT_HAND`, `HEART_LINE`). Custom UIs convert these tokens into human language using their platform-native string catalogs.

---

## 2. Example: Flutter or Custom Compose ViewModel

```kotlin
class CustomAstrologyViewModel(private val aynvora: Aynvora) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    suspend fun onCalculateClicked(name: String, dob: String, tob: String, lat: Double, lon: Double) {
        _uiState.value = UiState.Loading
        val request = AstrologyRequest(
            name = name,
            dateOfBirth = dob,
            timeOfBirth = tob,
            location = AstrologyLocation(latitude = lat, longitude = lon)
        )
        when (val res = aynvora.astrology.calculate(request)) {
            is AynvoraResult.Success -> {
                _uiState.value = UiState.Success(
                    lagna = res.value.lagnaSign,
                    planets = res.value.planets.map { it.name to it.sign }
                )
            }
            is AynvoraResult.Error -> {
                _uiState.value = UiState.Error(res.error.message)
            }
        }
    }
}
```
