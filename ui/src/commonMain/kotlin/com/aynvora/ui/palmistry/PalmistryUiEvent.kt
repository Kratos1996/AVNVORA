package com.aynvora.ui.palmistry

import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmImageSource
import com.aynvora.core.palmistry.PalmReadingSession

/**
 * Strongly typed UI events emitted by the Palmistry experience.
 */
sealed class PalmistryUiEvent(
    eventId: String,
    screenId: String = "palmistry",
    componentId: String = "palmistry_ui",
    payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
) : AynvoraClickEvent(
    eventId = eventId,
    screenId = screenId,
    componentId = componentId,
    payload = payload,
) {
    data object ScreenOpened : PalmistryUiEvent("palmistry.screen.opened")

    data object DisclaimerAccepted : PalmistryUiEvent("palmistry.disclaimer.viewed")

    data class HandSelected(val hand: HandType) : PalmistryUiEvent(
        eventId = "palmistry.hand.selected",
        payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
    )

    data class ImageSelected(val imageSource: PalmImageSource) : PalmistryUiEvent(
        eventId = "palmistry.image.selected",
        payload = AynvoraEventPayload.PalmImageSourcePayload(imageSource.sourceType.name),
    )

    data class StartAnalysis(val hand: HandType) : PalmistryUiEvent(
        eventId = "palmistry.analysis.started",
        payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
    )

    data class AnalysisRetry(val hand: HandType) : PalmistryUiEvent(
        eventId = "palmistry.analysis.started",
        payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
    )

    data class QuestionSubmitted(val questionText: String) : PalmistryUiEvent(
        eventId = "palmistry.question.submitted",
        payload = AynvoraEventPayload.PalmQuestionPayload(questionText),
    )

    data class AnalysisCompleted(val hand: HandType) : PalmistryUiEvent(
        eventId = "palmistry.analysis.completed",
        payload = AynvoraEventPayload.PalmSelectHandPayload(hand.name),
    )

    data class SaveSession(val session: PalmReadingSession) : PalmistryUiEvent(
        eventId = "palmistry.session.saved",
        payload = AynvoraEventPayload.PalmSaveSessionPayload(session.id),
    )

    data class RecordFeatureFeedback(val feedback: PalmFeatureFeedback) : PalmistryUiEvent(
        eventId = "palmistry.feedback.feature_recorded",
        payload = AynvoraEventPayload.PalmFeatureFeedbackPayload(
            sessionId = feedback.readingId,
            featureType = feedback.featureType,
            category = feedback.category.name,
        ),
    )

    data class RecordAnswerFeedback(val feedback: PalmAnswerFeedback) : PalmistryUiEvent(
        eventId = "palmistry.feedback.answer_recorded",
        payload = AynvoraEventPayload.PalmAnswerFeedbackPayload(
            sessionId = feedback.readingId,
            questionId = feedback.questionId,
            isHelpful = feedback.isHelpful,
        ),
    )

    data class FeedbackSubmitted(val starRating: Int, val comment: String? = null) :
        PalmistryUiEvent(
            eventId = "palmistry.feedback.submitted",
            payload = AynvoraEventPayload.PalmFeedbackPayload(starRating, comment),
        )

    data class SelectPastSession(val sessionId: String) :
        PalmistryUiEvent("palmistry.session.selected")

    data object ReportRequested : PalmistryUiEvent(
        eventId = "report.generate_clicked",
        payload = AynvoraEventPayload.ReportGeneratePayload("palmistry"),
    )

    data object PdfRequested : PalmistryUiEvent(
        eventId = "report.pdf.download_clicked",
        payload = AynvoraEventPayload.ReportPdfPayload("palmistry"),
    )

    data object ShareRequested : PalmistryUiEvent(
        eventId = "report.share.clicked",
        payload = AynvoraEventPayload.ReportSharePayload("palmistry"),
    )

    data object CloseClicked : PalmistryUiEvent(
        eventId = "palmistry.close_clicked",
        componentId = "close_button",
    )

    data object BackClicked : PalmistryUiEvent(
        eventId = "palmistry.back_clicked",
        componentId = "back_button",
    )
}
