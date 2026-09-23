package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Supported celestial bodies and mathematical lunar nodes in AYNVORA.
 */
@Serializable
enum class CelestialBody {
    SUN,
    MOON,
    MERCURY,
    VENUS,
    MARS,
    JUPITER,
    SATURN,
    RAHU,
    KETU,
}

/**
 * 12 Sidereal Zodiac Signs (Rashis), each spanning exactly 30 degrees.
 */
@Serializable
enum class Rashi(val index: Int, val displayName: String, val sanskritName: String) {
    ARIES(0, "Aries", "Mesha"),
    TAURUS(1, "Taurus", "Vrishabha"),
    GEMINI(2, "Gemini", "Mithuna"),
    CANCER(3, "Cancer", "Karka"),
    LEO(4, "Leo", "Simha"),
    VIRGO(5, "Virgo", "Kanya"),
    LIBRA(6, "Libra", "Tula"),
    SCORPIO(7, "Scorpio", "Vrishchika"),
    SAGITTARIUS(8, "Sagittarius", "Dhanu"),
    CAPRICORN(9, "Capricorn", "Makara"),
    AQUARIUS(10, "Aquarius", "Kumbha"),
    PISCES(11, "Pisces", "Meena");

    companion object {
        fun fromIndex(index: Int): Rashi = entries[index.coerceIn(0, 11)]
    }
}

/**
 * 27 Lunar Mansions (Nakshatras), each spanning 13° 20' (13.333333°).
 */
@Serializable
enum class Nakshatra(val index: Int, val displayName: String) {
    ASHWINI(0, "Ashwini"),
    BHARANI(1, "Bharani"),
    KRITTIKA(2, "Krittika"),
    ROHINI(3, "Rohini"),
    MRIGASHIRA(4, "Mrigashira"),
    ARDRA(5, "Ardra"),
    PUNARVASU(6, "Punarvasu"),
    PUSHYA(7, "Pushya"),
    ASHLESHA(8, "Ashlesha"),
    MAGHA(9, "Magha"),
    PURVA_PHALGUNI(10, "Purva Phalguni"),
    UTTARA_PHALGUNI(11, "Uttara Phalguni"),
    HASTA(12, "Hasta"),
    CHITRA(13, "Chitra"),
    SWATI(14, "Swati"),
    VISHAKHA(15, "Vishakha"),
    ANURADHA(16, "Anuradha"),
    JYESHTHA(17, "Jyeshtha"),
    MULA(18, "Mula"),
    PURVA_ASHADHA(19, "Purva Ashadha"),
    UTTARA_ASHADHA(20, "Uttara Ashadha"),
    SHRAVANA(21, "Shravana"),
    DHANISHTA(22, "Dhanishta"),
    SHATABHISHA(23, "Shatabhisha"),
    PURVA_BHADRAPADA(24, "Purva Bhadrapada"),
    UTTARA_BHADRAPADA(25, "Uttara Bhadrapada"),
    REVATI(26, "Revati");

    companion object {
        fun fromIndex(index: Int): Nakshatra = entries[index.coerceIn(0, 26)]
    }
}

/**
 * Sign placement details.
 */
@Serializable
data class RashiPosition(
    val rashi: Rashi,
    val degreeInSign: Double,
    val totalSiderealLongitude: Double,
)

/**
 * Nakshatra and Pada placement details.
 */
@Serializable
data class NakshatraPosition(
    val nakshatra: Nakshatra,
    val degreeInNakshatra: Double,
    val pada: Int,
)

/**
 * Ascendant / Lagna astronomical details.
 */
@Serializable
data class LagnaDetails(
    val tropicalLongitude: Double,
    val siderealLongitude: Double,
    val rashiPosition: RashiPosition,
    val nakshatraPosition: NakshatraPosition,
    val localSiderealTimeDegrees: Double = 0.0,
    val obliquityDegrees: Double = 0.0,
    val midheavenTropicalLongitude: Double = 0.0,
    val midheavenSiderealLongitude: Double = 0.0,
)

/**
 * Calculated house (Bhava) details.
 */
@Serializable
data class HouseDetails(
    val houseNumber: Int, // 1..12
    val system: HouseSystem,
    val cuspLongitude: Double,
    val startLongitude: Double,
    val endLongitude: Double,
    val rashiPosition: RashiPosition,
)

/**
 * Classical planetary aspect classification types.
 */
@Serializable
enum class AspectType(val exactAngle: Double, val defaultOrb: Double) {
    CONJUNCTION(0.0, 8.0),
    SEXTILE(60.0, 6.0),
    SQUARE(90.0, 7.0),
    TRINE(120.0, 8.0),
    OPPOSITION(180.0, 8.0),
}

/**
 * Apparent planetary motion state.
 */
@Serializable
enum class PlanetMotionState {
    DIRECT,
    RETROGRADE,
}

/**
 * Planetary solar combustion (Asta) state.
 */
@Serializable
enum class CombustionState {
    NORMAL,
    COMBUST,
    NOT_APPLICABLE,
}

/**
 * Factual aspect relationship between two distinct celestial bodies.
 */
@Serializable
data class Aspect(
    val firstBody: CelestialBody,
    val secondBody: CelestialBody,
    val type: AspectType,
    val exactAngle: Double,
    val actualSeparation: Double,
    val orb: Double,
)

/**
 * Planetary condition and state details.
 */
@Serializable
data class PlanetState(
    val body: CelestialBody,
    val motionState: PlanetMotionState,
    val combustionState: CombustionState,
    val separationFromSun: Double? = null,
    val combustionThresholdDegrees: Double? = null,
)

/**
 * Calculated planetary coordinate, house occupancy, and motion output.
 */
@Serializable
data class PlanetaryPosition(
    val body: CelestialBody,
    val tropicalLongitude: Double,
    val siderealLongitude: Double,
    val rashiPosition: RashiPosition,
    val nakshatraPosition: NakshatraPosition,
    val houseNumber: Int = 1,
    val isRetrograde: Boolean,
    val dailyMotionDegrees: Double,
    val motionState: PlanetMotionState = if (isRetrograde) PlanetMotionState.RETROGRADE else PlanetMotionState.DIRECT,
    val combustionState: CombustionState = CombustionState.NORMAL,
)
