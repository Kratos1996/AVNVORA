package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Phase 10.7: Citation Integrity & Invented Source Rejection Test.
 *
 * Verifies that:
 * 1. AI cannot invent sources, manuscripts, or unverified authors.
 * 2. Any invented source reference causes immediate validation failure.
 */
class NumerologyAiCitationIntegrityTest {

    private val validator = NumerologyAiOutputValidator()
    private val connector = NumerologyFeatureDataConnector()

    private fun getGoldenContext(): NumerologyAiGroundingContext {
        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        return connector.buildGroundingContext(res)
    }

    @Test
    fun testInventedSource_SecretManuscriptRejected() {
        val context = getGoldenContext()
        val maliciousOutput =
            "Your number is 7 according to the secret book discovered in Alexandria."
        val validation = validator.validateOutput(maliciousOutput, context)
        assertTrue(validation is AynvoraResult.Failure, "Invented source must be rejected")
    }

    @Test
    fun testInventedSource_SecretManuscriptExplicitRejected() {
        val context = getGoldenContext()
        val maliciousOutput =
            "Your Life Path 7 is grounded in a secret manuscript from an unverified modern channel."
        val validation = validator.validateOutput(maliciousOutput, context)
        assertTrue(validation is AynvoraResult.Failure, "Secret manuscript claim must be rejected")
    }

    @Test
    fun testValidCitation_PassesValidation() {
        val context = getGoldenContext()
        val validOutput = """
            Verified Calculated Values:
            • Life Path: 7
            
            Primary Focus: Matthew Oliver Goodwin (1981)
            The archetype represents introspective depth and analytical discernment.
            
            Contemplative Reflection:
            Consider how this symbol encourages quiet study and personal inquiry.
        """.trimIndent()

        val validation = validator.validateOutput(validOutput, context)
        assertTrue(
            validation is AynvoraResult.Success<*>,
            "Valid source citation must pass validation"
        )
    }
}
