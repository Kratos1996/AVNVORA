package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.2: Golden Reference Test for Renaissance Agrippan Occult Arithmancy.
 *
 * Authorities:
 * - Heinrich Cornelius Agrippa von Nettesheim, "De Occulta Philosophia libri tres" (1533), Book II, Ch. 20–34
 */
class NumerologyAgrippanGoldenReferenceTest {

    @Test
    fun testAgrippaNameReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 14,
            birthMonth = 9,
            birthYear = 1486,
            fullName = "AGRIPPA",
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // 1. Name Number (Agrippa 1533 scale: A=1..I=9, K=10..S=90, T=100..Z=500):
        // A(1) + G(7) + R(80) + I(9) + P(60) + P(60) + A(1) = 218
        // 218 -> 2+1+8 = 11 -> 1+1 = 2
        assertNotNull(profile.nameNumber)
        assertEquals("AGRIPPA", profile.nameNumber!!.fullName)
        assertEquals(2, profile.nameNumber!!.nameValue)
        assertEquals(NameNumberSystem.AGRIPPAN, profile.nameNumber!!.system)

        // Renaissance Occult Arithmancy calculates Name Number from Latin text;
        // it does not calculate biographical Radical or Destiny birth numbers.
        assertNull(profile.radical)
        assertNull(profile.destiny)
        assertNull(profile.loShu)
        assertNull(profile.planetaryAssociation)
        assertTrue(profile.combinations.isEmpty())
    }

    @Test
    fun testRomaReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 21,
            birthMonth = 4,
            birthYear = 753,
            fullName = "ROMA",
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // R(80) + O(50) + M(30) + A(1) = 161
        // 161 -> 1+6+1 = 8
        assertNotNull(profile.nameNumber)
        assertEquals(8, profile.nameNumber!!.nameValue)
    }

    @Test
    fun testClassicalLatinLetterEquivalences() {
        // In Agrippa's 1533 Latin table:
        // I = J = 9
        // U = V = W = 200
        val req1 = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "IULIUS",
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
        )
        val req2 = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "JULIUS",
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
        )

        val r1 = NumerologyCalculationEngine.calculate(req1)
        val r2 = NumerologyCalculationEngine.calculate(req2)

        assertTrue(r1 is AynvoraResult.Success)
        assertTrue(r2 is AynvoraResult.Success)

        // IULIUS: I(9) + U(200) + L(20) + I(9) + U(200) + S(90) = 528
        // JULIUS: J(9) + U(200) + L(20) + I(9) + U(200) + S(90) = 528
        // 528 -> 5+2+8 = 15 -> 1+5 = 6
        assertEquals(6, r1.value.profile.nameNumber?.nameValue)
        assertEquals(6, r2.value.profile.nameNumber?.nameValue)
    }

    @Test
    fun testRejectionOfNonLatinScript() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "שלום",
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Latin-transliterated characters"))
    }
}
