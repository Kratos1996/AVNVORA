package com.aynvora.astro.panchang

import com.aynvora.astro.math.AstroMath
import com.aynvora.astro.planets.SunCalculator
import com.aynvora.astro.time.JulianDay

internal data class SolarDayEvents(val sunriseJulianDay: Double, val sunsetJulianDay: Double)

/** Apparent sunrise/sunset at the conventional solar-center altitude of -0.833 degrees. */
internal object SolarDayEventCalculator {
    fun calculate(
        localDateJd: Double,
        latitudeDeg: Double,
        longitudeDeg: Double,
        utcOffsetMinutes: Int
    ): SolarDayEvents {
        require(latitudeDeg in -90.0..90.0)
        require(longitudeDeg in -180.0..180.0)
        require(utcOffsetMinutes in -840..840)
        val utcStart = localDateJd - utcOffsetMinutes / 1440.0
        val rise = findCrossing(utcStart, utcStart + 0.5, latitudeDeg, longitudeDeg, rising = true)
            ?: throw UnsupportedOperationException("Sunrise does not occur on this local date at this latitude")
        val set =
            findCrossing(utcStart + 0.5, utcStart + 1.0, latitudeDeg, longitudeDeg, rising = false)
                ?: throw UnsupportedOperationException("Sunset does not occur on this local date at this latitude")
        return SolarDayEvents(rise, set)
    }

    private fun findCrossing(
        start: Double,
        end: Double,
        latitude: Double,
        longitude: Double,
        rising: Boolean
    ): Double? {
        fun altitudeResidual(jd: Double) = solarAltitude(jd, latitude, longitude) + 0.833
        var left = start
        var leftValue = altitudeResidual(left)
        var bracket: Pair<Double, Double>? = null
        for (step in 1..288) {
            val right = start + (end - start) * step / 288.0
            val rightValue = altitudeResidual(right)
            if ((rising && leftValue < 0.0 && rightValue >= 0.0) ||
                (!rising && leftValue >= 0.0 && rightValue < 0.0)
            ) {
                bracket = left to right
                break
            }
            left = right
            leftValue = rightValue
        }
        var (lo, hi) = bracket ?: return null
        repeat(40) {
            val mid = (lo + hi) / 2.0
            val belowHorizon = altitudeResidual(mid) < 0.0
            if (belowHorizon == rising) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    private fun solarAltitude(jd: Double, latitudeDeg: Double, longitudeDeg: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val eclipticLongitude = SunCalculator.calculate(JulianDay(jd)).apparentLongitude
        val obliquity = 23.439291 - 0.0130042 * t
        val rightAscension = AstroMath.atan2Deg(
            AstroMath.cosDeg(obliquity) * AstroMath.sinDeg(eclipticLongitude),
            AstroMath.cosDeg(eclipticLongitude),
        )
        val declination =
            AstroMath.asinDeg(AstroMath.sinDeg(obliquity) * AstroMath.sinDeg(eclipticLongitude))
        val gmst = AstroMath.normalizeDegrees(
            280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t,
        )
        val hourAngle = AstroMath.normalizeSignedDegrees(gmst + longitudeDeg - rightAscension)
        val sinAltitude = AstroMath.sinDeg(latitudeDeg) * AstroMath.sinDeg(declination) +
                AstroMath.cosDeg(latitudeDeg) * AstroMath.cosDeg(declination) * AstroMath.cosDeg(
            hourAngle
        )
        return kotlin.math.asin(sinAltitude.coerceIn(-1.0, 1.0)) * 180.0 / kotlin.math.PI
    }
}
