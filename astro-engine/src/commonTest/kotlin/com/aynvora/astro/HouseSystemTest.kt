package com.aynvora.astro

import com.aynvora.astro.houses.EqualHouseCalculator
import com.aynvora.astro.houses.HouseCalculationInput
import com.aynvora.astro.houses.HouseSystemRegistry
import com.aynvora.astro.houses.PlacidusHouseCalculator
import com.aynvora.astro.houses.WholeSignHouseCalculator
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HouseSystemTest {

    private fun createSampleLagna(siderealLong: Double, rashiIndex: Int, degreeInRashi: Double): LagnaPosition {
        return LagnaPosition(
            tropicalLongitude = normalizeDegrees(siderealLong + 24.0),
            siderealLongitude = siderealLong,
            rashiIndex = rashiIndex,
            rashiName = "Rashi_$rashiIndex",
            degreeInRashi = degreeInRashi,
            nakshatraIndex = 0,
            nakshatraName = "Ashwini",
            degreeInNakshatra = 0.0,
            pada = 1,
        )
    }

    @Test
    fun testWholeSignTwelveHouseSequence() {
        // Lagna in Leo (Rashi index 4, sidereal 135.0°)
        val lagna = createSampleLagna(siderealLong = 135.0, rashiIndex = 4, degreeInRashi = 15.0)

        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 28.6139,
            longitudeDeg = 77.2090,
            ayanamsaDegrees = 24.0,
            planetPositions = emptyMap(),
        )

        val result = WholeSignHouseCalculator.calculate(input)
        assertEquals(12, result.houses.size)

        for (h in 1..12) {
            val house = result.houses[h - 1]
            assertEquals(h, house.houseNumber)
            val expectedRashiIndex = (4 + (h - 1)) % 12
            assertEquals(expectedRashiIndex, house.rashiIndex, "House $h rashi index")
            assertEquals(normalizeDegrees(expectedRashiIndex * 30.0), house.startLongitude, 1e-9)
            assertEquals(normalizeDegrees((expectedRashiIndex + 1) * 30.0), house.endLongitude, 1e-9)
        }
    }

    @Test
    fun testWholeSignPlanetToHouseMapping() {
        // Lagna in Leo (Index 4)
        val lagna = createSampleLagna(siderealLong = 135.0, rashiIndex = 4, degreeInRashi = 15.0)

        val planets = mapOf(
            BodyId.SUN to 140.0, // Leo (Index 4) -> House 1
            BodyId.MOON to 165.0, // Virgo (Index 5) -> House 2
            BodyId.MARS to 215.0, // Scorpio (Index 7) -> House 4
            BodyId.JUPITER to 310.0, // Aquarius (Index 10) -> House 7
            BodyId.SATURN to 35.0, // Taurus (Index 1) -> House 10
            BodyId.RAHU to 80.0, // Gemini (Index 2) -> House 11
            BodyId.KETU to 260.0, // Sagittarius (Index 8) -> House 5
        )

        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 28.6139,
            longitudeDeg = 77.2090,
            ayanamsaDegrees = 24.0,
            planetPositions = planets,
        )

        val result = WholeSignHouseCalculator.calculate(input)
        assertEquals(1, result.planetHouseOccupancy[BodyId.SUN])
        assertEquals(2, result.planetHouseOccupancy[BodyId.MOON])
        assertEquals(4, result.planetHouseOccupancy[BodyId.MARS])
        assertEquals(7, result.planetHouseOccupancy[BodyId.JUPITER])
        assertEquals(10, result.planetHouseOccupancy[BodyId.SATURN])
        assertEquals(11, result.planetHouseOccupancy[BodyId.RAHU])
        assertEquals(5, result.planetHouseOccupancy[BodyId.KETU])

        // Verify Rahu and Ketu are in exact 7th house aspect to each other (11 -> 5 is opposite)
        val rahuHouse = result.planetHouseOccupancy[BodyId.RAHU]!!
        val ketuHouse = result.planetHouseOccupancy[BodyId.KETU]!!
        val diff = (rahuHouse - ketuHouse + 12) % 12
        assertEquals(6, diff, "Rahu and Ketu must occupy opposite houses (difference of 6)")
    }

    @Test
    fun testEqualHouseTwelveHousesSpacedThirtyDegrees() {
        // Lagna at 18.25° Aries
        val lagna = createSampleLagna(siderealLong = 18.25, rashiIndex = 0, degreeInRashi = 18.25)

        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 28.6139,
            longitudeDeg = 77.2090,
            ayanamsaDegrees = 24.0,
            planetPositions = emptyMap(),
        )

        val result = EqualHouseCalculator.calculate(input)
        assertEquals(12, result.houses.size)

        for (h in 1..12) {
            val house = result.houses[h - 1]
            assertEquals(h, house.houseNumber)
            val expectedCusp = normalizeDegrees(18.25 + (h - 1) * 30.0)
            val expectedEnd = normalizeDegrees(18.25 + h * 30.0)
            assertEquals(expectedCusp, house.cuspLongitude, 1e-9)
            assertEquals(expectedCusp, house.startLongitude, 1e-9)
            assertEquals(expectedEnd, house.endLongitude, 1e-9)
        }
    }

    @Test
    fun testEqualHouseBoundaryConditionsAndWraparound() {
        // Lagna at 350.0° (Pisces)
        val lagna = createSampleLagna(siderealLong = 350.0, rashiIndex = 11, degreeInRashi = 20.0)

        // House 1: [350.0, 20.0)
        // House 2: [20.0, 50.0)
        // House 3: [50.0, 80.0)
        val planets = mapOf(
            BodyId.SUN to 350.0, // Exactly at House 1 cusp -> House 1
            BodyId.MOON to 19.999, // Just before House 2 cusp -> House 1
            BodyId.MERCURY to 20.0, // Exactly at House 2 cusp -> House 2
            BodyId.VENUS to 49.999, // Just before House 3 cusp -> House 2
            BodyId.MARS to 50.0, // Exactly at House 3 cusp -> House 3
            BodyId.SATURN to 349.999, // Just before House 1 cusp -> House 12
        )

        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 10.0,
            longitudeDeg = 20.0,
            ayanamsaDegrees = 0.0,
            planetPositions = planets,
        )

        val result = EqualHouseCalculator.calculate(input)
        assertEquals(1, result.planetHouseOccupancy[BodyId.SUN])
        assertEquals(1, result.planetHouseOccupancy[BodyId.MOON])
        assertEquals(2, result.planetHouseOccupancy[BodyId.MERCURY])
        assertEquals(2, result.planetHouseOccupancy[BodyId.VENUS])
        assertEquals(3, result.planetHouseOccupancy[BodyId.MARS])
        assertEquals(12, result.planetHouseOccupancy[BodyId.SATURN])
    }

    @Test
    fun testPlacidusExplicitlyUnsupported() {
        val lagna = createSampleLagna(siderealLong = 0.0, rashiIndex = 0, degreeInRashi = 0.0)
        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 0.0,
            longitudeDeg = 0.0,
            ayanamsaDegrees = 0.0,
            planetPositions = emptyMap(),
        )

        assertFailsWith<UnsupportedOperationException> {
            PlacidusHouseCalculator.calculate(input)
        }
    }

    @Test
    fun testHouseSystemRegistry() {
        assertEquals(WholeSignHouseCalculator, HouseSystemRegistry.forName("WHOLE_SIGN"))
        assertEquals(WholeSignHouseCalculator, HouseSystemRegistry.forName("whole_sign"))
        assertEquals(WholeSignHouseCalculator, HouseSystemRegistry.forName("RASHI_BHAVA"))
        assertEquals(EqualHouseCalculator, HouseSystemRegistry.forName("EQUAL_HOUSE"))
        assertEquals(EqualHouseCalculator, HouseSystemRegistry.forName("equal_house"))
        assertEquals(PlacidusHouseCalculator, HouseSystemRegistry.forName("PLACIDUS"))

        assertFailsWith<UnsupportedOperationException> {
            HouseSystemRegistry.forName("KOCH")
        }
    }

    @Test
    fun testEveryPlanetAssignedToExactlyOneHouseProperty() {
        val lagna = createSampleLagna(siderealLong = 45.0, rashiIndex = 1, degreeInRashi = 15.0)
        val allBodies = BodyId.entries.associateWith { (it.ordinal * 40.0) % 360.0 }

        val input = HouseCalculationInput(
            lagna = lagna,
            latitudeDeg = 15.0,
            longitudeDeg = 75.0,
            ayanamsaDegrees = 24.0,
            planetPositions = allBodies,
        )

        val wholeSignResult = WholeSignHouseCalculator.calculate(input)
        assertEquals(BodyId.entries.size, wholeSignResult.planetHouseOccupancy.size)
        wholeSignResult.planetHouseOccupancy.values.forEach { houseNum ->
            assertTrue(houseNum in 1..12, "House number must be 1..12, got: $houseNum")
        }

        val equalHouseResult = EqualHouseCalculator.calculate(input)
        assertEquals(BodyId.entries.size, equalHouseResult.planetHouseOccupancy.size)
        equalHouseResult.planetHouseOccupancy.values.forEach { houseNum ->
            assertTrue(houseNum in 1..12, "House number must be 1..12, got: $houseNum")
        }
    }
}
