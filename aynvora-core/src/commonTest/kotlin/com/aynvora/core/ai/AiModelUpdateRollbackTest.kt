package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 8.7 Model Update, Rollback & Deletion Lifecycle Tests.
 */
class AiModelUpdateRollbackTest {

    private val initialVariant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
    private val updateVariant = AiModelCatalog.QWEN_2_5_0_5B_Q5_K_M

    private class TestStorageRepository(
        var failChecksum: Boolean = false,
        var availableStorageBytes: Long = 16L * 1024L * 1024L * 1024L,
    ) : AiModelStorageRepository {
        private var installed: AiModelVariant? = null
        private val files = mutableMapOf<String, String>()

        override suspend fun getInstalledModel(): AiModelVariant? = installed

        override suspend fun isModelInstalled(modelId: String): Boolean =
            installed?.modelId == modelId

        override suspend fun getModelFilePath(modelId: String): String? = files[modelId]

        override suspend fun downloadModel(
            variant: AiModelVariant,
            onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
        ): AynvoraResult<String> {
            val required = variant.minFreeStorageBytes + (installed?.fileSizeBytes ?: 0L)
            if (availableStorageBytes < required) {
                return AynvoraResult.Failure.CalculationFailure(
                    "INSUFFICIENT_STORAGE",
                    "Not enough disk space"
                )
            }
            onProgress(variant.fileSizeBytes / 2, variant.fileSizeBytes)
            onProgress(variant.fileSizeBytes, variant.fileSizeBytes)
            return AynvoraResult.Success("/temp/${variant.modelId}.part")
        }

        override suspend fun verifyModelChecksum(
            tempFilePath: String,
            expectedSha256: String
        ): Boolean {
            return !failChecksum
        }

        override suspend fun commitInstallation(
            tempFilePath: String,
            variant: AiModelVariant
        ): AynvoraResult<String> {
            val path = "/models/${variant.modelId}.gguf"
            files[variant.modelId] = path
            installed = variant
            return AynvoraResult.Success(path)
        }

        override suspend fun deleteModel(modelId: String): AynvoraResult<Unit> {
            files.remove(modelId)
            if (installed?.modelId == modelId) {
                installed = null
            }
            return AynvoraResult.Success(Unit)
        }

        override suspend fun cleanupTempFiles() {}
    }

    @Test
    fun successfulModelInstallationLifecycle() = runBlocking {
        val repo = TestStorageRepository()
        val manager = AiModelLifecycleManager(repo)
        manager.initialize()
        assertEquals(AiModelLifecycleState.NotInstalled, manager.state.value)

        manager.downloadAndInstall(initialVariant)

        // Wait until Ready state
        var isReady = false
        for (i in 0 until 50) {
            val current = manager.state.value
            if (current is AiModelLifecycleState.Ready) {
                isReady = true
                assertEquals(initialVariant.modelId, current.installedVariant.modelId)
                break
            }
            kotlinx.coroutines.delay(10)
        }
        assertTrue(isReady, "Expected lifecycle manager to reach Ready state")
    }

    @Test
    fun failedUpdateRollsBackToPreviousModel() = runBlocking {
        val repo = TestStorageRepository()
        val manager = AiModelLifecycleManager(repo)

        // 1. Install initial model successfully
        manager.downloadAndInstall(initialVariant)
        for (i in 0 until 50) {
            if (manager.state.value is AiModelLifecycleState.Ready) break
            kotlinx.coroutines.delay(10)
        }
        assertTrue(manager.state.value is AiModelLifecycleState.Ready)

        // 2. Attempt update to new variant, but simulate checksum corruption
        repo.failChecksum = true
        manager.downloadAndInstall(updateVariant)

        var hasError = false
        for (i in 0 until 50) {
            val current = manager.state.value
            if (current is AiModelLifecycleState.Error) {
                hasError = true
                assertEquals(
                    AiModelLifecycleState.Error.FailureReason.CHECKSUM_MISMATCH,
                    current.failureReason
                )
                assertNotNull(current.previousReadyState)
                assertEquals(
                    initialVariant.modelId,
                    current.previousReadyState?.installedVariant?.modelId
                )
                break
            }
            kotlinx.coroutines.delay(10)
        }
        assertTrue(hasError, "Expected checksum error during corrupt update")

        // 3. Rollback to previous model
        val rollbackSuccess = manager.rollbackToPrevious()
        assertTrue(rollbackSuccess)
        val stateAfterRollback = manager.state.value
        assertTrue(stateAfterRollback is AiModelLifecycleState.Ready)
        assertEquals(
            initialVariant.modelId,
            (stateAfterRollback as AiModelLifecycleState.Ready).installedVariant.modelId
        )
    }

    @Test
    fun deleteInstalledModelClearsStateAndPreventsSilentRedownload() = runBlocking {
        val repo = TestStorageRepository()
        val manager = AiModelLifecycleManager(repo)

        // Install model
        manager.downloadAndInstall(initialVariant)
        for (i in 0 until 50) {
            if (manager.state.value is AiModelLifecycleState.Ready) break
            kotlinx.coroutines.delay(10)
        }
        assertTrue(manager.state.value is AiModelLifecycleState.Ready)

        // Delete model
        val deleteResult = manager.deleteInstalledModel()
        assertTrue(deleteResult is AynvoraResult.Success)
        assertEquals(AiModelLifecycleState.NotInstalled, manager.state.value)
        assertFalse(repo.isModelInstalled(initialVariant.modelId))

        // Confirm rollback is not possible after deletion
        val rollbackResult = manager.rollbackToPrevious()
        assertFalse(rollbackResult)
        assertEquals(AiModelLifecycleState.NotInstalled, manager.state.value)
    }

    @Test
    fun insufficientStorageRejectsDownload() = runBlocking {
        // Storage is constrained (only 500MB free)
        val repo = TestStorageRepository(availableStorageBytes = 500L * 1024L * 1024L)
        val manager = AiModelLifecycleManager(repo)

        manager.downloadAndInstall(initialVariant)
        for (i in 0 until 50) {
            val current = manager.state.value
            if (current is AiModelLifecycleState.Error) {
                assertEquals(
                    AiModelLifecycleState.Error.FailureReason.NETWORK_FAILED,
                    current.failureReason
                )
                break
            }
            kotlinx.coroutines.delay(10)
        }
        assertTrue(manager.state.value is AiModelLifecycleState.Error)
    }
}
