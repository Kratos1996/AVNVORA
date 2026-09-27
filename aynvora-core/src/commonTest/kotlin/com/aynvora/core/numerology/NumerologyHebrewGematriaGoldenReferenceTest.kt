package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Phase 10.2: Golden Reference Test for Classical Hebrew Gematria.
 *
 * Authorities:
 * - Sefer Yetzirah (Book of Creation, 2nd–6th c. CE)
 * - Talmud Bavli (Tractate Sanhedrin 22a)
 * - Rabbi Moses Cordovero, "Pardes Rimonim" (1591), Gate 30
 */
class NumerologyHebrewGematriaGoldenReferenceTest {

    @Test
    fun testShalomReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "שלום",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Gematria result must be present
        val gem = profile.gematria
        assertNotNull(gem)
        assertEquals("שלום", gem.rawText)
        assertEquals("שלום", gem.normalizedHebrew)

        // Mispar Hechrachi (Absolute Value):
        // ש (300) + ל (30) + ו (6) + ם (40) = 376
        assertEquals(376, gem.absoluteValue)

        // Mispar Katan (Small Root Reduction):
        // 3+7+6 = 16 -> 1+6 = 7
        assertEquals(7, gem.reducedValue)

        // Letter breakdown
        assertEquals(4, gem.letterValues.size)
        assertEquals("ש" to 300, gem.letterValues[0])
        assertEquals("ל" to 30, gem.letterValues[1])
        assertEquals("ו" to 6, gem.letterValues[2])
        assertEquals("ם" to 40, gem.letterValues[3])

        // Evidence graph
        val graph = NumerologyEvidenceGraphFactory.create(result.value, request)
        assertTrue(graph.nodes.containsKey("num_fact_hebrew_text"))
        assertTrue(graph.nodes.containsKey("num_derived_gematria_absolute"))
        assertTrue(graph.nodes.containsKey("num_derived_gematria_reduced"))
    }

    @Test
    fun testChaiReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "חי",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // ח (8) + י (10) = 18
        assertEquals(18, gem.absoluteValue)
        // 1+8 = 9
        assertEquals(9, gem.reducedValue)
    }

    @Test
    fun testAhavahReferenceVector() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "אהבה",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        // א (1) + ה (5) + ב (2) + ה (5) = 13
        assertEquals(13, gem.absoluteValue)
        // 1+3 = 4
        assertEquals(4, gem.reducedValue)
    }

    @Test
    fun testHebrewNiqqudStripping() {
        // "שָׁלוֹם" with vowel points (kamatz, shin dot, holam)
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "שָׁלוֹם",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val gem = result.value.profile.gematria
        assertNotNull(gem)

        assertEquals("שלום", gem.normalizedHebrew)
        assertEquals(376, gem.absoluteValue)
        assertEquals(7, gem.reducedValue)
    }

    @Test
    fun testRejectionOfLatinScriptForHebrewGematria() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "SHALOM",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Hebrew script"))
    }

    @Test
    fun testRejectionOfDevanagariScriptForHebrewGematria() {
        val request = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "शान्ति",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("requires Hebrew script"))
    }

    @Test
    fun testNoRadicalOrDestinyForHebrewGematria() {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "שלום",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val result = NumerologyCalculationEngine.calculate(request)
        assertTrue(result is AynvoraResult.Success)
        val profile = result.value.profile

        // Gematria is text-based alphanumeric equivalence, not life path astrology
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
            fullName = "שלום",
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        )

        val r1 = NumerologyCalculationEngine.calculate(request)
        val r2 = NumerologyCalculationEngine.calculate(request)

        assertTrue(r1 is AynvoraResult.Success)
        assertTrue(r2 is AynvoraResult.Success)
        assertEquals(
            r1.value.profile.gematria?.absoluteValue,
            r2.value.profile.gematria?.absoluteValue
        )
        assertEquals(
            r1.value.profile.gematria?.reducedValue,
            r2.value.profile.gematria?.reducedValue
        )
    }
}
