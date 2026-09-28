package com.aynvora.core.ai

import kotlinx.serialization.Serializable

/**
 * CPU Architectures recognized by AYNVORA AI device profiling.
 */
@Serializable
enum class CpuArchitecture {
    ARM64,
    ARM32,
    X86_64,
    X86,
    UNKNOWN;

    val is64Bit: Boolean
        get() = this == ARM64 || this == X86_64
}

/**
 * Host operating systems / runtime platforms.
 */
@Serializable
enum class OsPlatform {
    ANDROID,
    JVM_DESKTOP,
    IOS,
    UNKNOWN,
}

/**
 * Execution backends for on-device inference.
 */
@Serializable
enum class AiRuntimeType {
    GGUF,
    ONNX,
    LITE_RT,
    DETERMINISTIC_FALLBACK,
}

/**
 * Hardware acceleration units available on host device.
 */
@Serializable
enum class AiAcceleratorType {
    CPU,
    GPU,
    NPU,
    NEURAL_ENGINE,
}

/**
 * Complete hardware and platform capability profile captured automatically on-device.
 *
 * @param totalRamBytes Total physical memory installed.
 * @param availableRamBytes Unused / reclaimable memory available to the app process.
 * @param freeStorageBytes Available disk space on the application's internal data storage.
 * @param cpuArchitecture Host processor architecture.
 * @param osPlatform Operating system.
 * @param osVersion Operating system version or API level.
 * @param supportedRuntimes Inference engines supported on this platform/ABI.
 * @param supportedAccelerators Accelerators detectable on this platform.
 * @param supportedLanguages Locale/language capabilities supported by host device.
 * @param maxSafeRamAllocationBytes Safe RAM threshold beyond which SLM loading risks OOM.
 */
@Serializable
data class AiDeviceProfile(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val freeStorageBytes: Long,
    val cpuArchitecture: CpuArchitecture,
    val osPlatform: OsPlatform,
    val osVersion: String = "",
    val supportedRuntimes: Set<AiRuntimeType> = setOf(
        AiRuntimeType.GGUF,
        AiRuntimeType.DETERMINISTIC_FALLBACK
    ),
    val supportedAccelerators: Set<AiAcceleratorType> = setOf(AiAcceleratorType.CPU),
    val supportedLanguages: Set<String> = setOf("en", "hi"),
    val maxSafeRamAllocationBytes: Long = calculateSafeRamAllocation(availableRamBytes),
    val manufacturer: String = "samsung",
    val modelName: String = "SM-S918B",
    val cpuAbi: String = "arm64-v8a",
    val sdkInt: Int = 36,
    val totalStorageBytes: Long = 238_370_684_928L, // 222 GB default
) {
    val is64BitSupported: Boolean
        get() = cpuArchitecture.is64Bit

    companion object {
        private const val BASELINE_OS_SAFETY_RESERVE_BYTES =
            350L * 1024L * 1024L // 350 MB OS safety reserve

        fun calculateSafeRamAllocation(availableRamBytes: Long): Long {
            val netAvailable = availableRamBytes - BASELINE_OS_SAFETY_RESERVE_BYTES
            return if (netAvailable > 0) (netAvailable * 0.75).toLong() else 0L
        }
    }
}

/**
 * Domain interface to automatically detect host device capabilities without user intervention.
 */
interface AiDeviceCapabilityDetector {
    suspend fun detectCapability(): AiDeviceProfile
}

/**
 * Testable, configurable mock implementation for deterministic unit testing.
 */
class MockAiDeviceCapabilityDetector(
    private val profile: AiDeviceProfile,
) : AiDeviceCapabilityDetector {
    override suspend fun detectCapability(): AiDeviceProfile = profile
}
