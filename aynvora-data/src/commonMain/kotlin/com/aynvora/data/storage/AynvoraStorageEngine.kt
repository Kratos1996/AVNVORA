package com.aynvora.data.storage

import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.entity.StorageContainerEntity
import com.aynvora.data.security.NoOpStorageCipher
import com.aynvora.data.security.StorageCipher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

/**
 * Multiplatform atomic structured storage engine.
 *
 * Coordinates thread-safe ACID-like atomic persistence, cryptographic encodings,
 * schema version verification, and non-destructive migrations.
 */
class AynvoraStorageEngine(
    private val driver: StorageDriver,
    private val cipher: StorageCipher = NoOpStorageCipher(),
    private val migrationRunner: MigrationRunner = MigrationRunner(),
    private val storageKey: String = DEFAULT_STORAGE_KEY,
) {
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = false
    }

    private var memoryCache: StorageContainerEntity? = null

    /**
     * Reads the top-level persisted container state.
     */
    internal suspend fun readContainer(): AynvoraResult<StorageContainerEntity> = mutex.withLock {
        loadContainerLocked()
    }

    /**
     * Atomically mutates the container state and writes it to persistence.
     */
    internal suspend fun updateContainer(
        transform: (StorageContainerEntity) -> StorageContainerEntity,
    ): AynvoraResult<StorageContainerEntity> = mutex.withLock {
        val currentResult = loadContainerLocked()
        if (currentResult is AynvoraResult.Failure) {
            return@withLock currentResult
        }

        val current = (currentResult as AynvoraResult.Success).value
        try {
            val updated = transform(current)
            val updatedJson = json.encodeToString(StorageContainerEntity.serializer(), updated)
            val encrypted = cipher.encrypt(updatedJson)
            driver.write(storageKey, encrypted)
            memoryCache = updated
            AynvoraResult.Success(updated)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "WRITE",
                message = "Failed to atomically write storage container: ${e.message}",
            )
        }
    }

    private suspend fun loadContainerLocked(): AynvoraResult<StorageContainerEntity> {
        memoryCache?.let { return AynvoraResult.Success(it) }

        val rawEncrypted = try {
            driver.read(storageKey)
        } catch (e: Exception) {
            return AynvoraResult.Failure.StorageFailure(
                operation = "READ",
                message = "Failed to read storage key '$storageKey': ${e.message}",
            )
        }

        if (rawEncrypted == null) {
            // First-time initialization
            val initial = StorageContainerEntity(
                schemaVersion = MigrationRunner.CURRENT_SCHEMA_VERSION,
            )
            val initialJson = json.encodeToString(StorageContainerEntity.serializer(), initial)
            val encrypted = cipher.encrypt(initialJson)
            try {
                driver.write(storageKey, encrypted)
                memoryCache = initial
                return AynvoraResult.Success(initial)
            } catch (e: Exception) {
                return AynvoraResult.Failure.StorageFailure(
                    operation = "INIT",
                    message = "Failed to initialize storage container: ${e.message}",
                )
            }
        }

        val plainJson = try {
            cipher.decrypt(rawEncrypted)
        } catch (e: Exception) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = storageKey,
                message = "Cryptographic payload decryption failed: ${e.message}",
            )
        }

        // Schema version inspection
        val detectedVersion = try {
            val parsedElement = json.parseToJsonElement(plainJson)
            parsedElement.jsonObject["schemaVersion"]?.jsonPrimitive?.intOrNull
                ?: MigrationRunner.CURRENT_SCHEMA_VERSION
        } catch (e: Exception) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = storageKey,
                message = "Invalid JSON structure in storage payload: ${e.message}",
            )
        }

        val migratedJson = try {
            migrationRunner.execute(
                rawPayload = plainJson,
                currentVersion = detectedVersion,
                targetVersion = MigrationRunner.CURRENT_SCHEMA_VERSION,
            )
        } catch (e: IllegalStateException) {
            return AynvoraResult.Failure.MigrationFailure(
                fromVersion = detectedVersion,
                toVersion = MigrationRunner.CURRENT_SCHEMA_VERSION,
                message = "Schema migration failed: ${e.message}",
            )
        } catch (e: Exception) {
            return AynvoraResult.Failure.MigrationFailure(
                fromVersion = detectedVersion,
                toVersion = MigrationRunner.CURRENT_SCHEMA_VERSION,
                message = "Unexpected migration error: ${e.message}",
            )
        }

        val entity = try {
            json.decodeFromString(StorageContainerEntity.serializer(), migratedJson)
        } catch (e: SerializationException) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = storageKey,
                message = "Failed to deserialize storage container: ${e.message}",
            )
        } catch (e: Exception) {
            return AynvoraResult.Failure.CorruptedData(
                resourceId = storageKey,
                message = "Unexpected deserialization failure: ${e.message}",
            )
        }

        memoryCache = entity
        return AynvoraResult.Success(entity)
    }

    /**
     * Clears in-memory cache, forcing next read from storage driver.
     */
    fun invalidateCache() {
        memoryCache = null
    }

    companion object {
        const val DEFAULT_STORAGE_KEY = "aynvora_local_store"
    }
}
