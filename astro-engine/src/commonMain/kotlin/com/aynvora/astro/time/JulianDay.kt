package com.aynvora.astro.time

import kotlin.math.floor

/**
 * Strongly typed representation of Julian Day Number.
 *
 * Implements deterministic Gregorian astronomical calendar calculations
 * per Jean Meeus "Astronomical Algorithms" (2nd Edition, Chapter 7).
 */
data class JulianDay(
    val value: Double,
) {
    init {
        require(!value.isNaN() && !value.isInfinite()) { "Julian Day value must be finite, got: $value" }
    }

    /**
     * Julian centuries elapsed since the standard astronomical epoch J2000.0 (2000 Jan 1, 12h UT).
     * Formula: T = (JD - 2451545.0) / 36525.0
     */
    val julianCenturiesJ2000: Double
        get() = (value - J2000_JD) / 36525.0

    /**
     * Julian millennia elapsed since J2000.0.
     * Formula: tau = T / 10.0
     */
    val julianMillenniaJ2000: Double
        get() = julianCenturiesJ2000 / 10.0

    companion object {
        const val J2000_JD = 2451545.0
        const val UNIX_EPOCH_JD = 2440587.5
        const val MILLIS_PER_DAY = 86_400_000.0

        /**
         * Calculates Julian Day from UTC calendar components using Meeus formula for the Gregorian calendar.
         */
        fun fromUtcCalendar(
            year: Int,
            month: Int,
            day: Int,
            hour: Int = 0,
            minute: Int = 0,
            second: Double = 0.0,
        ): JulianDay {
            require(year in 1..9999) { "Year must be in range 1..9999, got: $year" }
            require(month in 1..12) { "Month must be in range 1..12, got: $month" }
            require(day in 1..31) { "Day must be in range 1..31, got: $day" }
            require(hour in 0..23) { "Hour must be in range 0..23, got: $hour" }
            require(minute in 0..59) { "Minute must be in range 0..59, got: $minute" }
            require(second >= 0.0 && second < 60.0) { "Second must be in range 0.0..<60.0, got: $second" }

            var y = year
            var m = month
            if (m <= 2) {
                y -= 1
                m += 12
            }

            val a = y / 100
            val b = 2 - a + (a / 4)

            val dayFraction = (hour + (minute / 60.0) + (second / 3600.0)) / 24.0

            val jd = floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                day + b - 1524.5 + dayFraction

            return JulianDay(jd)
        }

        /**
         * Calculates Julian Day from Unix epoch milliseconds (UTC).
         */
        fun fromEpochMs(epochMs: Long): JulianDay =
            JulianDay((epochMs.toDouble() / MILLIS_PER_DAY) + UNIX_EPOCH_JD)
    }
}
