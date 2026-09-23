package com.aynvora.astro.planets

import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.time.JulianDay

/**
 * Calculator for Lunar Nodes (Rahu and Ketu) per Brown-Chapront / Meeus Chapter 47.
 *
 * Rahu represents the Ascending / North Node of the Moon.
 * Ketu is mathematically guaranteed to be exactly 180° opposite Rahu at all times.
 * In Vedic astrology, the mean nodes exhibit constant retrograde motion.
 */
object LunarNodesCalculator {

    data class LunarNodePosition(
        val apparentLongitude: Double,
        val dailyMotionDegrees: Double,
        val isRetrograde: Boolean = true,
    )

    data class LunarNodesResult(
        val rahu: LunarNodePosition,
        val ketu: LunarNodePosition,
    )

    fun calculate(jd: JulianDay): LunarNodesResult {
        val t = jd.julianCenturiesJ2000

        // Mean longitude of the ascending lunar node (Meeus eq 47.7)
        val omega = normalizeDegrees(
            125.04452 - 1934.136261 * t + 0.0020708 * t * t + (t * t * t / 450000.0),
        )

        // Mean daily motion: -1934.136261 / 36525 = -0.0529538 degrees/day
        val dailyMotion = -1934.136261 / 36525.0

        val rahuLong = omega
        val ketuLong = normalizeDegrees(rahuLong + 180.0)

        return LunarNodesResult(
            rahu = LunarNodePosition(
                apparentLongitude = rahuLong,
                dailyMotionDegrees = dailyMotion,
                isRetrograde = true,
            ),
            ketu = LunarNodePosition(
                apparentLongitude = ketuLong,
                dailyMotionDegrees = dailyMotion,
                isRetrograde = true,
            ),
        )
    }
}
