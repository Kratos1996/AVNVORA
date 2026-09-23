package com.aynvora.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * AYNVORA Typography Specification (Master UI Style Guide v1.0).
 *
 * Font Roles:
 * - Brand / Display: Cormorant Garamond (Serif fallback)
 * - Product / Data: Inter (SansSerif fallback)
 * - Indian Scripts: Noto Sans family
 *
 * Exact Scale:
 * 40, 36, 32, 28, 24, 20, 18, 16, 14, 12, 11 sp.
 */
@Immutable
data class AynvoraTypography(
    val display40: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
    ),
    val display36: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp,
    ),
    val display32: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    val headline28: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    val headline24: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    val title20: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    val title18: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    val body16: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    val body14: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    val caption12: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    val overline11: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
) {
    /**
     * Maps AYNVORA typography tokens directly into Material 3 Typography.
     */
    fun toMaterial3(): Typography = Typography(
        displayLarge = display40,
        displayMedium = display36,
        displaySmall = display32,
        headlineLarge = headline28,
        headlineMedium = headline24,
        headlineSmall = title20,
        titleLarge = title20,
        titleMedium = title18,
        titleSmall = body16.copy(fontWeight = FontWeight.Medium),
        bodyLarge = body16,
        bodyMedium = body14,
        bodySmall = caption12,
        labelLarge = body14.copy(fontWeight = FontWeight.Medium),
        labelMedium = caption12.copy(fontWeight = FontWeight.Medium),
        labelSmall = overline11,
    )
}

val DefaultAynvoraTypography = AynvoraTypography()
