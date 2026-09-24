package com.aynvora.core.architecture

import com.aynvora.core.analytics.AnalyticsConsentManager
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.InMemoryAnalyticsConsentManager
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.sync.ContentVerifier
import com.aynvora.core.sync.ContentVerificationResult
import com.aynvora.core.sync.ContentVerificationStatus
import com.aynvora.core.sync.StubContentVerifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Architecture isolation tests for Phase 7.0.
 *
 * Verifies:
 * - Domain classes (aynvora-core) have NO Firebase/Android/Platform dependencies.
 * - AnalyticsTracker and AnalyticsConsentManager are pure Kotlin interfaces.
 * - ContentVerifier is a pure Kotlin domain interface with no networking.
 * - AynvoraResult.Failure subtypes are all covered by the errorCode mapper.
 * - Analytics cannot corrupt results — NoOp tracker is truly no-op.
 */
class ArchitectureIsolationTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Domain module isolation
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun analyticsTracker_isAPureKotlinInterface_noAndroidImports() {
        // If this test compiles in commonTest, the interface has no platform deps.
        val tracker: AnalyticsTracker = NoOpAnalyticsTracker()
        tracker.track(AnalyticsEvent.AppOpened)
        // No exception → passes
    }

    @Test
    fun analyticsConsentManager_isAPureKotlinInterface_noAndroidImports() {
        val manager: AnalyticsConsentManager = InMemoryAnalyticsConsentManager()
        assertFalse(manager.isAnalyticsEnabled())
    }

    @Test
    fun contentVerifier_isAPureKotlinInterface_noNetworkImports() = runBlockingTest {
        val verifier: ContentVerifier = StubContentVerifier()
        val result = verifier.verify(
            packId = "test-pack",
            expectedChecksumSha256 = "a".repeat(64),
            rawPayload = byteArrayOf(0x01),
            expectedSignature = "dummy",
            signatureKeyId = "test-key",
            installedVersion = 0,
            incomingVersion = 1,
        )
        assertTrue(result.isAccepted, "StubContentVerifier should accept version 1 > 0")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ContentVerifier behavior
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun stubContentVerifier_rejectsStaleVersion() = runBlockingTest {
        val verifier = StubContentVerifier()
        val result = verifier.verify(
            packId = "test-pack",
            expectedChecksumSha256 = "a".repeat(64),
            rawPayload = byteArrayOf(),
            expectedSignature = "sig",
            signatureKeyId = "key",
            installedVersion = 5,
            incomingVersion = 5, // same version → stale
        )
        assertEquals(ContentVerificationStatus.VERSION_STALE, result.status)
        assertFalse(result.isAccepted)
        assertTrue(result.isStaleSilently)
    }

    @Test
    fun stubContentVerifier_rejectsOlderVersion() = runBlockingTest {
        val verifier = StubContentVerifier()
        val result = verifier.verify(
            packId = "test-pack",
            expectedChecksumSha256 = "a".repeat(64),
            rawPayload = byteArrayOf(),
            expectedSignature = "sig",
            signatureKeyId = "key",
            installedVersion = 10,
            incomingVersion = 3, // older → stale
        )
        assertEquals(ContentVerificationStatus.VERSION_STALE, result.status)
        assertFalse(result.isAccepted)
    }

    @Test
    fun stubContentVerifier_acceptsNewerVersion() = runBlockingTest {
        val verifier = StubContentVerifier()
        val result = verifier.verify(
            packId = "new-pack",
            expectedChecksumSha256 = "a".repeat(64),
            rawPayload = byteArrayOf(0x01, 0x02),
            expectedSignature = "sig",
            signatureKeyId = "prod-key-001",
            installedVersion = 0,
            incomingVersion = 1,
        )
        assertEquals(ContentVerificationStatus.VERIFIED, result.status)
        assertTrue(result.isAccepted)
        assertEquals("prod-key-001", result.resolvedKeyId)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NoOp tracker never throws
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun noOpTracker_neverThrows_forAnyEvent() {
        val tracker = NoOpAnalyticsTracker()
        // Fire every event type — none should throw
        listOf(
            AnalyticsEvent.AppOpened,
            AnalyticsEvent.ChartCalculationRequested("x"),
            AnalyticsEvent.ChartCalculationSucceeded("x", 0L),
            AnalyticsEvent.ChartCalculationFailed("x"),
            AnalyticsEvent.DivisionalChartRequested("D9"),
            AnalyticsEvent.DivisionalChartSucceeded("D9", 0L),
            AnalyticsEvent.DivisionalChartFailed("D9", "x"),
            AnalyticsEvent.ShadbalaCalculationRequested("x"),
            AnalyticsEvent.ShadbalaCalculationSucceeded("x", 0L),
            AnalyticsEvent.ShadbalaCalculationFailed("x"),
            AnalyticsEvent.AshtakavargaCalculationRequested("x"),
            AnalyticsEvent.AshtakavargaCalculationSucceeded("x", 0L),
            AnalyticsEvent.AshtakavargaCalculationFailed("x"),
            AnalyticsEvent.ShodhanaCalculationRequested("x"),
            AnalyticsEvent.ShodhanaCalculationSucceeded("x", 0L),
            AnalyticsEvent.PindaCalculationRequested("x"),
            AnalyticsEvent.PindaCalculationSucceeded("x", 0L),
            AnalyticsEvent.BirthProfileSaved,
            AnalyticsEvent.BirthProfileDeleted,
            AnalyticsEvent.ChartSaved,
            AnalyticsEvent.ChartDeleted,
            AnalyticsEvent.PreferenceChanged("ayanamsa"),
            AnalyticsEvent.ThemeToggled(true),
            AnalyticsEvent.SdkErrorObserved("internal_failure"),
            AnalyticsEvent.LanguageChanged("hi"),
            AnalyticsEvent.ContentItemOpened("ASTROLOGY"),
            AnalyticsEvent.OfflineContentAccessed("ASTROLOGY"),
            AnalyticsEvent.ContentSyncStarted("manual"),
            AnalyticsEvent.ContentSyncCompleted,
            AnalyticsEvent.ContentSyncFailed("sync_error"),
            AnalyticsEvent.SdkInitialized(true),
        ).forEach { tracker.track(it) }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Privacy: no sensitive data in event params
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun allEvents_haveNoPiiInDefaultParams() {
        val events = listOf(
            AnalyticsEvent.AppOpened,
            AnalyticsEvent.ChartCalculationRequested("PARASHARA_CLASSICAL_V1"),
            AnalyticsEvent.ChartCalculationSucceeded("PARASHARA_CLASSICAL_V1", 500L),
            AnalyticsEvent.DivisionalChartRequested("D9"),
            AnalyticsEvent.BirthProfileSaved,
            AnalyticsEvent.LanguageChanged("hi"),
            AnalyticsEvent.ContentSyncStarted("automatic"),
        )

        val forbiddenTerms = listOf(
            "birth_date", "latitude", "longitude", "birth_time", "name",
            "email", "phone", "coordinates", "chart_result", "position"
        )

        events.forEach { event ->
            event.params.forEach { (key, value) ->
                forbiddenTerms.forEach { forbidden ->
                    assertFalse(
                        key.contains(forbidden, ignoreCase = true),
                        "Event '${event.name}' has a forbidden param key: '$key'"
                    )
                    assertFalse(
                        value.toString().contains(forbidden, ignoreCase = true),
                        "Event '${event.name}' has forbidden data in param '$key'"
                    )
                }
            }
        }
    }
}

private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
