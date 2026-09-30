package com.aynvora.astro.transit

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.ayanamsa.AyanamsaCalculator
import com.aynvora.astro.planets.LunarNodesCalculator
import com.aynvora.astro.planets.MoonCalculator
import com.aynvora.astro.planets.PlanetaryCalculator
import com.aynvora.astro.planets.PlanetaryCalculator.Planet
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.provenance.CalculationMetadata
import kotlinx.serialization.Serializable

/**
 * Snapshot of planetary transit positions for a specific Julian Day.
 */
@Serializable
data class TransitSnapshot(
    val julianDay: Double,
    val ayanamsaDegrees: Double,
    val planetaryPositions: List<BodyPosition>,
    val calculationMetadata: CalculationMetadata = CalculationMetadata(
        calculationProfileId = "TRANSIT_ANALYTICAL_V1",
        calculationModel = "TRANSIT_ANALYTICAL_BODY_MODEL",
        conventions = mapOf("ayanamsa_convention" to "UNSPECIFIED"),
    ),
) {
    fun findBody(bodyId: BodyId): BodyPosition? =
        planetaryPositions.find { it.bodyId == bodyId }
}

/**
 * Astrological interaction between a transiting planet and a natal house/planet.
 */
@Serializable
data class TransitInteraction(
    val transitingBody: BodyId,
    val transitRashiIndex: Int,
    val transitDegreeInRashi: Double,
    val isRetrograde: Boolean,
    val natalHouseTarget: Int, // House 1..12 from Lagna or Moon
    val natalConjunctionBody: BodyId? = null,
)

/**
 * Timeline of transit snapshots sampled across a time window.
 */
@Serializable
data class TransitTimeline(
    val startJulianDay: Double,
    val endJulianDay: Double,
    val sampleStepDays: Double,
    val snapshots: List<TransitSnapshot>,
    val calculationMetadata: CalculationMetadata = snapshots.firstOrNull()?.calculationMetadata
        ?: CalculationMetadata(
            calculationProfileId = "TRANSIT_ANALYTICAL_V1",
            calculationModel = "TRANSIT_ANALYTICAL_BODY_MODEL",
        ),
)

/**
 * Deterministic planetary transit calculation engine.
 * Computes planetary positions for any target Julian Day or future range
 * using the core Meeus & VSOP87 algorithms from `:astro-engine`.
 */
object TransitCalculator {

    /**
     * Calculates planetary transit positions for a single instant.
     */
    fun calculateSnapshot(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): TransitSnapshot {
        val julianDay = JulianDay(jd)
        val ayanamsaCalc = AyanamsaCalculator.forConvention(ayanamsaConvention)
        val ayanamsaDegrees = ayanamsaCalc.calculate(julianDay)

        val positions = mutableListOf<BodyPosition>()

        // 1. Sun
        val sun = SunCalculator.calculate(julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.SUN,
                sun.apparentLongitude,
                ayanamsaDegrees,
                false,
                sun.dailyMotionDegrees
            )
        )

