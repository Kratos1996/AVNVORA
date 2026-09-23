package com.aynvora.astro.lagna

import com.aynvora.astro.math.AstroMath.atan2Deg
import com.aynvora.astro.math.AstroMath.cosDeg
import com.aynvora.astro.math.AstroMath.sinDeg
import com.aynvora.astro.math.AstroMath.tanDeg
import com.aynvora.astro.time.JulianDay
import com.aynvora.astro.time.SiderealTimeCalculator
import com.aynvora.astro.zodiac.ZodiacCalculator
import kotlinx.serialization.Serializable

/**
 * Calculated Ascendant / Lagna position.
 */
@Serializable
data class LagnaPosition(
    val tropicalLongitude: Double,
    val siderealLongitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInRashi: Double,
    val nakshatraIndex: Int,
    val nakshatraName: String,
    val degreeInNakshatra: Double,
    val pada: Int,
    val localSiderealTimeDegrees: Double = 0.0,
    val obliquityDegrees: Double = 0.0,
    val midheavenTropicalLongitude: Double = 0.0,
    val midheavenSiderealLongitude: Double = 0.0,
)

/**
 * Deterministic Ascendant (Lagna) and Midheaven (MC) astronomical calculator.
 *
 * Implements rigorous spherical trigonometry formulas derived from celestial horizon
 * and ecliptic circle intersection at the observer's geographic coordinates.
 */
object LagnaCalculator {

    /**
     * Calculates the Ascendant (Lagna) and Midheaven (MC) for a given [JulianDay],
     * observer geographic coordinates, and Ayanamsa offset.
     *
     * @param jd Julian Day instant
     * @param latitudeDeg Geographic latitude in degrees [-90.0, 90.0] (positive North, negative South)
     * @param longitudeDeg Geographic longitude in degrees [-180.0, 180.0] (positive East, negative West)
     * @param ayanamsaDegrees Sidereal ayanamsa offset in degrees
     */
    fun calculate(
        jd: JulianDay,
        latitudeDeg: Double,
        longitudeDeg: Double,
        ayanamsaDegrees: Double,
    ): LagnaPosition {
        require(latitudeDeg in -90.0..90.0) { "Latitude must be between -90.0 and 90.0, got: $latitudeDeg" }
        require(longitudeDeg in -180.0..180.0) { "Longitude must be between -180.0 and 180.0, got: $longitudeDeg" }

        val lst = SiderealTimeCalculator.calculateLst(jd, longitudeDeg)
        val eps = SiderealTimeCalculator.calculateMeanObliquity(jd)

        // Clamp latitude to safe bounds to avoid division by zero or infinite tangent at exact poles
        val safeLat = latitudeDeg.coerceIn(-89.9999, 89.9999)

        // Spherical trigonometry for Ascendant (eastern horizon intersection with ecliptic)
        // tan(Asc) = cos(theta) / (-sin(theta)*cos(eps) - tan(phi)*sin(eps))
        val y = cosDeg(lst)
        val x = -sinDeg(lst) * cosDeg(eps) - tanDeg(safeLat) * sinDeg(eps)
        val tropicalAsc = atan2Deg(y, x)

        // Midheaven (MC): intersection of meridian with ecliptic
        // tan(MC) = sin(theta) / (cos(theta)*cos(eps))
        val mcY = sinDeg(lst)
        val mcX = cosDeg(lst) * cosDeg(eps)
        val tropicalMc = atan2Deg(mcY, mcX)

        val siderealAsc = ZodiacCalculator.toSidereal(tropicalAsc, ayanamsaDegrees)
        val siderealMc = ZodiacCalculator.toSidereal(tropicalMc, ayanamsaDegrees)

        val rashi = ZodiacCalculator.calculateRashi(siderealAsc)
        val nakshatra = ZodiacCalculator.calculateNakshatra(siderealAsc)

        return LagnaPosition(
            tropicalLongitude = tropicalAsc,
            siderealLongitude = siderealAsc,
            rashiIndex = rashi.index,
            rashiName = rashi.name,
            degreeInRashi = rashi.degreeInRashi,
            nakshatraIndex = nakshatra.index,
            nakshatraName = nakshatra.name,
            degreeInNakshatra = nakshatra.degreeInNakshatra,
            pada = nakshatra.pada,
            localSiderealTimeDegrees = lst,
            obliquityDegrees = eps,
            midheavenTropicalLongitude = tropicalMc,
            midheavenSiderealLongitude = siderealMc,
        )
    }
}
