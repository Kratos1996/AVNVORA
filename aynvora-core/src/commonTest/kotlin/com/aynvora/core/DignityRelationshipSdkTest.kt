package com.aynvora.core

import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.CompoundRelationshipType
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.DignityType
import com.aynvora.core.models.DivisionalChart
import com.aynvora.core.models.NaturalRelationshipType
import com.aynvora.core.models.PlanetaryDignity
import com.aynvora.core.models.PlanetaryRelationship
import com.aynvora.core.models.TemporaryRelationshipType
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DignityRelationshipSdkTest {

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
    fun testMetadataIncludesPhase54Domains() {
        val metadata = sdk.getMetadata()
        val domains = metadata.supportedDomains.toSet()

        assertTrue("PLANETARY_DIGNITIES" in domains)
        assertTrue("EXALTATION" in domains)
        assertTrue("DEBILITATION" in domains)
        assertTrue("MOOLATRIKONA" in domains)
        assertTrue("OWN_SIGN" in domains)
        assertTrue("NATURAL_RELATIONSHIPS" in domains)
        assertTrue("TEMPORARY_RELATIONSHIPS" in domains)
        assertTrue("COMPOUND_RELATIONSHIPS" in domains)
        assertTrue("PANCHADHA_MAITRI" in domains)
    }

    @Test
    fun testCalculateChartIncludesDignitiesAndRelationships() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())
            val result = sdk.calculateChart(request)

            assertIs<AynvoraResult.Success<ChartResult>>(result)
            val chart = result.value

            assertNotNull(chart.planetaryDignities)
            assertNotNull(chart.planetaryRelationships)

            // Dignities for all 9 bodies (7 planets + Rahu + Ketu)
            assertEquals(9, chart.planetaryDignities.size)
            val bodiesInDignity = chart.planetaryDignities.map { it.body }.toSet()
            assertEquals(CelestialBody.entries.toSet(), bodiesInDignity)

            // Rahu and Ketu must have NOT_APPLICABLE dignity
            val rahuDignity = chart.planetaryDignities.first { it.body == CelestialBody.RAHU }
            assertEquals(DignityType.NOT_APPLICABLE, rahuDignity.dignityType)
            assertFalse(rahuDignity.isExalted)
            assertFalse(rahuDignity.isDebilitated)

            val ketuDignity = chart.planetaryDignities.first { it.body == CelestialBody.KETU }
            assertEquals(DignityType.NOT_APPLICABLE, ketuDignity.dignityType)

            // Relationships between all pairs of 9 bodies = 9 * 8 = 72 directional relationships
            assertEquals(72, chart.planetaryRelationships.size)
            for (rel in chart.planetaryRelationships) {
                assertEquals(DivisionalChart.D1, rel.chart)
                assertTrue(rel.relativeHouseDistance in 1..12)
            }
        }
    }

    @Test
    fun testCalculateDignitiesForD1AndD9() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())

            // D1 Dignities
            val d1Result = sdk.calculateDignities(request, DivisionalChart.D1)
            assertIs<AynvoraResult.Success<List<PlanetaryDignity>>>(d1Result)
            assertEquals(9, d1Result.value.size)
            assertTrue(d1Result.value.all { it.chart == DivisionalChart.D1 })

            // D9 Navamsa Dignities
            val d9Result = sdk.calculateDignities(request, DivisionalChart.D9)
            assertIs<AynvoraResult.Success<List<PlanetaryDignity>>>(d9Result)
            assertEquals(9, d9Result.value.size)
            assertTrue(d9Result.value.all { it.chart == DivisionalChart.D9 })
        }
    }

    @Test
    fun testCalculateRelationshipsForD1AndD9() {
        runBlocking {
            val request = ChartRequest(birthData = sampleBirthData())

            // D1 Relationships
            val d1Rel = sdk.calculateRelationships(request, DivisionalChart.D1)
            assertIs<AynvoraResult.Success<List<PlanetaryRelationship>>>(d1Rel)
            assertEquals(72, d1Rel.value.size)
            assertTrue(d1Rel.value.all { it.chart == DivisionalChart.D1 })

            // D9 Relationships
            val d9Rel = sdk.calculateRelationships(request, DivisionalChart.D9)
            assertIs<AynvoraResult.Success<List<PlanetaryRelationship>>>(d9Rel)
            assertEquals(72, d9Rel.value.size)
            assertTrue(d9Rel.value.all { it.chart == DivisionalChart.D9 })
        }
    }

    @Test
    fun testUnsupportedRulesetHandlingInDignityCalculation() {
        runBlocking {
            val request = ChartRequest(
                birthData = sampleBirthData(),
                config = CalculationConfig(vargaRulesetId = "UNKNOWN_NON_EXISTENT_RULESET"),
            )

            val result = sdk.calculateDignities(request, DivisionalChart.D9)
            assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
            assertTrue(result.message.contains("UNKNOWN_NON_EXISTENT_RULESET"))
        }
    }

    @Test
    fun testInvalidInputHandlingInDignityCalculation() {
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            BirthDate(2000, 13, 1)
        }
    }
}
