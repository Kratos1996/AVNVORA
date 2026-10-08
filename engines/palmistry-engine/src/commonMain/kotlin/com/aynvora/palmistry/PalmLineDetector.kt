package com.aynvora.palmistry

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Common abstraction for on-device palm line detection and geometric segmentation.
 *
 * Exposes a uniform contract allowing interchangeable backends (e.g. deterministic
 * image-driven crease valley ridge segmentation algorithm vs verified ML models)
 * while preserving consistent PalmEvidence structures and truthful coordinates.
 */
interface PalmLineDetector {
    /**
     * Identifies major and minor palm lines from the normalized hand source and landmark orientation.
     */
    fun detectPalmLines(
        source: PalmImageSource,
        detection: HandDetectionResult,
    ): PalmEvidence
}

/**
 * Pixel-driven curvilinear boundary ridge and crease valley detector.
 *
 * Derives line geometry directly from image pixel luminance valleys anchored to anatomical regions.
 * Strictly enforces:
 * 1. Zero hardcoded/static Point2D line coordinates.
 * 2. Crease geometry depends directly on actual pixel contrast valleys.
 * 3. If a crease cannot be reliably traced from pixel contrast, marks detected = false with LOW_CONFIDENCE.
 */
class DeterministicPalmRidgeDetector : PalmLineDetector {

    override fun detectPalmLines(
        source: PalmImageSource,
        detection: HandDetectionResult,
    ): PalmEvidence {
        val bytes = source.data ?: ByteArray(0)
        val hand = detection.detectedHand ?: detection.selectedHand
        val bounds = detection.palmBounds

        // Decode image bytes to normalized 2D luminance grid (Rec. 601 grayscale [0.0..1.0])
        val grid = decodeImageToLuminanceGrid(bytes) ?: createFallbackLuminanceGrid(bytes, source.widthPx, source.heightPx)

        val heartLine = traceHeartLine(grid, hand, bounds, detection.landmarks)
        val headLine = traceHeadLine(grid, hand, bounds, detection.landmarks)
        val lifeLine = traceLifeLine(grid, hand, bounds, detection.landmarks)
        val fateLine = traceFateLine(grid, hand, bounds, detection.landmarks)

        val captureId = "palm_${System.currentTimeMillis()}_${bytes.size}"

        return PalmEvidence(
            imageId = captureId,
            selectedHand = detection.selectedHand,
            detectedHand = detection.detectedHand,
            handConfidence = detection.handConfidence,
            palmQuality = detection.quality,
            landmarks = detection.landmarks,
            palmBounds = bounds,
            orientation = detection.orientationDegrees,
            heartLine = heartLine,
            headLine = headLine,
            lifeLine = lifeLine,
            fateLine = fateLine,
            additionalDetectedLines = emptyList(),
            modelMetadata = PalmModelMetadata(),
            annotationVersion = "1.0.0",
            rawHandLabel = detection.rawHandLabel ?: (detection.detectedHand?.name ?: "UNKNOWN"),
            rawHandScore = detection.rawHandScore ?: detection.handConfidence,
            decisionReason = detection.decisionReason ?: "",
        )
    }

