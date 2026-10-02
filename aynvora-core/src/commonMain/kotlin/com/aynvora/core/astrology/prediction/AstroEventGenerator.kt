package com.aynvora.core.astrology.prediction

import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.core.models.KundaliSnapshot
import kotlinx.serialization.Serializable

/** Static calculator links are resolved here, separately from event catalog data. */
object AstroEventCalculatorRegistry {
    fun isRegistered(calculatorId: String): Boolean = calculatorId == AstroEventEngine.CALCULATOR_ID
}

@Serializable
data class AstroEventGenerationTrace(
    val catalogId: String,
    val catalogVersion: String,
    val evaluatedDefinitionCount: Int,
    val sliceCount: Int,
    val dynamicFactCalculationCount: Int,
    val reusedBaseFactCount: Int,
    val generatedOccurrenceCount: Int,
    val skippedDefinitionIds: List<String>,
)

@Serializable
data class AstroEventGenerationResult(
    val occurrences: List<AstroEventOccurrence>,
    val trace: AstroEventGenerationTrace,
)

@Serializable
data class AstroEventSnapshotResult(
    val snapshot: KundaliSnapshot,
    val generation: AstroEventGenerationResult,
)

/**
 * Evaluates event definitions against one shared set of natal facts and per-slice facts.
 * `factsForSlice` should calculate only time-varying evidence (for example, Transit), never
 * regenerate the natal chart. Occurrences carry Julian Day boundaries for exact downstream formatting.
 */
object AstroEventGenerator {
    fun generateEvents(
        catalog: AstroEventCatalog,
        range: AstroEventObservationRange,
        baseFacts: List<AstroEventFact>,
        provenance: CalculationMetadata,
        factsForSlice: (AstroEventDefinition, AstroEventTimeSlice) -> List<AstroEventFact>,
    ): AstroEventGenerationResult {
        val slices = range.slices()
        val supportedDefinitions = catalog.definitions.filter { it.status == AstroEventDefinitionStatus.SUPPORTED }
        val skipped = catalog.definitions.filter { it.status != AstroEventDefinitionStatus.SUPPORTED || !AstroEventCalculatorRegistry.isRegistered(it.calculatorId) }
            .map { it.eventId }.toMutableSet()
        val rawMatches = mutableListOf<AstroEventOccurrence>()
        var dynamicCalculationCount = 0

        supportedDefinitions.filter { AstroEventCalculatorRegistry.isRegistered(it.calculatorId) }.forEach { definition ->
            slices.forEach sliceLoop@{ slice ->
                val dynamicFacts = factsForSlice(definition, slice)
                dynamicCalculationCount++
                val providedFeatureIds = (baseFacts.map { it.featureId } + dynamicFacts.map { it.featureId }).toSet()
                if (!providedFeatureIds.containsAll(definition.requiredFeatureIds)) {
                    skipped += definition.eventId
                    return@sliceLoop
                }
                val occurrence = AstroEventEngine.evaluate(
                    definition = definition,
                    period = AstroEventTimeRange(slice.startJulianDay.toJdLabel(), slice.endJulianDay.toJdLabel()),
                    facts = baseFacts + dynamicFacts,
                    provenance = provenance,
                    featureId = definition.featureId,
                )
                if (occurrence.status == AstroEventOccurrenceStatus.SUPPORTED ||
                    (occurrence.status == AstroEventOccurrenceStatus.PARTIAL && occurrence.matchedEvidenceIds.isNotEmpty())
                ) {
                    rawMatches += occurrence.copy(
                        startJulianDay = slice.startJulianDay,
                        endJulianDay = slice.endJulianDay,
                    )
                }
            }
        }

        val merged = mergeAdjacent(rawMatches)
        return AstroEventGenerationResult(
            occurrences = merged,
            trace = AstroEventGenerationTrace(
                catalogId = catalog.catalogId,
                catalogVersion = catalog.version,
                evaluatedDefinitionCount = supportedDefinitions.count { AstroEventCalculatorRegistry.isRegistered(it.calculatorId) },
                sliceCount = slices.size,
                dynamicFactCalculationCount = dynamicCalculationCount,
                reusedBaseFactCount = if (dynamicCalculationCount == 0) 0 else baseFacts.size * dynamicCalculationCount,
                generatedOccurrenceCount = merged.size,
                skippedDefinitionIds = skipped.sorted(),
            ),
        )
    }

    private fun mergeAdjacent(occurrences: List<AstroEventOccurrence>): List<AstroEventOccurrence> {
        val merged = mutableListOf<AstroEventOccurrence>()
        occurrences.sortedWith(compareBy({ it.eventId }, { it.startJulianDay }, { it.endJulianDay })).forEach { next ->
            val prior = merged.lastOrNull()
            if (prior != null && prior.eventId == next.eventId &&
                prior.endJulianDay != null && next.startJulianDay != null &&
                kotlin.math.abs(prior.endJulianDay - next.startJulianDay) < 1e-9
            ) {
                merged[merged.lastIndex] = prior.copy(
                    end = next.end,
                    endJulianDay = next.endJulianDay,
                    evidence = (prior.evidence + next.evidence).distinct().sorted(),
                    matchedEvidenceIds = (prior.matchedEvidenceIds + next.matchedEvidenceIds).distinct().sorted(),
                    sourceRefs = (prior.sourceRefs + next.sourceRefs).distinct().sorted(),
                    status = if (prior.status == AstroEventOccurrenceStatus.PARTIAL || next.status == AstroEventOccurrenceStatus.PARTIAL) {
                        AstroEventOccurrenceStatus.PARTIAL
                    } else AstroEventOccurrenceStatus.SUPPORTED,
                )
            } else merged += next
        }
        return merged.sortedWith(compareBy({ it.startJulianDay }, { it.eventId }, { it.endJulianDay }))
    }

    private fun Double.toJdLabel(): String = "JD:${toString()}"
}
