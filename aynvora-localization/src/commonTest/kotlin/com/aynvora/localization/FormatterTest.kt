package com.aynvora.localization

import com.aynvora.localization.format.LocaleFormatter
import com.aynvora.localization.locale.LanguageRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests 19–21: Date formatting, number formatting, degree formatting.
 */
class FormatterTest {

    private val english = LanguageRegistry.ENGLISH
    private val hindi = LanguageRegistry.HINDI

    // ── Test 19: Date formatting ──────────────────────────────────────────
    @Test
    fun testDateFormattingEnglish() {
        val result = LocaleFormatter.formatDate(2000, 1, 1, english)
        assertEquals("01/01/2000", result)
    }

    @Test
    fun testDateFormattingHindi() {
        val result = LocaleFormatter.formatDate(2000, 1, 1, hindi)
        assertEquals("01/01/2000", result) // India uses DD/MM/YYYY for both
    }

    @Test
    fun testDateFormattingPaddingForSingleDigits() {
        val result = LocaleFormatter.formatDate(1992, 3, 5, english)
        assertEquals("05/03/1992", result)
    }

    @Test
    fun testTimeFormatting24Hour() {
        assertEquals("14:30:00", LocaleFormatter.formatTime(14, 30, 0, english))
        assertEquals("00:00:00", LocaleFormatter.formatTime(0, 0, 0, english))
        assertEquals("23:59:59", LocaleFormatter.formatTime(23, 59, 59, english))
    }

    // ── Test 20: Number formatting ────────────────────────────────────────
    @Test
    fun testNumberFormattingBasic() {
        val result = LocaleFormatter.formatNumber(23.857092, english, decimalPlaces = 4)
        assertEquals("23.8571", result)
    }

    @Test
    fun testNumberFormattingWithThousandsSeparator() {
        val result = LocaleFormatter.formatNumber(1234567.89, english, decimalPlaces = 2, showThousands = true)
        assertEquals("1,234,567.89", result)
    }

    @Test
    fun testNumberFormattingZero() {
        val result = LocaleFormatter.formatNumber(0.0, english, decimalPlaces = 2)
        assertEquals("0.00", result)
    }

    @Test
    fun testJulianDayFormatting() {
        val jd = LocaleFormatter.formatJulianDay(2451545.0)
        assertEquals("2451545.0000", jd)
    }

    // ── Test 21: Degree formatting ─────────────────────────────────────────
    @Test
    fun testDegreesFormattingMeeusExample() {
        // Lahiri at J2000.0 = 23.857092°
        // 23.857092° → 23° 51′ 25.5″
        val result = LocaleFormatter.formatDegrees(23.857092, arcSecondPrecision = 1)
        assertEquals("23° 51′ 25.5″", result)
    }

    @Test
    fun testDegreesFormattingZero() {
        val result = LocaleFormatter.formatDegrees(0.0)
        assertEquals("0° 0′ 0.0″", result)
    }

    @Test
    fun testDegreesFormatting90() {
        val result = LocaleFormatter.formatDegrees(90.0)
        assertEquals("90° 0′ 0.0″", result)
    }

    @Test
    fun testDegreesFormatting359point999() {
        // 359.999° → 359° 59′ 56.4″
        val result = LocaleFormatter.formatDegrees(359.999, arcSecondPrecision = 1)
        assertTrue(result.startsWith("359°"), "Expected 359° prefix, got: $result")
    }

    @Test
    fun testLongitudeFormatting() {
        val result = LocaleFormatter.formatLongitude(125.04, decimalPlaces = 2)
        assertEquals("125.04°", result)
    }

    @Test
    fun testDegreesUsesCorrectUnicodeSymbols() {
        val result = LocaleFormatter.formatDegrees(23.857092)
        // ° = U+00B0, ′ = U+2032, ″ = U+2033
        assertTrue(result.contains("°"), "Must contain degree symbol °")
        assertTrue(result.contains("′"), "Must contain prime symbol ′ (U+2032)")
        assertTrue(result.contains("″"), "Must contain double-prime symbol ″ (U+2033)")

        // Must NOT contain typewriter apostrophe/quote as degree symbols
        assertFalse(result.contains("'"), "Must not contain typewriter apostrophe for arcminutes")
        assertFalse(result.contains("\""), "Must not contain typewriter quote for arcseconds")
    }
}
