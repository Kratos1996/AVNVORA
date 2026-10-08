package com.aynvora.numerology

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Pure, deterministic astronomical calculation engine for Solar Terms (Jie Qi).
 *
 * Specifically computes the exact astronomical instant of Li Chun (立春, Start of Spring),
 * which corresponds to the moment the apparent ecliptic longitude of the Sun reaches exactly 315.0°.
 *
 * Authorities:
 * - Jean Meeus, "Astronomical Algorithms" (2nd Ed. 1998), Chapters 7 & 25 (Solar Coordinates)
 * - Chinese National Standard GB/T 33661-2017 (Calculation and promulgation of the Chinese calendar)
 */
object SolarTermEngine {

    private const val DEG_TO_RAD = PI / 180.0
    private const val RAD_TO_DEG = 180.0 / PI

    /**
     * Converts a Gregorian calendar date/time to Julian Day (UT).
     */
    fun gregorianToJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0
    ): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        val dayFraction = (hour + (minute + second / 60.0) / 60.0) / 24.0
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + dayFraction + b - 1524.5
    }

    /**
     * Converts a Julian Day (UT) to Unix epoch milliseconds.
     */
    fun julianDayToEpochMs(jd: Double): Long = ((jd - 2440587.5) * 86400000.0).toLong()

    /**
     * Calculates the apparent ecliptic longitude of the Sun (in degrees [0..360))
     * according to the Jean Meeus Solar Coordinates model.
     */
    fun getSunApparentLongitude(jd: Double): Double {
        // Julian centuries from J2000.0
        val t = (jd - 2451545.0) / 36525.0

        // Geometric mean longitude of the Sun (degrees)
        var l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        l0 = normalize360(l0)

        // Mean anomaly of the Sun (degrees)
        val m = normalize360(357.52911 + 35999.05029 * t - 0.0001536 * t * t)
        val mRad = m * DEG_TO_RAD

        // Sun's equation of the center (degrees)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2.0 * mRad) +
                0.000290 * sin(3.0 * mRad)

        // Sun's true longitude (degrees)
        val trueLong = l0 + c

        // Apparent longitude correcting for nutation and aberration
        val omega = 125.04 - 1934.136 * t
        val lambda = trueLong - 0.00569 - 0.00478 * sin(omega * DEG_TO_RAD)

        return normalize360(lambda)
    }

    /**
     * Finds the exact Julian Day when the Sun reaches a specific target ecliptic longitude
     * within a given calendar year using Newton-Raphson iterative refinement.
     */
    fun findSolarLongitudeInstant(
        year: Int,
        targetLongitudeDeg: Double,
        approxMonth: Int,
        approxDay: Int
    ): Double {
        // Initial estimate at 00:00 UT
        var jd = gregorianToJulianDay(year, approxMonth, approxDay, 0, 0, 0)

        // Iterative convergence (sun moves approx ~0.9856° to ~1.015° per day in February)
        for (iteration in 0 until 5) {
            val currentLong = getSunApparentLongitude(jd)
            var diff = targetLongitudeDeg - currentLong
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0

            // The Sun moves at approximately 1.01° per day near perihelion (early February)
            val dtDays = diff / 1.014
            jd += dtDays
            if (kotlin.math.abs(diff) < 0.00001) break // Precision better than 1 second
        }

        return jd
    }

    /**
     * Calculates the exact Li Chun (立春, 315.0° solar longitude) instant for a given Gregorian year.
     * Returns the Julian Day (UT) and epoch milliseconds.
     */
    fun calculateLiChun(year: Int): Pair<Double, Long> {
        val jd = findSolarLongitudeInstant(year, 315.0, 2, 4)
        val epochMs = julianDayToEpochMs(jd)
        return Pair(jd, epochMs)
    }

    /**
     * Determines the Chinese solar year for a given birth date and time (UTC).
     *
     * If the birth instant is strictly before the Li Chun instant of that Gregorian year,
     * the solar year is (year - 1). Otherwise, the solar year is year.
     */
    fun determineSolarYear(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12,
        minute: Int = 0
    ): Pair<Int, Boolean> {
        val birthJd = gregorianToJulianDay(year, month, day, hour, minute, 0)
        val (liChunJd, _) = calculateLiChun(year)

        val isBefore = birthJd < liChunJd
        val solarYear = if (isBefore) year - 1 else year
        return Pair(solarYear, isBefore)
    }

    private fun normalize360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0.0) d += 360.0
        return d
    }
}
