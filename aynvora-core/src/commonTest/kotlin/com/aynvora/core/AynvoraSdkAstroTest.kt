package com.aynvora.core

import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.Coordinates
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Success<ChartResult>>(result)

        val chart = result.value
        assertEquals("0.2.0", chart.engineVersion)
        assertEquals("CALCULATED", chart.calculationStatus)
        assertEquals("MEEUS_VSOP87", chart.calculationModel)

        // J2000.0 epoch check
        assertEquals(2451545.0, chart.julianDay, 1e-4)

        // Lahiri Ayanamsa at J2000.0 is ~23.857°
        assertEquals(23.857, chart.ayanamsaDegrees, 0.01)

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

        // Rahu & Ketu opposition check
        val rahu = chart.planetaryPositions.first { it.body == CelestialBody.RAHU }
        val ketu = chart.planetaryPositions.first { it.body == CelestialBody.KETU }
        val separation = abs(ketu.siderealLongitude - rahu.siderealLongitude)
        assertEquals(180.0, separation, 1e-9)
        assertTrue(rahu.isRetrograde)
        assertTrue(ketu.isRetrograde)
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

        for (i in result1.value.planetaryPositions.indices) {
            val p1 = result1.value.planetaryPositions[i]
            val p2 = result2.value.planetaryPositions[i]
            assertEquals(p1.body, p2.body)
            assertEquals(p1.tropicalLongitude, p2.tropicalLongitude)
            assertEquals(p1.siderealLongitude, p2.siderealLongitude)
            assertEquals(p1.rashiPosition, p2.rashiPosition)
            assertEquals(p1.nakshatraPosition, p2.nakshatraPosition)
            assertEquals(p1.isRetrograde, p2.isRetrograde)
            assertEquals(p1.dailyMotionDegrees, p2.dailyMotionDegrees)
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
}
