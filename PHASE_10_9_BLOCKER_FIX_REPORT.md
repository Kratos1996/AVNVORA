# Aynvora Release Blocker Fix Report — Vedic Astrology Navigation & Palmistry Camera/Gallery

**Target Device**: Samsung Galaxy S23 Ultra (`SM-S918B`)  
**OS Version**: Android 16 (SDK 36, One UI 8.0)  
**Date**: September 27, 2026  
**Status**: **ALL BLOCKERS RESOLVED & VERIFIED ON HARDWARE**

---

## 1. Blocker Summary Table

| Blocker ID    | Feature             | Problem Observed                                                                                                            | Root Cause                                                                                                                                                                                                                                                                                                                                     | Solution                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | Physical Device Verification Status                                                                                                                                                                                                                                     |
|:--------------|:--------------------|:----------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **BLOCKER-1** | **Vedic Astrology** | Bottom sheet opened, but clicking Vedic Astrology option did not launch the feature. User tapped repeatedly with no effect. | 1. `FeatureFoundationDetailSheet` only provided an "Understood — Close" button without a selectable feature launch action.<br>2. `AynvoraAppState` lacked `isAstrologyOpen`.<br>3. `AynvoraNavigationTarget` had no `AstrologyHome` entry.<br>4. Missing UI composable route in `ui` module to render Vedic natal charts from `:astro-engine`. | 1. Added `VEDIC_ASTROLOGY_SELECT`, `ASTROLOGY_CALCULATE`, and `ASTROLOGY_CLOSE` to `QaActionId`.<br>2. Added `AynvoraNavigationTarget.AstrologyHome` & registered in `AynvoraNavigationValidator`.<br>3. Added `OpenAstrology` and `CloseAstrology` to `AynvoraAppUiEvent` and `isAstrologyOpen` to `AynvoraAppState`.<br>4. Added primary launch CTA in bottom sheet for `CoreFeatureId.ASTROLOGY`.<br>5. Implemented `AstrologyRoute` and `AstrologyScreen` displaying Lagna, planetary positions (Grahas), Rashis, Nakshatras, and Houses. | **PASS** — Verified on Galaxy S23 Ultra: sheet opened -> clicked "वैदिक ज्योतिष खोलें" -> Vedic Astrology screen launched -> calculated full chart -> back button closed cleanly -> repeatable without failure.                                                         |
| **BLOCKER-2** | **Palmistry**       | Camera action did not launch the camera; Gallery action did not launch the photo picker.                                    | Actions in `PalmistryRoute.kt` (`PalmInputSelectionScreen`) generated synthetic mock byte arrays (`generateSyntheticPalmImageBytes`) instead of delegating to Android system ActivityResult contracts. Camera permission and FileProvider cache path were also missing.                                                                        | 1. Added `android.permission.CAMERA` and camera hardware feature to `AndroidManifest.xml`.<br>2. Configured `<cache-path name="palm_images" path="palm_images/" />` in `report_file_paths.xml`.<br>3. Built multiplatform `rememberPalmImagePicker` (expect/actual): Android implementation uses `ActivityResultContracts.TakePicture()` via FileProvider URI and `ActivityResultContracts.PickVisualMedia()`.<br>4. Wired `PalmCaptureScreen` to trigger platform picker and feed real image bytes into `PalmAnalysisEngine`.                | **PASS** — Verified on Galaxy S23 Ultra: Camera opened Samsung native camera viewfinder -> captured photo -> "OK" returned image -> quality analysis passed 100%. Gallery opened Android 16 system photo picker -> user selected photo -> quality analysis passed 100%. |

---

## 2. Detailed Breakdown

### Blocker 1 — Vedic Astrology Navigation

#### Root Cause

1. **Missing Navigation State & Target**: `AynvoraNavigationTarget` sealed interface lacked an
   `AstrologyHome` target. Consequently, `AynvoraNavigationValidator` rejected any astrology
   navigation event.
2. **Missing UI View State**: `AynvoraAppState` did not track `isAstrologyOpen`, and
   `AynvoraAppViewModel` had no handler to open or close the astrology view.
3. **Passive Bottom Sheet**: The bottom sheet for foundation features (
   `FeatureFoundationDetailSheet`) displayed informational cards with only an "Understood — Close"
   dismissal button. There was no primary action button to actually transition into Vedic Astrology.
4. **Missing UI Presentation Layer**: While `:astro-engine` provided calculation logic for planetary
   positions, Rashis, Nakshatras, and Houses, the `:ui` module had no corresponding Composable
   screen.

#### Applied Solution

