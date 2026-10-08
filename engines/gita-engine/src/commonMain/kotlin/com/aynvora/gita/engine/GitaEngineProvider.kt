package com.aynvora.gita.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Bhagavad Gita Feature Engine.
 */
class GitaEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GITA
    override fun create(): AynvoraFeatureEngine = GitaFeatureEngine()
}
