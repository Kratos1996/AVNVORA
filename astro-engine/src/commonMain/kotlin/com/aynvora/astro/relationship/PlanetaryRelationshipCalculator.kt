package com.aynvora.astro.relationship

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaPosition

/**
 * Deterministic facts-only calculator for natural (Naisargika), temporary (Tatkalika),
 * and compound (Panchadha) planetary relationships under the Parashara classical tradition (BPHS Ch. 3).
 */
object PlanetaryRelationshipCalculator {

    val CLASSICAL_PLANETS: List<BodyId> = listOf(
        BodyId.SUN,
        BodyId.MOON,
        BodyId.MARS,
        BodyId.MERCURY,
        BodyId.JUPITER,
        BodyId.VENUS,
        BodyId.SATURN,
    )

    /**
     * Natural directional relationship matrix (Naisargika Maitri)
     * Source: Brihat Parashara Hora Shastra, Chapter 3, Slokas 55-58.
     * Evaluated as source -> target. Note: Directional/asymmetric!
     */
    private val NATURAL_RELATIONSHIP_MATRIX: Map<BodyId, Map<BodyId, NaturalRelationshipType>> = mapOf(
        BodyId.SUN to mapOf(
            BodyId.MOON to NaturalRelationshipType.FRIEND,
            BodyId.MARS to NaturalRelationshipType.FRIEND,
            BodyId.MERCURY to NaturalRelationshipType.NEUTRAL,
            BodyId.JUPITER to NaturalRelationshipType.FRIEND,
            BodyId.VENUS to NaturalRelationshipType.ENEMY,
            BodyId.SATURN to NaturalRelationshipType.ENEMY,
        ),
        BodyId.MOON to mapOf(
            BodyId.SUN to NaturalRelationshipType.FRIEND,
            BodyId.MERCURY to NaturalRelationshipType.FRIEND,
            BodyId.MARS to NaturalRelationshipType.NEUTRAL,
            BodyId.JUPITER to NaturalRelationshipType.NEUTRAL,
            BodyId.VENUS to NaturalRelationshipType.NEUTRAL,
            BodyId.SATURN to NaturalRelationshipType.NEUTRAL,
        ),
        BodyId.MARS to mapOf(
            BodyId.SUN to NaturalRelationshipType.FRIEND,
            BodyId.MOON to NaturalRelationshipType.FRIEND,
            BodyId.MERCURY to NaturalRelationshipType.ENEMY,
            BodyId.JUPITER to NaturalRelationshipType.FRIEND,
            BodyId.VENUS to NaturalRelationshipType.NEUTRAL,
            BodyId.SATURN to NaturalRelationshipType.NEUTRAL,
        ),
        BodyId.MERCURY to mapOf(
            BodyId.SUN to NaturalRelationshipType.FRIEND,
            BodyId.MOON to NaturalRelationshipType.ENEMY,
            BodyId.MARS to NaturalRelationshipType.NEUTRAL,
            BodyId.JUPITER to NaturalRelationshipType.NEUTRAL,
            BodyId.VENUS to NaturalRelationshipType.FRIEND,
            BodyId.SATURN to NaturalRelationshipType.NEUTRAL,
        ),
        BodyId.JUPITER to mapOf(
            BodyId.SUN to NaturalRelationshipType.FRIEND,
            BodyId.MOON to NaturalRelationshipType.FRIEND,
            BodyId.MARS to NaturalRelationshipType.FRIEND,
            BodyId.MERCURY to NaturalRelationshipType.ENEMY,
            BodyId.VENUS to NaturalRelationshipType.ENEMY,
            BodyId.SATURN to NaturalRelationshipType.NEUTRAL,
        ),
        BodyId.VENUS to mapOf(
            BodyId.SUN to NaturalRelationshipType.ENEMY,
            BodyId.MOON to NaturalRelationshipType.ENEMY,
            BodyId.MARS to NaturalRelationshipType.NEUTRAL,
            BodyId.MERCURY to NaturalRelationshipType.FRIEND,
            BodyId.JUPITER to NaturalRelationshipType.NEUTRAL,
            BodyId.SATURN to NaturalRelationshipType.FRIEND,
        ),
        BodyId.SATURN to mapOf(
            BodyId.SUN to NaturalRelationshipType.ENEMY,
            BodyId.MOON to NaturalRelationshipType.ENEMY,
            BodyId.MARS to NaturalRelationshipType.ENEMY,
            BodyId.MERCURY to NaturalRelationshipType.FRIEND,
            BodyId.JUPITER to NaturalRelationshipType.NEUTRAL,
            BodyId.VENUS to NaturalRelationshipType.FRIEND,
        ),
    )

    /**
     * Returns the natural directional relationship from [source] to [target].
     * Rahu and Ketu are not applicable under classical 7-planet Parashara rules.
     */
    fun getNaturalRelationship(source: BodyId, target: BodyId): NaturalRelationshipType {
        if (source == target) return NaturalRelationshipType.NOT_APPLICABLE
        if (source == BodyId.RAHU || source == BodyId.KETU || target == BodyId.RAHU || target == BodyId.KETU) {
            return NaturalRelationshipType.NOT_APPLICABLE
        }
        return NATURAL_RELATIONSHIP_MATRIX[source]?.get(target) ?: NaturalRelationshipType.NOT_APPLICABLE
    }

    /**
     * Calculates the 1-indexed relative sign distance from [sourceRashiIndex] to [targetRashiIndex].
     * Range: 1 to 12.
     */
    fun calculateRelativeHouseDistance(sourceRashiIndex: Int, targetRashiIndex: Int): Int {
        val diff = (targetRashiIndex - sourceRashiIndex) % 12
        return if (diff < 0) diff + 12 + 1 else diff + 1
    }

