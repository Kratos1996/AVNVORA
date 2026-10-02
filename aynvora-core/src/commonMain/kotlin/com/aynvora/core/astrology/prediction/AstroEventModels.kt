package com.aynvora.core.astrology.prediction

import com.aynvora.astro.provenance.CalculationMetadata
import com.aynvora.astro.context.AstroObservationContext
import kotlinx.serialization.Serializable

/** Rule data is stored separately from deterministic evaluator code and interpretation text. */
@Serializable
data class AstroEventDefinition(
    val eventId: String,
    val nameKey: String,
    val tradition: String,
    val conditions: List<String>,
    val evidenceRequirements: List<String>,
    val sourceRefs: List<String>,
    val ruleVersion: String,
    val ruleId: String = eventId,
    val sourcePage: String? = null,
    val licenseStatus: String = "NOT_VERIFIED",
    val packVersion: String? = null,
    val checksum: String? = null,
    val typedConditions: List<AstroEventCondition> = emptyList(),
    val featureId: String = "astro.event",
    val descriptionKey: String? = null,
    val tags: Set<AstroEventTag> = emptySet(),
    val calculatorId: String = AstroEventEngine.CALCULATOR_ID,
    val requiredFeatureIds: List<String> = emptyList(),
    val status: AstroEventDefinitionStatus = AstroEventDefinitionStatus.NOT_VERIFIED,
)

@Serializable
enum class AstroEventTag {
    GOCHAR, DASHA, KP, PHALADEESH, MUHURTA, RELATIONSHIP, CAREER, FINANCE, TRAVEL,
    ANNUAL, MONTHLY, PANCHANG, PLANETARY,
}

@Serializable
enum class AstroEventDefinitionStatus { SUPPORTED, PARTIAL, AMBIGUOUS, UNSUPPORTED, NOT_VERIFIED }

@Serializable
data class AstroEventOccurrence(
    val eventId: String,
    val start: String,
    val end: String,
    val featureId: String = "astro.event",
    val conditions: List<AstroEventCondition> = emptyList(),
    val evidence: List<String>,
    val sourceRefs: List<String>,
    val status: AstroEventOccurrenceStatus,
    val matchedEvidenceIds: List<String> = emptyList(),
    val provenance: CalculationMetadata? = null,
    val startJulianDay: Double? = null,
    val endJulianDay: Double? = null,
)

@Serializable
enum class AstroEventOccurrenceStatus { SUPPORTED, PARTIAL, AMBIGUOUS, UNSUPPORTED, NOT_VERIFIED, NO_MATCH }

/** Source-backed knowledge metadata only; this model intentionally carries no interpretation prose. */
@Serializable
data class KnowledgeChunk(
    val chunkId: String,
    val traditionId: String,
    val topic: String,
    val condition: String,
    val interpretationKey: String,
    val sourceRef: String,
    val sourcePage: String? = null,
    val sourceEdition: String? = null,
    val language: String,
    val licenseStatus: String,
    val checksum: String,
    val version: String,
    val sourceAuthor: String? = null,
    val sourceYear: Int? = null,
    val sourceLocation: String? = null,
    val packVersion: String? = null,
    val chapter: String? = null,
    val page: Int? = null,
    val ruleId: String? = null,
    val embeddingModel: String? = null,
    val embeddingVersion: String? = null,
    val sourceId: String? = null,
    val text: String = "",
    val structuredFacts: Map<String, String> = emptyMap(),
    val ruleIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
)

@Serializable
data class AstroEventTimeRange(val start: String, val end: String) {
    init { require(start.isNotBlank() && end.isNotBlank() && start <= end) }
}

@Serializable
enum class AstroObservationPurpose { TRANSIT, PANCHANG, PHALADEESH, MUHURTA, VARSHAPHAL, EVENT_SCAN, OTHER }

