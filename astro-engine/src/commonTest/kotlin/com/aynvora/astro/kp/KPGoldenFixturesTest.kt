package com.aynvora.astro.kp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KPGoldenFixturesTest {

    @Test
    fun testExactZeroAndBoundaryEpsilons() {
        val subAtZero = KP249Engine.findSubdivision(0.0)
        assertEquals(1, subAtZero.index)
        assertEquals("Aries", subAtZero.signName)
        assertEquals("Mars", subAtZero.signLord)
        assertEquals("Ashwini", subAtZero.nakshatraName)
        assertEquals("Ketu", subAtZero.starLord)
        assertEquals("Ketu", subAtZero.subLord)

        // Epsilon just above zero
        val subJustAbove = KP249Engine.findSubdivision(0.000001)
        assertEquals(1, subJustAbove.index)

        // Epsilon just below 360°
        val subJustBelow360 = KP249Engine.findSubdivision(359.999999)
        assertEquals(249, subJustBelow360.index)
        assertEquals("Pisces", subJustBelow360.signName)
        assertEquals("Jupiter", subJustBelow360.signLord)
        assertEquals("Revati", subJustBelow360.nakshatraName)
        assertEquals("Mercury", subJustBelow360.starLord)
        assertEquals("Saturn", subJustBelow360.subLord)
    }

    @Test
    fun testFirstSignBoundaryAt30Degrees_KrittikaSplit() {
        // At 30.0°, Krittika Rahu sub is split between Aries and Taurus
        // Just before 30°: Aries
        val subBefore30 = KP249Engine.findSubdivision(29.9999)
        assertEquals("Aries", subBefore30.signName)
        assertEquals("Mars", subBefore30.signLord)
        assertEquals("Krittika", subBefore30.nakshatraName)
        assertEquals("Sun", subBefore30.starLord)
        assertEquals("Rahu", subBefore30.subLord)
        assertEquals(22, subBefore30.index)

        // Just after 30°: Taurus
        val subAfter30 = KP249Engine.findSubdivision(30.0001)
        assertEquals("Taurus", subAfter30.signName)
        assertEquals("Venus", subAfter30.signLord)
        assertEquals("Krittika", subAfter30.nakshatraName)
        assertEquals("Sun", subAfter30.starLord)
        assertEquals("Rahu", subAfter30.subLord)
        assertEquals(23, subAfter30.index)
    }

    @Test
    fun testSecondSignBoundaryAt60Degrees_MrigashiraCleanBoundary() {
        // At 60.0°, Saturn sub ends and Mercury sub begins with ZERO sub-split!
        val subBefore60 = KP249Engine.findSubdivision(59.9999)
        assertEquals("Taurus", subBefore60.signName)
        assertEquals("Venus", subBefore60.signLord)
        assertEquals("Mrigashira", subBefore60.nakshatraName)
        assertEquals("Mars", subBefore60.starLord)
        assertEquals("Saturn", subBefore60.subLord)

        val subAfter60 = KP249Engine.findSubdivision(60.0001)
        assertEquals("Gemini", subAfter60.signName)
        assertEquals("Mercury", subAfter60.signLord)
        assertEquals("Mrigashira", subAfter60.nakshatraName)
        assertEquals("Mars", subAfter60.starLord)
        assertEquals("Mercury", subAfter60.subLord)
    }

    @Test
    fun testKrishnamurtiAyanamshaCalculation() {
        // For J2000 (JD 2451545.0), year 2000:
        // elapsed = 2000 - 291 = 1709 years
        // rate = 50.2388475" / 3600 = 0.0139552354 deg/year
        // ayanamsha ~ 23.8495°
        val ayanamsaJ2000 = KPAyanamshaCalculator.calculate(2451545.0)
        assertTrue(ayanamsaJ2000 > 23.8 && ayanamsaJ2000 < 23.9)

        // For year 2026:
        val jd2026 = 2451545.0 + 26.0 * 365.25
        val ayanamsa2026 = KPAyanamshaCalculator.calculate(jd2026)
        assertTrue(ayanamsa2026 > 24.2 && ayanamsa2026 < 24.3)
    }
}
