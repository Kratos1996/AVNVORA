package com.aynvora.astro.planets

import com.aynvora.astro.math.AstroMath.atan2Deg
import com.aynvora.astro.math.AstroMath.cosDeg
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.math.AstroMath.normalizeSignedDegrees
import com.aynvora.astro.math.AstroMath.sinDeg
import com.aynvora.astro.math.AstroMath.solveKepler
import com.aynvora.astro.time.JulianDay
import kotlin.math.sqrt

/**
 * Geocentric planetary position and motion calculator based on Keplerian orbital elements
 * with secular variations (Simon et al., 1994; Meeus Chapters 31-33) and planetary perturbations.
 */
object PlanetaryCalculator {

    enum class Planet {
        MERCURY,
        VENUS,
        MARS,
        JUPITER,
        SATURN,
    }

    data class PlanetPosition(
        val planet: Planet,
        val apparentLongitude: Double,
        val geocentricDistanceAu: Double,
        val dailyMotionDegrees: Double,
        val isRetrograde: Boolean,
    )

    private data class HeliocentricCoords(
        val x: Double,
        val y: Double,
        val z: Double,
        val r: Double,
    )

    fun calculate(planet: Planet, jd: JulianDay): PlanetPosition {
        val t = jd.julianCenturiesJ2000
        val pos = computeGeocentricLongitudeAt(planet, t)

        val dtDays = 0.002
        val dtCenturies = dtDays / 36525.0
        val posNext = computeGeocentricLongitudeAt(planet, t + dtCenturies)

        val dailyMotion = normalizeSignedDegrees(posNext.first - pos.first) / dtDays
        val isRetrograde = dailyMotion < 0.0

        return PlanetPosition(
            planet = planet,
            apparentLongitude = pos.first,
            geocentricDistanceAu = pos.second,
            dailyMotionDegrees = dailyMotion,
            isRetrograde = isRetrograde,
        )
    }

    private fun computeGeocentricLongitudeAt(planet: Planet, t: Double): Pair<Double, Double> {
        val earthCoords = computeEarthHeliocentric(t)

        // Initial heliocentric position of target planet
        val targetCoordsInitial = computePlanetHeliocentric(planet, t)

        // Geocentric distance
        val dx0 = targetCoordsInitial.x - earthCoords.x
        val dy0 = targetCoordsInitial.y - earthCoords.y
        val dz0 = targetCoordsInitial.z - earthCoords.z
        val distanceAu = sqrt(dx0 * dx0 + dy0 * dy0 + dz0 * dz0)

        // Light-time correction (tau in days = 0.0057755183 * distance)
        val tauDays = 0.0057755183 * distanceAu
        val tauCenturies = tauDays / 36525.0
        val targetCoords = computePlanetHeliocentric(planet, t - tauCenturies)

        // Corrected geocentric vector
        val dx = targetCoords.x - earthCoords.x
        val dy = targetCoords.y - earthCoords.y

        var apparentLongitude = atan2Deg(dy, dx)

        // Apply major planetary perturbations for Jupiter & Saturn (Meeus Chapter 32)
        if (planet == Planet.JUPITER || planet == Planet.SATURN) {
            val deltaLambda = computeJupiterSaturnPerturbation(planet, t)
            apparentLongitude = normalizeDegrees(apparentLongitude + deltaLambda)
        }

        return Pair(apparentLongitude, distanceAu)
    }

    private fun computeEarthHeliocentric(t: Double): HeliocentricCoords {
        val a = 1.00000261 + 0.00000562 * t
        val e = 0.01671123 - 0.00004392 * t
        val i = 0.00005 - 0.013000 * t
        val l = normalizeDegrees(100.46457 + 36000.76983 * t + 0.000303 * t * t)
        val p = normalizeDegrees(102.93768 + 1.719530 * t)
        val node = 0.0

        return heliocentricFromElements(a, e, i, l, p, node)
    }

