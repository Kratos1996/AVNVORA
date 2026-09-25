package com.aynvora.core.tarot

import com.aynvora.core.ai.AiContext
import com.aynvora.core.ai.AiEvidence
import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiOutputValidator
import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.ai.AiPromptTemplate
import com.aynvora.core.ai.AiProvenance
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult

/**
 * Real On-Device SLM implementation of [TarotExplanationEngine] backed by [AiInferenceEngine].
 *
 * Invariant: If the SLM is unavailable, uninstalled, slow, or fails validation,
 * it immediately and transparently delegates to [deterministicEngine] with zero user disruption.
 */
class GroundedSlmTarotExplanationEngine(
    private val aiInferenceEngine: AiInferenceEngine,
    private val deterministicEngine: TarotExplanationEngine = DeterministicTarotExplanationEngine(),
    private val outputValidator: AiOutputValidator = AiOutputValidator(),
) : TarotExplanationEngine {

    override suspend fun explain(request: TarotExplanationRequest): AynvoraResult<TarotExplanationResult> {
        val loadedModel = aiInferenceEngine.getLoadedModel()
        val isSlmReady =
            aiInferenceEngine.getStatus() == AiInferenceStatus.READY && loadedModel != null

        if (!request.allowSlmInference || !isSlmReady) {
            val fallbackReason =
                if (!request.allowSlmInference) "SLM_INFERENCE_NOT_REQUESTED" else "SLM_NOT_READY"
            return executeFallback(request, fallbackReason)
        }

        try {
            val draw = request.draw
            val orientationStr =
                if (draw.orientation == TarotCardOrientation.UPRIGHT) "Upright" else "Reversed"
            val meaning = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                request.deterministicContent.uprightMeaning
            } else {
                request.deterministicContent.reversedMeaning
            }

            val evidenceProvenance = EvidenceProvenance(
                domain = CoreFeatureId.TAROT,
                sourceName = "AYNVORA Tarot Knowledge Package",
                rulesetOrEdition = "RWS_STANDARD_78",
                engineVersion = "1.0.0",
                timestampEpochMs = 1714000000000L,
                locale = request.language,
            )

            val evidenceItems = listOf(
                AiEvidence(
                    evidenceId = "card_fact_${draw.card.id}",
                    featureId = CoreFeatureId.TAROT,
                    category = EvidenceCategory.FACT,
                    confidence = 1.0f,
                    summaryText = "Drawn Card: ${draw.card.name} [$orientationStr]. Spread Position: ${request.spreadPositionContext}. Arcana: ${draw.card.arcana.name}.",
                    provenance = evidenceProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                ),
                AiEvidence(
                    evidenceId = "approved_meaning_${draw.card.id}",
                    featureId = CoreFeatureId.TAROT,
                    category = EvidenceCategory.INTERPRETATION,
                    confidence = 0.95f,
                    summaryText = "Traditional Symbolic Core: $meaning. Keywords: ${
                        request.deterministicContent.keywords.joinToString(
                            ", "
                        )
                    }.",
                    provenance = evidenceProvenance,
                    privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                ),
            )

            val aiContext = AiContext(
                featureId = CoreFeatureId.TAROT,
                evidenceItems = evidenceItems,
                privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                language = request.language,
                userQuery = "Offer a contemplation for this card in the position: ${request.spreadPositionContext}.",
            )

            val systemPrompt = AiPromptTemplate.buildSystemPrompt(aiContext)
            val userPrompt = AiPromptTemplate.buildUserPrompt(aiContext)

            val generationRequest = AiGenerationRequest(
                requestId = request.requestId,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                temperature = 0.6f,
                maxTokens = 384,
                language = request.language,
                evidenceProvenance = listOf(
                    AiProvenance(
                        sourceDomain = "TAROT",
                        calculationRulesetOrEdition = "RWS_STANDARD_78",
                        verifiedTimestampEpochMs = 1714000000000L,
                        isRetrievedFact = true,
                    )
                ),
            )

            val genResult = aiInferenceEngine.generate(generationRequest)
            if (genResult is AynvoraResult.Success) {
                val validationResult =
                    outputValidator.validateOutput(genResult.value.text, aiContext)
                if (validationResult is AynvoraResult.Success) {
                    return AynvoraResult.Success(
                        TarotExplanationResult(
                            requestId = request.requestId,
                            cardId = draw.card.id,
                            language = request.language,
                            explanationText = validationResult.value,
                            fallbackUsed = false,
                            provenance = TarotExplanationProvenance(
                                producedBy = "GroundedSlmTarotExplanationEngine",
                                modelId = loadedModel.modelId,
                                contentVersion = request.deterministicContent.contentVersion,
                                fallbackUsed = false,
                                fallbackReason = null,
                                sourceAttribution = "AYNVORA On-Device AI (${loadedModel.name}) + RWS Approved Wisdom",
                            ),
                        )
                    )
                } else {
                    return executeFallback(request, "SLM_OUTPUT_FAILED_VALIDATION")
                }
            } else {
                return executeFallback(request, "SLM_INFERENCE_EXECUTION_FAILED")
            }
        } catch (t: Throwable) {
            return executeFallback(request, "SLM_EXCEPTION: ${t.message}")
        }
    }

    private suspend fun executeFallback(
        request: TarotExplanationRequest,
        reason: String,
    ): AynvoraResult<TarotExplanationResult> {
        val fallbackResult = deterministicEngine.explain(request)
        return when (fallbackResult) {
            is AynvoraResult.Success -> {
                val res = fallbackResult.value
                AynvoraResult.Success(
                    res.copy(
                        provenance = res.provenance.copy(
                            fallbackUsed = true,
                            fallbackReason = reason,
                        )
                    )
                )
            }

            is AynvoraResult.Failure -> fallbackResult
        }
    }
}
