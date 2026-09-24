package com.aynvora.core.intelligence

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.garudapuran.GarudaPuranContentItem

/** Converts only repository-vetted Garuda Puran items into evidence nodes. No astro linkage is inferred. */
object GarudaPuranEvidenceGraphFactory {
    fun create(
        queryId: String,
        items: List<GarudaPuranContentItem>,
        timestampEpochMs: Long
    ): EvidenceGraph {
        require(queryId.isNotBlank())
        require(timestampEpochMs >= 0L)
        require(items.map { it.contentId }.distinct().size == items.size)

        val nodes = linkedMapOf<String, EvidenceItem>()
        val edges = mutableListOf<EvidenceGraphEdge>()
        items.sortedWith(
            compareBy(
                { it.section.order },
                { it.reference.chapterNumber ?: Int.MAX_VALUE },
                { it.contentId })
        )
            .forEach { item ->
                val baseId = "${queryId}_${item.contentId}"
                fun provenance(contentType: String) = EvidenceProvenance(
                    domain = CoreFeatureId.GARUDA_PURAN,
                    sourceName = item.sourceEdition.title,
                    rulesetOrEdition = item.sourceEdition.editionId,
                    engineVersion = "content-v${item.contentVersion}",
                    timestampEpochMs = timestampEpochMs,
                    locale = item.languageCode,
                    referenceId = item.reference.canonicalReferenceId,
                    contentVersion = "v${item.contentVersion}",
                    contentType = contentType,
                )

                val sourceId = "${baseId}_source"
                nodes[sourceId] = EvidenceItem(
                    evidenceId = sourceId,
                    domain = CoreFeatureId.GARUDA_PURAN,
                    category = EvidenceCategory.TRADITIONAL_RULE,
                    ruleId = item.reference.canonicalReferenceId,
                    summary = item.text.localizedPresentation,
                    provenance = provenance(item.contentType.name),
                    priority = 5,
                )
                item.interpretations.forEachIndexed { index, interpretation ->
                    val id = "${baseId}_interpretation_${index}"
                    nodes[id] = EvidenceItem(
                        evidenceId = id,
                        domain = CoreFeatureId.GARUDA_PURAN,
                        category = EvidenceCategory.INTERPRETATION,
                        ruleId = interpretation.traditionId,
                        summary = interpretation.text,
                        provenance = provenance("TRADITIONAL_INTERPRETATION"),
                        priority = 4,
                    )
                    edges += EvidenceGraphEdge(sourceId, id, "SUPPORTED_BY_SOURCE_REFERENCE")
                }
                item.practices.forEachIndexed { index, practice ->
                    val id = "${baseId}_practice_${index}"
                    nodes[id] = EvidenceItem(
                        evidenceId = id,
                        domain = CoreFeatureId.GARUDA_PURAN,
                        category = EvidenceCategory.TRADITIONAL_RULE,
                        ruleId = practice.practiceType,
                        summary = practice.text,
                        provenance = provenance("TRADITIONAL_PRACTICE"),
                        priority = 3,
                    )
                    edges += EvidenceGraphEdge(sourceId, id, "SUPPORTED_BY_SOURCE_REFERENCE")
                }
            }
        return EvidenceGraph(queryId, nodes.toMap(), edges.toList())
    }
}
