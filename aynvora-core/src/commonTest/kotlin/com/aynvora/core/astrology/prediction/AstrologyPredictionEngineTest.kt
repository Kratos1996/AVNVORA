package com.aynvora.core.astrology.prediction

import com.aynvora.core.Aynvora
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.Coordinates
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AstrologyPredictionEngineTest {

    private val sdk = Aynvora.create()
    private val engine = AstrologyPredictionEngine(sdk)

    private val sampleBirthData = BirthData(
        date = BirthDate(year = 1990, month = 5, day = 15),
        time = BirthTime(hour = 14, minute = 30, second = 0),
        place = BirthPlace(
            name = "New Delhi, India",
            coordinates = Coordinates(latitude = 28.6139, longitude = 77.2090),
            timezoneId = "Asia/Kolkata",
        ),
    )

    @Test
    fun testPredictionEvaluation_CareerTopic_GeneratesTraceableEvidenceGraph() = runBlocking {
        val request = PredictionEvaluationRequest(
            predictionId = "pred_career_001",
            birthData = sampleBirthData,
            topic = PredictionTopic.CAREER,
            birthTimePrecision = BirthTimePrecision.EXACT,
            targetHorizonStartEpochMs = 1716388400000L, // May 2024
            targetHorizonEndEpochMs = 1747924400000L,   // May 2025
            timeZoneId = "Asia/Kolkata",
        )

        val result = engine.evaluatePrediction(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals("pred_career_001", response.predictionId)
        assertEquals(PredictionTopic.CAREER, response.topic)
        assertEquals(BirthTimePrecision.EXACT, response.birthTimePrecision)

        // Rule evaluation check
        assertFalse(response.ruleMatches.isEmpty())
        val firstMatch = response.ruleMatches.first()
        assertEquals(RuleEvaluationStatus.MATCHED, firstMatch.status)
        assertTrue(firstMatch.resultingInterpretation.isNotBlank())

        // Timing windows check
        assertEquals(1, response.timingWindows.size)
        val window = response.timingWindows.first()
        assertEquals("PRIMARY", window.confidenceGrade)
        assertTrue(window.primaryMahaLord.isNotBlank())
        assertTrue(window.primaryAntarLord.isNotBlank())
        assertFalse(window.transitingTriggers.isEmpty())

        // Evidence graph traceability check
        val graph = response.evidenceGraph
        assertFalse(graph.nodes.isEmpty())
        assertFalse(graph.edges.isEmpty())

        // Trace why: verifying that rule matches trace back to natal/dasha factors
        val ruleNodes =
            graph.nodes.values.filter { it.category == com.aynvora.core.intelligence.EvidenceCategory.TRADITIONAL_RULE }
        assertFalse(ruleNodes.isEmpty())
        val targetRule = ruleNodes.first()
        val contributingEvidence = graph.traceWhy(targetRule.evidenceId)
        assertFalse(contributingEvidence.isEmpty())
    }

    @Test
    fun testPredictionEvaluation_FinanceTopic() = runBlocking {
        val request = PredictionEvaluationRequest(
            predictionId = "pred_finance_001",
            birthData = sampleBirthData,
            topic = PredictionTopic.FINANCE,
            birthTimePrecision = BirthTimePrecision.ROUNDED,
            targetHorizonStartEpochMs = 1716388400000L,
            targetHorizonEndEpochMs = 1747924400000L,
            timeZoneId = "Asia/Kolkata",
        )

        val result = engine.evaluatePrediction(request)
        assertTrue(result is AynvoraResult.Success)

        val response = result.value
        assertEquals(PredictionTopic.FINANCE, response.topic)
        assertEquals(BirthTimePrecision.ROUNDED, response.birthTimePrecision)
        assertTrue(response.synthesisSummary.contains("Wealth & Prosperity"))
    }
}
