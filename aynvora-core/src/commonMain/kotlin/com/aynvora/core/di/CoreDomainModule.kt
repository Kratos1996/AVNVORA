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
    single<AynvoraSdk> { Aynvora.create(numerology = getOrNull()) }

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
    single<com.aynvora.core.tarot.TarotExplanationEngine> {
        com.aynvora.core.tarot.GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = get(),
            deterministicEngine = com.aynvora.core.tarot.DeterministicTarotExplanationEngine(),
        )
    }

    // Tarot Conversational Experience (Phase 8.9)
    single<com.aynvora.core.tarot.TarotClock> { com.aynvora.core.tarot.SystemTarotClock() }
    single {
        com.aynvora.core.tarot.TarotReadingAvailabilityPolicy(
            sessionRepository = get(),
            clock = get(),
        )
    }
    single {
        com.aynvora.core.tarot.TarotQuestionEngine(
            explanationEngine = get(),
            drawEngine = get(),
            clock = get(),
            localizationProvider = getOrNull(),
        )
    }


    // On-Device AI Core Services (Phase 8.6 & 8.8 Native Runtime)
    single { com.aynvora.core.ai.AiModelSelector() }
    single<com.aynvora.core.ai.AiInferenceEngine> { com.aynvora.core.ai.LocalNativeInferenceEngine() }
    single { com.aynvora.core.ai.AiModelLifecycleManager(get()) }

    // Unified On-Device Intelligence Platform (Centralized Local Intelligence)
    single<com.aynvora.core.ai.LocalAiRuntime> {
        com.aynvora.core.ai.DefaultLocalAiRuntime(
            inferenceEngine = get()
        )
    }
    single { com.aynvora.core.ai.AynvoraAiOutputValidator() }
    single<com.aynvora.core.ai.AynvoraLocalIntelligence> {
        com.aynvora.core.ai.DefaultAynvoraLocalIntelligence(
            runtime = get(),
            lifecycleManager = get(),
            outputValidator = get(),
        )
    }
    single { com.aynvora.core.ai.gita.GitaReflectionPipeline(localIntelligence = get()) }
    single { com.aynvora.core.ai.adapters.AstrologyAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.GitaAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.TarotAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.NumerologyAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.PalmistryAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.GemstoneAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.GarudaPuranAiAdapter(get()) }
    single { com.aynvora.core.ai.adapters.CrossFeatureReflectionAdapter(get()) }


    // Unified Report Domain (calculation and document generation only; platform renderers stay in UI hosts)
    single { ReportGeneratorRegistry() }
    factory { GenerateReportUseCase(get(), get(), get()) }
    factory { PrepareGarudaPuranReportUseCase(get()) }
    factory { com.aynvora.core.report.PrepareTarotReportUseCase(get(), get()) }
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

    // Palmistry Domain Services (Phase 8.10)
    single { com.aynvora.core.palmistry.PalmImageAnalysisEngine() }
    single { com.aynvora.core.palmistry.PalmistryFeatureDataConnector() }
    single<com.aynvora.core.palmistry.PalmistryExplanationEngine> {
        com.aynvora.core.palmistry.GroundedSlmPalmistryExplanationEngine(
            aiInferenceEngine = get(),
            deterministicEngine = com.aynvora.core.palmistry.DeterministicPalmistryExplanationEngine(),
        )
    }
    single { com.aynvora.core.palmistry.PalmQuestionEngine(explanationEngine = get()) }

    // Gemstone Domain Services (Phase 8.2)
    single<com.aynvora.core.gemstone.CertificateImageAnalyzer> {
        com.aynvora.core.gemstone.DefaultCertificateImageAnalyzer()
    }

    // Unified Event SDK (Phase 9.0)
    single<com.aynvora.core.event.AynvoraEventGuard> { com.aynvora.core.event.StandardAynvoraEventGuard() }
    single<com.aynvora.core.event.AynvoraEventDeduplicator> { com.aynvora.core.event.DefaultAynvoraEventDeduplicator() }
    single<com.aynvora.core.event.AynvoraEventAnalyticsMapper> { com.aynvora.core.event.DefaultAynvoraEventAnalyticsMapper() }
    single { com.aynvora.core.event.AynvoraEventAnalyticsBridge(get(), get()) }
    single<com.aynvora.core.event.AynvoraEventDispatcher> {
        val dispatcher = com.aynvora.core.event.DefaultAynvoraEventDispatcher(get(), get())
        val bridge: com.aynvora.core.event.AynvoraEventAnalyticsBridge = get()
        dispatcher.addObserver(bridge)
        dispatcher
    }
}
