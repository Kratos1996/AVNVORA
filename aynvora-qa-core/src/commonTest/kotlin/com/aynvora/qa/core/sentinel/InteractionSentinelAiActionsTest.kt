package com.aynvora.qa.core.sentinel

import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaConfidence
import com.aynvora.qa.core.models.QaStateSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InteractionSentinelAiActionsTest {

    @Test
    fun testAiActionIdentifiersAreRegistered() {
        assertEquals("AI_DASHBOARD_CARD_OPEN", QaActionId.AI_OPEN.identifier)
        assertEquals("AI_CARD_LINK_VIEW_DETAILS", QaActionId.AI_VIEW_MODEL_DETAILS.identifier)
        assertEquals("AI_SETUP_BUTTON_DOWNLOAD", QaActionId.AI_DOWNLOAD_MODEL.identifier)
        assertEquals("AI_SETUP_BUTTON_DELETE", QaActionId.AI_DELETE_MODEL.identifier)
        assertEquals("AI_DETAILS_BUTTON_LOAD", QaActionId.AI_LOAD_MODEL.identifier)
        assertEquals("AI_DETAILS_BUTTON_UNLOAD", QaActionId.AI_UNLOAD_MODEL.identifier)
        assertEquals("AI_INTERACTION_BUTTON_ASK", QaActionId.AI_ASK_QUESTION.identifier)
        assertEquals("AI_SETUP_BUTTON_RETRY", QaActionId.AI_RETRY.identifier)
        assertEquals("AI_INTERACTION_PICKER_CHANGE_CONTEXT", QaActionId.AI_CHANGE_FEATURE_CONTEXT.identifier)
        assertEquals("AI_DETAILS_CLOSE_BUTTON_CLOSE", QaActionId.AI_CLOSE_DETAILS.identifier)
    }

    @Test
    fun testSentinelTracksAiActionExecutionFlow() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.AI_VIEW_MODEL_DETAILS

        val preSnapshot = QaStateSnapshot(
            route = "dashboard",
            screen = "CoreFeatureDashboard",
            timestampMs = 1000L,
        )
        sentinel.startAction(actionId, "TAP", preSnapshot, 1000L)

        val postSnapshot = QaStateSnapshot(
            route = "ai_system_details",
            screen = "AiDiagnosticScreen",
            timestampMs = 1050L,
        )
        val result = sentinel.completeAction(actionId, postSnapshot, 1050L)

        assertEquals(QaClassification.ACTION_SUCCESS, result.classification)
        assertEquals(QaConfidence.HIGH, result.confidence)
        assertNull(result.failureCapsule)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSentinelDetectsRepeatedTapNoOpOnAiActions() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.AI_LOAD_MODEL

        val state = QaStateSnapshot(
            route = "ai_system_details",
            screen = "AiDiagnosticScreen",
            timestampMs = 2000L,
        )

        // First tap: identical state -> ACTION_NO_OP
        sentinel.startAction(actionId, "TAP", state, 2000L)
        val res1 = sentinel.completeAction(actionId, state.copy(timestampMs = 2200L), 2200L)
        assertEquals(QaClassification.ACTION_NO_OP, res1.classification)
        assertEquals(1, res1.repeatedCount)

        // Second tap: same state again -> ACTION_REPEATED_NO_OP
        sentinel.startAction(actionId, "TAP", state, 2300L)
        val res2 = sentinel.completeAction(actionId, state.copy(timestampMs = 2500L), 2500L)
        assertEquals(QaClassification.ACTION_REPEATED_NO_OP, res2.classification)
        assertEquals(2, res2.repeatedCount)
        assertNotNull(res2.failureCapsule)
    }
}
