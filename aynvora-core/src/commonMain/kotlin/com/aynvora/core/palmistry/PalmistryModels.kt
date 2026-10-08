package com.aynvora.core.palmistry

import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.Serializable

/**
 * Identified hand for palm analysis.
 */
@Serializable
enum class HandType {
    LEFT,
    RIGHT,
}

/**
 * Major anatomical regions of the palm.
 */
@Serializable
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
@Serializable
enum class PalmLineType {
    LIFE_LINE,       // Jeevan Rekha
    HEAD_LINE,       // Mastishk Rekha
    HEART_LINE,      // Hriday Rekha
    FATE_LINE,       // Bhagya Rekha
    SUN_LINE,        // Surya Rekha
    MERCURY_LINE,    // Budh Rekha
    MARRIAGE_LINE,   // Vivah Rekha
    INTUITION_LINE,  // Intuition Line
    HEALTH_LINE,     // Health Line
}

/**
 * Validation match result between selected hand and detected hand.
 */
@Serializable
enum class PalmHandValidationStatus {
    PASS,
    WRONG_HAND,
    RETRY,
}

/**
 * 2D normalized coordinate in [0.0..1.0] image space.
 */
@Serializable
data class Point2D(
    val x: Float,
    val y: Float,
)

/**
 * Bounding rectangle in normalized [0.0..1.0] image space.
 */
@Serializable
data class PalmRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * Anatomical 21-landmark model point matching MediaPipe Hand Landmarker standards.
 */
@Serializable
data class HandLandmark(
    val index: Int,
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float = 0.0f,
)

/**
 * Structured geometric measurement of an identified palm line.
 */
@Serializable
data class PalmLineGeometry(
    val type: PalmLineType,
    val detected: Boolean,
    val confidence: Float,
    val continuity: Float,
    val normalizedLength: Float,
    val curvature: Float,
    val originPoint: Point2D,
    val terminationPoint: Point2D,
    val geometry: List<Point2D> = emptyList(),
)

/**
 * Comprehensive, multi-metric palm image quality validation result.
 */
@Serializable
data class PalmQualityResult(
    val overallScore: Float,
    val isUsable: Boolean,
    val blurScore: Float,
    val brightnessScore: Float,
    val palmCoverageScore: Float,
    val occlusionScore: Float,
    val orientationScore: Float,
    val failures: List<String> = emptyList(),
)

/**
 * Verifiable provenance and licensing metadata for external ML models.
 */
@Serializable
data class PalmModelMetadata(
    val handDetectorModel: String = "MediaPipe Hand Landmarker (Google AI Edge)",
    val handDetectorVersion: String = "0.10.14",
    val handDetectorLicense: String = "Apache-2.0",
    val handDetectorSha256: String = "c3f8e586b971a8bc8f7c9e0a293bf6dfa996df4482b6c7a9171f654b03692bf9",
    val lineSegmentationModel: String = "AYNVORA Boundary Ridge Segmenter",
    val lineSegmentationVersion: String = "1.0.0",
    val lineSegmentationLicense: String = "Proprietary / In-House",
    val isOfflineOnDevice: Boolean = true,
    val tfliteAuditResult: String = "TensorFlow/TFLite palm-line model integration not completed because no acceptable licensed/technically compatible model was verified.",
)

@Serializable
data class ImageSpaceTransform(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val displayWidth: Float,
    val displayHeight: Float,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float = 1.0f,
    val isMirrored: Boolean = false,
) {
    /**
     * Projects a normalized point [0.0..1.0] onto the canvas coordinate frame.
     */
    fun toCanvasPoint(p: Point2D): Point2D {
        val normX = if (isMirrored) (1.0f - p.x) else p.x
        val cx = offsetX + normX * displayWidth
        val cy = offsetY + p.y * displayHeight
        return Point2D(cx, cy)
    }

    /**
     * Inverts a canvas coordinate back into normalized [0.0..1.0] image space.
     */
    fun toNormalizedPoint(canvasX: Float, canvasY: Float): Point2D {
        val relX = if (displayWidth > 0f) (canvasX - offsetX) / displayWidth else 0f
        val relY = if (displayHeight > 0f) (canvasY - offsetY) / displayHeight else 0f
        val normX = if (isMirrored) (1.0f - relX) else relX
        return Point2D(normX.coerceIn(0f, 1f), relY.coerceIn(0f, 1f))
    }

    companion object {
        /**
         * Calculates the aspect-fit transform for rendering a source image on a canvas of [canvasWidth] x [canvasHeight].
         */
        fun fit(sourceWidth: Int, sourceHeight: Int, canvasWidth: Float, canvasHeight: Float, isMirrored: Boolean = false): ImageSpaceTransform {
            val sw = if (sourceWidth > 0) sourceWidth.toFloat() else canvasWidth
            val sh = if (sourceHeight > 0) sourceHeight.toFloat() else canvasHeight
            val imageAspect = if (sh > 0f) sw / sh else 1.0f
            val canvasAspect = if (canvasHeight > 0f) canvasWidth / canvasHeight else 1.0f

            val (dispW, dispH, ox, oy) = if (imageAspect > canvasAspect) {
                // Letterbox: image is wider than canvas
                val dw = canvasWidth
                val dh = if (imageAspect > 0f) canvasWidth / imageAspect else canvasHeight
                listOf(dw, dh, 0f, (canvasHeight - dh) / 2f)
            } else {
                // Pillarbox: image is taller than canvas
                val dh = canvasHeight
                val dw = canvasHeight * imageAspect
                listOf(dw, dh, (canvasWidth - dw) / 2f, 0f)
            }

            val scale = if (sw > 0f) dispW / sw else 1.0f
            return ImageSpaceTransform(
                sourceWidth = if (sourceWidth > 0) sourceWidth else canvasWidth.toInt(),
                sourceHeight = if (sourceHeight > 0) sourceHeight else canvasHeight.toInt(),
                displayWidth = dispW,
                displayHeight = dispH,
                offsetX = ox,
                offsetY = oy,
                scale = scale,
                isMirrored = isMirrored,
            )
        }
    }
}

