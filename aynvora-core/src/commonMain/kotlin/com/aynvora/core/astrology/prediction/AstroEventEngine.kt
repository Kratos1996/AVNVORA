package com.aynvora.core.astrology.prediction

import com.aynvora.astro.provenance.CalculationMetadata

/** Deterministic event condition matching. This engine produces evidence links, never prose. */
object AstroEventEngine {
    fun evaluate(
        definition: AstroEventDefinition,
        period: AstroEventTimeRange,
        facts: List<AstroEventFact>,
        provenance: CalculationMetadata,
        featureId: String = definition.featureId,
    ): AstroEventOccurrence {
        val conditions = definition.typedConditions
        val evidenceByRequirement = facts.groupBy { it.evidenceRequirementId }
        val matchedEvidence = linkedSetOf<String>()
        var hasMissingEvidence = false
        var hasMismatch = false

        conditions.forEach { condition ->
            val candidates = facts.filter { it.factKey == condition.factKey && it.evidenceRequirementId == condition.evidenceRequirementId }
            if (candidates.isEmpty()) {
                hasMissingEvidence = true
            } else {
                val matched = candidates.any { fact -> matches(condition, fact.value) }
                if (matched) candidates.filter { matches(condition, it.value) }.forEach { matchedEvidence += it.evidenceId }
                else hasMismatch = true
            }
        }
        val missingRequirements = definition.evidenceRequirements.any { evidenceByRequirement[it].isNullOrEmpty() }
        val hasApprovedSource = definition.status == AstroEventDefinitionStatus.SUPPORTED &&
            definition.sourceRefs.isNotEmpty() && definition.licenseStatus == "CLEARED"
        val status = when {
            definition.calculatorId != CALCULATOR_ID -> AstroEventOccurrenceStatus.UNSUPPORTED
            definition.status == AstroEventDefinitionStatus.UNSUPPORTED -> AstroEventOccurrenceStatus.UNSUPPORTED
            definition.status == AstroEventDefinitionStatus.AMBIGUOUS -> AstroEventOccurrenceStatus.AMBIGUOUS
            definition.status == AstroEventDefinitionStatus.PARTIAL -> AstroEventOccurrenceStatus.PARTIAL
            !hasApprovedSource || conditions.isEmpty() -> AstroEventOccurrenceStatus.NOT_VERIFIED
            hasMismatch -> AstroEventOccurrenceStatus.NO_MATCH
            hasMissingEvidence || missingRequirements -> AstroEventOccurrenceStatus.PARTIAL
            else -> AstroEventOccurrenceStatus.SUPPORTED
        }
        val sourceRefs = (definition.sourceRefs + facts.filter { it.evidenceId in matchedEvidence }.flatMap { it.sourceRefs }).distinct().sorted()
        return AstroEventOccurrence(
            eventId = definition.eventId,
            start = period.start,
            end = period.end,
            featureId = featureId,
            conditions = conditions,
            evidence = matchedEvidence.toList().sorted(),
            sourceRefs = sourceRefs,
            status = status,
            matchedEvidenceIds = matchedEvidence.toList().sorted(),
            provenance = provenance,
        )
    }

    private fun matches(condition: AstroEventCondition, actual: String): Boolean = when (condition.comparison) {
        AstroEventComparison.EQUALS -> actual == condition.expectedValue
        AstroEventComparison.NOT_EQUALS -> actual != condition.expectedValue
        AstroEventComparison.ONE_OF -> condition.expectedValue.split('|').any { it == actual }
        AstroEventComparison.GREATER_THAN -> compareNumbers(actual, condition.expectedValue) { a, b -> a > b }
        AstroEventComparison.LESS_THAN -> compareNumbers(actual, condition.expectedValue) { a, b -> a < b }
    }

    private fun compareNumbers(actual: String, expected: String, predicate: (Double, Double) -> Boolean): Boolean {
        val actualNumber = actual.toDoubleOrNull()?.takeIf { it.isFinite() } ?: return false
        val expectedNumber = expected.toDoubleOrNull()?.takeIf { it.isFinite() } ?: return false
        return predicate(actualNumber, expectedNumber)
    }

    const val CALCULATOR_ID = "TYPED_CONDITION_MATCH_V1"
}
