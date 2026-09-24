package com.aynvora.data.garudapuran

import com.aynvora.core.garudapuran.GarudaPuranContentItem
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GarudaPuranUnavailableReason
import com.aynvora.core.models.ContentItem
import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentPack
import com.aynvora.core.models.ContentTrustState
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GarudaPuranRepositoryTest {
    @Test
    fun emptyOfflineStoreExposesTopicsAsUnavailableInsteadOfSeedingInventedContent() = runBlocking {
        val repository =
            ContentBackedGarudaPuranRepository(FakeContentRepository(emptyList(), emptyList()))

        val result = assertIs<AynvoraResult.Success<*>>(repository.getCatalog("en"))
        val catalog = result.value as com.aynvora.core.garudapuran.GarudaPuranCatalog

        assertTrue(catalog.topics.isNotEmpty())
        assertTrue(catalog.topics.all { it.status == GarudaPuranContentStatus.CONTENT_UNAVAILABLE && it.itemCount == 0 })
        val content = assertIs<AynvoraResult.Success<List<GarudaPuranContentItem>>>(
            repository.getAvailableContent("en")
        )
        assertTrue(content.value.isEmpty())
        val topic =
            assertIs<AynvoraResult.Success<com.aynvora.core.garudapuran.GarudaPuranTopicContent>>(
                repository.getTopic(GarudaPuranTopicId.DEATH_AND_AFTERLIFE, "en")
            ).value
        assertEquals(GarudaPuranContentStatus.CONTENT_UNAVAILABLE, topic.availability.status)
        assertEquals(
            GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED,
            topic.availability.unavailableReason
        )
        assertTrue(topic.items.isEmpty())
    }

    @Test
    fun approvedPackRowsPreserveReferenceVersionAndSelectedLanguage() = runBlocking {
        val pack = approvedPack()
        val item = contentItem(metadataJson = validMetadata)
        val repository =
            ContentBackedGarudaPuranRepository(FakeContentRepository(listOf(pack), listOf(item)))

        val catalog =
            assertIs<AynvoraResult.Success<*>>(repository.getCatalog("en")).value as com.aynvora.core.garudapuran.GarudaPuranCatalog
        val content =
            assertIs<AynvoraResult.Success<List<*>>>(repository.getAvailableContent("en")).value.single() as com.aynvora.core.garudapuran.GarudaPuranContentItem

        assertEquals("v3", catalog.contentVersion)
        assertEquals(
            GarudaPuranContentStatus.AVAILABLE,
            catalog.topics.single { it.topicId == GarudaPuranTopicId.DHARMA_AND_CONDUCT }.status
        )
        assertEquals("GP_TEST_CHAPTER_1_V2", content.reference.canonicalReferenceId)
        assertEquals("v3", content.contentVersion.toString().let { "v$it" })
        assertEquals("en", content.languageCode)
        assertEquals("TEST SOURCE TEXT", content.text.originalSourceText)
    }

    @Test
    fun malformedSourceMetadataFailsClosed() = runBlocking {
        val repository = ContentBackedGarudaPuranRepository(
            FakeContentRepository(
                listOf(approvedPack()),
                listOf(contentItem(metadataJson = "{}"))
            )
        )

        assertIs<AynvoraResult.Failure.CorruptedData>(repository.getAvailableContent("en"))
        Unit
    }

    @Test
    fun verifiedButUnapprovedPackageCannotSupplyGarudaContent() = runBlocking {
        val unapprovedPack = approvedPack().copy(trustState = ContentTrustState.VERIFIED)
        val repository = ContentBackedGarudaPuranRepository(
            FakeContentRepository(
                listOf(unapprovedPack),
                listOf(contentItem(metadataJson = validMetadata))
            ),
        )

        assertIs<AynvoraResult.Failure.CorruptedData>(repository.getAvailableContent("en"))
        Unit
    }

    @Test
    fun rejectsUnsupportedLanguageWithoutEnglishFallback() = runBlocking {
        val repository: GarudaPuranRepository =
            ContentBackedGarudaPuranRepository(FakeContentRepository(emptyList(), emptyList()))

        assertIs<AynvoraResult.Failure.InvalidInput>(repository.getCatalog("fr"))
        Unit
    }

    private class FakeContentRepository(
        private val packs: List<ContentPack>,
        private val items: List<ContentItem>,
    ) : ContentRepository {
        override fun observeApprovedContent(
            moduleId: ContentModuleId,
            language: String
        ): Flow<AynvoraResult<List<ContentItem>>> =
            flowOf(AynvoraResult.Success(items.filter { it.moduleId == moduleId && it.language == language }))

        override fun observeContentItem(id: String): Flow<AynvoraResult<ContentItem>> =
            flowOf(items.firstOrNull { it.id == id }?.let { AynvoraResult.Success(it) }
                ?: AynvoraResult.Failure.NotFound(id, "missing"))

        override suspend fun getContentItem(id: String): AynvoraResult<ContentItem> =
            items.firstOrNull { it.id == id }?.let { AynvoraResult.Success(it) }
                ?: AynvoraResult.Failure.NotFound(id, "missing")

        override suspend fun getInstalledPacks(moduleId: ContentModuleId?): AynvoraResult<List<ContentPack>> =
            AynvoraResult.Success(packs.filter { moduleId == null || it.moduleId == moduleId })
    }

    private fun approvedPack() = ContentPack(
        packId = "garuda-test-pack-en",
        moduleId = ContentModuleId.GARUD_PURAN,
        contentVersion = 3,
        language = "en",
        title = "Test package",
        sourceAttribution = "Test approved fixture",
        trustState = ContentTrustState.APPROVED_FOR_PUBLICATION,
        checksumSha256 = "0".repeat(64),
        installedAtEpochMs = 1,
        lastSyncEpochMs = 1,
    )

    private fun contentItem(metadataJson: String) = ContentItem(
        id = "test-item-1",
        packId = "garuda-test-pack-en",
        moduleId = ContentModuleId.GARUD_PURAN,
        itemKey = "test-item-1",
        title = "Approved test topic",
        body = "Approved test presentation.",
        language = "en",
        metadataJson = metadataJson,
        trustState = ContentTrustState.APPROVED_FOR_PUBLICATION,
    )

    private val validMetadata = """
        {
          "schemaVersion": 1,
          "topicId": "dharma_and_conduct",
          "sectionId": "test-section",
          "sectionTitle": "Test section",
          "canonicalReferenceId": "GP_TEST_CHAPTER_1_V2",
          "chapterNumber": 1,
          "verseStart": 2,
          "verseEnd": 2,
          "sourceEditionId": "test-edition",
          "sourceLanguage": "sa",
          "sourcePublisherOrEditor": "AYNVORA test fixture",
          "originalSourceText": "TEST SOURCE TEXT",
          "transliteration": "TEST TRANSLITERATION",
          "sourceMeaning": "Approved test meaning.",
          "interpretations": [{"text": "Test interpretation.", "sourceReferenceIds": ["GP_TEST_CHAPTER_1_V2"]}],
          "practices": []
        }
    """.trimIndent()
}
