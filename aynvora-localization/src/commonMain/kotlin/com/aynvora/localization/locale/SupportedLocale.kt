package com.aynvora.localization.locale

import com.aynvora.core.localization.AynvoraLocale
import kotlinx.serialization.Serializable

/**
 * Layout direction for a locale.
 *
 * Used by Compose UI to set [androidx.compose.ui.unit.LayoutDirection] reactively
 * when the locale changes, without requiring any platform-specific code.
 */
@Serializable
enum class TextDirection {
    /** Left-to-right (Latin, Devanagari, etc.) */
    LTR,

    /** Right-to-left (Arabic, Hebrew, etc.) */
    RTL,
}

/**
 * Immutable, stable representation of a locale supported by AYNVORA.
 *
 * Do NOT scatter raw locale strings ("en", "hi") across the application.
 * Always resolve through [LanguageRegistry] and pass [SupportedLocale] values.
 *
 * Canonical locale IDs:
 *   - "en"  → English   (en-IN)
 *   - "hi"  → Hindi     (hi-IN)
 *   - "ar"  → Arabic    (ar) — RTL
 *   - "bn"  → Bengali   (bn-IN)
 *   - "gu"  → Gujarati  (gu-IN)
 *   - "mr"  → Marathi   (mr-IN)
 *   - "pa"  → Punjabi   (pa-IN)
 *   - "ta"  → Tamil     (ta-IN)
 *   - "te"  → Telugu    (te-IN)
 *   - "kn"  → Kannada   (kn-IN)
 *   - "ml"  → Malayalam (ml-IN)
 *
 * @param localeId       Short canonical identifier. Used as persistence key.
 * @param languageTag    BCP-47 language tag. Used for platform locale APIs.
 * @param nativeName     Name of the language in that language itself.
 * @param englishName    Name of the language in English.
 * @param direction      Text and layout direction.
 * @param isSupported    Whether this locale is fully supported in the current release.
 * @param fallbackLocaleId Locale to fall back to when a translation key is missing.
 */
@Serializable
data class SupportedLocale(
    val localeId: String,
    val languageTag: String,
    val nativeName: String,
    val englishName: String,
    val direction: TextDirection,
    val isSupported: Boolean = true,
    val fallbackLocaleId: String? = null,
) {
    init {
        require(localeId.isNotBlank()) { "localeId must not be blank" }
        require(languageTag.isNotBlank()) { "languageTag must not be blank" }
        require(nativeName.isNotBlank()) { "nativeName must not be blank" }
        require(englishName.isNotBlank()) { "englishName must not be blank" }
    }

    /** Whether this locale uses right-to-left text direction. */
    val isRtl: Boolean get() = direction == TextDirection.RTL

    /** The canonical [AynvoraLocale] representation. */
    val aynvoraLocale: AynvoraLocale get() = AynvoraLocale.fromId(localeId)
}
