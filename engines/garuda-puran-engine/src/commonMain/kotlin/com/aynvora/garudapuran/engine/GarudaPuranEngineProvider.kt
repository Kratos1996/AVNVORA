package com.aynvora.garudapuran.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Garuda Puran Feature Engine.
 */
class GarudaPuranEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.GARUDA_PURAN
    override fun create(): AynvoraFeatureEngine = GarudaPuranFeatureEngine()
}
