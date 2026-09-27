package com.aynvora.ui.tarot

import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.AiImprovementSignal
import com.aynvora.core.tarot.DeterministicTarotExplanationEngine
import com.aynvora.core.tarot.PerformTarotReadingUseCase
import com.aynvora.core.tarot.SystemTarotClock
import com.aynvora.core.tarot.TarotAnswerFeedback
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardFeedback
import com.aynvora.core.tarot.TarotClarificationCard
import com.aynvora.core.tarot.TarotDrawEngine
import com.aynvora.core.tarot.TarotFeedback
import com.aynvora.core.tarot.TarotQuestion
import com.aynvora.core.tarot.TarotQuestionAnswer
import com.aynvora.core.tarot.TarotQuestionEngine
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotReadingAvailabilityPolicy
import com.aynvora.core.tarot.TarotReadingSession
import com.aynvora.core.tarot.TarotReadingStatus
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.core.tarot.TarotSessionRepository
import com.aynvora.core.tarot.TarotSpread
import com.aynvora.core.tarot.TarotStandardDeck
import com.aynvora.core.tarot.TarotTimelineEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class TarotViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

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
                    title = "The Magician",
                    shortDescription = "Action and power",
                    keywords = listOf("power", "will"),
                    uprightMeaning = "Manifestation, resourcefulness, power",
                    reversedMeaning = "Illusion, out of touch",
                )
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
        ): AynvoraResult<Unit> = AynvoraResult.Success(Unit)
    }

    private class FakeTarotSessionRepository : TarotSessionRepository {
        val sessions = mutableListOf<TarotReadingSession>()
        override suspend fun saveSession(session: TarotReadingSession): AynvoraResult<Unit> {
            sessions.add(session)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getLatestSession(): AynvoraResult<TarotReadingSession?> =
            AynvoraResult.Success(sessions.firstOrNull())

        override suspend fun getSession(sessionId: String): AynvoraResult<TarotReadingSession?> =
            AynvoraResult.Success(sessions.firstOrNull { it.id == sessionId })

        override suspend fun updateSessionStatus(
            sessionId: String,
            status: TarotReadingStatus
        ): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override fun observeSessions(): Flow<List<TarotReadingSession>> = MutableStateFlow(sessions)
        override suspend fun saveQuestion(question: TarotQuestion): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun updateQuestion(question: TarotQuestion): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun getQuestionsForSession(sessionId: String): AynvoraResult<List<TarotQuestion>> =
            AynvoraResult.Success(emptyList())

        override suspend fun saveAnswer(answer: TarotQuestionAnswer): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun getAnswer(questionId: String): AynvoraResult<TarotQuestionAnswer?> =
            AynvoraResult.Success(null)

        override suspend fun saveClarificationCard(card: TarotClarificationCard): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun getClarificationCard(questionId: String): AynvoraResult<TarotClarificationCard?> =
            AynvoraResult.Success(null)

        override suspend fun appendTimelineEvent(event: TarotTimelineEvent): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun getTimelineForSession(sessionId: String): AynvoraResult<List<TarotTimelineEvent>> =
            AynvoraResult.Success(emptyList())

        override suspend fun saveFeedback(feedback: TarotFeedback): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun getFeedback(sessionId: String): AynvoraResult<TarotFeedback?> =
            AynvoraResult.Success(null)

        override suspend fun saveAnswerFeedback(feedback: TarotAnswerFeedback): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun saveCardFeedback(feedback: TarotCardFeedback): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun saveImprovementSignal(signal: AiImprovementSignal): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)
    }

    private fun createViewModel(
        sessionRepo: FakeTarotSessionRepository = FakeTarotSessionRepository(),
    ): TarotViewModel {
        val clock = SystemTarotClock()
        val availabilityPolicy = TarotReadingAvailabilityPolicy(sessionRepo, clock)
        val readingUseCase = PerformTarotReadingUseCase(
            tarotRepository = FakeTarotRepository(),
            drawEngine = TarotDrawEngine(),
            analyticsTracker = NoOpAnalyticsTracker(),
        )
        val questionEngine = TarotQuestionEngine(
            explanationEngine = DeterministicTarotExplanationEngine(),
            drawEngine = TarotDrawEngine(),
            clock = clock,
        )
        return TarotViewModel(
            sessionRepository = sessionRepo,
            availabilityPolicy = availabilityPolicy,
            readingUseCase = readingUseCase,
            questionEngine = questionEngine,
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialTarotState() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(TarotDestination.Disclaimer, state.destination)
        assertEquals("rider_waite_smith_standard", state.selectedDeckId)
        assertNull(state.activeSession)
    }

    @Test
    fun testDisclaimerAcceptedNavigatesHome() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(TarotUiEvent.DisclaimerAccepted)
        advanceUntilIdle()

        assertEquals(TarotDestination.Home, vm.uiState.value.destination)
    }

    @Test
    fun testDeckSelection() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(TarotUiEvent.DeckSelected("custom_marseille_deck"))
        advanceUntilIdle()

        assertEquals("custom_marseille_deck", vm.uiState.value.selectedDeckId)
    }

    @Test
    fun testHistoryOpened() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(TarotUiEvent.HistoryOpened)
        advanceUntilIdle()

        assertEquals(TarotDestination.History, vm.uiState.value.destination)
    }

    @Test
    fun testStressRapidTarotEventsNoCrash() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val events: List<TarotUiEvent> = listOf(
            TarotUiEvent.ScreenOpened,
            TarotUiEvent.DisclaimerAccepted,
            TarotUiEvent.DeckSelected("rider_waite_smith_standard"),
            TarotUiEvent.HistoryOpened,
            TarotUiEvent.BackClicked,
            TarotUiEvent.CloseClicked,
            TarotUiEvent.FeedbackSubmitted(starRating = 5, optionalText = "Insightful"),
        )

        for (i in 0 until 50) {
            vm.onEvent(events[i % events.size])
        }
        advanceUntilIdle()

        assertNotNull(vm.uiState.value)
    }
}
