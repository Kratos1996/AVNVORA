package com.aynvora.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.aynvora.designsystem.adaptive.AynvoraAdaptiveProvider
import com.aynvora.designsystem.adaptive.AynvoraWindowInfo
import com.aynvora.designsystem.adaptive.LocalAynvoraWindowInfo

/**
 * Static AYNVORA Color Palette Constants.
 */
object AynvoraColors {
    // Primary / Brand Gold
    val Gold = Color(0xFFC9A227)
    val GoldLight = Color(0xFFE4C65A)
    val GoldDeep = Color(0xFF8F6F16)

    // Dark / Cosmic Foundations
    val CosmicBlack = Color(0xFF080B14)
    val CosmicNavy = Color(0xFF0D1224)
    val CosmicIndigo = Color(0xFF151B38)

    // Light / Neutral Foundations
    val Ivory = Color(0xFFFAF8F2)
    val White = Color(0xFFFFFFFF)
    val SoftGold = Color(0xFFF7F1DE)

    // Accent
    val CelestialBlue = Color(0xFF6B8CFF)

    // Typography & Content Colors (Light Backgrounds)
    val TextDark = Color(0xFF171A21)
    val TextSecondary = Color(0xFF555B66)
    val TextMuted = Color(0xFF7A808A)

    // Typography & Content Colors (Dark Backgrounds)
    val TextLight = Color(0xFFF5F2E9)
    val TextLightSecondary = Color(0xFFB9BECA)
    val TextLightMuted = Color(0xFF7F8798)

    // Functional State Colors
    val Success = Color(0xFF2E8B67)
    val Warning = Color(0xFFD99A2B)
    val Error = Color(0xFFC94B4B)
    val Info = Color(0xFF4D7CFE)
}

/**
 * Dynamic, adaptive color scheme that automatically resolves primary text, secondary text,
 * surfaces, backgrounds, and borders based on dark/light theme mode.
 */
@Immutable
data class AynvoraColorScheme(
    val isDark: Boolean = true,
    val textPrimary: Color = if (isDark) AynvoraColors.TextLight else AynvoraColors.TextDark,
    val textSecondary: Color = if (isDark) AynvoraColors.TextLightSecondary else AynvoraColors.TextSecondary,
    val textMuted: Color = if (isDark) AynvoraColors.TextLightMuted else AynvoraColors.TextMuted,
    val background: Color = if (isDark) AynvoraColors.CosmicBlack else AynvoraColors.Ivory,
    val surfacePrimary: Color = if (isDark) AynvoraColors.CosmicNavy else AynvoraColors.White,
    val surfaceSecondary: Color = if (isDark) AynvoraColors.CosmicIndigo else AynvoraColors.SoftGold,
    val border: Color = if (isDark) AynvoraColors.CosmicIndigo else AynvoraColors.Gold.copy(alpha = 0.35f),
    val selectedGold: Color =  AynvoraColors.Gold.copy(alpha = if (isDark) 0.15f else 0.22f),

    // Backward-compatible static aliases
    val TextDark: Color = AynvoraColors.TextDark,
    @get:kotlin.jvm.JvmName("getLegacyTextSecondary")
    val TextSecondary: Color = AynvoraColors.TextSecondary,
    @get:kotlin.jvm.JvmName("getLegacyTextMuted")
    val TextMuted: Color = AynvoraColors.TextMuted,
    val TextLight: Color = AynvoraColors.TextLight,
    val TextLightSecondary: Color = AynvoraColors.TextLightSecondary,
    val TextLightMuted: Color = AynvoraColors.TextLightMuted,
    val CosmicBlack: Color = AynvoraColors.CosmicBlack,
    val CosmicNavy: Color = AynvoraColors.CosmicNavy,
    val CosmicIndigo: Color = AynvoraColors.CosmicIndigo,
    val Ivory: Color = AynvoraColors.Ivory,
    val White: Color = AynvoraColors.White,
    val SoftGold: Color = AynvoraColors.SoftGold,
    val CelestialBlue: Color = AynvoraColors.CelestialBlue,
    val Success: Color = AynvoraColors.Success,
    val Warning: Color = AynvoraColors.Warning,
    val Error: Color = AynvoraColors.Error,
    val Info: Color = AynvoraColors.Info,
    val Gold: Color = AynvoraColors.Gold,
    val GoldLight: Color = AynvoraColors.GoldLight,
    val GoldDeep: Color = AynvoraColors.GoldDeep,
)

