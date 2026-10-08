package com.aynvora.sdk

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.core.access.FeatureAccessController
import com.aynvora.core.access.FeatureAccessDecision
import com.aynvora.core.orchestration.AynvoraEventRouter
import com.aynvora.sdk.clients.AynvoraAiClient
import com.aynvora.sdk.clients.AynvoraAstrologyClient
import com.aynvora.sdk.clients.AynvoraGarudaPuranClient
import com.aynvora.sdk.clients.AynvoraGemstoneClient
import com.aynvora.sdk.clients.AynvoraGitaClient
import com.aynvora.sdk.clients.AynvoraGuidanceClient
import com.aynvora.sdk.clients.AynvoraJadiClient
import com.aynvora.sdk.clients.AynvoraNumerologyClient
import com.aynvora.sdk.clients.AynvoraPalmistryClient
import com.aynvora.sdk.clients.AynvoraReportClient
import com.aynvora.sdk.clients.AynvoraRudrakshaClient
import com.aynvora.sdk.clients.AynvoraTarotClient
import com.aynvora.sdk.clients.AynvoraYantraClient
import kotlinx.serialization.json.Json

/**
 * Universal Entrypoint to the AYNVORA Modular SDK Platform.
 *
 * Provides two primary interaction paradigms:
 * - MODE A: Strongly Typed APIs ([astrology], [palmistry], [numerology], [tarot], [gemstone], [ai], [report], etc.)
 * - MODE B: Raw Event/JSON API ([dispatch]) for headless execution, cross-platform bridges, or external apps.
 *
 * Feature access is guarded dynamically by [AynvoraFeatureSettings].
 */
class Aynvora internal constructor(
    val config: AynvoraConfig,
    internal val router: AynvoraEventRouter,
) {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
    }

    // --- MODE A: Strongly Typed SDK Clients ---
    val astrology: AynvoraAstrologyClient by lazy { AynvoraAstrologyClient(router, json) }
    val palmistry: AynvoraPalmistryClient by lazy { AynvoraPalmistryClient(router, json) }
    val numerology: AynvoraNumerologyClient by lazy { AynvoraNumerologyClient(router, json) }
    val tarot: AynvoraTarotClient by lazy { AynvoraTarotClient(router, json) }
    val gemstone: AynvoraGemstoneClient by lazy { AynvoraGemstoneClient(router, json) }
    val gita: AynvoraGitaClient by lazy { AynvoraGitaClient(router, json) }
    val garudaPuran: AynvoraGarudaPuranClient by lazy { AynvoraGarudaPuranClient(router, json) }
    val rudraksha: AynvoraRudrakshaClient by lazy { AynvoraRudrakshaClient(router, json) }
    val jadi: AynvoraJadiClient by lazy { AynvoraJadiClient(router, json) }
    val yantra: AynvoraYantraClient by lazy { AynvoraYantraClient(router, json) }
    val guidance: AynvoraGuidanceClient by lazy { AynvoraGuidanceClient(router, json) }
    val ai: AynvoraAiClient by lazy { AynvoraAiClient(router, json) }
    val report: AynvoraReportClient by lazy { AynvoraReportClient(router, json) }

    // --- MODE B: Raw Event / JSON SDK API ---
    suspend fun dispatch(request: AynvoraEventRequest): AynvoraEventResponse = router.route(request)
    suspend fun dispatch(event: AynvoraEvent): AynvoraEventResponse = router.route(event)

    // --- Feature Access Control & Settings ---
    fun updateSettings(newSettings: AynvoraFeatureSettings) {
        router.accessController.updateSettings(newSettings)
    }

    fun getSettings(): AynvoraFeatureSettings = router.accessController.getSettings()

    fun isFeatureEnabled(featureId: AynvoraFeatureId): Boolean {
        return router.accessController.checkAccess(featureId) is FeatureAccessDecision.Allowed
    }

    fun isFeatureAvailable(featureId: AynvoraFeatureId): Boolean {
        return router.registry.isAvailable(featureId)
    }

    fun getLifecycleState(featureId: AynvoraFeatureId): com.aynvora.contracts.EngineLifecycleState {
        return router.registry.getLifecycleState(featureId)
    }

    fun releaseEngine(featureId: AynvoraFeatureId) {
        router.registry.release(featureId)
    }

    fun releaseAllEngines() {
        router.registry.releaseAll()
    }

    companion object {
        fun create(config: AynvoraConfig = AynvoraConfig()): Aynvora {
            val registry = config.registry
            val providers = if (config.engineProviders.isEmpty() && config.engines.isEmpty()) {
                AynvoraBundledEngines.allProviders()
            } else {
                config.engineProviders
            }
            providers.forEach { registry.register(it) }
            config.engines.forEach { registry.register(it) }

            val accessController = FeatureAccessController(config.settings)
            val router = AynvoraEventRouter(
                registry = registry,
                accessController = accessController,
                analyticsTracker = config.analyticsTracker,
            )
            return Aynvora(config, router)
        }
    }
}
