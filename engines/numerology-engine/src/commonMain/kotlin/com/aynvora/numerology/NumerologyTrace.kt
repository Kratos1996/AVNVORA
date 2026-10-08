package com.aynvora.numerology

import kotlinx.serialization.Serializable

/**
 * A single step in an iterative digit reduction sequence.
 */
@Serializable
data class ReductionStep(
    val stepNumber: Int,
    val startingValue: Int,
    val digits: List<Int>,
    val reducedSum: Int,
    val equation: String,
)

/**
 * Complete, immutable calculation trace explaining how a final numerological value was derived.
 * Enables full provenance and transparency without embedding presentation strings in domain calculations.
 */
@Serializable
data class NumerologyCalculationTrace(
    val calculationType: NumerologyCalculationType,
    val rulesetId: String,
    val rawInput: String,
    val normalizedInput: String,
    val letterValues: List<Int>? = null,
    val compoundSum: Int,
    val reductionSteps: List<ReductionStep>,
    val finalValue: Int,
    val isMasterNumber: Boolean,
)
