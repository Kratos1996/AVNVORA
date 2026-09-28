package com.aynvora.core.ai.adapters

import com.aynvora.core.ai.AynvoraAiRequest
import com.aynvora.core.ai.AynvoraAiResponse
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.ai.AynvoraResponseMode
import com.aynvora.core.ai.AynvoraUserContext
import com.aynvora.core.ai.knowledge.AynvoraKnowledgePack
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.result.AynvoraResult

/**
 * Common contract for all domain AI adapters.
 *
 * Invariant: Adapters NEVER instantiate an AI model, NEVER download model files,
 * and NEVER run a separate AI runtime. All inference is delegated to [AynvoraLocalIntelligence].
 */
interface FeatureAiAdapter {
    val featureId: CoreFeatureId
    val localIntelligence: AynvoraLocalIntelligence

    suspend fun synthesizeExplanation(
        question: String,
        userContext: AynvoraUserContext,
        evidence: List<EvidenceItem> = emptyList(),
        evidenceGraph: EvidenceGraph? = null,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        val pack = localIntelligence.getKnowledgePack(featureId)
        val finalEvidence = if (evidence.isNotEmpty()) evidence else {
            pack?.retrieveRelevantEvidence(question, userContext) ?: emptyList()
        }

        val request = AynvoraAiRequest(
            requestId = "${featureId.name.lowercase()}_req_${System.currentTimeMillis()}",
            featureId = featureId,
            knowledgePackId = pack?.knowledgePackId ?: "kp_${featureId.name.lowercase()}_default",
            rulesetId = "${featureId.name}_STANDARD_RULESET",
            evidence = finalEvidence,
            evidenceGraph = evidenceGraph,
            userContext = userContext,
            question = question,
            locale = locale,
            responseMode = AynvoraResponseMode.REFLECTIVE,
        )

        return localIntelligence.synthesize(request)
    }
}

/**
 * Vedic Astrology AI Adapter.
 */
class AstrologyAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.ASTROLOGY

    suspend fun explainChart(
        question: String,
        userContext: AynvoraUserContext,
        chartEvidence: List<EvidenceItem>,
        evidenceGraph: EvidenceGraph? = null,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = chartEvidence,
            evidenceGraph = evidenceGraph,
            locale = locale,
        )
    }
}

/**
 * Bhagavad Gita AI Adapter.
 */
class GitaAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.GITA

    suspend fun reflect(
        question: String,
        userContext: AynvoraUserContext,
        versesEvidence: List<EvidenceItem> = emptyList(),
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = versesEvidence,
            locale = locale,
        )
    }
}

/**
 * Tarot AI Adapter.
 */
class TarotAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.TAROT

    suspend fun explainSpread(
        question: String,
        userContext: AynvoraUserContext,
        cardEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = cardEvidence,
            locale = locale,
        )
    }
}

/**
 * Numerology AI Adapter.
 */
class NumerologyAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.NUMEROLOGY

    suspend fun explainNumberProfile(
        question: String,
        userContext: AynvoraUserContext,
        numberEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = numberEvidence,
            locale = locale,
        )
    }
}

/**
 * Palmistry AI Adapter.
 */
class PalmistryAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.PALMISTRY

    suspend fun explainLineObservations(
        question: String,
        userContext: AynvoraUserContext,
        lineEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = lineEvidence,
            locale = locale,
        )
    }
}

/**
 * Gemstone AI Adapter.
 */
class GemstoneAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.GEMSTONE

    suspend fun explainCompatibility(
        question: String,
        userContext: AynvoraUserContext,
        gemstoneEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = gemstoneEvidence,
            locale = locale,
        )
    }
}

/**
 * Garuda Puran AI Adapter.
 */
class GarudaPuranAiAdapter(
    override val localIntelligence: AynvoraLocalIntelligence,
) : FeatureAiAdapter {
    override val featureId: CoreFeatureId = CoreFeatureId.GARUDA_PURAN

    suspend fun explainDharmaPassage(
        question: String,
        userContext: AynvoraUserContext,
        passageEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        return synthesizeExplanation(
            question = question,
            userContext = userContext,
            evidence = passageEvidence,
            locale = locale,
        )
    }
}

/**
 * Controlled Cross-Feature Reflection Adapter (Step 14).
 *
 * Clearly separates distinct traditions (e.g. Astrology + Gita) and formats:
 * [ASTROLOGY EVIDENCE]
 * [GITA SOURCE]
 * [REFLECTIVE SYNTHESIS]
 *
 * Invariant: Never merges separate traditions into one singular authoritative fact.
 */
class CrossFeatureReflectionAdapter(
    val localIntelligence: AynvoraLocalIntelligence,
) {
    suspend fun reflect(
        primaryFeature: CoreFeatureId,
        secondaryFeature: CoreFeatureId,
        question: String,
        userContext: AynvoraUserContext,
        primaryEvidence: List<EvidenceItem>,
        secondaryEvidence: List<EvidenceItem>,
        locale: String = "en",
    ): AynvoraResult<AynvoraAiResponse> {
        val primaryPack = localIntelligence.getKnowledgePack(primaryFeature)
        val secondaryPack = localIntelligence.getKnowledgePack(secondaryFeature)

        val combinedEvidence = primaryEvidence + secondaryEvidence

        val request = AynvoraAiRequest(
            requestId = "cross_${primaryFeature.name}_${secondaryFeature.name}_${System.currentTimeMillis()}",
            featureId = primaryFeature,
            knowledgePackId = "${primaryPack?.knowledgePackId ?: "primary"}_and_${secondaryPack?.knowledgePackId ?: "secondary"}",
            rulesetId = "CROSS_FEATURE_REFLECTION",
            evidence = combinedEvidence,
            userContext = userContext,
            question = question,
            locale = locale,
            responseMode = AynvoraResponseMode.CROSS_FEATURE_REFLECTION,
            contributingDomains = setOf(primaryFeature, secondaryFeature),
        )

        return localIntelligence.synthesize(request)
    }
}
