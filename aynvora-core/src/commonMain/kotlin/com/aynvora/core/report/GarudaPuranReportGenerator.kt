package com.aynvora.core.report

import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranCatalogFactory
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentSnapshotFactory
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.GarudaPuranEvidenceGraphFactory
import com.aynvora.core.result.AynvoraResult

/** Internal construction ensures report input comes through the local approved-content repository use case. */
class GarudaPuranReportInput internal constructor(
    val language: ReportLanguage,
    val generatedAtEpochMs: Long,
    val catalog: GarudaPuranCatalog,
    val items: List<GarudaPuranContentItem>,
    val evidenceGraph: EvidenceGraph,
    val queryId: String,
) : ReportGeneratorInput

internal object GarudaPuranReportInputFactory {
    fun create(
        language: ReportLanguage,
        generatedAtEpochMs: Long,
        queryId: String,
        catalog: GarudaPuranCatalog,
        items: List<GarudaPuranContentItem>,
    ): GarudaPuranReportInput {
        require(generatedAtEpochMs >= 0L && queryId.isNotBlank())
        require(items.isNotEmpty()) { "Garuda Puran report input requires approved source-backed content" }
        require(catalog.languageCode == language.code && items.all { it.languageCode == language.code })
        require(catalog.topics.map { it.topicId }.toSet() == GarudaPuranTopicId.entries.toSet()) {
            "Garuda Puran report catalog must include every typed topic availability"
        }
        val itemCounts = items.groupingBy { it.topicId }.eachCount()
        require(catalog.topics.all { topic ->
            val count = itemCounts[topic.topicId] ?: 0
            topic.itemCount == count && (count == 0 || topic.status == GarudaPuranContentStatus.AVAILABLE)
        }) { "Garuda Puran catalog availability must match its source-backed items" }
        val editionIds = catalog.sourceEditions.map { it.editionId }.toSet()
        require(items.all { it.sourceEdition.editionId in editionIds }) {
            "Every Garuda Puran report item must name an edition in the selected catalog"
        }
        val itemSnapshot =
            GarudaPuranContentSnapshotFactory.create(language.code, catalog.contentVersion, items)
        val catalogSnapshot = GarudaPuranCatalogFactory.create(
            catalog.languageCode,
            catalog.contentVersion,
            catalog.topics,
            catalog.sourceEditions,
            catalog.sourceManifest,
        )
        return GarudaPuranReportInput(
            language = language,
            generatedAtEpochMs = generatedAtEpochMs,
            catalog = catalogSnapshot,
            items = itemSnapshot.items,
            evidenceGraph = GarudaPuranEvidenceGraphFactory.create(
                queryId,
                itemSnapshot.items,
                generatedAtEpochMs
            ),
            queryId = queryId,
        )
    }
}

/** Creates source-backed report input from the approved offline catalog, without AI or network access. */
class PrepareGarudaPuranReportUseCase(private val repository: GarudaPuranRepository) {
    suspend fun execute(
        language: ReportLanguage,
        generatedAtEpochMs: Long,
        queryId: String,
    ): AynvoraResult<GarudaPuranReportInput> {
        if (generatedAtEpochMs < 0L || queryId.isBlank()) {
            return AynvoraResult.Failure.InvalidInput(
                "report",
                "Garuda Puran report request metadata is invalid"
            )
        }
        val catalog = when (val result = repository.getCatalog(language.code)) {
            is AynvoraResult.Success -> result.value
            is AynvoraResult.Failure -> return result
        }
        if (catalog.languageCode != language.code) {
            return AynvoraResult.Failure.CorruptedData(
                "garuda_catalog",
                "Repository returned content in a different language"
            )
        }
        val items = when (val result = repository.getAvailableContent(language.code)) {
            is AynvoraResult.Success -> result.value
            is AynvoraResult.Failure -> return result
        }
        if (items.isEmpty()) {
            return AynvoraResult.Failure.NotFound(
                resourceId = "garuda_puran_content:${language.code}",
                message = "No approved source-backed Garuda Puran content is installed for this language",
            )
        }
        if (items.any { it.languageCode != language.code }) {
            return AynvoraResult.Failure.CorruptedData(
                "garuda_puran_content",
                "Repository returned mixed-language content"
            )
        }
        return runCatching {
            AynvoraResult.Success(
                GarudaPuranReportInputFactory.create(
                    language,
                    generatedAtEpochMs,
                    queryId,
                    catalog,
                    items
                )
            )
        }.getOrElse {
            AynvoraResult.Failure.CorruptedData(
                "garuda_puran_report_input",
                "Approved content failed report input validation"
            )
        }
    }
}

