package com.aynvora.core.astrology.knowledge

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import kotlinx.serialization.Serializable

@Serializable
enum class AstroToolStatus { AVAILABLE, PARTIAL, RESEARCH_ONLY, UNAVAILABLE }
@Serializable
data class AstroToolDescriptor(val toolId: String, val description: String, val inputSchema: String, val outputSchema: String, val deterministic: Boolean, val source: String, val status: AstroToolStatus, val allowedUse: String)

/** Describes tools to a host AI. Execution stays in the deterministic SDK/host adapter. */
object AstroToolRegistry {
    private val core = listOf("getKundali", "getChart", "getPlanetaryPositions", "getLagna", "getHouses", "getVarga", "getDasha", "getTransit", "getPanchang", "getAshtakavarga", "getShadbala", "getEvents", "searchKnowledge", "getSource", "getPageContext")
    fun all(remoteSearchConfigured: Boolean = false): List<AstroToolDescriptor> = (core.map { id -> AstroToolDescriptor(id, "Retrieve structured $id evidence from the SDK.", "{\"type\":\"object\"}", "{\"type\":\"object\"}", id !in setOf("searchKnowledge", "getSource", "getPageContext"), "AYNVORA SDK", AstroToolStatus.AVAILABLE, "Retrieve evidence; preserve deterministic values and provenance.") } + listOf(
        descriptor("getKP", AstroToolStatus.UNAVAILABLE, "No verified KP production engine or knowledge pack."),
        descriptor("getLalKitab", AstroToolStatus.UNAVAILABLE, "No verified Lal Kitab production pack."),
        descriptor("getVarshaphal", AstroToolStatus.PARTIAL, "The SDK calculates a solar return, annual chart, and primary-source Muntha sign/lord when natal ascendant longitude is available; Varsheshwara, Sahams, Tajika aspects, and Mudda Dasha remain unverified or unsupported."),
        descriptor("getMuddaDasha", AstroToolStatus.UNAVAILABLE, "No verified Mudda Dasha implementation."),
        descriptor("getMuntha", AstroToolStatus.AVAILABLE, "Evaluate selected sourced Muntha house rules from explicit caller-calculated facts."),
        descriptor("getMunthaLord", AstroToolStatus.AVAILABLE, "Calculate Muntha sign and sign lord from natal sidereal ascendant longitude and elapsed solar-return cycles."),
        descriptor("getVarsheshwara", AstroToolStatus.UNAVAILABLE, "The complete source-verified year-lord selection method is not implemented."),
        descriptor("getSahams", AstroToolStatus.UNAVAILABLE, "No verified Sahams implementation."),
        descriptor("getTajikaAspects", AstroToolStatus.UNAVAILABLE, "No verified Tajika aspect implementation."),
        descriptor("getPhaladesh", AstroToolStatus.UNAVAILABLE, "No verified Phaladesh production engine or pack."),
        descriptor("getKPResearch", AstroToolStatus.RESEARCH_ONLY, "Research material only; this is not a KP calculation."),
        descriptor("getLalKitabResearch", AstroToolStatus.RESEARCH_ONLY, "Research material only; this is not a Lal Kitab calculation."),
        descriptor("searchWeb", if (remoteSearchConfigured) AstroToolStatus.AVAILABLE else AstroToolStatus.UNAVAILABLE, "Requires an explicitly configured legal search provider."),
    )).sortedBy { it.toolId }
    private fun descriptor(id: String, status: AstroToolStatus, description: String) = AstroToolDescriptor(id, description, "{\"type\":\"object\"}", "{\"type\":\"object\"}", true, "AYNVORA SDK", status, "Do not calculate or assert unsupported astrology features.")
}

@Serializable
data class AstroPageContext(val pageId: String, val featureId: String, val kundaliId: String? = null, val visibleData: Map<String, String> = emptyMap(), val currentChart: Map<String, String> = emptyMap(), val traditionId: String? = null, val currentPeriod: Map<String, String> = emptyMap(), val evidence: List<AstroEvidenceItem> = emptyList(), val sourceReferences: List<String> = emptyList(), val searchableTerms: List<String> = emptyList())
@Serializable
enum class AstroAnswerMode { SHORT, NORMAL, DETAILED, DEEP }
@Serializable
data class GroundedAstroAsk(val question: String, val pageContext: AstroPageContext, val mode: AstroAnswerMode, val evidence: AstroEvidenceBundle, val groundingStatus: String = "GROUNDED_RAG")

object KnowledgePackValidator {
    fun validate(pack: com.aynvora.core.astrology.prediction.KnowledgePack, chunks: List<com.aynvora.core.astrology.prediction.KnowledgeChunk>): List<String> {
        val errors = mutableListOf<String>()
        if (pack.packId.isBlank() || pack.version.isBlank()) errors += "Pack ID and version are required."
        if (pack.rules.map { it.ruleId }.distinct().size != pack.rules.size) errors += "Rule IDs must be unique."
        if (chunks.map { it.chunkId }.distinct().size != chunks.size) errors += "Chunk IDs must be unique."
        val sources = pack.sources.associateBy { it.sourceId }
        pack.rules.forEach { rule ->
            if (rule.conditions.isEmpty()) errors += "Rule ${rule.ruleId} needs structured conditions."
            if (rule.sourceId !in sources) errors += "Rule ${rule.ruleId} references an unknown source."
            if (rule.rightsStatus != "VERIFIED") errors += "Rule ${rule.ruleId} rights are not verified."
        }
        chunks.forEach { chunk ->
            val source = chunk.sourceId?.let(sources::get)
            if (source == null) errors += "Chunk ${chunk.chunkId} has no pack source."
            if (chunk.text.isBlank()) errors += "Chunk ${chunk.chunkId} has no text."
            if (chunk.checksum.isBlank()) errors += "Chunk ${chunk.chunkId} has no checksum."
            if (chunk.text.isNotBlank() && chunk.checksum.isNotBlank() &&
                !GarudaChecksumVerifier.calculateSha256(chunk.text.encodeToByteArray()).equals(chunk.checksum, ignoreCase = true)
            ) errors += "Chunk ${chunk.chunkId} checksum does not match its text."
            if (chunk.traditionId != pack.tradition) errors += "Chunk ${chunk.chunkId} tradition does not match pack tradition."
            if (chunk.licenseStatus != "VERIFIED" || source?.rightsStatus != "VERIFIED") errors += "Chunk ${chunk.chunkId} does not have verified rights."
        }
        return errors.distinct()
    }
}
