package com.aynvora.data.palmistry

import com.aynvora.core.palmistry.HandImageReference
import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmFinding
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmReadingStatus
import com.aynvora.core.palmistry.PalmSessionRepository
import com.aynvora.core.palmistry.PalmTimelineEvent
import com.aynvora.core.palmistry.PalmTimelineEventType
import com.aynvora.core.palmistry.PalmistryAnalysisResult
import com.aynvora.core.palmistry.PalmistryContentPackage
import com.aynvora.core.palmistry.PalmistryRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.AiImprovementSignal
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mutex-protected thread-safe implementation of [PalmSessionRepository] and [PalmistryRepository].
 */
class PalmSessionRepositoryImpl(
    private val storage: PalmSessionStorage,
) : PalmSessionRepository, PalmistryRepository {

    private val mutex = Mutex()

    // ── PalmSessionRepository Implementation ──────────────────────────────────────

    override suspend fun saveSession(session: PalmReadingSession): AynvoraResult<Unit> =
        mutex.withLock {
            try {
                val sessions = storage.readSessions().toMutableList()
                val existingIndex = sessions.indexOfFirst { it.id == session.id }
                if (existingIndex >= 0) {
                    sessions[existingIndex] = session
                } else {
                    sessions.add(0, session)
                }
                storage.writeSessions(sessions)

                // Emit AI improvement signal if feedback is present
                session.feedback?.let { fb ->
                    val signal = AiImprovementSignal(
                        signalId = "sig_palm_${session.id}_${fb.timestampEpochMs}",
                        featureId = "PALMISTRY",
                        sessionId = session.id,
                        readingId = session.id,
                        feedbackRating = fb.ratingStars,
                        feedbackType = "SESSION_RATING",
                        language = session.language,
                        modelId = session.analysisResult?.analysisVersion ?: "1.0.0",
                        modelVersion = "1.0.0",
                        promptVersion = "1.0.0",
                        contentVersion = 1,
                        timestampEpochMs = fb.timestampEpochMs,
                    )
                    storage.writeImprovementSignal(signal)
                }

                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.InternalFailure("Failed to save palm session: ${e.message}")
            }
        }

    override suspend fun getSession(sessionId: String): AynvoraResult<PalmReadingSession?> =
        mutex.withLock {
            try {
                val session = storage.readSessions().firstOrNull { it.id == sessionId }
                AynvoraResult.Success(session)
            } catch (e: Exception) {
                AynvoraResult.Failure.InternalFailure("Failed to read palm session: ${e.message}")
            }
        }

    override suspend fun getLatestSession(): AynvoraResult<PalmReadingSession?> = mutex.withLock {
        try {
            val sessions = storage.readSessions()
            AynvoraResult.Success(sessions.firstOrNull())
        } catch (e: Exception) {
            AynvoraResult.Failure.InternalFailure("Failed to read latest palm session: ${e.message}")
        }
    }

    override suspend fun getAllSessions(): AynvoraResult<List<PalmReadingSession>> =
        mutex.withLock {
            try {
                val sessions = storage.readSessions()
                AynvoraResult.Success(sessions)
            } catch (e: Exception) {
                AynvoraResult.Failure.InternalFailure("Failed to read all palm sessions: ${e.message}")
            }
        }

    override suspend fun deleteSession(sessionId: String): AynvoraResult<Unit> = mutex.withLock {
        try {
            val sessions = storage.readSessions().filterNot { it.id == sessionId }
            storage.writeSessions(sessions)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.InternalFailure("Failed to delete palm session: ${e.message}")
        }
    }

    override suspend fun recordFeatureFeedback(feedback: PalmFeatureFeedback): AynvoraResult<Unit> =
        mutex.withLock {
            try {
                storage.writeFeatureFeedback(feedback)
                val signal = AiImprovementSignal(
                    signalId = "sig_palm_feat_${feedback.readingId}_${feedback.timestampEpochMs}",
                    featureId = "PALMISTRY",
                    sessionId = feedback.readingId,
                    readingId = feedback.readingId,
                    feedbackRating = 4,
                    feedbackType = feedback.category.name,
                    language = "en",
                    modelId = "palm-vision-v1",
                    modelVersion = "1.0.0",
                    promptVersion = "1.0.0",
                    contentVersion = 1,
                    timestampEpochMs = feedback.timestampEpochMs,
                )
                storage.writeImprovementSignal(signal)
                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.InternalFailure("Failed to record feature feedback: ${e.message}")
            }
        }

    override suspend fun recordAnswerFeedback(feedback: PalmAnswerFeedback): AynvoraResult<Unit> =
        mutex.withLock {
            try {
                storage.writeAnswerFeedback(feedback)
                val signal = AiImprovementSignal(
                    signalId = "sig_palm_ans_${feedback.questionId}_${feedback.timestampEpochMs}",
                    featureId = "PALMISTRY",
                    sessionId = feedback.readingId,
                    readingId = feedback.readingId,
                    questionId = feedback.questionId,
                    feedbackRating = if (feedback.isHelpful) 5 else 1,
                    feedbackType = if (feedback.isHelpful) "HELPFUL" else "NOT_HELPFUL",
                    language = "en",
                    modelId = "slm-ondevice-palm-v1",
                    modelVersion = "1.0.0",
                    promptVersion = "1.0.0",
                    contentVersion = 1,
                    timestampEpochMs = feedback.timestampEpochMs,
                )
                storage.writeImprovementSignal(signal)
                AynvoraResult.Success(Unit)
            } catch (e: Exception) {
                AynvoraResult.Failure.InternalFailure("Failed to record answer feedback: ${e.message}")
            }
        }

    // ── Legacy PalmistryRepository Implementation ────────────────────────────────

    override suspend fun saveSession(
        reference: HandImageReference,
        finding: PalmFinding,
    ): AynvoraResult<String> {
        val sessionId = "palm_session_${reference.captureTimestampEpochMs}"
        val meanings = PalmistryContentPackage.getMeaningsForFindings(finding, "en")
        val analysisResult = PalmistryAnalysisResult(
            sessionId = sessionId,
            handType = reference.handType,
            primaryStrengths = meanings.map { it.title },
            reflectiveTendencies = meanings.map { it.traditionalInterpretation },
            traditionalCommentary = finding.lines.associate { it.lineType to "${it.strength} (${it.lengthCategory})" },
        )

        val session = PalmReadingSession(
            id = sessionId,
            startedAtEpochMs = reference.captureTimestampEpochMs,
            handType = reference.handType,
            imageReference = reference,
            finding = finding,
            meanings = meanings,
            analysisResult = analysisResult,
            timeline = listOf(
                PalmTimelineEvent(
                    eventId = "evt_init_$sessionId",
                    readingId = sessionId,
                    timestampEpochMs = reference.captureTimestampEpochMs,
                    eventType = PalmTimelineEventType.READING_STARTED,
                    summary = "Reading started for ${reference.handType} hand",
                )
            ),
            status = PalmReadingStatus.ACTIVE,
        )

        return when (val saveRes = saveSession(session)) {
            is AynvoraResult.Success -> AynvoraResult.Success(sessionId)
            is AynvoraResult.Failure -> saveRes
        }
    }

    override suspend fun getAnalysisResult(sessionId: String): AynvoraResult<PalmistryAnalysisResult> {
        return when (val sessionRes = getSession(sessionId)) {
            is AynvoraResult.Success -> {
                val res = sessionRes.value?.analysisResult
                if (res != null) {
                    AynvoraResult.Success(res)
                } else {
                    AynvoraResult.Failure.NotFound(
                        sessionId,
                        "Palm analysis result not found for $sessionId"
                    )
                }
            }

            is AynvoraResult.Failure -> AynvoraResult.Failure.InternalFailure(sessionRes.message)
        }
    }
}
