package com.aynvora.core.wallpaper

import com.aynvora.core.result.AynvoraResult

/**
 * Production implementation of [WallpaperPromptBuilder].
 * Builds device-optimized, sacred geometry and Vedic prompts with explicit safe area guidance.
 */
class DefaultWallpaperPromptBuilder : WallpaperPromptBuilder {

    override fun buildPrompt(request: WallpaperRequest): AynvoraResult<WallpaperPrompt> {
        val rashiStr = request.rashi?.name ?: "COSMIC"
        val nakshatraStr = request.nakshatra?.name ?: "SACRED_STAR"

        val themeVisuals = when (request.theme) {
            WallpaperTheme.COSMIC_SACRED_GEOMETRY ->
                "ultra-high-resolution sacred geometry mandala, Sri Yantra resonance, golden ratio spirals, cosmic navy deep space backdrop"

            WallpaperTheme.MINIMAL_GOLD_VECTORS ->
                "minimalist luxury vector art, crisp 24k gold foil filigree lines, clean matte cosmic black negative space, uncluttered center"

            WallpaperTheme.VEDIC_MANDALA ->
                "traditional intricate Vedic temple ceiling mandala, Navagraha energy vortex, subtle radiant lotus petals in gold and celestial blue"

            WallpaperTheme.NEBULA_CONSTELLATION ->
                "ethereal deep galaxy nebula, shimmering constellation map of $nakshatraStr, glowing stellar dust, photorealistic astronomy"

            WallpaperTheme.NATURE_SPIRITUAL_ELEMENTS ->
                "tranquil sacred Himalayan morning dawn, golden sunlight reflecting on sacred waters, subtle Om vibration ripples"
        }

        val intentionNote =
            request.userIntentionOrGoal?.let { ", evoking serenity and focus for: $it" } ?: ""

        val prompt =
            "Vertical smartphone wallpaper lockscreen art (Aspect ratio ${request.deviceProfile.dimensions.aspectRatio}), " +
                    "$themeVisuals, astrological resonance of zodiac sign $rashiStr and lunar mansion $nakshatraStr$intentionNote. " +
                    "Composition designed with wide open negative space in the upper third (top ${request.deviceProfile.safeAreas.topPaddingPx}px) to ensure digital clock and notification clarity, " +
                    "and unobstructed bottom margin (${request.deviceProfile.safeAreas.bottomPaddingPx}px) for system swipe bar. " +
                    "Cinematic lighting, 8k resolution, serene, divine, elegant, masterpiece."

        val negativePrompt =
            "blurry, low resolution, crowded upper center, text, watermark, distorted geometry, harsh neon colors, chaotic artifacts, human face, realistic photographic people"

        val safeAreaGuidance =
            "Device: ${request.deviceProfile.deviceName} (${request.deviceProfile.dimensions.widthPx}x${request.deviceProfile.dimensions.heightPx}px). " +
                    "Clock Clearance: Upper ${request.deviceProfile.safeAreas.topPaddingPx}px. Bottom Swipe Clearance: ${request.deviceProfile.safeAreas.bottomPaddingPx}px."

        return AynvoraResult.Success(
            WallpaperPrompt(
                promptText = prompt,
                negativePromptText = negativePrompt,
                suggestedAspect = request.deviceProfile.dimensions.aspectRatio,
                safeAreaGuidance = safeAreaGuidance,
                themeName = request.theme.name,
            )
        )
    }
}
