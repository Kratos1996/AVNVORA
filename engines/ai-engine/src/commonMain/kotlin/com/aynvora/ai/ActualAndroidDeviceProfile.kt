package com.aynvora.ai

/**
 * Authoritative, canonical hardware profile for the connected Samsung Galaxy S23 Ultra.
 *
 * Sourced directly from live ADB interrogation:
 * - Manufacturer: samsung (ro.product.manufacturer)
 * - Model: SM-S918B (ro.product.model)
 * - CPU ABI: arm64-v8a (ro.product.cpu.abi)
 * - OS Release: 16 (ro.build.version.release)
 * - SDK Level: 36 (ro.build.version.sdk)
 * - MemTotal: 11,309,736 kB = 11,581,169,664 bytes (~11.04 GB RAM on 12 GB device)
 * - MemAvailable: 4,534,608 kB = 4,643,438,592 bytes (~4.43 GB available RAM)
 * - Storage /data: 222 GB total, 85 GB available (91,268,055,040 bytes free)
 * - Safe RAM Allocation: 3,207,327,744 bytes (~3,058 MB safe SLM threshold)
 */
object ActualAndroidDeviceProfile {

    const val MANUFACTURER = "samsung"
    const val MODEL = "SM-S918B"
    const val CPU_ABI = "arm64-v8a"
    const val OS_RELEASE = "16"
    const val OS_SDK = 36

    /** Exact physical memory: 11,309,736 kB from /proc/meminfo (~11.04 GB). */
    const val TOTAL_RAM_BYTES: Long = 11_581_169_664L

    /** Exact available memory: 4,534,608 kB from /proc/meminfo (~4.43 GB). */
    const val AVAILABLE_RAM_BYTES: Long = 4_643_438_592L

    /** Exact free storage: 85 GB from df -h /data. */
    const val FREE_STORAGE_BYTES: Long = 91_268_055_040L

    /** Exact total storage: 222 GB from df -h /data. */
    const val TOTAL_STORAGE_BYTES: Long = 238_370_684_928L

    /**
     * Canonical [AiDeviceProfile] derived from actual hardware.
     */
    val PROFILE: AiDeviceProfile = AiDeviceProfile(
        totalRamBytes = TOTAL_RAM_BYTES,
        availableRamBytes = AVAILABLE_RAM_BYTES,
        freeStorageBytes = FREE_STORAGE_BYTES,
        cpuArchitecture = CpuArchitecture.ARM64,
        osPlatform = OsPlatform.ANDROID,
        osVersion = "Android $OS_RELEASE (API $OS_SDK)",
        supportedRuntimes = setOf(
            AiRuntimeType.GGUF,
            AiRuntimeType.DETERMINISTIC_FALLBACK,
        ),
        supportedAccelerators = setOf(
            AiAcceleratorType.CPU,
            AiAcceleratorType.GPU,
        ),
        supportedLanguages = setOf("en", "hi", "ar"),
        maxSafeRamAllocationBytes = AiDeviceProfile.calculateSafeRamAllocation(AVAILABLE_RAM_BYTES),
        manufacturer = MANUFACTURER,
        modelName = MODEL,
        cpuAbi = CPU_ABI,
        sdkInt = OS_SDK,
        totalStorageBytes = TOTAL_STORAGE_BYTES,
    )
}
