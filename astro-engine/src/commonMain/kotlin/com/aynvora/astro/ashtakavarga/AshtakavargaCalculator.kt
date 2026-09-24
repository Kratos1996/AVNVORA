package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.zodiac.ZodiacCalculator

/**
 * Deterministic classical Ashtakavarga Calculator according to Parashara tradition (`PARASHARA_CLASSICAL_V1`).
 *
 * Implements:
 * - Bhinnashtakavarga (BAV) for the 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn)
 *   from the 8 classical contributors (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Lagna).
 * - Sarvashtakavarga (SAV) aggregate grid.
 * - Classical Bindu/Rekha totals and mathematical invariant verification.
 * - Explicit unsupported handling for lunar nodes (Rahu and Ketu).
 *
 * Reference: Brihat Parashara Hora Shastra, Chapters 66–73; Dr. B.V. Raman, *The Ashtakavarga System of Direction*.
 */
object AshtakavargaCalculator {

    const val RULESET_ID = "PARASHARA_CLASSICAL_V1"

    /**
     * Auspicious house positions (1-indexed from contributor's sign) per target planet and contributor.
     * Sources: BPHS Ch. 66–72; Raman Ch. 2.
     */
    val BAV_CONTRIBUTIONS: Map<BodyId, Map<AshtakavargaContributor, Set<Int>>> = mapOf(
        // Surya Ashtakavarga (BPHS Ch. 66) - Total = 48
        BodyId.SUN to mapOf(
            AshtakavargaContributor.SUN to setOf(1, 2, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.MOON to setOf(3, 6, 10, 11),
            AshtakavargaContributor.MARS to setOf(1, 2, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.MERCURY to setOf(3, 5, 6, 9, 10, 11, 12),
            AshtakavargaContributor.JUPITER to setOf(5, 6, 9, 11),
            AshtakavargaContributor.VENUS to setOf(6, 7, 12),
            AshtakavargaContributor.SATURN to setOf(1, 2, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.LAGNA to setOf(3, 4, 6, 10, 11, 12),
        ),
        // Chandra Ashtakavarga (BPHS Ch. 67) - Total = 49
        BodyId.MOON to mapOf(
            AshtakavargaContributor.SUN to setOf(3, 6, 7, 8, 10, 11),
            AshtakavargaContributor.MOON to setOf(1, 3, 6, 7, 10, 11),
            AshtakavargaContributor.MARS to setOf(2, 3, 5, 6, 9, 10, 11),
            AshtakavargaContributor.MERCURY to setOf(1, 3, 4, 5, 7, 8, 10, 11),
            AshtakavargaContributor.JUPITER to setOf(1, 4, 7, 8, 10, 11, 12),
            AshtakavargaContributor.VENUS to setOf(3, 4, 5, 7, 9, 10, 11),
            AshtakavargaContributor.SATURN to setOf(3, 5, 6, 11),
            AshtakavargaContributor.LAGNA to setOf(3, 6, 10, 11),
        ),
        // Mangala Ashtakavarga (BPHS Ch. 68) - Total = 39
        BodyId.MARS to mapOf(
            AshtakavargaContributor.SUN to setOf(3, 5, 6, 10, 11),
            AshtakavargaContributor.MOON to setOf(3, 6, 11),
            AshtakavargaContributor.MARS to setOf(1, 2, 4, 7, 8, 10, 11),
            AshtakavargaContributor.MERCURY to setOf(3, 5, 6, 11),
            AshtakavargaContributor.JUPITER to setOf(6, 10, 11, 12),
            AshtakavargaContributor.VENUS to setOf(6, 8, 11, 12),
            AshtakavargaContributor.SATURN to setOf(1, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.LAGNA to setOf(1, 3, 6, 10, 11),
        ),
        // Budha Ashtakavarga (BPHS Ch. 69) - Total = 54
        BodyId.MERCURY to mapOf(
            AshtakavargaContributor.SUN to setOf(5, 6, 9, 11, 12),
            AshtakavargaContributor.MOON to setOf(2, 4, 6, 8, 10, 11),
            AshtakavargaContributor.MARS to setOf(1, 2, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.MERCURY to setOf(1, 3, 5, 6, 9, 10, 11, 12),
            AshtakavargaContributor.JUPITER to setOf(6, 8, 11, 12),
            AshtakavargaContributor.VENUS to setOf(1, 2, 3, 4, 5, 8, 9, 11),
            AshtakavargaContributor.SATURN to setOf(1, 2, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.LAGNA to setOf(1, 2, 4, 6, 8, 10, 11),
        ),
        // Guru Ashtakavarga (BPHS Ch. 70) - Total = 56
        BodyId.JUPITER to mapOf(
            AshtakavargaContributor.SUN to setOf(1, 2, 3, 4, 7, 8, 9, 10, 11),
            AshtakavargaContributor.MOON to setOf(2, 5, 7, 9, 11),
            AshtakavargaContributor.MARS to setOf(1, 2, 4, 7, 8, 10, 11),
            AshtakavargaContributor.MERCURY to setOf(1, 2, 4, 5, 6, 9, 10, 11),
            AshtakavargaContributor.JUPITER to setOf(1, 2, 3, 4, 7, 8, 10, 11),
            AshtakavargaContributor.VENUS to setOf(2, 5, 6, 9, 10, 11),
            AshtakavargaContributor.SATURN to setOf(3, 5, 6, 12),
            AshtakavargaContributor.LAGNA to setOf(1, 2, 4, 5, 6, 7, 9, 10, 11),
        ),
        // Shukra Ashtakavarga (BPHS Ch. 71) - Total = 52
        BodyId.VENUS to mapOf(
            AshtakavargaContributor.SUN to setOf(8, 11, 12),
            AshtakavargaContributor.MOON to setOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
            AshtakavargaContributor.MARS to setOf(3, 5, 6, 9, 11, 12),
            AshtakavargaContributor.MERCURY to setOf(3, 5, 6, 9, 11),
            AshtakavargaContributor.JUPITER to setOf(5, 8, 9, 10, 11),
            AshtakavargaContributor.VENUS to setOf(1, 2, 3, 4, 5, 8, 9, 10, 11),
            AshtakavargaContributor.SATURN to setOf(3, 4, 5, 8, 9, 10, 11),
            AshtakavargaContributor.LAGNA to setOf(1, 2, 3, 4, 5, 8, 9, 11),
        ),
        // Shani Ashtakavarga (BPHS Ch. 72) - Total = 39
        BodyId.SATURN to mapOf(
            AshtakavargaContributor.SUN to setOf(1, 2, 4, 7, 8, 10, 11),
            AshtakavargaContributor.MOON to setOf(3, 6, 11),
            AshtakavargaContributor.MARS to setOf(3, 5, 6, 10, 11, 12),
            AshtakavargaContributor.MERCURY to setOf(6, 8, 9, 10, 11, 12),
            AshtakavargaContributor.JUPITER to setOf(5, 6, 11, 12),
            AshtakavargaContributor.VENUS to setOf(6, 11, 12),
            AshtakavargaContributor.SATURN to setOf(3, 5, 6, 11),
            AshtakavargaContributor.LAGNA to setOf(1, 3, 4, 6, 10, 11),
        ),
    )

    /**
     * Classical constant total Bindus for each of the 7 planets.
     */
    val EXPECTED_BAV_TOTALS: Map<BodyId, Int> = mapOf(
        BodyId.SUN to 48,
        BodyId.MOON to 49,
        BodyId.MARS to 39,
        BodyId.MERCURY to 54,
        BodyId.JUPITER to 56,
        BodyId.VENUS to 52,
        BodyId.SATURN to 39,
    )

    const val EXPECTED_SAV_TOTAL = 337

    /**
     * Extracts contributor Rashi indices ($0..11$) from body positions and Lagna.
     */
    fun extractContributorPositions(
        positions: List<BodyPosition>,
        lagna: LagnaPosition,
    ): Map<AshtakavargaContributor, Int> {
        val posMap = positions.associateBy { it.bodyId }
        val map = mutableMapOf<AshtakavargaContributor, Int>()

        posMap[BodyId.SUN]?.let { map[AshtakavargaContributor.SUN] = it.rashiIndex }
        posMap[BodyId.MOON]?.let { map[AshtakavargaContributor.MOON] = it.rashiIndex }
        posMap[BodyId.MARS]?.let { map[AshtakavargaContributor.MARS] = it.rashiIndex }
        posMap[BodyId.MERCURY]?.let { map[AshtakavargaContributor.MERCURY] = it.rashiIndex }
        posMap[BodyId.JUPITER]?.let { map[AshtakavargaContributor.JUPITER] = it.rashiIndex }
        posMap[BodyId.VENUS]?.let { map[AshtakavargaContributor.VENUS] = it.rashiIndex }
        posMap[BodyId.SATURN]?.let { map[AshtakavargaContributor.SATURN] = it.rashiIndex }
        map[AshtakavargaContributor.LAGNA] = lagna.rashiIndex

        return map
    }

    /**
     * Calculates Bhinnashtakavarga (BAV) for a specific target body.
     */
    fun calculateBhinnashtakavarga(
        targetBody: BodyId,
        contributorPositions: Map<AshtakavargaContributor, Int>,
        rulesetId: String = RULESET_ID,
    ): BhinnashtakavargaChart? {
        val contributionRules = BAV_CONTRIBUTIONS[targetBody] ?: return null

        // Grid of 8 contributors x 12 signs (each initialized to 0)
        val contributorGrid = mutableMapOf<AshtakavargaContributor, MutableList<Int>>()
        for (contributor in AshtakavargaContributor.entries) {
            contributorGrid[contributor] = MutableList(12) { 0 }
        }

        val signContributingBodies = List(12) { mutableListOf<AshtakavargaContributor>() }

        for ((contributor, relativeHouses) in contributionRules) {
            val contributorSign = contributorPositions[contributor] ?: continue
            val gridRow = contributorGrid[contributor] ?: continue

            for (houseOffset in relativeHouses) {
                val targetSign = (contributorSign + (houseOffset - 1)) % 12
                gridRow[targetSign] = 1
                signContributingBodies[targetSign].add(contributor)
            }
        }

        val signScores = (0 until 12).map { signIndex ->
            val contributors = signContributingBodies[signIndex].toList()
            val binduCount = contributors.size
            val rekhaCount = 8 - binduCount
            BhinnashtakavargaSignScore(
                rashiIndex = signIndex,
                rashiName = ZodiacCalculator.RASHI_NAMES[signIndex],
                binduCount = binduCount,
                rekhaCount = rekhaCount,
                contributingBodies = contributors,
            )
        }

        val totalBindus = signScores.sumOf { it.binduCount }
        val totalRekhas = signScores.sumOf { it.rekhaCount }

        val immutableGrid = contributorGrid.mapValues { it.value.toList() }

        return BhinnashtakavargaChart(
            targetBody = targetBody,
            rulesetId = rulesetId,
            signScores = signScores,
            totalBindus = totalBindus,
            totalRekhas = totalRekhas,
            contributorGrid = immutableGrid,
        )
    }

    /**
     * Aggregates 7 planetary BAV charts into the Sarvashtakavarga (SAV) chart.
     */
    fun calculateSarvashtakavarga(
        bavCharts: Map<BodyId, BhinnashtakavargaChart>,
        rulesetId: String = RULESET_ID,
    ): SarvashtakavargaChart {
        val classicalBodies = listOf(
            BodyId.SUN,
            BodyId.MOON,
            BodyId.MARS,
            BodyId.MERCURY,
            BodyId.JUPITER,
            BodyId.VENUS,
            BodyId.SATURN,
        )

        val signScores = (0 until 12).map { signIndex ->
            val planetBindus = mutableMapOf<BodyId, Int>()
            var signTotalBindus = 0

            for (body in classicalBodies) {
                val chart = bavCharts[body]
                val bindus = chart?.signScores?.getOrNull(signIndex)?.binduCount ?: 0
                planetBindus[body] = bindus
                signTotalBindus += bindus
            }

            val signTotalRekhas = 56 - signTotalBindus

            SarvashtakavargaSignScore(
                rashiIndex = signIndex,
                rashiName = ZodiacCalculator.RASHI_NAMES[signIndex],
                totalBindus = signTotalBindus,
                totalRekhas = signTotalRekhas,
                planetBindus = planetBindus,
            )
        }

        val grandTotalBindus = signScores.sumOf { it.totalBindus }
        val grandTotalRekhas = signScores.sumOf { it.totalRekhas }
        val isInvariantValid = (grandTotalBindus == EXPECTED_SAV_TOTAL)

        return SarvashtakavargaChart(
            rulesetId = rulesetId,
            signScores = signScores,
            grandTotalBindus = grandTotalBindus,
            grandTotalRekhas = grandTotalRekhas,
            isInvariantValid = isInvariantValid,
        )
    }

    /**
     * Complete calculation of Ashtakavarga from planetary positions and Lagna.
     */
    fun calculateAshtakavarga(
        positions: List<BodyPosition>,
        lagna: LagnaPosition,
        rulesetId: String = RULESET_ID,
    ): AshtakavargaResult {
        val contributorPositions = extractContributorPositions(positions, lagna)

        val classicalBodies = listOf(
            BodyId.SUN,
            BodyId.MOON,
            BodyId.MARS,
            BodyId.MERCURY,
            BodyId.JUPITER,
            BodyId.VENUS,
            BodyId.SATURN,
        )

        val bavMap = mutableMapOf<BodyId, BhinnashtakavargaChart>()
        for (body in classicalBodies) {
            val chart = calculateBhinnashtakavarga(body, contributorPositions, rulesetId)
            if (chart != null) {
                bavMap[body] = chart
            }
        }

        val sav = calculateSarvashtakavarga(bavMap, rulesetId)

        val completeness = if (bavMap.size == 7 && sav.isInvariantValid) {
            AshtakavargaCompleteness.COMPLETE
        } else {
            AshtakavargaCompleteness.PARTIAL
        }

        return AshtakavargaResult(
            rulesetId = rulesetId,
            bhinnashtakavarga = bavMap,
            sarvashtakavarga = sav,
            completeness = completeness,
            unsupportedBodies = listOf(BodyId.RAHU, BodyId.KETU),
        )
    }
}
