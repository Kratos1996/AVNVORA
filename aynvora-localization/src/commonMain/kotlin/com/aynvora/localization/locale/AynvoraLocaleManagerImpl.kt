package com.aynvora.localization.locale

import com.aynvora.core.models.UserPreferences
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Production implementation of [AynvoraLocaleManager].
 *
 * Architecture:
 * - Backed by [MutableStateFlow] for reactive Compose integration.
 * - Persists locale via [UserPreferencesRepository] (existing Phase 3 contract).
 * - Uses [Mutex] for thread-safe writes.
 * - Does NOT depend on :astro-engine, :aynvora-data, or any platform UI.
 *
 * Startup flow (call [initialize] once at app start before UI renders):
 * 1. Read persisted [UserPreferences.languageCode].
 * 2. Validate via [LanguageRegistry.isSupported].
 * 3. If supported → use it. If unsupported or error → use [LanguageRegistry.defaultLocale].
 * 4. Emit to [currentLocale] flow.
 *
 * Language-change flow:
 * 1. Acquire mutex.
 * 2. Validate locale.
 * 3. Emit new value to [_currentLocale] (triggers Compose recomposition immediately).
 * 4. Persist to [UserPreferencesRepository].
 * 5. Release mutex.
 *
 * The UI observes [currentLocale] via `collectAsState()`. No restart required.
 */
class AynvoraLocaleManagerImpl(
    private val preferencesRepository: UserPreferencesRepository,
) : AynvoraLocaleManager {

    private val mutex = Mutex()
    private val _currentLocale = MutableStateFlow(LanguageRegistry.defaultLocale())

    override val currentLocale: StateFlow<SupportedLocale> = _currentLocale.asStateFlow()

    /**
     * Initialize the locale from persisted preferences.
     * Must be called once at application startup, before the first UI frame if possible.
     *
     * Failure modes are handled deterministically:
     * - IO error → use default locale, do not crash.
     * - Unsupported persisted locale → use default locale, do not crash.
     * - Blank locale ID → use default locale.
     */
    suspend fun initialize() {
        val resolved = try {
            when (val result = preferencesRepository.getPreferences()) {
                is AynvoraResult.Success -> {
                    val localeId = result.value.languageCode
                    if (localeId.isNotBlank() && LanguageRegistry.isSupported(localeId)) {
                        LanguageRegistry.getLocaleOrDefault(localeId)
                    } else {
                        LanguageRegistry.defaultLocale()
                    }
                }
                is AynvoraResult.Failure -> LanguageRegistry.defaultLocale()
            }
        } catch (_: Exception) {
            LanguageRegistry.defaultLocale()
        }
        _currentLocale.value = resolved
    }

    override suspend fun setLocale(locale: SupportedLocale) {
        val validated = if (locale.isSupported && LanguageRegistry.isSupported(locale.localeId)) {
            locale
        } else {
            LanguageRegistry.defaultLocale()
        }
        mutex.withLock {
            _currentLocale.value = validated
            persistLocale(validated.localeId)
        }
    }

    override suspend fun setLocale(localeId: String) {
        setLocale(LanguageRegistry.getLocaleOrDefault(localeId))
    }

    override suspend fun resetToDefault() {
        setLocale(LanguageRegistry.defaultLocale())
    }

    private suspend fun persistLocale(localeId: String) {
        try {
            when (val current = preferencesRepository.getPreferences()) {
                is AynvoraResult.Success -> {
                    val updated = current.value.copy(languageCode = localeId)
                    preferencesRepository.updatePreferences(updated)
                }
                is AynvoraResult.Failure -> {
                    // Persistence failure is non-fatal: locale is already updated in-memory.
                    // On next startup, it will fall back to default. This is acceptable.
                }
            }
        } catch (_: Exception) {
            // Persistence failure is non-fatal. In-memory state is authoritative.
        }
    }
}