    /**
     * Evaluates temporary relationship (Tatkalika Maitri) from [source] to [target]
     * based on relative sign positions.
     * BPHS Ch. 3, Sloka 59:
     * Houses 2, 3, 4, 10, 11, 12 from planet's sign = FRIEND.
     * Houses 1, 5, 6, 7, 8, 9 from planet's sign = ENEMY.
     */
    fun getTemporaryRelationship(
        source: BodyId,
        target: BodyId,
        sourceRashiIndex: Int,
        targetRashiIndex: Int,
    ): TemporaryRelationshipType {
        if (source == target) return TemporaryRelationshipType.NOT_APPLICABLE
        if (source == BodyId.RAHU || source == BodyId.KETU || target == BodyId.RAHU || target == BodyId.KETU) {
            return TemporaryRelationshipType.NOT_APPLICABLE
        }
        val distance = calculateRelativeHouseDistance(sourceRashiIndex, targetRashiIndex)
        return when (distance) {
            2, 3, 4, 10, 11, 12 -> TemporaryRelationshipType.FRIEND
            1, 5, 6, 7, 8, 9 -> TemporaryRelationshipType.ENEMY
            else -> TemporaryRelationshipType.NOT_APPLICABLE
        }
    }

    /**
     * Evaluates compound five-fold relationship (Panchadha Maitri) combining
     * natural and temporary relationships according to BPHS Ch. 3, Sloka 60:
     * - Friend + Friend = GREAT_FRIEND (+2)
     * - Friend + Enemy = NEUTRAL (0)
     * - Neutral + Friend = FRIEND (+1)
     * - Neutral + Enemy = ENEMY (-1)
     * - Enemy + Friend = NEUTRAL (0)
     * - Enemy + Enemy = GREAT_ENEMY (-2)
     */
    fun getCompoundRelationship(
        natural: NaturalRelationshipType,
        temporary: TemporaryRelationshipType,
    ): CompoundRelationshipType {
        if (natural == NaturalRelationshipType.NOT_APPLICABLE || temporary == TemporaryRelationshipType.NOT_APPLICABLE) {
            return CompoundRelationshipType.NOT_APPLICABLE
        }
        return when (natural) {
            NaturalRelationshipType.FRIEND -> when (temporary) {
                TemporaryRelationshipType.FRIEND -> CompoundRelationshipType.GREAT_FRIEND
                TemporaryRelationshipType.ENEMY -> CompoundRelationshipType.NEUTRAL
                TemporaryRelationshipType.NOT_APPLICABLE -> CompoundRelationshipType.NOT_APPLICABLE
            }
            NaturalRelationshipType.NEUTRAL -> when (temporary) {
                TemporaryRelationshipType.FRIEND -> CompoundRelationshipType.FRIEND
                TemporaryRelationshipType.ENEMY -> CompoundRelationshipType.ENEMY
                TemporaryRelationshipType.NOT_APPLICABLE -> CompoundRelationshipType.NOT_APPLICABLE
            }
            NaturalRelationshipType.ENEMY -> when (temporary) {
                TemporaryRelationshipType.FRIEND -> CompoundRelationshipType.NEUTRAL
                TemporaryRelationshipType.ENEMY -> CompoundRelationshipType.GREAT_ENEMY
                TemporaryRelationshipType.NOT_APPLICABLE -> CompoundRelationshipType.NOT_APPLICABLE
            }
            NaturalRelationshipType.NOT_APPLICABLE -> CompoundRelationshipType.NOT_APPLICABLE
        }
    }

    /**
     * Calculates all directional relationships between celestial bodies for the given D1 positions.
     */
    fun calculateRelationships(
        positions: List<BodyPosition>,
        chart: DivisionalChart = DivisionalChart.D1,
    ): List<PlanetaryRelationshipPosition> {
        val signMap = positions.associate { it.bodyId to it.rashiIndex }
        return calculateRelationshipsFromSignMap(signMap, chart)
    }

    /**
     * Calculates relationships from divisional chart placements.
     */
    fun calculateRelationshipsFromVargaPositions(
        vargaPositions: List<VargaPosition>,
        chart: DivisionalChart,
    ): List<PlanetaryRelationshipPosition> {
        val signMap = vargaPositions
            .filter { it.bodyId != null && !it.isLagna }
            .associate { it.bodyId!! to it.resultingRashiIndex }
        return calculateRelationshipsFromSignMap(signMap, chart)
    }

    /**
     * Calculates all pairwise directional relationships given a mapping of body to sign index.
     */
    fun calculateRelationshipsFromSignMap(
        signMap: Map<BodyId, Int>,
        chart: DivisionalChart,
    ): List<PlanetaryRelationshipPosition> {
        val results = mutableListOf<PlanetaryRelationshipPosition>()
        val bodies = BodyId.entries

        for (source in bodies) {
            val sourceRashi = signMap[source] ?: continue
            for (target in bodies) {
                if (source == target) continue
                val targetRashi = signMap[target] ?: continue

                val distance = calculateRelativeHouseDistance(sourceRashi, targetRashi)
                val natural = getNaturalRelationship(source, target)
                val temporary = getTemporaryRelationship(source, target, sourceRashi, targetRashi)
                val compound = getCompoundRelationship(natural, temporary)

                results.add(
                    PlanetaryRelationshipPosition(
                        sourceBodyId = source,
                        targetBodyId = target,
                        chart = chart,
                        naturalRelationship = natural,
                        temporaryRelationship = temporary,
                        compoundRelationship = compound,
                        sourceRashiIndex = sourceRashi,
                        targetRashiIndex = targetRashi,
                        relativeHouseDistance = distance,
                    ),
                )
            }
        }
        return results
    }
}
