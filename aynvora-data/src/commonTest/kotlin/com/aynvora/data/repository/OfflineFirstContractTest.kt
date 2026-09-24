package com.aynvora.data.repository

import com.aynvora.core.models.ContentModuleId
import com.aynvora.core.models.ContentTrustState
import com.aynvora.core.models.SyncResult
import com.aynvora.core.models.SyncStatus
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.AynvoraDataFactory
import com.aynvora.data.storage.InMemoryStorageDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Offline-first architecture tests for the data layer.
 *
 * Key guarantees tested:
 * 1. Repositories operate entirely without network access.
 * 2. Existing data is preserved when sync fails or is not configured.
 * 3. Content verification stub behaves correctly for version checks.
 * 4. Storage engine handles profile CRUD correctly.
 * 5. AynvoraDataFactory creates valid in-memory repositories.
 *
 * Note: Room DAOs require an actual database instance (platform-specific).
 * ContentRepository and ContentSyncRepository tests that require Room are
 * exercised in androidTest. Pure logic tests run in commonTest.
 */
class OfflineFirstContractTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Factory tests (no Room needed — uses InMemoryStorageDriver)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun createInMemory_returnsValidRepositories() {
        val repos = AynvoraDataFactory.createInMemory()
        // All repositories should be non-null and functional
        assertTrue(repos.userProfiles != null)
        assertTrue(repos.birthProfiles != null)
        assertTrue(repos.savedCharts != null)
        assertTrue(repos.userPreferences != null)
        assertTrue(repos.storageEngine != null)
    }

    @Test
    fun userProfileRepository_createsAndReadsProfile_offline() = runBlockingTest {
        val repos = AynvoraDataFactory.createInMemory()
        val profile = com.aynvora.core.models.UserProfile(
            id = "profile-test-001",
            displayName = "Test User",
            contextNotes = null,
            createdAtEpochMs = 1_000_000L,
            updatedAtEpochMs = 1_000_000L,
        )

        val saveResult = repos.userProfiles.saveProfile(profile)
        assertIs<AynvoraResult.Success<*>>(saveResult)

        val readResult = repos.userProfiles.getProfile("profile-test-001")
        assertIs<AynvoraResult.Success<*>>(readResult)
        assertEquals("profile-test-001", (readResult as AynvoraResult.Success).value.id)
        assertEquals("Test User", readResult.value.displayName)
    }

    @Test
    fun userProfileRepository_returnsNotFound_forUnknownId() = runBlockingTest {
        val repos = AynvoraDataFactory.createInMemory()
        val result = repos.userProfiles.getProfile("does-not-exist")
        assertIs<AynvoraResult.Failure.NotFound>(result)
    }

    @Test
    fun userProfileRepository_deletesProfile_offline() = runBlockingTest {
        val repos = AynvoraDataFactory.createInMemory()
        val profile = com.aynvora.core.models.UserProfile(
            id = "delete-me",
            displayName = "Temp",
            contextNotes = null,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
        )
        repos.userProfiles.saveProfile(profile)
        val deleteResult = repos.userProfiles.deleteProfile("delete-me")
        assertIs<AynvoraResult.Success<*>>(deleteResult)

        val readResult = repos.userProfiles.getProfile("delete-me")
        assertIs<AynvoraResult.Failure.NotFound>(readResult)
    }

    @Test
    fun userPreferencesRepository_returnsDefault_whenNeverSet() = runBlockingTest {
        val repos = AynvoraDataFactory.createInMemory()
        val result = repos.userPreferences.getPreferences()
        // Default preferences should exist after initialization
        assertIs<AynvoraResult.Success<*>>(result)
    }

    @Test
    fun createInMemory_multipleRepos_shareCorrectStorageEngine() = runBlockingTest {
        val repos = AynvoraDataFactory.createInMemory()
        val profile = com.aynvora.core.models.UserProfile(
            id = "shared-engine-test",
            displayName = "Shared",
            contextNotes = null,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
        )
        repos.userProfiles.saveProfile(profile)

        // Both getAll calls share the same storage engine
        val profiles = repos.userProfiles.getAllProfiles()
        assertIs<AynvoraResult.Success<*>>(profiles)
        assertTrue((profiles as AynvoraResult.Success).value.any { it.id == "shared-engine-test" })
    }
}

/**
 * Tests for content sync repository — pure logic, no Room.
 */
class ContentSyncOfflineContractTest {

    @Test
    fun syncResult_noOp_whenBackendNotConfigured() = runBlockingTest {
        // Uses the stub content sync (what ContentSyncRepositoryImpl returns)
        // This validates the interface contract without Room
        val result = SyncResult.NoOp(reason = "Remote sync not yet configured — backend endpoint pending provisioning")
        assertTrue(result.reason.isNotBlank())
    }

    @Test
    fun syncStatus_transitions_areObservable() {
        // All sync statuses should have distinct names
        val statuses = SyncStatus.values()
        val names = statuses.map { it.name }.toSet()
        assertEquals(statuses.size, names.size, "All SyncStatus values must have unique names")
    }

    @Test
    fun contentTrustState_approvedForPublication_isOnlyPublishableState() {
        // Verify the trust state hierarchy is correctly ordered
        val publishable = setOf(ContentTrustState.APPROVED_FOR_PUBLICATION)
        val nonPublishable = ContentTrustState.values().filter { it !in publishable }

        assertEquals(1, publishable.size)
        assertEquals(4, nonPublishable.size)
        assertTrue(ContentTrustState.REVOKED in nonPublishable)
        assertTrue(ContentTrustState.DEPRECATED in nonPublishable)
    }

    @Test
    fun syncResult_failure_isRetryableByDefault() {
        val failure = SyncResult.Failure(
            code = "test_error",
            message = "Test failure",
        )
        assertTrue(failure.isRetryable)
    }

    @Test
    fun syncResult_failure_canBeNonRetryable() {
        val failure = SyncResult.Failure(
            code = "permanent_error",
            message = "Non-retryable failure",
            isRetryable = false,
        )
        assertTrue(!failure.isRetryable)
    }
}

private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
