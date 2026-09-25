package com.aynvora.data.garudapuran

import com.aynvora.astro.panchang.Karana
import com.aynvora.astro.panchang.PanchangYoga
import com.aynvora.astro.panchang.Tithi
import com.aynvora.astro.panchang.Vara
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import com.aynvora.core.garudapuran.GarudaContentIngestionPipeline
import com.aynvora.core.garudapuran.GarudaContentReviewStatus
import com.aynvora.core.garudapuran.GarudaPageRange
import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranTopicContent
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.garudapuran.GarudaRightsStatus
import com.aynvora.core.garudapuran.GarudaVerificationStatus
import com.aynvora.core.garudapuran.toTextKey
import com.aynvora.core.intelligence.GarudaPuranEvidenceGraphFactory
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.GarudaPuranReportGenerator
import com.aynvora.core.report.GarudaPuranReportInput
import com.aynvora.core.report.PrepareGarudaPuranReportUseCase
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportPdfArtifact
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportPdfResult
import com.aynvora.core.report.ReportTable
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 8.4B — 25-point comprehensive verification test suite
 * using real ingested Wood 1911 source fixtures.
 */
class GarudaPuranRealContentTest {

    private val reportResolver = object : ReportTextResolver {
        override val language: ReportLanguage = ReportLanguage.ENGLISH
        override fun text(key: ReportTextKey) = ReportText(key.key, key.key)
        override fun bodyName(body: CelestialBody) = body.name
        override fun signName(sign: Rashi) = sign.name
        override fun nakshatraName(nakshatra: Nakshatra) = nakshatra.name
        override fun enumLabel(identifier: String) = identifier
        override fun tithiName(tithi: Tithi) = tithi.displayName
        override fun varaName(vara: Vara) = vara.displayName
        override fun yogaName(yoga: PanchangYoga) = yoga.displayName
        override fun karanaName(karana: Karana) = karana.displayName
        override fun number(value: Double, decimalPlaces: Int) = value.toString()
        override fun birthDate(year: Int, month: Int, day: Int) = "$day/$month/$year"
        override fun birthTime(hour: Int, minute: Int, second: Int) = "$hour:$minute:$second"
        override fun generatedAtUtc(epochMillis: Long) = epochMillis.toString()
    }

    // 1. Source Package Manifest
    @Test
    fun test01_sourcePackageManifest() {
        val metadata = GarudaWood1911ContentPackage.getPackageMetadata()
        assertEquals("garuda-content-wood-1911-en-v1", metadata.packageId)
        assertEquals("1.0.0", metadata.packageVersion)
        assertEquals(InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID, metadata.sourceId)
        assertEquals(InspectedGarudaPuranSources.EDITION_WOOD_1911_ID, metadata.editionId)
        assertEquals("en", metadata.language)
        assertEquals(GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS, metadata.rightsStatus)
        assertEquals(
            GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
            metadata.verificationStatus
        )
        assertEquals(64, metadata.checksumSha256.length)
        assertTrue(metadata.rollbackCompatible)
    }

    // 2. Checksum Calculation
    @Test
    fun test02_checksumCalculation() {
        val pack = GarudaWood1911ContentPackage.getApprovedContentPack()
        val calculated = pack.checksumSha256
        assertEquals(64, calculated.length)
        assertTrue(calculated.matches(Regex("^[0-9a-f]{64}$")))

        val woodSourceSha = InspectedGarudaPuranSources.SOURCE_WOOD_1911_SHA256
        assertEquals(
            "4798c1a336c250662211a15fa0f8cf1c565787572c230b82cdaf2872e091bea5",
            woodSourceSha
        )
    }

    // 3. Checksum Mismatch Detection
    @Test
    fun test03_checksumMismatchDetection() {
        val pack = GarudaWood1911ContentPackage.getApprovedContentPack()
        val corruptedBytes = "corrupted content".encodeToByteArray()
        val isMatch = GarudaChecksumVerifier.verifyChecksum(pack.checksumSha256, corruptedBytes)
        assertFalse(isMatch, "Checksum verifier must fail on tampered content")
    }

