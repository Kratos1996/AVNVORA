package com.aynvora.core.intelligence

import kotlinx.serialization.Serializable

/**
 * Origin of verification evidence.
 */
enum class EvidenceSource {
    PHYSICAL_HARDWARE,
    LOCAL_SYNTHETIC,
    MOCK,
}

/**
 * Outcome of a qualification scenario.
 */
enum class QualificationResult {
    PASS,
    FAIL,
    BLOCKED,
}

/**
 * Authoritative record of capability qualification on a device.
 * Excludes raw pixels, image buffers, base64 strings, and biometric coordinates.
 */
@Serializable
data class CapabilityQualificationRecord(
    val qualificationId: String,
    val capabilityId: String,
    val device: String,
    val androidVersion: String,
    val appVersion: String,
    val timestampEpochMs: Long,
    val testScenario: String,
    val result: QualificationResult,
    val confidence: Float? = null,
    val durationMs: Long = 0,
    val error: String? = null,
    val evidenceSource: EvidenceSource,
    val physicalDeviceRequired: Boolean,
    val physicalDeviceVerified: Boolean,
    val artifactReference: String? = null,
) {
    init {
        // Enforce privacy invariant: artifactReference cannot contain raw encoded imagery or coordinates
        if (artifactReference != null) {
            require(!artifactReference.contains("base64", ignoreCase = true)) {
                "artifactReference must not contain base64 image data"
            }
            require(!artifactReference.contains("data:image", ignoreCase = true)) {
                "artifactReference must not contain raw image URI"
            }
        }
    }
}
