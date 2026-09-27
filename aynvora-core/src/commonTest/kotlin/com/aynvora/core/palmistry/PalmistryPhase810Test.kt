package com.aynvora.core.palmistry

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiGenerationResponse
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.report.PalmistryReportGenerator
import com.aynvora.core.report.PalmistryReportInput
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ReportType
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PalmistryPhase810Test {

    private val analysisEngine = PalmImageAnalysisEngine()
    private val contentPackage = PalmistryContentPackage
    private val dataConnector = PalmistryFeatureDataConnector()

    private fun createSyntheticPalmBytes(
        width: Int = 480,
        height: Int = 640,
        baseLum: Int = 150
    ): ByteArray {
        val total = width * height
        val bytes = ByteArray(total)
        for (i in 0 until total) {
            val x = i % width
            val y = i / width
            var lum =
                baseLum + (sin(x.toDouble() / 15.0) * 15).toInt() + (sin(y.toDouble() / 20.0) * 12).toInt()
            if (abs(y - (x * 0.8 + 20)) < 4.0) lum -= 45 // Life line arc
            if (abs(y - height * 0.45) < 4.0 && x in (width * 0.2).toInt()..(width * 0.8).toInt()) lum -= 40 // Head line
            if (abs(y - height * 0.30 - (sin(x.toDouble() / 20.0) * 5)) < 4.0 && x in (width * 0.2).toInt()..(width * 0.85).toInt()) lum -= 42 // Heart line
            if (abs(x - width * 0.5) < 3.0 && y in (height * 0.35).toInt()..(height * 0.75).toInt()) lum -= 35 // Fate line
            bytes[i] = lum.coerceIn(40, 220).toByte()
        }
        return bytes
    }

    private fun PalmFinding.toEvidenceList(readingId: String): List<PalmistryEvidence> {
        return lines.filter { it.detected }.map { line ->
            PalmistryEvidence(
                evidenceId = "ev_${readingId}_${line.lineType.name}",
                readingId = readingId,
                hand = handType,
                featureType = line.lineType.name,
                observation = "${line.lineType.name} detected with ${line.strength} strength and ${line.lengthCategory} length",
                confidence = line.clarityScore,
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. IMAGE QUALITY TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testValidImageQuality() {
        val bytes = createSyntheticPalmBytes(480, 640, baseLum = 150)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertEquals(ImageQualityState.GOOD, assessment.state)
        assertTrue(assessment.isAcceptable)
        assertTrue(assessment.score > 0.6f)
    }

    @Test
    fun testLowResolutionImage() {
        val bytes = createSyntheticPalmBytes(120, 160)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 120,
            heightPx = 160,
            sourceType = PalmImageSourceType.GALLERY,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertEquals(ImageQualityState.LOW_RESOLUTION, assessment.state)
        assertFalse(assessment.isAcceptable)
    }

    @Test
    fun testTooDarkImage() {
        val bytes = ByteArray(480 * 640) { 20.toByte() }
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertEquals(ImageQualityState.TOO_DARK, assessment.state)
        assertFalse(assessment.isAcceptable)
    }

    @Test
    fun testTooBrightImage() {
        val bytes = ByteArray(480 * 640) { 245.toByte() }
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertEquals(ImageQualityState.TOO_BRIGHT, assessment.state)
        assertFalse(assessment.isAcceptable)
    }

    @Test
    fun testHandNotDetectedImage() {
        val bytes = ByteArray(480 * 640) { 128.toByte() }
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.GALLERY,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertTrue(
            assessment.state == ImageQualityState.HAND_NOT_DETECTED ||
                    assessment.state == ImageQualityState.BLURRY ||
                    assessment.state == ImageQualityState.PALM_NOT_VISIBLE
        )
        assertFalse(assessment.isAcceptable)
    }

    @Test
    fun testEmptyOrInvalidImageBytes() {
        val source = PalmImageSource(
            data = ByteArray(0),
            widthPx = 0,
            heightPx = 0,
            sourceType = PalmImageSourceType.GALLERY,
            capturedAtEpochMs = 1000L,
        )
        val assessment = analysisEngine.validateImageQuality(source)
        assertEquals(ImageQualityState.UNSUPPORTED, assessment.state)
        assertFalse(assessment.isAcceptable)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. PALM ANALYSIS & EVIDENCE GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testRealPalmAnalysisDetectsMajorLines() {
        val bytes = createSyntheticPalmBytes(480, 640, baseLum = 150)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val result = analysisEngine.analyzePalm(
            source = source,
            handType = HandType.RIGHT,
        )
        assertTrue(result is AynvoraResult.Success)
        val finding = (result as AynvoraResult.Success).value

        assertEquals(HandType.RIGHT, finding.handType)
        assertTrue(finding.overallClarity > 0.0f)

        // Verify major lines were processed
        val lifeLine = finding.lines.firstOrNull { it.lineType == PalmLineType.LIFE_LINE }
        assertNotNull(lifeLine)
        assertTrue(lifeLine.clarityScore > 0.0f)

        val headLine = finding.lines.firstOrNull { it.lineType == PalmLineType.HEAD_LINE }
        assertNotNull(headLine)

        val heartLine = finding.lines.firstOrNull { it.lineType == PalmLineType.HEART_LINE }
        assertNotNull(heartLine)

        // Minor unsupported lines in Phase 8.10 must be marked NOT_DETECTED
        val sunLine = finding.lines.firstOrNull { it.lineType == PalmLineType.SUN_LINE }
        assertNotNull(sunLine)
        assertFalse(sunLine.detected, "Unsupported sun line must not be falsely detected")

        val mercuryLine = finding.lines.firstOrNull { it.lineType == PalmLineType.MERCURY_LINE }
        assertNotNull(mercuryLine)
        assertFalse(mercuryLine.detected, "Unsupported mercury line must not be falsely detected")

        // Check capabilities registry
        assertTrue(PalmistryAnalysisCapabilities.isLifeLineSupported)
        assertTrue(PalmistryAnalysisCapabilities.isHeadLineSupported)
        assertTrue(PalmistryAnalysisCapabilities.isHeartLineSupported)
        assertFalse(PalmistryAnalysisCapabilities.isMarriageLineSupported)
        assertFalse(PalmistryAnalysisCapabilities.isSunLineSupported)
        assertFalse(PalmistryAnalysisCapabilities.isMercuryLineSupported)
    }

    @Test
    fun testStructuredPalmEvidenceGraphIntegration() {
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.LEFT) as AynvoraResult.Success).value
        val session = PalmReadingSession(
            id = "reading_graph",
            startedAtEpochMs = 1000L,
            handType = HandType.LEFT,
            finding = finding,
        )

        val evidenceList = dataConnector.extractEvidence(session)
        assertTrue(evidenceList.isNotEmpty())

        val graph = dataConnector.buildEvidenceGraph(session)
        assertEquals("reading_graph", graph.queryId)
        assertTrue(graph.nodes.isNotEmpty())

        // Check node provenance
        val shapeNode = graph.nodes["palm_reading_graph_shape"]
        assertNotNull(shapeNode)
        assertEquals("AYNVORA Palm Vision Engine", shapeNode.provenance.sourceName)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. TRADITIONAL CONTENT & LOCALIZATION
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testTraditionalContentEnglishAndHindi() {
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(data = bytes, widthPx = 480, heightPx = 640)
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value

        val enMeanings = contentPackage.getMeaningsForFindings(finding, "en")
        assertTrue(enMeanings.isNotEmpty())
        val enShape = enMeanings.find { it.featureType == "PALM_SHAPE" }
        assertNotNull(enShape)
        assertEquals("en", enShape.language)
        assertTrue(enShape.sourceReference.isNotBlank())

        val hiMeanings = contentPackage.getMeaningsForFindings(finding, "hi")
        assertTrue(hiMeanings.isNotEmpty())
        val hiShape = hiMeanings.find { it.featureType == "PALM_SHAPE" }
        assertNotNull(hiShape)
        assertEquals("hi", hiShape.language)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. ON-DEVICE AI EXPLANATION & DETERMINISTIC FALLBACK
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testDeterministicFallbackExplanation(): Unit = runBlocking {
        val fallbackEngine = DeterministicPalmistryExplanationEngine()
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value

        val request = PalmistryExplanationRequest(
            readingId = "reading_fallback",
            hand = HandType.RIGHT,
            language = "en",
            evidence = finding.toEvidenceList("reading_fallback"),
            approvedMeanings = contentPackage.getMeaningsForFindings(finding, "en"),
            sourceReference = "Samudrika Shastra Classical Hastrekha System",
        )

        val result = fallbackEngine.explain(request)
        assertTrue(result is AynvoraResult.Success)
        val explanation = (result as AynvoraResult.Success).value
        assertTrue(explanation.fallbackUsed)
        assertEquals("reading_fallback", explanation.readingId)
        assertTrue(explanation.summary.isNotBlank())
        assertTrue(explanation.reflection.isNotBlank())
        assertTrue(explanation.keyThemes.isNotEmpty())
        assertEquals(
            "Samudrika Shastra Classical Hastrekha System",
            explanation.provenance.sourceName
        )
    }

    @Test
    fun testGroundedSlmExplanationWithMockEngine(): Unit = runBlocking {
        val mockAi = object : AiInferenceEngine {
            override suspend fun load(
                variant: AiModelVariant,
                modelFilePath: String
            ): AynvoraResult<Unit> = AynvoraResult.Success(Unit)

            override suspend fun unload(): AynvoraResult<Unit> = AynvoraResult.Success(Unit)
            override suspend fun cancel(requestId: String): Boolean = true
            override fun getStatus(): AiInferenceStatus = AiInferenceStatus.READY
            override fun getLoadedModel(): AiModelVariant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Success(
                    AiGenerationResponse(
                        requestId = request.requestId,
                        text = "SUMMARY: Classical palm analysis reflects harmonious vital currents.\n" +
                                "REFLECTION: Ground your actions in patient determination.\n" +
                                "THEMES: Vitality, Resilience, Clarity",
                        tokensGenerated = 40,
                        finishReason = "STOP",
                    )
                )
            }
        }

        val slmEngine = GroundedSlmPalmistryExplanationEngine(aiInferenceEngine = mockAi)
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value

        val request = PalmistryExplanationRequest(
            readingId = "reading_slm",
            hand = HandType.RIGHT,
            language = "en",
            evidence = finding.toEvidenceList("reading_slm"),
            approvedMeanings = contentPackage.getMeaningsForFindings(finding, "en"),
            sourceReference = "Samudrika Shastra",
        )

        val result = slmEngine.explain(request)
        assertTrue(result is AynvoraResult.Success)
        val explanation = (result as AynvoraResult.Success).value
        assertFalse(explanation.fallbackUsed)
        assertTrue(
            explanation.summary.contains("qwen") || explanation.summary.contains("Qwen") || explanation.summary.contains(
                "Palmistry"
            )
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. QUESTION SYSTEM & INSUFFICIENT EVIDENCE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testQuestionEngineAnswersDetectedLine(): Unit = runBlocking {
        val questionEngine =
            PalmQuestionEngine(explanationEngine = DeterministicPalmistryExplanationEngine())
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value
        val session = PalmReadingSession(
            id = "reading_q1",
            startedAtEpochMs = 1000L,
            handType = HandType.RIGHT,
            finding = finding,
        )

        val answerResult = questionEngine.answerQuestion(
            session = session,
            questionText = "What does my heart line suggest about emotional balance?",
            language = "en",
        )
        assertTrue(answerResult is AynvoraResult.Success)
        val (question, event) = (answerResult as AynvoraResult.Success).value
        assertTrue(question.status == PalmAnswerStatus.COMPLETED || question.status == PalmAnswerStatus.FALLBACK)
        assertTrue(question.answerSummary?.isNotBlank() == true)
        assertTrue(question.answerInterpretation?.isNotBlank() == true)
        assertTrue(event.eventType == PalmTimelineEventType.AI_ANSWER_GENERATED || event.eventType == PalmTimelineEventType.AI_ANSWER_FALLBACK)
    }

    @Test
    fun testQuestionEngineHonestInsufficientEvidenceForUnsupportedLine(): Unit = runBlocking {
        val questionEngine =
            PalmQuestionEngine(explanationEngine = DeterministicPalmistryExplanationEngine())
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value
        val session = PalmReadingSession(
            id = "reading_q2",
            startedAtEpochMs = 1000L,
            handType = HandType.RIGHT,
            finding = finding,
        )

        val answerResult = questionEngine.answerQuestion(
            session = session,
            questionText = "When will I get married according to my marriage line?",
            language = "en",
        )
        assertTrue(answerResult is AynvoraResult.Success)
        val (question, _) = (answerResult as AynvoraResult.Success).value
        assertEquals(PalmAnswerStatus.INSUFFICIENT_EVIDENCE, question.status)
        assertTrue(question.answerInterpretation?.contains("not distinctly detected") == true)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. REPORT GENERATOR
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun testPalmistryReportGeneratorProducesDocument() {
        val reportGen = PalmistryReportGenerator()
        val bytes = createSyntheticPalmBytes(480, 640)
        val source = PalmImageSource(
            data = bytes,
            widthPx = 480,
            heightPx = 640,
            sourceType = PalmImageSourceType.CAMERA,
            capturedAtEpochMs = 1000L,
        )
        val finding =
            (analysisEngine.analyzePalm(source, HandType.RIGHT) as AynvoraResult.Success).value
        val session = PalmReadingSession(
            id = "report_session",
            startedAtEpochMs = 1000L,
            handType = HandType.RIGHT,
            finding = finding,
        )

        val input = PalmistryReportInput(
            language = ReportLanguage.ENGLISH,
            generatedAtEpochMs = 1700000000000L,
            session = session,
        )

        val mockResolver = object : ReportTextResolver {
            override val language: ReportLanguage = ReportLanguage.ENGLISH
            override fun text(key: ReportTextKey): ReportText =
                ReportText(key.key, "Hastrekha Reflection Report")

            override fun bodyName(body: com.aynvora.core.models.CelestialBody): String = body.name
            override fun signName(sign: com.aynvora.core.models.Rashi): String = sign.name
            override fun nakshatraName(nakshatra: com.aynvora.core.models.Nakshatra): String =
                nakshatra.name

            override fun enumLabel(identifier: String): String = identifier
            override fun tithiName(tithi: com.aynvora.astro.panchang.Tithi): String = tithi.name
            override fun varaName(vara: com.aynvora.astro.panchang.Vara): String = vara.name
            override fun yogaName(yoga: com.aynvora.astro.panchang.PanchangYoga): String = yoga.name
            override fun karanaName(karana: com.aynvora.astro.panchang.Karana): String = karana.name
            override fun number(value: Double, decimalPlaces: Int): String = value.toString()
            override fun birthDate(year: Int, month: Int, day: Int): String = "$year-$month-$day"
            override fun birthTime(hour: Int, minute: Int, second: Int): String =
                "$hour:$minute:$second"

            override fun generatedAtUtc(epochMillis: Long): String = "$epochMillis"
        }

        val doc = reportGen.generate(input, mockResolver)

        assertEquals(ReportType.PALMISTRY.id, doc.metadata.reportTypeId)
        assertEquals("palm_report_session", doc.metadata.reportId)
        assertTrue(doc.sections.isNotEmpty())

        val disclaimerSection = doc.sections.find { it.id == "disclaimer" }
        assertNotNull(disclaimerSection)
        assertTrue(
            disclaimerSection.title.value.contains(
                "Disclaimer",
                ignoreCase = true
            ) || disclaimerSection.title.value.contains("Disclosure", ignoreCase = true)
        )

        val linesSection = doc.sections.find { it.id == "lines" }
        assertNotNull(linesSection)
    }
}
