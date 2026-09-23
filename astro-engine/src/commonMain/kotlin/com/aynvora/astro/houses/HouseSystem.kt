package com.aynvora.astro.houses

import com.aynvora.astro.BodyId
import com.aynvora.astro.lagna.LagnaPosition
import com.aynvora.astro.math.AstroMath.normalizeDegrees
import com.aynvora.astro.zodiac.ZodiacCalculator
import kotlinx.serialization.Serializable

/**
 * Calculated astrological house (Bhava) details.
 */
@Serializable
data class HousePosition(
    val houseNumber: Int, // 1..12
    val cuspLongitude: Double,
    val startLongitude: Double,
    val endLongitude: Double,
    val rashiIndex: Int,
    val rashiName: String,
    val degreeInRashi: Double,
    val houseSystem: String,
)

/**
 * Input parameters required for house calculation.
 */
data class HouseCalculationInput(
    val lagna: LagnaPosition,
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val ayanamsaDegrees: Double,
    val planetPositions: Map<BodyId, Double>, // BodyId -> sidereal longitude
)

/**
 * Output of house and planet-to-house assignment calculation.
 */
@Serializable
data class HouseCalculationResult(
    val houseSystem: String,
    val houses: List<HousePosition>,
    val planetHouseOccupancy: Map<BodyId, Int>, // BodyId -> HouseNumber (1..12)
)

/**
 * Pluggable abstraction for astrological house division systems.
 */
interface HouseSystemCalculator {
    val systemName: String

    fun calculate(input: HouseCalculationInput): HouseCalculationResult
}

/**
 * Whole Sign (Rashi Bhava) house calculator.
 *
 * In the Whole Sign system:
 * - House 1 is the entire 30° zodiac sign containing the Sidereal Ascendant.
 * - Each subsequent house occupies the entire subsequent 30° zodiac sign.
 * - Boundary intervals are strictly [start, end) aligned with zodiac sign boundaries.
 */
object WholeSignHouseCalculator : HouseSystemCalculator {
    override val systemName: String = "WHOLE_SIGN"

    override fun calculate(input: HouseCalculationInput): HouseCalculationResult {
        val lagnaRashiIndex = input.lagna.rashiIndex
        val houses = mutableListOf<HousePosition>()

        for (h in 1..12) {
            val signIndex = (lagnaRashiIndex + (h - 1)) % 12
            val startDeg = normalizeDegrees(signIndex * 30.0)
            val endDeg = normalizeDegrees((signIndex + 1) * 30.0)
            val cuspDeg = if (h == 1) {
                input.lagna.siderealLongitude
            } else {
                normalizeDegrees(startDeg + input.lagna.degreeInRashi)
            }
            val rashiInfo = ZodiacCalculator.calculateRashi(cuspDeg)

            houses.add(
                HousePosition(
                    houseNumber = h,
                    cuspLongitude = cuspDeg,
                    startLongitude = startDeg,
                    endLongitude = endDeg,
                    rashiIndex = rashiInfo.index,
                    rashiName = rashiInfo.name,
                    degreeInRashi = rashiInfo.degreeInRashi,
                    houseSystem = systemName,
                ),
            )
        }

        val planetHouseOccupancy = mutableMapOf<BodyId, Int>()
        for ((bodyId, siderealLong) in input.planetPositions) {
            val norm = normalizeDegrees(siderealLong)
            val planetSignIndex = (norm / 30.0).toInt().coerceIn(0, 11)
            val houseNumber = ((planetSignIndex - lagnaRashiIndex + 12) % 12) + 1
            planetHouseOccupancy[bodyId] = houseNumber
        }

        return HouseCalculationResult(
            houseSystem = systemName,
            houses = houses,
            planetHouseOccupancy = planetHouseOccupancy,
        )
    }
}

/**
 * Equal House (30-degree divisions from Ascendant) house calculator.
 *
 * In the Equal House system:
 * - House 1 begins at the Sidereal Ascendant longitude.
 * - Each subsequent house spans exactly 30°: House n = normalize(Asc + (n - 1) * 30°).
 * - Boundaries are strictly [start, end) intervals with safe 360° wraparound.
 */
object EqualHouseCalculator : HouseSystemCalculator {
    override val systemName: String = "EQUAL_HOUSE"

    override fun calculate(input: HouseCalculationInput): HouseCalculationResult {
        val asc = input.lagna.siderealLongitude
        val houses = mutableListOf<HousePosition>()

        for (h in 1..12) {
            val cuspDeg = normalizeDegrees(asc + ((h - 1) * 30.0))
            val startDeg = cuspDeg
            val endDeg = normalizeDegrees(asc + (h * 30.0))
            val rashiInfo = ZodiacCalculator.calculateRashi(cuspDeg)

            houses.add(
                HousePosition(
                    houseNumber = h,
                    cuspLongitude = cuspDeg,
                    startLongitude = startDeg,
                    endLongitude = endDeg,
                    rashiIndex = rashiInfo.index,
                    rashiName = rashiInfo.name,
                    degreeInRashi = rashiInfo.degreeInRashi,
                    houseSystem = systemName,
                ),
            )
        }

        val planetHouseOccupancy = mutableMapOf<BodyId, Int>()
        for ((bodyId, siderealLong) in input.planetPositions) {
            val norm = normalizeDegrees(siderealLong)
            val offset = normalizeDegrees(norm - asc)
            val houseIdx = (offset / 30.0).toInt().coerceIn(0, 11)
            val houseNumber = houseIdx + 1
            planetHouseOccupancy[bodyId] = houseNumber
        }

        return HouseCalculationResult(
            houseSystem = systemName,
            houses = houses,
            planetHouseOccupancy = planetHouseOccupancy,
        )
    }
}

/**
 * Placidus house system calculator placeholder.
 *
 * In accordance with Phase 5.1 specifications, semi-arc quadrant systems that require
 * iterative convergence or become mathematically undefined at polar latitudes are
 * explicitly designated unsupported until certified astronomical implementations are provided.
 */
object PlacidusHouseCalculator : HouseSystemCalculator {
    override val systemName: String = "PLACIDUS"

    override fun calculate(input: HouseCalculationInput): HouseCalculationResult {
        throw UnsupportedOperationException(
            "Placidus house system is not supported in the current engine version. " +
                "Supported house systems: WHOLE_SIGN, EQUAL_HOUSE.",
        )
    }
}

/**
 * Registry resolving [HouseSystemCalculator] strategies by identifier.
 */
object HouseSystemRegistry {

    fun forName(name: String): HouseSystemCalculator = when (name.uppercase()) {
        "WHOLE_SIGN", "WHOLE_SIGN_HOUSE", "RASHI_BHAVA" -> WholeSignHouseCalculator
        "EQUAL_HOUSE", "EQUAL", "SRIPATI_EQUAL" -> EqualHouseCalculator
        "PLACIDUS" -> PlacidusHouseCalculator
        else -> throw UnsupportedOperationException(
            "House system '$name' is not supported in the current engine version. " +
                "Supported house systems: WHOLE_SIGN, EQUAL_HOUSE.",
        )
    }
}
