package com.aynvora.core.event

import com.aynvora.core.feature.CoreFeatureId
import kotlin.reflect.KClass

/**
 * Metadata definition for an explicitly registered event.
 */
data class RegisteredEventDefinition(
    val eventId: String,
    val eventType: AynvoraEventType,
    val allowedFeature: CoreFeatureId?,
    val expectedPayloadClass: KClass<out AynvoraEventPayload>,
    val defaultIdempotencyPolicy: AynvoraDeduplicationPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
    val analyticsEventName: String? = null,
)

/**
 * Strict central registry of all authorized events within the AYNVORA system.
 *
 * Enforces:
 * - Event ID stability and explicit declaration
 * - Strict 1:1 or N:1 payload ownership (prevents payload mismatch attacks)
 * - Prohibits unauthorized or synthetic events from bypassing domain checks
 */
object AynvoraEventRegistry {
    private val definitions = mutableMapOf<String, RegisteredEventDefinition>()

    init {
        // ── Dashboard & Shell ───────────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.feature.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.FeatureOpenPayload::class,
                analyticsEventName = "feature_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.theme.toggle_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "theme_toggled",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.language.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.language.change_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.LanguageChangePayload::class,
                analyticsEventName = "language_changed",
            )
        )

        // ── AI Setup & Model Lifecycle ──────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.ai.download_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiDownloadPayload::class,
                defaultIdempotencyPolicy = AynvoraDeduplicationPolicy.IDEMPOTENT,
                analyticsEventName = "ai_download_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.ai.cancel_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiCancelPayload::class,
                defaultIdempotencyPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
                analyticsEventName = "ai_download_cancelled",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.ai.delete_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiDeletePayload::class,
                defaultIdempotencyPolicy = AynvoraDeduplicationPolicy.IDEMPOTENT,
                analyticsEventName = "ai_deleted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.ai.retry_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiRetryPayload::class,
                defaultIdempotencyPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.opened",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiOpenedPayload::class,
                analyticsEventName = "ai_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.model_status_viewed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiModelStatusViewedPayload::class,
                analyticsEventName = "ai_model_status_viewed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.model_download_started",
                eventType = AynvoraEventType.DOWNLOAD,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiDownloadStartedPayload::class,
                analyticsEventName = "ai_model_download_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.model_download_completed",
                eventType = AynvoraEventType.DOWNLOAD,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiDownloadCompletedPayload::class,
                analyticsEventName = "ai_model_download_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.model_verification_completed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiVerificationCompletedPayload::class,
                analyticsEventName = "ai_model_verification_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.inference_started",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiInferenceStartedPayload::class,
                analyticsEventName = "ai_inference_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.inference_completed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiInferenceCompletedPayload::class,
                analyticsEventName = "ai_inference_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.fallback_used",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiFallbackUsedPayload::class,
                analyticsEventName = "ai_fallback_used",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "ai.validation_failed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.AiValidationFailedPayload::class,
                analyticsEventName = "ai_validation_failed",
            )
        )

        // ── Vedic Astrology Module ──────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.astrology.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.ASTROLOGY,
                expectedPayloadClass = AynvoraEventPayload.FeatureOpenPayload::class,
                analyticsEventName = "astrology_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "astrology.close_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.ASTROLOGY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "astrology.calculate_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.ASTROLOGY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "astrology_chart_calculated",
            )
        )

        // ── Tarot Module ───────────────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.tarot.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.FeatureOpenPayload::class,
                analyticsEventName = "tarot_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.disclaimer.viewed",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "tarot_disclaimer_viewed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.disclaimer.accepted",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.deck.selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotSelectDeckPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.spread.selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotSelectSpreadPayload::class,
                analyticsEventName = "tarot_spread_selected",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.card.draw_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotDrawCardPayload::class,
                analyticsEventName = "tarot_reading_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.card.reveal_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "tarot_card_revealed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.question.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotQuestionPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.clarification.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotClarificationPayload::class,
                analyticsEventName = "tarot_clarification_requested",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.clarification.accepted",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "tarot_clarification_accepted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.feedback.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotFeedbackPayload::class,
                analyticsEventName = "tarot_feedback_submitted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.card_feedback.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotCardFeedbackPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.history.opened",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "tarot_history_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.report.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.ReportGeneratePayload::class,
                analyticsEventName = "report_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.pdf.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.ReportPdfPayload::class,
                analyticsEventName = "report_pdf_generated",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.share.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.ReportSharePayload::class,
                analyticsEventName = "report_shared",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.close_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.screen.opened",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "tarot_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.answer_feedback.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotFeedbackPayload::class,
                analyticsEventName = "tarot_feedback_submitted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.clarification.drawn",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotClarificationPayload::class,
                analyticsEventName = "tarot_clarification_drawn",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.reading.lock_shown",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotLockPayload::class,
                analyticsEventName = "tarot_reading_lock_shown",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.content.opened",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotContentPayload::class,
                analyticsEventName = "tarot_content_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.ai.answer_generated",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.TarotAiAnswerPayload::class,
                analyticsEventName = "tarot_ai_answer_generated",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "tarot.back_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.TAROT,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )

        // ── Palmistry Module ───────────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.palmistry.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.FeatureOpenPayload::class,
                analyticsEventName = "palmistry_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.disclaimer.viewed",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "palmistry_disclaimer_viewed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.disclaimer.accepted",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.hand.selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmSelectHandPayload::class,
                analyticsEventName = "palmistry_hand_selected",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.image.selected",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmImageSourcePayload::class,
                analyticsEventName = "palmistry_image_selected",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.camera.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "palmistry_camera_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.gallery.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "palmistry_gallery_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.analysis.started",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmSelectHandPayload::class,
                analyticsEventName = "palmistry_analysis_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.analysis.completed",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmSelectHandPayload::class,
                analyticsEventName = "palmistry_analysis_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.question.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmQuestionPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.feedback.submitted",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmFeedbackPayload::class,
                analyticsEventName = "palmistry_feedback_submitted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.session.selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.NavigationPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.report.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.ReportGeneratePayload::class,
                analyticsEventName = "report_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.pdf.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.ReportPdfPayload::class,
                analyticsEventName = "report_pdf_generated",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.share.requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.ReportSharePayload::class,
                analyticsEventName = "report_shared",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.close_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.screen.opened",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "palmistry_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.session.saved",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmSaveSessionPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.feedback.feature_recorded",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmFeatureFeedbackPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.feedback.answer_recorded",
                eventType = AynvoraEventType.SUBMIT,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.PalmAnswerFeedbackPayload::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "palmistry.back_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.PALMISTRY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )

        // ── Garuda Puran Module ─────────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "garuda_puran.screen.opened",
                eventType = AynvoraEventType.CUSTOM,
                allowedFeature = CoreFeatureId.GARUDA_PURAN,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
                analyticsEventName = "garuda_puran_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "garuda_puran.topic.selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.GARUDA_PURAN,
                expectedPayloadClass = AynvoraEventPayload.GarudaTopicPayload::class,
                analyticsEventName = "garuda_puran_topic_opened",
            )
        )

        // ── Reports ────────────────────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "report.generate_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.ReportGeneratePayload::class,
                analyticsEventName = "report_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "report.pdf.download_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.ReportPdfPayload::class,
                analyticsEventName = "report_pdf_generated",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "report.share.clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.ReportSharePayload::class,
                analyticsEventName = "report_shared",
            )
        )

        // ── Generic Shell Commands ──────────────────────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "app.sheet.dismiss",
                eventType = AynvoraEventType.CLOSE,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "app.dialog.dismiss",
                eventType = AynvoraEventType.CLOSE,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "app.navigate",
                eventType = AynvoraEventType.NAVIGATE,
                allowedFeature = null,
                expectedPayloadClass = AynvoraEventPayload.NavigationPayload::class,
            )
        )

        // ── Numerology Module (Phase 10.0 & 10.4) ───────────────────────────
        register(
            RegisteredEventDefinition(
                eventId = "dashboard.numerology.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.FeatureOpenPayload::class,
                analyticsEventName = "numerology_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.calculate_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyCalculatePayload::class,
                defaultIdempotencyPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
                analyticsEventName = "numerology_calculation_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ruleset_selected",
                eventType = AynvoraEventType.SELECT,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyRulesetSelectPayload::class,
                analyticsEventName = "numerology_ruleset_selected",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.calculation_completed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyResultViewPayload::class,
                analyticsEventName = "numerology_calculation_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.calculation_failed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyCalculationFailedPayload::class,
                analyticsEventName = "numerology_calculation_failed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.result_viewed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyResultViewPayload::class,
                analyticsEventName = "numerology_result_viewed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.trace_viewed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyTraceViewPayload::class,
                analyticsEventName = "numerology_trace_viewed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.method_compared",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyComparePayload::class,
                analyticsEventName = "numerology_method_compared",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.report_requested",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyReportRequestPayload::class,
                analyticsEventName = "numerology_report_requested",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.history_opened",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyHistoryOpenPayload::class,
                analyticsEventName = "numerology_history_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.close_clicked",
                eventType = AynvoraEventType.CLOSE,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.Empty::class,
            )
        )

        // Numerology AI Conversational Events (Phase 10.6)
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.open_clicked",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiOpenPayload::class,
                analyticsEventName = "numerology_ai_opened",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.question_submitted",
                eventType = AynvoraEventType.CLICK,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload::class,
                analyticsEventName = "numerology_ai_question_submitted",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.explanation_started",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiExplanationStartedPayload::class,
                analyticsEventName = "numerology_ai_explanation_started",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.explanation_completed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiExplanationCompletedPayload::class,
                analyticsEventName = "numerology_ai_explanation_completed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.explanation_failed",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiExplanationFailedPayload::class,
                analyticsEventName = "numerology_ai_explanation_failed",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.fallback_used",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiFallbackUsedPayload::class,
                analyticsEventName = "numerology_ai_fallback_used",
            )
        )
        register(
            RegisteredEventDefinition(
                eventId = "numerology.ai.clarification_requested",
                eventType = AynvoraEventType.FEATURE_ACTION,
                allowedFeature = CoreFeatureId.NUMEROLOGY,
                expectedPayloadClass = AynvoraEventPayload.NumerologyAiClarificationPayload::class,
                analyticsEventName = "numerology_ai_clarification_requested",
            )
        )
    }

    fun register(definition: RegisteredEventDefinition) {
        definitions[definition.eventId] = definition
    }

    fun find(eventId: String): RegisteredEventDefinition? = definitions[eventId]

    fun isRegistered(eventId: String): Boolean = definitions.containsKey(eventId)

    fun getAll(): Map<String, RegisteredEventDefinition> = definitions.toMap()
}
