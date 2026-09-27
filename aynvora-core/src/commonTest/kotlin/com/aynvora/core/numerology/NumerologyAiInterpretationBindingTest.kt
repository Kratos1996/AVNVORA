package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.7: Interpretation Package Integrity & Binding Test.
 *
 * Verifies that AI grounding requests resolve interpretation content using the exact ruleset
 * that generated the calculation, never generic number interpretations.
 */
class NumerologyAiInterpretationBindingTest {

    private val connector = NumerologyFeatureDataConnector()

    @Test
    fun testNumber7_StrictContentIsolationAcrossTraditions() {
        // 1. Pythagorean 7: Life Path 7 -> Goodwin & Jordan archetype
        val pythagoreanResult =
            getResultForDate(11, 7, 1996, NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id)
        val pythContext = connector.buildGroundingContext(pythagoreanResult)
        assertNotNull(pythContext.primaryInterpretation)
        assertEquals("num_interp_pythagorean_7", pythContext.primaryInterpretation.contentId)
        assertEquals(
            NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            pythContext.primaryInterpretation.rulesetId
        )
        assertTrue(pythContext.primaryInterpretation.sourceReferences.any {
            it.contains(
                "Goodwin",
                ignoreCase = true
            )
        })

        // 2. Chaldean 7: Cheiro's 7 -> Moon / Neptune spiritual archetype
        val chaldeanResult = getResultForDate(7, 1, 2000, NumerologyRuleset.CHALDEAN_CHEIRO_V1.id)
        val chaldeanContext = connector.buildGroundingContext(chaldeanResult)
        assertNotNull(chaldeanContext.primaryInterpretation)
        assertEquals(
            NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
            chaldeanContext.primaryInterpretation.rulesetId
        )
        assertTrue(chaldeanContext.primaryInterpretation.sourceReferences.any {
            it.contains(
                "Cheiro",
                ignoreCase = true
            )
        })

        // 3. Indian Vedic 7: Ketu planet ruler
        val indianResult = getResultForDate(7, 1, 2000, NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id)
        val indianContext = connector.buildGroundingContext(indianResult)
        assertNotNull(indianContext.primaryInterpretation)
        assertEquals("num_interp_indian_7", indianContext.primaryInterpretation.contentId)
        assertEquals(
            NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
            indianContext.primaryInterpretation.rulesetId
        )
        assertTrue(indianContext.primaryInterpretation.sourceReferences.any {
            it.contains(
                "Johari",
                ignoreCase = true
            ) || it.contains("Sethuraman", ignoreCase = true) || it.contains(
                "Katakkar",
                ignoreCase = true
            )
        })

        // 4. Lo Shu 7: Magic Square digit 7 / Lake Trigram
        val loshuResult = getResultForDate(7, 7, 1977, NumerologyRuleset.LO_SHU_CLASSICAL_V1.id)
        val loshuContext = connector.buildGroundingContext(loshuResult)
        assertNotNull(loshuContext.primaryInterpretation)
        assertEquals(
            NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
            loshuContext.primaryInterpretation.rulesetId
        )
        assertTrue(loshuContext.primaryInterpretation.contentId.startsWith("num_interp_loshu_"))

        // 5. Nine Star Ki 7: 7 Red Metal
        val nskResult = getResultForDate(1, 1, 1985, NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id)
        val nskContext = connector.buildGroundingContext(nskResult)
        assertNotNull(nskContext.primaryInterpretation)
        assertEquals(
            NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
            nskContext.primaryInterpretation.rulesetId
        )
        assertTrue(nskContext.primaryInterpretation.contentId.contains("ninestarki_7"))

        // 6. Tarot 7: The Chariot (Major Arcana VII) (1+1+1994=1996->25->7)
        val tarotResult = getResultForDate(1, 1, 1994, NumerologyRuleset.TAROT_BIRTH_CARD_V1.id)
        val tarotContext = connector.buildGroundingContext(tarotResult)
        assertNotNull(tarotContext.primaryInterpretation)
        assertEquals(
            NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
            tarotContext.primaryInterpretation.rulesetId
        )
        assertEquals("num_interp_tarot_card_7", tarotContext.primaryInterpretation.contentId)
        assertTrue(tarotContext.primaryInterpretation.sourceReferences.any {
            it.contains(
                "Greer",
                ignoreCase = true
            )
        })
    }

    @Test
    fun testMasterNumbers_11_22_33_Binding() {
        // Master Number 11 in Pythagorean (7 + 2 + (1991->2) = 11)
        val res11 = getResultForDate(7, 2, 1991, NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id)
        val ctx11 = connector.buildGroundingContext(res11)
        val interp11 = ctx11.primaryInterpretation
        assertNotNull(interp11)
        assertEquals(11, interp11.value)
        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, interp11.rulesetId)

        // Master Number 22 in Pythagorean (4 + 9 + (1980->9) = 22)
        val res22 = getResultForDate(4, 9, 1980, NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id)
        val ctx22 = connector.buildGroundingContext(res22)
        val interp22 = ctx22.primaryInterpretation
        assertNotNull(interp22)
        assertEquals(22, interp22.value)
        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, interp22.rulesetId)

        // Master Number 33 in Pythagorean (11 + 11 + (2009->11) = 33)
        val res33 = getResultForDate(11, 11, 2009, NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id)
        val ctx33 = connector.buildGroundingContext(res33)
        val interp33 = ctx33.primaryInterpretation
        assertNotNull(interp33)
        assertEquals(33, interp33.value)
        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, interp33.rulesetId)
    }

    private fun getResultForDate(
        day: Int,
        month: Int,
        year: Int,
        rulesetId: String
    ): NumerologyResult {
        val req = NumerologyRequest(
            birthDay = day,
            birthMonth = month,
            birthYear = year,
            fullName = if (rulesetId in listOf(
                    NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
                    NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
                    NumerologyRuleset.TAROT_BIRTH_CARD_V1.id
                )
            ) null else "TEST PROFILE",
            rulesetId = rulesetId,
        )
        return (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
    }
}
