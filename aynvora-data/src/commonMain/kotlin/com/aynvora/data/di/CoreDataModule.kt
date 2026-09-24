package com.aynvora.data.di

import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.repository.ContentSyncRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.core.sync.ContentVerifier
import com.aynvora.core.sync.StubContentVerifier
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.repository.BirthProfileRepositoryImpl
import com.aynvora.data.repository.ContentRepositoryImpl
import com.aynvora.data.repository.ContentSyncRepositoryImpl
import com.aynvora.data.repository.SavedChartRepositoryImpl
import com.aynvora.data.repository.UserPreferencesRepositoryImpl
import com.aynvora.data.repository.UserProfileRepositoryImpl
import com.aynvora.data.security.NoOpStorageCipher
import com.aynvora.data.security.StorageCipher
import com.aynvora.data.storage.AynvoraStorageEngine
import com.aynvora.data.storage.InMemoryStorageDriver
import com.aynvora.data.storage.MigrationRunner
import com.aynvora.data.storage.StorageDriver
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module providing Aynvora Data-layer dependencies:
 * - Storage drivers & ciphers
 * - Storage engine
 * - Repository implementations bound to Domain repository contracts
 */
val coreDataModule: Module = module {
    single<StorageDriver> { InMemoryStorageDriver() }
    single<StorageCipher> { NoOpStorageCipher() }
    single<MigrationRunner> { MigrationRunner() }
    single<ContentVerifier> { StubContentVerifier() }

    single {
        AynvoraStorageEngine(
            driver = get(),
            cipher = get(),
            migrationRunner = get(),
        )
    }

    single<UserProfileRepository> { UserProfileRepositoryImpl(get()) }
    single<BirthProfileRepository> { BirthProfileRepositoryImpl(get()) }
    single<SavedChartRepository> { SavedChartRepositoryImpl(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }

    single<ContentRepository> {
        val database: AynvoraDatabase = get()
        ContentRepositoryImpl(
            contentItemDao = database.contentItemDao(),
            contentPackDao = database.contentPackDao(),
        )
    }

    single<ContentSyncRepository> {
        val database: AynvoraDatabase = get()
        ContentSyncRepositoryImpl(
            contentSyncMetadataDao = database.contentSyncMetadataDao(),
        )
    }

    single<TarotRepository> {
        val database: AynvoraDatabase = get()
        com.aynvora.data.tarot.TarotRepositoryImpl(
            tarotDao = database.tarotDao(),
        )
    }

    single<GarudaPuranRepository> {
        com.aynvora.data.garudapuran.ContentBackedGarudaPuranRepository(get())
    }

    single<com.aynvora.core.gita.GitaRepository> {
        com.aynvora.data.gita.InMemoryGitaRepository()
    }
}
