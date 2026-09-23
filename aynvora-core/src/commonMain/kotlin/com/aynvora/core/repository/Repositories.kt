package com.aynvora.core.repository

import com.aynvora.core.models.BirthProfile
import com.aynvora.core.models.SavedChart
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.models.UserProfile
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for managing user profile persistence.
 */
interface UserProfileRepository {
    suspend fun getProfile(id: String): AynvoraResult<UserProfile>
    suspend fun getAllProfiles(): AynvoraResult<List<UserProfile>>
    suspend fun saveProfile(profile: UserProfile): AynvoraResult<UserProfile>
    suspend fun deleteProfile(id: String): AynvoraResult<Unit>
    fun observeProfile(id: String): Flow<AynvoraResult<UserProfile>>
}

/**
 * Repository contract for managing reusable birth profile persistence.
 */
interface BirthProfileRepository {
    suspend fun getBirthProfile(id: String): AynvoraResult<BirthProfile>
    suspend fun getAllBirthProfiles(): AynvoraResult<List<BirthProfile>>
    suspend fun saveBirthProfile(profile: BirthProfile): AynvoraResult<BirthProfile>
    suspend fun deleteBirthProfile(id: String): AynvoraResult<Unit>
    fun observeAllBirthProfiles(): Flow<AynvoraResult<List<BirthProfile>>>
    fun observeBirthProfile(id: String): Flow<AynvoraResult<BirthProfile>>
}

/**
 * Repository contract for managing persisted chart metadata and calculation cache.
 */
interface SavedChartRepository {
    suspend fun getSavedChart(id: String): AynvoraResult<SavedChart>
    suspend fun getChartsForBirthProfile(birthProfileId: String): AynvoraResult<List<SavedChart>>
    suspend fun getAllSavedCharts(): AynvoraResult<List<SavedChart>>
    suspend fun saveChart(chart: SavedChart): AynvoraResult<SavedChart>
    suspend fun deleteChart(id: String): AynvoraResult<Unit>
    fun observeChartsForBirthProfile(birthProfileId: String): Flow<AynvoraResult<List<SavedChart>>>
}

/**
 * Repository contract for managing user and application preferences.
 */
interface UserPreferencesRepository {
    suspend fun getPreferences(): AynvoraResult<UserPreferences>
    suspend fun updatePreferences(preferences: UserPreferences): AynvoraResult<UserPreferences>
    fun observePreferences(): Flow<AynvoraResult<UserPreferences>>
}
