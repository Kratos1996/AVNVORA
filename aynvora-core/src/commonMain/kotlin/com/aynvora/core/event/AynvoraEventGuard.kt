package com.aynvora.core.event

import com.aynvora.core.feature.CanonicalCoreFeatures

/**
 * Result of validating an event before execution.
 */
sealed interface AynvoraEventValidationResult {
    data object Valid : AynvoraEventValidationResult
    data class InvalidPayload(val reason: String) : AynvoraEventValidationResult
    data class Unauthorized(val reason: String) : AynvoraEventValidationResult
    data class Duplicate(val eventId: String) : AynvoraEventValidationResult
    data class InvalidState(val reason: String) : AynvoraEventValidationResult
    data class Unsupported(val reason: String) : AynvoraEventValidationResult
    data class RateLimited(val waitMs: Long) : AynvoraEventValidationResult
}

/**
 * Validates events against business invariants, permissions, registry, and payload safety.
 * Events must never bypass domain security.
 */
interface AynvoraEventGuard {
    fun validate(event: AynvoraEvent): AynvoraEventValidationResult
}

/**
 * Production implementation of [AynvoraEventGuard].
 *
 * Verifies:
 * - Structural non-blank invariants (eventId, screenId)
 * - Negative timestamp prevention
 * - Namespace isolation (prohibiting admin.*, system.*, internal.* namespaces)
 * - Strict registration check against [AynvoraEventRegistry]
 * - Payload ownership validation (rejecting mismatched eventId/payload combinations)
 * - Navigation target security via [AynvoraNavigationValidator]
 * - Domain payload boundary validation (star ratings, question text, etc.)
 */
class StandardAynvoraEventGuard(
    private val registry: AynvoraEventRegistry = AynvoraEventRegistry,
) : AynvoraEventGuard {

    override fun validate(event: AynvoraEvent): AynvoraEventValidationResult {
        // 1. Basic structural validation
        if (event.eventId.isBlank()) {
            return AynvoraEventValidationResult.InvalidPayload("Event ID cannot be blank")
        }
        if (event.screenId.isBlank()) {
            return AynvoraEventValidationResult.InvalidPayload("Screen ID cannot be blank")
        }
        if (event.timestampEpochMs < 0L) {
            return AynvoraEventValidationResult.InvalidPayload("Timestamp cannot be negative")
        }

        // 2. Namespace security
        if (event.eventId.startsWith("admin.") ||
            event.eventId.startsWith("system.") ||
            event.eventId.startsWith("internal.")
        ) {
            return AynvoraEventValidationResult.Unauthorized(
                "Security violation: event origin or namespace is forbidden: ${event.eventId}"
            )
        }

        // 3. Explicit Event Registry verification
        val reg = registry.find(event.eventId)
            ?: return AynvoraEventValidationResult.Unauthorized("Unregistered event: ${event.eventId}")

        // 4. Payload ownership check
        val expected = reg.expectedPayloadClass
        val actual = event.payload::class
        if (actual != expected && expected != AynvoraEventPayload.Empty::class) {
            return AynvoraEventValidationResult.InvalidPayload(
                "Payload type mismatch for ${event.eventId}: expected ${expected.simpleName}, got ${actual.simpleName}"
            )
        }

        // 5. Deep payload content validation
        return when (val payload = event.payload) {
            is AynvoraEventPayload.Empty -> AynvoraEventValidationResult.Valid

            is AynvoraEventPayload.FeatureOpenPayload -> {
                val desc = CanonicalCoreFeatures.firstOrNull { it.id == payload.featureId }
                if (desc == null) {
                    AynvoraEventValidationResult.InvalidPayload("Unknown feature ID: ${payload.featureId}")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.NavigationPayload -> {
                when (val navValidation = AynvoraNavigationValidator.validate(payload.target)) {
                    is NavigationValidationResult.Valid -> AynvoraEventValidationResult.Valid
                    is NavigationValidationResult.Invalid -> AynvoraEventValidationResult.InvalidPayload(
                        navValidation.reason
                    )

                    is NavigationValidationResult.Unauthorized -> AynvoraEventValidationResult.Unauthorized(
                        navValidation.reason
                    )
                }
            }

            is AynvoraEventPayload.LanguageChangePayload -> {
                if (payload.localeId.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Locale ID cannot be blank")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.AiDownloadPayload -> {
                if (payload.modelId.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Model ID cannot be blank for AI download")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.TarotSelectDeckPayload -> {
                if (payload.deckId.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Deck ID cannot be blank")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.TarotSelectSpreadPayload -> {
                if (payload.spreadId.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Spread ID cannot be blank")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.TarotFeedbackPayload -> {
                if (payload.starRating !in 1..5) {
                    AynvoraEventValidationResult.InvalidPayload("Star rating must be between 1 and 5")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.PalmFeedbackPayload -> {
                if (payload.starRating !in 1..5) {
                    AynvoraEventValidationResult.InvalidPayload("Star rating must be between 1 and 5")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.TarotQuestionPayload -> {
                if (payload.questionText.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Question text cannot be blank")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            is AynvoraEventPayload.PalmQuestionPayload -> {
                if (payload.questionText.isBlank()) {
                    AynvoraEventValidationResult.InvalidPayload("Question text cannot be blank")
                } else {
                    AynvoraEventValidationResult.Valid
                }
            }

            else -> AynvoraEventValidationResult.Valid
        }
    }
}
