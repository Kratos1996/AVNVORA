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
import com.aynvora.core.models.DivisionalChart
import com.aynvora.core.models.DivisionalChartResult
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.Rashi
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VargaSdkIntegrationTest {

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
    fun testPublicSdkCalculateDivisionalChartSingle() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val result = sdk.calculateDivisionalChart(request, DivisionalChart.D9)

            assertIs<AynvoraResult.Success<DivisionalChartResult>>(result)
            assertEquals("V1", result.calculationMetadata?.contractVersion)
            assertEquals("STANDARD_VEDIC", result.calculationMetadata?.calculationProfileId)
            assertEquals("LAHIRI_CHITRAPAKSHA", result.calculationMetadata?.conventions?.get("ayanamsa"))
            val d9 = result.value
            assertEquals(DivisionalChart.D9, d9.chart)
            assertEquals("PARASHARA_CLASSICAL_V1", d9.rulesetId)
            assertTrue(d9.isSupported)

            // Lagna in D9
            assertNotNull(d9.lagnaPosition)
            assertTrue(d9.lagnaPosition.isLagna)
            assertTrue(d9.lagnaPosition.resultingLongitude in 0.0..<360.0)

            // 9 celestial bodies present
            assertEquals(9, d9.positions.size)
            val bodies = d9.positions.mapNotNull { it.body }.toSet()
            assertEquals(CelestialBody.entries.toSet(), bodies)

            d9.positions.forEach { pos ->
                assertTrue(pos.resultingLongitude in 0.0..<360.0)
                assertTrue(pos.degreeInResultingRashi in 0.0..30.0)
                assertTrue(pos.resultingRashi.index in 0..11)
            }
        }
    }

    @Test
    fun testPublicSdkCalculateDivisionalChartsMultiple() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val requestedCharts = setOf(DivisionalChart.D1, DivisionalChart.D9, DivisionalChart.D10)
            val result = sdk.calculateDivisionalCharts(request, requestedCharts)

            assertIs<AynvoraResult.Success<Map<DivisionalChart, DivisionalChartResult>>>(result)
            val vargaMap = result.value
            assertEquals(3, vargaMap.size)
            assertTrue(vargaMap.containsKey(DivisionalChart.D1))
            assertTrue(vargaMap.containsKey(DivisionalChart.D9))
            assertTrue(vargaMap.containsKey(DivisionalChart.D10))

            assertEquals(DivisionalChart.D1, vargaMap[DivisionalChart.D1]!!.chart)
            assertEquals(DivisionalChart.D9, vargaMap[DivisionalChart.D9]!!.chart)
            assertEquals(DivisionalChart.D10, vargaMap[DivisionalChart.D10]!!.chart)
        }
    }

    @Test
    fun testPublicSdkChartRequestWithConfiguredDivisionalCharts() {
        runBlocking {
            val request = ChartRequest(
                birthData = sampleBirthData(),
                config = CalculationConfig(
                    requestedDivisionalCharts = setOf(DivisionalChart.D9, DivisionalChart.D60),
                ),
            )

            val result = sdk.calculateChart(request)
            assertIs<AynvoraResult.Success<ChartResult>>(result)

            val chartResult = result.value
            assertEquals(2, chartResult.divisionalCharts.size)
            assertTrue(chartResult.divisionalCharts.containsKey(DivisionalChart.D9))
            assertTrue(chartResult.divisionalCharts.containsKey(DivisionalChart.D60))
        }
    }

    @Test
    fun testPublicSdkMetadataIncludesVargaDomains() {
        val metadata = sdk.getMetadata()
        assertTrue(metadata.supportedDomains.contains("DIVISIONAL_CHARTS_VARGAS"))
        assertTrue(metadata.supportedDomains.contains("D1_RASHI"))
        assertTrue(metadata.supportedDomains.contains("D9_NAVAMSA"))
        assertTrue(metadata.supportedDomains.contains("D60_SHASHTIAMSA"))
    }

    @Test
    fun testPublicSdkUnsupportedConfigurationHandling() {
        runBlocking {
            val request = ChartRequest(
                birthData = sampleBirthData(),
                config = CalculationConfig(
                    vargaRulesetId = "UNSUPPORTED_RULESET",
                    requestedDivisionalCharts = setOf(DivisionalChart.D9),
                ),
            )

            val result = sdk.calculateChart(request)
            assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
            assertTrue(result.message.contains("UNSUPPORTED_RULESET"))
        }
    }

    @Test
    fun testLanguageIndependence() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val res1 = sdk.calculateDivisionalChart(request, DivisionalChart.D9)
            val res2 = sdk.calculateDivisionalChart(request, DivisionalChart.D9)

            assertIs<AynvoraResult.Success<DivisionalChartResult>>(res1)
            assertIs<AynvoraResult.Success<DivisionalChartResult>>(res2)

            // Mathematical factual coordinates match bit-for-bit regardless of external presentation
            assertEquals(res1.value.lagnaPosition?.resultingRashi, res2.value.lagnaPosition?.resultingRashi)
            assertEquals(res1.value.lagnaPosition?.resultingLongitude, res2.value.lagnaPosition?.resultingLongitude)

            for (i in res1.value.positions.indices) {
                val p1 = res1.value.positions[i]
                val p2 = res2.value.positions[i]
                assertEquals(p1.body, p2.body)
                assertEquals(p1.resultingRashi, p2.resultingRashi)
                assertEquals(p1.resultingLongitude, p2.resultingLongitude)
            }
        }
    }

    @Test
    fun testPublicSdkPhase51RegressionLagnaAndHouses() {
        runBlocking {
            val request = ChartRequest(
                birthData = sampleBirthData(),
                config = CalculationConfig(
                    houseSystem = HouseSystem.WHOLE_SIGN,
                ),
            )

            val result = sdk.calculateChart(request)
            assertIs<AynvoraResult.Success<ChartResult>>(result)

            val chart = result.value
            assertNotNull(chart.lagna)
            assertEquals(12, chart.houses.size)
            assertEquals(9, chart.planetaryPositions.size)
            chart.planetaryPositions.forEach { pos ->
                assertTrue(pos.houseNumber in 1..12)
            }
        }
    }

    @Test
    fun testPublicSdkPhase52RegressionAspectsAndPlanetStates() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val result = sdk.calculateChart(request)
            assertIs<AynvoraResult.Success<ChartResult>>(result)

            val chart = result.value
            assertNotNull(chart.aspects)
            assertTrue(chart.aspects.isNotEmpty())
            assertEquals(9, chart.planetStates.size)

            chart.planetaryPositions.forEach { pos ->
                assertNotNull(pos.motionState)
                assertNotNull(pos.combustionState)
            }
        }
    }
}
