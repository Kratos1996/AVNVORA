package com.aynvora.localization

import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.TextDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests 1–4: Language registry correctness.
 */
class LocaleRegistryTest {

    // ── Test 1: Default locale ────────────────────────────────────────────
    @Test
    fun testDefaultLocaleIsEnglish() {
        val default = LanguageRegistry.defaultLocale()
        assertEquals("en", default.localeId)
        assertEquals("en-IN", default.languageTag)
        assertEquals("English", default.englishName)
        assertEquals(TextDirection.LTR, default.direction)
        assertFalse(default.isRtl)
    }

    // ── Test 2: Supported locale registry ───────────────────────────────
    @Test
    fun testAvailableLocalesContainsExpectedEntries() {
        val locales = LanguageRegistry.availableLocales()
        assertTrue(locales.isNotEmpty(), "Available locales must not be empty")

        val ids = locales.map { it.localeId }
        assertTrue("en" in ids, "English must be available")
        assertTrue("hi" in ids, "Hindi must be available")

        // All entries must be fully supported
        locales.forEach { locale ->
            assertTrue(locale.isSupported, "${locale.localeId} must be marked as supported")
            assertTrue(locale.localeId.isNotBlank(), "localeId must not be blank")
            assertTrue(locale.nativeName.isNotBlank(), "nativeName must not be blank")
            assertTrue(locale.englishName.isNotBlank(), "englishName must not be blank")
        }
    }

    // ── Test 3: Locale lookup ───────────────────────────────────────────
    @Test
    fun testGetLocaleByIdReturnsCorrectEntry() {
        val en = LanguageRegistry.getLocale("en")
        assertNotNull(en)
        assertEquals("English", en.englishName)

        val hi = LanguageRegistry.getLocale("hi")
        assertNotNull(hi)
        assertEquals("हिन्दी", hi.nativeName)
        assertEquals("Hindi", hi.englishName)
        assertEquals("hi-IN", hi.languageTag)
    }

    @Test
    fun testGetLocaleReturnsNullForUnknownId() {
        assertNull(LanguageRegistry.getLocale("xx"))
        assertNull(LanguageRegistry.getLocale(""))
        assertNull(LanguageRegistry.getLocale("fr"))
    }

    // ── Test 4: Invalid locale fallback ─────────────────────────────────
    @Test
    fun testGetLocaleOrDefaultFallsBackForUnknownId() {
        val result = LanguageRegistry.getLocaleOrDefault("xx_UNKNOWN")
        assertEquals("en", result.localeId, "Unknown locale must fall back to English")
    }

    @Test
    fun testIsSupportedReturnsTrueForKnownLocales() {
        assertTrue(LanguageRegistry.isSupported("en"))
        assertTrue(LanguageRegistry.isSupported("hi"))
        assertTrue(LanguageRegistry.isSupported("ar"))
    }

    @Test
    fun testIsSupportedReturnsFalseForUnknown() {
        assertFalse(LanguageRegistry.isSupported("fr"))
        assertFalse(LanguageRegistry.isSupported(""))
        assertFalse(LanguageRegistry.isSupported("de"))
        assertFalse(LanguageRegistry.isSupported("es"))
    }


    @Test
    fun testResolveFallbackForHindiIsEnglish() {
        val hindi = LanguageRegistry.getLocale("hi")!!
        val fallback = LanguageRegistry.resolveFallback(hindi)
        assertEquals("en", fallback.localeId)
    }

    @Test
    fun testResolveFallbackForEnglishIsDefault() {
        val english = LanguageRegistry.getLocale("en")!!
        val fallback = LanguageRegistry.resolveFallback(english)
        assertEquals("en", fallback.localeId)
    }

    @Test
    fun testHindiMetadata() {
        val hi = LanguageRegistry.getLocale("hi")!!
        assertEquals(TextDirection.LTR, hi.direction)
        assertFalse(hi.isRtl)
        assertEquals("en", hi.fallbackLocaleId)
    }
}
