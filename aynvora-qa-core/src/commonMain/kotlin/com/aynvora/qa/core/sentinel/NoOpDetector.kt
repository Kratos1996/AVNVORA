package com.aynvora.qa.core.sentinel

import com.aynvora.qa.core.models.QaActionContract
import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaConfidence
import com.aynvora.qa.core.models.QaExpectedOutcome
import com.aynvora.qa.core.models.QaFailureCapsule
import com.aynvora.qa.core.models.QaStateTransition

/**
 * Deterministic classifier evaluating UI action outcomes, distinguishing real NO_OPs
 * from legitimate subtle state changes, loading hangs, errors, and repeated taps.
 */
class NoOpDetector(
    private val contracts: Map<QaActionId, QaActionContract> = emptyMap(),
) {
    // Track consecutive identical interactions: key is (actionId.identifier, preState.structuralKey)
    private val repetitionHistory = mutableMapOf<String, Int>()

    fun classify(
        actionId: QaActionId,
        transition: QaStateTransition,
        failureIdGenerator: () -> String = { "INT-${transition.postState.timestampMs}" },
    ): Pair<QaClassification, QaConfidence> {
        val contract = contracts[actionId]

        // 1. Check for runtime exceptions / error states
        if (transition.errorObserved != null || (!transition.preState.hasError && transition.postState.hasError)) {
            val isNavError =
                transition.errorObserved?.contains("navigation", ignoreCase = true) == true
            val isResourceError =
                transition.errorObserved?.contains("resource", ignoreCase = true) == true
            val classification = when {
                isNavError -> QaClassification.ACTION_NAVIGATION_FAILURE
                isResourceError -> QaClassification.ACTION_RESOURCE_FAILURE
                else -> QaClassification.ACTION_ERROR
            }
            return classification to QaConfidence.HIGH
        }

        // 2. Check for loading hang (started loading but never exited loading within window)
        if (transition.postState.isLoading && transition.durationMs >= (contract?.maxDurationMs
                ?: 3000L)
        ) {
            return QaClassification.ACTION_LOADING_HANG to QaConfidence.HIGH
        }

        // 3. Check ActionContract match
        if (contract != null) {
            if (contract.matchesAny(transition)) {
                return QaClassification.ACTION_SUCCESS to QaConfidence.HIGH
            }
            // Contract specified but not matched
            if (transition.isIdenticalState) {
                val repKey = "${actionId.identifier}#${transition.preState.structuralKey}"
                val count = (repetitionHistory[repKey] ?: 0) + 1
                repetitionHistory[repKey] = count
                return if (count >= 2) {
                    QaClassification.ACTION_REPEATED_NO_OP to QaConfidence.HIGH
                } else {
                    QaClassification.ACTION_NO_OP to QaConfidence.HIGH
                }
            }
            return QaClassification.ACTION_UNEXPECTED_STATE to QaConfidence.MEDIUM
        }

        // 4. Heuristic classification in the absence of explicit contract
        // Did navigation occur?
        if (transition.routeChanged || transition.screenChanged) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.HIGH
        }

        // Did sheet or dialog open or close?
        if (transition.sheetStateChanged || transition.dialogStateChanged) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.HIGH
        }

        // Did theme or locale change?
        if (transition.themeChanged || transition.localeChanged) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.HIGH
        }

        // Did events emit from the interaction?
        if (transition.eventsObserved.isNotEmpty()) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.MEDIUM
        }

        // Did loading transition occur and conclude?
        if (transition.observedLoading && !transition.postState.isLoading && !transition.postState.hasError) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.MEDIUM
        }

        // Did snackbar show?
        if (transition.snackbarObserved) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.MEDIUM
        }

        // Did semantic fingerprint change safely?
        if (transition.preState.semanticFingerprint != transition.postState.semanticFingerprint) {
            return QaClassification.ACTION_SUCCESS to QaConfidence.LOW
        }

        // At this point: Pre-state and post-state are completely identical, no events, no route change.
        val repKey = "${actionId.identifier}#${transition.preState.structuralKey}"
        val count = (repetitionHistory[repKey] ?: 0) + 1
        repetitionHistory[repKey] = count

        return if (count >= 2) {
            QaClassification.ACTION_REPEATED_NO_OP to QaConfidence.HIGH
        } else {
            // First time no-op: check confidence
            // If action name suggests state mutation (e.g. submit, calculate, select, draw, open, close) -> HIGH confidence NO_OP
            val actionName = actionId.action.lowercase()
            val isMutatingAction = actionName.contains("select") ||
                    actionName.contains("calc") ||
                    actionName.contains("draw") ||
                    actionName.contains("open") ||
                    actionName.contains("close") ||
                    actionName.contains("submit")

            if (isMutatingAction) {
                QaClassification.ACTION_NO_OP to QaConfidence.HIGH
            } else {
                QaClassification.ACTION_NO_OP to QaConfidence.MEDIUM
            }
        }
    }

    fun getRepetitionCount(actionId: QaActionId, preStateStructuralKey: String): Int {
        val repKey = "${actionId.identifier}#$preStateStructuralKey"
        return repetitionHistory[repKey] ?: 1
    }

    fun resetRepetition(actionId: QaActionId, preStateStructuralKey: String) {
        val repKey = "${actionId.identifier}#$preStateStructuralKey"
        repetitionHistory.remove(repKey)
    }

    fun clearRepetitions() {
        repetitionHistory.clear()
    }
}
