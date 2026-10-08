package com.aynvora.ai

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.ai.engine.AiFeatureEngine
import com.aynvora.ai.engine.AiGroundingRequest
import com.aynvora.ai.engine.AiGroundingResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiFeatureEngineTest {

    private val engine = AiFeatureEngine()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAiGroundingWithPalmistryEvidence() = runTest {
        val request = AiGroundingRequest(
            query = "What does my palm line indicate?",
            targetFeatureId = AynvoraFeatureId.PALMISTRY,
            structuredEvidenceJson = """{"validation":"PASS","qualityScore":0.88}""",
        )

        val event = AynvoraEvent(
            eventId = "evt_ai_1",
            featureId = AynvoraFeatureId.AI_ASSISTANT,
            eventType = "AI_GROUNDING",
            timestampEpochMs = 1774000000000L,
            requestId = "req_ai_1",
            payloadJson = json.encodeToString(request),
        )

        val response = engine.handle(event)
        assertTrue(response.isSuccess)
        assertEquals(AynvoraStatus.SUCCESS, response.status)

        val result = json.decodeFromString<AiGroundingResult>(response.resultJson)
        assertTrue(result.isGrounded)
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", result.modelId)
    }
}
