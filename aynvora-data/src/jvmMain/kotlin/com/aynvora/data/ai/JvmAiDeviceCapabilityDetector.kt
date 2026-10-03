package com.aynvora.data.ai

import com.aynvora.core.ai.AiAcceleratorType
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiRuntimeType
import com.aynvora.core.ai.CpuArchitecture
import com.aynvora.core.ai.OsPlatform
import java.io.File
import java.lang.management.ManagementFactory

/**
 * Live JVM host implementation of [AiDeviceCapabilityDetector].
 * Inspects live runtime memory, physical host disk space, and processor architecture
 * for Mac OS X, Windows, and Linux desktop hosts.
 */
class JvmAiDeviceCapabilityDetector : AiDeviceCapabilityDetector {

    override suspend fun detectCapability(): AiDeviceProfile {
        val osName = System.getProperty("os.name") ?: "Desktop JVM"
        val osVersion = System.getProperty("os.version") ?: ""
        val osArch = System.getProperty("os.arch")?.lowercase() ?: "x86_64"

        val isMac = osName.contains("Mac", ignoreCase = true)
        val isWindows = osName.contains("Windows", ignoreCase = true)
        val isLinux = osName.contains("Linux", ignoreCase = true)

        val manufacturer = when {
            isMac -> "Apple"
            isWindows -> "Microsoft / PC"
            isLinux -> "Linux Workstation"
            else -> osName
        }

        val modelName = when {
            isMac -> if (osArch.contains("aarch64") || osArch.contains("arm64")) "Mac (Apple Silicon $osArch)" else "Mac (Intel $osArch)"
            isWindows -> "PC Workstation ($osArch)"
            isLinux -> "Linux PC ($osArch)"
            else -> "$osName ($osArch)"
        }

        val cpuArchitecture = when {
            osArch.contains("aarch64") || osArch.contains("arm64") -> CpuArchitecture.ARM64
            osArch.contains("arm") -> CpuArchitecture.ARM32
            osArch.contains("amd64") || osArch.contains("x86_64") -> CpuArchitecture.X86_64
            osArch.contains("x86") -> CpuArchitecture.X86
            else -> CpuArchitecture.UNKNOWN
        }

        val rootDir = File(".")
        val freeStorage = rootDir.usableSpace
        val totalStorage = rootDir.totalSpace

        // Query physical system memory using ManagementFactory
        var totalRam = Runtime.getRuntime().maxMemory()
        var availableRam = Runtime.getRuntime().freeMemory()

        try {
            val osBean = ManagementFactory.getOperatingSystemMXBean()
            val totalMemMethod = osBean.javaClass.methods.firstOrNull { it.name == "getTotalMemorySize" || it.name == "getTotalPhysicalMemorySize" }
            val freeMemMethod = osBean.javaClass.methods.firstOrNull { it.name == "getFreeMemorySize" || it.name == "getFreePhysicalMemorySize" }

            if (totalMemMethod != null && freeMemMethod != null) {
                val sysTotal = totalMemMethod.invoke(osBean) as? Long
                val sysFree = freeMemMethod.invoke(osBean) as? Long
                if (sysTotal != null && sysTotal > 0) totalRam = sysTotal
                if (sysFree != null && sysFree > 0) availableRam = sysFree
            }
        } catch (_: Throwable) {
            // Fallback to JVM process memory
        }

        val accelerators = mutableSetOf(AiAcceleratorType.CPU)
        if (isMac && (osArch.contains("aarch64") || osArch.contains("arm64"))) {
            accelerators.add(AiAcceleratorType.GPU)
            accelerators.add(AiAcceleratorType.NEURAL_ENGINE)
        }

        return AiDeviceProfile(
            totalRamBytes = totalRam,
            availableRamBytes = availableRam,
            freeStorageBytes = freeStorage,
            totalStorageBytes = totalStorage,
            cpuArchitecture = cpuArchitecture,
            osPlatform = OsPlatform.JVM_DESKTOP,
            osVersion = "$osName $osVersion",
            manufacturer = manufacturer,
            modelName = modelName,
            cpuAbi = osArch,
            sdkInt = 0,
            supportedRuntimes = setOf(AiRuntimeType.GGUF, AiRuntimeType.DETERMINISTIC_FALLBACK),
            supportedAccelerators = accelerators,
            supportedLanguages = setOf("en", "hi", "ar"),
        )
    }
}
