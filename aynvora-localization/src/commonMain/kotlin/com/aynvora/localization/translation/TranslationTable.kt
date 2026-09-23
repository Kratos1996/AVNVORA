package com.aynvora.localization.translation

/**
 * A flat map of stable string keys → localized display strings for one locale.
 *
 * Variable placeholder syntax: {variable_name}
 * Example: "Saved {count} charts" — resolved by [AynvoraTranslator.translateWithArgs].
 *
 * Plural keys use suffix convention:
 *   "chart.saved_one"   → singular
 *   "chart.saved_other" → plural (default)
 *
 * @param localeId  Canonical locale identifier (matches [SupportedLocale.localeId]).
 * @param entries   Immutable map of key → localized string.
 */
data class TranslationTable(
    val localeId: String,
    val entries: Map<String, String>,
) {
    /**
     * Looks up a translation by key.
     * @return The localized string, or null if the key is not present.
     */
    fun get(key: String): String? = entries[key]

    /**
     * Returns all registered keys. Useful for test validation.
     */
    fun keys(): Set<String> = entries.keys
}