/**
 * Machine-readable palm evidence object capturing all verified vision telemetry.
 */
@Serializable
data class PalmEvidence(
    val imageId: String,
    val selectedHand: HandType,
    val detectedHand: HandType?,
    val handConfidence: Float,
    val palmQuality: PalmQualityResult,
    val landmarks: List<HandLandmark>,
    val palmBounds: PalmRect,
    val orientation: Float,
    val heartLine: PalmLineGeometry?,
    val headLine: PalmLineGeometry?,
    val lifeLine: PalmLineGeometry?,
    val fateLine: PalmLineGeometry?,
    val additionalDetectedLines: List<PalmLineGeometry> = emptyList(),
    val modelMetadata: PalmModelMetadata = PalmModelMetadata(),
    val annotationVersion: String = "1.0.0",
    val validationStatus: PalmHandValidationStatus = when {
        detectedHand == null || handConfidence < 0.6f -> PalmHandValidationStatus.RETRY
        detectedHand == selectedHand -> PalmHandValidationStatus.PASS
        else -> PalmHandValidationStatus.WRONG_HAND
    },
    val userContext: PalmUserContext? = null,
    val rawHandLabel: String = detectedHand?.name ?: "UNKNOWN",
    val rawHandScore: Float = handConfidence,
    val decisionReason: String = "",
)

/**
 * User context associated with palm capture.
 * Stored separately so AI reasoning does NOT rely on OCR.
 */
@Serializable
data class PalmUserContext(
    val displayName: String,
    val selectedHand: HandType,
    val detectedHand: HandType?,
    val handConfidence: Float,
    val captureSource: PalmImageSourceType,
    val captureTimestamp: Long,
    val qualityScore: Float,
)

/**
 * Triple image bundle preserving non-destructive original image.
 */
data class PalmImageBundle(
    val originalImage: ByteArray,
    val normalizedImage: ByteArray,
    val annotatedImage: ByteArray,
    val evidence: PalmEvidence,
    val userContext: PalmUserContext,
    val watermarkText: String? = null,
)

/**
 * Evaluated geometric/anatomical shape of the palm.
 */
@Serializable
enum class PalmShape {
    SQUARE,
    RECTANGULAR,
    LONG,
    WIDE,
    UNKNOWN,
}

/**
 * Physical local reference to a captured hand image.
 * Hand images remain local-only by default and must never be uploaded without explicit user consent.
 */
@Serializable
data class HandImageReference(
    val imagePath: String,
    val captureTimestampEpochMs: Long,
    val handType: HandType,
    val widthPx: Int,
    val heightPx: Int,
    val isLocalEncrypted: Boolean = true,
    val rawBytes: ByteArray? = null,
)

/**
 * Origin type for palm image input.
 */
@Serializable
enum class PalmImageSourceType {
    CAMERA,
    GALLERY,
    SAMPLE,
}

/**
 * Abstract palm image source decoupling camera/platform APIs from domain logic.
 */
data class PalmImageSource(
    val data: ByteArray? = null,
    val filePath: String? = null,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val sourceType: PalmImageSourceType = PalmImageSourceType.CAMERA,
    val capturedAtEpochMs: Long = 0L,
    val inferredHand: HandType? = null,
)

/**
 * Validation state for input palm image quality.
 */
