package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 8.7 Local Model Runtime Hardening Tests.
 *
 * Verifies memory safety before model load, context bounds, cold vs warm load,
 * unload, cancellation, timeout, and language verification.
 */
class AiModelRuntimeHardeningTest {

    private val testVariant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M

    @Test
    fun loadFailsWithInsufficientRuntimeMemory_whenAvailableRamIsLow() = runBlocking {
        // Available RAM is only 600MB: net available = 250MB, safe allocation = 187MB.
        // testVariant requires 500MB safe RAM.
        val lowRamEngine = LocalAiInferenceEngine(
            availableRamProvider = { 600L * 1024L * 1024L }
        )

        val loadResult = lowRamEngine.load(testVariant, "/models/qwen05b.gguf")
        assertTrue(loadResult is AynvoraResult.Failure.CalculationFailure)
        val failure = loadResult as AynvoraResult.Failure.CalculationFailure
        assertEquals(AiRuntimeErrorCode.INSUFFICIENT_RUNTIME_MEMORY.name, failure.code)
        assertEquals(AiInferenceStatus.ERROR, lowRamEngine.getStatus())
        assertEquals(
            AiRuntimeErrorCode.INSUFFICIENT_RUNTIME_MEMORY,
            lowRamEngine.getDiagnostics().lastErrorCode
        )
    }

    @Test
    fun loadSucceedsAndTracksColdVsWarmLoad() = runBlocking {
        val engine = LocalAiInferenceEngine(
            availableRamProvider = { 4L * 1024L * 1024L * 1024L }
        )

        // 1. Cold Load
        val coldResult = engine.load(testVariant, "/models/qwen05b.gguf")
        assertTrue(coldResult is AynvoraResult.Success)
        assertEquals(AiInferenceStatus.READY, engine.getStatus())
        val coldDiagnostics = engine.getDiagnostics()
        assertTrue(coldDiagnostics.coldLoadDurationMs > 0L)
        assertEquals(0L, coldDiagnostics.warmLoadDurationMs)

        // 2. Warm Load (same variant and path)
        val warmResult = engine.load(testVariant, "/models/qwen05b.gguf")
        assertTrue(warmResult is AynvoraResult.Success)
        val warmDiagnostics = engine.getDiagnostics()
        assertTrue(warmDiagnostics.warmLoadDurationMs > 0L)
    }

    @Test
    fun unloadReleasesModelAndResetsState() = runBlocking {
        val engine = LocalAiInferenceEngine()
        engine.load(testVariant, "/models/qwen05b.gguf")
        assertEquals(AiInferenceStatus.READY, engine.getStatus())
        assertNotNull(engine.getLoadedModel())

        val unloadResult = engine.unload()
        assertTrue(unloadResult is AynvoraResult.Success)
        assertEquals(AiInferenceStatus.UNLOADED, engine.getStatus())
        assertNull(engine.getLoadedModel())

        // Generating after unload should fail with INFERENCE_NOT_READY
        val genResult = engine.generate(
            AiGenerationRequest(
                requestId = "req_1",
                systemPrompt = "sys",
                userPrompt = "user",
            )
        )
        assertTrue(genResult is AynvoraResult.Failure.CalculationFailure)
        val failure = genResult as AynvoraResult.Failure.CalculationFailure
        assertEquals(AiRuntimeErrorCode.INFERENCE_NOT_READY.name, failure.code)
    }

    @Test
    fun contextOverflowRejectsPromptExceedingLimit() = runBlocking {
        val engine = LocalAiInferenceEngine()
        engine.load(testVariant, "/models/qwen05b.gguf")

        // Construct massive prompt exceeding default context window (2048 tokens ≈ 8192 chars)
        val giantPrompt = "word ".repeat(3000)
        val req = AiGenerationRequest(
            requestId = "overflow_req",
            systemPrompt = "System context",
            userPrompt = giantPrompt,
            maxTokens = 256,
        )

        val result = engine.generate(req)
        assertTrue(result is AynvoraResult.Failure.CalculationFailure)
        val failure = result as AynvoraResult.Failure.CalculationFailure
        assertEquals(AiRuntimeErrorCode.CONTEXT_OVERFLOW.name, failure.code)
    }

    @Test
    fun englishAndHindiGenerationWorkSeamlessly() = runBlocking {
        val engine = LocalAiInferenceEngine()
        engine.load(testVariant, "/models/qwen05b.gguf")

        // English request
        val enResult = engine.generate(
            AiGenerationRequest(
                requestId = "en_req",
                systemPrompt = "You are an AI assistant.",
                userPrompt = "Explain The Hermit card.",
                language = "en",
            )
        )
        assertTrue(enResult is AynvoraResult.Success)
        val enText = (enResult as AynvoraResult.Success).value.text
        assertTrue(enText.contains("Reflecting"))
        assertTrue(enText.contains(testVariant.name))

        // Hindi request
        val hiResult = engine.generate(
            AiGenerationRequest(
                requestId = "hi_req",
                systemPrompt = "आप एक एआई सहायक हैं।",
                userPrompt = "द हर्मिट कार्ड की व्याख्या करें।",
                language = "hi",
            )
        )
        assertTrue(hiResult is AynvoraResult.Success)
        val hiText = (hiResult as AynvoraResult.Success).value.text
        assertTrue(hiText.contains("प्रस्तुत साक्ष्य और ज्ञान के आधार पर"))
    }

    @Test
    fun unsupportedLanguageReturnsTypedFailure() = runBlocking {
        val engine = LocalAiInferenceEngine()
        engine.load(testVariant, "/models/qwen05b.gguf")

        val frResult = engine.generate(
            AiGenerationRequest(
                requestId = "fr_req",
                systemPrompt = "Bonjour",
                userPrompt = "Expliquez l'Ermite",
                language = "fr", // French is not in testVariant.supportedLanguages (en, hi)
            )
        )
        assertTrue(frResult is AynvoraResult.Failure.CalculationFailure)
        val failure = frResult as AynvoraResult.Failure.CalculationFailure
        assertEquals(AiRuntimeErrorCode.UNSUPPORTED_LANGUAGE.name, failure.code)
    }

    @Test
    fun cancellationTerminatesGenerationCleanly() = runBlocking {
        val engine = LocalAiInferenceEngine()
        engine.load(testVariant, "/models/qwen05b.gguf")

        // Pre-cancel request ID
        engine.cancel("cancel_req_1")

        val result = engine.generate(
            AiGenerationRequest(
                requestId = "cancel_req_1",
                systemPrompt = "System",
                userPrompt = "User question",
                maxTokens = 256,
            )
        )

        // Cancelled request returns finishReason CANCELLED without throwing unhandled exception
        assertTrue(result is AynvoraResult.Success)
        val response = (result as AynvoraResult.Success).value
        assertEquals("CANCELLED", response.finishReason)
        assertEquals(AiInferenceStatus.READY, engine.getStatus())
    }
}