    /**
     * Traces the Life Line (Jeevan Rekha) curving around the thenar eminence from between
     * the thumb and index base down towards the wrist.
     */
    private fun traceLifeLine(
        grid: LuminanceGrid,
        hand: HandType,
        bounds: PalmRect,
        landmarks: List<HandLandmark>,
    ): PalmLineGeometry {
        val isRight = (hand == HandType.RIGHT)
        val bLeft = if (bounds.left > 0f && bounds.left < bounds.right) bounds.left else 0.15f
        val bRight = if (bounds.right > bounds.left && bounds.right <= 1f) bounds.right else 0.85f
        val bTop = if (bounds.top > 0f && bounds.top < bounds.bottom) bounds.top else 0.15f
        val bBottom = if (bounds.bottom > bounds.top && bounds.bottom <= 1f) bounds.bottom else 0.85f
        val palmW = bRight - bLeft
        val palmH = bBottom - bTop

        // Landmark or bound-anchored start & center of thenar arc
        val thumbMcp = landmarks.firstOrNull { it.name == "THUMB_MCP" }
        val indexMcp = landmarks.firstOrNull { it.name == "INDEX_FINGER_MCP" }
        val wrist = landmarks.firstOrNull { it.name == "WRIST" }

        val startX = when {
            indexMcp != null && thumbMcp != null -> (indexMcp.x + thumbMcp.x) / 2f
            isRight -> bLeft + palmW * 0.22f
            else -> bRight - palmW * 0.22f
        }
        val startY = when {
            indexMcp != null -> indexMcp.y + palmH * 0.12f
            else -> bTop + palmH * 0.38f
        }

        val arcCenterX = if (isRight) bLeft + palmW * 0.18f else bRight - palmW * 0.18f
        val arcCenterY = bTop + palmH * 0.58f
        val baseRadiusX = palmW * 0.26f
        val baseRadiusY = palmH * 0.32f

        val numSteps = 10
        val detectedPoints = mutableListOf<Point2D>()
        var totalContrast = 0.0f

        for (step in 0 until numSteps) {
            val t = step.toFloat() / (numSteps - 1).toFloat()
            // Angle sweeps from upper thenar around thumb to lower palm
            val angle = if (isRight) {
                // Sweeps from ~-0.6 rad to ~1.7 rad
                -0.6f + t * 2.3f
            } else {
                Math.PI.toFloat() + 0.6f - t * 2.3f
            }

            val expectedX = arcCenterX + cos(angle.toDouble()).toFloat() * baseRadiusX
            val expectedY = arcCenterY + sin(angle.toDouble()).toFloat() * baseRadiusY

            // Search radially perpendicular to the arc for the darkest pixel valley (crease)
            val valley = findLocalCreaseValley(
                grid = grid,
                centerX = expectedX.coerceIn(0.05f, 0.95f),
                centerY = expectedY.coerceIn(0.05f, 0.95f),
                searchNormalAngle = angle,
                searchRadiusNorm = 0.045f,
            )

            if (valley != null && valley.contrast > 0.015f) {
                detectedPoints.add(valley.point)
                totalContrast += valley.contrast
            }
        }

        return buildLineGeometry(
            type = PalmLineType.LIFE_LINE,
            points = detectedPoints,
            minPoints = 4,
            avgContrast = if (detectedPoints.isNotEmpty()) totalContrast / detectedPoints.size else 0f,
            expectedDefault = null,
        )
    }

    /**
     * Traces the Head Line (Mastishk Rekha) traversing across the mid-palm from the radial side.
     */
    private fun traceHeadLine(
        grid: LuminanceGrid,
        hand: HandType,
        bounds: PalmRect,
        landmarks: List<HandLandmark>,
    ): PalmLineGeometry {
        val isRight = (hand == HandType.RIGHT)
        val bLeft = if (bounds.left > 0f && bounds.left < bounds.right) bounds.left else 0.15f
        val bRight = if (bounds.right > bounds.left && bounds.right <= 1f) bounds.right else 0.85f
        val bTop = if (bounds.top > 0f && bounds.top < bounds.bottom) bounds.top else 0.15f
        val bBottom = if (bounds.bottom > bounds.top && bounds.bottom <= 1f) bounds.bottom else 0.85f
        val palmW = bRight - bLeft
        val palmH = bBottom - bTop

        val numSteps = 9
        val detectedPoints = mutableListOf<Point2D>()
        var totalContrast = 0.0f

        val xStart = if (isRight) bLeft + palmW * 0.20f else bRight - palmW * 0.20f
        val xEnd = if (isRight) bLeft + palmW * 0.82f else bRight - palmW * 0.82f
        val yBase = bTop + palmH * 0.46f

        for (step in 0 until numSteps) {
            val t = step.toFloat() / (numSteps - 1).toFloat()
            val expectedX = xStart + (xEnd - xStart) * t
            // Head line typically slopes downward slightly across the palm
            val expectedY = yBase + (t * 0.12f * palmH)

            // Search vertically for local crease valley
            val valley = findLocalCreaseValley(
                grid = grid,
                centerX = expectedX.coerceIn(0.05f, 0.95f),
                centerY = expectedY.coerceIn(0.05f, 0.95f),
                searchNormalAngle = Math.PI.toFloat() / 2f,
                searchRadiusNorm = 0.055f,
            )

            if (valley != null && valley.contrast > 0.015f) {
                detectedPoints.add(valley.point)
                totalContrast += valley.contrast
            }
        }

        return buildLineGeometry(
            type = PalmLineType.HEAD_LINE,
            points = detectedPoints,
            minPoints = 4,
            avgContrast = if (detectedPoints.isNotEmpty()) totalContrast / detectedPoints.size else 0f,
            expectedDefault = null,
        )
    }

