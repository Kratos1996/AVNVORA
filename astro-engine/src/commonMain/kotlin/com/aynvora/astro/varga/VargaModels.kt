package com.aynvora.astro.varga

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

/**
 * Calculated factual position of a celestial body or Lagna within a divisional chart.
 */
@Serializable
data class VargaPosition(
    val bodyId: BodyId?,
    val isLagna: Boolean = false,
    val sourceLongitude: Double,
    val sourceRashiIndex: Int,
    val divisionIndex: Int,
    val resultingRashiIndex: Int,
    val degreeInResultingRashi: Double,
    val resultingLongitude: Double,
)

/**
 * Deterministic factual result of a divisional chart calculation.
 * Contains no astrological interpretation or prediction.
 */
@Serializable
data class VargaChartResult(
    val chart: DivisionalChart,
    val rulesetId: String,
    val isSupported: Boolean,
    val lagnaPosition: VargaPosition?,
    val positions: List<VargaPosition>,
)
