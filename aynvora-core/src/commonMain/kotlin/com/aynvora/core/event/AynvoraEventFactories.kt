package com.aynvora.core.event

import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.palmistry.HandType

/**
 * Type-safe event factories preventing manual errors in constructing event metadata, IDs, and payloads.
 */
object DashboardEvents {
    fun openFeature(featureId: CoreFeatureId, screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.feature.open_clicked",
            screenId = screenId,
            componentId = "feature_card_${featureId.name.lowercase()}",
            payload = AynvoraEventPayload.FeatureOpenPayload(featureId),
        )

    fun toggleTheme(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.theme.toggle_clicked",
            screenId = screenId,
            componentId = "theme_toggle",
            payload = AynvoraEventPayload.Empty,
        )

    fun openLanguagePicker(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.language.open_clicked",
            screenId = screenId,
            componentId = "language_selector",
            payload = AynvoraEventPayload.Empty,
        )

    fun changeLanguage(localeId: String, screenId: String = "language_picker"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.language.change_clicked",
            screenId = screenId,
            componentId = "language_row_$localeId",
            payload = AynvoraEventPayload.LanguageChangePayload(localeId),
        )
}

object AiEvents {
    fun downloadModel(modelId: String, screenId: String = "ai_setup"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.ai.download_clicked",
            screenId = screenId,
            componentId = "download_ai_button",
            payload = AynvoraEventPayload.AiDownloadPayload(modelId),
        )

    fun cancelDownload(modelId: String = "", screenId: String = "ai_setup"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.ai.cancel_clicked",
            screenId = screenId,
            componentId = "cancel_ai_button",
            payload = AynvoraEventPayload.AiCancelPayload(modelId),
        )

    fun deleteModel(modelId: String = "", screenId: String = "ai_setup"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.ai.delete_clicked",
            screenId = screenId,
            componentId = "delete_ai_button",
            payload = AynvoraEventPayload.AiDeletePayload(modelId),
        )

    fun retryModel(modelId: String = "", screenId: String = "ai_setup"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.ai.retry_clicked",
            screenId = screenId,
            componentId = "retry_ai_button",
            payload = AynvoraEventPayload.AiRetryPayload(modelId),
        )

    fun openAi(screenId: String = "ai_system_details"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "ai.opened",
            screenId = screenId,
            componentId = "open_ai_button",
            payload = AynvoraEventPayload.AiOpenedPayload(screenId),
        )

    fun modelStatusViewed(
        modelId: String?,
        status: String,
        screenId: String = "ai_system_details"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.model_status_viewed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "system_details",
            payload = AynvoraEventPayload.AiModelStatusViewedPayload(modelId, status),
        )

    fun downloadStarted(modelId: String, screenId: String = "ai_system_details"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.model_download_started",
            eventType = AynvoraEventType.DOWNLOAD,
            screenId = screenId,
            componentId = "download_progress",
            payload = AynvoraEventPayload.AiDownloadStartedPayload(modelId),
        )

    fun downloadCompleted(
        modelId: String,
        sizeBytes: Long,
        screenId: String = "ai_system_details"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.model_download_completed",
            eventType = AynvoraEventType.DOWNLOAD,
            screenId = screenId,
            componentId = "download_progress",
            payload = AynvoraEventPayload.AiDownloadCompletedPayload(modelId, sizeBytes),
        )

    fun verificationCompleted(
        modelId: String,
        isSuccess: Boolean,
        screenId: String = "ai_system_details"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.model_verification_completed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "checksum_verifier",
            payload = AynvoraEventPayload.AiVerificationCompletedPayload(modelId, isSuccess),
        )

    fun inferenceStarted(
        featureId: String,
        requestId: String,
        screenId: String = "ai_inference"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.inference_started",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_runtime",
            payload = AynvoraEventPayload.AiInferenceStartedPayload(featureId, requestId),
        )

    fun inferenceCompleted(
        featureId: String,
        requestId: String,
        executionMode: String,
        latencyMs: Long,
        screenId: String = "ai_inference"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.inference_completed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_runtime",
            payload = AynvoraEventPayload.AiInferenceCompletedPayload(
                featureId,
                requestId,
                executionMode,
                latencyMs
            ),
        )

