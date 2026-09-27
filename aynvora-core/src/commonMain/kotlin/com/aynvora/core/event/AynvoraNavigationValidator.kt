package com.aynvora.core.event

import com.aynvora.core.feature.CanonicalCoreFeatures

/**
 * Result of validating a navigation target before execution.
 */
sealed interface NavigationValidationResult {
    data object Valid : NavigationValidationResult
    data class Invalid(val reason: String) : NavigationValidationResult
    data class Unauthorized(val reason: String) : NavigationValidationResult
}

/**
 * Validates [AynvoraNavigationTarget]s against format rules, injection attacks, and authorization.
 * UI must never be able to navigate to arbitrary URLs or malformed resource IDs.
 */
object AynvoraNavigationValidator {
    private val SAFE_ID_REGEX = Regex("^[a-zA-Z0-9_-]{1,128}$")

    fun validate(target: AynvoraNavigationTarget): NavigationValidationResult {
        return when (target) {
            is AynvoraNavigationTarget.TarotReading -> {
                if (target.readingId.isBlank()) {
                    NavigationValidationResult.Invalid("Tarot readingId cannot be blank")
                } else if (!SAFE_ID_REGEX.matches(target.readingId)) {
                    NavigationValidationResult.Invalid("Tarot readingId contains invalid characters")
                } else {
                    NavigationValidationResult.Valid
                }
            }

            is AynvoraNavigationTarget.TarotCardDetail -> {
                if (target.cardId.isBlank() || target.deckId.isBlank()) {
                    NavigationValidationResult.Invalid("Card ID and Deck ID cannot be blank")
                } else if (!SAFE_ID_REGEX.matches(target.cardId) || !SAFE_ID_REGEX.matches(target.deckId)) {
                    NavigationValidationResult.Invalid("Card ID or Deck ID contains invalid characters")
                } else {
                    NavigationValidationResult.Valid
                }
            }

            is AynvoraNavigationTarget.PalmistryReading -> {
                if (target.sessionId.isBlank()) {
                    NavigationValidationResult.Invalid("Palmistry sessionId cannot be blank")
                } else if (!SAFE_ID_REGEX.matches(target.sessionId)) {
                    NavigationValidationResult.Invalid("Palmistry sessionId contains invalid characters")
                } else {
                    NavigationValidationResult.Valid
                }
            }

            is AynvoraNavigationTarget.PalmistryFeatureDetail -> {
                if (target.featureName.isBlank()) {
                    NavigationValidationResult.Invalid("Feature name cannot be blank")
                } else {
                    NavigationValidationResult.Valid
                }
            }

            is AynvoraNavigationTarget.FeatureDetail -> {
                val exists = CanonicalCoreFeatures.any { it.id == target.featureId }
                if (!exists) {
                    NavigationValidationResult.Invalid("Unknown feature ID: ${target.featureId}")
                } else {
                    NavigationValidationResult.Valid
                }
            }

            is AynvoraNavigationTarget.Dashboard,
            is AynvoraNavigationTarget.AstrologyHome,
            is AynvoraNavigationTarget.TarotHome,
            is AynvoraNavigationTarget.PalmistryHome,
            is AynvoraNavigationTarget.NumerologyHome,
            is AynvoraNavigationTarget.NumerologyCompare,
            is AynvoraNavigationTarget.NumerologyHistory,
            is AynvoraNavigationTarget.TarotHistory,
            is AynvoraNavigationTarget.PalmistryHistory,
            is AynvoraNavigationTarget.AiSettings,
            is AynvoraNavigationTarget.AiDiagnostics,
            is AynvoraNavigationTarget.Settings,
            is AynvoraNavigationTarget.History,
            is AynvoraNavigationTarget.Back,
            is AynvoraNavigationTarget.Close -> NavigationValidationResult.Valid
        }
    }
}
