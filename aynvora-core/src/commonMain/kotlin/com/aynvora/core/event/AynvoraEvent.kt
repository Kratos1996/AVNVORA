package com.aynvora.core.event

import com.aynvora.core.feature.CoreFeatureId
import kotlinx.serialization.Serializable

/**
 * High-level classification of user interactions and system events.
 */
enum class AynvoraEventType {
    CLICK,
    SUBMIT,
    SELECT,
    TOGGLE,
    SWIPE,
    LONG_PRESS,
    BACK,
    CLOSE,
    REFRESH,
    RETRY,
    DELETE,
    DOWNLOAD,
    SHARE,
    NAVIGATE,
    FEATURE_ACTION,
    CUSTOM,
}

/**
 * Immutable metadata attached to every [AynvoraEvent].
 * Explicitly excludes sensitive PII, passwords, prompts, and raw biometric bytes.
 */
@Serializable
data class AynvoraEventMetadata(
    val screenId: String,
    val componentId: String,
    val correlationId: String = "",
    val locale: String = "en",
    val sessionId: String = "",
    val timestampEpochMs: Long = 0L,
    val source: String = "ui",
    val version: String = "1.0.0",
)

/**
 * Sealed contract for strictly typed event payloads.
 * Arbitrary Map<String, Any> is strictly forbidden as an event contract.
 */
sealed interface AynvoraEventPayload {
    data object Empty : AynvoraEventPayload

    // Dashboard & Navigation payloads
    data class FeatureOpenPayload(val featureId: CoreFeatureId) : AynvoraEventPayload
    data class NavigationPayload(val target: AynvoraNavigationTarget) : AynvoraEventPayload
    data class LanguageChangePayload(val localeId: String) : AynvoraEventPayload
    data class ThemeTogglePayload(val isDark: Boolean) : AynvoraEventPayload

    // AI Lifecycle payloads
    data class AiDownloadPayload(val modelId: String) : AynvoraEventPayload
    data class AiCancelPayload(val modelId: String = "") : AynvoraEventPayload
    data class AiDeletePayload(val modelId: String = "") : AynvoraEventPayload
    data class AiRetryPayload(val modelId: String = "") : AynvoraEventPayload

    // Tarot payloads
    data class TarotSelectDeckPayload(val deckId: String) : AynvoraEventPayload
    data class TarotSelectSpreadPayload(val spreadId: String) : AynvoraEventPayload
    data class TarotDrawCardPayload(val position: Int) : AynvoraEventPayload
    data class TarotQuestionPayload(val questionText: String) : AynvoraEventPayload
    data class TarotClarificationPayload(val cardId: String = "", val reason: String = "") :
        AynvoraEventPayload

    data class TarotFeedbackPayload(val starRating: Int, val optionalText: String? = null) :
        AynvoraEventPayload

    data class TarotCardFeedbackPayload(val cardId: String, val helpful: Boolean) :
        AynvoraEventPayload

    data class TarotLockPayload(val remainingDuration: String) : AynvoraEventPayload
    data class TarotContentPayload(val cardId: String, val language: String) : AynvoraEventPayload
    data class TarotAiAnswerPayload(
        val modelId: String? = null,
        val promptVersion: String? = null,
        val fallbackUsed: Boolean = false,
        val language: String = "en"
    ) : AynvoraEventPayload

    // Palmistry payloads
    data class PalmSelectHandPayload(val hand: String) : AynvoraEventPayload
    data class PalmImageSourcePayload(val sourceType: String) : AynvoraEventPayload
    data class PalmQuestionPayload(val questionText: String, val targetLine: String? = null) :
        AynvoraEventPayload

    data class PalmFeedbackPayload(val starRating: Int, val comment: String? = null) :
        AynvoraEventPayload

    data class PalmFeatureDetailPayload(val lineType: String) : AynvoraEventPayload
    data class PalmSaveSessionPayload(val sessionId: String) : AynvoraEventPayload
    data class PalmFeatureFeedbackPayload(
        val sessionId: String,
        val featureType: String,
        val category: String
    ) : AynvoraEventPayload