    /**
     * Traces the Heart Line (Hriday Rekha) starting under the pinky/mercury mount
     * and sweeping across upper palm toward the index/jupiter mount.
     */
    private fun traceHeartLine(
        grid: LuminanceGrid,
        hand: HandType,
        bounds: PalmRect,
        landmarks: List<HandLandmark>,
    ): PalmLineGeometry {
        val isRight = (hand == HandType.RIGHT)
        val bLeft = if (bounds.left > 0f && bounds.left < bounds.right) bounds.left else 0.15f
        val bRight = if (bounds.right > bounds.left && bounds.right <= 1f) bounds.right else 0.85f
        val bTop = if (bounds.top > 0f && bounds.top < bounds.bottom) bounds.top else 0.15f
        val bBottom = if (bounds.bottom > bounds.top && bounds.bottom <= 1f) bounds.bottom else 0.85f
        val palmW = bRight - bLeft
        val palmH = bBottom - bTop

        val numSteps = 9
        val detectedPoints = mutableListOf<Point2D>()
        var totalContrast = 0.0f

        // Starts on ulnar side (under pinky) and goes towards radial side (under index)
        val xStart = if (isRight) bRight - palmW * 0.15f else bLeft + palmW * 0.15f
        val xEnd = if (isRight) bLeft + palmW * 0.28f else bRight - palmW * 0.28f
        val yStart = bTop + palmH * 0.38f
        val yEnd = bTop + palmH * 0.34f

        for (step in 0 until numSteps) {
            val t = step.toFloat() / (numSteps - 1).toFloat()
            val expectedX = xStart + (xEnd - xStart) * t
            val expectedY = yStart + (yEnd - yStart) * t

            val valley = findLocalCreaseValley(
                grid = grid,
                centerX = expectedX.coerceIn(0.05f, 0.95f),
                centerY = expectedY.coerceIn(0.05f, 0.95f),
                searchNormalAngle = Math.PI.toFloat() / 2f,
                searchRadiusNorm = 0.05f,
            )

            if (valley != null && valley.contrast > 0.015f) {
                detectedPoints.add(valley.point)
                totalContrast += valley.contrast
            }
        }

        // Heart line points run from origin (pinky side) to termination (index side)
        return buildLineGeometry(
            type = PalmLineType.HEART_LINE,
            points = detectedPoints,
            minPoints = 4,
            avgContrast = if (detectedPoints.isNotEmpty()) totalContrast / detectedPoints.size else 0f,
            expectedDefault = null,
        )
    }

    /**
     * Traces the Fate Line (Bhagya Rekha) rising vertically along the median line toward Saturn mount.
     */
    private fun traceFateLine(
        grid: LuminanceGrid,
        hand: HandType,
        bounds: PalmRect,
        landmarks: List<HandLandmark>,
    ): PalmLineGeometry {
        val bLeft = if (bounds.left > 0f && bounds.left < bounds.right) bounds.left else 0.15f
        val bRight = if (bounds.right > bounds.left && bounds.right <= 1f) bounds.right else 0.85f
        val bTop = if (bounds.top > 0f && bounds.top < bounds.bottom) bounds.top else 0.15f
        val bBottom = if (bounds.bottom > bounds.top && bounds.bottom <= 1f) bounds.bottom else 0.85f
        val palmW = bRight - bLeft
        val palmH = bBottom - bTop

        val midX = bLeft + palmW * 0.49f
        val numSteps = 7
        val detectedPoints = mutableListOf<Point2D>()
        var totalContrast = 0.0f

        val yStart = bBottom - palmH * 0.16f
        val yEnd = bTop + palmH * 0.38f

        for (step in 0 until numSteps) {
            val t = step.toFloat() / (numSteps - 1).toFloat()
            val expectedY = yStart + (yEnd - yStart) * t

            // Search horizontally along the central corridor
            val valley = findLocalCreaseValley(
                grid = grid,
                centerX = midX.coerceIn(0.05f, 0.95f),
                centerY = expectedY.coerceIn(0.05f, 0.95f),
                searchNormalAngle = 0.0f,
                searchRadiusNorm = 0.045f,
            )

            if (valley != null && valley.contrast > 0.025f) {
                detectedPoints.add(valley.point)
                totalContrast += valley.contrast
            }
        }

        return buildLineGeometry(
            type = PalmLineType.FATE_LINE,
            points = detectedPoints,
            minPoints = 3,
            avgContrast = if (detectedPoints.isNotEmpty()) totalContrast / detectedPoints.size else 0f,
            expectedDefault = null,
        )
    }

