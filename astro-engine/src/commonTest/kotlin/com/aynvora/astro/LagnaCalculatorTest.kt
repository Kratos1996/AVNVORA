package com.aynvora.astro

import com.aynvora.astro.ayanamsa.LahiriAyanamsaCalculator
import com.aynvora.astro.lagna.LagnaCalculator
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.time.SiderealTimeCalculator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LagnaCalculatorTest {

    @Test
    fun testGreenwichMeanSiderealTimeJ2000() {
        // At J2000.0 (2000-01-01 12:00 UT, JD 2451545.0)
        // GMST is 18h 41m 50.55s = 280.46061837 degrees
        val jd = JulianDay(2451545.0)
        val gmst = SiderealTimeCalculator.calculateGmst(jd)
        assertTrue(abs(gmst - 280.4606) < 0.001, "GMST at J2000.0 expected ~280.4606°, got $gmst")
    }

    @Test
    fun testLocalSiderealTimeObserverLongitude() {
        val jd = JulianDay(2451545.0)
        val gmst = SiderealTimeCalculator.calculateGmst(jd)

        // New Delhi: Longitude 77.2090° E
        val lstDelhi = SiderealTimeCalculator.calculateLst(jd, 77.2090)
        val expectedDelhi = (gmst + 77.2090) % 360.0
        assertEquals(expectedDelhi, lstDelhi, 1e-9)

        // New York: Longitude -74.0060° W
        val lstNy = SiderealTimeCalculator.calculateLst(jd, -74.0060)
        val expectedNy = (gmst - 74.0060 + 360.0) % 360.0
        assertEquals(expectedNy, lstNy, 1e-9)
    }

    @Test
    fun testMeanObliquityJ2000() {
        // At J2000.0, obliquity = 23° 26' 21.448" = 23.43929111°
        val jd = JulianDay(2451545.0)
        val eps = SiderealTimeCalculator.calculateMeanObliquity(jd)
        assertTrue(abs(eps - 23.439291) < 0.0001, "Obliquity at J2000.0 expected ~23.439291°, got $eps")
    }

    @Test
    fun testEquatorAscendantMathematicalProperty() {
        // At latitude = 0.0, when LST = 0.0, Ascendant must be exactly 90.0° (Cancer 0°)
        val jd = JulianDay(2451545.0)
        val gmst = SiderealTimeCalculator.calculateGmst(jd)
        val lagna = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 0.0,
            longitudeDeg = 360.0 - gmst, // cancels GMST so LST = 0.0
            ayanamsaDegrees = 0.0,
        )
        assertEquals(90.0, lagna.tropicalLongitude, 1e-5)
    }

    @Test
    fun testLagnaKnownLocationNewDelhi() {
        // 2024-03-21 06:00 UTC (11:30 AM IST), New Delhi (28.6139° N, 77.2090° E)
        val jd = JulianDay.fromUtcCalendar(2024, 3, 21, 6, 0, 0.0)
        val ayanamsa = LahiriAyanamsaCalculator.calculate(jd)

        val lagna = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 28.6139,
            longitudeDeg = 77.2090,
            ayanamsaDegrees = ayanamsa,
        )

        assertNotNull(lagna)
        assertTrue(lagna.tropicalLongitude in 0.0..<360.0)
        assertTrue(lagna.siderealLongitude in 0.0..<360.0)
        assertTrue(lagna.rashiIndex in 0..11)
        assertTrue(lagna.degreeInRashi in 0.0..<30.0)
        assertTrue(lagna.nakshatraIndex in 0..26)
        assertTrue(lagna.pada in 1..4)
    }

    @Test
    fun testLagnaSiderealTransformationMatchesLahiri() {
        val jd = JulianDay.fromUtcCalendar(2000, 1, 1, 12, 0, 0.0)
        val ayanamsa = LahiriAyanamsaCalculator.calculate(jd)

        val lagna = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 28.6139,
            longitudeDeg = 77.2090,
            ayanamsaDegrees = ayanamsa,
        )

        var expectedSidereal = lagna.tropicalLongitude - ayanamsa
        if (expectedSidereal < 0.0) expectedSidereal += 360.0
        assertEquals(expectedSidereal, lagna.siderealLongitude, 1e-7)
    }

    @Test
    fun testHighLatitudeSafety() {
        val jd = JulianDay.fromUtcCalendar(2020, 6, 21, 12, 0, 0.0)
        val ayanamsa = LahiriAyanamsaCalculator.calculate(jd)

        // Near Arctic circle (70° N)
        val lagnaArctic = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 70.0,
            longitudeDeg = 25.0,
            ayanamsaDegrees = ayanamsa,
        )
        assertNotNull(lagnaArctic)
        assertTrue(!lagnaArctic.tropicalLongitude.isNaN() && !lagnaArctic.tropicalLongitude.isInfinite())

        // Near North Pole (89.99° N)
        val lagnaNorthPole = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 89.99,
            longitudeDeg = 0.0,
            ayanamsaDegrees = ayanamsa,
        )
        assertNotNull(lagnaNorthPole)
        assertTrue(!lagnaNorthPole.tropicalLongitude.isNaN() && !lagnaNorthPole.tropicalLongitude.isInfinite())

        // Exact 90° boundary clamp test
        val lagnaExactPole = LagnaCalculator.calculate(
            jd = jd,
            latitudeDeg = 90.0,
            longitudeDeg = 0.0,
            ayanamsaDegrees = ayanamsa,
        )
        assertNotNull(lagnaExactPole)
        assertTrue(!lagnaExactPole.tropicalLongitude.isNaN() && !lagnaExactPole.tropicalLongitude.isInfinite())
    }

    @Test
    fun testInvalidCoordinatesThrowException() {
        val jd = JulianDay(2451545.0)

        assertFailsWith<IllegalArgumentException> {
            LagnaCalculator.calculate(jd, latitudeDeg = 91.0, longitudeDeg = 0.0, ayanamsaDegrees = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            LagnaCalculator.calculate(jd, latitudeDeg = -90.5, longitudeDeg = 0.0, ayanamsaDegrees = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            LagnaCalculator.calculate(jd, latitudeDeg = 0.0, longitudeDeg = 181.0, ayanamsaDegrees = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            LagnaCalculator.calculate(jd, latitudeDeg = 0.0, longitudeDeg = -181.0, ayanamsaDegrees = 0.0)
        }
    }
}
