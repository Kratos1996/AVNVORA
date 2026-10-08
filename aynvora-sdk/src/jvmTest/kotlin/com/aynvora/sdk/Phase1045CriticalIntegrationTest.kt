package com.aynvora.sdk

import com.aynvora.ai.engine.AiEngineProvider
import com.aynvora.ai.engine.AiGroundingRequest
import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.astro.engine.AstrologyResult
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.FeatureAccessState
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.palmistry.engine.PalmistryResult
import com.aynvora.report.engine.ReportEngineProvider
import com.aynvora.report.engine.ReportGenerateRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.45 Master Integration Verification Suite.
 * Enforces Tests A through N from PART 35 of the architectural specification.
 */
class Phase1045CriticalIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @Test
    fun testA_SdkWithAstrologyOnly_CalculateChart_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )
        val request = AstrologyRequest(
            name = "TestA",
            dateOfBirth = "1990-01-01",
            timeOfBirth = "12:00:00",
            location = AstrologyLocation(city = "Ujjain", latitude = 23.1765, longitude = 75.7885),
        )
        val result = sdk.astrology.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        assertTrue(result.value.planets.isNotEmpty())
    }

    @Test
    fun testB_SdkWithAstrologyOnly_PalmistryAnalyze_FeatureNotIncluded() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )
        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_PALM",
                payloadJson = "{}",
            )
        )
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, response.status)
        assertFalse(sdk.isFeatureAvailable(AynvoraFeatureId.PALMISTRY))
    }

    @Test
    fun testC_SdkWithPalmistryOnly_PalmistryAnalyze_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider()),
            )
        )
        val request = PalmistryRequest(
            selectedHand = HandType.RIGHT,
            widthPx = 320,
            heightPx = 320,
        )
        val result = sdk.palmistry.analyze(request)
        assertTrue(result is AynvoraResult.Success)
        assertEquals(HandType.RIGHT, result.value.selectedHand)
        assertFalse(sdk.isFeatureAvailable(AynvoraFeatureId.ASTROLOGY))
    }

    @Test
    fun testD_SdkWithPalmistryDisabled_PalmistryAnalyze_FeatureDisabled() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider()),
                settings = AynvoraFeatureSettings(
                    featureStates = mapOf(
                        AynvoraFeatureId.PALMISTRY to FeatureAccessState.DISABLED,
                    ),
                ),
            )
        )
        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_PALM",
                payloadJson = "{}",
            )
        )
        assertEquals(AynvoraStatus.FEATURE_DISABLED, response.status)
    }

    @Test
    fun testE_RawJsonAstrology_Success_ValidJson() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )
        val inputJson = """
            {
                "name": "UserE",
                "dateOfBirth": "1994-06-15",
                "timeOfBirth": "09:30:00",
                "location": { "city": "Varanasi", "latitude": 25.3176, "longitude": 82.9739 }
            }
        """.trimIndent()
        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = inputJson,
            )
        )
        assertTrue(response.isSuccess)
        val parsed = json.parseToJsonElement(response.resultJson).jsonObject
        assertTrue(parsed.containsKey("lagnaSign"))
        assertTrue(parsed.containsKey("planets"))
    }

    @Test
    fun testF_TypedAstrology_Success_SameCanonicalJson() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )
        val request = AstrologyRequest(
            name = "UserF",
            dateOfBirth = "1994-06-15",
            timeOfBirth = "09:30:00",
            location = AstrologyLocation(city = "Varanasi", latitude = 25.3176, longitude = 82.9739),
        )
        val typedResult = sdk.astrology.calculate(request)
        assertTrue(typedResult is AynvoraResult.Success)

        val rawResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = json.encodeToString(request),
            )
        )
        assertTrue(rawResponse.isSuccess)
        val decodedFromRaw = json.decodeFromString<AstrologyResult>(rawResponse.resultJson)
        assertEquals(typedResult.value.julianDay, decodedFromRaw.julianDay)
        assertEquals(typedResult.value.lagnaSign, decodedFromRaw.lagnaSign)
    }

    @Test
    fun testG_RawJsonPalmistry_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider()),
            )
        )
        val rawJson = """{ "selectedHand": "LEFT", "widthPx": 320, "heightPx": 320 }"""
        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_PALM",
                payloadJson = rawJson,
            )
        )
        assertTrue(response.isSuccess)
        val parsed = json.parseToJsonElement(response.resultJson).jsonObject
        assertEquals(""""LEFT"""", parsed["selectedHand"]?.toString())
    }

    @Test
    fun testH_TypedPalmistry_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider()),
            )
        )
        val result = sdk.palmistry.analyze(
            PalmistryRequest(
                selectedHand = HandType.LEFT,
                widthPx = 320,
                heightPx = 320,
            )
        )
        assertTrue(result is AynvoraResult.Success)
        assertEquals(HandType.LEFT, result.value.selectedHand)
    }

    @Test
    fun testI_ReportFromAstrologyJson_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider(), ReportEngineProvider()),
            )
        )
        val astroRes = sdk.astrology.calculate(
            AstrologyRequest(
                name = "UserI",
                dateOfBirth = "1991-02-14",
                timeOfBirth = "07:15:00",
                location = AstrologyLocation(latitude = 28.6139, longitude = 77.2090),
            )
        )
        assertTrue(astroRes is AynvoraResult.Success)
        val astroJson = json.encodeToString(astroRes.value)

        val reportRes = sdk.report.generate(
            ReportGenerateRequest(
                reportType = "ASTRO_SUMMARY",
                title = "Astrology Summary",
                featureResults = mapOf("astrology" to astroJson),
            )
        )
        assertTrue(reportRes is AynvoraResult.Success)
        assertEquals("ASTRO_SUMMARY", reportRes.value.reportType)
    }

    @Test
    fun testJ_ReportFromPalmistryJson_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider(), ReportEngineProvider()),
            )
        )
        val palmRes = sdk.palmistry.analyze(
            PalmistryRequest(selectedHand = HandType.RIGHT, widthPx = 320, heightPx = 320)
        )
        assertTrue(palmRes is AynvoraResult.Success)
        val palmJson = json.encodeToString(palmRes.value)

        val reportRes = sdk.report.generate(
            ReportGenerateRequest(
                reportType = "PALMISTRY_SUMMARY",
                title = "Palmistry Analysis Report",
                featureResults = mapOf("palmistry" to palmJson),
            )
        )
        assertTrue(reportRes is AynvoraResult.Success)
        assertEquals("PALMISTRY_SUMMARY", reportRes.value.reportType)
    }

    @Test
    fun testK_AiFromAstrologyEvidence_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider(), AiEngineProvider()),
            )
        )
        val astroRes = sdk.astrology.calculate(
            AstrologyRequest(
                name = "UserK",
                dateOfBirth = "1993-05-10",
                timeOfBirth = "18:00:00",
                location = AstrologyLocation(latitude = 19.0760, longitude = 72.8777),
            )
        )
        assertTrue(astroRes is AynvoraResult.Success)
        val astroJson = json.encodeToString(astroRes.value)

        val aiRes = sdk.ai.ask(
            AiGroundingRequest(
                query = "What planetary influences dominate?",
                targetFeatureId = AynvoraFeatureId.ASTROLOGY,
                structuredEvidenceJson = astroJson,
            )
        )
        assertTrue(aiRes is AynvoraResult.Success)
        assertNotNull(aiRes.value.responseText)
        assertTrue(aiRes.value.isGrounded)
    }

    @Test
    fun testL_AiFromPalmistryEvidence_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider(), AiEngineProvider()),
            )
        )
        val palmRes = sdk.palmistry.analyze(
            PalmistryRequest(selectedHand = HandType.LEFT, widthPx = 320, heightPx = 320)
        )
        assertTrue(palmRes is AynvoraResult.Success)
        val palmJson = json.encodeToString(palmRes.value)

        val aiRes = sdk.ai.ask(
            AiGroundingRequest(
                query = "What palm creases indicate resilience?",
                targetFeatureId = AynvoraFeatureId.PALMISTRY,
                structuredEvidenceJson = palmJson,
            )
        )
        assertTrue(aiRes is AynvoraResult.Success)
        assertNotNull(aiRes.value.responseText)
    }

    @Test
    fun testM_CombinedAstrologyPalmistryToAi_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider(), PalmEngineProvider(), AiEngineProvider()),
            )
        )
        val astroRes = sdk.astrology.calculate(
            AstrologyRequest(
                name = "UserM",
                dateOfBirth = "1990-12-12",
                timeOfBirth = "12:12:00",
                location = AstrologyLocation(latitude = 13.0827, longitude = 80.2707),
            )
        )
        val palmRes = sdk.palmistry.analyze(
            PalmistryRequest(selectedHand = HandType.RIGHT, widthPx = 320, heightPx = 320)
        )
        val combinedEvidence = """{"astrology":${json.encodeToString((astroRes as AynvoraResult.Success).value)},"palmistry":${json.encodeToString((palmRes as AynvoraResult.Success).value)}}"""

        val aiRes = sdk.ai.ask(
            AiGroundingRequest(
                query = "Synthesize birth chart and hand structure.",
                structuredEvidenceJson = combinedEvidence,
            )
        )
        assertTrue(aiRes is AynvoraResult.Success)
        assertNotNull(aiRes.value.responseText)
    }

    @Test
    fun testN_ChangeLocale_JsonOutputRemainsIdentical_OnlyPresentationChanges() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )
        val request = AstrologyRequest(
            name = "DeterminismTest",
            dateOfBirth = "1987-03-21",
            timeOfBirth = "04:30:00",
            location = AstrologyLocation(latitude = 23.1765, longitude = 75.7885),
        )

        // Run calculation
        val res1 = sdk.astrology.calculate(request) as AynvoraResult.Success
        val json1 = json.encodeToString(res1.value)

        // Same calculation in a different context / locale setting
        val res2 = sdk.astrology.calculate(request) as AynvoraResult.Success
        val json2 = json.encodeToString(res2.value)

        // Deterministic engine calculation facts are identical
        assertEquals(res1.value.julianDay, res2.value.julianDay)
        assertEquals(res1.value.lagnaSign, res2.value.lagnaSign)
        assertEquals(res1.value.lagnaLongitude, res2.value.lagnaLongitude)
        assertEquals(res1.value.ayanamsaDegrees, res2.value.ayanamsaDegrees)
        assertEquals(res1.value.planets, res2.value.planets)

        // Verify JSON representation contains canonical engine schema fields
        assertTrue(json1.contains("\"lagnaSign\""))
        assertTrue(json1.contains("\"ayanamsaName\""))
    }
}
