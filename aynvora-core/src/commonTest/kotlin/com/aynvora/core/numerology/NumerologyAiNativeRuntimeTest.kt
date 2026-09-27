package com.aynvora.core.numerology

import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiGenerationResponse
import com.aynvora.core.ai.AiInferenceDiagnostics
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.ai.LlamaNativeRuntimeDriver
import com.aynvora.core.ai.LocalNativeInferenceEngine
import com.aynvora.core.ai.LocalSimulationEngine
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.7: Native vs Simulated Runtime & Lifecycle Verification Test.
 *
 * Enforces non-negotiable architectural boundaries:
 * 1. Never label simulation as native runtime.
 * 2. If native library is unavailable, LocalNativeInferenceEngine reports typed failure.
 * 3. LocalSimulationEngine explicitly marks executionMode = LOCAL_SIMULATION.
 * 4. GroundedSlmNumerologyExplanationEngine executes all 7 categories cleanly with fallback.
 */
class NumerologyAiNativeRuntimeTest {

    private val connector = NumerologyFeatureDataConnector()

    @Test
    fun testLocalNativeInferenceEngine_WithoutNativeLibraryFailsGracefully() = runBlocking {
        // Native driver without JNI bridge
        val driver = LlamaNativeRuntimeDriver(bridge = null)
        assertFalse(driver.isAvailable(), "Driver must report unavailable without bridge")

        val nativeEngine = LocalNativeInferenceEngine(nativeRuntime = driver)
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M

        // Attempting to load native model when runtime is unavailable
        val loadResult = nativeEngine.load(variant, "/data/local/tmp/fake.gguf")
        assertTrue(
            loadResult is AynvoraResult.Failure,
            "Native load must fail without native library"
        )
        assertEquals(AiInferenceStatus.ERROR, nativeEngine.getStatus())
    }

    @Test
    fun testLocalSimulationEngine_ExplicitlyReportsSimulatedMode() = runBlocking {
        val simEngine = LocalSimulationEngine()
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M

        val loadRes = simEngine.load(variant, "/models/qwen.gguf")
        assertTrue(loadRes is AynvoraResult.Success)
        assertEquals(AiInferenceStatus.READY, simEngine.getStatus())

        val genReq = AiGenerationRequest(
            requestId = "req_sim_1",
            systemPrompt = "System instructions",
            userPrompt = "Explain number 7",
            language = "en",
        )
        val genRes = simEngine.generate(genReq)
        assertTrue(genRes is AynvoraResult.Success)
        assertEquals(
            AiExecutionMode.LOCAL_SIMULATION,
            genRes.value.executionMode,
            "Must never label simulation as native"
        )

        val diag = simEngine.getDiagnostics()
        assertEquals(AiExecutionMode.LOCAL_SIMULATION, diag.executionMode)
    }

    @Test
    fun testGroundedSlmEngine_ExecutesAll7QuestionCategories() = runBlocking {
        val fakeInference = object : AiInferenceEngine {
            override suspend fun load(variant: AiModelVariant, modelFilePath: String) =
                AynvoraResult.Success(Unit)

            override suspend fun unload() = AynvoraResult.Success(Unit)
            override suspend fun generate(request: AiGenerationRequest): AynvoraResult<AiGenerationResponse> {
                val synthesized =
                    "Verified Calculated Values: • Life Path: 7. In the Pythagorean tradition, this is a symbol of contemplation."
                return AynvoraResult.Success(
                    AiGenerationResponse(
                        requestId = request.requestId,
                        text = synthesized,
                        tokensGenerated = 15,
                        finishReason = "STOP",
                        isOfflineExecution = true,
                        executionMode = AiExecutionMode.REAL_MODEL_INFERENCE,
                    )
                )
            }

            override suspend fun cancel(requestId: String) = true
            override fun getStatus() = AiInferenceStatus.READY
            override fun getLoadedModel(): AiModelVariant? = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
            override fun getDiagnostics() = AiInferenceDiagnostics()
        }

        val slmEngine = GroundedSlmNumerologyExplanationEngine(
            aiInferenceEngine = fakeInference,
            deterministicEngine = DeterministicNumerologyExplanationEngine(),
        )

        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value

        val categoriesToTest = listOf(
            NumerologyAiQuestionCategory.EXPLAIN_RESULT,
            NumerologyAiQuestionCategory.EXPLAIN_CALCULATION,
            NumerologyAiQuestionCategory.EXPLAIN_TRADITION,
            NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON,
            NumerologyAiQuestionCategory.CLARIFY_SOURCE,
            NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
            NumerologyAiQuestionCategory.REPORT_SUMMARY,
        )

        for (category in categoriesToTest) {
            val context = connector.buildGroundingContext(
                result = res,
                userQuestion = "Question for $category",
                questionCategory = category,
            )
            val explanation = slmEngine.explain(context)
            assertTrue(explanation is AynvoraResult.Success, "Must explain category $category")
            val resp = explanation.value
            assertNotNull(resp.answer)
            assertTrue(resp.answer.isNotBlank())
            assertEquals(category, resp.questionCategory)
            assertTrue(resp.isOfflineExecution, "Execution must be offline")
        }
    }
}
