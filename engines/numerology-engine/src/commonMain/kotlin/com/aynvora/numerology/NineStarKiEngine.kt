package com.aynvora.numerology

/**
 * Pure, deterministic calculation engine for Chinese Nine Star Ki (Kyuusei Kigaku).
 *
 * Authorities:
 * - Xuan Kong Fei Xing (Flying Star Feng Shui treatises)
 * - I Ching Solar Calendar (Solar Year determined by Li Chun, 315.0° solar longitude)
 * - Dr. David A. Phillips, "The Complete Book of Numerology" (1992)
 */
object NineStarKiEngine {

    val NINE_STARS: Map<Int, NineStar> = mapOf(
        1 to NineStar(1, "1 White Water", "Kan", "Water", "North"),
        2 to NineStar(2, "2 Black Earth", "Kun", "Earth", "Southwest"),
        3 to NineStar(3, "3 Jade Wood", "Zhen", "Wood", "East"),
        4 to NineStar(4, "4 Green Wood", "Xun", "Wood", "Southeast"),
        5 to NineStar(5, "5 Yellow Earth", "Taiji", "Earth", "Center"),
        6 to NineStar(6, "6 White Metal", "Qian", "Metal", "Northwest"),
        7 to NineStar(7, "7 Red Metal", "Dui", "Metal", "West"),
        8 to NineStar(8, "8 White Earth", "Gen", "Earth", "Northeast"),
        9 to NineStar(9, "9 Purple Fire", "Li", "Fire", "South"),
    )

    /**
     * Calculates the Nine Star Ki Principal Star for a given birth date and time.
     *
     * Accurately determines the Chinese solar year using astronomical Li Chun transition.
     */
    fun calculate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
        rulesetId: String = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
    ): Pair<NineStarKiResult, NumerologyCalculationTrace> {
        val (solarYear, isBeforeLiChun) = SolarTermEngine.determineSolarYear(
            year,
            month,
            day,
            hour,
            minute
        )
        val (_, liChunEpochMs) = SolarTermEngine.calculateLiChun(year)
        val birthJd = SolarTermEngine.gregorianToJulianDay(year, month, day, hour, minute, 0)
        val birthEpochMs = SolarTermEngine.julianDayToEpochMs(birthJd)

        // 1. Reduce solar year digits to single digit
        val yearReduction = NumerologyReductionEngine.reduce(
            solarYear,
            MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
        )
        val reducedYearDigit = yearReduction.finalValue

        // 2. Subtract reduced year digit from 11 (classical formula for principal star)
        var rawStarNumber = 11 - reducedYearDigit
        if (rawStarNumber > 9) {
            val rem = rawStarNumber % 9
            rawStarNumber = if (rem == 0) 9 else rem
        } else if (rawStarNumber <= 0) {
            rawStarNumber += 9
        }

        val principalStar =
            NINE_STARS[rawStarNumber] ?: NineStar(rawStarNumber, "$rawStarNumber Star", "", "", "")

        val dateDisplay =
            "${day.toString().padStart(2, '0')}-${month.toString().padStart(2, '0')}-$year"
        val equation =
            "Solar Year $solarYear (Li Chun transition ${if (isBeforeLiChun) "PENDING" else "PASSED"}) -> Sum $reducedYearDigit -> 11 - $reducedYearDigit = $rawStarNumber (${principalStar.starName})"

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.NINE_STAR_KI_PRINCIPAL,
            rulesetId = rulesetId,
            rawInput = "$dateDisplay ${hour.toString().padStart(2, '0')}:${
                minute.toString().padStart(2, '0')
            } UTC",
            normalizedInput = "SolarYear=$solarYear,LiChunBefore=$isBeforeLiChun",
            letterValues = emptyList(),
            compoundSum = solarYear,
            reductionSteps = listOf(
                ReductionStep(
                    stepNumber = 1,
                    startingValue = solarYear,
                    digits = listOf(reducedYearDigit),
                    reducedSum = rawStarNumber,
                    equation = equation,
                )
            ),
            finalValue = rawStarNumber,
            isMasterNumber = false,
        )

        val result = NineStarKiResult(
            birthDateDisplay = dateDisplay,
            birthTimestampUtc = birthEpochMs,
            solarYear = solarYear,
            liChunInstantUtc = liChunEpochMs,
            isBeforeLiChun = isBeforeLiChun,
            principalStar = principalStar,
            rulesetId = rulesetId,
            sourceRule = "XUAN_KONG_NINE_STAR_KI",
        )

        return Pair(result, trace)
    }
}
