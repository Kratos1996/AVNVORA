package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.3: Golden Reference Test for Arabic Hisab al-Jummal — Maghribi Order Variant.
 *
 * Primary Authority:
 * - Ibn Khaldun, "The Muqaddimah" (1377 CE), Ch. 6, Section 28 (Ilm al-Huruf)
 *
 * Distinct Maghribi letter values:
 * ص (Sa') = 60     (vs Mashriqi 90)
 * ض (Da) = 90      (vs Mashriqi 800)
 * س (Sin) = 300    (vs Mashriqi 60)
 * ظ (Zha) = 800    (vs Mashriqi 900)
 * غ (Ghayn) = 900  (vs Mashriqi 1000)
 * ش (Sheen) = 1000 (vs Mashriqi 300)
 */
class NumerologyArabicMaghribiGoldenReferenceTest {

    @Test
    fun testShamsReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "شمس",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)
        assertEquals("شمس", abj.rawText)
        assertEquals("شمس", abj.normalizedArabic)
        assertEquals("MAGHRIBI", abj.variant)

        // Maghribi values:
        // ش (1000) + م (40) + س (300) = 1340
        // (In Mashriqi: 300 + 40 + 60 = 400)
        assertEquals(1340, abj.jummalKabir)

        // Jummal Saghir: 1+3+4+0 = 8
        // (In Mashriqi: 4+0+0 = 4)
        assertEquals(8, abj.jummalSaghir)

        assertEquals(3, abj.letterValues.size)
        assertEquals("ش" to 1000, abj.letterValues[0])
        assertEquals("م" to 40, abj.letterValues[1])
        assertEquals("س" to 300, abj.letterValues[2])
    }

    @Test
    fun testSabrReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "صبر",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // Maghribi: ص (60) + ب (2) + ر (200) = 262
        // (In Mashriqi: ص=90 giving 292)
        assertEquals(262, abj.jummalKabir)
        // 2+6+2 = 10 -> 1+0 = 1
        // (In Mashriqi: 2+9+2 = 13 -> 4)
        assertEquals(1, abj.jummalSaghir)
        assertEquals("ص" to 60, abj.letterValues[0])
    }

    @Test
    fun testDawReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "ضوء",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // Maghribi: ض (90) + و (6) + ء (1) = 97
        // (In Mashriqi: ض=800 giving 807)
        assertEquals(97, abj.jummalKabir)
        // 9+7 = 16 -> 1+6 = 7
        assertEquals(7, abj.jummalSaghir)
        assertEquals("ض" to 90, abj.letterValues[0])
    }

    @Test
    fun testZillReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "ظل",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // Maghribi: ظ (800) + ل (30) = 830
        // (In Mashriqi: ظ=900 giving 930)
        assertEquals(830, abj.jummalKabir)
        // 8+3+0 = 11 -> 1+1 = 2
        assertEquals(2, abj.jummalSaghir)
        assertEquals("ظ" to 800, abj.letterValues[0])
    }

    @Test
    fun testTashkeelStrippingUnderMaghribi() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "شَمْسٌ",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)
        assertEquals("شمس", abj.normalizedArabic)
        assertEquals(1340, abj.jummalKabir)
        assertEquals(8, abj.jummalSaghir)
    }

    @Test
    fun testRejectionOfLatinScriptForMaghribi() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "SHAMS",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Arabic script"))
    }
}
