package com.aynvora.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 8.7 Device-Adaptive Model Selection & Profiling Tests.
 *
 * Verifies automatic, zero-configuration model and quantization selection across
 * deterministic synthetic device profiles: VERY_LOW_RESOURCE, LOW_RESOURCE,
 * BALANCED, HIGH_RESOURCE, and UNSUPPORTED.
 */
class AiDeviceProfilingAdaptiveTest {

    private val selector = AiModelSelector()

    @Test
    fun veryLowResourceDevice_rejectsWithInsufficientResourcesSafely() {
        // VERY_LOW_RESOURCE: 512MB RAM, 250MB available RAM, 400MB storage
        val profile = AiDeviceProfile(
            totalRamBytes = 512L * 1024L * 1024L,
            availableRamBytes = 250L * 1024L * 1024L,
            freeStorageBytes = 400L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "9",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        // With 400MB storage, cannot satisfy min 1080MB storage buffer
        assertTrue(
            result.status == AiModelSelectionStatus.INSUFFICIENT_STORAGE ||
                    result.status == AiModelSelectionStatus.INSUFFICIENT_MEMORY
        )
        assertNull(result.selectedModel)
        assertFalse(result.userSummary.isActionable)
        assertEquals("Unavailable", result.userSummary.downloadSizeFormatted)
    }

    @Test
    fun lowResourceDevice_automaticallySelects0_5B_Q4_K_M() {
        // LOW_RESOURCE: 2GB RAM, 1050MB available RAM (safe RAM: 525MB), 2.5GB storage
        val profile = AiDeviceProfile(
            totalRamBytes = 2L * 1024L * 1024L * 1024L,
            availableRamBytes = 1050L * 1024L * 1024L,
            freeStorageBytes = 2500L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "11",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        assertEquals("qwen2.5-0.5b-instruct-q4_k_m", result.selectedModel?.modelId)
        assertEquals(AiQuantization.Q4_K_M, result.selectedModel?.quantization)
        assertTrue(result.userSummary.isActionable)
        assertEquals("Download AYNVORA AI", result.userSummary.actionLabel)
    }

    @Test
    fun balancedResourceDevice_automaticallySelectsOptimal0_5B_HighQuantOr1_5B() {
        // BALANCED: 4GB RAM, 2.2GB available RAM (safe RAM: 1387MB), 16GB storage
        val profile = AiDeviceProfile(
            totalRamBytes = 4L * 1024L * 1024L * 1024L,
            availableRamBytes = 2200L * 1024L * 1024L,
            freeStorageBytes = 16L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "13",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        // With 1387MB safe RAM and 16GB storage, can comfortably run 0.5B Q8_0 or 1.5B Q4_K_M
        assertTrue(
            result.selectedModel?.modelId == "qwen2.5-0.5b-instruct-q8_0" ||
                    result.selectedModel?.modelId == "qwen2.5-1.5b-instruct-q4_k_m"
        )
    }

    @Test
    fun highResourceDevice_automaticallySelects1_5B_Q5_K_M() {
        // HIGH_RESOURCE: 8GB RAM, 5GB available RAM (safe RAM: 3487MB), 64GB storage
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 5000L * 1024L * 1024L,
            freeStorageBytes = 64L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "14",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", result.selectedModel?.modelId)
        assertEquals(AiQuantization.Q5_K_M, result.selectedModel?.quantization)
    }

    @Test
    fun unsupportedPlatform_rejectsWithUnsupportedPlatformStatus() {
        // UNSUPPORTED: Runtime does not include GGUF
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 5000L * 1024L * 1024L,
            freeStorageBytes = 64L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.UNKNOWN,
            supportedRuntimes = setOf(AiRuntimeType.DETERMINISTIC_FALLBACK),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.UNSUPPORTED_PLATFORM, result.status)
        assertNull(result.selectedModel)
        assertFalse(result.userSummary.isActionable)
        assertTrue(result.userSummary.title.contains("Platform"))
    }

    @Test
    fun userExperience_zeroConfigurationGuarantee() {
        // Verify user summary contains zero technical jargon (no tensor formats or raw quant strings)
        val profile = AiDeviceProfile(
            totalRamBytes = 6L * 1024L * 1024L * 1024L,
            availableRamBytes = 3500L * 1024L * 1024L,
            freeStorageBytes = 32L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        val summary = result.userSummary
        assertFalse(summary.title.contains("Q4_K_M"))
        assertFalse(summary.title.contains("GGUF"))
        assertFalse(summary.description.contains("quantization"))
        assertEquals("Download AYNVORA AI", summary.actionLabel)
    }
}
