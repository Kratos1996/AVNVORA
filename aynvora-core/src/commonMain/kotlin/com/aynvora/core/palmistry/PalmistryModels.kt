package com.aynvora.core.palmistry

import com.aynvora.core.result.AynvoraResult

/**
 * Identified hand for palm analysis.
 */
enum class HandType {
    LEFT,
    RIGHT,
}

/**
 * Major anatomical regions of the palm.
 */
enum class PalmRegion {
    MOUNT_OF_JUPITER,
    MOUNT_OF_SATURN,
    MOUNT_OF_SUN,
    MOUNT_OF_MERCURY,
    MOUNT_OF_VENUS,
    MOUNT_OF_MARS_LOWER,
    MOUNT_OF_MARS_UPPER,
    MOUNT_OF_MOON,
    PLAIN_OF_MARS,
}

/**
 * Canonical palm lines according to traditional Hastrekha.
 */
enum class PalmLineType {
    LIFE_LINE,       // Jeevan Rekha
    HEAD_LINE,       // Mastishk Rekha
    HEART_LINE,      // Hriday Rekha
    FATE_LINE,       // Bhagya Rekha
    SUN_LINE,        // Surya Rekha
    MERCURY_LINE,    // Budh Rekha
    MARRIAGE_LINE,   // Vivah Rekha
}

/**
 * Physical local reference to a captured hand image.
 * Hand images remain local-only by default and must never be uploaded without explicit user consent.
 */
data class HandImageReference(
    val imagePath: String,
    val captureTimestampEpochMs: Long,
    val handType: HandType,
    val widthPx: Int,
    val heightPx: Int,
    val isLocalEncrypted: Boolean = true,
)

/**
 * Structured observation from on-device hand vision.
 */
data class PalmLineFinding(
    val lineType: PalmLineType,
    val clarityScore: Float, // 0.0 to 1.0
    val curvatureScore: Float,
    val lengthCategory: String, // Short, Medium, Long
    val breaksDetected: Boolean,
)

data class PalmMountFinding(
    val region: PalmRegion,
    val prominenceScore: Float, // 0.0 to 1.0
    val markings: List<String> = emptyList(),
)

/**
 * Complete set of structured findings derived from palm capture.
 */
data class PalmFinding(
    val handType: HandType,
    val lines: List<PalmLineFinding>,
    val mounts: List<PalmMountFinding>,
    val overallClarity: Float,
)

/**
 * Result of traditional Hastrekha evaluation.
 * Note: Non-medical, non-fatalistic; provides self-reflection patterns only.
 */
data class PalmistryAnalysisResult(
    val sessionId: String,
    val handType: HandType,
    val primaryStrengths: List<String>,
    val reflectiveTendencies: List<String>,
    val traditionalCommentary: Map<PalmLineType, String>,
    val ethicalDisclaimer: String = "Hastrekha observations are for self-reflection and personal insight. They do not constitute medical, psychological, or lifespan predictions.",
)

/**
 * Domain repository contract for Palmistry sessions.
 */
interface PalmistryRepository {
    suspend fun saveSession(
        reference: HandImageReference,
        finding: PalmFinding
    ): AynvoraResult<String>

    suspend fun getAnalysisResult(sessionId: String): AynvoraResult<PalmistryAnalysisResult>
}
