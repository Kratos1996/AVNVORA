package com.aynvora.core.astrology.knowledge.tajika

import com.aynvora.core.astrology.knowledge.AstroKnowledgeSource
import com.aynvora.core.astrology.knowledge.AstroRightsStatus
import com.aynvora.core.astrology.knowledge.AstroSourceClass
import com.aynvora.core.astrology.knowledge.AstroSourceStatus
import com.aynvora.core.astrology.knowledge.AstroSourceType
import com.aynvora.core.astrology.knowledge.KnowledgePackValidator
import com.aynvora.core.astrology.prediction.AstroEventComparison
import com.aynvora.core.astrology.prediction.AstroEventCondition
import com.aynvora.core.astrology.prediction.KnowledgeChunk
import com.aynvora.core.astrology.prediction.KnowledgePack
import com.aynvora.core.astrology.prediction.KnowledgeRule
import com.aynvora.core.astrology.prediction.KnowledgeSource
import kotlinx.serialization.Serializable

@Serializable
data class TajikaGlossaryEntry(val term: String, val definition: String, val sourceRef: String, val license: String = "CC BY-SA 4.0")

/** Scoped, provenance-complete production pack. It covers selected Muntha and Varshesha indications only. */
@Serializable
data class TajikaKnowledgePack(
    val metadata: KnowledgePack,
    val sourceRegistry: List<AstroKnowledgeSource>,
    val chunks: List<KnowledgeChunk>,
    val glossary: List<TajikaGlossaryEntry>,
    val checksum: String,
    val license: String,
    val attribution: String,
) {
    fun validate(): List<String> = KnowledgePackValidator.validate(metadata, chunks)
    fun getSource(sourceId: String): AstroKnowledgeSource? = sourceRegistry.firstOrNull { it.sourceId == sourceId }

    companion object {
        const val PACK_ID = "TAJIKA_V1"
        const val VERSION = "1.0.0"
        const val CHECKSUM = "513be82525a8a3b846a86b6aaf770ef1575b61fcafe196e0f41de0bd4213301d"
        const val TRANSCRIPTION_SOURCE_ID = "tajika-neelakanthi-wikisource-pages-112-122-124"
        const val ATTRIBUTION = "Wikisource contributors, Tājika Nīlakaṇṭhī (Mahidhar Hindi commentary), Sanskrit Wikisource, revisions 378434, 378444, 378446, CC BY-SA 4.0. AYNVORA adaptations are distributed under CC BY-SA 4.0."
        private const val SCAN_ID = "tajika-neelakanthi-1907-scan"
        private val page112 = "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/112&oldid=378434"
        private val page122 = "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/122&oldid=378444"
        private val page124 = "https://sa.wikisource.org/w/index.php?title=पृष्ठम्:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf/124&oldid=378446"
        private const val SCAN_HASH = "a6968d0f22a277eca7d64649490baad1c1cd08b71dde4adacccd267420e5f989"
        private const val TEXT_HASH = "3ba811a6b537fe0fbecbb89674299107060f14504205c28a5f9bd3d2d47eda76"
        private const val PACK_TIME = 1790860608087L

        fun v1(): TajikaKnowledgePack {
            val sources = listOf(
                AstroKnowledgeSource(
                    sourceId = SCAN_ID,
                    title = "Tājika Nīlakaṇṭhī (Mahidhar Hindi commentary edition)",
                    author = "Nīlakaṇṭha Daivajña; Hindi commentary attributed in the edition to Mahidhar",
                    publisher = "Khemraj Shri Venkateshwar Steam Press, Mumbai",
                    sourceType = AstroSourceType.PUBLIC_DOMAIN_SCAN,
                    sourceClass = AstroSourceClass.PUBLIC_DOMAIN,
                    url = "https://commons.wikimedia.org/wiki/File:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf",
                    repository = "https://commons.wikimedia.org/",
                    edition = "Samvat 1964 / Śaka 1829 (1907 CE)",
                    publicationYear = 1907,
                    language = "Sanskrit with Hindi commentary",
                    license = "Public domain in India and the United States per Wikimedia Commons file record; territorial status may vary elsewhere.",
                    rightsStatus = AstroRightsStatus.VERIFIED,
                    accessDateEpochMs = PACK_TIME,
                    rightsTerritories = listOf("India", "United States"),
                    contentHash = SCAN_HASH,
                    version = "1907-scan-sha256:a6968d0f",
                    status = AstroSourceStatus.VERIFIED,
                    inspectedFiles = listOf("280-page PDF; folios 112, 122, 124 visually reviewed"),
                ),
                AstroKnowledgeSource(
                    sourceId = TRANSCRIPTION_SOURCE_ID,
                    title = "Wikisource transcription of selected Tājika Nīlakaṇṭhī pages",
                    author = "Wikisource contributors",
                    organization = "Sanskrit Wikisource",
                    publisher = "Wikisource contributors",
                    sourceType = AstroSourceType.WEB_PAGE,
                    sourceClass = AstroSourceClass.LICENSED,
                    url = "https://sa.wikisource.org/wiki/अनुक्रमणिका:ताजिकनीलकण्ठी_(महीधरकृतभाषाटीकासहिता).pdf",
                    repository = "https://sa.wikisource.org/",
                    edition = "Transcription revisions 378434, 378444, 378446 of the 1907 edition",
                    publicationYear = 2023,
                    language = "Sanskrit with Hindi commentary",
                    license = "CC BY-SA 4.0; attribution and ShareAlike apply to transcribed text and adaptations.",
                    rightsStatus = AstroRightsStatus.VERIFIED,
                    accessDateEpochMs = PACK_TIME,
                    rightsTerritories = listOf("Worldwide under CC BY-SA 4.0 terms"),
                    contentHash = TEXT_HASH,
                    version = "revisions:378434,378444,378446",
                    status = AstroSourceStatus.VERIFIED,
                    inspectedFiles = listOf("source/source_pages.json; pages 112, 122, 124; Wikisource marks these pages unproofread, cross-checked against the scan"),
                ),
            )
            val sourceRefs = mapOf(112 to page112, 122 to page122, 124 to page124)
            val texts = mapOf(
                112 to "The 1907 Hindi commentary describes annual indications for the Sun when it is Varshesha (year lord), distinguishing strong, middling, and weak conditions. It also says natal strength modifies how fully the annual indication manifests.",
                122 to "The commentary associates Muntha in the 3rd and 5th houses with favorable traditional indications, and in the 4th and 6th with challenging indications. Its house-specific descriptions are preserved as tradition-attributed themes, not factual forecasts.",
                124 to "The commentary qualifies house-based Muntha indications: malefic occupation or hostile malefic aspect can suppress favorable indications and strengthen adverse ones; benefic association/aspect and a strong lord can support favorable indications.",
            )
            val chunkData = listOf(
                Triple(112, "Varshesha indications", "11-13"),
                Triple(122, "Muntha phala", "7-11"),
                Triple(124, "Muntha phala and qualifications", "17-20"),
            )
            val chunks = chunkData.map { (page, topic, verses) ->
                val text = texts.getValue(page)
                val checksum = sha(text)
                KnowledgeChunk(
                    chunkId = "tajika:$page:${checksum.take(16)}",
                    traditionId = "TAJIKA",
                    topic = topic,
                    condition = "",
                    interpretationKey = "tajika.source.section.$page",
                    sourceRef = "${sourceRefs.getValue(page)}#printed-page-${if (page == 112) 104 else if (page == 122) 114 else 116}-verse-$verses",
                    sourcePage = "$page (printed p.${if (page == 112) 104 else if (page == 122) 114 else 116})",
                    sourceEdition = "1907 edition",
                    language = "en",
                    licenseStatus = "VERIFIED",
                    checksum = checksum,
                    version = VERSION,
                    sourceAuthor = "AYNVORA adaptation of Wikisource contributors' CC BY-SA 4.0 transcription",
                    sourceYear = 1907,
                    sourceLocation = sourceRefs.getValue(page),
                    packVersion = VERSION,
                    chapter = null,
                    page = page,
                    sourceId = TRANSCRIPTION_SOURCE_ID,
                    text = text,
                    tags = listOf("TAJIKA", "VARSHAPHAL", if (page == 112) "VARS HESHA".replace(" ", "") else "MUNTHA"),
                )
            }
            val rules = listOf(
                rule("TAJIKA_MUNTHA_H3_001", "Muntha", "muntha_house", "3", "TRADITIONAL_INDICATION", "tajika.muntha.house.3", "The source associates Muntha in house 3 with favorable traditional themes including initiative, reputation, comfort, and support.", listOf("muntha_house"), 122, "7"),
                rule("TAJIKA_MUNTHA_H4_001", "Muntha", "muntha_house", "4", "TRADITIONAL_INDICATION", "tajika.muntha.house.4", "The source associates Muntha in house 4 with challenging traditional themes, including distress and disruption of comfort.", listOf("muntha_house"), 122, "8"),
                rule("TAJIKA_MUNTHA_H5_001", "Muntha", "muntha_house", "5", "TRADITIONAL_INDICATION", "tajika.muntha.house.5", "The source associates Muntha in house 5 with favorable traditional themes including judgment, happiness, and gain.", listOf("muntha_house"), 122, "9"),
                rule("TAJIKA_MUNTHA_H6_001", "Muntha", "muntha_house", "6", "TRADITIONAL_INDICATION", "tajika.muntha.house.6", "The source associates Muntha in house 6 with challenging traditional themes.", listOf("muntha_house"), 122, "10"),
                rule("TAJIKA_MUNTHA_AFFLICTION_001", "Muntha", "muntha_bhava_malefic_afflicted", "true", "QUALIFIER", "tajika.muntha.affliction", "The source says malefic occupation or hostile malefic aspect can suppress favorable results for the occupied Muntha house and increase adverse indications.", listOf("muntha_house", "malefic_occupation", "hostile_malefic_aspect"), 124, "17"),
                rule("TAJIKA_MUNTHA_BENEFIC_001", "Muntha", "muntha_lord_strength", "strong", "QUALIFIER", "tajika.muntha.support", "The source says benefic association or aspect and a strong lord can support favorable indications for the Muntha house.", listOf("muntha_house", "muntha_lord_strength", "benefic_association", "benefic_aspect"), 124, "18"),
                rule("TAJIKA_VARSHESHA_SUN_STRONG_001", "Varshesha", "varshesha_planet", "SUN", "ANNUAL_PLANETARY_INDICATION", "tajika.varshesha.sun.strong", "The source associates a strong Sun acting as Varshesha with favorable traditional themes; natal strength is stated to modify how completely these indications manifest.", listOf("varshesha_planet", "varshesha_strength", "natal_sun_strength"), 112, "11"),
                rule("TAJIKA_VARSHESHA_SUN_MID_001", "Varshesha", "varshesha_planet", "SUN", "ANNUAL_PLANETARY_INDICATION", "tajika.varshesha.sun.middling", "The source describes a middling Sun as Varshesha as giving moderated or mixed traditional indications.", listOf("varshesha_planet", "varshesha_strength"), 112, "12"),
            )
            val knowledgeSources = sources.map { s ->
                KnowledgeSource(
                    sourceId = s.sourceId, title = s.title, author = s.author, edition = s.edition,
                    year = s.publicationYear, language = s.language, location = s.url, url = s.url,
                    pageOrChapter = if (s.sourceId == TRANSCRIPTION_SOURCE_ID) "Wikisource revisions 378434, 378444, 378446" else "folios 112, 122, 124",
                    licenseStatus = "VERIFIED", version = s.version, checksum = s.contentHash,
                    sourceClass = s.sourceClass.name, rightsStatus = "VERIFIED", repository = s.repository,
                    commit = s.version, contentHash = s.contentHash,
                )
            }
            val knowledgePack = KnowledgePack(
                packId = PACK_ID, tradition = "TAJIKA", version = VERSION, sources = knowledgeSources,
                rules = rules, checksum = CHECKSUM, status = "VERIFIED_SCOPED_CONTENT", locale = "en",
                sourceVersions = sources.associate { it.sourceId to (it.version ?: "") },
                buildTimestampEpochMs = PACK_TIME,
            )
            val glossary = listOf(
                TajikaGlossaryEntry("Muntha", "A Tajika annual-chart factor named by the cited source; this pack records source-specific house indications and does not assert equivalence with other traditions.", "${page122}#printed-page-114"),
                TajikaGlossaryEntry("Muntha phala", "The source section's term for Muntha-related indications.", "${page124}#printed-page-116"),
                TajikaGlossaryEntry("Bhava", "House, as used in the cited Hindi commentary.", "${page124}#printed-page-116-verse-17"),
                TajikaGlossaryEntry("Krura graha", "A malefic or adverse planet in the terminology of the cited commentary.", "${page124}#printed-page-116-verse-17"),
                TajikaGlossaryEntry("Shubha graha", "A benefic planet in the terminology of the cited commentary.", "${page124}#printed-page-116-verse-18"),
                TajikaGlossaryEntry("Varshesha", "The annual year lord referred to in the cited commentary; selected material covers only the Sun subsection.", "${page112}#printed-page-104-verse-11"),
            )
            return TajikaKnowledgePack(knowledgePack, sources, chunks, glossary, CHECKSUM, "CC BY-SA 4.0 (adapted transcription); source scan public domain in India and US", ATTRIBUTION)
        }

        private fun rule(id: String, topic: String, fact: String, value: String, result: String, key: String, meaning: String, inputs: List<String>, page: Int, verse: String): KnowledgeRule {
            val url = when (page) { 112 -> page112; 122 -> page122; else -> page124 }
            val printed = when (page) { 112 -> 104; 122 -> 114; else -> 116 }
            val requirements = inputs.map { "input:$it" }
            return KnowledgeRule(
                ruleId = id, tradition = "TAJIKA", topic = topic,
                conditions = listOf(AstroEventCondition(fact, AstroEventComparison.EQUALS, value, "input:$fact")),
                evidenceRequirements = requirements, interpretationKey = key, sourceId = TRANSCRIPTION_SOURCE_ID,
                sourceVersion = "revisions:378434,378444,378446", sourcePage = "$page (printed p.$printed), verse $verse",
                licenseStatus = "VERIFIED_CC_BY_SA_4_0", packVersion = VERSION,
                checksum = when (id) {
                    "TAJIKA_MUNTHA_H3_001" -> "777b4cfcbd7a4e2353310271ad44f173a425d02a7cddd5ff4c49681209f7709a"
                    "TAJIKA_MUNTHA_H4_001" -> "1a6369d73c85d9c58793258b8de1a46530b76aadab37ba3033a7af79dd4f2feb"
                    "TAJIKA_MUNTHA_H5_001" -> "2fe637eee3c6ba960e445f645a9e53c2614f0a7b69b170f5068233d26084145c"
                    "TAJIKA_MUNTHA_H6_001" -> "cc91ec56168f3e400a9aad87a992fddaedb72058b13f7755baecc890c10ec5c2"
                    "TAJIKA_MUNTHA_AFFLICTION_001" -> "67fa21363be5b4ab03b13799f19aec3a7481e72332d8e9239c1d62925df3566d"
                    "TAJIKA_MUNTHA_BENEFIC_001" -> "69dee0192d1249c22007c5768aeed77433cb91a22391ed9950610d508828154f"
                    "TAJIKA_VARSHESHA_SUN_STRONG_001" -> "12b23102d26f5d4a7b7ebc2768e4e25f0d2220c96180774ba264384e05216352"
                    else -> "3baebf63ef869dc355b3b931bf6679e837b0d7550f1ad570f75803e4e1141fa7"
                },
                traditionId = "TAJIKA", tags = listOf("TAJIKA", topic.uppercase()), inputs = inputs,
                resultType = result, sourceRefs = listOf("$url#printed-page-$printed-verse-$verse"), rightsStatus = "VERIFIED",
            )
        }

        private fun sha(value: String): String = com.aynvora.core.garudapuran.GarudaChecksumVerifier.calculateSha256(value.encodeToByteArray())
    }
}
