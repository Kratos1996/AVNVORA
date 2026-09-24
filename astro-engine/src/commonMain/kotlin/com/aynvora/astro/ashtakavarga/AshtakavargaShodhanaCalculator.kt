package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.zodiac.ZodiacCalculator

/**
 * Deterministic classical Ashtakavarga Shodhana Calculator according to Parashara tradition (`PARASHARA_CLASSICAL_V1`).
 *
 * Implements:
 * 1. **Trikona Shodhana** (Triplicity Reduction, BPHS Ch. 73; Raman Ch. 4).
 * 2. **Ekadhipatya Shodhana** (Dual-Ownership Reduction, BPHS Ch. 74; Raman Ch. 5).
 * 3. **Shodhita Sarvashtakavarga** aggregation summing reduced BAVs sign-by-sign.
 *
 * References:
 * - *Brihat Parashara Hora Shastra*, Chapters 73 & 74 (Santhanam / Sharma editions).
 * - Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapters 4 & 5.
 * - *Phaladeepika*, Chapter 24.
 * - *Jataka Parijata*, Chapter 10.
 */
object AshtakavargaShodhanaCalculator {

    const val RULESET_ID = "PARASHARA_CLASSICAL_V1"

    /**
     * The 4 classical triplicities (Trikonas), each spaced 120 degrees (5th and 9th houses) apart.
     * 1. Fire: Mesha (0), Simha (4), Dhanus (8)
     * 2. Earth: Vrishabha (1), Kanya (5), Makara (9)
     * 3. Air: Mithuna (2), Tula (6), Kumbha (10)
     * 4. Water: Karka (3), Vrishchika (7), Meena (11)
     */
    val TRIKONAS: List<List<Int>> = listOf(
        listOf(0, 4, 8),   // Agni (Fire): Aries, Leo, Sagittarius
        listOf(1, 5, 9),   // Prithvi (Earth): Taurus, Virgo, Capricorn
        listOf(2, 6, 10),  // Vayu (Air): Gemini, Libra, Aquarius
        listOf(3, 7, 11),  // Jala (Water): Cancer, Scorpio, Pisces
    )

    /**
     * The 5 pairs of signs owned by planets with dual rulership.
     * - Mars: Aries (0) & Scorpio (7)
     * - Venus: Taurus (1) & Libra (6)
     * - Mercury: Gemini (2) & Virgo (5)
     * - Jupiter: Sagittarius (8) & Pisces (11)
     * - Saturn: Capricorn (9) & Aquarius (10)
     */
    val DUAL_OWNERSHIP_PAIRS: List<Pair<Int, Int>> = listOf(
        0 to 7,   // Mars: Mesha & Vrishchika
        1 to 6,   // Venus: Vrishabha & Tula
        2 to 5,   // Mercury: Mithuna & Kanya
        8 to 11,  // Jupiter: Dhanus & Meena
        9 to 10,  // Saturn: Makara & Kumbha
    )

    /**
     * Single-ownership signs exempt from Ekadhipatya Shodhana.
     * - Cancer (3): Owned by Moon.
     * - Leo (4): Owned by Sun.
     */
    val EXEMPT_SINGLE_OWNERSHIP_SIGNS: Set<Int> = setOf(3, 4)

    /**
     * The 7 classical physical planets that qualify as occupying planets.
     * In classical Parashara Ashtakavarga, Rahu and Ketu do NOT possess signs
     * and do NOT count as occupying planets for Ekadhipatya Shodhana.
     */
    val CLASSICAL_OCCUPYING_BODIES: Set<BodyId> = setOf(
        BodyId.SUN,
        BodyId.MOON,
        BodyId.MARS,
        BodyId.MERCURY,
        BodyId.JUPITER,
        BodyId.VENUS,
        BodyId.SATURN,
    )

    /**
     * Extracts the set of sidereal Rashi indices ($0..11$) occupied by the 7 classical planets in D1.
     */
    fun extractOccupiedSigns(positions: List<BodyPosition>): Set<Int> =
        positions
            .filter { it.bodyId in CLASSICAL_OCCUPYING_BODIES }
            .map { it.rashiIndex }
            .toSet()

