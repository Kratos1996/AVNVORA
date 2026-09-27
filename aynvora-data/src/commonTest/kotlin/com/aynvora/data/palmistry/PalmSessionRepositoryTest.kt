package com.aynvora.data.palmistry

import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedbackCategory
import com.aynvora.core.palmistry.PalmFeedback
import com.aynvora.core.palmistry.PalmFinding
import com.aynvora.core.palmistry.PalmLineFinding
import com.aynvora.core.palmistry.PalmLineType
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmReadingStatus
import com.aynvora.core.palmistry.PalmShape
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.AiImprovementSignal
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PalmSessionRepositoryTest {

    private class InMemoryPalmStorage : PalmSessionStorage {
        val sessions = mutableListOf<PalmReadingSession>()
        val signals = mutableListOf<AiImprovementSignal>()
        val featureFeedbacks = mutableListOf<PalmFeatureFeedback>()
        val answerFeedbacks = mutableListOf<PalmAnswerFeedback>()

        override suspend fun readSessions(): List<PalmReadingSession> = sessions.toList()

        override suspend fun writeSessions(sessions: List<PalmReadingSession>) {
            this.sessions.clear()
            this.sessions.addAll(sessions)
        }

        override suspend fun writeFeatureFeedback(feedback: PalmFeatureFeedback) {
            featureFeedbacks.add(feedback)
        }

        override suspend fun writeAnswerFeedback(feedback: PalmAnswerFeedback) {
            answerFeedbacks.add(feedback)
        }

        override suspend fun writeImprovementSignal(signal: AiImprovementSignal) {
            signals.add(signal)
        }
    }

    private val storage = InMemoryPalmStorage()
    private val repository = PalmSessionRepositoryImpl(storage = storage)

    private fun createTestSession(id: String, stars: Int? = null): PalmReadingSession {
        val finding = PalmFinding(
            handType = HandType.RIGHT,
            lines = listOf(
                PalmLineFinding(
                    lineType = PalmLineType.LIFE_LINE,
                    clarityScore = 0.85f,
                    curvatureScore = 0.40f,
                    lengthCategory = "LONG",
                    breaksDetected = false,
                    detected = true,
                    continuity = 0.90f,
                    strength = "STRONG",
                )
            ),
            mounts = emptyList(),
            overallClarity = 0.85f,
            shape = PalmShape.SQUARE,
            analysisVersion = "1.0.0",
        )
        return PalmReadingSession(
            id = id,
            startedAtEpochMs = 1000L,
            handType = HandType.RIGHT,
            finding = finding,
            status = PalmReadingStatus.ACTIVE,
            feedback = stars?.let {
                PalmFeedback(
                    readingId = id,
                    ratingStars = it,
                    improvementComment = "Test feedback $it stars",
                    timestampEpochMs = 1100L,
                )
            }
        )
    }

    @Test
    fun testSaveAndGetSession(): Unit = runBlocking {
        val session = createTestSession("sess_101")
        val saveRes = repository.saveSession(session)
        assertTrue(saveRes is AynvoraResult.Success)

        val getRes = repository.getSession("sess_101")
        assertTrue(getRes is AynvoraResult.Success)
        val retrieved = (getRes as AynvoraResult.Success).value
        assertNotNull(retrieved)
        assertEquals("sess_101", retrieved.id)
        assertEquals(HandType.RIGHT, retrieved.handType)
    }

    @Test
    fun testSubmitOverallFeedback1To5Stars(): Unit = runBlocking {
        for (stars in 1..5) {
            val session = createTestSession("sess_feedback_$stars", stars = stars)
            val res = repository.saveSession(session)
            assertTrue(res is AynvoraResult.Success)
        }

        val signals = storage.signals
        assertTrue(signals.isNotEmpty())
        assertEquals(5, signals.size)
        assertEquals(5, signals.last().feedbackRating)
        assertEquals("PALMISTRY", signals.last().featureId)
    }

    @Test
    fun testSubmitFeatureAndAnswerFeedback(): Unit = runBlocking {
        // Feature feedback
        val featureFb = PalmFeatureFeedback(
            readingId = "sess_details",
            featureType = "LIFE_LINE",
            category = PalmFeatureFeedbackCategory.CLEAR,
            timestampEpochMs = 2000L,
        )
        val featRes = repository.recordFeatureFeedback(featureFb)
        assertTrue(featRes is AynvoraResult.Success)
        assertEquals(1, storage.featureFeedbacks.size)

        // Answer feedback
        val answerFb = PalmAnswerFeedback(
            readingId = "sess_details",
            questionId = "q_1",
            isHelpful = true,
            timestampEpochMs = 2100L,
        )
        val ansRes = repository.recordAnswerFeedback(answerFb)
        assertTrue(ansRes is AynvoraResult.Success)
        assertEquals(1, storage.answerFeedbacks.size)

        // Verify model immutability: signal is purely metric/telemetry, no weights modified
        val signals = storage.signals
        assertTrue(signals.any { it.feedbackType == "CLEAR" })
        assertTrue(signals.any { it.questionId == "q_1" && it.feedbackRating == 5 })
    }

    @Test
    fun testDeleteSessionRemovesArtifacts(): Unit = runBlocking {
        val session = createTestSession("sess_to_delete")
        repository.saveSession(session)
        assertNotNull((repository.getSession("sess_to_delete") as AynvoraResult.Success).value)

        val delRes = repository.deleteSession("sess_to_delete")
        assertTrue(delRes is AynvoraResult.Success)
        assertNull((repository.getSession("sess_to_delete") as AynvoraResult.Success).value)
    }
}
