package com.aynvora.core.astrology.knowledge

import com.aynvora.core.garudapuran.GarudaChecksumVerifier
import kotlinx.serialization.Serializable

@Serializable
enum class AstroSourceClass { OFFICIAL_PRIMARY, OPEN_SOURCE, PUBLIC_DOMAIN, LICENSED, RESEARCH_REFERENCE, SECONDARY_WEB, UNKNOWN_RIGHTS }
@Serializable
enum class AstroSourceStatus { VERIFIED, RESEARCH_ONLY, LICENSE_REQUIRED, NOT_VERIFIED, REJECTED }
@Serializable
enum class AstroRightsStatus { VERIFIED, REVIEW_REQUIRED, LICENSE_REQUIRED, NOT_VERIFIED, REJECTED }
@Serializable
enum class AstroSourceType { WEB_PAGE, GITHUB_REPOSITORY, GITHUB_FILE, PUBLIC_API, PUBLIC_PDF, PUBLIC_DOMAIN_SCAN, LICENSED_DOCUMENT, STRUCTURED_DATASET }

@Serializable
data class AstroKnowledgeSource(
    val sourceId: String,
    val title: String,
    val author: String? = null,
    val organization: String? = null,
    val publisher: String? = null,
    val sourceType: AstroSourceType,
    val sourceClass: AstroSourceClass,
    val url: String,
    val repository: String? = null,
    val commit: String? = null,
    val edition: String? = null,
    val publicationYear: Int? = null,
    val language: String,
    val license: String? = null,
    val rightsStatus: AstroRightsStatus = AstroRightsStatus.NOT_VERIFIED,
    val accessDateEpochMs: Long? = null,
    val rightsTerritories: List<String> = emptyList(),
    val contentHash: String? = null,
    val version: String? = null,
    val status: AstroSourceStatus = AstroSourceStatus.NOT_VERIFIED,
    val inspectedFiles: List<String> = emptyList(),
    /** Per-file audit hashes; these are not whole-repository or production-pack hashes. */
    val inspectedFileHashes: Map<String, String> = emptyMap(),
    val codeCopied: Boolean = false,
    val dataCopied: Boolean = false,
    val formulaReused: Boolean = false,
) {
    init {
        require(sourceId.isNotBlank() && title.isNotBlank() && language.isNotBlank())
        require(url.startsWith("https://", ignoreCase = true)) { "Knowledge sources must use HTTPS." }
        require(commit == null || repository != null) { "A commit requires a repository URL." }
    }
}

object AstroKnowledgeSourcePolicy {
    fun mayUseForResearch(source: AstroKnowledgeSource): Boolean =
        source.status != AstroSourceStatus.REJECTED && source.rightsStatus != AstroRightsStatus.REJECTED

    fun mayEnterProduction(source: AstroKnowledgeSource): Boolean =
        source.status == AstroSourceStatus.VERIFIED &&
            source.rightsStatus == AstroRightsStatus.VERIFIED &&
            source.sourceClass in setOf(AstroSourceClass.OFFICIAL_PRIMARY, AstroSourceClass.OPEN_SOURCE, AstroSourceClass.PUBLIC_DOMAIN, AstroSourceClass.LICENSED) &&
            !source.license.isNullOrBlank() &&
            source.contentHash?.matches(Regex("[a-fA-F0-9]{64}")) == true
}

class AstroKnowledgeSourceRegistry(sources: List<AstroKnowledgeSource>) {
    private val byId = sources.associateBy { it.sourceId }
    init { require(byId.size == sources.size) { "Knowledge source IDs must be unique." } }
    fun all(): List<AstroKnowledgeSource> = byId.values.sortedBy { it.sourceId }
    fun find(sourceId: String): AstroKnowledgeSource? = byId[sourceId]
    fun productionSources(): List<AstroKnowledgeSource> = all().filter(AstroKnowledgeSourcePolicy::mayEnterProduction)

