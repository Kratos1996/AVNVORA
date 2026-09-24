package com.aynvora.data.tarot

import com.aynvora.core.tarot.TarotCardContent

/**
 * Verified starter reflective content for standard Tarot cards.
 *
 * Provides presentation-ready content in English and Hindi for all 78 standard cards.
 * Formatted respectfully for mindfulness and self-reflection; free from supernatural claims.
 */
object TarotStarterContent {

    val EnglishCards: List<TarotCardContent> get() = TarotCompleteContentPack.EnglishCards

    val HindiCards: List<TarotCardContent> get() = TarotCompleteContentPack.HindiCards

    val AllStarterContent: List<TarotCardContent> get() = TarotCompleteContentPack.AllCards
}