        // 2. Moon
        val moon = MoonCalculator.calculate(julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.MOON,
                moon.apparentLongitude,
                ayanamsaDegrees,
                false,
                moon.dailyMotionDegrees
            )
        )

        // 3. Mercury
        val mercury = PlanetaryCalculator.calculate(Planet.MERCURY, julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.MERCURY,
                mercury.apparentLongitude,
                ayanamsaDegrees,
                mercury.isRetrograde,
                mercury.dailyMotionDegrees
            )
        )

        // 4. Venus
        val venus = PlanetaryCalculator.calculate(Planet.VENUS, julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.VENUS,
                venus.apparentLongitude,
                ayanamsaDegrees,
                venus.isRetrograde,
                venus.dailyMotionDegrees
            )
        )

        // 5. Mars
        val mars = PlanetaryCalculator.calculate(Planet.MARS, julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.MARS,
                mars.apparentLongitude,
                ayanamsaDegrees,
                mars.isRetrograde,
                mars.dailyMotionDegrees
            )
        )

        // 6. Jupiter
        val jupiter = PlanetaryCalculator.calculate(Planet.JUPITER, julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.JUPITER,
                jupiter.apparentLongitude,
                ayanamsaDegrees,
                jupiter.isRetrograde,
                jupiter.dailyMotionDegrees
            )
        )

        // 7. Saturn
        val saturn = PlanetaryCalculator.calculate(Planet.SATURN, julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.SATURN,
                saturn.apparentLongitude,
                ayanamsaDegrees,
                saturn.isRetrograde,
                saturn.dailyMotionDegrees
            )
        )

        // 8. Rahu & 9. Ketu
        val nodes = LunarNodesCalculator.calculate(julianDay)
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.RAHU,
                nodes.rahu.apparentLongitude,
                ayanamsaDegrees,
                true,
                nodes.rahu.dailyMotionDegrees
            )
        )
        positions.add(
            com.aynvora.astro.AynvoraAstroEngine.createBodyPosition(
                BodyId.KETU,
                nodes.ketu.apparentLongitude,
                ayanamsaDegrees,
                true,
                nodes.ketu.dailyMotionDegrees
            )
        )

        return TransitSnapshot(
            julianDay = jd,
            ayanamsaDegrees = ayanamsaDegrees,
            planetaryPositions = positions,
            calculationMetadata = CalculationMetadata(
                calculationProfileId = "TRANSIT_ANALYTICAL_V1",
                calculationModel = "TRANSIT_ANALYTICAL_BODY_MODEL",
                conventions = mapOf("ayanamsa_convention" to ayanamsaConvention.uppercase()),
            ),
        )
    }

    /**
     * Samples transits across a time window with a fixed day step.
     */
    fun calculateTimeline(
        startJd: Double,
        endJd: Double,
        stepDays: Double = 1.0,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): TransitTimeline {
        require(endJd >= startJd) { "endJd ($endJd) must be >= startJd ($startJd)" }
        require(stepDays > 0.0) { "stepDays must be positive, got: $stepDays" }

        val snapshots = mutableListOf<TransitSnapshot>()
        var curJd = startJd
        while (curJd <= endJd) {
            snapshots.add(calculateSnapshot(curJd, ayanamsaConvention))
            curJd += stepDays
        }

        return TransitTimeline(
            startJulianDay = startJd,
            endJulianDay = endJd,
            sampleStepDays = stepDays,
            snapshots = snapshots,
        )
    }

    /**
     * Evaluates Gochar (transit) interactions relative to natal chart references (Lagna Rashi or Moon Rashi).
     */
    fun evaluateInteractions(
        transitSnapshot: TransitSnapshot,
        referenceRashiIndex: Int, // 0..11 (Lagna or Chandra Rashi)
        natalPositions: Map<BodyId, Int>, // BodyId -> RashiIndex
    ): List<TransitInteraction> {
        val interactions = mutableListOf<TransitInteraction>()

        for (transitBody in transitSnapshot.planetaryPositions) {
            val transitRashi = transitBody.rashiIndex
            // House from reference rashi (1-indexed: 1..12)
            val houseFromRef = ((transitRashi - referenceRashiIndex + 12) % 12) + 1

            // Check if any natal planet occupies the exact same rashi
            val natalConjunction = natalPositions.entries.find { it.value == transitRashi }?.key

            interactions.add(
                TransitInteraction(
                    transitingBody = transitBody.bodyId,
                    transitRashiIndex = transitRashi,
                    transitDegreeInRashi = transitBody.degreeInRashi,
                    isRetrograde = transitBody.isRetrograde,
                    natalHouseTarget = houseFromRef,
                    natalConjunctionBody = natalConjunction,
                )
            )
        }

        return interactions
    }
}
