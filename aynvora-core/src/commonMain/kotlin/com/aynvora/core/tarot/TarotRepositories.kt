package com.aynvora.core.tarot

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for Tarot deck and content management.
 *
 * Provides offline-first access to cards and localized reflective content.
 */
interface TarotRepository {

    /**
     * Gets all cards belonging to the standard or specified [deckId].
     */
    suspend fun getDeckCards(deckId: String = TarotStandardDeck.Deck.id): AynvoraResult<List<TarotCard>>

    /**
     * Retrieves localized reflective content for a specific [cardId] in [language].
     */
    suspend fun getCardContent(cardId: String, language: String): AynvoraResult<TarotCardContent>

    /**
     * Observes localized reflective content for all cards in [language].
     */
    fun observeAllCardContent(language: String): Flow<AynvoraResult<List<TarotCardContent>>>

    /**
     * Lists all supported spreads.
     */
    suspend fun getSupportedSpreads(): AynvoraResult<List<TarotSpread>>

    /**
     * Saves a completed reading to local history.
     * Note: Purely local; no private user questions or journals are recorded.
     */
    suspend fun saveReading(reading: TarotReading): AynvoraResult<Unit>

    /**
     * Observes recent local reading history.
     */
    fun observeRecentReadings(limit: Int = 20): Flow<List<TarotReading>>

    /**
     * Deletes a specific reading by [id] from local history.
     */
    suspend fun deleteReading(id: String): AynvoraResult<Unit>

    /**
     * Clears all reading history from local storage.
     */
    suspend fun clearReadingHistory(): AynvoraResult<Unit>

    /**
     * Applies a validated, verified remote Tarot content pack update transactionally.
     * Rejects updates with version <= currently installed version.
     */
    suspend fun updateContentPack(
        language: String,
        newVersion: Int,
        contentList: List<TarotCardContent>,
    ): AynvoraResult<Unit>
}
