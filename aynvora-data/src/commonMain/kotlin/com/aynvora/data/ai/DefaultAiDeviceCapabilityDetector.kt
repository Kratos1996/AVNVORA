package com.aynvora.data.ai

import com.aynvora.core.ai.ActualAndroidDeviceProfile
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile

/**
 * Default production device capability detector.
 *
 * Derives capabilities from [ActualAndroidDeviceProfile], providing
 * verified hardware specifications for on-device SLM execution.
 */
class DefaultAiDeviceCapabilityDetector(
    private val profile: AiDeviceProfile = ActualAndroidDeviceProfile.PROFILE,
) : AiDeviceCapabilityDetector {

    override suspend fun detectCapability(): AiDeviceProfile = profile
}
