package com.aynvora.data

import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.repository.ContentSyncRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.core.sync.ContentVerifier
import com.aynvora.core.sync.StubContentVerifier
import com.aynvora.data.AynvoraDataFactory.createWithRoom
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.gita.RoomGitaRepository
import com.aynvora.data.repository.BirthProfileRepositoryImpl
import com.aynvora.data.repository.ContentRepositoryImpl
import com.aynvora.data.repository.ContentSyncRepositoryImpl
import com.aynvora.data.repository.RoomSavedChartRepository
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
 * Legacy bundle — profile/preference data only (AynvoraStorageEngine-backed).
 */
class AynvoraRepositories(
    val userProfiles: UserProfileRepository,
    val birthProfiles: BirthProfileRepository,
    val savedCharts: SavedChartRepository,
    val userPreferences: UserPreferencesRepository,
    val storageEngine: AynvoraStorageEngine,
)

/**
 * Full data layer component bundle including Room-backed content repositories.
 *
 * Architecture:
 * - Profile/preference data → [AynvoraStorageEngine] (JSON, mutex-serialized, encrypted)
 * - Content/sync data → Room KMP (reactive Flow, offline-first, transactional)
 *
 * This is the preferred bundle for all platform applications.
 * Use [AynvoraDataFactory.createWithRoom] to obtain an instance.
 */
class AynvoraDataComponents(
    val userProfiles: UserProfileRepository,
    val birthProfiles: BirthProfileRepository,
    val savedCharts: SavedChartRepository,
    val userPreferences: UserPreferencesRepository,
    val contentRepository: ContentRepository,
    val contentSyncRepository: ContentSyncRepository,
    val tarotRepository: com.aynvora.core.tarot.TarotRepository,
    val garudaPuranRepository: GarudaPuranRepository,
    /** Phase 8.3 — Bhagavad Gita offline-first repository backed by Room + gita/gita dataset. */
    val gitaRepository: GitaRepository,
    internal val storageEngine: AynvoraStorageEngine,
)

/**
 * Factory for creating AYNVORA repositories and persistence infrastructure.
 */
object AynvoraDataFactory {

    /**
     * Creates an in-memory repository bundle (primarily for automated testing and preview sessions).
     * Content repositories are not included. Use [createInMemoryWithStubs] for full testing.
     */
    fun createInMemory(
        cipher: StorageCipher = NoOpStorageCipher(),
    ): AynvoraRepositories {
        val driver = InMemoryStorageDriver()
        return create(driver, cipher)
    }

    /**
     * Creates repositories backed by the provided [StorageDriver] and optional [StorageCipher].
     * Does not include Room-backed content repositories.
     * Use [createWithRoom] for production applications.
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

    /**
     * Creates the full [AynvoraDataComponents] bundle with Room-backed content repositories.
     *
     * @param database A fully-initialized [AynvoraDatabase] Room instance.
     *   On Android: `Room.databaseBuilder(context, AynvoraDatabase::class.java, AynvoraDatabase.DATABASE_NAME).build()`
     * @param driver Platform [StorageDriver] for profile/preference JSON storage.
     * @param cipher Optional [StorageCipher] for payload encryption. Defaults to [NoOpStorageCipher].
     * @param migrationRunner Optional [MigrationRunner] for StorageEngine schema migrations.
     * @param contentVerifier Optional [ContentVerifier]. Defaults to [StubContentVerifier].
     *   MUST be replaced with a real verifier before production release.
     */
    fun createWithRoom(
        database: AynvoraDatabase,
        driver: StorageDriver,
        cipher: StorageCipher = NoOpStorageCipher(),
        migrationRunner: MigrationRunner = MigrationRunner(),
        contentVerifier: ContentVerifier = StubContentVerifier(),
    ): AynvoraDataComponents {
        val engine = AynvoraStorageEngine(
            driver = driver,
            cipher = cipher,
            migrationRunner = migrationRunner,
        )
        val contentRepository = ContentRepositoryImpl(
            contentItemDao = database.contentItemDao(),
            contentPackDao = database.contentPackDao(),
        )
        return AynvoraDataComponents(
            userProfiles = UserProfileRepositoryImpl(engine),
            birthProfiles = BirthProfileRepositoryImpl(engine),
            savedCharts = RoomSavedChartRepository(database.savedChartDao()),
            userPreferences = UserPreferencesRepositoryImpl(engine),
            contentRepository = contentRepository,
            contentSyncRepository = ContentSyncRepositoryImpl(
                contentSyncMetadataDao = database.contentSyncMetadataDao(),
            ),
            tarotRepository = com.aynvora.data.tarot.TarotRepositoryImpl(
                tarotDao = database.tarotDao(),
            ),
            garudaPuranRepository = com.aynvora.data.garudapuran.ContentBackedGarudaPuranRepository(
                contentRepository
            ),
            gitaRepository = RoomGitaRepository(
                dao = database.gitaDao(),
            ),
            storageEngine = engine,
        )
    }
}
