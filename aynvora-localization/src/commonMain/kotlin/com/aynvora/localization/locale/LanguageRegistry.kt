package com.aynvora.localization.locale

/**
 * Centralized registry of all locales supported by AYNVORA.
 *
 * RULES:
 * - Do NOT duplicate locale lists in individual screens or components.
 * - Consume this registry everywhere a locale list is needed.
 * - New languages are added here only; no other module changes required.
 * - The Astro Engine is never modified to add a language.
 *
 * Current release: English, Hindi.
 */
object LanguageRegistry {

    /** English (India variant) — default locale for the application. */
    val ENGLISH = SupportedLocale(
        localeId = "en",
        languageTag = "en-IN",
        nativeName = "English",
        englishName = "English",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = null,
    )

    /** Hindi — first regional language. */
    val HINDI = SupportedLocale(
        localeId = "hi",
        languageTag = "hi-IN",
        nativeName = "हिन्दी",
        englishName = "Hindi",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /**
     * All currently supported locales, in display order.
     * Only fully supported locales are included; partial/draft locales are excluded.
     */
    private val allLocales: List<SupportedLocale> = listOf(
        ENGLISH,
        HINDI,
    )

    /**
     * Returns all fully supported locales available for user selection.
     */
    fun availableLocales(): List<SupportedLocale> = allLocales.filter { it.isSupported }

    /**
     * The default application locale (English).
     * Used as the ultimate fallback when a persisted locale cannot be resolved.
     */
    fun defaultLocale(): SupportedLocale = ENGLISH

    /**
     * Looks up a locale by its canonical [localeId].
     *
     * @return The matching [SupportedLocale], or null if not found.
     */
    fun getLocale(localeId: String): SupportedLocale? =
        allLocales.firstOrNull { it.localeId == localeId }

    /**
     * Looks up a locale by its canonical [localeId], falling back to [defaultLocale] if not found
     * or if the found locale is not fully supported.
     */
    fun getLocaleOrDefault(localeId: String): SupportedLocale =
        getLocale(localeId)?.takeIf { it.isSupported } ?: defaultLocale()

    /**
     * Returns whether [localeId] maps to a currently fully supported locale.
     */
    fun isSupported(localeId: String): Boolean =
        allLocales.any { it.localeId == localeId && it.isSupported }

    /**
     * Resolves the fallback locale for a given locale.
     * Walks up the fallback chain until finding a supported locale or reaching the default.
     */
    fun resolveFallback(locale: SupportedLocale): SupportedLocale {
        val fallbackId = locale.fallbackLocaleId ?: return defaultLocale()
        val fallback = getLocale(fallbackId) ?: return defaultLocale()
        return if (fallback.isSupported) fallback else defaultLocale()
    }
}
