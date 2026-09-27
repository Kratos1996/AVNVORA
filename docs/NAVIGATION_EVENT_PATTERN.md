# AYNVORA Navigation Event Pattern

Version: 1.0 (Phase 9.0)

## 1. Zero Direct UI Route Strings

Direct UI calls such as `navigator.navigate("tarot")` or raw route strings are prohibited.
All navigation requests travel through the event and effect pipeline:

```
UI Click
  ↓
AynvoraClickEvent(eventId = "dashboard.tarot.open_clicked")
  ↓
ViewModel.onEvent(event)
  ↓
private handleEvent()
  ↓
emitEffect(AynvoraEffect.Navigate(AynvoraNavigationTarget.TarotHome))
  ↓
NavHost / Root Composable collects Effect
  ↓
Renders Target Screen
```

## 2. Typed Navigation Targets

Navigation destinations are strongly typed in `AynvoraNavigationTarget`:

- `TarotHome`
- `PalmistryHome`
- `FeatureDetail(featureId: CoreFeatureId)`
- `TarotReading(readingId: String)`
- `PalmistryResult(sessionId: String)`
- `AiSettings`
- `Back`
- `Close`

Arguments travel inside typed data classes rather than raw string maps or unstructured JSON.