/** Uses only the supplied approved corpus. It contains no scripture prose or assumed chapter references. */
class GarudaPuranReportGenerator : ReportGenerator {
    override val reportType: ReportType = ReportType.GARUDA_PURAN

    override fun generate(
        input: ReportGeneratorInput,
        resolver: ReportTextResolver
    ): ReportDocument {
        require(input is GarudaPuranReportInput) { "Garuda Puran generator requires repository-prepared input" }
        require(resolver.language == input.language && input.catalog.languageCode == input.language.code)
        require(input.items.isNotEmpty()) { "No source-backed Garuda Puran content is available" }
        require(input.items.all { it.languageCode == input.language.code }) { "Report content language must be uniform" }

        val orderedItems = input.items.sortedWith(
            compareBy(
                { it.section.order },
                { it.reference.chapterNumber ?: Int.MAX_VALUE },
                { it.contentId })
        )
        val editions = orderedItems.map { it.sourceEdition }.distinctBy { it.editionId }
            .sortedBy { it.editionId }
        val availableTopics =
            input.catalog.topics.filter { it.status == GarudaPuranContentStatus.AVAILABLE && it.itemCount > 0 }
        val sections = mutableListOf<ReportSection>()
        sections += ReportSection(
            id = "introduction",
            title = resolver.text(ReportTextKey.GARUDA_INTRODUCTION),
            blocks = listOf(
                ReportParagraph(
                    ReportContentKind.SOURCE,
                    resolver.text(ReportTextKey.GARUDA_INTRODUCTION)
                )
            ),
        )
        sections += ReportSection(
            id = "source_information",
            title = resolver.text(ReportTextKey.GARUDA_SOURCE_INFORMATION),
            blocks = listOf(
                ReportTable(
                    headers = listOf(
                        ReportTextKey.GARUDA_COLUMN_EDITION,
                        ReportTextKey.SOURCE_ID,
                        ReportTextKey.GARUDA_COLUMN_LANGUAGE,
                        ReportTextKey.CONTENT_VERSION
                    ).map(resolver::text),
                    rows = editions.map { edition ->
                        listOf(
                            edition.title,
                            edition.editionId,
                            edition.sourceLanguage,
                            orderedItems.filter { it.sourceEdition.editionId == edition.editionId }
                                .map { "v${it.contentVersion}" }.distinct().sorted()
                                .joinToString(", ")
                        )
                    },
                    kind = ReportContentKind.SOURCE,
                )
            ),
            evidence = orderedItems.map(::reportEvidence),
        )
        sections += ReportSection(
            id = "available_topics",
            title = resolver.text(ReportTextKey.GARUDA_AVAILABLE_TOPICS),
            blocks = listOf(
                ReportTable(
                    headers = listOf(
                        ReportTextKey.GARUDA_COLUMN_TOPIC,
                        ReportTextKey.GARUDA_COLUMN_CONTENT_COUNT
                    ).map(resolver::text),
                    rows = availableTopics.map {
                        listOf(
                            topicName(it.topicId, resolver).value,
                            it.itemCount.toString()
                        )
                    },
                    kind = ReportContentKind.SOURCE,
                )
            ),
        )

        sections += ReportSection(
            id = "topic_details",
            title = resolver.text(ReportTextKey.GARUDA_TOPIC_DETAILS),
            blocks = orderedItems.flatMap { item ->
                buildList {
                    add(
                        ReportParagraph(
                            ReportContentKind.SOURCE,
                            resolver.text(ReportTextKey.GARUDA_SCRIPTURAL_PREFIX)
                        )
                    )
                    item.text.originalSourceText?.let {
                        add(
                            ReportParagraph(
                                ReportContentKind.SOURCE,
                                ReportText("garuda.content.${item.contentId}.original", it)
                            )
                        )
                    }
                    add(
                        ReportParagraph(
                            ReportContentKind.SOURCE,
                            ReportText(
                                "garuda.content.${item.contentId}.meaning",
                                item.text.sourceMeaning
                            )
                        )
                    )
                    add(
                        ReportParagraph(
                            ReportContentKind.SOURCE,
                            ReportText(
                                "garuda.content.${item.contentId}.presentation",
                                item.text.localizedPresentation
                            )
                        )
                    )
                }
            },
            subsections = orderedItems.map { item ->
                ReportSubsection(
                    id = "topic_${item.topicId.wireId}_${item.contentId}",
                    title = ReportText(
                        "garuda.content.${item.contentId}.title",
                        item.section.title
                    ),
                    blocks = listOf(
                        ReportKeyValue(
                            resolver.text(ReportTextKey.GARUDA_COLUMN_TOPIC),
                            topicName(item.topicId, resolver).value,
                            ReportContentKind.SOURCE
                        ),
                        ReportKeyValue(
                            resolver.text(ReportTextKey.GARUDA_COLUMN_SECTION),
                            item.section.title,
                            ReportContentKind.SOURCE
                        ),
                        ReportKeyValue(
                            resolver.text(ReportTextKey.GARUDA_COLUMN_REFERENCE),
                            item.reference.canonicalReferenceId,
                            ReportContentKind.SOURCE
                        ),
                        ReportParagraph(
                            ReportContentKind.SOURCE,
                            ReportText(
                                "garuda.content.${item.contentId}.meaning",
                                item.text.sourceMeaning
                            )
                        ),
                        ReportParagraph(
                            ReportContentKind.SOURCE,
                            ReportText(
                                "garuda.content.${item.contentId}.presentation",
                                item.text.localizedPresentation
                            )
                        ),
                    ),
                    evidence = listOf(reportEvidence(item)),
                )
            },
            evidence = orderedItems.map(::reportEvidence),
        )

        val teachings = orderedItems.flatMap { item ->
            item.interpretations.map { interpretation ->
                ReportInterpretation(
                    text = ReportText(
                        "garuda.content.${item.contentId}.interpretation",
                        interpretation.text
                    ),
                    traditionId = interpretation.traditionId,
                    source = reportSource(item),
                    supportingEvidenceIds = interpretation.sourceReferenceIds.distinct()
                        .map { "${input.queryId}_${item.contentId}_source" },
                )
            } + item.practices.map { practice ->
                ReportParagraph(
                    ReportContentKind.INTERPRETATION,
                    ReportText(
                        "garuda.content.${item.contentId}.practice.${practice.practiceType}",
                        practice.text
                    ),
                )
            }
        }
        if (teachings.isNotEmpty()) {
            sections += ReportSection(
                id = "traditional_teachings",
                title = resolver.text(ReportTextKey.GARUDA_TRADITIONAL_TEACHINGS),
                blocks = teachings,
                evidence = orderedItems.filter { it.interpretations.isNotEmpty() || it.practices.isNotEmpty() }
                    .map(::reportEvidence),
            )
        }

        val referenceRows = orderedItems.map { item ->
            listOf(
                item.sourceEdition.title,
                item.reference.chapterNumber?.toString()
                    ?: resolver.text(ReportTextKey.NOT_PROVIDED).value,
                item.section.title,
                item.reference.canonicalReferenceId,
                "v${item.contentVersion}",
            )
        }
        sections += ReportSection(
            id = "source_references",
            title = resolver.text(ReportTextKey.GARUDA_SOURCE_REFERENCES),
            blocks = listOf(
                ReportTable(
                    headers = listOf(
                        ReportTextKey.GARUDA_COLUMN_EDITION,
                        ReportTextKey.GARUDA_COLUMN_CHAPTER,
                        ReportTextKey.GARUDA_COLUMN_SECTION,
                        ReportTextKey.GARUDA_COLUMN_REFERENCE,
                        ReportTextKey.GARUDA_COLUMN_CONTENT_VERSION,
                    ).map(resolver::text),
                    rows = referenceRows,
                    kind = ReportContentKind.SOURCE,
                )
            ),
            evidence = orderedItems.map(::reportEvidence),
        )

        val unavailableTopics = GarudaPuranTopicId.entries.mapNotNull { topic ->
            val entry = input.catalog.topics.firstOrNull { it.topicId == topic }
            if (entry?.status == GarudaPuranContentStatus.AVAILABLE && entry.itemCount > 0) return@mapNotNull null
            val reason = entry?.unavailableReason
                ?: GarudaPuranUnavailableReason.TOPIC_NOT_IN_APPROVED_SOURCE
            ReportSectionAvailability(
                sectionId = "topic_${topic.wireId}",
                title = topicName(topic, resolver),
                status = ReportSectionStatus.OMITTED,
                reasonCode = reportReason(reason),
                reason = reasonText(reason, resolver),
            )
        }
        if (unavailableTopics.isNotEmpty()) {
            sections += ReportSection(
                id = "limitations",
                title = resolver.text(ReportTextKey.GARUDA_LIMITATIONS),
                blocks = unavailableTopics.map {
                    ReportParagraph(
                        ReportContentKind.DISCLAIMER,
                        it.reason
                    )
                },
            )
        }

        val versionLabel =
            orderedItems.map { "v${it.contentVersion}" }.distinct().sorted().joinToString("+")
        return ReportDocumentFactory.create(
            metadata = ReportMetadata(
                reportId = "garuda_puran-${input.generatedAtEpochMs}-${input.queryId}",
                reportTypeId = reportType.id,
                generatedAtEpochMs = input.generatedAtEpochMs,
                language = input.language,
                version = ReportVersion("1.0.0", "not_applicable", versionLabel),
                identity = ReportIdentity(),
                feature = reportType.feature,
                featureStatus = ReportFeatureStatus.LIMITED,
            ),
            title = resolver.text(ReportTextKey.GARUDA_PURAN_TITLE),
            sections = sections,
            availability = unavailableTopics,
            disclaimer = ReportDisclaimer(
                resolver.text(ReportTextKey.GARUDA_DISCLAIMER_TITLE),
                resolver.text(ReportTextKey.GARUDA_DISCLAIMER_BODY),
            ),
            graph = input.evidenceGraph,
        )
    }

