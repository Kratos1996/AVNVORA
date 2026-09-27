package com.aynvora.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEvent
import com.aynvora.core.event.AynvoraEventDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel adhering to AYNVORA Clean Architecture.
 *
 * Responsibilities:
 * - Expose persistent screen state as [StateFlow]
 * - Accept user interaction [Event]s through [onEvent]
 * - Delegate to protected [handleEvent] (preventing direct UI mutation)
 * - Emit transient one-shot commands as [Effect]s via [effects]
 * - Optional dispatch of events through central [AynvoraEventDispatcher] for telemetry/observers
 */
abstract class AynvoraBaseViewModel<Event : AynvoraEvent, State, Effect : AynvoraEffect>(
    initialState: State,
    private val eventDispatcher: AynvoraEventDispatcher? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    protected val currentState: State get() = _uiState.value

    private var privateHandler: (suspend (Event) -> Unit)? = null

    /**
     * Registers the feature ViewModel's private handler function (e.g. `registerEventHandler(::handleEvent)`).
     * Accessible ONLY during subclass construction / initialization.
     * The business event handler itself remains strictly private inside the concrete feature ViewModel.
     */
    protected fun registerEventHandler(handler: suspend (Event) -> Unit) {
        this.privateHandler = handler
    }

    /**
     * Sole public interaction entry point for the UI.
     * UI callers can only invoke `viewModel.onEvent(event)` and observe `uiState` and `effects`.
     */
    fun onEvent(event: Event) {
        viewModelScope.launch {
            // Dispatch to central event pipeline (EventGuard, Deduplication, Analytics)
            eventDispatcher?.dispatch(event)
            // Execute private business handler
            try {
                privateHandler?.invoke(event)
            } catch (t: Throwable) {
                onError(t, event)
            }
        }
    }

    protected fun updateState(reducer: State.() -> State) {
        _uiState.value = _uiState.value.reducer()
    }

    protected fun emitEffect(effect: Effect) {
        _effects.trySend(effect)
    }

    protected open fun onError(throwable: Throwable, event: Event) {
        // Can be overridden by subclasses to handle errors cleanly without crashing
    }
}
