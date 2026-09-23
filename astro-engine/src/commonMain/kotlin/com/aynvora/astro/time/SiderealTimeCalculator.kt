package com.aynvora.astro.time

import com.aynvora.astro.math.AstroMath.normalizeDegrees

/**
 * Astronomical time and Earth rotation calculator.
 *
 * Implements Greenwich Mean Sidereal Time (GMST), Local Sidereal Time (LST),
 * and Obliquity of the Ecliptic per Jean Meeus "Astronomical Algorithms" (2nd Edition, Chapter 12 & 22)
 * and IAU standards.
 */
object SiderealTimeCalculator {

    /**
     * Calculates Greenwich Mean Sidereal Time (GMST) in degrees [0.0, 360.0) for the given [JulianDay].
     *
     * Meeus formula (12.4):
     * theta0 = 280.46061837 + 360.98564736629 * (JD - 2451545.0) + 0.000387933 * T^2 - T^3 / 38710000.0
     */
    fun calculateGmst(jd: JulianDay): Double {
        val t = jd.julianCenturiesJ2000
        val d = jd.value - JulianDay.J2000_JD

        val theta0 = 280.46061837 +
            (360.98564736629 * d) +
            (0.000387933 * t * t) -
            ((t * t * t) / 38710000.0)

        return normalizeDegrees(theta0)
    }

    /**
     * Calculates Local Sidereal Time (LST) in degrees [0.0, 360.0) for an observer
     * at [geographicLongitudeDeg] (positive East, negative West).
     */
    fun calculateLst(jd: JulianDay, geographicLongitudeDeg: Double): Double {
        val gmst = calculateGmst(jd)
        return normalizeDegrees(gmst + geographicLongitudeDeg)
    }

    /**
     * Calculates the mean obliquity of the ecliptic (epsilon) in degrees for the given [JulianDay].
     *
     * IAU formula / Meeus equation (22.2):
     * epsilon0 = 23° 26' 21.448" - 46.8150" * T - 0.00059" * T^2 + 0.001813" * T^3
     * In degrees: 23.43929111 - 0.013004167 * T - 0.0000001639 * T^2 + 0.0000005036 * T^3
     */
    fun calculateMeanObliquity(jd: JulianDay): Double {
        val t = jd.julianCenturiesJ2000
        val eps = 23.43929111 -
            (0.013004167 * t) -
            (0.0000001639 * t * t) +
            (0.0000005036 * t * t * t)
        return eps
    }
}
