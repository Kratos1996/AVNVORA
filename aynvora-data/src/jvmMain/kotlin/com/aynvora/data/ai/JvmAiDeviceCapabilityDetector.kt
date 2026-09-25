package com.aynvora.data.ai

import com.aynvora.core.ai.AiAcceleratorType
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiRuntimeType
import com.aynvora.core.ai.CpuArchitecture
import com.aynvora.core.ai.OsPlatform
import java.io.File

/**
 * JVM host implementation of [AiDeviceCapabilityDetector].
 * Inspects live runtime memory, disk space, and processor architecture.
 */
class JvmAiDeviceCapabilityDetector : AiDeviceCapabilityDetector {

    override suspend fun detectCapability(): AiDeviceProfile {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()

        // Available memory is allocatable space up to JVM max limit
        val availableRam = (maxMemory - totalMemory) + freeMemory

        val rootDir = File(".")
        val usableStorage = rootDir.usableSpace

        val osArch = System.getProperty("os.arch")?.lowercase().orEmpty()
        val cpuArchitecture = when {
            osArch.contains("aarch64") || osArch.contains("arm64") -> CpuArchitecture.ARM64
            osArch.contains("arm") -> CpuArchitecture.ARM32
            osArch.contains("amd64") || osArch.contains("x86_64") -> CpuArchitecture.X86_64
            osArch.contains("x86") -> CpuArchitecture.X86
            else -> CpuArchitecture.UNKNOWN
        }

        return AiDeviceProfile(
            totalRamBytes = maxMemory,
            availableRamBytes = availableRam,
            freeStorageBytes = usableStorage,
            cpuArchitecture = cpuArchitecture,
            osPlatform = OsPlatform.JVM_DESKTOP,
            osVersion = System.getProperty("os.name")
                .orEmpty() + " " + System.getProperty("os.version").orEmpty(),
            supportedRuntimes = setOf(AiRuntimeType.GGUF, AiRuntimeType.DETERMINISTIC_FALLBACK),
            supportedAccelerators = setOf(AiAcceleratorType.CPU),
            supportedLanguages = setOf("en", "hi"),
        )
    }
}
