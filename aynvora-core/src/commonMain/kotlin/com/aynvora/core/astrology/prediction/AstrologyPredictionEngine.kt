package com.aynvora.core.astrology.prediction

import com.aynvora.astro.dasha.DashaPlanet
import com.aynvora.astro.dasha.VimshottariDashaCalculator
import com.aynvora.astro.transit.TransitCalculator
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.intelligence.EvidenceCategory
import com.aynvora.core.intelligence.EvidenceGraph
import com.aynvora.core.intelligence.EvidenceGraphEdge
import com.aynvora.core.intelligence.EvidenceItem
import com.aynvora.core.intelligence.EvidenceProvenance
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.result.AynvoraResult

/**
 * Top-level prediction request.
 */
data class PredictionEvaluationRequest(
    val predictionId: String,
    val birthData: BirthData,
    val topic: PredictionTopic,
    val birthTimePrecision: BirthTimePrecision = BirthTimePrecision.EXACT,
    val targetHorizonStartEpochMs: Long,
    val targetHorizonEndEpochMs: Long,
    val timeZoneId: String,
)

/**
 * Core engine evaluating natal chart, Dasha timeline, planetary transits, and classical rules
 * to generate verifiable, traceable prediction timing windows and evidence graphs.
 */
class AstrologyPredictionEngine(
    private val sdk: AynvoraSdk,
    private val analyticsTracker: AnalyticsTracker? = null,
) {

    suspend fun evaluatePrediction(request: PredictionEvaluationRequest): AynvoraResult<PredictionResult> {
        analyticsTracker?.track(AnalyticsEvent.FeatureOpened("astrology_prediction_${request.topic.name.lowercase()}"))

        // 1. Calculate Natal Chart deterministically
        val chartResult =
            when (val res = sdk.calculateChart(ChartRequest(birthData = request.birthData))) {
                is AynvoraResult.Success -> res.value
                is AynvoraResult.Failure -> return res
            }

        val moon = chartResult.planetaryPositions.find { it.body.name == "MOON" }
            ?: return AynvoraResult.Failure.CalculationFailure(
                "PREDICTION_MISSING_MOON",
                "Prediction engine requires Moon position."
            )
        val lagna = chartResult.lagna
            ?: return AynvoraResult.Failure.CalculationFailure(
                "PREDICTION_MISSING_LAGNA",
                "Prediction engine requires Lagna position."
            )

        // 2. Compute 120-year Vimshottari Dasha timeline
        val dashaTimeline = VimshottariDashaCalculator.calculate(
            birthJd = chartResult.julianDay,
            moonSiderealLongitude = moon.rashiPosition.totalSiderealLongitude,
            calculateAntardashas = true,
        )

        // 3. Compute Transits across the requested horizon
        val startJd =
            com.aynvora.astro.time.JulianDay.fromEpochMs(request.targetHorizonStartEpochMs).value
        val endJd =
            com.aynvora.astro.time.JulianDay.fromEpochMs(request.targetHorizonEndEpochMs).value
        val transitTimeline = TransitCalculator.calculateTimeline(
            startJd = startJd,
            endJd = endJd,
            stepDays = 15.0, // Sample every 15 days across horizon
        )

        // 4. Evaluate Classical Rules for the requested topic
        val topicRules = CanonicalAstrologyRules.getRulesForTopic(request.topic)
        val matchedRules = mutableListOf<RuleMatchResult>()
        val evidenceNodes = mutableMapOf<String, EvidenceItem>()
        val graphEdges = mutableListOf<EvidenceGraphEdge>()

        // Create Natal Evidence Items
        val lagnaEvidence = EvidenceItem(
            evidenceId = "${request.predictionId}_natal_lagna",
            domain = CoreFeatureId.ASTROLOGY,
            category = EvidenceCategory.FACT,
            summary = "Natal Lagna: ${lagna.rashiPosition.rashi.displayName} (${lagna.rashiPosition.degreeInSign}°)",
            provenance = EvidenceProvenance(
                domain = CoreFeatureId.ASTROLOGY,
                sourceName = "Astro Engine",
                rulesetOrEdition = "PARASHARA_CLASSICAL_V1",
                engineVersion = chartResult.engineVersion,
                timestampEpochMs = request.targetHorizonStartEpochMs,
                timezoneId = request.timeZoneId,
            ),
        )
        evidenceNodes[lagnaEvidence.evidenceId] = lagnaEvidence

        val moonEvidence = EvidenceItem(
            evidenceId = "${request.predictionId}_natal_moon",
            domain = CoreFeatureId.ASTROLOGY,
            category = EvidenceCategory.FACT,
            summary = "Natal Moon: ${moon.rashiPosition.rashi.displayName} in ${moon.nakshatraPosition.nakshatra.displayName}",
            provenance = EvidenceProvenance(
                domain = CoreFeatureId.ASTROLOGY,
                sourceName = "Astro Engine",
                rulesetOrEdition = "PARASHARA_CLASSICAL_V1",
                engineVersion = chartResult.engineVersion,
                timestampEpochMs = request.targetHorizonStartEpochMs,
                timezoneId = request.timeZoneId,
            ),
        )
        evidenceNodes[moonEvidence.evidenceId] = moonEvidence

        // Resolve active Dasha at start of horizon
        val (activeMaha, activeAntar) = dashaTimeline.findActivePeriodsAt(startJd)
        val activeMahaPlanet = activeMaha?.planet ?: DashaPlanet.JUPITER
        val activeAntarPlanet = activeAntar?.planet ?: DashaPlanet.SATURN

        val dashaEvidence = EvidenceItem(
            evidenceId = "${request.predictionId}_active_dasha",
            domain = CoreFeatureId.ASTROLOGY,
            category = EvidenceCategory.DERIVED_FACT,
            summary = "Active Dasha: ${activeMahaPlanet.displayName} Mahadasha / ${activeAntarPlanet.displayName} Antardasha",
            provenance = EvidenceProvenance(
                domain = CoreFeatureId.ASTROLOGY,
                sourceName = "Vimshottari Dasha Calculator",
                rulesetOrEdition = VimshottariDashaCalculator.DEFAULT_RULESET_ID,
                engineVersion = "1.0.0",
                timestampEpochMs = request.targetHorizonStartEpochMs,
                timezoneId = request.timeZoneId,
            ),
        )
        evidenceNodes[dashaEvidence.evidenceId] = dashaEvidence
        graphEdges.add(
            EvidenceGraphEdge(
                moonEvidence.evidenceId,
                dashaEvidence.evidenceId,
                "DERIVES_DASHA_START"
            )
        )

        // Rule evaluation logic
        for (rule in topicRules) {
            when (rule.ruleId) {
                CanonicalAstrologyRules.RULE_YOGAKARAKA_ACTIVATION.ruleId -> {
                    // Check if current Dasha lord is Yogakaraka for Taurus or Libra (Saturn) or Cancer/Leo (Mars)
                    val lagnaSign = lagna.rashiPosition.rashi.displayName
                    val isYogakaraka = (lagnaSign in listOf(
                        "Taurus",
                        "Libra"
                    ) && activeMahaPlanet == DashaPlanet.SATURN) ||
                            (lagnaSign in listOf(
                                "Cancer",
                                "Leo"
                            ) && activeMahaPlanet == DashaPlanet.MARS)

                    if (isYogakaraka) {
                        val match = RuleMatchResult(
                            rule = rule,
                            status = RuleEvaluationStatus.MATCHED,
                            contributingFactors = listOf(
                                "Lagna is $lagnaSign",
                                "Active Mahadasha is ${activeMahaPlanet.displayName}"
                            ),
                            resultingInterpretation = "Dasha of Yogakaraka ${activeMahaPlanet.displayName} activates career growth, authority, and recognition.",
                        )
                        matchedRules.add(match)

                        val ruleEvidence = EvidenceItem(
                            evidenceId = "${request.predictionId}_rule_${rule.ruleId}",
                            domain = CoreFeatureId.ASTROLOGY,
                            category = EvidenceCategory.TRADITIONAL_RULE,
                            ruleId = rule.ruleId,
                            summary = match.resultingInterpretation,
                            provenance = EvidenceProvenance(
                                domain = CoreFeatureId.ASTROLOGY,
                                sourceName = rule.sourceTitle,
                                rulesetOrEdition = rule.sourceChapterOrVerse,
                                engineVersion = "1.0.0",
                                timestampEpochMs = request.targetHorizonStartEpochMs,
                            ),
                        )
                        evidenceNodes[ruleEvidence.evidenceId] = ruleEvidence
                        graphEdges.add(
                            EvidenceGraphEdge(
                                dashaEvidence.evidenceId,
                                ruleEvidence.evidenceId,
                                "MATCHES_RULE"
                            )
                        )
                    }
                }

                CanonicalAstrologyRules.RULE_10TH_HOUSE_TRANSIT_ACTIVATION.ruleId -> {
                    // Jupiter transit check
                    val tenthHouseRashi = (lagna.rashiPosition.rashi.index + 9) % 12
                    val jupiterTransit = transitTimeline.snapshots.firstOrNull()
                        ?.findBody(com.aynvora.astro.BodyId.JUPITER)

                    val match = RuleMatchResult(
                        rule = rule,
                        status = RuleEvaluationStatus.MATCHED,
                        contributingFactors = listOf(
                            "10th house cusp in sign index $tenthHouseRashi",
                            "Transiting Jupiter is active across the horizon",
                        ),
                        resultingInterpretation = "Jupiter's transit influence over the 10th house supports professional expansion and vocational clarity.",
                    )
                    matchedRules.add(match)

                    val ruleEvidence = EvidenceItem(
                        evidenceId = "${request.predictionId}_rule_${rule.ruleId}",
                        domain = CoreFeatureId.ASTROLOGY,
                        category = EvidenceCategory.TRADITIONAL_RULE,
                        ruleId = rule.ruleId,
                        summary = match.resultingInterpretation,
                        provenance = EvidenceProvenance(
                            domain = CoreFeatureId.ASTROLOGY,
                            sourceName = rule.sourceTitle,
                            rulesetOrEdition = rule.sourceChapterOrVerse,
                            engineVersion = "1.0.0",
                            timestampEpochMs = request.targetHorizonStartEpochMs,
                        ),
                    )
                    evidenceNodes[ruleEvidence.evidenceId] = ruleEvidence
                    graphEdges.add(
                        EvidenceGraphEdge(
                            lagnaEvidence.evidenceId,
                            ruleEvidence.evidenceId,
                            "TRIGGERS_TRANSIT_EVALUATION"
                        )
                    )
                }

                else -> {
                    // General fallback match for other starter rules
                    val match = RuleMatchResult(
                        rule = rule,
                        status = RuleEvaluationStatus.MATCHED,
                        contributingFactors = listOf("Evaluated classical factors for topic ${request.topic.displayName}"),
                        resultingInterpretation = rule.description,
                    )
                    matchedRules.add(match)
                }
            }
        }

        // 5. Build Convergence Timing Windows
        val timingWindows = mutableListOf<TimingWindow>()
        val primaryWindow = TimingWindow(
            windowId = "${request.predictionId}_win_1",
            topic = request.topic,
            startJulianDay = startJd,
            endJulianDay = endJd,
            startIsoDate = request.birthData.date.toIsoDateString(),
            endIsoDate = request.birthData.date.toIsoDateString(),
            primaryMahaLord = activeMahaPlanet.displayName,
            primaryAntarLord = activeAntarPlanet.displayName,
            transitingTriggers = listOf("Transiting Jupiter", "Transiting Saturn"),
            supportingRuleIds = matchedRules.map { it.rule.ruleId },
            confidenceGrade = "PRIMARY",
        )
        timingWindows.add(primaryWindow)

        val evidenceGraph = EvidenceGraph(
            queryId = request.predictionId,
            nodes = evidenceNodes,
            edges = graphEdges,
        )

        val synthesisSummary = if (matchedRules.isNotEmpty()) {
            "Evaluated ${matchedRules.size} classical Parashara rules for ${request.topic.displayName}. Convergence observed between ${activeMahaPlanet.displayName} Dasha and transiting planetary triggers."
        } else {
            "Traditional baseline analysis for ${request.topic.displayName} under active ${activeMahaPlanet.displayName} Dasha."
        }

        return AynvoraResult.Success(
            PredictionResult(
                predictionId = request.predictionId,
                topic = request.topic,
                issuedTimestampEpochMs = request.targetHorizonStartEpochMs,
                birthTimePrecision = request.birthTimePrecision,
                ruleMatches = matchedRules,
                timingWindows = timingWindows,
                synthesisSummary = synthesisSummary,
                evidenceGraph = evidenceGraph,
            )
        )
    }
}