    companion object {
        /** Pinned research leads from Phase 10.22; none is production-approved. */
        fun phase1022ResearchReferences(): AstroKnowledgeSourceRegistry = AstroKnowledgeSourceRegistry(listOf(
            AstroKnowledgeSource("vedastro-fcb4dede", "VedAstro", organization = "VedAstro", sourceType = AstroSourceType.GITHUB_REPOSITORY, sourceClass = AstroSourceClass.OPEN_SOURCE, url = "https://github.com/VedAstro/VedAstro", repository = "https://github.com/VedAstro/VedAstro", commit = "fcb4dede360372545eb244c53e9a80ec3510e194", language = "en", license = "MIT (repository); referenced data/dependencies require separate review", rightsStatus = AstroRightsStatus.REVIEW_REQUIRED, version = "fcb4dede", status = AstroSourceStatus.RESEARCH_ONLY, inspectedFiles = listOf("Library/Data/EventData.cs", "Library/Data/TimeRange.cs")),
            AstroKnowledgeSource("kp-astrology-2c261382", "KP-Astrology", author = "girishbp-wq", sourceType = AstroSourceType.GITHUB_REPOSITORY, sourceClass = AstroSourceClass.UNKNOWN_RIGHTS, url = "https://github.com/girishbp-wq/KP-Astrology", repository = "https://github.com/girishbp-wq/KP-Astrology", commit = "2c261382cb53720ae301ca944249540dc4e711cf", language = "en", rightsStatus = AstroRightsStatus.NOT_VERIFIED, version = "2c261382", status = AstroSourceStatus.RESEARCH_ONLY, inspectedFiles = listOf("README.md", "index.html", "Rashis with star lords with Degrees.html"), inspectedFileHashes = mapOf("README.md" to "1020a051e3ca96d766d780acb9e2f04f529fbf8bc96ead873569ea5c7a1ef3e4")),
            AstroKnowledgeSource("mayaastrolib-4fc184a6", "MayaAstrolib", author = "ranganc007", sourceType = AstroSourceType.GITHUB_REPOSITORY, sourceClass = AstroSourceClass.OPEN_SOURCE, url = "https://github.com/ranganc007/mayaastrolib", repository = "https://github.com/ranganc007/mayaastrolib", commit = "4fc184a679a2c77170ce9374ed7c1cd577507cda", language = "en", license = "MIT repository; pyswisseph LGPL; Swiss Ephemeris separate GPL/commercial terms", rightsStatus = AstroRightsStatus.REVIEW_REQUIRED, version = "4fc184a6", status = AstroSourceStatus.RESEARCH_ONLY, inspectedFiles = listOf("mayaastrolib/vedic/kp.py", "tests/test_vedic_kp.py", "tests/golden/test_vedic_positions.py", "LICENSE", "LICENSING.md"), inspectedFileHashes = mapOf("mayaastrolib/vedic/kp.py" to "5c9acc7c90b58fc18766d58d5547e1b68336fa7a4fff69f462583f4e471ed171", "LICENSING.md" to "b188d90bb6a73b2732605f0adc7c0866a422455b7066484f954023d503c4efcd")),
            AstroKnowledgeSource("vedic-calc-620d5456", "vedic-calc", author = "atolat", sourceType = AstroSourceType.GITHUB_REPOSITORY, sourceClass = AstroSourceClass.OPEN_SOURCE, url = "https://github.com/atolat/vedic-calc", repository = "https://github.com/atolat/vedic-calc", commit = "620d54560ee7e108496495eeb17054a52630a0bf", language = "en", license = "AGPL-3.0-or-later; pyswisseph has separate license terms", rightsStatus = AstroRightsStatus.REVIEW_REQUIRED, version = "620d5456", status = AstroSourceStatus.RESEARCH_ONLY, inspectedFiles = listOf("src/vedic_calc/kp/sublords.py", "tests/test_kp.py", "tests/test_varshaphal.py", "pyproject.toml", "LICENSE", "README.md"), inspectedFileHashes = mapOf("src/vedic_calc/kp/sublords.py" to "bb01beba8cccb8d81b4831cb39ef089d0ba7142e6498ddce40d3252ca57d1ad4", "LICENSE" to "b263407b3e9e2fd0b91b7ec099d38bd045a94011abffda84337027a42f4312a5")),
        ))
    }
}

@Serializable
data class AstroSourceCollectionRequest(
    val source: AstroKnowledgeSource,
    val requireProductionRights: Boolean = false,
    val expectedContentHash: String? = null,
    val maxBytes: Long = 20L * 1024L * 1024L,
) {
    init { require(maxBytes > 0L) }
}

data class AstroSourcePayload(
    val bytes: ByteArray,
    val mediaType: String,
    val finalUrl: String,
    val providerId: String,
    val accessedAtEpochMs: Long,
)
fun interface AstroSourceFetcher {
    suspend fun fetch(url: String, sourceType: AstroSourceType, maxBytes: Long): AstroSourcePayload
}
data class AstroCollectedSource(val source: AstroKnowledgeSource, val payload: AstroSourcePayload)

