package com.aynvora.report.engine

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureId

/**
 * Explicit lazy provider for the Independent Composite Report Engine.
 */
class ReportEngineProvider : AynvoraEngineProvider {
    override val featureId: AynvoraFeatureId = AynvoraFeatureId.REPORT
    override fun create(): AynvoraFeatureEngine = ReportFeatureEngine()
}
