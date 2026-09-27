package com.aynvora.core.numerology

import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.result.AynvoraResult

/**
 * Real On-Device SLM implementation of [NumerologyExplanationEngine] backed by the shared [AiInferenceEngine].
 *
 * Implements strict non-authoritative grounding:
 * - AI is subordinate to the deterministic calculation engine and historical interpretation catalog.
 * - Any model failure, timeout, cancellation, or safety violation triggers instant delegation
 *   to [deterministicEngine] with zero user disruption.
 */
class GroundedSlmNumerologyExplanationEngine(
    private val aiInferenceEngine: AiInferenceEngine,
    private val deterministicEngine: NumerologyExplanationEngine = DeterministicNumerologyExplanationEngine(),
    private val outputValidator: NumerologyAiOutputValidator = NumerologyAiOutputValidator(),
) : NumerologyExplanationEngine {

    override suspend fun explain(context: NumerologyAiGroundingContext): AynvoraResult<NumerologyAiResponse> {
        val loadedModel = aiInferenceEngine.getLoadedModel()
        val isSlmReady =
            aiInferenceEngine.getStatus() == AiInferenceStatus.READY && loadedModel != null

        // 1. Availability check: if SLM is not ready or uninstalled, delegate to deterministic fallback
        if (!isSlmReady) {
            val fallbackReason =
                if (loadedModel == null) "MODEL_NOT_INSTALLED" else "ENGINE_NOT_READY"
            val fallbackResult = deterministicEngine.explain(context)
            return if (fallbackResult is AynvoraResult.Success) {
                AynvoraResult.Success(fallbackResult.value.copy(fallbackReason = fallbackReason))
            } else fallbackResult
        }

        // 2. Language support check: if loaded model does not support requested locale, use deterministic fallback
        val reqLang = context.requestedLocale.lowercase().trim()
        val isLangSupported = loadedModel.supportedLanguages.any { reqLang.startsWith(it) }
        if (!isLangSupported) {
            val fallbackResult = deterministicEngine.explain(context)
            return if (fallbackResult is AynvoraResult.Success) {
                AynvoraResult.Success(
                    fallbackResult.value.copy(
                        fallbackReason = "LOCALE_NOT_SUPPORTED_BY_MODEL_${loadedModel.modelId}",
                        locale = context.requestedLocale,
                    )
                )
            } else fallbackResult
        }

        try {
            val systemPrompt = NumerologyAiPromptTemplate.buildSystemPrompt(context)
            val userPrompt = NumerologyAiPromptTemplate.buildUserPrompt(context)

            val generationRequest = AiGenerationRequest(
                requestId = "num_ai_${context.rulesetId}_${context.questionCategory}_${context.requestedLocale}",
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                temperature = 0.5f,
                maxTokens = 400,
                language = context.requestedLocale,
                evidenceProvenance = emptyList(),
            )

            when (val genResult = aiInferenceEngine.generate(generationRequest)) {
                is AynvoraResult.Success -> {
                    val rawText = genResult.value.text
                    when (val validResult = outputValidator.validateOutput(rawText, context)) {
                        is AynvoraResult.Success -> {
                            val modelMeta = "${loadedModel.modelId} (${loadedModel.name})"
                            val referencedEvidenceIds = context.evidenceItems.map { it.evidenceId }
                            val citedSources =
                                context.sourceReferences.ifEmpty { listOf(context.primarySourceReference) }

                            return AynvoraResult.Success(
                                NumerologyAiResponse(
                                    answer = validResult.value,
                                    questionCategory = context.questionCategory,
                                    referencedRulesetId = context.rulesetId,
                                    referencedEvidenceIds = referencedEvidenceIds,
                                    citedSources = citedSources,
                                    locale = context.requestedLocale,
                                    safetyStatus = NumerologyAiSafetyStatus.VALIDATED,
                                    isFallback = false,
                                    fallbackReason = null,
                                    modelMetadata = modelMeta,
                                    isOfflineExecution = true,
                                )
                            )
                        }

                        is AynvoraResult.Failure -> {
                            // Validation failed -> fall back to deterministic engine
                            val fallbackResult = deterministicEngine.explain(context)
                            return if (fallbackResult is AynvoraResult.Success) {
                                AynvoraResult.Success(
                                    fallbackResult.value.copy(
                                        safetyStatus = NumerologyAiSafetyStatus.FALLBACK_APPLIED,
                                        fallbackReason = "VALIDATION_FAILED: ${validResult.message}",
                                        modelMetadata = "${loadedModel.modelId} (Rejected by Validator)",
                                    )
                                )
                            } else fallbackResult
                        }
                    }
                }

                is AynvoraResult.Failure -> {
                    // Inference execution failed or timed out -> fall back to deterministic engine
                    val fallbackResult = deterministicEngine.explain(context)
                    return if (fallbackResult is AynvoraResult.Success) {
                        AynvoraResult.Success(
                            fallbackResult.value.copy(
                                fallbackReason = "INFERENCE_FAILED: ${genResult.message}",
                            )
                        )
                    } else fallbackResult
                }
            }
        } catch (e: Exception) {
            val fallbackResult = deterministicEngine.explain(context)
            return if (fallbackResult is AynvoraResult.Success) {
                AynvoraResult.Success(
                    fallbackResult.value.copy(
                        fallbackReason = "EXCEPTION: ${e.message}",
                    )
                )
            } else fallbackResult
        }
    }
}
