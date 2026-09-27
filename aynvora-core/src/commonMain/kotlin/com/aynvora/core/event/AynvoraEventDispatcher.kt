package com.aynvora.core.event

import kotlinx.coroutines.sync.Mutex

/**
 * Result of executing an event through the pipeline.
 */
sealed interface EventHandlingResult {
    data object Handled : EventHandlingResult
    data class Rejected(val reason: String) : EventHandlingResult
    data object IgnoredDuplicate : EventHandlingResult
    data class Invalid(val reason: String) : EventHandlingResult
    data class Unauthorized(val reason: String) : EventHandlingResult
    data class Failed(val error: Throwable) : EventHandlingResult
}

/**
 * Handler interface for business/domain execution of typed events.
 */
interface AynvoraEventHandler {
    fun canHandle(event: AynvoraEvent): Boolean
    suspend fun handle(event: AynvoraEvent): EventHandlingResult
}

/**
 * Observer of events that have been processed through the pipeline.
 * Observers (such as analytics, telemetry, and debug tracing) must never mutate business state.
 */
interface AynvoraEventObserver {
    suspend fun onEvent(event: AynvoraEvent, result: EventHandlingResult)
}

/**
 * Central event dispatcher for routing events to handlers and notifying observers.
 */
interface AynvoraEventDispatcher {
    suspend fun dispatch(
        event: AynvoraEvent,
        policy: AynvoraDeduplicationPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
    ): EventHandlingResult

    fun registerHandler(handler: AynvoraEventHandler)
    fun unregisterHandler(handler: AynvoraEventHandler)

    fun addObserver(observer: AynvoraEventObserver)
    fun removeObserver(observer: AynvoraEventObserver)
}

/**
 * Thread-safe production implementation of [AynvoraEventDispatcher].
 */
class DefaultAynvoraEventDispatcher(
    private val guard: AynvoraEventGuard = StandardAynvoraEventGuard(),
    private val deduplicator: AynvoraEventDeduplicator = DefaultAynvoraEventDeduplicator(),
) : AynvoraEventDispatcher {

    private val mutex = Mutex()
    private val handlers = mutableListOf<AynvoraEventHandler>()
    private val observers = mutableListOf<AynvoraEventObserver>()

    override fun registerHandler(handler: AynvoraEventHandler) {
        handlers.add(handler)
    }

    override fun unregisterHandler(handler: AynvoraEventHandler) {
        handlers.remove(handler)
    }

    override fun addObserver(observer: AynvoraEventObserver) {
        observers.add(observer)
    }

    override fun removeObserver(observer: AynvoraEventObserver) {
        observers.remove(observer)
    }

    override suspend fun dispatch(
        event: AynvoraEvent,
        policy: AynvoraDeduplicationPolicy,
    ): EventHandlingResult {
        // 1. Validation via EventGuard
        val validation = guard.validate(event)
        if (validation != AynvoraEventValidationResult.Valid) {
            val result = when (validation) {
                is AynvoraEventValidationResult.InvalidPayload -> EventHandlingResult.Invalid(
                    validation.reason
                )

                is AynvoraEventValidationResult.Unauthorized -> EventHandlingResult.Unauthorized(
                    validation.reason
                )

                is AynvoraEventValidationResult.Duplicate -> EventHandlingResult.IgnoredDuplicate
                is AynvoraEventValidationResult.InvalidState -> EventHandlingResult.Rejected(
                    validation.reason
                )

                is AynvoraEventValidationResult.Unsupported -> EventHandlingResult.Rejected(
                    validation.reason
                )

                is AynvoraEventValidationResult.RateLimited -> EventHandlingResult.Rejected("Rate limited: ${validation.waitMs}ms")
                AynvoraEventValidationResult.Valid -> EventHandlingResult.Handled
            }
            notifyObservers(event, result)
            return result
        }

        // 2. Idempotency / Deduplication check
        val allowed = deduplicator.checkAndRecord(event, policy)
        if (!allowed) {
            val result = EventHandlingResult.IgnoredDuplicate
            notifyObservers(event, result)
            return result
        }

        // 3. Delegate to registered handler
        val matchingHandler = handlers.firstOrNull { it.canHandle(event) }
        val result = if (matchingHandler != null) {
            try {
                matchingHandler.handle(event)
            } catch (t: Throwable) {
                EventHandlingResult.Failed(t)
            }
        } else {
            EventHandlingResult.Rejected("No handler registered for event: ${event.eventId}")
        }

        // 4. Notify observers (Analytics, Logging)
        notifyObservers(event, result)

        return result
    }

    private suspend fun notifyObservers(event: AynvoraEvent, result: EventHandlingResult) {
        for (observer in observers) {
            try {
                observer.onEvent(event, result)
            } catch (_: Exception) {
                // Observers must never crash dispatch pipeline
            }
        }
    }
}
