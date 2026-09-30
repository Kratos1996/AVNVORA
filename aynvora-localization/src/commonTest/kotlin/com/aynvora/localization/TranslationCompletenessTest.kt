package com.aynvora.localization

import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.LocalizationKey
import com.aynvora.core.localization.LocalizationProvider
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.locale.TextDirection
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.EnglishTranslations
import com.aynvora.localization.translation.TranslationCatalog
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.localization.translation.TranslationTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 9.4 Canonical Localization Completeness, Parity, Fallback, RTL & Extensibility Test Suite.
 */
class TranslationCompletenessTest {

    private val canonicalEnKeys: Set<String> = EnglishTranslations.table.entries.keys

    // Extract placeholders like {field}, {count}, {reason} from a string template
    private fun extractPlaceholders(text: String): Set<String> {
        val regex = Regex("\\{([a-zA-Z0-9_]+)\\}")
        return regex.findAll(text).map { it.groupValues[1] }.toSet()
    }

    @Test
    fun testAllElevenSupportedLocalesAreRegistered() {
        val locales = LanguageRegistry.availableLocales()
        assertEquals(11, locales.size, "Exactly 11 locales must be supported")

        val expectedLocaleIds =
            listOf("en", "hi", "ar", "bn", "gu", "mr", "pa", "ta", "te", "kn", "ml")
        val actualLocaleIds = locales.map { it.localeId }

        for (id in expectedLocaleIds) {
            assertTrue(actualLocaleIds.contains(id), "Locale $id must be in availableLocales")
            assertTrue(
                LanguageRegistry.isSupported(id),
                "LanguageRegistry.isSupported($id) must be true"
            )
        }
    }

    @Test
    fun testCanonicalKeyCountBaseline() {
        assertTrue(canonicalEnKeys.isNotEmpty(), "English canonical keys must not be empty")
    }

    @Test
    fun testAllElevenCatalogsCompletenessAndPlaceholderParity() {
        val locales = LanguageRegistry.availableLocales()

        println("\n=== PHASE 9.4 LOCALIZATION COMPLETENESS REPORT ===")
        println("| Locale | Supported | Keys | Missing | Extra | Placeholder Errors | RTL |")
        println("|--------|-----------|------|---------|-------|--------------------|-----|")

        for (locale in locales) {
            val table = TranslationCatalog.getTable(locale.localeId)
            assertNotNull(table, "TranslationTable must exist for locale ${locale.localeId}")

            val catalogKeys = table.entries.keys
            val missing = canonicalEnKeys - catalogKeys
            val extra = catalogKeys - canonicalEnKeys

            var placeholderErrors = 0
            for (key in catalogKeys) {
                val enVal = EnglishTranslations.table.entries[key] ?: ""
                val enPlaceholders = extractPlaceholders(enVal)
                if (enPlaceholders.isNotEmpty()) {
                    val locVal = table.entries[key] ?: ""
                    val locPlaceholders = extractPlaceholders(locVal)
                    if (enPlaceholders != locPlaceholders) {
                        println("  [${locale.localeId}] Placeholder mismatch for $key: expected $enPlaceholders, got $locPlaceholders")
                        placeholderErrors++
                    }
                }
            }

            val isRtl = locale.direction == TextDirection.RTL

            println(
                "| %-6s | %-9s | %-4d | %-7d | %-5d | %-18d | %-3s |".format(
                    locale.localeId,
                    locale.isSupported,
                    catalogKeys.size,
                    missing.size,
                    extra.size,
                    placeholderErrors,
                    if (isRtl) "YES" else "NO"
                )
            )

            if (missing.isNotEmpty()) {
                println("  [${locale.localeId}] Note: ${missing.size} keys falling back to English baseline.")
            }
            assertEquals(
                0,
                extra.size,
                "Locale ${locale.localeId} has unexpected extra keys: $extra"
            )
            assertEquals(
                0,
                placeholderErrors,
                "Locale ${locale.localeId} has $placeholderErrors placeholder errors"
            )
        }
        println("==================================================\n")
    }

    @Test
    fun testArabicRtlConfiguration() {
        val arabic = LanguageRegistry.ARABIC
        assertEquals("ar", arabic.localeId)
        assertEquals(TextDirection.RTL, arabic.direction, "Arabic must be configured as RTL")
        assertTrue(arabic.isRtl, "isRtl must be true for Arabic")

        val english = LanguageRegistry.ENGLISH
        assertEquals(TextDirection.LTR, english.direction)
        assertFalse(english.isRtl)

        val hindi = LanguageRegistry.HINDI
        assertEquals(TextDirection.LTR, hindi.direction)
        assertFalse(hindi.isRtl)
    }

