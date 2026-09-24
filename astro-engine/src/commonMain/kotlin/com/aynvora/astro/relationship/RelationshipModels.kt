package com.aynvora.astro.relationship

import com.aynvora.astro.BodyId
import com.aynvora.astro.varga.DivisionalChart
import kotlinx.serialization.Serializable

/**
 * Natural planetary relationship classification (Naisargika Maitri)
 * according to Brihat Parashara Hora Shastra (BPHS) Chapter 3, Slokas 55-58.
 */
@Serializable
enum class NaturalRelationshipType {
    FRIEND,
    NEUTRAL,
    ENEMY,
    NOT_APPLICABLE,
}

/**
 * Temporary planetary relationship classification (Tatkalika Maitri)
 * based on mutual sign positions according to BPHS Chapter 3, Sloka 59.
 */
@Serializable
enum class TemporaryRelationshipType {
    FRIEND,
    ENEMY,
    NOT_APPLICABLE,
}

/**
 * Five-fold compound planetary relationship classification (Panchadha Maitri)
 * according to BPHS Chapter 3, Sloka 60.
 */
@Serializable
enum class CompoundRelationshipType {
    GREAT_FRIEND,
    FRIEND,
    NEUTRAL,
    ENEMY,
    GREAT_ENEMY,
    NOT_APPLICABLE,
}

/**
 * Calculated directional relationship from [sourceBodyId] to [targetBodyId].
 */
@Serializable
data class PlanetaryRelationshipPosition(
    val sourceBodyId: BodyId,
    val targetBodyId: BodyId,
    val chart: DivisionalChart = DivisionalChart.D1,
    val naturalRelationship: NaturalRelationshipType,
    val temporaryRelationship: TemporaryRelationshipType,
    val compoundRelationship: CompoundRelationshipType,
    val sourceRashiIndex: Int,
    val targetRashiIndex: Int,
    val relativeHouseDistance: Int,
)