@Serializable
enum class ImageQualityState {
    GOOD,
    LOW_RESOLUTION,
    BLURRY,
    TOO_DARK,
    TOO_BRIGHT,
    HAND_NOT_DETECTED,
    PALM_NOT_VISIBLE,
    OBSTRUCTED,
    WRONG_ORIENTATION,
    UNSUPPORTED,
}

/**
 * Result of image quality pre-validation.
 */
@Serializable
data class ImageQualityAssessment(
    val state: ImageQualityState,
    val score: Float, // 0.0 to 1.0
    val issues: List<String> = emptyList(),
    val guidanceKey: String = "palmistry.quality.guidance.good",
    val isAcceptable: Boolean = (state == ImageQualityState.GOOD),
)

/**
 * Explicit registry of supported vs unsupported Palmistry analysis capabilities.
 */
object PalmistryAnalysisCapabilities {
    const val ANALYSIS_VERSION = "1.0.0"

    val isHandDetectionSupported: Boolean = true
    val isPalmSegmentationSupported: Boolean = true
    val isPalmShapeSupported: Boolean = true

    // Major Lines
    val isLifeLineSupported: Boolean = true
    val isHeadLineSupported: Boolean = true
    val isHeartLineSupported: Boolean = true
    val isFateLineSupported: Boolean = true

    // Minor Lines (Architecture supports future expansion)
    val isSunLineSupported: Boolean = false
    val isMercuryLineSupported: Boolean = false
    val isMarriageLineSupported: Boolean = false
    val isIntuitionLineSupported: Boolean = false
    val isHealthLineSupported: Boolean = false

    // Mounts
    val isMountAnalysisSupported: Boolean = true
}

/**
 * Structured observation from on-device hand vision.
 */
@Serializable
data class PalmLineFinding(
    val lineType: PalmLineType,
    val clarityScore: Float, // 0.0 to 1.0
    val curvatureScore: Float,
    val lengthCategory: String, // Short, Medium, Long
    val breaksDetected: Boolean,
    val detected: Boolean = true,
    val continuity: Float = 0.8f,
    val strength: String = "MODERATE", // STRONG, MODERATE, FAINT, NOT_DETECTED
    val relativeStartRatio: Float = 0.2f,
    val relativeEndRatio: Float = 0.8f,
)

@Serializable
data class PalmMountFinding(
    val region: PalmRegion,
    val prominenceScore: Float, // 0.0 to 1.0
    val markings: List<String> = emptyList(),
    val detected: Boolean = true,
    val developmentLevel: String = "BALANCED", // UNDERDEVELOPED, BALANCED, PROMINENT, NOT_DETECTED
)

/**
 * Complete set of structured findings derived from palm capture.
 */
@Serializable
data class PalmFinding(
    val handType: HandType,
    val lines: List<PalmLineFinding>,
    val mounts: List<PalmMountFinding> = emptyList(),
    val overallClarity: Float = 0.8f,
    val shape: PalmShape = PalmShape.RECTANGULAR,
    val analysisVersion: String = PalmistryAnalysisCapabilities.ANALYSIS_VERSION,
    val evidence: PalmEvidence? = null,
)

/**
 * Structured source-controlled traditional Hastrekha interpretation.
 */
@Serializable
data class PalmistryMeaning(
    val featureType: String,
    val condition: String,
    val title: String = "",
    val description: String = "",
    val traditionalInterpretation: String = "",
    val caution: String = "",
    val sourceReference: String = "Samudrika Shastra (Classical Hastrekha Tradition)",
    val contentVersion: String = "1.0.0",
    val language: String = "en",
    val titleKey: String = "",
    val descriptionKey: String = "",
    val interpretationKey: String = "",
    val cautionKey: String = "",
)


/**
 * Structured atomic evidence item grounded in image findings.
 */
@Serializable
data class PalmistryEvidence(
    val evidenceId: String,
    val readingId: String,
    val hand: HandType,
    val featureType: String,
    val observation: String,
    val confidence: Float, // Image/feature measurement confidence (0.0 to 1.0), NOT future probability
    val analysisVersion: String = PalmistryAnalysisCapabilities.ANALYSIS_VERSION,
    val source: String = "AYNVORA Palm Vision Engine",
    val contentVersion: String = "1.0.0",
)

/**
 * Result of traditional Hastrekha evaluation.
 * Note: Non-medical, non-fatalistic; provides self-reflection patterns only.
 */
@Serializable
data class PalmistryAnalysisResult(
    val sessionId: String,
    val handType: HandType,
    val primaryStrengths: List<String>,
    val reflectiveTendencies: List<String>,
    val traditionalCommentary: Map<PalmLineType, String>,
    val ethicalDisclaimer: String = "Hastrekha observations are for self-reflection and personal insight. They do not constitute medical, psychological, or lifespan predictions.",
    val analysisVersion: String = PalmistryAnalysisCapabilities.ANALYSIS_VERSION,
)

