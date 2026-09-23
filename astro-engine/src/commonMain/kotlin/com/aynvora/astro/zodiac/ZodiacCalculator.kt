package com.aynvora.astro.zodiac

import com.aynvora.astro.math.AstroMath.normalizeDegrees

/**
 * Astrological zodiac divisions calculator.
 *
 * Implements deterministic tropical-to-sidereal conversion, 12 Rashi (sign) divisions,
 * 27 Nakshatras, and 4 Padas per Nakshatra with boundary-safe arithmetic.
 */
object ZodiacCalculator {

    const val DEGREES_PER_RASHI = 30.0
    const val DEGREES_PER_NAKSHATRA = 360.0 / 27.0 // 13° 20' = 13.333333333333334°
    const val DEGREES_PER_PADA = DEGREES_PER_NAKSHATRA / 4.0 // 3° 20' = 3.3333333333333335°

    data class RashiInfo(
        val index: Int, // 0..11
        val name: String,
        val degreeInRashi: Double,
        val totalSiderealLongitude: Double,
    )

    data class NakshatraInfo(
        val index: Int, // 0..26
        val name: String,
        val degreeInNakshatra: Double,
        val pada: Int, // 1..4
    )

    val RASHI_NAMES = listOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces",
    )

    val NAKSHATRA_NAMES = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati",
    )

    /**
     * Converts tropical geocentric ecliptic longitude to sidereal longitude using [ayanamsaDegrees].
     */
    fun toSidereal(tropicalLongitude: Double, ayanamsaDegrees: Double): Double =
        normalizeDegrees(tropicalLongitude - ayanamsaDegrees)

    /**
     * Calculates Rashi (zodiac sign) from sidereal longitude.
     */
    fun calculateRashi(siderealLongitude: Double): RashiInfo {
        val norm = normalizeDegrees(siderealLongitude)
        var index = (norm / DEGREES_PER_RASHI).toInt()
        if (index > 11) index = 11
        if (index < 0) index = 0

        val degreeInRashi = norm - (index * DEGREES_PER_RASHI)
        return RashiInfo(
            index = index,
            name = RASHI_NAMES[index],
            degreeInRashi = degreeInRashi,
            totalSiderealLongitude = norm,
        )
    }

    /**
     * Calculates Nakshatra and Pada from sidereal longitude.
     */
    fun calculateNakshatra(siderealLongitude: Double): NakshatraInfo {
        val norm = normalizeDegrees(siderealLongitude)
        var nakshatraIdx = (norm / DEGREES_PER_NAKSHATRA).toInt()
        if (nakshatraIdx > 26) nakshatraIdx = 26
        if (nakshatraIdx < 0) nakshatraIdx = 0

        val degreeInNakshatra = norm - (nakshatraIdx * DEGREES_PER_NAKSHATRA)
        var padaIdx = (degreeInNakshatra / DEGREES_PER_PADA).toInt()
        if (padaIdx > 3) padaIdx = 3
        if (padaIdx < 0) padaIdx = 0

        val pada = padaIdx + 1

        return NakshatraInfo(
            index = nakshatraIdx,
            name = NAKSHATRA_NAMES[nakshatraIdx],
            degreeInNakshatra = degreeInNakshatra,
            pada = pada,
        )
    }
}
