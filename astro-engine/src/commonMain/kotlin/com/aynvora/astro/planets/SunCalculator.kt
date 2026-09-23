package com.aynvora.astro.planets

import com.aynvora.astro.math.AstroMath.cosDeg
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.math.AstroMath.normalizeSignedDegrees
import com.aynvora.astro.math.AstroMath.sinDeg
import com.aynvora.astro.time.JulianDay

/**
 * Solar coordinates calculator per Jean Meeus "Astronomical Algorithms" (2nd Edition, Chapter 25).
 *
 * Computes geocentric apparent ecliptic longitude of the Sun, including
 * equation of the center, nutation, and aberration corrections.
 * Accuracy: ~0.01° (0.6 arcminutes) across several centuries around J2000.0.
 */
object SunCalculator {

    data class SunPosition(
        val apparentLongitude: Double,
        val geometricMeanLongitude: Double,
        val meanAnomaly: Double,
        val equationOfCenter: Double,
        val dailyMotionDegrees: Double,
    )

    /**
     * Calculates the Sun's geocentric apparent ecliptic longitude at the given [JulianDay].
     */
    fun calculate(jd: JulianDay): SunPosition {
        val t = jd.julianCenturiesJ2000
        val pos = computeLongitudeAt(t)

        // Compute daily motion via finite difference over dt = 0.001 day
        val dtDays = 0.001
        val dtCenturies = dtDays / 36525.0
        val posNext = computeLongitudeAt(t + dtCenturies)
        val dailyMotion = normalizeSignedDegrees(posNext.first - pos.first) / dtDays

        return SunPosition(
            apparentLongitude = pos.first,
            geometricMeanLongitude = pos.second,
            meanAnomaly = pos.third,
            equationOfCenter = pos.fourth,
            dailyMotionDegrees = dailyMotion,
        )
    }

    private fun computeLongitudeAt(t: Double): Quadruple<Double, Double, Double, Double> {
        val l0 = normalizeDegrees(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = normalizeDegrees(357.52911 + 35999.05029 * t - 0.0001537 * t * t)

        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sinDeg(m) +
            (0.019993 - 0.000101 * t) * sinDeg(2.0 * m) +
            0.000289 * sinDeg(3.0 * m)

        val trueLongitude = l0 + c

        // Nutation and aberration correction (Meeus eq 25.8)
        val omega = 125.04 - 1934.136 * t
        val apparentLongitude = normalizeDegrees(trueLongitude - 0.00569 - 0.00478 * sinDeg(omega))

        return Quadruple(apparentLongitude, l0, m, c)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
