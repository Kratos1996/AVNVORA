package com.aynvora.tarot.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Tarot Feature Engine.
 */
class TarotEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.TAROT
    override fun create(): AynvoraFeatureEngine = TarotFeatureEngine()
}
