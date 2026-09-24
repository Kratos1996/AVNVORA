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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AshtakavargaSdkTest {

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
    fun testMetadataIncludesPhase61AshtakavargaDomains() {
        val metadata = sdk.getMetadata()
        val domains = metadata.supportedDomains.toSet()

        assertTrue("ASHTAKAVARGA" in domains)
        assertTrue("BHINNASHTAKAVARGA" in domains)
        assertTrue("SARVASHTAKAVARGA" in domains)
        assertTrue("PRASTARASHTAKAVARGA" in domains)
        assertTrue("SURYA_ASHTAKAVARGA" in domains)
        assertTrue("CHANDRA_ASHTAKAVARGA" in domains)
        assertTrue("KUJA_ASHTAKAVARGA" in domains)
        assertTrue("BUDHA_ASHTAKAVARGA" in domains)
        assertTrue("GURU_ASHTAKAVARGA" in domains)
        assertTrue("SHUKRA_ASHTAKAVARGA" in domains)
        assertTrue("SHANI_ASHTAKAVARGA" in domains)
    }

    @Test
    fun testCalculateAshtakavargaStandaloneSdk() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateAshtakavarga(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val av = (result as AynvoraResult.Success).value

        assertEquals("PARASHARA_CLASSICAL_V1", av.rulesetId)
        assertEquals(AshtakavargaCompleteness.COMPLETE, av.completeness)
        assertEquals(listOf(CelestialBody.RAHU, CelestialBody.KETU), av.unsupportedBodies)

        // Verify BAV tables for 7 classical planets
        val expectedTotals = mapOf(
            CelestialBody.SUN to 48,
            CelestialBody.MOON to 49,
            CelestialBody.MARS to 39,
            CelestialBody.MERCURY to 54,
            CelestialBody.JUPITER to 56,
            CelestialBody.VENUS to 52,
            CelestialBody.SATURN to 39,
        )

        for ((body, expectedTotal) in expectedTotals) {
            val bav = av.bhinnashtakavarga[body]
            assertNotNull(bav, "Missing BAV for body: $body")
            assertEquals(expectedTotal, bav.totalBindus, "Mismatch in total Bindus for $body")
            assertEquals(96 - expectedTotal, bav.totalRekhas, "Mismatch in total Rekhas for $body")
            assertEquals(12, bav.signScores.size)

            for (signScore in bav.signScores) {
                assertEquals(8, signScore.binduCount + signScore.rekhaCount)
            }
        }

        // Verify Sarvashtakavarga (SAV)
        val sav = av.sarvashtakavarga
        assertEquals(12, sav.signScores.size)
        assertEquals(337, sav.grandTotalBindus, "SAV Grand total must be exactly 337")
        assertEquals(672 - 337, sav.grandTotalRekhas)
        assertTrue(sav.isInvariantValid)

        for (i in 0 until 12) {
            val signScore = sav.signScores[i]
            val expectedSignBindus = expectedTotals.keys.sumOf { body ->
                av.bhinnashtakavarga[body]!!.signScores[i].binduCount
            }
            assertEquals(expectedSignBindus, signScore.totalBindus)
            assertEquals(56 - expectedSignBindus, signScore.totalRekhas)
        }
    }

    @Test
    fun testChartResultIncludesAshtakavarga() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())
        val result = sdk.calculateChart(request)

        assertIs<AynvoraResult.Success<*>>(result)
        val chart = (result as AynvoraResult.Success).value

        val av = chart.ashtakavarga
        assertNotNull(av)
        assertEquals("PARASHARA_CLASSICAL_V1", av.rulesetId)
        assertEquals(337, av.sarvashtakavarga.grandTotalBindus)
        assertTrue(av.sarvashtakavarga.isInvariantValid)
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
