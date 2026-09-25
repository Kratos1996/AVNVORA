package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Observable lifecycle state of the on-device AI model.
 */
sealed class AiModelLifecycleState {
    object NotInstalled : AiModelLifecycleState()

    data class Downloading(
        val progressFraction: Float, // 0.0f to 1.0f
        val bytesDownloaded: Long,
        val totalBytes: Long,
    ) : AiModelLifecycleState() {
        val percentage: Int
            get() = (progressFraction * 100).toInt().coerceIn(0, 100)
    }

    object VerifyingChecksum : AiModelLifecycleState()

    object Installing : AiModelLifecycleState()

    data class Ready(
        val installedVariant: AiModelVariant,
        val localFilePath: String,
    ) : AiModelLifecycleState()

    data class Error(
        val message: String,
        val isRetryable: Boolean = true,
        val failureReason: FailureReason = FailureReason.UNKNOWN,
        val previousReadyState: Ready? = null,
    ) : AiModelLifecycleState() {
        enum class FailureReason {
            CHECKSUM_MISMATCH,
            NETWORK_FAILED,
            STORAGE_FULL,
            CORRUPTED_PACKAGE,
            USER_CANCELLED,
            UNKNOWN,
        }
    }
}

/**
 * Storage and file I/O operations for model artifacts.
 */
interface AiModelStorageRepository {
    suspend fun getInstalledModel(): AiModelVariant?
    suspend fun isModelInstalled(modelId: String): Boolean
    suspend fun getModelFilePath(modelId: String): String?
    suspend fun downloadModel(
        variant: AiModelVariant,
        onProgress: (bytesDownloaded: Long, totalBytes: Long) -> Unit,
    ): AynvoraResult<String> // Returns temp file path

    suspend fun verifyModelChecksum(tempFilePath: String, expectedSha256: String): Boolean
    suspend fun commitInstallation(
        tempFilePath: String,
        variant: AiModelVariant
    ): AynvoraResult<String> // Returns final installed path

    suspend fun deleteModel(modelId: String): AynvoraResult<Unit>
    suspend fun cleanupTempFiles()
}

/**
 * Manager orchestrating the model download, verification, atomic installation, rollback, and removal lifecycle.
 */
