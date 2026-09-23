package com.aynvora.localization.format

import com.aynvora.localization.locale.SupportedLocale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Reusable localized formatting utilities for AYNVORA.
 *
 * Rules:
 * - Internal calculation precision NEVER changes due to formatting.
 * - These functions format for DISPLAY only; never use the output as calculation input.
 * - Do not use raw String.format() or hardcoded "°", "'", "\"" in UI code.
 * - Always route through this formatter.
 *
 * Degree notation:
 *   23.857091234° → "23° 51′ 25.5″"
 *   Symbols: ° (U+00B0), ′ (U+2032 prime), ″ (U+2033 double prime)
 */
object LocaleFormatter {

    /**
     * Formats decimal degrees to degrees-arcminutes-arcseconds.
     *
     * Example:
     *   23.857092  → "23° 51′ 25.5″"
     *   0.0        → "0° 0′ 0.0″"
     *   359.999    → "359° 59′ 56.4″"
     *
     * @param degrees Decimal degree value in [0, 360).
     * @param arcSecondPrecision Decimal places for arcseconds. Default 1.
     */
    fun formatDegrees(degrees: Double, arcSecondPrecision: Int = 1): String {
        val d = degrees % 360.0
        val absD = if (d < 0) d + 360.0 else d
        val wholeDeg = floor(absD).toInt()
        val minutesDecimal = (absD - wholeDeg) * 60.0
        val wholeMin = floor(minutesDecimal).toInt()
        val secondsDecimal = (minutesDecimal - wholeMin) * 60.0
        val secStr = formatFixed(secondsDecimal, arcSecondPrecision)
        return "${wholeDeg}° ${wholeMin}′ ${secStr}″"
    }

    /**
     * Formats a longitude value as a signed degrees string.
     *
     * Example:
     *   125.04 → "125.04°"
     *   -0.5   → "-0.5°"
     */
    fun formatLongitude(longitude: Double, decimalPlaces: Int = 4): String =
        "${formatFixed(longitude, decimalPlaces)}°"

    /**
     * Formats a number with locale-appropriate decimal separator.
     *
     * Currently:
     *   English: 1,234.56   (period decimal, comma thousands) — standard Western
     *   Hindi:   1,234.56   (same display convention used in India for numerics)
     *
     * IMPORTANT: The output is DISPLAY ONLY. Never parse this back as a calculation input.
     *
     * @param value          The numeric value to format.
     * @param locale         The target locale.
     * @param decimalPlaces  Number of decimal places. Default 2.
     * @param showThousands  Whether to show thousands separator. Default false for coordinates.
     */
    fun formatNumber(
        value: Double,
        locale: SupportedLocale,
        decimalPlaces: Int = 2,
        showThousands: Boolean = false,
    ): String {
        val formatted = formatFixed(value, decimalPlaces)
        // All current locales use Western numeric format (period decimal).
        // This is the injection point for future locale-specific number formatting.
        return if (showThousands) {
            insertThousandsSeparator(formatted, locale)
        } else {
            formatted
        }
    }

    /**
     * Formats a date for display in the given locale.
     *
     * DOES NOT change the underlying birth instant — only changes display order/format.
     *
     * Format per locale:
     *   English: DD/MM/YYYY  (Indian standard)
     *   Hindi:   DD/MM/YYYY  (same, standard in India)
     *
     * Future locales may use different separators or ordering.
     */
    fun formatDate(year: Int, month: Int, day: Int, locale: SupportedLocale): String {
        val dd = day.toString().padStart(2, '0')
        val mm = month.toString().padStart(2, '0')
        return when (locale.localeId) {
            "en", "hi" -> "$dd/$mm/$year"
            else -> "$dd/$mm/$year"
        }
    }

    /**
     * Formats a time for display in the given locale.
     *
     * Format per locale:
     *   English: HH:MM:SS (24-hour)
     *   Hindi:   HH:MM:SS (24-hour — standard for Jyotisha time notation)
     *
     * Future: 12-hour AM/PM option can be added here without changing the engine.
     */
    fun formatTime(hour: Int, minute: Int, second: Int, locale: SupportedLocale): String {
        val hh = hour.toString().padStart(2, '0')
        val mm = minute.toString().padStart(2, '0')
        val ss = second.toString().padStart(2, '0')
        return "$hh:$mm:$ss"
    }

    /**
     * Formats a Julian Day number for display.
     *
     * Example: 2451545.0 → "2451545.0000"
     */
    fun formatJulianDay(jd: Double): String = formatFixed(jd, 4)

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun formatFixed(value: Double, decimalPlaces: Int): String {
        if (decimalPlaces == 0) {
            val rounded = kotlin.math.round(value).toLong()
            return rounded.toString()
        }
        val factor = pow10(decimalPlaces)
        // Round to the desired precision as a long integer
        val sign = if (value < 0) "-" else ""
        val absValue = kotlin.math.abs(value)
        // Add 0.5/factor for rounding, then convert to integer
        val totalUnits = (absValue * factor + 0.5).toLong()
        val intPart = totalUnits / factor
        val fracPart = totalUnits % factor
        val fracStr = fracPart.toString().padStart(decimalPlaces, '0')
        return "${sign}${intPart}.${fracStr}"
    }

    private fun pow10(n: Int): Long {
        var result = 1L
        repeat(n) { result *= 10 }
        return result
    }

    private fun insertThousandsSeparator(numStr: String, locale: SupportedLocale): String {
        val dotIndex = numStr.indexOf('.')
        val intPart = if (dotIndex >= 0) numStr.substring(0, dotIndex) else numStr
        val fracPart = if (dotIndex >= 0) numStr.substring(dotIndex) else ""
        val isNegative = intPart.startsWith('-')
        val digits = if (isNegative) intPart.substring(1) else intPart
        val withCommas = buildString {
            digits.reversed().forEachIndexed { i, c ->
                if (i > 0 && i % 3 == 0) append(',')
                append(c)
            }
        }.reversed()
        return "${if (isNegative) "-" else ""}$withCommas$fracPart"
    }
}
