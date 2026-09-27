package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.3: Golden Reference Test for Hebrew Gematria — Mispar Gadol Variant.
 *
 * Primary Authority:
 * - Rabbi Moses Cordovero, "Pardes Rimonim" (1591), Gate 30 (Sha'ar Ha-Tzeruf)
 *
 * In Mispar Gadol (the Large Number), the five final forms (Sofiyot) receive hundreds values:
 * ך = 500, ם = 600, ן = 700, ף = 800, ץ = 900.
 * Regular non-final letters retain standard values (1..400).
 */
class NumerologyHebrewGadolGoldenReferenceTest {

    @Test
    fun testShalomWithMemSofitReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "שלום",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)
        assertEquals("שלום", gem.rawText)
        assertEquals("שלום", gem.normalizedHebrew)
        assertEquals("MISPAR_GADOL", gem.variant)

        // ש (300) + ל (30) + ו (6) + ם (600) = 936
        // Note: In Ragil (standard), ם=40 giving 376. In Gadol, ם=600 giving 936!
        assertEquals(936, gem.absoluteValue)

        // Mispar Katan reduction: 9+3+6 = 18 -> 1+8 = 9
        assertEquals(9, gem.reducedValue)

        assertEquals(4, gem.letterValues.size)
        assertEquals("ש" to 300, gem.letterValues[0])
        assertEquals("ל" to 30, gem.letterValues[1])
        assertEquals("ו" to 6, gem.letterValues[2])
        assertEquals("ם" to 600, gem.letterValues[3])
    }

    @Test
    fun testMelekhWithKafSofitReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "מלך",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // מ (40) + ל (30) + ך (500) = 570
        // (In Ragil: 40+30+20 = 90)
        assertEquals(570, gem.absoluteValue)
        // 5+7+0 = 12 -> 1+2 = 3
        assertEquals(3, gem.reducedValue)
        assertEquals("ך" to 500, gem.letterValues[2])
    }

    @Test
    fun testGanWithNunSofitReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "גן",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // ג (3) + ן (700) = 703
        // (In Ragil: 3+50 = 53)
        assertEquals(703, gem.absoluteValue)
        // 7+0+3 = 10 -> 1+0 = 1
        assertEquals(1, gem.reducedValue)
        assertEquals("ן" to 700, gem.letterValues[1])
    }

    @Test
    fun testKesefWithPeSofitReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "כסף",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // כ (20) + ס (60) + ף (800) = 880
        // (In Ragil: 20+60+80 = 160)
        assertEquals(880, gem.absoluteValue)
        // 8+8+0 = 16 -> 1+6 = 7
        assertEquals(7, gem.reducedValue)
        assertEquals("ף" to 800, gem.letterValues[2])
    }

    @Test
    fun testEretzWithTsadiSofitReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "ארץ",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // א (1) + ר (200) + ץ (900) = 1101
        // (In Ragil: 1+200+90 = 291)
        assertEquals(1101, gem.absoluteValue)
        // 1+1+0+1 = 3
        assertEquals(3, gem.reducedValue)
        assertEquals("ץ" to 900, gem.letterValues[2])
    }

    @Test
    fun testNiqqudStrippingWithFinalLetters() {
        // "מֶלֶךְ" with segol and sheva
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "מֶלֶךְ",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)
        assertEquals("מלך", gem.normalizedHebrew)
        assertEquals(570, gem.absoluteValue)
        assertEquals(3, gem.reducedValue)
    }

    @Test
    fun testRejectionOfLatinScriptForHebrewGadol() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "SHALOM",
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Hebrew script"))
    }
}
