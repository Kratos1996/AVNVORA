package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Visual theme mode selection.
 */
@Serializable
enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Platform-independent user preferences contract.
 *
 * Only includes settings justified by existing product and technical documentation:
 * theme, language, and default astrological calculation configurations.
 */
@Serializable
data class UserPreferences(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val languageCode: String = "en",
    val defaultCalculationProfile: CalculationProfile = CalculationProfile.STANDARD_VEDIC,
    val defaultAyanamsa: AyanamsaConvention = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
    val defaultHouseSystem: HouseSystem = HouseSystem.EQUAL_HOUSE,
) {
    init {
        require(languageCode.isNotBlank()) { "Language code cannot be blank" }
    }
}
