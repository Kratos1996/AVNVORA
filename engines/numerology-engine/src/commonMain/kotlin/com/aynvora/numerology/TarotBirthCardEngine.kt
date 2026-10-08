package com.aynvora.numerology

/**
 * Pure, deterministic calculation engine for Tarot Birth Cards and Pair-Card Archetypes.
 *
 * Authorities:
 * - Mary K. Greer, "Tarot for Your Self: A Workbook for the Inward Journey" (1984), Chapter 2
 * - Angeles Arrien, "The Tarot Handbook: Practical Applications of Ancient Visual Symbols" (1987)
 */
object TarotBirthCardEngine {

    val MAJOR_ARCANA_NAMES: Map<Int, String> = mapOf(
        1 to "The Magician",
        2 to "The High Priestess",
        3 to "The Empress",
        4 to "The Emperor",
        5 to "The Hierophant",
        6 to "The Lovers",
        7 to "The Chariot",
        8 to "Strength",
        9 to "The Hermit",
        10 to "Wheel of Fortune",
        11 to "Justice",
        12 to "The Hanged Man",
        13 to "Death",
        14 to "Temperance",
        15 to "The Devil",
        16 to "The Tower",
        17 to "The Star",
        18 to "The Moon",
        19 to "The Sun",
        20 to "Judgement",
        21 to "The World",
        22 to "The Fool",
    )

    /**
     * Calculates the Personality Card and Soul Card according to Mary K. Greer's 1984 reduction algorithm.
     */
    fun calculate(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        rulesetId: String = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
    ): Pair<TarotBirthCardResult, NumerologyCalculationTrace> {
        val dateDisplay = "${birthDay.toString().padStart(2, '0')}-${
            birthMonth.toString().padStart(2, '0')
        }-$birthYear"

        // 1. Initial integer sum: MM + DD + YYYY
        val rawSum = birthMonth + birthDay + birthYear

        // 2. Reduce raw sum by adding its digits
        val steps = mutableListOf<ReductionStep>()
        var currentSum = sumDigits(rawSum)
        steps.add(
            ReductionStep(
                stepNumber = 1,
                startingValue = rawSum,
                digits = rawSum.toString().map { it.digitToInt() },
                reducedSum = currentSum,
                equation = "$birthMonth + $birthDay + $birthYear = $rawSum -> ${
                    rawSum.toString().map { it.toString() }.joinToString(" + ")
                } = $currentSum",
            )
        )

        var stepNum = 2
        while (currentSum > 22) {
            val nextSum = sumDigits(currentSum)
            steps.add(
                ReductionStep(
                    stepNumber = stepNum,
                    startingValue = currentSum,
                    digits = currentSum.toString().map { it.digitToInt() },
                    reducedSum = nextSum,
                    equation = "${
                        currentSum.toString().map { it.toString() }.joinToString(" + ")
                    } = $nextSum",
                )
            )
            currentSum = nextSum
            stepNum++
        }

        val personalityCardNum = currentSum
        val personalityCardName =
            MAJOR_ARCANA_NAMES[personalityCardNum] ?: "Unknown Card $personalityCardNum"

        // 3. Derive Soul Card from Personality Card
        val (soulCardNum, isPair, shadowNum) = when {
            personalityCardNum in 10..21 -> {
                val reducedSoul = sumDigits(personalityCardNum)
                val shadow = if (personalityCardNum == 19 && reducedSoul == 10) 1 else null
                Triple(reducedSoul, true, shadow)
            }

            personalityCardNum == 22 -> {
                // Card 22 represents The Fool (22/0)
                Triple(22, false, null)
            }

            else -> {
                // 1..9: Personality and Soul are the same single archetype in Greer's system
                Triple(personalityCardNum, false, null)
            }
        }

        val soulCardName = MAJOR_ARCANA_NAMES[soulCardNum] ?: "Unknown Card $soulCardNum"
        val shadowCardName = shadowNum?.let { MAJOR_ARCANA_NAMES[it] }

        val equation = if (isPair) {
            "Personality: $personalityCardNum ($personalityCardName), Soul: $soulCardNum ($soulCardName)"
        } else {
            "Single Archetype: $personalityCardNum ($personalityCardName)"
        }

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.TAROT_BIRTH_CARD,
            rulesetId = rulesetId,
            rawInput = dateDisplay,
            normalizedInput = "$rawSum",
            letterValues = emptyList(),
            compoundSum = rawSum,
            reductionSteps = steps,
            finalValue = personalityCardNum,
            isMasterNumber = false,
        )

        val result = TarotBirthCardResult(
            birthDateDisplay = dateDisplay,
            rawSum = rawSum,
            personalityCardNumber = personalityCardNum,
            personalityCardName = personalityCardName,
            soulCardNumber = soulCardNum,
            soulCardName = soulCardName,
            isPair = isPair,
            shadowCardNumber = shadowNum,
            shadowCardName = shadowCardName,
            rulesetId = rulesetId,
            sourceRule = "GREER_1984_TAROT_BIRTH_CARDS",
        )

        return Pair(result, trace)
    }

    private fun sumDigits(num: Int): Int {
        var n = num
        var sum = 0
        while (n > 0) {
            sum += n % 10
            n /= 10
        }
        return sum
    }
}
