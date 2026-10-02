package com.aynvora.core.astrology.prediction

import kotlin.test.Test
import kotlin.test.assertEquals

class KnowledgeSearchEngineTest {
    private val exact = chunk("exact", topic = "Navamsha chart", condition = "D9 divisional chart")
    private val token = chunk("token", topic = "Varga", condition = "Navamsha represents a divisional chart")
    private val unrelated = chunk("other", topic = "Transit", condition = "Saturn movement")

    @Test
    fun phraseAndTokenSearchIsDeterministicAndFiltersMetadata() {
        val found = KnowledgeSearchEngine.search(
            "Navamsha chart", listOf(token, unrelated, exact),
            KnowledgeSearchFilters(traditionId = "VEDIC", language = "en"),
        )
        assertEquals(listOf("exact", "token"), found.map { it.chunk.chunkId })
        assertEquals(KnowledgeSearchMatchType.EXACT_PHRASE, found.first().matchType)
        assertEquals(listOf("exact"), KnowledgeSearchEngine.search(
            "Navamsha", listOf(exact, token), KnowledgeSearchFilters(ruleId = "rule-exact"),
        ).map { it.chunk.chunkId })
    }

    @Test
    fun semanticSearchOnlyFillsSlotsAfterLocalTextMatches() {
        val semantic = KnowledgeSemanticSearch { _, _ -> listOf(KnowledgeSemanticHit("other", 0.99), KnowledgeSemanticHit("exact", 0.8)) }
        val found = KnowledgeSearchEngine.search("Navamsha", listOf(unrelated, exact), limit = 2, semanticSearch = semantic)
        assertEquals(listOf(KnowledgeSearchMatchType.EXACT_PHRASE, KnowledgeSearchMatchType.SEMANTIC), found.map { it.matchType })
        assertEquals(listOf("exact", "other"), found.map { it.chunk.chunkId })
    }

    private fun chunk(id: String, topic: String, condition: String) = KnowledgeChunk(
        chunkId = id, traditionId = "VEDIC", topic = topic, condition = condition,
        interpretationKey = "key.$id", sourceRef = "source:$id", language = "en",
        licenseStatus = "CLEARED", checksum = "checksum:$id", version = "1",
        ruleId = "rule-$id",
    )
}
