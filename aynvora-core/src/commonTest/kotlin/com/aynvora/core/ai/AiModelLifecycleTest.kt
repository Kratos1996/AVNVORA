package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FakeAiModelStorageRepository(
    var shouldFailDownload: Boolean = false,
    var shouldFailChecksum: Boolean = false,
    var shouldFailCommit: Boolean = false,
) : AiModelStorageRepository {

    private var installedVariant: AiModelVariant? = null
    val cleanedUpTempFiles = mutableListOf<String>()

    override suspend fun getInstalledModel(): AiModelVariant? = installedVariant

    override suspend fun isModelInstalled(modelId: String): Boolean =
        installedVariant?.modelId == modelId

    override suspend fun getModelFilePath(modelId: String): String? =
        if (installedVariant?.modelId == modelId) "/models/$modelId.gguf" else null

    override suspend fun downloadModel(
        variant: AiModelVariant,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): AynvoraResult<String> {
        if (shouldFailDownload) {
            return AynvoraResult.Failure.InternalFailure("Network simulated connection reset")
        }
        onProgress(variant.fileSizeBytes / 2, variant.fileSizeBytes)
        onProgress(variant.fileSizeBytes, variant.fileSizeBytes)
        return AynvoraResult.Success("/tmp/temp_${variant.modelId}.part")
    }

    override suspend fun verifyModelChecksum(
        tempFilePath: String,
        expectedSha256: String
    ): Boolean {
        return !shouldFailChecksum
    }

    override suspend fun commitInstallation(
        tempFilePath: String,
        variant: AiModelVariant,
    ): AynvoraResult<String> {
        if (shouldFailCommit) {
            return AynvoraResult.Failure.InternalFailure("Disk I/O error on commit")
        }
        installedVariant = variant
        return AynvoraResult.Success("/models/${variant.modelId}.gguf")
    }

    override suspend fun deleteModel(modelId: String): AynvoraResult<Unit> {
        if (installedVariant?.modelId == modelId) {
            installedVariant = null
        }
        return AynvoraResult.Success(Unit)
    }

    override suspend fun cleanupTempFiles() {
        cleanedUpTempFiles.add("cleaned")
    }
}

class AiModelLifecycleTest {

    @Test
    fun successfulDownloadVerificationAndInstallationLifecycle() = runBlocking {
        val repo = FakeAiModelStorageRepository()
        val manager = AiModelLifecycleManager(repo, this)

        manager.initialize()
        assertEquals(AiModelLifecycleState.NotInstalled, manager.state.value)

        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        manager.downloadAndInstall(variant)

        // Wait briefly for launch to complete
        kotlinx.coroutines.delay(100)

        val currentState = manager.state.value
        assertIs<AiModelLifecycleState.Ready>(currentState)
        assertEquals(variant.modelId, currentState.installedVariant.modelId)
        assertEquals("/models/${variant.modelId}.gguf", currentState.localFilePath)
    }

    @Test
    fun checksumMismatchTriggersErrorAndCleanup() = runBlocking {
        val repo = FakeAiModelStorageRepository(shouldFailChecksum = true)
        val manager = AiModelLifecycleManager(repo, this)

        manager.downloadAndInstall(AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M)
        kotlinx.coroutines.delay(100)

        val currentState = manager.state.value
        assertIs<AiModelLifecycleState.Error>(currentState)
        assertEquals(
            AiModelLifecycleState.Error.FailureReason.CHECKSUM_MISMATCH,
            currentState.failureReason
        )
        assertTrue(repo.cleanedUpTempFiles.isNotEmpty())
    }

    @Test
    fun deletionResetsStateToNotInstalled() = runBlocking {
        val repo = FakeAiModelStorageRepository()
        val manager = AiModelLifecycleManager(repo, this)

        manager.downloadAndInstall(AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M)
        kotlinx.coroutines.delay(100)
        assertIs<AiModelLifecycleState.Ready>(manager.state.value)

        val deleteResult = manager.deleteInstalledModel()
        assertIs<AynvoraResult.Success<Unit>>(deleteResult)
        assertEquals(AiModelLifecycleState.NotInstalled, manager.state.value)
    }
}
