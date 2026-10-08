package com.aynvora.core.orchestration

import com.aynvora.contracts.AynvoraEvent
import com.aynvora.contracts.AynvoraEventRequest
import com.aynvora.contracts.AynvoraEventResponse
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.core.access.FeatureAccessController
import com.aynvora.core.access.FeatureAccessDecision
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.registry.AynvoraEngineRegistry

/**
 * Central event routing and execution transaction manager in AYNVORA Core.
 *
 * Implements the canonical flow:
 * External App / UI -> AynvoraEventRequest -> FeatureAccessController -> EngineRegistry -> FeatureEngine -> Result JSON -> Analytics -> Response
 */
class AynvoraEventRouter(
    val registry: AynvoraEngineRegistry = AynvoraEngineRegistry(),
    val accessController: FeatureAccessController = FeatureAccessController(),
    val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {

    suspend fun route(request: AynvoraEventRequest): AynvoraEventResponse {
        val event = AynvoraEvent(
            eventId = "evt_${System.currentTimeMillis()}_${request.featureId.name.lowercase()}",
            featureId = request.featureId,
            eventType = request.eventType,
            timestampEpochMs = System.currentTimeMillis(),
            requestId = request.requestId,
            payloadJson = request.payloadJson,
            metadata = request.metadata,
        )
        return route(event)
    }

    suspend fun route(event: AynvoraEvent): AynvoraEventResponse {
        val startTime = System.currentTimeMillis()

        // 1. Feature Access and Gate Verification
        val decision = accessController.checkAccess(event.featureId)
        if (decision is FeatureAccessDecision.Blocked) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = event.featureId,
                status = decision.status,
                messageKey = "error.access.${decision.status.name.lowercase()}",
                details = decision.reason,
                durationMs = System.currentTimeMillis() - startTime,
            )
        }

        // 2. Engine Discovery & Availability Check
        if (!registry.isAvailable(event.featureId)) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = event.featureId,
                status = AynvoraStatus.FEATURE_NOT_INCLUDED,
                messageKey = "error.registry.feature_not_included",
                details = "No engine registered or bundled in this binary for feature: ${event.featureId}",
                durationMs = System.currentTimeMillis() - startTime,
            )
        }

        val engine = registry.getEngine(event.featureId)
        if (engine == null) {
            return AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = event.featureId,
                status = AynvoraStatus.FEATURE_NOT_INCLUDED,
                messageKey = "error.registry.engine_initialization_failed",
                details = "Engine failed to initialize or not found for feature: ${event.featureId}",
                durationMs = System.currentTimeMillis() - startTime,
            )
        }

        // 3. Delegate to isolated Feature Engine with Lifecycle Tracking
        registry.setLifecycleState(event.featureId, com.aynvora.contracts.EngineLifecycleState.RUNNING)
        val response = try {
            val res = engine.handle(event)
            registry.setLifecycleState(
                event.featureId,
                if (res.isSuccess) com.aynvora.contracts.EngineLifecycleState.IDLE
                else com.aynvora.contracts.EngineLifecycleState.FAILED
            )
            res
        } catch (t: Throwable) {
            registry.setLifecycleState(event.featureId, com.aynvora.contracts.EngineLifecycleState.FAILED)
            AynvoraEventResponse.failure(
                eventId = event.eventId,
                requestId = event.requestId,
                featureId = event.featureId,
                status = AynvoraStatus.ENGINE_ERROR,
                messageKey = "error.core.unhandled_engine_exception",
                details = t.message ?: "Unknown unhandled exception",
                durationMs = System.currentTimeMillis() - startTime,
            )
        }

        return response
    }
}
