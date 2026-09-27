package com.aynvora.core.palmistry

import com.aynvora.core.ai.AiContext
import com.aynvora.core.ai.AiEvidence
import com.aynvora.core.ai.AiGenerationRequest
import com.aynvora.core.ai.AiInferenceEngine
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiOutputValidator
import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.ai.AiPromptTemplate
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult

import com.aynvora.core.localization.AynvoraLocale
import com.aynvora.core.localization.FallbackLocalizationProvider
import com.aynvora.core.localization.LocalizationProvider
import com.aynvora.core.localization.RawLocalizationKey

/**
 * Deterministic fallback explanation engine used when on-device SLM is unavailable or uninstalled.
 */
class DeterministicPalmistryExplanationEngine(
    private val localizationProvider: LocalizationProvider? = null,
) : PalmistryExplanationEngine {

    override suspend fun explain(request: PalmistryExplanationRequest): AynvoraResult<PalmistryExplanationResult> {
        val locale = AynvoraLocale.fromId(request.language)
        val provider = localizationProvider ?: FallbackLocalizationProvider(locale)

        val handKey =
            if (request.hand == HandType.RIGHT) "palmistry.results.hand_right_label" else "palmistry.results.hand_left_label"
        val handStr = provider.get(RawLocalizationKey(handKey))

        val items = request.approvedMeanings.joinToString(" • ") { it.title }
        val summary = provider.get(
            RawLocalizationKey("palmistry.observation.summary_format"),
            mapOf("hand" to handStr, "items" to items),
        )

        val intro = provider.get(RawLocalizationKey("palmistry.observation.intro"))
        val importantNote = provider.get(RawLocalizationKey("palmistry.observation.important_note"))
        val cautionPrefix =
            provider.get(RawLocalizationKey("palmistry.observation.reflection_prefix"))

        val reflection = buildString {
            append("$intro\n\n")
            request.approvedMeanings.forEach { meaning ->
                append("• ${meaning.title}: ${meaning.traditionalInterpretation}\n")
                if (meaning.caution.isNotBlank()) {
                    append("  ↳ $cautionPrefix${meaning.caution}\n")
                }
                append("\n")
            }
            append(importantNote)
        }


        val keyThemes = request.approvedMeanings.map { it.condition }.distinct()

        val provenance = EvidenceProvenance(
            domain = CoreFeatureId.PALMISTRY,
            sourceName = request.sourceReference,
            rulesetOrEdition = "SAMUDRIKA_TRADITION",
            engineVersion = request.analysisVersion,
            timestampEpochMs = 1714000000000L,
            locale = request.language,
            contentVersion = request.contentVersion,
        )

        return AynvoraResult.Success(
            PalmistryExplanationResult(
                readingId = request.readingId,
                language = request.language,
                summary = summary,
                reflection = reflection,
                keyThemes = keyThemes,
                supportingEvidence = request.evidence.map { it.evidenceId },
                provenance = provenance,
                modelMetadata = "aynvora-deterministic-palmistry-v1",
                fallbackUsed = true,
            )
        )
    }
}

/**
 * Grounded on-device SLM implementation of [PalmistryExplanationEngine].
 * Transparently falls back to [DeterministicPalmistryExplanationEngine] when SLM is offline or not installed.
 */
class GroundedSlmPalmistryExplanationEngine(
    private val aiInferenceEngine: AiInferenceEngine,
    private val deterministicEngine: PalmistryExplanationEngine = DeterministicPalmistryExplanationEngine(),
    private val outputValidator: AiOutputValidator = AiOutputValidator(),
) : PalmistryExplanationEngine {

    override suspend fun explain(request: PalmistryExplanationRequest): AynvoraResult<PalmistryExplanationResult> {
        val loadedModel = aiInferenceEngine.getLoadedModel()
        val isSlmReady =
            aiInferenceEngine.getStatus() == AiInferenceStatus.READY && loadedModel != null

        if (!request.allowSlmInference || !isSlmReady) {
            return deterministicEngine.explain(request)
        }

        try {
            val provenance = EvidenceProvenance(
                domain = CoreFeatureId.PALMISTRY,
                sourceName = request.sourceReference,
                rulesetOrEdition = "SAMUDRIKA_AI_GROUNDING",
                engineVersion = request.analysisVersion,
                timestampEpochMs = 1714000000000L,
                locale = request.language,
                contentVersion = request.contentVersion,
            )

            val evidenceItems = mutableListOf<AiEvidence>()
            request.evidence.forEach { ev ->
                evidenceItems.add(
                    AiEvidence(
                        evidenceId = ev.evidenceId,
                        featureId = CoreFeatureId.PALMISTRY,
                        category = EvidenceCategory.FACT,
                        confidence = ev.confidence,
                        summaryText = ev.observation,
                        provenance = provenance,
                        privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                    )
                )
            }
            request.approvedMeanings.forEach { meaning ->
                evidenceItems.add(
                    AiEvidence(
                        evidenceId = "meaning_${meaning.featureType.lowercase()}",
                        featureId = CoreFeatureId.PALMISTRY,
                        category = EvidenceCategory.INTERPRETATION,
                        confidence = 0.95f,
                        summaryText = "${meaning.title}: ${meaning.traditionalInterpretation}",
                        provenance = provenance,
                        privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                    )
                )
            }

            val queryPrompt = if (!request.userQuestion.isNullOrBlank()) {
                request.userQuestion
            } else {
                "Synthesize a balanced, non-fatalistic contemplation based strictly on these detected palm features and traditional meanings."
            }

            val aiContext = AiContext(
                featureId = CoreFeatureId.PALMISTRY,
                evidenceItems = evidenceItems,
                privacyClass = AiPrivacyClass.STRICT_LOCAL_ONLY,
                language = request.language,
                userQuery = queryPrompt,
            )

            val systemPrompt = AiPromptTemplate.buildSystemPrompt(aiContext)
            val userPrompt = AiPromptTemplate.buildUserPrompt(aiContext)

            val generationRequest = AiGenerationRequest(
                requestId = request.readingId,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                temperature = 0.6f,
                maxTokens = 384,
            )

            when (val genResult = aiInferenceEngine.generate(generationRequest)) {
                is AynvoraResult.Success -> {
                    when (val validResult =
                        outputValidator.validateOutput(genResult.value.text, aiContext)) {
                        is AynvoraResult.Success -> {
                            val modelMeta = "${loadedModel.modelId} (${loadedModel.name})"
                            val summary = if (request.language.lowercase().startsWith("hi")) {
                                "हस्तरेखा विश्लेषण • $modelMeta"
                            } else {
                                "Palmistry Reflection • $modelMeta"
                            }

                            return AynvoraResult.Success(
                                PalmistryExplanationResult(
                                    readingId = request.readingId,
                                    language = request.language,
                                    summary = summary,
                                    reflection = validResult.value,
                                    keyThemes = request.approvedMeanings.map { it.condition }
                                        .distinct(),
                                    supportingEvidence = request.evidence.map { it.evidenceId },
                                    provenance = provenance,
                                    modelMetadata = modelMeta,
                                    fallbackUsed = false,
                                )
                            )
                        }

                        is AynvoraResult.Failure -> {
                            // Validation failed (e.g. safety check) -> delegate to deterministic fallback
                            return deterministicEngine.explain(request)
                        }
                    }
                }

                is AynvoraResult.Failure -> {
                    // Inference failed -> delegate to deterministic fallback
                    return deterministicEngine.explain(request)
                }
            }
        } catch (e: Exception) {
            return deterministicEngine.explain(request)
        }
    }
}
