package com.aynvora.astro.time

import kotlin.math.floor

/** Presentation helper for expressing an already calculated Julian Day as a UTC civil timestamp. */
object JulianDayFormatter {
    fun utcTimestamp(julianDay: Double): String {
        require(julianDay.isFinite()) { "Julian Day must be finite" }
        // Round to the nearest second before extracting the civil date so midnight carries correctly.
        val jd = julianDay + 0.5 / 86_400.0
        val shifted = jd + 0.5
        val z = floor(shifted).toInt()
        val fraction = shifted - floor(shifted)
        val a = if (z >= 2_299_161) {
            val alpha = floor((z - 1_867_216.25) / 36_524.25)
            z + 1 + alpha.toInt() - floor(alpha / 4.0).toInt()
        } else z
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val d = floor(365.25 * c).toInt()
        val e = floor((b - d) / 30.6001).toInt()
        val dayDecimal = b - d - floor(30.6001 * e) + fraction
        val day = floor(dayDecimal).toInt()
        val secondsOfDay = ((dayDecimal - day) * 86_400.0).toInt().coerceIn(0, 86_399)
        val hour = secondsOfDay / 3_600
        val minute = secondsOfDay % 3_600 / 60
        val second = secondsOfDay % 60
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4_716 else c - 4_715
        return "%04d-%02d-%02d %02d:%02d:%02d UTC".format(year, month, day, hour, minute, second)
    }
}
