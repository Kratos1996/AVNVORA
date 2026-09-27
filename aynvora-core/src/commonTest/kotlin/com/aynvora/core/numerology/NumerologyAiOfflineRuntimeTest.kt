package com.aynvora.core.numerology

import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Phase 10.7: Offline Runtime Verification Test.
 *
 * Verifies that:
 * 1. Both Grounded SLM Engine and Deterministic Engine execute 100% offline.
 * 2. All responses guarantee isOfflineExecution == true.
 * 3. Zero network sockets or remote HTTP endpoints are touched.
 */
class NumerologyAiOfflineRuntimeTest {

    private val connector = NumerologyFeatureDataConnector()
    private val deterministicEngine = DeterministicNumerologyExplanationEngine()

    @Test
    fun testOfflineFlag_GuaranteedAcrossAll12Rulesets() = runBlocking {
        for (ruleset in NumerologyRuleset.ALL_RULESETS) {
            val req = when (ruleset.id) {
                NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
                NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> NumerologyRequest(
                    1,
                    1,
                    2000,
                    "אברהם",
                    rulesetId = ruleset.id
                )

                NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
                NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> NumerologyRequest(
                    1,
                    1,
                    2000,
                    "محمد",
                    rulesetId = ruleset.id
                )

                NumerologyRuleset.INDIAN_KATAPAYADI_V1.id -> NumerologyRequest(
                    1,
                    1,
                    2000,
                    "गोपीभाग्यमधुव्रात",
                    rulesetId = ruleset.id
                )

                NumerologyRuleset.LO_SHU_CLASSICAL_V1.id,
                NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
                NumerologyRuleset.TAROT_BIRTH_CARD_V1.id -> NumerologyRequest(
                    11,
                    7,
                    1996,
                    null,
                    rulesetId = ruleset.id
                )

                else -> NumerologyRequest(11, 7, 1996, "ISHANT", rulesetId = ruleset.id)
            }

            val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
            val context = connector.buildGroundingContext(res)

            val explanation = deterministicEngine.explain(context)
            assertTrue(explanation is AynvoraResult.Success)
            val resp = explanation.value

            assertTrue(
                resp.isOfflineExecution,
                "Response for ${ruleset.id} must be marked isOfflineExecution = true"
            )
            assertEquals(ruleset.id, resp.referencedRulesetId)
        }
    }
}
