package com.aynvora.core.intelligence

import com.aynvora.core.Aynvora
import com.aynvora.core.ai.DefaultAiToolRegistry
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.garudapuran.GarudaPageRange
import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranInterpretation
import com.aynvora.core.garudapuran.GarudaPuranReference
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.garudapuran.GarudaPuranSection
import com.aynvora.core.garudapuran.GarudaPuranSourceEdition
import com.aynvora.core.garudapuran.GarudaPuranText
import com.aynvora.core.garudapuran.GarudaPuranTopicAvailability
import com.aynvora.core.garudapuran.GarudaPuranTopicContent
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaRightsStatus
import com.aynvora.core.garudapuran.GarudaSourceReference
import com.aynvora.core.garudapuran.GarudaVerificationStatus
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GarudaPuranOrchestratorTest {
    @Test
    fun standaloneGarudaUsesNoBirthDataAddsCitedTraditionalEvidenceAndDoesNotInferAstrologyLinks() =
        runBlocking {
            val item = sourceItem()
            val repository = object : GarudaPuranRepository {
                override suspend fun getCatalog(languageCode: String) =
                    AynvoraResult.Success(
                        GarudaPuranCatalog.empty(
                            languageCode,
                            com.aynvora.core.garudapuran.GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED
                        )
                    )

                override suspend fun getAvailableContent(languageCode: String) =
                    AynvoraResult.Success(listOf(item))

                override suspend fun getTopic(topicId: GarudaPuranTopicId, languageCode: String) =
                    AynvoraResult.Success(
                        GarudaPuranTopicContent(
                            GarudaPuranTopicAvailability(
                                topicId,
                                GarudaPuranContentStatus.AVAILABLE,
                                1
                            ),
                            listOf(item),
                        )
                    )
            }
            val request = IntelligenceRequest(
                queryContext = QueryContext(
                    queryId = "garuda_only",
                    intentType = IntelligenceIntentType.GENERAL_KNOWLEDGE,
                    requestedDomains = setOf(CoreFeatureId.GARUDA_PURAN),
                    timeContext = TimeContext(1_000L, "UTC"),
                ),
                userContext = UserContext(),
            )
            val result = MultiFeatureOrchestrator(
                sdk = Aynvora.create(),
                toolRegistry = DefaultAiToolRegistry(),
                garudaPuranRepository = repository,
            ).orchestrate(request)

            val response = (result as AynvoraResult.Success).value
            assertEquals(GuidanceStatus.TRADITIONAL_INTERPRETATION, response.status)
            assertEquals(
                setOf(CoreFeatureId.GARUDA_PURAN),
                response.evidenceBundle.contributingDomains
            )
            assertEquals(2, response.evidenceBundle.items.size)
            assertTrue(response.evidenceBundle.items.all { it.domain == CoreFeatureId.GARUDA_PURAN })
            assertTrue(response.evidenceBundle.items.all { it.provenance.referenceId == "GP_TEST_CHAPTER_1_V2" })
            assertTrue(response.evidenceBundle.items.all {
                it.provenance.sourceReference?.sourceId == "test-source" &&
                        it.provenance.sourceReference.pageRange?.printedPageStart == 20
            })
            assertEquals(1, response.evidenceGraph.edges.size)
            assertTrue(response.evidenceGraph.edges.none { edge ->
                response.evidenceGraph.nodes[edge.sourceEvidenceId]?.domain == CoreFeatureId.ASTROLOGY ||
                        response.evidenceGraph.nodes[edge.targetEvidenceId]?.domain == CoreFeatureId.ASTROLOGY
            })
        }

    @Test
    fun missingLocalCorpusProducesUnavailableCapabilityWithoutInventedEvidence() = runBlocking {
        val request = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "garuda_empty",
                intentType = IntelligenceIntentType.GENERAL_KNOWLEDGE,
                requestedDomains = setOf(CoreFeatureId.GARUDA_PURAN),
                timeContext = TimeContext(1_000L, "UTC"),
            ),
            userContext = UserContext(),
        )
        val response = (MultiFeatureOrchestrator(
            Aynvora.create(),
            DefaultAiToolRegistry()
        ).orchestrate(request) as AynvoraResult.Success).value
        assertEquals(GuidanceStatus.UNAVAILABLE_CAPABILITY, response.status)
        assertTrue(response.evidenceBundle.items.isEmpty())
        assertTrue(response.evidenceGraph.nodes.isEmpty())
    }

    private fun sourceItem() = GarudaPuranContentItem(
        contentId = "approved-fixture-item",
        topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
        section = GarudaPuranSection("test-section", "Test section", 1),
        reference = GarudaPuranReference(
            "GP_TEST_CHAPTER_1_V2",
            "test-section",
            chapterNumber = 1,
            verseStart = 2,
            verseEnd = 2,
            sourceProvenance = GarudaSourceReference(
                sourceId = "test-source",
                editionId = "fixture",
                chapter = "1",
                section = "test-section",
                pageRange = GarudaPageRange(pdfPageStart = 21, printedPageStart = 20),
                language = "sa",
                contentVersion = "v3",
                rightsStatus = GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
                verificationStatus = GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                canonicalReferenceId = "GP_TEST_CHAPTER_1_V2",
            ),
        ),
        sourceEdition = GarudaPuranSourceEdition(
            "fixture",
            "Approved test fixture",
            "AYNVORA test fixture",
            "sa"
        ),
        text = GarudaPuranText(
            "en",
            "TEST SOURCE TEXT",
            null,
            "Approved test meaning.",
            "Approved test presentation."
        ),
        interpretations = listOf(
            GarudaPuranInterpretation(
                "en",
                "Test traditional interpretation.",
                listOf("GP_TEST_CHAPTER_1_V2")
            )
        ),
        contentVersion = 3,
        languageCode = "en",
    )
}
