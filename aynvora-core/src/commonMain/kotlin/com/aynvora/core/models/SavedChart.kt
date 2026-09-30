package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Calculation lifecycle status for persisted chart records.
 */
@Serializable
enum class ChartCalculationStatus {
    PENDING,
    COMPLETED,
    FAILED,
}

/**
 * Persisted chart metadata and cache reference.
 *
 * Contains calculation configuration and metadata necessary to reproduce or retrieve
 * calculations deterministically. Does not duplicate astrological calculation formulas.
 */
@Serializable
data class SavedChart(
    val id: String,
    val birthProfileId: String,
    val calculationConfig: CalculationConfig,
    val engineVersion: String,
    val calculationTimestampEpochMs: Long,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val status: ChartCalculationStatus = ChartCalculationStatus.COMPLETED,
    val cachedResultJson: String? = null,
    val snapshotSchemaVersion: String? = null,
    val calculationContractVersion: String? = null,
    val createdAtEpochMs: Long = calculationTimestampEpochMs,
    val updatedAtEpochMs: Long = calculationTimestampEpochMs,
    val lastOpenedAtEpochMs: Long? = null,
    val identityFingerprint: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Chart ID cannot be blank" }
        require(birthProfileId.isNotBlank()) { "Birth profile ID cannot be blank" }
        require(engineVersion.isNotBlank()) { "Engine version cannot be blank" }
        require(calculationTimestampEpochMs > 0) { "Calculation timestamp must be positive, got: $calculationTimestampEpochMs" }
        require(schemaVersion >= 1) { "Schema version must be at least 1, got: $schemaVersion" }
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
