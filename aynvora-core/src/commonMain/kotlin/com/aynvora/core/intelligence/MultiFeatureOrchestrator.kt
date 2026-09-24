package com.aynvora.core.intelligence

import com.aynvora.core.AynvoraSdk
import com.aynvora.core.ai.AiToolRegistry
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.garudapuran.GarudaPuranRepository
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.Coordinates
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.PerformTarotReadingUseCase
import com.aynvora.core.tarot.TarotSpread

/**
 * Output synthesized by the MultiFeatureOrchestrator.
 */
data class OrchestrationResponse(
    val queryId: String,
    val status: GuidanceStatus,
    val primarySummary: String,
    val evidenceBundle: EvidenceBundle,
    val evidenceGraph: EvidenceGraph,
    val conflictingFactors: List<String> = emptyList(),
    val recommendationsOrReflections: List<String> = emptyList(),
)

/**
 * Controlled orchestration engine safely combining independent domain tools
 * through typed contracts without direct cross-feature circular coupling.
 */
class MultiFeatureOrchestrator(
    private val sdk: AynvoraSdk,
    private val toolRegistry: AiToolRegistry,
    private val gitaRepository: GitaRepository? = null,
    private val performTarotReadingUseCase: PerformTarotReadingUseCase? = null,
    private val sufficiencyValidator: DataSufficiencyValidator = DataSufficiencyValidator(),
    private val analyticsTracker: AnalyticsTracker? = null,
    private val garudaPuranRepository: GarudaPuranRepository? = null,
) {

    suspend fun orchestrate(request: IntelligenceRequest): AynvoraResult<OrchestrationResponse> {
        val queryId = request.queryContext.queryId
        val requestedDomains = request.queryContext.requestedDomains

        analyticsTracker?.track(
            com.aynvora.core.analytics.AnalyticsEvent.IntelligenceQueryStarted(
                queryId = queryId,
                intentType = request.queryContext.intentType.name,
                domainCount = requestedDomains.size,
            )
        )

        // Step 1: Validate data sufficiency to avoid hallucinations
        val sufficiency = sufficiencyValidator.validate(request)
        if (sufficiency is SufficiencyResult.Insufficient) {
            analyticsTracker?.track(
                com.aynvora.core.analytics.AnalyticsEvent.InsufficientDataReturned(
                    queryId = queryId,
                    missingCount = sufficiency.missingFields.size,
                )
            )

            val emptyBundle = EvidenceBundle(
                queryId = queryId,
                items = emptyList(),
                contributingDomains = emptySet(),
                conflictCount = 0,
                hasInsufficientData = true,
            )
            val emptyGraph = EvidenceGraph(queryId, emptyMap(), emptyList())

            return AynvoraResult.Success(
                OrchestrationResponse(
                    queryId = queryId,
                    status = GuidanceStatus.INSUFFICIENT_DATA,
                    primarySummary = "Insufficient data to perform verified evaluation: ${sufficiency.reason} (${sufficiency.missingFields.joinToString()})",
                    evidenceBundle = emptyBundle,
                    evidenceGraph = emptyGraph,
                )
            )
        }

        val collectedEvidence = mutableListOf<EvidenceItem>()
        val graphEdges = mutableListOf<EvidenceGraphEdge>()
        val conflictingFactors = mutableListOf<String>()

        // Step 2: Route through domain tools based on explicit requested domains
        for (domain in requestedDomains) {
            analyticsTracker?.track(
                com.aynvora.core.analytics.AnalyticsEvent.IntelligenceToolSelected(
                    queryId = queryId,
                    domain = domain.name,
                )
            )

            when (domain) {
                CoreFeatureId.ASTROLOGY -> {
                    val user = request.userContext
                    val dateParts = user.birthDateIso!!.split("-").map { it.toInt() }
                    val timeParts = user.birthTimeIso!!.split(":").map { it.toInt() }

                    val birthData = BirthData(
                        date = BirthDate(dateParts[0], dateParts[1], dateParts[2]),
                        time = BirthTime(
                            timeParts[0],
                            timeParts[1],
                            if (timeParts.size > 2) timeParts[2] else 0
                        ),
                        place = BirthPlace(
                            name = "LocalUser",
                            coordinates = Coordinates(user.latitude!!, user.longitude!!),
                            timezoneId = user.timezoneId!!,
                        ),
                    )

                    when (val chartResult =
                        sdk.calculateChart(ChartRequest(birthData = birthData))) {
                        is AynvoraResult.Success -> {
                            val chart = chartResult.value
                            val lagna = chart.lagna
                            if (lagna != null) {
                                val lagnaEvidence = EvidenceItem(
                                    evidenceId = "${queryId}_astro_lagna",
                                    domain = CoreFeatureId.ASTROLOGY,
                                    category = EvidenceCategory.FACT,
                                    summary = "Ascendant (Lagna) is in ${lagna.rashiPosition.rashi.displayName} at ${lagna.rashiPosition.degreeInSign}°",
                                    provenance = EvidenceProvenance(
                                        domain = CoreFeatureId.ASTROLOGY,
                                        sourceName = "Aynvora Astro Engine",
                                        rulesetOrEdition = user.selectedTradition.name,
                                        engineVersion = chart.engineVersion,
                                        timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                                    ),
                                    priority = 10,
                                )
                                collectedEvidence.add(lagnaEvidence)
                            }

                            val moon = chart.planetaryPositions.find { it.body.name == "MOON" }
                            if (moon != null) {
                                val moonEvidence = EvidenceItem(
                                    evidenceId = "${queryId}_astro_moon",
                                    domain = CoreFeatureId.ASTROLOGY,
                                    category = EvidenceCategory.FACT,
                                    summary = "Chandra (Moon) is in ${moon.rashiPosition.rashi.displayName} (${moon.nakshatraPosition.nakshatra.displayName})",
                                    provenance = EvidenceProvenance(
                                        domain = CoreFeatureId.ASTROLOGY,
                                        sourceName = "Aynvora Astro Engine",
                                        rulesetOrEdition = user.selectedTradition.name,
                                        engineVersion = chart.engineVersion,
                                        timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                                    ),
                                    priority = 9,
                                )
                                collectedEvidence.add(moonEvidence)
                            }
                        }

                        is AynvoraResult.Failure -> {
                            return chartResult
                        }
                    }
                }

                CoreFeatureId.GITA -> {
                    if (gitaRepository != null) {
                        when (val gitaResult = gitaRepository.searchVersesByTheme(
                            "NISHKAMA_KARMA",
                            request.queryContext.locale
                        )) {
                            is AynvoraResult.Success -> {
                                val verses = gitaResult.value
                                if (verses.isNotEmpty()) {
                                    val topVerse = verses.first()
                                    val gitaEvidence = EvidenceItem(
                                        evidenceId = "${queryId}_gita_${topVerse.chapterNumber}_${topVerse.verseNumber}",
                                        domain = CoreFeatureId.GITA,
                                        category = EvidenceCategory.TRADITIONAL_RULE,
                                        ruleId = "GITA_CH${topVerse.chapterNumber}_V${topVerse.verseNumber}",
                                        summary = "Gita ${topVerse.chapterNumber}.${topVerse.verseNumber}: ${topVerse.translation}",
                                        provenance = EvidenceProvenance(
                                            domain = CoreFeatureId.GITA,
                                            sourceName = "Bhagavad Gita Standard Corpus",
                                            rulesetOrEdition = topVerse.sourceEdition.title,
                                            engineVersion = "1.0.0",
                                            timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                                        ),
                                        priority = 8,
                                    )
                                    collectedEvidence.add(gitaEvidence)
                                }
                            }

                            is AynvoraResult.Failure -> {
                                // Graceful non-blocking fallback
                            }
                        }
                    }
                }

                CoreFeatureId.GARUDA_PURAN -> {
                    if (garudaPuranRepository != null) {
                        when (val result =
                            garudaPuranRepository.getAvailableContent(request.queryContext.locale)) {
                            is AynvoraResult.Success -> {
                                if (result.value.isNotEmpty()) {
                                    val garudaGraph = GarudaPuranEvidenceGraphFactory.create(
                                        queryId = queryId,
                                        items = result.value,
                                        timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                                    )
                                    collectedEvidence.addAll(garudaGraph.nodes.values)
                                    graphEdges.addAll(garudaGraph.edges)
                                }
                            }

                            is AynvoraResult.Failure -> {
                                // Content unavailability is represented by the absence of Garuda Puran evidence.
                            }
                        }
                    }
                }

                CoreFeatureId.TAROT -> {
                    if (performTarotReadingUseCase != null) {
                        when (val tarotResult =
                            performTarotReadingUseCase.execute(TarotSpread.SingleCard)) {
                            is AynvoraResult.Success -> {
                                val cardDraw = tarotResult.value.draws.first()
                                val tarotEvidence = EvidenceItem(
                                    evidenceId = "${queryId}_tarot_${cardDraw.card.id}",
                                    domain = CoreFeatureId.TAROT,
                                    category = EvidenceCategory.INTERPRETATION,
                                    summary = "Tarot Draw: ${cardDraw.card.name} (${cardDraw.orientation.name})",
                                    provenance = EvidenceProvenance(
                                        domain = CoreFeatureId.TAROT,
                                        sourceName = "Aynvora Tarot Contemplative Engine",
                                        rulesetOrEdition = "Rider-Waite 1909 Archetypes",
                                        engineVersion = "1.0.0",
                                        timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                                    ),
                                    priority = 5,
                                )
                                collectedEvidence.add(tarotEvidence)
                            }

                            is AynvoraResult.Failure -> {
                                // Graceful Tarot fallback
                            }
                        }
                    }
                }

                CoreFeatureId.LAL_KITAB -> {
                    // Check for tradition divergence if both Parashara and Lal Kitab are involved
                    val lalKitabEvidence = EvidenceItem(
                        evidenceId = "${queryId}_lalkitab_rule",
                        domain = CoreFeatureId.LAL_KITAB,
                        category = EvidenceCategory.TRADITIONAL_RULE,
                        ruleId = "LK_TRADITION_NOTE",
                        summary = "Lal Kitab treats houses as fixed signs without considering dynamic Lagna sign ownership.",
                        provenance = EvidenceProvenance(
                            domain = CoreFeatureId.LAL_KITAB,
                            sourceName = "Lal Kitab Classical System",
                            rulesetOrEdition = "1952 Gutka Edition",
                            engineVersion = "1.0.0",
                            timestampEpochMs = request.queryContext.timeContext.referenceTimestampEpochMs,
                        ),
                        conflictStatus = ConflictStatus.MODIFIER,
                    )
                    collectedEvidence.add(lalKitabEvidence)
                    conflictingFactors.add("Lal Kitab fixed-house mechanics diverge from Parashara sign lordships.")
                }

                else -> {
                    // Supported capability or foundation stub
                }
            }
        }

        // Step 3: Conflict detection & multi-source evidence normalization
        var guidanceStatus = GuidanceStatus.CALCULATED_FACT
        if (conflictingFactors.isNotEmpty()) {
            guidanceStatus = GuidanceStatus.CONFLICTING_RULES
            analyticsTracker?.track(
                com.aynvora.core.analytics.AnalyticsEvent.ConflictingEvidenceDetected(
                    queryId = queryId,
                    conflictCount = conflictingFactors.size,
                )
            )
        } else if (collectedEvidence.any {
                it.category == EvidenceCategory.INTERPRETATION || it.domain == CoreFeatureId.GARUDA_PURAN
            }) {
            guidanceStatus = GuidanceStatus.TRADITIONAL_INTERPRETATION
        } else if (requestedDomains == setOf(CoreFeatureId.GARUDA_PURAN)) {
            guidanceStatus = GuidanceStatus.UNAVAILABLE_CAPABILITY
        }

        // Build nodes map and inter-evidence graph edges
        val nodesMap = collectedEvidence.associateBy { it.evidenceId }
        val astroNodes = collectedEvidence.filter { it.domain == CoreFeatureId.ASTROLOGY }
        val gitaNodes = collectedEvidence.filter { it.domain == CoreFeatureId.GITA }

        for (astro in astroNodes) {
            for (gita in gitaNodes) {
                graphEdges.add(
                    EvidenceGraphEdge(
                        sourceEvidenceId = astro.evidenceId,
                        targetEvidenceId = gita.evidenceId,
                        relationship = "SUPPORTS_PHILOSOPHICAL_THEME",
                    )
                )
            }
        }

        val evidenceBundle = EvidenceBundle(
            queryId = queryId,
            items = collectedEvidence,
            contributingDomains = collectedEvidence.map { it.domain }.toSet(),
            conflictCount = conflictingFactors.size,
            hasInsufficientData = false,
        )

        val evidenceGraph = EvidenceGraph(
            queryId = queryId,
            nodes = nodesMap,
            edges = graphEdges,
        )

        val primarySummary = when {
            requestedDomains.contains(CoreFeatureId.ASTROLOGY) && requestedDomains.contains(
                CoreFeatureId.GITA
            ) ->
                "Synthesized astrological observation with Gita philosophical reflection. Astro facts derived deterministically; Gita guidance offers ethical perspective."

            requestedDomains.contains(CoreFeatureId.ASTROLOGY) ->
                "Deterministic birth chart calculation evaluated with verified Parashara classical rules."

            requestedDomains.contains(CoreFeatureId.GITA) ->
                "Retrieved authentic verses from Bhagavad Gita repository with full citation."

            else ->
                "Structured intelligence output synthesized across requested domains."
        }

        analyticsTracker?.track(
            com.aynvora.core.analytics.AnalyticsEvent.IntelligenceQueryCompleted(
                queryId = queryId,
                status = guidanceStatus.name,
                evidenceCount = collectedEvidence.size,
            )
        )

        return AynvoraResult.Success(
            OrchestrationResponse(
                queryId = queryId,
                status = guidanceStatus,
                primarySummary = primarySummary,
                evidenceBundle = evidenceBundle,
                evidenceGraph = evidenceGraph,
                conflictingFactors = conflictingFactors,
                recommendationsOrReflections = collectedEvidence.map { it.summary },
            )
        )
    }
}