    /**
     * Performs classical Trikona Shodhana (Triplicity Reduction) on a 12-sign Bindu list.
     *
     * Rules per BPHS Ch. 73 & Raman Ch. 4:
     * - For each triplicity $(s_0, s_1, s_2)$ with values $(v_0, v_1, v_2)$:
     *   1. If all 3 signs have 0: all remain 0.
     *   2. If exactly 2 signs have 0: the third sign is also reduced to 0 (all become 0).
     *   3. If exactly 1 sign has 0: no reduction is made in this triplicity.
     *   4. If all 3 signs are non-zero:
     *      a. If all 3 figures are equal: all 3 become 0.
     *      b. If figures are unequal: subtract the minimum figure from all three signs.
     */
    fun applyTrikonaShodhana(rawScores: List<Int>): List<Int> {
        require(rawScores.size == 12) { "Raw scores list must contain exactly 12 signs" }
        val result = rawScores.toMutableList()

        for (trikona in TRIKONAS) {
            val i0 = trikona[0]
            val i1 = trikona[1]
            val i2 = trikona[2]

            val v0 = result[i0]
            val v1 = result[i1]
            val v2 = result[i2]

            val zeroCount = listOf(v0, v1, v2).count { it == 0 }
            when (zeroCount) {
                3 -> {
                    // All three 0: remain 0
                    result[i0] = 0
                    result[i1] = 0
                    result[i2] = 0
                }
                2 -> {
                    // Two are 0: third becomes 0
                    result[i0] = 0
                    result[i1] = 0
                    result[i2] = 0
                }
                1 -> {
                    // One is 0: no reduction in this triplicity
                }
                0 -> {
                    // All three non-zero
                    if (v0 == v1 && v1 == v2) {
                        // All equal: all become 0
                        result[i0] = 0
                        result[i1] = 0
                        result[i2] = 0
                    } else {
                        // Unequal: subtract minimum
                        val minVal = minOf(v0, minOf(v1, v2))
                        result[i0] = v0 - minVal
                        result[i1] = v1 - minVal
                        result[i2] = v2 - minVal
                    }
                }
            }
        }

        return result.toList()
    }

    /**
     * Performs classical Ekadhipatya Shodhana (Dual-Ownership Reduction) on Trikona-reduced scores.
     *
     * Rules per BPHS Ch. 74 & Raman Ch. 5:
     * - Applied to the 5 dual-ownership sign pairs. Single-ownership signs (Cancer & Leo) are exempt.
     * - For each pair $(S_1, S_2)$ with values $(v_1, v_2)$:
     *   1. **Zero Exemption**: If either $v_1 == 0$ or $v_2 == 0$, no reduction is made.
     *   2. **Both Occupied**: If both signs are occupied by at least one classical planet, no reduction.
     *   3. **Both Unoccupied**:
     *      a. If $v_1 == v_2$: both are reduced to 0.
     *      b. If $v_1 \ne v_2$: the larger figure is reduced to the smaller figure (both become $\min(v_1, v_2)$).
     *   4. **One Occupied, One Unoccupied**:
     *      Let $v_{\text{occ}}$ be the occupied sign's figure, $v_{\text{unocc}}$ the unoccupied sign's figure.
     *      The occupied sign retains $v_{\text{occ}}$.
     *      a. If $v_{\text{occ}} \ge v_{\text{unocc}}$: unoccupied sign becomes 0.
     *      b. If $v_{\text{occ}} < v_{\text{unocc}}$: unoccupied sign is reduced to $v_{\text{occ}}$.
     */
    fun applyEkadhipatyaShodhana(
        trikonaScores: List<Int>,
        occupiedSigns: Set<Int>,
    ): List<Int> {
        require(trikonaScores.size == 12) { "Trikona scores list must contain exactly 12 signs" }
        val result = trikonaScores.toMutableList()

        for (pair in DUAL_OWNERSHIP_PAIRS) {
            val s1 = pair.first
            val s2 = pair.second

            val v1 = result[s1]
            val v2 = result[s2]

            // Rule 1: Zero Exemption
            if (v1 == 0 || v2 == 0) {
                continue
            }

            val occ1 = s1 in occupiedSigns
            val occ2 = s2 in occupiedSigns

            // Rule 2: Both signs occupied
            if (occ1 && occ2) {
                continue
            }

            // Rule 3: Both signs unoccupied
            if (!occ1 && !occ2) {
                if (v1 == v2) {
                    result[s1] = 0
                    result[s2] = 0
                } else {
                    val minVal = minOf(v1, v2)
                    result[s1] = minVal
                    result[s2] = minVal
                }
                continue
            }

            // Rule 4: One sign occupied, one unoccupied
            val (occSign, unoccSign, occVal, unoccVal) = if (occ1) {
                ReductionPair(occSign = s1, unoccSign = s2, occVal = v1, unoccVal = v2)
            } else {
                ReductionPair(occSign = s2, unoccSign = s1, occVal = v2, unoccVal = v1)
            }

            result[occSign] = occVal
            if (occVal >= unoccVal) {
                result[unoccSign] = 0
            } else {
                result[unoccSign] = occVal
            }
        }

        return result.toList()
    }

