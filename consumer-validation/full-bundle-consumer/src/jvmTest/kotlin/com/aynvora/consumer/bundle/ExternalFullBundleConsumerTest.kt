package com.aynvora.consumer.bundle

import com.aynvora.ai.engine.AiEngineProvider
import com.aynvora.ai.engine.AiGroundingRequest
import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.report.engine.ReportEngineProvider
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExternalFullBundleConsumerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun test5_AstrologyAndPalmistry_BothSuccess() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    PalmEngineProvider(),
                    AiEngineProvider(),
                    ReportEngineProvider(),
                )
            )
        )

        val astroResult = sdk.astrology.calculate(
            AstrologyRequest(
                name = "Bundle User",
                dateOfBirth = "1991-03-15",
                timeOfBirth = "09:30:00",
                location = AstrologyLocation(latitude = 28.6139, longitude = 77.2090),
            )
        )
        assertTrue(astroResult is AynvoraResult.Success)

        val palmResult = sdk.palmistry.analyze(
            PalmistryRequest(selectedHand = HandType.RIGHT, widthPx = 320, heightPx = 320)
        )
        assertTrue(palmResult is AynvoraResult.Success)
    }

    @Test
    fun test6_AstrologyPlusPalmistryPlusAi_GroundedResponse() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    PalmEngineProvider(),
                    AiEngineProvider(),
                    ReportEngineProvider(),
                )
            )
        )

        val astroResult = (sdk.astrology.calculate(
            AstrologyRequest(
                name = "AI Grounding User",
                dateOfBirth = "1991-03-15",
                timeOfBirth = "09:30:00",
                location = AstrologyLocation(latitude = 28.6139, longitude = 77.2090),
            )
        ) as AynvoraResult.Success).value

        val palmResult = (sdk.palmistry.analyze(
            PalmistryRequest(selectedHand = HandType.RIGHT, widthPx = 320, heightPx = 320)
        ) as AynvoraResult.Success).value

        val combinedEvidence = """
            {
                "astrology": ${json.encodeToString(astroResult)},
                "palmistry": ${json.encodeToString(palmResult)}
            }
        """.trimIndent()

        val aiResult = sdk.ai.ask(
            AiGroundingRequest(
                query = "Explain life tendencies based on chart and palm evidence.",
                structuredEvidenceJson = combinedEvidence,
            )
        )

        assertTrue(aiResult is AynvoraResult.Success)
        assertNotNull(aiResult.value.responseText)
        assertTrue(aiResult.value.tokensUsed > 0)
    }

    @Test
    fun test10_TypedConsumer_AllTypedClients() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    PalmEngineProvider(),
                    AiEngineProvider(),
                    ReportEngineProvider(),
                )
            )
        )

        assertNotNull(sdk.astrology)
        assertNotNull(sdk.palmistry)
        assertNotNull(sdk.ai)
        assertNotNull(sdk.report)
    }

    @Test
    fun test13_ReportEngine_ConsumesFeatureJson() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    ReportEngineProvider(),
                )
            )
        )

        val astroResult = (sdk.astrology.calculate(
            AstrologyRequest(
                name = "Report User",
                dateOfBirth = "1991-03-15",
                timeOfBirth = "09:30:00",
                location = AstrologyLocation(latitude = 28.6139, longitude = 77.2090),
            )
        ) as AynvoraResult.Success).value

        val astroJson = json.encodeToString(astroResult)

        val reportRequest = com.aynvora.report.engine.ReportGenerateRequest(
            title = "Comprehensive Native Profile",
            featureResults = mapOf(
                "astrology" to astroJson
            )
        )

        val reportResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.REPORT,
                eventType = "SYNTHESIZE_REPORT",
                payloadJson = json.encodeToString(reportRequest)
            )
        )

        assertEquals(AynvoraStatus.SUCCESS, reportResponse.status)
        assertNotNull(reportResponse.resultJson)
        assertTrue(reportResponse.resultJson!!.contains("sectionCount"))
    }

    @Test
    fun test14_AiOnly_ConsumesEvidenceJson() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AiEngineProvider()
                )
            )
        )

        val evidenceJson = """{"fact": "Sun in Aries", "line": "Strong heart line"}"""

        val aiResult = sdk.ai.ask(
            AiGroundingRequest(
                query = "Synthesize evidence.",
                structuredEvidenceJson = evidenceJson
            )
        )

        assertTrue(aiResult is AynvoraResult.Success)
        assertNotNull(aiResult.value.responseText)
    }
}
