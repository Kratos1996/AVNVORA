package com.aynvora.core.garudapuran

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class GarudaPuranDomainTest {
    @Test
    fun emptyCatalogKeepsKnownTopicsExplicitlyUnavailableWithoutInventingText() {
        val catalog = GarudaPuranCatalog.empty(
            "en",
            GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED
        )

        assertEquals("en", catalog.languageCode)
        assertTrue(catalog.topics.isNotEmpty())
        assertTrue(catalog.topics.all { it.status == GarudaPuranContentStatus.CONTENT_UNAVAILABLE })
        assertTrue(catalog.topics.all { it.unavailableReason == GarudaPuranUnavailableReason.APPROVED_PACKAGE_NOT_INSTALLED })
        assertTrue(catalog.sourceEditions.isEmpty())
    }

    @Test
    fun catalogFactoryDefensivelyCopiesNestedCollections() {
        val interpretationReferences = mutableListOf("GP_TEST_CHAPTER_1")
        val interpretations = mutableListOf(
            GarudaPuranInterpretation("en", "Test interpretation text.", interpretationReferences),
        )
        val items = mutableListOf(testItem(interpretations))
        val topics = mutableListOf(
            GarudaPuranTopicAvailability(
                GarudaPuranTopicId.DHARMA_AND_CONDUCT,
                GarudaPuranContentStatus.AVAILABLE,
                itemCount = 1
            ),
        )
        val catalog = GarudaPuranCatalogFactory.create(
            "en",
            "v3",
            topics,
            listOf(items.single().sourceEdition)
        )
        val snapshot = GarudaPuranContentSnapshotFactory.create("en", "v3", items)

        interpretationReferences.clear()
        interpretations.clear()
        items.clear()
        topics.clear()

        assertEquals(1, catalog.topics.size)
        assertEquals(1, snapshot.items.size)
        assertEquals(
            listOf("GP_TEST_CHAPTER_1"),
            snapshot.items.single().interpretations.single().sourceReferenceIds
        )
        assertNotSame(items, snapshot.items)
    }

    @Test
    fun contentRequiresCanonicalReferenceAndSupportedLanguage() {
        val item = testItem()
        assertEquals("en", item.languageCode)
        assertEquals("GP_TEST_CHAPTER_1", item.reference.canonicalReferenceId)
        assertEquals(3, item.contentVersion)
    }

    private fun testItem(
        interpretations: List<GarudaPuranInterpretation> = emptyList(),
    ) = GarudaPuranContentItem(
        contentId = "test-content-1",
        topicId = GarudaPuranTopicId.DHARMA_AND_CONDUCT,
        section = GarudaPuranSection("test-section", "Test section", order = 1),
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
        interpretations = interpretations,
        contentVersion = 3,
        languageCode = "en",
    )
}
