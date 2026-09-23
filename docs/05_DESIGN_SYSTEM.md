# AYNVORA Design System
Version: 1.1

## Source of Truth
The Master UI Style Guide and machine-readable tokens are the source of truth for product UI.

## Token Families
- Color
- Typography
- Spacing
- Shape
- Border/divider
- Elevation
- Motion
- Iconography

## KMP API Direction
AynvoraTheme
AynvoraColors
AynvoraTypography
AynvoraSpacing
AynvoraShapes
AynvoraMotion
AynvoraIcons

## Machine-Readable Goal
Tokens should eventually exist in version-controlled structured files so design and code consume the same values.

## Rule
Hard-coded visual values are prohibited when an approved token exists.

## Localization Components (Phase 4.5)

Added to `com.aynvora.designsystem.localization`:

### Composition Locals (LocalLocale.kt)
- `LocalAynvoraLocale` — current `SupportedLocale`; changes trigger recomposition
- `LocalAynvoraTranslator` — `AynvoraTranslator` for the current locale
- `LocalAynvoraIsRtl` — `Boolean`; `true` if current locale is right-to-left

### AynvoraLocalizationProvider
```kotlin
@Composable
fun AynvoraLocalizationProvider(
    localeManager: AynvoraLocaleManager,
    useReducedMotion: Boolean = false,
    content: @Composable () -> Unit,
)
```
- Place once at application root, inside `AynvoraTheme`
- Crossfades content on locale change (`AynvoraMotion.durationDefault` = 200ms)
- `useReducedMotion = true` skips animation

### AynvoraLanguageSelectorItem
```kotlin
@Composable
fun AynvoraLanguageSelectorItem(
    locale: SupportedLocale,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
)
```
- Minimum 48dp height (WCAG 2.1)
- Gold border + checkmark when selected
- Accessibility semantics: contentDescription, role=Button, selected
- RTL-aware Row layout

### AynvoraLanguageSelector
```kotlin
@Composable
fun AynvoraLanguageSelector(
    availableLocales: List<SupportedLocale>,
    selectedLocale: SupportedLocale,
    onLocaleSelected: (SupportedLocale) -> Unit,
    modifier: Modifier = Modifier,
)
```
- Renders all available locales as a vertical picker
- Use `LanguageRegistry.availableLocales()` as the source
