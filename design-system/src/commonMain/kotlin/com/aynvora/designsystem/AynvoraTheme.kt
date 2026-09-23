package com.aynvora.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.aynvora.designsystem.adaptive.AynvoraAdaptiveProvider
import com.aynvora.designsystem.adaptive.AynvoraWindowInfo
import com.aynvora.designsystem.adaptive.LocalAynvoraWindowInfo

/**
 * AYNVORA Color Palette (Master UI Style Guide v1.0).
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

val LocalAynvoraColors = staticCompositionLocalOf { AynvoraColors }
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

    val colors: AynvoraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraColors.current

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

    AynvoraAdaptiveProvider {
        CompositionLocalProvider(
            LocalAynvoraDarkTheme provides darkTheme,
            LocalAynvoraColors provides AynvoraColors,
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

