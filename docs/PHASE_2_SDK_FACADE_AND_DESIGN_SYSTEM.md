# PHASE 2: PUBLIC SDK API FACADE + DESIGN SYSTEM INPUT/NAVIGATION FOUNDATION

## 1. Phase Objective
Formalize `:aynvora-core` as the stable, platform-independent public SDK API facade so UI and application layers do not depend directly on `:astro-engine`. At the same time, expand the `:design-system` module with reusable, accessible form input and navigation primitives required by future screens, adhering strictly to the Master UI Style Guide and design tokens without implementing persistence or premature astrology business logic.

---

## 2. New Module(s)
- **`:aynvora-core`**: The public SDK boundary module.
  - Multiplatform targets: Android (`androidLibrary`), JVM (`jvm`), iOS (`iosArm64`, `iosSimulatorArm64`).
  - Responsibility: SDK entry point factory (`Aynvora.create()`), public API interface (`AynvoraSdk`), immutable public request/response models, deterministic error hierarchy (`AynvoraResult`), and internal adapter (`AstroEngineAdapter`) to `:astro-engine`.

---

## 3. Public SDK API
The public entry point is defined in package `com.aynvora.core`:

```kotlin
interface AynvoraSdk {
    suspend fun calculateChart(request: ChartRequest): AynvoraResult<ChartResult>
    suspend fun calculateChart(
        birthData: BirthData,
        config: CalculationConfig = CalculationConfig(),
    ): AynvoraResult<ChartResult>
    fun getMetadata(): EngineMetadata
}

object Aynvora {
    fun create(): AynvoraSdk = DefaultAynvoraSdk()
}
```

---

## 4. Public Models
All public models are immutable, platform-independent, serializable Kotlin data classes residing in package `com.aynvora.core.models`:
- **`BirthDate`**: Validated calendar date (`year: Int [1..9999]`, `month: Int [1..12]`, `day: Int [1..31]`). Provides `toIsoDateString()`.
- **`BirthTime`**: Validated 24-hour time (`hour: Int [0..23]`, `minute: Int [0..59]`, `second: Int [0..59]`). Provides `toIsoTimeString()`.
- **`Coordinates`**: Geographic coordinates (`latitude: Double [-90.0..90.0]`, `longitude: Double [-180.0..180.0]`).
- **`BirthPlace`**: Location metadata (`name: String`, `coordinates: Coordinates`, `timezoneId: String`).
- **`BirthData`**: Aggregated birth specification (`date: BirthDate`, `time: BirthTime`, `place: BirthPlace`). Provides `toIsoDateTimeString()`.
- **`CalculationConfig`**: Calculation settings (`profile: CalculationProfile`, `ayanamsa: AyanamsaConvention`, `houseSystem: HouseSystem`, `customParameters: Map<String, String>`).
- **`ChartRequest`**: Public calculation request wrapping `BirthData` and `CalculationConfig`.
- **`ChartResult`**: Public calculated chart containing `engineVersion`, `calculationStatus`, `birthData`, and `config`.
- **`EngineMetadata`**: Engine capability discovery model (`engineVersion`, `buildNumber`, `isDeterministic`, `supportedDomains`).

---

## 5. Internal Adapter Architecture
`aynvora-core` encapsulates an internal adapter:
```
Public API (AynvoraSdk)
        ↓
Core Facade (DefaultAynvoraSdk)
        ↓
Internal Adapter (AstroEngineAdapter) [visibility: internal]
        ↓
Astro Engine (AynvoraAstroEngine in :astro-engine)
```
- `AstroEngineAdapter` translates public `com.aynvora.core.models.BirthData` into engine-internal `com.aynvora.astro.BirthData`.
- Isolates raw engine types (`CalculationResult`) and maps them into `ChartResult`.
- Catches internal engine exceptions and translates them into typed `AynvoraResult.Failure.InternalFailure`, preventing unhandled exceptions across the SDK boundary.
- Does not duplicate astrology calculation logic or formulas.

---

