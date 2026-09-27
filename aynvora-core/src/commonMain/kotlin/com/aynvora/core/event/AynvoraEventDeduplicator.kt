package com.aynvora.core.event

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Deduplication policy per event type.
 */
enum class AynvoraDeduplicationPolicy {
    NO_DEDUP,
    DEDUP_SHORT_WINDOW,
    IDEMPOTENT,
}

/**
 * Deduplicator preventing accidental double-tap or repeated actions.
 */
interface AynvoraEventDeduplicator {
    suspend fun checkAndRecord(event: AynvoraEvent, policy: AynvoraDeduplicationPolicy): Boolean
    suspend fun reset(eventId: String)
    suspend fun clear()
}

/**
 * Thread-safe implementation of [AynvoraEventDeduplicator].
 */
class DefaultAynvoraEventDeduplicator(
    private val shortWindowMs: Long = 400L,
    private val timeProvider: () -> Long = { timeMark.elapsedNow().inWholeMilliseconds },
) : AynvoraEventDeduplicator {

    private val mutex = Mutex()
    private val lastDispatchTimes = mutableMapOf<String, Long>()
    private val idempotentCompletedEvents = mutableSetOf<String>()

    override suspend fun checkAndRecord(
        event: AynvoraEvent,
        policy: AynvoraDeduplicationPolicy,
    ): Boolean = mutex.withLock {
        val now = timeProvider()
        val key = buildKey(event)

        when (policy) {
            AynvoraDeduplicationPolicy.NO_DEDUP -> true

            AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW -> {
                val lastTime = lastDispatchTimes[key]
                if (lastTime != null && (now - lastTime) < shortWindowMs) {
                    false // Rejected duplicate
                } else {
                    lastDispatchTimes[key] = now
                    true
                }
            }

            AynvoraDeduplicationPolicy.IDEMPOTENT -> {
                if (idempotentCompletedEvents.contains(key)) {
                    false // Already completed idempotently
                } else {
                    idempotentCompletedEvents.add(key)
                    true
                }
            }
        }
    }

    override suspend fun reset(eventId: String): Unit = mutex.withLock {
        lastDispatchTimes.remove(eventId)
        idempotentCompletedEvents.remove(eventId)
    }

    override suspend fun clear(): Unit = mutex.withLock {
        lastDispatchTimes.clear()
        idempotentCompletedEvents.clear()
    }

    private fun buildKey(event: AynvoraEvent): String {
        return if (event.correlationId.isNotBlank()) {
            "${event.eventId}:${event.correlationId}"
        } else {
            "${event.screenId}.${event.componentId}.${event.eventId}"
        }
    }

    companion object {
        private val timeMark = kotlin.time.TimeSource.Monotonic.markNow()
    }
}