    data class PalmAnswerFeedbackPayload(
        val sessionId: String,
        val questionId: String,
        val isHelpful: Boolean
    ) : AynvoraEventPayload

    // Garuda Puran payloads
    data class GarudaTopicPayload(val topicId: String) : AynvoraEventPayload

    // Report payloads
    data class ReportGeneratePayload(val reportTypeId: String) : AynvoraEventPayload
    data class ReportPdfPayload(val reportTypeId: String) : AynvoraEventPayload
    data class ReportSharePayload(val reportTypeId: String) : AynvoraEventPayload

    // Numerology payloads
    data class NumerologyCalculatePayload(
        val rulesetId: String,
        val hasName: Boolean,
        val hasTargetYear: Boolean,
    ) : AynvoraEventPayload

    data class NumerologyRulesetSelectPayload(val rulesetId: String) : AynvoraEventPayload
    data class NumerologyResultViewPayload(val rulesetId: String) : AynvoraEventPayload
    data class NumerologyTraceViewPayload(val rulesetId: String) : AynvoraEventPayload
    data class NumerologyComparePayload(val rulesetIds: List<String>) : AynvoraEventPayload
    data class NumerologyHistoryOpenPayload(val sessionCount: Int = 0) : AynvoraEventPayload
    data class NumerologyCalculationFailedPayload(val errorCode: String) : AynvoraEventPayload
    data class NumerologyReportRequestPayload(val rulesetId: String) : AynvoraEventPayload
    data class NumerologyAiOpenPayload(val rulesetId: String) : AynvoraEventPayload
    data class NumerologyAiQuestionSubmittedPayload(val rulesetId: String, val category: String) :
        AynvoraEventPayload

    data class NumerologyAiExplanationStartedPayload(val rulesetId: String, val category: String) :
        AynvoraEventPayload

    data class NumerologyAiExplanationCompletedPayload(
        val rulesetId: String,
        val category: String,
        val fallbackUsed: Boolean,
        val modelId: String? = null,
    ) : AynvoraEventPayload

    data class NumerologyAiExplanationFailedPayload(val rulesetId: String, val errorCode: String) :
        AynvoraEventPayload

    data class NumerologyAiFallbackUsedPayload(val rulesetId: String, val reason: String) :
        AynvoraEventPayload

    data class NumerologyAiClarificationPayload(val rulesetId: String, val category: String) :
        AynvoraEventPayload
}

/**
 * Base immutable contract for all typed AYNVORA events.
 */
interface AynvoraEvent {
    val eventId: String
    val eventType: AynvoraEventType
    val timestampEpochMs: Long
    val screenId: String
    val componentId: String
    val source: String
    val correlationId: String
    val payload: AynvoraEventPayload
    val metadata: AynvoraEventMetadata
    val schemaVersion: Int get() = 1
}

/**
 * Base UI event emitted by interactive components.
 */
open class AynvoraUiEvent(
    override val eventId: String,
    override val eventType: AynvoraEventType,
    override val timestampEpochMs: Long = 0L,
    override val screenId: String,
    override val componentId: String,
    override val source: String = "ui",
    override val correlationId: String = "",
    override val payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
    override val metadata: AynvoraEventMetadata = AynvoraEventMetadata(
        screenId = screenId,
        componentId = componentId,
        correlationId = correlationId,
        timestampEpochMs = timestampEpochMs,
        source = source,
    ),
    override val schemaVersion: Int = 1,
) : AynvoraEvent

/**
 * Typed Click Event representing user tap/click interactions.
 */
open class AynvoraClickEvent(
    eventId: String,
    screenId: String,
    componentId: String,
    payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
    metadata: AynvoraEventMetadata = AynvoraEventMetadata(
        screenId = screenId,
        componentId = componentId,
    ),
    timestampEpochMs: Long = 0L,
    correlationId: String = "",
) : AynvoraUiEvent(
    eventId = eventId,
    eventType = AynvoraEventType.CLICK,
    timestampEpochMs = timestampEpochMs,
    screenId = screenId,
    componentId = componentId,
    source = "ui_click",
    correlationId = correlationId,
    payload = payload,
    metadata = metadata,
)
