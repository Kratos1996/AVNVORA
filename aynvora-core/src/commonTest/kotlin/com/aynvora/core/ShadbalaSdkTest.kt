package com.aynvora.core

import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.PlanetaryShadbala
import com.aynvora.core.models.ShadbalaCompleteness
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShadbalaSdkTest {

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
    fun testMetadataIncludesPhase56ShadbalaDomains() {
        val metadata = sdk.getMetadata()
        val domains = metadata.supportedDomains.toSet()

        assertTrue("SHADBALA" in domains)
        assertTrue("STHANA_BALA" in domains)
        assertTrue("DIG_BALA" in domains)
        assertTrue("NAISARGIKA_BALA" in domains)
        assertTrue("UCHCHA_BALA" in domains)
        assertTrue("SAPTAVARGAJA_BALA" in domains)
        assertTrue("KENDRA_BALA" in domains)
        assertTrue("DREKKANA_BALA" in domains)
        assertTrue("OJHAYUGMARASYAMSA_BALA" in domains)
        assertTrue("KALA_BALA" in domains)
        assertTrue("CHESTA_BALA" in domains)
        assertTrue("DRIK_BALA" in domains)
        assertTrue("NATHONNATHA_BALA" in domains)
        assertTrue("PAKSHA_BALA" in domains)
        assertTrue("TRIBHAGA_BALA" in domains)
        assertTrue("VARA_BALA" in domains)
        assertTrue("HORA_BALA" in domains)
        assertTrue("MASA_BALA" in domains)
        assertTrue("VARSHA_BALA" in domains)
        assertTrue("AYANA_BALA" in domains)
        assertTrue("YUDDHA_BALA" in domains)
        assertTrue("CHESTA_KENDRA" in domains)
        assertTrue("DRISHTI_BALA" in domains)
    }

    @Test
    fun testCalculateChartIncludesCompleteShadbala() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val result = sdk.calculateChart(request)

            assertIs<AynvoraResult.Success<ChartResult>>(result)
            val chart = result.value

            assertNotNull(chart.shadbala)
            assertEquals(9, chart.shadbala.size)

            val bodies = chart.shadbala.map { it.body }.toSet()
            assertEquals(CelestialBody.entries.toSet(), bodies)

            val classicalPlanets = listOf(
                CelestialBody.SUN, CelestialBody.MOON, CelestialBody.MARS,
                CelestialBody.MERCURY, CelestialBody.JUPITER, CelestialBody.VENUS, CelestialBody.SATURN
            )

            for (body in classicalPlanets) {
                val sb = chart.shadbala.first { it.body == body }
                assertEquals(ShadbalaCompleteness.COMPLETE, sb.completeness)
                assertTrue(sb.isComplete, "Shadbala must be complete in Phase 5.6")
                assertNotNull(sb.totalVirupas, "Total Virupas must not be null when all components are evaluated")
                assertNotNull(sb.totalRupas, "Total Rupas must not be null when all components are evaluated")
                assertTrue(sb.totalVirupas > 0.0)

                // 1. Sthana Bala
                assertTrue(sb.sthanaBala.isEvaluated)
                assertTrue(sb.sthanaBala.uchchaBalaVirupas in 0.0..60.0)
                assertTrue(sb.sthanaBala.saptavargajaBalaVirupas >= 0.0)
                assertTrue(sb.sthanaBala.ojhayugmarasyamsaBalaVirupas in 0.0..30.0)
                assertTrue(sb.sthanaBala.kendraBalaVirupas in listOf(15.0, 30.0, 60.0))
                assertTrue(sb.sthanaBala.drekkanaBalaVirupas in listOf(0.0, 15.0))
                assertTrue(sb.sthanaBala.totalVirupas > 0.0)
                assertEquals(sb.sthanaBala.totalVirupas / 60.0, sb.sthanaBala.totalRupas, 1e-9)

                // 2. Dig Bala
                assertTrue(sb.digBala.isEvaluated)
                assertTrue(sb.digBala.virupas in 0.0..60.0)
                assertEquals(sb.digBala.virupas / 60.0, sb.digBala.rupas, 1e-9)

                // 3. Naisargika Bala
                assertTrue(sb.naisargikaBala.isEvaluated)
                assertTrue(sb.naisargikaBala.virupas in 8.0..60.0)
                assertTrue(sb.naisargikaBala.rank in 1..7)

                // 4. Kala Bala
                assertTrue(sb.kalaBala.isEvaluated)
                assertTrue(sb.kalaBala.nathonnathaBalaVirupas in 0.0..60.0)
                assertTrue(sb.kalaBala.pakshaBalaVirupas in 0.0..60.0)
                assertTrue(sb.kalaBala.tribhagaBalaVirupas in listOf(0.0, 60.0))
                assertTrue(sb.kalaBala.varaBalaVirupas in listOf(0.0, 45.0))
                assertTrue(sb.kalaBala.horaBalaVirupas in listOf(0.0, 60.0))
                assertTrue(sb.kalaBala.masaBalaVirupas in listOf(0.0, 30.0))
                assertTrue(sb.kalaBala.varshaBalaVirupas in listOf(0.0, 15.0))
                assertTrue(sb.kalaBala.ayanaBalaVirupas in 0.0..60.0)
                assertTrue(sb.kalaBala.totalVirupas > 0.0)

                // 5. Chesta Bala
                assertTrue(sb.chestaBala.isEvaluated)
                assertTrue(sb.chestaBala.virupas in 0.0..60.0)
                assertTrue(sb.chestaBala.motionCategory in listOf("VAKRA", "VIKALA", "CHARA", "MANDA", "SAMA", "DIRECT"))

                // 6. Drik Bala
                assertTrue(sb.drikBala.isEvaluated)
                assertNotNull(sb.drikBala.virupas)

                // Total match
                val expectedTotal = sb.sthanaBala.totalVirupas +
                    sb.digBala.virupas +
                    sb.kalaBala.totalVirupas +
                    sb.chestaBala.virupas +
                    sb.naisargikaBala.virupas +
                    sb.drikBala.virupas

                assertEquals(expectedTotal, sb.totalVirupas, 1e-9)
                assertEquals(expectedTotal / 60.0, sb.totalRupas, 1e-9)

                // Ruleset
                assertEquals("PARASHARA_CLASSICAL_V1", sb.rulesetId)
            }

            // Rahu and Ketu
            for (node in listOf(CelestialBody.RAHU, CelestialBody.KETU)) {
                val sb = chart.shadbala.first { it.body == node }
                assertEquals(ShadbalaCompleteness.UNSUPPORTED, sb.completeness)
                assertFalse(sb.isComplete)
                assertNull(sb.totalVirupas)
                assertNull(sb.totalRupas)
            }
        }
    }

    @Test
    fun testDedicatedCalculateShadbalaEntrypoint() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val result = sdk.calculateShadbala(request)

            assertIs<AynvoraResult.Success<List<PlanetaryShadbala>>>(result)
            val shadbalaList = result.value

            assertEquals(9, shadbalaList.size)

            val sun = shadbalaList.first { it.body == CelestialBody.SUN }
            assertEquals(1, sun.naisargikaBala.rank)
            assertEquals(60.0, sun.naisargikaBala.virupas, 1e-9)
            assertNotNull(sun.totalVirupas)
            assertTrue(sun.totalVirupas > 0.0)

            val saturn = shadbalaList.first { it.body == CelestialBody.SATURN }
            assertEquals(7, saturn.naisargikaBala.rank)
            assertEquals(60.0 * 1.0 / 7.0, saturn.naisargikaBala.virupas, 1e-9)
            assertNotNull(saturn.totalVirupas)
            assertTrue(saturn.totalVirupas > 0.0)
        }
    }

    @Test
    fun testInvalidInputThrowsOnConstruction() {
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            BirthDate(2000, 13, 1) // Invalid month
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            BirthTime(25, 0, 0) // Invalid hour
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            Coordinates(100.0, 0.0) // Invalid latitude
        }
    }
}
