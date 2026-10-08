package com.aynvora.sdk

import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.FeatureAccessState
import com.aynvora.numerology.NumerologyRequest
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.tarot.engine.TarotRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AynvoraSdkTest {

    @Test
    fun testModeATypedAstrologyCalculation() = runTest {
        val sdk = Aynvora.create()
        val request = AstrologyRequest(
            name = "Test Native",
            dateOfBirth = "1990-05-15",
            timeOfBirth = "14:30:00",
            location = AstrologyLocation(
                latitude = 28.6139,
                longitude = 77.2090,
                timezone = 5.5,
            ),
        )

        val result = sdk.astrology.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertTrue(data.julianDay > 0.0)
        assertTrue(data.planets.isNotEmpty())
    }

    @Test
    fun testModeATypedNumerologyCalculation() = runTest {
        val sdk = Aynvora.create()
        val request = NumerologyRequest(
            birthDay = 15,
            birthMonth = 5,
            birthYear = 1990,
            fullName = "Alexander",
        )

        val result = sdk.numerology.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertNotNull(data.profile.radical)
        assertNotNull(data.profile.destiny)
    }

    @Test
    fun testModeATypedTarotDraw() = runTest {
        val sdk = Aynvora.create()
        val request = TarotRequest(
            spreadId = "three_card_timeline",
        )

        val result = sdk.tarot.draw(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertEquals(3, data.draws.size)
    }

    @Test
    fun testModeBRawEventAstrologyJsonDispatch() = runTest {
        val sdk = Aynvora.create()
        val jsonPayload = """
            {
                "name": "Headless User",
                "dateOfBirth": "1995-10-25",
                "timeOfBirth": "08:15:00",
                "location": {
                    "latitude": 19.0760,
                    "longitude": 72.8777,
                    "timezone": 5.5
                }
            }
        """.trimIndent()

        val rawRequest = AynvoraEventRequest(
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "CALCULATE_CHART",
            payloadJson = jsonPayload,
            requestId = "headless_req_001",
        )

        val response = sdk.dispatch(rawRequest)
        assertEquals(AynvoraStatus.SUCCESS, response.status)
        assertTrue(response.isSuccess)
        assertTrue(response.resultJson.contains("\"julianDay\""))
        assertTrue(response.resultJson.contains("\"planets\""))
    }

    @Test
    fun testModeBRawEventPalmistryJsonDispatch() = runTest {
        // Headless Demonstration Example 2:
        // External App -> SDK -> PALMISTRY_ANALYZE JSON -> PALMISTRY_RESULT JSON
        val sdk = Aynvora.create()
        val jsonPayload = """
            {
                "selectedHand": "RIGHT",
                "widthPx": 800,
                "heightPx": 1200
            }
        """.trimIndent()

        val rawRequest = AynvoraEventRequest(
            featureId = AynvoraFeatureId.PALMISTRY,
            eventType = "ANALYZE_PALM",
            payloadJson = jsonPayload,
            requestId = "headless_palm_002",
        )

        val response = sdk.dispatch(rawRequest)
        assertEquals(AynvoraStatus.SUCCESS, response.status)
        assertTrue(response.isSuccess)
        assertTrue(response.resultJson.contains("\"selectedHand\""))
        assertTrue(response.resultJson.contains("\"validation\""))
    }

    @Test
    fun testModeBRawEventReportJsonDispatch() = runTest {
        // Headless Demonstration Example 3:
        // External App -> SDK -> REPORT_GENERATE JSON -> REPORT_RESULT JSON
        val sdk = Aynvora.create()
        val jsonPayload = """
            {
                "reportType": "KUNDALI",
                "title": "Comprehensive Vedic Kundali Report",
                "format": "PDF",
                "sections": ["LAGNA_CHART", "PLANETARY_DETAILS", "DASHA_TIMELINE"]
            }
        """.trimIndent()

        val rawRequest = AynvoraEventRequest(
            featureId = AynvoraFeatureId.REPORT,
            eventType = "GENERATE_REPORT",
            payloadJson = jsonPayload,
            requestId = "headless_report_003",
        )

        val response = sdk.dispatch(rawRequest)
        assertEquals(AynvoraStatus.SUCCESS, response.status)
        assertTrue(response.isSuccess)
        assertTrue(response.resultJson.contains("\"reportId\""))
        assertTrue(response.resultJson.contains("\"artifactPath\""))
    }

    @Test
    fun testModeBRawEventAiJsonDispatch() = runTest {
        // Headless Demonstration Example 4:
        // External App -> SDK -> AI_REQUEST JSON -> AI_RESULT JSON
        val sdk = Aynvora.create()
        val jsonPayload = """
            {
                "query": "Explain dharma and righteous duty in personal conduct",
                "targetFeatureId": "ASTROLOGY",
                "structuredEvidenceJson": "{\"lagna\":\"Aries\",\"atmakaraka\":\"Sun\"}"
            }
        """.trimIndent()

        val rawRequest = AynvoraEventRequest(
            featureId = AynvoraFeatureId.AI_ASSISTANT,
            eventType = "GROUNDED_QUERY",
            payloadJson = jsonPayload,
            requestId = "headless_ai_004",
        )

        val response = sdk.dispatch(rawRequest)
        assertEquals(AynvoraStatus.SUCCESS, response.status)
        assertTrue(response.isSuccess)
        assertTrue(response.resultJson.contains("\"responseText\""))
        assertTrue(response.resultJson.contains("\"modelId\""))
        assertTrue(response.resultJson.contains("qwen2.5-1.5b-instruct-q5_k_m"))
    }

    @Test
    fun testModeATypedGemstoneRecommendation() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.gemstone.engine.GemstoneRequest(action = "CATALOG")
        val result = sdk.gemstone.recommend(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertNotNull(data.catalog)
        assertTrue(data.catalog!!.isNotEmpty())
    }

    @Test
    fun testModeATypedGitaVerse() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.gita.engine.GitaRequest(action = "GET_VERSE", chapterNumber = 2, verseNumber = 47)
        val result = sdk.gita.getVerse(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertNotNull(data.verse)
        assertEquals(2, data.verse!!.chapterNumber)
        assertEquals(47, data.verse!!.verseNumber)
    }

    @Test
    fun testModeATypedGarudaPuranTopics() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.garudapuran.engine.GarudaPuranRequest(action = "GET_TOPICS")
        val result = sdk.garudaPuran.getChapter(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertTrue(data.topics.isNotEmpty())
    }

    @Test
    fun testModeATypedRudrakshaLookup() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.rudraksha.engine.RudrakshaRequest(action = "CATALOG")
        val result = sdk.rudraksha.lookup(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertTrue(data.types.isNotEmpty())
    }

    @Test
    fun testModeATypedJadiLookup() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.jadi.engine.JadiRequest(action = "PLANETS")
        val result = sdk.jadi.lookup(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertEquals("FOUNDATION_ONLY", data.status)
    }

    @Test
    fun testModeATypedYantraLookup() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.yantra.engine.YantraRequest(action = "CATALOG")
        val result = sdk.yantra.lookup(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertTrue(data.yantras.isNotEmpty())
    }

    @Test
    fun testModeATypedGuidance() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.guidance.engine.GuidanceRequest(dateIso = "2026-10-08")
        val result = sdk.guidance.getGuidance(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertNotNull(data.dailyGuidance)
        assertEquals("2026-10-08", data.dailyGuidance.dateIso)
    }

    @Test
    fun testModeATypedAiAssistant() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.ai.engine.AiGroundingRequest(
            query = "What is the cosmic nature of Jupiter?",
            structuredEvidenceJson = "{\"graha\":\"JUPITER\"}",
        )
        val result = sdk.ai.ask(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertTrue(data.isGrounded)
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", data.modelId)
    }

    @Test
    fun testModeATypedReportGeneration() = runTest {
        val sdk = Aynvora.create()
        val request = com.aynvora.report.engine.ReportGenerateRequest(
            reportType = "COMBINED",
            title = "Holistic Self-Discovery Blueprint",
            sections = listOf("ASTROLOGY", "NUMEROLOGY", "PALMISTRY"),
        )
        val result = sdk.report.generate(request)
        assertTrue(result is AynvoraResult.Success)
        val data = (result as AynvoraResult.Success).value
        assertEquals("COMBINED", data.reportType)
        assertEquals(3, data.sectionCount)
    }

    @Test
    fun testFeatureAccessGateEnforcement() = runTest {
        val sdk = Aynvora.create()

        // 1. Verify astrology is initially enabled
        assertTrue(sdk.isFeatureEnabled(AynvoraFeatureId.ASTROLOGY))

        // 2. Disable astrology dynamically
        val disabledSettings = sdk.getSettings().withFeatureState(
            AynvoraFeatureId.ASTROLOGY,
            FeatureAccessState.DISABLED,
        )
        sdk.updateSettings(disabledSettings)

        // 3. Verify it is now marked disabled
        assertTrue(!sdk.isFeatureEnabled(AynvoraFeatureId.ASTROLOGY))

        // 4. Raw dispatch must return FEATURE_DISABLED
        val rawRequest = AynvoraEventRequest(
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "CALCULATE_CHART",
            payloadJson = "{}",
            requestId = "req_blocked",
        )
        val response = sdk.dispatch(rawRequest)
        assertEquals(AynvoraStatus.FEATURE_DISABLED, response.status)

        // 5. Typed API must return UnsupportedConfiguration failure
        val typedRequest = AstrologyRequest(
            dateOfBirth = "1990-01-01",
            timeOfBirth = "12:00:00",
            location = AstrologyLocation(latitude = 0.0, longitude = 0.0),
        )
        val typedResult = sdk.astrology.calculate(typedRequest)
        assertTrue(typedResult is AynvoraResult.Failure.UnsupportedConfiguration)

        // 6. Re-enable astrology
        sdk.updateSettings(sdk.getSettings().withFeatureState(
            AynvoraFeatureId.ASTROLOGY,
            FeatureAccessState.ENABLED,
        ))
        assertTrue(sdk.isFeatureEnabled(AynvoraFeatureId.ASTROLOGY))
    }
}

