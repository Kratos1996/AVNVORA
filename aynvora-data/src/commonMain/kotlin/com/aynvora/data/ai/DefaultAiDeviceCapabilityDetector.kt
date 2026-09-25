package com.aynvora.data.ai

import com.aynvora.core.ai.AiAcceleratorType
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiRuntimeType
import com.aynvora.core.ai.CpuArchitecture
import com.aynvora.core.ai.OsPlatform

/**
 * Default production device capability detector.
 *
 * Provides calibrated device profile defaults for host execution,
 * safely sizing RAM and storage limits for on-device SLM execution.
 */
class DefaultAiDeviceCapabilityDetector(
    private val totalRamBytes: Long = 4L * 1024L * 1024L * 1024L, // 4 GB default
    private val availableRamBytes: Long = 2500L * 1024L * 1024L, // 2.5 GB available
    private val freeStorageBytes: Long = 16L * 1024L * 1024L * 1024L, // 16 GB free storage
    private val cpuArchitecture: CpuArchitecture = CpuArchitecture.ARM64,
    private val osPlatform: OsPlatform = OsPlatform.ANDROID,
) : AiDeviceCapabilityDetector {

    override suspend fun detectCapability(): AiDeviceProfile {
        return AiDeviceProfile(
            totalRamBytes = totalRamBytes,
            availableRamBytes = availableRamBytes,
            freeStorageBytes = freeStorageBytes,
            cpuArchitecture = cpuArchitecture,
            osPlatform = osPlatform,
            osVersion = "Default",
            supportedRuntimes = setOf(AiRuntimeType.GGUF, AiRuntimeType.DETERMINISTIC_FALLBACK),
            supportedAccelerators = setOf(AiAcceleratorType.CPU, AiAcceleratorType.GPU),
            supportedLanguages = setOf("en", "hi"),
        )
    }
}
