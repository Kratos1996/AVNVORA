package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.1: Golden Reference Test for Classical Chaldean / Cheiro Numerology.
 *
 * Authorities:
 * - Cheiro (Count Louis Hamon), "Cheiro's Book of Numbers" (1926)
 * - Pandit Sethuraman, "Adhristavijnanam" / Indian Ank Jyotish tradition
 */
class NumerologyChaldeanGoldenReferenceTest {

    @Test
    fun testJkrReferenceVector_Chaldean() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            targetMonth = 9,
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Radical (Moolank): Day 11 -> preserved as Master 11 in Cheiro tradition
        assertNotNull(profile.radical)
        assertEquals(11, profile.radical!!.radicalValue)
        assertTrue(profile.radical!!.isMasterNumber)
        assertEquals("Master 11 (Moon Octave)", profile.radical!!.rulingPlanet)

        // Destiny (Bhagyank): 1+1+7+1+9+9+6 = 34 -> 3+4 = 7
        assertNotNull(profile.destiny)
        assertEquals(7, profile.destiny!!.destinyValue)
        assertFalse(profile.destiny!!.isMasterNumber)
        assertEquals("Ketu / Neptune", profile.destiny!!.rulingPlanet)

        // Name Number (Namank): I(1)+S(3)+H(5)+A(1)+N(5)+T(4) = 19 -> 1+9 = 10 -> 1
        assertNotNull(profile.nameNumber)
        assertEquals(1, profile.nameNumber!!.nameValue)
        assertEquals(NameNumberSystem.CHALDEAN, profile.nameNumber!!.system)
        assertEquals("Sun", profile.nameNumber!!.rulingPlanet)

        // Soul Urge, Personality, Pinnacles are unsupported in classical Chaldean
        assertNull(profile.soulUrge)
        assertNull(profile.personality)
        assertTrue(profile.pinnacles.isEmpty())
        assertNull(profile.loShu)

        // Combinations: 11 (Moon root 2) & 7 (Ketu)
        assertEquals(1, profile.combinations.size)
        assertEquals(NumerologyRelationshipType.NEUTRAL, profile.combinations[0].relationshipType)

        // Traces
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.RADICAL_NUMBER))
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.DESTINY_NUMBER))
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.NAME_NUMBER))
        assertFalse(profile.calculationTraces.containsKey(NumerologyCalculationType.SOUL_URGE_NUMBER))
    }

    @Test
    fun testCheiroClassicVector_Day22() {
        // Cheiro Ch. 3: Number 22 (Rahu / 4 octave)
        val request = NumerologyRequest(
            birthDay = 22,
            birthMonth = 4,
            birthYear = 1980,
            fullName = "JOHN",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        assertEquals(22, profile.radical?.radicalValue)
        assertTrue(profile.radical?.isMasterNumber == true)

        // Destiny: 2+2+4+1+9+8+0 = 26 -> 8
        assertEquals(8, profile.destiny?.destinyValue)
        assertEquals("Saturn", profile.destiny?.rulingPlanet)

        // Name "JOHN": J(1)+O(7)+H(5)+N(5) = 18 -> 9
        assertEquals(9, profile.nameNumber?.nameValue)
        assertEquals("Mars", profile.nameNumber?.rulingPlanet)
    }
}
