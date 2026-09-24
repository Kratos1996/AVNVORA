package com.aynvora.astro.varga

import kotlin.math.floor

/**
 * Result of computing a divisional chart mapping for a single sidereal coordinate.
 */
data class VargaDivisionResult(
    val sourceRashiIndex: Int,
    val divisionIndex: Int,
    val resultingRashiIndex: Int,
    val degreeInResultingRashi: Double,
    val resultingLongitude: Double,
)

/**
 * Strategy contract for computing Vedic divisional charts (Vargas).
 */
interface VargaCalculationStrategy {
    val chart: DivisionalChart
    fun calculate(siderealLongitude: Double): VargaDivisionResult
}

/**
 * Base helper providing rigorous angular normalization and half-open boundary intervals [start, end).
 */
abstract class AbstractVargaStrategy(override val chart: DivisionalChart) : VargaCalculationStrategy {

    protected fun normalizeDegrees(degrees: Double): Double {
        val normalized = degrees % 360.0
        return if (normalized < 0.0) normalized + 360.0 else normalized
    }

    protected fun decomposeSign(siderealLongitude: Double): Pair<Int, Double> {
        val normalized = normalizeDegrees(siderealLongitude)
        var sign = floor(normalized / 30.0).toInt().coerceIn(0, 11)
        var degreeInSign = normalized - (sign * 30.0)

        // Safety for floating-point 30.0 edge case
        if (degreeInSign >= 30.0) {
            sign = (sign + 1) % 12
            degreeInSign = 0.0
        } else if (degreeInSign < 0.0) {
            degreeInSign = 0.0
        }
        return Pair(sign, degreeInSign)
    }

    protected fun computeEqualDivision(
        siderealLongitude: Double,
        divisions: Int,
        startSignCalculator: (sourceSign: Int) -> Int,
    ): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val span = 30.0 / divisions.toDouble()
        val divisionIndex = floor(degreeInSign / span).toInt().coerceIn(0, divisions - 1)
        val startSign = startSignCalculator(sourceSign)
        val resultingRashi = (startSign + divisionIndex) % 12
        val degreeInPart = degreeInSign - (divisionIndex * span)
        val degreeInResulting = ((degreeInPart / span) * 30.0).coerceIn(0.0, 30.0)
        val resultingLong = normalizeDegrees((resultingRashi * 30.0) + degreeInResulting)

        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = divisionIndex,
            resultingRashiIndex = resultingRashi,
            degreeInResultingRashi = degreeInResulting,
            resultingLongitude = resultingLong,
        )
    }
}

/**
 * D1: Rashi Chart (Base Nirayana Chart)
 * BPHS Ch. 6, slokas 3-4 (ASTRO-R09)
 */
class D1RashiStrategy : AbstractVargaStrategy(DivisionalChart.D1) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val normalized = normalizeDegrees(siderealLongitude)
        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = 0,
            resultingRashiIndex = sourceSign,
            degreeInResultingRashi = degreeInSign,
            resultingLongitude = normalized,
        )
    }
}

/**
 * D2: Hora Chart (Sun/Moon Parashara Hora)
 * BPHS Ch. 6, slokas 5-6 (ASTRO-R10)
 */
class D2HoraStrategy : AbstractVargaStrategy(DivisionalChart.D2) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val divisionIndex = floor(degreeInSign / 15.0).toInt().coerceIn(0, 1)
        val isOddSign = (sourceSign % 2 == 0) // 0-indexed: Aries=0 (odd), Taurus=1 (even)

        val resultingRashi = if (isOddSign) {
            if (divisionIndex == 0) 4 /* Leo / Sun */ else 3 /* Cancer / Moon */
        } else {
            if (divisionIndex == 0) 3 /* Cancer / Moon */ else 4 /* Leo / Sun */
        }

        val degreeInPart = degreeInSign - (divisionIndex * 15.0)
        val degreeInResulting = ((degreeInPart / 15.0) * 30.0).coerceIn(0.0, 30.0)
        val resultingLong = normalizeDegrees((resultingRashi * 30.0) + degreeInResulting)

        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = divisionIndex,
            resultingRashiIndex = resultingRashi,
            degreeInResultingRashi = degreeInResulting,
            resultingLongitude = resultingLong,
        )
    }
}

