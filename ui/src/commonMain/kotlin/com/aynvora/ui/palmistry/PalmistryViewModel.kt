package com.aynvora.ui.palmistry

import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.event.AynvoraNavigationTarget
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmSessionRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.launch

/**
 * Observable UI state for Palmistry presentation.
 */
data class PalmistryUiState(
    val selectedHand: HandType = HandType.RIGHT,
    val currentSession: PalmReadingSession? = null,
    val pastSessions: List<PalmReadingSession> = emptyList(),
    val showFeedbackDialog: Boolean = false,
    val isLoading: Boolean = false,
)

/**
 * Event-driven ViewModel for Hastrekha / Palmistry experience.
 *
 * All user actions flow through [onEvent] to private [handleEvent].
 * State transitions and navigation effects are decoupled from UI composables.
 */
class PalmistryViewModel(
    private val repository: PalmSessionRepository,
    eventDispatcher: AynvoraEventDispatcher? = null,
) : AynvoraBaseViewModel<PalmistryUiEvent, PalmistryUiState, AynvoraEffect>(
    initialState = PalmistryUiState(),
    eventDispatcher = eventDispatcher,
) {

    init {
        registerEventHandler(::handleEvent)
        viewModelScope.launch {
            when (val res = repository.getAllSessions()) {
                is AynvoraResult.Success -> updateState { copy(pastSessions = res.value) }
                is AynvoraResult.Failure -> Unit
            }
        }
    }

    private suspend fun handleEvent(event: PalmistryUiEvent) {
        when (event) {
            is PalmistryUiEvent.DisclaimerAccepted -> {
                // Handled via event pipeline and UI destination
            }

            is PalmistryUiEvent.HandSelected -> {
                updateState { copy(selectedHand = event.hand) }
            }

            is PalmistryUiEvent.ImageSelected -> {
                // Handled in workflow
            }

            is PalmistryUiEvent.StartAnalysis -> {
                updateState { copy(isLoading = true) }
            }

            is PalmistryUiEvent.AnalysisRetry -> {
                updateState { copy(isLoading = true) }
            }

            is PalmistryUiEvent.QuestionSubmitted -> {
                // Q&A interaction processed
            }

            is PalmistryUiEvent.ScreenOpened -> {
                // Observed and tracked via event pipeline
            }

            is PalmistryUiEvent.AnalysisCompleted -> {
                updateState { copy(isLoading = false) }
            }

            is PalmistryUiEvent.SaveSession -> {
                repository.saveSession(event.session)
                updateState {
                    copy(
                        currentSession = event.session,
                        pastSessions = (pastSessions.filter { it.id != event.session.id } + event.session),
                    )
                }
            }

            is PalmistryUiEvent.RecordFeatureFeedback -> {
                repository.recordFeatureFeedback(event.feedback)
            }

            is PalmistryUiEvent.RecordAnswerFeedback -> {
                repository.recordAnswerFeedback(event.feedback)
            }

            is PalmistryUiEvent.FeedbackSubmitted -> {
                updateState { copy(showFeedbackDialog = false) }
            }

            is PalmistryUiEvent.SelectPastSession -> {
                val session = uiState.value.pastSessions.firstOrNull { it.id == event.sessionId }
                if (session != null) {
                    updateState { copy(currentSession = session) }
                    emitEffect(AynvoraEffect.Navigate(AynvoraNavigationTarget.PalmistryReading(event.sessionId)))
                }
            }

            is PalmistryUiEvent.ReportRequested -> {
                emitEffect(
                    AynvoraEffect.Navigate(
                        AynvoraNavigationTarget.FeatureDetail(
                            CoreFeatureId.PALMISTRY
                        )
                    )
                )
            }

            is PalmistryUiEvent.PdfRequested -> {
                emitEffect(AynvoraEffect.ShowSnackbar("PDF generation initiated"))
            }

            is PalmistryUiEvent.ShareRequested -> {
                emitEffect(AynvoraEffect.Share("AYNVORA Palmistry Report"))
            }

            is PalmistryUiEvent.CloseClicked -> {
                emitEffect(AynvoraEffect.Navigate(AynvoraNavigationTarget.Close))
            }

            is PalmistryUiEvent.BackClicked -> {
                emitEffect(AynvoraEffect.Navigate(AynvoraNavigationTarget.Back))
            }
        }
    }
}
