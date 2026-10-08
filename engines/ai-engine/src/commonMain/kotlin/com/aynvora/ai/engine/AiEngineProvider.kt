package com.aynvora.ai.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the AI Intelligence / Grounding Engine.
 */
class AiEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.AI_ASSISTANT
    override fun create(): AynvoraFeatureEngine = AiFeatureEngine()
}
