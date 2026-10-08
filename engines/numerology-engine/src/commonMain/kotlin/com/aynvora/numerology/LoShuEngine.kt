package com.aynvora.numerology

/**
 * Pure, deterministic engine for calculating Classical Chinese Lo Shu 3x3 Magic Square grids,
 * digit frequencies, missing digits, and 8 Planes of Arrows.
 *
 * Authorities:
 * - I Ching (Book of Changes, Luoshu Scroll, Zhou Dynasty)
 * - Dr. David A. Phillips, "The Complete Book of Numerology" (1992), Ch. 5
 */
object LoShuEngine {

    /**
     * Canonical cell positions in the 3x3 Magic Square (Row 1: 4-9-2, Row 2: 3-5-7, Row 3: 8-1-6).
     * Every row, column, and diagonal sums to 15.
     */
    val CANONICAL_CELL_COORDINATES: Map<Int, Pair<Int, Int>> = mapOf(
        4 to Pair(1, 1),
        9 to Pair(1, 2),
        2 to Pair(1, 3),
        3 to Pair(2, 1),
        5 to Pair(2, 2),
        7 to Pair(2, 3),
        8 to Pair(3, 1),
        1 to Pair(3, 2),
        6 to Pair(3, 3),
    )

    /**
     * The 8 standard Lo Shu planes, their digits, and classical arrow names.
     */
    data class PlaneDefinition(
        val type: LoShuPlaneType,
        val digits: List<Int>,
        val planeName: String,
        val strengthName: String,
        val weaknessName: String,
    )

    val PLANES: List<PlaneDefinition> = listOf(
        // 3 Horizontal Planes (Rows)
        PlaneDefinition(
            type = LoShuPlaneType.MIND_PLANE,
            digits = listOf(4, 9, 2),
            planeName = "Mind Plane (Horizontal Row 1)",
            strengthName = "Arrow of Intellect",
            weaknessName = "Arrow of Poor Memory",
        ),
        PlaneDefinition(
            type = LoShuPlaneType.SOUL_PLANE,
            digits = listOf(3, 5, 7),
            planeName = "Soul Plane (Horizontal Row 2)",
            strengthName = "Arrow of Emotional Balance",
            weaknessName = "Arrow of Hypersensitivity",
        ),
        PlaneDefinition(
            type = LoShuPlaneType.PRACTICAL_PLANE,
            digits = listOf(8, 1, 6),
            planeName = "Practical Plane (Horizontal Row 3)",
            strengthName = "Arrow of Practicality",
            weaknessName = "Arrow of Disorder",
        ),

        // 3 Vertical Planes (Columns)
        PlaneDefinition(
            type = LoShuPlaneType.THOUGHT_PLANE,
            digits = listOf(4, 3, 8),
            planeName = "Thought Plane (Vertical Column 1)",
            strengthName = "Arrow of Planning",
            weaknessName = "Arrow of Indecision",
        ),
        PlaneDefinition(
            type = LoShuPlaneType.WILL_PLANE,
            digits = listOf(9, 5, 1),
            planeName = "Will Plane (Vertical Column 2)",
            strengthName = "Arrow of Willpower",
            weaknessName = "Arrow of Frustration",
        ),
        PlaneDefinition(
            type = LoShuPlaneType.ACTION_PLANE,
            digits = listOf(2, 7, 6),
            planeName = "Action Plane (Vertical Column 3)",
            strengthName = "Arrow of Action",
            weaknessName = "Arrow of Hesitation",
        ),

        // 2 Diagonal Planes
        PlaneDefinition(
            type = LoShuPlaneType.DETERMINATION_PLANE,
            digits = listOf(4, 5, 6),
            planeName = "Determination Plane (Diagonal 4-5-6)",
            strengthName = "Arrow of Determination",
            weaknessName = "Arrow of Vacillation",
        ),
        PlaneDefinition(
            type = LoShuPlaneType.COMPASSION_PLANE,
            digits = listOf(2, 5, 8),
            planeName = "Compassion Plane (Diagonal 2-5-8)",
            strengthName = "Arrow of Compassion & Spirituality",
            weaknessName = "Arrow of Skepticism",
        ),
    )