    // ── Valley and Ridge Search Utilities ──────────────────────────────────────

    private data class CreaseValley(
        val point: Point2D,
        val contrast: Float,
    )

    private fun findLocalCreaseValley(
        grid: LuminanceGrid,
        centerX: Float,
        centerY: Float,
        searchNormalAngle: Float,
        searchRadiusNorm: Float,
    ): CreaseValley? {
        val samples = 9
        var minLuma = 1.0f
        var bestX = centerX
        var bestY = centerY
        var sumLuma = 0.0f

        val cosA = cos(searchNormalAngle.toDouble()).toFloat()
        val sinA = sin(searchNormalAngle.toDouble()).toFloat()

        for (i in -samples / 2..samples / 2) {
            val offsetFrac = (i.toFloat() / (samples / 2).toFloat()) * searchRadiusNorm
            val sampleX = (centerX + offsetFrac * cosA).coerceIn(0.02f, 0.98f)
            val sampleY = (centerY + offsetFrac * sinA).coerceIn(0.02f, 0.98f)

            val luma = grid.getNormalized(sampleX, sampleY)
            sumLuma += luma
            if (luma < minLuma) {
                minLuma = luma
                bestX = sampleX
                bestY = sampleY
            }
        }

        val avgLuma = sumLuma / samples.toFloat()
        val contrast = max(0.0f, avgLuma - minLuma)

        return CreaseValley(Point2D(bestX, bestY), contrast)
    }

    private fun buildLineGeometry(
        type: PalmLineType,
        points: List<Point2D>,
        minPoints: Int,
        avgContrast: Float,
        expectedDefault: List<Point2D>?,
    ): PalmLineGeometry {
        val detected = points.size >= minPoints && avgContrast >= 0.012f
        if (!detected) {
            val emptyPoint = Point2D(0.5f, 0.5f)
            return PalmLineGeometry(
                type = type,
                detected = false,
                confidence = (avgContrast * 10f).coerceIn(0.10f, 0.45f),
                continuity = 0.0f,
                normalizedLength = 0.0f,
                curvature = 0.0f,
                originPoint = emptyPoint,
                terminationPoint = emptyPoint,
                geometry = emptyList(),
            )
        }

        val continuity = calculateContinuity(points)
        val length = calculatePathLength(points)
        val curvature = calculateCurvature(points)
        val confidence = (0.50f + avgContrast * 8f + continuity * 0.25f).coerceIn(0.52f, 0.94f)

        return PalmLineGeometry(
            type = type,
            detected = true,
            confidence = confidence,
            continuity = continuity,
            normalizedLength = length,
            curvature = curvature,
            originPoint = points.first(),
            terminationPoint = points.last(),
            geometry = points,
        )
    }

    private fun calculatePathLength(points: List<Point2D>): Float {
        if (points.size < 2) return 0.0f
        var length = 0.0f
        for (i in 0 until points.size - 1) {
            val dx = points[i + 1].x - points[i].x
            val dy = points[i + 1].y - points[i].y
            length += sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return length
    }

    private fun calculateContinuity(points: List<Point2D>): Float {
        if (points.size < 3) return 1.0f
        var maxGap = 0.0f
        var avgGap = 0.0f
        for (i in 0 until points.size - 1) {
            val dx = points[i + 1].x - points[i].x
            val dy = points[i + 1].y - points[i].y
            val d = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (d > maxGap) maxGap = d
            avgGap += d
        }
        avgGap /= (points.size - 1).toFloat()
        val regularity = if (maxGap > 0f) (avgGap / maxGap).coerceIn(0.3f, 1.0f) else 1.0f
        return regularity
    }

    private fun calculateCurvature(points: List<Point2D>): Float {
        if (points.size < 3) return 0.0f
        var totalAngleChange = 0.0
        for (i in 1 until points.size - 1) {
            val dx1 = (points[i].x - points[i - 1].x).toDouble()
            val dy1 = (points[i].y - points[i - 1].y).toDouble()
            val dx2 = (points[i + 1].x - points[i].x).toDouble()
            val dy2 = (points[i + 1].y - points[i].y).toDouble()
            val a1 = atan2(dy1, dx1)
            val a2 = atan2(dy2, dx2)
            var diff = abs(a2 - a1)
            if (diff > Math.PI) diff = 2 * Math.PI - diff
            totalAngleChange += diff
        }
        return (totalAngleChange / (points.size - 2).toDouble()).toFloat().coerceIn(0.0f, 1.0f)
    }
}
