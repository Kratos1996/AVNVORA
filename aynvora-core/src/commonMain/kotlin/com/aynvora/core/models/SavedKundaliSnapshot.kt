package com.aynvora.core.models

import com.aynvora.core.AynvoraSdk
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@kotlinx.serialization.Serializable
enum class SavedSnapshotOpenStatus { OPEN, MIGRATION_REQUIRED, NO_SNAPSHOT, CORRUPT }

@kotlinx.serialization.Serializable
data class SavedKundaliSnapshot(
    val status: SavedSnapshotOpenStatus,
    val snapshot: KundaliSnapshot? = null,
    val message: String? = null,
)

/** Persists the complete snapshot through the existing SavedChart JSON cache. */
suspend fun AynvoraSdk.persistKundaliSnapshot(
    snapshot: KundaliSnapshot,
    birthProfileId: String,
    chartId: String,
    timestampEpochMs: Long,
): AynvoraResult<SavedChart> {
    val repository = savedCharts
        ?: return AynvoraResult.Failure.UnsupportedConfiguration("Saved chart storage is not configured.")
    return repository.saveChart(
        SavedChart(
            id = chartId,
            birthProfileId = birthProfileId,
            calculationConfig = snapshot.natalChart.config,
            engineVersion = snapshot.calculation.engineVersion,
            calculationTimestampEpochMs = timestampEpochMs,
            cachedResultJson = KundaliSnapshotJson.encode(snapshot),
            snapshotSchemaVersion = snapshot.schemaVersion,
            calculationContractVersion = snapshot.calculation.calculationModel,
        ),
    )
}

/** Opens stored JSON as-is; it never calls an astrology calculation engine. */
suspend fun AynvoraSdk.openSavedKundaliSnapshot(chartId: String): AynvoraResult<SavedKundaliSnapshot> {
    val repository = savedCharts
        ?: return AynvoraResult.Failure.UnsupportedConfiguration("Saved chart storage is not configured.")
    return when (val stored = repository.getSavedChart(chartId)) {
        is AynvoraResult.Failure -> stored
        is AynvoraResult.Success -> {
            val json = stored.value.cachedResultJson
                ?: return AynvoraResult.Success(SavedKundaliSnapshot(SavedSnapshotOpenStatus.NO_SNAPSHOT, message = "No cached Kundali snapshot is stored."))
            try {
                val root = Json.parseToJsonElement(json).jsonObject
                val version = root["schemaVersion"]?.jsonPrimitive?.content
                if (version != KundaliSnapshot.CURRENT_SCHEMA_VERSION) {
                    AynvoraResult.Success(SavedKundaliSnapshot(SavedSnapshotOpenStatus.MIGRATION_REQUIRED, message = "Snapshot schema '$version' requires migration."))
                } else {
                    AynvoraResult.Success(SavedKundaliSnapshot(SavedSnapshotOpenStatus.OPEN, KundaliSnapshotJson.decode(json)))
                }
            } catch (error: Throwable) {
                AynvoraResult.Success(SavedKundaliSnapshot(SavedSnapshotOpenStatus.CORRUPT, message = error.message ?: "Stored snapshot is invalid."))
            }
        }
    }
}
