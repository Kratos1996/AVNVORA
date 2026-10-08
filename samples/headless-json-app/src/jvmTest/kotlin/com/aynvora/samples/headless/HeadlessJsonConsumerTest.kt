package com.aynvora.samples.headless

import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeadlessJsonConsumerTest {

    @Test
    fun testHeadlessRawJsonApi() = runTest {
        // App D: Pure headless JSON consumer (e.g. backend service or cross-platform bridge)
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider()),
            )
        )

        val rawInput = """
            {
                "name": "HeadlessUser",
                "dateOfBirth": "1995-04-12",
                "timeOfBirth": "10:20:00",
                "location": {
                    "city": "Delhi",
                    "latitude": 28.6139,
                    "longitude": 77.2090,
                    "timezone": 5.5
                }
            }
        """.trimIndent()

        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = rawInput,
            )
        )

        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        // Parse result purely with standard JSON parser - no domain classes required
        val parsed = Json.parseToJsonElement(response.resultJson).jsonObject
        assertTrue(parsed.containsKey("lagnaSign"))
        assertTrue(parsed.containsKey("planets"))
        assertTrue(parsed.containsKey("julianDay"))
    }
}
