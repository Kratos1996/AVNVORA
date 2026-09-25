package com.aynvora.core.ai

import kotlinx.serialization.Serializable

/**
 * High-level outcome of internal automatic model selection.
 */
@Serializable
enum class AiModelSelectionStatus {
    READY_TO_DOWNLOAD,
    ALREADY_INSTALLED,
    INSUFFICIENT_STORAGE,
    INSUFFICIENT_MEMORY,
    UNSUPPORTED_PLATFORM,
    UNSUPPORTED_LANGUAGE,
    NO_COMPATIBLE_MODEL,
}

/**
 * User-facing summary devoid of raw technical jargon (no model weights, quantizations, or tensor formats).
 */
@Serializable
data class AiUserSelectionSummary(
    val title: String,
    val description: String,
    val downloadSizeFormatted: String,
    val languagesSupported: List<String>,
    val isActionable: Boolean,
    val actionLabel: String,
)

/**
 * Granular reason why a specific model candidate was eliminated during profiling.
 */
@Serializable
data class AiCandidateEvaluation(
    val modelId: String,
    val isCompatible: Boolean,
    val rejectionReasons: List<String> = emptyList(),
)

/**
 * Diagnostics output for logging, metrics, and developer inspection.
 */
@Serializable
data class AiSelectionDiagnostics(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val freeStorageMb: Long,
    val safeRamThresholdMb: Long,
    val cpuArchitecture: String,
    val candidateEvaluations: List<AiCandidateEvaluation>,
    val selectedModelId: String?,
)

/**
 * Result of the automatic model selection engine.
 */
@Serializable
data class AiModelSelectionResult(
    val status: AiModelSelectionStatus,
    val selectedModel: AiModelVariant?,
    val userSummary: AiUserSelectionSummary,
    val diagnostics: AiSelectionDiagnostics,
)

/**
 * Automatic On-Device AI Model Selection Engine.
 *
 * Implements the core principle: "You use AYNVORA. AYNVORA decides the AI."
 * The user never has to choose model sizes, quantizations, or technical parameters.
 */
