package com.aynvora.astro.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical mathematical utilities and coordinate helpers.
 *
 * All trigonometric functions accept or return angles in degrees to maintain
 * clean astronomical semantics without repeated manual radian conversions.
 */
object AstroMath {

    private const val DEG2RAD = PI / 180.0
    private const val RAD2DEG = 180.0 / PI

    /**
     * Normalizes any angle in degrees strictly to the range [0.0, 360.0).
     */
    fun normalizeDegrees(degrees: Double): Double {
        var d = degrees % 360.0
        if (d < 0.0) d += 360.0
        return if (d >= 360.0) 0.0 else d
    }

    /**
     * Normalizes an angle difference to the range [-180.0, 180.0).
     */
    fun normalizeSignedDegrees(degrees: Double): Double {
        val norm = normalizeDegrees(degrees)
        return if (norm >= 180.0) norm - 360.0 else norm
    }

    /**
     * Calculates the shortest angular separation between two ecliptic longitudes in degrees.
     * Guaranteed to return a value in the range [0.0, 180.0].
     */
    fun angularSeparation(deg1: Double, deg2: Double): Double {
        val norm1 = normalizeDegrees(deg1)
        val norm2 = normalizeDegrees(deg2)
        val diff = abs(norm1 - norm2) % 360.0
        return if (diff > 180.0) 360.0 - diff else diff
    }

    fun sinDeg(deg: Double): Double = sin(deg * DEG2RAD)

    fun cosDeg(deg: Double): Double = cos(deg * DEG2RAD)

    fun tanDeg(deg: Double): Double = tan(deg * DEG2RAD)

    fun asinDeg(x: Double): Double = asin(x.coerceIn(-1.0, 1.0)) * RAD2DEG

    fun acosDeg(x: Double): Double = acos(x.coerceIn(-1.0, 1.0)) * RAD2DEG

    fun atan2Deg(y: Double, x: Double): Double = normalizeDegrees(atan2(y, x) * RAD2DEG)

    /**
     * Solves Kepler's Equation E - e * sin(E) = M for eccentric anomaly E in degrees.
     *
     * Uses Newton-Raphson iteration with quadratic convergence.
     */
    fun solveKepler(
        meanAnomalyDeg: Double,
        eccentricity: Double,
        toleranceDeg: Double = 1e-9,
        maxIterations: Int = 100,
    ): Double {
        val mRad = normalizeDegrees(meanAnomalyDeg) * DEG2RAD
        var eRad = mRad + eccentricity * sin(mRad)

        for (i in 0 until maxIterations) {
            val delta = (eRad - eccentricity * sin(eRad) - mRad) / (1.0 - eccentricity * cos(eRad))
            eRad -= delta
            if (abs(delta) < toleranceDeg * DEG2RAD) break
        }

        return normalizeDegrees(eRad * RAD2DEG)
    }
}
