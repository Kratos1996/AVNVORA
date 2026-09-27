package com.aynvora.ui.tarot

import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEventPayload

/**
 * Structured typed UI events for Tarot user interactions.
 */
sealed class TarotUiEvent(
    eventId: String,
    screenId: String = "tarot",
    componentId: String = "ui",
    payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
) : AynvoraClickEvent(
    eventId = eventId,
    screenId = screenId,
    componentId = componentId,
    payload = payload,
) {
    data object ScreenOpened : TarotUiEvent("tarot.screen.opened", componentId = "screen")
    data object DisclaimerAccepted :
        TarotUiEvent("tarot.disclaimer.viewed", componentId = "accept_disclaimer")

    data class DeckSelected(val deckId: String) : TarotUiEvent(
        "tarot.deck.selected",
        componentId = "deck_selector",
        payload = AynvoraEventPayload.TarotSelectDeckPayload(deckId),
    )

    data class SpreadSelected(val spreadId: String) : TarotUiEvent(
        "tarot.spread.selected",
        componentId = "spread_selector",
        payload = AynvoraEventPayload.TarotSelectSpreadPayload(spreadId),
    )

    data class DrawCard(val position: Int) : TarotUiEvent(
        "tarot.card.draw_clicked",
        componentId = "draw_card_button",
        payload = AynvoraEventPayload.TarotDrawCardPayload(position),
    )

    data object RevealCard :
        TarotUiEvent("tarot.card.reveal_clicked", componentId = "reveal_button")

    data class QuestionSubmitted(val questionText: String) : TarotUiEvent(
        "tarot.question.submitted",
        componentId = "question_input",
        payload = AynvoraEventPayload.TarotQuestionPayload(questionText),
    )

    data class ClarificationRequested(val cardId: String = "", val reason: String = "") :
        TarotUiEvent(
            "tarot.clarification.requested",
            componentId = "clarification_button",
            payload = AynvoraEventPayload.TarotClarificationPayload(cardId, reason),
        )

    data object ClarificationAccepted :
        TarotUiEvent("tarot.clarification.accepted", componentId = "clarification_accept")

    data class FeedbackSubmitted(val starRating: Int, val optionalText: String? = null) :
        TarotUiEvent(
            "tarot.feedback.submitted",
            componentId = "feedback_form",
            payload = AynvoraEventPayload.TarotFeedbackPayload(starRating, optionalText),
        )

    data class CardFeedbackSubmitted(val cardId: String, val helpful: Boolean) : TarotUiEvent(
        "tarot.card.feedback_submitted",
        componentId = "card_feedback",
        payload = AynvoraEventPayload.TarotCardFeedbackPayload(cardId, helpful),
    )

    data class AnswerFeedbackSubmitted(val starRating: Int) : TarotUiEvent(
        "tarot.answer_feedback.submitted",
        componentId = "answer_feedback",
        payload = AynvoraEventPayload.TarotFeedbackPayload(starRating),
    )

    data class ClarificationDrawn(val cardId: String, val orientation: String) : TarotUiEvent(
        "tarot.clarification.drawn",
        componentId = "clarification_card",
        payload = AynvoraEventPayload.TarotClarificationPayload(cardId, orientation),
    )

    data class ReadingLockShown(val remainingDuration: String) : TarotUiEvent(
        "tarot.reading.lock_shown",
        componentId = "lock_dialog",
        payload = AynvoraEventPayload.TarotLockPayload(remainingDuration),
    )

    data class ContentOpened(val cardId: String, val language: String = "en") : TarotUiEvent(
        "tarot.content.opened",
        componentId = "card_detail",
        payload = AynvoraEventPayload.TarotContentPayload(cardId, language),
    )

    data class AiAnswerGenerated(
        val modelId: String? = null,
        val promptVersion: String? = null,
        val fallbackUsed: Boolean = false,
        val language: String = "en",
    ) : TarotUiEvent(
        "tarot.ai.answer_generated",
        componentId = "ai_answer",
        payload = AynvoraEventPayload.TarotAiAnswerPayload(
            modelId,
            promptVersion,
            fallbackUsed,
            language
        ),
    )

    data object HistoryOpened : TarotUiEvent("tarot.history.opened", componentId = "history_button")

    data class ReportRequested(val reportTypeId: String = "tarot") : TarotUiEvent(
        "report.generate_clicked",
        componentId = "report_button",
        payload = AynvoraEventPayload.ReportGeneratePayload(reportTypeId),
    )

    data class PdfRequested(val reportTypeId: String = "tarot") : TarotUiEvent(
        "report.pdf.download_clicked",
        componentId = "pdf_button",
        payload = AynvoraEventPayload.ReportPdfPayload(reportTypeId),
    )

    data class ShareRequested(val reportTypeId: String = "tarot") : TarotUiEvent(
        "report.share.clicked",
        componentId = "share_button",
        payload = AynvoraEventPayload.ReportSharePayload(reportTypeId),
    )

    data object CloseClicked : TarotUiEvent("tarot.close_clicked", componentId = "close_button")
    data object BackClicked : TarotUiEvent("tarot.back_clicked", componentId = "back_button")
}