/**
 * Chronological event types in a palm reading timeline.
 */
@Serializable
enum class PalmTimelineEventType {
    READING_STARTED,
    HAND_SELECTED,
    IMAGE_CAPTURED,
    QUALITY_VALIDATED,
    ANALYSIS_COMPLETED,
    FEATURES_DETECTED,
    QUESTION_ASKED,
    AI_ANSWER_GENERATED,
    AI_ANSWER_FALLBACK,
    FEEDBACK_SUBMITTED,
    READING_SATISFIED,
    READING_COMPLETED,
}

/**
 * Atomic chronological timeline event for Palmistry.
 */
@Serializable
data class PalmTimelineEvent(
    val eventId: String,
    val readingId: String,
    val timestampEpochMs: Long,
    val eventType: PalmTimelineEventType,
    val summary: String,
    val language: String = "en",
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Lifecycle status of a Palm reading session.
 */
@Serializable
enum class PalmReadingStatus {
    ACTIVE,
    SATISFIED,
    COMPLETED,
}

/**
 * Status of question answering.
 */
@Serializable
enum class PalmAnswerStatus {
    GENERATING,
    COMPLETED,
    INSUFFICIENT_EVIDENCE,
    FALLBACK,
    FAILED,
}

/**
 * Conversational follow-up question within an active Palm reading.
 */
@Serializable
data class PalmQuestion(
    val questionId: String,
    val readingId: String,
    val questionText: String,
    val language: String,
    val timestampEpochMs: Long,
    val status: PalmAnswerStatus = PalmAnswerStatus.GENERATING,
    val answerSummary: String? = null,
    val answerInterpretation: String? = null,
    val keyThemes: List<String> = emptyList(),
    val supportingEvidenceIds: List<String> = emptyList(),
    val fallbackUsed: Boolean = false,
    val modelMetadata: String? = null,
)

/**
 * User feedback on the entire reading session.
 */
@Serializable
data class PalmFeedback(
    val readingId: String,
    val ratingStars: Int, // 1 to 5
    val improvementComment: String? = null,
    val timestampEpochMs: Long,
)

/**
 * Category feedback on individual palm feature cards.
 */
@Serializable
enum class PalmFeatureFeedbackCategory {
    CLEAR,
    CONFUSING,
    NEED_MORE_CONTEXT,
}

@Serializable
data class PalmFeatureFeedback(
    val readingId: String,
    val featureType: String,
    val category: PalmFeatureFeedbackCategory,
    val timestampEpochMs: Long,
)

@Serializable
data class PalmAnswerFeedback(
    val readingId: String,
    val questionId: String,
    val isHelpful: Boolean,
    val timestampEpochMs: Long,
)

/**
 * Complete Palm reading session.
 */
@Serializable
data class PalmReadingSession(
    val id: String,
    val startedAtEpochMs: Long,
    val handType: HandType,
    val imageReference: HandImageReference? = null,
    val qualityAssessment: ImageQualityAssessment? = null,
    val finding: PalmFinding? = null,
    val meanings: List<PalmistryMeaning> = emptyList(),
    val analysisResult: PalmistryAnalysisResult? = null,
    val questions: List<PalmQuestion> = emptyList(),
    val timeline: List<PalmTimelineEvent> = emptyList(),
    val feedback: PalmFeedback? = null,
    val status: PalmReadingStatus = PalmReadingStatus.ACTIVE,
    val language: String = "en",
    val evidence: PalmEvidence? = null,
    val userContext: PalmUserContext? = null,
)

/**
 * Domain repository contract for Palmistry sessions.
 */
interface PalmistryRepository {
    suspend fun saveSession(
        reference: HandImageReference,
        finding: PalmFinding,
    ): AynvoraResult<String>

    suspend fun getAnalysisResult(sessionId: String): AynvoraResult<PalmistryAnalysisResult>
}

/**
 * Extended repository contract for complete Palmistry sessions (Phase 8.10).
 */
interface PalmSessionRepository {
    suspend fun saveSession(session: PalmReadingSession): AynvoraResult<Unit>
    suspend fun getSession(sessionId: String): AynvoraResult<PalmReadingSession?>
    suspend fun getLatestSession(): AynvoraResult<PalmReadingSession?>
    suspend fun getAllSessions(): AynvoraResult<List<PalmReadingSession>>
    suspend fun deleteSession(sessionId: String): AynvoraResult<Unit>
    suspend fun recordFeatureFeedback(feedback: PalmFeatureFeedback): AynvoraResult<Unit>
    suspend fun recordAnswerFeedback(feedback: PalmAnswerFeedback): AynvoraResult<Unit>
}
