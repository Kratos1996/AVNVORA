package com.aynvora.yantra

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.yantra.engine.YantraFeatureEngine
import com.aynvora.yantra.engine.YantraRequest
import com.aynvora.yantra.engine.YantraResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YantraFeatureEngineTest {

    private val engine = YantraFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testYantraCatalog() = runTest {
        val request = YantraRequest(action = "CATALOG")
        val event = AynvoraEvent(
            eventId = "evt_yan_1",
            featureId = AynvoraFeatureId.YANTRA,
            eventType = "CATALOG",
            timestampEpochMs = 1774000000000L,
            requestId = "req_yan_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<YantraResult>(response.resultJson)
        assertEquals(13, result.yantras.size)
    }
}
