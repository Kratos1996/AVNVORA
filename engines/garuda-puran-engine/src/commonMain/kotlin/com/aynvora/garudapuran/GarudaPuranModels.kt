package com.aynvora.garudapuran

import com.aynvora.contracts.AynvoraResult
import kotlinx.serialization.Serializable

/** Canonical topic IDs. They identify subject areas; they do not imply available scripture content. */
@Serializable
enum class GarudaPuranTopicId(val translationKey: String) {
    INTRODUCTION("garuda.topic.introduction"),
    DIALOGUE_CONTEXT("garuda.topic.dialogue_context"),
    DHARMA_AND_CONDUCT("garuda.topic.dharma_and_conduct"),
    TRADITIONAL_TEACHINGS("garuda.topic.traditional_teachings"),
    LIFE_GUIDANCE("garuda.topic.life_guidance"),
    DEATH_AND_AFTERLIFE("garuda.topic.death_and_afterlife"),
    KARMA("garuda.topic.karma"),
    RITUAL_PRACTICES("garuda.topic.ritual_practices"),
    SPIRITUAL_GUIDANCE("garuda.topic.spiritual_guidance"),
    OTHER_SOURCE_BACKED("garuda.topic.other_source_backed");

    val wireId: String get() = name.lowercase()
}

@Serializable
enum class GarudaPuranContentStatus { AVAILABLE, FOUNDATION_ONLY, CONTENT_UNAVAILABLE }

@Serializable
enum class GarudaPuranContentType { SOURCE_PASSAGE, APPROVED_TRANSLATION, EDITORIAL_SUMMARY }

@Serializable
enum class GarudaPuranUnavailableReason(val translationKey: String) {
    APPROVED_PACKAGE_NOT_INSTALLED("garuda.status.package_missing"),
    NO_APPROVED_CONTENT_FOR_LANGUAGE("garuda.status.language_content_missing"),
    TOPIC_NOT_IN_APPROVED_SOURCE("garuda.status.topic_not_in_source"),
    SOURCE_REFERENCE_MISSING("garuda.status.source_reference_missing"),
    CONTENT_INVALID("garuda.status.approved_content_invalid"),
}

