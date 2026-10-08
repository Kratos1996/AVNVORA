package com.aynvora.jadi

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.jadi.engine.JadiFeatureEngine
import com.aynvora.jadi.engine.JadiRequest
import com.aynvora.jadi.engine.JadiResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JadiFeatureEngineTest {

    private val engine = JadiFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testJadiStatus() = runTest {
        val request = JadiRequest(action = "STATUS")
        val event = AynvoraEvent(
            eventId = "evt_jadi_1",
            featureId = AynvoraFeatureId.JADI,
            eventType = "STATUS",
            timestampEpochMs = 1774000000000L,
            requestId = "req_jadi_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<JadiResult>(response.resultJson)
        assertEquals("FOUNDATION_ONLY", result.status)
    }
}
