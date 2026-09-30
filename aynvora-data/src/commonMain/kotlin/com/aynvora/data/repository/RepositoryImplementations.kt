package com.aynvora.data.repository

import com.aynvora.core.models.BirthProfile
import com.aynvora.core.models.SavedChart
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.models.UserProfile
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.mapper.toDomain
import com.aynvora.data.mapper.toEntity
import com.aynvora.data.storage.AynvoraStorageEngine
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

/**
 * Concrete implementation of [UserProfileRepository] delegating to [AynvoraStorageEngine].
 */
class UserProfileRepositoryImpl(
    private val storageEngine: AynvoraStorageEngine,
) : UserProfileRepository {

    private val changeEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getProfile(id: String): AynvoraResult<UserProfile> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        val entity = container.userProfiles[id]
            ?: return AynvoraResult.Failure.NotFound(
                resourceId = id,
                message = "UserProfile with id '$id' does not exist",
            )

        return try {
            AynvoraResult.Success(entity.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = id,
                message = "Failed to deserialize UserProfile '$id': ${e.message}",
            )
        }
    }

    override suspend fun getAllProfiles(): AynvoraResult<List<UserProfile>> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        return try {
            val list = container.userProfiles.values.map { it.toDomain() }
            AynvoraResult.Success(list)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = "user_profiles",
                message = "Failed to deserialize user profile list: ${e.message}",
            )
        }
    }

    override suspend fun saveProfile(profile: UserProfile): AynvoraResult<UserProfile> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                userProfiles = container.userProfiles + (profile.id to profile.toEntity()),
                lastUpdatedEpochMs = profile.updatedAtEpochMs,
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(profile)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override suspend fun deleteProfile(id: String): AynvoraResult<Unit> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                userProfiles = container.userProfiles - id,
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(Unit)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override fun observeProfile(id: String): Flow<AynvoraResult<UserProfile>> = flow {
        emit(getProfile(id))
        changeEvents.collect {
            emit(getProfile(id))
        }
    }
}

/**
 * Concrete implementation of [BirthProfileRepository] delegating to [AynvoraStorageEngine].
 */
class BirthProfileRepositoryImpl(
    private val storageEngine: AynvoraStorageEngine,
) : BirthProfileRepository {

    private val changeEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getBirthProfile(id: String): AynvoraResult<BirthProfile> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        val entity = container.birthProfiles[id]
            ?: return AynvoraResult.Failure.NotFound(
                resourceId = id,
                message = "BirthProfile with id '$id' does not exist",
            )

        return try {
            AynvoraResult.Success(entity.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = id,
                message = "Failed to deserialize BirthProfile '$id': ${e.message}",
            )
        }
    }

    override suspend fun getAllBirthProfiles(): AynvoraResult<List<BirthProfile>> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        return try {
            val list = container.birthProfiles.values.map { it.toDomain() }
                .sortedWith(compareByDescending<BirthProfile> { it.lastOpenedAtEpochMs ?: it.updatedAtEpochMs }.thenBy { it.id })
            AynvoraResult.Success(list)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = "birth_profiles",
                message = "Failed to deserialize birth profile list: ${e.message}",
            )
        }
    }

    override suspend fun saveBirthProfile(profile: BirthProfile): AynvoraResult<BirthProfile> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                birthProfiles = container.birthProfiles + (profile.id to profile.toEntity()),
                lastUpdatedEpochMs = profile.updatedAtEpochMs,
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(profile)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override suspend fun deleteBirthProfile(id: String): AynvoraResult<Unit> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                birthProfiles = container.birthProfiles - id,
                savedCharts = container.savedCharts.filterValues { it.birthProfileId != id },
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(Unit)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override fun observeAllBirthProfiles(): Flow<AynvoraResult<List<BirthProfile>>> = flow {
        emit(getAllBirthProfiles())
        changeEvents.collect {
            emit(getAllBirthProfiles())
        }
    }

    override fun observeBirthProfile(id: String): Flow<AynvoraResult<BirthProfile>> = flow {
        emit(getBirthProfile(id))
        changeEvents.collect {
            emit(getBirthProfile(id))
        }
    }
}

/**
 * Concrete implementation of [SavedChartRepository] delegating to [AynvoraStorageEngine].
 */
class SavedChartRepositoryImpl(
    private val storageEngine: AynvoraStorageEngine,
) : SavedChartRepository {

    private val changeEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getSavedChart(id: String): AynvoraResult<SavedChart> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        val entity = container.savedCharts[id]
            ?: return AynvoraResult.Failure.NotFound(
                resourceId = id,
                message = "SavedChart with id '$id' does not exist",
            )

        return try {
            AynvoraResult.Success(entity.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = id,
                message = "Failed to deserialize SavedChart '$id': ${e.message}",
            )
        }
    }

    override suspend fun getChartsForBirthProfile(birthProfileId: String): AynvoraResult<List<SavedChart>> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        return try {
            val list = container.savedCharts.values
                .filter { it.birthProfileId == birthProfileId }
                .map { it.toDomain() }
            AynvoraResult.Success(list)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = "saved_charts",
                message = "Failed to deserialize chart list for birth profile '$birthProfileId': ${e.message}",
            )
        }
    }

    override suspend fun getAllSavedCharts(): AynvoraResult<List<SavedChart>> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        return try {
            val list = container.savedCharts.values.map { it.toDomain() }
            AynvoraResult.Success(list)
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = "saved_charts",
                message = "Failed to deserialize saved chart list: ${e.message}",
            )
        }
    }

    override suspend fun saveChart(chart: SavedChart): AynvoraResult<SavedChart> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                savedCharts = container.savedCharts + (chart.id to chart.toEntity()),
                lastUpdatedEpochMs = chart.calculationTimestampEpochMs,
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(chart)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override suspend fun deleteChart(id: String): AynvoraResult<Unit> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                savedCharts = container.savedCharts - id,
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(Unit)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override fun observeChartsForBirthProfile(birthProfileId: String): Flow<AynvoraResult<List<SavedChart>>> = flow {
        emit(getChartsForBirthProfile(birthProfileId))
        changeEvents.collect {
            emit(getChartsForBirthProfile(birthProfileId))
        }
    }
}

/**
 * Concrete implementation of [UserPreferencesRepository] delegating to [AynvoraStorageEngine].
 */
class UserPreferencesRepositoryImpl(
    private val storageEngine: AynvoraStorageEngine,
) : UserPreferencesRepository {

    private val changeEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun getPreferences(): AynvoraResult<UserPreferences> {
        val containerResult = storageEngine.readContainer()
        if (containerResult is AynvoraResult.Failure) return containerResult

        val container = (containerResult as AynvoraResult.Success).value
        return try {
            AynvoraResult.Success(container.userPreferences.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.CorruptedData(
                resourceId = "user_preferences",
                message = "Failed to deserialize user preferences: ${e.message}",
            )
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences): AynvoraResult<UserPreferences> {
        val updateResult = storageEngine.updateContainer { container ->
            container.copy(
                userPreferences = preferences.toEntity(),
            )
        }

        return when (updateResult) {
            is AynvoraResult.Success -> {
                changeEvents.tryEmit(Unit)
                AynvoraResult.Success(preferences)
            }
            is AynvoraResult.Failure -> updateResult
        }
    }

    override fun observePreferences(): Flow<AynvoraResult<UserPreferences>> = flow {
        emit(getPreferences())
        changeEvents.collect {
            emit(getPreferences())
        }
    }
}
