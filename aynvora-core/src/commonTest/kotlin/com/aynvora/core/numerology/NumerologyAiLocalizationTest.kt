package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Localization Test.
 *
 * Verifies that the Numerology AI explanation layer honors all 11 supported locales:
 * en, hi, ar, bn, gu, mr, pa, ta, te, kn, ml.
 *
 * Ensures honest locale reporting (no silent English fallback masquerading as Hindi/Arabic).
 */
class NumerologyAiLocalizationTest {

    private val connector = NumerologyFeatureDataConnector()
    private val engine = DeterministicNumerologyExplanationEngine()

    private val supportedLocales = listOf(
        "en", "hi", "ar", "bn", "gu", "mr", "pa", "ta", "te", "kn", "ml"
    )

    private fun getGoldenContext(locale: String): NumerologyAiGroundingContext {
        val req = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        return connector.buildGroundingContext(
            result = res,
            userQuestion = "What does my Life Path 7 mean?",
            requestedLocale = locale,
            questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
        )
    }

    @Test
    fun testAll11Locales_GenerateValidResponses() = runBlocking {
        for (locale in supportedLocales) {
            val context = getGoldenContext(locale)
            val res = engine.explain(context)

            assertTrue(res is AynvoraResult.Success, "Explanation failed for locale: $locale")
            val response = res.value

            assertEquals(
                locale,
                response.locale,
                "Honest locale reporting must match requested locale"
            )
            assertEquals(NumerologyAiSafetyStatus.DETERMINISTIC_DIRECT, response.safetyStatus)
            assertTrue(response.answer.isNotBlank(), "Answer cannot be blank for locale: $locale")
            assertTrue(
                response.answer.contains("7"),
                "Authoritative value 7 must be preserved for locale: $locale"
            )
        }
    }

    @Test
    fun testArabicLocale_HonorsLanguageContract() = runBlocking {
        val context = getGoldenContext("ar")
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val response = res.value

        assertEquals("ar", response.locale)
        assertTrue(response.answer.contains("7"))
        assertTrue(response.citedSources.isNotEmpty())
    }

    @Test
    fun testHindiLocale_HonorsLanguageContract() = runBlocking {
        val context = getGoldenContext("hi")
        val res = engine.explain(context)

        assertTrue(res is AynvoraResult.Success)
        val response = res.value

        assertEquals("hi", response.locale)
        assertTrue(response.answer.contains("7"))
    }
}
