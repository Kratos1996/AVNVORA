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

class ShodhitaAshtakavargaSdkTest {

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
    fun testMetadataIncludesPhase62ShodhanaDomains() {
        val metadata = sdk.getMetadata()
        val domains = metadata.supportedDomains.toSet()

        assertTrue("TRIKONA_SHODHANA" in domains, "Metadata must declare TRIKONA_SHODHANA")
        assertTrue("EKADHIPATYA_SHODHANA" in domains, "Metadata must declare EKADHIPATYA_SHODHANA")
        assertTrue("SHODHITA_ASHTAKAVARGA" in domains, "Metadata must declare SHODHITA_ASHTAKAVARGA")
    }

    @Test
    fun testCalculateShodhitaAshtakavargaStandaloneSdk() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateShodhitaAshtakavarga(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val shodhita = (result as AynvoraResult.Success).value

        assertEquals("PARASHARA_CLASSICAL_V1", shodhita.rulesetId)
        assertEquals(AshtakavargaCompleteness.COMPLETE, shodhita.completeness)
        assertEquals(7, shodhita.shodhitaBhinnashtakavarga.size)

        val classicalBodies = listOf(
            CelestialBody.SUN,
            CelestialBody.MOON,
            CelestialBody.MARS,
            CelestialBody.MERCURY,
            CelestialBody.JUPITER,
            CelestialBody.VENUS,
            CelestialBody.SATURN,
        )

        val expectedRawTotals = mapOf(
            CelestialBody.SUN to 48,
            CelestialBody.MOON to 49,
            CelestialBody.MARS to 39,
            CelestialBody.MERCURY to 54,
            CelestialBody.JUPITER to 56,
            CelestialBody.VENUS to 52,
            CelestialBody.SATURN to 39,
        )

        for (body in classicalBodies) {
            val chart = shodhita.shodhitaBhinnashtakavarga[body]
            assertNotNull(chart, "Missing Shodhita BAV for $body")
            assertEquals(expectedRawTotals[body], chart.rawTotalBindus)

            // Monotonic non-increasing property: raw >= trikona >= shodhita >= 0
            assertTrue(
                chart.rawTotalBindus >= chart.trikonaTotalBindus,
                "$body: raw total (${chart.rawTotalBindus}) must be >= trikona total (${chart.trikonaTotalBindus})"
            )
            assertTrue(
                chart.trikonaTotalBindus >= chart.shodhitaTotalBindus,
                "$body: trikona total (${chart.trikonaTotalBindus}) must be >= shodhita total (${chart.shodhitaTotalBindus})"
            )
            assertTrue(
                chart.shodhitaTotalBindus >= 0,
                "$body: shodhita total must be non-negative"
            )

            // Verify each sign
            for (sign in 0 until 12) {
                val score = chart.signScores[sign]
                assertTrue(
                    score.rawBindus >= score.trikonaReducedBindus,
                    "$body sign $sign: raw (${score.rawBindus}) >= trikona (${score.trikonaReducedBindus})"
                )
                assertTrue(
                    score.trikonaReducedBindus >= score.shodhitaBindus,
                    "$body sign $sign: trikona (${score.trikonaReducedBindus}) >= shodhita (${score.shodhitaBindus})"
                )
                assertTrue(
                    score.shodhitaBindus >= 0,
                    "$body sign $sign: shodhita must be non-negative"
                )
            }
        }

        // Verify Shodhita SAV
        val sav = shodhita.shodhitaSarvashtakavarga
        assertEquals(337, sav.grandTotalRawBindus)
        assertTrue(sav.grandTotalRawBindus >= sav.grandTotalTrikonaBindus)
        assertTrue(sav.grandTotalTrikonaBindus >= sav.grandTotalShodhitaBindus)

        // Verify each sign sum in Shodhita SAV equals sum of the 7 planets' shodhita bindus
        for (sign in 0 until 12) {
            val expectedSum = classicalBodies.sumOf { body ->
                shodhita.shodhitaBhinnashtakavarga[body]!!.signScores[sign].shodhitaBindus
            }
            assertEquals(
                expectedSum,
                sav.signScores[sign].shodhitaTotalBindus,
                "Sign $sign in Shodhita SAV must equal sum of 7 planets' shodhita bindus"
            )
        }
    }

    @Test
    fun testShodhitaAshtakavargaInFullChartResult() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateChart(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val chart = (result as AynvoraResult.Success).value

        // Direct property on ChartResult
        val shodhita = chart.shodhitaAshtakavarga
        assertNotNull(shodhita, "ChartResult must contain shodhitaAshtakavarga")
        assertEquals(337, shodhita.shodhitaSarvashtakavarga.grandTotalRawBindus)

        // Sub-property on AshtakavargaResult
        val av = chart.ashtakavarga
        assertNotNull(av, "ChartResult must contain ashtakavarga")
        val shodhanaSub = av.shodhana
        assertNotNull(shodhanaSub, "AshtakavargaResult must contain shodhana")
        assertEquals(
            shodhita.shodhitaSarvashtakavarga.grandTotalShodhitaBindus,
            shodhanaSub.shodhitaSarvashtakavarga.grandTotalShodhitaBindus,
            "Shodhita SAV in chartResult must match ashtakavarga.shodhana"
        )

        // Backward compatibility: raw Ashtakavarga totals remain strictly unchanged
        assertEquals(337, av.sarvashtakavarga.grandTotalBindus)
        assertTrue(av.sarvashtakavarga.isInvariantValid)
    }

    @Test
    fun testDeterminismOfShodhitaCalculation() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result1 = sdk.calculateShodhitaAshtakavarga(request)
        val result2 = sdk.calculateShodhitaAshtakavarga(request)

        assertIs<AynvoraResult.Success<*>>(result1)
        assertIs<AynvoraResult.Success<*>>(result2)

        val s1 = (result1 as AynvoraResult.Success).value
        val s2 = (result2 as AynvoraResult.Success).value

        assertEquals(s1.shodhitaSarvashtakavarga.grandTotalShodhitaBindus, s2.shodhitaSarvashtakavarga.grandTotalShodhitaBindus)
        for (sign in 0 until 12) {
            assertEquals(
                s1.shodhitaSarvashtakavarga.signScores[sign].shodhitaTotalBindus,
                s2.shodhitaSarvashtakavarga.signScores[sign].shodhitaTotalBindus
            )
        }
    }
}