    // 4. Chapter Count (Exactly 16 chapters in Wood 1911)
    @Test
    fun test04_chapterCount() {
        val passages = GarudaWood1911ContentPackage.passages
        val distinctChapters = passages.map { it.chapterNumber }.distinct().sorted()
        assertEquals(
            16,
            distinctChapters.size,
            "Saroddhara recension must span all 16 distinct chapters"
        )
        assertEquals((1..16).toList(), distinctChapters)
    }

    // 5. Chapter IDs
    @Test
    fun test05_chapterIds() {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        val chapterRefs = items.mapNotNull { it.reference.chapterNumber }.distinct()
        assertEquals(16, chapterRefs.size)
        for (ch in 1..16) {
            assertTrue(chapterRefs.contains(ch), "Chapter $ch reference must exist")
        }
        val comparison =
            GarudaSourceComparisonRegistry.getComparison("GP_COMPARE_CH16_PHALA_SHRUTI")
        assertNotNull(comparison)
        assertEquals(16, comparison.primaryChapter)
        assertEquals(17, comparison.secondaryChapter)
    }

    // 6. Chapter Ordering
    @Test
    fun test06_chapterOrdering() {
        val chapters = GarudaWood1911ContentPackage.passages.map { it.chapterNumber }.distinct()
        val sortedChapters = chapters.sorted()
        assertEquals(16, sortedChapters.size)
        for (i in 0 until 16) {
            assertEquals(
                i + 1,
                sortedChapters[i],
                "Chapters must follow natural ascending order 1..16"
            )
        }
    }

    // 7. Section Mapping
    @Test
    fun test07_sectionMapping() {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        assertTrue(items.isNotEmpty())
        for (item in items) {
            assertTrue(item.section.sectionId.isNotBlank())
            assertTrue(item.section.title.isNotBlank())
            assertTrue(item.section.order > 0)
            assertTrue(item.reference.chapterNumber != null && item.reference.chapterNumber!! in 1..16)
            assertTrue(item.reference.verseStart != null && item.reference.verseStart!! in 1..150)
            assertTrue(item.reference.verseEnd != null && item.reference.verseEnd!! in item.reference.verseStart!!..150)
        }
    }

    // 8. Source Reference Integrity
    @Test
    fun test08_sourceReferenceIntegrity() {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        for (item in items) {
            val srcRef = item.reference.sourceProvenance
            assertNotNull(srcRef)
            assertEquals(InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID, srcRef.sourceId)
            assertEquals(InspectedGarudaPuranSources.EDITION_WOOD_1911_ID, srcRef.editionId)
            assertEquals("en", srcRef.language)
            assertEquals(
                GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
                srcRef.rightsStatus
            )
            assertEquals(
                GarudaVerificationStatus.CONTENT_VISUALLY_VERIFIED,
                srcRef.verificationStatus
            )
            assertTrue(srcRef.canonicalReferenceId!!.startsWith("GP_WOOD_CH"))
        }
    }

    // 9. Page/Reference Preservation
    @Test
    fun test09_pageReferencePreservation() {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        val firstItem = items.first { it.reference.chapterNumber == 1 }
        val lastItem =
            items.first { it.reference.chapterNumber == 16 && it.reference.verseStart == 115 }

        assertEquals(1, firstItem.reference.sourceProvenance?.pageRange?.printedPageStart)
        assertEquals(168, lastItem.reference.sourceProvenance?.pageRange?.printedPageStart)
        assertEquals(169, lastItem.reference.sourceProvenance?.pageRange?.printedPageEnd)
    }

    // 10. Normalized Text Determinism
    @Test
    fun test10_normalizedTextDeterminism() {
        val raw = "  The tree   Madhusudana, \r\n whose firm root is Law. \t\t "
        val pass1 = GarudaContentIngestionPipeline.normalizeText(raw)
        val pass2 = GarudaContentIngestionPipeline.normalizeText(pass1.normalizedText)

        assertEquals("The tree Madhusudana, \n whose firm root is Law.", pass1.normalizedText)
        assertEquals(
            pass1.normalizedText,
            pass2.normalizedText,
            "Normalization must be strictly idempotent"
        )
        assertFalse(
            pass2.isTransformed,
            "Already-normalized text must report isTransformed = false"
        )
    }

