package com.aynvora.core.numerology

import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.5 Numerology Interpretation Core Test Suite.
 *
 * Verifies:
 * - Dynamic resolution of interpretations for all 12 rulesets.
 * - EvidenceGraph interpretation node creation and provenance linking.
 * - Strict non-personality classification for mnemonic and alphanumeric traditions.
 * - Non-fatalistic language policy and prohibition of medical/financial claims.
 */
class NumerologyInterpretationTest {

    @Test
    fun testResolveInterpretationsForAllRulesets() {
        val rulesets = NumerologyRuleset.ALL_RULESETS
        assertEquals(12, rulesets.size)

        for (ruleset in rulesets) {
            val req = when (ruleset) {
                NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1,
                NumerologyRuleset.HEBREW_MISPAR_GADOL_V1 -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "שלום",
                    rulesetId = ruleset.id,
                )

                NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1,
                NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1 -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "محمد",
                    rulesetId = ruleset.id,
                )

                NumerologyRuleset.INDIAN_KATAPAYADI_V1 -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "राम",
                    rulesetId = ruleset.id,
                )

                NumerologyRuleset.LO_SHU_CLASSICAL_V1,
                NumerologyRuleset.CHINESE_NINE_STAR_KI_V1,
                NumerologyRuleset.TAROT_BIRTH_CARD_V1 -> NumerologyRequest(
                    birthDay = 15, birthMonth = 8, birthYear = 1947,
                    fullName = null,
                    rulesetId = ruleset.id,
                )

                else -> NumerologyRequest(
                    birthDay = 15, birthMonth = 8, birthYear = 1947,
                    fullName = "INDIA",
                    rulesetId = ruleset.id,
                )
            }

            val calcResult = NumerologyCalculationEngine.calculate(req)
            assertTrue(
                calcResult is AynvoraResult.Success,
                "Calculation must succeed for ${ruleset.id}"
            )
            val result = calcResult.value

            val bundle = NumerologyInterpretationPackage.resolveInterpretations(result)
            assertEquals(ruleset.id, bundle.rulesetId)
            assertNotNull(
                bundle.primaryInterpretation,
                "Primary interpretation must be resolved for ${ruleset.id}"
            )
            assertTrue(
                bundle.allInterpretations.isNotEmpty(),
                "At least one interpretation must be present for ${ruleset.id}"
            )
            assertTrue(
                bundle.provenanceSources.isNotEmpty(),
                "Provenance sources must exist for ${ruleset.id}"
            )

            // Verify non-personality marking for Katapayadi, Gematria, Abjad
            when (ruleset) {
                NumerologyRuleset.INDIAN_KATAPAYADI_V1,
                NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1,
                NumerologyRuleset.HEBREW_MISPAR_GADOL_V1,
                NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1,
                NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1 -> {
                    assertFalse(
                        bundle.isPersonalityInterpretation,
                        "Tradition ${ruleset.name} must NOT be a personality interpretation"
                    )
                    assertNotNull(
                        bundle.nonPersonalityNoticeKey,
                        "Non-personality notice key must be present for ${ruleset.id}"
                    )
                }

                else -> {
                    assertTrue(
                        bundle.isPersonalityInterpretation,
                        "Tradition ${ruleset.name} should provide personality archetypes"
                    )
                }
            }
        }
    }

    @Test
    fun testEvidenceGraphContainsInterpretationNodes() {
        val req = NumerologyRequest(
            birthDay = 7, birthMonth = 7, birthYear = 1977,
            fullName = "SEEKER",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val result = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val graph =
            NumerologyEvidenceGraphFactory.create(result, req, timestampEpochMs = 1727400000000L)

        val interpNodes =
            graph.nodes.values.filter { it.category == EvidenceCategory.INTERPRETATION }
        assertTrue(interpNodes.isNotEmpty(), "EvidenceGraph must contain INTERPRETATION nodes")

        val primaryInterpNode = interpNodes.first()
        assertEquals(EvidenceCategory.INTERPRETATION, primaryInterpNode.category)
        assertTrue(primaryInterpNode.rawPayloadJson?.contains("titleKey") == true)
        assertTrue(primaryInterpNode.rawPayloadJson?.contains("summaryKey") == true)
        assertNotNull(primaryInterpNode.provenance.contentVersion)
        assertEquals("TRADITIONAL_INTERPRETATION", primaryInterpNode.provenance.contentType)

        // Verify edges: AUTHORIZES_INTERPRETATION and INTERPRETS
        val authEdges = graph.edges.filter { it.relationship == "AUTHORIZES_INTERPRETATION" }
        assertTrue(authEdges.isNotEmpty(), "Must have AUTHORIZES_INTERPRETATION edges from ruleset")

        val interpEdges = graph.edges.filter { it.relationship == "INTERPRETS" }
        assertTrue(interpEdges.isNotEmpty(), "Must have INTERPRETS edges from derived facts")
    }

    @Test
    fun testNonFatalisticSafetyPolicyOnAllInterpretations() {
        val prohibitedPhrases = listOf(
            "guaranteed wealth", "you will be rich", "guaranteed success",
            "cure", "disease diagnosis", "medical treatment",
            "guaranteed future", "definitely happen"
        )

        for ((key, interp) in NumerologyInterpretationPackage.ALL_INTERPRETATIONS) {
            val combinedText =
                "${interp.titleKey} ${interp.summaryKey} ${interp.reflectionKey}".lowercase()
            for (prohibited in prohibitedPhrases) {
                assertFalse(
                    combinedText.contains(prohibited),
                    "Interpretation $key must not contain fatalistic or medical claim: '$prohibited'"
                )
            }
        }
    }
}
