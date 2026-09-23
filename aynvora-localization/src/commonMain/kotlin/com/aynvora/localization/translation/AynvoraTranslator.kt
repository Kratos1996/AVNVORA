package com.aynvora.localization.translation

import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale

/**
 * Centralized in-memory translation catalog.
 *
 * All translations are BUNDLED at compile time — no network calls, no remote service.
 * This satisfies the offline-first requirement.
 */
internal object TranslationCatalog {

    private val tables: Map<String, TranslationTable> = mapOf(
        "en" to EnglishTranslations.table,
        "hi" to HindiTranslations.table,
    )

    fun getTable(localeId: String): TranslationTable? = tables[localeId]

    fun getDefaultTable(): TranslationTable = tables["en"]!!
}

/**
 * Resolves localized strings for a given locale.
 *
 * Fallback chain (per Part T of Phase 4.5 spec):
 * 1. Exact locale table (e.g., "hi")
 * 2. Language fallback (via [SupportedLocale.fallbackLocaleId], e.g., "en")
 * 3. Default locale table ("en")
 * 4. Stable debug key (key itself) — NEVER blank
 *
 * Variable substitution:
 *   translateWithArgs("error.invalid_input", "field" to "month")
 *   → "Invalid input: month"
 *
 * Pluralization:
 *   plural(count, "chart.saved") selects "chart.saved_one" or "chart.saved_other"
 *
 * Rules:
 * - Never returns blank string.
 * - Never silently substitutes an unrelated translation.
 * - In debug builds, missing keys produce "[MISSING: key]" to aid detection.
 */
class AynvoraTranslator(
    private val locale: SupportedLocale,
    private val isDebug: Boolean = false,
) {

    private val primaryTable: TranslationTable? = TranslationCatalog.getTable(locale.localeId)
    private val fallbackTable: TranslationTable? = locale.fallbackLocaleId
        ?.let { TranslationCatalog.getTable(it) }
    private val defaultTable: TranslationTable = TranslationCatalog.getDefaultTable()

    /**
     * Translates a [TranslationKey] to a localized display string.
     * Variable placeholders are NOT substituted here — use [translateWithArgs].
     */
    fun translate(key: TranslationKey): String = resolve(key.key)

    /**
     * Translates a [TranslationKey] and substitutes named variables.
     *
     * Example:
     * ```kotlin
     * translator.translateWithArgs(TranslationKey.Errors.InvalidInput, "field" to "month")
     * // → "Invalid input: month"  (English)
     * // → "अमान्य इनपुट: month"  (Hindi)
     * ```
     */
    fun translateWithArgs(key: TranslationKey, vararg args: Pair<String, String>): String {
        var result = resolve(key.key)
        for ((name, value) in args) {
            result = result.replace("{$name}", value)
        }
        return result
    }

    /**
     * Resolves a plural translation by count.
     *
     * Key convention:
     *   baseKey + "_one"   → singular (count == 1)
     *   baseKey + "_other" → plural   (count != 1)
     *
     * Example:
     * ```kotlin
     * translator.plural(1, "chart.saved")  // → "1 chart saved"
     * translator.plural(3, "chart.saved")  // → "3 charts saved"
     * ```
     */
    fun plural(count: Int, baseKey: String, vararg extraArgs: Pair<String, String>): String {
        val pluralKey = if (count == 1) "${baseKey}_one" else "${baseKey}_other"
        var result = resolve(pluralKey)
        result = result.replace("{count}", count.toString())
        for ((name, value) in extraArgs) {
            result = result.replace("{$name}", value)
        }
        return result
    }

    /**
     * Resolves a raw string key through the fallback chain.
     * Never returns blank string.
     */
    fun resolve(rawKey: String): String {
        // 1. Exact locale
        primaryTable?.get(rawKey)?.let { return it }
        // 2. Language fallback
        fallbackTable?.get(rawKey)?.let { return it }
        // 3. Default locale
        defaultTable.get(rawKey)?.let { return it }
        // 4. Debug/stable key fallback
        return if (isDebug) "[MISSING: $rawKey]" else rawKey
    }
}
