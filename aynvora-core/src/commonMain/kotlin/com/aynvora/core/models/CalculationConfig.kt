package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Calculation profile convention per docs/08_ASTRO_ENGINE.md.
 */
@Serializable
enum class CalculationProfile {
    STANDARD_VEDIC,
    SURYA_SIDDHANTA,
    DRIG_GANITA,
}

/**
 * Ayanamsa reference system for sidereal calculations.
 */
@Serializable
enum class AyanamsaConvention {
    LAHIRI_CHITRAPAKSHA,
    RAMAN,
    KRISHNAMURTI_KP,
    TROPICAL,
}

/**
 * Astrological house division system.
 */
@Serializable
enum class HouseSystem {
    EQUAL_HOUSE,
    SHRIPATI_PORPHYRY,
    PLACIDUS,
}

/**
 * Deterministic configuration settings for astrological calculations.
 */
@Serializable
data class CalculationConfig(
    val profile: CalculationProfile = CalculationProfile.STANDARD_VEDIC,
    val ayanamsa: AyanamsaConvention = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
    val houseSystem: HouseSystem = HouseSystem.EQUAL_HOUSE,
)