/** Explicit host-authorized fetch interface; this component never discovers or scrapes URLs. */
class AstroSourceCollector(
    private val fetcher: AstroSourceFetcher,
    private val allowedHosts: Set<String>,
    private val blockedHosts: Set<String> = emptySet(),
) {
    suspend fun collect(request: AstroSourceCollectionRequest): AstroCollectedSource {
        val source = request.source
        require(AstroKnowledgeSourcePolicy.mayUseForResearch(source)) { "Rejected sources cannot be collected." }
        if (request.requireProductionRights) require(AstroKnowledgeSourcePolicy.mayEnterProduction(source)) {
            "Source rights and verification are not approved for production."
        }
        require(isAllowedHost(source.url)) { "The source host is not allowlisted." }
        val payload = fetcher.fetch(source.url, source.sourceType, request.maxBytes)
        require(payload.bytes.size.toLong() <= request.maxBytes) { "Source payload exceeds the configured size limit." }
        require(isAllowedHost(payload.finalUrl)) { "The fetch provider redirected outside the allowlist." }
        val digest = GarudaChecksumVerifier.calculateSha256(payload.bytes)
        if (request.expectedContentHash != null) require(digest.equals(request.expectedContentHash, ignoreCase = true)) {
            "Collected content checksum does not match the expected SHA-256."
        }
        return AstroCollectedSource(source.copy(contentHash = digest, accessDateEpochMs = payload.accessedAtEpochMs), payload)
    }

    private fun isAllowedHost(url: String): Boolean {
        if (!url.startsWith("https://", ignoreCase = true)) return false
        val host = url.substringAfter("://", "").substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':').lowercase().trimEnd('.')
        if (host.isBlank() || blockedHosts.any { blocked ->
                val normalized = blocked.lowercase().trim().trimEnd('.')
                host == normalized || host.endsWith(".$normalized")
            }) return false
        return allowedHosts.any { allowed ->
            val normalized = allowed.lowercase().trim().trimEnd('.')
            normalized.isNotBlank() && (host == normalized || host.endsWith(".$normalized"))
        }
    }
}

data class AstroDocumentFragment(val text: String, val page: Int? = null, val chapter: String? = null)
data class AstroKnowledgeDocument(
    val source: AstroKnowledgeSource,
    val traditionId: String,
    val language: String,
    val fragments: List<AstroDocumentFragment>,
    val tags: List<String> = emptyList(),
)

class AstroKnowledgeChunker {
    fun chunk(document: AstroKnowledgeDocument, maxChars: Int = 1600): List<com.aynvora.core.astrology.prediction.KnowledgeChunk> {
        require(maxChars >= 128)
        require(AstroKnowledgeSourcePolicy.mayEnterProduction(document.source)) {
            "Only verified, rights-cleared sources can enter production chunks."
        }
        val sourceVersion = document.source.version?.takeIf { it.isNotBlank() }
            ?: error("Production sources require an explicit source version.")
        val output = mutableListOf<com.aynvora.core.astrology.prediction.KnowledgeChunk>()
        document.fragments.forEach { fragment ->
            val paragraphs = mutableListOf<String>()
            fun flush() {
                if (paragraphs.isNotEmpty()) {
                    output += paragraphs.joinToString("\n\n").toKnowledgeChunk(document, fragment, sourceVersion)
                    paragraphs.clear()
                }
            }
            fragment.text.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotBlank() }.forEach { paragraph ->
                if (paragraph.length > maxChars) {
                    flush()
                    paragraph.chunkByWords(maxChars).forEach { output += it.toKnowledgeChunk(document, fragment, sourceVersion) }
                } else {
                    val currentLength = paragraphs.sumOf { it.length + 2 }
                    if (paragraphs.isNotEmpty() && currentLength + paragraph.length + 2 > maxChars) flush()
                    paragraphs += paragraph
                }
            }
            flush()
        }
        return output
    }

    private fun String.chunkByWords(maxChars: Int): List<String> {
        val pieces = mutableListOf<String>()
        var current = StringBuilder()
        split(Regex("\\s+")).forEach { word ->
            if (current.isNotEmpty() && current.length + word.length + 1 > maxChars) {
                pieces += current.toString()
                current = StringBuilder()
            }
            if (word.length > maxChars) {
                if (current.isNotEmpty()) { pieces += current.toString(); current = StringBuilder() }
                word.chunked(maxChars).forEach(pieces::add)
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(word)
            }
        }
        if (current.isNotEmpty()) pieces += current.toString()
        return pieces
    }

    private fun String.toKnowledgeChunk(
        document: AstroKnowledgeDocument,
        fragment: AstroDocumentFragment,
        sourceVersion: String,
    ): com.aynvora.core.astrology.prediction.KnowledgeChunk {
        val source = document.source
        val hash = GarudaChecksumVerifier.calculateSha256(encodeToByteArray())
        val locator = listOfNotNull(source.url, fragment.chapter?.let { "chapter=${it}" }, fragment.page?.let { "page=${it}" }).joinToString("#")
        val idHash = GarudaChecksumVerifier.calculateSha256("${source.sourceId}|$locator|$hash")
        return com.aynvora.core.astrology.prediction.KnowledgeChunk(
            chunkId = "${source.sourceId}:$idHash",
            traditionId = document.traditionId,
            topic = fragment.chapter ?: source.title,
            condition = "",
            interpretationKey = "knowledge.${source.sourceId}.$idHash",
            sourceRef = locator,
            sourcePage = fragment.page?.toString(),
            sourceEdition = source.edition,
            language = document.language,
            licenseStatus = "VERIFIED",
            checksum = hash,
            version = sourceVersion,
            sourceAuthor = source.author,
            sourceYear = source.publicationYear,
            sourceLocation = source.url,
            packVersion = sourceVersion,
            chapter = fragment.chapter,
            page = fragment.page,
            sourceId = source.sourceId,
            text = this,
            tags = document.tags.distinct().sorted(),
        )
    }
}
