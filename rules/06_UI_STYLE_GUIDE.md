# AYNVORA Master UI Style Guide
Version: 1.3

## Colors & Adaptive Color Scheme
Gold #C9A227; Light Gold #E4C65A; Deep Gold #8F6F16.
Cosmic Black #080B14; Cosmic Navy #0D1224; Cosmic Indigo #151B38.
Ivory #FAF8F2; White #FFFFFF; Soft Gold #F7F1DE.
Celestial Blue #6B8CFF.
Functional: Success #2E8B67, Warning #D99A2B, Error #C94B4B, Info #4D7CFE.

### Adaptive Theme Tokens (`AynvoraColorScheme`)
Color resolution automatically selects appropriate light/dark tokens based on `AynvoraTheme.isDark`:
- `AynvoraTheme.colors.textPrimary` (Light text in dark mode, dark text in light mode)
- `AynvoraTheme.colors.textSecondary` (Secondary text token)
- `AynvoraTheme.colors.textMuted` (Muted/disabled text token)
- `AynvoraTheme.colors.background` (Cosmic Black in dark mode, Ivory in light mode)
- `AynvoraTheme.colors.surfacePrimary` (Cosmic Navy in dark mode, White in light mode)
- `AynvoraTheme.colors.surfaceSecondary` (Cosmic Indigo in dark mode, Soft Gold in light mode)

## Typography Components (Mandatory)
Cormorant Garamond for display/brand moments; Inter for product/data; Noto Sans family for Indian scripts.
Scale: 40, 36, 32, 28, 24, 20, 18, 16, 14, 12, 11sp.

**Mandatory Typography Usage Rule**:
Raw `Text(text = ..., style = ...)` with inline `.copy(fontSize = ...)` is strictly prohibited in UI screens.
All text must be rendered using standardized design system components from `com.aynvora.designsystem.components`:
- `AynvoraDisplay` (40sp / 36sp Display)
- `AynvoraTitle`, `AynvoraTitleMedium`, `AynvoraTitleSmall` (20sp / 18sp / 16sp Title)
- `AynvoraRegularText`, `AynvoraRegularMedium`, `AynvoraRegularBold`, `AynvoraRegularExtraBold`, `AynvoraRegularUnderline` (16sp Body)
- `AynvoraSmallText`, `AynvoraSmallMedium`, `AynvoraSmallBold` (14sp Small)
- `AynvoraExtraSmallText`, `AynvoraExtraSmallBold` (12sp Caption)

## Form Input Components (Mandatory)
- Free text inputs MUST use `AynvoraEditText`.
- Read-only date, time, and location picker fields MUST use `AynvoraReadOnlyField`.
- Picker fields MUST NOT open soft keyboards or show text cursors. Tapping anywhere on the field opens the corresponding modal dialog or bottom sheet.

## Buttons (Mandatory)
Primary, secondary, outlined, and ghost actions MUST consume standard button components:
- `AynvoraPrimaryButton` & `AynvoraPrimaryButtonSmall`
- `AynvoraSecondaryButton`
- `AynvoraOutlinedButton`
- `AynvoraGhostButton`

## Spacing
4, 8, 12, 16, 20, 24, 32, 40, 48, 64dp.

## Radius
4, 8, 12, 16, 24, pill.

## Motion
0, 120, 200, 300, 450, 700ms. Default interaction: 200ms.

## Responsive Form Factors & Device Adaptation (Mandatory)
AYNVORA interfaces must natively adapt to all device form factors with dedicated layout paradigms:

1. **Mobile (Compact Width < 600dp)**:
   - **Paradigm**: Focused, single-column vertical scroll.
   - **Goal**: Optimized for single-hand use, clear touch targets (>= 48dp), and readable stacked cards.

2. **Foldables (Medium Width 600dp – 839dp)**:
   - **Paradigm**: Dynamic dual-pane / side-by-side transition when unfolded.
   - **Goal**: Seamlessly display complementary data (e.g., list on left, details on right) without requiring deep navigation stacks.

3. **Tablets & Desktop (Expanded Width >= 840dp / Ultra-wide >= 1200dp)**:
   - **Paradigm**: **Information-Dense Multi-Column Master-Detail & Grid Layouts**.
   - **Goal**: **NEVER** stretch mobile layouts across desktop viewports. Desktop UI must look and feel like a native desktop application—displaying more data simultaneously on a single screen (e.g., sidebar navigation rails, multi-column card grids, simultaneous chart rendering alongside tabular planetary positions and commentary).

### Scalable Units (sdp & ssp)
To preserve visual balance across device densities and screen widths without stretching or distortion:
- **`sdp` (Scalable dp)**: Relative dimension scaling based on standard 360dp mobile baseline, with proportional clamping on foldables, tablets, and desktop.
- **`ssp` (Scalable sp)**: Relative typography scaling that adjusts harmoniously with device width while respecting user accessibility text size settings.

## Mandatory Screen Verification Rule
Whenever creating or updating any screen or component, the UI must be verified across:
1. Compact Phone (360dp baseline)
2. Foldable Device (unfolded 600dp–720dp)
3. Tablet (840dp–1024dp)
4. Desktop window (>= 1200dp)
Single-column phone UI must never be stretched unconstrained across tablet or desktop viewports.

## Components
Approved component families:
AynvoraButton (Primary, Secondary, Outlined, Ghost), AynvoraCard, AynvoraEditText, AynvoraReadOnlyField, AynvoraChip, AynvoraTabs, AynvoraDialog, AynvoraBottomSheet, AynvoraList, AynvoraDesktopSidebar, AynvoraChart, AynvoraDesktopDataTable, Report.

## Accessibility
48dp minimum interactive target; support dynamic font scaling, semantics, keyboard focus, contrast, reduced motion, and non-color state communication.

## Astrology UI
Chart data and rendering are separate. Support North Indian, South Indian and Western renderers without duplicating calculation logic.
