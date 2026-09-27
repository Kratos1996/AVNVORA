package com.aynvora.qa.core.sentinel

import com.aynvora.qa.core.models.QaActionContract
import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaConfidence
import com.aynvora.qa.core.models.QaDenylist
import com.aynvora.qa.core.models.QaExpectedOutcome
import com.aynvora.qa.core.models.QaStateSnapshot
import com.aynvora.qa.core.report.QaSessionReporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InteractionSentinelTest {

    @Test
    fun testActionIdentityFormatting() {
        val actionId = QaActionId.of("numerology", "form", "button", "calculate", "chaldean")
        assertEquals("NUMEROLOGY_FORM_BUTTON_CALCULATE_CHALDEAN", actionId.identifier)
        assertEquals("NUMEROLOGY_FORM_BUTTON_CALCULATE_CHALDEAN", actionId.toString())
    }

    @Test
    fun testDangerousActionDenylist() {
        assertTrue(
            QaDenylist.isDangerous(
                QaActionId.of(
                    "account",
                    "settings",
                    "btn",
                    "delete_profile"
                )
            )
        )
        assertTrue(QaDenylist.isDangerous(QaActionId.of("auth", "profile", "btn", "logout")))
        assertTrue(QaDenylist.isDangerous(QaActionId.of("billing", "checkout", "btn", "purchase")))
        assertTrue(
            QaDenylist.isDangerous(
                QaActionId.of(
                    "data",
                    "settings",
                    "btn",
                    "clear_all_records"
                )
            )
        )
        assertFalse(
            QaDenylist.isDangerous(
                QaActionId.of(
                    "dashboard",
                    "home",
                    "btn",
                    "open_numerology"
                )
            )
        )
        assertFalse(
            QaDenylist.isDangerous(
                QaActionId.of(
                    "localization",
                    "sheet",
                    "item",
                    "select_hindi"
                )
            )
        )
    }

    @Test
    fun testSuccessfulNavigationAction() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.DASHBOARD_OPEN_FEATURE

        val pre = QaStateSnapshot(
            route = "dashboard",
            screen = "CoreFeatureDashboard",
            timestampMs = 1000L,
        )
        sentinel.startAction(actionId, "TAP", pre, 1000L)

        val post = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyScreen",
            timestampMs = 1150L,
        )
        val result = sentinel.completeAction(actionId, post, 1150L)

        assertEquals(QaClassification.ACTION_SUCCESS, result.classification)
        assertEquals(QaConfidence.HIGH, result.confidence)
        assertNull(result.failureCapsule)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSingleNoOpDetection() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.NUMEROLOGY_CALCULATE

        val pre = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyScreen",
            semanticFingerprint = "name=John,dob=1990-01-01",
            timestampMs = 2000L,
        )
        sentinel.startAction(actionId, "TAP", pre, 2000L)

        // Post state is identical, no events emitted
        val post = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyScreen",
            semanticFingerprint = "name=John,dob=1990-01-01",
            timestampMs = 2300L,
        )
        val result = sentinel.completeAction(actionId, post, 2300L)

        assertEquals(QaClassification.ACTION_NO_OP, result.classification)
        assertEquals(QaConfidence.HIGH, result.confidence)
        assertNotNull(result.failureCapsule)
        assertEquals(1, result.repeatedCount)
    }

    @Test
    fun testRepeatedNoOpDetection() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.of("numerology", "sheet", "option", "select_tradition")

        val state = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyTraditionSheet",
            isSheetOpen = true,
            timestampMs = 3000L,
        )

        // Tap 1
        sentinel.startAction(actionId, "TAP", state, 3000L)
        val res1 = sentinel.completeAction(actionId, state.copy(timestampMs = 3200L), 3200L)
        assertEquals(QaClassification.ACTION_NO_OP, res1.classification)

        // Tap 2: same state, same action, no result
        sentinel.startAction(actionId, "TAP", state, 3300L)
        val res2 = sentinel.completeAction(actionId, state.copy(timestampMs = 3500L), 3500L)
        assertEquals(QaClassification.ACTION_REPEATED_NO_OP, res2.classification)
        assertEquals(QaConfidence.HIGH, res2.confidence)
        assertEquals(2, res2.repeatedCount)
        val capsule1 = res2.failureCapsule
        assertNotNull(capsule1)
        assertEquals(2, capsule1.repeatedCount)
    }

    @Test
    fun testLoadingHangDetection() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.NUMEROLOGY_OPEN_AI

        val pre = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyResult",
            isLoading = false,
            timestampMs = 4000L,
        )
        sentinel.startAction(actionId, "TAP", pre, 4000L)
        sentinel.recordLoading(actionId)

        // After 3500ms, still loading
        val post = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyResult",
            isLoading = true,
            timestampMs = 7500L,
        )
        val result = sentinel.completeAction(actionId, post, 7500L)

        assertEquals(QaClassification.ACTION_LOADING_HANG, result.classification)
        assertEquals(QaConfidence.HIGH, result.confidence)
        assertNotNull(result.failureCapsule)
    }

    @Test
    fun testExceptionCorrelation() {
        val sentinel = InteractionSentinel()
        val actionId = QaActionId.of("tarot", "draw", "card", "flip")

        val pre = QaStateSnapshot(
            route = "tarot",
            screen = "TarotScreen",
            timestampMs = 8000L,
        )
        sentinel.startAction(actionId, "TAP", pre, 8000L)
        sentinel.recordError(
            actionId,
            "IllegalStateException: Card already drawn",
            "Stack trace at TarotViewModel.kt:42"
        )

        val post = QaStateSnapshot(
            route = "tarot",
            screen = "TarotScreen",
            hasError = true,
            timestampMs = 8100L,
        )
        val result = sentinel.completeAction(actionId, post, 8100L)

        assertEquals(QaClassification.ACTION_ERROR, result.classification)
        assertEquals(QaConfidence.HIGH, result.confidence)
        val capsule2 = result.failureCapsule
        assertNotNull(capsule2)
        assertTrue(capsule2.stackTrace?.contains("TarotViewModel.kt:42") == true)
    }

    @Test
    fun testContractMatchingAndAllowedNoOp() {
        val copyActionId = QaActionId.of("numerology", "result", "icon", "copy_summary")
        val contract = QaActionContract(
            actionId = copyActionId,
            expectedOutcomes = listOf(QaExpectedOutcome.NoVisibleChangeAllowed),
            allowedNoOp = true,
        )
        val sentinel = InteractionSentinel(mapOf(copyActionId to contract))

        val state = QaStateSnapshot(
            route = "numerology",
            screen = "NumerologyResult",
            timestampMs = 9000L,
        )
        sentinel.startAction(copyActionId, "TAP", state, 9000L)
        val result = sentinel.completeAction(copyActionId, state.copy(timestampMs = 9100L), 9100L)

        assertEquals(QaClassification.ACTION_SUCCESS, result.classification)
        assertNull(result.failureCapsule)
    }

    @Test
    fun testSessionReportGeneration() {
        val sentinel = InteractionSentinel()
        val action1 = QaActionId.DASHBOARD_OPEN_FEATURE
        val action2 = QaActionId.NUMEROLOGY_CALCULATE

        val s1 = QaStateSnapshot("dashboard", "Dashboard", timestampMs = 100L)
        sentinel.startAction(action1, "TAP", s1, 100L)
        sentinel.completeAction(
            action1,
            s1.copy(route = "numerology", screen = "Numerology", timestampMs = 200L),
            200L
        )

        val s2 = QaStateSnapshot("numerology", "Numerology", timestampMs = 300L)
        sentinel.startAction(action2, "TAP", s2, 300L)
        sentinel.completeAction(action2, s2.copy(timestampMs = 400L), 400L)

        val summary = QaSessionReporter.generateSummary(
            sessionId = "TEST-SESSION-001",
            timestampMs = 500L,
            screensVisited = sentinel.getVisitedScreens(),
            history = sentinel.getHistory(),
            failureCapsules = sentinel.getFailureCapsules(),
        )

        assertEquals(2, summary.totalActions)
        assertEquals(1, summary.totalSuccess)
        assertEquals(1, summary.totalNoOp)
        assertEquals(1, summary.failureCapsules.size)

        val json = QaSessionReporter.toJson(summary)
        assertTrue(json.contains("TEST-SESSION-001"))
        assertTrue(json.contains("calculate_button"))
        assertTrue(json.contains("submit"))

        val markdown = QaSessionReporter.toMarkdown(summary)
        assertTrue(markdown.contains("# AYNVORA INTERACTION SENTINEL VERIFICATION REPORT"))
        assertTrue(markdown.contains("NUMEROLOGY_FORM_CALCULATE_BUTTON_SUBMIT"))
        assertTrue(markdown.contains("Failure Capsule"))
    }
}
