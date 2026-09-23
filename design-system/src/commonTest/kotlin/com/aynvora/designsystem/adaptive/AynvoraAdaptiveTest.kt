package com.aynvora.designsystem.adaptive

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AynvoraAdaptiveTest {

    @Test
    fun windowSizeClassDetectionIsAccurateAcrossDevices() {
        // Small Mobile
        val smallPhone = calculateAynvoraWindowInfo(320.dp, 568.dp)
        assertEquals(AynvoraWindowWidthSizeClass.Compact, smallPhone.widthSizeClass)
        assertEquals(AynvoraDeviceType.Mobile, smallPhone.deviceType)
        assertTrue(smallPhone.isCompact)
        assertTrue(smallPhone.isMobile)

        // Standard Mobile
        val phone = calculateAynvoraWindowInfo(390.dp, 844.dp)
        assertEquals(AynvoraWindowWidthSizeClass.Compact, phone.widthSizeClass)
        assertEquals(AynvoraDeviceType.Mobile, phone.deviceType)

        // Foldable (unfolded)
        val foldable = calculateAynvoraWindowInfo(700.dp, 800.dp)
        assertEquals(AynvoraWindowWidthSizeClass.Medium, foldable.widthSizeClass)
        assertEquals(AynvoraDeviceType.Foldable, foldable.deviceType)
        assertTrue(foldable.isMedium)
        assertTrue(foldable.isFoldable)

        // Tablet
        val tablet = calculateAynvoraWindowInfo(1024.dp, 768.dp)
        assertEquals(AynvoraWindowWidthSizeClass.Expanded, tablet.widthSizeClass)
        assertEquals(AynvoraDeviceType.Tablet, tablet.deviceType)
        assertTrue(tablet.isExpanded)
        assertTrue(tablet.isTablet)

        // Desktop
        val desktop = calculateAynvoraWindowInfo(1440.dp, 900.dp)
        assertEquals(AynvoraWindowWidthSizeClass.Expanded, desktop.widthSizeClass)
        assertEquals(AynvoraDeviceType.Desktop, desktop.deviceType)
        assertTrue(desktop.isExpanded)
        assertTrue(desktop.isDesktop)
    }

    @Test
    fun scalableUnitsCalculationsAreBalancedAndClamped() {
        // Baseline 360dp gives exact 1.0 factor
        val factor360 = calculateScaleFactor(360.dp)
        assertEquals(1.0f, factor360)
        assertEquals(16.dp, calculateSdp(16f, 360.dp))
        assertEquals(14.sp, calculateSsp(14f, 360.dp))

        // Small phone (320dp) scales down gracefully
        val sdpSmall = calculateSdp(16f, 320.dp)
        assertTrue(sdpSmall.value < 16f)
        assertTrue(sdpSmall.value >= 16f * 0.80f)

        // Foldable (700dp) scales moderately without doubling
        val sdpFoldable = calculateSdp(16f, 700.dp)
        assertTrue(sdpFoldable.value in (16f * 1.10f)..(16f * 1.30f))

        // Large Desktop (1920dp) is strictly clamped to max factor (1.45f)
        val sdpDesktop = calculateSdp(16f, 1920.dp)
        assertEquals((16f * 1.45f).dp, sdpDesktop)
        val sspDesktop = calculateSsp(18f, 1920.dp)
        assertEquals((18f * 1.45f).sp, sspDesktop)
    }
}
