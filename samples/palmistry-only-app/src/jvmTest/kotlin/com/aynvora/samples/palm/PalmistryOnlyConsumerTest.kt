package com.aynvora.samples.palm

import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.palmistry.HandType
import com.aynvora.palmistry.engine.PalmEngineProvider
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PalmistryOnlyConsumerTest {

    @Test
    fun testPalmistryOnlyAppFunctionsIndependentlyWithoutAstrology() = runTest {
        // App C: Palmistry Only consumer - no astrology dependency in build
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider()),
            )
        )

        // 1. Palmistry execution without any astrology birth data
        val request = PalmistryRequest(
            selectedHand = HandType.RIGHT,
            widthPx = 320,
            heightPx = 320,
        )

        val result = sdk.palmistry.analyze(request)
        assertTrue(result is AynvoraResult.Success, "Expected successful palm analysis: $result")
        val analysis = result.value
        assertEquals(HandType.RIGHT, analysis.selectedHand)
        assertNotNull(analysis.validation)
        assertTrue(analysis.lines.isNotEmpty())

        // 2. Raw JSON dispatch for Palmistry works
        val jsonPayload = """
            {
                "selectedHand": "LEFT",
                "widthPx": 320,
                "heightPx": 320
            }
        """.trimIndent()

        val jsonResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_PALM",
                payloadJson = jsonPayload,
            )
        )
        assertTrue(jsonResponse.isSuccess)
        assertTrue(jsonResponse.resultJson.contains(""""selectedHand":"LEFT""""))

        // 3. Execution of Astrology MUST FAIL with FEATURE_NOT_INCLUDED
        // Note: Astrology classes are physically NOT imported in this compilation unit
        val astroRawResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = "{}",
            )
        )
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, astroRawResponse.status)
        assertFalse(sdk.isFeatureAvailable(AynvoraFeatureId.ASTROLOGY))
    }
}
