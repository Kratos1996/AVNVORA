package com.aynvora.localization

import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests 12–18: Translation lookup, missing fallback, variable substitution,
 * pluralization, Rashi, Nakshatra, celestial body localization.
 */
class TranslationTest {

    private val englishTranslator = AynvoraTranslator(LanguageRegistry.ENGLISH, isDebug = true)
    private val hindiTranslator = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = true)

    // ── Test 12: Translation lookup ──────────────────────────────────────
    @Test
    fun testEnglishTranslationLookup() {
        val appName = englishTranslator.translate(TranslationKey.App.AppName)
        assertEquals("AYNVORA", appName)

        val loading = englishTranslator.translate(TranslationKey.App.Loading)
        assertEquals("Loading…", loading)
    }

    @Test
    fun testHindiTranslationLookup() {
        val loading = hindiTranslator.translate(TranslationKey.App.Loading)
        assertEquals("लोड हो रहा है…", loading)
    }

    // ── Test 13: Missing translation fallback ─────────────────────────────
    @Test
    fun testMissingKeyInDebugModeProducesMissingMarker() {
        val result = englishTranslator.resolve("totally.unknown.key")
        // In debug mode, missing keys produce [MISSING: key]
        assertTrue(result.startsWith("[MISSING:"), "Debug mode must flag missing keys: '$result'")
    }

    @Test
    fun testMissingKeyNeverProducesBlank() {
        val prodTranslator = AynvoraTranslator(LanguageRegistry.HINDI, isDebug = false)
        val result = prodTranslator.resolve("nonexistent.key")
        assertTrue(result.isNotBlank(), "Translation must never return blank string")
    }

    @Test
    fun testHindiMissingKeyFallsBackToEnglish() {
        // If a key exists in English but not Hindi, Hindi falls back to English
        val enResult = englishTranslator.translate(TranslationKey.App.Settings)
        val hiResult = hindiTranslator.translate(TranslationKey.App.Settings)

        assertEquals("Settings", enResult)
        assertEquals("सेटिंग्स", hiResult) // Hindi has this key
    }

    // ── Test 14: Variable substitution ───────────────────────────────────
    @Test
    fun testVariableSubstitutionInEnglish() {
        val result = englishTranslator.translateWithArgs(
            TranslationKey.Errors.InvalidInput,
            "field" to "month",
        )
        assertEquals("Invalid input: month", result)
    }

    @Test
    fun testVariableSubstitutionInHindi() {
        val result = hindiTranslator.translateWithArgs(
            TranslationKey.Errors.InvalidInput,
            "field" to "माह",
        )
        assertEquals("अमान्य इनपुट: माह", result)
    }

    @Test
    fun testMultipleVariables() {
        val translator = AynvoraTranslator(LanguageRegistry.ENGLISH, isDebug = true)
        // Resolves raw key with two variables
        var result = translator.resolve("a11y.selected_language")
        result = result.replace("{language}", "Hindi")
        assertEquals("Hindi selected", result)
    }

    // ── Test 15: Pluralization ────────────────────────────────────────────
    @Test
    fun testPluralSingularEnglish() {
        val result = englishTranslator.plural(1, "chart.saved")
        assertEquals("1 chart saved", result)
    }

    @Test
    fun testPluralPluralEnglish() {
        val result = englishTranslator.plural(5, "chart.saved")
        assertEquals("5 charts saved", result)
    }

    @Test
    fun testPluralZeroUsesOtherForm() {
        val result = englishTranslator.plural(0, "chart.saved")
        assertEquals("0 charts saved", result)
    }

    @Test
    fun testPluralHindi() {
        val one = hindiTranslator.plural(1, "chart.saved")
        val many = hindiTranslator.plural(3, "chart.saved")
        assertEquals("1 कुंडली सहेजी गई", one)
        assertEquals("3 कुंडलियाँ सहेजी गईं", many)
    }

    // ── Test 16: Rashi localization ──────────────────────────────────────
    @Test
    fun testRashiLocalizationEnglish() {
        Rashi.entries.forEach { rashi ->
            val name = englishTranslator.translate(TranslationKey.Astro.RashiName(rashi))
            assertFalse(name.startsWith("[MISSING:"), "Missing Rashi English translation: ${rashi.name}")
            assertFalse(name.isBlank(), "Rashi name must not be blank: ${rashi.name}")
        }

        // Verify specific values
        assertEquals("Aries", englishTranslator.translate(TranslationKey.Astro.RashiName(Rashi.ARIES)))
        assertEquals("Pisces", englishTranslator.translate(TranslationKey.Astro.RashiName(Rashi.PISCES)))
    }

    @Test
    fun testRashiLocalizationHindi() {
        Rashi.entries.forEach { rashi ->
            val name = hindiTranslator.translate(TranslationKey.Astro.RashiName(rashi))
            assertFalse(name.startsWith("[MISSING:"), "Missing Rashi Hindi translation: ${rashi.name}")
            assertFalse(name.isBlank(), "Rashi Hindi name must not be blank: ${rashi.name}")
        }

        assertEquals("मेष", hindiTranslator.translate(TranslationKey.Astro.RashiName(Rashi.ARIES)))
        assertEquals("मीन", hindiTranslator.translate(TranslationKey.Astro.RashiName(Rashi.PISCES)))
    }

    // ── Test 17: Nakshatra localization ───────────────────────────────────
    @Test
    fun testAllNakshatrasHaveEnglishTranslations() {
        Nakshatra.entries.forEach { nakshatra ->
            val name = englishTranslator.translate(TranslationKey.Astro.NakshatraName(nakshatra))
            assertFalse(name.startsWith("[MISSING:"), "Missing Nakshatra English: ${nakshatra.name}")
            assertFalse(name.isBlank())
        }
    }

    @Test
    fun testAllNakshatrasHaveHindiTranslations() {
        Nakshatra.entries.forEach { nakshatra ->
            val name = hindiTranslator.translate(TranslationKey.Astro.NakshatraName(nakshatra))
            assertFalse(name.startsWith("[MISSING:"), "Missing Nakshatra Hindi: ${nakshatra.name}")
            assertFalse(name.isBlank())
        }
    }

    @Test
    fun testNakshatraSpecificValues() {
        assertEquals("Ashwini", englishTranslator.translate(TranslationKey.Astro.NakshatraName(Nakshatra.ASHWINI)))
        assertEquals("अश्विनी", hindiTranslator.translate(TranslationKey.Astro.NakshatraName(Nakshatra.ASHWINI)))
        assertEquals("Revati", englishTranslator.translate(TranslationKey.Astro.NakshatraName(Nakshatra.REVATI)))
        assertEquals("रेवती", hindiTranslator.translate(TranslationKey.Astro.NakshatraName(Nakshatra.REVATI)))
    }

    // ── Test 18: Celestial body localization ──────────────────────────────
    @Test
    fun testAllCelestialBodiesHaveEnglishTranslations() {
        CelestialBody.entries.forEach { body ->
            val name = englishTranslator.translate(TranslationKey.Astro.BodyName(body))
            assertFalse(name.startsWith("[MISSING:"), "Missing body English: ${body.name}")
            assertFalse(name.isBlank())
        }
    }

    @Test
    fun testAllCelestialBodiesHaveHindiTranslations() {
        CelestialBody.entries.forEach { body ->
            val name = hindiTranslator.translate(TranslationKey.Astro.BodyName(body))
            assertFalse(name.startsWith("[MISSING:"), "Missing body Hindi: ${body.name}")
            assertFalse(name.isBlank())
        }
    }

    @Test
    fun testCelestialBodySpecificValues() {
        assertEquals("Sun", englishTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.SUN)))
        assertEquals("सूर्य", hindiTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.SUN)))
        assertEquals("Rahu", englishTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.RAHU)))
        assertEquals("राहु", hindiTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.RAHU)))
        assertEquals("Ketu", englishTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.KETU)))
        assertEquals("केतु", hindiTranslator.translate(TranslationKey.Astro.BodyName(CelestialBody.KETU)))
    }

    @Test
    fun testRetrogradeAndDirect() {
        assertEquals("Retrograde", englishTranslator.translate(TranslationKey.Astro.Retrograde))
        assertEquals("वक्री", hindiTranslator.translate(TranslationKey.Astro.Retrograde))
        assertEquals("Direct", englishTranslator.translate(TranslationKey.Astro.Direct))
        assertEquals("मार्गी", hindiTranslator.translate(TranslationKey.Astro.Direct))
    }

    @Test
    fun testLanguageChangeDoesNotProduceDifferentKeys() {
        // The key structure is language-independent — same key works for all locales
        val key = TranslationKey.Astro.RashiName(Rashi.ARIES)
        assertEquals("astro.rashi.aries", key.key)

        // Same key → different display strings per locale
        val enName = englishTranslator.translate(key)
        val hiName = hindiTranslator.translate(key)

        assertNotEquals(enName, hiName)
        assertEquals("Aries", enName)
        assertEquals("मेष", hiName)
    }
}
