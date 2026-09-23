# AYNVORA — Phase 1A Reproducible Build Foundation

## 1. Environment
- **Operating System**: macOS (Darwin 26.7, `x86_64`)
- **Xcode Command Line Tools**: Installed at `/Library/Developer/CommandLineTools`
- **Full Xcode Application**: Not installed (`/Applications/Xcode.app` absent)
- **Android SDK**: Installed at `/Users/ishant/Library/Android/sdk` (`android-36`, `android-37.0`)

## 2. Java / JDK Version
- **Runtime**: OpenJDK 21.0.11 (`JBR-21.0.11+10-1163.116-nomod`)
- **JVM Vendor**: JetBrains s.r.o.
- **Java Home Target**: Java 21

## 3. Gradle Version
- **Gradle Wrapper**: Generated and functional with Gradle **9.6.0** (compatible with AGP 9.4.0, Kotlin 2.4.20, and Compose Multiplatform 1.12.1).

## 4. Kotlin Version
- **Kotlin**: `2.4.20` (configured across all KMP modules via version catalog `gradle/libs.versions.toml`).

## 5. Compose Version
- **Compose Multiplatform**: `1.12.1` (`org.jetbrains.compose`).
- **Compose Compiler Plugin**: `org.jetbrains.kotlin.plugin.compose` (Kotlin `2.4.20`).

## 6. Android Gradle Plugin (AGP) Version
- **AGP**: `9.4.0` (`com.android.application` and `com.android.kotlin.multiplatform.library`).

## 7. Module List
- `:design-system` — Multiplatform design system containing Master UI Style Guide tokens, adaptive window sizing, `sdp`/`ssp` units, and approved components (`AynvoraButton`, `AynvoraCard`).
- `:astro-engine` — Multiplatform deterministic astrology calculation boundary (free of UI dependencies).
- `:ui` — Shared Compose Multiplatform UI (`AynvoraApp`, `MainViewController` iOS bridge).
- `:androidApp` — Reference native Android host application.
- `:desktopApp` — Reference JVM desktop host application.

## 8. Platform Targets
- **Android**: Target SDK 37, Min SDK 23, Compile SDK 37.
- **Desktop (JVM)**: Java 21 desktop application runtime.
- **iOS (Apple)**: KMP compilation targets `iosArm64` and `iosSimulatorArm64`.
- **Form Factors Supported**: Mobile (Compact), Foldable (Medium), Tablet (Expanded), Desktop (Expanded/Large).

## 9. Gradle Wrapper Status
- Fully operational: [gradlew](file:///Users/ishant/Desktop/aynvora-sdk/gradlew), [gradlew.bat](file:///Users/ishant/Desktop/aynvora-sdk/gradlew.bat), [gradle-wrapper.jar](file:///Users/ishant/Desktop/aynvora-sdk/gradle/wrapper/gradle-wrapper.jar), and [gradle-wrapper.properties](file:///Users/ishant/Desktop/aynvora-sdk/gradle/wrapper/gradle-wrapper.properties).

## 10. Build Commands Executed
- `java -version`
- `./gradlew --version`
- `xcodebuild -version`
- `./gradlew tasks`
- `./gradlew :androidApp:assembleDebug`
- `./gradlew :desktopApp:build`
- `./gradlew test`
- `./gradlew :design-system:jvmTest :astro-engine:jvmTest`

## 11. Successful Commands
- `java -version`: Verified OpenJDK 21.0.11.
- `./gradlew --version`: Verified Gradle 9.6.0 wrapper.
- `./gradlew tasks`: Verified project task graph.
- `./gradlew :androidApp:assembleDebug`: Generated debug APK `androidApp-debug.apk`.
- `./gradlew :desktopApp:build`: Compiled and assembled Desktop runnable JAR.
- `./gradlew test`: Verified test lifecycle tasks.
- `./gradlew :design-system:jvmTest :astro-engine:jvmTest`: All token and adaptive unit tests passed.

## 12. Failed Commands
- `xcodebuild -version`: Exited with code 1.
- `./gradlew build` (when running native iOS test linker tasks `linkDebugTestIosSimulatorArm64`): Exited with code 1.

## 13. Exact Failure Reasons
- Host active developer directory is set to `/Library/Developer/CommandLineTools` rather than full `/Applications/Xcode.app`. `xcodebuild` is only distributed with full Xcode; consequently, the Kotlin/Native Mach-O linker cannot locate `iphonesimulator` SDK sysroots during native binary linking.

## 14. Test Status
- Automated test suite in `design-system` (`AynvoraThemeTest`, `AynvoraAdaptiveTest`) and `astro-engine` (`AstroEngineTest`) is passing:
  - Color palette verification: **PASSED**
  - 11-step typography scale: **PASSED**
  - Spacing tokens (4dp to 64dp): **PASSED**
  - Shape and motion constants: **PASSED**
  - Window size class detection across 5 device categories: **PASSED**
  - Scalable units (`sdp`/`ssp`) clamping: **PASSED**
  - AstroEngine contract stub: **PASSED**

## 15. iOS Status
- **KMP Framework Target**: Configured in `ui/build.gradle.kts`.
- **KMP `.klib` Compilation**: Successfully compiles for both `iosArm64` and `iosSimulatorArm64`.
- **Compose Bridge**: `MainViewController.kt` implemented.
- **Native Host**: SwiftUI app structure in `iosApp/` implemented.
- **Native Binary Linking**: Requires full Xcode installation on macOS.

## 16. Known Limitations
- Host environment lacks full `/Applications/Xcode.app`, restricting native iOS verification to `.klib` compilation.
- Kotlin/Native host compiler reports deprecation notice for Intel Mac (`macos_x64`).

## 17. Dependency Changes
- None during Phase 1A (existing clean version catalog preserved).

## 18. Files Changed
- None (source code untouched in Phase 1A).

## 19. Files Added
- `docs/PHASE_1A_BUILD_FOUNDATION.md`

## 20. Files Deleted
- None.

## 21. Next Recommended Phase
Phase 2: Formalize public SDK API facade (`aynvora-core`) and expand foundational design system components (`TextField`, `Chip`, `Tabs`) per `07_COMPONENT_LIBRARY.md` before proceeding to offline data persistence.
