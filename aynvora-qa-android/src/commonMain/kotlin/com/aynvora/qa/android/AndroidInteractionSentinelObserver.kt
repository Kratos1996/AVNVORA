package com.aynvora.qa.android

import com.aynvora.core.event.AynvoraEvent
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.event.AynvoraEventObserver
import com.aynvora.core.event.EventHandlingResult
import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaActionResult
import com.aynvora.qa.core.models.QaStateSnapshot
import com.aynvora.qa.core.report.QaSessionReporter
import com.aynvora.qa.core.report.QaSessionSummary
import com.aynvora.qa.core.sentinel.InteractionSentinel
import kotlinx.coroutines.delay

/**
 * Runtime coordinator for Interaction Sentinel on Android.
 * Supports Monitor Mode (passive human testing) and Autonomous Safe Exploration.
 */
object AndroidInteractionSentinelRuntime : AynvoraEventObserver {

    private val sentinel = InteractionSentinel()
    private var isMonitoring = false
    private var eventDispatcher: AynvoraEventDispatcher? = null

    // Provider to snapshot UI state from outside
    var stateSnapshotProvider: () -> QaStateSnapshot = {
        QaStateSnapshot(
            screen = "Unknown",
            timestampMs = System.currentTimeMillis(),
        )
    }

    private var activeActionId: QaActionId? = null

    fun initialize(dispatcher: AynvoraEventDispatcher? = null) {
        eventDispatcher = dispatcher
        dispatcher?.addObserver(this)
        isMonitoring = true
        AndroidExceptionCorrelator.attach()
    }

    fun isMonitorModeActive(): Boolean = isMonitoring

    fun setMonitorMode(enabled: Boolean) {
        isMonitoring = enabled
    }

    fun getSentinel(): InteractionSentinel = sentinel

    fun getActiveActionId(): QaActionId? = activeActionId

    override suspend fun onEvent(event: AynvoraEvent, result: EventHandlingResult) {
        if (!isMonitoring) return
        val currentAction = activeActionId ?: return
        val eventDesc =
            "${event.eventType}/${event.screenId}:${event.componentId} (status=${result::class.simpleName})"
        sentinel.recordEvent(currentAction, eventDesc)
    }

    fun onActionTriggered(
        actionId: QaActionId,
        interactionType: String = "TAP",
        preState: QaStateSnapshot = stateSnapshotProvider(),
    ) {
        if (!isMonitoring) return
        activeActionId = actionId
        sentinel.startAction(actionId, interactionType, preState, preState.timestampMs)
    }

    fun onActionFinished(
        actionId: QaActionId,
        postState: QaStateSnapshot = stateSnapshotProvider(),
        error: Throwable? = null,
    ): QaActionResult {
        if (error != null) {
            sentinel.recordError(
                actionId,
                error.message ?: error.toString(),
                error.stackTraceToString()
            )
        }
        val result = sentinel.completeAction(actionId, postState, postState.timestampMs)
        if (activeActionId == actionId) {
            activeActionId = null
        }
        return result
    }

    /**
     * Executes an action with automatic pre/post snapshotting and multi-window settling.
     */
    suspend fun executeMonitoredAction(
        actionId: QaActionId,
        interactionType: String = "TAP",
        settleDelayMs: Long = 400L,
        block: suspend () -> Unit,
    ): QaActionResult {
        val pre = stateSnapshotProvider()
        onActionTriggered(actionId, interactionType, pre)
        var thrown: Throwable? = null
        try {
            block()
        } catch (t: Throwable) {
            thrown = t
        }
        if (settleDelayMs > 0) {
            delay(settleDelayMs)
        }
        val post = stateSnapshotProvider()
        return onActionFinished(actionId, post, thrown)
    }

    fun recordUnhandledException(error: Throwable) {
        val currentAction =
            activeActionId ?: QaActionId.of("runtime", "system", "uncaught", "exception")
        sentinel.recordError(
            currentAction,
            error.message ?: error.toString(),
            error.stackTraceToString()
        )
        val post = stateSnapshotProvider()
        sentinel.completeAction(currentAction, post.copy(hasError = true), post.timestampMs)
        activeActionId = null
    }

    fun generateSessionSummary(sessionId: String = "SESSION-${System.currentTimeMillis()}"): QaSessionSummary {
        return QaSessionReporter.generateSummary(
            sessionId = sessionId,
            timestampMs = System.currentTimeMillis(),
            screensVisited = sentinel.getVisitedScreens(),
            history = sentinel.getHistory(),
            failureCapsules = sentinel.getFailureCapsules(),
        )
    }

    fun generateMarkdownReport(sessionId: String = "SESSION-${System.currentTimeMillis()}"): String {
        return QaSessionReporter.toMarkdown(generateSessionSummary(sessionId))
    }

    fun generateJsonReport(sessionId: String = "SESSION-${System.currentTimeMillis()}"): String {
        return QaSessionReporter.toJson(generateSessionSummary(sessionId))
    }

    fun reset() {
        sentinel.clear()
        activeActionId = null
    }
}
