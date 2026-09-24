package com.aynvora.ui.di

import com.aynvora.core.di.coreDomainModule
import com.aynvora.data.di.coreDataModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module aggregating UI and core application dependencies for Compose Multiplatform.
 */
val uiModule: Module = module {
    // UI viewmodels and navigation-level factories can be registered here
}

/**
 * Complete Koin modules collection for AYNVORA multiplatform application bootstrap.
 */
val aynvoraAppModules: List<Module> = listOf(
    coreDomainModule,
    coreDataModule,
    uiModule,
)
