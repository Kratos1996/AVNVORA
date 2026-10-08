package com.aynvora.sdk

import com.aynvora.contracts.AynvoraEngineProvider
import com.aynvora.contracts.AynvoraFeatureEngine
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.registry.AynvoraEngineRegistry

/**
 * Configuration options for initializing the AYNVORA SDK.
 *
 * @param settings Feature access and security settings (enables/disables specific features).
 * @param analyticsTracker Centralized analytics observer.
 * @param registry Engine registry. If custom registry is not provided, initialized with [engineProviders] and [engines].
 * @param engineProviders Explicit list of lazy engine providers included in this consumer application.
 * @param engines Optional list of already-instantiated engines.
 */
data class AynvoraConfig(
    val settings: AynvoraFeatureSettings = AynvoraFeatureSettings.DEFAULT,
    val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
    val registry: AynvoraEngineRegistry = AynvoraEngineRegistry(),
    val engineProviders: List<AynvoraEngineProvider> = emptyList(),
    val engines: List<AynvoraFeatureEngine> = emptyList(),
)
