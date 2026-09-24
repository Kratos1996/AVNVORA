package com.aynvora.core.analytics

/**
 * Platform-agnostic analytics reporting contract for AYNVORA.
 *
 * Architecture contract:
 * - This interface belongs to the domain layer (`:aynvora-core`).
 * - Firebase, Mixpanel, or any other analytics SDK MUST NOT be imported here.
 * - Platform implementations live in the host application module (e.g. `androidApp`).
 * - A no-op implementation ([NoOpAnalyticsTracker]) is used for JVM Desktop, tests,
 *   and any context where analytics are not configured.
 *
 * Privacy contract:
 * - Implementations MUST NOT log or forward PII.
 * - Birth data, chart results, names, coordinates, and raw exception messages are PII.
 * - Callers are responsible for ensuring [AnalyticsEvent] payloads are PII-free
 *   before dispatching; implementations are NOT expected to sanitize inputs.
 */
interface AnalyticsTracker {

    /**
     * Records a single analytics event.
     *
     * Implementations should be non-blocking and should not throw; any
     * internal failures must be swallowed silently so analytics never
     * destabilises the core calculation or persistence paths.
     */
    fun track(event: AnalyticsEvent)
}

/**
 * No-operation implementation of [AnalyticsTracker].
 *
 * Used as the default fallback for:
 * - JVM Desktop application
 * - iOS (until a native Firebase Analytics wrapper is added)
 * - Unit tests and instrumented tests
 * - Any context where analytics are explicitly disabled
 */
class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun track(event: AnalyticsEvent): Unit = Unit
}
