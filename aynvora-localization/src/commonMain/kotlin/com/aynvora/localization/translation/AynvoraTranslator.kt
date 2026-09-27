package com.aynvora.localization.translation

import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.LocalizationKey
import com.aynvora.core.localization.LocalizationProvider
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
        "ar" to ArabicTranslations.table,
        "bn" to BengaliTranslations.table,
        "gu" to GujaratiTranslations.table,
        "mr" to MarathiTranslations.table,
        "pa" to PunjabiTranslations.table,
        "ta" to TamilTranslations.table,
        "te" to TeluguTranslations.table,
        "kn" to KannadaTranslations.table,
        "ml" to MalayalamTranslations.table,
    )

    fun getTable(localeId: String): TranslationTable? = tables[localeId]

    fun getDefaultTable(): TranslationTable = tables["en"]!!
}

/**
 * Resolves localized strings for a given locale.
 *
 * Implements [LocalizationProvider] for full architectural decoupling.
 *
 * Fallback chain:
 * 1. Exact locale table (e.g., "ar", "hi")
 * 2. Language fallback (via [SupportedLocale.fallbackLocaleId], e.g., "en")
 * 3. Default locale table ("en")
 * 4. Stable debug key (key itself) — NEVER blank
 *
 * Variable substitution:
 *   get(key, mapOf("field" to "month"))
 *   translateWithArgs(key, "field" to "month")
 *   → "Invalid input: month"
 *
 * Pluralization:
 *   plural(count, "chart.saved") selects "chart.saved_one" or "chart.saved_other"
 */
class AynvoraTranslator(
    val locale: SupportedLocale,
    private val isDebug: Boolean = false,
) : LocalizationProvider {

    private val primaryTable: TranslationTable? = TranslationCatalog.getTable(locale.localeId)
    private val fallbackTable: TranslationTable? = locale.fallbackLocaleId
        ?.let { TranslationCatalog.getTable(it) }
    private val defaultTable: TranslationTable = TranslationCatalog.getDefaultTable()

    /**
     * Resolves localized string for [key] from [LocalizationProvider].
     */
    override fun get(key: LocalizationKey): String = resolve(key.key)

    /**
     * Resolves localized string with interpolated arguments from [LocalizationProvider].
     */
    override fun get(key: LocalizationKey, args: Map<String, Any?>): String {
        var result = resolve(key.key)
        for ((name, value) in args) {
            result = result.replace("{$name}", value?.toString() ?: "")
        }
        return result
    }

    /**
     * Current [AynvoraLocale] representation.
     */
    override fun currentLocale(): AynvoraLocale = locale.aynvoraLocale

    /**
     * Checks whether [locale] is supported in the registry.
     */
    override fun isSupported(locale: AynvoraLocale): Boolean =
        LanguageRegistry.isSupported(locale.localeId)

    /**
     * Translates a [TranslationKey] to a localized display string.
     * Variable placeholders are NOT substituted here — use [translateWithArgs] or [get].
     */
    fun translate(key: TranslationKey): String = resolve(key.key)

    /**
     * Translates a [TranslationKey] and substitutes named variables.
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
     * Resolves a raw string key through the deterministic fallback chain.
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
