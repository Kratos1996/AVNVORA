package com.aynvora.palmistry.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Palmistry Feature Engine.
 */
class PalmEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.PALMISTRY
    override fun create(): AynvoraFeatureEngine = PalmFeatureEngine()
}
