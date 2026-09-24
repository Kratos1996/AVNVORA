package com.aynvora.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for analytics consent architecture.
 *
 * Verifies:
 * - Default state is PENDING
 * - grantConsent → GRANTED
 * - denyConsent → DENIED
 * - isAnalyticsEnabled() gates correctly
 * - ConsentAwareAnalyticsTracker suppresses events when not GRANTED
 * - ConsentAwareAnalyticsTracker passes events when GRANTED
 */
class AnalyticsConsentTest {

    private class RecordingTracker : AnalyticsTracker {
        val events = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) { events += event }
    }

    @Test
    fun inMemoryConsentManager_defaultState_isPending() {
        val manager = InMemoryAnalyticsConsentManager()
        assertEquals(AnalyticsConsent.PENDING, manager.consentState.value)
    }

    @Test
    fun inMemoryConsentManager_initialConsent_isRespected() {
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.GRANTED)
        assertEquals(AnalyticsConsent.GRANTED, manager.consentState.value)
    }

    @Test
    fun inMemoryConsentManager_grantConsent_setsGranted() = runBlockingTest {
        val manager = InMemoryAnalyticsConsentManager()
        manager.grantConsent()
        assertEquals(AnalyticsConsent.GRANTED, manager.consentState.value)
    }

    @Test
    fun inMemoryConsentManager_denyConsent_setsDenied() = runBlockingTest {
        val manager = InMemoryAnalyticsConsentManager()
        manager.denyConsent()
        assertEquals(AnalyticsConsent.DENIED, manager.consentState.value)
    }

    @Test
    fun isAnalyticsEnabled_returnsFalse_whenPending() {
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.PENDING)
        assertFalse(manager.isAnalyticsEnabled())
    }

    @Test
    fun isAnalyticsEnabled_returnsFalse_whenDenied() {
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.DENIED)
        assertFalse(manager.isAnalyticsEnabled())
    }

    @Test
    fun isAnalyticsEnabled_returnsTrue_whenGranted() {
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.GRANTED)
        assertTrue(manager.isAnalyticsEnabled())
    }

    @Test
    fun consentAwareTracker_suppressesEvents_whenPending() {
        val recorder = RecordingTracker()
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.PENDING)
        val tracker = ConsentAwareAnalyticsTracker(recorder, manager)

        tracker.track(AnalyticsEvent.AppOpened)

        assertTrue(recorder.events.isEmpty(), "Events must not fire when consent is PENDING")
    }

    @Test
    fun consentAwareTracker_suppressesEvents_whenDenied() {
        val recorder = RecordingTracker()
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.DENIED)
        val tracker = ConsentAwareAnalyticsTracker(recorder, manager)

        tracker.track(AnalyticsEvent.AppOpened)
        tracker.track(AnalyticsEvent.ChartCalculationRequested("x"))

        assertTrue(recorder.events.isEmpty(), "Events must not fire when consent is DENIED")
    }

    @Test
    fun consentAwareTracker_passesEvents_whenGranted() {
        val recorder = RecordingTracker()
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.GRANTED)
        val tracker = ConsentAwareAnalyticsTracker(recorder, manager)

        tracker.track(AnalyticsEvent.AppOpened)
        tracker.track(AnalyticsEvent.ChartCalculationRequested("PARASHARA_CLASSICAL_V1"))

        assertEquals(2, recorder.events.size)
        assertEquals("app_opened", recorder.events[0].name)
        assertEquals("chart_calculation_requested", recorder.events[1].name)
    }

    @Test
    fun consentAwareTracker_suppressesEvents_afterConsentWithdrawn() = runBlockingTest {
        val recorder = RecordingTracker()
        val manager = InMemoryAnalyticsConsentManager(AnalyticsConsent.GRANTED)
        val tracker = ConsentAwareAnalyticsTracker(recorder, manager)

        tracker.track(AnalyticsEvent.AppOpened)

        manager.denyConsent()

        tracker.track(AnalyticsEvent.ChartCalculationRequested("x"))

        assertEquals(1, recorder.events.size, "Only the pre-denial event should have been tracked")
        assertEquals("app_opened", recorder.events[0].name)
    }

    @Test
    fun newEvents_languageChanged_hasCorrectNameAndParam() {
        val event = AnalyticsEvent.LanguageChanged("hi")
        assertEquals("language_changed", event.name)
        assertEquals("hi", event.params[AnalyticsEvent.Param.LANGUAGE_CODE])
    }

    @Test
    fun newEvents_contentSyncStarted_hasReason() {
        val event = AnalyticsEvent.ContentSyncStarted("manual")
        assertEquals("content_sync_started", event.name)
        assertEquals("manual", event.params[AnalyticsEvent.Param.SYNC_REASON])
    }

    @Test
    fun newEvents_contentSyncCompleted_hasNoParams() {
        assertTrue(AnalyticsEvent.ContentSyncCompleted.params.isEmpty())
    }

    @Test
    fun newEvents_sdkInitialized_hasAnalyticsEnabledFlag() {
        val event = AnalyticsEvent.SdkInitialized(true)
        assertEquals("sdk_initialized", event.name)
        assertEquals("true", event.params["analytics_enabled"])
    }

    @Test
    fun newEvents_offlineContentAccessed_hasModuleId() {
        val event = AnalyticsEvent.OfflineContentAccessed("ASTROLOGY")
        assertEquals("offline_content_accessed", event.name)
        assertEquals("ASTROLOGY", event.params[AnalyticsEvent.Param.MODULE_ID])
    }

    @Test
    fun noSensitiveData_languageChanged_doesNotContainBirthData() {
        val event = AnalyticsEvent.LanguageChanged("hi")
        // Event params must only contain the language code — no birth data
        assertEquals(1, event.params.size)
        val value = event.params.values.first()
        assertFalse(
            value.toString().contains("birth", ignoreCase = true),
            "LanguageChanged must not contain birth data in any parameter",
        )
    }
}

// Minimal blocking coroutine test helper (no kotlinx-test dependency needed for these tests)
private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
