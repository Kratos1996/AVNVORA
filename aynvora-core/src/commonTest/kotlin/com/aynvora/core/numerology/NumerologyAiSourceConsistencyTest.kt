package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.7: Source Authority Audit & Consistency Test.
 *
 * Verifies that for every supported Numerology ruleset:
 * AI source metadata == Interpretation source metadata == Ruleset source metadata == Calculation source metadata.
 */
class NumerologyAiSourceConsistencyTest {

    private val connector = NumerologyFeatureDataConnector()

    @Test
    fun testAllRulesets_SourceAuthorityAlignment() {
        val allRulesets = NumerologyRuleset.ALL_RULESETS
        assertEquals(12, allRulesets.size, "Must have exactly 12 registered rulesets")

        for (ruleset in allRulesets) {
            // 1. Verify Ruleset source authority
            assertTrue(
                ruleset.primarySourceReference.isNotBlank(),
                "Ruleset ${ruleset.id} must have non-blank source reference"
            )
            assertTrue(
                ruleset.authorityDescription.isNotBlank(),
                "Ruleset ${ruleset.id} must have non-blank authority description"
            )

            // 2. Perform sample calculation to obtain result
            val request = when (ruleset.id) {
                NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
                NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "אברהם", rulesetId = ruleset.id,
                )

                NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
                NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "محمد", rulesetId = ruleset.id,
                )

                NumerologyRuleset.INDIAN_KATAPAYADI_V1.id -> NumerologyRequest(
                    birthDay = 1, birthMonth = 1, birthYear = 2000,
                    fullName = "गोपीभाग्यमधुव्रात", rulesetId = ruleset.id,
                )

                NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
                NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
                NumerologyRuleset.TAROT_BIRTH_CARD_V1.id -> NumerologyRequest(
                    birthDay = 11, birthMonth = 7, birthYear = 1996,
                    fullName = null, rulesetId = ruleset.id,
                )

                else -> NumerologyRequest(
                    birthDay = 11, birthMonth = 7, birthYear = 1996,
                    fullName = "ISHANT", rulesetId = ruleset.id,
                )
            }

            val calcRes = NumerologyCalculationEngine.calculate(request)
            assertTrue(
                calcRes is AynvoraResult.Success,
                "Calculation must succeed for ruleset ${ruleset.id}"
            )
            val result = calcRes.value

            // 3. Verify interpretation bundle source alignment
            val bundle = NumerologyInterpretationPackage.resolveInterpretations(result)
            assertEquals(
                ruleset.id,
                bundle.rulesetId,
                "Interpretation bundle must match calculation rulesetId"
            )
            assertTrue(
                bundle.provenanceSources.isNotEmpty(),
                "Interpretation bundle must provide provenance sources"
            )

            // 4. Verify AI Grounding Context alignment
            val context = connector.buildGroundingContext(result)
            assertEquals(
                ruleset.id,
                context.rulesetId,
                "AI context rulesetId must match canonical ruleset"
            )
            assertEquals(
                ruleset.name,
                context.rulesetName,
                "AI context rulesetName must match canonical ruleset"
            )
            assertEquals(
                ruleset.primarySourceReference,
                context.primarySourceReference,
                "AI context primary source must match ruleset"
            )

            // 5. Evidence items provenance check
            val rulesetEvidence =
                context.evidenceItems.firstOrNull { it.evidenceId.startsWith("ruleset_") }
            assertNotNull(rulesetEvidence, "Must contain ruleset authority evidence")
            assertEquals(ruleset.id, rulesetEvidence.provenance.rulesetOrEdition)
            assertEquals(ruleset.primarySourceReference, rulesetEvidence.provenance.sourceName)
        }
    }

    @Test
    fun testTraditionRegistry_LinksExactCanonicalRulesets() {
        val implemented = NumerologyTraditionRegistry.getImplementedTraditions()
        assertEquals(12, implemented.size, "Must have exactly 12 implemented traditions")

        for (tradition in implemented) {
            val rulesetId = tradition.rulesetId
            assertNotNull(
                rulesetId,
                "Implemented tradition '${tradition.traditionId}' must have non-null rulesetId"
            )

            val ruleset = NumerologyRuleset.fromId(rulesetId)
            assertNotNull(
                ruleset,
                "RulesetId '$rulesetId' must exist in canonical NumerologyRuleset registry"
            )
            assertEquals(rulesetId, ruleset.id)

            // Primary source in Tradition registry must match primary source in Ruleset
            val traditionSources = tradition.primarySources.joinToString(" ")
            val keywords = traditionSources.split(" ", ",", "'", "\"", "(", ")", ";", ":")
                .map { it.trim() }
                .filter { it.length >= 5 }

            assertTrue(
                keywords.any { ruleset.primarySourceReference.contains(it, ignoreCase = true) },
                "Sources must align between TraditionRegistry and Ruleset for ${tradition.traditionId}: tradition=$traditionSources, ruleset=${ruleset.primarySourceReference}",
            )
        }
    }
}