class AiModelLifecycleManager(
    private val storageRepository: AiModelStorageRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow<AiModelLifecycleState>(AiModelLifecycleState.NotInstalled)
    val state: StateFlow<AiModelLifecycleState> = _state.asStateFlow()

    private val mutex = Mutex()
    private var activeDownloadJob: Job? = null
    private var lastReadyState: AiModelLifecycleState.Ready? = null

    suspend fun initialize() {
        mutex.withLock {
            val installed = storageRepository.getInstalledModel()
            if (installed != null) {
                val path = storageRepository.getModelFilePath(installed.modelId)
                if (path != null) {
                    val readyState = AiModelLifecycleState.Ready(installed, path)
                    lastReadyState = readyState
                    _state.value = readyState
                    return
                }
            }
            _state.value = AiModelLifecycleState.NotInstalled
        }
    }

    fun downloadAndInstall(variant: AiModelVariant) {
        scope.launch {
            mutex.withLock {
                if (_state.value is AiModelLifecycleState.Downloading ||
                    _state.value is AiModelLifecycleState.VerifyingChecksum ||
                    _state.value is AiModelLifecycleState.Installing
                ) {
                    return@launch // Already in progress
                }

                // Preserve previous ready model if updating
                val current = _state.value
                if (current is AiModelLifecycleState.Ready) {
                    lastReadyState = current
                }

                _state.value = AiModelLifecycleState.Downloading(0.0f, 0L, variant.fileSizeBytes)
            }

            activeDownloadJob = launch {
                try {
                    // 1. Download to temporary directory
                    val downloadResult =
                        storageRepository.downloadModel(variant) { downloaded, total ->
                            val fraction =
                                if (total > 0) (downloaded.toFloat() / total.toFloat()).coerceIn(
                                    0.0f,
                                    1.0f
                                ) else 0.0f
                            _state.value =
                                AiModelLifecycleState.Downloading(fraction, downloaded, total)
                        }

                    if (downloadResult is AynvoraResult.Failure) {
                        _state.value = AiModelLifecycleState.Error(
                            message = downloadResult.message,
                            isRetryable = true,
                            failureReason = AiModelLifecycleState.Error.FailureReason.NETWORK_FAILED,
                            previousReadyState = lastReadyState,
                        )
                        return@launch
                    }

                    val tempFilePath = (downloadResult as AynvoraResult.Success).value

                    // 2. Cryptographic Checksum Verification
                    _state.value = AiModelLifecycleState.VerifyingChecksum
                    val isChecksumValid =
                        storageRepository.verifyModelChecksum(tempFilePath, variant.sha256Checksum)

                    if (!isChecksumValid) {
                        storageRepository.cleanupTempFiles()
                        _state.value = AiModelLifecycleState.Error(
                            message = "Integrity check failed: Model checksum mismatch.",
                            isRetryable = true,
                            failureReason = AiModelLifecycleState.Error.FailureReason.CHECKSUM_MISMATCH,
                            previousReadyState = lastReadyState,
                        )
                        return@launch
                    }

                    // 3. Atomic Installation
                    _state.value = AiModelLifecycleState.Installing
                    val commitResult = storageRepository.commitInstallation(tempFilePath, variant)

                    when (commitResult) {
                        is AynvoraResult.Success -> {
                            val newReadyState =
                                AiModelLifecycleState.Ready(variant, commitResult.value)
                            lastReadyState = newReadyState
                            _state.value = newReadyState
                        }

                        is AynvoraResult.Failure -> {
                            storageRepository.cleanupTempFiles()
                            _state.value = AiModelLifecycleState.Error(
                                message = "Installation failed: ${commitResult.message}",
                                isRetryable = true,
                                failureReason = AiModelLifecycleState.Error.FailureReason.CORRUPTED_PACKAGE,
                                previousReadyState = lastReadyState,
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    storageRepository.cleanupTempFiles()
                    _state.value = AiModelLifecycleState.Error(
                        message = "Download cancelled by user.",
                        isRetryable = true,
                        failureReason = AiModelLifecycleState.Error.FailureReason.USER_CANCELLED,
                        previousReadyState = lastReadyState,
                    )
                } catch (t: Throwable) {
                    storageRepository.cleanupTempFiles()
                    _state.value = AiModelLifecycleState.Error(
                        message = t.message ?: "Unexpected error during model setup",
                        isRetryable = true,
                        failureReason = AiModelLifecycleState.Error.FailureReason.UNKNOWN,
                        previousReadyState = lastReadyState,
                    )
                } finally {
                    activeDownloadJob = null
                }
            }
        }
    }

    /**
     * Rolls back to previous successfully installed model variant if an update failed.
     */
    suspend fun rollbackToPrevious(): Boolean {
        return mutex.withLock {
            val previous = lastReadyState
            if (previous != null && storageRepository.isModelInstalled(previous.installedVariant.modelId)) {
                _state.value = previous
                true
            } else {
                false
            }
        }
    }

    suspend fun cancelDownload() {
        mutex.withLock {
            activeDownloadJob?.cancel()
            activeDownloadJob = null
            storageRepository.cleanupTempFiles()
            _state.value = lastReadyState ?: AiModelLifecycleState.NotInstalled
        }
    }

    suspend fun deleteInstalledModel(): AynvoraResult<Unit> {
        return mutex.withLock {
            val currentState = _state.value
            lastReadyState = null
            if (currentState is AiModelLifecycleState.Ready) {
                val deleteResult =
                    storageRepository.deleteModel(currentState.installedVariant.modelId)
                if (deleteResult is AynvoraResult.Success) {
                    _state.value = AiModelLifecycleState.NotInstalled
                }
                deleteResult
            } else {
                _state.value = AiModelLifecycleState.NotInstalled
                AynvoraResult.Success(Unit)
            }
        }
    }
}


