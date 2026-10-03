package com.aynvora.core.models

import com.aynvora.core.astrology.knowledge.AstroEvidenceItem
import com.aynvora.core.astrology.knowledge.AstroPageContext
import kotlinx.serialization.Serializable

/**
 * Standardized AI page context data contract conforming to Phase 10.31 Section 18.
 * Directly consumed by UI screens and the on-device AI grounding engine.
 */
@Serializable
data class PageContext(
    val pageId: String,
    val featureId: String,
    val kundaliId: String? = null,
    val selectedItem: String? = null,
    val selectedChart: String? = null,
    val selectedPeriod: String? = null,
    val visibleData: Map<String, String> = emptyMap(),
    val evidence: List<String> = emptyList(),
    val sourceRefs: List<String> = emptyList(),
    val tradition: String = "PARASHARI",
    val calculationProfile: String = "AYNVORA_CANONICAL_V1",
) {
    /**
     * Converts to model-facing [AstroPageContext] for constrained tool planning.
     */
    fun toAstroPageContext(evidenceItems: List<AstroEvidenceItem> = emptyList()): AstroPageContext =
        AstroPageContext(
            pageId = pageId,
            featureId = featureId,
            kundaliId = kundaliId,
            visibleData = visibleData,
            currentChart = selectedChart?.let { mapOf("chart" to it) } ?: emptyMap(),
            traditionId = tradition,
            currentPeriod = selectedPeriod?.let { mapOf("period" to it) } ?: emptyMap(),
            evidence = evidenceItems,
            sourceReferences = sourceRefs,
            searchableTerms = listOfNotNull(selectedItem, selectedChart, selectedPeriod)
        )

    companion object {
        fun fromAstroPageContext(ctx: AstroPageContext, profile: String = "AYNVORA_CANONICAL_V1"): PageContext =
            PageContext(
                pageId = ctx.pageId,
                featureId = ctx.featureId,
                kundaliId = ctx.kundaliId,
                selectedItem = ctx.searchableTerms.firstOrNull(),
                selectedChart = ctx.currentChart["chart"],
                selectedPeriod = ctx.currentPeriod["period"],
                visibleData = ctx.visibleData,
                evidence = ctx.evidence.map { it.text },
                sourceRefs = ctx.sourceReferences,
                tradition = ctx.traditionId ?: "PARASHARI",
                calculationProfile = profile
            )
    }
}