    private fun reportEvidence(item: GarudaPuranContentItem): ReportEvidence {
        val source = reportSource(item)
        return ReportEvidence(
            evidenceId = "garuda:${item.contentId}:source",
            contentKind = ReportContentKind.SOURCE,
            text = ReportText("garuda.content.${item.contentId}.meaning", item.text.sourceMeaning),
            calculation = ReportCalculationReference(
                calculationId = item.contentId,
                calculationProfile = "content:${item.contentVersion}:${item.languageCode}:${item.contentType.name}:${item.reference.canonicalReferenceId}",
                calculationVersion = "v${item.contentVersion}",
                sourceId = source.sourceId,
                referenceStatus = ReportReferenceStatus.NOT_ASSESSED,
            ),
            source = source,
        )
    }

    private fun reportSource(item: GarudaPuranContentItem) =
        ReportSource(
            item.sourceEdition.editionId,
            item.sourceEdition.title,
            item.reference.canonicalReferenceId
        )

    private fun topicName(topic: GarudaPuranTopicId, resolver: ReportTextResolver): ReportText =
        resolver.text(
            when (topic) {
                GarudaPuranTopicId.INTRODUCTION -> ReportTextKey.GARUDA_TOPIC_INTRODUCTION
                GarudaPuranTopicId.DIALOGUE_CONTEXT -> ReportTextKey.GARUDA_TOPIC_DIALOGUE_CONTEXT
                GarudaPuranTopicId.DHARMA_AND_CONDUCT -> ReportTextKey.GARUDA_TOPIC_DHARMA_AND_CONDUCT
                GarudaPuranTopicId.TRADITIONAL_TEACHINGS -> ReportTextKey.GARUDA_TOPIC_TRADITIONAL_TEACHINGS
                GarudaPuranTopicId.LIFE_GUIDANCE -> ReportTextKey.GARUDA_TOPIC_LIFE_GUIDANCE
                GarudaPuranTopicId.DEATH_AND_AFTERLIFE -> ReportTextKey.GARUDA_TOPIC_DEATH_AND_AFTERLIFE
                GarudaPuranTopicId.KARMA -> ReportTextKey.GARUDA_TOPIC_KARMA
                GarudaPuranTopicId.RITUAL_PRACTICES -> ReportTextKey.GARUDA_TOPIC_RITUAL_PRACTICES
                GarudaPuranTopicId.SPIRITUAL_GUIDANCE -> ReportTextKey.GARUDA_TOPIC_SPIRITUAL_GUIDANCE
                GarudaPuranTopicId.OTHER_SOURCE_BACKED -> ReportTextKey.GARUDA_TOPIC_OTHER_SOURCE_BACKED
            }
        )

