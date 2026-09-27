package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.7: EvidenceGraph Integrity and Cross-Tradition Contract Test.
 *
 * Verifies that:
 * 1. Single-tradition grounding context contains ONLY evidence strictly from the selected ruleset.
 * 2. Cross-tradition comparisons require explicit question category and comparison inputs.
 * 3. Unrelated traditions (Lo Shu, Tarot, Indian, Chaldean) are completely excluded from single-tradition contexts.
 */
class NumerologyAiEvidenceGraphIsolationTest {

    private val connector = NumerologyFeatureDataConnector()

    @Test
    fun testSingleTradition_PythagoreanExcludesForeignTraditions() {
        val req = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(res)

        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, context.rulesetId)

        // 1. Evidence items must not contain foreign IDs
        for (evidence in context.evidenceItems) {
            assertFalse(
                evidence.evidenceId.contains("chaldean", ignoreCase = true),
                "Must not leak Chaldean evidence"
            )
            assertFalse(
                evidence.evidenceId.contains("loshu", ignoreCase = true),
                "Must not leak Lo Shu evidence"
            )
            assertFalse(
                evidence.evidenceId.contains("tarot", ignoreCase = true),
                "Must not leak Tarot evidence"
            )
            assertFalse(
                evidence.evidenceId.contains("gematria", ignoreCase = true),
                "Must not leak Gematria evidence"
            )
            assertFalse(
                evidence.evidenceId.contains("abjad", ignoreCase = true),
                "Must not leak Abjad evidence"
            )
            assertFalse(
                evidence.evidenceId.contains("katapayadi", ignoreCase = true),
                "Must not leak Katapayadi evidence"
            )
        }

        // 2. Primary interpretation must belong strictly to Pythagorean
        assertNotNull(context.primaryInterpretation)
        assertEquals(
            NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            context.primaryInterpretation.rulesetId
        )

        // 3. Comparison profiles must be empty when not comparing
        assertTrue(
            context.comparisonProfiles.isEmpty(),
            "Single-tradition context must have empty comparison profiles"
        )
        assertTrue(
            context.comparisonInterpretations.isEmpty(),
            "Single-tradition context must have empty comparison interpretations"
        )
    }

    @Test
    fun testCrossTraditionComparison_RequiresExplicitContext() {
        val pythReq = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val pythRes =
            (NumerologyCalculationEngine.calculate(pythReq) as AynvoraResult.Success).value

        val chaldeanReq = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        )
        val chaldeanRes =
            (NumerologyCalculationEngine.calculate(chaldeanReq) as AynvoraResult.Success).value

        // Construct context with explicit comparison
        val context = connector.buildGroundingContext(
            result = pythRes,
            userQuestion = "Compare Pythagorean and Chaldean interpretations.",
            questionCategory = NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON,
            comparisonResults = listOf(chaldeanRes),
        )

        assertEquals(
            NumerologyAiQuestionCategory.CROSS_TRADITION_COMPARISON,
            context.questionCategory
        )
        assertEquals(1, context.comparisonProfiles.size)
        assertTrue(context.comparisonProfiles.containsKey(NumerologyRuleset.CHALDEAN_CHEIRO_V1.id))

        // System prompt generation must format traditions separately
        val systemPrompt = NumerologyAiPromptTemplate.buildSystemPrompt(context)
        assertTrue(systemPrompt.contains("treat each tradition separately"))

        val userPrompt = NumerologyAiPromptTemplate.buildUserPrompt(context)
        assertTrue(userPrompt.contains("Comparison Evidence"))
        assertTrue(userPrompt.contains(NumerologyRuleset.CHALDEAN_CHEIRO_V1.id))
    }
}
