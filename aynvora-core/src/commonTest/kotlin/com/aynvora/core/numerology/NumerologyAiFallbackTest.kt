package com.aynvora.core.numerology

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
import kotlin.test.assertTrue

class NumerologyFakeInferenceEngine(
    var mockStatus: AiInferenceStatus = AiInferenceStatus.READY,
    var mockLoadedModel: AiModelVariant? = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M,
    var generatedText: String = "According to Western Pythagorean traditions, your Life Path number 7 represents contemplative reflection.",
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
            return AynvoraResult.Failure.InternalFailure("Inference timeout / Out of Memory")
        }
        return AynvoraResult.Success(
            AiGenerationResponse(
                requestId = request.requestId,
                text = generatedText,
                tokensGenerated = 20,
                finishReason = "STOP",
                isOfflineExecution = true,
                provenance = request.evidenceProvenance,
            )
        )
    }

    override suspend fun cancel(requestId: String): Boolean = true
}

/**
 * Phase 10.6: Numerology AI Fallback Test.
 *
 * Verifies that when local SLM is uninstalled, fails, or produces invalid output:
 * - System seamlessly falls back to 100% deterministic source-backed explanation.
 * - Zero network / cloud calls occur.
 * - Deterministic numbers and provenance are strictly preserved.
 */
class NumerologyAiFallbackTest {

    private val connector = NumerologyFeatureDataConnector()

    private fun getGoldenContext(): NumerologyAiGroundingContext {
        val req = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        return connector.buildGroundingContext(
            result = res,
            userQuestion = "What does my Life Path 7 mean?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )
    }

    @Test
    fun testUninstalledModel_TriggersDeterministicFallback() = runBlocking {
        val fakeEngine = NumerologyFakeInferenceEngine(
            mockStatus = AiInferenceStatus.UNLOADED,
            mockLoadedModel = null,
        )
        val engine = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = fakeEngine,
            deterministicEngine = DeterministicNumerologyExplanationEngine(),
        )

        val context = getGoldenContext()
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val response = res.value

        assertTrue(response.isFallback)
        assertEquals("MODEL_NOT_INSTALLED", response.fallbackReason)
        assertTrue(response.answer.contains("7"), "Fallback must contain Life Path 7")
        assertTrue(response.citedSources.isNotEmpty())
    }

    @Test
    fun testInferenceFailure_TriggersDeterministicFallback() = runBlocking {
        val failingEngine = NumerologyFakeInferenceEngine(
            shouldFailGeneration = true,
        )

        val engine = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = failingEngine,
            deterministicEngine = DeterministicNumerologyExplanationEngine(),
        )

        val context = getGoldenContext()
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val response = res.value

        assertTrue(response.isFallback)
        assertTrue(response.fallbackReason?.contains("INFERENCE_FAILED") == true)
        assertTrue(response.answer.contains("7"), "Fallback must preserve authoritative value")
    }

    @Test
    fun testValidationFailure_TriggersDeterministicFallback() = runBlocking {
        // Model that alters the number to 8 (hallucination)
        val hallucinatingEngine = NumerologyFakeInferenceEngine(
            generatedText = "Your Life Path number is 8. Number 8 brings wealth and fortune.",
        )

        val engine = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = hallucinatingEngine,
            deterministicEngine = DeterministicNumerologyExplanationEngine(),
        )

        val context = getGoldenContext()
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val response = res.value

        // Must reject the hallucinated 8 and fall back to verified 7
        assertTrue(response.isFallback, "Must fall back when model hallucinated number")
        assertTrue(
            response.fallbackReason?.startsWith("VALIDATION_FAILED") == true,
            "Must indicate validation failure"
        )
        assertTrue(response.answer.contains("7"), "Fallback output must preserve true Life Path 7")
    }

    @Test
    fun testFallback_IsCompletelyOffline() = runBlocking {
        val engine = DeterministicNumerologyExplanationEngine()
        val context = getGoldenContext()
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        assertTrue(res.value.isOfflineExecution)
    }
}
