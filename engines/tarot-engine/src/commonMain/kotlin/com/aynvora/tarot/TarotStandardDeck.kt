package com.aynvora.tarot

/**
 * Standard 78-card Tarot Deck definition according to public domain tradition.
 *
 * Consists of 22 Major Arcana cards (0..21) and 56 Minor Arcana cards
 * (4 suits: Wands, Cups, Swords, Pentacles; 14 cards per suit: Ace..10, Page, Knight, Queen, King).
 */
object TarotStandardDeck {

    val Deck = TarotDeck(
        id = "rider_waite_smith_standard",
        name = "Standard Reflective Deck",
        description = "Classic 78-card contemplative archetype collection.",
        cardCount = 78,
    )

    val MajorArcana: List<TarotCard> = listOf(
        TarotCard("major_00_fool", 0, "The Fool", TarotArcana.MAJOR),
        TarotCard("major_01_magician", 1, "The Magician", TarotArcana.MAJOR),
        TarotCard("major_02_high_priestess", 2, "The High Priestess", TarotArcana.MAJOR),
        TarotCard("major_03_empress", 3, "The Empress", TarotArcana.MAJOR),
        TarotCard("major_04_emperor", 4, "The Emperor", TarotArcana.MAJOR),
        TarotCard("major_05_hierophant", 5, "The Hierophant", TarotArcana.MAJOR),
        TarotCard("major_06_lovers", 6, "The Lovers", TarotArcana.MAJOR),
        TarotCard("major_07_chariot", 7, "The Chariot", TarotArcana.MAJOR),
        TarotCard("major_08_strength", 8, "Strength", TarotArcana.MAJOR),
        TarotCard("major_09_hermit", 9, "The Hermit", TarotArcana.MAJOR),
        TarotCard("major_10_wheel_of_fortune", 10, "Wheel of Fortune", TarotArcana.MAJOR),
        TarotCard("major_11_justice", 11, "Justice", TarotArcana.MAJOR),
        TarotCard("major_12_hanged_man", 12, "The Hanged Man", TarotArcana.MAJOR),
        TarotCard(
            "major_13_death",
            13,
            "Transformation",
            TarotArcana.MAJOR
        ), // Framing as transformation/renewal
        TarotCard("major_14_temperance", 14, "Temperance", TarotArcana.MAJOR),
        TarotCard("major_15_devil", 15, "Shadow / Attachment", TarotArcana.MAJOR),
        TarotCard("major_16_tower", 16, "Sudden Awakening", TarotArcana.MAJOR),
        TarotCard("major_17_star", 17, "The Star", TarotArcana.MAJOR),
        TarotCard("major_18_moon", 18, "The Moon", TarotArcana.MAJOR),
        TarotCard("major_19_sun", 19, "The Sun", TarotArcana.MAJOR),
        TarotCard("major_20_judgement", 20, "Reckoning & Calling", TarotArcana.MAJOR),
        TarotCard("major_21_world", 21, "The World", TarotArcana.MAJOR),
    )

    private fun suitCards(suit: TarotSuit, suitPrefix: String, suitName: String): List<TarotCard> {
        val court = listOf(
            11 to "Page of $suitName",
            12 to "Knight of $suitName",
            13 to "Queen of $suitName",
            14 to "King of $suitName",
        )
        val pips = (1..10).map { num ->
            val rankName = if (num == 1) "Ace" else num.toString()
            num to "$rankName of $suitName"
        }
        return (pips + court).map { (num, name) ->
            TarotCard(
                id = "${suitPrefix}_${num.toString().padStart(2, '0')}",
                number = num,
                name = name,
                arcana = TarotArcana.MINOR,
                suit = suit,
            )
        }
    }

    val Wands: List<TarotCard> = suitCards(TarotSuit.WANDS, "wands", "Wands")
    val Cups: List<TarotCard> = suitCards(TarotSuit.CUPS, "cups", "Cups")
    val Swords: List<TarotCard> = suitCards(TarotSuit.SWORDS, "swords", "Swords")
    val Pentacles: List<TarotCard> = suitCards(TarotSuit.PENTACLES, "pentacles", "Pentacles")

    val AllCards: List<TarotCard> = MajorArcana + Wands + Cups + Swords + Pentacles
}