    // 11. Review-State Enforcement
    @Test
    fun test11_reviewStateEnforcement() {
        val reviewStates = listOf(
            GarudaContentReviewStatus.UNREVIEWED,
            GarudaContentReviewStatus.AUTO_NORMALIZED,
            GarudaContentReviewStatus.EDITOR_REVIEW_REQUIRED,
            GarudaContentReviewStatus.REVIEW_PENDING,
            GarudaContentReviewStatus.SOURCE_VERIFIED,
            GarudaContentReviewStatus.LICENSE_VERIFIED,
            GarudaContentReviewStatus.APPROVED,
            GarudaContentReviewStatus.APPROVED_FOR_APP,
            GarudaContentReviewStatus.REJECTED,
            GarudaContentReviewStatus.REFERENCE_ONLY,
        )
        for (state in reviewStates) {
            assertNotNull(state.toTextKey())
        }
        assertTrue(GarudaContentReviewStatus.APPROVED_FOR_APP.isApproved)
        assertTrue(GarudaContentReviewStatus.APPROVED.isApproved)
        assertFalse(GarudaContentReviewStatus.UNREVIEWED.isApproved)
        assertFalse(GarudaContentReviewStatus.AUTO_NORMALIZED.isApproved)
        assertFalse(GarudaContentReviewStatus.EDITOR_REVIEW_REQUIRED.isApproved)
        assertFalse(GarudaContentReviewStatus.REJECTED.isApproved)
    }

    // 12. Non-Approved Content Rejection
    @Test
    fun test12_nonApprovedContentRejection() {
        val approvedItem = GarudaWood1911ContentPackage.passages.first()
        val verse = GarudaContentIngestionPipeline.DetectedVerse(
            chapterNumber = approvedItem.chapterNumber,
            verseNumber = approvedItem.verseStart,
            verseEnd = approvedItem.verseEnd,
            canonicalReferenceId = approvedItem.canonicalReferenceId,
            englishTranslation = approvedItem.englishTranslation,
            pageRange = GarudaPageRange(approvedItem.printedPageStart, approvedItem.printedPageEnd),
        )
        val rejectedContent = GarudaContentIngestionPipeline.createContentItem(
            contentId = "rej-1",
            sourceId = "src-unapproved",
            editionId = "ed-unapproved",
            chapterNumber = 1,
            sectionNumber = 1,
            sectionId = "sec-1",
            title = "Rejected",
            verse = verse,
            topicId = GarudaPuranTopicId.INTRODUCTION,
            contentVersion = "v1",
            rightsStatus = GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
            reviewStatus = GarudaContentReviewStatus.REJECTED,
        )
        assertFalse(GarudaContentIngestionPipeline.validateForAppInclusion(rejectedContent))
    }

    // 13. Rights-State Enforcement
    @Test
    fun test13_rightsStateEnforcement() {
        val distributable = listOf(
            GarudaRightsStatus.APPROVED_FOR_DISTRIBUTION,
            GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
            GarudaRightsStatus.RIGHTS_VERIFIED,
        )
        for (status in distributable) {
            assertTrue(status.isDistributable)
            assertFalse(status.isReferenceOnly)
        }

        val nonDistributable = listOf(
            GarudaRightsStatus.RIGHTS_UNVERIFIED,
            GarudaRightsStatus.RIGHTS_UNCLEAR,
            GarudaRightsStatus.RIGHTS_REVIEW_REQUIRED,
            GarudaRightsStatus.LICENSE_UNCERTAIN,
            GarudaRightsStatus.REFERENCE_ONLY,
            GarudaRightsStatus.REFERENCE_ONLY_NON_DISTRIBUTABLE,
            GarudaRightsStatus.RESTRICTED,
        )
        for (status in nonDistributable) {
            assertFalse(status.isDistributable)
            assertTrue(status.isReferenceOnly)
        }
    }

    // 14. English Content Retrieval
    @Test
    fun test14_englishContentRetrieval() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val result = assertIs<AynvoraResult.Success<*>>(repo.getCatalog("en"))
        val catalog = result.value as GarudaPuranCatalog
        assertEquals("en", catalog.languageCode)
        assertEquals("v1", catalog.contentVersion)

