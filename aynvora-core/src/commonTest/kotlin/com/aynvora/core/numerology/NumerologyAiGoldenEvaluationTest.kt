package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 10.7: Golden AI Response Evaluation Test.
 *
 * Evaluates generated explanations against 10 strict property checks across multiple traditions:
 * 1. correct number preservation
 * 2. correct ruleset
 * 3. correct source
 * 4. correct interpretation
 * 5. no unsupported claims
 * 6. no invented calculation
 * 7. no invented source
 * 8. no cross-tradition leakage
 * 9. requested locale
 * 10. safety framing
 */
class NumerologyAiGoldenEvaluationTest {

    private val connector = NumerologyFeatureDataConnector()
    private val engine = DeterministicNumerologyExplanationEngine()

    data class GoldenFixture(
        val rulesetId: String,
        val day: Int,
        val month: Int,
        val year: Int,
        val name: String?,
        val expectedPrimaryNumber: String,
        val expectedSourceKeywords: List<String>,
        val expectedTraditionName: String,
        val isNonPersonality: Boolean = false,
        val locale: String = "en",
    )

    private val all12CanonicalFixtures = listOf(
        // 1. Pythagorean Western
        GoldenFixture(
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            day = 11, month = 7, year = 1996, name = "ISHANT",
            expectedPrimaryNumber = "7",
            expectedSourceKeywords = listOf("Goodwin", "Campbell"),
            expectedTraditionName = "Pythagorean",
        ),
        // 2. Chaldean (Cheiro)
        GoldenFixture(
            rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
            day = 7, month = 1, year = 2000, name = "ALEX",
            expectedPrimaryNumber = "7",
            expectedSourceKeywords = listOf("Cheiro"),
            expectedTraditionName = "Chaldean",
        ),
        // 3. Indian Ank Jyotish
        GoldenFixture(
            rulesetId = NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
            day = 15, month = 8, year = 1947, name = "BHARAT",
            expectedPrimaryNumber = "6",
            expectedSourceKeywords = listOf("Johari", "Sethuraman", "Katakkar"),
            expectedTraditionName = "Indian / Vedic",
        ),
        // 4. Classical Lo Shu Magic Square
        GoldenFixture(
            rulesetId = NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
            day = 7, month = 7, year = 1977, name = null,
            expectedPrimaryNumber = "1",
            expectedSourceKeywords = listOf("Webster", "Chinese", "Luoshu", "I Ching"),
            expectedTraditionName = "Lo Shu",
        ),
        // 5. Classical Hebrew Gematria (Ragil)
        GoldenFixture(
            rulesetId = NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
            day = 1, month = 1, year = 2000, name = "שלום",
            expectedPrimaryNumber = "376",
            expectedSourceKeywords = listOf("Sefer Yetzirah", "Scholem", "Gematria"),
            expectedTraditionName = "Hebrew Gematria",
            isNonPersonality = true,
        ),
        // 6. Hebrew Mispar Gadol
        GoldenFixture(
            rulesetId = NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
            day = 1, month = 1, year = 2000, name = "שלום",
            expectedPrimaryNumber = "936",
            expectedSourceKeywords = listOf("Cordovero", "Pardes Rimonim"),
            expectedTraditionName = "Mispar Gadol",
            isNonPersonality = true,
        ),
        // 7. Arabic Abjad Mashriqi
        GoldenFixture(
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
            day = 1, month = 1, year = 2000, name = "الله",
            expectedPrimaryNumber = "66",
            expectedSourceKeywords = listOf("Ibn Khaldun", "Muqaddimah"),
            expectedTraditionName = "Arabic Abjad",
            isNonPersonality = true,
        ),
        // 8. Arabic Abjad Maghribi
        GoldenFixture(
            rulesetId = NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
            day = 1, month = 1, year = 2000, name = "شمس",
            expectedPrimaryNumber = "1340",
            expectedSourceKeywords = listOf("Ibn al-Banna", "Marrakushi", "Ibn Khaldun"),
            expectedTraditionName = "Maghribi",
            isNonPersonality = true,
        ),
        // 9. Renaissance Agrippan Arithmancy
        GoldenFixture(
            rulesetId = NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
            day = 14, month = 9, year = 1486, name = "AGRIPPA",
            expectedPrimaryNumber = "2",
            expectedSourceKeywords = listOf("Agrippa", "Occulta"),
            expectedTraditionName = "Agrippan",
        ),
        // 10. Indian Katapayadi Mnemonic
        GoldenFixture(
            rulesetId = NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
            day = 1, month = 1, year = 2000, name = "खगो",
            expectedPrimaryNumber = "32",
            expectedSourceKeywords = listOf("Sadratnamala", "Sankaravarman", "Haridatta"),
            expectedTraditionName = "Katapayadi",
            isNonPersonality = true,
        ),
        // 11. Chinese Nine Star Ki
        GoldenFixture(
            rulesetId = NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
            day = 1, month = 2, year = 1996, name = null,
            expectedPrimaryNumber = "5",
            expectedSourceKeywords = listOf("Yoshikawa", "Kushi", "Ki"),
            expectedTraditionName = "Nine Star Ki",
        ),
        // 12. Tarot Birth Cards
        GoldenFixture(
            rulesetId = NumerologyRuleset.TAROT_BIRTH_CARD_V1.id,
            day = 1, month = 1, year = 1994, name = null,
            expectedPrimaryNumber = "7", // The Chariot VII (1+1+1994=1996->25->7)
            expectedSourceKeywords = listOf("Greer", "Arrien"),
            expectedTraditionName = "Tarot",
        ),
    )

