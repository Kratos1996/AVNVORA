package com.aynvora.yantra.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Yantra Engine.
 */
class YantraEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.YANTRA
    override fun create(): AynvoraFeatureEngine = YantraFeatureEngine()
}
