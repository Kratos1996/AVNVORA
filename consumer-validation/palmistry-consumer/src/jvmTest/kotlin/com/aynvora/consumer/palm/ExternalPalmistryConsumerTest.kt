package com.aynvora.consumer.palm

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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExternalPalmistryConsumerTest {

    @Test
    fun test3_PalmistryAnalyze_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider())
            )
        )

        // Provide minimal Palmistry input (selectedHand + synthetic dimensions)
        val request = PalmistryRequest(
            selectedHand = HandType.RIGHT,
            widthPx = 320,
            heightPx = 320,
        )

        val result = sdk.palmistry.analyze(request)
        assertTrue(result is AynvoraResult.Success)
        val data = result.value
        assertEquals(HandType.RIGHT, data.selectedHand)
        assertTrue(data.qualityScore > 0f)
        assertNotNull(data.evidence)
    }

    @Test
    fun test4_AstrologyEvent_ReturnsFeatureNotIncluded() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(PalmEngineProvider())
            )
        )

        // Feature is reported as unavailable
        assertEquals(false, sdk.isFeatureAvailable(AynvoraFeatureId.ASTROLOGY))

        // Mode B Raw JSON dispatch returns FEATURE_NOT_INCLUDED
        val rawResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = """{"name":"Native","dateOfBirth":"1990-01-01","timeOfBirth":"12:00:00","location":{"latitude":28.6139,"longitude":77.2090}}"""
            )
        )
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, rawResponse.status)
    }

    @Test
    fun testClasspath_PhysicalExclusion_ZeroAstroEngineClasses() {
        // Verifies Astro calculation engine class is physically absent from the consumer classpath
        assertFailsWith<ClassNotFoundException> {
            Class.forName("com.aynvora.astro.AstroEngine")
        }
    }
}
