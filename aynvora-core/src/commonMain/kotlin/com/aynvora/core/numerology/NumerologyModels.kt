package com.aynvora.core.numerology

import kotlinx.serialization.Serializable

/**
 * AYNVORA Numerology Domain — Foundation Contracts
 *
 * Phase 8.1: Registration and contract definitions only.
 * STATUS: FOUNDATION_ONLY
 *
 * Numerology is a first-class independent core domain (Rule 72 — Numerology Core Rule).
 * Formulas are NOT implemented until independently documented source rules are confirmed.
 *
 * Reference values for JKR-117480 (test/engineering vector only):
 * - Radical (Moolank): 11 (date = 11)
 * - Destiny (Bhagyank): 7  (11 + 7 + 1996 => digit sum)
 * - Name Number: 8
 * - Pinnacles: 9, 9, 9, 5
 *
 * DO NOT invent formulas. DO NOT claim implementation until verified source exists.
 */

// ============================================================================
// NUMEROLOGY DOMAIN MODELS
// ============================================================================

/**
 * The Radical (Moolank) is derived from the day-of-birth digit root.
 * Reference: Cheiro's Book of Numbers, Lo Shu Grid, Pythagorean tradition.
 * Master numbers (11, 22, 33) are preserved without further reduction.
 */
@Serializable
data class RadicalNumber(
    val rawDayOfBirth: Int,
    val radicalValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "CHEIRO_MOOLANK",
)

/**
 * The Destiny Number (Bhagyank) is derived from the complete date of birth.
 * Digit sum of DD + MM + YYYY, reduced to single digit or master number.
 */
@Serializable
data class DestinyNumber(
    val rawSum: Int,
    val destinyValue: Int,
    val isMasterNumber: Boolean,
    val rulingPlanet: String,
    val sourceRule: String = "CHEIRO_BHAGYANK",
)

/**
 * Name Number derived from Chaldean or Pythagorean letter mapping of full birth name.
 * System must be declared explicitly — Chaldean and Pythagorean differ.
 */
@Serializable
data class NameNumber(
    val fullName: String,
    val nameValue: Int,
    val system: NameNumberSystem,
    val rulingPlanet: String,
    val sourceRule: String,
)

@Serializable
enum class NameNumberSystem {
    CHALDEAN,
    PYTHAGOREAN,
}

/**
 * Personal Year number for a given calendar year (birthday month/day + year).
 */
@Serializable
data class PersonalYear(
    val year: Int,
    val personalYearValue: Int,
    val sourceRule: String = "PYTHAGOREAN_PERSONAL_YEAR",
)

/**
 * Personal Month number within a Personal Year.
 */
@Serializable
data class PersonalMonth(
    val year: Int,
    val month: Int,
    val personalMonthValue: Int,
    val sourceRule: String = "PYTHAGOREAN_PERSONAL_MONTH",
)

/**
 * Pinnacle cycle (9-year blocks derived from birth date components).
 * Four Pinnacles in a lifetime.
 */
@Serializable
data class Pinnacle(
    val cycle: Int,         // 1–4
    val pinnacleValue: Int,
    val compoundValue: Int,
    val startAge: Int,
    val endAge: Int?,       // null = open-ended (4th pinnacle)
    val sourceRule: String = "PYTHAGOREAN_PINNACLE",
)

/**
 * A repeating Numerology combination detected (e.g., Combination 1 & 1, 6 & 6, 7 & 7).
 * Documents synergistic number pairings visible in radical, destiny, and name numbers.
 */
@Serializable
data class NumerologyCombination(
    val firstNumber: Int,
    val secondNumber: Int,
    val description: String,
)

/**
 * Complete Numerology profile for a birth data input.
 * All fields are nullable at FOUNDATION_ONLY status — only populated when calculation engine is implemented.
 */
@Serializable
data class NumerologyProfile(
    val birthDateDisplay: String,           // "11-07-1996"
    val radical: RadicalNumber?,            // FOUNDATION_ONLY: null until engine implemented
    val destiny: DestinyNumber?,            // FOUNDATION_ONLY: null until engine implemented
    val nameNumber: NameNumber?,            // FOUNDATION_ONLY: null until engine implemented
    val personalYears: List<PersonalYear>,
    val personalMonths: List<PersonalMonth>,
    val pinnacles: List<Pinnacle>,
    val combinations: List<NumerologyCombination>,
    val engineStatus: NumerologyEngineStatus = NumerologyEngineStatus.FOUNDATION_ONLY,
)

/**
 * Numerology Result returned from a complete analysis query.
 */
@Serializable
data class NumerologyResult(
    val profile: NumerologyProfile,
    val disclaimer: String = NUMEROLOGY_DISCLAIMER,
)

/**
 * Engine implementation status.
 */
@Serializable
enum class NumerologyEngineStatus {
    /** Contracts defined; formulas not yet implemented. */
    FOUNDATION_ONLY,

    /** Radical and Destiny Number implemented and verified. */
    PARTIAL,

    /** All numerology calculations implemented and validated. */
    IMPLEMENTED,
}

const val NUMEROLOGY_DISCLAIMER =
    "Numerology is a contemplative tradition. Numbers and their influences " +
            "are a matter of cultural belief. AYNVORA presents them as interpretive " +
            "tools for self-reflection, not as deterministic prediction of events or " +
            "guaranteed outcomes."

// ============================================================================
// REPOSITORY CONTRACT
// ============================================================================

/**
 * Repository contract for Numerology domain.
 * Implementations live in the data layer (:aynvora-data).
 */
interface NumerologyRepository {
    /**
     * Returns the Numerology profile for a birth date and name.
     * FOUNDATION_ONLY: Returns a profile with FOUNDATION_ONLY status and null calculation fields.
     */
    suspend fun getProfile(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        fullName: String,
        system: NameNumberSystem = NameNumberSystem.CHALDEAN,
    ): NumerologyResult
}
