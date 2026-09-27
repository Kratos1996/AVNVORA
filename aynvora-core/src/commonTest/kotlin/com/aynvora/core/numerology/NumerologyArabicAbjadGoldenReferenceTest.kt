package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.2: Golden Reference Test for Arabic Hisab al-Jummal (Eastern Mashriqi Standard).
 *
 * Authorities:
 * - Ibn Khaldun, "The Muqaddimah" (1377 CE), Chapter 6, Section 28 (Ilm al-Huruf)
 * - Ahmad al-Buni, "Shams al-Ma'arif al-Kubra" (c. 1225 CE)
 */
class NumerologyArabicAbjadGoldenReferenceTest {

    @Test
    fun testAllahReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "الله",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        val abj = profile.abjad
        assertNotNull(abj)
        assertEquals("الله", abj.rawText)
        assertEquals("الله", abj.normalizedArabic)

        // Jummal Kabir (Great Sum):
        // ا (1) + ل (30) + ل (30) + ه (5) = 66
        assertEquals(66, abj.jummalKabir)

        // Jummal Saghir (Small Root Reduction):
        // 6+6 = 12 -> 1+2 = 3
        assertEquals(3, abj.jummalSaghir)

        // Letter breakdown
        assertEquals(4, abj.letterValues.size)
        assertEquals("ا" to 1, abj.letterValues[0])
        assertEquals("ل" to 30, abj.letterValues[1])
        assertEquals("ل" to 30, abj.letterValues[2])
        assertEquals("ه" to 5, abj.letterValues[3])

        // Evidence graph
        val graph = NumerologyEvidenceGraphFactory.create(result.value, request)
        assertTrue(graph.nodes.containsKey("num_fact_arabic_text"))
        assertTrue(graph.nodes.containsKey("num_derived_abjad_kabir"))
        assertTrue(graph.nodes.containsKey("num_derived_abjad_saghir"))
    }

    @Test
    fun testMuhammadReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "محمد",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // م (40) + ح (8) + م (40) + د (4) = 92
        assertEquals(92, abj.jummalKabir)
        // 9+2 = 11 -> 1+1 = 2
        assertEquals(2, abj.jummalSaghir)
    }

    @Test
    fun testSalamReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "سلام",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // س (60) + ل (30) + ا (1) + م (40) = 131
        assertEquals(131, abj.jummalKabir)
        // 1+3+1 = 5
        assertEquals(5, abj.jummalSaghir)
    }

    @Test
    fun testTashkeelStrippingAndTaMarbuta() {
        // "مَحَبَّة" with fatha, shaddah, ta marbuta
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "مَحَبَّة",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val abj = result.value.profile.abjad
        assertNotNull(abj)

        // Tashkeel stripped, normalized to "محبة"
        assertEquals("محبة", abj.normalizedArabic)

        // م (40) + ح (8) + ب (2) + ة (400) = 450
        assertEquals(450, abj.jummalKabir)
        // 4+5+0 = 9
        assertEquals(9, abj.jummalSaghir)
    }

    @Test
    fun testRejectionOfLatinScriptForArabicAbjad() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "MUHAMMAD",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Arabic script"))
    }

    @Test
    fun testRejectionOfHebrewScriptForArabicAbjad() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "שלום",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Arabic script"))
    }

    @Test
    fun testNoRadicalOrDestinyForArabicAbjad() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "سلام",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Abjad arithmetic is alphanumeric equivalence, not astrological birth path
        assertNull(profile.radical)
        assertNull(profile.destiny)
        assertNull(profile.loShu)
        assertNull(profile.planetaryAssociation)
        assertTrue(profile.combinations.isEmpty())
    }

    @Test
    fun testDeterministicRepeatability() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "محمد",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )

        val r1 = NumerologyCalculationEngine.calculate(request)
        val r2 = NumerologyCalculationEngine.calculate(request)

        assertTrue(r1 is AynvoraResult.Success)
        assertTrue(r2 is AynvoraResult.Success)
        assertEquals(r1.value.profile.abjad?.jummalKabir, r2.value.profile.abjad?.jummalKabir)
        assertEquals(r1.value.profile.abjad?.jummalSaghir, r2.value.profile.abjad?.jummalSaghir)
    }
}