    private fun reportReason(reason: GarudaPuranUnavailableReason) = when (reason) {
        GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED -> ReportUnavailableReason.APPROVED_CONTENT_NOT_INSTALLED
        GarudaPuranUnavailableReason.NO_APPROVED_CONTENT_FOR_LANGUAGE -> ReportUnavailableReason.CONTENT_UNAVAILABLE_IN_LANGUAGE
        GarudaPuranUnavailableReason.TOPIC_NOT_IN_APPROVED_SOURCE -> ReportUnavailableReason.TOPIC_NOT_AVAILABLE_IN_SOURCE
        GarudaPuranUnavailableReason.SOURCE_REFERENCE_MISSING,
        GarudaPuranUnavailableReason.CONTENT_INVALID -> ReportUnavailableReason.TOPIC_NOT_AVAILABLE_IN_SOURCE
    }

    private fun reasonText(reason: GarudaPuranUnavailableReason, resolver: ReportTextResolver) =
        resolver.text(
            when (reason) {
                GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED -> ReportTextKey.GARUDA_PACKAGE_MISSING
                GarudaPuranUnavailableReason.NO_APPROVED_CONTENT_FOR_LANGUAGE -> ReportTextKey.GARUDA_LANGUAGE_CONTENT_MISSING
                else -> ReportTextKey.GARUDA_TOPIC_NOT_IN_SOURCE
            }
        )
}
