# Phase 4.5 — Runtime Localization + Multilingual Foundation
Version: 1.0  
Status: COMPLETE

---

## 1. Localization Architecture

```
:androidApp / :desktopApp / :iosApp
        ↓
    :ui (Compose screens)
        ↓
:design-system  (AynvoraLocalizationProvider, AynvoraLanguageSelector, CompositionLocals)
        ↓
:aynvora-localization  (SupportedLocale, LanguageRegistry, AynvoraLocaleManager, AynvoraTranslator, LocaleFormatter)
        ↓
:aynvora-core  (UserPreferencesRepository contract, Rashi/Nakshatra/CelestialBody enums)
        ↓
:aynvora-data  (storage implementation — NOT directly imported by :aynvora-localization)
```

**Module independence guarantee:**
- `:aynvora-localization` does NOT depend on `:astro-engine`
- `:aynvora-localization` does NOT depend on `:aynvora-data` (only the `:aynvora-core` repository contract)
- `:aynvora-localization` does NOT contain any Compose code (pure logic)
- `:astro-engine` has zero knowledge of localization

---

## 2. :aynvora-localization Module

**Package:** `com.aynvora.localization`  
**Build:** KMP (Android, JVM, iosArm64, iosSimulatorArm64)  
**Dependencies:** `:aynvora-core`, `kotlinx-coroutines-core`, `kotlinx-serialization-json`  
**No Compose dependency** — pure logic, fully testable without UI

### Sub-packages
| Package | Contents |
|---|---|
| `locale` | SupportedLocale, TextDirection, LanguageRegistry, AynvoraLocaleManager, AynvoraLocaleManagerImpl |
| `translation` | TranslationKey, TranslationTable, EnglishTranslations, HindiTranslations, AynvoraTranslator |
| `format` | LocaleFormatter |

---

## 3. Locale Model

```kotlin
data class SupportedLocale(
    val localeId: String,          // "en", "hi" — persistence key
    val languageTag: String,       // "en-IN", "hi-IN" — BCP-47 tag for platform APIs
    val nativeName: String,        // "English", "हिन्दी"
    val englishName: String,       // "English", "Hindi"
    val direction: TextDirection,  // LTR | RTL
    val isSupported: Boolean,
    val fallbackLocaleId: String?,
)
```

**Canonical locale IDs:**

| ID | Language Tag | Native Name | Direction |
|---|---|---|---|
| `en` | `en-IN` | English | LTR |
| `hi` | `hi-IN` | हिन्दी | LTR |

Future IDs (not yet active): `sa`, `ta`, `te`, `ml`, `bn`, `ar` (RTL)

---

## 4. Language Registry

`LanguageRegistry` is the single source of truth for all supported locales.

- `availableLocales()` → filtered list of supported locales
- `defaultLocale()` → English (`en`)
- `getLocale(id)` → nullable lookup
- `getLocaleOrDefault(id)` → falls back to English
- `isSupported(id)` → boolean
- `resolveFallback(locale)` → follows `fallbackLocaleId` chain

**Adding a new language requires only adding an entry to `LanguageRegistry` and a translations file. No other module changes are required.**

---

## 5. Locale Manager

```kotlin
interface AynvoraLocaleManager {
    val currentLocale: StateFlow<SupportedLocale>   // immutable; triggers recomposition
    suspend fun setLocale(locale: SupportedLocale)
    suspend fun setLocale(localeId: String)
    suspend fun resetToDefault()
}
```

`AynvoraLocaleManagerImpl`:
- `MutableStateFlow` backed; exposes only `StateFlow` (immutable externally)
- `Mutex`-protected writes (thread-safe)
- Calls `UserPreferencesRepository.updatePreferences()` to persist locale
- Persistence failure is non-fatal — in-memory state is authoritative

---

## 6. Runtime Switching

Language switching is **completely restart-free**:

```
User selects Hindi
    ↓
localeManager.setLocale("hi")     — suspend, ~1ms
    ↓
_currentLocale.value = hindiLocale  — MutableStateFlow emit
    ↓
Compose collectAsState() detects change
    ↓
AynvoraLocalizationProvider recomposes
    ↓
AnimatedContent crossfades (200ms)
    ↓
All child composables recompose with new CompositionLocals
    ↓
Hindi text appears — No restart, no force-close, no nav reset
```

---

## 7. Persistence

