package com.aynvora.guidance

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.guidance.engine.GuidanceFeatureEngine
import com.aynvora.guidance.engine.GuidanceRequest
import com.aynvora.guidance.engine.GuidanceResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GuidanceFeatureEngineTest {

    private val engine = GuidanceFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGuidanceGeneration() = runTest {
        val request = GuidanceRequest(dateIso = "2026-10-06")
        val event = AynvoraEvent(
            eventId = "evt_gui_1",
            featureId = AynvoraFeatureId.GUIDANCE,
            eventType = "GET_GUIDANCE",
            timestampEpochMs = 1774000000000L,
            requestId = "req_gui_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<GuidanceResult>(response.resultJson)
        assertEquals("2026-10-06", result.dailyGuidance.dateIso)
        assertTrue(result.dailyGuidance.morning.actionItems.isNotEmpty())
    }
}
