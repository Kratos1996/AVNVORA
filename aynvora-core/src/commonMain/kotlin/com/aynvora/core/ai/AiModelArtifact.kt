package com.aynvora.core.ai

import kotlinx.serialization.Serializable

/**
 * Verified metadata, licensing, and provenance for an on-device GGUF model artifact.
 *
 * Implements Section 3 of Phase 10.26.
 */
@Serializable
data class AiModelArtifact(
    val modelId: String,
    val filename: String,
    val sourceUrl: String,
    val revision: String,
    val sizeBytes: Long,
    val sha256: String,
    val license: String,
    val rightsStatus: String,
    val tokenizerInfo: String,
    val architecture: String,
    val quantization: String,
    val contextLength: Int,
)
