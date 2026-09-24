package com.aynvora.astro.dignity

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.relationship.CompoundRelationshipType
import com.aynvora.astro.relationship.PlanetaryRelationshipCalculator
import com.aynvora.astro.varga.DivisionalChart
import com.aynvora.astro.varga.VargaPosition

/**
 * Deterministic facts-only calculator for traditional planetary dignities
 * under the Parashara classical tradition (Brihat Parashara Hora Shastra, Chapter 3).
 */
object PlanetaryDignityCalculator {

    /**
     * Classical planetary sign rulership (Swakshetra)
     * Rule: ASTRO-R25
     */
    val SIGN_LORDS: Map<Int, BodyId> = mapOf(
        0 to BodyId.MARS, // Aries
        1 to BodyId.VENUS, // Taurus
        2 to BodyId.MERCURY, // Gemini
        3 to BodyId.MOON, // Cancer
        4 to BodyId.SUN, // Leo
        5 to BodyId.MERCURY, // Virgo
        6 to BodyId.VENUS, // Libra
        7 to BodyId.MARS, // Scorpio
        8 to BodyId.JUPITER, // Sagittarius
        9 to BodyId.SATURN, // Capricorn
        10 to BodyId.SATURN, // Aquarius
        11 to BodyId.JUPITER, // Pisces
    )

    fun getSignLord(rashiIndex: Int): BodyId? = SIGN_LORDS[rashiIndex]

    /**
     * Exaltation definitions: sign index, deep exaltation degree (Parama Uchcha),
     * and optional degree range within sign (for Moon and Mercury).
     * Rule: ASTRO-R26
     */
    data class ExaltationDef(
        val rashiIndex: Int,
        val deepDegree: Double,
        val minDegree: Double = 0.0,
        val maxDegree: Double = 30.0,
    )

    val EXALTATION_MAP: Map<BodyId, ExaltationDef> = mapOf(
        BodyId.SUN to ExaltationDef(rashiIndex = 0, deepDegree = 10.0),
        BodyId.MOON to ExaltationDef(rashiIndex = 1, deepDegree = 3.0, minDegree = 0.0, maxDegree = 3.0),
        BodyId.MARS to ExaltationDef(rashiIndex = 9, deepDegree = 28.0),
        BodyId.MERCURY to ExaltationDef(rashiIndex = 5, deepDegree = 15.0, minDegree = 0.0, maxDegree = 15.0),
        BodyId.JUPITER to ExaltationDef(rashiIndex = 3, deepDegree = 5.0),
        BodyId.VENUS to ExaltationDef(rashiIndex = 11, deepDegree = 27.0),
        BodyId.SATURN to ExaltationDef(rashiIndex = 6, deepDegree = 20.0),
    )

    /**
     * Debilitation definitions: sign index and deep debilitation degree (Parama Neecha).
     * Rule: ASTRO-R26
     */
    data class DebilitationDef(
        val rashiIndex: Int,
        val deepDegree: Double,
    )

    val DEBILITATION_MAP: Map<BodyId, DebilitationDef> = mapOf(
        BodyId.SUN to DebilitationDef(rashiIndex = 6, deepDegree = 10.0),
        BodyId.MOON to DebilitationDef(rashiIndex = 7, deepDegree = 3.0),
        BodyId.MARS to DebilitationDef(rashiIndex = 3, deepDegree = 28.0),
        BodyId.MERCURY to DebilitationDef(rashiIndex = 11, deepDegree = 15.0),
        BodyId.JUPITER to DebilitationDef(rashiIndex = 9, deepDegree = 5.0),
        BodyId.VENUS to DebilitationDef(rashiIndex = 5, deepDegree = 27.0),
        BodyId.SATURN to DebilitationDef(rashiIndex = 0, deepDegree = 20.0),
    )

    /**
     * Moolatrikona definitions: sign index and degree range [startDegree, endDegree).
     * Rule: ASTRO-R27
     */
    data class MoolatrikonaDef(
        val rashiIndex: Int,
        val startDegree: Double,
        val endDegree: Double,
    )

    val MOOLATRIKONA_MAP: Map<BodyId, MoolatrikonaDef> = mapOf(
        BodyId.SUN to MoolatrikonaDef(rashiIndex = 4, startDegree = 0.0, endDegree = 20.0),
        BodyId.MOON to MoolatrikonaDef(rashiIndex = 1, startDegree = 3.0, endDegree = 30.0),
        BodyId.MARS to MoolatrikonaDef(rashiIndex = 0, startDegree = 0.0, endDegree = 12.0),
        BodyId.MERCURY to MoolatrikonaDef(rashiIndex = 5, startDegree = 15.0, endDegree = 20.0),
        BodyId.JUPITER to MoolatrikonaDef(rashiIndex = 8, startDegree = 0.0, endDegree = 10.0),
        BodyId.VENUS to MoolatrikonaDef(rashiIndex = 6, startDegree = 0.0, endDegree = 15.0),
        BodyId.SATURN to MoolatrikonaDef(rashiIndex = 10, startDegree = 0.0, endDegree = 20.0),
    )

