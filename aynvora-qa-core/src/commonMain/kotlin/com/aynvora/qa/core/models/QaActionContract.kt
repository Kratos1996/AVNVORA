package com.aynvora.qa.core.models

import kotlinx.serialization.Serializable

/**
 * Expected observable outcomes for an action.
 * Allows multi-outcome contracts (e.g. submit can lead to success navigation OR validation error).
 */
@Serializable
sealed interface QaExpectedOutcome {
    @Serializable
    data class NavigationChanged(val targetRoute: String? = null) : QaExpectedOutcome

    @Serializable
    data class BottomSheetStateChanged(val open: Boolean) : QaExpectedOutcome

    @Serializable
    data class DialogVisible(val open: Boolean) : QaExpectedOutcome

    @Serializable
    data class StateChanged(val property: String? = null) : QaExpectedOutcome

    @Serializable
    data class EventEmitted(val eventName: String) : QaExpectedOutcome

    @Serializable
    data object LoadingThenSuccess : QaExpectedOutcome

    @Serializable
    data object SnackbarShown : QaExpectedOutcome

    @Serializable
    data object ExternalActionStarted : QaExpectedOutcome

    @Serializable
    data object NoVisibleChangeAllowed : QaExpectedOutcome
}

/**
 * Optional but strongly recommended contract for testable UI controls.
 */
@Serializable
data class QaActionContract(
    val actionId: QaActionId,
    val expectedOutcomes: List<QaExpectedOutcome>,
    val maxDurationMs: Long = 3000L,
    val allowedNoOp: Boolean = false,
) {
    constructor(actionId: QaActionId, outcome: QaExpectedOutcome, maxDurationMs: Long = 3000L) :
            this(actionId, listOf(outcome), maxDurationMs)

    fun matchesAny(transition: QaStateTransition): Boolean {
        if (allowedNoOp && transition.isIdenticalState) return true

        return expectedOutcomes.any { outcome ->
            when (outcome) {
                is QaExpectedOutcome.NavigationChanged -> {
                    transition.routeChanged && (outcome.targetRoute == null || transition.postState.route == outcome.targetRoute)
                }

                is QaExpectedOutcome.BottomSheetStateChanged -> {
                    transition.sheetStateChanged && (transition.postState.isSheetOpen == outcome.open)
                }

                is QaExpectedOutcome.DialogVisible -> {
                    transition.dialogStateChanged && (transition.postState.isDialogOpen == outcome.open)
                }

                is QaExpectedOutcome.StateChanged -> {
                    !transition.isIdenticalState || transition.eventsObserved.isNotEmpty()
                }

                is QaExpectedOutcome.EventEmitted -> {
                    transition.eventsObserved.any {
                        it.contains(
                            outcome.eventName,
                            ignoreCase = true
                        )
                    }
                }

                is QaExpectedOutcome.LoadingThenSuccess -> {
                    transition.observedLoading && !transition.postState.isLoading && !transition.postState.hasError
                }

                is QaExpectedOutcome.SnackbarShown -> {
                    transition.snackbarObserved
                }

                is QaExpectedOutcome.ExternalActionStarted -> {
                    true // external actions may not produce local UI delta
                }

                is QaExpectedOutcome.NoVisibleChangeAllowed -> {
                    true
                }
            }
        }
    }
}
