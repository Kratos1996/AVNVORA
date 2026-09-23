package com.aynvora.localization.translation

import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi

/**
 * Stable, typed translation key hierarchy for AYNVORA.
 *
 * RULES:
 * - Never use raw UI strings as translation keys (e.g. Text("Calculate")).
 * - Always resolve through [AynvoraTranslator.translate].
 * - Keys are stable across app versions. Renaming a key is a breaking change.
 * - Keys are language-independent.
 *
 * String key derivation:
 *   TranslationKey.App.AppName   → "app.app_name"
 *   TranslationKey.App.Loading   → "app.loading"
 *   TranslationKey.Astro.Rashi(Rashi.ARIES) → "astro.rashi.aries"
 *
 * Variable placeholders use {variable_name} syntax:
 *   "saved_charts_count" → "Saved {count} charts"
 *
 * Plural keys use _one / _other suffix:
 *   "chart.saved_one"   → "1 chart saved"
 *   "chart.saved_other" → "{count} charts saved"
 */
sealed class TranslationKey {

    /** Derived string key used for catalog lookup. */
    abstract val key: String

    // -------------------------------------------------------------------------
    // App-level strings
    // -------------------------------------------------------------------------
    object App {
        object AppName : TranslationKey() { override val key = "app.app_name" }
        object Loading : TranslationKey() { override val key = "app.loading" }
        object Error : TranslationKey() { override val key = "app.error" }
        object Retry : TranslationKey() { override val key = "app.retry" }
        object Cancel : TranslationKey() { override val key = "app.cancel" }
        object Save : TranslationKey() { override val key = "app.save" }
        object Delete : TranslationKey() { override val key = "app.delete" }
        object Confirm : TranslationKey() { override val key = "app.confirm" }
        object Settings : TranslationKey() { override val key = "app.settings" }
        object Language : TranslationKey() { override val key = "app.language" }
        object SelectLanguage : TranslationKey() { override val key = "app.select_language" }
        object Theme : TranslationKey() { override val key = "app.theme" }
        object Done : TranslationKey() { override val key = "app.done" }
        object Back : TranslationKey() { override val key = "app.back" }
    }

    // -------------------------------------------------------------------------
    // Astrology domain strings (bodies, signs, nakshatras)
    // -------------------------------------------------------------------------
    object Astro {
        /** Localized display name for a Rashi (Zodiac Sign). */
        class RashiName(val rashi: Rashi) : TranslationKey() {
            override val key = "astro.rashi.${rashi.name.lowercase()}"
        }

        /** Localized Sanskrit name for a Rashi. */
        class RashiSanskrit(val rashi: Rashi) : TranslationKey() {
            override val key = "astro.rashi.${rashi.name.lowercase()}.sanskrit"
        }

        /** Localized display name for a Nakshatra. */
        class NakshatraName(val nakshatra: Nakshatra) : TranslationKey() {
            override val key = "astro.nakshatra.${nakshatra.name.lowercase()}"
        }

        /** Localized display name for a celestial body. */
        class BodyName(val body: CelestialBody) : TranslationKey() {
            override val key = "astro.body.${body.name.lowercase()}"
        }

        // Calculation status labels
        object StatusCalculating : TranslationKey() { override val key = "astro.status.calculating" }
        object StatusCalculated : TranslationKey() { override val key = "astro.status.calculated" }
        object StatusError : TranslationKey() { override val key = "astro.status.error" }

        // Retrograde
        object Retrograde : TranslationKey() { override val key = "astro.retrograde" }
        object Direct : TranslationKey() { override val key = "astro.direct" }

        // Ayanamsa labels
        object AyanamsaLahiri : TranslationKey() { override val key = "astro.ayanamsa.lahiri" }
        object AyanamsaTropical : TranslationKey() { override val key = "astro.ayanamsa.tropical" }

        // House system labels
        object HouseEqual : TranslationKey() { override val key = "astro.house.equal" }

        // Calculation profile
        object ProfileStandardVedic : TranslationKey() { override val key = "astro.profile.standard_vedic" }
    }

    // -------------------------------------------------------------------------
    // Error strings
    // -------------------------------------------------------------------------
    object Errors {
        object GenericError : TranslationKey() { override val key = "error.generic" }
        object InvalidInput : TranslationKey() { override val key = "error.invalid_input" }
        object NotFound : TranslationKey() { override val key = "error.not_found" }
        object UnsupportedConfig : TranslationKey() { override val key = "error.unsupported_config" }
        object NetworkUnavailable : TranslationKey() { override val key = "error.network_unavailable" }
    }

    // -------------------------------------------------------------------------
    // Chart / BirthData strings
    // -------------------------------------------------------------------------
    object Chart {
        object Title : TranslationKey() { override val key = "chart.title" }
        object BirthDate : TranslationKey() { override val key = "chart.birth_date" }
        object BirthTime : TranslationKey() { override val key = "chart.birth_time" }
        object BirthPlace : TranslationKey() { override val key = "chart.birth_place" }
        object Calculate : TranslationKey() { override val key = "chart.calculate" }
        object JulianDay : TranslationKey() { override val key = "chart.julian_day" }
        object Ayanamsa : TranslationKey() { override val key = "chart.ayanamsa" }
        object PlanetaryPositions : TranslationKey() { override val key = "chart.planetary_positions" }
        // Plural: "chart.saved_one" / "chart.saved_other"
        object SavedOne : TranslationKey() { override val key = "chart.saved_one" }
        object SavedOther : TranslationKey() { override val key = "chart.saved_other" }
    }

    // -------------------------------------------------------------------------
    // Accessibility strings
    // -------------------------------------------------------------------------
    object Accessibility {
        object LanguageSelectorLabel : TranslationKey() { override val key = "a11y.language_selector" }
        object SelectedLanguage : TranslationKey() { override val key = "a11y.selected_language" }
        object LoadingIndicator : TranslationKey() { override val key = "a11y.loading" }
        object ErrorMessage : TranslationKey() { override val key = "a11y.error_message" }
        object RetrogradeIndicator : TranslationKey() { override val key = "a11y.retrograde" }
    }
}
