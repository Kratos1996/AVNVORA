package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * Pinda calculation result for an individual classical planet.
 *
 * Sourced from Shodhita Bhinnashtakavarga (after Trikona and Ekadhipatya Shodhana).
 * - Rashi Pinda: Sum of (Shodhita Bindus * Rashi Gunakara) across all 12 signs.
 * - Graha Pinda: Sum of (Shodhita Bindus in sign occupied by planet P * Graha Gunakara of planet P) across all 7 classical planets.
 * - Shodhya Pinda: Rashi Pinda + Graha Pinda.
 *
 * Source: Brihat Parashara Hora Shastra, Ch. 74/75; Dr. B.V. Raman, *The Ashtakavarga System of Direction*, Ch. 6.
 */
@Serializable
data class PlanetaryPindaResult(
    val targetBody: BodyId,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val rasiPinda: Int,
    val grahaPinda: Int,
    val shodhyaPinda: Int,
    val rasiContributions: Map<Int, Int>,
    val grahaContributions: Map<BodyId, Int>,
)

/**
 * Top-level Ashtakavarga Pinda calculation result across all 7 classical planets.
 */
@Serializable
data class AshtakavargaPindaResult(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val planetaryPindas: Map<BodyId, PlanetaryPindaResult>,
    val totalRasiPinda: Int,
    val totalGrahaPinda: Int,
    val totalShodhyaPinda: Int,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<BodyId> = listOf(BodyId.RAHU, BodyId.KETU),
)
