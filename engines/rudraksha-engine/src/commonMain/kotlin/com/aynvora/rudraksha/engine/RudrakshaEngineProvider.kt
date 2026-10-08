package com.aynvora.rudraksha.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Rudraksha Recommendation Engine.
 */
class RudrakshaEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.RUDRAKSHA
    override fun create(): AynvoraFeatureEngine = RudrakshaFeatureEngine()
}