The selected locale is persisted via **`UserPreferences.languageCode`** (already existed as `String = "en"`).

Architecture:
```
AynvoraLocaleManagerImpl
    ↓  updatePreferences(prefs.copy(languageCode = "hi"))
UserPreferencesRepository (contract in :aynvora-core)
    ↓
UserPreferencesRepositoryImpl (in :aynvora-data)
    ↓
AynvoraStorageEngine (JSON file storage)
```

No new database. No new preference store. No platform-specific storage.

---

## 8. Startup Restoration

`AynvoraLocaleManagerImpl.initialize()` is called once at application startup:

1. `getPreferences()` → read `languageCode`
2. `LanguageRegistry.isSupported(localeId)` → validate
3. If supported → `_currentLocale.value = locale`
4. If unsupported or blank → `_currentLocale.value = defaultLocale()`
5. If IO failure → `_currentLocale.value = defaultLocale()` (never crashes)

---

## 9. Translation Resources

All translations are **bundled at compile time** — no network, no remote service, no API key.

| File | Locale | Coverage |
|---|---|---|
| `EnglishTranslations.kt` | `en` | 100% — baseline; all fallbacks chain to this |
| `HindiTranslations.kt` | `hi` | 100% — all 12 Rashis, 27 Nakshatras, 9 bodies, UI strings |

---

## 10. Translation Fallback

4-level deterministic fallback:

```
Requested locale ("hi")
    ↓  HindiTranslations.get(key) → found? return it
Language fallback ("en" via fallbackLocaleId)
    ↓  EnglishTranslations.get(key) → found? return it
Default locale ("en")
    ↓  EnglishTranslations.get(key) → found? return it
Stable key (the key string itself — never blank)
    Production: returns the key unchanged
    Debug: returns "[MISSING: key]" for detectability
```

---

## 11. Variable/Plural Handling

**Variables:** `{variable_name}` placeholders in translation strings
```kotlin
translator.translateWithArgs(TranslationKey.Errors.InvalidInput, "field" to "month")
// en: "Invalid input: month"
// hi: "अमान्य इनपुट: month"
```

**Pluralization:** `_one` / `_other` key suffix convention
```kotlin
translator.plural(1, "chart.saved")  // → "1 chart saved"
translator.plural(5, "chart.saved")  // → "5 charts saved"
```

---

## 12. Astrology Terminology

**Rule (Critical):** The Astro Engine returns stable domain enum IDs. The localization layer converts them to display text. Never the reverse.

```
AstroEngine → Rashi.ARIES → TranslationKey.Astro.RashiName(Rashi.ARIES)
    → "Aries"    (English)
    → "मेष"      (Hindi)
```

**Coverage in initial catalog:**
- 12 Rashis — English (Latin) + Sanskrit names + Hindi (Devanagari)
- 27 Nakshatras — English + Hindi
- 9 Celestial bodies — English + Hindi
- Retrograde/Direct — English ("Retrograde", "Direct") + Hindi ("वक्री", "मार्गी")
- Ayanamsa labels, house system labels, calculation profile labels

---

## 13. Formatting

`LocaleFormatter` (in `:aynvora-localization/format/`):

| Method | Example output |
|---|---|
| `formatDegrees(23.857092)` | `"23° 51′ 25.5″"` |
| `formatLongitude(125.04)` | `"125.04°"` |
| `formatNumber(1234.56, en)` | `"1234.56"` |
| `formatNumber(1234.56, en, showThousands=true)` | `"1,234.56"` |
| `formatDate(2000, 1, 1, en)` | `"01/01/2000"` |
| `formatTime(14, 30, 0, en)` | `"14:30:00"` |
| `formatJulianDay(2451545.0)` | `"2451545.0000"` |

**Degree symbols used:** `°` (U+00B0), `′` (U+2032 prime), `″` (U+2033 double prime) — NOT typewriter `'` or `"`.

Internal engine precision is NEVER modified by formatting.

---

## 14. RTL

- `SupportedLocale.direction: TextDirection` (LTR | RTL)
- `SupportedLocale.isRtl: Boolean` computed property
- `LocalAynvoraIsRtl` CompositionLocal in `:design-system` reacts to locale change
- All current supported locales (English, Hindi) are LTR (Devanagari is LTR)
- RTL infrastructure is in place for Arabic/Hebrew future locales
- **Row layout uses Start/End, not Left/Right** — Compose handles RTL mirroring

