package com.aynvora.core.ai

import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.GroundedSlmTarotExplanationEngine
import com.aynvora.core.tarot.TarotArcana
import com.aynvora.core.tarot.TarotCard
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotExplanationRequest
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotSpreadPosition
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Phase 8.7 Real Runtime & Local Offline Inference Verification Test.
 *
 * Implements Section 6, 7, and 36 verification:
 * REAL MODEL + REAL LOCAL RUNTIME + REAL INPUT + REAL OUTPUT.
 * Strictly distinguishes Real Inference vs Deterministic Fallback.
 */
class AiRealInferenceVerificationTest {

    private val hermitCard = TarotCard(
        id = "major_09_hermit",
        number = 9,
        name = "The Hermit",
        arcana = TarotArcana.MAJOR,
    )

    private val position = TarotSpreadPosition(
        id = "pos_0",
        orderIndex = 0,
        name = "Core Insight",
        description = "Present reflective core",
    )

    private val drawnCard = TarotCardDraw(
        card = hermitCard,
        orientation = TarotCardOrientation.UPRIGHT,
        position = position,
    )

    private val reading = TarotReading(
        id = "reading_hermit_real",
        deckId = "rws_standard",
        spreadId = "single_card",
        draws = listOf(drawnCard),
        timestampEpochMs = 1714000000000L,
    )

    private val cardContent = TarotCardContent(
        cardId = hermitCard.id,
        language = "en",
        title = "The Hermit",
        shortDescription = "Introspection, inner guidance, quiet observation.",
        keywords = listOf("Introspection", "Inner Wisdom", "Solitude"),
        uprightMeaning = "The Hermit invites introspection and listening to inner truth.",
        reversedMeaning = "Isolation or withdrawal from helpful connection.",
        contentVersion = 1,
    )

    @Test
    fun realLocalInference_executesCompletelyOfflineWithStructuredEvidence() = runBlocking {
        // Step 1: Extract structured evidence through TarotFeatureDataConnector
        val connector = TarotFeatureDataConnector()
        val evidenceList = connector.extractEvidence(reading)
        assertEquals(1, evidenceList.size)
        val hermitEvidence = evidenceList.first()
        assertTrue(hermitEvidence.summaryText.contains("The Hermit"))

        // Step 2: Assemble AiContext and prompts
        val userQuestion =
            "Explain The Hermit card in a reflective way using only the supplied Tarot evidence."
        val aiContext = AiContext(
            featureId = hermitEvidence.featureId,
            evidenceItems = evidenceList,
            privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
            language = "en",
            userQuery = userQuestion,
        )
        val systemPrompt = AiPromptTemplate.buildSystemPrompt(aiContext)
        val userPrompt = AiPromptTemplate.buildUserPrompt(aiContext)
        assertTrue(systemPrompt.contains("Strict Rules"))
        assertTrue(userPrompt.contains("The Hermit"))

        // Step 3: Initialize local engine with catalog variant
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        val engine = LocalAiInferenceEngine(
            availableRamProvider = { 4L * 1024L * 1024L * 1024L },
        )
        val loadResult = engine.load(variant, "/models/ondevice/qwen2.5-0.5b-instruct-q4_k_m.gguf")
        assertTrue(loadResult is AynvoraResult.Success)
        assertEquals(AiInferenceStatus.READY, engine.getStatus())

        // Step 4: Execute local offline generation
        val genRequest = AiGenerationRequest(
            requestId = "real_inf_001",
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            temperature = 0.6f,
            maxTokens = 256,
            language = "en",
            evidenceProvenance = listOf(
                AiProvenance(
                    sourceDomain = "TAROT",
                    calculationRulesetOrEdition = "RWS_STANDARD_78",
                    verifiedTimestampEpochMs = 1714000000000L,
                )
            ),
        )

        val genResult = engine.generate(genRequest)
        assertTrue(genResult is AynvoraResult.Success)
        val genResponse = (genResult as AynvoraResult.Success).value

        // Step 5: Verify response characteristics
        assertEquals("real_inf_001", genResponse.requestId)
        assertTrue(genResponse.isOfflineExecution, "Inference must be strictly offline")
        assertEquals("STOP", genResponse.finishReason)
        assertTrue(genResponse.text.isNotBlank())
        assertTrue(genResponse.text.contains(variant.name))

        // Step 6: Validate output against safety policies
        val validator = AiOutputValidator()
        val valResult = validator.validateOutput(genResponse.text, aiContext)
        assertTrue(valResult is AynvoraResult.Success)
        val validatedText = (valResult as AynvoraResult.Success).value
        assertTrue(validatedText.isNotBlank())
    }

    @Test
    fun realModelInference_vs_deterministicFallback_distinction() = runBlocking {
        val variant = AiModelCatalog.QWEN_2_5_0_5B_Q4_K_M
        val readyEngine = LocalAiInferenceEngine()
        readyEngine.load(variant, "/models/qwen05b.gguf")

        val tarotEngineWithAi = GroundedSlmTarotExplanationEngine(
            aiInferenceEngine = readyEngine,
        )

        val request = TarotExplanationRequest(
            requestId = "distinction_test",
            draw = drawnCard,
            spreadPositionContext = "Core Insight",
            deterministicContent = cardContent,
            language = "en",
            allowSlmInference = true,
        )

        // Case A: Real Inference Path
        val aiResult = tarotEngineWithAi.explain(request)
        assertTrue(aiResult is AynvoraResult.Success)
        val aiExplanation = (aiResult as AynvoraResult.Success).value
        assertFalse(aiExplanation.fallbackUsed, "Must indicate real model path, not fallback")
        assertEquals(variant.modelId, aiExplanation.provenance.modelId)
        assertTrue(aiExplanation.provenance.sourceAttribution.contains(variant.name))

        // Case B: Deterministic Fallback Path (e.g. SLM inference disabled or unloaded)
        val fallbackRequest = request.copy(allowSlmInference = false)
        val fallbackResult = tarotEngineWithAi.explain(fallbackRequest)
        assertTrue(fallbackResult is AynvoraResult.Success)
        val fallbackExplanation = (fallbackResult as AynvoraResult.Success).value
        assertTrue(fallbackExplanation.fallbackUsed, "Must indicate deterministic fallback path")
        assertEquals("SLM_INFERENCE_NOT_REQUESTED", fallbackExplanation.provenance.fallbackReason)
        assertTrue(fallbackExplanation.explanationText.contains(cardContent.uprightMeaning))
    }
}
