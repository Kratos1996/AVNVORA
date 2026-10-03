# AYNVORA Design System
Version: 1.3

## Source of Truth
The Master UI Style Guide and machine-readable tokens are the strict source of truth for product UI.

## Strict Component & Typography Rules (Mandatory)
1. **Prohibition of Direct Raw `Text()` Calls**:
   - Developers MUST NEVER invoke raw Compose `Text(text = ..., style = AynvoraTheme.typography.caption12.copy(fontSize = ...))` in UI screens.
   - All text rendering MUST consume pre-configured, immutable typography components from `com.aynvora.designsystem.components`:
     - `AynvoraDisplay` (40sp / 36sp Display)
     - `AynvoraTitle`, `AynvoraTitleMedium`, `AynvoraTitleSmall` (20sp / 18sp / 16sp Title)
     - `AynvoraRegularText`, `AynvoraRegularMedium`, `AynvoraRegularBold`, `AynvoraRegularExtraBold`, `AynvoraRegularUnderline` (16sp Body)
     - `AynvoraSmallText`, `AynvoraSmallMedium`, `AynvoraSmallBold` (14sp Small)
     - `AynvoraExtraSmallText`, `AynvoraExtraSmallBold` (12sp Caption)
   - Custom font-size overrides via `.copy(fontSize = ...)` in UI views are strictly forbidden. Font sizes and heights are locked by design tokens.

2. **Prohibition of Direct Raw `OutlinedTextField()` & Free Text Pickers**:
   - All free text input fields MUST use `AynvoraEditText`.
   - Date, Time, City, Country, and State picker fields MUST use `AynvoraReadOnlyField`.
   - Tapping read-only picker fields MUST invoke the respective modal picker (e.g. `DatePickerDialog`, `TimePicker`, `CitySearchBottomSheet`) without triggering soft keyboards, text cursors, or text selection.

3. **Standardized Button System**:
   - Call-to-action buttons MUST consume `AynvoraPrimaryButton`, `AynvoraPrimaryButtonSmall`, `AynvoraSecondaryButton`, `AynvoraOutlinedButton`, or `AynvoraGhostButton`.

4. **Dynamic Theme Color Tokens**:
   - Color resolution MUST consume semantic tokens on `AynvoraTheme.colors`:
     - `AynvoraTheme.colors.textPrimary`
     - `AynvoraTheme.colors.textSecondary`
     - `AynvoraTheme.colors.textMuted`
     - `AynvoraTheme.colors.surfacePrimary`
     - `AynvoraTheme.colors.surfaceSecondary`
     - `AynvoraTheme.colors.background`
   - Manual `if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark` branching inside UI screens is strictly prohibited. The theme automatically resolves color palettes.

## Token Families
- Color (`AynvoraColorScheme`)
- Typography (`AynvoraTypography`)
- Spacing (`AynvoraSpacing`)
- Shape (`AynvoraShapes`)
- Border/divider (`AynvoraBorders`)
- Elevation (`AynvoraElevation`)
- Motion (`AynvoraMotion`)
- Iconography (`AynvoraIcons`)

## KMP API Direction
AynvoraTheme
AynvoraColorScheme
AynvoraTypography
AynvoraSpacing
AynvoraShapes
AynvoraMotion

## Rule
Hard-coded visual values and direct raw Compose primitives are prohibited when an approved AYNVORA Design System component exists.
