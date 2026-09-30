package com.aynvora.data.entity

import kotlinx.serialization.Serializable

/**
 * Storage representation of a user profile.
 *
 * Internal to :aynvora-data to preserve domain/storage separation.
 */
@Serializable
internal data class UserProfileEntity(
    val id: String,
    val displayName: String,
    val contextNotes: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

/**
 * Storage representation of birth profile components.
 */
@Serializable
internal data class BirthProfileEntity(
    val id: String,
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
    val lastOpenedAtEpochMs: Long? = null,
)

/**
 * Storage representation of a persisted chart and calculation metadata.
 */
@Serializable
internal data class SavedChartEntity(
    val id: String,
    val birthProfileId: String,
    val calculationProfile: String,
    val ayanamsa: String,
    val houseSystem: String,
    val engineVersion: String,
    val calculationTimestampEpochMs: Long,
    val schemaVersion: Int,
    val status: String,
    val cachedResultJson: String? = null,
    val snapshotSchemaVersion: String? = null,
    val calculationContractVersion: String? = null,
    val createdAtEpochMs: Long = calculationTimestampEpochMs,
    val updatedAtEpochMs: Long = calculationTimestampEpochMs,
    val lastOpenedAtEpochMs: Long? = null,
    val identityFingerprint: String? = null,
)

/**
 * Storage representation of user preferences.
 */
@Serializable
internal data class UserPreferencesEntity(
    val theme: String,
    val languageCode: String,
    val defaultCalculationProfile: String,
    val defaultAyanamsa: String,
    val defaultHouseSystem: String,
)

/**
 * Top-level container representing the persisted database state.
 */
@Serializable
internal data class StorageContainerEntity(
    val schemaVersion: Int = 1,
    val userProfiles: Map<String, UserProfileEntity> = emptyMap(),
    val birthProfiles: Map<String, BirthProfileEntity> = emptyMap(),
    val savedCharts: Map<String, SavedChartEntity> = emptyMap(),
    val userPreferences: UserPreferencesEntity = UserPreferencesEntity(
        theme = "SYSTEM",
        languageCode = "en",
        defaultCalculationProfile = "STANDARD_VEDIC",
        defaultAyanamsa = "LAHIRI_CHITRAPAKSHA",
        defaultHouseSystem = "EQUAL_HOUSE",
    ),
    val lastUpdatedEpochMs: Long = 0L,
)