@Serializable
enum class GarudaPuranTextKey(val key: String) {
    PAGE_TITLE("garuda.title"),
    PAGE_SUBTITLE("garuda.subtitle"),
    LOADING("garuda.loading"),
    LOAD_ERROR("garuda.load_error"),
    RETRY("garuda.retry"),
    AVAILABLE_TOPICS("garuda.available_topics"),
    TOPIC_DETAILS("garuda.topic_details"),
    SOURCE_AND_TRADITION("garuda.source_and_tradition"),
    SOURCE_TEXT("garuda.source_text"),
    SOURCE_MEANING("garuda.source_meaning"),
    TRADITIONAL_INTERPRETATION("garuda.traditional_interpretation"),
    TRADITIONAL_PRACTICE("garuda.traditional_practice"),
    REFERENCE("garuda.reference"),
    CONTENT_UNAVAILABLE("garuda.content_unavailable"),
    GENERATE_REPORT("garuda.generate_report"),
    REPORT_LOADING("garuda.report_loading"),
    BACK_TO_TOPICS("garuda.back_to_topics"),
    TRADITION_LABEL("garuda.tradition_label"),
    DISCLAIMER_TITLE("garuda.disclaimer.title"),
    DISCLAIMER_BODY("garuda.disclaimer.body"),
    REPORT_INTRODUCTION("garuda.report.introduction"),
    REPORT_SOURCE_INFORMATION("garuda.report.source_information"),
    REPORT_AVAILABLE_TOPICS("garuda.report.available_topics"),
    REPORT_TOPIC_DETAILS("garuda.report.topic_details"),
    REPORT_TRADITIONAL_TEACHINGS("garuda.report.traditional_teachings"),
    REPORT_SOURCE_REFERENCES("garuda.report.source_references"),
    REPORT_LIMITATIONS("garuda.report.limitations"),
    TOPIC("garuda.column.topic"),
    CONTENT_COUNT("garuda.column.content_count"),
    EDITION("garuda.column.edition"),
    CHAPTER("garuda.column.chapter"),
    SECTION("garuda.column.section"),
    CANONICAL_REFERENCE("garuda.column.canonical_reference"),
    CONTENT_VERSION("garuda.column.content_version"),
    LANGUAGE("garuda.column.language"),
    DISCLAIMER_PREFIX("garuda.scriptural_prefix"),
    SOURCE_RIGHTS_UNVERIFIED("garuda.source_status.rights_unverified"),
    SOURCE_RIGHTS_UNCLEAR("garuda.source_status.rights_unclear"),
    SOURCE_REFERENCE_ONLY("garuda.source_status.reference_only"),
    SOURCE_RESTRICTED("garuda.source_status.restricted"),
    SOURCE_APPROVED_FOR_DISTRIBUTION("garuda.source_status.approved_for_distribution"),
    SOURCE_LICENSE_NOT_STATED("garuda.source_status.license_not_stated"),
    SOURCE_LICENSE_EXPLICIT("garuda.source_status.license_explicit"),
    SOURCE_RIGHTS_EXPLICITLY_GRANTED("garuda.source_status.rights_explicitly_granted"),
    SOURCE_PUBLIC_DOMAIN_CLAIM_EXPLICIT("garuda.source_status.public_domain_claim_explicit"),
    SOURCE_VERIFICATION_NOT_VERIFIED("garuda.source_status.not_verified"),
    SOURCE_VERIFICATION_FILE_IDENTITY("garuda.source_status.file_identity_verified"),
    SOURCE_VERIFICATION_INDEX("garuda.source_status.index_visually_verified"),
    SOURCE_VERIFICATION_CONTENT("garuda.source_status.content_visually_verified"),
    SOURCE_VERIFICATION_PARTIAL("garuda.source_status.partially_verified"),
    SOURCE_RIGHTS_LICENSE_UNCERTAIN("garuda.source_status.license_uncertain"),
    SOURCE_REFERENCE_ONLY_NON_DISTRIBUTABLE("garuda.source_status.reference_only_non_distributable"),
    SOURCE_RIGHTS_PUBLIC_DOMAIN_ELIGIBLE("garuda.source_status.public_domain_eligible"),
    SOURCE_RIGHTS_VERIFIED("garuda.source_status.rights_verified"),
    SOURCE_RIGHTS_REVIEW_REQUIRED("garuda.source_status.rights_review_required"),
    SOURCE_REVIEW_UNREVIEWED("garuda.source_status.review_unreviewed"),
    SOURCE_REVIEW_AUTO_NORMALIZED("garuda.source_status.review_auto_normalized"),
    SOURCE_REVIEW_EDITOR_REQUIRED("garuda.source_status.review_editor_required"),
    SOURCE_REVIEW_PENDING("garuda.source_status.review_pending"),
    SOURCE_REVIEW_SOURCE_VERIFIED("garuda.source_status.review_source_verified"),
    SOURCE_REVIEW_LICENSE_VERIFIED("garuda.source_status.review_license_verified"),
    SOURCE_REVIEW_APPROVED_FOR_APP("garuda.source_status.review_approved_for_app"),
    SOURCE_REVIEW_REJECTED("garuda.source_status.review_rejected"),
}

@Serializable
data class GarudaPuranSourceEdition(
    val editionId: String,
    val title: String,
    val publisherOrEditor: String,
    val sourceLanguage: String,
    val rightsNote: String? = null,
) {
    init {
        require(editionId.isNotBlank() && title.isNotBlank() && publisherOrEditor.isNotBlank() && sourceLanguage.isNotBlank())
    }
}

@Serializable
data class GarudaPuranSection(
    val sectionId: String,
    val title: String,
    val order: Int,
) {
    init {
        require(sectionId.isNotBlank() && title.isNotBlank() && order >= 0)
    }
}

