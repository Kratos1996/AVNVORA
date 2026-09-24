package com.aynvora.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.aynvora.data.database.entity.BirthProfileRoomEntity
import com.aynvora.data.database.entity.ContentItemRoomEntity
import com.aynvora.data.database.entity.ContentPackRoomEntity
import com.aynvora.data.database.entity.ContentSyncMetadataRoomEntity
import com.aynvora.data.database.entity.SavedChartRoomEntity
import com.aynvora.data.database.entity.UserPreferencesRoomEntity
import com.aynvora.data.database.entity.UserProfileRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: String): UserProfileRoomEntity?

    @Query("SELECT * FROM user_profiles ORDER BY updatedAtEpochMs DESC")
    suspend fun getAll(): List<UserProfileRoomEntity>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun observeById(id: String): Flow<UserProfileRoomEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<UserProfileRoomEntity>>

    @Upsert
    suspend fun upsert(entity: UserProfileRoomEntity)

    @Upsert
    suspend fun upsertAll(entities: List<UserProfileRoomEntity>)

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface BirthProfileDao {
    @Query("SELECT * FROM birth_profiles WHERE id = :id")
    suspend fun getById(id: String): BirthProfileRoomEntity?

    @Query("SELECT * FROM birth_profiles ORDER BY updatedAtEpochMs DESC")
    suspend fun getAll(): List<BirthProfileRoomEntity>

    @Query("SELECT * FROM birth_profiles WHERE id = :id")
    fun observeById(id: String): Flow<BirthProfileRoomEntity?>

    @Query("SELECT * FROM birth_profiles ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<BirthProfileRoomEntity>>

    @Upsert
    suspend fun upsert(entity: BirthProfileRoomEntity)

    @Upsert
    suspend fun upsertAll(entities: List<BirthProfileRoomEntity>)

    @Query("DELETE FROM birth_profiles WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SavedChartDao {
    @Query("SELECT * FROM saved_charts WHERE id = :id")
    suspend fun getById(id: String): SavedChartRoomEntity?

    @Query("SELECT * FROM saved_charts WHERE birthProfileId = :birthProfileId ORDER BY calculationTimestampEpochMs DESC")
    suspend fun getByBirthProfileId(birthProfileId: String): List<SavedChartRoomEntity>

    @Query("SELECT * FROM saved_charts ORDER BY calculationTimestampEpochMs DESC")
    suspend fun getAll(): List<SavedChartRoomEntity>

    @Query("SELECT * FROM saved_charts WHERE birthProfileId = :birthProfileId ORDER BY calculationTimestampEpochMs DESC")
    fun observeByBirthProfileId(birthProfileId: String): Flow<List<SavedChartRoomEntity>>

    @Upsert
    suspend fun upsert(entity: SavedChartRoomEntity)

    @Upsert
    suspend fun upsertAll(entities: List<SavedChartRoomEntity>)

    @Query("DELETE FROM saved_charts WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = :id LIMIT 1")
    suspend fun getById(id: String = "default_preferences"): UserPreferencesRoomEntity?

    @Query("SELECT * FROM user_preferences WHERE id = :id LIMIT 1")
    fun observeById(id: String = "default_preferences"): Flow<UserPreferencesRoomEntity?>

    @Upsert
    suspend fun upsert(entity: UserPreferencesRoomEntity)
}

@Dao
interface ContentPackDao {
    @Query("SELECT * FROM content_packs WHERE packId = :packId")
    suspend fun getById(packId: String): ContentPackRoomEntity?

    @Query("SELECT * FROM content_packs ORDER BY installedAtEpochMs DESC")
    suspend fun getAll(): List<ContentPackRoomEntity>

    @Query("SELECT * FROM content_packs WHERE moduleId = :moduleId ORDER BY installedAtEpochMs DESC")
    suspend fun getByModule(moduleId: String): List<ContentPackRoomEntity>

    @Upsert
    suspend fun upsert(entity: ContentPackRoomEntity)

    @Query("DELETE FROM content_packs WHERE packId = :packId")
    suspend fun deleteById(packId: String)
}

@Dao
interface ContentItemDao {
    @Query("SELECT * FROM content_items WHERE id = :id AND trustState = 'APPROVED_FOR_PUBLICATION'")
    suspend fun getApprovedById(id: String): ContentItemRoomEntity?

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getById(id: String): ContentItemRoomEntity?

    @Query("SELECT * FROM content_items WHERE moduleId = :moduleId AND language = :language AND trustState = 'APPROVED_FOR_PUBLICATION' ORDER BY orderIndex ASC")
    fun observeApprovedItems(moduleId: String, language: String): Flow<List<ContentItemRoomEntity>>

    @Query("SELECT * FROM content_items WHERE id = :id AND trustState = 'APPROVED_FOR_PUBLICATION'")
    fun observeApprovedItemById(id: String): Flow<ContentItemRoomEntity?>

    @Query("SELECT * FROM content_items WHERE packId = :packId ORDER BY orderIndex ASC")
    suspend fun getItemsForPack(packId: String): List<ContentItemRoomEntity>

    @Upsert
    suspend fun upsertAll(entities: List<ContentItemRoomEntity>)

    @Query("DELETE FROM content_items WHERE packId = :packId")
    suspend fun deleteByPackId(packId: String)

    @Transaction
    suspend fun replacePackItems(packId: String, items: List<ContentItemRoomEntity>) {
        deleteByPackId(packId)
        upsertAll(items)
    }
}

@Dao
interface ContentSyncMetadataDao {
    @Query("SELECT * FROM content_sync_metadata WHERE id = :id LIMIT 1")
    suspend fun getById(id: String = "global_sync_state"): ContentSyncMetadataRoomEntity?

    @Query("SELECT * FROM content_sync_metadata WHERE id = :id LIMIT 1")
    fun observeById(id: String = "global_sync_state"): Flow<ContentSyncMetadataRoomEntity?>

    @Upsert
    suspend fun upsert(entity: ContentSyncMetadataRoomEntity)
}

@Dao
interface TarotDao {
    // Tarot Decks
    @Query("SELECT * FROM tarot_decks WHERE id = :id")
    suspend fun getDeckById(id: String): com.aynvora.data.database.entity.TarotDeckRoomEntity?

    @Query("SELECT * FROM tarot_decks")
    suspend fun getAllDecks(): List<com.aynvora.data.database.entity.TarotDeckRoomEntity>

    @Upsert
    suspend fun upsertDeck(entity: com.aynvora.data.database.entity.TarotDeckRoomEntity)

    // Tarot Cards
    @Query("SELECT * FROM tarot_cards WHERE deckId = :deckId ORDER BY number ASC")
    suspend fun getCardsForDeck(deckId: String): List<com.aynvora.data.database.entity.TarotCardRoomEntity>

    @Query("SELECT * FROM tarot_cards WHERE id = :id")
    suspend fun getCardById(id: String): com.aynvora.data.database.entity.TarotCardRoomEntity?

    @Upsert
    suspend fun upsertCards(entities: List<com.aynvora.data.database.entity.TarotCardRoomEntity>)

    // Tarot Card Content
    @Query("SELECT * FROM tarot_card_content WHERE cardId = :cardId AND language = :language LIMIT 1")
    suspend fun getContentByCardAndLanguage(
        cardId: String,
        language: String
    ): com.aynvora.data.database.entity.TarotCardContentRoomEntity?

    @Query("SELECT * FROM tarot_card_content WHERE language = :language")
    fun observeContentByLanguage(language: String): Flow<List<com.aynvora.data.database.entity.TarotCardContentRoomEntity>>

    @Upsert
    suspend fun upsertCardContentList(entities: List<com.aynvora.data.database.entity.TarotCardContentRoomEntity>)

    @Query("SELECT COUNT(*) FROM tarot_cards WHERE deckId = :deckId")
    suspend fun getCardCount(deckId: String = "rider_waite_smith_standard"): Int

    @Query("SELECT COUNT(*) FROM tarot_card_content WHERE language = :language")
    suspend fun getContentCount(language: String): Int

    @Query("SELECT * FROM tarot_card_content WHERE language = :language")
    suspend fun getAllContentForLanguage(language: String): List<com.aynvora.data.database.entity.TarotCardContentRoomEntity>

    // Reading History
    @Upsert
    suspend fun insertReading(entity: com.aynvora.data.database.entity.TarotReadingHistoryRoomEntity)

    @Query("SELECT * FROM tarot_reading_history ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeRecentReadings(limit: Int): Flow<List<com.aynvora.data.database.entity.TarotReadingHistoryRoomEntity>>

    @Query("DELETE FROM tarot_reading_history WHERE id = :id")
    suspend fun deleteReadingById(id: String)

    @Query("DELETE FROM tarot_reading_history")
    suspend fun clearHistory()
}
