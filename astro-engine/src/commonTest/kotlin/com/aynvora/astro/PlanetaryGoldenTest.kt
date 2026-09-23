package com.aynvora.astro

import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.ayanamsa.LahiriAyanamsaCalculator
import com.aynvora.astro.ayanamsa.TropicalAyanamsaCalculator
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.PlanetaryCalculator
import com.aynvora.astro.planets.PlanetaryCalculator.Planet
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.time.JulianDay
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlanetaryGoldenTest {

    private val j2000 = JulianDay(JulianDay.J2000_JD) // 2000 Jan 1, 12h UT

    @Test
    fun testSunMeeusGoldenExample() {
        // Meeus "Astronomical Algorithms", 2nd Ed., Example 25.a:
        // 1992 October 13, 0h TD -> JD 2448908.5
        val jd1992 = JulianDay(2448908.5)
        val sun = SunCalculator.calculate(jd1992)

        // Meeus values:
        // Geometric mean longitude L0 = 201.807°
        // Equation of center C = -1.897°
        // True longitude = 199.910°
        // Apparent longitude = 199.908°
        assertEquals(199.908, sun.apparentLongitude, 0.01)
        assertEquals(201.807, sun.geometricMeanLongitude, 0.01)
        assertEquals(-1.897, sun.equationOfCenter, 0.01)
        assertTrue(sun.dailyMotionDegrees > 0.0) // Direct motion
    }

    @Test
    fun testSunAtJ2000Epoch() {
        val sun = SunCalculator.calculate(j2000)
        // At J2000.0 (Jan 1, 12h UT), Sun tropical longitude is ~280.46° (in Capricorn)
        assertEquals(280.46, sun.apparentLongitude, 0.1)
        assertTrue(sun.dailyMotionDegrees > 0.95 && sun.dailyMotionDegrees < 1.05)
    }

    @Test
    fun testMoonAtJ2000Epoch() {
        val moon = MoonCalculator.calculate(j2000)
        // At J2000.0, Moon mean longitude Lp is ~218.32°
        assertEquals(218.32, moon.meanLongitude, 0.1)
        // Apparent longitude with perturbations
        assertTrue(moon.apparentLongitude in 0.0..<360.0)
        assertTrue(moon.dailyMotionDegrees > 11.0 && moon.dailyMotionDegrees < 16.0)
    }

    @Test
    fun testLunarNodesAtJ2000Epoch() {
        val nodes = LunarNodesCalculator.calculate(j2000)

        // At J2000.0, Mean Rahu is at ~125.04° (Cancer)
        assertEquals(125.04, nodes.rahu.apparentLongitude, 0.1)

        // Ketu is exactly 180° opposite Rahu: (125.04 + 180) = 305.04° (Aquarius)
        assertEquals(305.04, nodes.ketu.apparentLongitude, 0.1)

        val separation = abs(nodes.ketu.apparentLongitude - nodes.rahu.apparentLongitude)
        assertEquals(180.0, separation, 1e-10)

        // Nodes must be retrograde
        assertTrue(nodes.rahu.isRetrograde)
        assertTrue(nodes.ketu.isRetrograde)
        assertTrue(nodes.rahu.dailyMotionDegrees < 0.0)
    }

    @Test
    fun testLahiriAyanamsaAtJ2000() {
        val ayanamsa = LahiriAyanamsaCalculator.calculate(j2000)
        // Standard Lahiri at J2000.0 is 23° 51' 25.53" = 23.857092°
        assertEquals(23.857092, ayanamsa, 0.0001)

        // Tropical Ayanamsa is 0.0
        val tropical = TropicalAyanamsaCalculator.calculate(j2000)
        assertEquals(0.0, tropical, 1e-10)
    }

    @Test
    fun testUnsupportedAyanamsaRejectedDeterministically() {
        assertFailsWith<UnsupportedOperationException> {
            AyanamsaCalculator.forConvention("RAMAN")
        }
        assertFailsWith<UnsupportedOperationException> {
            AyanamsaCalculator.forConvention("KRISHNAMURTI_KP")
        }
        assertFailsWith<UnsupportedOperationException> {
            AyanamsaCalculator.forConvention("UNKNOWN_CONVENTION")
        }
    }

    @Test
    fun testPlanetaryCoordinatesSanityAtJ2000() {
        for (planet in Planet.entries) {
            val pos = PlanetaryCalculator.calculate(planet, j2000)
            assertTrue(pos.apparentLongitude in 0.0..<360.0, "Planet $planet longitude outside [0, 360)")
            assertTrue(pos.geocentricDistanceAu > 0.0, "Planet $planet distance must be positive")
        }

        // Test retrograde state for planets
        val mercury = PlanetaryCalculator.calculate(Planet.MERCURY, j2000)
        val venus = PlanetaryCalculator.calculate(Planet.VENUS, j2000)
        val mars = PlanetaryCalculator.calculate(Planet.MARS, j2000)
        val jupiter = PlanetaryCalculator.calculate(Planet.JUPITER, j2000)
        val saturn = PlanetaryCalculator.calculate(Planet.SATURN, j2000)

        // At J2000.0 (2000-01-01), Saturn was retrograde (opposition was in Nov 1999, direct station was Jan 12, 2000)
        assertTrue(saturn.isRetrograde, "Saturn was retrograde on Jan 1, 2000")
        assertFalse(venus.isRetrograde, "Venus was direct on Jan 1, 2000")
        assertFalse(mars.isRetrograde, "Mars was direct on Jan 1, 2000")
    }
}
