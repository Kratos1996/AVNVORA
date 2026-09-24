package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition

/**
 * Deterministic classical Ashtakavarga Pinda Calculator according to Parashara tradition (`PARASHARA_CLASSICAL_V1`).
 *
 * Implements:
 * 1. **Rashi Pinda**: Zodiacal product summing (Shodhita Bindus * Rashi Gunakara) across all 12 signs.
 * 2. **Graha Pinda**: Planetary product summing (Shodhita Bindus in natal sign of planet P * Graha Gunakara of P)
 *    across all 7 classical planets.
 * 3. **Shodhya Pinda**: Sum of Rashi Pinda and Graha Pinda.
 *
 * References:
 * - *Brihat Parashara Hora Shastra*, Chapters 74 & 75 (Santhanam / Sharma editions).
 * - Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Chapter 6 ("Pindas").
 * - *Phaladeepika*, Chapter 24.
 * - *Jataka Parijata*, Chapter 10.
 */
object AshtakavargaPindaCalculator {

    const val RULESET_ID = "PARASHARA_CLASSICAL_V1"

    /**
     * Classical Rashi Gunakaras (Zodiac Sign Multipliers) per BPHS & Raman:
     * - Aries (0): 7
     * - Taurus (1): 10
     * - Gemini (2): 8
     * - Cancer (3): 4
     * - Leo (4): 10
     * - Virgo (5): 5
     * - Libra (6): 7
     * - Scorpio (7): 8
     * - Sagittarius (8): 9
     * - Capricorn (9): 5
     * - Aquarius (10): 11
     * - Pisces (11): 12
     */
    val RASI_GUNAKARAS: Map<Int, Int> = mapOf(
        0 to 7,   // Mesha
        1 to 10,  // Vrishabha
        2 to 8,   // Mithuna
        3 to 4,   // Karka
        4 to 10,  // Simha
        5 to 5,   // Kanya
        6 to 7,   // Tula
        7 to 8,   // Vrishchika
        8 to 9,   // Dhanus
        9 to 5,   // Makara
        10 to 11, // Kumbha
        11 to 12, // Meena
    )

    /**
     * Classical Graha Gunakaras (Planetary Multipliers) per BPHS & Raman:
     * - Sun: 5
     * - Moon: 5
     * - Mars: 8
     * - Mercury: 5
     * - Jupiter: 10
     * - Venus: 7
     * - Saturn: 5
     */
    val GRAHA_GUNAKARAS: Map<BodyId, Int> = mapOf(
        BodyId.SUN to 5,
        BodyId.MOON to 5,
        BodyId.MARS to 8,
        BodyId.MERCURY to 5,
        BodyId.JUPITER to 10,
        BodyId.VENUS to 7,
        BodyId.SATURN to 5,
    )

    /**
     * The 7 classical planets eligible for Pinda calculations.
     */
    val CLASSICAL_GRAHAS: List<BodyId> = listOf(
        BodyId.SUN,
        BodyId.MOON,
        BodyId.MARS,
        BodyId.MERCURY,
        BodyId.JUPITER,
        BodyId.VENUS,
        BodyId.SATURN,
    )

    /**
     * Calculates Rashi Pinda, Graha Pinda, and Shodhya Pinda for a single planet
     * using its Shodhita Bhinnashtakavarga chart and the natal placements of the 7 planets.
     */
    fun calculatePlanetaryPinda(
        shodhitaBav: ShodhitaBhinnashtakavargaChart,
        planetPositions: Map<BodyId, Int>,
    ): PlanetaryPindaResult {
        require(shodhitaBav.signScores.size == 12) { "Shodhita BAV must contain exactly 12 signs" }

        val shodhitaBindus = shodhitaBav.signScores.map { it.shodhitaBindus }

        // 1. Rashi Pinda = Sum of (Shodhita Bindus * Rashi Gunakara)
        val rasiContributions = mutableMapOf<Int, Int>()
        var rasiPinda = 0
        for (sign in 0 until 12) {
            val mult = RASI_GUNAKARAS[sign] ?: 0
            val bindus = shodhitaBindus[sign]
            val contribution = bindus * mult
            rasiContributions[sign] = contribution
            rasiPinda += contribution
        }

        // 2. Graha Pinda = Sum of (Shodhita Bindus in natal sign of planet P * Graha Gunakara of planet P)
        val grahaContributions = mutableMapOf<BodyId, Int>()
        var grahaPinda = 0
        for (body in CLASSICAL_GRAHAS) {
            val sign = planetPositions[body]
            if (sign != null && sign in 0 until 12) {
                val mult = GRAHA_GUNAKARAS[body] ?: 0
                val bindus = shodhitaBindus[sign]
                val contribution = bindus * mult
                grahaContributions[body] = contribution
                grahaPinda += contribution
            }
        }

        // 3. Shodhya Pinda = Rashi Pinda + Graha Pinda
        val shodhyaPinda = rasiPinda + grahaPinda

        return PlanetaryPindaResult(
            targetBody = shodhitaBav.targetBody,
            rulesetId = shodhitaBav.rulesetId,
            rasiPinda = rasiPinda,
            grahaPinda = grahaPinda,
            shodhyaPinda = shodhyaPinda,
            rasiContributions = rasiContributions,
            grahaContributions = grahaContributions,
        )
    }

    /**
     * Calculates Pindas across all 7 classical planets from a Shodhita Ashtakavarga result
     * and natal body positions.
     */
    fun calculatePindas(
        shodhitaAshtakavarga: ShodhitaAshtakavargaResult,
        positions: List<BodyPosition>,
    ): AshtakavargaPindaResult {
        val planetPositions = positions
            .filter { it.bodyId in CLASSICAL_GRAHAS }
            .associate { it.bodyId to it.rashiIndex }

        val planetaryPindas = mutableMapOf<BodyId, PlanetaryPindaResult>()
        for (body in CLASSICAL_GRAHAS) {
            val shodhitaChart = shodhitaAshtakavarga.shodhitaBhinnashtakavarga[body]
            if (shodhitaChart != null) {
                planetaryPindas[body] = calculatePlanetaryPinda(shodhitaChart, planetPositions)
            }
        }

        val totalRasiPinda = planetaryPindas.values.sumOf { it.rasiPinda }
        val totalGrahaPinda = planetaryPindas.values.sumOf { it.grahaPinda }
        val totalShodhyaPinda = planetaryPindas.values.sumOf { it.shodhyaPinda }

        val completeness = if (planetaryPindas.size == 7 && shodhitaAshtakavarga.completeness == AshtakavargaCompleteness.COMPLETE) {
            AshtakavargaCompleteness.COMPLETE
        } else {
            AshtakavargaCompleteness.PARTIAL
        }

        return AshtakavargaPindaResult(
            rulesetId = shodhitaAshtakavarga.rulesetId,
            planetaryPindas = planetaryPindas,
            totalRasiPinda = totalRasiPinda,
            totalGrahaPinda = totalGrahaPinda,
            totalShodhyaPinda = totalShodhyaPinda,
            completeness = completeness,
            unsupportedBodies = shodhitaAshtakavarga.unsupportedBodies,
        )
    }
}