@Serializable
data class AstroEventObservationRange(
    val start: AstroObservationContext,
    val end: AstroObservationContext,
    val purpose: AstroObservationPurpose,
    val sliceDurationDays: Double = 1.0,
) {
    init {
        require(start.julianDay <= end.julianDay)
        require(start.timeZoneId == end.timeZoneId)
        require(start.latitude == end.latitude && start.longitude == end.longitude)
        require(start.timezoneDataVersion == end.timezoneDataVersion)
        require(sliceDurationDays.isFinite() && sliceDurationDays > 0.0)
        require(kotlin.math.ceil((end.julianDay - start.julianDay) / sliceDurationDays) <= MAX_SLICE_COUNT)
    }

    fun slices(): List<AstroEventTimeSlice> {
        if (start.julianDay == end.julianDay) return listOf(AstroEventTimeSlice(0, start.julianDay, end.julianDay))
        val result = mutableListOf<AstroEventTimeSlice>()
        var cursor = start.julianDay
        while (cursor < end.julianDay) {
            val next = (cursor + sliceDurationDays).coerceAtMost(end.julianDay)
            require(next > cursor) { "Slice duration is below the Julian day resolution for this range." }
            result += AstroEventTimeSlice(result.size, cursor, next)
            cursor = next
        }
        return result
    }

    companion object { const val MAX_SLICE_COUNT = 10_000 }
}

@Serializable
data class AstroEventTimeSlice(val index: Int, val startJulianDay: Double, val endJulianDay: Double) {
    init { require(index >= 0 && startJulianDay.isFinite() && endJulianDay.isFinite() && startJulianDay <= endJulianDay) }
}

@Serializable
enum class AstroEventComparison { EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, ONE_OF }

@Serializable
data class AstroEventCondition(
    val factKey: String,
    val comparison: AstroEventComparison,
    val expectedValue: String,
    val evidenceRequirementId: String,
) {
    init { require(factKey.isNotBlank() && evidenceRequirementId.isNotBlank()) }
}

@Serializable
data class AstroEventFact(
    val evidenceId: String,
    val factKey: String,
    val value: String,
    val evidenceRequirementId: String,
    val sourceRefs: List<String> = emptyList(),
    val featureId: String = "unspecified",
)

/** A source record is metadata only; it does not imply permission to reproduce source content. */
@Serializable
data class KnowledgeSource(
    val sourceId: String,
    val title: String,
    val author: String? = null,
    val edition: String? = null,
    val year: Int? = null,
    val language: String,
    val location: String? = null,
    val url: String? = null,
    val pageOrChapter: String? = null,
    val licenseStatus: String = "NOT_VERIFIED",
    val version: String? = null,
    val checksum: String? = null,
    val sourceClass: String? = null,
    val rightsStatus: String = "NOT_VERIFIED",
    val repository: String? = null,
    val commit: String? = null,
    val contentHash: String? = null,
)

@Serializable
data class KnowledgeRule(
    val ruleId: String,
    val tradition: String,
    val topic: String,
    val conditions: List<AstroEventCondition>,
    val evidenceRequirements: List<String>,
    val interpretationKey: String,
    val sourceId: String,
    val sourceVersion: String? = null,
    val sourcePage: String? = null,
    val licenseStatus: String = "NOT_VERIFIED",
    val packVersion: String,
    val checksum: String? = null,
    val traditionId: String? = null,
    val tags: List<String> = emptyList(),
    val inputs: List<String> = emptyList(),
    val resultType: String = "INTERPRETATION_KEY",
    val sourceRefs: List<String> = emptyList(),
    val rightsStatus: String = "NOT_VERIFIED",
)

@Serializable
data class KnowledgePack(
    val packId: String,
    val tradition: String,
    val version: String,
    val sources: List<KnowledgeSource>,
    val rules: List<KnowledgeRule>,
    val checksum: String? = null,
    val status: String = "NOT_VERIFIED",
    val locale: String = "en",
    val sourceVersions: Map<String, String> = emptyMap(),
    val buildTimestampEpochMs: Long? = null,
)
