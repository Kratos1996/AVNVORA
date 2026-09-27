package com.aynvora.data.numerology

import com.aynvora.core.numerology.NumerologyHistoryEntry
import com.aynvora.core.numerology.NumerologyHistoryRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.storage.StorageDriver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * StorageDriver-backed implementation of [NumerologyHistoryRepository].
 *
 * Thread-safe, persistent, offline-first.
 */
class NumerologyHistoryRepositoryImpl(
    private val driver: StorageDriver,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : NumerologyHistoryRepository {

    private val mutex = Mutex()

    companion object {
        private const val NUMEROLOGY_HISTORY_KEY = "aynvora_numerology_history_v1"
        private const val MAX_HISTORY_ITEMS = 50
    }

    override suspend fun save(entry: NumerologyHistoryEntry): AynvoraResult<Unit> = mutex.withLock {
        try {
            val existing = loadEntriesInternal().filter { it.id != entry.id }
            val updated = (listOf(entry) + existing).take(MAX_HISTORY_ITEMS)
            driver.write(NUMEROLOGY_HISTORY_KEY, json.encodeToString(updated))
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                "save",
                "Failed to persist numerology history: ${e.message}"
            )
        }
    }

    override suspend fun getAll(): AynvoraResult<List<NumerologyHistoryEntry>> = mutex.withLock {
        try {
            AynvoraResult.Success(loadEntriesInternal())
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                "read",
                "Failed to read numerology history: ${e.message}"
            )
        }
    }

    override suspend fun clear(): AynvoraResult<Unit> = mutex.withLock {
        try {
            driver.delete(NUMEROLOGY_HISTORY_KEY)
            AynvoraResult.Success(Unit)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                "delete",
                "Failed to clear numerology history: ${e.message}"
            )
        }
    }

    private suspend fun loadEntriesInternal(): List<NumerologyHistoryEntry> {
        val raw = driver.read(NUMEROLOGY_HISTORY_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<NumerologyHistoryEntry>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
