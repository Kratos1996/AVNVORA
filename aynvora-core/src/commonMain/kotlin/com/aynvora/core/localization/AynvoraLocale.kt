package com.aynvora.core.localization

/**
 * Canonical locale representation for AYNVORA across all platforms.
 *
 * Designed to support English, Hindi, Arabic (with RTL), and regional Indian languages.
 * Adding a new locale does NOT require modifying domain or feature business logic.
 */
sealed class AynvoraLocale(
    val localeId: String,
    val languageTag: String,
    val isRtl: Boolean = false,
) {
    data object English : AynvoraLocale(localeId = "en", languageTag = "en-IN", isRtl = false)
    data object Hindi : AynvoraLocale(localeId = "hi", languageTag = "hi-IN", isRtl = false)
    data object Arabic : AynvoraLocale(localeId = "ar", languageTag = "ar", isRtl = true)
    data object Bengali : AynvoraLocale(localeId = "bn", languageTag = "bn-IN", isRtl = false)
    data object Gujarati : AynvoraLocale(localeId = "gu", languageTag = "gu-IN", isRtl = false)
    data object Marathi : AynvoraLocale(localeId = "mr", languageTag = "mr-IN", isRtl = false)
    data object Punjabi : AynvoraLocale(localeId = "pa", languageTag = "pa-IN", isRtl = false)
    data object Tamil : AynvoraLocale(localeId = "ta", languageTag = "ta-IN", isRtl = false)
    data object Telugu : AynvoraLocale(localeId = "te", languageTag = "te-IN", isRtl = false)
    data object Kannada : AynvoraLocale(localeId = "kn", languageTag = "kn-IN", isRtl = false)
    data object Malayalam : AynvoraLocale(localeId = "ml", languageTag = "ml-IN", isRtl = false)

    data class Custom(
        val customId: String,
        val customTag: String = customId,
        val customRtl: Boolean = false,
    ) : AynvoraLocale(customId, customTag, customRtl)

    companion object {
        fun fromId(id: String): AynvoraLocale {
            val normalized = id.trim().lowercase()
            return when {
                normalized.startsWith("en") -> English
                normalized.startsWith("hi") -> Hindi
                normalized.startsWith("ar") -> Arabic
                normalized.startsWith("bn") -> Bengali
                normalized.startsWith("gu") -> Gujarati
                normalized.startsWith("mr") -> Marathi
                normalized.startsWith("pa") -> Punjabi
                normalized.startsWith("ta") -> Tamil
                normalized.startsWith("te") -> Telugu
                normalized.startsWith("kn") -> Kannada
                normalized.startsWith("ml") -> Malayalam
                else -> Custom(normalized)
            }
        }
    }
}
