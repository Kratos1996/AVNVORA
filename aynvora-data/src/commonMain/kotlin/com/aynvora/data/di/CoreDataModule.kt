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
import com.aynvora.data.repository.RoomSavedChartRepository
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
    single<SavedChartRepository> { RoomSavedChartRepository(get<AynvoraDatabase>().savedChartDao()) }
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

    // Phase 8.3 — Bhagavad Gita: Room-backed, offline-first, 701 verses from gita/gita (Public Domain)
    single<com.aynvora.core.gita.GitaRepository> {
        val database: AynvoraDatabase = get()
        com.aynvora.data.gita.RoomGitaRepository(
            dao = database.gitaDao(),
        )
    }

    single<com.aynvora.core.tarot.TarotAssetVerifier> {
        com.aynvora.data.tarot.TarotAssetVerifierImpl()
    }

    single<com.aynvora.core.tarot.TarotAssetRepository> {
        com.aynvora.data.tarot.TarotAssetRepositoryImpl(verifier = get())
    }

    // On-Device AI Data & Storage (Phase 8.6)
    single<com.aynvora.core.ai.AiModelStorageRepository> {
        com.aynvora.data.ai.AiModelStorageRepositoryImpl(driver = get())
    }

    single<com.aynvora.core.ai.AiDeviceCapabilityDetector> {
        com.aynvora.data.ai.DefaultAiDeviceCapabilityDetector()
    }

    // Tarot Conversational Session Repository (Phase 8.9)
    single<com.aynvora.data.tarot.TarotSessionStorage> {
        com.aynvora.data.tarot.DriverTarotSessionStorage(driver = get())
    }

    single<com.aynvora.core.tarot.TarotSessionRepository> {
        com.aynvora.data.tarot.TarotSessionRepositoryImpl(storage = get())
    }

    // Palmistry Session Repository (Phase 8.10)
    single<com.aynvora.data.palmistry.PalmSessionStorage> {
        com.aynvora.data.palmistry.DriverPalmSessionStorage(driver = get())
    }

    single<com.aynvora.data.palmistry.PalmSessionRepositoryImpl> {
        com.aynvora.data.palmistry.PalmSessionRepositoryImpl(storage = get())
    }

    single<com.aynvora.core.palmistry.PalmSessionRepository> {
        get<com.aynvora.data.palmistry.PalmSessionRepositoryImpl>()
    }

    single<com.aynvora.core.palmistry.PalmistryRepository> {
        get<com.aynvora.data.palmistry.PalmSessionRepositoryImpl>()
    }

    // Numerology Domain Repository (Phase 10.0 & 10.4)
    single<com.aynvora.core.numerology.NumerologyRepository> {
        com.aynvora.data.numerology.NumerologyRepositoryImpl()
    }
    single<com.aynvora.core.numerology.NumerologyHistoryRepository> {
        com.aynvora.data.numerology.NumerologyHistoryRepositoryImpl(driver = get())
    }

    // Gemstone Domain Repository (Phase 8.2)
    single<com.aynvora.core.gemstone.GemstoneRepository> {
        com.aynvora.data.gemstone.GemstoneRepositoryImpl(driver = get())
    }
}
