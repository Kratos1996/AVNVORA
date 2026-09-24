package com.aynvora.core.tarot

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TarotDomainTest {

    @Test
    fun standardDeck_contains78Cards() {
        assertEquals(78, TarotStandardDeck.AllCards.size)
        assertEquals(22, TarotStandardDeck.MajorArcana.size)
        assertEquals(14, TarotStandardDeck.Wands.size)
        assertEquals(14, TarotStandardDeck.Cups.size)
        assertEquals(14, TarotStandardDeck.Swords.size)
        assertEquals(14, TarotStandardDeck.Pentacles.size)

        // Unique card IDs
        val uniqueIds = TarotStandardDeck.AllCards.map { it.id }.toSet()
        assertEquals(78, uniqueIds.size)
    }

    @Test
    fun drawEngine_singleCard_drawsExactlyOneCard() {
        val engine = TarotDrawEngine(DeterministicTarotRandomSource(seed = 42L))
        val reading = engine.drawSpread(TarotSpread.SingleCard)

        assertEquals(1, reading.draws.size)
        assertEquals("single_focus", reading.draws[0].position.id)
        assertFalse(reading.draws[0].card.name.isBlank())
    }

    @Test
    fun drawEngine_threeCard_drawsThreeDistinctCards() {
        val engine = TarotDrawEngine(DeterministicTarotRandomSource(seed = 12345L))
        val reading = engine.drawSpread(TarotSpread.ThreeCardTimeline)

        assertEquals(3, reading.draws.size)
        val drawnCardIds = reading.draws.map { it.card.id }
        assertEquals(
            3,
            drawnCardIds.distinct().size,
            "Drawn cards must never contain duplicates in a standard spread"
        )

        assertEquals("timeline_past", reading.draws[0].position.id)
        assertEquals("timeline_present", reading.draws[1].position.id)
        assertEquals("timeline_future", reading.draws[2].position.id)
    }

    @Test
    fun drawEngine_deterministicSource_producesIdenticalDraw() {
        val engine1 = TarotDrawEngine(DeterministicTarotRandomSource(seed = 999L))
        val engine2 = TarotDrawEngine(DeterministicTarotRandomSource(seed = 999L))

        val reading1 = engine1.drawSpread(TarotSpread.ThreeCardTimeline)
        val reading2 = engine2.drawSpread(TarotSpread.ThreeCardTimeline)

        for (i in 0 until 3) {
            assertEquals(reading1.draws[i].card.id, reading2.draws[i].card.id)
            assertEquals(reading1.draws[i].orientation, reading2.draws[i].orientation)
        }
    }

    @Test
    fun drawEngine_orientation_respectsReversedFlag() {
        val engine = TarotDrawEngine(DeterministicTarotRandomSource(seed = 42L))
        val reading = engine.drawSpread(
            spread = TarotSpread.SingleCard,
            allowReversed = false,
        )

        assertEquals(TarotCardOrientation.UPRIGHT, reading.draws[0].orientation)
    }

    @Test
    fun useCase_tracksAnalyticsWithoutPII() = runBlockingTest {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        val testTracker = object : AnalyticsTracker {
            override fun track(event: AnalyticsEvent) {
                trackedEvents.add(event)
            }
        }

        val fakeRepo = FakeTarotRepository()
        val useCase = PerformTarotReadingUseCase(
            tarotRepository = fakeRepo,
            drawEngine = TarotDrawEngine(DeterministicTarotRandomSource(seed = 1L)),
            analyticsTracker = testTracker,
        )

        val result = useCase.execute(TarotSpread.SingleCard)
        assertTrue(result is AynvoraResult.Success)

        // Verify analytics events
        assertTrue(trackedEvents.any { it.name == "tarot_reading_started" })
        assertTrue(trackedEvents.any { it.name == "tarot_card_drawn" })
        assertTrue(trackedEvents.any { it.name == "tarot_reading_completed" })

        // Verify no PII in parameters
        trackedEvents.forEach { event ->
            event.params.forEach { (key, value) ->
                assertNotEquals("name", key)
                assertNotEquals("question", key)
                assertNotEquals("journal", key)
                assertNotEquals("birth_date", key)
                assertNotEquals("birth_time", key)
            }
        }
    }

    @Test
    fun tarotDisclaimer_containsEthicalNonPredictiveWording() {
        assertTrue(TarotDisclaimer.ENGLISH_TEXT.contains("relaxation"))
        assertTrue(TarotDisclaimer.ENGLISH_TEXT.contains("not constitute scientific prediction"))
        assertTrue(TarotDisclaimer.HINDI_TEXT.contains("आत्म-चिंतन"))
    }
}

private class FakeTarotRepository : TarotRepository {
    val savedReadings = mutableListOf<TarotReading>()

    override suspend fun getDeckCards(deckId: String): AynvoraResult<List<TarotCard>> =
        AynvoraResult.Success(TarotStandardDeck.AllCards)

    override suspend fun getCardContent(
        cardId: String,
        language: String
    ): AynvoraResult<TarotCardContent> =
        AynvoraResult.Success(
            TarotCardContent(
                cardId = cardId,
                language = language,
                title = "Title for $cardId",
                shortDescription = "Description",
                keywords = listOf("Test"),
                uprightMeaning = "Upright",
                reversedMeaning = "Reversed",
            ),
        )

    override fun observeAllCardContent(language: String): Flow<AynvoraResult<List<TarotCardContent>>> =
        flowOf(AynvoraResult.Success(emptyList()))

    override suspend fun getSupportedSpreads(): AynvoraResult<List<TarotSpread>> =
        AynvoraResult.Success(TarotSpread.StandardSpreads)

    override suspend fun saveReading(reading: TarotReading): AynvoraResult<Unit> {
        savedReadings.add(reading)
        return AynvoraResult.Success(Unit)
    }

    override fun observeRecentReadings(limit: Int): Flow<List<TarotReading>> =
        flowOf(savedReadings)

    override suspend fun deleteReading(id: String): AynvoraResult<Unit> {
        savedReadings.removeAll { it.id == id }
        return AynvoraResult.Success(Unit)
    }

    override suspend fun clearReadingHistory(): AynvoraResult<Unit> {
        savedReadings.clear()
        return AynvoraResult.Success(Unit)
    }

    override suspend fun updateContentPack(
        language: String,
        newVersion: Int,
        contentList: List<TarotCardContent>,
    ): AynvoraResult<Unit> {
        return AynvoraResult.Success(Unit)
    }
}

private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
