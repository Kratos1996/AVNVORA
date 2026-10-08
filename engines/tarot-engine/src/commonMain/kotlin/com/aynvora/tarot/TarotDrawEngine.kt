package com.aynvora.tarot

/**
 * Domain engine for executing Tarot card draws.
 *
 * Rules:
 * - Operates entirely locally with zero network calls.
 * - Cards are drawn without replacement (no duplicates within a single standard draw).
 * - Orientation (Upright vs Reversed) is determined deterministically or randomly via [TarotRandomSource].
 * - Completely isolated from astrology calculations, Firebase, and UI.
 */
class TarotDrawEngine(
    private val randomSource: TarotRandomSource = DefaultTarotRandomSource(),
) {

    /**
     * Draws cards for the requested [spread] from [deckCards].
     *
     * @param spread The spread defining positions and number of cards.
     * @param deckCards Available cards to draw from (defaults to full [TarotStandardDeck.AllCards]).
     * @param allowReversed Whether cards can be drawn reversed. Defaults to true.
     * @param timestampEpochMs Timestamp of the reading.
     * @return [TarotReading] containing the drawn cards mapped to their respective positions.
     * @throws IllegalArgumentException if [deckCards] has fewer cards than [spread.cardCount].
     */
    fun drawSpread(
        spread: TarotSpread,
        deckCards: List<TarotCard> = TarotStandardDeck.AllCards,
        allowReversed: Boolean = true,
        deckId: String = TarotStandardDeck.Deck.id,
        timestampEpochMs: Long = 0L,
    ): TarotReading {
        require(deckCards.size >= spread.cardCount) {
            "Deck contains ${deckCards.size} cards, but spread '${spread.id}' requires ${spread.cardCount} cards."
        }

        // Fisher-Yates shuffle using randomSource
        val pool = deckCards.toMutableList()
        val drawn = mutableListOf<TarotCardDraw>()

        for (i in 0 until spread.cardCount) {
            val pickIndex = randomSource.nextInt(pool.size)
            val card = pool.removeAt(pickIndex)
            val orientation = if (allowReversed && randomSource.nextBoolean()) {
                TarotCardOrientation.REVERSED
            } else {
                TarotCardOrientation.UPRIGHT
            }
            drawn.add(
                TarotCardDraw(
                    card = card,
                    orientation = orientation,
                    position = spread.positions[i],
                ),
            )
        }

        val readingId = "tarot_reading_${timestampEpochMs}_${randomSource.nextInt(100_000)}"

        return TarotReading(
            id = readingId,
            spreadId = spread.id,
            deckId = deckId,
            draws = drawn,
            timestampEpochMs = timestampEpochMs,
        )
    }
}
