package com.aynvora.core.tarot

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Reading Session Status
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Lifecycle status of a [TarotReadingSession].
 *
 * Transitions:
 *  ACTIVE → SATISFIED → COMPLETED → EXPIRED
 *  ACTIVE → COMPLETED (user skips feedback)
 *  COMPLETED → EXPIRED (24h window expires)
 */
@Serializable
enum class TarotReadingStatus {
    ACTIVE,
    SATISFIED,
    COMPLETED,
    EXPIRED,
    ARCHIVED,
}

// ─────────────────────────────────────────────────────────────────────────────
// Reading Session
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Persistent record of a complete Tarot reading session.
 *
 * One session = one rolling 24-hour primary reading window.
 * Follow-up questions, clarification cards, and feedback all belong to this session.
 *
 * @param id Stable unique session identifier.
 * @param reading The primary spread reading from [TarotDrawEngine].
 * @param startedAtEpochMs Epoch ms when the session was started.
 * @param expiresAtEpochMs Epoch ms when a new reading becomes available (startedAt + 24h).
 * @param status Current lifecycle status.
 * @param language The active language when the session was started.
 * @param deckId Selected deck identifier.
 */
@Serializable
data class TarotReadingSession(
    val id: String,
    val reading: TarotReading,
    val startedAtEpochMs: Long,
    val expiresAtEpochMs: Long,
    val status: TarotReadingStatus = TarotReadingStatus.ACTIVE,
    val language: String = "en",
    val deckId: String = TarotStandardDeck.Deck.id,
) {
    companion object {
        /** Rolling 24-hour new-reading lock duration in milliseconds. */
        const val READING_LOCK_DURATION_MS: Long = 24L * 60L * 60L * 1000L
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reading Availability
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Result of the reading availability policy evaluation.
 * UI always consumes this — never re-implements the 24h calculation.
 */
@Serializable
sealed class TarotReadingAvailability {
    @Serializable
    data object Available : TarotReadingAvailability()

    @Serializable
    data class Locked(
        val session: TarotReadingSession,
        val remainingMs: Long,
        val nextAvailableAtEpochMs: Long,
    ) : TarotReadingAvailability()

    @Serializable
    data class ActiveReadingExists(
        val session: TarotReadingSession,
    ) : TarotReadingAvailability()
}

// ─────────────────────────────────────────────────────────────────────────────
// Answer Status
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
enum class TarotAnswerStatus {
    GENERATING,
    COMPLETED,
    FALLBACK,
    INSUFFICIENT_EVIDENCE,
    FAILED,
}

// ─────────────────────────────────────────────────────────────────────────────
// Clarification Card
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Additional card drawn by [TarotDrawEngine] at AI recommendation.
 *
 * CONTRACT:
 * - AI NEVER draws this card. Only [TarotDrawEngine] draws it.
 * - At most ONE per [TarotQuestion].
 * - Does NOT reset the 24-hour reading lock.
 */
@Serializable
data class TarotClarificationCard(
    val id: String,
    val readingId: String,
    val questionId: String,
    val draw: TarotCardDraw,
    val drawnAtEpochMs: Long,
    val reason: String,
    val contentVersion: Int = 1,
)

// ─────────────────────────────────────────────────────────────────────────────
// Question Answer
// ─────────────────────────────────────────────────────────────────────────────

/**
 * AI-generated or deterministic reflective answer to a [TarotQuestion].
 *
 * The AI is strictly an explanation layer.
 * It NEVER selects cards, modifies card IDs, changes orientations, or alters timestamps.
 */
@Serializable
data class TarotQuestionAnswer(
    val id: String,
    val questionId: String,
    val readingId: String,
    val language: String,
    val summary: String,
    val interpretation: String,
    val keyThemes: List<String> = emptyList(),
    val supportingCardIds: List<String> = emptyList(),
    val clarificationRecommended: Boolean = false,
    val clarificationReason: String? = null,
    val status: TarotAnswerStatus = TarotAnswerStatus.COMPLETED,
    val fallbackUsed: Boolean = false,
    val modelId: String? = null,
    val promptVersion: String = "1.0",
    val contentVersion: Int = 1,
    val createdAtEpochMs: Long,
)

// ─────────────────────────────────────────────────────────────────────────────
// Question
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A follow-up question asked within an existing [TarotReadingSession].
 * NOT a new reading. Does NOT reset the 24-hour timer.
 */
@Serializable
data class TarotQuestion(
    val id: String,
    val readingId: String,
    val sessionId: String,
    val askedAtEpochMs: Long,
    val questionText: String,
    val originalLanguage: String,
    val sequenceNumber: Int,
    val cardEvidenceIds: List<String> = emptyList(),
    val clarificationCardId: String? = null,
    val answerId: String? = null,
    val answerStatus: TarotAnswerStatus = TarotAnswerStatus.GENERATING,
) {
    val clarificationUsed: Boolean get() = clarificationCardId != null
}

// ─────────────────────────────────────────────────────────────────────────────
// Timeline Event Types
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
enum class TarotTimelineEventType {
    READING_STARTED,
    SPREAD_SELECTED,
    CARDS_SHUFFLED,
    PRIMARY_CARD_DRAWN,
    CARD_REVEALED,
    QUESTION_ASKED,
    AI_ANSWER_GENERATED,
    AI_ANSWER_FALLBACK,
    CLARIFICATION_RECOMMENDED,
    CLARIFICATION_ACCEPTED,
    CLARIFICATION_DECLINED,
    CLARIFICATION_CARD_DRAWN,
    ANSWER_UPDATED,
    QUESTION_COMPLETED,
    FEEDBACK_SUBMITTED,
    READING_SATISFIED,
    READING_COMPLETED,
    READING_EXPIRED,
}

// ─────────────────────────────────────────────────────────────────────────────
// Timeline Event
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single chronological event in the reading timeline.
 * All events are associated with [readingId]. Ordered by [timestampEpochMs] + [sequenceIndex].
 */
@Serializable
data class TarotTimelineEvent(
    val id: String,
    val readingId: String,
    val sessionId: String,
    val questionId: String? = null,
    val timestampEpochMs: Long,
    val sequenceIndex: Int,
    val eventType: TarotTimelineEventType,
    val language: String,
    val summaryKey: String = "",
    val metadataJson: String = "{}",
    val provenance: String = "AYNVORA_TAROT",
)

// ─────────────────────────────────────────────────────────────────────────────
// Card Feedback
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
enum class TarotCardFeedbackCategory {
    HELPFUL,
    CLEAR,
    CONFUSING,
    NOT_RELEVANT,
    NEED_MORE_CONTEXT,
}

@Serializable
data class TarotCardFeedback(
    val id: String,
    val readingId: String,
    val sessionId: String,
    val cardId: String,
    val category: TarotCardFeedbackCategory,
    val starRating: Int? = null,
    val submittedAtEpochMs: Long,
)

// ─────────────────────────────────────────────────────────────────────────────
// Reading Feedback
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class TarotFeedback(
    val id: String,
    val sessionId: String,
    val readingId: String,
    val starRating: Int,
    val optionalText: String? = null,
    val language: String,
    val submittedAtEpochMs: Long,
)

// ─────────────────────────────────────────────────────────────────────────────
// Answer Feedback
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class TarotAnswerFeedback(
    val id: String,
    val answerId: String,
    val questionId: String,
    val readingId: String,
    val sessionId: String,
    val starRating: Int,
    val language: String,
    val submittedAtEpochMs: Long,
)

// ─────────────────────────────────────────────────────────────────────────────
// AI Improvement Signal
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Structured improvement signal created from user feedback.
 *
 * LOCAL record only. NEVER directly modifies model weights.
 * Privacy: version IDs, ratings, technical categories only.
 * Never contains: raw question text, answer text, personal data.
 */
@Serializable
data class AiImprovementSignal(
    val signalId: String,
    val featureId: String = "TAROT",
    val sessionId: String,
    val readingId: String,
    val questionId: String? = null,
    val answerId: String? = null,
    val cardId: String? = null,
    val feedbackRating: Int,
    val feedbackType: String,
    val language: String,
    val modelId: String? = null,
    val modelVersion: String? = null,
    val promptVersion: String,
    val contentVersion: Int,
    val timestampEpochMs: Long,
)
