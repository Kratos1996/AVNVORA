package com.aynvora.core.models

import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Standardized chart rendering model contract for UI presentation conforming to Phase 10.31 Section 11.
 * Pure presentation layout model produced from [AstroChart] without recalculating astrology.
 */
@Serializable
data class ChartRenderModel(
    val chartId: String,
    val chartType: String,
    val houses: List<RenderHouse>,
    val ascendantLongitude: Double? = null,
    val hasCollisions: Boolean = false,
)

@Serializable
data class RenderHouse(
    val houseNumber: Int,
    val sign: Rashi,
    val planets: List<RenderPlanet>,
    val markers: List<String> = emptyList(),
)

@Serializable
data class RenderPlanet(
    val planetId: String,
    val displayKey: String,
    val planetDegree: Double,
    val formattedDegree: String,
    val retrograde: Boolean,
    val combust: Boolean,
    val markers: List<String> = emptyList(),
    val displayOrder: Int,
    val collisionHints: List<String> = emptyList(),
)

/**
 * Deterministic engine for arranging planets inside chart houses with proximity detection.
 */
object PlanetLayoutEngine {

    const val COLLISION_THRESHOLD_DEGREES = 1.2

    fun layout(chart: AstroChart): ChartRenderModel {
        var anyCollision = false

        val renderHouses = chart.houses.map { house ->
            val sortedPlanets = house.planets.sortedBy { it.degreeInSign }
            val renderPlanets = mutableListOf<RenderPlanet>()

            for (i in sortedPlanets.indices) {
                val p = sortedPlanets[i]
                val hints = mutableListOf<String>()

                // Check preceding planet collision
                if (i > 0) {
                    val prev = sortedPlanets[i - 1]
                    if (abs(p.degreeInSign - prev.degreeInSign) < COLLISION_THRESHOLD_DEGREES) {
                        hints.add("CLOSE_CONJUNCTION_WITH_${prev.planetId}")
                        hints.add("OFFSET_Y_PLUS")
                        anyCollision = true
                    }
                }
                // Check succeeding planet collision
                if (i < sortedPlanets.size - 1) {
                    val next = sortedPlanets[i + 1]
                    if (abs(next.degreeInSign - p.degreeInSign) < COLLISION_THRESHOLD_DEGREES) {
                        hints.add("CLOSE_CONJUNCTION_WITH_${next.planetId}")
                        hints.add("OFFSET_Y_MINUS")
                        anyCollision = true
                    }
                }

                val markers = mutableListOf<String>()
                if (p.exalted == true) markers.add("EXALTED")
                if (p.debilitated == true) markers.add("DEBILITATED")
                if (p.retrograde) markers.add("RETROGRADE")
                if (p.combust) markers.add("COMBUST")

                val degInt = p.degreeInSign.toInt()
                val minInt = ((p.degreeInSign - degInt) * 60).toInt()
                val formatted = "${degInt}°${minInt.toString().padStart(2, '0')}'"

                renderPlanets.add(
                    RenderPlanet(
                        planetId = p.planetId,
                        displayKey = p.displayKey,
                        planetDegree = p.degreeInSign,
                        formattedDegree = formatted,
                        retrograde = p.retrograde,
                        combust = p.combust,
                        markers = markers,
                        displayOrder = i + 1,
                        collisionHints = hints.distinct(),
                    )
                )
            }

            RenderHouse(
                houseNumber = house.houseNumber,
                sign = house.sign,
                planets = renderPlanets,
                markers = house.markers,
            )
        }

        return ChartRenderModel(
            chartId = chart.chartId,
            chartType = chart.chartType,
            houses = renderHouses,
            ascendantLongitude = chart.ascendant?.longitude,
            hasCollisions = anyCollision,
        )
    }
}
