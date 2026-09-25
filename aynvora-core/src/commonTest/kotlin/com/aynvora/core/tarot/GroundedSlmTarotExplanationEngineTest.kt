package com.aynvora.core.tarot

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiGenerationResponse
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FakeAiInferenceEngine(
    var mockStatus: AiInferenceStatus = AiInferenceStatus.UNLOADED,
    var mockLoadedModel: AiModelVariant? = null,
    var generatedText: String = "Contemplative reflection produced on-device by SLM.",
    var shouldFailGeneration: Boolean = false,
) : AiInferenceEngine {

    override fun getStatus(): AiInferenceStatus = mockStatus
    override fun getLoadedModel(): AiModelVariant? = mockLoadedModel

    override suspend fun load(variant: AiModelVariant, modelFilePath: String): AynvoraResult<Unit> {
        mockLoadedModel = variant
        mockStatus = AiInferenceStatus.READY
        return AynvoraResult.Success(Unit)
    }

    override suspend fun unload(): AynvoraResult<Unit> {
        mockLoadedModel = null
        mockStatus = AiInferenceStatus.UNLOADED
        return AynvoraResult.Success(Unit)
    }

    override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
        if (shouldFailGeneration) {
            return AynvoraResult.Failure.InternalFailure("Generation crashed")
        }
        return AynvoraResult.Success(
            AiGenerationResponse(
                requestId = request.requestId,
                text = generatedText,
                tokensGenerated = 32,
                finishReason = "STOP",
                isOfflineExecution = true,
                provenance = request.evidenceProvenance,
            )
        )
    }

    override suspend fun cancel(requestId: String): Boolean = true
}

class GroundedSlmTarotExplanationEngineTest {

    private val card = TarotCard(
        id = "major_00_fool",
        number = 0,
        name = "The Fool",
        arcana = TarotArcana.MAJOR,
    )

    private val draw = TarotCardDraw(
        card = card,
        orientation = TarotCardOrientation.UPRIGHT,
        position = TarotSpreadPosition(
            id = "focus",
            orderIndex = 0,
            name = "Daily Focus",
            description = "Contemplation anchor",
        ),
    )

    private val cardContent = TarotCardContent(
        cardId = "major_00_fool",
        language = "en",
        title = "The Fool",
        shortDescription = "Beginnings, openness, spontaneity.",
        keywords = listOf("fresh", "open", "trust"),
        uprightMeaning = "Embrace beginners mind and open potential.",
        reversedMeaning = "Beware of recklessness or hesitating out of fear.",
        contentVersion = 1,
    )

    @Test
    fun slmInferenceExecutedWhenEngineReadyAndAllowed() = runBlocking {
        val fakeInference = FakeAiInferenceEngine(
            mockStatus = AiInferenceStatus.READY,
            mockLoadedModel = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M,
            generatedText = "A reflective synthesis exploring The Fool in the daily focus position.",
        )
        val engine = GroundedSlmTarotExplanationEngine(fakeInference)

        val request = TarotExplanationRequest(
            requestId = "req_1",
            draw = draw,
            spreadPositionContext = "Daily Focus",
            language = "en",
            deterministicContent = cardContent,
            allowSlmInference = true,
        )

        val result = engine.explain(request)
        assertIs<AynvoraResult.Success<TarotExplanationResult>>(result)
        val value = result.value

        assertFalse(value.fallbackUsed)
        assertEquals("major_00_fool", value.cardId)
        assertTrue(value.explanationText.contains("A reflective synthesis"))
        assertEquals("GroundedSlmTarotExplanationEngine", value.provenance.producedBy)
        assertEquals(AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M.modelId, value.provenance.modelId)
    }

    @Test
    fun fallsBackToDeterministicWhenSlmNotReady() = runBlocking {
        val fakeInference = FakeAiInferenceEngine(mockStatus = AiInferenceStatus.UNLOADED)
        val engine = GroundedSlmTarotExplanationEngine(fakeInference)

        val request = TarotExplanationRequest(
            requestId = "req_2",
            draw = draw,
            spreadPositionContext = "Daily Focus",
            language = "en",
            deterministicContent = cardContent,
            allowSlmInference = true, // requested, but engine is UNLOADED
        )

        val result = engine.explain(request)
        assertIs<AynvoraResult.Success<TarotExplanationResult>>(result)
        val value = result.value

        assertTrue(value.fallbackUsed)
        assertEquals("SLM_NOT_READY", value.provenance.fallbackReason)
        assertTrue(value.explanationText.contains("Embrace beginners mind"))
    }

    @Test
    fun fallsBackWhenSlmInferenceNotRequested() = runBlocking {
        val fakeInference = FakeAiInferenceEngine(
            mockStatus = AiInferenceStatus.READY,
            mockLoadedModel = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M,
        )
        val engine = GroundedSlmTarotExplanationEngine(fakeInference)

        val request = TarotExplanationRequest(
            requestId = "req_3",
            draw = draw,
            spreadPositionContext = "Daily Focus",
            language = "en",
            deterministicContent = cardContent,
            allowSlmInference = false,
        )

        val result = engine.explain(request)
        assertIs<AynvoraResult.Success<TarotExplanationResult>>(result)
        val value = result.value

        assertTrue(value.fallbackUsed)
        assertEquals("SLM_INFERENCE_NOT_REQUESTED", value.provenance.fallbackReason)
    }

    @Test
    fun fallsBackWhenSlmOutputFailsSafetyValidation() = runBlocking {
        val fakeInference = FakeAiInferenceEngine(
            mockStatus = AiInferenceStatus.READY,
            mockLoadedModel = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M,
            generatedText = "You will die soon and surely win lottery tomorrow.", // violates safety policy
        )
        val engine = GroundedSlmTarotExplanationEngine(fakeInference)

        val request = TarotExplanationRequest(
            requestId = "req_4",
            draw = draw,
            spreadPositionContext = "Daily Focus",
            language = "en",
            deterministicContent = cardContent,
            allowSlmInference = true,
        )

        val result = engine.explain(request)
        assertIs<AynvoraResult.Success<TarotExplanationResult>>(result)
        val value = result.value

        assertTrue(value.fallbackUsed)
        assertEquals("SLM_OUTPUT_FAILED_VALIDATION", value.provenance.fallbackReason)
        // Must contain approved safe deterministic meaning
        assertTrue(value.explanationText.contains("Embrace beginners mind"))
    }
}
