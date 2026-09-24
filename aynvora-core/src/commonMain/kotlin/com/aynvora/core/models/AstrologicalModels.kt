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

/**
 * Strongly typed enumeration of classical Vedic divisional charts (Vargas).
 */
@Serializable
enum class DivisionalChart(val divisionNumber: Int) {
    D1(1),
    D2(2),
    D3(3),
    D4(4),
    D7(7),
    D9(9),
    D10(10),
    D12(12),
    D16(16),
    D20(20),
    D24(24),
    D27(27),
    D30(30),
    D40(40),
    D45(45),
    D60(60);

    companion object {
        fun fromDivisionNumber(number: Int): DivisionalChart? =
            entries.find { it.divisionNumber == number }
    }
}

/**
 * Factual divisional chart position of a celestial body or Lagna.
 */
@Serializable
data class DivisionalPosition(
    val body: CelestialBody?,
    val isLagna: Boolean = false,
    val sourceLongitude: Double,
    val sourceRashi: Rashi,
    val divisionIndex: Int,
    val resultingRashi: Rashi,
    val degreeInResultingRashi: Double,
    val resultingLongitude: Double,
)

/**
 * Public factual result of a divisional chart calculation.
 * Contains no astrological interpretation or prediction.
 */
@Serializable
data class DivisionalChartResult(
    val chart: DivisionalChart,
    val rulesetId: String,
    val isSupported: Boolean,
    val lagnaPosition: DivisionalPosition?,
    val positions: List<DivisionalPosition>,
)

/**
 * Factual planetary dignity classification according to traditional Vedic astrology.
 */
@Serializable
enum class DignityType {
    EXALTATION,
    DEBILITATION,
    MOOLATRIKONA,
    OWN_SIGN,
    GREAT_FRIEND_SIGN,
    FRIEND_SIGN,
    NEUTRAL_SIGN,
    ENEMY_SIGN,
    GREAT_ENEMY_SIGN,
    NOT_APPLICABLE,
}

/**
 * Factual planetary dignity evaluated in a specific chart position.
 */
@Serializable
data class PlanetaryDignity(
    val body: CelestialBody,
    val chart: DivisionalChart = DivisionalChart.D1,
    val rashi: Rashi,
    val signLord: CelestialBody?,
    val dignityType: DignityType,
    val isExalted: Boolean,
    val isDebilitated: Boolean,
    val isMoolatrikona: Boolean,
    val isOwnSign: Boolean,
    val deepExaltationDegree: Double? = null,
    val deepDebilitationDegree: Double? = null,
    val degreeInSign: Double = 0.0,
    val ruleId: String = "PARASHARA_CLASSICAL_V1",
)

/**
 * Natural planetary relationship (Naisargika Maitri) according to BPHS Ch. 3, Slokas 55-58.
 */
@Serializable
enum class NaturalRelationshipType {
    FRIEND,
    NEUTRAL,
    ENEMY,
    NOT_APPLICABLE,
}

/**
 * Temporary planetary relationship (Tatkalika Maitri) according to BPHS Ch. 3, Sloka 59.
 */
@Serializable
enum class TemporaryRelationshipType {
    FRIEND,
    ENEMY,
    NOT_APPLICABLE,
}

/**
 * Five-fold compound planetary relationship (Panchadha Maitri) according to BPHS Ch. 3, Sloka 60.
 */
@Serializable
enum class CompoundRelationshipType {
    GREAT_FRIEND,
    FRIEND,
    NEUTRAL,
    ENEMY,
    GREAT_ENEMY,
    NOT_APPLICABLE,
}

/**
 * Factual directional relationship from [sourceBody] to [targetBody].
 */
@Serializable
data class PlanetaryRelationship(
    val sourceBody: CelestialBody,
    val targetBody: CelestialBody,
    val chart: DivisionalChart = DivisionalChart.D1,
    val naturalRelationship: NaturalRelationshipType,
    val temporaryRelationship: TemporaryRelationshipType,
    val compoundRelationship: CompoundRelationshipType,
    val sourceRashi: Rashi,
    val targetRashi: Rashi,
    val relativeHouseDistance: Int,
)

/**
 * Auditability state for Shadbala calculations.
 */
@Serializable
enum class ShadbalaCompleteness {
    COMPLETE,
    PARTIAL_FOUNDATION,
    UNSUPPORTED,
}

/**
 * Positional strength (Sthana Bala) and its 5 subcomponents.
 * Source: BPHS Chapter 28, Slokas 2-12 (ASTRO-R34).
 */
