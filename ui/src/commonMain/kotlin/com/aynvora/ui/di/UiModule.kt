package com.aynvora.ui.di

import com.aynvora.core.di.coreDomainModule
import com.aynvora.data.di.coreDataModule
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.AynvoraLocaleManagerImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module aggregating UI and core application dependencies for Compose Multiplatform.
 */
val uiModule: Module = module {
    // UI viewmodels and navigation-level factories can be registered here
}

/**
 * Localization module — provides [AynvoraLocaleManager] as a singleton.
 *
 * Depends on [coreDataModule] for [com.aynvora.core.repository.UserPreferencesRepository].
 * The manager is initialized lazily; Compose observes [AynvoraLocaleManager.currentLocale]
 * via [collectAsState] — no Activity restart needed on language change.
 */
val localizationModule: Module = module {
    single<AynvoraLocaleManager> { AynvoraLocaleManagerImpl(preferencesRepository = get()) }
}

/**
 * Complete Koin modules collection for AYNVORA multiplatform application bootstrap.
 */
val aynvoraAppModules: List<Module> = listOf(
    coreDomainModule,
    coreDataModule,
    localizationModule,
    uiModule,
)