    /**
     * Calculates Shodhita Bhinnashtakavarga for a single planet.
     */
    fun calculateShodhitaBhinnashtakavarga(
        bavChart: BhinnashtakavargaChart,
        occupiedSigns: Set<Int>,
    ): ShodhitaBhinnashtakavargaChart {
        val rawScores = bavChart.signScores.map { it.binduCount }
        val trikonaScores = applyTrikonaShodhana(rawScores)
        val ekadhipatyaScores = applyEkadhipatyaShodhana(trikonaScores, occupiedSigns)

        val signScores = (0 until 12).map { signIndex ->
            ShodhitaBhinnashtakavargaSignScore(
                rashiIndex = signIndex,
                rashiName = ZodiacCalculator.RASHI_NAMES[signIndex],
                rawBindus = rawScores[signIndex],
                trikonaReducedBindus = trikonaScores[signIndex],
                ekadhipatyaReducedBindus = ekadhipatyaScores[signIndex],
            )
        }

        return ShodhitaBhinnashtakavargaChart(
            targetBody = bavChart.targetBody,
            rulesetId = bavChart.rulesetId,
            signScores = signScores,
            rawTotalBindus = rawScores.sum(),
            trikonaTotalBindus = trikonaScores.sum(),
            shodhitaTotalBindus = ekadhipatyaScores.sum(),
        )
    }

    /**
     * Calculates Shodhita Sarvashtakavarga by aggregating the 7 Shodhita BAV charts sign by sign.
     */
    fun calculateShodhitaSarvashtakavarga(
        shodhitaBavMap: Map<BodyId, ShodhitaBhinnashtakavargaChart>,
        rulesetId: String = RULESET_ID,
    ): ShodhitaSarvashtakavargaChart {
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
            var rawSignTotal = 0
            var trikonaSignTotal = 0
            var shodhitaSignTotal = 0
            val planetShodhita = mutableMapOf<BodyId, Int>()

            for (body in classicalBodies) {
                val chart = shodhitaBavMap[body]
                val score = chart?.signScores?.getOrNull(signIndex)
                if (score != null) {
                    rawSignTotal += score.rawBindus
                    trikonaSignTotal += score.trikonaReducedBindus
                    shodhitaSignTotal += score.ekadhipatyaReducedBindus
                    planetShodhita[body] = score.ekadhipatyaReducedBindus
                }
            }

            ShodhitaSarvashtakavargaSignScore(
                rashiIndex = signIndex,
                rashiName = ZodiacCalculator.RASHI_NAMES[signIndex],
                rawTotalBindus = rawSignTotal,
                trikonaTotalBindus = trikonaSignTotal,
                shodhitaTotalBindus = shodhitaSignTotal,
                planetShodhitaBindus = planetShodhita,
            )
        }

        return ShodhitaSarvashtakavargaChart(
            rulesetId = rulesetId,
            signScores = signScores,
            grandTotalRawBindus = signScores.sumOf { it.rawTotalBindus },
            grandTotalTrikonaBindus = signScores.sumOf { it.trikonaTotalBindus },
            grandTotalShodhitaBindus = signScores.sumOf { it.shodhitaTotalBindus },
        )
    }

    /**
     * Complete calculation of Shodhita Ashtakavarga from existing AshtakavargaResult and planetary positions.
     */
    fun calculateShodhana(
        ashtakavargaResult: AshtakavargaResult,
        positions: List<BodyPosition>,
    ): ShodhitaAshtakavargaResult {
        val occupiedSigns = extractOccupiedSigns(positions)

        val shodhitaBavMap = mutableMapOf<BodyId, ShodhitaBhinnashtakavargaChart>()
        for ((body, bavChart) in ashtakavargaResult.bhinnashtakavarga) {
            shodhitaBavMap[body] = calculateShodhitaBhinnashtakavarga(bavChart, occupiedSigns)
        }

        val shodhitaSav = calculateShodhitaSarvashtakavarga(
            shodhitaBavMap = shodhitaBavMap,
            rulesetId = ashtakavargaResult.rulesetId,
        )

        val completeness = if (shodhitaBavMap.size == 7 && ashtakavargaResult.completeness == AshtakavargaCompleteness.COMPLETE) {
            AshtakavargaCompleteness.COMPLETE
        } else {
            AshtakavargaCompleteness.PARTIAL
        }

        return ShodhitaAshtakavargaResult(
            rulesetId = ashtakavargaResult.rulesetId,
            shodhitaBhinnashtakavarga = shodhitaBavMap,
            shodhitaSarvashtakavarga = shodhitaSav,
            completeness = completeness,
            unsupportedBodies = ashtakavargaResult.unsupportedBodies,
        )
    }

    private data class ReductionPair(
        val occSign: Int,
        val unoccSign: Int,
        val occVal: Int,
        val unoccVal: Int,
    )
}