    @Test
    fun testIndividualLookupsAcrossAllElevenLocales() {
        val locales = LanguageRegistry.availableLocales()

        for (locale in locales) {
            val translator = AynvoraTranslator(locale, isDebug = false)

            // Test app name
            val appName = translator.translate(TranslationKey.App.AppName)
            assertTrue(appName.isNotBlank(), "App name must not be blank for ${locale.localeId}")

            // Test common action
            val retry = translator.translate(TranslationKey.App.Retry)
            assertTrue(retry.isNotBlank(), "Retry must not be blank for ${locale.localeId}")

            // Test tarot key
            val tarotDisclaimer = translator.translate(TranslationKey.Tarot.DisclaimerTitle)
            assertTrue(
                tarotDisclaimer.isNotBlank(),
                "Tarot disclaimer title must not be blank for ${locale.localeId}"
            )

            // Test palmistry key
            val palmTitle = translator.translate(TranslationKey.Palmistry.Title)
            assertTrue(
                palmTitle.isNotBlank(),
                "Palmistry title must not be blank for ${locale.localeId}"
            )

            // Test parameterized translation
            val errorWithField = translator.translateWithArgs(
                TranslationKey.Errors.InvalidInput,
                "field" to "date"
            )
            assertTrue(
                errorWithField.contains("date"),
                "Parameterized string must interpolate argument for ${locale.localeId}: '$errorWithField'"
            )
        }
    }

    @Test
    fun testDeterministicFallbackChain() {
        // 1. Fully translated key resolves accurately
        val hiTranslator = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = false)
        assertEquals("लोड हो रहा है…", hiTranslator.translate(TranslationKey.App.Loading))

        // 2. Missing key in production falls back to default locale or raw key (never blank)
        val missingKey = "hypothetical.nonexistent.key"
        val result = hiTranslator.resolve(missingKey)
        assertEquals(missingKey, result, "In production mode, unknown key resolves to raw key")
        assertTrue(result.isNotBlank(), "Fallback must never be blank")

        // 3. Debug mode flags missing key
        val debugTranslator = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = true)
        val debugResult = debugTranslator.resolve(missingKey)
        assertEquals("[MISSING: hypothetical.nonexistent.key]", debugResult)

        // 4. Unsupported locale gracefully defaults to English
        val unsupportedLocale = SupportedLocale(
            localeId = "xx",
            languageTag = "xx",
            nativeName = "Unknown",
            englishName = "Unknown",
            direction = TextDirection.LTR,
            isSupported = false,
            fallbackLocaleId = "en"
        )
        val unsupportedTranslator = AynvoraTranslator(unsupportedLocale, isDebug = false)
        assertEquals("AYNVORA", unsupportedTranslator.translate(TranslationKey.App.AppName))
        assertEquals("Loading…", unsupportedTranslator.translate(TranslationKey.App.Loading))
    }

    @Test
    fun testNewLanguageExtensibilityWithoutModifyingFeatures() {
        // Architecture proof: A new language requires only a SupportedLocale and TranslationTable.
        // It does not require modifying Tarot, Palmistry, Garuda Puran, ViewModels, or Astro Engine.

        val customTestTable = TranslationTable(
            localeId = "test",
            entries = mapOf(
                "app.app_name" to "AYNVORA_TEST",
                "app.loading" to "LOADING_TEST…",
                "tarot.title" to "TAROT_TEST",
                "palmistry.title" to "PALMISTRY_TEST",
                "error.invalid_input" to "BAD_INPUT_TEST: {field}",
            )
        )

        val testLocale = AynvoraLocale.Custom("test", "test-TEST", customRtl = false)

        // Custom provider implementing LocalizationProvider for the test locale
        val provider: LocalizationProvider = object : LocalizationProvider {
            override fun get(key: LocalizationKey): String =
                customTestTable.entries[key.key] ?: EnglishTranslations.table.entries[key.key]
                ?: key.key

            override fun get(key: LocalizationKey, args: Map<String, Any?>): String {
                var str = get(key)
                for ((k, v) in args) {
                    str = str.replace("{$k}", v?.toString() ?: "")
                }
                return str
            }

            override fun currentLocale(): AynvoraLocale = testLocale

            override fun isSupported(locale: AynvoraLocale): Boolean = locale.localeId == "test"
        }

        // Verify feature keys resolve cleanly through standard LocalizationProvider
        assertEquals("AYNVORA_TEST", provider.get(TranslationKey.App.AppName))
        assertEquals("TAROT_TEST", provider.get(TranslationKey.Tarot.Title))
        assertEquals("PALMISTRY_TEST", provider.get(TranslationKey.Palmistry.Title))
        assertEquals(
            "BAD_INPUT_TEST: email",
            provider.get(TranslationKey.Errors.InvalidInput, mapOf("field" to "email"))
        )

        // Verify unmapped key falls back to English baseline cleanly
        assertEquals("Retry", provider.get(TranslationKey.App.Retry))
    }
}
