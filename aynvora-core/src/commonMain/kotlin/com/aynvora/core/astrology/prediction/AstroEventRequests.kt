package com.aynvora.core.astrology.prediction

import com.aynvora.astro.provenance.CalculationMetadata
import kotlinx.serialization.Serializable

/** Caller supplies facts produced by registered feature calculations; event rules never calculate astronomy. */
@Serializable
data class AstroEventEvaluationRequest(
    val definition: AstroEventDefinition,
    val period: AstroEventTimeRange,
    val facts: List<AstroEventFact>,
    val provenance: CalculationMetadata,
    val featureId: String = "astro.event",
)

/** Deterministic, source-gated matching of a normalized knowledge rule against feature evidence. */
object KnowledgeRuleMatcher {
    fun evaluate(
        rule: KnowledgeRule,
        source: KnowledgeSource,
        period: AstroEventTimeRange,
        facts: List<AstroEventFact>,
        provenance: CalculationMetadata,
    ): AstroEventOccurrence? {
        if (source.sourceId != rule.sourceId) return null
        val rights = if (source.licenseStatus == "CLEARED" && rule.licenseStatus == "CLEARED") "CLEARED" else "NOT_VERIFIED"
        return AstroEventEngine.evaluate(
            definition = AstroEventDefinition(
                eventId = rule.ruleId,
                nameKey = rule.interpretationKey,
                tradition = rule.tradition,
                conditions = rule.conditions.map { "${it.factKey}:${it.comparison}:${it.expectedValue}" },
                evidenceRequirements = rule.evidenceRequirements,
                sourceRefs = listOf(source.sourceId),
                ruleVersion = rule.sourceVersion ?: "1",
                ruleId = rule.ruleId,
                sourcePage = rule.sourcePage ?: source.pageOrChapter,
                licenseStatus = rights,
                packVersion = rule.packVersion,
                checksum = rule.checksum ?: source.checksum,
                typedConditions = rule.conditions,
                featureId = "knowledge.rule_match",
                status = if (rights == "CLEARED") AstroEventDefinitionStatus.SUPPORTED else AstroEventDefinitionStatus.NOT_VERIFIED,
            ),
            period = period,
            facts = facts,
            provenance = provenance,
            featureId = "knowledge.rule_match",
        )
    }
}