    fun fallbackUsed(
        featureId: String,
        reason: String,
        screenId: String = "ai_inference"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.fallback_used",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "fallback_engine",
            payload = AynvoraEventPayload.AiFallbackUsedPayload(featureId, reason),
        )

    fun validationFailed(
        featureId: String,
        reason: String,
        screenId: String = "ai_inference"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "ai.validation_failed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "output_validator",
            payload = AynvoraEventPayload.AiValidationFailedPayload(featureId, reason),
        )
}

object TarotEvents {
    fun open(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.tarot.open_clicked",
            screenId = screenId,
            componentId = "feature_card_tarot",
            payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.TAROT),
        )

    fun acceptDisclaimer(screenId: String = "tarot_disclaimer"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.disclaimer.accepted",
            screenId = screenId,
            componentId = "accept_disclaimer_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun selectDeck(deckId: String, screenId: String = "tarot_home"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.deck.selected",
            eventType = AynvoraEventType.SELECT,
            screenId = screenId,
            componentId = "deck_selector",
            payload = AynvoraEventPayload.TarotSelectDeckPayload(deckId),
        )

    fun selectSpread(spreadId: String, screenId: String = "tarot_home"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.spread.selected",
            eventType = AynvoraEventType.SELECT,
            screenId = screenId,
            componentId = "spread_selector",
            payload = AynvoraEventPayload.TarotSelectSpreadPayload(spreadId),
        )

    fun drawCard(position: Int, screenId: String = "tarot_draw"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.card.draw_clicked",
            screenId = screenId,
            componentId = "card_slot_$position",
            payload = AynvoraEventPayload.TarotDrawCardPayload(position),
        )

    fun revealCard(screenId: String = "tarot_reveal"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.card.reveal_clicked",
            screenId = screenId,
            componentId = "reveal_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun submitQuestion(questionText: String, screenId: String = "tarot_reading"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.question.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "question_input",
            payload = AynvoraEventPayload.TarotQuestionPayload(questionText),
        )

    fun submitFeedback(
        rating: Int,
        optionalText: String? = null,
        screenId: String = "tarot_feedback"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "feedback_dialog",
            payload = AynvoraEventPayload.TarotFeedbackPayload(rating, optionalText),
        )

    fun openHistory(screenId: String = "tarot_home"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.history.opened",
            screenId = screenId,
            componentId = "history_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun close(screenId: String = "tarot"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.close_clicked",
            screenId = screenId,
            componentId = "close_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun screenOpened(screenId: String = "tarot"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.screen.opened",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "screen",
            payload = AynvoraEventPayload.Empty,
        )

    fun answerFeedback(rating: Int, screenId: String = "tarot_reading"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.answer_feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "answer_feedback",
            payload = AynvoraEventPayload.TarotFeedbackPayload(rating),
        )

    fun clarificationDrawn(
        cardId: String,
        orientation: String,
        screenId: String = "tarot_reading"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.clarification.drawn",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "clarification_card",
            payload = AynvoraEventPayload.TarotClarificationPayload(cardId, orientation),
        )

    fun cardFeedback(
        cardId: String,
        helpful: Boolean,
        screenId: String = "tarot_reading"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.card.feedback_submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "card_feedback",
            payload = AynvoraEventPayload.TarotCardFeedbackPayload(cardId, helpful),
        )

    fun readingLockShown(remainingDuration: String, screenId: String = "tarot"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.reading.lock_shown",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "lock_dialog",
            payload = AynvoraEventPayload.TarotLockPayload(remainingDuration),
        )

    fun contentOpened(
        cardId: String,
        language: String,
        screenId: String = "tarot"
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.content.opened",
            screenId = screenId,
            componentId = "card_detail",
            payload = AynvoraEventPayload.TarotContentPayload(cardId, language),
        )

    fun aiAnswerGenerated(
        modelId: String? = null,
        promptVersion: String? = null,
        fallbackUsed: Boolean = false,
        language: String = "en",
        screenId: String = "tarot_reading"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "tarot.ai.answer_generated",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "ai_answer",
            payload = AynvoraEventPayload.TarotAiAnswerPayload(
                modelId,
                promptVersion,
                fallbackUsed,
                language
            ),
        )

    fun back(screenId: String = "tarot"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "tarot.back_clicked",
            screenId = screenId,
            componentId = "back_button",
            payload = AynvoraEventPayload.Empty,
        )
}

