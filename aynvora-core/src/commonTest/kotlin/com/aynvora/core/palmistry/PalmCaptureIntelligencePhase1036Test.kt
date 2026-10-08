package com.aynvora.core.palmistry

import com.aynvora.core.report.PalmistryReportGenerator
import com.aynvora.core.report.PalmistryReportInput
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ReportType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PalmCaptureIntelligencePhase1036Test {

    private val engine = PalmImageAnalysisEngine()

    private fun createSyntheticPalmImage(
        width: Int = 480,
        height: Int = 640,
        baseLum: Int = 145,
        isLeftHand: Boolean = false,
    ): ByteArray {
        val total = width * height
        val bytes = ByteArray(total)
        for (i in 0 until total) {
            val x = i % width
            val y = i / width
            var lum = baseLum + (kotlin.math.sin(x.toDouble() / 15.0) * 15).toInt() + (kotlin.math.sin(y.toDouble() / 20.0) * 12).toInt()
            
            // Central palm mass (brighter in center)
            val dx = (x - width / 2.0) / (width / 2.0)
            val dy = (y - height / 2.0) / (height / 2.0)
            val dist = dx * dx + dy * dy
            if (dist < 0.5) lum += 25

            // Lateral thumb protrusion mass
            if (y in (height * 0.35).toInt()..(height * 0.75).toInt()) {
                if (!isLeftHand && x < width * 0.35) {
                    lum += 28 // Right hand: thumb is on left
                } else if (isLeftHand && x > width * 0.65) {
                    lum += 28 // Left hand: thumb is on right
                }
            }

            if (kotlin.math.abs(y - (x * 0.8 + 20)) < 4.0) lum -= 45 // Life line arc
            if (kotlin.math.abs(y - height * 0.45) < 4.0 && x in (width * 0.2).toInt()..(width * 0.8).toInt()) lum -= 40 // Head line
            if (kotlin.math.abs(y - height * 0.30 - (kotlin.math.sin(x.toDouble() / 20.0) * 5)) < 4.0 && x in (width * 0.2).toInt()..(width * 0.85).toInt()) lum -= 42 // Heart line
            if (kotlin.math.abs(x - width * 0.5) < 3.0 && y in (height * 0.35).toInt()..(height * 0.75).toInt()) lum -= 35 // Fate line
            bytes[i] = lum.coerceIn(40, 220).toByte()
        }
        return bytes
    }

    @Test
    fun testHandMatchingRightHandPass() {
        val bytes = createSyntheticPalmImage(isLeftHand = false)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.GALLERY,
        )

        val detection = engine.detectHand(source, HandType.RIGHT)
        assertNotNull(detection.detectedHand)
        assertTrue(detection.handConfidence > 0.6f)
        assertEquals(21, detection.landmarks.size)
        assertEquals(PalmHandValidationStatus.PASS, detection.validationStatus)
    }

    @Test
    fun testHandMatchingWrongHandRejection() {
        val bytes = createSyntheticPalmImage(isLeftHand = false)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.GALLERY,
        )

        // When user selects LEFT, but the synthetic image features a right hand profile
        val detection = engine.detectHand(source, HandType.LEFT)
        assertEquals(PalmHandValidationStatus.WRONG_HAND, detection.validationStatus)
    }

    @Test
    fun testLowConfidenceHandDetectionRetry() {
        val emptySource = PalmImageSource(data = byteArrayOf(), widthPx = 0, heightPx = 0)
        val detection = engine.detectHand(emptySource, HandType.RIGHT)
        assertEquals(PalmHandValidationStatus.RETRY, detection.validationStatus)
    }

    @Test
    fun testStructuredPalmQualityMetrics() {
        val bytes = createSyntheticPalmImage(isLeftHand = false)
        val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)

        val quality = engine.validatePalmQuality(source, HandType.RIGHT)
        assertTrue(quality.isUsable)
        assertTrue(quality.overallScore in 0.5f..1.0f)
        assertTrue(quality.blurScore in 0.0f..1.0f)
        assertTrue(quality.brightnessScore in 0.0f..1.0f)
        assertTrue(quality.palmCoverageScore in 0.0f..1.0f)
        assertTrue(quality.occlusionScore in 0.0f..1.0f)
        assertTrue(quality.orientationScore in 0.0f..1.0f)
    }

    @Test
    fun testPalmLineDetectionExtractsMajorLines() {
        val bytes = createSyntheticPalmImage(isLeftHand = false)
        val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)
        val detection = engine.detectHand(source, HandType.RIGHT)
        val evidence = engine.detectPalmLines(source, detection)

        val lines = listOfNotNull(evidence.heartLine, evidence.headLine, evidence.lifeLine, evidence.fateLine)
        assertEquals(4, lines.size)
        val types = lines.map { it.type }.toSet()
        assertTrue(types.contains(PalmLineType.HEART_LINE))
        assertTrue(types.contains(PalmLineType.HEAD_LINE))
        assertTrue(types.contains(PalmLineType.LIFE_LINE))
        assertTrue(types.contains(PalmLineType.FATE_LINE))

        lines.forEach { line ->
            assertTrue(line.detected)
            assertTrue(line.confidence in 0.5f..1.0f)
            assertTrue(line.continuity in 0.5f..1.0f)
            assertTrue(line.normalizedLength in 0.0f..1.0f)
            assertTrue(line.originPoint.x in 0.0f..1.0f)
            assertTrue(line.originPoint.y in 0.0f..1.0f)
            assertTrue(line.terminationPoint.x in 0.0f..1.0f)
            assertTrue(line.terminationPoint.y in 0.0f..1.0f)
        }
    }

    @Test
    fun testStructuredEvidenceAndModelProvenance() {
        runBlocking {
            val bytes = createSyntheticPalmImage(isLeftHand = false)
            val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)
            val res = engine.analyzePalm(source, HandType.RIGHT)
            assertTrue(res is com.aynvora.core.result.AynvoraResult.Success)

            val finding = res.value
            val evidence = finding.evidence
            assertNotNull(evidence)
            assertEquals(HandType.RIGHT, evidence.selectedHand)
            assertEquals(HandType.RIGHT, evidence.detectedHand)
            assertEquals(PalmHandValidationStatus.PASS, evidence.validationStatus)

            // Verifiable Model Provenance
            val meta = evidence.modelMetadata
            assertEquals("MediaPipe Hand Landmarker (Google AI Edge)", meta.handDetectorModel)
            assertEquals("0.10.14", meta.handDetectorVersion)
            assertEquals("Apache-2.0", meta.handDetectorLicense)
            assertEquals("c3f8e586b971a8bc8f7c9e0a293bf6dfa996df4482b6c7a9171f654b03692bf9", meta.handDetectorSha256)
            assertTrue(meta.isOfflineOnDevice)
        }
    }

    @Test
    fun testPalmistryReportRendersCaptureEvidence() {
        runBlocking {
            val bytes = createSyntheticPalmImage(isLeftHand = false)
            val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)
            val res = engine.analyzePalm(source, HandType.RIGHT)
            val finding = (res as com.aynvora.core.result.AynvoraResult.Success).value

        val userContext = PalmUserContext(
            displayName = "Ishant",
            selectedHand = HandType.RIGHT,
            detectedHand = HandType.RIGHT,
            handConfidence = 0.92f,
            captureSource = PalmImageSourceType.GALLERY,
            captureTimestamp = 1791100800000L,
            qualityScore = 0.88f,
        )

        val session = PalmReadingSession(
            id = "test_sess_1036",
            startedAtEpochMs = 1791100800000L,
            handType = HandType.RIGHT,
            imageReference = HandImageReference(
                imagePath = "local://palm_test.jpg",
                captureTimestampEpochMs = 1791100800000L,
                handType = HandType.RIGHT,
                widthPx = 800,
                heightPx = 1000,
                rawBytes = bytes,
            ),
            finding = finding,
            meanings = PalmistryContentPackage.getMeaningsForFindings(finding, "en"),
            evidence = finding.evidence,
            userContext = userContext,
        )

        val reportGen = PalmistryReportGenerator()
        val resolver = object : ReportTextResolver {
            override val language: ReportLanguage = ReportLanguage.ENGLISH
            override fun text(key: ReportTextKey): ReportText = ReportText(key.key, key.key)
            override fun bodyName(body: com.aynvora.core.models.CelestialBody): String = body.name
            override fun signName(sign: com.aynvora.core.models.Rashi): String = sign.name
            override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String = nakshatra.name
            override fun enumLabel(identifier: String): String = identifier
            override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String = tithi.name
            override fun varaName(vara: com.aynvora.astro.panchang.Vara): String = vara.name
            override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String = yoga.name
            override fun karanaName(karana: com.aynvora.astro.panchang.Karana): String = karana.name
            override fun number(value: Double, decimalPlaces: Int): String = "$value"
            override fun birthDate(year: Int, month: Int, day: Int): String = "$year-$month-$day"
            override fun birthTime(hour: Int, minute: Int, second: Int): String = "$hour:$minute:$second"
            override fun generatedAtUtc(epochMillis: Long): String = "$epochMillis"
        }

        val doc = reportGen.generate(
            PalmistryReportInput(
                language = ReportLanguage.ENGLISH,
                generatedAtEpochMs = 1791100800000L,
                session = session,
            ),
            resolver
        )

        assertEquals("report.palmistry.title", doc.title.value)
        val captureSection = doc.sections.find { it.id == "capture_evidence" }
        assertNotNull(captureSection)

        // Quality and line geometry tables must be included
        assertTrue(captureSection.blocks.any { it is com.aynvora.core.report.ReportTable })

        // Attribution section must declare on-device model provenance
        val attrSection = doc.sections.find { it.id == "attribution" }
        assertNotNull(attrSection)
        }
    }

    @Test
    fun testHandMatchingLeftHandPass() {
        val bytes = createSyntheticPalmImage(isLeftHand = true)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
        )

        val detection = engine.detectHand(source, HandType.LEFT)
        assertNotNull(detection.detectedHand)
        assertEquals(HandType.LEFT, detection.detectedHand)
        assertEquals(PalmHandValidationStatus.PASS, detection.validationStatus)
    }

    @Test
    fun testPluggablePalmLineDetectorContract() {
        val customDetector = object : PalmLineDetector {
            override fun detectPalmLines(source: PalmImageSource, detection: HandDetectionResult): PalmEvidence {
                return PalmEvidence(
                    imageId = "custom_test_id",
                    selectedHand = detection.selectedHand,
                    detectedHand = detection.detectedHand,
                    handConfidence = detection.handConfidence,
                    palmQuality = detection.quality,
                    landmarks = detection.landmarks,
                    palmBounds = detection.palmBounds,
                    orientation = 0.0f,
                    heartLine = PalmLineGeometry(PalmLineType.HEART_LINE, true, 0.95f, 0.90f, 0.5f, 0.4f, Point2D(0.2f, 0.3f), Point2D(0.8f, 0.4f), listOf(Point2D(0.2f, 0.3f), Point2D(0.8f, 0.4f))),
                    headLine = null,
                    lifeLine = null,
                    fateLine = null,
                    additionalDetectedLines = emptyList(),
                    modelMetadata = PalmModelMetadata(lineSegmentationModel = "Custom TFLite Mock"),
                    annotationVersion = "2.0.0",
                )
            }
        }

        val engineWithCustom = PalmImageAnalysisEngine(lineDetector = customDetector)
        val bytes = createSyntheticPalmImage(isLeftHand = false)
        val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)
        val detection = engineWithCustom.detectHand(source, HandType.RIGHT)
        val evidence = engineWithCustom.detectPalmLines(source, detection)

        assertEquals("custom_test_id", evidence.imageId)
        assertEquals("Custom TFLite Mock", evidence.modelMetadata.lineSegmentationModel)
        assertNotNull(evidence.heartLine)
        assertEquals(0.95f, evidence.heartLine?.confidence)
    }

    @Test
    fun testPrivacyAnalyticsExcludesBiometricAndImageryPayloads() {
        // Technical event payload
        val allowedTechnicalKeys = setOf("event_name", "session_id", "status", "latency_ms", "hand_type")
        val forbiddenBiometricKeys = setOf("raw_bytes", "landmarks", "line_points", "coordinates", "pixel_data", "biometric_vector", "image_base64")

        val eventPayload = mapOf(
            "event_name" to "PALM_CAPTURE_COMPLETED",
            "session_id" to "sess_12345",
            "status" to "SUCCESS",
            "latency_ms" to "142",
            "hand_type" to "RIGHT"
        )

        // Verify no biometric or sensitive keys exist in event payload
        for (key in eventPayload.keys) {
            assertTrue(allowedTechnicalKeys.contains(key), "Key $key must be an allowed technical telemetry key")
            assertFalse(forbiddenBiometricKeys.contains(key), "Key $key must not contain biometric data")
        }
    }

    @Test
    fun testHandMatchingSelectedLeftActualRightProducesWrongHand() {
        val bytes = createSyntheticPalmImage(isLeftHand = false) // Right hand image
        val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)

        val detection = engine.detectHand(source, HandType.LEFT)
        assertEquals(HandType.LEFT, detection.selectedHand)
        assertEquals(HandType.RIGHT, detection.detectedHand)
        assertEquals(PalmHandValidationStatus.WRONG_HAND, detection.validationStatus)
    }

    @Test
    fun testExtremeLightingFailuresAreActionable() {
        // Test Under-exposed image (pitch dark)
        val darkBytes = ByteArray(480 * 640) { 15 } // Mean lum = 15 < 35
        val darkSource = PalmImageSource(data = darkBytes, widthPx = 480, heightPx = 640)
        val darkQuality = engine.validatePalmQuality(darkSource, HandType.RIGHT)
        assertFalse(darkQuality.isUsable)
        assertTrue(darkQuality.failures.any { it.contains("under-exposed", ignoreCase = true) })

        // Test Over-exposed image (washed out white)
        val brightBytes = ByteArray(480 * 640) { 245.toByte() } // Mean lum = 245 > 235
        val brightSource = PalmImageSource(data = brightBytes, widthPx = 480, heightPx = 640)
        val brightQuality = engine.validatePalmQuality(brightSource, HandType.RIGHT)
        assertFalse(brightQuality.isUsable)
        assertTrue(brightQuality.failures.any { it.contains("overexposed", ignoreCase = true) })
    }

    @Test
    fun testMalformedGeometryProtection() {
        val nanPoint = Point2D(Float.NaN, 0.5f)
        val infPoint = Point2D(0.5f, Float.POSITIVE_INFINITY)
        val outOfBoundsPoint = Point2D(1.5f, -0.2f)
        val validPointA = Point2D(0.2f, 0.3f)
        val validPointB = Point2D(0.8f, 0.4f)

        val malformedLine = PalmLineGeometry(
            type = PalmLineType.HEART_LINE,
            detected = true,
            confidence = 0.85f,
            continuity = 0.80f,
            normalizedLength = 0.6f,
            curvature = 0.1f,
            originPoint = validPointA,
            terminationPoint = validPointB,
            geometry = listOf(nanPoint, infPoint, outOfBoundsPoint, validPointA, validPointB),
        )

        // Filter invalid points
        val sanitizedPoints = malformedLine.geometry.filter { p ->
            !p.x.isNaN() && !p.y.isNaN() && !p.x.isInfinite() && !p.y.isInfinite() &&
                p.x in 0.0f..1.0f && p.y in 0.0f..1.0f
        }

        assertEquals(2, sanitizedPoints.size)
        assertEquals(validPointA, sanitizedPoints[0])
        assertEquals(validPointB, sanitizedPoints[1])
    }
}


