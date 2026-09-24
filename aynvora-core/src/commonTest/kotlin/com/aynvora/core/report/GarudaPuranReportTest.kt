package com.aynvora.core.report

import com.aynvora.core.Aynvora
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranInterpretation
import com.aynvora.core.garudapuran.GarudaPuranReference
import com.aynvora.core.garudapuran.GarudaPuranSection
import com.aynvora.core.garudapuran.GarudaPuranSourceEdition
import com.aynvora.core.garudapuran.GarudaPuranText
import com.aynvora.core.garudapuran.GarudaPuranTopicAvailability
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.intelligence.CapabilityStatus
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.FeatureCapabilityRegistry
import com.aynvora.core.intelligence.GarudaPuranEvidenceGraphFactory
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GarudaPuranReportTest {
    private val resolver = TestReportTextResolver(ReportLanguage.ENGLISH)

    @Test
    fun registeredGeneratorBuildsLocalizedSourceBackedReportWithOnlyAvailableSections() =
        runBlocking {
            val input = reportInput()
            val request = ReportGenerationRequest(
                reportType = ReportType.GARUDA_PURAN,
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1_000L,
                generatorInput = input,
                evidenceGraph = input.evidenceGraph,
            )
            val result = GenerateReportUseCase(Aynvora.create()).execute(request, resolver)
            val document = assertIs<ReportGenerationResult.Generated>(result).document

            assertEquals("garuda_puran", document.metadata.reportTypeId)
            assertEquals(ReportFeatureStatus.LIMITED, document.metadata.featureStatus)
            assertEquals("v3", document.metadata.version.contentVersion)
            assertEquals(
                listOf(
                    "introduction",
                    "source_information",
                    "available_topics",
                    "topic_details",
                    "traditional_teachings",
                    "source_references",
                    "limitations"
                ),
                document.sections.map { it.id },
            )
            assertFalse(document.sections.any { it.id == "empty_topic" })
            val topic = document.sections.single { it.id == "topic_details" }
            assertTrue(
                topic.blocks.filterIsInstance<ReportParagraph>()
                    .any { it.kind == ReportContentKind.SOURCE && it.text.value == "Approved test meaning." })
            assertTrue(
                topic.blocks.filterIsInstance<ReportInterpretation>()
                    .all { it.traditionId == "GARUDA_PURAN_DHARMA" })
            assertEquals(
                "GP_TEST_CHAPTER_1",
                document.sections.single { it.id == "source_references" }.blocks.filterIsInstance<ReportTable>()
                    .single().rows.single()[3]
            )
            assertEquals(
                GarudaPuranTopicId.entries.size - 1,
                document.sectionAvailability.count { it.status == ReportSectionStatus.OMITTED })
            assertNotNull(document.evidenceGraph?.nodes?.get("test-query_test-content-1_source"))
            val repeated = assertIs<ReportGenerationResult.Generated>(
                GenerateReportUseCase(Aynvora.create()).execute(
                    request,
                    resolver
                )
            ).document
            assertEquals(
                document,
                repeated,
                "The same approved content and request metadata must produce the same report"
            )
            Unit
        }

    @Test
    fun preparationAndReportEngineKeepMissingCorpusUnavailable() = runBlocking {
        val repository = object : com.aynvora.core.garudapuran.GarudaPuranRepository {
            override suspend fun getCatalog(languageCode: String) = AynvoraResult.Success(
                GarudaPuranCatalog.empty(
                    languageCode,
                    com.aynvora.core.garudapuran.GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED
                )
            )

            override suspend fun getAvailableContent(languageCode: String) =
                AynvoraResult.Success(emptyList<GarudaPuranContentItem>())

            override suspend fun getTopic(topicId: GarudaPuranTopicId, languageCode: String) =
                AynvoraResult.Success(
                    com.aynvora.core.garudapuran.GarudaPuranTopicContent(
                        GarudaPuranTopicAvailability(
                            topicId,
                            GarudaPuranContentStatus.CONTENT_UNAVAILABLE,
                            0,
                            com.aynvora.core.garudapuran.GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED,
                        ),
                        emptyList(),
                    )
                )
        }
        val prepared = PrepareGarudaPuranReportUseCase(repository).execute(
            ReportLanguage.ENGLISH,
            1_000L,
            "empty"
        )
        assertIs<AynvoraResult.Failure.NotFound>(prepared)

        val unavailable = GenerateReportUseCase(Aynvora.create()).execute(
            ReportGenerationRequest(
                ReportType.GARUDA_PURAN,
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1_000L
            ),
            resolver,
        )
        val gated = assertIs<ReportGenerationResult.Unavailable>(unavailable)
        assertEquals(ReportErrorCode.FOUNDATION_ONLY, gated.code)
        Unit
    }

    @Test
    fun missingOrMismatchedContentDoesNotGenerateAReport() {
        val generator = GarudaPuranReportGenerator()
        val error = runCatching {
            generator.generate(
                reportInput(items = emptyList()),
                resolver
            )
        }.exceptionOrNull()
        assertNotNull(error)

        val prepared = reportInput()
        val wrongLanguage = runCatching {
            GarudaPuranReportInputFactory.create(
                language = ReportLanguage.HINDI,
                generatedAtEpochMs = prepared.generatedAtEpochMs,
                queryId = prepared.queryId,
                catalog = prepared.catalog,
                items = prepared.items,
            )
        }.exceptionOrNull()
        assertNotNull(wrongLanguage)

        val incorrectCatalog = prepared.catalog.copy(
            topics = prepared.catalog.topics.map { availability ->
                if (availability.topicId == GarudaPuranTopicId.DHARMA_AND_CONDUCT) availability.copy(
                    itemCount = 2
                )
                else availability
            },
        )
        val mismatchedCatalog = runCatching {
            GarudaPuranReportInputFactory.create(
                language = prepared.language,
                generatedAtEpochMs = prepared.generatedAtEpochMs,
                queryId = prepared.queryId,
                catalog = incorrectCatalog,
                items = prepared.items,
            )
        }.exceptionOrNull()
        assertNotNull(mismatchedCatalog)
    }

    @Test
    fun sourceBackedGarudaContentDoesNotRequireBirthDataOrAstrologyAndKeepsFeatureStatusLimited() =
        runBlocking {
            val descriptor = CanonicalCoreFeatures.first { it.id == CoreFeatureId.GARUDA_PURAN }
            assertIs<FeatureAvailability.ComingSoon>(descriptor.availability)
            assertEquals(
                CapabilityStatus.FOUNDATION_ONLY,
                FeatureCapabilityRegistry.getCapabilitiesForDomain(CoreFeatureId.GARUDA_PURAN)
                    .first { it.capabilityId == "garuda_chapter_search" }.status
            )
            assertEquals(
                CapabilityStatus.IMPLEMENTED,
                FeatureCapabilityRegistry.getCapabilitiesForDomain(CoreFeatureId.GARUDA_PURAN)
                    .first { it.capabilityId == "garuda_source_provenance" }.status
            )

            val result = GenerateReportUseCase(Aynvora.create()).execute(
                ReportGenerationRequest(
                    ReportType.GARUDA_PURAN,
                    language = ReportLanguage.ENGLISH,
                    generatedAtEpochMs = 1_000L,
                    generatorInput = reportInput()
                ),
                resolver,
            )
            assertEquals(
                ReportFeatureStatus.LIMITED,
                assertIs<ReportGenerationResult.Generated>(result).document.metadata.featureStatus
            )
        }

    @Test
    fun evidenceGraphSeparatesScripturalContentFromInterpretationAndCarriesVersionReferenceAndLocale() {
        val item = reportInput().items.single()
        val graph = GarudaPuranEvidenceGraphFactory.create("q1", listOf(item), 1_000L)
        val source = graph.nodes.getValue("q1_test-content-1_source")
        val interpretation = graph.nodes.getValue("q1_test-content-1_interpretation_0")
        assertEquals(EvidenceCategory.TRADITIONAL_RULE, source.category)
        assertEquals(EvidenceCategory.INTERPRETATION, interpretation.category)
        assertEquals("GP_TEST_CHAPTER_1", source.provenance.referenceId)
        assertEquals("v3", source.provenance.contentVersion)
        assertEquals("en", source.provenance.locale)
        assertEquals(
            source.evidenceId,
            graph.traceWhy(interpretation.evidenceId).single().evidenceId
        )
    }

    @Test
    fun reportPdfAndShareAnalyticsUseExistingAbstractionWithoutContentOrReferences() = runBlocking {
        val events = mutableListOf<AnalyticsEvent>()
        val tracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                events += event
            }
        }
        val request = ReportGenerationRequest(
            reportType = ReportType.GARUDA_PURAN,
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1_000L,
            generatorInput = reportInput(),
        )
        val document = assertIs<ReportGenerationResult.Generated>(
            GenerateReportUseCase(Aynvora.create(), analyticsTracker = tracker).execute(
                request,
                resolver
            ),
        ).document
        val pdf = assertIs<ReportPdfOutcome.Generated>(
            GenerateReportPdfUseCase(object : ReportPdfGenerator {
                override fun generate(document: ReportDocument) =
                    ReportPdfResult.Generated(
                        ReportPdfArtifact(
                            "garuda.pdf",
                            bytes = byteArrayOf(37, 80, 68, 70)
                        )
                    )
            }, tracker).execute(document),
        ).artifact
        ShareReportUseCase(object : ReportShareService {
            override fun share(artifact: ReportPdfArtifact) = ReportShareResult.Shared
        }, tracker).execute(pdf, ReportType.GARUDA_PURAN.id)

        assertTrue(events.any { it.name == "garuda_puran_report_generated" && it.params.isEmpty() })
        assertTrue(events.any { it.name == "garuda_puran_pdf_generated" && it.params.isEmpty() })
        assertTrue(events.any { it.name == "garuda_puran_shared" && it.params.isEmpty() })
        assertTrue(events.flatMap { it.params.values }.none {
            it.toString().contains("Approved test") || it.toString()
                .contains("GP_TEST") || it.toString().contains("TEST SOURCE")
        })
    }

    private fun reportInput(items: List<GarudaPuranContentItem> = listOf(testItem())): GarudaPuranReportInput {
        val topic = GarudaPuranTopicAvailability(
            GarudaPuranTopicId.DHARMA_AND_CONDUCT,
            GarudaPuranContentStatus.AVAILABLE,
            itemCount = items.size
        )
        val catalog = GarudaPuranCatalog(
            "en",
            "v3",
            GarudaPuranTopicId.entries.map { id ->
                if (id == topic.topicId) topic
                else GarudaPuranTopicAvailability(
                    id,
                    GarudaPuranContentStatus.CONTENT_UNAVAILABLE,
                    itemCount = 0,
                    unavailableReason = com.aynvora.core.garudapuran.GarudaPuranUnavailableReason.TOPIC_NOT_IN_APPROVED_SOURCE,
                )
            },
            items.map { it.sourceEdition }.distinctBy { it.editionId },
        )
        return GarudaPuranReportInputFactory.create(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1_000L,
            queryId = "test-query",
            catalog = catalog,
            items = items,
        )
    }

    private fun testItem() = GarudaPuranContentItem(
        contentId = "test-content-1",
        topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
        section = GarudaPuranSection("test-section", "Test section", 1),
        reference = GarudaPuranReference("GP_TEST_CHAPTER_1", "test-section", chapterNumber = 1),
        sourceEdition = GarudaPuranSourceEdition(
            "test-edition",
            "Test approved fixture",
            "AYNVORA test fixture",
            "sa"
        ),
        text = GarudaPuranText(
            languageCode = "en",
            originalSourceText = "TEST SOURCE TEXT",
            sourceMeaning = "Approved test meaning.",
            localizedPresentation = "Approved test presentation.",
        ),
        interpretations = listOf(
            GarudaPuranInterpretation(
                "en",
                "Test traditional interpretation.",
                listOf("GP_TEST_CHAPTER_1")
            )
        ),
        contentVersion = 3,
        languageCode = "en",
    )
}
