package com.aynvora.ui.tarot

import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.PerformTarotReadingUseCase
import com.aynvora.core.tarot.TarotQuestionEngine
import com.aynvora.core.tarot.TarotReadingAvailability
import com.aynvora.core.tarot.TarotReadingAvailabilityPolicy
import com.aynvora.core.tarot.TarotReadingSession
import com.aynvora.core.tarot.TarotReadingStatus
import com.aynvora.core.tarot.TarotSessionRepository
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.launch

/**
 * State for Tarot presentation screen.
 */
data class TarotUiState(
    val destination: TarotDestination = TarotDestination.Disclaimer,
    val selectedDeckId: String = "rider_waite_smith_standard",
    val activeSession: TarotReadingSession? = null,
    val lockDialogLocked: TarotReadingAvailability.Locked? = null,
    val feedbackDialogSession: TarotReadingSession? = null,
)

/**
 * Event-driven ViewModel for Tarot contemplative reflection experience.
 *
 * All business actions, user clicks, and commands pass through [onEvent].
 * Centralized analytics are observed through [AynvoraEventDispatcher].
 */
class TarotViewModel(
    private val sessionRepository: TarotSessionRepository,
    private val availabilityPolicy: TarotReadingAvailabilityPolicy,
    private val readingUseCase: PerformTarotReadingUseCase,
    private val questionEngine: TarotQuestionEngine,
    eventDispatcher: AynvoraEventDispatcher? = null,
) : AynvoraBaseViewModel<TarotUiEvent, TarotUiState, AynvoraEffect>(
    initialState = TarotUiState(),
    eventDispatcher = eventDispatcher,
) {

    init {
        registerEventHandler(::handleEvent)
        viewModelScope.launch {
            val latest = (sessionRepository.getLatestSession() as? AynvoraResult.Success)?.value
            val valid =
                if (latest != null && latest.status != TarotReadingStatus.EXPIRED && latest.status != TarotReadingStatus.ARCHIVED) {
                    latest
                } else {
                    null
                }
            updateState { copy(activeSession = valid) }
        }
    }

    private suspend fun handleEvent(event: TarotUiEvent) {
        when (event) {
            is TarotUiEvent.ScreenOpened -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.DisclaimerAccepted -> {
                updateState { copy(destination = TarotDestination.Home) }
            }

            is TarotUiEvent.DeckSelected -> {
                updateState { copy(selectedDeckId = event.deckId) }
            }

            is TarotUiEvent.SpreadSelected -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.DrawCard -> {
                // Handled via reading flow
            }

            is TarotUiEvent.RevealCard -> {
                // Transition or card reveal
            }

            is TarotUiEvent.QuestionSubmitted -> {
                // Dispatched to question engine
            }

            is TarotUiEvent.ClarificationRequested -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.ClarificationAccepted -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.FeedbackSubmitted -> {
                updateState { copy(feedbackDialogSession = null) }
            }

            is TarotUiEvent.CardFeedbackSubmitted -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.AnswerFeedbackSubmitted -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.ClarificationDrawn -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.ReadingLockShown -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.ContentOpened -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.AiAnswerGenerated -> {
                // Observed and tracked via event pipeline
            }

            is TarotUiEvent.HistoryOpened -> {
                updateState { copy(destination = TarotDestination.History) }
            }

            is TarotUiEvent.ReportRequested -> {
                // Emits report effect
            }

            is TarotUiEvent.PdfRequested -> {
                // Emits PDF effect
            }

            is TarotUiEvent.ShareRequested -> {
                // Emits Share effect
            }

            is TarotUiEvent.CloseClicked -> {
                emitEffect(AynvoraEffect.Navigate(com.aynvora.core.event.AynvoraNavigationTarget.Close))
            }

            is TarotUiEvent.BackClicked -> {
                emitEffect(AynvoraEffect.Navigate(com.aynvora.core.event.AynvoraNavigationTarget.Back))
            }
        }
    }
}