## 6. Dependency Direction
Enforced architecture rule:
```
UI / App Layer (:ui, :androidApp, :desktopApp, future iOS app)
        ↓
Public Facade (:aynvora-core)
        ↓
Astrology Engine (:astro-engine)
```
- `:aynvora-core` depends on `:astro-engine`.
- `:ui` depends on `:aynvora-core` and `:design-system`.
- `:ui` does NOT depend on `:astro-engine`.
- `:androidApp` and `:desktopApp` depend on `:ui`.
- `:astro-engine` has zero dependencies on `:aynvora-core`, `:ui`, or `:design-system`.

---

## 7. UI Dependency Changes
- **`ui/build.gradle.kts`**:
  - Removed: `implementation(project(":astro-engine"))`
  - Added: `implementation(project(":aynvora-core"))`
- Verification confirmed that no UI files import classes from `com.aynvora.astro`.

---

## 8. Error Model
Deterministic sealed hierarchy in `com.aynvora.core.result.AynvoraResult<out T>`:
- **`AynvoraResult.Success<out T>`**: Contains `value: T` and `metadata: EngineMetadata`.
- **`AynvoraResult.Failure`**:
  - **`InvalidInput`**: `field: String`, `message: String`. Raised for out-of-bounds dates, times, coordinates, or blank timezones.
  - **`UnsupportedConfiguration`**: `message: String`. Raised when an unsupported calculation profile or ayanamsa convention is requested.
  - **`CalculationFailure`**: `code: String`, `message: String`. Raised when the engine encounters deterministic calculation issues.
  - **`InternalFailure`**: `message: String`. Sanitized unexpected failure protecting sensitive details.
- Functional operators provided: `onSuccess {}`, `onFailure {}`, `getOrNull()`, `isSuccess`, `isFailure`.

---

## 9. Tests
- **`AynvoraSdkTest`** (`aynvora-core/src/commonTest`):
  1. `publicApiConstructsValidRequest`: Tests ISO conversion, coordinates, and timezone preservation.
  2. `invalidBirthDateOrCoordinatesThrowIllegalArgumentExceptionOnConstruction`: Tests boundary checks for month, hour, latitude, and timezone.
  3. `facadeDelegatesToEngineAndReturnsDecoupledResult`: Tests end-to-end execution of `Aynvora.create().calculateChart(...)`, verifying engine delegation, decoupled `ChartResult`, and metadata.
  4. `errorModelDistinguishesFailureCategories`: Tests distinct branches of `AynvoraResult.Failure`.
  5. `engineMetadataCanBeDiscoveredWithoutCalculation`: Tests capability inspection without running calculations.
- **`AynvoraComponentsTest`** (`design-system/src/commonTest`):
  - Tests rendering of `AynvoraFormField`, `AynvoraTextField`, `AynvoraDateField`, `AynvoraTimeField`, `AynvoraCheckbox`, `AynvoraSwitch`, `AynvoraTopAppBar`, `AynvoraNavigationBar`, `AynvoraNavigationRail`, and `AynvoraSectionHeader`.
- **`AynvoraThemeTest` & `AynvoraAdaptiveTest`**:
  - Regression tested tokens, color schemes, typography, and sdp/ssp scalers.

---

## 10. New Design-System Components
Implemented in `:design-system`:
1. **`AynvoraFormField`**: Form control wrapper with label, required asterisk (*), supporting text, and non-color-only error indicator.
2. **`AynvoraTextField`**: Clean text input with label, placeholder, leading/trailing icons, error state, enabled/read-only, min 48dp touch target, and design token styling.
3. **`AynvoraDateField`**: Date input with ISO formatting (`YYYY-MM-DD`), leading calendar icon, validation, and error states.
4. **`AynvoraTimeField`**: 24-hour time input (`HH:MM` or `HH:MM:SS`), leading time icon, validation, and error states.
5. **`AynvoraCheckbox`**: Accessible checkbox with minimum 48dp touch target, gold check indicator, label, and error state.
6. **`AynvoraSwitch`**: Accessible toggle switch with minimum 48dp touch target, gold active thumb/track, label, and disabled state.
7. **`AynvoraTopAppBar`**: Header bar with title, optional navigation icon, actions slot, and cosmic surface styling.
8. **`AynvoraNavigationBar` & `AynvoraNavigationBarItem`**: Bottom navigation bar with item slots, badge support, and gold active indicators for compact viewports.
9. **`AynvoraNavigationRail` & `AynvoraNavigationRailItem`**: Vertical navigation rail with header, item slots, and footer for medium/expanded viewports (tablets, foldables, desktop).
10. **`AynvoraSectionHeader`**: Content section header with gold title, optional subtitle, and trailing action button/slot.

