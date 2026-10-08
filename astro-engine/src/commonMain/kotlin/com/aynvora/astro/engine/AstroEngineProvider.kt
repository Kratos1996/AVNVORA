package com.aynvora.astro.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Astro Calculation Engine.
 */
class AstroEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.ASTROLOGY
    override fun create(): AynvoraFeatureEngine = AstroFeatureEngine()
}
