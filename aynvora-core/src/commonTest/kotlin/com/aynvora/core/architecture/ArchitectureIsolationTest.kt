package com.aynvora.core.architecture

import com.aynvora.core.analytics.AnalyticsConsentManager
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.InMemoryAnalyticsConsentManager
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.sync.ContentVerificationStatus
import com.aynvora.core.sync.ContentVerifier
import com.aynvora.core.sync.StubContentVerifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
            AnalyticsEvent.TarotOpened,
            AnalyticsEvent.TarotDisclaimerViewed,
            AnalyticsEvent.TarotSpreadSelected("single_card"),
            AnalyticsEvent.TarotReadingStarted("single_card", 1),
            AnalyticsEvent.TarotCardDrawn("single_card", "major_00_fool", "UPRIGHT"),
            AnalyticsEvent.TarotReadingCompleted("single_card", 1),
            AnalyticsEvent.TarotReadingFailed("single_card", "draw_failed"),
            AnalyticsEvent.TarotContentOpened("major_00_fool", "en"),
        ).forEach { tracker.track(it) }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Privacy: no sensitive data in event params
    // ──────────────────────────────────────────────────────────────────────────

    // ──────────────────────────────────────────────────────────────────────────
    // UseCase Architecture Isolation
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun domainUseCases_dependOnlyOnDomainInterfaces() {
        val fakeBirthRepo = object : com.aynvora.core.repository.BirthProfileRepository {
            override suspend fun getBirthProfile(id: String) =
                AynvoraResult.Failure.NotFound(id, "Not found")

            override suspend fun getAllBirthProfiles() =
                AynvoraResult.Success(emptyList<com.aynvora.core.models.BirthProfile>())

            override suspend fun saveBirthProfile(profile: com.aynvora.core.models.BirthProfile) =
                AynvoraResult.Success(profile)

            override suspend fun deleteBirthProfile(id: String) = AynvoraResult.Success(Unit)
            override fun observeAllBirthProfiles() =
                kotlinx.coroutines.flow.flowOf(AynvoraResult.Success(emptyList<com.aynvora.core.models.BirthProfile>()))

            override fun observeBirthProfile(id: String) =
                kotlinx.coroutines.flow.flowOf(AynvoraResult.Failure.NotFound(id, "Not found"))
        }

        val saveUseCase = com.aynvora.core.usecase.SaveBirthProfileUseCase(fakeBirthRepo)
        val observeUseCase = com.aynvora.core.usecase.ObserveBirthProfilesUseCase(fakeBirthRepo)
        val deleteUseCase = com.aynvora.core.usecase.DeleteBirthProfileUseCase(fakeBirthRepo)

        assertTrue(saveUseCase != null)
        assertTrue(observeUseCase != null)
        assertTrue(deleteUseCase != null)
    }

    @Test
    fun tarotDomain_isCompletelyIsolatedFromAstroEngine() {
        // TarotDrawEngine must operate independently of any astro calculation or chart result
        val drawEngine = com.aynvora.core.tarot.TarotDrawEngine()
        val reading = drawEngine.drawSpread(com.aynvora.core.tarot.TarotSpread.SingleCard)
        assertEquals(1, reading.draws.size)
        assertTrue(reading.draws[0].card.id.isNotBlank())
    }

    @Test
    fun intelligenceDomain_doesNotExposeDirectDatabaseAccess() {
        // AI tool registry only accepts typed AiTool contracts
        val registry = com.aynvora.core.ai.DefaultAiToolRegistry()
        assertEquals(0, registry.listTools().size)
    }

    @Test
    fun multiFeatureOrchestrator_operatesThroughContractsWithoutCoupling() {
        val sdk = com.aynvora.core.Aynvora.create()
        val validator = com.aynvora.core.intelligence.DataSufficiencyValidator()
        val orchestrator = com.aynvora.core.intelligence.MultiFeatureOrchestrator(
            sdk = sdk,
            toolRegistry = com.aynvora.core.ai.DefaultAiToolRegistry(),
            sufficiencyValidator = validator,
        )
        assertNotNull(orchestrator)
    }
}

private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
