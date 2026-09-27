# AYNVORA ViewModel Event Handling Pattern

Version: 1.0 (Phase 9.0)

## 1. Contract Overview

All ViewModels handling user interactions extend `AynvoraBaseViewModel<Event, State, Effect>`:

```kotlin
open class AynvoraBaseViewModel<Event : AynvoraEvent, State, Effect : AynvoraEffect>(
    initialState: State,
    protected val eventDispatcher: AynvoraEventDispatcher? = null,
) : ViewModel() {
    val uiState: StateFlow<State>
    val effects: Flow<Effect>

    fun onEvent(event: Event) {
        viewModelScope.launch {
            // Optional registration with event pipeline
            eventDispatcher?.dispatch(event)
            // Private execution in ViewModel
            handleEvent(event)
        }
    }

    protected abstract suspend fun handleEvent(event: Event)
    protected fun updateState(reducer: State.() -> State)
    protected suspend fun emitEffect(effect: Effect)
}
```

## 2. Key Invariants

- **Public API**: Only `uiState`, `effects`, and `onEvent(event)` are exposed to the UI.
- **Private Handler**: `handleEvent` is protected/private, ensuring UI cannot execute business
  methods directly.
- **Pure State Transitions**: `updateState` transforms state immutably.
- **Buffered Effects**: Effects use `Channel.BUFFERED` and `receiveAsFlow()`, guaranteeing single
  delivery for navigation and one-shot commands.
