package com.aynvora.core.localization

/**
 * Common localization provider contract for AYNVORA.
 *
 * Responsibilities:
 * 1. Resolve current active locale.
 * 2. Retrieve translation by [LocalizationKey].
 * 3. Perform safe template interpolation without regex hazards.
 * 4. Fallback deterministically (requested -> fallback -> English -> key).
 * 5. Never crash on missing keys.
 * 6. Never expose private user data to analytics.
 */
interface LocalizationProvider {

    /**
     * Resolves the localized display string for [key].
     */
    fun get(key: LocalizationKey): String

    /**
     * Resolves the localized display string for [key] with interpolated named arguments.
     * Placeholders in the template must use {variable_name} syntax.
     */
    fun get(key: LocalizationKey, args: Map<String, Any?>): String

    /**
     * The currently active [AynvoraLocale].
     */
    fun currentLocale(): AynvoraLocale

    /**
     * Checks if [locale] is supported by this provider.
     */
    fun isSupported(locale: AynvoraLocale): Boolean
}

/**
 * Convenience extension for vararg pair interpolation.
 *
 * Example:
 * ```kotlin
 * localization.get(LocalizationKey.PalmObservationTitle, "hand" to handName, "items" to items)
 * ```
 */
fun LocalizationProvider.get(key: LocalizationKey, vararg args: Pair<String, Any?>): String =
    get(key, args.toMap())

/**
 * Deterministic default in-memory provider used when no external provider is supplied.
 * Always resolves using English fallback or returns key to avoid any NPE or crash.
 */
class FallbackLocalizationProvider(
    private val locale: AynvoraLocale = AynvoraLocale.English,
    private val translations: Map<String, String> = emptyMap(),
) : LocalizationProvider {

    override fun get(key: LocalizationKey): String =
        translations[key.key] ?: key.key

    override fun get(key: LocalizationKey, args: Map<String, Any?>): String {
        var template = get(key)
        for ((k, v) in args) {
            template = template.replace("{$k}", v?.toString() ?: "")
        }
        return template
    }

    override fun currentLocale(): AynvoraLocale = locale

    override fun isSupported(locale: AynvoraLocale): Boolean = true
}
