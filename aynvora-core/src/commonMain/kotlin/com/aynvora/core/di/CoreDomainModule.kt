package com.aynvora.core.di

import com.aynvora.core.Aynvora
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.garudapuran.GetGarudaPuranCatalogUseCase
import com.aynvora.core.garudapuran.GetGarudaPuranContentUseCase
import com.aynvora.core.garudapuran.GetGarudaPuranTopicUseCase
import com.aynvora.core.report.GenerateReportUseCase
import com.aynvora.core.report.PrepareGarudaPuranReportUseCase
import com.aynvora.core.report.ReportGeneratorRegistry
import com.aynvora.core.tarot.DefaultTarotRandomSource
import com.aynvora.core.tarot.GetTarotCardContentUseCase
import com.aynvora.core.tarot.PerformTarotReadingUseCase
import com.aynvora.core.tarot.TarotDrawEngine
import com.aynvora.core.tarot.TarotRandomSource
import com.aynvora.core.usecase.DeleteBirthProfileUseCase
import com.aynvora.core.usecase.DeleteChartUseCase
import com.aynvora.core.usecase.GetUserPreferencesUseCase
import com.aynvora.core.usecase.ObserveBirthProfilesUseCase
import com.aynvora.core.usecase.ObserveSavedChartsUseCase
import com.aynvora.core.usecase.ObserveUserPreferencesUseCase
import com.aynvora.core.usecase.SaveBirthProfileUseCase
import com.aynvora.core.usecase.SaveChartUseCase
import com.aynvora.core.usecase.UpdateUserPreferencesUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module providing Aynvora Domain-layer dependencies:
 * - Public SDK entry point [AynvoraSdk]
 * - Analytics contract [AnalyticsTracker] (defaults to [NoOpAnalyticsTracker] unless overridden)
 * - Domain Use Cases for Profile, Chart, Preference, and Tarot operations
 */
val coreDomainModule: Module = module {
    // Analytics Tracker (can be overridden by platform modules)
    single<AnalyticsTracker> { NoOpAnalyticsTracker() }

    // Core SDK
    single<AynvoraSdk> { Aynvora.create() }

    // Domain Use Cases
    factory { SaveBirthProfileUseCase(get()) }
    factory { ObserveBirthProfilesUseCase(get()) }
    factory { DeleteBirthProfileUseCase(get()) }

    factory { SaveChartUseCase(get()) }
    factory { ObserveSavedChartsUseCase(get()) }
    factory { DeleteChartUseCase(get()) }

    factory { GetUserPreferencesUseCase(get()) }
    factory { ObserveUserPreferencesUseCase(get()) }
    factory { UpdateUserPreferencesUseCase(get()) }

    // Tarot Use Cases & Random Source
    single<TarotRandomSource> { DefaultTarotRandomSource() }
    single { TarotDrawEngine(get()) }
    factory { PerformTarotReadingUseCase(get(), get(), get()) }
    factory { GetTarotCardContentUseCase(get()) }

    // Unified Report Domain (calculation and document generation only; platform renderers stay in UI hosts)
    single { ReportGeneratorRegistry() }
    factory { GenerateReportUseCase(get(), get(), get()) }
    factory { PrepareGarudaPuranReportUseCase(get()) }
    factory { GetGarudaPuranCatalogUseCase(get()) }
    factory { GetGarudaPuranContentUseCase(get()) }
    factory { GetGarudaPuranTopicUseCase(get()) }

    // Core Features Foundation (Phase 7.4)
    single<com.aynvora.core.wallpaper.WallpaperPromptBuilder> { com.aynvora.core.wallpaper.DefaultWallpaperPromptBuilder() }

    single<com.aynvora.core.ai.AiToolRegistry> {
        val registry = com.aynvora.core.ai.DefaultAiToolRegistry()
        registry.registerTool(com.aynvora.core.ai.CalculateBirthChartTool(get()))
        val gitaRepo: com.aynvora.core.gita.GitaRepository? = getOrNull()
        if (gitaRepo != null) {
            registry.registerTool(com.aynvora.core.ai.SearchGitaTool(gitaRepo))
        }
        registry
    }

    // Core Intelligence & Orchestration (Phase 7.5)
    single { com.aynvora.core.intelligence.FeatureCapabilityRegistry }
    single { com.aynvora.core.intelligence.DataSufficiencyValidator() }
    single {
        com.aynvora.core.intelligence.MultiFeatureOrchestrator(
            sdk = get(),
            toolRegistry = get(),
            gitaRepository = getOrNull(),
            garudaPuranRepository = getOrNull(),
            performTarotReadingUseCase = getOrNull(),
            sufficiencyValidator = get(),
            analyticsTracker = getOrNull(),
        )
    }

    // Astrology Prediction Engine (Phase 8.0)
    single {
        com.aynvora.core.astrology.prediction.AstrologyPredictionEngine(
            sdk = get(),
            analyticsTracker = getOrNull(),
        )
    }
}