    private fun computePlanetHeliocentric(planet: Planet, t: Double): HeliocentricCoords = when (planet) {
        Planet.MERCURY -> {
            val a = 0.38709893 + 0.00000066 * t
            val e = 0.20563069 + 0.00002527 * t
            val i = 7.00487 - 0.005947 * t
            val l = normalizeDegrees(252.25084 + 149474.07225 * t + 0.000304 * t * t)
            val p = normalizeDegrees(77.45645 + 1.556478 * t)
            val node = normalizeDegrees(48.33167 - 0.125341 * t)
            heliocentricFromElements(a, e, i, l, p, node)
        }
        Planet.VENUS -> {
            val a = 0.72333199 + 0.00000092 * t
            val e = 0.00677323 - 0.00004938 * t
            val i = 3.39471 - 0.000789 * t
            val l = normalizeDegrees(181.97973 + 58519.21303 * t + 0.000310 * t * t)
            val p = normalizeDegrees(131.57294 + 1.402229 * t)
            val node = normalizeDegrees(76.68069 - 0.277694 * t)
            heliocentricFromElements(a, e, i, l, p, node)
        }
        Planet.MARS -> {
            val a = 1.52366231 - 0.00007221 * t
            val e = 0.09341233 + 0.00011902 * t
            val i = 1.85061 - 0.006947 * t
            val l = normalizeDegrees(355.45332 + 19141.69614 * t + 0.000311 * t * t)
            val p = normalizeDegrees(336.04084 + 1.841045 * t)
            val node = normalizeDegrees(49.55954 - 0.292573 * t)
            heliocentricFromElements(a, e, i, l, p, node)
        }
        Planet.JUPITER -> {
            val a = 5.20336301 + 0.00060737 * t
            val e = 0.04839266 - 0.00012880 * t
            val i = 1.30530 - 0.004156 * t
            val l = normalizeDegrees(34.40438 + 3034.90567 * t - 0.000085 * t * t)
            val p = normalizeDegrees(14.72848 + 1.619663 * t)
            val node = normalizeDegrees(100.55615 + 0.213809 * t)
            heliocentricFromElements(a, e, i, l, p, node)
        }
        Planet.SATURN -> {
            val a = 9.53707032 - 0.00301530 * t
            val e = 0.05415060 - 0.00036762 * t
            val i = 2.48446 + 0.006114 * t
            val l = normalizeDegrees(49.94432 + 1222.11379 * t - 0.000210 * t * t)
            val p = normalizeDegrees(92.59888 - 0.418972 * t)
            val node = normalizeDegrees(113.71504 - 0.288678 * t)
            heliocentricFromElements(a, e, i, l, p, node)
        }
    }

    private fun heliocentricFromElements(
        a: Double,
        e: Double,
        i: Double,
        l: Double,
        p: Double,
        node: Double,
    ): HeliocentricCoords {
        val m = normalizeDegrees(l - p)
        val bigE = solveKepler(m, e)

        val xPrime = a * (cosDeg(bigE) - e)
        val yPrime = a * sqrt(1.0 - e * e) * sinDeg(bigE)
        val r = a * (1.0 - e * cosDeg(bigE))
        val v = atan2Deg(yPrime, xPrime)

        val u = normalizeDegrees(v + p - node)

        val x = r * (cosDeg(node) * cosDeg(u) - sinDeg(node) * sinDeg(u) * cosDeg(i))
        val y = r * (sinDeg(node) * cosDeg(u) + cosDeg(node) * sinDeg(u) * cosDeg(i))
        val z = r * (sinDeg(u) * sinDeg(i))

        return HeliocentricCoords(x, y, z, r)
    }

    /**
     * Great Inequality mutual perturbations between Jupiter and Saturn.
     */
    private fun computeJupiterSaturnPerturbation(planet: Planet, t: Double): Double {
        val lj = normalizeDegrees(34.40438 + 3034.90567 * t)
        val pj = normalizeDegrees(14.72848 + 1.619663 * t)
        val mj = normalizeDegrees(lj - pj)

        val ls = normalizeDegrees(49.94432 + 1222.11379 * t)
        val ps = normalizeDegrees(92.59888 - 0.418972 * t)
        val ms = normalizeDegrees(ls - ps)

        return when (planet) {
            Planet.JUPITER -> {
                -0.332 * sinDeg(2.0 * mj - 5.0 * ms - 67.6) -
                    0.056 * sinDeg(2.0 * mj - 2.0 * ms + 21.0) +
                    0.042 * sinDeg(3.0 * mj - 5.0 * ms + 21.0) -
                    0.036 * sinDeg(mj - 2.0 * ms)
            }
            Planet.SATURN -> {
                0.812 * sinDeg(2.0 * mj - 5.0 * ms - 67.6) -
                    0.229 * cosDeg(2.0 * mj - 4.0 * ms - 2.0) +
                    0.119 * sinDeg(mj - 2.0 * ms - 3.0)
            }
            else -> 0.0
        }
    }
}
