package com.aynvora.gemstone.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Gemstone Feature Engine.
 */
class GemstoneEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GEMSTONE
    override fun create(): AynvoraFeatureEngine = GemstoneFeatureEngine()
}
