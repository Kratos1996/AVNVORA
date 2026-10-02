package com.aynvora.core.astrology.prediction

import kotlinx.serialization.Serializable

/** Optional host supplied semantic index. The SDK itself remains usable without embeddings. */
fun interface KnowledgeSemanticSearch {
    fun search(query: String, limit: Int): List<KnowledgeSemanticHit>
}

@Serializable
data class KnowledgeSemanticHit(val chunkId: String, val score: Double) {
    init { require(score.isFinite()) }
}

@Serializable
data class KnowledgeSearchFilters(
    val traditionId: String? = null,
    val topic: String? = null,
    val language: String? = null,
    val licenseStatus: String? = null,
    val sourceRef: String? = null,
    val sourceId: String? = null,
    val ruleId: String? = null,
)

@Serializable
data class KnowledgeSearchResult(
    val chunk: KnowledgeChunk,
    val matchType: KnowledgeSearchMatchType,
    val score: Double,
)

@Serializable
enum class KnowledgeSearchMatchType { EXACT_PHRASE, TOKEN, SYNONYM, FUZZY, SEMANTIC }

/** Deterministic local token search, with optional semantic candidates ranked after text matches. */
object KnowledgeSearchEngine {
    fun search(
        query: String,
        chunks: List<KnowledgeChunk>,
        filters: KnowledgeSearchFilters = KnowledgeSearchFilters(),
        limit: Int = 20,
        semanticSearch: KnowledgeSemanticSearch? = null,
        synonyms: Map<String, Set<String>> = emptyMap(),
    ): List<KnowledgeSearchResult> {
        require(limit >= 0)
        if (limit == 0 || query.isBlank()) return emptyList()
        val eligible = chunks.filter { chunk ->
            (filters.traditionId == null || chunk.traditionId == filters.traditionId) &&
                (filters.topic == null || chunk.topic.equals(filters.topic, ignoreCase = true)) &&
                (filters.language == null || chunk.language.equals(filters.language, ignoreCase = true)) &&
                (filters.licenseStatus == null || chunk.licenseStatus.equals(filters.licenseStatus, ignoreCase = true)) &&
                (filters.sourceRef == null || chunk.sourceRef == filters.sourceRef) &&
                (filters.sourceId == null || chunk.sourceId == filters.sourceId) &&
                (filters.ruleId == null || chunk.ruleId == filters.ruleId)
        }
        val queryText = normalize(query)
        val queryTokens = tokenize(query)
        val textResults = eligible.mapNotNull { chunk ->
            val fields = listOf(chunk.topic, chunk.condition, chunk.interpretationKey, chunk.traditionId, chunk.text) +
                chunk.structuredFacts.values + chunk.tags
            val normalizedFields = fields.map(::normalize)
            val phraseMatch = normalizedFields.any { queryText in it }
            val fieldTokens = normalizedFields.flatMap(::tokenize).toSet()
            val matchingTokens = queryTokens.count { it in fieldTokens }
            val synonymMatches = queryTokens.count { token ->
                token !in fieldTokens && synonyms.entries.any { (canonical, aliases) ->
                    val normalizedCanonical = normalize(canonical)
                    val normalizedAliases = aliases.map(::normalize)
                    (token == normalizedCanonical || token in normalizedAliases) &&
                        (normalizedCanonical in fieldTokens || normalizedAliases.any { it in fieldTokens })
                }
            }
            val fuzzyMatches = queryTokens.count { token ->
                token !in fieldTokens && synonyms.keys.none { normalize(it) == token } &&
                    fieldTokens.any { candidate -> candidate.length >= 4 && editDistanceAtMostOne(token, candidate) }
            }
            when {
                phraseMatch -> KnowledgeSearchResult(chunk, KnowledgeSearchMatchType.EXACT_PHRASE, 1.0)
                matchingTokens > 0 -> KnowledgeSearchResult(chunk, KnowledgeSearchMatchType.TOKEN, matchingTokens.toDouble() / queryTokens.size.coerceAtLeast(1))
                synonymMatches > 0 -> KnowledgeSearchResult(chunk, KnowledgeSearchMatchType.SYNONYM, synonymMatches.toDouble() / queryTokens.size.coerceAtLeast(1))
                fuzzyMatches > 0 -> KnowledgeSearchResult(chunk, KnowledgeSearchMatchType.FUZZY, fuzzyMatches.toDouble() / queryTokens.size.coerceAtLeast(1))
                else -> null
            }
        }.sortedWith(compareBy<KnowledgeSearchResult>({ it.matchType.ordinal }, { -it.score }, { it.chunk.chunkId }))

        val selected = textResults.take(limit).toMutableList()
        val remaining = limit - selected.size
        if (remaining > 0 && semanticSearch != null) {
            val byId = eligible.associateBy { it.chunkId }
            semanticSearch.search(query, remaining)
                .asSequence()
                .filter { hit -> selected.none { it.chunk.chunkId == hit.chunkId } && hit.chunkId in byId }
                .sortedWith(compareByDescending<KnowledgeSemanticHit> { it.score }.thenBy { it.chunkId })
                .take(remaining)
                .forEach { hit ->
                    selected += KnowledgeSearchResult(byId.getValue(hit.chunkId), KnowledgeSearchMatchType.SEMANTIC, hit.score)
                }
        }
        return selected
    }

    private fun normalize(value: String): String = value.lowercase().trim().replace(Regex("\\s+"), " ")
    private fun tokenize(value: String): Set<String> {
        val tokens = linkedSetOf<String>()
        val current = StringBuilder()
        fun flush() {
            if (current.isNotEmpty()) {
                tokens += current.toString()
                current.clear()
            }
        }
        normalize(value).forEach { character ->
            if (character.isLetterOrDigit() || character == '_') current.append(character) else flush()
        }
        flush()
        return tokens
    }

    private fun editDistanceAtMostOne(first: String, second: String): Boolean {
        if (kotlin.math.abs(first.length - second.length) > 1) return false
        var left = 0
        var right = 0
        var edits = 0
        while (left < first.length && right < second.length) {
            if (first[left] == second[right]) { left++; right++; continue }
            edits++
            if (edits > 1) return false
            when {
                first.length > second.length -> left++
                second.length > first.length -> right++
                else -> { left++; right++ }
            }
        }
        if (left < first.length || right < second.length) edits++
        return edits <= 1
    }
}
