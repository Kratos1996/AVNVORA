package com.aynvora.samples.astro

import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.astro.engine.AstrologyLocation
import com.aynvora.astro.engine.AstrologyRequest
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraResult
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AstrologyOnlyConsumerTest {

    @Test
    fun testAstrologyOnlyAppFunctionsIndependently() = runTest {
        // App A: Astrology Only consumer
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )

        // 1. Typed Astrology Calculation works
        val request = AstrologyRequest(
            name = "Aryabhata",
            dateOfBirth = "1990-05-15",
            timeOfBirth = "14:30:00",
            location = AstrologyLocation(
                city = "Ujjain",
                latitude = 23.1765,
                longitude = 75.7885,
                timezone = 5.5,
            ),
        )

        val result = sdk.astrology.calculate(request)
        assertTrue(result is AynvoraResult.Success, "Expected successful calculation")
        val chart = result.value
        assertTrue(chart.lagnaSign.isNotBlank())
        assertTrue(chart.planets.isNotEmpty())

        // 2. Raw JSON dispatch for Astrology works
        val jsonPayload = """
            {
                "name": "Varahamihira",
                "dateOfBirth": "1985-11-20",
                "timeOfBirth": "06:15:00",
                "gender": "MALE",
                "location": {
                    "city": "Ujjain",
                    "latitude": 23.1765,
                    "longitude": 75.7885,
                    "timezone": 5.5
                }
            }
        """.trimIndent()

        val jsonResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = jsonPayload,
            )
        )
        assertTrue(jsonResponse.isSuccess)
        assertTrue(jsonResponse.resultJson.contains("lagnaSign"))

        // 3. Execution of Palmistry MUST FAIL with FEATURE_NOT_INCLUDED
        // Note: Palmistry classes are physically NOT imported in this compilation unit
        val palmRawResponse = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.PALMISTRY,
                eventType = "ANALYZE_PALM",
                payloadJson = "{}",
            )
        )
        assertEquals(AynvoraStatus.FEATURE_NOT_INCLUDED, palmRawResponse.status)
        assertFalse(sdk.isFeatureAvailable(AynvoraFeatureId.PALMISTRY))
    }
}
