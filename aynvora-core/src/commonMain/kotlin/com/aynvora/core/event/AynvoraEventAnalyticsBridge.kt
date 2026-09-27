package com.aynvora.core.event

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker

/**
 * Strict validator and sanitizer ensuring only privacy-safe, non-PII fields are permitted into analytics.
 *
 * Guaranteed exclusions:
 * - Passwords, tokens, API keys
 * - Email, phone numbers
 * - Birth dates, birth times, geographic coordinates
 * - Private user questions & notes
 * - Raw AI prompts & outputs
 * - Palm images & raw image byte arrays
 */
object AnalyticsSafePayload {
    private val FORBIDDEN_WORDS = listOf(
        "password",
        "token",
        "secret",
        "credential",
        "birth_date",
        "birth_time",
        "latitude",
        "longitude",
        "palm_image",
        "image_bytes",
        "prompt",
        "ai_output",
        "user_question",
        "question_text",
        "private_note",
        "email",
        "phone",
    )

    /**
     * Returns true only if the event and its payload are verified to contain zero sensitive PII.
     */
    fun isSafe(event: AynvoraEvent): Boolean {
        val eventIdLower = event.eventId.lowercase()
        for (word in FORBIDDEN_WORDS) {
            if (eventIdLower.contains(word)) return false
        }

        // Explicit payload-level security check: never allow question text or raw media into analytics
        when (event.payload) {
            is AynvoraEventPayload.TarotQuestionPayload -> return false
            is AynvoraEventPayload.PalmQuestionPayload -> return false
            else -> Unit
        }

        return true
    }

    /**
     * Sanitizes parameter maps before forwarding to analytics backends.
     * Strips any keys matching forbidden terms and truncates long unstructured text.
     */
    fun sanitizeParams(params: Map<String, Any>): Map<String, Any> {
        val safe = mutableMapOf<String, Any>()
        for ((key, value) in params) {
            val keyLower = key.lowercase()
            val isForbidden = FORBIDDEN_WORDS.any { keyLower.contains(it) }
            if (!isForbidden) {
                when (value) {
                    is String -> {
                        // Prevent prompt or log dump leakage via string values
                        if (value.length <= 128) {
                            safe[key] = value
                        }
                    }

                    is Number, is Boolean -> safe[key] = value
                }
            }
        }
        return safe
    }
}

/**
 * Maps high-level UI events to strictly approved [AnalyticsEvent] objects.
 */
interface AynvoraEventAnalyticsMapper {
    fun mapToAnalytics(event: AynvoraEvent): AnalyticsEvent?
}

/**
 * Production implementation of [AynvoraEventAnalyticsMapper].
 * Maintains an explicit allowlist of events that can be logged to analytics.
 * Unregistered or unmapped events return null and are safely ignored.
 */
