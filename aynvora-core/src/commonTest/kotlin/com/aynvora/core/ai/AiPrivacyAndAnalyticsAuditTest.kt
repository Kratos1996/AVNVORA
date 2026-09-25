package com.aynvora.core.ai

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 8.7 Privacy & Analytics Hygiene Audit Tests.
 *
 * Verifies strict local execution boundaries, zero external network leakage,
 * and zero PII/prompt/response parameters in analytics telemetry.
 */
class AiPrivacyAndAnalyticsAuditTest {

    @Test
    fun inferenceEngineExecution_runsStrictlyLocallyWithoutNetwork() = runBlocking {
        val engine = LocalAiInferenceEngine()
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        engine.load(variant, "/models/qwen05b.gguf")

        val request = AiGenerationRequest(
            requestId = "audit_req_1",
            systemPrompt = "System prompt",
            userPrompt = "Contemplative question",
            evidenceProvenance = listOf(
                AiProvenance(
                    sourceDomain = "TAROT",
                    calculationRulesetOrEdition = "RWS_STANDARD_78",
                    verifiedTimestampEpochMs = 1714000000000L,
                )
            ),
        )

        val result = engine.generate(request)
        assertTrue(result is AynvoraResult.Success)
        val response = (result as AynvoraResult.Success).value

        // Invariant: isOfflineExecution is true, executionMode is local simulation / on-device
        assertTrue(response.isOfflineExecution)
        assertEquals(AiExecutionMode.LOCAL_SIMULATION, response.executionMode)
    }

    @Test
    fun analyticsHygiene_noPromptOrResponseOrPrivateDataAllowed() {
        val forbiddenKeys = listOf(
            "prompt", "response", "user_query", "question", "text", "shloka",
            "birth_data", "latitude", "longitude", "palm_image", "private_evidence"
        )

        val events: List<AnalyticsEvent> = listOf(
            AnalyticsEvent.AiDownloadStarted("qwen2.5-0.5b-instruct-q4_k_m"),
            AnalyticsEvent.AiDownloadCompleted("qwen2.5-0.5b-instruct-q4_k_m", 4500L),
            AnalyticsEvent.AiModelLoaded("qwen2.5-0.5b-instruct-q4_k_m", 120L),
            AnalyticsEvent.AiModelLoadFailed(
                "qwen2.5-0.5b-instruct-q4_k_m",
                "INSUFFICIENT_RUNTIME_MEMORY"
            ),
            AnalyticsEvent.AiInferenceStarted("qwen2.5-0.5b-instruct-q4_k_m", "en"),
            AnalyticsEvent.AiInferenceCompleted("qwen2.5-0.5b-instruct-q4_k_m", "UNDER_1S", 42),
            AnalyticsEvent.AiInferenceFailed("qwen2.5-0.5b-instruct-q4_k_m", "INFERENCE_TIMEOUT"),
            AnalyticsEvent.AiFallbackUsed("qwen2.5-0.5b-instruct-q4_k_m", "SLM_NOT_READY"),
        )

        for (event in events) {
            for (forbidden in forbiddenKeys) {
                assertFalse(
                    event.params.containsKey(forbidden),
                    "Event '${event.name}' violated privacy by including forbidden parameter '$forbidden'"
                )
            }
        }
    }

    @Test
    fun evidenceModel_enforcesStrictLocalOnlyPrivacyClass() {
        val evidence = AiEvidence(
            evidenceId = "ev_1",
            featureId = CoreFeatureId.TAROT,
            category = EvidenceCategory.FACT,
            summaryText = "The Hermit [Upright]",
            provenance = EvidenceProvenance(
                domain = CoreFeatureId.TAROT,
                sourceName = "AYNVORA Tarot",
                rulesetOrEdition = "RWS_STANDARD_78",
                engineVersion = "1.0.0",
                timestampEpochMs = 1714000000000L,
                locale = "en",
            ),
        )

        assertEquals(AiPrivacyClass.STRICT_LOCAL_ONLY, evidence.privacyClass)
    }
}
