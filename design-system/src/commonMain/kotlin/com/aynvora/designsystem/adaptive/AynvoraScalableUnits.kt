package com.aynvora.designsystem.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.jvm.JvmName

/**
 * Baseline mobile viewport width in dp for scaling calculations.
 */
const val BASELINE_WIDTH_DP = 360f

/**
 * Calculates the adaptive scale factor relative to [BASELINE_WIDTH_DP],
 * applying calibrated clamping to prevent visual distortion on foldables,
 * tablets, and desktops.
 */
fun calculateScaleFactor(windowWidth: Dp): Float {
    val rawRatio = windowWidth.value / BASELINE_WIDTH_DP
    return when {
        windowWidth < 600.dp -> rawRatio.coerceIn(0.80f, 1.25f)
        windowWidth < 840.dp -> rawRatio.coerceIn(1.10f, 1.30f)
        windowWidth < 1200.dp -> rawRatio.coerceIn(1.15f, 1.40f)
        else -> rawRatio.coerceIn(1.15f, 1.45f)
    }
}

/**
 * Calculates scalable dp (sdp) from a base numeric value and window width.
 */
fun calculateSdp(baseValue: Float, windowWidth: Dp): Dp {
    val factor = calculateScaleFactor(windowWidth)
    return (baseValue * factor).dp
}

/**
 * Calculates scalable sp (ssp) from a base numeric value and window width.
 */
fun calculateSsp(baseValue: Float, windowWidth: Dp): TextUnit {
    val factor = calculateScaleFactor(windowWidth)
    return (baseValue * factor).sp
}

// ----------------------------------------------------------------------------
// Scalable DP (sdp) Extensions
// ----------------------------------------------------------------------------

val Int.sdp: Dp
    @Composable
    @ReadOnlyComposable
    get() = calculateSdp(this.toFloat(), LocalAynvoraWindowInfo.current.width)

val Dp.sdp: Dp
    @JvmName("getDpSdp")
    @Composable
    @ReadOnlyComposable
    get() = calculateSdp(this.value, LocalAynvoraWindowInfo.current.width)

val Double.sdp: Dp
    @JvmName("getDoubleSdp")
    @Composable
    @ReadOnlyComposable
    get() = calculateSdp(this.toFloat(), LocalAynvoraWindowInfo.current.width)

// ----------------------------------------------------------------------------
// Scalable SP (ssp) Extensions
// ----------------------------------------------------------------------------

val Int.ssp: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = calculateSsp(this.toFloat(), LocalAynvoraWindowInfo.current.width)

val TextUnit.ssp: TextUnit
    @JvmName("getTextUnitSsp")
    @Composable
    @ReadOnlyComposable
    get() = calculateSsp(this.value, LocalAynvoraWindowInfo.current.width)

val Double.ssp: TextUnit
    @JvmName("getDoubleSsp")
    @Composable
    @ReadOnlyComposable
    get() = calculateSsp(this.toFloat(), LocalAynvoraWindowInfo.current.width)
