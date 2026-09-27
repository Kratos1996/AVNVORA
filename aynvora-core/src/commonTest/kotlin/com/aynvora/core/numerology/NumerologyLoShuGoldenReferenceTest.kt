package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.1: Golden Reference Test for Classical Lo Shu 3x3 Magic Square Numerology.
 *
 * Authorities:
 * - I Ching (Luoshu River Scroll, Zhou Dynasty)
 * - Dr. David A. Phillips, "The Complete Book of Numerology" (1992), Ch. 5
 */
class NumerologyLoShuGoldenReferenceTest {

    @Test
    fun testJkrReferenceVector_LoShu() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile
        val loShuResult = result.value.loShuResult

        assertNotNull(loShuResult)
        assertNotNull(profile.loShu)

        val grid = profile.loShu!!

        // 1. Digit Extraction & Zero Filtering
        // Date "11-07-1996" -> non-zero digits: [1, 1, 7, 1, 9, 9, 6], zero: 1
        assertEquals(listOf(1, 1, 7, 1, 9, 9, 6), grid.extractedDigits)
        assertEquals(1, grid.zeroDigitsCount)
        assertEquals(7, grid.extractedDigits.size)

        // 2. Frequencies
        val freqMap = grid.frequencies.associate { it.digit to it.count }
        assertEquals(3, freqMap[1])
        assertEquals(0, freqMap[2])
        assertEquals(0, freqMap[3])
        assertEquals(0, freqMap[4])
        assertEquals(0, freqMap[5])
        assertEquals(1, freqMap[6])
        assertEquals(1, freqMap[7])
        assertEquals(0, freqMap[8])
        assertEquals(2, freqMap[9])

        // 3. Present and Missing Digits
        assertEquals(listOf(1, 6, 7, 9), grid.presentDigits)
        assertEquals(listOf(2, 3, 4, 5, 8), grid.missingDigits)

        // 4. Exactly 9 Magic Square Cells
        assertEquals(9, grid.cells.size)
        val cell1 = grid.cells.first { it.digit == 1 }
        assertEquals(3, cell1.row)
        assertEquals(2, cell1.column)
        assertEquals(3, cell1.count)

        val cell9 = grid.cells.first { it.digit == 9 }
        assertEquals(1, cell9.row)
        assertEquals(2, cell9.column)
        assertEquals(2, cell9.count)

        // 5. Eight Planes of Arrows
        assertEquals(8, grid.arrows.size)

        // Strength Arrows: none
        val strength = grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_STRENGTH }
        assertTrue(strength.isEmpty())

        // Weakness Arrows: Thought Plane (4-3-8) and Compassion Plane (2-5-8)
        val weakness = grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_WEAKNESS }
        assertEquals(2, weakness.size)
        val weaknessPlanes = weakness.map { it.plane }
        assertTrue(weaknessPlanes.contains(LoShuPlaneType.THOUGHT_PLANE))
        assertTrue(weaknessPlanes.contains(LoShuPlaneType.COMPASSION_PLANE))

        // 6. Invariant: Radical, Destiny, Pinnacles are null/empty in Lo Shu ruleset
        assertNull(profile.radical)
        assertNull(profile.destiny)
        assertNull(profile.nameNumber)
        assertTrue(profile.pinnacles.isEmpty())

        // 7. Trace
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.LO_SHU_GRID))
        val trace = profile.calculationTraces[NumerologyCalculationType.LO_SHU_GRID]!!
        assertEquals(NumerologyRuleset.LO_SHU_CLASSICAL_V1.id, trace.rulesetId)
        assertEquals("11-07-1996", trace.rawInput)
        assertEquals(7, trace.finalValue)
    }

    @Test
    fun testLoShuArrowOfStrength_WillPlane() {
        // Date 15-09-1982 -> digits: 1, 5, 9, 1, 9, 8, 2
        // Will Plane (9, 5, 1) has all digits present:
        // 9: count 2, 5: count 1, 1: count 2 -> ARROW_OF_STRENGTH
        val request = NumerologyRequest(
            birthDay = 15,
            birthMonth = 9,
            birthYear = 1982,
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val grid = result.value.profile.loShu!!

        val willArrow = grid.arrows.first { it.plane == LoShuPlaneType.WILL_PLANE }
        assertEquals(LoShuArrowStatus.ARROW_OF_STRENGTH, willArrow.status)
        assertEquals("Arrow of Willpower", willArrow.strengthName)
    }
}