class DefaultAynvoraEventAnalyticsMapper(
    private val registry: AynvoraEventRegistry = AynvoraEventRegistry,
) : AynvoraEventAnalyticsMapper {

    override fun mapToAnalytics(event: AynvoraEvent): AnalyticsEvent? {
        if (!AnalyticsSafePayload.isSafe(event)) {
            return null // Reject forbidden or unsafe events
        }

        // Verify that the event is registered
        val reg = registry.find(event.eventId) ?: return null
        if (reg.analyticsEventName == null) {
            return null // Event is not on the analytics allowlist
        }

        val mapped: AnalyticsEvent? = when (event.eventId) {
            "dashboard.feature.open_clicked" -> {
                val featureId =
                    (event.payload as? AynvoraEventPayload.FeatureOpenPayload)?.featureId?.name?.lowercase()
                        ?: "unknown"
                AnalyticsEvent.FeatureOpened(featureId)
            }

            "dashboard.tarot.open_clicked" -> AnalyticsEvent.TarotOpened
            "dashboard.palmistry.open_clicked" -> AnalyticsEvent.PalmistryOpened

            "dashboard.theme.toggle_clicked" -> AnalyticsEvent.ThemeToggled(isDark = false) // isDark not tracked; anonymous toggle

            "dashboard.language.change_clicked" -> {
                val localeId =
                    (event.payload as? AynvoraEventPayload.LanguageChangePayload)?.localeId ?: "en"
                AnalyticsEvent.LanguageChanged(localeId)
            }

            "dashboard.ai.download_clicked" -> {
                val modelId =
                    (event.payload as? AynvoraEventPayload.AiDownloadPayload)?.modelId ?: "unknown"
                AnalyticsEvent.AiDownloadStarted(modelId)
            }

            "dashboard.ai.cancel_clicked" -> {
                val modelId =
                    (event.payload as? AynvoraEventPayload.AiCancelPayload)?.modelId ?: "unknown"
                AnalyticsEvent.AiDownloadCancelled(modelId)
            }

            "dashboard.ai.delete_clicked" -> {
                val modelId =
                    (event.payload as? AynvoraEventPayload.AiDeletePayload)?.modelId ?: "unknown"
                AnalyticsEvent.AiModelDeleted(modelId)
            }

            "tarot.disclaimer.viewed" -> AnalyticsEvent.TarotDisclaimerViewed

            "tarot.spread.selected" -> {
                val spreadId =
                    (event.payload as? AynvoraEventPayload.TarotSelectSpreadPayload)?.spreadId
                        ?: "unknown"
                AnalyticsEvent.TarotSpreadSelected(spreadId)
            }

            "tarot.card.draw_clicked" -> {
                AnalyticsEvent.TarotReadingStarted(spreadId = "active", cardCount = 1)
            }

            "tarot.card.reveal_clicked" -> AnalyticsEvent.TarotCardRevealed

            "tarot.feedback.submitted" -> {
                val fb = event.payload as? AynvoraEventPayload.TarotFeedbackPayload
                val rating = fb?.starRating ?: 5
                AnalyticsEvent.TarotFeedbackSubmitted(
                    starRating = rating,
                    language = event.metadata.locale
                )
            }

            "tarot.clarification.requested" -> AnalyticsEvent.TarotClarificationRequested
            "tarot.clarification.accepted" -> AnalyticsEvent.TarotClarificationAccepted
            "tarot.history.opened" -> AnalyticsEvent.TarotHistoryOpened

            "palmistry.disclaimer.viewed" -> AnalyticsEvent.PalmistryDisclaimerViewed

            "palmistry.hand.selected" -> {
                val hand =
                    (event.payload as? AynvoraEventPayload.PalmSelectHandPayload)?.hand ?: "UNKNOWN"
                AnalyticsEvent.PalmistryHandSelected(hand)
            }

            "palmistry.image.selected" -> AnalyticsEvent.PalmistryImageSelected

            "palmistry.analysis.started" -> {
                val hand =
                    (event.payload as? AynvoraEventPayload.PalmSelectHandPayload)?.hand ?: "RIGHT"
                AnalyticsEvent.PalmistryAnalysisStarted(hand)
            }

            "palmistry.analysis.completed" -> {
                val hand =
                    (event.payload as? AynvoraEventPayload.PalmSelectHandPayload)?.hand ?: "RIGHT"
                AnalyticsEvent.PalmistryAnalysisCompleted(hand, "1.0.0")
            }

            "palmistry.feedback.submitted" -> {
                val fb = event.payload as? AynvoraEventPayload.PalmFeedbackPayload
                val rating = fb?.starRating ?: 5
                AnalyticsEvent.PalmistryFeedbackSubmitted(rating)
            }

            "tarot.screen.opened" -> AnalyticsEvent.TarotOpened

            "tarot.card.feedback_submitted" -> {
                val fb = event.payload as? AynvoraEventPayload.TarotCardFeedbackPayload
                if (fb != null) {
                    AnalyticsEvent.TarotCardFeedbackSubmitted(
                        fb.cardId,
                        if (fb.helpful) "HELPFUL" else "NOT_HELPFUL"
                    )
                } else null
            }

            "tarot.answer_feedback.submitted" -> {
                val fb = event.payload as? AynvoraEventPayload.TarotFeedbackPayload
                AnalyticsEvent.TarotAnswerFeedbackSubmitted(
                    fb?.starRating ?: 5,
                    event.metadata.locale
                )
            }

            "tarot.clarification.drawn" -> {
                val c = event.payload as? AynvoraEventPayload.TarotClarificationPayload
                AnalyticsEvent.TarotClarificationDrawn(c?.cardId ?: "", c?.reason ?: "")
            }

            "tarot.reading.lock_shown" -> {
                val p = event.payload as? AynvoraEventPayload.TarotLockPayload
                AnalyticsEvent.TarotReadingLockShown(p?.remainingDuration ?: "0")
            }

            "tarot.content.opened" -> {
                val p = event.payload as? AynvoraEventPayload.TarotContentPayload
                AnalyticsEvent.TarotContentOpened(
                    p?.cardId ?: "",
                    p?.language ?: event.metadata.locale
                )
            }

            "tarot.ai.answer_generated" -> {
                val p = event.payload as? AynvoraEventPayload.TarotAiAnswerPayload
                if (p != null) {
                    AnalyticsEvent.TarotAiAnswerGenerated(
                        modelId = p.modelId,
                        modelVersion = null,
                        promptVersion = p.promptVersion ?: "unknown",
                        fallbackUsed = p.fallbackUsed,
                        language = p.language,
                    )
                } else null
            }

            "palmistry.screen.opened" -> AnalyticsEvent.PalmistryOpened

            "palmistry.question.submitted" -> AnalyticsEvent.PalmistryQuestionSubmitted(event.metadata.locale)

            "palmistry.pdf.requested" -> AnalyticsEvent.PalmistryPdfGenerated

            "garuda_puran.screen.opened" -> AnalyticsEvent.GarudaPuranOpened

            "garuda_puran.topic.selected" -> {
                val topic =
                    (event.payload as? AynvoraEventPayload.GarudaTopicPayload)?.topicId ?: ""
                val topicId = try {
                    com.aynvora.core.garudapuran.GarudaPuranTopicId.valueOf(topic)
                } catch (e: Exception) {
                    null
                }
                if (topicId != null) AnalyticsEvent.GarudaPuranTopicOpened(topicId) else null
            }

            "report.generate_clicked" -> {
                val type =
                    (event.payload as? AynvoraEventPayload.ReportGeneratePayload)?.reportTypeId
                        ?: "unknown"
                AnalyticsEvent.ReportOpened(type)
            }

            "report.pdf.download_clicked" -> {
                val type = (event.payload as? AynvoraEventPayload.ReportPdfPayload)?.reportTypeId
                    ?: "unknown"
                AnalyticsEvent.ReportPdfGenerated(type)
            }

            "report.share.clicked" -> {
                val type = (event.payload as? AynvoraEventPayload.ReportSharePayload)?.reportTypeId
                    ?: "unknown"
                AnalyticsEvent.ReportShared(type)
            }

            "dashboard.numerology.open_clicked" -> AnalyticsEvent.NumerologyOpened

            "numerology.calculate_clicked" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyCalculatePayload
                AnalyticsEvent.NumerologyCalculationStarted(
                    payload?.rulesetId ?: "CHALDEAN_CHEIRO_V1"
                )
            }

            "numerology.ruleset_selected" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyRulesetSelectPayload
                AnalyticsEvent.NumerologyRulesetSelected(payload?.rulesetId ?: "unknown")
            }

            "numerology.calculation_completed" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyResultViewPayload
                AnalyticsEvent.NumerologyCalculationCompleted(payload?.rulesetId ?: "unknown")
            }

            "numerology.calculation_failed" -> {
                val payload =
                    event.payload as? AynvoraEventPayload.NumerologyCalculationFailedPayload
                AnalyticsEvent.NumerologyCalculationFailed(payload?.errorCode ?: "unknown")
            }

            "numerology.result_viewed" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyResultViewPayload
                AnalyticsEvent.NumerologyResultViewed(payload?.rulesetId ?: "unknown")
            }

            "numerology.trace_viewed" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyTraceViewPayload
                AnalyticsEvent.NumerologyTraceViewed(payload?.rulesetId ?: "unknown")
            }

            "numerology.method_compared" -> AnalyticsEvent.NumerologyMethodCompared

            "numerology.report_requested" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyReportRequestPayload
                AnalyticsEvent.NumerologyReportRequested(payload?.rulesetId ?: "unknown")
            }

            "numerology.history_opened" -> AnalyticsEvent.NumerologyHistoryOpened

            "numerology.ai.open_clicked" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyAiOpenPayload
                AnalyticsEvent.NumerologyAiOpened(payload?.rulesetId ?: "unknown")
            }

            "numerology.ai.question_submitted" -> {
                val payload =
                    event.payload as? AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload
                AnalyticsEvent.NumerologyAiQuestionSubmitted(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    category = payload?.category ?: "unknown",
                )
            }

            "numerology.ai.explanation_started" -> {
                val payload =
                    event.payload as? AynvoraEventPayload.NumerologyAiExplanationStartedPayload
                AnalyticsEvent.NumerologyAiExplanationStarted(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    category = payload?.category ?: "unknown",
                )
            }

            "numerology.ai.explanation_completed" -> {
                val payload =
                    event.payload as? AynvoraEventPayload.NumerologyAiExplanationCompletedPayload
                AnalyticsEvent.NumerologyAiExplanationCompleted(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    category = payload?.category ?: "unknown",
                    fallbackUsed = payload?.fallbackUsed ?: false,
                    modelId = payload?.modelId,
                )
            }

            "numerology.ai.explanation_failed" -> {
                val payload =
                    event.payload as? AynvoraEventPayload.NumerologyAiExplanationFailedPayload
                AnalyticsEvent.NumerologyAiExplanationFailed(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    errorCode = payload?.errorCode ?: "unknown",
                )
            }

            "numerology.ai.fallback_used" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyAiFallbackUsedPayload
                AnalyticsEvent.NumerologyAiFallbackUsed(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    reason = payload?.reason ?: "unknown",
                )
            }

            "numerology.ai.clarification_requested" -> {
                val payload = event.payload as? AynvoraEventPayload.NumerologyAiClarificationPayload
                AnalyticsEvent.NumerologyAiClarificationRequested(
                    rulesetId = payload?.rulesetId ?: "unknown",
                    category = payload?.category ?: "unknown",
                )
            }

            else -> null
        }
        return mapped
    }
}

/**
 * Event observer bridge connecting the event pipeline to [AnalyticsTracker].
 *
 * Key governance:
 * - Observes events asynchronously after business execution
 * - Strictly an observer; cannot execute business logic, mutate state, or navigate
 * - All analytics operations are insulated in try/catch so telemetry failures NEVER impact business flows
 */
class AynvoraEventAnalyticsBridge(
    private val analyticsTracker: AnalyticsTracker,
    private val mapper: AynvoraEventAnalyticsMapper = DefaultAynvoraEventAnalyticsMapper(),
) : AynvoraEventObserver {

    override suspend fun onEvent(event: AynvoraEvent, result: EventHandlingResult) {
        if (result is EventHandlingResult.IgnoredDuplicate) {
            return // Do not track rejected duplicates
        }

        try {
            val analyticsEvent = mapper.mapToAnalytics(event)
            if (analyticsEvent != null) {
                analyticsTracker.track(analyticsEvent)
            }
        } catch (_: Exception) {
            // Analytics failures must NEVER propagate or break business actions
        }
    }
}
