package com.aynvora.astro.dignity

import com.aynvora.astro.BodyId
import com.aynvora.astro.varga.DivisionalChart
import kotlinx.serialization.Serializable

/**
 * Factual planetary dignity classification according to traditional Vedic astrology
 * (Brihat Parashara Hora Shastra, Chapter 3).
 */
@Serializable
enum class DignityType {
    EXALTATION,
    DEBILITATION,
    MOOLATRIKONA,
    OWN_SIGN,
    GREAT_FRIEND_SIGN,
    FRIEND_SIGN,
    NEUTRAL_SIGN,
    ENEMY_SIGN,
    GREAT_ENEMY_SIGN,
    NOT_APPLICABLE,
}

/**
 * Calculated planetary dignity position in a specific chart.
 */
@Serializable
data class PlanetaryDignityPosition(
    val bodyId: BodyId,
    val chart: DivisionalChart = DivisionalChart.D1,
    val sourceRashiIndex: Int,
    val signLordBodyId: BodyId?,
    val dignityType: DignityType,
    val isExalted: Boolean,
    val isDebilitated: Boolean,
    val isMoolatrikona: Boolean,
    val isOwnSign: Boolean,
    val deepExaltationDegree: Double? = null,
    val deepDebilitationDegree: Double? = null,
    val degreeInSign: Double = 0.0,
    val ruleId: String = "PARASHARA_CLASSICAL_V1",
)