---

## 15. Font Fallback

Strategy (documented; font loading is platform-specific):

| Script | Font | Usage |
|---|---|---|
| Latin | Cormorant Garamond | Display/heading text |
| Latin | Inter | UI/product text |
| Devanagari (Hindi, Sanskrit) | Noto Sans Devanagari | All Devanagari script content |
| Fallback | System default | All other scripts |

Compose `FontFamily` definitions in `:design-system/AynvoraTypography.kt` should specify font families in order: primary font → Noto Sans → system. This ensures Devanagari characters render correctly when English fonts don't contain those glyphs.

**Current status:** Typography definitions exist in `AynvoraTypography.kt`. Noto Sans Devanagari font loading (via `downloadable fonts` or bundled font file) is deferred pending font asset availability.

---

## 16. Accessibility

All translation keys include dedicated accessibility strings:

| Key | English | Hindi |
|---|---|---|
| `a11y.language_selector` | "Language selector" | "भाषा चयनकर्ता" |
| `a11y.selected_language` | "{language} selected" | "{language} चयनित" |
| `a11y.loading` | "Loading, please wait" | "लोड हो रहा है, कृपया प्रतीक्षा करें" |
| `a11y.error_message` | "Error: {message}" | "त्रुटि: {message}" |
| `a11y.retrograde` | "Retrograde motion" | "वक्री गति" |

`AynvoraLanguageSelectorItem` has:
- `semantics { contentDescription = ... }` with locale name + selected state
- `semantics { role = Role.Button }` for keyboard/TalkBack navigation
- `semantics { selected = isSelected }` for screen readers

---

## 17. Reduced Motion

`AynvoraLocalizationProvider` accepts `useReducedMotion: Boolean`:
- `useReducedMotion = false` (default): 200ms crossfade via `AnimatedContent`
- `useReducedMotion = true`: immediate content switch, zero animation

Application layer should pass this value from the platform's accessibility settings:
- Android: `LocalContext.current.getSystemService(AccessibilityManager::class.java).isEnabled`
- Desktop/iOS: platform-specific accessibility API

---

## 18. Offline Behavior

- All translations are bundled in `.kt` files compiled into the binary
- No network request is ever made for translations
- No remote translation service is used
- No API keys are stored
- Language switching works in airplane mode / zero connectivity

---

## 19. Security/Privacy

- Locale selection is NOT treated as sensitive
- Locale is stored in the existing user preferences storage (same security boundary)
- No locale data is sent to servers
- No analytics event is emitted for language selection
- No external translation API is called

---

## 20. Cross-Platform Implementation

| Capability | Platform |
|---|---|
| Locale model, registry, manager, translator, formatter | `commonMain` — shared across all platforms |
| Compose integration (provider, composition locals) | `commonMain` in `:design-system` (Compose Multiplatform) |
| Persistence | Via `UserPreferencesRepository` — platform drivers in `jvmMain`/`androidMain` |
| iOS KMP | Source sets compile (iosArm64, iosSimulatorArm64); runtime requires Xcode |

---

## 21. Initial Supported Languages

| # | Language | Locale ID | Script | Status |
|---|---|---|---|---|
| 1 | English | `en` | Latin | ✅ Fully translated |
| 2 | Hindi | `hi` | Devanagari | ✅ Fully translated |

---

## 22. Adding a New Language

1. Add a `SupportedLocale` entry to `LanguageRegistry`.
2. Create `XxxTranslations.kt` with a `TranslationTable`.
3. Register the table in `TranslationCatalog`.
4. Add a test verifying all keys are covered.
5. Verify font rendering for the script.
6. No changes to `:astro-engine`, `:aynvora-core`, or `:aynvora-data` required.

---

## 23. Testing

27 deterministic tests across 5 test files:

| File | Tests |
|---|---|
| `LocaleRegistryTest.kt` | 1–4: default locale, registry, lookup, fallback |
| `LocaleManagerTest.kt` | 5–11: switching, StateFlow, persistence, startup, unsupported fallback |
| `TranslationTest.kt` | 12–18: lookup, missing, variables, plurals, Rashi/Nakshatra/body |
| `FormatterTest.kt` | 19–21: date, number, degrees |
| `LocaleMetadataTest.kt` | 22–24: RTL metadata, direction |
| `IsolationTest.kt` | 25–27: language ≠ calculation, language ≠ BirthData, language ≠ result |