object PalmistryEvents {
    fun open(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.palmistry.open_clicked",
            screenId = screenId,
            componentId = "feature_card_palmistry",
            payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.PALMISTRY),
        )

    fun acceptDisclaimer(screenId: String = "palmistry_disclaimer"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "palmistry.disclaimer.accepted",
            screenId = screenId,
            componentId = "accept_disclaimer_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun selectHand(hand: HandType, screenId: String = "palmistry_home"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.hand.selected",
            eventType = AynvoraEventType.SELECT,
            screenId = screenId,
            componentId = "hand_selector",
            payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
        )

    fun selectImageSource(source: String, screenId: String = "palmistry_home"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "palmistry.image.selected",
            screenId = screenId,
            componentId = "image_source_$source",
            payload = AynvoraEventPayload.PalmImageSourcePayload(source),
        )

    fun startAnalysis(hand: HandType, screenId: String = "palmistry_home"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "palmistry.analysis.started",
            screenId = screenId,
            componentId = "start_analysis_button",
            payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
        )

    fun submitQuestion(
        questionText: String,
        targetLine: String? = null,
        screenId: String = "palmistry_reading"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.question.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "question_input",
            payload = AynvoraEventPayload.PalmQuestionPayload(questionText, targetLine),
        )

    fun submitFeedback(
        rating: Int,
        comment: String? = null,
        screenId: String = "palmistry_feedback"
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "feedback_dialog",
            payload = AynvoraEventPayload.PalmFeedbackPayload(rating, comment),
        )

    fun screenOpened(screenId: String = "palmistry"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.screen.opened",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "screen",
            payload = AynvoraEventPayload.Empty,
        )

    fun saveSession(sessionId: String, screenId: String = "palmistry"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.session.saved",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "session_manager",
            payload = AynvoraEventPayload.PalmSaveSessionPayload(sessionId),
        )

    fun recordFeatureFeedback(
        sessionId: String,
        featureType: String,
        category: String,
        screenId: String = "palmistry_result",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.feedback.feature_recorded",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "feature_feedback",
            payload = AynvoraEventPayload.PalmFeatureFeedbackPayload(
                sessionId,
                featureType,
                category
            ),
        )

    fun recordAnswerFeedback(
        sessionId: String,
        questionId: String,
        isHelpful: Boolean,
        screenId: String = "palmistry_result",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "palmistry.feedback.answer_recorded",
            eventType = AynvoraEventType.SUBMIT,
            screenId = screenId,
            componentId = "answer_feedback",
            payload = AynvoraEventPayload.PalmAnswerFeedbackPayload(
                sessionId,
                questionId,
                isHelpful
            ),
        )

    fun close(screenId: String = "palmistry"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "palmistry.close_clicked",
            screenId = screenId,
            componentId = "close_button",
            payload = AynvoraEventPayload.Empty,
        )

    fun back(screenId: String = "palmistry"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "palmistry.back_clicked",
            screenId = screenId,
            componentId = "back_button",
            payload = AynvoraEventPayload.Empty,
        )
}

object GarudaPuranEvents {
    fun screenOpened(screenId: String = "garuda_puran"): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "garuda_puran.screen.opened",
            eventType = AynvoraEventType.CUSTOM,
            screenId = screenId,
            componentId = "screen",
            payload = AynvoraEventPayload.Empty,
        )

    fun topicSelected(topicId: String, screenId: String = "garuda_puran"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "garuda_puran.topic.selected",
            screenId = screenId,
            componentId = "topic_card_$topicId",
            payload = AynvoraEventPayload.GarudaTopicPayload(topicId),
        )
}

