package com.aynvora.astro.states

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.math.AstroMath.angularSeparation
import kotlinx.serialization.Serializable

/**
 * Apparent longitudinal motion state.
 */
@Serializable
enum class PlanetMotionState {
    DIRECT,
    RETROGRADE,
}

/**
 * Solar combustion (Asta) condition.
 */
@Serializable
enum class CombustionState {
    NORMAL,
    COMBUST,
    NOT_APPLICABLE,
}

/**
 * Factual state analysis for a celestial body.
 */
@Serializable
data class PlanetStatePosition(
    val bodyId: BodyId,
    val motionState: PlanetMotionState,
    val combustionState: CombustionState,
    val separationFromSun: Double? = null,
    val combustionThresholdDegrees: Double? = null,
)

/**
 * Classical Vedic combustion (Asta) and planetary motion state calculator.
 *
 * Implements classical thresholds per Brihat Parashara Hora Shastra and Surya Siddhanta:
 * - Moon: 12.0°
 * - Mars: 17.0°
 * - Mercury: 14.0° (direct), 12.0° (retrograde)
 * - Jupiter: 11.0°
 * - Venus: 10.0° (direct), 8.0° (retrograde)
 * - Saturn: 15.0°
 * - Sun, Rahu, Ketu: NOT_APPLICABLE
 */
object PlanetStateCalculator {

    fun calculate(positions: List<BodyPosition>): List<PlanetStatePosition> {
        val sun = positions.firstOrNull { it.bodyId == BodyId.SUN }

        return positions.map { body ->
            val motionState = if (body.isRetrograde) {
                PlanetMotionState.RETROGRADE
            } else {
                PlanetMotionState.DIRECT
            }

            val (combustionState, sepFromSun, threshold) = if (sun == null ||
                body.bodyId == BodyId.SUN ||
                body.bodyId == BodyId.RAHU ||
                body.bodyId == BodyId.KETU
            ) {
                Triple(CombustionState.NOT_APPLICABLE, null, null)
            } else {
                val sep = angularSeparation(sun.siderealLongitude, body.siderealLongitude)
                val thresh = getCombustionThreshold(body.bodyId, body.isRetrograde)
                val isCombust = if (thresh != null) sep <= thresh else false
                val state = if (isCombust) CombustionState.COMBUST else CombustionState.NORMAL
                Triple(state, sep, thresh)
            }

            PlanetStatePosition(
                bodyId = body.bodyId,
                motionState = motionState,
                combustionState = combustionState,
                separationFromSun = sepFromSun,
                combustionThresholdDegrees = threshold,
            )
        }.sortedBy { it.bodyId.ordinal }
    }

    private fun getCombustionThreshold(bodyId: BodyId, isRetrograde: Boolean): Double? = when (bodyId) {
        BodyId.MOON -> 12.0
        BodyId.MARS -> 17.0
        BodyId.MERCURY -> if (isRetrograde) 12.0 else 14.0
        BodyId.JUPITER -> 11.0
        BodyId.VENUS -> if (isRetrograde) 8.0 else 10.0
        BodyId.SATURN -> 15.0
        BodyId.SUN, BodyId.RAHU, BodyId.KETU -> null
    }
}