@Serializable
data class GarudaPuranReference(
    val canonicalReferenceId: String,
    val sectionId: String? = null,
    val chapterNumber: Int? = null,
    val verseStart: Int? = null,
    val verseEnd: Int? = null,
    /** PDF/edition provenance when this citation has been verified to page level. */
    val sourceProvenance: GarudaSourceReference? = null,
) {
    init {
        require(canonicalReferenceId.isNotBlank())
        require(sectionId == null || sectionId.isNotBlank())
        require(chapterNumber == null || chapterNumber > 0)
        require(verseStart == null || verseStart > 0)
        require(verseEnd == null || verseEnd > 0)
        require(verseStart == null || verseEnd == null || verseEnd >= verseStart)
        require(sectionId != null || chapterNumber != null) { "A source location must identify a section or chapter" }
        require(
            sourceProvenance == null || sourceProvenance.canonicalReferenceId == null ||
                    sourceProvenance.canonicalReferenceId == canonicalReferenceId
        )
    }
}

/** Text is supplied by the approved content package in exactly one selected AYNVORA language. */
@Serializable
data class GarudaPuranText(
    val languageCode: String,
    val originalSourceText: String? = null,
    val transliteration: String? = null,
    val sourceMeaning: String,
    val localizedPresentation: String,
) {
    init {
        require(languageCode in SUPPORTED_GARUDA_LANGUAGES)
        require(sourceMeaning.isNotBlank() && localizedPresentation.isNotBlank())
        require(originalSourceText == null || originalSourceText.isNotBlank())
        require(transliteration == null || transliteration.isNotBlank())
    }
}

@Serializable
data class GarudaPuranInterpretation(
    val languageCode: String,
    val text: String,
    val sourceReferenceIds: List<String>,
    val traditionId: String = "GARUDA_PURAN_DHARMA",
) {
    init {
        require(languageCode in SUPPORTED_GARUDA_LANGUAGES)
        require(text.isNotBlank() && traditionId.isNotBlank() && sourceReferenceIds.isNotEmpty())
        require(sourceReferenceIds.all { it.isNotBlank() })
    }
}

@Serializable
data class GarudaPuranPractice(
    val languageCode: String,
    val text: String,
    val sourceReferenceIds: List<String>,
    val practiceType: String,
) {
    init {
        require(languageCode in SUPPORTED_GARUDA_LANGUAGES)
        require(text.isNotBlank() && practiceType.isNotBlank() && sourceReferenceIds.isNotEmpty())
        require(sourceReferenceIds.all { it.isNotBlank() })
    }
}

/** One approved source passage and its separately typed localized meaning/interpretation/practice. */
@Serializable
data class GarudaPuranContentItem(
    val contentId: String,
    val topicId: GarudaPuranTopicId,
    val section: GarudaPuranSection,
    val reference: GarudaPuranReference,
    val sourceEdition: GarudaPuranSourceEdition,
    val text: GarudaPuranText,
    val interpretations: List<GarudaPuranInterpretation> = emptyList(),
    val practices: List<GarudaPuranPractice> = emptyList(),
    val contentVersion: Int,
    val languageCode: String,
    val contentType: GarudaPuranContentType = GarudaPuranContentType.SOURCE_PASSAGE,
) {
    init {
        require(contentId.isNotBlank() && contentVersion > 0 && contentType.name.isNotBlank())
        require(languageCode in SUPPORTED_GARUDA_LANGUAGES && text.languageCode == languageCode)
        require(interpretations.all { it.languageCode == languageCode })
        require(practices.all { it.languageCode == languageCode })
        require(interpretations.all { interpretation -> interpretation.sourceReferenceIds.all { it == reference.canonicalReferenceId } })
        require(practices.all { practice -> practice.sourceReferenceIds.all { it == reference.canonicalReferenceId } })
    }
}

