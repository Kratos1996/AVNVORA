package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Ruleset Isolation Test.
 *
 * Verifies strict tradition isolation:
 * - Single-tradition grounding does not leak across traditions.
 * - Non-personality systems (Gematria, Abjad, Katapayadi) never generate personality predictions.
 * - Cross-tradition comparisons maintain distinct labeled sections without universal blending.
 */
class NumerologyAiRulesetIsolationTest {

    private val connector = NumerologyFeatureDataConnector()
    private val engine = DeterministicNumerologyExplanationEngine()

    @Test
    fun testSingleTradition_PythagoreanDoesNotLeakChaldean() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(
            result = res,
            userQuestion = "What does my name number mean?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )

        val explanation = (engine.explain(context) as AynvoraResult.Success).value
        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, explanation.referencedRulesetId)

        // Pythagorean name number is 8
        assertTrue(explanation.answer.contains("8"), "Must reference Pythagorean Name Number 8")
        assertFalse(
            explanation.answer.contains("Chaldean Cheiro"),
            "Must not leak Chaldean tradition name as authority"
        )
    }

    @Test
    fun testNonPersonalityTradition_KatapayadiRejectsHoroscope() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "गोपीभाग्यमधुव्रात",
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(
            result = res,
            userQuestion = "What is my personality horoscope from Katapayadi?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )

        assertFalse(context.isPersonalityInterpretation, "Katapayadi is marked non-personality")
        val explanation = (engine.explain(context) as AynvoraResult.Success).value

        assertTrue(
            explanation.answer.contains("Non-Personality System Notice") ||
                    explanation.answer.contains("mnemonic") ||
                    explanation.answer.contains("does not treat it as a personality horoscope"),
            "Must state non-personality nature of Katapayadi"
        )
        assertFalse(
            explanation.answer.contains("Your personality is"),
            "Must not invent personality predictions"
        )
    }

    @Test
    fun testNonPersonalityTradition_ArabicAbjadRejectsHoroscope() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            fullName = "محمد",
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(
            result = res,
            userQuestion = "What is my personality according to Abjad?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )

        assertFalse(context.isPersonalityInterpretation, "Abjad is non-personality")
        val explanation = (engine.explain(context) as AynvoraResult.Success).value

        assertTrue(
            explanation.answer.contains("Non-Personality") ||
                    explanation.answer.contains("alphanumeric calculation"),
            "Must emphasize alphanumeric nature without horoscopic claims"
        )
    }

    @Test
    fun testCrossTraditionComparison_KeepsSectionsDistinct() = runBlocking {
        val reqPyth = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val resPyth =
            (NumerologyCalculationEngine.calculate(reqPyth) as AynvoraResult.Success).value

        val reqChald = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )
        val resChald =
            (NumerologyCalculationEngine.calculate(reqChald) as AynvoraResult.Success).value

        val context = connector.buildGroundingContext(
            result = resPyth,
            userQuestion = "How is my number interpreted differently in Pythagorean and Chaldean?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON,
            comparisonResults = listOf(resChald),
        )

        val explanation = (engine.explain(context) as AynvoraResult.Success).value
        assertEquals(
            NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON,
            explanation.questionCategory
        )

        // Must explicitly label sections and not combine into universal archetype
        assertTrue(explanation.answer.contains("Pythagorean"), "Must have Pythagorean section")
        assertTrue(explanation.answer.contains("Chaldean"), "Must have Chaldean section")
        assertFalse(
            explanation.answer.contains("Universal meaning of"),
            "Must not blend into a universal meaning"
        )
    }
}
