package com.aynvora.core.event

/**
 * Diagnostic trace for observing event lifecycle without exposing sensitive data.
 */
data class EventTrace(
    val eventId: String,
    val screenId: String,
    val componentId: String,
    val timestampEpochMs: Long,
    val result: String,
    val failureCode: String? = null,
)

/**
 * In-memory diagnostic observer recording recent event execution traces.
 */
class EventTracingObserver(
    private val maxTraces: Int = 100,
) : AynvoraEventObserver {

    private val traces = mutableListOf<EventTrace>()

    override suspend fun onEvent(event: AynvoraEvent, result: EventHandlingResult) {
        val trace = EventTrace(
            eventId = event.eventId,
            screenId = event.screenId,
            componentId = event.componentId,
            timestampEpochMs = event.timestampEpochMs,
            result = when (result) {
                EventHandlingResult.Handled -> "HANDLED"
                EventHandlingResult.IgnoredDuplicate -> "IGNORED_DUPLICATE"
                is EventHandlingResult.Invalid -> "INVALID"
                is EventHandlingResult.Rejected -> "REJECTED"
                is EventHandlingResult.Unauthorized -> "UNAUTHORIZED"
                is EventHandlingResult.Failed -> "FAILED"
            },
            failureCode = when (result) {
                is EventHandlingResult.Invalid -> result.reason
                is EventHandlingResult.Rejected -> result.reason
                is EventHandlingResult.Unauthorized -> result.reason
                is EventHandlingResult.Failed -> result.error.message
                else -> null
            },
        )

        synchronized(traces) {
            if (traces.size >= maxTraces) {
                traces.removeAt(0)
            }
            traces.add(trace)
        }
    }

    fun getTraces(): List<EventTrace> = synchronized(traces) {
        traces.toList()
    }

    fun clear() = synchronized(traces) {
        traces.clear()
    }
}
