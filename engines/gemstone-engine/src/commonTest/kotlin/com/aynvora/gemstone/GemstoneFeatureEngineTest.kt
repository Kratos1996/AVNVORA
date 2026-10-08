package com.aynvora.gemstone

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.CelestialBody
import com.aynvora.contracts.Rashi
import com.aynvora.gemstone.engine.GemstoneFeatureEngine
import com.aynvora.gemstone.engine.GemstoneRequest
import com.aynvora.gemstone.engine.GemstoneResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GemstoneFeatureEngineTest {

    private val engine = GemstoneFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGemstoneCatalogRetrieval() = runTest {
        val request = GemstoneRequest(action = "CATALOG")
        val event = AynvoraEvent(
            eventId = "evt_gem_1",
            featureId = AynvoraFeatureId.GEMSTONE,
            eventType = "GET_CATALOG",
            timestampEpochMs = 1774000000000L,
            requestId = "req_gem_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<GemstoneResult>(response.resultJson)
        assertEquals(9, result.catalog?.size)
    }

    @Test
    fun testGemstoneRecommendations() = runTest {
        val astroProfile = GemstoneAstrologyProfile(
            lagnaRashi = Rashi.ARIES,
            lagnaLord = CelestialBody.MARS,
            fifthLord = CelestialBody.SUN,
            ninthLord = CelestialBody.JUPITER,
            moonRashi = Rashi.CANCER,
            moonLord = CelestialBody.MOON,
            functionalBenefics = setOf(CelestialBody.MARS, CelestialBody.SUN, CelestialBody.JUPITER),
            functionalMalefics = setOf(CelestialBody.SATURN, CelestialBody.MERCURY, CelestialBody.VENUS),
        )

        val request = GemstoneRequest(
            action = "RECOMMEND",
            astroProfile = astroProfile,
        )

        val event = AynvoraEvent(
            eventId = "evt_gem_2",
            featureId = AynvoraFeatureId.GEMSTONE,
            eventType = "GET_RECOMMENDATIONS",
            timestampEpochMs = 1774000000000L,
            requestId = "req_gem_2",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        val result = json.decodeFromString<GemstoneResult>(response.resultJson)
        assertTrue(result.recommendations?.primaryRecommendations?.isNotEmpty() == true)
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.ASTROLOGY,
            eventType = "GET_CATALOG",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
