package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Grounding Test.
 *
 * Verifies that the conversational AI layer operates strictly as an explanation layer:
 * - Numerical calculations are authoritative only when supplied by AYNVORA engine.
 * - AI never recalculates or modifies numbers.
 * - AI never invents sources or formulas.
 * - Source fidelity and ruleset isolation are preserved.
 */
class NumerologyAiGroundingTest {

    private val connector = NumerologyFeatureDataConnector()
    private val engine = DeterministicNumerologyExplanationEngine()

    private fun getGoldenPythagoreanResult(): NumerologyResult {
        val request = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            targetYear = 2026,
            targetMonth = 9,
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = NumerologyCalculationEngine.calculate(request)
        assertTrue(res is AynvoraResult.Success)
        return res.value
    }

    @Test
    fun testGroundedExplanation_ContainsAuthoritativeNumbers() = runBlocking {
        val result = getGoldenPythagoreanResult()
        val context = connector.buildGroundingContext(
            result = result,
            userQuestion = "What does my Life Path mean?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )

        val explanationRes = engine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, response.referencedRulesetId)
        assertEquals("en", response.locale)
        assertEquals(NumerologyAiSafetyStatus.DETERMINISTIC_DIRECT, response.safetyStatus)
        assertTrue(response.isOfflineExecution)

        // Must contain authoritative numbers from AYNVORA engine
        assertTrue(response.answer.contains("7"), "Answer must contain Life Path 7")
        assertTrue(response.answer.contains("11"), "Answer must contain Radical 11")

        // Must cite authentic sources
        assertTrue(response.citedSources.isNotEmpty())
        assertTrue(response.citedSources.any {
            it.contains(
                "Goodwin",
                ignoreCase = true
            ) || it.contains("Pythagorean", ignoreCase = true)
        })

        // Must have referenced evidence IDs
        assertTrue(response.referencedEvidenceIds.isNotEmpty())
    }

    @Test
    fun testCalculationExplanation_PreservesTraceWithoutRecalculation() = runBlocking {
        val result = getGoldenPythagoreanResult()
        val context = connector.buildGroundingContext(
            result = result,
            userQuestion = "How was my Life Path 7 calculated?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_CALCULATION,
        )

        val explanationRes = engine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        assertEquals(NumerologyAiQuestionCategory.EXPLAIN_CALCULATION, response.questionCategory)
        assertTrue(
            response.answer.contains("Calculation Trace"),
            "Must explain the calculation trace"
        )
        assertTrue(response.answer.contains("7"), "Must mention final value 7")
        assertTrue(
            response.answer.contains("11/7/1996") || response.answer.contains("11"),
            "Must reference birth date inputs"
        )
    }

    @Test
    fun testSourceClarification_SurfacesAuthoritativeProvenance() = runBlocking {
        val result = getGoldenPythagoreanResult()
        val context = connector.buildGroundingContext(
            result = result,
            userQuestion = "Which source is this interpretation based on?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.CLARIFY_SOURCE,
        )

        val explanationRes = engine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        assertEquals(NumerologyAiQuestionCategory.CLARIFY_SOURCE, response.questionCategory)
        assertTrue(response.answer.contains("Source Provenance"), "Must clarify source provenance")
        assertTrue(response.citedSources.isNotEmpty(), "Must cite sources")
    }

    @Test
    fun testReflectiveQuestion_ContainsNonDeterministicFraming() = runBlocking {
        val result = getGoldenPythagoreanResult()
        val context = connector.buildGroundingContext(
            result = result,
            userQuestion = "What does this number mean for self-reflection?",
            requestedLocale = "en",
            questionCategory = NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
        )

        val explanationRes = engine.explain(context)
        assertTrue(explanationRes is AynvoraResult.Success)
        val response = explanationRes.value

        assertTrue(
            response.answer.contains("Reflective Contemplation"),
            "Must be framed reflectively"
        )
        assertFalse(response.answer.contains("guaranteed"), "Must not make guarantees")
        assertFalse(response.answer.contains("destined to"), "Must not make fatalistic predictions")
    }

    @Test
    fun testGroundingContext_ExtractsStrictLocalOnlyEvidence() {
        val result = getGoldenPythagoreanResult()
        val context = connector.buildGroundingContext(result = result)

        assertEquals(com.aynvora.core.ai.AiPrivacyClass.STRICT_LOCAL_ONLY, context.privacyClass)
        assertTrue(context.evidenceItems.isNotEmpty())
        assertTrue(context.evidenceItems.all { it.privacyClass == com.aynvora.core.ai.AiPrivacyClass.STRICT_LOCAL_ONLY })
        assertEquals("7", context.calculatedValues["DESTINY_NUMBER"])
        assertEquals("11", context.calculatedValues["RADICAL_NUMBER"])
        assertEquals("8", context.calculatedValues["NAME_NUMBER"])
    }
}
