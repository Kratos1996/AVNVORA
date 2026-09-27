package com.aynvora.core.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiModelSelectorTest {

    private val selector = AiModelSelector()

    @Test
    fun highEndDeviceSelectsLargestParameterAndQuantization() {
        // High-end device: 8GB total RAM, 4GB available RAM, 64GB storage, ARM64
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 4L * 1024L * 1024L * 1024L,
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
        assertTrue(result.userSummary.isActionable)
        assertEquals("Download AYNVORA AI", result.userSummary.actionLabel)
        assertEquals(
            "${result.selectedModel?.displaySizeMb} MB",
            result.userSummary.downloadSizeFormatted
        )
    }

    @Test
    fun midRangeDeviceSelectsOptimal0_5BVariant() {
        // Mid-range device: 3GB total RAM, 1.2GB available RAM, 10GB storage
        val profile = AiDeviceProfile(
            totalRamBytes = 3L * 1024L * 1024L * 1024L,
            availableRamBytes = 1200L * 1024L * 1024L,
            freeStorageBytes = 10L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "13",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        // Safe RAM allocation: minOf(3GB * 0.75, 1200MB - 350MB) = 850MB -> fits Q8_0 or Q5_K_M
        assertTrue(result.selectedModel?.parameterCountNumber == 0.5)
    }

    @Test
    fun budgetDeviceSelectsQ4_K_M() {
        // Budget device: 2GB total RAM, 1050MB available RAM (525MB safe allocation), 2GB free storage
        val profile = AiDeviceProfile(
            totalRamBytes = 2L * 1024L * 1024L * 1024L,
            availableRamBytes = 1050L * 1024L * 1024L,
            freeStorageBytes = 2L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "12",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        assertEquals("qwen2.5-0.5b-instruct-q4_k_m", result.selectedModel?.modelId)
    }

    @Test
    fun deviceWithInsufficientStorageIsRejectedGracefully() {
        // Storage is only 500MB (less than any model buffer)
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 4L * 1024L * 1024L * 1024L,
            freeStorageBytes = 500L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "14",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.INSUFFICIENT_STORAGE, result.status)
        assertNull(result.selectedModel)
        assertFalse(result.userSummary.isActionable)
        assertTrue(result.userSummary.title.contains("Storage"))
    }

    @Test
    fun deviceWithInsufficientRamIsRejectedGracefully() {
        // RAM available is only 200MB (safe allocation is 0)
        val profile = AiDeviceProfile(
            totalRamBytes = 1L * 1024L * 1024L * 1024L,
            availableRamBytes = 200L * 1024L * 1024L,
            freeStorageBytes = 16L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "10",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.INSUFFICIENT_MEMORY, result.status)
        assertNull(result.selectedModel)
        assertFalse(result.userSummary.isActionable)
    }

    @Test
    fun alreadyInstalledModelIsRecognized() {
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 4L * 1024L * 1024L * 1024L,
            freeStorageBytes = 64L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "14",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(
            profile = profile,
            installedModelId = "qwen2.5-1.5b-instruct-q5_k_m",
        )
        assertEquals(AiModelSelectionStatus.ALREADY_INSTALLED, result.status)
        assertFalse(result.userSummary.isActionable)
        assertEquals("Installed", result.userSummary.actionLabel)
    }

    @Test
    fun unsupportedRuntimeIsRejectedGracefully() {
        val profile = AiDeviceProfile(
            totalRamBytes = 8L * 1024L * 1024L * 1024L,
            availableRamBytes = 4L * 1024L * 1024L * 1024L,
            freeStorageBytes = 64L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.X86_64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "14",
            supportedRuntimes = emptySet(), // No supported runtimes
        )

        val result = selector.selectOptimalModel(profile)
        assertEquals(AiModelSelectionStatus.UNSUPPORTED_PLATFORM, result.status)
        assertNull(result.selectedModel)
        assertFalse(result.userSummary.isActionable)
    }

    @Test
    fun selectionIsDeterministicForEquivalentProfiles() {
        val profile1 = AiDeviceProfile(
            totalRamBytes = 4L * 1024L * 1024L * 1024L,
            availableRamBytes = 2L * 1024L * 1024L * 1024L,
            freeStorageBytes = 32L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "14",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )
        val profile2 = profile1.copy()

        val result1 = selector.selectOptimalModel(profile1)
        val result2 = selector.selectOptimalModel(profile2)

        assertEquals(result1.status, result2.status)
        assertEquals(result1.selectedModel?.modelId, result2.selectedModel?.modelId)
    }

    @Test
    fun connectedPhysicalDeviceProfile_SamsungS23Ultra_SelectsOptimalModel() {
        // Factual physical device values read via ADB from connected SM-S918B:
        // MemTotal: 11,309,736 kB (~11.3 GB), MemAvailable: 3,602,812 kB (~3.6 GB), freeStorage: 31 GB
        val physicalDeviceProfile = AiDeviceProfile(
            totalRamBytes = 11_309_736L * 1024L,
            availableRamBytes = 3_602_812L * 1024L,
            freeStorageBytes = 31L * 1024L * 1024L * 1024L,
            cpuArchitecture = CpuArchitecture.ARM64,
            osPlatform = OsPlatform.ANDROID,
            osVersion = "16",
            supportedRuntimes = setOf(AiRuntimeType.GGUF),
        )

        val result = selector.selectOptimalModel(physicalDeviceProfile)
        assertEquals(AiModelSelectionStatus.READY_TO_DOWNLOAD, result.status)
        assertNotNull(result.selectedModel)
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", result.selectedModel?.modelId)
        assertTrue(result.userSummary.isActionable)
        assertEquals("Download AYNVORA AI", result.userSummary.actionLabel)
        assertEquals("qwen2.5-1.5b-instruct-q5_k_m", result.diagnostics.selectedModelId)
    }
}