@Serializable
data class GarudaPuranTopicAvailability(
    val topicId: GarudaPuranTopicId,
    val status: GarudaPuranContentStatus,
    val itemCount: Int,
    val unavailableReason: GarudaPuranUnavailableReason? = null,
) {
    init {
        require(itemCount >= 0)
        when (status) {
            GarudaPuranContentStatus.AVAILABLE -> require(itemCount > 0 && unavailableReason == null)
            GarudaPuranContentStatus.FOUNDATION_ONLY, GarudaPuranContentStatus.CONTENT_UNAVAILABLE ->
                require(itemCount == 0 && unavailableReason != null)
        }
    }
}

@Serializable
data class GarudaPuranCatalog(
    val languageCode: String,
    val contentVersion: String?,
    val topics: List<GarudaPuranTopicAvailability>,
    val sourceEditions: List<GarudaPuranSourceEdition>,
    /** Inspected source metadata, including reference-only records; it does not imply an installed corpus. */
    val sourceManifest: GarudaSourceManifest = GarudaSourceManifest.empty(),
) {
    init {
        require(languageCode in SUPPORTED_GARUDA_LANGUAGES)
        require(topics.map { it.topicId }.distinct().size == topics.size)
        require(sourceEditions.map { it.editionId }.distinct().size == sourceEditions.size)
    }

    companion object {
        fun empty(languageCode: String, reason: GarudaPuranUnavailableReason) =
            GarudaPuranCatalogFactory.create(
                languageCode = languageCode,
                contentVersion = null,
                topics = GarudaPuranTopicId.entries.map { topic ->
                    GarudaPuranTopicAvailability(
                        topic,
                        GarudaPuranContentStatus.CONTENT_UNAVAILABLE,
                        0,
                        reason
                    )
                },
                sourceEditions = emptyList(),
                sourceManifest = GarudaSourceManifest.empty(),
            )
    }
}

@Serializable
data class GarudaPuranTopicContent(
    val availability: GarudaPuranTopicAvailability,
    val items: List<GarudaPuranContentItem>,
)

@Serializable
data class GarudaPuranContentSnapshot(
    val languageCode: String,
    val contentVersion: String?,
    val items: List<GarudaPuranContentItem>,
)

object GarudaPuranCatalogFactory {
    fun create(
        languageCode: String,
        contentVersion: String?,
        topics: List<GarudaPuranTopicAvailability>,
        sourceEditions: List<GarudaPuranSourceEdition>,
        sourceManifest: GarudaSourceManifest = GarudaSourceManifest.empty(),
    ) = GarudaPuranCatalog(
        languageCode,
        contentVersion,
        topics.toList(),
        sourceEditions.toList(),
        GarudaSourceManifestFactory.create(
            sourceManifest.manifestVersion,
            sourceManifest.sources,
            sourceManifest.packages,
        ),
    )
}

object GarudaPuranContentSnapshotFactory {
    fun create(languageCode: String, contentVersion: String?, items: List<GarudaPuranContentItem>) =
        GarudaPuranContentSnapshot(languageCode, contentVersion, items.map { it.deepCopy() })

    private fun GarudaPuranContentItem.deepCopy() = copy(
        interpretations = interpretations.map { it.copy(sourceReferenceIds = it.sourceReferenceIds.toList()) },
        practices = practices.map { it.copy(sourceReferenceIds = it.sourceReferenceIds.toList()) },
    )
}

interface GarudaPuranRepository {
    /** Reads only approved locally installed content. Implementations must not make network requests. */
    fun getSourceManifest(): GarudaSourceManifest = GarudaSourceManifest.empty()
    suspend fun getCatalog(languageCode: String): AynvoraResult<GarudaPuranCatalog>
    suspend fun getAvailableContent(languageCode: String): AynvoraResult<List<GarudaPuranContentItem>>
    suspend fun getTopic(
        topicId: GarudaPuranTopicId,
        languageCode: String
    ): AynvoraResult<GarudaPuranTopicContent>
}

interface GarudaPuranTextResolver {
    val languageCode: String
    fun text(key: GarudaPuranTextKey): String
    fun topicTitle(topicId: GarudaPuranTopicId): String
    fun unavailableReason(reason: GarudaPuranUnavailableReason): String
}

val SUPPORTED_GARUDA_LANGUAGES: Set<String> = setOf("en", "hi")