    /**
     * Calculates the complete Lo Shu Grid and calculation trace for a birth date.
     */
    fun calculateGrid(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
    ): Pair<LoShuGrid, NumerologyCalculationTrace> {
        val dStr = if (birthDay < 10) "0$birthDay" else "$birthDay"
        val mStr = if (birthMonth < 10) "0$birthMonth" else "$birthMonth"
        val yStr = birthYear.toString()
        val dateString = "$dStr-$mStr-$yStr"

        val extractedDigits = mutableListOf<Int>()
        var zeroCount = 0

        for (ch in dateString) {
            if (ch in '1'..'9') {
                extractedDigits.add(ch.digitToInt())
            } else if (ch == '0') {
                zeroCount++
            }
        }

        // Frequencies 1..9
        val counts = (1..9).associateWith { digit ->
            extractedDigits.count { it == digit }
        }

        val frequencies = (1..9).map { digit ->
            LoShuDigitFrequency(digit = digit, count = counts[digit] ?: 0)
        }

        // Canonical 9 Cells
        val cells = CANONICAL_CELL_COORDINATES.map { (digit, coords) ->
            LoShuCell(
                digit = digit,
                row = coords.first,
                column = coords.second,
                count = counts[digit] ?: 0,
            )
        }

        val presentDigits = (1..9).filter { (counts[it] ?: 0) > 0 }
        val missingDigits = (1..9).filter { (counts[it] ?: 0) == 0 }

        // Evaluate 8 planes
        val arrows = PLANES.map { planeDef ->
            val dCounts = planeDef.digits.map { counts[it] ?: 0 }
            val status = when {
                dCounts.all { it > 0 } -> LoShuArrowStatus.ARROW_OF_STRENGTH
                dCounts.all { it == 0 } -> LoShuArrowStatus.ARROW_OF_WEAKNESS
                else -> LoShuArrowStatus.PARTIAL
            }
            LoShuArrow(
                plane = planeDef.type,
                digits = planeDef.digits,
                status = status,
                planeName = planeDef.planeName,
                strengthName = planeDef.strengthName,
                weaknessName = planeDef.weaknessName,
            )
        }

        val grid = LoShuGrid(
            cells = cells,
            frequencies = frequencies,
            extractedDigits = extractedDigits,
            presentDigits = presentDigits,
            missingDigits = missingDigits,
            zeroDigitsCount = zeroCount,
            arrows = arrows,
            sourceRule = "LO_SHU_3X3_MAGIC_SQUARE",
        )

        val strengthArrows = arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_STRENGTH }
        val weaknessArrows = arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_WEAKNESS }

        val steps = listOf(
            ReductionStep(
                stepNumber = 1,
                startingValue = extractedDigits.size,
                digits = extractedDigits,
                reducedSum = extractedDigits.size,
                equation = "DOB: $dateString -> Extracted non-zero digits: $extractedDigits (zero digits filtered: $zeroCount)",
            ),
            ReductionStep(
                stepNumber = 2,
                startingValue = presentDigits.size,
                digits = presentDigits,
                reducedSum = presentDigits.size,
                equation = "Present digits: $presentDigits, Missing digits: $missingDigits",
            ),
            ReductionStep(
                stepNumber = 3,
                startingValue = cells.size,
                digits = cells.map { it.count },
                reducedSum = 9,
                equation = "Placed into 3x3 Magic Square (Row 1: 4-9-2, Row 2: 3-5-7, Row 3: 8-1-6)",
            ),
            ReductionStep(
                stepNumber = 4,
                startingValue = arrows.size,
                digits = listOf(strengthArrows.size, weaknessArrows.size),
                reducedSum = arrows.size,
                equation = "Evaluated 8 planes -> Strength arrows: ${strengthArrows.map { it.strengthName }}, Weakness arrows: ${weaknessArrows.map { it.weaknessName }}",
            ),
        )

        val trace = NumerologyCalculationTrace(
            calculationType = NumerologyCalculationType.LO_SHU_GRID,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
            rawInput = dateString,
            normalizedInput = "Digits:$extractedDigits (Zeros:$zeroCount)",
            compoundSum = extractedDigits.size,
            reductionSteps = steps,
            finalValue = extractedDigits.size,
            isMasterNumber = false,
        )

        return Pair(grid, trace)
    }
}
