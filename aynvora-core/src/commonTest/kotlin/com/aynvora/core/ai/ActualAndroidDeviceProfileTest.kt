package com.aynvora.core.ai

import com.aynvora.core.ai.knowledge.GitaKnowledgePack
import com.aynvora.core.ai.knowledge.VedicAstrologyKnowledgePack
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Authoritative regression tests for Phase 10.11:
 * - Real hardware profile metrics (Samsung Galaxy S23 Ultra).
 * - Deterministic model selection (Qwen2.5 1.5B Q5_K_M).
 * - Truthful execution mode: NEVER display LOCAL_NATIVE without real native tokens.
 * - JNI linkage diagnostic reporting.
 * - Graceful deterministic fallback with zero fake output.
 */
class ActualAndroidDeviceProfileTest {

    private val selector = AiModelSelector()

    @Test
    fun testActualAndroidDeviceProfile_ValuesMatchHardware() {
        val profile = ActualAndroidDeviceProfile.PROFILE

        assertEquals("samsung", profile.manufacturer)
        assertEquals("SM-S918B", profile.modelName)
        assertEquals("arm64-v8a", profile.cpuAbi)
        assertEquals(36, profile.sdkInt)
        assertEquals(CpuArchitecture.ARM64, profile.cpuArchitecture)
        assertTrue(profile.is64BitSupported)
        assertEquals(OsPlatform.ANDROID, profile.osPlatform)

        // 11,309,736 kB total memory
        assertEquals(11_581_169_664L, profile.totalRamBytes)
        // 4,534,608 kB available memory
        assertEquals(4_643_438_592L, profile.availableRamBytes)
        // 85 GB free internal storage
        assertEquals(91_268_055_040L, profile.freeStorageBytes)
        // 222 GB total storage
        assertEquals(238_370_684_928L, profile.totalStorageBytes)

        // Safe RAM allocation: (4,643,438,592 - 350*1024*1024) * 0.75 = ~3,058 MB
        val safeMb = profile.maxSafeRamAllocationBytes / (1024L * 1024L)
        assertTrue(safeMb >= 3000L, "Safe RAM allocation should be >= 3000 MB; was $safeMb MB")
    }

    @Test
    fun testModelSelector_SelectsQwen1_5B_Q5_ForActualProfile() {
        val profile = ActualAndroidDeviceProfile.PROFILE
        val result = selector.selectOptimalModel(profile)

        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)

        // Exact selected model variant for the 12GB Galaxy S23 Ultra hardware:
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", result.selectedModel?.modelId)
        assertEquals("Qwen2.5 1.5B Instruct (Q5_K_M)", result.selectedModel?.name)
        assertEquals(AiQuantization.Q5_K_M, result.selectedModel?.quantization)
        assertEquals("1.5B", result.selectedModel?.parameterCount)
        assertEquals(1225, result.selectedModel?.displaySizeMb)
        assertEquals("b46661073c18e5b56a41fa320975f866a00def1ff08feef4718e013258896f8c", result.selectedModel?.sha256Checksum)
    }

    @Test
    fun testExecutionModeTruthfulness_UnlinkedNativeLibraryDefaultsToFallback() {
        // When native library is not linked, executionMode MUST be DETERMINISTIC_FALLBACK
        val runtime = DefaultLocalAiRuntime()

        assertFalse(runtime.isNativeVerified())
        assertEquals(AiExecutionMode.DETERMINISTIC_FALLBACK, runtime.executionMode)
        assertTrue(runtime.getNativeLibraryStatus().contains("NOT_VERIFIED"))
        assertTrue(runtime.getJniStatus().contains("UNLINKED") || runtime.getJniStatus().contains("NOT_VERIFIED"))
    }

    @Test
    fun testExecutionModeTruthfulness_MockVerifiedNativeLibraryBecomesLocalNativeOnlyWhenLoaded() {
        val mockBridge = object : NativeLibraryBridge {
            override fun isAvailable(): Boolean = true
            override fun load(modelPath: String, contextLength: Int, threads: Int): Long = 42L
            override fun generate(
                handle: Long,
                prompt: String,
                maxTokens: Int,
                temperature: Float,
                onTokenGenerated: (String) -> Boolean,
            ): String = "Verified native token output"
            override fun cancel(handle: Long) {}
            override fun release(handle: Long) {}
        }

        val driver = LlamaNativeRuntimeDriver(bridge = mockBridge)
        assertTrue(driver.isAvailable())

        val engine = LocalNativeInferenceEngine(nativeRuntime = driver)
        val runtime = DefaultLocalAiRuntime(
            inferenceEngine = engine,
            nativeRuntimeDriver = driver,
        )

        assertTrue(runtime.isNativeVerified())
        // Even if library is verified, before model load, execution mode must NOT be LOCAL_NATIVE:
        assertEquals(AiExecutionMode.DETERMINISTIC_FALLBACK, runtime.executionMode)

        // Now load a model
        runBlocking {
            val loadResult = runtime.loadModel(AiModelCatalog.QWEN_2_5_1_5B_Q5_K_M, "/path/to/model.gguf")
            assertTrue(loadResult is AynvoraResult.Success)
        }

        // In Phase 10.12, after load but before token generation, executionMode remains DETERMINISTIC_FALLBACK
        assertEquals(AiExecutionMode.DETERMINISTIC_FALLBACK, runtime.executionMode)

        // Only after actual native token generation succeeds does executionMode become LOCAL_NATIVE:
        runBlocking {
            val genResult = runtime.generate(
                AiGenerationRequest(
                    requestId = "req_test",
                    systemPrompt = "You are a contemplative assistant.",
                    userPrompt = "Reflect on duty.",
                )
            )
            assertTrue(genResult is AynvoraResult.Success)
            assertEquals(AiExecutionMode.LOCAL_NATIVE, (genResult as AynvoraResult.Success).value.executionMode)
        }
        assertEquals(AiExecutionMode.LOCAL_NATIVE, runtime.executionMode)
    }

    @Test
    fun testAynvoraLocalIntelligence_FallbackAlwaysUsedWhenModelNotReady() = runBlocking {
        val intelligence = DefaultAynvoraLocalIntelligence(
            runtime = DefaultLocalAiRuntime(),
            initialKnowledgePacks = listOf(
                VedicAstrologyKnowledgePack(),
                GitaKnowledgePack(),
            ),
        )

        // Querying intelligence without loaded native model must return deterministic fallback
        val request = AynvoraAiRequest(
            requestId = "test_req_01",
            featureId = CoreFeatureId.GITA,
            knowledgePackId = "kp_gita_canonical_v1",
            userContext = AynvoraUserContext(question = "How do I deal with doubt in duty?"),
            question = "How do I deal with doubt in duty?",
        )

        val result = intelligence.synthesize(request)
        assertTrue(result is AynvoraResult.Success)

        val response = (result as AynvoraResult.Success).value
        assertTrue(response.fallbackUsed)
        assertEquals(AiExecutionMode.DETERMINISTIC_FALLBACK, response.executionMode)
        assertEquals(AynvoraValidationStatus.FALLBACK_APPLIED, response.validationStatus)
        assertTrue(response.responseText.isNotBlank())
    }
}
