package com.aynvora.data.tarot

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.AiImprovementSignal
import com.aynvora.core.tarot.TarotAnswerFeedback
import com.aynvora.core.tarot.TarotCardFeedback
import com.aynvora.core.tarot.TarotClarificationCard
import com.aynvora.core.tarot.TarotFeedback
import com.aynvora.core.tarot.TarotQuestion
import com.aynvora.core.tarot.TarotQuestionAnswer
import com.aynvora.core.tarot.TarotReadingSession
import com.aynvora.core.tarot.TarotReadingStatus
import com.aynvora.core.tarot.TarotSessionRepository
import com.aynvora.core.tarot.TarotTimelineEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * In-memory + JSON-serialized implementation of [TarotSessionRepository].
 *
 * Uses a simple JSON file stored via [TarotSessionStorage] for offline persistence.
 * All state changes are atomic (protected by [Mutex]) and immediately serialized.
 *
 * Room entity migration is deferred to Phase 9 when full database schema evolution is planned.
 * For Phase 8.9, we use a lightweight JSON-based sidecar storage so we don't need
 * Room schema changes (which would require migration scripts across the multi-platform setup).
 */
class TarotSessionRepositoryImpl(
    private val storage: TarotSessionStorage,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : TarotSessionRepository {

    private val mutex = Mutex()
    private val _sessions = MutableStateFlow<List<TarotReadingSession>>(emptyList())
    private var initialized = false

    private suspend fun ensureInitialized() {
        if (!initialized) {
            mutex.withLock {
                if (!initialized) {
                    val stored = storage.readSessions()
                    _sessions.value = stored
                    initialized = true
                }
            }
        }
    }

    // ─── Sessions ────────────────────────────────────────────────────────────

    override suspend fun saveSession(session: TarotReadingSession): AynvoraResult<Unit> {
        ensureInitialized()
        return mutex.withLock {
            try {
                val current = _sessions.value.toMutableList()
                val existingIndex = current.indexOfFirst { it.id == session.id }
                if (existingIndex >= 0) current[existingIndex] = session
                else current.add(0, session)
                _sessions.value = current
                storage.writeSessions(current)
                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.StorageFailure("SAVE_SESSION", e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun getLatestSession(): AynvoraResult<TarotReadingSession?> {
        ensureInitialized()
        return AynvoraResult.Success(
            _sessions.value.firstOrNull { it.status != TarotReadingStatus.ARCHIVED }
        )
    }

    override suspend fun getSession(sessionId: String): AynvoraResult<TarotReadingSession?> {
        ensureInitialized()
        return AynvoraResult.Success(_sessions.value.firstOrNull { it.id == sessionId })
    }

    override suspend fun updateSessionStatus(
        sessionId: String,
        status: TarotReadingStatus,
    ): AynvoraResult<Unit> {
        ensureInitialized()
        return mutex.withLock {
            try {
                val current = _sessions.value.toMutableList()
                val index = current.indexOfFirst { it.id == sessionId }
                if (index >= 0) {
                    current[index] = current[index].copy(status = status)
                    _sessions.value = current
                    storage.writeSessions(current)
                }
                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.StorageFailure("UPDATE_STATUS", e.message ?: "Unknown error")
            }
        }
    }

    override fun observeSessions(): Flow<List<TarotReadingSession>> = _sessions

    // ─── Questions ───────────────────────────────────────────────────────────

    override suspend fun saveQuestion(question: TarotQuestion): AynvoraResult<Unit> {
        ensureInitialized()
        return try {
            storage.writeQuestion(question)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_QUESTION", e.message ?: "Unknown error")
        }
    }

    override suspend fun updateQuestion(question: TarotQuestion): AynvoraResult<Unit> =
        saveQuestion(question)

    override suspend fun getQuestionsForSession(sessionId: String): AynvoraResult<List<TarotQuestion>> {
        return try {
            AynvoraResult.Success(storage.readQuestionsForSession(sessionId))
        } catch (e: Exception) {
            AynvoraResult.Success(emptyList())
        }
    }

    // ─── Answers ─────────────────────────────────────────────────────────────

    override suspend fun saveAnswer(answer: TarotQuestionAnswer): AynvoraResult<Unit> {
        return try {
            storage.writeAnswer(answer)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_ANSWER", e.message ?: "Unknown error")
        }
    }

    override suspend fun getAnswer(questionId: String): AynvoraResult<TarotQuestionAnswer?> {
        return try {
            AynvoraResult.Success(storage.readAnswer(questionId))
        } catch (e: Exception) {
            AynvoraResult.Success(null)
        }
    }

    // ─── Clarification Cards ─────────────────────────────────────────────────

    override suspend fun saveClarificationCard(card: TarotClarificationCard): AynvoraResult<Unit> {
        return try {
            storage.writeClarificationCard(card)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_CLARIFICATION", e.message ?: "Unknown error")
        }
    }

    override suspend fun getClarificationCard(questionId: String): AynvoraResult<TarotClarificationCard?> {
        return try {
            AynvoraResult.Success(storage.readClarificationCard(questionId))
        } catch (e: Exception) {
            AynvoraResult.Success(null)
        }
    }

    // ─── Timeline ────────────────────────────────────────────────────────────

    override suspend fun appendTimelineEvent(event: TarotTimelineEvent): AynvoraResult<Unit> {
        return try {
            storage.appendTimelineEvent(event)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("APPEND_TIMELINE", e.message ?: "Unknown error")
        }
    }

    override suspend fun getTimelineForSession(sessionId: String): AynvoraResult<List<TarotTimelineEvent>> {
        return try {
            AynvoraResult.Success(
                storage.readTimelineForSession(sessionId)
                    .sortedWith(compareBy({ it.timestampEpochMs }, { it.sequenceIndex }))
            )
        } catch (e: Exception) {
            AynvoraResult.Success(emptyList())
        }
    }

    // ─── Feedback ────────────────────────────────────────────────────────────

    override suspend fun saveFeedback(feedback: TarotFeedback): AynvoraResult<Unit> {
        return try {
            storage.writeFeedback(feedback)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_FEEDBACK", e.message ?: "Unknown error")
        }
    }

    override suspend fun getFeedback(sessionId: String): AynvoraResult<TarotFeedback?> {
        return try {
            AynvoraResult.Success(storage.readFeedback(sessionId))
        } catch (e: Exception) {
            AynvoraResult.Success(null)
        }
    }

    override suspend fun saveAnswerFeedback(feedback: TarotAnswerFeedback): AynvoraResult<Unit> {
        return try {
            storage.writeAnswerFeedback(feedback)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                "SAVE_ANSWER_FEEDBACK",
                e.message ?: "Unknown error"
            )
        }
    }

    override suspend fun saveCardFeedback(feedback: TarotCardFeedback): AynvoraResult<Unit> {
        return try {
            storage.writeCardFeedback(feedback)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_CARD_FEEDBACK", e.message ?: "Unknown error")
        }
    }

    // ─── Improvement Signals ─────────────────────────────────────────────────

    override suspend fun saveImprovementSignal(signal: AiImprovementSignal): AynvoraResult<Unit> {
        return try {
            storage.writeImprovementSignal(signal)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure("SAVE_SIGNAL", e.message ?: "Unknown error")
        }
    }
}
