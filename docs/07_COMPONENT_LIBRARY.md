# AYNVORA Component Library
Version: 1.0

## Reuse Rule
Search and reuse an existing component before creating a new one.

## Component Contract
Every reusable component documents:
- purpose
- anatomy
- parameters
- tokens used
- visual states
- accessibility behavior
- platform differences
- examples
- tests

## Required States
Default, pressed, focused, selected, disabled, loading, error, success and empty where applicable.

## Implemented Components

The following components are implemented in `:design-system` and available across all platforms:

### 1. Button Family
- **`AynvoraButton`**
  - **Location**: `com.aynvora.designsystem.components.AynvoraButton`
  - **Purpose**: Primary interactive call to action.
  - **Variants**: `Primary` (Gold fill), `Secondary` (Gold outline), `Text` (Text with hover), `Ghost` (Subtle container), `Destructive` (Error fill).
  - **Sizes**: `Small` (36dp height), `Medium` (44dp height), `Large` (52dp height).
  - **Tokens**: `AynvoraTheme.colors.Gold`, `CosmicBlack`, `Error`, `AynvoraSpacing`, `AynvoraShapes.cornerMedium`.
  - **States**: Default, pressed, focused, disabled, loading (progress spinner).
  - **Accessibility**: Min 48dp touch target via padding, semantic role `Button`.

### 2. Card Family
- **`AynvoraCard`**
  - **Location**: `com.aynvora.designsystem.components.AynvoraCard`
  - **Purpose**: Container for grouping related content and information.
  - **Variants**: `Elevated` (Surface elevation), `Outlined` (Border with subtle fill), `Filled` (Solid cosmic surface).
  - **Tokens**: `CosmicNavy`, `CosmicIndigo`, `BorderSubtle`, `AynvoraElevation`, `AynvoraShapes.cornerLarge`.
  - **States**: Default, interactive hover/click.

### 3. Form Input Family
- **`AynvoraFormField`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraFormField`
  - **Purpose**: Standard container providing label, required asterisk (*), supporting text, and non-color-only error indicators.
  - **Tokens**: `TextLightSecondary`, `Gold`, `TextMuted`, `Error`, `AynvoraSpacing.space4`.

- **`AynvoraTextField`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraTextField`
  - **Purpose**: General text input field.
  - **Tokens**: `CosmicNavy`, `Gold`, `TextLight`, `BorderSubtle`, `Error`, `AynvoraShapes.cornerMedium`.
  - **States**: Default, focused, error, disabled, read-only.
  - **Accessibility**: Minimum 48dp touch target, content description slots for leading/trailing icons.

- **`AynvoraDateField`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraDateField`
  - **Purpose**: Standardized date input with calendar icon and ISO formatted value validation (`YYYY-MM-DD`).
  - **Tokens**: `CosmicNavy`, `Gold`, `TextLight`, `Error`.
  - **States**: Default, focused, error, disabled.

- **`AynvoraTimeField`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraTimeField`
  - **Purpose**: Standardized time input with clock icon supporting 24-hour time (`HH:MM` or `HH:MM:SS`).
  - **Tokens**: `CosmicNavy`, `Gold`, `TextLight`, `Error`.
  - **States**: Default, focused, error, disabled.

