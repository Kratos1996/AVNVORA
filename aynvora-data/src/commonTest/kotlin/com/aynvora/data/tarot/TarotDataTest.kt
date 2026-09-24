package com.aynvora.data.tarot

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpread
import com.aynvora.core.tarot.TarotStandardDeck
import com.aynvora.data.database.dao.TarotDao
import com.aynvora.data.database.entity.TarotCardContentRoomEntity
import com.aynvora.data.database.entity.TarotCardRoomEntity
import com.aynvora.data.database.entity.TarotDeckRoomEntity
import com.aynvora.data.database.entity.TarotReadingHistoryRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TarotDataTest {

    @Test
    fun tarotRepository_bootstrapsDefaultDeckWhenEmpty() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        val cardsResult = repo.getDeckCards()
        assertTrue(cardsResult is AynvoraResult.Success)
        assertEquals(78, cardsResult.value.size)

        // Verify seeded in DAO
        assertEquals(78, fakeDao.cards.size)
        assertEquals(1, fakeDao.decks.size)
    }

    @Test
    fun tarotRepository_getCardContent_returnsEnglishAndHindi() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        val enContent = repo.getCardContent("major_00_fool", "en")
        assertTrue(enContent is AynvoraResult.Success)
        assertEquals("The Fool", enContent.value.title)
        assertFalse(enContent.value.uprightMeaning.isBlank())

        val hiContent = repo.getCardContent("major_00_fool", "hi")
        assertTrue(hiContent is AynvoraResult.Success)
        assertTrue(hiContent.value.title.contains("द फूल"))
        assertFalse(hiContent.value.uprightMeaning.isBlank())
    }

    @Test
    fun tarotRepository_saveReading_persistsLocallyWithoutPII() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        val reading = TarotReading(
            id = "reading_test_001",
            spreadId = TarotSpread.SingleCard.id,
            deckId = TarotStandardDeck.Deck.id,
            draws = listOf(
                TarotCardDraw(
                    card = TarotStandardDeck.MajorArcana[0],
                    orientation = TarotCardOrientation.UPRIGHT,
                    position = TarotSpread.SingleCard.positions[0],
                ),
            ),
            timestampEpochMs = 1_000_000L,
        )

        val saveResult = repo.saveReading(reading)
        assertTrue(saveResult is AynvoraResult.Success)
        assertEquals(1, fakeDao.readings.size)

        val stored = fakeDao.readings[0]
        assertEquals("reading_test_001", stored.id)
        assertTrue(stored.serializedDrawsJson.contains("major_00_fool"))
        assertFalse(stored.serializedDrawsJson.contains("user_name"))
        assertFalse(stored.serializedDrawsJson.contains("question"))
    }

    @Test
    fun tarotCompleteContentPack_verifiesAll78CardsInEnglishAndHindi() {
        assertEquals(78, TarotCompleteContentPack.EnglishCards.size)
        assertEquals(78, TarotCompleteContentPack.HindiCards.size)

        // Ensure 1:1 mapping with TarotStandardDeck
        val standardCardIds = TarotStandardDeck.AllCards.map { it.id }.toSet()
        val enIds = TarotCompleteContentPack.EnglishCards.map { it.cardId }.toSet()
        val hiIds = TarotCompleteContentPack.HindiCards.map { it.cardId }.toSet()

        assertEquals(standardCardIds, enIds)
        assertEquals(standardCardIds, hiIds)

        // Verify quality for every single card
        TarotCompleteContentPack.AllCards.forEach { content ->
            assertFalse(content.title.isBlank(), "Card ${content.cardId} must have a title")
            assertFalse(
                content.shortDescription.isBlank(),
                "Card ${content.cardId} must have a short description"
            )
            assertFalse(
                content.uprightMeaning.isBlank(),
                "Card ${content.cardId} must have upright meaning"
            )
            assertFalse(
                content.reversedMeaning.isBlank(),
                "Card ${content.cardId} must have reversed meaning"
            )
            assertTrue(content.keywords.isNotEmpty(), "Card ${content.cardId} must have keywords")
            assertEquals(1, content.contentVersion)
            assertEquals(
                "AYNVORA Contemplative Traditions Archive (Public Domain)",
                content.sourceAttribution
            )
        }
    }

    @Test
    fun tarotRepository_idempotentSeeding_preservesExistingData() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        // First call seeds
        repo.getDeckCards()
        val cardsCount1 = fakeDao.cards.size
        val contentCount1 = fakeDao.contents.size
        assertEquals(78, cardsCount1)
        assertEquals(156, contentCount1) // 78 en + 78 hi

        // Second call does not duplicate
        repo.getDeckCards()
        assertEquals(cardsCount1, fakeDao.cards.size)
        assertEquals(contentCount1, fakeDao.contents.size)
    }

    @Test
    fun tarotRepository_readingHistory_supportsDeletionAndClearing() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        val reading = TarotReading(
            id = "reading_001",
            spreadId = TarotSpread.SingleCard.id,
            deckId = TarotStandardDeck.Deck.id,
            draws = emptyList(),
            timestampEpochMs = 12345L,
        )
        repo.saveReading(reading)
        assertEquals(1, fakeDao.readings.size)

        val deleteResult = repo.deleteReading("reading_001")
        assertTrue(deleteResult is AynvoraResult.Success)
        assertEquals(0, fakeDao.readings.size)

        repo.saveReading(reading)
        repo.clearReadingHistory()
        assertEquals(0, fakeDao.readings.size)
    }

    @Test
    fun tarotRepository_syncUpdate_acceptsNewerVersionAndRejectsStale() = runBlocking {
        val fakeDao = FakeTarotDao()
        val repo = TarotRepositoryImpl(fakeDao)

        // Seed initial v1
        repo.getDeckCards()

        // Incoming v1 is stale (not newer than installed v1)
        val staleResult = repo.updateContentPack("en", 1, TarotCompleteContentPack.EnglishCards)
        assertTrue(staleResult is AynvoraResult.Failure.SyncFailure)
        assertEquals("VERSION_STALE", (staleResult as AynvoraResult.Failure.SyncFailure).code)

        // Incoming v2 is valid
        val validUpdateResult =
            repo.updateContentPack("en", 2, TarotCompleteContentPack.EnglishCards)
        assertTrue(validUpdateResult is AynvoraResult.Success)

        // Verify version updated in DAO
        val updatedCard = fakeDao.getContentByCardAndLanguage("major_00_fool", "en")
        assertEquals(2, updatedCard?.contentVersion)
    }
}