/**
 * D3: Drekkana Chart
 * BPHS Ch. 6, slokas 7-8 (ASTRO-R11)
 * 1st part: 1st, 2nd part: 5th, 3rd part: 9th
 */
class D3DrekkanaStrategy : AbstractVargaStrategy(DivisionalChart.D3) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val divisionIndex = floor(degreeInSign / 10.0).toInt().coerceIn(0, 2)
        val resultingRashi = (sourceSign + (divisionIndex * 4)) % 12
        val degreeInPart = degreeInSign - (divisionIndex * 10.0)
        val degreeInResulting = ((degreeInPart / 10.0) * 30.0).coerceIn(0.0, 30.0)
        val resultingLong = normalizeDegrees((resultingRashi * 30.0) + degreeInResulting)

        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = divisionIndex,
            resultingRashiIndex = resultingRashi,
            degreeInResultingRashi = degreeInResulting,
            resultingLongitude = resultingLong,
        )
    }
}

/**
 * D4: Chaturthamsa (Turyamsa) Chart
 * BPHS Ch. 6, sloka 9 (ASTRO-R12)
 * Kendra signs: 1st, 4th, 7th, 10th
 */
class D4ChaturthamsaStrategy : AbstractVargaStrategy(DivisionalChart.D4) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val divisionIndex = floor(degreeInSign / 7.5).toInt().coerceIn(0, 3)
        val resultingRashi = (sourceSign + (divisionIndex * 3)) % 12
        val degreeInPart = degreeInSign - (divisionIndex * 7.5)
        val degreeInResulting = ((degreeInPart / 7.5) * 30.0).coerceIn(0.0, 30.0)
        val resultingLong = normalizeDegrees((resultingRashi * 30.0) + degreeInResulting)

        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = divisionIndex,
            resultingRashiIndex = resultingRashi,
            degreeInResultingRashi = degreeInResulting,
            resultingLongitude = resultingLong,
        )
    }
}

/**
 * D7: Saptamsa Chart
 * BPHS Ch. 6, slokas 10-11 (ASTRO-R13)
 * Odd signs: from sign itself; Even signs: from 7th sign
 */
class D7SaptamsaStrategy : AbstractVargaStrategy(DivisionalChart.D7) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 7) { sourceSign ->
            val isOddSign = (sourceSign % 2 == 0)
            if (isOddSign) sourceSign else (sourceSign + 6) % 12
        }
}

/**
 * D9: Navamsa Chart
 * BPHS Ch. 6, slokas 12-14 (ASTRO-R14)
 * Elemental starting signs: Fiery->Aries, Earthy->Capricorn, Airy->Libra, Watery->Cancer
 */
class D9NavamsaStrategy : AbstractVargaStrategy(DivisionalChart.D9) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 9) { sourceSign ->
            when (sourceSign % 4) {
                0 -> 0 // Fiery (Aries, Leo, Sag) -> starts Aries (0)
                1 -> 9 // Earthy (Taurus, Virgo, Cap) -> starts Capricorn (9)
                2 -> 6 // Airy (Gemini, Libra, Aqu) -> starts Libra (6)
                3 -> 3 // Watery (Cancer, Scorpio, Pis) -> starts Cancer (3)
                else -> 0
            }
        }
}

/**
 * D10: Dasamsa Chart
 * BPHS Ch. 6, slokas 15-16 (ASTRO-R15)
 * Odd signs: from sign itself; Even signs: from 9th sign
 */
class D10DasamsaStrategy : AbstractVargaStrategy(DivisionalChart.D10) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 10) { sourceSign ->
            val isOddSign = (sourceSign % 2 == 0)
            if (isOddSign) sourceSign else (sourceSign + 8) % 12
        }
}

/**
 * D12: Dwadasamsa Chart
 * BPHS Ch. 6, slokas 17-18 (ASTRO-R16)
 * Continuous count from sign itself
 */
class D12DwadasamsaStrategy : AbstractVargaStrategy(DivisionalChart.D12) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 12) { sourceSign -> sourceSign }
}