- **`AynvoraCheckbox`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraCheckbox`
  - **Purpose**: Selection control for binary choices.
  - **Tokens**: `Gold` checked state, `CosmicBlack` checkmark, `TextLightSecondary` label, `AynvoraShapes.cornerSmall`.
  - **States**: Checked, unchecked, disabled, error.
  - **Accessibility**: Min 48dp touch target, semantic role `Checkbox`.

- **`AynvoraSwitch`**
  - **Location**: `com.aynvora.designsystem.components.inputs.AynvoraSwitch`
  - **Purpose**: Toggle switch for immediate preference changes.
  - **Tokens**: `Gold` active track/thumb, `CosmicIndigo` inactive track, `TextLightSecondary` label.
  - **States**: Checked, unchecked, disabled.
  - **Accessibility**: Min 48dp touch target, semantic role `Switch`.

### 4. Navigation Family
- **`AynvoraTopAppBar`**
  - **Location**: `com.aynvora.designsystem.components.navigation.AynvoraTopAppBar`
  - **Purpose**: Header bar for screen identification and top-level actions.
  - **Tokens**: `CosmicBlack` / `Ivory` surface, `TextLight` / `TextDark`, `Gold` action tint, `AynvoraElevation.level1`.
  - **Accessibility**: 56dp height (exceeds min 48dp touch target), navigation icon content description.

- **`AynvoraNavigationBar` & `AynvoraNavigationBarItem`**
  - **Location**: `com.aynvora.designsystem.components.navigation.AynvoraNavigationBar`
  - **Purpose**: Primary bottom navigation for compact viewports (mobile).
  - **Tokens**: `CosmicBlack`, `Gold` active pill indicator, `TextLightSecondary` inactive label.
  - **Accessibility**: Min 48dp touch targets, semantic role `Tab`.

- **`AynvoraNavigationRail` & `AynvoraNavigationRailItem`**
  - **Location**: `com.aynvora.designsystem.components.navigation.AynvoraNavigationRail`
  - **Purpose**: Vertical navigation rail for medium and expanded viewports (tablets, foldables, desktop).
  - **Tokens**: `CosmicNavy` surface, `Gold` active indicator, `BorderSubtle` end divider.
  - **Accessibility**: 72dp rail width, min 48dp item touch target.

- **`AynvoraSectionHeader`**
  - **Location**: `com.aynvora.designsystem.components.navigation.AynvoraSectionHeader`
  - **Purpose**: Standardized section division with title, optional subtitle, and trailing action.
  - **Tokens**: `Gold` title, `TextMuted` subtitle, `AynvoraSpacing.space16`.

### 5. Bottom Sheet Family (Powered by CottonSheet)
- **`CottonSheetHost` & `AynvoraBottomSheetHost`**
  - **Location**: `dev.ishant.cottonsheet.CottonSheetHost`, `com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHost`
  - **Purpose**: Zero-boilerplate, stackable modal bottom sheet container hosting dynamically managed sheets.
  - **Library**: `CottonSheet` (Compose Multiplatform).
  - **Integration**: Placed at the root composition tree inside `AynvoraTheme`.
  - **Controller**: Driven by `LocalCottonSheetController.current` (`show`, `dismiss`, `dismissAll`).

- **`AynvoraBottomSheetDefaults`**
  - **Location**: `com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults`
  - **Purpose**: Pre-configured `CottonSheetParams` applying AYNVORA Master UI Style Guide tokens.
  - **Tokens**: `CosmicNavy` container (dark) / `White` (light), `Gold` drag handle, `AynvoraShapes.cornerLarge` (16dp rounded top corners), 560dp adaptive `sheetMaxWidth` (for foldables/tablets/desktop).

- **`AynvoraBottomSheetHeader`**
  - **Location**: `com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHeader`
  - **Purpose**: Standardized bottom sheet header with title, optional subtitle, and ghost close button.
  - **Tokens**: `Gold` title, `TextSecondary` / `TextLightSecondary` subtitle.

### 6. Dialog Family (Powered by PopBox)
- **`PopBoxHost` & `AynvoraDialogHost`**
  - **Location**: `dev.ishant.popbox.PopBoxHost`, `com.aynvora.designsystem.components.dialogs.AynvoraDialogHost`
  - **Purpose**: Zero-boilerplate, stackable modal dialog container hosting globally managed dialogs.
  - **Library**: `PopBox` (Compose Multiplatform).
  - **Integration**: Placed at the root composition tree inside `AynvoraTheme`.
  - **Controller**: Driven by `LocalPopBoxController.current` (`show`, `dismiss`, `dismissAll`).

- **`AynvoraDialogDefaults`**
  - **Location**: `com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults`
  - **Purpose**: Pre-configured `PopBoxParams` applying AYNVORA Master UI Style Guide tokens.
  - **Tokens**: `CosmicNavy` container (dark) / `White` (light), `AynvoraShapes.cornerLarge` (16dp rounded corners), 24dp horizontal & content padding.

- **`AynvoraDialogHeader`**
  - **Location**: `com.aynvora.designsystem.components.dialogs.AynvoraDialogHeader`
  - **Purpose**: Standardized dialog header with title, optional subtitle, and ghost close button.
  - **Tokens**: `Gold` title, `TextSecondary` / `TextLightSecondary` subtitle.



