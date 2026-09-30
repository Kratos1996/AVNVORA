package com.aynvora.ui.astrology

import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.core.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AynvoraChartTest {
    private val metadata = CalculationMetadata(calculationProfileId = "TEST", calculationModel = "TEST")
    private fun planet(id: String, n: Int) = PlanetInHouse(
        planetId = id, displayKey = "body.$id", degreeInSign = 19.44, degrees = 19,
        longitude = 49.44, minutes = 26, seconds = 24.0,
        nakshatra = "KRITTIKA", pada = 2, retrograde = n == 1, combust = n == 2,
        exalted = null, debilitated = null, vargottama = null, dignity = null,
        state = null, houseNumber = 2, sign = Rashi.TAURUS, provenance = "test",
    )

    @Test fun renderMapperOnlyMapsHouseOwnedPlacements() {
        val houses = (1..12).map { n ->
            AstroChartHouseData(n, Rashi.fromIndex((n + 1) % 12), planets = if (n == 2) listOf(planet("MOON", 1), planet("VENUS", 2)) else emptyList())
        }
        val chart = AstroChart("D1", "RASHI", "chart.d1", "SIDEREAL", "WHOLE_SIGN", AstroChartAscendant(Rashi.TAURUS), houses, metadata, "test", CalculationAvailability.AVAILABLE)
        val rendered = ChartRenderMapper.map(chart)
        assertEquals(listOf("MOON", "VENUS"), rendered[1].planetItems.map { it.planetId })
        assertTrue(rendered[0].planetItems.isEmpty())
        assertEquals("19°26′24″", rendered[1].planetItems.first().degreeText)
        assertEquals("☼", rendered[1].planetItems.last().markerText)
    }

    @Test fun collisionLayoutIsStableAndHouseLocalForFivePlanets() {
        val planets = (1..5).map { n ->
            ChartPlanetRenderModel("P$n", "P$n", "Planet $n", "${n}°00′00″", "${n}°00′", "", null, n, 0, "Planet $n")
        }
        val a = ChartPlanetLayoutEngine.positions(planets)
        val b = ChartPlanetLayoutEngine.positions(planets)
        assertEquals(a, b)
        assertEquals(5, a.size)
        assertTrue(a.all { (_, xy) -> xy.first in 0f..1f && xy.second in 0f..1f })
        assertEquals(5, a.map { it.second }.distinct().size)
        a.indices.forEach { i -> (i + 1 until a.size).forEach { j ->
            val (x1, y1) = a[i].second
            val (x2, y2) = a[j].second
            assertTrue(kotlin.math.abs(x1 - x2) >= .51f || kotlin.math.abs(y1 - y2) >= .25f)
        } }
    }
}
