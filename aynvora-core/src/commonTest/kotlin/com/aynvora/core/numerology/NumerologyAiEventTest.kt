package com.aynvora.core.numerology

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.event.AynvoraEventRegistry
import com.aynvora.core.event.AynvoraEventType
import com.aynvora.core.event.AynvoraUiEvent
import com.aynvora.core.event.DefaultAynvoraEventAnalyticsMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Phase 10.6: Numerology AI Event SDK and Analytics Bridge Test.
 *
 * Verifies that all 7 Numerology AI events are registered and mapped correctly.
 */
class NumerologyAiEventTest {

    private val mapper = DefaultAynvoraEventAnalyticsMapper()

    @Test
    fun testEventRegistry_ContainsAllAiEventIds() {
        val expectedIds = listOf(
            "numerology.ai.open_clicked",
            "numerology.ai.question_submitted",
            "numerology.ai.explanation_started",
            "numerology.ai.explanation_completed",
            "numerology.ai.explanation_failed",
            "numerology.ai.fallback_used",
            "numerology.ai.clarification_requested",
        )

        for (id in expectedIds) {
            assertTrue(
                AynvoraEventRegistry.isRegistered(id),
                "Event ID $id must be registered in AynvoraEventRegistry"
            )
        }
    }

    @Test
    fun testAll7Events_MapToAnalyticsWithoutPii() {
        val rulesetId = NumerologyRuleset.CHALDEAN_CHEIRO_V1.id

        // 1. Open
        val openEvent = AynvoraUiEvent(
            "numerology.ai.open_clicked",
            AynvoraEventType.CLICK,
            screenId = "numerology",
            componentId = "btn",
            payload = AynvoraEventPayload.NumerologyAiOpenPayload(rulesetId)
        )
        val aOpen = mapper.mapToAnalytics(openEvent)
        assertNotNull(aOpen)
        assertEquals("numerology_ai_opened", aOpen.name)
        assertEquals(rulesetId, aOpen.params[AnalyticsEvent.Param.RULESET_ID])

        // 2. Question Submitted
        val qSub = AynvoraUiEvent(
            "numerology.ai.question_submitted",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "btn",
            payload = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload(
                rulesetId,
                "EXPLAIN_RESULT"
            )
        )
        val aSub = mapper.mapToAnalytics(qSub)
        assertNotNull(aSub)
        assertEquals("numerology_ai_question_submitted", aSub.name)
        assertEquals("EXPLAIN_RESULT", aSub.params["question_category"])

        // 3. Explanation Started
        val start = AynvoraUiEvent(
            "numerology.ai.explanation_started",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationStartedPayload(
                rulesetId,
                "EXPLAIN_RESULT"
            )
        )
        val aStart = mapper.mapToAnalytics(start)
        assertNotNull(aStart)
        assertEquals("numerology_ai_explanation_started", aStart.name)
        assertEquals(rulesetId, aStart.params[AnalyticsEvent.Param.RULESET_ID])

        // 4. Explanation Completed
        val comp = AynvoraUiEvent(
            "numerology.ai.explanation_completed",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationCompletedPayload(
                rulesetId,
                "EXPLAIN_RESULT",
                fallbackUsed = true,
                modelId = "local-slm"
            )
        )
        val aComp = mapper.mapToAnalytics(comp)
        assertNotNull(aComp)
        assertEquals("numerology_ai_explanation_completed", aComp.name)
        assertEquals("true", aComp.params["fallback_used"])
        assertEquals("local-slm", aComp.params["model_id"])

        // 5. Explanation Failed
        val fail = AynvoraUiEvent(
            "numerology.ai.explanation_failed",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "engine",
            payload = AynvoraEventPayload.NumerologyAiExplanationFailedPayload(rulesetId, "TIMEOUT")
        )
        val aFail = mapper.mapToAnalytics(fail)
        assertNotNull(aFail)
        assertEquals("numerology_ai_explanation_failed", aFail.name)
        assertEquals("TIMEOUT", aFail.params[AnalyticsEvent.Param.ERROR_CODE])

        // 6. Fallback Used
        val fallback = AynvoraUiEvent(
            "numerology.ai.fallback_used",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "engine",
            payload = AynvoraEventPayload.NumerologyAiFallbackUsedPayload(
                rulesetId,
                "MODEL_UNINSTALLED"
            )
        )
        val aFallback = mapper.mapToAnalytics(fallback)
        assertNotNull(aFallback)
        assertEquals("numerology_ai_fallback_used", aFallback.name)
        assertEquals("MODEL_UNINSTALLED", aFallback.params["fallback_reason"])

        // 7. Clarification Requested
        val clar = AynvoraUiEvent(
            "numerology.ai.clarification_requested",
            AynvoraEventType.FEATURE_ACTION,
            screenId = "numerology",
            componentId = "engine",
            payload = AynvoraEventPayload.NumerologyAiClarificationPayload(
                rulesetId,
                "AMBIGUOUS_TRADITION"
            )
        )
        val aClar = mapper.mapToAnalytics(clar)
        assertNotNull(aClar)
        assertEquals("numerology_ai_clarification_requested", aClar.name)
        assertEquals("AMBIGUOUS_TRADITION", aClar.params["question_category"])
    }
}
