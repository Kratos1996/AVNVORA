package com.aynvora.palmistry

import kotlinx.serialization.Serializable

/**
 * Structured error codes for palm image acquisition and hardware camera operations.
 */
@Serializable
enum class PalmCaptureErrorCode {
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    CAPTURE_FAILED,
    INVALID_IMAGE,
    POOR_QUALITY,
    CANCELLED,
    ANALYSIS_UNAVAILABLE,
    TIMEOUT,
    UNKNOWN,
}

/**
 * Domain-level structured error representing a failure during camera or gallery acquisition.
 * Decouples platform camera/activity result exceptions from the UI.
 */
@Serializable
data class PalmCaptureError(
    val code: PalmCaptureErrorCode,
    val message: String,
    val details: String? = null,
) {
    companion object {
        fun permissionDenied(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.PERMISSION_DENIED,
            message = "Camera permission was denied. Please grant camera access in system settings.",
            details = details,
        )

        fun cameraUnavailable(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.CAMERA_UNAVAILABLE,
            message = "Camera hardware is not available on this device.",
            details = details,
        )

        fun captureFailed(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.CAPTURE_FAILED,
            message = "Failed to capture image from camera.",
            details = details,
        )

        fun invalidImage(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.INVALID_IMAGE,
            message = "The acquired image data was empty or unreadable.",
            details = details,
        )

        fun cancelled(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.CANCELLED,
            message = "Image capture was cancelled.",
            details = details,
        )

        fun poorQuality(reason: String) = PalmCaptureError(
            code = PalmCaptureErrorCode.POOR_QUALITY,
            message = "Image quality is insufficient: $reason",
            details = reason,
        )

        fun analysisUnavailable(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.ANALYSIS_UNAVAILABLE,
            message = "Palm analysis engine is currently unavailable.",
            details = details,
        )

        fun timeout(details: String? = null) = PalmCaptureError(
            code = PalmCaptureErrorCode.TIMEOUT,
            message = "Image acquisition operation timed out.",
            details = details,
        )
    }
}
