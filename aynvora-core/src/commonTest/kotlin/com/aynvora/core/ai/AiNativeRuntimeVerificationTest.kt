package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.DeterministicTarotExplanationEngine
import com.aynvora.core.tarot.GroundedSlmTarotExplanationEngine
import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotExplanationRequest
import com.aynvora.core.tarot.TarotSpreadPosition
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiNativeRuntimeVerificationTest {

    private val testVariant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M

    // Controlled mock native bridge for verifying JNI/C++ native lifecycle patterns
    private class TestNativeBridge(
        var available: Boolean = true,
        var shouldFailLoad: Boolean = false,
        var shouldTimeout: Boolean = false,
        var generatedText: String = "Contemplative reflection produced by native llama.cpp tensor execution.",
    ) : NativeLibraryBridge {
        val allocatedHandles = mutableSetOf<Long>()
        val releasedHandles = mutableSetOf<Long>()
        var cancelledHandle: Long? = null
        private var nextHandle = 4200L

        override fun isAvailable(): Boolean = available

        override fun load(modelPath: String, contextLength: Int, threads: Int): Long {
            if (shouldFailLoad) return 0L
            val h = nextHandle++
            allocatedHandles.add(h)
            return h
        }

        override fun generate(
            handle: Long,
            prompt: String,
            maxTokens: Int,
            temperature: Float,
            onTokenGenerated: (String) -> Boolean,
        ): String {
            if (shouldTimeout) {
                Thread.sleep(500)
            }
            onTokenGenerated("token1")
            onTokenGenerated("token2")
            return generatedText
        }

        override fun cancel(handle: Long) {
            cancelledHandle = handle
        }

        override fun release(handle: Long) {
            releasedHandles.add(handle)
            allocatedHandles.remove(handle)
        }
    }

    @Test
    fun testNativeRuntimeUnavailableWithoutLibrary(): Unit = runBlocking {
        val driver = LlamaNativeRuntimeDriver(bridge = null)
        assertFalse(driver.isAvailable())

        val result = driver.loadModel("/path/to/model.gguf", 2048)
        assertTrue(result is NativeModelHandleResult.Failure)
        val failure = result as NativeModelHandleResult.Failure
        assertEquals(AiRuntimeErrorCode.NATIVE_RUNTIME_UNAVAILABLE, failure.errorCode)
    }

    @Test
    fun testGgufFormatValidation(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val driver = LlamaNativeRuntimeDriver(bridge)

        // Invalid format without .gguf extension
        val invalidResult = driver.loadModel("/data/models/weights.bin", 2048)
        assertTrue(invalidResult is NativeModelHandleResult.Failure)
        assertEquals(
            AiRuntimeErrorCode.INVALID_MODEL_FORMAT,
            (invalidResult as NativeModelHandleResult.Failure).errorCode
        )

        // Blank path
        val blankResult = driver.loadModel("   ", 2048)
        assertTrue(blankResult is NativeModelHandleResult.Failure)
        assertEquals(
            AiRuntimeErrorCode.MODEL_FILE_NOT_FOUND,
            (blankResult as NativeModelHandleResult.Failure).errorCode
        )

        // Valid .gguf path
        val validResult = driver.loadModel("/data/models/qwen.gguf", 2048)
        assertTrue(validResult is NativeModelHandleResult.Success)
    }

    @Test
    fun testLocalNativeInferenceEngineMemoryRejection(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val driver = LlamaNativeRuntimeDriver(bridge)
        // System with only 100MB RAM (cannot safely load 0.5B model requiring 500MB safe buffer)
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = driver,
            availableRamProvider = { 100L * 1024L * 1024L },
        )

        val loadResult = engine.load(testVariant, "/data/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")
        assertTrue(loadResult is AynvoraResult.Failure.CalculationFailure)
        val failure = loadResult as AynvoraResult.Failure.CalculationFailure
        assertEquals(AiRuntimeErrorCode.INSUFFICIENT_RUNTIME_MEMORY.name, failure.code)
        assertEquals(AiInferenceStatus.ERROR, engine.getStatus())
    }

    @Test
    fun testLocalNativeInferenceEngineColdAndWarmLoad(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val driver = LlamaNativeRuntimeDriver(bridge)
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = driver,
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )

        // 1. Cold Load
        val load1 = engine.load(testVariant, "/data/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")
        assertTrue(load1 is AynvoraResult.Success)
        assertEquals(AiInferenceStatus.READY, engine.getStatus())
        assertTrue(engine.getDiagnostics().coldLoadDurationMs > 0)
        assertEquals(1, bridge.allocatedHandles.size)

        // 2. Warm Load (same model and path)
        val load2 = engine.load(testVariant, "/data/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")
        assertTrue(load2 is AynvoraResult.Success)
        assertTrue(engine.getDiagnostics().warmLoadDurationMs > 0)
        // No duplicate handle allocated on warm load
        assertEquals(1, bridge.allocatedHandles.size)
    }

    @Test
    fun testNativeResourceCleanupOnUnload(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val driver = LlamaNativeRuntimeDriver(bridge)
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = driver,
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )

        engine.load(testVariant, "/data/models/qwen.gguf")
        assertEquals(1, bridge.allocatedHandles.size)
        val handleId = bridge.allocatedHandles.first()

        engine.unload()
        assertEquals(AiInferenceStatus.UNLOADED, engine.getStatus())
        assertNull(engine.getLoadedModel())
        assertTrue(bridge.releasedHandles.contains(handleId))
        assertEquals(0, bridge.allocatedHandles.size)
    }

    @Test
    fun testRealNativeInferenceExecution(): Unit = runBlocking {
        val expectedText =
            "The Hermit card illuminates introspective depth and patient discernment."
        val bridge = TestNativeBridge(generatedText = expectedText)
        val driver = LlamaNativeRuntimeDriver(bridge)
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = driver,
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )

        engine.load(testVariant, "/data/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")

        val req = AiGenerationRequest(
            requestId = "test_req_001",
            systemPrompt = "You are AYNVORA AI on-device contemplative companion.",
            userPrompt = "Explain The Hermit card in Upright orientation for Self-Reflection context.",
            temperature = 0.7f,
            maxTokens = 256,
            language = "en",
        )

        val result = engine.generate(req)
        assertTrue(result is AynvoraResult.Success)
        val response = (result as AynvoraResult.Success).value
        assertEquals("test_req_001", response.requestId)
        assertEquals(expectedText, response.text)
        assertEquals(AiExecutionMode.LOCAL_NATIVE, response.executionMode)
        assertTrue(response.isOfflineExecution)
        assertEquals("STOP", response.finishReason)
    }

    @Test
    fun testClearSeparationBetweenNativeAndSimulationEngine(): Unit = runBlocking {
        val bridge = TestNativeBridge(generatedText = "Native output")
        val nativeEngine = LocalNativeInferenceEngine(
            nativeRuntime = LlamaNativeRuntimeDriver(bridge),
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )
        val simulationEngine = LocalSimulationEngine()

        nativeEngine.load(testVariant, "/data/models/qwen.gguf")
        simulationEngine.load(testVariant, "/data/models/qwen.gguf")

        val req = AiGenerationRequest("req1", "system", "user")
        val nativeRes = (nativeEngine.generate(req) as AynvoraResult.Success).value
        val simRes = (simulationEngine.generate(req) as AynvoraResult.Success).value

        // Strictly verify execution modes are different
        assertEquals(AiExecutionMode.LOCAL_NATIVE, nativeRes.executionMode)
        assertEquals(AiExecutionMode.LOCAL_SIMULATION, simRes.executionMode)
    }

    @Test
    fun testNativeFailureRoutesToDeterministicFallbackWithoutSimulation(): Unit = runBlocking {
        // Native bridge that fails load
        val bridge = TestNativeBridge(shouldFailLoad = true)
        val nativeEngine = LocalNativeInferenceEngine(
            nativeRuntime = LlamaNativeRuntimeDriver(bridge),
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )

        val deterministicEngine = DeterministicTarotExplanationEngine()
        val slmEngine = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = nativeEngine,
            deterministicEngine = deterministicEngine,
        )

        // Attempting to load fails native runtime
        val loadRes = nativeEngine.load(testVariant, "/data/models/qwen.gguf")
        assertTrue(loadRes is AynvoraResult.Failure)

        // Tarot reading explanation must cleanly route to deterministic fallback
        val card = TarotCard(
            id = "major_09_hermit",
            number = 9,
            name = "The Hermit",
            arcana = TarotArcana.MAJOR
        )
        val pos = TarotSpreadPosition(
            id = "single_pos",
            orderIndex = 0,
            name = "Insight",
            description = "Core insight"
        )
        val draw =
            TarotCardDraw(card = card, orientation = TarotCardOrientation.UPRIGHT, position = pos)
        val content = TarotCardContent(
            cardId = card.id,
            language = "en",
            title = "The Hermit",
            shortDescription = "Inner guidance and contemplation.",
            keywords = listOf("introspection", "solitude"),
            uprightMeaning = "Inner guidance and contemplation.",
            reversedMeaning = "Isolation and withdrawal.",
            contentVersion = 1,
        )

        val req = TarotExplanationRequest(
            requestId = "req_fallback_test",
            draw = draw,
            deterministicContent = content,
            spreadPositionContext = "Core Insight",
            language = "en",
            allowSlmInference = true,
        )

        val explainResult = slmEngine.explain(req)
        assertTrue(explainResult is AynvoraResult.Success)
        val resultValue = (explainResult as AynvoraResult.Success).value
        assertTrue(resultValue.fallbackUsed)
        assertEquals("SLM_NOT_READY", resultValue.provenance.fallbackReason)
        assertTrue(resultValue.explanationText.contains("Inner guidance"))
    }

    @Test
    fun testContextOverflowProtection(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = LlamaNativeRuntimeDriver(bridge),
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )
        engine.load(testVariant, "/data/models/qwen.gguf")

        // Prompt of 9000 chars exceeds 2048 token context
        val hugePrompt = "A".repeat(9000)
        val req = AiGenerationRequest(
            requestId = "req_huge",
            systemPrompt = "System",
            userPrompt = hugePrompt,
        )

        val result = engine.generate(req)
        assertTrue(result is AynvoraResult.Failure.CalculationFailure)
        assertEquals(
            AiRuntimeErrorCode.CONTEXT_OVERFLOW.name,
            (result as AynvoraResult.Failure.CalculationFailure).code
        )
    }

    @Test
    fun testCancellationStopsNativeGeneration(): Unit = runBlocking {
        val bridge = TestNativeBridge()
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = LlamaNativeRuntimeDriver(bridge),
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )
        engine.load(testVariant, "/data/models/qwen.gguf")

        engine.cancel("req_cancel_test")

        val req = AiGenerationRequest(
            requestId = "req_cancel_test",
            systemPrompt = "System",
            userPrompt = "Contemplate",
        )

        val result = engine.generate(req)
        assertTrue(result is AynvoraResult.Failure.CalculationFailure)
        assertEquals(
            AiRuntimeErrorCode.INFERENCE_CANCELLED.name,
            (result as AynvoraResult.Failure.CalculationFailure).code
        )
    }

    @Test
    fun testTarotEvidenceAuthorityPreservedUnderRealNativeAi(): Unit = runBlocking {
        val hermitReflection =
            "The Hermit in context of Self-Reflection represents a deliberate pause for introspection."
        val bridge = TestNativeBridge(generatedText = hermitReflection)
        val engine = LocalNativeInferenceEngine(
            nativeRuntime = LlamaNativeRuntimeDriver(bridge),
            availableRamProvider = { 8L * 1024L * 1024L * 1024L },
        )
        engine.load(testVariant, "/data/models/qwen2.5-0.5b-instruct-q4_k_m.gguf")

        val card = TarotCard(
            id = "major_09_hermit",
            number = 9,
            name = "The Hermit",
            arcana = TarotArcana.MAJOR
        )
        val pos = TarotSpreadPosition(
            id = "single_pos",
            orderIndex = 0,
            name = "Insight",
            description = "Core insight"
        )
        val draw =
            TarotCardDraw(card = card, orientation = TarotCardOrientation.UPRIGHT, position = pos)
        val content = TarotCardContent(
            cardId = card.id,
            language = "en",
            title = "The Hermit",
            shortDescription = "Inner contemplation and wisdom.",
            keywords = listOf("introspection", "guidance"),
            uprightMeaning = "Inner contemplation and wisdom.",
            reversedMeaning = "Isolation and withdrawal.",
            contentVersion = 1,
        )

        val slmEngine = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = engine,
            deterministicEngine = DeterministicTarotExplanationEngine(),
        )

        val req = TarotExplanationRequest(
            requestId = "req_tarot_real",
            draw = draw,
            deterministicContent = content,
            spreadPositionContext = "Context",
            language = "en",
            allowSlmInference = true,
        )

        val explainResult = slmEngine.explain(req)
        assertTrue(explainResult is AynvoraResult.Success)
        val result = (explainResult as AynvoraResult.Success).value

        // AI explanation produced without fallback
        assertFalse(result.fallbackUsed)
        assertNull(result.provenance.fallbackReason)
        assertEquals(hermitReflection, result.explanationText)

        // Authority invariants: card ID, language, model ID strictly preserved
        assertEquals("major_09_hermit", result.cardId)
        assertEquals("en", result.language)
        assertEquals(testVariant.modelId, result.provenance.modelId)
    }
}
