package com.aynvora.designsystem.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard window width size classes based on Material Design 3 and AYNVORA specifications.
 */
enum class AynvoraWindowWidthSizeClass {
    /**
     * Mobile phones in portrait (< 600dp).
     */
    Compact,

    /**
     * Foldables (unfolded), small tablets, and phones in landscape (600dp - 839dp).
     */
    Medium,

    /**
     * Tablets, desktop windows, and large unfolded screens (>= 840dp).
     */
    Expanded,
}

/**
 * Targeted device form factor categories.
 */
enum class AynvoraDeviceType {
    /**
     * Standard mobile phone (< 600dp width).
     */
    Mobile,

    /**
     * Foldable device (600dp - 839dp width).
     */
    Foldable,

    /**
     * Tablet device (840dp - 1199dp width).
     */
    Tablet,

    /**
     * Desktop window or large display (>= 1200dp width).
     */
    Desktop,
}

/**
 * Current window dimensions, size classes, and device categorization.
 */
@Immutable
data class AynvoraWindowInfo(
    val width: Dp,
    val height: Dp,
    val widthSizeClass: AynvoraWindowWidthSizeClass,
    val deviceType: AynvoraDeviceType,
) {
    val isCompact: Boolean get() = widthSizeClass == AynvoraWindowWidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == AynvoraWindowWidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == AynvoraWindowWidthSizeClass.Expanded

    val isMobile: Boolean get() = deviceType == AynvoraDeviceType.Mobile
    val isFoldable: Boolean get() = deviceType == AynvoraDeviceType.Foldable
    val isTablet: Boolean get() = deviceType == AynvoraDeviceType.Tablet
    val isDesktop: Boolean get() = deviceType == AynvoraDeviceType.Desktop
}

/**
 * Pure calculation mapping width and height to [AynvoraWindowInfo].
 */
fun calculateAynvoraWindowInfo(width: Dp, height: Dp): AynvoraWindowInfo {
    val widthSizeClass = when {
        width < 600.dp -> AynvoraWindowWidthSizeClass.Compact
        width < 840.dp -> AynvoraWindowWidthSizeClass.Medium
        else -> AynvoraWindowWidthSizeClass.Expanded
    }

    val deviceType = when {
        width < 600.dp -> AynvoraDeviceType.Mobile
        width < 840.dp -> AynvoraDeviceType.Foldable
        width < 1200.dp -> AynvoraDeviceType.Tablet
        else -> AynvoraDeviceType.Desktop
    }

    return AynvoraWindowInfo(
        width = width,
        height = height,
        widthSizeClass = widthSizeClass,
        deviceType = deviceType,
    )
}

val DefaultAynvoraWindowInfo = AynvoraWindowInfo(
    width = 360.dp,
    height = 640.dp,
    widthSizeClass = AynvoraWindowWidthSizeClass.Compact,
    deviceType = AynvoraDeviceType.Mobile,
)

val LocalAynvoraWindowInfo = staticCompositionLocalOf { DefaultAynvoraWindowInfo }

/**
 * Accessor for current [AynvoraWindowInfo].
 */
object AynvoraWindow {
    val info: AynvoraWindowInfo
        @Composable
        @ReadOnlyComposable
        get() = LocalAynvoraWindowInfo.current
}

/**
 * Root adaptive container that computes [AynvoraWindowInfo] using [BoxWithConstraints]
 * and provides it to all child composables via [LocalAynvoraWindowInfo].
 */
@Composable
fun AynvoraAdaptiveProvider(
    modifier: Modifier = Modifier,
    content: @Composable (AynvoraWindowInfo) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val windowInfo = calculateAynvoraWindowInfo(maxWidth, maxHeight)
        CompositionLocalProvider(LocalAynvoraWindowInfo provides windowInfo) {
            content(windowInfo)
        }
    }
}
