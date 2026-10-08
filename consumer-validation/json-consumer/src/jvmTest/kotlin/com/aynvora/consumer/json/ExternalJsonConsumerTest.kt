package com.aynvora.consumer.json

import com.aynvora.astro.engine.AstroEngineProvider
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.FeatureAccessState
import com.aynvora.sdk.Aynvora
import com.aynvora.sdk.AynvoraConfig
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExternalJsonConsumerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun test9_RawJsonAstrologyDispatch_Success() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        val rawInput = """
            {
                "name": "Headless Native",
                "dateOfBirth": "1994-07-12",
                "timeOfBirth": "18:45:00",
                "location": {
                    "latitude": 13.0827,
                    "longitude": 80.2707,
                    "city": "Chennai",
                    "country": "India"
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

        assertEquals(AynvoraStatus.SUCCESS, response.status)
        val resultJson = response.resultJson
        assertNotNull(resultJson)
        val root = json.parseToJsonElement(resultJson).jsonObject
        assertTrue(root.containsKey("julianDay"))
        assertTrue(root.containsKey("lagnaSign"))
        assertTrue(root.containsKey("planets"))
    }

    @Test
    fun test8_InvalidPayloadOrSchema_ReturnsGracefulError() = runTest {
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        // Completely malformed payload
        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = """{"invalid_field": 123}"""
            )
        )

        assertTrue(
            response.status == AynvoraStatus.INVALID_REQUEST ||
            response.status == AynvoraStatus.ENGINE_ERROR ||
            response.status == AynvoraStatus.CALCULATION_FAILED
        )
        assertNotNull(response.error)
    }

    @Test
    fun test7_FeatureDisabledViaSettings_ReturnsFeatureDisabled() = runTest {
        val settings = AynvoraFeatureSettings(
            featureStates = mapOf(AynvoraFeatureId.ASTROLOGY to FeatureAccessState.DISABLED)
        )
        val sdk = Aynvora.create(
            AynvoraConfig(
                settings = settings,
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        val response = sdk.dispatch(
            AynvoraEventRequest(
                featureId = AynvoraFeatureId.ASTROLOGY,
                eventType = "CALCULATE_CHART",
                payloadJson = """{"name":"Test","dateOfBirth":"1990-01-01","timeOfBirth":"12:00:00","location":{"latitude":0.0,"longitude":0.0}}"""
            )
        )

        assertEquals(AynvoraStatus.FEATURE_DISABLED, response.status)
    }

    @Test
    fun test12_CustomUiDataConsumption_NoComposeDependency() = runTest {
        // Simulates a custom UI ViewModel extracting values from canonical JSON
        val sdk = Aynvora.create(
            AynvoraConfig(
                engineProviders = listOf(AstroEngineProvider())
            )
        )

        val rawInput = """
            {
                "name": "Custom UI User",
                "dateOfBirth": "1988-11-05",
                "timeOfBirth": "08:15:00",
                "location": {
                    "latitude": 28.6139,
                    "longitude": 77.2090
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

        assertEquals(AynvoraStatus.SUCCESS, response.status)
        val jsonString = response.resultJson!!

        // Custom UI layer projections
        data class CustomUiChartCard(val lagna: String, val planetCount: Int)
        val parsed = json.parseToJsonElement(jsonString).jsonObject
        val card = CustomUiChartCard(
            lagna = parsed["lagnaSign"].toString().replace("\"", ""),
            planetCount = parsed["planets"]?.toString()?.split("name")?.size?.minus(1) ?: 0
        )

        assertTrue(card.lagna.isNotBlank())
        assertTrue(card.planetCount > 0)
    }
}
