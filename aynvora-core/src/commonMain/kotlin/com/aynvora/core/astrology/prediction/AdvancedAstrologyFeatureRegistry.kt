package com.aynvora.core.astrology.prediction

import com.aynvora.core.models.AstroFeatureStatus
import kotlinx.serialization.Serializable

/** Truthful capability inventory for tradition engines; entries do not imply calculations exist. */
@Serializable
data class AdvancedAstrologyFeatureCapability(
    val featureId: String,
    val status: AstroFeatureStatus,
    val dependencies: List<String>,
    val missingRequirements: List<String>,
)

object AdvancedAstrologyFeatureRegistry {
    val capabilities: List<AdvancedAstrologyFeatureCapability> = listOf(
        AdvancedAstrologyFeatureCapability(
            "KP_ANALYSIS", AstroFeatureStatus.NOT_VERIFIED,
            listOf("core.time", "core.location", "core.ephemeris", "core.planetary_positions", "vedic.houses"),
            listOf("verified KP ayanamsha and cusp rules", "licensed 249 subdivision source table", "source-backed significator and timing rules"),
        ),
        AdvancedAstrologyFeatureCapability(
            "LAL_KITAB_ANALYSIS", AstroFeatureStatus.NOT_VERIFIED,
            listOf("vedic.chart"),
            listOf("approved edition with documented reproduction rights", "normalized and source-referenced rule pack"),
        ),
        AdvancedAstrologyFeatureCapability(
            "VARSHAPHAL", AstroFeatureStatus.NOT_VERIFIED,
            listOf("core.time", "core.location", "core.ephemeris", "core.planetary_positions", "vedic.houses"),
            listOf("verified Muntha progression method", "source-backed Varsheshwara, Sahams, Tajika aspects, and Mudda Dasha rules"),
        ),
        AdvancedAstrologyFeatureCapability(
            "PHALADEESH", AstroFeatureStatus.UNSUPPORTED,
            listOf("vedic.dasha", "vedic.transit", "vedic.vargas", "astro.events", "knowledge.rules"),
            listOf("supported event/rule packs", "monthly evidence integration"),
        ),
    )

    fun get(featureId: String): AdvancedAstrologyFeatureCapability? =
        capabilities.firstOrNull { it.featureId == featureId }
}
