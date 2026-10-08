package com.aynvora.numerology

/**
 * Result of a deterministic digit reduction, including all intermediate steps.
 */
data class ReductionResult(
    val startingValue: Int,
    val finalValue: Int,
    val isMasterNumber: Boolean,
    val steps: List<ReductionStep>,
)

/**
 * Reusable, deterministic reduction engine implementing digit-sum reduction
 * and master-number exception policies.
 */
object NumerologyReductionEngine {

    /**
     * Reduces a positive integer to a single digit (1..9) or an accepted master number
     * according to the specified [policy].
     */
    fun reduce(value: Int, policy: MasterNumberPolicy): ReductionResult {
        require(value > 0) { "Numerology reduction input must be positive: $value" }

        // If already a master number under policy
        if (isMasterNumber(value, policy)) {
            return ReductionResult(
                startingValue = value,
                finalValue = value,
                isMasterNumber = true,
                steps = emptyList(),
            )
        }

        // If already a single digit
        if (value in 1..9) {
            return ReductionResult(
                startingValue = value,
                finalValue = value,
                isMasterNumber = false,
                steps = emptyList(),
            )
        }

        val steps = mutableListOf<ReductionStep>()
        var current = value
        var stepCount = 1

        while (current > 9 && !isMasterNumber(current, policy) && stepCount <= 10) {
            val digits = extractDigits(current)
            val sum = digits.sum()
            val equation = "${digits.joinToString(" + ")} = $sum"

            steps += ReductionStep(
                stepNumber = stepCount,
                startingValue = current,
                digits = digits,
                reducedSum = sum,
                equation = equation,
            )

            current = sum
            stepCount++
        }

        val isMaster = isMasterNumber(current, policy)
        return ReductionResult(
            startingValue = value,
            finalValue = current,
            isMasterNumber = isMaster,
            steps = steps,
        )
    }

    /**
     * Determines whether a value qualifies as a master number under the given [policy].
     */
    fun isMasterNumber(value: Int, policy: MasterNumberPolicy): Boolean = when (policy) {
        MasterNumberPolicy.PRESERVE_11_22_33 -> value == 11 || value == 22 || value == 33
        MasterNumberPolicy.PRESERVE_11_22 -> value == 11 || value == 22
        MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT -> false
    }

    /**
     * Reduces a master number to its root base digit (e.g. 11 -> 2, 22 -> 4, 33 -> 6).
     */
    fun reduceToRoot(value: Int): Int {
        if (value in 1..9) return value
        var current = value
        while (current > 9) {
            current = extractDigits(current).sum()
        }
        return current
    }

    /**
     * Extracts decimal digits of a positive integer in order from most to least significant.
     */
    fun extractDigits(number: Int): List<Int> {
        require(number >= 0) { "number must be non-negative: $number" }
        if (number == 0) return listOf(0)
        return number.toString().map { it.digitToInt() }
    }
}
