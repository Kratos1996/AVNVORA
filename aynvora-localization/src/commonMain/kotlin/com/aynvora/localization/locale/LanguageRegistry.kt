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

    /** Hindi — regional language. */
    val HINDI = SupportedLocale(
        localeId = "hi",
        languageTag = "hi-IN",
        nativeName = "हिन्दी",
        englishName = "Hindi",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Arabic — international language (RTL layout). */
    val ARABIC = SupportedLocale(
        localeId = "ar",
        languageTag = "ar",
        nativeName = "العربية",
        englishName = "Arabic",
        direction = TextDirection.RTL,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Bengali — Indian regional language. */
    val BENGALI = SupportedLocale(
        localeId = "bn",
        languageTag = "bn-IN",
        nativeName = "বাংলা",
        englishName = "Bengali",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Gujarati — Indian regional language. */
    val GUJARATI = SupportedLocale(
        localeId = "gu",
        languageTag = "gu-IN",
        nativeName = "ગુજરાતી",
        englishName = "Gujarati",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Marathi — Indian regional language. */
    val MARATHI = SupportedLocale(
        localeId = "mr",
        languageTag = "mr-IN",
        nativeName = "मराठी",
        englishName = "Marathi",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Punjabi — Indian regional language. */
    val PUNJABI = SupportedLocale(
        localeId = "pa",
        languageTag = "pa-IN",
        nativeName = "ਪੰਜਾਬੀ",
        englishName = "Punjabi",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Tamil — Indian regional language. */
    val TAMIL = SupportedLocale(
        localeId = "ta",
        languageTag = "ta-IN",
        nativeName = "தமிழ்",
        englishName = "Tamil",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Telugu — Indian regional language. */
    val TELUGU = SupportedLocale(
        localeId = "te",
        languageTag = "te-IN",
        nativeName = "తెలుగు",
        englishName = "Telugu",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Kannada — Indian regional language. */
    val KANNADA = SupportedLocale(
        localeId = "kn",
        languageTag = "kn-IN",
        nativeName = "ಕನ್ನಡ",
        englishName = "Kannada",
        direction = TextDirection.LTR,
        isSupported = true,
        fallbackLocaleId = "en",
    )

    /** Malayalam — Indian regional language. */
    val MALAYALAM = SupportedLocale(
        localeId = "ml",
        languageTag = "ml-IN",
        nativeName = "മലയാളം",
        englishName = "Malayalam",
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
        ARABIC,
        BENGALI,
        GUJARATI,
        MARATHI,
        PUNJABI,
        TAMIL,
        TELUGU,
        KANNADA,
        MALAYALAM,
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
