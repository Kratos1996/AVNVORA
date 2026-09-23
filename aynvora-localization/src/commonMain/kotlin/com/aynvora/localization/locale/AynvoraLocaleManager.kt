package com.aynvora.localization.locale

import kotlinx.coroutines.flow.StateFlow

/**
 * Runtime locale manager contract for AYNVORA.
 *
 * Exposes an immutable [StateFlow] of the currently active [SupportedLocale].
 * Compose UI collects this flow via [collectAsState()] to recompose reactively
 * when the language changes — WITHOUT requiring an Activity restart, process recreation,
 * or any application restart.
 *
 * Usage from Compose:
 * ```kotlin
 * val locale by localeManager.currentLocale.collectAsState()
 * ```
 *
 * Contract:
 * - [currentLocale] is always a fully supported, non-null locale.
 * - [setLocale] validates against [LanguageRegistry] before applying.
 * - Unsupported locale IDs fall back deterministically to [LanguageRegistry.defaultLocale].
 * - Changes are persisted immediately; no network call is made.
 * - All public members are safe to call from any coroutine context.
 */
interface AynvoraLocaleManager {

    /**
     * Immutable view of the currently active locale.
     * Emits a new value immediately when [setLocale] or [resetToDefault] is called.
     */
    val currentLocale: StateFlow<SupportedLocale>

    /**
     * Changes the active locale to [locale].
     *
     * Steps:
     * 1. Validates that [locale] is supported.
     * 2. Updates [currentLocale] flow immediately.
     * 3. Persists the locale ID via [com.aynvora.core.repository.UserPreferencesRepository].
     *
     * If [locale] is not supported, falls back to [LanguageRegistry.defaultLocale].
     */
    suspend fun setLocale(locale: SupportedLocale)

    /**
     * Changes the active locale by [localeId] string.
     * Resolves via [LanguageRegistry.getLocaleOrDefault].
     */
    suspend fun setLocale(localeId: String)

    /**
     * Resets the locale to [LanguageRegistry.defaultLocale] and persists the change.
     */
    suspend fun resetToDefault()
}
