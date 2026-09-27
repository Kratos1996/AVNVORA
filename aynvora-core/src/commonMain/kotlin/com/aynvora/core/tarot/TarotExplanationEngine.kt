package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult

/**
 * Input contract for an SLM-based (or deterministic-fallback) Tarot reflection explanation.
 *
 * The SLM is strictly an **explanatory contemplation layer**. It is never used to:
 * - Select which cards are drawn (draw engine is deterministic + random source)
 * - Predict future events, relationships, career, health, or money
 * - Override approved deterministic card meanings
 *
 * All produced explanations are clearly framed as reflection, not prediction.
 */
data class TarotExplanationRequest(
    val requestId: String,
    val draw: TarotCardDraw,
    val spreadPositionContext: String,
    val language: String = "en",
    val deterministicContent: TarotCardContent,
    val allowSlmInference: Boolean = false,
)

/**
 * Provenance metadata for an explanation — what produced it and what it references.
 */
data class TarotExplanationProvenance(
    val producedBy: String,
    val modelId: String? = null,
    val contentVersion: Int,
    val fallbackUsed: Boolean,
    val fallbackReason: String? = null,
    val sourceAttribution: String,
)

/**
 * Result from the TarotExplanationEngine.
 *
 * Always returns usable content — either SLM-enriched or deterministic fallback.
 * Never returns null or empty text.
 */
data class TarotExplanationResult(
    val requestId: String,
    val cardId: String,
    val language: String,
    val explanationText: String,
    val fallbackUsed: Boolean,
    val provenance: TarotExplanationProvenance,
)

/**
 * Engine contract for producing contemplative Tarot card explanations.
 *
 * Implementations may use an SLM for richer contextual reflection, but MUST
 * immediately fall back to approved [TarotCardContent] if the SLM is unavailable,
 * too slow, or produces an invalid response.
 *
 * Contract invariants:
 * 1. NEVER fails — always returns a [TarotExplanationResult] with usable content.
 * 2. Deterministic fallback is always enabled and always takes priority on failure.
 * 3. SLM inference is only engaged when [TarotExplanationRequest.allowSlmInference] is true.
 * 4. The produced explanation MUST be explicitly non-predictive.
 */
interface TarotExplanationEngine {
    suspend fun explain(request: TarotExplanationRequest): AynvoraResult<TarotExplanationResult>
}

/**
 * Deterministic, always-available fallback implementation of [TarotExplanationEngine].
 *
 * Produces approved card meanings from [TarotCardContent] with no SLM inference.
 * Used as the production default until an SLM integration is deployed in Phase 8.6.
 */
class DeterministicTarotExplanationEngine(
    private val localizationProvider: com.aynvora.core.localization.LocalizationProvider? = null,
) : TarotExplanationEngine {

    override suspend fun explain(request: TarotExplanationRequest): AynvoraResult<TarotExplanationResult> {
        val content = request.deterministicContent
        val meaning = if (request.draw.orientation == TarotCardOrientation.UPRIGHT) {
            content.uprightMeaning
        } else {
            content.reversedMeaning
        }

        val explanationText = buildDeterministicExplanation(
            cardName = content.title,
            orientation = request.draw.orientation,
            meaning = meaning,
            positionContext = request.spreadPositionContext,
            keywords = content.keywords,
            language = request.language,
        )

        val result = TarotExplanationResult(
            requestId = request.requestId,
            cardId = request.draw.card.id,
            language = request.language,
            explanationText = explanationText,
            fallbackUsed = true,
            provenance = TarotExplanationProvenance(
                producedBy = "DeterministicTarotExplanationEngine",
                modelId = null,
                contentVersion = content.contentVersion,
                fallbackUsed = true,
                fallbackReason = "SLM inference not requested or not available.",
                sourceAttribution = content.sourceAttribution,
            ),
        )
        return AynvoraResult.Success(result)
    }

    private fun buildDeterministicExplanation(
        cardName: String,
        orientation: TarotCardOrientation,
        meaning: String,
        positionContext: String,
        keywords: List<String>,
        language: String,
    ): String {
        val locale = com.aynvora.core.localization.AynvoraLocale.fromId(language)
        val provider =
            localizationProvider ?: com.aynvora.core.localization.FallbackLocalizationProvider(
                locale
            )

        val orientationKey = if (orientation == TarotCardOrientation.UPRIGHT) {
            "tarot.screen.upright"
        } else {
            "tarot.screen.reversed"
        }
        val rawOrientation =
            provider.get(com.aynvora.core.localization.RawLocalizationKey(orientationKey))
        val orientationLabel = if (rawOrientation.startsWith("tarot.")) {
            if (orientation == TarotCardOrientation.UPRIGHT) "Upright" else "Reversed"
        } else rawOrientation

        val keywordsText = if (keywords.isNotEmpty()) {
            val rawLabel =
                provider.get(com.aynvora.core.localization.RawLocalizationKey("tarot.screen.keywords_label"))
            val label = if (rawLabel.startsWith("tarot.")) "Keywords: " else rawLabel
            "$label${keywords.joinToString(" · ")}"
        } else ""

        val template = provider.get(
            com.aynvora.core.localization.RawLocalizationKey("tarot.explanation.template"),
            mapOf(
                "position" to positionContext,
                "card" to cardName,
                "orientation" to orientationLabel,
                "meaning" to meaning,
                "keywords" to keywordsText,
            ),
        )
        if (template.isNotEmpty() && !template.startsWith("tarot.explanation.template")) {
            return template
        }

        return buildString {
            append("In the position of $positionContext, the card $cardName ($orientationLabel) has emerged.\n\n")
            append(meaning)
            if (keywordsText.isNotEmpty()) {
                append("\n\n$keywordsText")
            }
            append("\n\n[This reflection is offered for contemplation, not prediction.]")
        }
    }
}
