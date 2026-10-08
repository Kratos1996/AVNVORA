package com.aynvora.core.access

import com.aynvora.contracts.AynvoraFeatureId
import com.aynvora.contracts.AynvoraFeatureSettings
import com.aynvora.contracts.AynvoraStatus
import com.aynvora.contracts.FeatureAccessState

/**
 * Access control and feature locking mechanism.
 *
 * Enforces dynamic enablement/disabling of engines as configured by SDK settings
 * before any event is dispatched to an underlying calculation engine.
 */
class FeatureAccessController(
    private var settings: AynvoraFeatureSettings = AynvoraFeatureSettings.DEFAULT,
) {

    fun updateSettings(newSettings: AynvoraFeatureSettings) {
        this.settings = newSettings
    }

    fun getSettings(): AynvoraFeatureSettings = settings

    fun checkAccess(featureId: AynvoraFeatureId): FeatureAccessDecision {
        // Resolve alias
        val canonicalId = when (featureId) {
            AynvoraFeatureId.GUIDANCE -> AynvoraFeatureId.DAILY_GUIDANCE
            else -> featureId
        }

        val state = settings.featureStates[canonicalId] ?: FeatureAccessState.ENABLED

        return when (state) {
            FeatureAccessState.NOT_INCLUDED -> FeatureAccessDecision.Blocked(
                status = AynvoraStatus.FEATURE_NOT_INCLUDED,
                reason = "Feature '${featureId.name}' is not included in this bundle/binary.",
            )
            FeatureAccessState.ENABLED,
            FeatureAccessState.AVAILABLE,
            FeatureAccessState.VERIFIED,
            FeatureAccessState.PRODUCTION -> {
                if (settings.enabledFeatures.contains(canonicalId)) {
                    FeatureAccessDecision.Allowed
                } else {
                    FeatureAccessDecision.Blocked(
                        status = AynvoraStatus.FEATURE_DISABLED,
                        reason = "Feature '${featureId.name}' is disabled in settings.",
                    )
                }
            }
            FeatureAccessState.DISABLED -> FeatureAccessDecision.Blocked(
                status = AynvoraStatus.FEATURE_DISABLED,
                reason = "Feature '${featureId.name}' is explicitly disabled.",
            )
            FeatureAccessState.LOCKED -> FeatureAccessDecision.Blocked(
                status = AynvoraStatus.FEATURE_LOCKED,
                reason = "Feature '${featureId.name}' is locked.",
            )
            FeatureAccessState.NOT_READY -> FeatureAccessDecision.Blocked(
                status = AynvoraStatus.FEATURE_NOT_READY,
                reason = "Feature '${featureId.name}' is not ready for execution.",
            )
            FeatureAccessState.UNSUPPORTED -> FeatureAccessDecision.Blocked(
                status = AynvoraStatus.FEATURE_UNSUPPORTED,
                reason = "Feature '${featureId.name}' is unsupported on this platform profile.",
            )
            FeatureAccessState.RESEARCH_ONLY -> {
                if (settings.enabledFeatures.contains(canonicalId)) {
                    FeatureAccessDecision.Allowed
                } else {
                    FeatureAccessDecision.Blocked(
                        status = AynvoraStatus.FEATURE_DISABLED,
                        reason = "Research-only feature '${featureId.name}' is not enabled in settings.",
                    )
                }
            }
        }
    }
}

sealed interface FeatureAccessDecision {
    data object Allowed : FeatureAccessDecision
    data class Blocked(
        val status: AynvoraStatus,
        val reason: String,
    ) : FeatureAccessDecision
}
