package com.aynvora.data.di

import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiModelStorageRepository
import com.aynvora.core.family.FamilyRepository
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.gemstone.GemstoneRepository
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.guidance.DailyGuidanceRepository
import com.aynvora.core.guidance.DefaultDailyGuidanceRepository
import com.aynvora.core.numerology.NumerologyHistoryRepository
import com.aynvora.core.numerology.NumerologyRepository
import com.aynvora.core.palmistry.PalmSessionRepository
import com.aynvora.core.palmistry.PalmistryRepository
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.ContentRepository
import com.aynvora.core.repository.ContentSyncRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.core.sync.ContentVerifier
import com.aynvora.core.sync.StubContentVerifier
import com.aynvora.core.tarot.TarotAssetRepository
import com.aynvora.core.tarot.TarotAssetVerifier
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.core.tarot.TarotSessionRepository
import com.aynvora.data.ai.AiModelStorageRepositoryImpl
import com.aynvora.data.ai.DefaultAiDeviceCapabilityDetector
import com.aynvora.data.database.AynvoraDatabase
import com.aynvora.data.family.FamilyRepositoryImpl
import com.aynvora.data.garudapuran.ContentBackedGarudaPuranRepository
import com.aynvora.data.gemstone.GemstoneRepositoryImpl
import com.aynvora.data.numerology.NumerologyHistoryRepositoryImpl
import com.aynvora.data.numerology.NumerologyRepositoryImpl
import com.aynvora.data.palmistry.DriverPalmSessionStorage
import com.aynvora.data.palmistry.PalmSessionRepositoryImpl
import com.aynvora.data.palmistry.PalmSessionStorage
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
import com.aynvora.data.tarot.DriverTarotSessionStorage
import com.aynvora.data.tarot.TarotAssetRepositoryImpl
import com.aynvora.data.tarot.TarotAssetVerifierImpl
import com.aynvora.data.tarot.TarotRepositoryImpl
import com.aynvora.data.tarot.TarotSessionRepositoryImpl
import com.aynvora.data.tarot.TarotSessionStorage
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
        TarotRepositoryImpl(
            tarotDao = database.tarotDao(),
        )
    }

    single<GarudaPuranRepository> {
        ContentBackedGarudaPuranRepository(get())
    }

    // Phase 8.3 — Bhagavad Gita: Room-backed, offline-first, 701 verses from gita/gita (Public Domain)
    single<GitaRepository> {
        val database: AynvoraDatabase = get()
        com.aynvora.data.gita.RoomGitaRepository(
            dao = database.gitaDao(),
        )
    }

    single<TarotAssetVerifier> {
        TarotAssetVerifierImpl()
    }

    single<TarotAssetRepository> {
        TarotAssetRepositoryImpl(verifier = get())
    }

    // On-Device AI Data & Storage (Phase 8.6)
    single<AiModelStorageRepository> {
        AiModelStorageRepositoryImpl(driver = get())
    }

    single<AiDeviceCapabilityDetector> {
        DefaultAiDeviceCapabilityDetector()
    }

    // Tarot Conversational Session Repository (Phase 8.9)
    single<TarotSessionStorage> {
        DriverTarotSessionStorage(driver = get())
    }

    single<TarotSessionRepository> {
        TarotSessionRepositoryImpl(storage = get())
    }

    // Palmistry Session Repository (Phase 8.10)
    single<PalmSessionStorage> {
        DriverPalmSessionStorage(driver = get())
    }

    single<PalmSessionRepositoryImpl> {
        PalmSessionRepositoryImpl(storage = get())
    }

    single<PalmSessionRepository> {
        get<PalmSessionRepositoryImpl>()
    }

    single<PalmistryRepository> {
        get<PalmSessionRepositoryImpl>()
    }

    // Numerology Domain Repository (Phase 10.0 & 10.4)
    single<NumerologyRepository> {
        NumerologyRepositoryImpl()
    }
    single<NumerologyHistoryRepository> {
        NumerologyHistoryRepositoryImpl(driver = get())
    }

    // Gemstone Domain Repository (Phase 8.2)
    single<GemstoneRepository> {
        GemstoneRepositoryImpl(driver = get())
    }

    // Family Graph & Couple Intelligence Repository
    single<FamilyRepository> {
        FamilyRepositoryImpl(driver = get())
    }

    // Daily Guidance Repository
    single<DailyGuidanceRepository> {
        DefaultDailyGuidanceRepository()
    }
}
