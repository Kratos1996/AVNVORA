package com.aynvora.core

import com.aynvora.core.models.AshtakavargaCompleteness
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.Coordinates
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AshtakavargaPindaSdkTest {

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
    fun testMetadataIncludesPhase63PindaDomains() {
        val metadata = sdk.getMetadata()
        val domains = metadata.supportedDomains.toSet()

        assertTrue("ASHTAKAVARGA_PINDA" in domains, "Metadata must declare ASHTAKAVARGA_PINDA")
        assertTrue("RASHI_PINDA" in domains, "Metadata must declare RASHI_PINDA")
        assertTrue("GRAHA_PINDA" in domains, "Metadata must declare GRAHA_PINDA")
        assertTrue("SHODHYA_PINDA" in domains, "Metadata must declare SHODHYA_PINDA")
    }

    @Test
    fun testCalculateAshtakavargaPindaStandaloneSdk() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateAshtakavargaPinda(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val pindaResult = (result as AynvoraResult.Success).value

        assertEquals("PARASHARA_CLASSICAL_V1", pindaResult.rulesetId)
        assertEquals(AshtakavargaCompleteness.COMPLETE, pindaResult.completeness)
        assertEquals(7, pindaResult.planetaryPindas.size)
        assertTrue(pindaResult.unsupportedBodies.contains(CelestialBody.RAHU))
        assertTrue(pindaResult.unsupportedBodies.contains(CelestialBody.KETU))

        val classicalBodies = listOf(
            CelestialBody.SUN,
            CelestialBody.MOON,
            CelestialBody.MARS,
            CelestialBody.MERCURY,
            CelestialBody.JUPITER,
            CelestialBody.VENUS,
            CelestialBody.SATURN,
        )

        var sumRasi = 0
        var sumGraha = 0
        var sumShodhya = 0

        for (body in classicalBodies) {
            val planetPinda = pindaResult.planetaryPindas[body]
            assertNotNull(planetPinda, "Missing Pinda for $body")

            assertEquals(body, planetPinda.targetBody)
            assertEquals("PARASHARA_CLASSICAL_V1", planetPinda.rulesetId)

            assertTrue(planetPinda.rasiPinda >= 0, "$body rasiPinda must be >= 0")
            assertTrue(planetPinda.grahaPinda >= 0, "$body grahaPinda must be >= 0")
            assertEquals(
                planetPinda.rasiPinda + planetPinda.grahaPinda,
                planetPinda.shodhyaPinda,
                "$body Shodhya Pinda must equal Rashi Pinda + Graha Pinda",
            )

            assertEquals(12, planetPinda.rasiContributions.size)
            assertEquals(
                planetPinda.rasiPinda,
                planetPinda.rasiContributions.values.sum(),
                "$body sum of rasiContributions must equal rasiPinda",
            )

            assertEquals(7, planetPinda.grahaContributions.size)
            assertEquals(
                planetPinda.grahaPinda,
                planetPinda.grahaContributions.values.sum(),
                "$body sum of grahaContributions must equal grahaPinda",
            )

            sumRasi += planetPinda.rasiPinda
            sumGraha += planetPinda.grahaPinda
            sumShodhya += planetPinda.shodhyaPinda
        }

        assertEquals(sumRasi, pindaResult.totalRasiPinda)
        assertEquals(sumGraha, pindaResult.totalGrahaPinda)
        assertEquals(sumShodhya, pindaResult.totalShodhyaPinda)
        assertEquals(pindaResult.totalRasiPinda + pindaResult.totalGrahaPinda, pindaResult.totalShodhyaPinda)
    }

    @Test
    fun testAshtakavargaPindaInFullChartResult() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateChart(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val chart = (result as AynvoraResult.Success).value

        // Direct property on ChartResult
        val pindaDirect = chart.ashtakavargaPinda
        assertNotNull(pindaDirect, "ChartResult must contain ashtakavargaPinda")

        // Sub-property on AshtakavargaResult
        val av = chart.ashtakavarga
        assertNotNull(av, "ChartResult must contain ashtakavarga")
        val pindaSub = av.pinda
        assertNotNull(pindaSub, "AshtakavargaResult must contain pinda")

        assertEquals(pindaDirect.totalRasiPinda, pindaSub.totalRasiPinda)
        assertEquals(pindaDirect.totalGrahaPinda, pindaSub.totalGrahaPinda)
        assertEquals(pindaDirect.totalShodhyaPinda, pindaSub.totalShodhyaPinda)

        // Backward compatibility: raw Ashtakavarga and Shodhita Ashtakavarga remain intact
        assertEquals(337, av.sarvashtakavarga.grandTotalBindus)
        assertTrue(av.sarvashtakavarga.isInvariantValid)
        val shodhana = av.shodhana
        assertNotNull(shodhana)
        assertEquals(337, shodhana.shodhitaSarvashtakavarga.grandTotalRawBindus)
    }

    @Test
    fun testDeterminismOfPindaCalculation() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result1 = sdk.calculateAshtakavargaPinda(request)
        val result2 = sdk.calculateAshtakavargaPinda(request)

        assertIs<AynvoraResult.Success<*>>(result1)
        assertIs<AynvoraResult.Success<*>>(result2)

        val p1 = (result1 as AynvoraResult.Success).value
        val p2 = (result2 as AynvoraResult.Success).value

        assertEquals(p1.totalRasiPinda, p2.totalRasiPinda)
        assertEquals(p1.totalGrahaPinda, p2.totalGrahaPinda)
        assertEquals(p1.totalShodhyaPinda, p2.totalShodhyaPinda)

        for ((body, pinda1) in p1.planetaryPindas) {
            val pinda2 = p2.planetaryPindas[body]
            assertNotNull(pinda2)
            assertEquals(pinda1.rasiPinda, pinda2.rasiPinda)
            assertEquals(pinda1.grahaPinda, pinda2.grahaPinda)
            assertEquals(pinda1.shodhyaPinda, pinda2.shodhyaPinda)
        }
    }

    @Test
    fun testInvalidInputThrowsOnConstruction() {
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            BirthDate(2000, 13, 1)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            BirthTime(25, 0, 0)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            Coordinates(95.0, 0.0)
        }
    }
}