@Serializable
data class SthanaBala(
    val uchchaBalaVirupas: Double,
    val saptavargajaBalaVirupas: Double,
    val ojhayugmarasyamsaBalaVirupas: Double,
    val kendraBalaVirupas: Double,
    val drekkanaBalaVirupas: Double,
    val totalVirupas: Double,
    val totalRupas: Double,
    val isEvaluated: Boolean = true,
)

/**
 * Directional strength (Dig Bala).
 * Source: BPHS Chapter 28, Slokas 7-8 (ASTRO-R33).
 */
@Serializable
data class DigBala(
    val powerfulPointDegrees: Double,
    val zeroPointDegrees: Double,
    val arcDegrees: Double,
    val virupas: Double,
    val rupas: Double,
    val isEvaluated: Boolean = true,
)

/**
 * Natural permanent strength (Naisargika Bala).
 * Source: BPHS Chapter 28, Slokas 13-14 (ASTRO-R32).
 */
@Serializable
data class NaisargikaBala(
    val virupas: Double,
    val rupas: Double,
    val rank: Int,
    val isEvaluated: Boolean = true,
)

/**
 * Temporal strength (Kala Bala) and its classical subcomponents.
 * Source: BPHS Chapter 28, Slokas 14-18 (ASTRO-R36).
 */
