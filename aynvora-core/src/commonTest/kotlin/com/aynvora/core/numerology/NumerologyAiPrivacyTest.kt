package com.aynvora.core.numerology

import com.aynvora.core.ai.AiPrivacyClass
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.event.AynvoraEventType
import com.aynvora.core.event.AynvoraUiEvent
import com.aynvora.core.event.DefaultAynvoraEventAnalyticsMapper
import com.aynvora.core.result.AynvoraResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Privacy and Zero-PII Test.
 *
 * Verifies that:
 * - Evidence processed by AI is STRICT_LOCAL_ONLY.
 * - No user question text, answer text, personal name, or birth date is present in analytics events.
 * - Telemetry contains only safe technical metrics.
 */
class NumerologyAiPrivacyTest {

    private val connector = NumerologyFeatureDataConnector()

    @Test
    fun testEvidenceItems_AreStrictlyLocalOnly() {
        val req = NumerologyRequest(
            birthDay = 11,
            birthMonth = 7,
            birthYear = 1996,
            fullName = "ISHANT",
            rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        )
        val res = (NumerologyCalculationEngine.calculate(req) as AynvoraResult.Success).value
        val context = connector.buildGroundingContext(res)

        assertEquals(AiPrivacyClass.STRICT_LOCAL_ONLY, context.privacyClass)
        assertTrue(context.evidenceItems.all { it.privacyClass == AiPrivacyClass.STRICT_LOCAL_ONLY })
    }

    @Test
    fun testAnalyticsBridge_ZeroPiiInPayloads() {
        val mapper = DefaultAynvoraEventAnalyticsMapper()

        // 1. Question Submitted Event
        val questionEvent = AynvoraUiEvent(
            eventId = "numerology.ai.question_submitted",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "btn_submit_ai_question",
            payload = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload(
                rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
                category = "EXPLAIN_RESULT",
            ),
        )

        val aEvent1 = mapper.mapToAnalytics(questionEvent)
        assertNotNull(aEvent1)
        assertEquals("numerology_ai_question_submitted", aEvent1.name)
        assertEquals(
            NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            aEvent1.params[AnalyticsEvent.Param.RULESET_ID]
        )
        assertEquals("EXPLAIN_RESULT", aEvent1.params["question_category"])

        // 2. Explanation Completed Event
        val completedEvent = AynvoraUiEvent(
            eventId = "numerology.ai.explanation_completed",
            eventType = AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "ai_explanation_engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationCompletedPayload(
                rulesetId = NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
                category = "EXPLAIN_RESULT",
                fallbackUsed = false,
                modelId = "gemma-2b-it-cpu",
            ),
        )

        val aEvent2 = mapper.mapToAnalytics(completedEvent)
        assertNotNull(aEvent2)
        assertEquals("numerology_ai_explanation_completed", aEvent2.name)
        assertEquals(
            NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            aEvent2.params[AnalyticsEvent.Param.RULESET_ID]
        )
        assertEquals("EXPLAIN_RESULT", aEvent2.params["question_category"])
        assertEquals("false", aEvent2.params["fallback_used"])
        assertEquals("gemma-2b-it-cpu", aEvent2.params["model_id"])
    }
}
