package com.aynvora.core.astrology.prediction

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceGraphEdge
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.models.KundaliSnapshot
import com.aynvora.core.models.AstroFeatureRecord
import com.aynvora.core.models.AstroFeatureStatus

/** Adds evaluated occurrences and deterministic provenance edges to a serializable chart snapshot. */
fun KundaliSnapshot.withEventOccurrences(occurrences: List<AstroEventOccurrence>): KundaliSnapshot {
    val mergedEvents = (events + occurrences).distinctBy { Triple(it.eventId, it.start, it.end) }
        .sortedWith(compareBy({ it.start }, { it.end }, { it.eventId }))
    val nodes = evidenceGraph?.nodes.orEmpty().toMutableMap()
    val edges = evidenceGraph?.edges.orEmpty().toMutableList()

    mergedEvents.forEach { occurrence ->
        val metadata = occurrence.provenance ?: return@forEach
        val sourceIds = occurrence.matchedEvidenceIds.distinct().sorted()
        sourceIds.forEach { evidenceId ->
            if (evidenceId !in nodes) nodes[evidenceId] = EvidenceItem(
                evidenceId = evidenceId,
                domain = CoreFeatureId.ASTROLOGY,
                category = EvidenceCategory.FACT,
                summary = "Feature evidence matched an event condition (${occurrence.eventId}).",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.ASTROLOGY,
                    sourceName = metadata.ephemerisSourceId,
                    rulesetOrEdition = metadata.calculationProfileId,
                    engineVersion = metadata.engineVersion,
                    calculationProfile = metadata.calculationProfileId,
                    timestampEpochMs = 0L,
                    referenceId = occurrence.sourceRefs.firstOrNull(),
                    contentVersion = metadata.ephemerisDataVersion,
                ),
            )
        }
        if (sourceIds.isNotEmpty() && occurrence.status in setOf(
                AstroEventOccurrenceStatus.SUPPORTED,
                AstroEventOccurrenceStatus.PARTIAL,
            )) {
            val eventNodeId = "astro-event:${occurrence.eventId}:${occurrence.start}"
            nodes[eventNodeId] = EvidenceItem(
                evidenceId = eventNodeId,
                domain = CoreFeatureId.ASTROLOGY,
                category = EvidenceCategory.DERIVED_FACT,
                ruleId = occurrence.eventId,
                summary = "Event conditions matched within the evaluated period.",
                provenance = EvidenceProvenance(
                    domain = CoreFeatureId.ASTROLOGY,
                    sourceName = "AstroEventEngine",
                    rulesetOrEdition = occurrence.eventId,
                    engineVersion = metadata.engineVersion,
                    calculationProfile = metadata.calculationProfileId,
                    timestampEpochMs = 0L,
                    referenceId = occurrence.sourceRefs.firstOrNull(),
                ),
            )
            sourceIds.forEach { sourceId ->
                val edge = EvidenceGraphEdge(sourceId, eventNodeId, "SUPPORTS_EVENT")
                if (edge !in edges) edges += edge
            }
        }
    }

    val graph = if (nodes.isEmpty()) evidenceGraph else EvidenceGraph(
        queryId = evidenceGraph?.queryId ?: "astro-event-snapshot",
        nodes = nodes.toList().sortedBy { it.first }.toMap(),
        edges = edges.distinct().sortedWith(compareBy({ it.targetEvidenceId }, { it.sourceEvidenceId }, { it.relationship })),
    )
    val eventStatus = when {
        mergedEvents.isEmpty() -> featureResults["astro_events"]?.status ?: AstroFeatureStatus.NOT_VERIFIED
        mergedEvents.any { it.status == AstroEventOccurrenceStatus.AMBIGUOUS } -> AstroFeatureStatus.AMBIGUOUS
        mergedEvents.any { it.status == AstroEventOccurrenceStatus.PARTIAL } -> AstroFeatureStatus.PARTIAL
        mergedEvents.any { it.status == AstroEventOccurrenceStatus.UNSUPPORTED } -> AstroFeatureStatus.UNSUPPORTED
        mergedEvents.any { it.status == AstroEventOccurrenceStatus.NOT_VERIFIED } -> AstroFeatureStatus.NOT_VERIFIED
        else -> AstroFeatureStatus.SUPPORTED
    }
    val eventRecord = AstroFeatureRecord(
        featureId = "astro_events",
        featureVersion = "1",
        status = eventStatus,
        dataRef = if (mergedEvents.isEmpty()) null else "events",
        dependencyIds = listOf("feature_evidence", "knowledge.rules"),
        provenance = mergedEvents.firstNotNullOfOrNull { it.provenance } ?: calculation,
        warnings = if (mergedEvents.any { it.status == AstroEventOccurrenceStatus.NOT_VERIFIED }) {
            listOf("One or more event definitions do not have verified source/rights metadata.")
        } else emptyList(),
    )
    return copy(
        events = mergedEvents,
        evidenceGraph = graph,
        featureResults = featureResults + ("astro_events" to eventRecord),
    )
}
