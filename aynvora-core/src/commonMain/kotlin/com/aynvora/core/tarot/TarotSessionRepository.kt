package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for Tarot reading session persistence.
 *
 * Manages the complete lifecycle of a session:
 * - Reading session (24h lock)
 * - Questions + answers
 * - Clarification cards
 * - Timeline events
 * - Feedback + improvement signals
 *
 * Storage is entirely local. No cloud sync in Phase 8.9.
 */
interface TarotSessionRepository {

    // ─── Sessions ────────────────────────────────────────────────────────────

    /** Saves a new or updated reading session. */
    suspend fun saveSession(session: TarotReadingSession): AynvoraResult<Unit>

    /** Returns the most recent non-expired session if any. */
    suspend fun getLatestSession(): AynvoraResult<TarotReadingSession?>

    /** Returns a session by stable [sessionId]. */
    suspend fun getSession(sessionId: String): AynvoraResult<TarotReadingSession?>

    /** Updates only the status field of an existing session. */
    suspend fun updateSessionStatus(
        sessionId: String,
        status: TarotReadingStatus
    ): AynvoraResult<Unit>

    /** Observes all sessions ordered newest-first. */
    fun observeSessions(): Flow<List<TarotReadingSession>>

    // ─── Questions ───────────────────────────────────────────────────────────

    /** Saves a question. Idempotent by [TarotQuestion.id]. */
    suspend fun saveQuestion(question: TarotQuestion): AynvoraResult<Unit>

    /** Updates an existing question (e.g. after clarification or answer). */
    suspend fun updateQuestion(question: TarotQuestion): AynvoraResult<Unit>

    /** Returns all questions for a session ordered by [TarotQuestion.sequenceNumber]. */
    suspend fun getQuestionsForSession(sessionId: String): AynvoraResult<List<TarotQuestion>>

    // ─── Answers ─────────────────────────────────────────────────────────────

    /** Saves a generated answer. Idempotent by [TarotQuestionAnswer.id]. */
    suspend fun saveAnswer(answer: TarotQuestionAnswer): AynvoraResult<Unit>

    /** Returns the answer for a specific [questionId]. */
    suspend fun getAnswer(questionId: String): AynvoraResult<TarotQuestionAnswer?>

    // ─── Clarification Cards ─────────────────────────────────────────────────

    /** Saves a clarification card. At most ONE per question (enforced by domain). */
    suspend fun saveClarificationCard(card: TarotClarificationCard): AynvoraResult<Unit>

    /** Returns the clarification card for a specific [questionId] if any. */
    suspend fun getClarificationCard(questionId: String): AynvoraResult<TarotClarificationCard?>

    // ─── Timeline ────────────────────────────────────────────────────────────

    /** Appends a single timeline event. Events are never deleted individually. */
    suspend fun appendTimelineEvent(event: TarotTimelineEvent): AynvoraResult<Unit>

    /** Returns all timeline events for a session ordered by timestamp + sequenceIndex. */
    suspend fun getTimelineForSession(sessionId: String): AynvoraResult<List<TarotTimelineEvent>>

    // ─── Feedback ────────────────────────────────────────────────────────────

    /** Saves reading-level feedback. Idempotent by [TarotFeedback.id]. */
    suspend fun saveFeedback(feedback: TarotFeedback): AynvoraResult<Unit>

    /** Returns the reading-level feedback for a session. */
    suspend fun getFeedback(sessionId: String): AynvoraResult<TarotFeedback?>

    /** Saves answer-level feedback. */
    suspend fun saveAnswerFeedback(feedback: TarotAnswerFeedback): AynvoraResult<Unit>

    /** Saves card-level swipe feedback. */
    suspend fun saveCardFeedback(feedback: TarotCardFeedback): AynvoraResult<Unit>

    // ─── AI Improvement Signals ──────────────────────────────────────────────

    /** Stores an improvement signal locally. Never modifies model weights. */
    suspend fun saveImprovementSignal(signal: AiImprovementSignal): AynvoraResult<Unit>
}
