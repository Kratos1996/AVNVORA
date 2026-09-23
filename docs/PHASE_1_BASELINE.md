# AYNVORA SDK — Phase 1 Baseline

## 1. Current Repository Structure

```text
aynvora-sdk/
├── 00_MASTER_RULES.md            # Highest-level engineering & product governance
├── AI_AGENT_INSTRUCTIONS.md      # Instructions for AI and coding assistants
├── README.md                     # Project overview and build instructions
├── build.gradle.kts              # Root Gradle configuration
├── settings.gradle.kts           # Module inclusion and repository resolution
├── gradle.properties             # Build and compiler flags
├── gradlew, gradlew.bat          # Official Gradle wrapper scripts (v9.6.0)
├── gradle/
│   ├── libs.versions.toml        # Centralized version catalog
│   └── wrapper/                  # Gradle wrapper properties and JAR
├── docs/                         # Authoritative specifications (00 to 31)
├── rules/                        # Governance specification pack mirror
├── design-system/                # KMP Design System module (tokens, adaptive, components)
│   └── src/
│       ├── commonMain/           # Colors, typography, spacing, shapes, motion, adaptive
│       └── commonTest/           # Token and adaptive unit tests
├── astro-engine/                 # KMP Astro Engine boundary module (decoupled from UI)
│   └── src/
│       ├── commonMain/           # AstroEngine contract and foundation stub
│       └── commonTest/           # Contract verification test
├── ui/                           # Shared Compose Multiplatform UI module
│   └── src/
│       ├── commonMain/           # AynvoraApp root composable
│       └── iosMain/              # MainViewController iOS bridge
├── androidApp/                   # Reference Android host application
│   └── src/main/                 # MainActivity and AndroidManifest
├── desktopApp/                   # Reference Desktop JVM host application
│   └── src/jvmMain/              # Desktop window entry point
└── iosApp/                       # Native Swift/SwiftUI iOS host structure
    └── iosApp/                   # iOSApp.swift, ContentView.swift, Info.plist
```

## 2. Current Build Configuration

- **Gradle Version**: 9.6.0
- **Android Gradle Plugin (AGP)**: 9.4.0
- **Kotlin**: 2.4.20
- **Compose Multiplatform**: 1.12.1
- **JDK Requirement**: Java 21
- **Android SDK Requirements**:
  - `compileSdk`: 37
  - `minSdk`: 23
  - `targetSdk`: 37

## 3. Current Modules

1. `:design-system` — Multiplatform design system containing Master UI Style Guide tokens (colors, 11-step typography scale, spacing, shapes, elevation, motion), adaptive layout tools (`sdp`/`ssp`, window size classes), and approved components (`AynvoraButton`, `AynvoraCard`).
2. `:astro-engine` — Multiplatform deterministic astrology calculation boundary, free of Compose or UI dependencies.
3. `:ui` — Shared Compose Multiplatform user interface layer, hosting `AynvoraApp` and the iOS `MainViewController` bridge.
4. `:androidApp` — Reference native Android application application host.
5. `:desktopApp` — Reference JVM desktop application host.

## 4. Current Platform Targets

- **Android**: Target SDK 37, Min SDK 23. Compiles to debug/release APK and KMP AARs.
- **Desktop (JVM)**: Java 21 desktop application runtime via Compose Multiplatform. Compiles to executable JAR.
- **iOS (Apple)**: Targets `iosArm64` and `iosSimulatorArm64`. Compiles to `.klib` and static framework `AynvoraUi.framework`.
- **Adaptive Screen Classes**: Compact (Mobile), Medium (Foldable), Expanded (Tablet, Desktop).

## 5. Existing Dependencies

- `org.jetbrains.compose`: `runtime`, `foundation`, `material3`, `ui`
- `androidx.activity:activity-compose`: `1.12.0` (Android)
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`: `1.10.2`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`: `1.9.0`
- `kotlin("test")`: standard test framework

## 6. Known Build Limitations

- **iOS Native Framework Linking**: The build machine has macOS Command Line Tools installed (`/Library/Developer/CommandLineTools`) but lacks full `/Applications/Xcode.app`. While Kotlin/Native klib compilation succeeds for `iosArm64` and `iosSimulatorArm64`, native Mach-O linking (`linkDebugFrameworkIosSimulatorArm64`, `linkDebugTestIosSimulatorArm64`) fails when `/usr/bin/xcrun xcodebuild -version` cannot locate the full iPhoneSimulator SDK.
- **Intel Mac Host Deprecation**: Kotlin/Native toolchain reports `macos_x64` host deprecation with recommendations for Apple Silicon migration in future Kotlin releases.

## 7. Missing Foundation Items

Per `03_ARCHITECTURE.md` and `04_PROJECT_STRUCTURE.md`:
1. **Public SDK Facade (`aynvora-core`)**: Currently `:ui` depends directly on `:astro-engine`. A domain coordinator/public facade is needed to strictly separate public SDK APIs from internal module implementations.
2. **Offline Data & Storage Layer (`aynvora-storage` / `aynvora-data`)**: Local database and caching layer (e.g., Room KMP / SQLDelight / encrypted KV store) specified in `10_DATA_ARCHITECTURE.md` and `11_OFFLINE_FIRST.md` is not yet scaffolded.
3. **Ephemeris & Astro Engine Calculations**: Real astronomical math / Swiss Ephemeris bindings / deterministic Vedic calculation modules (`08_ASTRO_ENGINE.md`, `09_ASTROLOGY_RULES.md`) have not yet been implemented (only the foundation stub exists).
4. **Licensing & Entitlements Layer (`aynvora-licensing`)**: Cryptographic signature validation and license models (`15_LICENSING_ENTITLEMENTS.md`) are not yet integrated.
5. **Remaining Component Families**: Component library currently implements `Button` and `Card`. Additional families from `07_COMPONENT_LIBRARY.md` (`TextField`, `Chip`, `Tabs`, `Dialog`, `BottomSheet`, `List`, `Navigation`, `Chart`) remain to be added as required by future screens.

## 8. Recommended Next Step

Begin Phase 2: Design System Component Expansion and SDK Public API Facade, formalizing the domain facade layer (`aynvora-core`) and expanding foundational input/navigation components before implementing data persistence and ephemeris calculations.
