package com.aynvora.astro.ashtakavarga

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * The 8 classical contributors in Ashtakavarga per Brihat Parashara Hora Shastra Ch. 66.
 * (7 classical planets + Lagna / Ascendant).
 */
@Serializable
enum class AshtakavargaContributor {
    SUN,
    MOON,
    MARS,
    MERCURY,
    JUPITER,
    VENUS,
    SATURN,
    LAGNA,
}

/**
 * Calculation completeness status for Ashtakavarga.
 */
@Serializable
enum class AshtakavargaCompleteness {
    COMPLETE,
    PARTIAL,
    UNSUPPORTED,
}

/**
 * Per-sign score in a planet's Bhinnashtakavarga (BAV).
 */
@Serializable
data class BhinnashtakavargaSignScore(
    val rashiIndex: Int,
    val rashiName: String,
    val binduCount: Int,
    val rekhaCount: Int,
    val contributingBodies: List<AshtakavargaContributor>,
)

/**
 * Individual planetary Bhinnashtakavarga (BAV) chart.
 */
@Serializable
data class BhinnashtakavargaChart(
    val targetBody: BodyId,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<BhinnashtakavargaSignScore>,
    val totalBindus: Int,
    val totalRekhas: Int,
    val contributorGrid: Map<AshtakavargaContributor, List<Int>>,
)

/**
 * Per-sign aggregate score in Sarvashtakavarga (SAV).
 */
@Serializable
data class SarvashtakavargaSignScore(
    val rashiIndex: Int,
    val rashiName: String,
    val totalBindus: Int,
    val totalRekhas: Int,
    val planetBindus: Map<BodyId, Int>,
)

/**
 * Comprehensive Sarvashtakavarga (SAV) aggregate chart.
 */
@Serializable
data class SarvashtakavargaChart(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<SarvashtakavargaSignScore>,
    val grandTotalBindus: Int,
    val grandTotalRekhas: Int,
    val isInvariantValid: Boolean,
)

/**
 * Top-level Ashtakavarga calculation result.
 */
@Serializable
data class AshtakavargaResult(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val bhinnashtakavarga: Map<BodyId, BhinnashtakavargaChart>,
    val sarvashtakavarga: SarvashtakavargaChart,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<BodyId> = listOf(BodyId.RAHU, BodyId.KETU),
    val shodhana: ShodhitaAshtakavargaResult? = null,
    val pinda: AshtakavargaPindaResult? = null,
)
