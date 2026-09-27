package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.0: Numerology Golden Reference Verification Test.
 *
 * Verifies exact source-gated calculation vectors against authoritative references:
 * 1. JKR-117480 Reference Vector (Ishant, 11-07-1996)
 * 2. Cheiro Classic Golden References (Cheiro's Book of Numbers, 1926)
 * 3. Goodwin Western Pythagorean Vectors (Goodwin 1981)
 * 4. Boundary and Leap Year Cases
 * 5. Trace Completeness and Explainability
 */
class NumerologyGoldenReferenceTest {

    // ========================================================================
    // 1. JKR-117480 GOLDEN REFERENCE VECTOR (11-07-1996, ISHANT)
    // ========================================================================

    @Test
    fun testJkrReferenceVector_Pythagorean() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            targetMonth = 9,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Radical (Moolank / Birth Day)
        assertNotNull(profile.radical)
        assertEquals(11, profile.radical!!.radicalValue)
        assertTrue(profile.radical!!.isMasterNumber)
        assertEquals(11, profile.radical!!.rawDayOfBirth)

        // Destiny (Life Path)
        assertNotNull(profile.destiny)
        assertEquals(7, profile.destiny!!.destinyValue)
        assertFalse(profile.destiny!!.isMasterNumber)

        // Name Number (Expression) — Pythagorean: I(9)+S(1)+H(8)+A(1)+N(5)+T(2) = 26 -> 8
        assertNotNull(profile.nameNumber)
        assertEquals(8, profile.nameNumber!!.nameValue)
        assertEquals("ISHANT", profile.nameNumber!!.fullName)
        assertEquals(NameNumberSystem.PYTHAGOREAN, profile.nameNumber!!.system)

        // Soul Urge (Vowels) — I(9)+A(1) = 10 -> 1
        assertNotNull(profile.soulUrge)
        assertEquals(1, profile.soulUrge!!.soulUrgeValue)

        // Personality (Consonants) — S(1)+H(8)+N(5)+T(2) = 16 -> 7
        assertNotNull(profile.personality)
        assertEquals(7, profile.personality!!.personalityValue)

        // Mathematical Invariant: Soul Urge (1) + Personality (7) == Expression (8)
        assertEquals(
            profile.nameNumber!!.nameValue,
            profile.soulUrge!!.soulUrgeValue + profile.personality!!.personalityValue
        )

        // Four Pinnacles: 9, 9, 9, 5
        assertEquals(4, profile.pinnacles.size)
        assertEquals(9, profile.pinnacles[0].pinnacleValue) // M(7) + D(2) = 9
        assertEquals(0, profile.pinnacles[0].startAge)
        assertEquals(29, profile.pinnacles[0].endAge) // 36 - LifePath(7) = 29

        assertEquals(9, profile.pinnacles[1].pinnacleValue) // D(2) + Y(7) = 9
        assertEquals(30, profile.pinnacles[1].startAge)
        assertEquals(38, profile.pinnacles[1].endAge)

        assertEquals(9, profile.pinnacles[2].pinnacleValue) // P1(9) + P2(9) = 18 -> 9
        assertEquals(39, profile.pinnacles[2].startAge)
        assertEquals(47, profile.pinnacles[2].endAge)

        assertEquals(5, profile.pinnacles[3].pinnacleValue) // M(7) + Y(7) = 14 -> 5
        assertEquals(48, profile.pinnacles[3].startAge)
        assertNull(profile.pinnacles[3].endAge)

        // Personal Year 2026: 7 + 11(or 2) + (2+0+2+6=1) = 10 -> 1
        assertEquals(1, profile.personalYears.size)
        assertEquals(2026, profile.personalYears[0].targetYear)
        assertEquals(1, profile.personalYears[0].personalYearValue)

        // Personal Month 9 in PY 1: 1 + 9 = 10 -> 1
        assertEquals(1, profile.personalMonths.size)
        assertEquals(1, profile.personalMonths[0].personalMonthValue)
    }

    @Test
    fun testJkrReferenceVector_Chaldean() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Radical: 11
        assertNotNull(profile.radical)
        assertEquals(11, profile.radical!!.radicalValue)
        assertTrue(profile.radical!!.isMasterNumber)

        // Destiny: 1+1+0+7+1+9+9+6 = 34 -> 7
        assertNotNull(profile.destiny)
        assertEquals(7, profile.destiny!!.destinyValue)
        assertEquals(34, profile.destiny!!.rawSum)

        // Name Number — Chaldean: I(1)+S(3)+H(5)+A(1)+N(5)+T(4) = 19 -> 10 -> 1
        assertNotNull(profile.nameNumber)
        assertEquals(1, profile.nameNumber!!.nameValue)
        assertEquals(NameNumberSystem.CHALDEAN, profile.nameNumber!!.system)

        // Chaldean ruleset marks Soul Urge, Personality, Pinnacles as unsupported
        assertNull(profile.soulUrge)
        assertNull(profile.personality)
        assertTrue(profile.pinnacles.isEmpty())

        // Combinations: Radical 11 (root 2) and Destiny 7
        assertEquals(1, profile.combinations.size)
        val comb = profile.combinations[0]
        assertEquals(11, comb.radicalValue)
        assertEquals(7, comb.destinyValue)
    }

    // ========================================================================
    // 2. CHEIRO CLASSICAL REFERENCES
    // ========================================================================

    @Test
    fun testCheiroClassicalDays() {
        // Born on the 15th: 1+5 = 6 (Venus)
        val (rad15, _) = NumerologyCalculationEngine.calculateRadical(
            15,
            NumerologyRuleset.CHALDEAN_CHEIRO_V1
        )
        assertEquals(6, rad15.radicalValue)
        assertFalse(rad15.isMasterNumber)
        assertEquals("Venus", rad15.rulingPlanet)

        // Born on the 28th: 2+8 = 10 -> 1 (Sun)
        val (rad28, _) = NumerologyCalculationEngine.calculateRadical(
            28,
            NumerologyRuleset.CHALDEAN_CHEIRO_V1
        )
        assertEquals(1, rad28.radicalValue)
        assertFalse(rad28.isMasterNumber)
        assertEquals("Sun", rad28.rulingPlanet)

        // Born on the 22nd: Master 22 preserved
        val (rad22, _) = NumerologyCalculationEngine.calculateRadical(
            22,
            NumerologyRuleset.CHALDEAN_CHEIRO_V1
        )
        assertEquals(22, rad22.radicalValue)
        assertTrue(rad22.isMasterNumber)
        assertEquals("Master 22 (Rahu Octave)", rad22.rulingPlanet)
    }

    // ========================================================================
    // 3. MASTER NUMBER POLICIES
    // ========================================================================

    @Test
    fun testMasterNumber33Policy() {
        // 33 with PRESERVE_11_22_33 preserves 33
        val res33Preserved =
            NumerologyReductionEngine.reduce(33, MasterNumberPolicy.PRESERVE_11_22_33).finalValue
        assertEquals(33, res33Preserved)

        // 33 with PRESERVE_11_22 reduces 33 to 6
        val res33Reduced =
            NumerologyReductionEngine.reduce(33, MasterNumberPolicy.PRESERVE_11_22).finalValue
        assertEquals(6, res33Reduced)

        // 11 with REDUCE_ALL_TO_SINGLE_DIGIT reduces to 2
        val res11Single = NumerologyReductionEngine.reduce(
            11,
            MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
        ).finalValue
        assertEquals(2, res11Single)

        // 22 with REDUCE_ALL_TO_SINGLE_DIGIT reduces to 4
        val res22Single = NumerologyReductionEngine.reduce(
            22,
            MasterNumberPolicy.REDUCE_ALL_TO_SINGLE_DIGIT
        ).finalValue
        assertEquals(4, res22Single)
    }

    // ========================================================================
    // 4. BOUNDARY VALUES & LEAP YEARS
    // ========================================================================

    @Test
    fun testLeapYearValid() {
        val leapRequest = NumerologyRequest(
            birthDay = 29,
            birthMonth = 2,
            birthYear = 2024, // 2024 is a leap year
        )
        val result = NumerologyCalculationEngine.calculate(leapRequest)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile
        assertEquals(11, profile.radical!!.radicalValue) // 2+9 = 11
        assertTrue(profile.radical!!.isMasterNumber)
    }

    @Test
    fun testNonLeapYearInvalid() {
        val invalidRequest = NumerologyRequest(
            birthDay = 29,
            birthMonth = 2,
            birthYear = 2023, // 2023 is not a leap year
        )
        val result = NumerologyCalculationEngine.calculate(invalidRequest)
        assertTrue(result is AynvoraResult.Failure.InvalidInput)
        assertEquals("birthDate", (result as AynvoraResult.Failure.InvalidInput).field)
    }

    @Test
    fun testInvalidDayAndMonthBoundaries() {
        // Feb 30
        assertTrue(
            NumerologyCalculationEngine.calculate(
                NumerologyRequest(
                    30,
                    2,
                    2024
                )
            ) is AynvoraResult.Failure.InvalidInput
        )
        // April 31
        assertTrue(
            NumerologyCalculationEngine.calculate(
                NumerologyRequest(
                    31,
                    4,
                    2024
                )
            ) is AynvoraResult.Failure.InvalidInput
        )
        // Day 0
        assertTrue(
            NumerologyCalculationEngine.calculate(
                NumerologyRequest(
                    0,
                    1,
                    2024
                )
            ) is AynvoraResult.Failure.InvalidInput
        )
        // Month 13
        assertTrue(
            NumerologyCalculationEngine.calculate(
                NumerologyRequest(
                    1,
                    13,
                    2024
                )
            ) is AynvoraResult.Failure.InvalidInput
        )
        // Year 0
        assertTrue(
            NumerologyCalculationEngine.calculate(
                NumerologyRequest(
                    1,
                    1,
                    0
                )
            ) is AynvoraResult.Failure.InvalidInput
        )
    }

    // ========================================================================
    // 5. NAME NORMALIZATION & DIACRITICS
    // ========================================================================

    @Test
    fun testNameNormalization() {
        val raw = "  Éléonore-Marie O'Connor, Jr.  "
        val normalized = NumerologyAlphabet.normalizeName(raw)
        assertEquals("ELEONOREMARIEOCONNORJR", normalized)
    }

    // ========================================================================
    // 6. CALCULATION TRACES
    // ========================================================================

    @Test
    fun testCalculationTracesPresentAndDetailed() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val traces = result.value.profile.calculationTraces

        // Check Radical trace
        val radTrace = traces[NumerologyCalculationType.RADICAL_NUMBER]
        assertNotNull(radTrace)
        assertEquals(11, radTrace.finalValue)
        assertTrue(radTrace.isMasterNumber)
        assertTrue(radTrace.reductionSteps.isNotEmpty())

        // Check Destiny trace
        val desTrace = traces[NumerologyCalculationType.DESTINY_NUMBER]
        assertNotNull(desTrace)
        assertEquals(7, desTrace.finalValue)
        assertTrue(desTrace.reductionSteps.isNotEmpty())

        // Check Name trace
        val nameTrace = traces[NumerologyCalculationType.NAME_NUMBER]
        assertNotNull(nameTrace)
        assertEquals(8, nameTrace.finalValue)
        assertEquals("ISHANT", nameTrace.normalizedInput)
        assertTrue(nameTrace.reductionSteps.isNotEmpty())
    }

    // ========================================================================
    // 7. DETERMINISTIC REPEATABILITY
    // ========================================================================

    @Test
    fun testDeterministicRepeatability() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            targetMonth = 9,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val initial = NumerologyCalculationEngine.calculate(request)
        assertTrue(initial is AynvoraResult.Success)

        repeat(50) {
            val next = NumerologyCalculationEngine.calculate(request)
            assertTrue(next is AynvoraResult.Success)
            assertEquals(initial.value.profile.radical, next.value.profile.radical)
            assertEquals(initial.value.profile.destiny, next.value.profile.destiny)
            assertEquals(initial.value.profile.nameNumber, next.value.profile.nameNumber)
            assertEquals(initial.value.profile.soulUrge, next.value.profile.soulUrge)
            assertEquals(initial.value.profile.personality, next.value.profile.personality)
            assertEquals(initial.value.profile.pinnacles, next.value.profile.pinnacles)
            assertEquals(initial.value.profile.personalYears, next.value.profile.personalYears)
            assertEquals(initial.value.profile.personalMonths, next.value.profile.personalMonths)
        }
    }
}
