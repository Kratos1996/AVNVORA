package com.aynvora.palmistry

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.palmistry.engine.PalmFeatureEngine
import com.aynvora.palmistry.engine.PalmistryRequest
import com.aynvora.palmistry.engine.PalmistryResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PalmFeatureEngineTest {

    private val engine = PalmFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    private fun createSyntheticPalm(width: Int = 320, height: Int = 320): ByteArray {
        val bytes = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var lum = 130
                val dx = (x - width / 2.0) / (width / 2.0)
                val dy = (y - height / 2.0) / (height / 2.0)
                if (dx * dx + dy * dy < 0.5) lum += 35
                // Thumb protrusion on left side for right hand
                if (y in (height * 0.4).toInt()..(height * 0.7).toInt() && x < width * 0.35) {
                    lum += 25
                }
                bytes[y * width + x] = lum.coerceIn(0, 255).toByte()
            }
        }
        return bytes
    }

    @Test
    fun testPalmEventHandling() = runTest {
        val imgBytes = createSyntheticPalm(320, 320)
        val request = PalmistryRequest(
            imageBytes = imgBytes,
            selectedHand = HandType.RIGHT,
            widthPx = 320,
            heightPx = 320,
        )

        val event = AynvoraEvent(
            eventId = "evt_palm_1",
            featureId = AynvoraFeatureId.PALMISTRY,
            eventType = "ANALYZE_PALM",
            timestampEpochMs = 1774000000000L,
            requestId = "req_palm_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Response failed: ${response.status} - ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<PalmistryResult>(response.resultJson)
        assertEquals(HandType.RIGHT, result.selectedHand)
        assertTrue(result.qualityScore > 0.0f)
    }

    @Test
    fun testMismatchedFeatureIdRejection() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "ANALYZE_PALM",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