/**
 * D16: Shodasamsa (Kalamsa) Chart
 * BPHS Ch. 6, slokas 19-21 (ASTRO-R17)
 * Mobility: Movable->Aries, Fixed->Leo, Dual->Sagittarius
 */
class D16ShodasamsaStrategy : AbstractVargaStrategy(DivisionalChart.D16) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 16) { sourceSign ->
            when (sourceSign % 3) {
                0 -> 0 // Movable -> Aries
                1 -> 4 // Fixed -> Leo
                2 -> 8 // Dual -> Sagittarius
                else -> 0
            }
        }
}

/**
 * D20: Vimsamsa Chart
 * BPHS Ch. 6, slokas 22-23 (ASTRO-R18)
 * Mobility: Movable->Aries, Fixed->Sagittarius, Dual->Leo
 */
class D20VimsamsaStrategy : AbstractVargaStrategy(DivisionalChart.D20) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 20) { sourceSign ->
            when (sourceSign % 3) {
                0 -> 0 // Movable -> Aries
                1 -> 8 // Fixed -> Sagittarius
                2 -> 4 // Dual -> Leo
                else -> 0
            }
        }
}

/**
 * D24: Chaturvimsamsa (Siddhamsa) Chart
 * BPHS Ch. 6, slokas 24-25 (ASTRO-R19)
 * Odd signs: starts Leo (4); Even signs: starts Cancer (3)
 */
class D24ChaturvimsamsaStrategy : AbstractVargaStrategy(DivisionalChart.D24) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 24) { sourceSign ->
            val isOddSign = (sourceSign % 2 == 0)
            if (isOddSign) 4 else 3
        }
}

/**
 * D27: Bhamsa (Saptavimsamsa / Nakshatramsa) Chart
 * BPHS Ch. 6, slokas 26-27 (ASTRO-R20)
 * Triplicities: Fiery->Aries, Earthy->Cancer, Airy->Libra, Watery->Capricorn
 */
class D27BhamsaStrategy : AbstractVargaStrategy(DivisionalChart.D27) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 27) { sourceSign ->
            when (sourceSign % 4) {
                0 -> 0 // Fiery -> Aries
                1 -> 3 // Earthy -> Cancer
                2 -> 6 // Airy -> Libra
                3 -> 9 // Watery -> Capricorn
                else -> 0
            }
        }
}

/**
 * D30: Trimsamsa Chart
 * BPHS Ch. 6, slokas 28-31 (ASTRO-R21)
 * 5 unequal planetary degree portions.
 */
class D30TrimsamsaStrategy : AbstractVargaStrategy(DivisionalChart.D30) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult {
        val (sourceSign, degreeInSign) = decomposeSign(siderealLongitude)
        val isOddSign = (sourceSign % 2 == 0)

        val divisionIndex: Int
        val resultingRashi: Int
        val startDeg: Double
        val span: Double

        if (isOddSign) {
            when {
                degreeInSign < 5.0 -> {
                    divisionIndex = 0
                    resultingRashi = 0 // Aries (Mars)
                    startDeg = 0.0
                    span = 5.0
                }
                degreeInSign < 10.0 -> {
                    divisionIndex = 1
                    resultingRashi = 10 // Aquarius (Saturn)
                    startDeg = 5.0
                    span = 5.0
                }
                degreeInSign < 18.0 -> {
                    divisionIndex = 2
                    resultingRashi = 8 // Sagittarius (Jupiter)
                    startDeg = 10.0
                    span = 8.0
                }
                degreeInSign < 25.0 -> {
                    divisionIndex = 3
                    resultingRashi = 2 // Gemini (Mercury)
                    startDeg = 18.0
                    span = 7.0
                }
                else -> {
                    divisionIndex = 4
                    resultingRashi = 6 // Libra (Venus)
                    startDeg = 25.0
                    span = 5.0
                }
            }
        } else {
            when {
                degreeInSign < 5.0 -> {
                    divisionIndex = 0
                    resultingRashi = 1 // Taurus (Venus)
                    startDeg = 0.0
                    span = 5.0
                }
                degreeInSign < 12.0 -> {
                    divisionIndex = 1
                    resultingRashi = 5 // Virgo (Mercury)
                    startDeg = 5.0
                    span = 7.0
                }
                degreeInSign < 20.0 -> {
                    divisionIndex = 2
                    resultingRashi = 11 // Pisces (Jupiter)
                    startDeg = 12.0
                    span = 8.0
                }
                degreeInSign < 25.0 -> {
                    divisionIndex = 3
                    resultingRashi = 9 // Capricorn (Saturn)
                    startDeg = 20.0
                    span = 5.0
                }
                else -> {
                    divisionIndex = 4
                    resultingRashi = 7 // Scorpio (Mars)
                    startDeg = 25.0
                    span = 5.0
                }
            }
        }

