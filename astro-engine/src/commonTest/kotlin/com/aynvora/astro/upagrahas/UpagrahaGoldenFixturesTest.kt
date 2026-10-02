package com.aynvora.astro.upagrahas

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class UpagrahaGoldenFixturesTest {

    @Test
    fun testMathematicalIdentity_UpaketuPlus30EqualsSun() {
        val testSunLongitudes = listOf(0.0, 15.5, 45.0, 90.0, 150.0, 210.0, 280.0, 359.5)

        for (sunLon in testSunLongitudes) {
            val result = UpagrahaEngine.calculate(
                sunLongitude = sunLon
            )

            val upaketu = result.upagrahas.single { it.name == "Upaketu" }
            val upaketuPlus30 = (upaketu.longitude + 30.0) % 360.0

            val diff = abs(upaketuPlus30 - sunLon)
            val normalizedDiff = if (diff > 180.0) 360.0 - diff else diff
            assertTrue(
                normalizedDiff < 1e-4,
                "Invariant violated: Upaketu ($upaketu) + 30° must equal Sun ($sunLon), diff=$normalizedDiff"
            )
        }
    }

    @Test
    fun testWrapAroundAtZodiacBoundaries() {
        // When Sun is near 350°, Dhuma wraps around 360°
        val result = UpagrahaEngine.calculate(
            sunLongitude = 350.0
        )

        for (u in result.upagrahas) {
            assertTrue(u.longitude >= 0.0 && u.longitude < 360.0, "Longitude out of bounds: ${u.name} = ${u.longitude}")
        }
    }
}
