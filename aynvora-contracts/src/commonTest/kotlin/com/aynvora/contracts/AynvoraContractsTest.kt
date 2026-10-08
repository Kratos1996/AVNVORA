package com.aynvora.contracts

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AynvoraContractsTest {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun testEventSerializationAndDeserialization() {
        val event = AynvoraEvent(
            eventId = "evt_123",
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "CALCULATE_CHART",
            timestampEpochMs = 1774000000000L,
            requestId = "req_999",
            payloadJson = """{"year":2026,"month":10}""",
        )

        val encoded = json.encodeToString(event)
        assertTrue(encoded.contains("ASTROLOGY"))
        assertTrue(encoded.contains("CALCULATE_CHART"))

        val decoded = json.decodeFromString<AynvoraEvent>(encoded)
        assertEquals(event.eventId, decoded.eventId)
        assertEquals(event.featureId, decoded.featureId)
        assertEquals(event.payloadJson, decoded.payloadJson)
    }

    @Test
    fun testEventResponseHelpers() {
        val success = AynvoraEventResponse.success(
            eventId = "evt_1",
            requestId = "req_1",
            featureId = AynvoraFeatureId.PALMISTRY,
            resultJson = """{"detectedHand":"RIGHT"}""",
            durationMs = 45L,
        )
        assertTrue(success.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, success.status)

        val failure = AynvoraEventResponse.failure(
            eventId = "evt_2",
            requestId = "req_2",
            featureId = AynvoraFeatureId.AI_ASSISTANT,
            status = AynvoraStatus.AI_UNAVAILABLE,
            messageKey = "error.ai.unavailable",
        )
        assertEquals(false, failure.isSuccess)
        assertEquals(AynvoraStatus.AI_UNAVAILABLE, failure.status)
        assertEquals("error.ai.unavailable", failure.error?.messageKey)
    }

    @Test
    fun testFeatureSettingsAccess() {
        val settings = AynvoraFeatureSettings()
        assertTrue(settings.isEnabled(AynvoraFeatureId.ASTROLOGY))
        assertTrue(settings.isEnabled(AynvoraFeatureId.PALMISTRY))

        val disabledSettings = settings.withFeatureState(AynvoraFeatureId.PALMISTRY, FeatureAccessState.DISABLED)
        assertEquals(false, disabledSettings.isEnabled(AynvoraFeatureId.PALMISTRY))
        assertTrue(disabledSettings.isEnabled(AynvoraFeatureId.ASTROLOGY))
    }
}
