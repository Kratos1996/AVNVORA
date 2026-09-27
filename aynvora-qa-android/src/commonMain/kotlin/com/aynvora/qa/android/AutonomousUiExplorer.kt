package com.aynvora.qa.android

import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaActionResult
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaDenylist
import kotlinx.coroutines.delay

/**
 * Candidate action discoverable on a screen.
 */
data class ActionCandidate(
    val actionId: QaActionId,
    val isDangerous: Boolean = QaDenylist.isDangerous(actionId),
    val execute: suspend () -> Unit,
)

/**
 * Result of an autonomous exploration run.
 */
data class ExplorationResult(
    val actionsDiscovered: Int,
    val actionsExecuted: Int,
    val actionsSkippedDangerous: Int,
    val actionsSkippedRepeated: Int,
    val successfulActions: Int,
    val noOpActions: Int,
    val errorActions: Int,
    val timeoutActions: Int,
    val screensExplored: Set<String>,
    val history: List<QaActionResult>,
)

/**
 * Autonomous, bounded UI exploration driver for testing connected Android devices safely.
 */
class AutonomousUiExplorer(
    private val maxDepth: Int = 5,
    private val maxActions: Int = 50,
    private val maxRepeatsPerAction: Int = 3,
    private val settleDelayMs: Long = 400L,
) {
    private val visitedScreenActionPairs = mutableSetOf<String>()
    private val actionExecutionCounts = mutableMapOf<String, Int>()

    suspend fun explore(
        discoverActions: suspend (currentScreen: String) -> List<ActionCandidate>,
        onBacktrack: suspend () -> Unit = {},
    ): ExplorationResult {
        var actionsDiscovered = 0
        var actionsExecuted = 0
        var actionsSkippedDangerous = 0
        var actionsSkippedRepeated = 0

        var currentDepth = 0

        while (actionsExecuted < maxActions && currentDepth < maxDepth) {
            val currentState = AndroidInteractionSentinelRuntime.stateSnapshotProvider()
            val candidates = discoverActions(currentState.screen)
            actionsDiscovered += candidates.size

            // Filter out dangerous actions and over-repeated actions
            val safeCandidates = candidates.filter { candidate ->
                if (candidate.isDangerous || QaDenylist.isDangerous(candidate.actionId)) {
                    actionsSkippedDangerous++
                    false
                } else {
                    val pairKey = "${currentState.screen}#${candidate.actionId.identifier}"
                    val count = actionExecutionCounts[pairKey] ?: 0
                    if (count >= maxRepeatsPerAction) {
                        actionsSkippedRepeated++
                        false
                    } else {
                        true
                    }
                }
            }

            if (safeCandidates.isEmpty()) {
                // Dead end reached, attempt backtrack
                if (currentDepth > 0) {
                    onBacktrack()
                    delay(settleDelayMs)
                    currentDepth--
                    continue
                } else {
                    break
                }
            }

            // Pick the next untried or least-tried safe candidate
            val target = safeCandidates.minByOrNull { candidate ->
                val pairKey = "${currentState.screen}#${candidate.actionId.identifier}"
                actionExecutionCounts[pairKey] ?: 0
            } ?: break

            val pairKey = "${currentState.screen}#${target.actionId.identifier}"
            actionExecutionCounts[pairKey] = (actionExecutionCounts[pairKey] ?: 0) + 1
            visitedScreenActionPairs.add(pairKey)

            // Execute through Sentinel
            AndroidInteractionSentinelRuntime.executeMonitoredAction(
                actionId = target.actionId,
                interactionType = "TAP",
                settleDelayMs = settleDelayMs,
            ) {
                target.execute()
            }

            actionsExecuted++
            val postState = AndroidInteractionSentinelRuntime.stateSnapshotProvider()
            if (postState.screen != currentState.screen || postState.route != currentState.route) {
                currentDepth++
            }
        }

        val sentinel = AndroidInteractionSentinelRuntime.getSentinel()
        val history = sentinel.getHistory()

        return ExplorationResult(
            actionsDiscovered = actionsDiscovered,
            actionsExecuted = actionsExecuted,
            actionsSkippedDangerous = actionsSkippedDangerous,
            actionsSkippedRepeated = actionsSkippedRepeated,
            successfulActions = history.count { it.classification == QaClassification.ACTION_SUCCESS },
            noOpActions = history.count { it.classification == QaClassification.ACTION_NO_OP || it.classification == QaClassification.ACTION_REPEATED_NO_OP },
            errorActions = history.count { it.classification == QaClassification.ACTION_ERROR || it.classification == QaClassification.ACTION_NAVIGATION_FAILURE },
            timeoutActions = history.count { it.classification == QaClassification.ACTION_TIMEOUT || it.classification == QaClassification.ACTION_LOADING_HANG },
            screensExplored = sentinel.getVisitedScreens(),
            history = history,
        )
    }

    fun reset() {
        visitedScreenActionPairs.clear()
        actionExecutionCounts.clear()
    }
}
