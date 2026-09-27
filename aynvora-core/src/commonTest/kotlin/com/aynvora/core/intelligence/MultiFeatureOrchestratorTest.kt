package com.aynvora.core.intelligence

import com.aynvora.core.Aynvora
import com.aynvora.core.ai.DefaultAiToolRegistry
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.gita.GitaAuthor
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaSourceEdition
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MultiFeatureOrchestratorTest {

    private val sdk = Aynvora.create()
    private val toolRegistry = DefaultAiToolRegistry()

    private val fakeGitaRepository = object : GitaRepository {
        override suspend fun getChapters(): AynvoraResult<List<GitaChapter>> {
            return AynvoraResult.Success(emptyList())
        }

        override suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter> {
            return AynvoraResult.Success(
                GitaChapter(
                    chapterNumber = chapterNumber,
                    nameSanskrit = "साङ्ख्ययोग",
                    nameTranslation = "Sankhya Yoga",
                )
            )
        }

        override suspend fun getVerse(
            chapterNumber: Int,
            verseNumber: Int,
            language: String
        ): AynvoraResult<GitaVerse> {
            return AynvoraResult.Success(
                GitaVerse(
                    chapterNumber = 2,
                    verseNumber = 47,
                    sanskritDevenagari = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
                    transliterationIast = "karmaṇy-evādhikāras te mā phaleṣu kadācana",
                    translation = "You have a right to perform your prescribed duty, but you are not entitled to the fruits of action.",
                    sourceEdition = GitaSourceEdition(
                        editionId = "vedanta_standard",
                        title = "Bhagavad Gita Standard Edition",
                        translatorOrCommentator = "Swami Paramananda",
                        language = "en",
                    ),
                    themeTags = listOf("NISHKAMA_KARMA"),
                )
            )
        }

        override suspend fun getVersesForChapter(
            chapterNumber: Int,
            language: String
        ): AynvoraResult<List<GitaVerse>> {
            val verse = getVerse(2, 47, language) as AynvoraResult.Success
            return AynvoraResult.Success(listOf(verse.value))
        }

        override suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>> {
            return searchVersesByTheme(query, "en")
        }

        override suspend fun searchVersesByTheme(
            themeTag: String,
            language: String
        ): AynvoraResult<List<GitaVerse>> {
            val verse = getVerse(2, 47, language) as AynvoraResult.Success
            return AynvoraResult.Success(listOf(verse.value))
        }

        override suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>> {
            return AynvoraResult.Success(emptyList())
        }

        override suspend fun isSeeded(): Boolean = true

        override suspend fun getSeededVerseCount(): Int = 1
    }

    private val orchestrator = MultiFeatureOrchestrator(
        sdk = sdk,
        toolRegistry = toolRegistry,
        gitaRepository = fakeGitaRepository,
    )

    @Test
    fun testStandaloneAstrologyExecution() = runBlocking {
        val request = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "q_standalone_astro",
                intentType = IntelligenceIntentType.ASTROLOGY_CHART,
                requestedDomains = setOf(CoreFeatureId.ASTROLOGY),
                timeContext = TimeContext(
                    referenceTimestampEpochMs = 1716388400000L,
                    timezoneId = "Asia/Kolkata",
                ),
            ),
            userContext = UserContext(
                birthDateIso = "1990-05-15",
                birthTimeIso = "14:30:00",
                latitude = 28.6139,
                longitude = 77.2090,
                timezoneId = "Asia/Kolkata",
                selectedTradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
            ),
        )

        val result = orchestrator.orchestrate(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals("q_standalone_astro", response.queryId)
        assertEquals(GuidanceStatus.CALCULATED_FACT, response.status)
        assertEquals(2, response.evidenceBundle.items.size)
        assertTrue(response.evidenceBundle.items.all { it.domain == CoreFeatureId.ASTROLOGY })
        assertTrue(response.evidenceBundle.items.all { it.category == EvidenceCategory.FACT })
        assertEquals(0, response.conflictingFactors.size)

        // Verify provenance survives
        val lagnaEvidence = response.evidenceBundle.items.first { it.evidenceId.contains("lagna") }
        assertEquals("Aynvora Astro Engine", lagnaEvidence.provenance.sourceName)
        assertEquals(
            TraditionProfile.PARASHARA_CLASSICAL_V1.name,
            lagnaEvidence.provenance.rulesetOrEdition
        )
    }

    @Test
    fun testStandaloneGitaExecution() = runBlocking {
        val request = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "q_standalone_gita",
                intentType = IntelligenceIntentType.GITA_PHILOSOPHICAL_REFLECTION,
                requestedDomains = setOf(CoreFeatureId.GITA),
                timeContext = TimeContext(
                    referenceTimestampEpochMs = 1716388400000L,
                    timezoneId = "Asia/Kolkata",
                ),
            ),
            userContext = UserContext(), // No birth data needed for Gita
        )

        val result = orchestrator.orchestrate(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals(GuidanceStatus.CALCULATED_FACT, response.status)
        assertEquals(1, response.evidenceBundle.items.size)
        val item = response.evidenceBundle.items.first()
        assertEquals(CoreFeatureId.GITA, item.domain)
        assertEquals(EvidenceCategory.TRADITIONAL_RULE, item.category)
        assertTrue(item.summary.contains("You have a right to perform"))
    }

    @Test
    fun testMultiFeatureComposition_AstrologyAndGita() = runBlocking {
        val request = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "q_composed_astro_gita",
                intentType = IntelligenceIntentType.CROSS_DOMAIN_SYNTHESIS,
                requestedDomains = setOf(CoreFeatureId.ASTROLOGY, CoreFeatureId.GITA),
                timeContext = TimeContext(
                    referenceTimestampEpochMs = 1716388400000L,
                    timezoneId = "Asia/Kolkata",
                ),
            ),
            userContext = UserContext(
                birthDateIso = "1990-05-15",
                birthTimeIso = "14:30:00",
                latitude = 28.6139,
                longitude = 77.2090,
                timezoneId = "Asia/Kolkata",
                selectedTradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
            ),
        )

        val result = orchestrator.orchestrate(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals(3, response.evidenceBundle.items.size) // 2 astro + 1 gita
        assertEquals(
            setOf(CoreFeatureId.ASTROLOGY, CoreFeatureId.GITA),
            response.evidenceBundle.contributingDomains
        )

        // Traceability graph check
        assertEquals(3, response.evidenceGraph.nodes.size)
        assertFalse(response.evidenceGraph.edges.isEmpty())

        val gitaEvidence = response.evidenceBundle.items.first { it.domain == CoreFeatureId.GITA }
        val contributors = response.evidenceGraph.traceWhy(gitaEvidence.evidenceId)
        assertEquals(2, contributors.size) // both astro facts link to it
    }

    @Test
    fun testConflictingTraditionsPreservation_ParasharaAndLalKitab() = runBlocking {
        val request = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "q_conflicting_rules",
                intentType = IntelligenceIntentType.CROSS_DOMAIN_SYNTHESIS,
                requestedDomains = setOf(CoreFeatureId.ASTROLOGY, CoreFeatureId.LAL_KITAB),
                timeContext = TimeContext(
                    referenceTimestampEpochMs = 1716388400000L,
                    timezoneId = "Asia/Kolkata",
                ),
            ),
            userContext = UserContext(
                birthDateIso = "1990-05-15",
                birthTimeIso = "14:30:00",
                latitude = 28.6139,
                longitude = 77.2090,
                timezoneId = "Asia/Kolkata",
                selectedTradition = TraditionProfile.PARASHARA_CLASSICAL_V1,
            ),
        )

        val result = orchestrator.orchestrate(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals(GuidanceStatus.CONFLICTING_RULES, response.status)
        assertEquals(1, response.conflictingFactors.size)
        assertTrue(
            response.conflictingFactors.first().contains("Lal Kitab fixed-house mechanics diverge")
        )

        // Conflicting item preserved in evidence bundle
        val lkItem = response.evidenceBundle.items.find { it.domain == CoreFeatureId.LAL_KITAB }
        assertNotNull(lkItem)
        assertEquals(ConflictStatus.MODIFIER, lkItem.conflictStatus)
    }

    @Test
    fun testDataSufficiencyPreventsHallucination() = runBlocking {
        val requestMissingBirthData = IntelligenceRequest(
            queryContext = QueryContext(
                queryId = "q_missing_data",
                intentType = IntelligenceIntentType.ASTROLOGY_CHART,
                requestedDomains = setOf(CoreFeatureId.ASTROLOGY),
                timeContext = TimeContext(
                    referenceTimestampEpochMs = 1716388400000L,
                    timezoneId = "Asia/Kolkata",
                ),
            ),
            userContext = UserContext(
                // Missing birth date, time, coordinates
                birthDateIso = null,
                birthTimeIso = null,
                latitude = null,
                longitude = null,
            ),
        )

        val result = orchestrator.orchestrate(requestMissingBirthData)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals(GuidanceStatus.INSUFFICIENT_DATA, response.status)
        assertTrue(response.evidenceBundle.hasInsufficientData)
        assertTrue(response.primarySummary.contains("Insufficient data"))
        assertTrue(response.evidenceBundle.items.isEmpty())
    }

    @Test
    fun testFeatureCapabilityRegistryTruthfulness() {
        val astroCaps = FeatureCapabilityRegistry.getCapabilitiesForDomain(CoreFeatureId.ASTROLOGY)
        assertTrue(astroCaps.any { it.capabilityId == "astro_birth_chart" && it.status == CapabilityStatus.IMPLEMENTED })
        assertTrue(astroCaps.any { it.capabilityId == "astro_dashas" && it.status == CapabilityStatus.IMPLEMENTED })
        assertTrue(astroCaps.any { it.capabilityId == "astro_transits" && it.status == CapabilityStatus.IMPLEMENTED })
        assertTrue(astroCaps.any { it.capabilityId == "astro_panchang" && it.status == CapabilityStatus.IMPLEMENTED })

        val palmCaps = FeatureCapabilityRegistry.getCapabilitiesForDomain(CoreFeatureId.PALMISTRY)
        assertTrue(palmCaps.all { it.status == CapabilityStatus.FOUNDATION_ONLY })

        assertTrue(FeatureCapabilityRegistry.isCapabilityImplemented("astro_dashas"))
        assertTrue(FeatureCapabilityRegistry.isCapabilityImplemented("astro_birth_chart"))
        assertFalse(FeatureCapabilityRegistry.isCapabilityImplemented("palm_line_detection"))
    }
}
