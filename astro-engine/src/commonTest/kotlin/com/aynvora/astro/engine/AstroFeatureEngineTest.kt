package com.aynvora.astro.engine

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AstroFeatureEngineTest {

    private val engine = AstroFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAstrologyEventHandlingDeterministicCalculation() = runTest {
        val request = AstrologyRequest(
            name = "Ishant",
            dateOfBirth = "1996-10-04",
            timeOfBirth = "14:30:00",
            location = AstrologyLocation(
                city = "New Delhi",
                latitude = 28.6139,
                longitude = 77.2090,
                timezone = 5.5,
            ),
        )

        val event = AynvoraEvent(
            eventId = "evt_astro_1",
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "CALCULATE_CHART",
            timestampEpochMs = 1774000000000L,
            requestId = "req_astro_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<AstrologyResult>(response.resultJson)
        assertTrue(result.julianDay > 2400000.0)
        assertTrue(result.planets.isNotEmpty())
        assertTrue(result.planets.any { it.name == "SUN" })
        assertTrue(result.planets.any { it.name == "MOON" })
    }

    @Test
    fun testInvalidFeatureIdRejection() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.PALMISTRY, // wrong feature
            eventType = "CALCULATE_CHART",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