---

## 24. Files Added

| File | Module | Purpose |
|---|---|---|
| `SupportedLocale.kt` | `:aynvora-localization` | Locale value type |
| `LanguageRegistry.kt` | `:aynvora-localization` | Centralized registry |
| `AynvoraLocaleManager.kt` | `:aynvora-localization` | Interface |
| `AynvoraLocaleManagerImpl.kt` | `:aynvora-localization` | Implementation |
| `TranslationKey.kt` | `:aynvora-localization` | Typed key hierarchy |
| `TranslationTable.kt` | `:aynvora-localization` | Per-locale map |
| `EnglishTranslations.kt` | `:aynvora-localization` | English catalog |
| `HindiTranslations.kt` | `:aynvora-localization` | Hindi catalog |
| `AynvoraTranslator.kt` | `:aynvora-localization` | Fallback/format engine |
| `LocaleFormatter.kt` | `:aynvora-localization` | Display formatters |
| `LocalLocale.kt` | `:design-system` | CompositionLocals |
| `AynvoraLocalizationProvider.kt` | `:design-system` | Root Compose provider |
| `AynvoraLanguageSelector.kt` | `:design-system` | Reusable UI component |
| 5 test files | `:aynvora-localization` | 27 deterministic tests |

---

## 25. Files Changed

| File | Change |
|---|---|
| `settings.gradle.kts` | Added `:aynvora-localization` to include list |
| `gradle/libs.versions.toml` | Added `kotlinx-coroutines-test` |
| `design-system/build.gradle.kts` | Added `:aynvora-localization` and `compose.animation` |

---

## 26. Dependencies Added

| Dependency | Reason | Impact |
|---|---|---|
| `kotlinx-coroutines-test` (test only) | `runTest` for coroutine tests | Test-only; zero production binary impact |
| `compose.animation` | `AnimatedContent` crossfade | Already in Compose Multiplatform bundle |

No new production runtime libraries.

---

## 27. Dependencies Removed

None.

---

## 28. Build Verification

```bash
./gradlew :aynvora-localization:jvmTest   → see test verification section
./gradlew :aynvora-core:jvmTest           → PASSED (Phase 4 baseline)
./gradlew :aynvora-data:jvmTest           → PASSED (Phase 3 baseline)
./gradlew :astro-engine:jvmTest           → PASSED (Phase 4 baseline)
./gradlew :design-system:jvmTest          → see build verification section
./gradlew :androidApp:assembleDebug       → see build verification section
./gradlew :desktopApp:packageDistributionForCurrentOS → see build verification section
```

iOS: KMP source sets compile (iosArm64/iosSimulatorArm64). Runtime iOS testing requires Xcode.
Xcode availability was not verified at time of writing.

---

## 29. Known Limitations

1. **Font loading**: Noto Sans Devanagari is specified in the font fallback strategy but not yet bundled as a font asset. Compose will fall back to the system Devanagari font which may differ by device.
2. **iOS runtime testing**: Cannot be verified without Xcode.
3. **Dynamic font scaling**: Translation strings are designed to accommodate expansion but no automated layout overflow tests exist yet.
4. **Locale animation in iOS/Desktop**: `AnimatedContent` behavior may differ across platforms.

---

## 30. Deferred Translation Work

- Sanskrit (`sa`), Tamil (`ta`), Telugu (`te`), Malayalam (`ml`), Bengali (`bn`) — catalogs not yet created
- Arabic (`ar`) — RTL; requires testing of `LocalAynvoraIsRtl` propagation in full app
- Panchang terminology (Tithi, Karana, Yoga, Vara, Hora) — deferred to Phase 5
- Dasha terminology — deferred to Dasha phase
- House names (Bhava 1–12) — deferred to Phase 5

---

## 31. Next Phase

Phase 5: Reference Application Screens & Workflows

The localization infrastructure is now in place. Phase 5 screens should:
- Wrap root in `AynvoraLocalizationProvider`
- Use `LocalAynvoraTranslator.current` for all display text
- Use `LocaleFormatter` for all dates, times, degrees
- Use `LanguageRegistry.availableLocales()` + `AynvoraLanguageSelector` for language selection
- Never hardcode UI strings directly in composables
