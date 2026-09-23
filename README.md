# AYNVORA SDK

Ancient Wisdom. Clearer Choices.

A Kotlin Multiplatform + Compose Multiplatform foundation for the AYNVORA astrology technology platform.

---

## Prerequisites & Environment Requirements

- **JDK Version**: Java 21 (OpenJDK / Eclipse Temurin / Azul Zulu recommended). Gradle 9.6.0 daemon runs on Java 21.
- **Android SDK**:
  - `compileSdk`: **37** (required for Compose Multiplatform 1.12.1 compatibility)
  - `targetSdk`: **37**
  - `minSdk`: **23** (Android 6.0+)
- **Xcode (iOS)**:
  - macOS host with full **Xcode** installed (`/Applications/Xcode.app`).
  - Active developer tools configured via:
    ```bash
    sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
    ```
  - *Note*: Xcode Command Line Tools alone are insufficient for linking native iOS binaries because the Kotlin/Native linker requires the iPhoneOS/iPhoneSimulator SDK toolchains provided by full Xcode.

---

## Project Modules

- `design-system` — Multiplatform module defining AYNVORA design tokens, colors, typography, theme, and shared UI primitives based on the Master UI Style Guide.
- `astro-engine` — Multiplatform deterministic astrology calculation engine and domain models. Completely decoupled from UI.
- `ui` — Shared Compose Multiplatform UI components, layout, and screens (`AynvoraApp`), exposing `MainViewController` for iOS.
- `androidApp` — Native Android reference application host.
- `desktopApp` — JVM Desktop reference application host using Compose Multiplatform.
- `iosApp` — Native SwiftUI iOS application host referencing the shared `AynvoraUi` framework.

---

## Gradle Wrapper Usage

Use the included Gradle wrapper for all builds across platforms:

```bash
# macOS / Linux
./gradlew <task>

# Windows
gradlew.bat <task>
```

Verify available Gradle tasks:
```bash
./gradlew tasks
```

---

## Build Commands

### Android Application
Compile and package the debug APK:
```bash
./gradlew :androidApp:assembleDebug
```
Output location:
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Desktop Application
Run the desktop Compose application:
```bash
./gradlew :desktopApp:run
```

Or package the desktop JAR:
```bash
./gradlew :desktopApp:jvmJar
```
Output location:
`desktopApp/build/libs/desktopApp-jvm.jar`

### Shared Multiplatform Compilations
Compile all multiplatform source sets (JVM, Android, iOS klibs):
```bash
./gradlew :design-system:assemble :astro-engine:assemble :ui:assemble
```

Run test suite:
```bash
./gradlew :design-system:jvmTest :astro-engine:jvmTest
```

---

## iOS Setup & Build Instructions

### 1. Requirements
Ensure full Xcode is installed and active on macOS.

### 2. Compile iOS Kotlin Targets
Compile the Kotlin Multiplatform libraries for iOS:
```bash
./gradlew :ui:compileKotlinIosSimulatorArm64 :ui:compileKotlinIosArm64
```

### 3. Generate Shared Framework
Link the shared framework binary for the simulator or device:
```bash
# For iOS Simulator (Apple Silicon / Simulator ARM64)
./gradlew :ui:linkDebugFrameworkIosSimulatorArm64

# For iOS Device (ARM64)
./gradlew :ui:linkDebugFrameworkIosArm64
```
The output framework will be generated at:
`ui/build/bin/iosSimulatorArm64/debugFramework/AynvoraUi.framework`

### 4. Running the iOS App
1. Open Xcode.
2. Open or create the Xcode project located in `iosApp/`.
3. In your Target settings under **Frameworks, Libraries, and Embedded Content**, link `AynvoraUi.framework`.
4. Ensure `Framework Search Paths` points to `$(SRCROOT)/../ui/build/bin/iosSimulatorArm64/debugFramework`.
5. Select an iOS Simulator destination and build/run.

---

## Architecture Rules

1. **Master UI Style Guide Compliance**: No product UI should bypass the AYNVORA Master UI Style Guide. All tokens, styles, and components must reside in `design-system`.
2. **Astro Engine Decoupling**: The Astro Engine must remain independent from any UI framework or presentation layer.
3. **Offline-First**: All core calculation and storage models must be architected for offline-first operation.
