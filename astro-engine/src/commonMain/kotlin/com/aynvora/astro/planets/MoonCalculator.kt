package com.aynvora.astro.planets

import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.math.AstroMath.normalizeSignedDegrees
import com.aynvora.astro.math.AstroMath.sinDeg
import com.aynvora.astro.time.JulianDay

/**
 * Geocentric apparent lunar coordinates calculator per Jean Meeus "Astronomical Algorithms"
 * (2nd Edition, Chapter 47) and Chapront ELP-2000/82 lunar theory.
 *
 * Implements fundamental arguments and major periodic perturbation terms (Evection,
 * Variation, Annual Equation, etc.) ensuring ~1-2 arcminute accuracy.
 */
object MoonCalculator {

    data class MoonPosition(
        val apparentLongitude: Double,
        val meanLongitude: Double,
        val dailyMotionDegrees: Double,
    )

    fun calculate(jd: JulianDay): MoonPosition {
        val t = jd.julianCenturiesJ2000
        val pos = computeLongitudeAt(t)

        // Daily motion via finite difference over dt = 0.001 day
        val dtDays = 0.001
        val dtCenturies = dtDays / 36525.0
        val posNext = computeLongitudeAt(t + dtCenturies)
        val dailyMotion = normalizeSignedDegrees(posNext.first - pos.first) / dtDays

        return MoonPosition(
            apparentLongitude = pos.first,
            meanLongitude = pos.second,
            dailyMotionDegrees = dailyMotion,
        )
    }

    private fun computeLongitudeAt(t: Double): Pair<Double, Double> {
        // Fundamental arguments (Meeus eq 47.1)
        val lp = normalizeDegrees(218.3164477 + 481267.8812854 * t - 0.0015786 * t * t + t * t * t / 538841.0)
        val d = normalizeDegrees(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t + t * t * t / 545868.0)
        val m = normalizeDegrees(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        val mp = normalizeDegrees(134.9633964 + 477198.8675055 * t + 0.0087414 * t * t + t * t * t / 69699.0)
        val f = normalizeDegrees(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t - t * t * t / 3526000.0)
        val omega = normalizeDegrees(125.04452 - 1934.136261 * t)

        // Principal periodic perturbation terms in longitude (degrees)
        var sigmaL = 0.0
        sigmaL += 6.288774 * sinDeg(mp)
        sigmaL += 1.274027 * sinDeg(2.0 * d - mp)
        sigmaL += 0.658314 * sinDeg(2.0 * d)
        sigmaL += 0.213618 * sinDeg(2.0 * mp)
        sigmaL += -0.185116 * sinDeg(m)
        sigmaL += -0.114332 * sinDeg(2.0 * f)
        sigmaL += 0.058793 * sinDeg(2.0 * d - 2.0 * mp)
        sigmaL += 0.057066 * sinDeg(2.0 * d - m - mp)
        sigmaL += 0.053322 * sinDeg(2.0 * d + mp)
        sigmaL += 0.045758 * sinDeg(2.0 * d - m)
        sigmaL += -0.040923 * sinDeg(m - mp)
        sigmaL += -0.034720 * sinDeg(d)
        sigmaL += -0.030383 * sinDeg(m + mp)
        sigmaL += 0.015327 * sinDeg(2.0 * d - 2.0 * f)
        sigmaL += -0.012528 * sinDeg(2.0 * d + m - mp)
        sigmaL += 0.010980 * sinDeg(2.0 * d + 2.0 * mp)
        sigmaL += 0.010675 * sinDeg(4.0 * d - mp)
        sigmaL += 0.010034 * sinDeg(3.0 * mp)
        sigmaL += 0.008548 * sinDeg(4.0 * d - 2.0 * mp)
        sigmaL += -0.007888 * sinDeg(2.0 * d + m)
        sigmaL += -0.006766 * sinDeg(2.0 * d - 2.0 * m)
        sigmaL += -0.005163 * sinDeg(d - mp)
        sigmaL += 0.004987 * sinDeg(d + m)
        sigmaL += 0.004036 * sinDeg(2.0 * d - m + mp)
        sigmaL += 0.003994 * sinDeg(2.0 * d + 2.0 * f)
        sigmaL += 0.003861 * sinDeg(4.0 * d)
        sigmaL += 0.003665 * sinDeg(2.0 * d - 3.0 * mp)

        // Planetary perturbations (Venus / Jupiter)
        val a1 = 119.75 + 131.849 * t
        val a2 = 53.09 + 479264.290 * t
        val a3 = 313.45 + 481266.484 * t
        val deltaLPlanetary = 0.003964 * sinDeg(a1) +
            0.001964 * sinDeg(lp - f) +
            0.002060 * sinDeg(a2) +
            0.001964 * sinDeg(a3)

        // Nutation in longitude
        val l0 = 280.46646 + 36000.76983 * t
        val deltaPsi = -0.00479 * sinDeg(omega) - 0.00037 * sinDeg(2.0 * l0)

        val apparentLongitude = normalizeDegrees(lp + sigmaL + deltaLPlanetary + deltaPsi)
        return Pair(apparentLongitude, lp)
    }
}
