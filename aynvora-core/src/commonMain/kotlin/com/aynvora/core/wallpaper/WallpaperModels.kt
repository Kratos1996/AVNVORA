package com.aynvora.core.wallpaper

import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.result.AynvoraResult

/**
 * Screen dimensions in physical pixels for wallpaper generation.
 */
data class ScreenDimensions(
    val widthPx: Int,
    val heightPx: Int,
    val aspectRatio: String = "9:16",
)

/**
 * Screen safe areas to prevent clock / icon obstruction.
 */
data class SafeAreas(
    val topPaddingPx: Int,
    val bottomPaddingPx: Int,
    val centerClearanceRequired: Boolean = true,
)

/**
 * Hardware profile of the target device.
 */
data class DeviceProfile(
    val deviceName: String,
    val dimensions: ScreenDimensions,
    val safeAreas: SafeAreas,
)

/**
 * Artistic visual theme for the wallpaper.
 */
enum class WallpaperTheme {
    COSMIC_SACRED_GEOMETRY,
    MINIMAL_GOLD_VECTORS,
    VEDIC_MANDALA,
    NEBULA_CONSTELLATION,
    NATURE_SPIRITUAL_ELEMENTS,
}

/**
 * Request payload to build a personalized wallpaper prompt.
 */
data class WallpaperRequest(
    val rashi: Rashi?,
    val nakshatra: Nakshatra?,
    val theme: WallpaperTheme,
    val userIntentionOrGoal: String? = null,
    val deviceProfile: DeviceProfile,
    val preferredLanguage: String = "en",
)

/**
 * High-fidelity prompt crafted for user-authorized external generator handoff.
 */
data class WallpaperPrompt(
    val promptText: String,
    val negativePromptText: String,
    val suggestedAspect: String,
    val safeAreaGuidance: String,
    val themeName: String,
)

/**
 * Domain service contract for Wallpaper Studio.
 */
interface WallpaperPromptBuilder {
    fun buildPrompt(request: WallpaperRequest): AynvoraResult<WallpaperPrompt>
}
