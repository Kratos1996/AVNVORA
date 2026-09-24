package com.aynvora.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a user profile.
 */
@Entity(tableName = "user_profiles")
data class UserProfileRoomEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val contextNotes: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

/**
 * Room entity representing reusable birth profile details.
 */
@Entity(tableName = "birth_profiles")
data class BirthProfileRoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val country: String? = null,
    val placeId: String? = null,
    val notes: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

/**
 * Room entity representing a persisted astrological chart with calculation cache.
 */
@Entity(
    tableName = "saved_charts",
    indices = [
        Index("birthProfileId"),
    ],
)
data class SavedChartRoomEntity(
    @PrimaryKey val id: String,
    val birthProfileId: String,
    val calculationProfile: String,
    val ayanamsa: String,
    val houseSystem: String,
    val engineVersion: String,
    val calculationTimestampEpochMs: Long,
    val schemaVersion: Int,
    val status: String,
    val cachedResultJson: String? = null,
)

/**
 * Room entity representing user preferences.
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesRoomEntity(
    @PrimaryKey val id: String = "default_preferences",
    val theme: String,
    val languageCode: String,
    val defaultCalculationProfile: String,
    val defaultAyanamsa: String,
    val defaultHouseSystem: String,
)

/**
 * Room entity representing a downloaded content pack.
 */
@Entity(
    tableName = "content_packs",
    indices = [
        Index("moduleId"),
        Index("language"),
    ],
)
data class ContentPackRoomEntity(
    @PrimaryKey val packId: String,
    val moduleId: String,
    val contentVersion: Int,
    val language: String,
    val title: String,
    val description: String? = null,
    val sourceAttribution: String,
    val trustState: String,
    val checksumSha256: String,
    val installedAtEpochMs: Long,
    val lastSyncEpochMs: Long,
)

/**
 * Room entity representing an individual content item (card, chapter, reference).
 */
@Entity(
    tableName = "content_items",
    indices = [
        Index("packId"),
        Index("moduleId"),
        Index("language"),
        Index(value = ["packId", "itemKey"], unique = true),
    ],
)
data class ContentItemRoomEntity(
    @PrimaryKey val id: String,
    val packId: String,
    val moduleId: String,
    val itemKey: String,
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val language: String,
    val metadataJson: String? = null,
    val orderIndex: Int = 0,
    val tagsCsv: String = "",
    val trustState: String,
)

/**
 * Room entity storing synchronization state and metadata.
 */
@Entity(tableName = "content_sync_metadata")
data class ContentSyncMetadataRoomEntity(
    @PrimaryKey val id: String = "global_sync_state",
    val lastSyncTimestampEpochMs: Long,
    val lastSyncStatus: String,
    val lastSyncError: String? = null,
    val activeManifestVersion: Int,
    val etag: String? = null,
)

/**
 * Room entity representing a Tarot Deck.
 */
@Entity(tableName = "tarot_decks")
data class TarotDeckRoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val cardCount: Int,
)

/**
 * Room entity representing a Tarot Card definition.
 */
@Entity(
    tableName = "tarot_cards",
    indices = [
        Index("deckId"),
        Index("arcana"),
    ],
)
data class TarotCardRoomEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val number: Int,
    val name: String,
    val arcana: String,
    val suit: String? = null,
)

/**
 * Room entity representing localized reflective content for a Tarot Card.
 */
@Entity(
    tableName = "tarot_card_content",
    indices = [
        Index("cardId"),
        Index("language"),
        Index(value = ["cardId", "language"], unique = true),
    ],
)
data class TarotCardContentRoomEntity(
    @PrimaryKey val id: String, // composite "${cardId}_${language}"
    val cardId: String,
    val language: String,
    val title: String,
    val shortDescription: String,
    val keywordsCsv: String,
    val uprightMeaning: String,
    val reversedMeaning: String,
    val sourceAttribution: String,
    val contentVersion: Int,
)

/**
 * Room entity storing local Tarot reading history.
 *
 * Privacy rule: strictly stores card IDs, orientations, and spread positions.
 * Never stores personal user questions, journal entries, or private notes.
 */
@Entity(
    tableName = "tarot_reading_history",
    indices = [
        Index("timestampEpochMs"),
    ],
)
data class TarotReadingHistoryRoomEntity(
    @PrimaryKey val id: String,
    val spreadId: String,
    val deckId: String,
    val serializedDrawsJson: String,
    val timestampEpochMs: Long,
)
