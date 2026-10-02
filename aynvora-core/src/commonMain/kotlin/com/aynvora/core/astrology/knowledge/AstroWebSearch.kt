package com.aynvora.core.astrology.knowledge

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import kotlinx.serialization.Serializable

@Serializable
enum class AstroWebQuality { PRIMARY, OFFICIAL, ACADEMIC_RESEARCH, OPEN_SOURCE, SECONDARY, UNKNOWN }

@Serializable
data class WebEvidence(
    val resultId: String,
    val title: String,
    val snippet: String,
    val url: String,
    val domain: String,
    val publishedAt: String? = null,
    val retrievedAtEpochMs: Long,
    val provider: String,
    val query: String,
    val contentHash: String,
    val quality: AstroWebQuality = AstroWebQuality.UNKNOWN,
    val traditionId: String? = null,
    val evidenceKind: String = "REMOTE_WEB",
)

/** Keep secrets out of serialization and toString by accepting a credential only at the transport boundary. */
data class AstroWebSearchConfig(
    val endpoint: String,
    val apiKey: String? = null,
    val maxResults: Int = 5,
    val locale: String = "en",
    val country: String? = null,
    val freshness: String? = null,
    val allowlist: Set<String> = emptySet(),
    val blocklist: Set<String> = emptySet(),
    val timeoutMillis: Long = 10_000,
    val maxCacheAgeMillis: Long = 86_400_000,
) {
    init { require(endpoint.startsWith("https://", true)); require(maxResults in 1..50); require(timeoutMillis > 0); require(maxCacheAgeMillis >= 0) }
    override fun toString(): String = "AstroWebSearchConfig(endpoint=$endpoint, apiKey=<redacted>, maxResults=$maxResults, locale=$locale)"
}

@Serializable
data class AstroWebSearchRequest(val query: String, val maxResults: Int, val locale: String, val country: String?, val freshness: String?, val allowlist: Set<String>, val blocklist: Set<String>)
@Serializable
data class AstroWebRawResult(val title: String, val snippet: String, val url: String, val publishedAt: String? = null, val providerResultId: String? = null)
fun interface RemoteWebSearchTransport { suspend fun search(config: AstroWebSearchConfig, request: AstroWebSearchRequest): List<AstroWebRawResult> }

/** Provider adapter is injected by the host: no search engine HTML scraping occurs in the SDK. */
class RemoteWebSearchProvider(private val config: AstroWebSearchConfig, private val transport: RemoteWebSearchTransport) {
    suspend fun search(query: String, retrievedAtEpochMs: Long, traditionId: String? = null): List<WebEvidence> {
        require(query.isNotBlank())
        val raw = transport.search(config, AstroWebSearchRequest(query, config.maxResults, config.locale, config.country, config.freshness, config.allowlist, config.blocklist))
        return raw.asSequence().filter { allowed(it.url) }.take(config.maxResults).map { result ->
            val domain = result.url.substringAfter("https://", "").substringBefore('/').substringBefore(':').lowercase()
            val snippet = sanitize(result.snippet).take(1200)
            val hash = GarudaChecksumVerifier.calculateSha256("${result.title}\n$snippet\n${result.url}".encodeToByteArray())
            WebEvidence(result.providerResultId ?: hash.take(20), sanitize(result.title).take(300), snippet, result.url, domain, result.publishedAt, retrievedAtEpochMs, "configured-provider", query, hash, quality(domain), traditionId)
        }.toList()
    }

    private fun allowed(url: String): Boolean {
        if (!url.startsWith("https://", true)) return false
        val domain = url.substringAfter("https://").substringBefore('/').substringBefore(':').lowercase().trimEnd('.')
        fun matches(host: String, rule: String): Boolean { val r = rule.lowercase().trim().trimStart('.').trimEnd('.'); return host == r || host.endsWith(".$r") }
        if (config.blocklist.any { matches(domain, it) }) return false
        return config.allowlist.isEmpty() || config.allowlist.any { matches(domain, it) }
    }
    private fun quality(domain: String): AstroWebQuality = when {
        domain.endsWith(".gov") || domain.endsWith(".edu") -> AstroWebQuality.OFFICIAL
        domain in setOf("github.com", "gitlab.com") -> AstroWebQuality.OPEN_SOURCE
        else -> AstroWebQuality.UNKNOWN
    }
    private fun sanitize(text: String): String = text
        .replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), " ")
        .replace(Regex("(?s)<[^>]*>"), " ")
        .replace(Regex("(?i)javascript:", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s+"), " ").trim()
}

/** Caller supplies time so offline and test behavior stays deterministic. Expired items are never returned. */
class AstroWebEvidenceCache(private val capacity: Int = 256) {
    private val entries = linkedMapOf<String, WebEvidence>()
    init { require(capacity > 0) }
    @Synchronized fun put(evidence: WebEvidence) { entries.remove(evidence.resultId); entries[evidence.resultId] = evidence; while (entries.size > capacity) entries.remove(entries.keys.first()) }
    @Synchronized fun get(resultId: String, nowEpochMs: Long, maxAgeMillis: Long): WebEvidence? = entries[resultId]?.takeIf { nowEpochMs >= it.retrievedAtEpochMs && nowEpochMs - it.retrievedAtEpochMs <= maxAgeMillis }
    @Synchronized fun search(query: String, nowEpochMs: Long, maxAgeMillis: Long): List<WebEvidence> = entries.values.filter { it.query.equals(query, true) && nowEpochMs >= it.retrievedAtEpochMs && nowEpochMs - it.retrievedAtEpochMs <= maxAgeMillis }
}
