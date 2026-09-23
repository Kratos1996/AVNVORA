package com.aynvora.astro.aspects

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * Astrological aspect classification types with classical exact angles and default orbs.
 */
@Serializable
enum class AspectType(val exactAngle: Double, val defaultOrb: Double) {
    CONJUNCTION(0.0, 8.0),
    SEXTILE(60.0, 6.0),
    SQUARE(90.0, 7.0),
    TRINE(120.0, 8.0),
    OPPOSITION(180.0, 8.0),
}

/**
 * Definition of an aspect type with its target angle and allowed orb tolerance.
 */
@Serializable
data class AspectDefinition(
    val type: AspectType,
    val exactAngle: Double = type.exactAngle,
    val allowedOrb: Double = type.defaultOrb,
)

/**
 * Configurable profile defining the aspect set and body participation rules.
 */
@Serializable
data class AspectProfile(
    val id: String = "STANDARD_MAJOR",
    val definitions: List<AspectDefinition> = listOf(
        AspectDefinition(AspectType.CONJUNCTION, 0.0, 8.0),
        AspectDefinition(AspectType.SEXTILE, 60.0, 6.0),
        AspectDefinition(AspectType.SQUARE, 90.0, 7.0),
        AspectDefinition(AspectType.TRINE, 120.0, 8.0),
        AspectDefinition(AspectType.OPPOSITION, 180.0, 8.0),
    ),
    val includeLunarNodes: Boolean = true,
)

/**
 * Calculated aspect fact between two distinct celestial bodies.
 */
@Serializable
data class AspectPosition(
    val firstBody: BodyId,
    val secondBody: BodyId,
    val type: AspectType,
    val exactAngle: Double,
    val actualSeparation: Double,
    val orb: Double,
)