    /**
     * Own signs (Swakshetra) for each planet.
     * Rule: ASTRO-R25
     */
    val OWN_SIGNS_MAP: Map<BodyId, Set<Int>> = mapOf(
        BodyId.SUN to setOf(4),
        BodyId.MOON to setOf(3),
        BodyId.MARS to setOf(0, 7),
        BodyId.MERCURY to setOf(2, 5),
        BodyId.JUPITER to setOf(8, 11),
        BodyId.VENUS to setOf(1, 6),
        BodyId.SATURN to setOf(9, 10),
    )

    /**
     * Evaluates the planetary dignity for a given body placed in [rashiIndex] at [degreeInSign],
     * considering other planetary placements in [signMap] for temporary relationship calculation.
     */
    fun evaluateDignity(
        bodyId: BodyId,
        rashiIndex: Int,
        degreeInSign: Double,
        signMap: Map<BodyId, Int>,
        chart: DivisionalChart = DivisionalChart.D1,
    ): PlanetaryDignityPosition {
        if (bodyId == BodyId.RAHU || bodyId == BodyId.KETU) {
            return PlanetaryDignityPosition(
                bodyId = bodyId,
                chart = chart,
                sourceRashiIndex = rashiIndex,
                signLordBodyId = getSignLord(rashiIndex),
                dignityType = DignityType.NOT_APPLICABLE,
                isExalted = false,
                isDebilitated = false,
                isMoolatrikona = false,
                isOwnSign = false,
                degreeInSign = degreeInSign,
                ruleId = "ASTRO-R26-PARASHARA-NODES-EXCLUDED",
            )
        }

        val exaltDef = EXALTATION_MAP[bodyId]
        val debDef = DEBILITATION_MAP[bodyId]
        val moolaDef = MOOLATRIKONA_MAP[bodyId]
        val ownSigns = OWN_SIGNS_MAP[bodyId] ?: emptySet()
        val signLord = getSignLord(rashiIndex)

        // 1. Exaltation check
        if (exaltDef != null && rashiIndex == exaltDef.rashiIndex) {
            if (degreeInSign >= exaltDef.minDegree && degreeInSign < exaltDef.maxDegree) {
                return PlanetaryDignityPosition(
                    bodyId = bodyId,
                    chart = chart,
                    sourceRashiIndex = rashiIndex,
                    signLordBodyId = signLord,
                    dignityType = DignityType.EXALTATION,
                    isExalted = true,
                    isDebilitated = false,
                    isMoolatrikona = false,
                    isOwnSign = false,
                    deepExaltationDegree = exaltDef.deepDegree,
                    deepDebilitationDegree = debDef?.deepDegree,
                    degreeInSign = degreeInSign,
                    ruleId = "ASTRO-R26-EXALTATION",
                )
            }
        }

        // 2. Debilitation check
        if (debDef != null && rashiIndex == debDef.rashiIndex) {
            return PlanetaryDignityPosition(
                bodyId = bodyId,
                chart = chart,
                sourceRashiIndex = rashiIndex,
                signLordBodyId = signLord,
                dignityType = DignityType.DEBILITATION,
                isExalted = false,
                isDebilitated = true,
                isMoolatrikona = false,
                isOwnSign = false,
                deepExaltationDegree = exaltDef?.deepDegree,
                deepDebilitationDegree = debDef.deepDegree,
                degreeInSign = degreeInSign,
                ruleId = "ASTRO-R26-DEBILITATION",
            )
        }

        // 3. Moolatrikona check
        if (moolaDef != null && rashiIndex == moolaDef.rashiIndex) {
            if (degreeInSign >= moolaDef.startDegree && degreeInSign < moolaDef.endDegree) {
                return PlanetaryDignityPosition(
                    bodyId = bodyId,
                    chart = chart,
                    sourceRashiIndex = rashiIndex,
                    signLordBodyId = signLord,
                    dignityType = DignityType.MOOLATRIKONA,
                    isExalted = false,
                    isDebilitated = false,
                    isMoolatrikona = true,
                    isOwnSign = false,
                    deepExaltationDegree = exaltDef?.deepDegree,
                    deepDebilitationDegree = debDef?.deepDegree,
                    degreeInSign = degreeInSign,
                    ruleId = "ASTRO-R27-MOOLATRIKONA",
                )
            }
        }

        // 4. Own Sign check
        if (rashiIndex in ownSigns) {
            return PlanetaryDignityPosition(
                bodyId = bodyId,
                chart = chart,
                sourceRashiIndex = rashiIndex,
                signLordBodyId = signLord,
                dignityType = DignityType.OWN_SIGN,
                isExalted = false,
                isDebilitated = false,
                isMoolatrikona = false,
                isOwnSign = true,
                deepExaltationDegree = exaltDef?.deepDegree,
                deepDebilitationDegree = debDef?.deepDegree,
                degreeInSign = degreeInSign,
                ruleId = "ASTRO-R25-OWN_SIGN",
            )
        }

        // 5. Sign Lord Compound Relationship check
        val compoundDignity = if (signLord != null) {
            val lordRashi = signMap[signLord]
            if (lordRashi != null) {
                val natural = PlanetaryRelationshipCalculator.getNaturalRelationship(bodyId, signLord)
                val temporary = PlanetaryRelationshipCalculator.getTemporaryRelationship(bodyId, signLord, rashiIndex, lordRashi)
                val compound = PlanetaryRelationshipCalculator.getCompoundRelationship(natural, temporary)
                when (compound) {
                    CompoundRelationshipType.GREAT_FRIEND -> DignityType.GREAT_FRIEND_SIGN
                    CompoundRelationshipType.FRIEND -> DignityType.FRIEND_SIGN
                    CompoundRelationshipType.NEUTRAL -> DignityType.NEUTRAL_SIGN
                    CompoundRelationshipType.ENEMY -> DignityType.ENEMY_SIGN
                    CompoundRelationshipType.GREAT_ENEMY -> DignityType.GREAT_ENEMY_SIGN
                    CompoundRelationshipType.NOT_APPLICABLE -> DignityType.NOT_APPLICABLE
                }
            } else {
                // If sign lord location not present, fall back to natural relationship
                val natural = PlanetaryRelationshipCalculator.getNaturalRelationship(bodyId, signLord)
                when (natural) {
                    com.aynvora.astro.relationship.NaturalRelationshipType.FRIEND -> DignityType.FRIEND_SIGN
                    com.aynvora.astro.relationship.NaturalRelationshipType.NEUTRAL -> DignityType.NEUTRAL_SIGN
                    com.aynvora.astro.relationship.NaturalRelationshipType.ENEMY -> DignityType.ENEMY_SIGN
                    com.aynvora.astro.relationship.NaturalRelationshipType.NOT_APPLICABLE -> DignityType.NOT_APPLICABLE
                }
            }
        } else {
            DignityType.NOT_APPLICABLE
        }

        return PlanetaryDignityPosition(
            bodyId = bodyId,
            chart = chart,
            sourceRashiIndex = rashiIndex,
            signLordBodyId = signLord,
            dignityType = compoundDignity,
            isExalted = false,
            isDebilitated = false,
            isMoolatrikona = false,
            isOwnSign = false,
            deepExaltationDegree = exaltDef?.deepDegree,
            deepDebilitationDegree = debDef?.deepDegree,
            degreeInSign = degreeInSign,
            ruleId = "ASTRO-R31-RESIDENTIAL",
        )
    }

