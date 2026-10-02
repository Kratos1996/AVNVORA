package com.aynvora.core.astrology.knowledge

import com.aynvora.core.astrology.prediction.KnowledgeChunk
import com.aynvora.core.astrology.prediction.KnowledgeSearchEngine
import com.aynvora.core.astrology.prediction.KnowledgeSearchFilters
import com.aynvora.core.astrology.prediction.KnowledgeRule
import kotlinx.serialization.Serializable

@Serializable
enum class AstroEvidenceKind { DETERMINISTIC_CALCULATION, LOCAL_KNOWLEDGE, KNOWLEDGE_RULE, REMOTE_WEB }
@Serializable
data class AstroEvidenceItem(val evidenceId: String, val kind: AstroEvidenceKind, val text: String, val sourceId: String? = null, val sourceRef: String? = null, val checksum: String? = null, val traditionId: String? = null, val featureId: String? = null, val metadata: Map<String, String> = emptyMap())
@Serializable
data class AstroEvidenceBundle(val query: String, val items: List<AstroEvidenceItem>, val packVersions: Map<String, String> = emptyMap(), val remoteWebUsed: Boolean = false)

fun interface KnowledgeSemanticEmbedder { suspend fun embed(text: String, modelId: String, version: String): List<Float> }

/** Small host-local vector index. Embeddings remain optional and are namespaced by model/version. */
class AstroLocalVectorIndex(private val capacity: Int = 10_000) {
    private data class Entry(val chunkId: String, val modelId: String, val version: String, val vector: List<Float>)
    private val entries = linkedMapOf<String, Entry>()
    init { require(capacity > 0) }
    @Synchronized fun put(chunkId: String, modelId: String, version: String, vector: List<Float>) {
        require(chunkId.isNotBlank() && modelId.isNotBlank() && version.isNotBlank() && vector.isNotEmpty() && vector.all(Float::isFinite))
        val key = "$modelId:$version:$chunkId"; entries.remove(key); entries[key] = Entry(chunkId, modelId, version, vector.toList()); while (entries.size > capacity) entries.remove(entries.keys.first())
    }
    @Synchronized fun search(vector: List<Float>, modelId: String, version: String, limit: Int): List<Pair<String, Double>> {
        require(limit >= 0)
        if (vector.isEmpty() || vector.any { !it.isFinite() }) return emptyList()
        return entries.values.filter { it.modelId == modelId && it.version == version && it.vector.size == vector.size }
            .map { it.chunkId to cosine(vector, it.vector) }.sortedWith(compareByDescending<Pair<String, Double>> { it.second }.thenBy { it.first }).take(limit)
    }
    private fun cosine(a: List<Float>, b: List<Float>): Double { val dot = a.indices.sumOf { a[it].toDouble() * b[it] }; val na = kotlin.math.sqrt(a.sumOf { it.toDouble() * it }); val nb = kotlin.math.sqrt(b.sumOf { it.toDouble() * it }); return if (na == 0.0 || nb == 0.0) 0.0 else dot / (na * nb) }
}

class KnowledgeRetriever(private val sourceRegistry: AstroKnowledgeSourceRegistry) {
    fun retrieve(query: String, chunks: List<KnowledgeChunk>, rules: List<KnowledgeRule> = emptyList(), filters: KnowledgeSearchFilters = KnowledgeSearchFilters(), deterministic: List<AstroEvidenceItem> = emptyList(), web: List<WebEvidence> = emptyList(), limit: Int = 20): AstroEvidenceBundle {
        val eligible = chunks.filter { chunk -> chunk.sourceId?.let(sourceRegistry::find)?.let(AstroKnowledgeSourcePolicy::mayEnterProduction) == true }
        val matches = KnowledgeSearchEngine.search(query, eligible, filters, limit)
        val items = mutableListOf<AstroEvidenceItem>(); items += deterministic
        matches.forEach { m ->
            val source = m.chunk.sourceId?.let(sourceRegistry::find)
            val metadata = mapOf("matchType" to m.matchType.name, "version" to m.chunk.version, "page" to (m.chunk.page?.toString() ?: ""), "chapter" to (m.chunk.chapter ?: "")) + source?.let {
                mapOf("sourceTitle" to it.title, "sourceLicense" to (it.license ?: ""), "rightsStatus" to it.rightsStatus.name, "sourceClass" to it.sourceClass.name, "sourceCommit" to (it.commit ?: ""))
            }.orEmpty()
            items += AstroEvidenceItem(m.chunk.chunkId, AstroEvidenceKind.LOCAL_KNOWLEDGE, m.chunk.text.ifBlank { m.chunk.interpretationKey }, m.chunk.sourceId, m.chunk.sourceRef, m.chunk.checksum, m.chunk.traditionId, metadata = metadata)
        }
        val ruleById = rules.associateBy { it.ruleId }
        matches.flatMap { it.chunk.ruleIds + listOfNotNull(it.chunk.ruleId) }.distinct().mapNotNull(ruleById::get).forEach { rule ->
            val source = sourceRegistry.find(rule.sourceId)
            if (source != null && AstroKnowledgeSourcePolicy.mayEnterProduction(source) && rule.rightsStatus == "VERIFIED") items += AstroEvidenceItem(rule.ruleId, AstroEvidenceKind.KNOWLEDGE_RULE, rule.interpretationKey, rule.sourceId, rule.sourceRefs.firstOrNull(), rule.checksum, rule.traditionId ?: rule.tradition, metadata = mapOf("sourceTitle" to source.title, "sourceLicense" to (source.license ?: ""), "rightsStatus" to source.rightsStatus.name, "sourceClass" to source.sourceClass.name, "sourceCommit" to (source.commit ?: ""), "packVersion" to rule.packVersion))
        }
        web.forEach { item -> items += AstroEvidenceItem(item.resultId, AstroEvidenceKind.REMOTE_WEB, "${item.title}: ${item.snippet}", sourceRef = item.url, checksum = item.contentHash, traditionId = item.traditionId, metadata = mapOf("domain" to item.domain, "quality" to item.quality.name, "provider" to item.provider, "retrievedAt" to item.retrievedAtEpochMs.toString())) }
        return AstroEvidenceBundle(query, items.distinctBy { it.evidenceId }, remoteWebUsed = web.isNotEmpty())
    }
}
