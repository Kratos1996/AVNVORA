package com.aynvora.data.tarot

import com.aynvora.core.tarot.AiImprovementSignal
import com.aynvora.core.tarot.TarotAnswerFeedback
import com.aynvora.core.tarot.TarotCardFeedback
import com.aynvora.core.tarot.TarotClarificationCard
import com.aynvora.core.tarot.TarotFeedback
import com.aynvora.core.tarot.TarotQuestion
import com.aynvora.core.tarot.TarotQuestionAnswer
import com.aynvora.core.tarot.TarotReadingSession
import com.aynvora.core.tarot.TarotTimelineEvent
import com.aynvora.data.storage.StorageDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Storage contract for Tarot reading sessions and related conversational data.
 *
 * Backs [TarotSessionRepositoryImpl] using persistent key-value storage via [StorageDriver].
 */
interface TarotSessionStorage {
    suspend fun readSessions(): List<TarotReadingSession>
    suspend fun writeSessions(sessions: List<TarotReadingSession>)

    suspend fun writeQuestion(question: TarotQuestion)
    suspend fun readQuestionsForSession(sessionId: String): List<TarotQuestion>

    suspend fun writeAnswer(answer: TarotQuestionAnswer)
    suspend fun readAnswer(questionId: String): TarotQuestionAnswer?

    suspend fun writeClarificationCard(card: TarotClarificationCard)
    suspend fun readClarificationCard(questionId: String): TarotClarificationCard?

    suspend fun appendTimelineEvent(event: TarotTimelineEvent)
    suspend fun readTimelineForSession(sessionId: String): List<TarotTimelineEvent>

    suspend fun writeFeedback(feedback: TarotFeedback)
    suspend fun readFeedback(sessionId: String): TarotFeedback?

    suspend fun writeAnswerFeedback(feedback: TarotAnswerFeedback)
    suspend fun writeCardFeedback(feedback: TarotCardFeedback)

    suspend fun writeImprovementSignal(signal: AiImprovementSignal)
}

/**
 * [StorageDriver]-backed implementation of [TarotSessionStorage].
 */
