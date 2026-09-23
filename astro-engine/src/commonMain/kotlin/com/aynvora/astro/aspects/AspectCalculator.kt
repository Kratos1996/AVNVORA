package com.aynvora.astro.aspects

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.math.AstroMath.angularSeparation
import kotlin.math.abs

/**
 * Deterministic planetary aspect and angular relationship calculator.
 *
 * Computes pairwise angular separations across distinct celestial bodies and
 * detects astrological aspects according to configurable profiles and orb tolerances.
 */
object AspectCalculator {

    /**
     * Calculates all active aspects between participating celestial bodies.
     *
     * @param positions List of calculated celestial body positions
     * @param profile Aspect definition and orb configuration profile
     * @return Deterministically ordered list of active aspects
     */
    fun calculate(
        positions: List<BodyPosition>,
        profile: AspectProfile = AspectProfile(),
    ): List<AspectPosition> {
        val participatingBodies = positions
            .filter { pos ->
                if (!profile.includeLunarNodes) {
                    pos.bodyId != BodyId.RAHU && pos.bodyId != BodyId.KETU
                } else {
                    true
                }
            }
            .sortedBy { it.bodyId.ordinal }

        val aspects = mutableListOf<AspectPosition>()
        val n = participatingBodies.size

        for (i in 0 until n - 1) {
            val bodyA = participatingBodies[i]
            for (j in i + 1 until n) {
                val bodyB = participatingBodies[j]

                val separation = angularSeparation(bodyA.siderealLongitude, bodyB.siderealLongitude)

                for (def in profile.definitions) {
                    val orb = abs(separation - def.exactAngle)
                    if (orb <= def.allowedOrb + 1e-9) {
                        aspects.add(
                            AspectPosition(
                                firstBody = bodyA.bodyId,
                                secondBody = bodyB.bodyId,
                                type = def.type,
                                exactAngle = def.exactAngle,
                                actualSeparation = separation,
                                orb = orb,
                            ),
                        )
                    }
                }
            }
        }

        return aspects.sortedWith(
            compareBy(
                { it.firstBody.ordinal },
                { it.secondBody.ordinal },
                { it.type.ordinal },
            ),
        )
    }
}