    @Test
    fun testAll12CanonicalRulesets_SatisfyGoldenProperties() = runBlocking {
        for (fixture in all12CanonicalFixtures) {
            val req = NumerologyRequest(
                birthDay = fixture.day,
                birthMonth = fixture.month,
                birthYear = fixture.year,
                fullName = fixture.name,
                rulesetId = fixture.rulesetId,
            )
            val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
            val context = connector.buildGroundingContext(
                result = res,
                userQuestion = "Explain my core calculation and meaning.",
                requestedLocale = fixture.locale,
                questionCategory = NumerologyAiQuestionCategory.EXPLAIN_RESULT,
            )

            val explanationRes = engine.explain(context)
            assertTrue(
                explanationRes is AynvoraResult.Success,
                "Explanation must succeed for ${fixture.rulesetId}"
            )
            val response = explanationRes.value

            // 1. Correct number preservation
            assertTrue(
                response.answer.contains(fixture.expectedPrimaryNumber),
                "Response must preserve primary number ${fixture.expectedPrimaryNumber} for ${fixture.rulesetId}. Answer: ${response.answer}",
            )

            // 2. Correct ruleset
            assertEquals(fixture.rulesetId, response.referencedRulesetId)

            // 3. Correct source attribution
            assertTrue(
                response.citedSources.any { source ->
                    fixture.expectedSourceKeywords.any { kw ->
                        source.contains(
                            kw,
                            ignoreCase = true
                        )
                    }
                },
                "Cited sources ${response.citedSources} must include one of ${fixture.expectedSourceKeywords} for ${fixture.rulesetId}",
            )

            // 4. Correct interpretation binding
            assertTrue(
                response.referencedEvidenceIds.isNotEmpty(),
                "Must reference evidence IDs for ${fixture.rulesetId}"
            )

            // 5. No unsupported claims (fatalism / medical / financial)
            assertFalse(response.answer.contains("guaranteed future", ignoreCase = true))
            assertFalse(response.answer.contains("will cure", ignoreCase = true))
            assertFalse(response.answer.contains("surely win lottery", ignoreCase = true))

            // 6. No invented calculation
            assertFalse(response.answer.contains("recalculated", ignoreCase = true))
            assertFalse(response.answer.contains("i calculated", ignoreCase = true))

            // 7. No invented source
            assertFalse(response.answer.contains("secret book", ignoreCase = true))

            // 8. No cross-tradition leakage
            if (fixture.rulesetId == NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id) {
                assertFalse(response.answer.contains("Navagraha", ignoreCase = true))
                assertFalse(response.answer.contains("Cheiro's Book", ignoreCase = true))
            }

            // 9. Requested locale
            assertEquals(fixture.locale, response.locale)

            // 10. Safety framing & non-personality banner
            if (fixture.isNonPersonality) {
                assertTrue(
                    response.answer.contains("Non-Personality System Notice", ignoreCase = true),
                    "Non-personality tradition ${fixture.rulesetId} must include explicit non-personality notice banner",
                )
            } else {
                assertTrue(
                    response.answer.contains("Contemplative Reflection", ignoreCase = true) ||
                            response.answer.contains("Reflective Contemplation", ignoreCase = true),
                    "Tradition ${fixture.rulesetId} must include reflective framing for self-awareness",
                )
            }
        }
    }

    @Test
    fun testMultipleQuestionCategories_GroundedCorrectly() = runBlocking {
        val req = NumerologyRequest(
            birthDay = 11, birthMonth = 7, birthYear = 1996, fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value

        val categoriesToTest = listOf(
            NumerologyAiQuestionCategory.EXPLAIN_CALCULATION,
            NumerologyAiQuestionCategory.EXPLAIN_TRADITION,
            NumerologyAiQuestionCategory.CLARIFY_SOURCE,
            NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
        )

        for (category in categoriesToTest) {
            val context = connector.buildGroundingContext(
                result = res,
                userQuestion = "Please address: ${category.name}",
                requestedLocale = "en",
                questionCategory = category,
            )

            val explanationRes = engine.explain(context)
            assertTrue(
                explanationRes is AynvoraResult.Success,
                "Category ${category.name} must succeed"
            )
            val response = explanationRes.value

            assertEquals(category, response.questionCategory)
            assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, response.referencedRulesetId)
            assertTrue(
                response.answer.isNotBlank(),
                "Answer for ${category.name} must not be blank"
            )
        }
    }
}
