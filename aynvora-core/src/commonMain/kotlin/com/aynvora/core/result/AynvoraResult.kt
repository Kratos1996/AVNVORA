package com.aynvora.core.result

import com.aynvora.core.models.EngineMetadata
import com.aynvora.core.models.CalculationMetadata

/**
 * Deterministic outcome of an AYNVORA SDK operation.
 * Prevents throwing unhandled exceptions across the SDK boundary.
 */
sealed interface AynvoraResult<out T> {

    /**
     * Successful operation outcome containing calculated or retrieved value and optional engine metadata.
     */
    data class Success<out T>(
        val value: T,
        val metadata: EngineMetadata = EngineMetadata(
            engineVersion = "0.1.0",
            buildNumber = "1",
            isDeterministic = true,
        ),
        /** Calculation conventions and source identity when the value is an astrology result. */
        val calculationMetadata: CalculationMetadata? = null,
    ) : AynvoraResult<T>

    /**
     * Controlled failure outcome categorized by root cause.
     */
    sealed interface Failure : AynvoraResult<Nothing> {
        val message: String

        /**
         * Raised when birth input data violates geographic, temporal, or calendar boundaries.
         */
        data class InvalidInput(
            val field: String,
            override val message: String,
        ) : Failure

        /**
         * Raised when a requested calculation configuration, profile, or convention is unsupported.
         */
        data class UnsupportedConfiguration(
            override val message: String,
        ) : Failure

        /**
         * Raised when calculation fails deterministically within the engine.
         */
        data class CalculationFailure(
            val code: String,
            override val message: String,
        ) : Failure

        /**
         * Raised when a requested persisted entity is not found.
         */
        data class NotFound(
            val resourceId: String,
            override val message: String,
        ) : Failure

        /**
         * Raised when local storage read/write operation fails.
         */
        data class StorageFailure(
            val operation: String,
            override val message: String,
        ) : Failure

        /**
         * Raised when persisted data is corrupted, invalid, or cannot be deserialized.
         */
        data class CorruptedData(
            val resourceId: String,
            override val message: String,
        ) : Failure

        /**
         * Raised when database or storage schema migration fails.
         */
        data class MigrationFailure(
            val fromVersion: Int,
            val toVersion: Int,
            override val message: String,
        ) : Failure

        /**
         * Raised when content synchronization or signature verification fails.
         */
        data class SyncFailure(
            val code: String,
            override val message: String,
            val isRetryable: Boolean = true,
        ) : Failure

        /**
         * Raised when an unexpected internal error occurs, sanitizing sensitive system details.
         */
        data class InternalFailure(
            override val message: String,
        ) : Failure

        /**
         * Structured standardized error for UI presentation and analytical taxonomy.
         */
        data class Structured(
            val error: AynvoraStructuredError,
            override val message: String = error.message,
        ) : Failure
    }

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value
}

inline fun <T> AynvoraResult<T>.onSuccess(action: (value: T) -> Unit): AynvoraResult<T> {
    if (this is AynvoraResult.Success) action(value)
    return this
}

inline fun <T> AynvoraResult<T>.onFailure(action: (failure: AynvoraResult.Failure) -> Unit): AynvoraResult<T> {
    if (this is AynvoraResult.Failure) action(this)
    return this
}

@kotlinx.serialization.Serializable
enum class AynvoraErrorCode {
    INVALID_LOCATION,
    INVALID_DATETIME,
    INVALID_TIMEZONE,
    MISSING_BIRTH_DATA,
    CALCULATION_ERROR,
    STORAGE_ERROR,
    CORRUPTED_DATA,
    UNSUPPORTED_CONFIGURATION,
    NETWORK_ERROR,
    UNKNOWN_ERROR,
}

@kotlinx.serialization.Serializable
data class AynvoraStructuredError(
    val code: AynvoraErrorCode,
    val message: String,
    val details: String? = null,
)
