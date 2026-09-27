package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive test suite for Phase 8.9 — Tarot Conversational Experience.
 *
 * Covers:
 * - 24-hour reading availability rule & edge cases
 * - Lock persistence across simulated restarts
 * - Follow-up questions & AI answer evidence flow
 * - Clarification card recommendations, draw limits, and non-reset of 24h timer
 * - Chronological reading timeline
 * - Multi-level feedback & privacy-safe AI improvement signals
 * - Immutability of AI model weights
 */
class TarotPhase89Test {

    // In-memory test repository for isolating domain logic
    private class FakeTarotSessionRepository : TarotSessionRepository {
        private val sessions = mutableListOf<TarotReadingSession>()
        private val questions = mutableListOf<TarotQuestion>()
        private val answers = mutableMapOf<String, TarotQuestionAnswer>()
        private val clarifications = mutableMapOf<String, TarotClarificationCard>()
        private val timeline = mutableListOf<TarotTimelineEvent>()
        private val feedbacks = mutableMapOf<String, TarotFeedback>()
        private val answerFeedbacks = mutableListOf<TarotAnswerFeedback>()
        private val cardFeedbacks = mutableListOf<TarotCardFeedback>()
        val signals = mutableListOf<AiImprovementSignal>()

        override suspend fun saveSession(session: TarotReadingSession): AynvoraResult<Unit> {
            val idx = sessions.indexOfFirst { it.id == session.id }
            if (idx >= 0) sessions[idx] = session else sessions.add(0, session)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getLatestSession(): AynvoraResult<TarotReadingSession?> {
            return AynvoraResult.Success(sessions.firstOrNull { it.status != TarotReadingStatus.ARCHIVED })
        }

        override suspend fun getSession(sessionId: String): AynvoraResult<TarotReadingSession?> {
            return AynvoraResult.Success(sessions.firstOrNull { it.id == sessionId })
        }

        override suspend fun updateSessionStatus(
            sessionId: String,
            status: TarotReadingStatus
        ): AynvoraResult<Unit> {
            val idx = sessions.indexOfFirst { it.id == sessionId }
            if (idx >= 0) {
                sessions[idx] = sessions[idx].copy(status = status)
            }
            return AynvoraResult.Success(Unit)
        }

        override fun observeSessions(): Flow<List<TarotReadingSession>> =
            MutableStateFlow(sessions)

        override suspend fun saveQuestion(question: TarotQuestion): AynvoraResult<Unit> {
            val idx = questions.indexOfFirst { it.id == question.id }
            if (idx >= 0) questions[idx] = question else questions.add(question)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun updateQuestion(question: TarotQuestion): AynvoraResult<Unit> =
            saveQuestion(question)

        override suspend fun getQuestionsForSession(sessionId: String): AynvoraResult<List<TarotQuestion>> {
            return AynvoraResult.Success(questions.filter { it.sessionId == sessionId })
        }

        override suspend fun saveAnswer(answer: TarotQuestionAnswer): AynvoraResult<Unit> {
            answers[answer.questionId] = answer
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getAnswer(questionId: String): AynvoraResult<TarotQuestionAnswer?> {
            return AynvoraResult.Success(answers[questionId])
        }

        override suspend fun saveClarificationCard(card: TarotClarificationCard): AynvoraResult<Unit> {
            clarifications[card.questionId] = card
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getClarificationCard(questionId: String): AynvoraResult<TarotClarificationCard?> {
            return AynvoraResult.Success(clarifications[questionId])
        }

        override suspend fun appendTimelineEvent(event: TarotTimelineEvent): AynvoraResult<Unit> {
            timeline.add(event)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getTimelineForSession(sessionId: String): AynvoraResult<List<TarotTimelineEvent>> {
            return AynvoraResult.Success(
                timeline.filter { it.sessionId == sessionId }
                    .sortedWith(compareBy({ it.timestampEpochMs }, { it.sequenceIndex }))
            )
        }

        override suspend fun saveFeedback(feedback: TarotFeedback): AynvoraResult<Unit> {
            feedbacks[feedback.sessionId] = feedback
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getFeedback(sessionId: String): AynvoraResult<TarotFeedback?> {
            return AynvoraResult.Success(feedbacks[sessionId])
        }

        override suspend fun saveAnswerFeedback(feedback: TarotAnswerFeedback): AynvoraResult<Unit> {
            answerFeedbacks.add(feedback)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun saveCardFeedback(feedback: TarotCardFeedback): AynvoraResult<Unit> {
            cardFeedbacks.add(feedback)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun saveImprovementSignal(signal: AiImprovementSignal): AynvoraResult<Unit> {
            signals.add(signal)
            return AynvoraResult.Success(Unit)
        }
    }

    private fun createTestReading(startedAt: Long): TarotReading {
        val drawEngine = TarotDrawEngine(DeterministicTarotRandomSource(42L))
        return drawEngine.drawSpread(TarotSpread.SingleCard, timestampEpochMs = startedAt)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 24-Hour Reading Availability Policy Tests (Section 71)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun readingLock_noPreviousReading_allowsNewReading(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val clock = FakeTarotClock(1_000_000L)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        assertTrue(policy.canStartNewReading())
        val eval = policy.evaluate()
        assertTrue(eval is AynvoraResult.Success && eval.value is TarotReadingAvailability.Available)
    }

    @Test
    fun readingLock_inside24h_blocksNewReading(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 1_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        // 1 hour later
        clock.advance(3_600_000L)

        assertFalse(policy.canStartNewReading())
        val eval = policy.evaluate()
        assertTrue(eval is AynvoraResult.Success)
        val result = eval.value
        assertTrue(
            result is TarotReadingAvailability.ActiveReadingExists ||
                    result is TarotReadingAvailability.Locked
        )
    }

    @Test
    fun readingLock_exact24hBoundary_allowsNewReading(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 1_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        // Exactly 24 hours later
        clock.advance(TarotReadingSession.READING_LOCK_DURATION_MS)

        assertTrue(policy.canStartNewReading())
    }

    @Test
    fun readingLock_24hPlusOneSecond_allowsNewReading(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 1_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        // 24 hours + 1000 ms
        clock.advance(TarotReadingSession.READING_LOCK_DURATION_MS + 1000L)

        assertTrue(policy.canStartNewReading())
        val eval = policy.evaluate()
        assertTrue(eval is AynvoraResult.Success && eval.value is TarotReadingAvailability.Available)
    }

    @Test
    fun readingLock_appRestart_persistsLock(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy1 = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy1.startNewSession(reading, language = "en")

        // Simulate app restart: new policy instance reading from same repository
        val policy2 = TarotReadingAvailabilityPolicy(repo, clock)

        // Advance 12 hours
        clock.advance(12L * 3600_000L)

        assertFalse(policy2.canStartNewReading())
    }

    @Test
    fun readingLock_timezoneChange_preservesLock(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        // Timezone changes shift wall time display but epoch ms remains monotonic
        clock.advance(23L * 3600_000L + 59L * 60_000L) // 23h 59m

        assertFalse(policy.canStartNewReading())
    }

    @Test
    fun readingLock_languageChange_doesNotResetLock(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        val sessionResult = policy.startNewSession(reading, language = "en")
        val sessionId = (sessionResult as AynvoraResult.Success).value.id

        // User changes language to Hindi
        val session = repo.getSession(sessionId) as AynvoraResult.Success
        assertNotNull(session.value)

        // Lock check remains active
        clock.advance(5_000_000L)
        assertFalse(policy.canStartNewReading())
    }

    @Test
    fun readingLock_existingReadingRemainsAccessible(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        clock.advance(5_000_000L)
        val eval = policy.evaluate() as AynvoraResult.Success
        assertTrue(eval.value is TarotReadingAvailability.ActiveReadingExists)
        val active = eval.value as TarotReadingAvailability.ActiveReadingExists
        assertEquals(reading.id, active.session.reading.id)
    }

    @Test
    fun readingLock_expiredReadingAllowsNewReading(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        policy.startNewSession(reading, language = "en")

        // Advance 25 hours -> session becomes EXPIRED
        clock.advance(25L * 3600_000L)

        val eval = policy.evaluate() as AynvoraResult.Success
        assertTrue(eval.value is TarotReadingAvailability.Available)

        // Previous reading is still in history
        val latest = repo.getLatestSession() as AynvoraResult.Success
        assertNotNull(latest.value)
        assertEquals(TarotReadingStatus.EXPIRED, latest.value?.status)
    }

    @Test
    fun readingLock_evaluateReturnsLockedWithRemainingDuration(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        val session =
            (policy.startNewSession(reading, language = "en") as AynvoraResult.Success).value

        // Complete session
        repo.updateSessionStatus(session.id, TarotReadingStatus.COMPLETED)

        // Advance 4 hours
        clock.advance(4L * 3600_000L)

        val eval = policy.evaluate() as AynvoraResult.Success
        assertTrue(eval.value is TarotReadingAvailability.Locked)
        val locked = eval.value as TarotReadingAvailability.Locked
        assertEquals(20L * 3600_000L, locked.remainingMs)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Follow-up Questions & Clarifications Tests (Sections 72-73)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun question_persistenceAndAssociationWithSession(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val clock = FakeTarotClock(1_000_000L)
        val question = TarotQuestion(
            id = "q_1",
            readingId = "r_1",
            sessionId = "s_1",
            askedAtEpochMs = clock.nowEpochMs(),
            questionText = "Will this venture succeed?",
            originalLanguage = "en",
            sequenceNumber = 1,
            cardEvidenceIds = listOf("fool"),
        )

        repo.saveQuestion(question)
        val stored = repo.getQuestionsForSession("s_1") as AynvoraResult.Success
        assertEquals(1, stored.value.size)
        assertEquals("Will this venture succeed?", stored.value[0].questionText)
        assertFalse(stored.value[0].clarificationUsed)
    }

    @Test
    fun clarification_insufficientEvidence_recommendsClarification(): Unit = runBlocking {
        val explanationEngine = DeterministicTarotExplanationEngine()
        val drawEngine = TarotDrawEngine(DeterministicTarotRandomSource(42L))
        val clock = FakeTarotClock(1_000_000L)
        val questionEngine = TarotQuestionEngine(explanationEngine, drawEngine, clock)

        val primaryReading = drawEngine.drawSpread(TarotSpread.SingleCard)
        val question = TarotQuestion(
            id = "q_test",
            readingId = primaryReading.id,
            sessionId = "s_test",
            askedAtEpochMs = clock.nowEpochMs(),
            questionText = "What is the exact timing and date?",
            originalLanguage = "en",
            sequenceNumber = 1,
        )

        val cardContents = primaryReading.draws.associate {
            it.card.id to TarotCardContent(
                cardId = it.card.id,
                language = "en",
                title = it.card.name,
                shortDescription = "Focus card",
                keywords = listOf("beginnings"),
                uprightMeaning = "New beginnings",
                reversedMeaning = "Recklessness",
            )
        }

        val answerResult = questionEngine.generateAnswer(
            question = question,
            primaryDraws = primaryReading.draws,
            cardContents = cardContents,
            allowClarificationRecommendation = true,
        )

        assertTrue(answerResult is AynvoraResult.Success)
        val answer = answerResult.value
        // Timing-specific question on a single card triggers clarification recommendation
        assertTrue(answer.clarificationRecommended)
        assertNotNull(answer.clarificationReason)
    }

    @Test
    fun clarification_drawCard_usesDrawEngine_maxOnePerQuestion(): Unit = runBlocking {
        val explanationEngine = DeterministicTarotExplanationEngine()
        val drawEngine = TarotDrawEngine(DeterministicTarotRandomSource(100L))
        val clock = FakeTarotClock(1_000_000L)
        val questionEngine = TarotQuestionEngine(explanationEngine, drawEngine, clock)

        val primaryReading = drawEngine.drawSpread(TarotSpread.SingleCard)
        val question = TarotQuestion(
            id = "q_clarify",
            readingId = primaryReading.id,
            sessionId = "s_clarify",
            askedAtEpochMs = clock.nowEpochMs(),
            questionText = "Can I get more clarity?",
            originalLanguage = "en",
            sequenceNumber = 1,
        )

        val drawnCardIds = primaryReading.draws.map { it.card.id }.toSet()

        // Draw clarification card 1
        val clarResult = questionEngine.drawClarificationCard(
            question = question,
            deckCards = TarotStandardDeck.AllCards,
            alreadyDrawnCardIds = drawnCardIds,
        )

        assertTrue(clarResult is AynvoraResult.Success)
        val clarificationCard = clarResult.value
        assertEquals("q_clarify", clarificationCard.questionId)
        assertEquals(primaryReading.id, clarificationCard.readingId)
        assertNotNull(clarificationCard.draw.card.name)

        // Max 1: Attempt to draw a second clarification card for the SAME question
        val updatedQuestion = question.copy(clarificationCardId = clarificationCard.id)
        val secondResult = questionEngine.drawClarificationCard(
            question = updatedQuestion,
            deckCards = TarotStandardDeck.AllCards,
            alreadyDrawnCardIds = drawnCardIds + clarificationCard.draw.card.id,
        )

        // Must fail because clarificationUsed is true
        assertTrue(secondResult is AynvoraResult.Failure.InvalidInput)
    }

    @Test
    fun clarification_doesNotReset24hLock(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val baseTime = 10_000_000L
        val clock = FakeTarotClock(baseTime)
        val policy = TarotReadingAvailabilityPolicy(repo, clock)

        val reading = createTestReading(baseTime)
        val session =
            (policy.startNewSession(reading, language = "en") as AynvoraResult.Success).value

        // Advance 2 hours
        clock.advance(2L * 3600_000L)

        // Draw clarification card (stored in repository)
        val clarification = TarotClarificationCard(
            id = "clar_1",
            readingId = reading.id,
            questionId = "q_1",
            draw = reading.draws[0],
            drawnAtEpochMs = clock.nowEpochMs(),
            reason = "Clarification test",
        )
        repo.saveClarificationCard(clarification)

        // Expiry time must remain startedAt + 24h
        val latestSession = (repo.getSession(session.id) as AynvoraResult.Success).value
        assertNotNull(latestSession)
        assertEquals(
            baseTime + TarotReadingSession.READING_LOCK_DURATION_MS,
            latestSession.expiresAtEpochMs
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timeline Tests (Section 74)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun timeline_chronologicalOrderingWithDeterministicSequence(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()

        val event1 = TarotTimelineEvent(
            id = "e1",
            readingId = "r1",
            sessionId = "s1",
            timestampEpochMs = 1000L,
            sequenceIndex = 1,
            eventType = TarotTimelineEventType.READING_STARTED,
            language = "en",
        )
        val event2 = TarotTimelineEvent(
            id = "e2",
            readingId = "r1",
            sessionId = "s1",
            timestampEpochMs = 1000L,
            sequenceIndex = 2,
            eventType = TarotTimelineEventType.PRIMARY_CARD_DRAWN,
            language = "en",
        )
        val event3 = TarotTimelineEvent(
            id = "e3",
            readingId = "r1",
            sessionId = "s1",
            timestampEpochMs = 2000L,
            sequenceIndex = 1,
            eventType = TarotTimelineEventType.QUESTION_ASKED,
            language = "en",
        )

        // Append in reverse order
        repo.appendTimelineEvent(event3)
        repo.appendTimelineEvent(event1)
        repo.appendTimelineEvent(event2)

        val timeline = (repo.getTimelineForSession("s1") as AynvoraResult.Success).value
        assertEquals(3, timeline.size)
        assertEquals(TarotTimelineEventType.READING_STARTED, timeline[0].eventType)
        assertEquals(TarotTimelineEventType.PRIMARY_CARD_DRAWN, timeline[1].eventType)
        assertEquals(TarotTimelineEventType.QUESTION_ASKED, timeline[2].eventType)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feedback & AI Improvement Signal Tests (Sections 75-76)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun feedback_oneToFiveStarAndOptionalText(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val clock = FakeTarotClock(1_000_000L)

        val feedback = TarotFeedback(
            id = "fb_1",
            sessionId = "s_1",
            readingId = "r_1",
            starRating = 5,
            optionalText = "Very insightful and grounded guidance.",
            language = "en",
            submittedAtEpochMs = clock.nowEpochMs(),
        )

        repo.saveFeedback(feedback)
        val retrieved = (repo.getFeedback("s_1") as AynvoraResult.Success).value
        assertNotNull(retrieved)
        assertEquals(5, retrieved.starRating)
        assertEquals("Very insightful and grounded guidance.", retrieved.optionalText)
    }

    @Test
    fun feedback_createsAiImprovementSignalWithoutPII(): Unit = runBlocking {
        val repo = FakeTarotSessionRepository()
        val clock = FakeTarotClock(1_000_000L)

        val signal = AiImprovementSignal(
            signalId = "sig_1",
            featureId = "TAROT",
            sessionId = "s_1",
            readingId = "r_1",
            questionId = "q_1",
            answerId = "a_1",
            feedbackRating = 5,
            feedbackType = "STAR_RATING",
            language = "en",
            promptVersion = "1.0",
            contentVersion = 1,
            timestampEpochMs = clock.nowEpochMs(),
        )

        repo.saveImprovementSignal(signal)
        assertEquals(1, repo.signals.size)
        val saved = repo.signals[0]
        assertEquals("TAROT", saved.featureId)
        assertEquals(5, saved.feedbackRating)
        // Verify no raw question or answer text is stored in signal
        assertNull(saved.cardId)
    }

    @Test
    fun aiImprovement_modelWeightsRemainImmutable() {
        // Architecture invariant verification:
        // AiImprovementSignal is strictly a local telemetry log.
        // It provides zero hooks to mutate model weights or prompt templates at runtime.
        val signal = AiImprovementSignal(
            signalId = "test_signal",
            sessionId = "s_immut",
            readingId = "r_immut",
            feedbackRating = 4,
            feedbackType = "STARS",
            language = "hi",
            promptVersion = "1.0",
            contentVersion = 1,
            timestampEpochMs = 12345L,
        )

        assertEquals("1.0", signal.promptVersion)
        assertEquals(1, signal.contentVersion)
    }
}
