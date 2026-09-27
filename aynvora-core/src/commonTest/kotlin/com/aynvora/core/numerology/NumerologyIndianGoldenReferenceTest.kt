package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.1: Golden Reference Test for Indian / Vedic Ank Jyotish.
 *
 * Authorities:
 * - Pandit Sethuraman, "Science of Fortune" (1954)
 * - Dr. M. Katakkar, "Miracles of Numerology" (1989)
 */
class NumerologyIndianGoldenReferenceTest {

    @Test
    fun testJkrReferenceVector_IndianAnkJyotish() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // 1. Moolank (Driver / Radical):
        // Day 11 reduces strictly to 1+1 = 2 (No master numbers in 9-Graha Vedic system)
        assertNotNull(profile.radical)
        assertEquals(2, profile.radical!!.radicalValue)
        assertFalse(profile.radical!!.isMasterNumber)
        assertEquals("Chandra (Moon)", profile.radical!!.rulingPlanet)

        // 2. Bhagyank (Conductor / Destiny):
        // 1+1+7+1+9+9+6 = 34 -> 3+4 = 7
        assertNotNull(profile.destiny)
        assertEquals(7, profile.destiny!!.destinyValue)
        assertFalse(profile.destiny!!.isMasterNumber)
        assertEquals("Ketu (South Node)", profile.destiny!!.rulingPlanet)

        // 3. Namank (Name Number):
        // Cheiro-Sethuraman mapping: I(1)+S(3)+H(5)+A(1)+N(5)+T(4) = 19 -> 1+9 = 10 -> 1
        assertNotNull(profile.nameNumber)
        assertEquals(1, profile.nameNumber!!.nameValue)
        assertEquals("Surya (Sun)", profile.nameNumber!!.rulingPlanet)

        // 4. Panchadha Maitri Relationship:
        // Moolank 2 (Chandra) vs Bhagyank 7 (Ketu)
        // Katakkar relationship matrix: 7 is neutral (Sama) for 2
        assertEquals(1, profile.combinations.size)
        val comb = profile.combinations[0]
        assertEquals(NumerologyRelationshipType.NEUTRAL, comb.relationshipType)
        assertEquals("KATAKKAR_SETHURAMAN_PANCHADHA_MAITRI", comb.sourceRule)

        // 5. Typed Planetary Association
        assertNotNull(profile.planetaryAssociation)
        assertEquals(2, profile.planetaryAssociation!!.number)
        assertEquals(NumerologyPlanetId.MOON, profile.planetaryAssociation!!.planetId)
        assertEquals("Chandra", profile.planetaryAssociation!!.sanskritName)
        assertEquals("Moon", profile.planetaryAssociation!!.englishName)

        // 6. Unsupported Calculations in Indian Ank Jyotish
        assertNull(profile.soulUrge)
        assertNull(profile.personality)
        assertTrue(profile.pinnacles.isEmpty())
        assertNull(profile.loShu)

        // 7. Traces
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.RADICAL_NUMBER))
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.DESTINY_NUMBER))
        assertTrue(profile.calculationTraces.containsKey(NumerologyCalculationType.NAME_NUMBER))
        assertFalse(profile.calculationTraces.containsKey(NumerologyCalculationType.PINNACLE_CYCLES))
    }

    @Test
    fun testIndianMasterNumberStrictReduction_Day22() {
        // In Indian Ank Jyotish, Day 22 reduces strictly to 2+2 = 4 (Rahu)
        val request = NumerologyRequest(
            birthDay = 22,
            birthMonth = 10,
            birthYear = 1985,
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        assertEquals(4, profile.radical?.radicalValue)
        assertFalse(profile.radical?.isMasterNumber == true)
        assertEquals("Rahu (North Node)", profile.radical?.rulingPlanet)

        // Destiny: 2+2 + 1+0 + 1+9+8+5 = 4 + 1 + 23 = 28 -> 10 -> 1 (Surya)
        assertEquals(1, profile.destiny?.destinyValue)
        assertEquals("Surya (Sun)", profile.destiny?.rulingPlanet)

        // Panchadha Maitri: 4 (Rahu) vs 1 (Surya) -> Neutral (Sama)
        assertEquals(NumerologyRelationshipType.NEUTRAL, profile.combinations[0].relationshipType)
    }

    @Test
    fun testIndianScriptValidation_RejectDevanagari() {
        // Step 8 & 9: Non-Latin scripts must be rejected rather than silently dropped or converted
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "इशांत", // Devanagari script
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure.InvalidInput)
        assertEquals("fullName", result.field)
        assertTrue(result.message.contains("Unsupported character or script"))
    }

    @Test
    fun testPanchadhaMaitriMatrix_ExhaustiveTypes() {
        // Sun 1 vs Moon 2 -> FRIENDLY (Mitra)
        val comb1 = NumerologyCalculationEngine.calculateCombination(
            1,
            2,
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        assertEquals(NumerologyRelationshipType.FRIENDLY, comb1.relationshipType)

        // Sun 1 vs Mercury 5 -> NEUTRAL (Sama)
        val comb2 = NumerologyCalculationEngine.calculateCombination(
            1,
            5,
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        assertEquals(NumerologyRelationshipType.NEUTRAL, comb2.relationshipType)

        // Sun 1 vs Saturn 8 -> CHALLENGING (Shatru)
        val comb3 = NumerologyCalculationEngine.calculateCombination(
            1,
            8,
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id
        )
        assertEquals(NumerologyRelationshipType.CHALLENGING, comb3.relationshipType)
    }
}