    /**
     * Calculates dignities for all bodies in the provided D1 positions.
     */
    fun calculateDignities(
        positions: List<BodyPosition>,
        chart: DivisionalChart = DivisionalChart.D1,
    ): List<PlanetaryDignityPosition> {
        val signMap = positions.associate { it.bodyId to it.rashiIndex }
        return positions.map { pos ->
            evaluateDignity(
                bodyId = pos.bodyId,
                rashiIndex = pos.rashiIndex,
                degreeInSign = pos.degreeInRashi,
                signMap = signMap,
                chart = chart,
            )
        }
    }

    /**
     * Calculates dignities for all bodies in a divisional chart.
     */
    fun calculateDignitiesFromVargaPositions(
        vargaPositions: List<VargaPosition>,
        chart: DivisionalChart,
    ): List<PlanetaryDignityPosition> {
        val planetPositions = vargaPositions.filter { it.bodyId != null && !it.isLagna }
        val signMap = planetPositions.associate { it.bodyId!! to it.resultingRashiIndex }
        return planetPositions.map { pos ->
            evaluateDignity(
                bodyId = pos.bodyId!!,
                rashiIndex = pos.resultingRashiIndex,
                degreeInSign = pos.degreeInResultingRashi,
                signMap = signMap,
                chart = chart,
            )
        }
    }
}
