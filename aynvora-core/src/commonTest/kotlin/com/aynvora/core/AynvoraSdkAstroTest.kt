package com.aynvora.core

import com.aynvora.core.models.AspectType
import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.CombustionState
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.PlanetMotionState
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AynvoraSdkAstroTest {

    private val sdk = Aynvora.create()

    private fun sampleBirthData(): BirthData = BirthData(
        date = BirthDate(2000, 1, 1),
        time = BirthTime(17, 30, 0), // 17:30 IST = 12:00 UTC (J2000.0)
        place = BirthPlace(
            name = "New Delhi",
            coordinates = Coordinates(28.6139, 77.2090),
            timezoneId = "Asia/Kolkata",
        ),
    )

    @Test
    fun testRealAstronomicalCalculationViaSdkFacade() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                ayanamsa = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
                houseSystem = HouseSystem.WHOLE_SIGN,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Success<ChartResult>>(result)

        val chart = result.value
        assertEquals("0.3.0", chart.engineVersion)
        assertEquals("CALCULATED", chart.calculationStatus)
        assertEquals("MEEUS_VSOP87", chart.calculationModel)

        // J2000.0 epoch check
        assertEquals(2451545.0, chart.julianDay, 1e-4)

        // Lahiri Ayanamsa at J2000.0 is ~23.857°
        assertEquals(23.857, chart.ayanamsaDegrees, 0.01)

        // Lagna verification
        assertNotNull(chart.lagna)
        assertTrue(chart.lagna.tropicalLongitude in 0.0..<360.0)
        assertTrue(chart.lagna.siderealLongitude in 0.0..<360.0)
        assertTrue(chart.lagna.rashiPosition.rashi.index in 0..11)
        assertTrue(chart.lagna.nakshatraPosition.nakshatra.index in 0..26)
        assertTrue(chart.lagna.nakshatraPosition.pada in 1..4)

        // Houses verification
        assertEquals(12, chart.houses.size)
        chart.houses.forEachIndexed { idx, house ->
            assertEquals(idx + 1, house.houseNumber)
            assertEquals(HouseSystem.WHOLE_SIGN, house.system)
            assertTrue(house.cuspLongitude in 0.0..<360.0)
            assertTrue(house.startLongitude in 0.0..<360.0)
            assertTrue(house.endLongitude in 0.0..<360.0)
        }

        // 9 celestial bodies must be calculated
        assertEquals(9, chart.planetaryPositions.size)

        val bodies = chart.planetaryPositions.map { it.body }
        assertTrue(bodies.contains(CelestialBody.SUN))
        assertTrue(bodies.contains(CelestialBody.MOON))
        assertTrue(bodies.contains(CelestialBody.MERCURY))
        assertTrue(bodies.contains(CelestialBody.VENUS))
        assertTrue(bodies.contains(CelestialBody.MARS))
        assertTrue(bodies.contains(CelestialBody.JUPITER))
        assertTrue(bodies.contains(CelestialBody.SATURN))
        assertTrue(bodies.contains(CelestialBody.RAHU))
        assertTrue(bodies.contains(CelestialBody.KETU))

        // Sun checks
        val sun = chart.planetaryPositions.first { it.body == CelestialBody.SUN }
        assertEquals(280.46, sun.tropicalLongitude, 0.1) // Tropical Capricorn
        assertTrue(sun.siderealLongitude in 0.0..<360.0)
        assertEquals(sun.tropicalLongitude - chart.ayanamsaDegrees, sun.siderealLongitude, 1e-6)
        assertTrue(sun.nakshatraPosition.pada in 1..4)
        assertTrue(sun.houseNumber in 1..12)

        // Rahu & Ketu opposition check
        val rahu = chart.planetaryPositions.first { it.body == CelestialBody.RAHU }
        val ketu = chart.planetaryPositions.first { it.body == CelestialBody.KETU }
        val separation = abs(ketu.siderealLongitude - rahu.siderealLongitude)
        assertEquals(180.0, separation, 1e-9)
        assertTrue(rahu.isRetrograde)
        assertTrue(ketu.isRetrograde)
        assertTrue(rahu.houseNumber in 1..12)
        assertTrue(ketu.houseNumber in 1..12)

        // Aspects check
        assertNotNull(chart.aspects)
        assertTrue(chart.aspects.isNotEmpty())
        chart.aspects.forEach { aspect ->
            assertTrue(aspect.firstBody != aspect.secondBody)
            assertTrue(aspect.firstBody.ordinal < aspect.secondBody.ordinal)
            assertTrue(aspect.actualSeparation in 0.0..180.0)
            assertTrue(aspect.orb >= 0.0)
        }
        // Rahu and Ketu must have an OPPOSITION aspect
        val nodeOpp = chart.aspects.firstOrNull { it.firstBody == CelestialBody.RAHU && it.secondBody == CelestialBody.KETU }
        assertNotNull(nodeOpp)
        assertEquals(AspectType.OPPOSITION, nodeOpp.type)
        assertEquals(0.0, nodeOpp.orb, 1e-6)

        // Planet states check
        assertEquals(9, chart.planetStates.size)
        val sunState = chart.planetStates.first { it.body == CelestialBody.SUN }
        assertEquals(CombustionState.NOT_APPLICABLE, sunState.combustionState)
        assertEquals(PlanetMotionState.DIRECT, sunState.motionState)

        val rahuState = chart.planetStates.first { it.body == CelestialBody.RAHU }
        assertEquals(CombustionState.NOT_APPLICABLE, rahuState.combustionState)
        assertEquals(PlanetMotionState.RETROGRADE, rahuState.motionState)
    }

    @Test
    fun testMathematicalDeterminism() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())

        val result1 = sdk.calculateChart(request)
        val result2 = sdk.calculateChart(request)

        assertIs<AynvoraResult.Success<ChartResult>>(result1)
        assertIs<AynvoraResult.Success<ChartResult>>(result2)

        assertEquals(result1.value.julianDay, result2.value.julianDay)
        assertEquals(result1.value.ayanamsaDegrees, result2.value.ayanamsaDegrees)
        assertEquals(result1.value.lagna?.siderealLongitude, result2.value.lagna?.siderealLongitude)

        for (i in result1.value.planetaryPositions.indices) {
            val p1 = result1.value.planetaryPositions[i]
            val p2 = result2.value.planetaryPositions[i]
            assertEquals(p1.body, p2.body)
            assertEquals(p1.tropicalLongitude, p2.tropicalLongitude)
            assertEquals(p1.siderealLongitude, p2.siderealLongitude)
            assertEquals(p1.rashiPosition, p2.rashiPosition)
            assertEquals(p1.nakshatraPosition, p2.nakshatraPosition)
            assertEquals(p1.houseNumber, p2.houseNumber)
            assertEquals(p1.motionState, p2.motionState)
            assertEquals(p1.combustionState, p2.combustionState)
            assertEquals(p1.isRetrograde, p2.isRetrograde)
            assertEquals(p1.dailyMotionDegrees, p2.dailyMotionDegrees)
        }

        assertEquals(result1.value.aspects.size, result2.value.aspects.size)
        for (i in result1.value.aspects.indices) {
            val a1 = result1.value.aspects[i]
            val a2 = result2.value.aspects[i]
            assertEquals(a1.firstBody, a2.firstBody)
            assertEquals(a1.secondBody, a2.secondBody)
            assertEquals(a1.type, a2.type)
            assertEquals(a1.orb, a2.orb)
        }
    }

    @Test
    fun testUnsupportedAyanamsaReturnsFailureWithoutSilentFallback() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                ayanamsa = AyanamsaConvention.RAMAN,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
        assertTrue(result.message.contains("RAMAN"))
    }

    @Test
    fun testPlacidusHouseSystemReturnsUnsupportedConfiguration() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                houseSystem = HouseSystem.PLACIDUS,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
        assertTrue(result.message.contains("Placidus"))
    }
}
