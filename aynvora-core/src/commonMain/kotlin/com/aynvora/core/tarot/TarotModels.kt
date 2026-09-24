package com.aynvora.core.tarot

import kotlinx.serialization.Serializable

/**
 * Orientation of a drawn Tarot card.
 *
 * Explicitly distinguishes Upright vs Reversed perspectives.
 * Free from supernatural claims; strictly models perspective/aspect orientation.
 */
@Serializable
enum class TarotCardOrientation {
    UPRIGHT,
    REVERSED,
}

/**
 * Arcana division of a Tarot card.
 */
@Serializable
enum class TarotArcana {
    MAJOR,
    MINOR,
}

/**
 * Suit for minor arcana cards.
 */
@Serializable
enum class TarotSuit {
    WANDS,
    CUPS,
    SWORDS,
    PENTACLES,
}

/**
 * Canonical domain definition of a Tarot Card.
 *
 * @param id Stable unique identifier (e.g. "major_00_fool", "wands_01_ace").
 * @param number Numeric index / sequence (0..21 for Major Arcana, 1..14 for Minor Arcana).
 * @param name English canonical name (e.g. "The Fool", "Ace of Wands").
 * @param arcana Major or Minor arcana.
 * @param suit Suit if minor arcana, null if major arcana.
 */
@Serializable
data class TarotCard(
    val id: String,
    val number: Int,
    val name: String,
    val arcana: TarotArcana,
    val suit: TarotSuit? = null,
)

/**
 * Canonical domain definition of a Tarot Deck.
 *
 * @param id Stable deck identifier (e.g. "rider_waite_smith_standard").
 * @param name Canonical deck name.
 * @param description Short editorial summary.
 * @param cardCount Total number of cards in the deck (typically 78).
 */
@Serializable
data class TarotDeck(
    val id: String,
    val name: String,
    val description: String,
    val cardCount: Int = 78,
)

/**
 * Position in a Tarot spread.
 *
 * @param id Stable position identifier (e.g. "single_focus", "three_past", "three_present", "three_future").
 * @param orderIndex Display ordering index (0-indexed).
 * @param name Localization translation key or default english label.
 * @param description Localization translation key or reflective description.
 */
@Serializable
data class TarotSpreadPosition(
    val id: String,
    val orderIndex: Int,
    val name: String,
    val description: String,
)

/**
 * Configurable Tarot Spread.
 *
 * @param id Stable spread identifier (e.g. "single_card", "three_card_timeline").
 * @param name Display name.
 * @param description Reflective purpose/focus of the spread.
 * @param positions The ordered positions comprising the spread.
 */
@Serializable
data class TarotSpread(
    val id: String,
    val name: String,
    val description: String,
    val positions: List<TarotSpreadPosition>,
) {
    val cardCount: Int get() = positions.size

    companion object {
        val SingleCard = TarotSpread(
            id = "single_card",
            name = "Single Card Reflection",
            description = "A single card for daily mindfulness, contemplation, and focus.",
            positions = listOf(
                TarotSpreadPosition(
                    id = "single_focus",
                    orderIndex = 0,
                    name = "Current Focus",
                    description = "Theme or perspective for present reflection.",
                ),
            ),
        )

        val ThreeCardTimeline = TarotSpread(
            id = "three_card_timeline",
            name = "Three Card Timeline",
            description = "Three cards exploring foundation, present awareness, and potential direction.",
            positions = listOf(
                TarotSpreadPosition(
                    id = "timeline_past",
                    orderIndex = 0,
                    name = "Foundation / Preceding Influences",
                    description = "Foundational context and past lessons leading to now.",
                ),
                TarotSpreadPosition(
                    id = "timeline_present",
                    orderIndex = 1,
                    name = "Present Awareness",
                    description = "Current dynamics, mindset, and immediate atmosphere.",
                ),
                TarotSpreadPosition(
                    id = "timeline_future",
                    orderIndex = 2,
                    name = "Potential Direction",
                    description = "Constructive perspectives and avenues for thoughtful reflection.",
                ),
            ),
        )

        val StandardSpreads = listOf(SingleCard, ThreeCardTimeline)
    }
}

/**
 * Representation of a single drawn card in a specific position with its orientation.
 */
@Serializable
data class TarotCardDraw(
    val card: TarotCard,
    val orientation: TarotCardOrientation,
    val position: TarotSpreadPosition,
)

/**
 * Complete Tarot Reading result.
 *
 * Purely reflective and contemplation-focused.
 * Never claims supernatural certainty or future prediction.
 */
@Serializable
data class TarotReading(
    val id: String,
    val spreadId: String,
    val deckId: String,
    val draws: List<TarotCardDraw>,
    val timestampEpochMs: Long,
)

/**
 * Presentation-ready reflective content for a Tarot card in a specific language.
 *
 * @param cardId Matches [TarotCard.id].
 * @param language Canonical locale code ("en", "hi").
 * @param title Localized card name.
 * @param shortDescription Concise summary.
 * @param keywords Keywords for quick thematic reflection.
 * @param uprightMeaning Reflective themes when card is drawn upright.
 * @param reversedMeaning Reflective themes when card is drawn reversed.
 * @param sourceAttribution Legal and editorial source reference (e.g. "Public Domain / AYNVORA Original").
 * @param contentVersion Version tracking for updates.
 */
@Serializable
data class TarotCardContent(
    val cardId: String,
    val language: String,
    val title: String,
    val shortDescription: String,
    val keywords: List<String>,
    val uprightMeaning: String,
    val reversedMeaning: String,
    val sourceAttribution: String = "AYNVORA Contemplative Traditions Archive (Public Domain)",
    val contentVersion: Int = 1,
)

/**
 * Explicit non-predictive governance disclosure for Tarot.
 */
object TarotDisclaimer {
    const val ENGLISH_TEXT =
        "Tarot readings in AYNVORA are offered strictly for relaxation, introspection, and personal reflection. " +
                "They do not constitute scientific prediction, medical, legal, or financial advice, nor guarantee future events. " +
                "All interpretations should be considered contemplative perspectives rather than absolute outcomes."

    const val HINDI_TEXT =
        "AYNVORA में टैरो पठन केवल विश्राम, आत्म-चिंतन और व्यक्तिगत चिंतन के उद्देश्य से प्रस्तुत किया गया है। " +
                "यह कोई वैज्ञानिक भविष्यवाणी, चिकित्सा, कानूनी या वित्तीय सलाह नहीं है, और न ही भविष्य की घटनाओं की गारंटी देता है।"
}