---

## 11. Component API Conventions
- Universal support for `modifier: Modifier = Modifier`.
- State hoisting pattern (`value`, `onValueChange`, `checked`, `onCheckedChange`).
- Reusable and generic APIs (e.g. `DateField`, not `BirthDateInputForKundali`).
- Dedicated slots for icons and actions (`leadingIcon: (@Composable () -> Unit)? = null`).
- Non-opinionated navigation: visual/interaction primitives only; routing logic belongs in UI/app layer.

---

## 12. Accessibility Support
- **Touch Target**: Minimum 48dp touch target enforced across all interactive components (`AynvoraButton`, `AynvoraTextField`, `AynvoraCheckbox`, `AynvoraSwitch`, navigation items) via internal padding or minimum size constraints.
- **Semantics**: Clear semantic roles (`Role.Button`, `Role.Checkbox`, `Role.Switch`, `Role.Tab`) attached via `Modifier.semantics`.
- **Non-Color-Only Indicators**: Error states accompanied by text labels (`errorMessage`), semantic properties, and high-contrast indicators.
- **Dynamic Text Scaling**: Typography integrates with scalable units (`ssp`) and adaptive layout scale factors.

---

## 13. Design-Token Usage
Strict adherence to Master UI Style Guide tokens:
- **Colors**: `AynvoraTheme.colors.Gold` (`#C9A227`), `CosmicBlack` (`#080B14`), `CosmicNavy` (`#0D1224`), `CosmicIndigo` (`#151B38`), `Ivory` (`#FAF8F2`), `TextDark` (`#171A21`), `TextLight` (`#FAF8F2`), `TextLightSecondary` (`#B3B8C2`), `BorderSubtle` (`#1E2640`), `Error` (`#E53935`).
- **Typography**: Display, Title, Body, and Caption styles with scalable units.
- **Spacing**: `AynvoraSpacing.space4`, `space8`, `space16`, `space24`, etc.
- **Shapes**: `AynvoraShapes.cornerSmall` (4dp), `cornerMedium` (8dp), `cornerLarge` (16dp).
- **Elevation**: `AynvoraElevation.level0` to `level3`.

---

## 14. Files Added
1. `aynvora-core/build.gradle.kts`
2. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/AynvoraSdk.kt`
3. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/BirthData.kt`
4. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/CalculationConfig.kt`
5. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/EngineMetadata.kt`
6. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/ChartRequest.kt`
7. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/models/ChartResult.kt`
8. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/result/AynvoraResult.kt`
9. `aynvora-core/src/commonMain/kotlin/com/aynvora/core/internal/AstroEngineAdapter.kt`
10. `aynvora-core/src/commonTest/kotlin/com/aynvora/core/AynvoraSdkTest.kt`
11. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraFormField.kt`
12. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraTextField.kt`
13. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraDateField.kt`
14. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraTimeField.kt`
15. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraCheckbox.kt`
16. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/inputs/AynvoraSwitch.kt`
17. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/navigation/AynvoraTopAppBar.kt`
18. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/navigation/AynvoraNavigationBar.kt`
19. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/navigation/AynvoraNavigationRail.kt`
20. `design-system/src/commonMain/kotlin/com/aynvora/designsystem/components/navigation/AynvoraSectionHeader.kt`
21. `design-system/src/commonTest/kotlin/com/aynvora/designsystem/components/AynvoraComponentsTest.kt`
22. `docs/PHASE_2_SDK_FACADE_AND_DESIGN_SYSTEM.md`

---

