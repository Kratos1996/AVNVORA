package com.aynvora.core.tarot

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.result.AynvoraResult

/**
 * Use case to perform a Tarot card draw for a given spread.
 *
 * Coordinates:
 * 1. Fetching available cards from [tarotRepository].
 * 2. Drawing cards via [drawEngine] (using [TarotRandomSource]).
 * 3. Saving reading history locally.
 * 4. Firing non-PII analytics events via [analyticsTracker].
 */
class PerformTarotReadingUseCase(
    private val tarotRepository: TarotRepository,
    private val drawEngine: TarotDrawEngine = TarotDrawEngine(),
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {

    suspend fun execute(
        spread: TarotSpread,
        allowReversed: Boolean = true,
        deckId: String = TarotStandardDeck.Deck.id,
        timestampEpochMs: Long = 0L,
    ): AynvoraResult<TarotReading> {
        analyticsTracker.track(AnalyticsEvent.TarotReadingStarted(spread.id, spread.cardCount))

        val cardsResult = tarotRepository.getDeckCards(deckId)
        val cards = when (cardsResult) {
            is AynvoraResult.Success -> cardsResult.value
            is AynvoraResult.Failure -> return cardsResult
        }

        return try {
            val reading = drawEngine.drawSpread(
                spread = spread,
                deckCards = cards,
                allowReversed = allowReversed,
                deckId = deckId,
                timestampEpochMs = timestampEpochMs,
            )

            // Track each drawn card's non-PII attributes
            reading.draws.forEach { draw ->
                analyticsTracker.track(
                    AnalyticsEvent.TarotCardDrawn(
                        spreadId = spread.id,
                        cardId = draw.card.id,
                        orientation = draw.orientation.name,
                    ),
                )
            }

            // Persist reading to local history
            tarotRepository.saveReading(reading)

            analyticsTracker.track(
                AnalyticsEvent.TarotReadingCompleted(
                    spread.id,
                    reading.draws.size
                )
            )
            AynvoraResult.Success(reading)
        } catch (e: Exception) {
            analyticsTracker.track(
                AnalyticsEvent.TarotReadingFailed(
                    spread.id,
                    "draw_execution_failed"
                )
            )
            AynvoraResult.Failure.InternalFailure(
                message = "Failed to complete Tarot reading: ${e.message}",
            )
        }
    }
}

/**
 * Use case to retrieve localized reflective content for a drawn card.
 */
class GetTarotCardContentUseCase(
    private val tarotRepository: TarotRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(cardId: String, language: String): AynvoraResult<TarotCardContent> {
        val result = tarotRepository.getCardContent(cardId, language)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.TarotContentOpened(cardId, language))
        }
        return result
    }
}
