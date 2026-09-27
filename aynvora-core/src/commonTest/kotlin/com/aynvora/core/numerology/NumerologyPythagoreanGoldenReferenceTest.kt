package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.1: Golden Reference Test for Modern Western Pythagorean Numerology.
 *
 * Authorities:
 * - Matthew Oliver Goodwin, "Numerology: The Complete Guide" (1981)
 * - Florence Campbell, "Your Days Are Numbered" (1931)
 */
class NumerologyPythagoreanGoldenReferenceTest {

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

        // Radical (Birth Day)
        assertNotNull(profile.radical)
        assertEquals(11, profile.radical!!.radicalValue)
        assertTrue(profile.radical!!.isMasterNumber)

        // Destiny (Life Path): Component sum (7 + 11 + (1+9+9+6=25->7)) = 25 -> 7
        assertNotNull(profile.destiny)
        assertEquals(7, profile.destiny!!.destinyValue)
        assertFalse(profile.destiny!!.isMasterNumber)

        // Name Number (Expression): I(9)+S(1)+H(8)+A(1)+N(5)+T(2) = 26 -> 8
        assertNotNull(profile.nameNumber)
        assertEquals(8, profile.nameNumber!!.nameValue)
        assertEquals(NameNumberSystem.PYTHAGOREAN, profile.nameNumber!!.system)

        // Soul Urge: I(9)+A(1) = 10 -> 1
        assertNotNull(profile.soulUrge)
        assertEquals(1, profile.soulUrge!!.soulUrgeValue)

        // Personality: S(1)+H(8)+N(5)+T(2) = 16 -> 7
        assertNotNull(profile.personality)
        assertEquals(7, profile.personality!!.personalityValue)

        // Invariant: Soul Urge (1) + Personality (7) == Expression (8)
        assertEquals(
            profile.nameNumber!!.nameValue,
            profile.soulUrge!!.soulUrgeValue + profile.personality!!.personalityValue
        )

        // 4 Pinnacles: 9, 9, 9, 5
        assertEquals(4, profile.pinnacles.size)
        assertEquals(9, profile.pinnacles[0].pinnacleValue)
        assertEquals(0, profile.pinnacles[0].startAge)
        assertEquals(29, profile.pinnacles[0].endAge)

        assertEquals(9, profile.pinnacles[1].pinnacleValue)
        assertEquals(30, profile.pinnacles[1].startAge)
        assertEquals(38, profile.pinnacles[1].endAge)

        assertEquals(9, profile.pinnacles[2].pinnacleValue)
        assertEquals(39, profile.pinnacles[2].startAge)
        assertEquals(47, profile.pinnacles[2].endAge)

        assertEquals(5, profile.pinnacles[3].pinnacleValue)
        assertEquals(48, profile.pinnacles[3].startAge)
        assertNull(profile.pinnacles[3].endAge)

        // Personal Year & Month
        assertEquals(1, profile.personalYears.size)
        assertEquals(1, profile.personalYears[0].personalYearValue)
        assertEquals(1, profile.personalMonths.size)
        assertEquals(1, profile.personalMonths[0].personalMonthValue)
    }

    @Test
    fun testGoodwinLifePathMaster33() {
        // Birth date: 29-09-1975
        // Month: 9
        // Day: 29 -> 2+9 = 11 (Master 11 preserved)
        // Year: 1975 -> 1+9+7+5 = 22 (Master 22 preserved)
        // Total: 9 + 11 + 22 = 42 -> 6 (in standard reduction)
        val request = NumerologyRequest(
            birthDay = 29,
            birthMonth = 9,
            birthYear = 1975,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        assertEquals(11, profile.radical?.radicalValue)
        assertTrue(profile.radical?.isMasterNumber == true)
        assertEquals(6, profile.destiny?.destinyValue)
    }
}