- **Action IDs**: Added `VEDIC_ASTROLOGY_SELECT`, `ASTROLOGY_CALCULATE`, and `ASTROLOGY_CLOSE` in [
  `aynvora-qa-core/src/commonMain/kotlin/com/aynvora/qa/core/models/QaActionId.kt`](file:///Users/ishant/Desktop/aynvora-sdk/aynvora-qa-core/src/commonMain/kotlin/com/aynvora/qa/core/models/QaActionId.kt).
- **Navigation Target**: Added `data object AstrologyHome : AynvoraNavigationTarget` in [
  `aynvora-core/src/commonMain/kotlin/com/aynvora/core/event/AynvoraEffect.kt`](file:///Users/ishant/Desktop/aynvora-sdk/aynvora-core/src/commonMain/kotlin/com/aynvora/core/event/AynvoraEffect.kt)
  and registered it in [
  `AynvoraNavigationValidator.kt`](file:///Users/ishant/Desktop/aynvora-sdk/aynvora-core/src/commonMain/kotlin/com/aynvora/core/event/AynvoraNavigationValidator.kt).
- **Event Registry**: Added `dashboard.astrology.open_clicked`, `astrology.close_clicked`, and
  `astrology.calculate_clicked` to [
  `AynvoraEventRegistry.kt`](file:///Users/ishant/Desktop/aynvora-sdk/aynvora-core/src/commonMain/kotlin/com/aynvora/core/event/AynvoraEventRegistry.kt).
- **App State & ViewModel**: Added `isAstrologyOpen: Boolean = false` to `AynvoraAppState` and
  handled `OpenAstrology` (emits `AstrologyHome` navigation effect) and `CloseAstrology` in [
  `AynvoraAppViewModel.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/AynvoraAppViewModel.kt).
- **Bottom Sheet Primary Action**: Updated `FeatureFoundationDetailSheet` in [
  `AynvoraApp.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/AynvoraApp.kt)
  to display a golden "वैदिक ज्योतिष खोलें" / "Open Vedic Astrology" primary button with QA tag
  `QaActionId.VEDIC_ASTROLOGY_SELECT`.
- **Screen Implementation**: Created [
  `AstrologyRoute.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/astrology/AstrologyRoute.kt)
  and [
  `AstrologyScreen.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/astrology/AstrologyScreen.kt),
  providing:
    - Input fields for Date (YYYY-MM-DD), Time (HH:mm:ss), Latitude, Longitude, Location, and
      Ayanamsha (Lahiri, Krishnamurti, Raman).
    - Calculate Chart CTA triggering `AynvoraAstroEngine`.
    - Rich Vedic results view displaying Ascendant (Lagna), planetary longitude degrees, Rashis,
      Nakshatras, Padas, retrograde indicators, and 12 Bhava (House) cusps.
    - Back navigation button cleanly returning to Dashboard.

---

### Blocker 2 — Palmistry Camera & Gallery Actions

#### Root Cause

1. **Synthetic Byte Bypass**: In [
   `ui/src/commonMain/kotlin/com/aynvora/ui/palmistry/PalmistryRoute.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/palmistry/PalmistryRoute.kt),
   buttons for "कैमरा से फोटो लें (Camera)" and "गैलरी से छवि चुनें (Gallery)" bypassed the platform
   camera and photo picker, immediately calling `generateSyntheticPalmImageBytes` with mock data.
2. **Missing Permissions**: Android manifest lacked
   `<uses-permission android:name="android.permission.CAMERA" />` and
   `<uses-feature android:name="android.hardware.camera" android:required="false" />`.
3. **Missing FileProvider Cache Path**: Captured images require a content URI from Android's
   FileProvider. `report_file_paths.xml` only had paths for reports, not temporary camera images.

#### Applied Solution

- **Manifest & FileProvider**: Added camera permission and
  `<cache-path name="palm_images" path="palm_images/" />` in [
  `androidApp/src/main/res/xml/report_file_paths.xml`](file:///Users/ishant/Desktop/aynvora-sdk/androidApp/src/main/res/xml/report_file_paths.xml).
- **Expect/Actual Platform Image Picker**:
    - Defined `expect class PalmImagePicker` with `launchCamera()` and `launchGallery()` and
      `rememberPalmImagePicker(...)` in [
      `ui/src/commonMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/commonMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.kt).
    - Implemented Android actual in [
      `ui/src/androidMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.android.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/androidMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.android.kt):
        - Uses `ActivityResultContracts.PickVisualMedia()` for modern Android Photo Picker (zero
          runtime permissions on Android 13+ / 16).
        - Uses `ActivityResultContracts.RequestPermission()` for camera runtime permission check.
        - Creates a timestamped cache file in `context.cacheDir/palm_images` and generates
          FileProvider URI `${applicationId}.report-files`.
        - Uses `ActivityResultContracts.TakePicture()` to launch native camera and read back byte
          array via ContentResolver upon capture.
    - Implemented JVM fallback in [
      `ui/src/jvmMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.jvm.kt`](file:///Users/ishant/Desktop/aynvora-sdk/ui/src/jvmMain/kotlin/com/aynvora/ui/palmistry/PalmImagePicker.jvm.kt).
- **Wired UI Flow**: Replaced synthetic mock generation in `PalmCaptureScreen` with
  `picker.launchCamera()` and `picker.launchGallery()`. Retained a distinct "मानक नमूना हथेली का
  उपयोग करें" (Use Standard Sample) button for deterministic QA/offline environments.

---

## 3. Physical Hardware Verification (Samsung Galaxy S23 Ultra)

All verification was executed directly on the connected physical device:

- **Device ID**: `adb-RZCX91AZ9EF-F3IQRH (2)._adb-tls-connect._tcp`
- **Model**: Samsung Galaxy S23 Ultra (`SM-S918B`)
- **OS**: Android 16 (SDK 36)

### Test Execution Results

| Step   | Action Performed                                        | Expected Behavior                                     | Actual Behavior Observed                                                                                                                                         | Result   |
|:-------|:--------------------------------------------------------|:------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------|
| **T1** | Tap "वैदिक ज्योतिष" on Dashboard                        | Detail bottom sheet appears                           | Bottom sheet displayed with feature details and golden "वैदिक ज्योतिष खोलें" button                                                                              | **PASS** |
| **T2** | Tap "वैदिक ज्योतिष खोलें" (`VEDIC_ASTROLOGY_SELECT`)    | Bottom sheet dismisses, Vedic Astrology screen opens  | Screen smoothly navigated to `AstrologyScreen` with birth data fields and Lagna card                                                                             | **PASS** |
| **T3** | Tap "कुंडली गणना करें" (`ASTROLOGY_CALCULATE`)          | Calculates natal chart deterministically              | Complete planetary positions (Surya, Chandra, Mangala, Budha, Guru, Shukra, Shani, Rahu, Ketu) and 12 Bhava cusps displayed with degrees, Rashis, and Nakshatras | **PASS** |
| **T4** | Tap Back Arrow (`ASTROLOGY_CLOSE`)                      | Returns to Dashboard                                  | Cleanly returned to Dashboard with bottom sheet dismissed                                                                                                        | **PASS** |
| **T5** | Repeat T1 & T2 multiple times                           | No crash or navigation lock                           | Bottom sheet and screen open and close cleanly without state leak                                                                                                | **PASS** |
| **T6** | Navigate to Palmistry -> "कैमरा से फोटो लें (Camera)"   | Prompts for permission / launches Samsung Camera      | Native Samsung Galaxy camera viewfinder launched with full controls (1x, 2x, 10x, 100x zoom, shutter button)                                                     | **PASS** |
| **T7** | Capture photo with camera shutter & tap "OK"            | Image captured, returned to Aynvora, quality analyzed | Captured photo bytes processed by `PalmAnalysisEngine`, Quality check passed 100%, proceeded to Palm Insights screen                                             | **PASS** |
| **T8** | Navigate to Palmistry -> "गैलरी से छवि चुनें (Gallery)" | Launches Android system photo picker                  | Android 16 photo picker modal bottom sheet opened displaying photos and screenshots                                                                              | **PASS** |
| **T9** | Select image in photo picker & tap "Done"               | Image loaded, quality analyzed                        | ContentResolver read URI bytes, quality check passed 100%, proceeded to Palm Insights screen                                                                     | **PASS** |

---

## 4. Verification Artifacts & Screenshots

1. **`screen_astrology_opened.png`**: Vedic Astrology screen launched from bottom sheet on physical
   device.
2. **`screen_chart_calculated.png`**: Vedic Astrology calculated chart showing Lagna, planetary
   coordinates, Rashis, Nakshatras, and Bhavas.
3. **`screen_closed_verified.png`**: Clean exit back to previous screen upon tapping Close.
4. **`screen_capture_actions.png`**: Palmistry capture options (Camera, Gallery, Sample) displayed
   on physical device.
5. **`screen_gallery_picker.png`**: Android 16 System Photo Picker modal launched after tapping
   Gallery.
6. **`screen_after_picker_done2.png`**: Image quality check passed after gallery photo selection.
7. **`screen_camera_launched.png`**: Samsung native camera viewfinder launched after tapping Camera.
8. **`screen_after_camera_ok.png`**: Image quality check passed after camera photo capture.
9. **`screen_palm_final_analysis.png`**: Complete Palmistry insights rendered from captured image.

---

## 5. Conclusion & Release Readiness

Both release-blocking functional defects have been resolved and verified on physical hardware
running Android 16 (SDK 36):

- **Blocker 1**: Resolved with typed navigation contracts, proper view model state management,
  bottom sheet primary CTA, and complete Vedic Astrology UI rendering from `:astro-engine`.
- **Blocker 2**: Resolved with multiplatform `PalmImagePicker`, Android FileProvider integration,
  and platform ActivityResult contracts for Camera (`TakePicture`) and Gallery (`PickVisualMedia`).

The codebase is fully stable, compliant with Aynvora architecture and design tokens, and ready for
release.
