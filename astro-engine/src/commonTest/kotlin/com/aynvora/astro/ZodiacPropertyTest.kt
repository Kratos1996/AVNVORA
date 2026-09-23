package com.aynvora.astro

import com.aynvora.astro.math.AstroMath
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.zodiac.ZodiacCalculator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZodiacPropertyTest {

    @Test
    fun testAngularNormalizationProperties() {
        assertEquals(0.0, AstroMath.normalizeDegrees(0.0), 1e-10)
        assertEquals(0.0, AstroMath.normalizeDegrees(360.0), 1e-10)
        assertEquals(0.0, AstroMath.normalizeDegrees(720.0), 1e-10)
        assertEquals(359.0, AstroMath.normalizeDegrees(-1.0), 1e-10)
        assertEquals(180.0, AstroMath.normalizeDegrees(-180.0), 1e-10)
        assertEquals(270.0, AstroMath.normalizeDegrees(-90.0), 1e-10)
        assertEquals(45.0, AstroMath.normalizeDegrees(405.0), 1e-10)
    }

    @Test
    fun testRashiBoundaryEdgeCases() {
        // Aries: 0° .. <30°
        val r0 = ZodiacCalculator.calculateRashi(0.0)
        assertEquals(0, r0.index)
        assertEquals("Aries", r0.name)
        assertEquals(0.0, r0.degreeInRashi, 1e-10)

        val rAriesEnd = ZodiacCalculator.calculateRashi(29.999999)
        assertEquals(0, rAriesEnd.index)
        assertEquals(29.999999, rAriesEnd.degreeInRashi, 1e-6)

        // Taurus: 30° .. <60°
        val rTaurusStart = ZodiacCalculator.calculateRashi(30.0)
        assertEquals(1, rTaurusStart.index)
        assertEquals("Taurus", rTaurusStart.name)
        assertEquals(0.0, rTaurusStart.degreeInRashi, 1e-10)

        // Pisces: 330° .. <360°
        val rPiscesStart = ZodiacCalculator.calculateRashi(330.0)
        assertEquals(11, rPiscesStart.index)
        assertEquals("Pisces", rPiscesStart.name)
        assertEquals(0.0, rPiscesStart.degreeInRashi, 1e-10)

        val rPiscesEnd = ZodiacCalculator.calculateRashi(359.999999)
        assertEquals(11, rPiscesEnd.index)
        assertEquals(29.999999, rPiscesEnd.degreeInRashi, 1e-6)
    }

    @Test
    fun testNakshatraAndPadaBoundaryEdgeCases() {
        // Ashwini (0..13°20'): Pada 1 (0°..3°20')
        val n0 = ZodiacCalculator.calculateNakshatra(0.0)
        assertEquals(0, n0.index)
        assertEquals("Ashwini", n0.name)
        assertEquals(0.0, n0.degreeInNakshatra, 1e-10)
        assertEquals(1, n0.pada)

        // Ashwini: Pada 2 (3°20' .. 6°40')
        val nPada2 = ZodiacCalculator.calculateNakshatra(3.3333333333333335)
        assertEquals(0, nPada2.index)
        assertEquals(2, nPada2.pada)

        // Ashwini: Pada 3 (6°40' .. 10°00')
        val nPada3 = ZodiacCalculator.calculateNakshatra(6.666666666666667)
        assertEquals(0, nPada3.index)
        assertEquals(3, nPada3.pada)

        // Ashwini: Pada 4 (10°00' .. 13°20')
        val nPada4 = ZodiacCalculator.calculateNakshatra(10.0)
        assertEquals(0, nPada4.index)
        assertEquals(4, nPada4.pada)

        // Bharani (index 1): starts at 13°20' = 13.333333333333334°
        val nBharani = ZodiacCalculator.calculateNakshatra(13.333333333333334)
        assertEquals(1, nBharani.index)
        assertEquals("Bharani", nBharani.name)
        assertEquals(1, nBharani.pada)
        assertEquals(0.0, nBharani.degreeInNakshatra, 1e-10)

        // Revati (index 26): Pada 4 ends near 360°
        val nRevati = ZodiacCalculator.calculateNakshatra(359.999999)
        assertEquals(26, nRevati.index)
        assertEquals("Revati", nRevati.name)
        assertEquals(4, nRevati.pada)
    }

    @Test
    fun testRahuKetuOppositePropertyAcrossRandomEpochs() {
        // Across 100 arbitrary days over 100 years, Ketu must ALWAYS be exactly 180° opposite Rahu
        for (i in 0 until 100) {
            val jd = JulianDay(2440000.0 + i * 365.25)
            val nodes = LunarNodesCalculator.calculate(jd)

            val rahu = nodes.rahu.apparentLongitude
            val ketu = nodes.ketu.apparentLongitude

            val separation = AstroMath.normalizeDegrees(ketu - rahu)
            assertEquals(180.0, separation, 1e-9, "Ketu ($ketu) is not 180° opposite Rahu ($rahu) at JD $jd")
        }
    }

    @Test
    fun testTropicalToSiderealWraparound() {
        val ayanamsa = 24.0

        // Longitude 10° - 24° ayanamsa should wrap to 346°
        val sidereal = ZodiacCalculator.toSidereal(10.0, ayanamsa)
        assertEquals(346.0, sidereal, 1e-10)

        // Longitude 0° - 24° ayanamsa should wrap to 336°
        val sidereal0 = ZodiacCalculator.toSidereal(0.0, ayanamsa)
        assertEquals(336.0, sidereal0, 1e-10)

        // Longitude 30° - 24° = 6°
        val sidereal30 = ZodiacCalculator.toSidereal(30.0, ayanamsa)
        assertEquals(6.0, sidereal30, 1e-10)
    }
}
