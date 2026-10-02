package com.aynvora.core.astrology.knowledge

import com.aynvora.core.astrology.prediction.KnowledgeChunk
import com.aynvora.core.astrology.prediction.KnowledgeSearchEngine
import com.aynvora.core.astrology.prediction.KnowledgeSearchFilters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AstroKnowledgePipelineTest {
    private fun source(id: String = "s1") = AstroKnowledgeSource(id, "Approved source", sourceType = AstroSourceType.PUBLIC_API, sourceClass = AstroSourceClass.OFFICIAL_PRIMARY, url = "https://example.org/data", language = "en", license = "CC0", rightsStatus = AstroRightsStatus.VERIFIED, contentHash = "a".repeat(64), version = "v1", status = AstroSourceStatus.VERIFIED)

    @Test fun productionPolicyRequiresVerificationAndRights() {
        assertTrue(AstroKnowledgeSourcePolicy.mayEnterProduction(source()))
        assertFalse(AstroKnowledgeSourcePolicy.mayEnterProduction(source().copy(rightsStatus = AstroRightsStatus.REVIEW_REQUIRED)))
        assertFalse(AstroKnowledgeSourcePolicy.mayEnterProduction(source().copy(contentHash = null)))
    }

    @Test fun collectorChecksAllowlistRedirectAndExpectedDigest() {
        val approved = source()
        val collector = AstroSourceCollector(
            AstroSourceFetcher { url, _, _ -> AstroSourcePayload("payload".encodeToByteArray(), "text/plain", url, "fixture", 1234) },
            allowedHosts = setOf("example.org"), blockedHosts = setOf("blocked.example.org"),
        )
        kotlinx.coroutines.runBlocking {
            val digest = com.aynvora.core.garudapuran.GarudaChecksumVerifier.calculateSha256("payload".encodeToByteArray())
            assertEquals(digest, collector.collect(AstroSourceCollectionRequest(approved, expectedContentHash = digest)).source.contentHash)
            kotlin.test.assertFailsWith<IllegalArgumentException> {
                collector.collect(AstroSourceCollectionRequest(approved.copy(url = "https://blocked.example.org/x")))
            }
            kotlin.test.assertFailsWith<IllegalArgumentException> {
                collector.collect(AstroSourceCollectionRequest(approved, expectedContentHash = "0".repeat(64)))
            }
        }
    }

    @Test fun chunkerPreservesPageChapterAndUniqueLocator() {
        val approved = source()
        val doc = AstroKnowledgeDocument(approved, "PARASHARA", "en", listOf(AstroDocumentFragment("First verified paragraph.", 4, "Dasha"), AstroDocumentFragment("First verified paragraph.", 5, "Dasha")))
        val chunks = AstroKnowledgeChunker().chunk(doc)
        assertEquals(2, chunks.size)
        assertEquals(4, chunks[0].page)
        assertEquals("Dasha", chunks[1].chapter)
        assertTrue(chunks[0].chunkId != chunks[1].chunkId)
    }

    @Test fun searchFiltersAndSynonymRemainOffline() {
        val chunk = KnowledgeChunk("c1", "KP", "Star Lord", "significator", "kp.significator", "https://example.org#page=2", language = "en", licenseStatus = "VERIFIED", checksum = "a", version = "v1", text = "planetary significator", sourceId = "s1", tags = listOf("significator"))
        assertEquals("c1", KnowledgeSearchEngine.search("significator", listOf(chunk), filters = KnowledgeSearchFilters(sourceId = "s1")).single().chunk.chunkId)
        assertEquals("c1", KnowledgeSearchEngine.search("significator", listOf(chunk), synonyms = mapOf("significator" to setOf("indicator"))).single().chunk.chunkId)
        assertTrue(KnowledgeSearchEngine.search("significtor", listOf(chunk)).isNotEmpty())
        assertTrue(KnowledgeSearchEngine.search("significator", listOf(chunk), filters = KnowledgeSearchFilters(sourceId = "wrong")).isEmpty())
    }

    @Test fun webEvidenceSanitizesAndCacheHonorsFreshness() {
        val config = AstroWebSearchConfig("https://search.example/api", allowlist = setOf("example.org"))
        val provider = RemoteWebSearchProvider(config) { _, _ -> listOf(AstroWebRawResult("<b>Title</b>", "Safe <script>steal()</script> snippet", "https://www.example.org/page"), AstroWebRawResult("blocked", "text", "https://elsewhere.net")) }
        kotlinx.coroutines.runBlocking {
            val item = provider.search("KP", 1000).single()
            assertEquals("Title", item.title)
            assertFalse("steal" in item.snippet)
            val cache = AstroWebEvidenceCache(1); cache.put(item)
            assertNotNull(cache.get(item.resultId, 1100, 500))
            assertNull(cache.get(item.resultId, 2000, 500))
        }
    }

    @Test fun registryLabelsUnsupportedTraditionsAndProviderConditionalWeb() {
        val tools = AstroToolRegistry.all()
        assertEquals(AstroToolStatus.AVAILABLE, tools.single { it.toolId == "getKP" }.status)
        assertEquals(AstroToolStatus.RESEARCH_ONLY, tools.single { it.toolId == "getLalKitab" }.status)
        assertEquals(AstroToolStatus.UNAVAILABLE, tools.single { it.toolId == "searchWeb" }.status)
        assertEquals(AstroToolStatus.AVAILABLE, AstroToolRegistry.all(true).single { it.toolId == "searchWeb" }.status)
    }

    @Test fun evidenceFusionKeepsLocalAndRemoteProvenanceSeparate() {
        val src = source()
        val registry = AstroKnowledgeSourceRegistry(listOf(src))
        val chunk = KnowledgeChunk("c2", "PARASHARA", "Dasha", "", "dasha.topic", "https://example.org/data#page=7", language = "en", licenseStatus = "VERIFIED", checksum = "sha", version = "v1", sourceId = src.sourceId, text = "Verified explanation", page = 7)
        val remote = WebEvidence("w1", "External", "Supplement", "https://outside.example/article", "outside.example", retrievedAtEpochMs = 100, provider = "fixture", query = "dasha", contentHash = "hash")
        val result = KnowledgeRetriever(registry).retrieve("Verified explanation", listOf(chunk), deterministic = listOf(AstroEvidenceItem("calc", AstroEvidenceKind.DETERMINISTIC_CALCULATION, "Moon degree: 10")), web = listOf(remote))
        assertTrue(result.remoteWebUsed)
        assertEquals(AstroEvidenceKind.DETERMINISTIC_CALCULATION, result.items.first().kind)
        assertTrue(result.items.any { it.kind == AstroEvidenceKind.LOCAL_KNOWLEDGE && it.metadata["sourceTitle"] == src.title })
        assertTrue(result.items.any { it.kind == AstroEvidenceKind.REMOTE_WEB && it.sourceRef == remote.url })
    }
}
