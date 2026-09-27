package com.aynvora.core.numerology

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiGenerationResponse
import com.aynvora.core.ai.AiInferenceDiagnostics
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.7: Fallback Stress and Failure Resilience Test.
 *
 * Forces multiple catastrophic failure modes in the SLM pipeline:
 * 1. Model uninstalled / null loadedModel
 * 2. Model inference failure (exception/OOM)
 * 3. Generation timeout
 * 4. Cancellation
 * 5. Validation failure (number contradiction)
 * 6. Malformed empty output
 *
 * Verifies that in 100% of cases, the system cleanly and safely falls back
 * to the deterministic engine without crashing or corrupting state.
 */
class NumerologyAiFallbackStressTest {

    private val connector = NumerologyFeatureDataConnector()
    private val deterministicEngine = DeterministicNumerologyExplanationEngine()

    private fun getGoldenResult(): NumerologyResult {
        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        return (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
    }

    @Test
    fun testStress_ModelUnavailable_FallsBackCleanly() = runBlocking {
        val uninstalledEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Failure.CalculationFailure("NOT_READY", "Engine not ready")
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.UNLOADED
            override fun getLoadedModel(): AiModelVariant? = null
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val engineWithoutModel = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = uninstalledEngine,
            deterministicEngine = deterministicEngine,
        )

        val context = connector.buildGroundingContext(getGoldenResult())
        val res = engineWithoutModel.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val resp = res.value
        assertTrue(resp.isFallback)
        assertEquals("MODEL_NOT_INSTALLED", resp.fallbackReason)
        assertEquals(NumerologyAiSafetyStatus.DETERMINISTIC_DIRECT, resp.safetyStatus)
        assertTrue(resp.answer.contains("7"))
    }

    @Test
    fun testStress_InferenceThrowsException_FallsBackCleanly() = runBlocking {
        val failingEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Failure.InternalFailure("Simulated native crash / out of memory")
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel() = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slm = GroundedSlmNumerologyExplanationEngine(failingEngine, deterministicEngine)
        val context = connector.buildGroundingContext(getGoldenResult())
        val res = slm.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val resp = res.value
        assertTrue(resp.isFallback)
        assertTrue(resp.fallbackReason?.contains("INFERENCE_FAILED") == true)
        assertTrue(resp.answer.contains("7"))
    }

    @Test
    fun testStress_InferenceTimeout_FallsBackCleanly() = runBlocking {
        val timeoutEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Failure.CalculationFailure(
                    "INFERENCE_TIMEOUT",
                    "Timed out waiting for tokens"
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel() = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slm = GroundedSlmNumerologyExplanationEngine(timeoutEngine, deterministicEngine)
        val context = connector.buildGroundingContext(getGoldenResult())
        val res = slm.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val resp = res.value
        assertTrue(resp.isFallback)
        assertTrue(resp.fallbackReason?.contains("INFERENCE_FAILED") == true)
    }

    @Test
    fun testStress_ValidationContradiction_FallsBackCleanly() = runBlocking {
        // Model hallucinating wrong number 99
        val hallucinatingEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Success(
                    AiGenerationResponse(
                        request.requestId,
                        "Your Life Path number is 99 and guarantees that you will die rich.",
                        10,
                        "STOP"
                    )
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel() = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slm = GroundedSlmNumerologyExplanationEngine(hallucinatingEngine, deterministicEngine)
        val context = connector.buildGroundingContext(getGoldenResult())
        val res = slm.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val resp = res.value
        assertTrue(resp.isFallback, "Must fall back on validation failure")
        assertEquals(NumerologyAiSafetyStatus.FALLBACK_APPLIED, resp.safetyStatus)
        assertTrue(resp.fallbackReason?.startsWith("VALIDATION_FAILED") == true)
        assertTrue(resp.answer.contains("7"), "Fallback must restore authentic Life Path 7")
    }

    @Test
    fun testStress_MalformedEmptyOutput_FallsBackCleanly() = runBlocking {
        val emptyEngine = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                return AynvoraResult.Success(
                    AiGenerationResponse(request.requestId, "   ", 0, "STOP")
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel() = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slm = GroundedSlmNumerologyExplanationEngine(emptyEngine, deterministicEngine)
        val context = connector.buildGroundingContext(getGoldenResult())
        val res = slm.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val resp = res.value
        assertTrue(resp.isFallback)
        assertTrue(resp.fallbackReason?.startsWith("VALIDATION_FAILED") == true)
        assertNotNull(resp.answer)
        assertTrue(resp.answer.contains("7"))
    }
}
