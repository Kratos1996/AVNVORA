package com.aynvora.core.palmistry

import com.aynvora.core.intelligence.EvidenceProvenance
import kotlinx.serialization.Serializable

/**
 * Request payload for Palmistry AI explanation generation.
 */
@Serializable
data class PalmistryExplanationRequest(
    val readingId: String,
    val hand: HandType,
    val language: String = "en",
    val evidence: List<PalmistryEvidence>,
    val approvedMeanings: List<PalmistryMeaning>,
    val sourceReference: String = PalmistryContentPackage.SOURCE_SAMUDRIKA,
    val analysisVersion: String = PalmistryAnalysisCapabilities.ANALYSIS_VERSION,
    val contentVersion: String = PalmistryContentPackage.CONTENT_VERSION,
    val userQuestion: String? = null,
    val allowSlmInference: Boolean = true,
)

/**
 * Validated AI or deterministic explanation output.
 */
@Serializable
data class PalmistryExplanationResult(
    val readingId: String,
    val language: String,
    val summary: String,
    val reflection: String,
    val keyThemes: List<String>,
    val supportingEvidence: List<String>,
    val provenance: EvidenceProvenance,
    val modelMetadata: String,
    val fallbackUsed: Boolean,
)

/**
 * Contract for generating grounded Palmistry explanations.
 */
interface PalmistryExplanationEngine {
    suspend fun explain(request: PalmistryExplanationRequest): com.aynvora.core.result.AynvoraResult<PalmistryExplanationResult>
}
