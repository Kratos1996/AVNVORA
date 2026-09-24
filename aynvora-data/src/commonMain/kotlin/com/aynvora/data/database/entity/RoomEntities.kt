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
