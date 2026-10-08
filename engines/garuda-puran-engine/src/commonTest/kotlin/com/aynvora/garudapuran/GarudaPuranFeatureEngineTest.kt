package com.aynvora.garudapuran

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.garudapuran.engine.GarudaPuranFeatureEngine
import com.aynvora.garudapuran.engine.GarudaPuranRequest
import com.aynvora.garudapuran.engine.GarudaPuranResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GarudaPuranFeatureEngineTest {

    private val engine = GarudaPuranFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testTopicsRetrieval() = runTest {
        val request = GarudaPuranRequest(action = "GET_TOPICS")
        val event = AynvoraEvent(
            eventId = "evt_garuda_1",
            featureId = AynvoraFeatureId.GARUDA_PURAN,
            eventType = "GET_TOPICS",
            timestampEpochMs = 1774000000000L,
            requestId = "req_garuda_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<GarudaPuranResult>(response.resultJson)
        assertTrue(result.topics.isNotEmpty())
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.GITA,
            eventType = "GET_TOPICS",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