class AiModelSelector(
    private val catalog: List<AiModelVariant> = AiModelCatalog.allVariants,
) {

    /**
     * Automatically evaluates the device profile and selects the safest, highest-quality model variant.
     *
     * @param profile The live detected hardware and platform profile.
     * @param targetLanguages Language codes needed by the user (defaulting to English and Hindi).
     * @param installedModelId If a model is already installed, its identifier.
     */
    fun selectOptimalModel(
        profile: AiDeviceProfile,
        targetLanguages: Set<String> = setOf("en", "hi"),
        installedModelId: String? = null,
    ): AiModelSelectionResult {
        val totalRamMb = profile.totalRamBytes / (1024L * 1024L)
        val availableRamMb = profile.availableRamBytes / (1024L * 1024L)
        val freeStorageMb = profile.freeStorageBytes / (1024L * 1024L)
        val safeRamThresholdMb = profile.maxSafeRamAllocationBytes / (1024L * 1024L)

        val candidateEvaluations = mutableListOf<AiCandidateEvaluation>()

        val compatibleCandidates = catalog.filter { variant ->
            val reasons = mutableListOf<String>()

            // 1. Runtime Compatibility
            if (!profile.supportedRuntimes.contains(variant.runtime)) {
                reasons.add("Runtime ${variant.runtime} unsupported on ${profile.osPlatform}")
            }

            // 2. CPU Architecture
            if (variant.parameterCountNumber > 1.0 && !profile.is64BitSupported) {
                reasons.add("Models > 1B require a 64-bit architecture; host is ${profile.cpuArchitecture}")
            }

            // 3. Language Support
            if (!variant.supportedLanguages.containsAll(targetLanguages)) {
                reasons.add("Variant does not support all target languages: $targetLanguages")
            }

            // 4. Memory Safety Threshold
            if (variant.minRamBytes > profile.maxSafeRamAllocationBytes) {
                reasons.add("Requires ${variant.minRamBytes / (1024L * 1024L)} MB RAM; safe threshold is $safeRamThresholdMb MB")
            }

            // 5. Free Storage Buffer
            if (variant.minFreeStorageBytes > profile.freeStorageBytes) {
                reasons.add("Requires ${variant.minFreeStorageBytes / (1024L * 1024L)} MB storage buffer; available is $freeStorageMb MB")
            }

            val isCompatible = reasons.isEmpty()
            candidateEvaluations.add(
                AiCandidateEvaluation(
                    modelId = variant.modelId,
                    isCompatible = isCompatible,
                    rejectionReasons = reasons,
                )
            )
            isCompatible
        }

        // Rank compatible candidates by:
        // 1. Parameter count (1.5B > 0.5B if resources allow)
        // 2. Quantization tier (Q8 > Q5 > Q4)
        val bestCandidate = compatibleCandidates
            .sortedWith(
                compareByDescending<AiModelVariant> { it.parameterCountNumber }
                    .thenByDescending { it.quantization.tierRank }
            )
            .firstOrNull()

        // Check if currently installed model matches the best candidate
        if (installedModelId != null && installedModelId == bestCandidate?.modelId) {
            val sizeMb = bestCandidate.displaySizeMb
            return AiModelSelectionResult(
                status = AiModelSelectionStatus.ALREADY_INSTALLED,
                selectedModel = bestCandidate,
                userSummary = AiUserSelectionSummary(
                    title = "AYNVORA AI Active",
                    description = "On-device intelligence is installed and running 100% offline.",
                    downloadSizeFormatted = "$sizeMb MB",
                    languagesSupported = listOf("English", "Hindi"),
                    isActionable = false,
                    actionLabel = "Installed",
                ),
                diagnostics = AiSelectionDiagnostics(
                    totalRamMb = totalRamMb,
                    availableRamMb = availableRamMb,
                    freeStorageMb = freeStorageMb,
                    safeRamThresholdMb = safeRamThresholdMb,
                    cpuArchitecture = profile.cpuArchitecture.name,
                    candidateEvaluations = candidateEvaluations,
                    selectedModelId = bestCandidate.modelId,
                ),
            )
        }

        if (bestCandidate != null) {
            val sizeMb = bestCandidate.displaySizeMb
            return AiModelSelectionResult(
                status = AiModelSelectionStatus.READY_TO_DOWNLOAD,
                selectedModel = bestCandidate,
                userSummary = AiUserSelectionSummary(
                    title = "AYNVORA AI Ready",
                    description = "Your device is fully capable of running private on-device intelligence.",
                    downloadSizeFormatted = "$sizeMb MB",
                    languagesSupported = listOf("English", "Hindi"),
                    isActionable = true,
                    actionLabel = "Download AYNVORA AI",
                ),
                diagnostics = AiSelectionDiagnostics(
                    totalRamMb = totalRamMb,
                    availableRamMb = availableRamMb,
                    freeStorageMb = freeStorageMb,
                    safeRamThresholdMb = safeRamThresholdMb,
                    cpuArchitecture = profile.cpuArchitecture.name,
                    candidateEvaluations = candidateEvaluations,
                    selectedModelId = bestCandidate.modelId,
                ),
            )
        }

        // Determine primary root cause for rejection
        val memoryFailureCount =
            candidateEvaluations.count { it.rejectionReasons.any { r -> r.contains("RAM") } }
        val storageFailureCount =
            candidateEvaluations.count { it.rejectionReasons.any { r -> r.contains("storage") } }
        val runtimeFailureCount =
            candidateEvaluations.count { it.rejectionReasons.any { r -> r.contains("Runtime") } }
        val languageFailureCount =
            candidateEvaluations.count { it.rejectionReasons.any { r -> r.contains("target languages") } }

        val status: AiModelSelectionStatus
        val userTitle: String
        val userDesc: String

        when {
            storageFailureCount == catalog.size -> {
                status = AiModelSelectionStatus.INSUFFICIENT_STORAGE
                userTitle = "More Storage Needed"
                userDesc =
                    "AYNVORA AI requires at least 1.1 GB of free storage space to download safely."
            }

            memoryFailureCount == catalog.size -> {
                status = AiModelSelectionStatus.INSUFFICIENT_MEMORY
                userTitle = "Low Memory Profile"
                userDesc =
                    "Device memory is currently constrained. AYNVORA will use deterministic wisdom instead."
            }

            runtimeFailureCount == catalog.size -> {
                status = AiModelSelectionStatus.UNSUPPORTED_PLATFORM
                userTitle = "Platform Unsupported"
                userDesc = "On-device AI runtime is not supported on this platform architecture."
            }

            languageFailureCount == catalog.size -> {
                status = AiModelSelectionStatus.UNSUPPORTED_LANGUAGE
                userTitle = "Language Not Available"
                userDesc = "The requested languages are not yet supported by on-device models."
            }

            else -> {
                status = AiModelSelectionStatus.NO_COMPATIBLE_MODEL
                userTitle = "AI Unavailable"
                userDesc =
                    "No suitable on-device AI profile could be safely configured for your device."
            }
        }

        return AiModelSelectionResult(
            status = status,
            selectedModel = null,
            userSummary = AiUserSelectionSummary(
                title = userTitle,
                description = userDesc,
                downloadSizeFormatted = "Unavailable",
                languagesSupported = listOf("English", "Hindi"),
                isActionable = false,
                actionLabel = "Unavailable",
            ),
            diagnostics = AiSelectionDiagnostics(
                totalRamMb = totalRamMb,
                availableRamMb = availableRamMb,
                freeStorageMb = freeStorageMb,
                safeRamThresholdMb = safeRamThresholdMb,
                cpuArchitecture = profile.cpuArchitecture.name,
                candidateEvaluations = candidateEvaluations,
                selectedModelId = null,
            ),
        )
    }
}
