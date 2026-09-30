package com.aynvora.core.astrology

import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.core.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AstroChartContractTest {
    private val metadata = CalculationMetadata(calculationProfileId = "STANDARD_VEDIC", calculationModel = "TEST")

    private fun source(placements: List<AstroChartPlacement>) = AstroChartSnapshot(
        chartId = "D1", chartTypeId = "RASHI", titleKey = "chart.d1", zodiacModeId = "SIDEREAL",
        houseSystemId = "WHOLE_SIGN", status = CalculationAvailability.AVAILABLE,
        ascendantSignIndex = 1,
        houses = (1..12).map { AstroChartHouse(it, (it % 12)) }, placements = placements, sourceMetadata = metadata,
    )

    private fun placement(id: String, house: Int, sign: Int = 1, degree: Double = 19.44) = AstroChartPlacement(
        bodyId = id, houseNumber = house, signIndex = sign, longitude = sign * 30.0 + degree,
        degreeInSign = degree, nakshatraId = "KRITTIKA", nakshatraIndex = 2, pada = 2,
        retrograde = id == "SATURN", combust = id == "MOON", exalted = null,
        debilitated = false, vargottama = null, dignityId = "OWN", sourceId = "test-ephemeris",
    )

    @Test fun groupsEveryPlanetUnderItsSourceHouseAndRoundTripsJson() {
        val result = assertIs<AstroChartBuildResult.Valid>(AstroChartBuilder.fromSnapshot(source(listOf(
            placement("MOON", 2), placement("VENUS", 2, degree = 19.32), placement("SUN", 3, sign = 2),
        ))).let { it })
        assertEquals(listOf("MOON", "VENUS"), result.chart.houses[1].planets.map { it.planetId })
        assertEquals("SUN", result.chart.houses[2].planets.single().planetId)
        assertEquals(2, result.chart.houses[1].planets.first().houseNumber)
        assertEquals(true, result.chart.houses[1].planets.first().combust)
        val encoded = AstroChartJson.encode(result.chart)
        val decoded = AstroChartJson.decode(encoded)
        assertEquals(result.chart, decoded)
        assertEquals(encoded, AstroChartJson.encode(decoded))
    }

    @Test fun rejectsDuplicateOrOrphanPlanetData() {
        val duplicate = AstroChartBuilder.fromSnapshot(source(listOf(placement("MOON", 1), placement("MOON", 2))))
        assertIs<AstroChartBuildResult.Invalid>(duplicate)
        val orphan = AstroChartBuilder.fromSnapshot(source(listOf(placement("MOON", 13))))
        assertIs<AstroChartBuildResult.Invalid>(orphan)
    }

    @Test fun rejectsChartWithMissingOrDuplicateHouseNumbers() {
        val base = source(emptyList())
        val bad = base.copy(houses = base.houses.dropLast(1) + base.houses.first())
        assertIs<AstroChartBuildResult.Invalid>(AstroChartBuilder.fromSnapshot(bad))
    }
}