@Serializable
data class KalaBala(
    val nathonnathaBalaVirupas: Double = 0.0,
    val pakshaBalaVirupas: Double = 0.0,
    val tribhagaBalaVirupas: Double = 0.0,
    val varaBalaVirupas: Double = 0.0,
    val horaBalaVirupas: Double = 0.0,
    val masaBalaVirupas: Double = 0.0,
    val varshaBalaVirupas: Double = 0.0,
    val ayanaBalaVirupas: Double = 0.0,
    val yuddhaBalaVirupas: Double = 0.0,
    val totalVirupas: Double = 0.0,
    val totalRupas: Double = 0.0,
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Motional strength (Chesta Bala).
 * Source: BPHS Chapter 28, Slokas 19-21 (ASTRO-R35).
 */
@Serializable
data class ChestaBala(
    val isRetrograde: Boolean = false,
    val dailyMotionDegrees: Double = 0.0,
    val chestaKendraDegrees: Double = 0.0,
    val virupas: Double = 0.0,
    val rupas: Double = 0.0,
    val motionCategory: String = "DIRECT",
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Aspectual strength (Drik Bala).
 * Source: BPHS Chapter 28, Slokas 22-24 (ASTRO-R37).
 */
@Serializable
data class DrikBala(
    val beneficAspectVirupas: Double = 0.0,
    val maleficAspectVirupas: Double = 0.0,
    val virupas: Double = 0.0,
    val rupas: Double = 0.0,
    val isEvaluated: Boolean = true,
    val deferredSubcomponents: List<String> = emptyList(),
)

/**
 * Comprehensive Shadbala strength breakdown for a single celestial body.
 * Source: BPHS Chapter 28 (ASTRO-R38).
 */
@Serializable
data class PlanetaryShadbala(
    val body: CelestialBody,
    val sthanaBala: SthanaBala,
    val digBala: DigBala,
    val naisargikaBala: NaisargikaBala,
    val kalaBala: KalaBala,
    val chestaBala: ChestaBala,
    val drikBala: DrikBala,
    val completeness: ShadbalaCompleteness,
    val isComplete: Boolean,
    val totalVirupas: Double?,
    val totalRupas: Double?,
    val deferredComponents: List<String> = emptyList(),
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
)

/**
 * Contributors in the classical Ashtakavarga system (7 planets + Lagna).
 * Source: BPHS Ch. 66 (ASTRO-R39).
 */
@Serializable
enum class AshtakavargaContributor {
    SUN,
    MOON,
    MARS,
    MERCURY,
    JUPITER,
    VENUS,
    SATURN,
    LAGNA,
}

/**
 * Ashtakavarga calculation completeness state.
 */
@Serializable
enum class AshtakavargaCompleteness {
    COMPLETE,
    PARTIAL,
    UNSUPPORTED,
}

/**
 * Score of a single zodiac sign in Bhinnashtakavarga (BAV).
 */
@Serializable
data class BhinnashtakavargaSignScore(
    val rashi: Rashi,
    val binduCount: Int,
    val rekhaCount: Int,
    val contributingBodies: List<AshtakavargaContributor>,
)

/**
 * Individual planetary Bhinnashtakavarga (BAV) chart.
 * Source: BPHS Ch. 66-72 (ASTRO-R39A-G).
 */
@Serializable
data class Bhinnashtakavarga(
    val targetBody: CelestialBody,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<BhinnashtakavargaSignScore>,
    val totalBindus: Int,
    val totalRekhas: Int,
    val contributorGrid: Map<AshtakavargaContributor, List<Int>>,
)

/**
 * Score of a single zodiac sign in Sarvashtakavarga (SAV).
 */
@Serializable
data class SarvashtakavargaSignScore(
    val rashi: Rashi,
    val totalBindus: Int,
    val totalRekhas: Int,
    val planetBindus: Map<CelestialBody, Int>,
)

/**
 * Aggregate Sarvashtakavarga (SAV) chart.
 * Source: BPHS Ch. 73 (ASTRO-R40).
 */
@Serializable
data class Sarvashtakavarga(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<SarvashtakavargaSignScore>,
    val grandTotalBindus: Int,
    val grandTotalRekhas: Int,
    val isInvariantValid: Boolean,
)

/**
 * Top-level Ashtakavarga calculation result.
 */
@Serializable
data class AshtakavargaResult(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val bhinnashtakavarga: Map<CelestialBody, Bhinnashtakavarga>,
    val sarvashtakavarga: Sarvashtakavarga,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<CelestialBody> = listOf(CelestialBody.RAHU, CelestialBody.KETU),
    val shodhana: ShodhitaAshtakavargaResult? = null,
    val pinda: AshtakavargaPinda? = null,
)

/**
 * Score of a single zodiac sign in Shodhita Bhinnashtakavarga (BAV),
 * tracking raw, Trikona-reduced, and final Ekadhipatya-reduced (Shodhita) figures.
 */
@Serializable
data class ShodhitaBhinnashtakavargaSignScore(
    val rashi: Rashi,
    val rawBindus: Int,
    val trikonaReducedBindus: Int,
    val ekadhipatyaReducedBindus: Int,
) {
    val shodhitaBindus: Int get() = ekadhipatyaReducedBindus
}

/**
 * Individual planetary Shodhita Bhinnashtakavarga chart.
 * Source: BPHS Ch. 73-74; Raman Ch. 4-5.
 */
@Serializable
data class ShodhitaBhinnashtakavarga(
    val targetBody: CelestialBody,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<ShodhitaBhinnashtakavargaSignScore>,
    val rawTotalBindus: Int,
    val trikonaTotalBindus: Int,
    val shodhitaTotalBindus: Int,
)

/**
 * Score of a single zodiac sign in Shodhita Sarvashtakavarga (SAV).
 */
@Serializable
data class ShodhitaSarvashtakavargaSignScore(
    val rashi: Rashi,
    val rawTotalBindus: Int,
    val trikonaTotalBindus: Int,
    val shodhitaTotalBindus: Int,
    val planetShodhitaBindus: Map<CelestialBody, Int>,
)

/**
 * Aggregate Shodhita Sarvashtakavarga chart derived by summing
 * the 7 classical planets' Shodhita BAV charts sign by sign.
 */
@Serializable
data class ShodhitaSarvashtakavarga(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val signScores: List<ShodhitaSarvashtakavargaSignScore>,
    val grandTotalRawBindus: Int,
    val grandTotalTrikonaBindus: Int,
    val grandTotalShodhitaBindus: Int,
)

/**
 * Top-level Shodhita Ashtakavarga calculation result.
 */
@Serializable
data class ShodhitaAshtakavargaResult(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val shodhitaBhinnashtakavarga: Map<CelestialBody, ShodhitaBhinnashtakavarga>,
    val shodhitaSarvashtakavarga: ShodhitaSarvashtakavarga,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<CelestialBody> = listOf(CelestialBody.RAHU, CelestialBody.KETU),
)

/**
 * Pinda calculation result for an individual classical planet.
 * Source: BPHS Ch. 74/75; Raman Ch. 6.
 */
@Serializable
data class PlanetaryPinda(
    val targetBody: CelestialBody,
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val rasiPinda: Int,
    val grahaPinda: Int,
    val shodhyaPinda: Int,
    val rasiContributions: Map<Rashi, Int>,
    val grahaContributions: Map<CelestialBody, Int>,
)

/**
 * Top-level Ashtakavarga Pinda calculation result across all 7 classical planets.
 */
@Serializable
data class AshtakavargaPinda(
    val rulesetId: String = "PARASHARA_CLASSICAL_V1",
    val planetaryPindas: Map<CelestialBody, PlanetaryPinda>,
    val totalRasiPinda: Int,
    val totalGrahaPinda: Int,
    val totalShodhyaPinda: Int,
    val completeness: AshtakavargaCompleteness,
    val unsupportedBodies: List<CelestialBody> = listOf(CelestialBody.RAHU, CelestialBody.KETU),
)




