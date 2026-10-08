package com.aynvora.gita

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.gita.engine.GitaFeatureEngine
import com.aynvora.gita.engine.GitaRequest
import com.aynvora.gita.engine.GitaResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GitaFeatureEngineTest {

    private val engine = GitaFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testVerseRetrieval() = runTest {
        val request = GitaRequest(
            action = "GET_VERSE",
            chapterNumber = 1,
            verseNumber = 1,
        )

        val event = AynvoraEvent(
            eventId = "evt_gita_1",
            featureId = AynvoraFeatureId.GITA,
            eventType = "GET_VERSE",
            timestampEpochMs = 1774000000000L,
            requestId = "req_gita_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess, "Failed: ${response.error?.details}")
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<GitaResult>(response.resultJson)
        assertEquals(1, result.verse?.chapterNumber)
        assertEquals(1, result.verse?.verseNumber)
        assertTrue(result.verse?.sanskritDevanagari?.isNotEmpty() == true)
    }

    @Test
    fun testFeatureIdMismatch() = runTest {
        val event = AynvoraEvent(
            eventId = "evt_err",
            featureId = AynvoraFeatureId.TAROT,
            eventType = "GET_VERSE",
            timestampEpochMs = 1774000000000L,
            requestId = "req_err",
            payloadJson = "{}",
        )

        val response = engine.handle(event)
        assertEquals(AynvoraStatus.INVALID_REQUEST, response.status)
    }
}