        val degreeInPart = degreeInSign - startDeg
        val degreeInResulting = ((degreeInPart / span) * 30.0).coerceIn(0.0, 30.0)
        val resultingLong = normalizeDegrees((resultingRashi * 30.0) + degreeInResulting)

        return VargaDivisionResult(
            sourceRashiIndex = sourceSign,
            divisionIndex = divisionIndex,
            resultingRashiIndex = resultingRashi,
            degreeInResultingRashi = degreeInResulting,
            resultingLongitude = resultingLong,
        )
    }
}

/**
 * D40: Khavedamsa (Swavedamsa) Chart
 * BPHS Ch. 6, slokas 32-33 (ASTRO-R22)
 * Odd signs: starts Aries (0); Even signs: starts Libra (6)
 */
class D40KhavedamsaStrategy : AbstractVargaStrategy(DivisionalChart.D40) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 40) { sourceSign ->
            val isOddSign = (sourceSign % 2 == 0)
            if (isOddSign) 0 else 6
        }
}

/**
 * D45: Akshavedamsa Chart
 * BPHS Ch. 6, slokas 34-35 (ASTRO-R23)
 * Mobility: Movable->Aries, Fixed->Leo, Dual->Sagittarius
 */
class D45AkshavedamsaStrategy : AbstractVargaStrategy(DivisionalChart.D45) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 45) { sourceSign ->
            when (sourceSign % 3) {
                0 -> 0 // Movable -> Aries
                1 -> 4 // Fixed -> Leo
                2 -> 8 // Dual -> Sagittarius
                else -> 0
            }
        }
}

/**
 * D60: Shashtiamsa Chart
 * BPHS Ch. 6, slokas 36-42 & Dr. B. V. Raman Ch. 9 (ASTRO-R24)
 * 60 divisions of 0.5°. Cyclic counting from sign itself.
 */
class D60ShashtiamsaStrategy : AbstractVargaStrategy(DivisionalChart.D60) {
    override fun calculate(siderealLongitude: Double): VargaDivisionResult =
        computeEqualDivision(siderealLongitude, 60) { sourceSign -> sourceSign }
}

/**
 * Central registry of calculation strategies.
 */
object VargaStrategyRegistry {
    private val strategies: Map<DivisionalChart, VargaCalculationStrategy> = mapOf(
        DivisionalChart.D1 to D1RashiStrategy(),
        DivisionalChart.D2 to D2HoraStrategy(),
        DivisionalChart.D3 to D3DrekkanaStrategy(),
        DivisionalChart.D4 to D4ChaturthamsaStrategy(),
        DivisionalChart.D7 to D7SaptamsaStrategy(),
        DivisionalChart.D9 to D9NavamsaStrategy(),
        DivisionalChart.D10 to D10DasamsaStrategy(),
        DivisionalChart.D12 to D12DwadasamsaStrategy(),
        DivisionalChart.D16 to D16ShodasamsaStrategy(),
        DivisionalChart.D20 to D20VimsamsaStrategy(),
        DivisionalChart.D24 to D24ChaturvimsamsaStrategy(),
        DivisionalChart.D27 to D27BhamsaStrategy(),
        DivisionalChart.D30 to D30TrimsamsaStrategy(),
        DivisionalChart.D40 to D40KhavedamsaStrategy(),
        DivisionalChart.D45 to D45AkshavedamsaStrategy(),
        DivisionalChart.D60 to D60ShashtiamsaStrategy(),
    )

    fun getStrategy(chart: DivisionalChart): VargaCalculationStrategy =
        strategies[chart] ?: error("No calculation strategy registered for divisional chart $chart")
}
