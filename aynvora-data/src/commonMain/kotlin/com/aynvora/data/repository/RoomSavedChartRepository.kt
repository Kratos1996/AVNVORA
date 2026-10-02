package com.aynvora.data.repository

import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CalculationProfile
import com.aynvora.core.models.ChartCalculationStatus
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.SavedChart
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.database.dao.SavedChartDao
import com.aynvora.data.database.entity.SavedChartRoomEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Room-backed chart snapshots. Loading a row never calls an astrology engine. */
class RoomSavedChartRepository(private val dao: SavedChartDao) : SavedChartRepository {
    override suspend fun getSavedChart(id: String): AynvoraResult<SavedChart> = try {
        dao.getById(id)?.toDomain()
            ?.let { AynvoraResult.Success(it) }
            ?: AynvoraResult.Failure.NotFound(id, "SavedChart with id '$id' does not exist")
    } catch (error: Exception) {
        AynvoraResult.Failure.StorageFailure("read_saved_chart", error.message ?: "Could not read saved chart")
    }

    override suspend fun getChartsForBirthProfile(birthProfileId: String): AynvoraResult<List<SavedChart>> = try {
        AynvoraResult.Success(dao.getByBirthProfileId(birthProfileId).map { it.toDomain() })
    } catch (error: Exception) {
        AynvoraResult.Failure.StorageFailure("read_saved_charts", error.message ?: "Could not read saved charts")
    }

    override suspend fun getAllSavedCharts(): AynvoraResult<List<SavedChart>> = try {
        AynvoraResult.Success(dao.getAll().map { it.toDomain() })
    } catch (error: Exception) {
        AynvoraResult.Failure.StorageFailure("read_saved_charts", error.message ?: "Could not read saved charts")
    }

    override suspend fun saveChart(chart: SavedChart): AynvoraResult<SavedChart> = try {
        dao.upsert(chart.toRoomEntity())
        AynvoraResult.Success(chart)
    } catch (error: Exception) {
        AynvoraResult.Failure.StorageFailure("save_chart", error.message ?: "Could not save chart")
    }

    override suspend fun deleteChart(id: String): AynvoraResult<Unit> = try {
        dao.deleteById(id)
        AynvoraResult.Success(Unit)
    } catch (error: Exception) {
        AynvoraResult.Failure.StorageFailure("delete_chart", error.message ?: "Could not delete chart")
    }

    override fun observeChartsForBirthProfile(birthProfileId: String): Flow<AynvoraResult<List<SavedChart>>> =
        dao.observeByBirthProfileId(birthProfileId)
            .map { rows -> AynvoraResult.Success(rows.map { it.toDomain() }) as AynvoraResult<List<SavedChart>> }
            .catch { emit(AynvoraResult.Failure.StorageFailure("observe_saved_charts", it.message ?: "Could not observe charts")) }
}

private fun SavedChartRoomEntity.toDomain() = SavedChart(
    id = id,
    birthProfileId = birthProfileId,
    calculationConfig = CalculationConfig(
        profile = CalculationProfile.valueOf(calculationProfile),
        ayanamsa = AyanamsaConvention.valueOf(ayanamsa),
        houseSystem = HouseSystem.valueOf(houseSystem),
    ),
    engineVersion = engineVersion,
    calculationTimestampEpochMs = calculationTimestampEpochMs,
    schemaVersion = schemaVersion,
    status = ChartCalculationStatus.valueOf(status),
    cachedResultJson = cachedResultJson,
    snapshotSchemaVersion = snapshotSchemaVersion,
    calculationContractVersion = calculationContractVersion,
    createdAtEpochMs = createdAtEpochMs.takeIf { it > 0 } ?: calculationTimestampEpochMs,
    updatedAtEpochMs = updatedAtEpochMs.takeIf { it > 0 } ?: calculationTimestampEpochMs,
    lastOpenedAtEpochMs = lastOpenedAtEpochMs,
    identityFingerprint = identityFingerprint,
)

private fun SavedChart.toRoomEntity() = SavedChartRoomEntity(
    id = id,
    birthProfileId = birthProfileId,
    calculationProfile = calculationConfig.profile.name,
    ayanamsa = calculationConfig.ayanamsa.name,
    houseSystem = calculationConfig.houseSystem.name,
    engineVersion = engineVersion,
    calculationTimestampEpochMs = calculationTimestampEpochMs,
    schemaVersion = schemaVersion,
    status = status.name,
    cachedResultJson = cachedResultJson,
    snapshotSchemaVersion = snapshotSchemaVersion,
    calculationContractVersion = calculationContractVersion,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
    lastOpenedAtEpochMs = lastOpenedAtEpochMs,
    identityFingerprint = identityFingerprint,
)
