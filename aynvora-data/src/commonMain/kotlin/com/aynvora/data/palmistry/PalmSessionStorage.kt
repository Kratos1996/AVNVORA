package com.aynvora.data.palmistry

import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.tarot.AiImprovementSignal
import com.aynvora.data.storage.StorageDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Storage contract for Palmistry reading sessions and related conversational data.
 */
interface PalmSessionStorage {
    suspend fun readSessions(): List<PalmReadingSession>
    suspend fun writeSessions(sessions: List<PalmReadingSession>)
    suspend fun writeFeatureFeedback(feedback: PalmFeatureFeedback)
    suspend fun writeAnswerFeedback(feedback: PalmAnswerFeedback)
    suspend fun writeImprovementSignal(signal: AiImprovementSignal)
}

/**
 * Persistent storage driver implementation of [PalmSessionStorage].
 */
class DriverPalmSessionStorage(
    private val driver: StorageDriver,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : PalmSessionStorage {

    private val mutex = Mutex()

    companion object {
        private const val SESSIONS_KEY = "palm_sessions_v1"
        private const val SIGNALS_KEY = "palm_ai_improvement_signals_v1"
    }

    override suspend fun readSessions(): List<PalmReadingSession> = mutex.withLock {
        val raw = driver.read(SESSIONS_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<PalmReadingSession>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun writeSessions(sessions: List<PalmReadingSession>): Unit = mutex.withLock {
        val serialized = json.encodeToString(sessions)
        driver.write(SESSIONS_KEY, serialized)
    }

    override suspend fun writeFeatureFeedback(feedback: PalmFeatureFeedback): Unit =
        mutex.withLock {
            val key = "palm_feat_feedback_${feedback.readingId}"
            val existing = driver.read(key)?.let {
                try {
                    json.decodeFromString<List<PalmFeatureFeedback>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
            driver.write(key, json.encodeToString(existing + feedback))
        }

    override suspend fun writeAnswerFeedback(feedback: PalmAnswerFeedback): Unit = mutex.withLock {
        val key = "palm_ans_feedback_${feedback.readingId}"
        val existing = driver.read(key)?.let {
            try {
                json.decodeFromString<List<PalmAnswerFeedback>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
        driver.write(key, json.encodeToString(existing + feedback))
    }

    override suspend fun writeImprovementSignal(signal: AiImprovementSignal): Unit =
        mutex.withLock {
            val existing = driver.read(SIGNALS_KEY)?.let {
                try {
                    json.decodeFromString<List<AiImprovementSignal>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
            driver.write(SIGNALS_KEY, json.encodeToString(existing + signal))
        }
}
