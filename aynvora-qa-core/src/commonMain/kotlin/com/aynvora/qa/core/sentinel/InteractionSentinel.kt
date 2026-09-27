package com.aynvora.qa.core.sentinel

import com.aynvora.qa.core.models.QaActionContract
import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaActionResult
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaConfidence
import com.aynvora.qa.core.models.QaFailureCapsule
import com.aynvora.qa.core.models.QaStateSnapshot
import com.aynvora.qa.core.models.QaStateTransition

/**
 * Pure runtime verification engine for UI actions.
 * Observes interactions, transitions, outcomes, and maintains diagnostic failure capsules.
 */
class InteractionSentinel(
    private val contracts: Map<QaActionId, QaActionContract> = emptyMap(),
) {
    private val detector = NoOpDetector(contracts)

    private data class ActiveAction(
        val actionId: QaActionId,
        val interactionType: String,
        val preState: QaStateSnapshot,
        val startTimeMs: Long,
        val observedEvents: MutableList<String> = mutableListOf(),
        var observedLoading: Boolean = false,
        var observedSnackbar: Boolean = false,
        var observedError: String? = null,
        var observedStackTrace: String? = null,
    )

    private val activeActions = mutableMapOf<String, ActiveAction>()
    private val actionHistory = mutableListOf<QaActionResult>()
    private val failureCapsules = mutableListOf<QaFailureCapsule>()
    private val visitedScreens = mutableSetOf<String>()
    private var failureCounter = 0

    fun startAction(
        actionId: QaActionId,
        interactionType: String = "TAP",
        preState: QaStateSnapshot,
        timestampMs: Long = preState.timestampMs,
    ) {
        visitedScreens.add(preState.screen)
        activeActions[actionId.identifier] = ActiveAction(
            actionId = actionId,
            interactionType = interactionType,
            preState = preState,
            startTimeMs = timestampMs,
            observedLoading = preState.isLoading,
        )
    }

    fun recordEvent(actionId: QaActionId, eventName: String) {
        activeActions[actionId.identifier]?.observedEvents?.add(eventName)
    }

    fun recordLoading(actionId: QaActionId) {
        activeActions[actionId.identifier]?.observedLoading = true
    }

    fun recordSnackbar(actionId: QaActionId) {
        activeActions[actionId.identifier]?.observedSnackbar = true
    }

    fun recordError(actionId: QaActionId, error: String, stackTrace: String? = null) {
        val active = activeActions[actionId.identifier]
        if (active != null) {
            active.observedError = error
            active.observedStackTrace = stackTrace
        }
    }

    fun completeAction(
        actionId: QaActionId,
        postState: QaStateSnapshot,
        timestampMs: Long = postState.timestampMs,
    ): QaActionResult {
        visitedScreens.add(postState.screen)
        val active = activeActions.remove(actionId.identifier)
        val preState = active?.preState ?: postState
        val durationMs = (timestampMs - (active?.startTimeMs ?: timestampMs)).coerceAtLeast(0L)

        val transition = QaStateTransition(
            preState = preState,
            postState = postState,
            durationMs = durationMs,
            observedLoading = active?.observedLoading ?: false,
            snackbarObserved = active?.observedSnackbar ?: false,
            eventsObserved = active?.observedEvents ?: emptyList(),
            errorObserved = active?.observedError,
        )

        val (classification, confidence) = detector.classify(actionId, transition)
        val repetitionCount = detector.getRepetitionCount(actionId, preState.structuralKey)

        var capsule: QaFailureCapsule? = null
        if (classification.isFailure) {
            failureCounter++
            val fid = "INT-${postState.timestampMs}-$failureCounter"
            val contract = contracts[actionId]
            val expectedDesc = contract?.expectedOutcomes?.joinToString(" OR ") { it.toString() }
                ?: "State, route, or sheet transition"
            val observedDesc = buildString {
                if (transition.isIdenticalState) append("No state/route/sheet change. ")
                if (transition.errorObserved != null) append("Error: ${transition.errorObserved}. ")
                if (transition.postState.isLoading) append("Still loading after ${durationMs}ms. ")
                if (transition.eventsObserved.isNotEmpty()) append("Events: ${transition.eventsObserved}. ")
            }.ifBlank { "Unchanged state." }

            capsule = QaFailureCapsule(
                failureId = fid,
                actionId = actionId,
                screen = postState.screen,
                route = postState.route,
                interactionType = active?.interactionType ?: "TAP",
                expected = expectedDesc,
                observed = observedDesc,
                repeatedCount = repetitionCount,
                durationMs = durationMs,
                classification = classification,
                confidence = confidence,
                beforeFingerprint = preState.structuralKey,
                afterFingerprint = postState.structuralKey,
                eventsLogged = transition.eventsObserved,
                stackTrace = active?.observedStackTrace,
                timestampMs = timestampMs,
            )
            failureCapsules.add(capsule)
        } else {
            // Action succeeded, reset repetition count
            detector.resetRepetition(actionId, preState.structuralKey)
        }

        val result = QaActionResult(
            actionId = actionId,
            interactionType = active?.interactionType ?: "TAP",
            transition = transition,
            classification = classification,
            confidence = confidence,
            failureCapsule = capsule,
            repeatedCount = repetitionCount,
        )
        actionHistory.add(result)
        return result
    }

    fun getHistory(): List<QaActionResult> = actionHistory.toList()

    fun getFailureCapsules(): List<QaFailureCapsule> = failureCapsules.toList()

    fun getVisitedScreens(): Set<String> = visitedScreens.toSet()

    fun clear() {
        activeActions.clear()
        actionHistory.clear()
        failureCapsules.clear()
        visitedScreens.clear()
        detector.clearRepetitions()
        failureCounter = 0
    }
}
