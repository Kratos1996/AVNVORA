package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * Per-sign score in a planet's reduced Bhinnashtakavarga (BAV),
 * tracking raw, Trikona-reduced, and final Ekadhipatya-reduced (Shodhita) figures.
 */
@Serializable
data class ShodhitaBhinnashtakavargaSignScore(
    val rashiIndex: Int,
    val rashiName: String,
    val rawBindus: Int,
    val trikonaReducedBindus: Int,
    val ekadhipatyaReducedBindus: Int,
) {
    /**
     * Alias for final reduced bindus after both Shodhanas.
     */
    val shodhitaBindus: Int get() = ekadhipatyaReducedBindus
}

/**
 * Individual planetary Shodhita Bhinnashtakavarga chart.
 * Contains the complete reduction history from raw BAV -> Trikona Shodhana -> Ekadhipatya Shodhana.
 */
@Serializable
data class ShodhitaBhinnashtakavargaChart(
    val targetBody: BodyId,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<ShodhitaBhinnashtakavargaSignScore>,
    val rawTotalBindus: Int,
    val trikonaTotalBindus: Int,
    val shodhitaTotalBindus: Int,
)

/**
 * Per-sign aggregate score in Shodhita Sarvashtakavarga (SAV).
 */
@Serializable
data class ShodhitaSarvashtakavargaSignScore(
    val rashiIndex: Int,
    val rashiName: String,
    val rawTotalBindus: Int,
    val trikonaTotalBindus: Int,
    val shodhitaTotalBindus: Int,
    val planetShodhitaBindus: Map<BodyId, Int>,
)

/**
 * Aggregate Shodhita Sarvashtakavarga chart derived by summing
 * the 7 classical planets' Shodhita BAV charts sign by sign.
 * Source: BPHS Ch. 74; Raman Ch. 5.
 */
@Serializable
data class ShodhitaSarvashtakavargaChart(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<ShodhitaSarvashtakavargaSignScore>,
    val grandTotalRawBindus: Int,
    val grandTotalTrikonaBindus: Int,
    val grandTotalShodhitaBindus: Int,
)

/**
 * Top-level Shodhita Ashtakavarga calculation result.
 */
@Serializable
data class ShodhitaAshtakavargaResult(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val shodhitaBhinnashtakavarga: Map<BodyId, ShodhitaBhinnashtakavargaChart>,
    val shodhitaSarvashtakavarga: ShodhitaSarvashtakavargaChart,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<BodyId> = listOf(BodyId.RAHU, BodyId.KETU),
)
