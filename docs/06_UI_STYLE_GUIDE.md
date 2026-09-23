# AYNVORA Master UI Style Guide
Version: 1.1

## Colors
Gold #C9A227; Light Gold #E4C65A; Deep Gold #8F6F16.
Cosmic Black #080B14; Cosmic Navy #0D1224; Cosmic Indigo #151B38.
Ivory #FAF8F2; White #FFFFFF; Soft Gold #F7F1DE.
Celestial Blue #6B8CFF.
Functional: Success #2E8B67, Warning #D99A2B, Error #C94B4B, Info #4D7CFE.

## Typography
Cormorant Garamond for display/brand moments.
Inter for product/data.
Noto Sans family for Indian scripts.
Scale: 40, 36, 32, 28, 24, 20, 18, 16, 14, 12, 11sp.

## Spacing
4, 8, 12, 16, 20, 24, 32, 40, 48, 64dp.

## Radius
4, 8, 12, 16, 24, pill.

## Motion
0, 120, 200, 300, 450, 700ms. Default interaction: 200ms.

## Responsive Form Factors & Device Adaptation
AYNVORA interfaces must natively adapt to all device form factors:
- **Small / Compact Phone**: Width < 360dp. Maintain strict 48dp touch targets, avoid horizontal clipping.
- **Standard Mobile**: Width 360dp - 599dp (Compact Width). Primary single-column layouts with optimized vertical scroll.
- **Foldables (Folded & Unfolded)**: Width 600dp - 839dp (Medium Width). Support responsive dual-pane or side-by-side arrangement when unfolded; adapt smoothly to posture changes.
- **Tablets**: Width 840dp - 1199dp (Expanded Width). Utilize multi-pane master-detail structures, side navigation rails, and balanced content margins.
- **Desktop**: Width >= 1200dp (Large / Ultra-wide). Utilize persistent navigation, multi-column cards/panes, and maximum content width boundaries.

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
Button, Card, TextField, Chip, Tabs, Dialog, BottomSheet, List, Navigation, Chart, Planet, Zodiac, Dasha, Panchang, Report.

## Accessibility
48dp minimum interactive target; support dynamic font scaling, semantics, keyboard focus, contrast, reduced motion, and non-color state communication.

## Astrology UI
Chart data and rendering are separate. Support North Indian, South Indian and Western renderers without duplicating calculation logic.
