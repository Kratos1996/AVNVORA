package com.aynvora.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class AynvoraThemeTest {

    @Test
    fun colorPaletteValuesAreValid() {
        assertEquals(Color(0xFFC9A227), AynvoraColors.Gold)
        assertEquals(Color(0xFFE4C65A), AynvoraColors.GoldLight)
        assertEquals(Color(0xFF8F6F16), AynvoraColors.GoldDeep)
        assertEquals(Color(0xFF080B14), AynvoraColors.CosmicBlack)
        assertEquals(Color(0xFF0D1224), AynvoraColors.CosmicNavy)
        assertEquals(Color(0xFF151B38), AynvoraColors.CosmicIndigo)
        assertEquals(Color(0xFFFAF8F2), AynvoraColors.Ivory)
        assertEquals(Color(0xFF6B8CFF), AynvoraColors.CelestialBlue)
        assertNotEquals(AynvoraColors.CosmicBlack, AynvoraColors.Ivory)
    }

    @Test
    fun typographyScaleMatchesStyleGuide() {
        val typography = DefaultAynvoraTypography
        assertEquals(40.sp, typography.display40.fontSize)
        assertEquals(36.sp, typography.display36.fontSize)
        assertEquals(32.sp, typography.display32.fontSize)
        assertEquals(28.sp, typography.headline28.fontSize)
        assertEquals(24.sp, typography.headline24.fontSize)
        assertEquals(20.sp, typography.title20.fontSize)
        assertEquals(18.sp, typography.title18.fontSize)
        assertEquals(16.sp, typography.body16.fontSize)
        assertEquals(14.sp, typography.body14.fontSize)
        assertEquals(12.sp, typography.caption12.fontSize)
        assertEquals(11.sp, typography.overline11.fontSize)

        val m3 = typography.toMaterial3()
        assertNotNull(m3.displayLarge)
        assertEquals(40.sp, m3.displayLarge.fontSize)
    }

    @Test
    fun spacingTokensMatchStyleGuide() {
        assertEquals(4.dp, AynvoraSpacing.space4)
        assertEquals(8.dp, AynvoraSpacing.space8)
        assertEquals(12.dp, AynvoraSpacing.space12)
        assertEquals(16.dp, AynvoraSpacing.space16)
        assertEquals(20.dp, AynvoraSpacing.space20)
        assertEquals(24.dp, AynvoraSpacing.space24)
        assertEquals(32.dp, AynvoraSpacing.space32)
        assertEquals(40.dp, AynvoraSpacing.space40)
        assertEquals(48.dp, AynvoraSpacing.space48)
        assertEquals(64.dp, AynvoraSpacing.space64)
    }

    @Test
    fun shapeAndMotionTokensAreValid() {
        assertEquals(4.dp, AynvoraShapes.radius4)
        assertEquals(8.dp, AynvoraShapes.radius8)
        assertEquals(12.dp, AynvoraShapes.radius12)
        assertEquals(16.dp, AynvoraShapes.radius16)
        assertEquals(24.dp, AynvoraShapes.radius24)

        assertEquals(0, AynvoraMotion.durationInstant)
        assertEquals(120, AynvoraMotion.durationFast)
        assertEquals(200, AynvoraMotion.durationDefault)
        assertEquals(300, AynvoraMotion.durationMedium)
        assertEquals(450, AynvoraMotion.durationSlow)
        assertEquals(700, AynvoraMotion.durationDeliberate)
    }
}
