package com.aynvora.contracts

import kotlinx.serialization.Serializable

/**
 * Lifecycle and access state for individual product feature engines.
 */
@Serializable
enum class FeatureAccessState {
    NOT_INCLUDED,
    ENABLED,
    DISABLED,
    LOCKED,
    NOT_READY,
    RESEARCH_ONLY,
    UNSUPPORTED,
    AVAILABLE,
    VERIFIED,
    PRODUCTION,
}

/**
 * Central SDK feature toggle and security settings.
 * Allows host apps or user preferences to selectively enable/disable engines.
 */
@Serializable
data class AynvoraFeatureSettings(
    val featureStates: Map<AynvoraFeatureId, FeatureAccessState> = defaultFeatureStates(),
) {
    val enabledFeatures: Set<AynvoraFeatureId>
        get() = featureStates.filter {
            it.value == FeatureAccessState.ENABLED ||
            it.value == FeatureAccessState.AVAILABLE ||
            it.value == FeatureAccessState.VERIFIED ||
            it.value == FeatureAccessState.PRODUCTION
        }.keys

    fun isEnabled(featureId: AynvoraFeatureId): Boolean {
        val state = featureStates[featureId]
        return state == FeatureAccessState.ENABLED ||
               state == FeatureAccessState.AVAILABLE ||
               state == FeatureAccessState.VERIFIED ||
               state == FeatureAccessState.PRODUCTION
    }

    fun isIncluded(featureId: AynvoraFeatureId): Boolean {
        return featureStates[featureId] != FeatureAccessState.NOT_INCLUDED
    }

    fun withFeatureState(featureId: AynvoraFeatureId, state: FeatureAccessState): AynvoraFeatureSettings {
        val updated = featureStates.toMutableMap()
        updated[featureId] = state
        return copy(featureStates = updated)
    }

    companion object {
        val DEFAULT: AynvoraFeatureSettings = AynvoraFeatureSettings()

        fun defaultFeatureStates(): Map<AynvoraFeatureId, FeatureAccessState> = mapOf(
            AynvoraFeatureId.ASTROLOGY to FeatureAccessState.ENABLED,
            AynvoraFeatureId.PALMISTRY to FeatureAccessState.ENABLED,
            AynvoraFeatureId.TAROT to FeatureAccessState.ENABLED,
            AynvoraFeatureId.NUMEROLOGY to FeatureAccessState.ENABLED,
            AynvoraFeatureId.GEMSTONE to FeatureAccessState.ENABLED,
            AynvoraFeatureId.GITA to FeatureAccessState.ENABLED,
            AynvoraFeatureId.GARUDA_PURAN to FeatureAccessState.ENABLED,
            AynvoraFeatureId.RUDRAKSHA to FeatureAccessState.ENABLED,
            AynvoraFeatureId.JADI to FeatureAccessState.ENABLED,
            AynvoraFeatureId.YANTRA to FeatureAccessState.ENABLED,
            AynvoraFeatureId.DAILY_GUIDANCE to FeatureAccessState.ENABLED,
            AynvoraFeatureId.AI_ASSISTANT to FeatureAccessState.ENABLED,
            AynvoraFeatureId.REPORT to FeatureAccessState.ENABLED,
            AynvoraFeatureId.LAL_KITAB to FeatureAccessState.RESEARCH_ONLY,
            AynvoraFeatureId.WALLPAPER to FeatureAccessState.ENABLED,
        )
    }
}
