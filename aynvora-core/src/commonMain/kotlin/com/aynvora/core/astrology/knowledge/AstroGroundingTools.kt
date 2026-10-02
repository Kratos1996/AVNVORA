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
        descriptor("getKP", AstroToolStatus.AVAILABLE, "Calculate KP cusps, star lords, sub lords, 249 subdivision table, ruling planets, and significators."),
        descriptor("getJaimini", AstroToolStatus.AVAILABLE, "Calculate Jaimini Chara Karakas (AK to DK), Arudhas (AL, UL), Karakamsha, and Rashi aspects."),
        descriptor("getMuhurta", AstroToolStatus.AVAILABLE, "Calculate Hora, Choghadiya, Rahu Kalam, Yamaganda, Gulika Kalam, and Abhijit Muhurta."),
        descriptor("getCompatibility", AstroToolStatus.AVAILABLE, "Calculate Ashtakoota 36 Guna Milan and South Indian Porutham compatibility."),
        descriptor("getYoga", AstroToolStatus.AVAILABLE, "Detect classical planetary Yogas (Gajakesari, Budhaditya, Raj Yoga)."),
        descriptor("getDosha", AstroToolStatus.AVAILABLE, "Detect classical Doshas (Manglik, Kala Sarpa, Pitru, Kemadruma)."),
        descriptor("getUpagraha", AstroToolStatus.AVAILABLE, "Calculate secondary planets and mathematical shadows (Dhuma, Vyatipata, Parivesha, Gulika, Mandi)."),
        descriptor("getPrashna", AstroToolStatus.AVAILABLE, "Evaluate horary queries, house significations, and KP 1-249 seeds."),
        descriptor("getLalKitab", AstroToolStatus.RESEARCH_ONLY, "Research-only: Classical Pandit Roop Chand Joshi editions (1939-1952)."),
        descriptor("getVastu", AstroToolStatus.RESEARCH_ONLY, "Research-only: Classical Brihat Samhita and Mayamatam directional geometry."),
        descriptor("getVarshaphal", AstroToolStatus.AVAILABLE, "Calculate full classical Varshaphal: solar return, annual chart, Muntha, Varsheshwara, Sahams, Tajika aspects, and Mudda Dasha."),
        descriptor("getMuddaDasha", AstroToolStatus.AVAILABLE, "Calculate Mudda Dasha annual planetary sequence and durations scaled to the solar return interval."),
        descriptor("getMuntha", AstroToolStatus.AVAILABLE, "Calculate Muntha sign, annual house, sign lord, and sourced house indications from natal ascendant longitude, elapsed cycles, and annual ascendant sign."),
        descriptor("getMunthaLord", AstroToolStatus.AVAILABLE, "Calculate Muntha sign and sign lord from natal sidereal ascendant longitude and elapsed solar-return cycles."),
        descriptor("getVarsheshwara", AstroToolStatus.AVAILABLE, "Calculate Varsheshwara (Year Lord), candidate five office-bearers, eligibility by Lagna aspect, and Panchavargiya strength."),
        descriptor("getSahams", AstroToolStatus.AVAILABLE, "Calculate verified classical Sahams (Punya, Vidya, Yasas, Karma) with Shodhya-Shuddhyashraya arc correction."),
        descriptor("getTajikaAspects", AstroToolStatus.AVAILABLE, "Calculate Tajika aspects, planetary orbs (deeptamsha), applying/separating, Itthashala, and Ishrafa."),
        descriptor("getPhaladesh", AstroToolStatus.PARTIAL, "Phaladesh event detection and planetary ingress."),
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
