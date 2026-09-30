# Gemstone image asset sync

## Result

Connected the nine existing images in `/Users/ishant/Desktop/gemstone` to the nine canonical Gemstone catalog entries. No replacement, generated, or stock images were added. The unmatched `white_coral_stone.webp` was left out because the catalog has no white coral entry.

| Catalog ID | Packaged drawable | Existing source file |
| --- | --- | --- |
| `gem_ruby` | `gem_ruby.webp` | `ruby_manik_img.webp` |
| `gem_pearl` | `gem_natural_pearl.webp` | `pearl_moti_img.webp` |
| `gem_red_coral` | `gem_red_coral.webp` | `red_coral_moonga_stone.webp` |
| `gem_emerald` | `gem_emerald.jpg` | `emerald_panna_img.jpg` |
| `gem_yellow_sapphire` | `gem_yellow_sapphire.webp` | `yellow_sapphire_pukhraj_stone_img.webp` |
| `gem_diamond` | `gem_diamond.jpg` | `diamond_img.jpg` |
| `gem_blue_sapphire` | `gem_blue_sapphire.jpg` | `blue_sapphire_neelam_stone_img.jpg` |
| `gem_hessonite` | `gem_hessonite.webp` | `hessonite_gomed_stone_img.webp` |
| `gem_cats_eye` | `gem_cats_eye.webp` | `cat_eye_lehsunia_stone_img.webp` |

The mapping is implemented in `ui/src/commonMain/kotlin/com/aynvora/ui/gemstone/GemstoneImageCatalog.kt`. Shared Compose resources and the provenance manifest are under `design-system/src/commonMain/composeResources`; equivalent Android assets are mirrored under `androidApp/src/main/assets/composeResources/com.aynvora.designsystem.generated.resources/` to match this repository's Android resource loading setup. Images now appear in the gemstone mandala, catalog rows, detail hero, recommendation cards, and inventory/compatibility choices. Accessibility descriptions use localized gemstone names in all 11 supported locales.

## Provenance

The source folder provides category landing-page URLs, but does not establish the individual image URLs, creators, or reuse licenses. The manifest records this uncertainty as `NOT_VERIFIED` and leaves individual image source URLs blank. The diamond image's EXIF metadata describes it as a 3D image and names `AnatolyM`; that does not independently establish authorship or reuse rights. The assets were used because the user directed that the existing images be used.

Manifest: `design-system/src/commonMain/composeResources/files/gemstone_image_manifest.json`.

## Verification

- `./gradlew :ui:jvmTest :aynvora-localization:jvmTest :desktopApp:assemble :androidApp:assembleDebug` — passed.
- Inspected the Android debug APK and confirmed all nine drawables plus the manifest are packaged.
- Inspected `design-system/build/libs/design-system-jvm.jar` and confirmed all nine drawables plus the manifest are packaged for Desktop.
- `./gradlew :desktopApp:run` — launched successfully and reported `AynvoraGita: Bhagavad Gita successfully seeded on Desktop JVM.` The available UI automation surface exposed no native app window, so I could not visually inspect the rendered Gemstone screen.
- `git diff --check` — passed.
