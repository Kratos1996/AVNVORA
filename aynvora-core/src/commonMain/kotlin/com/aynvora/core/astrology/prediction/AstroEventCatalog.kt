package com.aynvora.core.astrology.prediction

import kotlinx.serialization.Serializable

/** Versioned static event data. Calculator code is resolved separately by calculatorId. */
@Serializable
data class AstroEventCatalog(
    val catalogId: String,
    val version: String,
    val definitions: List<AstroEventDefinition>,
    val checksum: String? = null,
) {
    init {
        require(catalogId.isNotBlank() && version.isNotBlank())
        require(definitions.map { it.eventId }.distinct().size == definitions.size) { "Event IDs must be unique within a catalog" }
    }

    fun find(eventId: String): AstroEventDefinition? = definitions.firstOrNull { it.eventId == eventId }

    fun search(
        tradition: String? = null,
        featureId: String? = null,
        tag: AstroEventTag? = null,
        ruleId: String? = null,
        sourceRef: String? = null,
        status: AstroEventDefinitionStatus? = null,
    ): List<AstroEventDefinition> = definitions.asSequence()
        .filter { tradition == null || it.tradition == tradition }
        .filter { featureId == null || it.featureId == featureId }
        .filter { tag == null || tag in it.tags }
        .filter { ruleId == null || it.ruleId == ruleId }
        .filter { sourceRef == null || sourceRef in it.sourceRefs }
        .filter { status == null || it.status == status }
        .sortedBy { it.eventId }
        .toList()
}
