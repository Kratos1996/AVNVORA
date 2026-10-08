package com.aynvora.numerology

import kotlinx.serialization.Serializable

/**
 * A single cell in the 3x3 Lo Shu Magic Square grid.
 *
 * In the standard Luoshu orientation:
 * Row 1 (top): 4, 9, 2
 * Row 2 (mid): 3, 5, 7
 * Row 3 (bot): 8, 1, 6
 */
@Serializable
data class LoShuCell(
    val digit: Int,
    val row: Int,       // 1..3 (1=Top, 2=Middle, 3=Bottom)
    val column: Int,    // 1..3 (1=Left, 2=Center, 3=Right)
    val count: Int,     // Frequency of digit in DOB (0 if absent)
)

/**
 * Digit frequency count for each digit from 1 to 9.
 */
@Serializable
data class LoShuDigitFrequency(
    val digit: Int,     // 1..9
    val count: Int,     // >= 0
)

/**
 * The 8 standard planes of the 3x3 Lo Shu Magic Square.
 *
 * Authorities:
 * - I Ching (Book of Changes, Luoshu River Scroll)
 * - Dr. David A. Phillips, "The Complete Book of Numerology" (1992), Ch. 5
 */
@Serializable
enum class LoShuPlaneType {
    // 3 Horizontal Planes (Rows)
    MIND_PLANE,         // Row 1: 4, 9, 2 (Mental / Intellectual Plane)
    SOUL_PLANE,         // Row 2: 3, 5, 7 (Emotional / Spiritual Plane)
    PRACTICAL_PLANE,    // Row 3: 8, 1, 6 (Physical / Material Plane)

    // 3 Vertical Planes (Columns)
    THOUGHT_PLANE,      // Column 1: 4, 3, 8 (Planning / Ideation Plane)
    WILL_PLANE,         // Column 2: 9, 5, 1 (Persistence / Determination Plane)
    ACTION_PLANE,       // Column 3: 2, 7, 6 (Execution / Activity Plane)

    // 2 Diagonal Planes
    DETERMINATION_PLANE,// Diagonal 1: 4, 5, 6 (Persistence / Success)
    COMPASSION_PLANE,   // Diagonal 2: 2, 5, 8 (Spirituality / Empathy)
}

/**
 * Status of an arrow across any of the 8 Lo Shu planes.
 */
@Serializable
enum class LoShuArrowStatus {
    /** All 3 numbers in the plane are present (frequency >= 1 for each). */
    ARROW_OF_STRENGTH,

    /** All 3 numbers in the plane are completely absent (frequency == 0 for each). */
    ARROW_OF_WEAKNESS,

    /** Some numbers present, some absent. */
    PARTIAL,
}

/**
 * Structured evaluation of an arrow pattern across a Lo Shu plane.
 */
@Serializable
data class LoShuArrow(
    val plane: LoShuPlaneType,
    val digits: List<Int>,
    val status: LoShuArrowStatus,
    val planeName: String,
    val strengthName: String,
    val weaknessName: String,
)

/**
 * Structured, immutable model of the complete Lo Shu 3x3 Magic Square grid.
 */
@Serializable
data class LoShuGrid(
    val cells: List<LoShuCell>,                       // 9 canonical cells exactly
    val frequencies: List<LoShuDigitFrequency>,       // 9 entries (digits 1..9)
    val extractedDigits: List<Int>,                   // All raw non-zero digits extracted from DOB
    val presentDigits: List<Int>,                     // Digits with count > 0, sorted
    val missingDigits: List<Int>,                     // Digits with count == 0, sorted
    val zeroDigitsCount: Int,                         // Number of '0' digits filtered out
    val arrows: List<LoShuArrow>,                     // Exactly 8 planes evaluated
    val sourceRule: String = "LO_SHU_3X3_MAGIC_SQUARE",
)

/**
 * Structured result of a dedicated Lo Shu analysis query.
 */
@Serializable
data class LoShuResult(
    val birthDateDisplay: String,
    val grid: LoShuGrid,
    val rulesetId: String,
    val trace: NumerologyCalculationTrace,
)
