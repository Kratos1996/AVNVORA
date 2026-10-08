package com.aynvora.rudraksha

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.rudraksha.engine.RudrakshaFeatureEngine
import com.aynvora.rudraksha.engine.RudrakshaRequest
import com.aynvora.rudraksha.engine.RudrakshaResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RudrakshaFeatureEngineTest {

    private val engine = RudrakshaFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testRudrakshaCatalog() = runTest {
        val request = RudrakshaRequest(action = "CATALOG")
        val event = AynvoraEvent(
            eventId = "evt_rudra_1",
            featureId = AynvoraFeatureId.RUDRAKSHA,
            eventType = "GET_CATALOG",
            timestampEpochMs = 1774000000000L,
            requestId = "req_rudra_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<RudrakshaResult>(response.resultJson)
        assertEquals(14, result.types.size)
    }
}