## 15. Files Changed
1. `settings.gradle.kts`: Included `:aynvora-core`.
2. `ui/build.gradle.kts`: Replaced `:astro-engine` dependency with `:aynvora-core`.
3. `docs/07_COMPONENT_LIBRARY.md`: Added implemented components section covering Button, Card, Form Inputs, and Navigation.
4. `docs/17_API_CONTRACTS.md`: Formalized SDK entry point and error model contract.

---

## 16. Files Removed
None.

---

## 17. Dependencies Added / Removed
- Added `:aynvora-core` to settings.
- In `ui`:
  - Removed dependency: `project(":astro-engine")`
  - Added dependency: `project(":aynvora-core")`
- In `aynvora-core`:
  - Added internal dependency: `project(":astro-engine")`
  - Added multiplatform libraries: `kotlinx-coroutines-core`, `kotlinx-serialization-json`.

---

## 18. Verification Commands
```bash
# 1. Clean
./gradlew clean

# 2. Unit and Component Tests
./gradlew :aynvora-core:jvmTest :design-system:jvmTest :astro-engine:jvmTest

# 3. KMP Multi-Target Compilation across all libraries
./gradlew :aynvora-core:compileKotlinJvm :aynvora-core:compileKotlinIosArm64 :aynvora-core:compileKotlinIosSimulatorArm64 :aynvora-core:compileAndroidMain \
          :design-system:compileKotlinJvm :design-system:compileKotlinIosArm64 :design-system:compileKotlinIosSimulatorArm64 :design-system:compileAndroidMain \
          :ui:compileKotlinJvm :ui:compileKotlinIosArm64 :ui:compileKotlinIosSimulatorArm64 :ui:compileAndroidMain \
          :astro-engine:compileKotlinJvm :astro-engine:compileKotlinIosArm64 :astro-engine:compileKotlinIosSimulatorArm64 :astro-engine:compileAndroidMain

# 4. Application Module Assemblies
./gradlew :desktopApp:packageDistributionForCurrentOS :androidApp:assembleDebug
```

---

## 19. Verification Results
- `:aynvora-core:jvmTest`: **PASSED** (All SDK facade tests, boundary validation, error hierarchy, and metadata tests passed with 0 failures).
- `:design-system:jvmTest`: **PASSED** (Component rendering tests for all new inputs/navigation primitives, theme, and adaptive unit tests passed with 0 failures).
- `:astro-engine:jvmTest`: **PASSED** (Engine baseline tests passed with 0 failures).
- **KMP Multiplatform Target Compilation**: **PASSED** across all modules (`:aynvora-core`, `:design-system`, `:ui`, `:astro-engine`) for JVM, Android, iOS Arm64, and iOS Simulator Arm64.
- `:desktopApp:packageDistributionForCurrentOS`: **PASSED** (Desktop application distribution package generated successfully).
- `:androidApp:assembleDebug`: **PASSED** (Android debug APK built successfully).

---

## 20. Known Limitations
- The underlying `:astro-engine` calculation stub remains in the baseline `FOUNDATION_READY` state as mandated by governance rules (no fake astrology algorithms).
- Native iOS application binary linking (`linkDebugFrameworkIos*`) requires Xcode.app on the macOS host; multiplatform KMP `.klib` compilation for `iosArm64` and `iosSimulatorArm64` targets succeeds cleanly.

---

## 21. Explicitly Deferred Persistence Work
Persistence is explicitly OUT OF SCOPE for Phase 2:
- No Room / SQLDelight / SQLite databases.
- No DataStore / SharedPreferences / NSUserDefaults.
- No CoreData or local filesystem profile storage.
- No user profile, chart history, or offline database synchronization.
- Deferred to dedicated persistence architecture phase.

---

## 22. Next Recommended Phase
**Phase 3: Reference Application Foundation & Screen Composition**
- Implement initial astrology form compositions (combining `AynvoraFormField`, `AynvoraDateField`, `AynvoraTimeField`, and `AynvoraTextField`).
- Integrate the facade `AynvoraSdk` into screen ViewModels.
- Implement responsive multi-device navigation shells (using `AynvoraNavigationBar` on mobile and `AynvoraNavigationRail` on tablet/desktop).
