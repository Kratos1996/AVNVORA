package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.3: Golden Reference Test for Indian Katapayadi Numerical Mnemonic System.
 *
 * Primary Authority:
 * - Sankaravarman, "Sadratnamala" (1819 CE), Prakarana 1, Verse 3–5:
 *   "नज्ञावचाश्च शून्यानि सङ्ख्याः कटपयादयः।
 *    मिश्रे तूपान्त्यहल् सङ्ख्या न च चिन्त्यो हलः स्वरः॥"
 *   "अङ्कानां वामतो गतिः" (Numbers move from right to left / reversed).
 *
 * Rules:
 * - क, ट, प, य groups:
 *   क=1, ख=2, ग=3, घ=4, ङ=5, च=6, छ=7, ज=8, झ=9, ञ=0
 *   ट=1, ठ=2, ड=3, ढ=4, ण=5, त=6, थ=7, द=8, ध=9, न=0
 *   प=1, फ=2, ब=3, भ=4, म=5
 *   य=1, र=2, ल=3, व=4, श=5, ष=6, स=7, ह=8, ळ=9
 * - Standalone independent vowels = 0.
 * - Conjunct consonants (Samyuktakshara): only the last consonant in the cluster receives value.
 * - Digits are read in reverse order (ankānām vāmato gatiḥ).
 */
class NumerologyKatapayadiGoldenReferenceTest {

    @Test
    fun testKhagoReferenceVector() {
        // "खगो" (bird / sky-dweller)
        // ख: ka-group (2)
        // गो: ka-group (3)
        // Digits: 2, 3 -> Reversed: 32
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "खगो",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val kata = result.value.profile.katapayadi
        assertNotNull(kata)
        assertEquals("खगो", kata.rawText)
        assertEquals("खगो", kata.normalizedText)
        assertEquals("23", kata.digitSequence)
        assertEquals("32", kata.finalNumber)
        assertTrue(kata.isReversed)
    }

    @Test
    fun testNabhaZeroContainingReferenceVector() {
        // "नभ" (sky/atmosphere)
        // न: ta-group (0)
        // भ: pa-group (4)
        // Digits: 0, 4 -> Reversed: 40
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "नभ",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val kata = result.value.profile.katapayadi
        assertNotNull(kata)
        assertEquals("04", kata.digitSequence)
        assertEquals("40", kata.finalNumber)
    }

    @Test
    fun testJaladhiReferenceVector() {
        // "जलधि" (ocean)
        // ज: ka-group (8)
        // ल: ya-group (3)
        // धि: ta-group (9)
        // Digits: 8, 3, 9 -> Reversed: 938
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "जलधि",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val kata = result.value.profile.katapayadi
        assertNotNull(kata)
        assertEquals("839", kata.digitSequence)
        assertEquals("938", kata.finalNumber)
    }

    @Test
    fun testSuryaConjunctReferenceVector() {
        // "सूर्य" (Sun)
        // सू: sa (7)
        // र्य: conjunct र् + य -> last consonant य (1)
        // Digits: 7, 1 -> Reversed: 17
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "सूर्य",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val kata = result.value.profile.katapayadi
        assertNotNull(kata)
        assertEquals("71", kata.digitSequence)
        assertEquals("17", kata.finalNumber)
    }

    @Test
    fun testAchalaStandaloneVowelReferenceVector() {
        // "अचल" (mountain / immovable)
        // अ: standalone vowel (0)
        // च: ka-group (6)
        // ल: ya-group (3)
        // Digits: 0, 6, 3 -> Reversed: 360
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "अचल",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val kata = result.value.profile.katapayadi
        assertNotNull(kata)
        assertEquals("063", kata.digitSequence)
        assertEquals("360", kata.finalNumber)
    }

    @Test
    fun testRejectionOfLatinScriptForKatapayadi() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "SURYA",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Devanagari script"))
    }
}