class DriverTarotSessionStorage(
    private val driver: StorageDriver,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : TarotSessionStorage {

    private val mutex = Mutex()

    companion object {
        private const val SESSIONS_KEY = "tarot_sessions_v1"
        private const val QUESTIONS_PREFIX = "tarot_questions_v1_"
        private const val ANSWERS_PREFIX = "tarot_answers_v1_"
        private const val CLARIFICATIONS_PREFIX = "tarot_clarifications_v1_"
        private const val TIMELINE_PREFIX = "tarot_timeline_v1_"
        private const val FEEDBACK_PREFIX = "tarot_feedback_v1_"
        private const val ANSWER_FEEDBACK_KEY = "tarot_answer_feedbacks_v1"
        private const val CARD_FEEDBACK_KEY = "tarot_card_feedbacks_v1"
        private const val SIGNALS_KEY = "tarot_improvement_signals_v1"
    }

    override suspend fun readSessions(): List<TarotReadingSession> = mutex.withLock {
        val raw = driver.read(SESSIONS_KEY) ?: return emptyList()
        try {
            json.decodeFromString<List<TarotReadingSession>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun writeSessions(sessions: List<TarotReadingSession>) = mutex.withLock {
        val encoded = json.encodeToString(sessions)
        driver.write(SESSIONS_KEY, encoded)
    }

    override suspend fun writeQuestion(question: TarotQuestion) = mutex.withLock {
        val key = "$QUESTIONS_PREFIX${question.sessionId}"
        val current = try {
            val raw = driver.read(key)
            if (raw != null) json.decodeFromString<List<TarotQuestion>>(raw)
                .toMutableList() else mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        val idx = current.indexOfFirst { it.id == question.id }
        if (idx >= 0) current[idx] = question else current.add(question)
        driver.write(key, json.encodeToString(current))
    }

    override suspend fun readQuestionsForSession(sessionId: String): List<TarotQuestion> =
        mutex.withLock {
            val key = "$QUESTIONS_PREFIX$sessionId"
            val raw = driver.read(key) ?: return emptyList()
            try {
                json.decodeFromString<List<TarotQuestion>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        }

    override suspend fun writeAnswer(answer: TarotQuestionAnswer) = mutex.withLock {
        val key = "$ANSWERS_PREFIX${answer.questionId}"
        driver.write(key, json.encodeToString(answer))
    }

    override suspend fun readAnswer(questionId: String): TarotQuestionAnswer? = mutex.withLock {
        val key = "$ANSWERS_PREFIX$questionId"
        val raw = driver.read(key) ?: return null
        try {
            json.decodeFromString<TarotQuestionAnswer>(raw)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun writeClarificationCard(card: TarotClarificationCard) = mutex.withLock {
        val key = "$CLARIFICATIONS_PREFIX${card.questionId}"
        driver.write(key, json.encodeToString(card))
    }

    override suspend fun readClarificationCard(questionId: String): TarotClarificationCard? =
        mutex.withLock {
            val key = "$CLARIFICATIONS_PREFIX$questionId"
            val raw = driver.read(key) ?: return null
            try {
                json.decodeFromString<TarotClarificationCard>(raw)
            } catch (_: Exception) {
                null
            }
        }

    override suspend fun appendTimelineEvent(event: TarotTimelineEvent) = mutex.withLock {
        val key = "$TIMELINE_PREFIX${event.sessionId}"
        val current = try {
            val raw = driver.read(key)
            if (raw != null) json.decodeFromString<List<TarotTimelineEvent>>(raw)
                .toMutableList() else mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        current.add(event)
        driver.write(key, json.encodeToString(current))
    }

    override suspend fun readTimelineForSession(sessionId: String): List<TarotTimelineEvent> =
        mutex.withLock {
            val key = "$TIMELINE_PREFIX$sessionId"
            val raw = driver.read(key) ?: return emptyList()
            try {
                json.decodeFromString<List<TarotTimelineEvent>>(raw)
            } catch (_: Exception) {
                emptyList()
            }
        }

    override suspend fun writeFeedback(feedback: TarotFeedback) = mutex.withLock {
        val key = "$FEEDBACK_PREFIX${feedback.sessionId}"
        driver.write(key, json.encodeToString(feedback))
    }

    override suspend fun readFeedback(sessionId: String): TarotFeedback? = mutex.withLock {
        val key = "$FEEDBACK_PREFIX$sessionId"
        val raw = driver.read(key) ?: return null
        try {
            json.decodeFromString<TarotFeedback>(raw)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun writeAnswerFeedback(feedback: TarotAnswerFeedback) = mutex.withLock {
        val current = try {
            val raw = driver.read(ANSWER_FEEDBACK_KEY)
            if (raw != null) json.decodeFromString<List<TarotAnswerFeedback>>(raw)
                .toMutableList() else mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        current.add(feedback)
        driver.write(ANSWER_FEEDBACK_KEY, json.encodeToString(current))
    }

    override suspend fun writeCardFeedback(feedback: TarotCardFeedback) = mutex.withLock {
        val current = try {
            val raw = driver.read(CARD_FEEDBACK_KEY)
            if (raw != null) json.decodeFromString<List<TarotCardFeedback>>(raw)
                .toMutableList() else mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        current.add(feedback)
        driver.write(CARD_FEEDBACK_KEY, json.encodeToString(current))
    }

    override suspend fun writeImprovementSignal(signal: AiImprovementSignal) = mutex.withLock {
        val current = try {
            val raw = driver.read(SIGNALS_KEY)
            if (raw != null) json.decodeFromString<List<AiImprovementSignal>>(raw)
                .toMutableList() else mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        current.add(signal)
        driver.write(SIGNALS_KEY, json.encodeToString(current))
    }
}
