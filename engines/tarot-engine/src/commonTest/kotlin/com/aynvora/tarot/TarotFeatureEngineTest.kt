package com.aynvora.tarot

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.tarot.engine.TarotFeatureEngine
import com.aynvora.tarot.engine.TarotRequest
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TarotFeatureEngineTest {

    private val engine = TarotFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testTarotSpreadDrawDeterministicSeed() = runTest {
        val request = TarotRequest(
            spreadId = "single_card",
            allowReversed = true,
            seed = 42L,
            timestampEpochMs = 1774000000000L,
        )

        val event = AynvoraEvent(
            eventId = "evt_tarot_1",
            featureId = AynvoraFeatureId.TAROT,
            eventType = "DRAW_SPREAD",
            timestampEpochMs = 1774000000000L,
            requestId = "req_tarot_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val reading = json.decodeFromString<TarotReading>(response.resultJson)
        assertEquals(1, reading.draws.size)
        assertEquals("single_card", reading.spreadId)

        // Draw again with same seed, cards must match
        val response2 = engine.handle(event)
        val reading2 = json.decodeFromString<TarotReading>(response2.resultJson)
        assertEquals(reading.draws[0].card.id, reading2.draws[0].card.id)
        assertEquals(reading.draws[0].orientation, reading2.draws[0].orientation)
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.NUMEROLOGY,
            eventType = "DRAW_SPREAD",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
