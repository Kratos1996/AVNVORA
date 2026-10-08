package com.aynvora.numerology.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Numerology Feature Engine.
 */
class NumerologyEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.NUMEROLOGY
    override fun create(): AynvoraFeatureEngine = NumerologyFeatureEngine()
}
