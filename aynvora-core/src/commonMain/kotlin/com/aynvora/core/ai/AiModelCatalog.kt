package com.aynvora.core.ai

import kotlinx.serialization.Serializable

/**
 * Model quantization levels supported for on-device inference.
 */
@Serializable
enum class AiQuantization {
    Q4_K_M,
    Q5_K_M,
    Q8_0;

    val tierRank: Int
        get() = when (this) {
            Q8_0 -> 3 // Highest fidelity, highest memory
            Q5_K_M -> 2 // High fidelity
            Q4_K_M -> 1 // Balanced baseline
        }
}

/**
 * Model family architectures.
 */
@Serializable
enum class AiModelFamily {
    QWEN_2_5,
}

/**
 * Technical specification for an on-device SLM variant.
 *
 * @param modelId Unique identifier in the catalog.
 * @param name Human-readable model variant name.
 * @param family Model family.
 * @param parameterCount Parameter count string (e.g. "0.5B", "1.5B").
 * @param parameterCountNumber Approximate parameter count in billions.
 * @param quantization Quantization level.
 * @param runtime Required execution runtime.
 * @param downloadUrl Verified download location for model weights.
 * @param fileSizeBytes Exact file size in bytes.
 * @param sha256Checksum Exact cryptographic SHA-256 hash.
 * @param minRamBytes Minimum safe available RAM required to load this model.
 * @param minFreeStorageBytes Minimum free storage required (includes staging buffer + safety margin).
 * @param defaultContextLength Context window size in tokens.
 * @param supportedLanguages ISO 639-1 language codes verified for this model.
 * @param license Open-source license governing model weights.
 */
@Serializable
data class AiModelVariant(
    val modelId: String,
    val name: String,
    val family: AiModelFamily,
    val parameterCount: String,
    val parameterCountNumber: Double,
    val quantization: AiQuantization,
    val runtime: AiRuntimeType,
    val downloadUrl: String,
    val fileSizeBytes: Long,
    val sha256Checksum: String,
    val minRamBytes: Long,
    val minFreeStorageBytes: Long,
    val defaultContextLength: Int = 2048,
    val supportedLanguages: Set<String> = setOf("en", "hi"),
    val license: String = "Apache-2.0",
    val format: String = "GGUF",
    val version: String = "1.0.0",
    val recommendedRamBytes: Long = minRamBytes * 2,
    val supportedAbis: Set<String> = setOf("arm64-v8a", "x86_64"),
) {
    val displaySizeMb: Int
        get() = (fileSizeBytes / (1024L * 1024L)).toInt()

    fun toModelInfo(isAvailableLocally: Boolean): AiModelInfo {
        return AiModelInfo(
            modelId = modelId,
            name = name,
            parameterCount = parameterCount,
            quantizationFormat = quantization.name,
            fileSizeBytes = fileSizeBytes,
            license = license,
            isAvailableLocally = isAvailableLocally,
        )
    }
}

/**
 * Catalog containing all officially verified on-device models with cryptographic checksums.
 */
object AiModelCatalog {

    val QWEN_2_5_0_5B_Q4_K_M = AiModelVariant(
        modelId = "qwen2.5-0.5b-instruct-q4_k_m",
        name = "Qwen2.5 0.5B Instruct (Q4_K_M)",
        family = AiModelFamily.QWEN_2_5,
        parameterCount = "0.5B",
        parameterCountNumber = 0.5,
        quantization = AiQuantization.Q4_K_M,
        runtime = AiRuntimeType.GGUF,
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
        fileSizeBytes = 491400032L, // 468.6 MB
        sha256Checksum = "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db",
        minRamBytes = 500L * 1024L * 1024L, // 500 MB
        minFreeStorageBytes = 1080L * 1024L * 1024L, // 1.05 GB safe storage
        defaultContextLength = 2048,
        supportedLanguages = setOf("en", "hi"),
        license = "Apache-2.0",
    )

