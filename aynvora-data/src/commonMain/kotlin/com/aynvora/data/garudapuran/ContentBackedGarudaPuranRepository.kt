package com.aynvora.data.garudapuran

import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranContentType
import com.aynvora.core.garudapuran.GarudaPuranInterpretation
import com.aynvora.core.garudapuran.GarudaPuranPractice
import com.aynvora.core.garudapuran.GarudaPuranReference
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.garudapuran.GarudaPuranSection
import com.aynvora.core.garudapuran.GarudaPuranSourceEdition
import com.aynvora.core.garudapuran.GarudaPuranText
import com.aynvora.core.garudapuran.GarudaPuranTopicAvailability
import com.aynvora.core.garudapuran.GarudaPuranTopicContent
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.garudapuran.GarudaSourceManifest
import com.aynvora.core.garudapuran.GarudaSourceReference
import com.aynvora.core.garudapuran.SUPPORTED_GARUDA_LANGUAGES
import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.ContentTrustState
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Reads typed Garuda Puran records from the existing offline-first, approved-content store.
 * This adapter never fetches remote content and never falls back to model-authored text.
 */
class ContentBackedGarudaPuranRepository(
    private val contentRepository: ContentRepository,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GarudaPuranRepository {

    override fun getSourceManifest(): GarudaSourceManifest = InspectedGarudaPuranSources.manifest

    override suspend fun getCatalog(languageCode: String): AynvoraResult<GarudaPuranCatalog> {
        validateLanguage(languageCode)?.let { return it }
        return load(languageCode).mapSuccess { loaded ->
            buildCatalog(languageCode, loaded.packs, loaded.items)
        }
    }

    override suspend fun getAvailableContent(languageCode: String): AynvoraResult<List<GarudaPuranContentItem>> {
        validateLanguage(languageCode)?.let { return it }
        return load(languageCode).mapSuccess { loaded -> loaded.items.toList() }
    }

    override suspend fun getTopic(
        topicId: GarudaPuranTopicId,
        languageCode: String
    ): AynvoraResult<GarudaPuranTopicContent> {
        validateLanguage(languageCode)?.let { return it }
        return when (val loaded = load(languageCode)) {
            is AynvoraResult.Failure -> loaded
            is AynvoraResult.Success -> {
                val catalog = buildCatalog(languageCode, loaded.value.packs, loaded.value.items)
                val availability = catalog.topics.first { it.topicId == topicId }
                AynvoraResult.Success(
                    GarudaPuranTopicContent(
                        availability,
                        loaded.value.items.filter { it.topicId == topicId }.toList()
                    )
                )
            }
        }
    }

    private fun validateLanguage(languageCode: String): AynvoraResult.Failure.InvalidInput? =
        if (languageCode in SUPPORTED_GARUDA_LANGUAGES) null
        else AynvoraResult.Failure.InvalidInput(
            "language",
            "Garuda Puran content supports only the selected English or Hindi catalog"
        )

    private suspend fun load(languageCode: String): AynvoraResult<LoadedGarudaContent> {
        val packs =
            when (val result = contentRepository.getInstalledPacks(ContentModuleId.GARUD_PURAN)) {
                is AynvoraResult.Success -> result.value
                is AynvoraResult.Failure -> return result
            }
        val items = when (val result =
            contentRepository.observeApprovedContent(ContentModuleId.GARUD_PURAN, languageCode)
                .first()) {
            is AynvoraResult.Success -> result.value
            is AynvoraResult.Failure -> return result
        }

        val matchingPacks =
            packs.filter { it.language == languageCode && it.trustState == ContentTrustState.APPROVED_FOR_PUBLICATION }
                .associateBy { it.packId }
        val parsed = mutableListOf<GarudaPuranContentItem>()
        for (item in items) {
            if (item.language != languageCode || item.moduleId != ContentModuleId.GARUD_PURAN) {
                return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Installed content language or module does not match the requested catalog"
                )
            }
            val pack = matchingPacks[item.packId]
                ?: return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Approved content row has no matching approved local package"
                )
            if (item.trustState != ContentTrustState.APPROVED_FOR_PUBLICATION) {
                return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Content row is not approved for publication"
                )
            }
            val converted = decode(item, pack)
            if (converted is AynvoraResult.Failure) return converted
            parsed += (converted as AynvoraResult.Success).value
        }
        return AynvoraResult.Success(LoadedGarudaContent(packs, parsed))
    }

    private fun decode(
        item: ContentItem,
        pack: ContentPack
    ): AynvoraResult<GarudaPuranContentItem> {
        return try {
            val encoded = item.metadataJson
                ?: return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Approved Garuda Puran item has no structured source metadata"
                )
            val stored = json.decodeFromString<GarudaPuranStoredMetadata>(encoded)
            if (stored.schemaVersion != GARUDA_CONTENT_SCHEMA_VERSION) {
                return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Unsupported Garuda Puran content schema"
                )
            }
            val topic = GarudaPuranTopicId.entries.firstOrNull { it.wireId == stored.topicId }
                ?: return AynvoraResult.Failure.CorruptedData(
                    item.id,
                    "Unknown Garuda Puran topic identifier"
                )
            val sourceEdition = GarudaPuranSourceEdition(
                editionId = stored.sourceEditionId,
                title = pack.title,
                publisherOrEditor = stored.sourcePublisherOrEditor,
                sourceLanguage = stored.sourceLanguage,
            )
            val reference = GarudaPuranReference(
                canonicalReferenceId = stored.canonicalReferenceId,
                sectionId = stored.sectionId,
                chapterNumber = stored.chapterNumber,
                verseStart = stored.verseStart,
                verseEnd = stored.verseEnd,
                sourceProvenance = stored.sourceProvenance,
            )
            val interpretations = stored.interpretations.map {
                GarudaPuranInterpretation(
                    item.language,
                    it.text,
                    it.sourceReferenceIds.toList(),
                    it.traditionId
                )
            }
            val practices = stored.practices.map {
                GarudaPuranPractice(
                    item.language,
                    it.text,
                    it.sourceReferenceIds.toList(),
                    it.practiceType
                )
            }
            val contentType =
                GarudaPuranContentType.entries.firstOrNull { it.name == stored.contentType }
                    ?: return AynvoraResult.Failure.CorruptedData(
                        item.id,
                        "Unknown Garuda Puran content type"
                    )
            AynvoraResult.Success(
                GarudaPuranContentItem(
                    contentId = item.itemKey,
                    topicId = topic,
                    section = GarudaPuranSection(
                        stored.sectionId,
                        stored.sectionTitle,
                        stored.sectionOrder
                    ),
                    reference = reference,
                    sourceEdition = sourceEdition,
                    text = GarudaPuranText(
                        item.language,
                        stored.originalSourceText,
                        stored.transliteration,
                        stored.sourceMeaning,
                        item.body
                    ),
                    interpretations = interpretations,
                    practices = practices,
                    contentVersion = pack.contentVersion,
                    languageCode = item.language,
                    contentType = contentType,
                )
            )
        } catch (_: Exception) {
            AynvoraResult.Failure.CorruptedData(
                item.id,
                "Approved Garuda Puran item failed schema or provenance validation"
            )
        }
    }

    private fun buildCatalog(
        languageCode: String,
        packs: List<ContentPack>,
        items: List<GarudaPuranContentItem>,
    ): GarudaPuranCatalog {
        val approvedForLanguage = packs.filter {
            it.moduleId == ContentModuleId.GARUD_PURAN &&
                    it.language == languageCode &&
                    it.trustState == ContentTrustState.APPROVED_FOR_PUBLICATION
        }
        val unavailable = when {
            approvedForLanguage.isNotEmpty() -> GarudaPuranUnavailableReason.TOPIC_NOT_IN_APPROVED_SOURCE
            packs.none { it.moduleId == ContentModuleId.GARUD_PURAN && it.trustState == ContentTrustState.APPROVED_FOR_PUBLICATION } ->
                GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED

            else -> GarudaPuranUnavailableReason.NO_APPROVED_CONTENT_FOR_LANGUAGE
        }
        val grouped = items.groupBy { it.topicId }
        val topics = GarudaPuranTopicId.entries.map { topic ->
            val count = grouped[topic]?.size ?: 0
            if (count > 0) GarudaPuranTopicAvailability(
                topic,
                GarudaPuranContentStatus.AVAILABLE,
                count
            )
            else GarudaPuranTopicAvailability(
                topic,
                GarudaPuranContentStatus.CONTENT_UNAVAILABLE,
                0,
                unavailable
            )
        }
        val editions =
            items.map { it.sourceEdition }.distinctBy { it.editionId }.sortedBy { it.editionId }
        val versions = approvedForLanguage.map { it.contentVersion }.distinct().sorted()
        val contentVersion = when (versions.size) {
            0 -> null
            1 -> "v${versions.single()}"
            else -> versions.joinToString(prefix = "mixed:", separator = "+") { "v$it" }
        }
        return GarudaPuranCatalog(
            languageCode,
            contentVersion,
            topics,
            editions,
            InspectedGarudaPuranSources.manifest,
        )
    }

    private data class LoadedGarudaContent(
        val packs: List<ContentPack>,
        val items: List<GarudaPuranContentItem>
    )

    private fun <T> AynvoraResult<LoadedGarudaContent>.mapSuccess(transform: (LoadedGarudaContent) -> T): AynvoraResult<T> =
        when (this) {
            is AynvoraResult.Success -> AynvoraResult.Success(transform(value))
            is AynvoraResult.Failure -> this
        }

    @Serializable
    private data class GarudaPuranStoredMetadata(
        val schemaVersion: Int,
        val topicId: String,
        val sectionId: String,
        val sectionTitle: String,
        val sectionOrder: Int = 0,
        val canonicalReferenceId: String,
        val chapterNumber: Int? = null,
        val verseStart: Int? = null,
        val verseEnd: Int? = null,
        val sourceEditionId: String,
        val sourceLanguage: String,
        val sourcePublisherOrEditor: String,
        val sourceProvenance: GarudaSourceReference? = null,
        val originalSourceText: String? = null,
        val transliteration: String? = null,
        val sourceMeaning: String,
        val contentType: String = GarudaPuranContentType.SOURCE_PASSAGE.name,
        val interpretations: List<StoredInterpretation> = emptyList(),
        val practices: List<StoredPractice> = emptyList(),
    )

    @Serializable
    private data class StoredInterpretation(
        val text: String,
        val sourceReferenceIds: List<String>,
        val traditionId: String = "GARUDA_PURAN_DHARMA",
    )

    @Serializable
    private data class StoredPractice(
        val text: String,
        val sourceReferenceIds: List<String>,
        val practiceType: String,
    )

    private companion object {
        const val GARUDA_CONTENT_SCHEMA_VERSION = 1
    }
}