val LocalAynvoraColorScheme = staticCompositionLocalOf { AynvoraColorScheme(isDark = true) }
val LocalAynvoraDarkTheme = staticCompositionLocalOf { true }
val LocalAynvoraTypography = staticCompositionLocalOf { DefaultAynvoraTypography }
val LocalAynvoraSpacing = staticCompositionLocalOf { AynvoraSpacing }
val LocalAynvoraShapes = staticCompositionLocalOf { AynvoraShapes }
val LocalAynvoraMotion = staticCompositionLocalOf { AynvoraMotion }
val LocalAynvoraElevation = staticCompositionLocalOf { AynvoraElevation }

private val LightScheme = lightColorScheme(
    primary = AynvoraColors.GoldDeep,
    onPrimary = AynvoraColors.White,
    primaryContainer = AynvoraColors.SoftGold,
    onPrimaryContainer = AynvoraColors.GoldDeep,
    secondary = AynvoraColors.CelestialBlue,
    onSecondary = AynvoraColors.White,
    background = AynvoraColors.Ivory,
    onBackground = AynvoraColors.TextDark,
    surface = AynvoraColors.White,
    onSurface = AynvoraColors.TextDark,
    surfaceVariant = AynvoraColors.SoftGold,
    onSurfaceVariant = AynvoraColors.TextSecondary,
    outline = AynvoraColors.SoftGold,
    error = AynvoraColors.Error,
    onError = AynvoraColors.White,
)

private val DarkScheme = darkColorScheme(
    primary = AynvoraColors.GoldLight,
    onPrimary = AynvoraColors.CosmicBlack,
    primaryContainer = AynvoraColors.CosmicIndigo,
    onPrimaryContainer = AynvoraColors.GoldLight,
    secondary = AynvoraColors.CelestialBlue,
    onSecondary = AynvoraColors.White,
    background = AynvoraColors.CosmicBlack,
    onBackground = AynvoraColors.TextLight,
    surface = AynvoraColors.CosmicNavy,
    onSurface = AynvoraColors.TextLight,
    surfaceVariant = AynvoraColors.CosmicIndigo,
    onSurfaceVariant = AynvoraColors.TextLightSecondary,
    outline = AynvoraColors.CosmicIndigo,
    error = AynvoraColors.Error,
    onError = AynvoraColors.White,
)

/**
 * Accessor object for AYNVORA Design System tokens and adaptive window state.
 */
object AynvoraTheme {
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraDarkTheme.current

    val colors: AynvoraColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraColorScheme.current

    val typography: AynvoraTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraTypography.current

    val spacing: AynvoraSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraSpacing.current

    val shapes: AynvoraShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraShapes.current

    val motion: AynvoraMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraMotion.current

    val elevation: AynvoraElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraElevation.current

    val window: AynvoraWindowInfo
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraWindowInfo.current
}

/**
 * Root AYNVORA Theme provider integrating adaptive device layout calculation.
 */
@Composable
fun AynvoraTheme(
    darkTheme: Boolean = true,
    typography: AynvoraTypography = DefaultAynvoraTypography,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val aynvoraColorScheme = AynvoraColorScheme(isDark = darkTheme)

    AynvoraAdaptiveProvider {
        CompositionLocalProvider(
            LocalAynvoraDarkTheme provides darkTheme,
            LocalAynvoraColorScheme provides aynvoraColorScheme,
            LocalAynvoraTypography provides typography,
            LocalAynvoraSpacing provides AynvoraSpacing,
            LocalAynvoraShapes provides AynvoraShapes,
            LocalAynvoraMotion provides AynvoraMotion,
            LocalAynvoraElevation provides AynvoraElevation,
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = typography.toMaterial3(),
                content = content,
            )
        }
    }
}
