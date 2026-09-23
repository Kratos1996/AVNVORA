package com.aynvora.data

import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.data.repository.BirthProfileRepositoryImpl
import com.aynvora.data.repository.SavedChartRepositoryImpl
import com.aynvora.data.repository.UserPreferencesRepositoryImpl
import com.aynvora.data.repository.UserProfileRepositoryImpl
import com.aynvora.data.security.NoOpStorageCipher
import com.aynvora.data.security.StorageCipher
import com.aynvora.data.storage.AynvoraStorageEngine
import com.aynvora.data.storage.InMemoryStorageDriver
import com.aynvora.data.storage.MigrationRunner
import com.aynvora.data.storage.StorageDriver

/**
 * Bundled persistence repositories providing access to the offline-first data layer.
 */
class AynvoraRepositories(
    val userProfiles: UserProfileRepository,
    val birthProfiles: BirthProfileRepository,
    val savedCharts: SavedChartRepository,
    val userPreferences: UserPreferencesRepository,
    val storageEngine: AynvoraStorageEngine,
)

/**
 * Factory for creating AYNVORA repositories and persistence infrastructure.
 */
object AynvoraDataFactory {

    /**
     * Creates an in-memory repository bundle (primarily for automated testing and preview sessions).
     */
    fun createInMemory(
        cipher: StorageCipher = NoOpStorageCipher(),
    ): AynvoraRepositories {
        val driver = InMemoryStorageDriver()
        return create(driver, cipher)
    }

    /**
     * Creates repositories backed by the provided [StorageDriver] and optional [StorageCipher].
     */
    fun create(
        driver: StorageDriver,
        cipher: StorageCipher = NoOpStorageCipher(),
        migrationRunner: MigrationRunner = MigrationRunner(),
    ): AynvoraRepositories {
        val engine = AynvoraStorageEngine(
            driver = driver,
            cipher = cipher,
            migrationRunner = migrationRunner,
        )
        return AynvoraRepositories(
            userProfiles = UserProfileRepositoryImpl(engine),
            birthProfiles = BirthProfileRepositoryImpl(engine),
            savedCharts = SavedChartRepositoryImpl(engine),
            userPreferences = UserPreferencesRepositoryImpl(engine),
            storageEngine = engine,
        )
    }
}
