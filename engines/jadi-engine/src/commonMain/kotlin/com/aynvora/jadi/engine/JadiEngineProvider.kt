package com.aynvora.jadi.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Herbal Jadi Recommendation Engine.
 */
class JadiEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.JADI
    override fun create(): AynvoraFeatureEngine = JadiFeatureEngine()
}