    val QWEN_2_5_0_5B_Q5_K_M = AiModelVariant(
        modelId = "qwen2.5-0.5b-instruct-q5_k_m",
        name = "Qwen2.5 0.5B Instruct (Q5_K_M)",
        family = AiModelFamily.QWEN_2_5,
        parameterCount = "0.5B",
        parameterCountNumber = 0.5,
        quantization = AiQuantization.Q5_K_M,
        runtime = AiRuntimeType.GGUF,
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q5_k_m.gguf",
        fileSizeBytes = 522186592L, // 498.0 MB
        sha256Checksum = "041474553fcabfc2a2d67903f9d2c2e50bd92528e670da4f33b5d0ce6e59fd55",
        minRamBytes = 600L * 1024L * 1024L, // 600 MB
        minFreeStorageBytes = 1150L * 1024L * 1024L, // 1.12 GB safe storage
        defaultContextLength = 2048,
        supportedLanguages = setOf("en", "hi"),
        license = "Apache-2.0",
    )

    val QWEN_2_5_0_5B_Q8_0 = AiModelVariant(
        modelId = "qwen2.5-0.5b-instruct-q8_0",
        name = "Qwen2.5 0.5B Instruct (Q8_0)",
        family = AiModelFamily.QWEN_2_5,
        parameterCount = "0.5B",
        parameterCountNumber = 0.5,
        quantization = AiQuantization.Q8_0,
        runtime = AiRuntimeType.GGUF,
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q8_0.gguf",
        fileSizeBytes = 675710816L, // 644.4 MB
        sha256Checksum = "ca59ca7f13d0e15a8cfa77bd17e65d24f6844b554a7b6c12e07a5f89ff76844e",
        minRamBytes = 850L * 1024L * 1024L, // 850 MB
        minFreeStorageBytes = 1500L * 1024L * 1024L, // 1.46 GB safe storage
        defaultContextLength = 2048,
        supportedLanguages = setOf("en", "hi"),
        license = "Apache-2.0",
    )

    val QWEN_2_5_1_5B_Q4_K_M = AiModelVariant(
        modelId = "qwen2.5-1.5b-instruct-q4_k_m",
        name = "Qwen2.5 1.5B Instruct (Q4_K_M)",
        family = AiModelFamily.QWEN_2_5,
        parameterCount = "1.5B",
        parameterCountNumber = 1.5,
        quantization = AiQuantization.Q4_K_M,
        runtime = AiRuntimeType.GGUF,
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
        fileSizeBytes = 1117320736L, // 1065.5 MB (~1.04 GB)
        sha256Checksum = "6a1a2eb6d15622bf3c96857206351ba97e1af16c30d7a74ee38970e434e9407e",
        minRamBytes = 1500L * 1024L * 1024L, // 1.5 GB
        minFreeStorageBytes = 2500L * 1024L * 1024L, // 2.44 GB safe storage
        defaultContextLength = 2048,
        supportedLanguages = setOf("en", "hi"),
        license = "Apache-2.0",
    )

    val QWEN_2_5_1_5B_Q5_K_M = AiModelVariant(
        modelId = "qwen2.5-1.5b-instruct-q5_k_m",
        name = "Qwen2.5 1.5B Instruct (Q5_K_M)",
        family = AiModelFamily.QWEN_2_5,
        parameterCount = "1.5B",
        parameterCountNumber = 1.5,
        quantization = AiQuantization.Q5_K_M,
        runtime = AiRuntimeType.GGUF,
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q5_k_m.gguf",
        fileSizeBytes = 1285494304L, // 1225.9 MB (~1.20 GB)
        sha256Checksum = "b46661073c18e5b56a41fa320975f866a00def1ff08feef4718e013258896f8c",
        minRamBytes = 1800L * 1024L * 1024L, // 1.8 GB
        minFreeStorageBytes = 2850L * 1024L * 1024L, // 2.78 GB safe storage
        defaultContextLength = 2048,
        supportedLanguages = setOf("en", "hi"),
        license = "Apache-2.0",
    )

    val allVariants: List<AiModelVariant> = listOf(
        QWEN_2_5_0_5B_Q4_K_M,
        QWEN_2_5_0_5B_Q5_K_M,
        QWEN_2_5_0_5B_Q8_0,
        QWEN_2_5_1_5B_Q4_K_M,
        QWEN_2_5_1_5B_Q5_K_M,
    )

    fun findById(modelId: String): AiModelVariant? {
        return allVariants.firstOrNull { it.modelId == modelId }
    }
}
