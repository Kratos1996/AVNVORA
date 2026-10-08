package com.aynvora.numerology

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.numerology.engine.NumerologyFeatureEngine
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumerologyFeatureEngineTest {

    private val engine = NumerologyFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testNumerologyEventHandling() = runTest {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "Ishant",
        )

        val event = AynvoraEvent(
            eventId = "evt_num_1",
            featureId = AynvoraFeatureId.NUMEROLOGY,
            eventType = "CALCULATE_NUMEROLOGY",
            timestampEpochMs = 1774000000000L,
            requestId = "req_num_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<NumerologyResult>(response.resultJson)
        assertEquals(11, result.profile.radical?.radicalValue)
        assertEquals(7, result.profile.destiny?.destinyValue)
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.TAROT,
            eventType = "CALCULATE_NUMEROLOGY",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
