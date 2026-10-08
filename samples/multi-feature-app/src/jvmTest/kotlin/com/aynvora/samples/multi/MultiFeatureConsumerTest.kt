package com.aynvora.samples.multi

import com.aynvora.ai.engine.AiEngineProvider
import com.aynvora.ai.engine.AiGroundingRequest
import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.astro.engine.AstrologyResult
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.palmistry.engine.PalmistryResult
import com.aynvora.report.engine.ReportEngineProvider
import com.aynvora.report.engine.ReportGenerateRequest
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MultiFeatureConsumerTest {

    @Test
    fun testMultiFeatureAppOrchestration() = runTest {
        // App B: Astrology + Palmistry + AI + Report
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(
                    AstroEngineProvider(),
                    PalmEngineProvider(),
                    AiEngineProvider(),
                    ReportEngineProvider(),
                ),
            )
        )

        // 1. Calculate Astrology
        val astroRequest = AstrologyRequest(
            name = "Raman",
            dateOfBirth = "1988-08-08",
            timeOfBirth = "08:08:00",
            location = AstrologyLocation(city = "Bangalore", latitude = 12.9716, longitude = 77.5946),
        )
        val astroResult = sdk.astrology.calculate(astroRequest)
        assertTrue(astroResult is AynvoraResult.Success)
        val astroJson = sdk.json.encodeToString<AstrologyResult>(astroResult.value)

        // 2. Analyze Palmistry
        val palmRequest = PalmistryRequest(
            selectedHand = HandType.RIGHT,
            widthPx = 320,
            heightPx = 320,
        )
        val palmResult = sdk.palmistry.analyze(palmRequest)
        assertTrue(palmResult is AynvoraResult.Success)
        val palmJson = sdk.json.encodeToString<PalmistryResult>(palmResult.value)

        // 3. Generate Composite Report consuming JSON facts (NO hidden recalculation)
        val reportRequest = ReportGenerateRequest(
            reportType = "COMPOSITE_LIFE_SYNTHESIS",
            title = "Raman - Composite Synthesis",
            featureResults = mapOf(
                "astrology" to astroJson,
                "palmistry" to palmJson,
            ),
        )
        val reportResult = sdk.report.generate(reportRequest)
        assertTrue(reportResult is AynvoraResult.Success)
        val report = reportResult.value
        assertEquals("COMPOSITE_LIFE_SYNTHESIS", report.reportType)
        assertTrue(report.sectionCount > 0)

        // 4. Grounded AI inference consuming evidence
        val aiRequest = AiGroundingRequest(
            query = "Synthesize planetary and palm evidence for career outlook.",
            targetFeatureId = AynvoraFeatureId.REPORT,
            structuredEvidenceJson = """{"astrology":$astroJson,"palmistry":$palmJson}""",
        )
        val aiResult = sdk.ai.ground(aiRequest)
        assertTrue(aiResult is AynvoraResult.Success)
        val grounding = aiResult.value
        assertNotNull(grounding.responseText)
        assertTrue(grounding.isGrounded)
    }
}
