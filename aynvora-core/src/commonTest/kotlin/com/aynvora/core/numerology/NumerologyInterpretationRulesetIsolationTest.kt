package com.aynvora.core.numerology

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Phase 10.5 Semantic Integrity & Ruleset Isolation Test.
 *
 * Verifies that:
 * - Number 7 (and any other number) resolves STRICTLY according to the selected ruleset.
 * - Pythagorean 7 != Chaldean 7 != Indian 7 != Lo Shu 7 != Agrippan 7 != Nine Star Ki 7 != Tarot 7.
 * - No global semantic leakage or unpartitioned fallback overrides the ruleset authority.
 */
class NumerologyInterpretationRulesetIsolationTest {

    @Test
    fun testArchetypeSevenStrictRulesetIsolation() {
        val pyth7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            subjectId = "pythagorean_7",
        )
        assertNotNull(pyth7)
        assertEquals("PYTHAGOREAN_WESTERN_V1", pyth7.rulesetId)
        assertEquals("numerology.interp.pythagorean_7.title", pyth7.titleKey)

        val chaldean7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
            subjectId = "chaldean_7",
        )
        assertNotNull(chaldean7)
        assertEquals("CHALDEAN_CHEIRO_V1", chaldean7.rulesetId)
        assertEquals("numerology.interp.chaldean_7.title", chaldean7.titleKey)

        val indian7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
            subjectId = "indian_7",
        )
        assertNotNull(indian7)
        assertEquals("INDIAN_ANK_JYOTISH_V1", indian7.rulesetId)
        assertEquals("numerology.interp.indian_7.title", indian7.titleKey)

        val loshu7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
            subjectId = "loshu_digit_7",
        )
        assertNotNull(loshu7)
        assertEquals("LO_SHU_CLASSICAL_V1", loshu7.rulesetId)

        val agrippan7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
            subjectId = "agrippan_scale_7",
        )
        assertNotNull(agrippan7)
        assertEquals("AGRIPPAN_OCCULT_V1", agrippan7.rulesetId)

        val nineStarKi7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
            subjectId = "ninestarki_7",
        )
        assertNotNull(nineStarKi7)
        assertEquals("CHINESE_NINE_STAR_KI_V1", nineStarKi7.rulesetId)

        val tarot7 = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
            subjectId = "tarot_card_7",
        )
        assertNotNull(tarot7)
        assertEquals("TAROT_BIRTH_CARD_V1", tarot7.rulesetId)

        // Verify distinct content IDs and distinct translation keys
        val contentIds = listOf(
            pyth7.contentId, chaldean7.contentId, indian7.contentId,
            loshu7.contentId, agrippan7.contentId, nineStarKi7.contentId, tarot7.contentId
        )
        assertEquals(
            7,
            contentIds.distinct().size,
            "All 7 representations of archetype 7 must have unique contentIds"
        )

        val titleKeys = listOf(
            pyth7.titleKey, chaldean7.titleKey, indian7.titleKey,
            loshu7.titleKey, agrippan7.titleKey, nineStarKi7.titleKey, tarot7.titleKey
        )
        assertEquals(
            7,
            titleKeys.distinct().size,
            "All 7 representations of archetype 7 must have unique titleKeys"
        )
    }

    @Test
    fun testCrossRulesetLookupRejection() {
        // Attempting to look up a Pythagorean key under Chaldean ruleset must return null
        val leakedPyth = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
            subjectId = "pythagorean_7",
        )
        assertNull(leakedPyth, "Cross-ruleset lookup must fail to prevent semantic leakage")

        // Attempting to look up an Indian key under Pythagorean ruleset must return null
        val leakedIndian = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            subjectId = "indian_1",
        )
        assertNull(leakedIndian, "Cross-ruleset lookup must fail to prevent semantic leakage")

        // Attempting to look up a Tarot card under Agrippan ruleset must return null
        val leakedTarot = NumerologyInterpretationPackage.getInterpretation(
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
            subjectId = "tarot_card_0",
        )
        assertNull(leakedTarot, "Cross-ruleset lookup must fail to prevent semantic leakage")
    }
}
