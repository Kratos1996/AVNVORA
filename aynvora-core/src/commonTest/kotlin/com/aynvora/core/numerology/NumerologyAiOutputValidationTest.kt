package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Output Validation Test.
 *
 * Verifies that NumerologyAiOutputValidator strictly enforces:
 * - Preservation of deterministic numbers
 * - Rejection of medical, financial, and scientific claims
 * - Rejection of fatalistic predictions
 * - Rejection of non-personality tradition horoscope violations
 */
class NumerologyAiOutputValidationTest {

    private val validator = NumerologyAiOutputValidator()

    private fun createBaseContext(
        rulesetId: String = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        isPersonality: Boolean = true,
        calculatedValues: Map<String, String> = mapOf(
            "DESTINY_NUMBER" to "7",
            "RADICAL_NUMBER" to "11"
        ),
        sources: List<String> = listOf("Goodwin, Numerology: The Complete Guide"),
    ): NumerologyAiGroundingContext {
        return NumerologyAiGroundingContext(
            rulesetId = rulesetId,
            rulesetName = "Western Pythagorean Numerology",
            rulesetVersion = "1.0.0",
            primarySourceReference = sources.first(),
            birthDateDisplay = "11/7/1996",
            fullNameNormalized = "ISHANT",
            calculatedValues = calculatedValues,
            calculationTraces = mapOf("LIFE_PATH" to "11 + 7 + 1996 = 2014 -> 7"),
            primaryInterpretation = null,
            sourceReferences = sources,
            isPersonalityInterpretation = isPersonality,
        )
    }

    @Test
    fun testValidOutput_PassesValidation() {
        val context = createBaseContext()
        val text =
            "According to Western Pythagorean traditions, your Life Path number 7 is traditionally associated with intellectual analysis and introspection."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Success)
        assertTrue(result.value.contains("Life Path"))
    }

    @Test
    fun testEmptyOutput_Rejected() {
        val context = createBaseContext()
        val result = validator.validateOutput("   ", context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("empty", ignoreCase = true))
    }

    @Test
    fun testChangedNumber_Rejected() {
        val context = createBaseContext()
        // Text claims Life Path is 8 instead of 7
        val text = "Your Life Path number is 8. Number 8 governs leadership and material strength."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(
            result.message.contains(
                "contradicted",
                ignoreCase = true
            ) || result.message.contains("Life Path", ignoreCase = true)
        )
    }

    @Test
    fun testMedicalClaim_Rejected() {
        val context = createBaseContext()
        val text =
            "In Pythagorean numerology, Life Path 7 cures cancer and prevents cardiovascular disease."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("medical", ignoreCase = true))
    }

    @Test
    fun testFinancialGuarantee_Rejected() {
        val context = createBaseContext()
        val text =
            "With Life Path 7, you are guaranteed wealth and will surely win lottery by age 35."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("financial", ignoreCase = true))
    }

    @Test
    fun testFatalisticPrediction_Rejected() {
        val context = createBaseContext()
        val text = "Your number indicates that you will die and your accident is certain."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("fatalistic", ignoreCase = true))
    }

    @Test
    fun testScientificProofClaim_Rejected() {
        val context = createBaseContext()
        val text = "Numerology is a scientific proof and scientifically proven law."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("scientific", ignoreCase = true))
    }

    @Test
    fun testNonPersonalitySystem_HoroscopeRejected() {
        val context = createBaseContext(
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
            isPersonality = false,
        )
        val text =
            "Your Katapayadi number indicates your personality is shy, intuitive, and romantically ambitious."
        val result = validator.validateOutput(text, context)

        assertTrue(result is AynvoraResult.Failure)
        assertTrue(result.message.contains("non-personality", ignoreCase = true))
    }
}