object NumerologyEvents {
    fun open(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.numerology.open_clicked",
            screenId = screenId,
            componentId = "feature_card_numerology",
            payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.NUMEROLOGY),
        )

    fun calculate(
        rulesetId: String,
        hasName: Boolean,
        hasTargetYear: Boolean,
        screenId: String = "numerology_screen",
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "numerology.calculate_clicked",
            screenId = screenId,
            componentId = "calculate_numerology_button",
            payload = AynvoraEventPayload.NumerologyCalculatePayload(
                rulesetId = rulesetId,
                hasName = hasName,
                hasTargetYear = hasTargetYear,
            ),
        )

    fun aiOpen(rulesetId: String, screenId: String = "numerology_screen"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "numerology.ai.open_clicked",
            screenId = screenId,
            componentId = "explain_ai_button",
            payload = AynvoraEventPayload.NumerologyAiOpenPayload(rulesetId),
        )

    fun aiQuestionSubmitted(
        rulesetId: String,
        category: String,
        screenId: String = "numerology_ai_screen",
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "numerology.ai.question_submitted",
            screenId = screenId,
            componentId = "submit_question_button",
            payload = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload(rulesetId, category),
        )

    fun aiExplanationStarted(
        rulesetId: String,
        category: String,
        screenId: String = "numerology_ai_screen",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "numerology.ai.explanation_started",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationStartedPayload(
                rulesetId,
                category
            ),
        )

    fun aiExplanationCompleted(
        rulesetId: String,
        category: String,
        fallbackUsed: Boolean,
        modelId: String? = null,
        screenId: String = "numerology_ai_screen",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "numerology.ai.explanation_completed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationCompletedPayload(
                rulesetId = rulesetId,
                category = category,
                fallbackUsed = fallbackUsed,
                modelId = modelId,
            ),
        )

    fun aiExplanationFailed(
        rulesetId: String,
        errorCode: String,
        screenId: String = "numerology_ai_screen",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "numerology.ai.explanation_failed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationFailedPayload(
                rulesetId,
                errorCode
            ),
        )

    fun aiFallbackUsed(
        rulesetId: String,
        reason: String,
        screenId: String = "numerology_ai_screen",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "numerology.ai.fallback_used",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_engine",
            payload = AynvoraEventPayload.NumerologyAiFallbackUsedPayload(rulesetId, reason),
        )

    fun aiClarificationRequested(
        rulesetId: String,
        category: String,
        screenId: String = "numerology_ai_screen",
    ): AynvoraUiEvent =
        AynvoraUiEvent(
            eventId = "numerology.ai.clarification_requested",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = screenId,
            componentId = "ai_engine",
            payload = AynvoraEventPayload.NumerologyAiClarificationPayload(rulesetId, category),
        )
}

object GemstoneEvents {
    fun open(screenId: String = "dashboard"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "dashboard.gemstone.open_clicked",
            screenId = screenId,
            componentId = "feature_card_gemstone",
            payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.GEMSTONE),
        )

    fun selectGemstone(
        gemstoneType: String,
        screenId: String = "gemstone_catalog"
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.catalog.selected",
            screenId = screenId,
            componentId = "gemstone_card_$gemstoneType",
        )

    fun addInventoryItem(
        gemstoneType: String,
        screenId: String = "gemstone_inventory"
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.inventory.added",
            screenId = screenId,
            componentId = "add_gemstone_button",
        )

    fun removeInventoryItem(
        gemstoneType: String,
        screenId: String = "gemstone_inventory"
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.inventory.removed",
            screenId = screenId,
            componentId = "remove_gemstone_button",
        )

    fun checkCompatibility(
        gemstoneType: String,
        screenId: String = "gemstone_compatibility"
    ): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.compatibility.checked",
            screenId = screenId,
            componentId = "check_compatibility_button",
        )

    fun generateRecommendation(screenId: String = "gemstone_recommendations"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.recommendation.generated",
            screenId = screenId,
            componentId = "generate_recommendations_button",
        )

    fun openCamera(screenId: String = "gemstone_certificate"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.certificate.camera_opened",
            screenId = screenId,
            componentId = "certificate_camera_button",
        )

    fun openGallery(screenId: String = "gemstone_certificate"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.certificate.gallery_opened",
            screenId = screenId,
            componentId = "certificate_gallery_button",
        )

    fun runInspection(screenId: String = "gemstone_certificate"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.certificate.inspected",
            screenId = screenId,
            componentId = "inspect_certificate_button",
        )

    fun generateReport(screenId: String = "gemstone_report"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.report.generated",
            screenId = screenId,
            componentId = "generate_report_button",
        )

    fun close(screenId: String = "gemstone"): AynvoraClickEvent =
        AynvoraClickEvent(
            eventId = "gemstone.close_clicked",
            screenId = screenId,
            componentId = "close_button",
        )
}

