package com.aynvora.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [AnalyticsEvent] and [NoOpAnalyticsTracker].
 *
 * Verifies:
 * - Event names and params are stable and PII-free by construction.
 * - NoOpAnalyticsTracker accepts all events without error.
 * - A recording test tracker can capture events for integration-style assertions.
 */
class AnalyticsTrackerTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Recording tracker for test assertions
    // ──────────────────────────────────────────────────────────────────────────

    private class RecordingTracker : AnalyticsTracker {
        val recorded = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) {
            recorded += event
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NoOp tests
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun noOpTracker_acceptsAllEventTypes_withoutThrowing() {
        val tracker = NoOpAnalyticsTracker()
        // All event subtypes must be accepted silently
        tracker.track(AnalyticsEvent.AppOpened)
        tracker.track(AnalyticsEvent.ChartCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.ChartCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))
        tracker.track(AnalyticsEvent.ChartCalculationFailed("invalid_input"))
        tracker.track(AnalyticsEvent.DivisionalChartRequested("D9"))
        tracker.track(AnalyticsEvent.DivisionalChartSucceeded("D9", 0L))
        tracker.track(AnalyticsEvent.DivisionalChartFailed("D9", "calculation_failure"))
        tracker.track(AnalyticsEvent.ShadbalaCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.ShadbalaCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))
        tracker.track(AnalyticsEvent.ShadbalaCalculationFailed("internal_failure"))
        tracker.track(AnalyticsEvent.AshtakavargaCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.AshtakavargaCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))
        tracker.track(AnalyticsEvent.AshtakavargaCalculationFailed("unsupported_configuration"))
        tracker.track(AnalyticsEvent.ShodhanaCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.ShodhanaCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))
        tracker.track(AnalyticsEvent.PindaCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.PindaCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))
        tracker.track(AnalyticsEvent.BirthProfileSaved)
        tracker.track(AnalyticsEvent.BirthProfileDeleted)
        tracker.track(AnalyticsEvent.ChartSaved)
        tracker.track(AnalyticsEvent.ChartDeleted)
        tracker.track(AnalyticsEvent.PreferenceChanged("ayanamsa"))
        tracker.track(AnalyticsEvent.ThemeToggled(true))
        tracker.track(AnalyticsEvent.SdkErrorObserved("internal_failure"))
        // If no exception was thrown, the test passes.
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Event name stability
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun eventNames_areStableSnakeCase() {
        assertEquals("app_opened", AnalyticsEvent.AppOpened.name)
        assertEquals("chart_calculation_requested", AnalyticsEvent.ChartCalculationRequested("x").name)
        assertEquals("chart_calculation_succeeded", AnalyticsEvent.ChartCalculationSucceeded("x", 0L).name)
        assertEquals("chart_calculation_failed", AnalyticsEvent.ChartCalculationFailed("x").name)
        assertEquals("divisional_chart_requested", AnalyticsEvent.DivisionalChartRequested("D9").name)
        assertEquals("divisional_chart_succeeded", AnalyticsEvent.DivisionalChartSucceeded("D9", 0L).name)
        assertEquals("divisional_chart_failed", AnalyticsEvent.DivisionalChartFailed("D9", "x").name)
        assertEquals("shadbala_calculation_requested", AnalyticsEvent.ShadbalaCalculationRequested("x").name)
        assertEquals("ashtakavarga_calculation_requested", AnalyticsEvent.AshtakavargaCalculationRequested("x").name)
        assertEquals("birth_profile_saved", AnalyticsEvent.BirthProfileSaved.name)
        assertEquals("chart_saved", AnalyticsEvent.ChartSaved.name)
        assertEquals("theme_toggled", AnalyticsEvent.ThemeToggled(false).name)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Params are PII-free by construction
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun chartCalculationRequested_paramsContainOnlyRulesetId() {
        val event = AnalyticsEvent.ChartCalculationRequested("PARASHARA_CLASSICAL_V1")
        assertEquals(1, event.params.size)
        assertEquals("PARASHARA_CLASSICAL_V1", event.params[AnalyticsEvent.Param.RULESET_ID])
    }

    @Test
    fun chartCalculationFailed_paramsContainOnlyErrorCode() {
        val event = AnalyticsEvent.ChartCalculationFailed("invalid_input")
        assertEquals(1, event.params.size)
        assertEquals("invalid_input", event.params[AnalyticsEvent.Param.ERROR_CODE])
    }

    @Test
    fun divisionalChartRequested_paramsContainOnlyDivision() {
        val event = AnalyticsEvent.DivisionalChartRequested("D9")
        assertEquals(1, event.params.size)
        assertEquals("D9", event.params[AnalyticsEvent.Param.CHART_DIVISION])
    }

    @Test
    fun appOpened_hasNoParams() {
        assertTrue(AnalyticsEvent.AppOpened.params.isEmpty())
    }

    @Test
    fun birthProfileSaved_hasNoParams() {
        assertTrue(AnalyticsEvent.BirthProfileSaved.params.isEmpty())
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Recording tracker captures all events
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun recordingTracker_capturesEventsInOrder() {
        val tracker = RecordingTracker()
        tracker.track(AnalyticsEvent.AppOpened)
        tracker.track(AnalyticsEvent.ChartCalculationRequested("PARASHARA_CLASSICAL_V1"))
        tracker.track(AnalyticsEvent.ChartCalculationSucceeded("PARASHARA_CLASSICAL_V1", 0L))

        assertEquals(3, tracker.recorded.size)
        assertEquals("app_opened", tracker.recorded[0].name)
        assertEquals("chart_calculation_requested", tracker.recorded[1].name)
        assertEquals("chart_calculation_succeeded", tracker.recorded[2].name)
    }
}
