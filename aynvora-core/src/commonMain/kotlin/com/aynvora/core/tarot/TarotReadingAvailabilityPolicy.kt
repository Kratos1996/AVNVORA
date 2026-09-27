package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult

/**
 * Centralized domain policy for determining whether a new Tarot reading is allowed.
 *
 * BUSINESS RULE:
 *   ONE new primary reading is allowed per rolling 24-hour window.
 *
 * This is the single source of truth for the 24h lock calculation.
 * UI MUST consume this policy — never re-implement the duration math.
 *
 * Lock applies regardless of:
 * - app restart
 * - language change
 * - deck change
 * - spread change
 * - navigation
 *
 * Clarification cards and follow-up questions do NOT create a new session
 * and do NOT reset this lock.
 */
class TarotReadingAvailabilityPolicy(
    private val sessionRepository: TarotSessionRepository,
    private val clock: TarotClock = SystemTarotClock(),
) {

    /**
     * Evaluates the current reading availability.
     *
     * Returns:
     * - [TarotReadingAvailability.Available] — no lock; new reading allowed.
     * - [TarotReadingAvailability.ActiveReadingExists] — a session is ACTIVE; user can re-enter it.
     * - [TarotReadingAvailability.Locked] — 24h window is active; new reading blocked.
     */
    suspend fun evaluate(): AynvoraResult<TarotReadingAvailability> {
        val sessionResult = sessionRepository.getLatestSession()
        val latestSession = when (sessionResult) {
            is AynvoraResult.Success -> sessionResult.value
            is AynvoraResult.Failure -> return AynvoraResult.Success(TarotReadingAvailability.Available)
        }

        if (latestSession == null) {
            return AynvoraResult.Success(TarotReadingAvailability.Available)
        }

        val nowMs = clock.nowEpochMs()

        // Expire sessions that have passed their window
        if (nowMs >= latestSession.expiresAtEpochMs &&
            latestSession.status !in listOf(TarotReadingStatus.EXPIRED, TarotReadingStatus.ARCHIVED)
        ) {
            sessionRepository.updateSessionStatus(latestSession.id, TarotReadingStatus.EXPIRED)
            return AynvoraResult.Success(TarotReadingAvailability.Available)
        }

        // Check for available (expired or archived)
        if (latestSession.status == TarotReadingStatus.EXPIRED ||
            latestSession.status == TarotReadingStatus.ARCHIVED
        ) {
            return AynvoraResult.Success(TarotReadingAvailability.Available)
        }

        // Active session — user can re-enter without starting a new reading
        if (latestSession.status == TarotReadingStatus.ACTIVE) {
            return AynvoraResult.Success(TarotReadingAvailability.ActiveReadingExists(latestSession))
        }

        // Completed or Satisfied within 24h window — still locked
        val remainingMs = (latestSession.expiresAtEpochMs - nowMs).coerceAtLeast(0L)
        return AynvoraResult.Success(
            TarotReadingAvailability.Locked(
                session = latestSession,
                remainingMs = remainingMs,
                nextAvailableAtEpochMs = latestSession.expiresAtEpochMs,
            )
        )
    }

    /**
     * Returns true if a new reading is currently allowed.
     */
    suspend fun canStartNewReading(): Boolean {
        return when (val result = evaluate()) {
            is AynvoraResult.Success -> result.value is TarotReadingAvailability.Available
            is AynvoraResult.Failure -> true // Fail-open
        }
    }

    /**
     * Creates a new [TarotReadingSession] with a 24h lock and saves it.
     *
     * Call this ONLY after [canStartNewReading] returns true.
     * The returned session will have [TarotReadingStatus.ACTIVE].
     */
    suspend fun startNewSession(
        reading: TarotReading,
        language: String = "en",
        deckId: String = TarotStandardDeck.Deck.id,
    ): AynvoraResult<TarotReadingSession> {

        val nowMs = clock.nowEpochMs()
        val session = TarotReadingSession(
            id = "session_${nowMs}_${reading.id.hashCode().toString().take(6)}",
            reading = reading,
            startedAtEpochMs = nowMs,
            expiresAtEpochMs = nowMs + TarotReadingSession.READING_LOCK_DURATION_MS,
            status = TarotReadingStatus.ACTIVE,
            language = language,
            deckId = deckId,
        )
        return when (val saveResult = sessionRepository.saveSession(session)) {
            is AynvoraResult.Success -> AynvoraResult.Success(session)
            is AynvoraResult.Failure -> saveResult
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock abstraction for testability
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Abstracted clock used by [TarotReadingAvailabilityPolicy].
 * Tests inject [FakeTarotClock] to control time.
 */
interface TarotClock {
    fun nowEpochMs(): Long
}

/** Production implementation using system time. */
class SystemTarotClock : TarotClock {
    override fun nowEpochMs(): Long = System.currentTimeMillis()
}

/** Test-only fake clock for time-travel tests. */
class FakeTarotClock(private var fixedEpochMs: Long) : TarotClock {
    override fun nowEpochMs(): Long = fixedEpochMs

    /** Advance the fake clock by [deltaMs] milliseconds. */
    fun advance(deltaMs: Long) {
        fixedEpochMs += deltaMs
    }

    /** Set the clock to an absolute value. */
    fun setTo(epochMs: Long) {
        fixedEpochMs = epochMs
    }
}