private class FakeTarotDao : TarotDao {
    val decks = mutableListOf<TarotDeckRoomEntity>()
    val cards = mutableListOf<TarotCardRoomEntity>()
    val contents = mutableListOf<TarotCardContentRoomEntity>()
    val readings = mutableListOf<TarotReadingHistoryRoomEntity>()

    override suspend fun getDeckById(id: String): TarotDeckRoomEntity? =
        decks.firstOrNull { it.id == id }

    override suspend fun getAllDecks(): List<TarotDeckRoomEntity> = decks

    override suspend fun upsertDeck(entity: TarotDeckRoomEntity) {
        decks.removeAll { it.id == entity.id }
        decks.add(entity)
    }

    override suspend fun getCardsForDeck(deckId: String): List<TarotCardRoomEntity> =
        cards.filter { it.deckId == deckId }

    override suspend fun getCardById(id: String): TarotCardRoomEntity? =
        cards.firstOrNull { it.id == id }

    override suspend fun upsertCards(entities: List<TarotCardRoomEntity>) {
        val ids = entities.map { it.id }.toSet()
        cards.removeAll { it.id in ids }
        cards.addAll(entities)
    }

    override suspend fun getContentByCardAndLanguage(
        cardId: String,
        language: String
    ): TarotCardContentRoomEntity? =
        contents.firstOrNull { it.cardId == cardId && it.language == language }

    override fun observeContentByLanguage(language: String): Flow<List<TarotCardContentRoomEntity>> =
        flowOf(contents.filter { it.language == language })

    override suspend fun upsertCardContentList(entities: List<TarotCardContentRoomEntity>) {
        val ids = entities.map { it.id }.toSet()
        contents.removeAll { it.id in ids }
        contents.addAll(entities)
    }

    override suspend fun getCardCount(deckId: String): Int =
        cards.count { it.deckId == deckId }

    override suspend fun getContentCount(language: String): Int =
        contents.count { it.language == language }

    override suspend fun getAllContentForLanguage(language: String): List<TarotCardContentRoomEntity> =
        contents.filter { it.language == language }

    override suspend fun insertReading(entity: TarotReadingHistoryRoomEntity) {
        readings.removeAll { it.id == entity.id }
        readings.add(entity)
    }

    override fun observeRecentReadings(limit: Int): Flow<List<TarotReadingHistoryRoomEntity>> =
        flowOf(readings.take(limit))

    override suspend fun deleteReadingById(id: String) {
        readings.removeAll { it.id == id }
    }

    override suspend fun clearHistory() {
        readings.clear()
    }
}
