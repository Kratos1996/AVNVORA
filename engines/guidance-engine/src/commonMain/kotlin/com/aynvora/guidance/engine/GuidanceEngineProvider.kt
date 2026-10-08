package com.aynvora.guidance.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Guidance / Daily Guidance Feature Engine.
 */
class GuidanceEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.DAILY_GUIDANCE
    override fun create(): AynvoraFeatureEngine = GuidanceFeatureEngine()
}
