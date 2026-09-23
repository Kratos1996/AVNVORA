# AYNVORA Accessibility
Version: 1.1

## Requirements
- minimum 48x48dp interactive target
- dynamic/system font scaling
- semantic labels
- visible keyboard focus
- non-color-only status/selection
- contrast
- reduced motion
- screen-reader compatible content
- accessible descriptions for complex astrology charts

## Rule
Accessibility is part of the definition of done, not a later polish phase.

## Localized Accessibility Strings (Phase 4.5)

All accessibility strings are localized via `TranslationKey.Accessibility` and the
`AynvoraTranslator`. Never hardcode accessibility strings in English only.

| TranslationKey | English | Hindi |
|---|---|---|
| `Accessibility.LanguageSelectorLabel` | "Language selector" | "भाषा चयनकर्ता" |
| `Accessibility.SelectedLanguage` | "{language} selected" | "{language} चयनित" |
| `Accessibility.LoadingIndicator` | "Loading, please wait" | "लोड हो रहा है, कृपया प्रतीक्षा करें" |
| `Accessibility.ErrorMessage` | "Error: {message}" | "त्रुटि: {message}" |
| `Accessibility.RetrogradeIndicator` | "Retrograde motion" | "वक्री गति" |

Usage pattern:
```kotlin
val translator = LocalAynvoraTranslator.current
Modifier.semantics {
    contentDescription = translator.translate(TranslationKey.Accessibility.LanguageSelectorLabel)
}
```

## Language Selector Accessibility

`AynvoraLanguageSelectorItem` sets:
- `contentDescription` = native name + English name + selected state
- `role = Role.Button` (TalkBack/VoiceOver announces as button)
- `selected = isSelected` (screen readers announce selected state)
- Minimum 48dp height (WCAG 2.1 touch target minimum)