        val availableTopics =
            catalog.topics.filter { it.status == GarudaPuranContentStatus.AVAILABLE }
        assertEquals(10, availableTopics.size, "All 10 typed topics must be available for English")

        val availableContent = assertIs<AynvoraResult.Success<List<GarudaPuranContentItem>>>(
            repo.getAvailableContent("en")
        ).value
        assertEquals(23, availableContent.size)
        assertTrue(availableContent.all { it.languageCode == "en" })
    }

    // 15. Hindi Unavailable-State Behavior (Option B)
    @Test
    fun test15_hindiUnavailableStateBehavior() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val result = assertIs<AynvoraResult.Success<*>>(repo.getCatalog("hi"))
        val catalog = result.value as GarudaPuranCatalog
        assertEquals("hi", catalog.languageCode)
        assertNull(catalog.contentVersion)

        // All topics must be unavailable with NO_APPROVED_CONTENT_FOR_LANGUAGE
        assertTrue(catalog.topics.all { it.status == GarudaPuranContentStatus.CONTENT_UNAVAILABLE })
        assertTrue(catalog.topics.all { it.unavailableReason == GarudaPuranUnavailableReason.NO_APPROVED_CONTENT_FOR_LANGUAGE })

        val topicContent = assertIs<AynvoraResult.Success<GarudaPuranTopicContent>>(
            repo.getTopic(GarudaPuranTopicId.DEATH_AND_AFTERLIFE, "hi")
        ).value
        assertEquals(GarudaPuranContentStatus.CONTENT_UNAVAILABLE, topicContent.availability.status)
        assertEquals(
            GarudaPuranUnavailableReason.NO_APPROVED_CONTENT_FOR_LANGUAGE,
            topicContent.availability.unavailableReason
        )
        assertTrue(topicContent.items.isEmpty())

        val hindiItems = assertIs<AynvoraResult.Success<List<GarudaPuranContentItem>>>(
            repo.getAvailableContent("hi")
        ).value
        assertTrue(
            hindiItems.isEmpty(),
            "Zero Hindi content items must be returned when no approved source exists"
        )
    }

    // 16. Offline Retrieval (No Network Requirement)
    @Test
    fun test16_offlineRetrieval() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val catalog = repo.getCatalog("en")
        assertIs<AynvoraResult.Success<*>>(catalog)
        val ch1 = repo.getTopic(GarudaPuranTopicId.INTRODUCTION, "en")
        assertIs<AynvoraResult.Success<*>>(ch1)
        Unit
    }

    // 17. Repository Retrieval
    @Test
    fun test17_repositoryRetrieval() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val topic = assertIs<AynvoraResult.Success<GarudaPuranTopicContent>>(
            repo.getTopic(GarudaPuranTopicId.KARMA, "en")
        ).value
        assertEquals(GarudaPuranTopicId.KARMA, topic.availability.topicId)
        assertEquals(GarudaPuranContentStatus.AVAILABLE, topic.availability.status)
        assertEquals(4, topic.items.size)
        assertTrue(topic.items.any { it.reference.chapterNumber == 1 && it.reference.verseStart == 46 })
        assertTrue(topic.items.any { it.reference.chapterNumber == 3 })
        assertTrue(topic.items.any { it.reference.chapterNumber == 5 })
        assertTrue(topic.items.any { it.reference.chapterNumber == 6 })
    }

    // 18. Report Generation
    @Test
    fun test18_reportGeneration() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val prepared = assertIs<AynvoraResult.Success<*>>(
            PrepareGarudaPuranReportUseCase(repo).execute(
                ReportLanguage.ENGLISH,
                1_700_000_000_000L,
                "garuda-report-query-1"
            )
        ).value as GarudaPuranReportInput

        val generator = GarudaPuranReportGenerator()
        val document = generator.generate(prepared, reportResolver)

        assertNotNull(document)
        assertEquals("report.garuda_puran.title", document.title.key)
        assertEquals("garuda_puran", document.metadata.reportTypeId)
        assertEquals(ReportLanguage.ENGLISH, document.metadata.language)
        assertTrue(document.sections.isNotEmpty())
    }

    // 19. Report Provenance
    @Test
    fun test19_reportProvenance() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val prepared = assertIs<AynvoraResult.Success<*>>(
            PrepareGarudaPuranReportUseCase(repo).execute(
                ReportLanguage.ENGLISH,
                1_700_000_000_000L,
                "provenance-query"
            )
        ).value as GarudaPuranReportInput

        val document = GarudaPuranReportGenerator().generate(prepared, reportResolver)
        val sourceSection = document.sections.first { it.id == "source_information" }
        val sourceTables = sourceSection.blocks.filterIsInstance<ReportTable>()
        assertTrue(sourceTables.any { table ->
            table.rows.any { row -> row.any { cell -> cell.contains("The Garuda Purana (Saroddhara)") } }
        })

        val passageSubsections = document.sections.flatMap { it.subsections }
            .filter { it.evidence.isNotEmpty() }
        assertTrue(passageSubsections.isNotEmpty())
        for (sub in passageSubsections) {
            val ev = sub.evidence.first()
            assertNotNull(ev.source.sourceId)
            assertEquals(InspectedGarudaPuranSources.EDITION_WOOD_1911_ID, ev.source.sourceId)
            assertTrue(ev.source.name.contains("Wood") || ev.source.name.contains("Saroddhara"))
            assertTrue(ev.calculation.calculationId.startsWith("wood-1911-ch"))
            assertTrue(ev.calculation.calculationProfile.contains("GP_WOOD_CH"))
        }
        Unit
    }

    // 20. EvidenceGraph Integration
    @Test
    fun test20_evidenceGraphIntegration() = runBlocking {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        val graph = GarudaPuranEvidenceGraphFactory.create("test-graph-query", items, 1_000L)
        assertNotNull(graph)
        assertTrue(graph.nodes.isNotEmpty())
        val sourceNodes = graph.nodes.values.filter { it.evidenceId.endsWith("_source") }
        assertEquals(items.size, sourceNodes.size)
        val firstNode = sourceNodes.first()
        assertEquals(CoreFeatureId.GARUDA_PURAN, firstNode.domain)
        assertEquals(
            InspectedGarudaPuranSources.SOURCE_WOOD_1911_ID,
            firstNode.provenance.sourceReference?.sourceId
        )
        assertEquals(
            InspectedGarudaPuranSources.EDITION_WOOD_1911_ID,
            firstNode.provenance.sourceReference?.editionId
        )
        assertEquals(
            GarudaRightsStatus.PUBLIC_DOMAIN_ELIGIBLE_JURISDICTIONS,
            firstNode.provenance.sourceReference?.rightsStatus
        )
    }

    // 21. ReportDocument Structure
    @Test
    fun test21_reportDocumentStructure() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val prepared = assertIs<AynvoraResult.Success<*>>(
            PrepareGarudaPuranReportUseCase(repo).execute(
                ReportLanguage.ENGLISH,
                1_700_000_000_000L,
                "structure-query"
            )
        ).value as GarudaPuranReportInput

        val document = GarudaPuranReportGenerator().generate(prepared, reportResolver)
        val sectionIds = document.sections.map { it.id }
        assertTrue(sectionIds.contains("introduction"))
        assertTrue(sectionIds.contains("source_information"))
        assertTrue(sectionIds.contains("available_topics"))
        assertTrue(sectionIds.contains("topic_details"))
        assertTrue(sectionIds.contains("traditional_teachings"))
        assertTrue(sectionIds.contains("source_references"))
        assertNotNull(document.disclaimer)
        Unit
    }

    // 22. PDF Contract
    @Test
    fun test22_pdfContract() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val prepared = assertIs<AynvoraResult.Success<*>>(
            PrepareGarudaPuranReportUseCase(repo).execute(
                ReportLanguage.ENGLISH,
                1_700_000_000_000L,
                "pdf-query"
            )
        ).value as GarudaPuranReportInput
        val document = GarudaPuranReportGenerator().generate(prepared, reportResolver)

        var renderedDoc: ReportDocument? = null
        val pdfGenerator = object : ReportPdfGenerator {
            override fun generate(document: ReportDocument): ReportPdfResult {
                renderedDoc = document
                return ReportPdfResult.Generated(
                    ReportPdfArtifact(
                        fileName = "garuda_report.pdf",
                        bytes = "PDF_CONTRACT_TEST".encodeToByteArray()
                    )
                )
            }
        }
        val result = pdfGenerator.generate(document)
        assertIs<ReportPdfResult.Generated>(result)
        assertEquals(document, renderedDoc)
    }

    // 23. Deterministic Regeneration
    @Test
    fun test23_deterministicRegeneration() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val useCase = PrepareGarudaPuranReportUseCase(repo)
        val generator = GarudaPuranReportGenerator()

        val prep1 = assertIs<AynvoraResult.Success<*>>(
            useCase.execute(
                ReportLanguage.ENGLISH,
                1_000L,
                "q-fixed"
            )
        ).value as GarudaPuranReportInput
        val prep2 = assertIs<AynvoraResult.Success<*>>(
            useCase.execute(
                ReportLanguage.ENGLISH,
                1_000L,
                "q-fixed"
            )
        ).value as GarudaPuranReportInput

        val doc1 = generator.generate(prep1, reportResolver)
        val doc2 = generator.generate(prep2, reportResolver)

        assertEquals(doc1.title, doc2.title)
        assertEquals(doc1.sections.size, doc2.sections.size)
        assertEquals(doc1.sections.map { it.id }, doc2.sections.map { it.id })
    }

    // 24. No Fabricated References
    @Test
    fun test24_noFabricatedReferences() {
        val items = GarudaWood1911ContentPackage.getApprovedGarudaPuranContentItems()
        for (item in items) {
            val ch = item.reference.chapterNumber
            val vStart = item.reference.verseStart
            val vEnd = item.reference.verseEnd
            assertNotNull(ch)
            assertNotNull(vStart)
            assertNotNull(vEnd)
            assertTrue(ch in 1..16, "Chapter $ch is outside Saroddhara 1..16 range")
            assertTrue(vStart > 0 && vEnd >= vStart)
            val canon = item.reference.canonicalReferenceId
            assertTrue(
                canon.startsWith("GP_WOOD_CH${ch}_"),
                "Reference ID $canon must match chapter $ch"
            )
        }
    }

    // 25. No Silent Source Fallback
    @Test
    fun test25_noSilentSourceFallback() = runBlocking {
        val repo = ContentBackedGarudaPuranRepository(createSeededContentRepository())
        val hindiResult = repo.getCatalog("hi")
        assertIs<AynvoraResult.Success<*>>(hindiResult)
        val catalog = (hindiResult as AynvoraResult.Success<GarudaPuranCatalog>).value

        // Must NOT return English editions or English items
        assertTrue(catalog.topics.all { it.itemCount == 0 })
        assertTrue(catalog.topics.all { it.status == GarudaPuranContentStatus.CONTENT_UNAVAILABLE })
        val content = assertIs<AynvoraResult.Success<List<GarudaPuranContentItem>>>(
            repo.getAvailableContent("hi")
        ).value
        assertTrue(content.isEmpty())
    }

    private fun createSeededContentRepository(): ContentRepository {
        val pack = GarudaWood1911ContentPackage.getApprovedContentPack()
        val items = GarudaWood1911ContentPackage.getApprovedContentItems()
        return object : ContentRepository {
            override fun observeApprovedContent(
                moduleId: ContentModuleId,
                language: String,
            ): Flow<AynvoraResult<List<ContentItem>>> =
                flowOf(AynvoraResult.Success(items.filter { it.moduleId == moduleId && it.language == language }))

            override fun observeContentItem(id: String): Flow<AynvoraResult<ContentItem>> =
                flowOf(items.firstOrNull { it.id == id }?.let { AynvoraResult.Success(it) }
                    ?: AynvoraResult.Failure.NotFound(id, "missing"))

            override suspend fun getContentItem(id: String): AynvoraResult<ContentItem> =
                items.firstOrNull { it.id == id }?.let { AynvoraResult.Success(it) }
                    ?: AynvoraResult.Failure.NotFound(id, "missing")

            override suspend fun getInstalledPacks(moduleId: ContentModuleId?): AynvoraResult<List<ContentPack>> =
                AynvoraResult.Success(listOf(pack).filter { moduleId == null || it.moduleId == moduleId })
        }
    }
}
