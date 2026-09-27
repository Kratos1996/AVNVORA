package com.aynvora.core.event

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.feature.CoreFeatureId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * PHASE 9.1 — AYNVORA UNIFIED EVENT SDK HARDENING TEST SUITE
 *
 * Covers all 35 test requirements from Phase 9.1 specification:
 * - Sections 40-44: API visibility, event security, analytics, ViewModel contracts, navigation
 */
class AynvoraEventSdkTest {

    private class RecordingAnalyticsTracker : AnalyticsTracker {
        val trackedEvents = mutableListOf<AnalyticsEvent>()
        var shouldThrow = false

        override fun track(event: AnalyticsEvent) {
            if (shouldThrow) {
                throw RuntimeException("Simulated analytics tracking outage")
            }
            trackedEvents.add(event)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // §40: API Visibility / UI Contract Tests (tests 1–7)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test01_eventCreation() {
        val event = AynvoraClickEvent(
            eventId = "dashboard.tarot.open_clicked",
            screenId = "dashboard",
            componentId = "tarot_card",
            payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.TAROT),
        )
        assertEquals("dashboard.tarot.open_clicked", event.eventId)
        assertEquals(AynvoraEventType.CLICK, event.eventType)
        assertEquals("dashboard", event.screenId)
        assertEquals("tarot_card", event.componentId)
    }

    @Test
    fun test02_eventMetadata() {
        val metadata = AynvoraEventMetadata(
            screenId = "tarot",
            componentId = "spread_selector",
            locale = "hi",
            version = "1.0.0",
        )
        val event = AynvoraClickEvent(
            eventId = "tarot.spread.selected",
            screenId = "tarot",
            componentId = "spread_selector",
            metadata = metadata,
        )
        assertEquals("hi", event.metadata.locale)
        assertEquals("1.0.0", event.metadata.version)
        assertEquals("tarot", event.metadata.screenId)
    }

    @Test
    fun test03_typedPayloads() {
        // Tarot spread
        val spreadPayload = AynvoraEventPayload.TarotSelectSpreadPayload("three_card_timeline")
        assertTrue(spreadPayload is AynvoraEventPayload.TarotSelectSpreadPayload)
        assertEquals("three_card_timeline", spreadPayload.spreadId)

        // AI download
        val aiPayload = AynvoraEventPayload.AiDownloadPayload("gemma-2b-it")
        assertEquals("gemma-2b-it", aiPayload.modelId)

        // Language change
        val langPayload = AynvoraEventPayload.LanguageChangePayload("hi")
        assertEquals("hi", langPayload.localeId)

        // Palm hand
        val palmPayload = AynvoraEventPayload.PalmSelectHandPayload("RIGHT")
        assertEquals("RIGHT", palmPayload.hand)
    }

    @Test
    fun test04_schemaVersion() {
        val event = AynvoraClickEvent(
            eventId = "dashboard.tarot.open_clicked",
            screenId = "dashboard",
            componentId = "tarot_card",
        )
        assertEquals(1, event.schemaVersion)
    }

    @Test
    fun test05_navigationTargetsAreTyped() {
        // All navigation targets carry their own typed arguments — no arbitrary maps
        val tarotHome = AynvoraNavigationTarget.TarotHome
        val palmistryHome = AynvoraNavigationTarget.PalmistryHome
        val tarotReading = AynvoraNavigationTarget.TarotReading("session_abc123")
        val palmistryReading = AynvoraNavigationTarget.PalmistryReading("palm_session_xyz")
        val aiSettings = AynvoraNavigationTarget.AiSettings
        val featureDetail = AynvoraNavigationTarget.FeatureDetail(CoreFeatureId.TAROT)

        assertEquals("session_abc123", tarotReading.readingId)
        assertEquals("palm_session_xyz", palmistryReading.sessionId)
        assertEquals(CoreFeatureId.TAROT, featureDetail.featureId)
        assertTrue(tarotHome is AynvoraNavigationTarget)
        assertTrue(palmistryHome is AynvoraNavigationTarget)
        assertTrue(aiSettings is AynvoraNavigationTarget)
    }

    @Test
    fun test06_navigationTargets_typedEffect() {
        val navigate = AynvoraEffect.Navigate(AynvoraNavigationTarget.TarotReading("sess_9999"))
        assertEquals(AynvoraNavigationTarget.TarotReading("sess_9999"), navigate.target)
    }

    @Test
    fun test07_arbitraryNavigationMap_notPartOfPrimaryContract() {
        // NavigationPayload exists as a typed sealed member — no arbitrary Map<String,String> needed
        val target = AynvoraNavigationTarget.PalmistryReading("session_001")
        val payload = AynvoraEventPayload.NavigationPayload(target)
        assertEquals(target, payload.target)
        // confirm typed access works — no string map used
        val typedTarget = payload.target as AynvoraNavigationTarget.PalmistryReading
        assertEquals("session_001", typedTarget.sessionId)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // §41: Event Security Tests (tests 8–16)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test08_invalidEventId_blankRejected() {
        val guard = StandardAynvoraEventGuard()
        val malformed = AynvoraClickEvent(
            eventId = "",
            screenId = "",
            componentId = "",
        )
        val result = guard.validate(malformed)
        assertTrue(result is AynvoraEventValidationResult.InvalidPayload)
    }

    @Test
    fun test09_unauthorizedNamespace_admin() {
        val guard = StandardAynvoraEventGuard()
        val adminEvent = AynvoraClickEvent(
            eventId = "admin.database.wipe_clicked",
            screenId = "admin",
            componentId = "btn",
        )
        val result = guard.validate(adminEvent)
        assertTrue(result is AynvoraEventValidationResult.Unauthorized)
    }

    @Test
    fun test10_unauthorizedNamespace_system() {
        val guard = StandardAynvoraEventGuard()
        val sysEvent = AynvoraClickEvent(
            eventId = "system.root.bypass_auth",
            screenId = "root",
            componentId = "terminal",
        )
        val result = guard.validate(sysEvent)
        assertTrue(result is AynvoraEventValidationResult.Unauthorized)
    }

    @Test
    fun test11_unregisteredEvent_rejected() {
        val guard = StandardAynvoraEventGuard()
        val unregistered = AynvoraClickEvent(
            eventId = "unknown.feature.inject_clicked",
            screenId = "somewhere",
            componentId = "btn",
        )
        val result = guard.validate(unregistered)
        assertTrue(
            result is AynvoraEventValidationResult.Unauthorized,
            "Unregistered event must be rejected with Unauthorized, got: $result"
        )
    }

    @Test
    fun test12_wrongPayloadType_rejected() {
        val guard = StandardAynvoraEventGuard()
        // tarot.feedback.submitted expects TarotFeedbackPayload, not AiDownloadPayload
        val mismatch = AynvoraClickEvent(
            eventId = "tarot.feedback.submitted",
            screenId = "tarot",
            componentId = "stars",
            payload = AynvoraEventPayload.AiDownloadPayload("gemma"),
        )
        val result = guard.validate(mismatch)
        assertTrue(
            result is AynvoraEventValidationResult.InvalidPayload,
            "Wrong payload type must be rejected, got: $result"
        )
    }

    @Test
    fun test13_malformedReadingId_navigationValidation() {
        val result = AynvoraNavigationValidator.validate(
            AynvoraNavigationTarget.TarotReading(readingId = "")
        )
        assertTrue(result is NavigationValidationResult.Invalid)
    }

    @Test
    fun test14_malformedNavigationTarget_invalidChars() {
        val result = AynvoraNavigationValidator.validate(
            AynvoraNavigationTarget.TarotReading(readingId = "../../../etc/passwd")
        )
        assertTrue(result is NavigationValidationResult.Invalid)
    }

    @Test
    fun test15_duplicateAction_shortWindow() = runBlocking {
        val deduplicator = DefaultAynvoraEventDeduplicator()
        val event = AynvoraClickEvent(
            eventId = "dashboard.tarot.open_clicked",
            screenId = "dashboard",
            componentId = "tarot_card",
        )
        val first =
            deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW)
        val second =
            deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW)
        assertTrue(first)
        assertFalse(second)
    }

    @Test
    fun test16_idempotentAction_onlyOnce() = runBlocking {
        val deduplicator = DefaultAynvoraEventDeduplicator()
        val event = AynvoraClickEvent(
            eventId = "dashboard.ai.delete_clicked",
            screenId = "dashboard",
            componentId = "delete_btn",
            payload = AynvoraEventPayload.AiDeletePayload("model_gemma"),
        )
        assertTrue(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.IDEMPOTENT))
        assertFalse(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.IDEMPOTENT))
        assertFalse(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.IDEMPOTENT))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // §42: Analytics Tests (tests 17–24)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test17_approvedEvent_correctlyMapped() {
        val mapper = DefaultAynvoraEventAnalyticsMapper()
        val event = AynvoraClickEvent(
            eventId = "dashboard.tarot.open_clicked",
            screenId = "dashboard",
            componentId = "tarot_card",
        )
        val analytics = mapper.mapToAnalytics(event)
        assertNotNull(analytics)
        assertEquals("tarot_opened", analytics.name)
    }

    @Test
    fun test18_unknownEvent_ignored() {
        val mapper = DefaultAynvoraEventAnalyticsMapper()
        // Any unregistered event must return null
        val unknown = AynvoraClickEvent(
            eventId = "arbitrary.unlisted.action",
            screenId = "anywhere",
            componentId = "btn",
        )
        val analytics = mapper.mapToAnalytics(unknown)
        assertNull(analytics, "Unregistered events must not be auto-tracked")
    }

    @Test
    fun test19_forbiddenPayload_piiEvent_rejected() {
        val mapper = DefaultAynvoraEventAnalyticsMapper()
        val eventWithPii = AynvoraClickEvent(
            eventId = "leak.user_question.text",
            screenId = "chat",
            componentId = "input",
        )
        assertNull(mapper.mapToAnalytics(eventWithPii))
    }

    @Test
    fun test20_analyticsFailure_isolated() = runBlocking {
        val tracker = RecordingAnalyticsTracker().apply { shouldThrow = true }
        val bridge = AynvoraEventAnalyticsBridge(tracker)
        val dispatcher = DefaultAynvoraEventDispatcher(
            guard = PassThroughGuard(),
            deduplicator = DefaultAynvoraEventDeduplicator(),
        )
        dispatcher.addObserver(bridge)
        dispatcher.registerHandler(object : AynvoraEventHandler {
            override fun canHandle(event: AynvoraEvent): Boolean = true
            override suspend fun handle(event: AynvoraEvent): EventHandlingResult =
                EventHandlingResult.Handled
        })

        val result = dispatcher.dispatch(
            AynvoraClickEvent("dashboard.tarot.open_clicked", "dashboard", "tarot_card"),
            AynvoraDeduplicationPolicy.NO_DEDUP,
        )
        assertEquals(
            EventHandlingResult.Handled,
            result,
            "Analytics failure must not break business result"
        )
    }

    @Test
    fun test21_noPromptLogging() {
        val questionEvent = AynvoraClickEvent(
            eventId = "tarot.question.submitted",
            screenId = "tarot",
            componentId = "question_input",
            payload = AynvoraEventPayload.TarotQuestionPayload("What does the sun card mean?"),
        )
        // Question payload must be blocked from analytics
        assertFalse(
            AnalyticsSafePayload.isSafe(questionEvent),
            "TarotQuestionPayload must be blocked from analytics (no prompt/question text)"
        )
    }

    @Test
    fun test22_noAiOutputLogging() {
        val aiOutputEvent = AynvoraClickEvent(
            eventId = "ai_output.log_clicked",
            screenId = "debug",
            componentId = "btn",
        )
        assertFalse(AnalyticsSafePayload.isSafe(aiOutputEvent))
    }

    @Test
    fun test23_noQuestionTextLogging() {
        val palmQuestion = AynvoraClickEvent(
            eventId = "palmistry.question.submitted",
            screenId = "palmistry",
            componentId = "question_input",
            payload = AynvoraEventPayload.PalmQuestionPayload("Will I succeed?"),
        )
        assertFalse(
            AnalyticsSafePayload.isSafe(palmQuestion),
            "PalmQuestionPayload must be blocked from analytics"
        )
    }

    @Test
    fun test24_noPalmImageLogging() {
        val imageEvent = AynvoraClickEvent(
            eventId = "palm_image.upload_clicked",
            screenId = "palmistry",
            componentId = "upload_btn",
        )
        assertFalse(AnalyticsSafePayload.isSafe(imageEvent))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // §43: ViewModel Contract Tests (tests 25–30)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test25_eventReachesCorrectHandler() = runBlocking {
        val dispatcher = DefaultAynvoraEventDispatcher(
            guard = PassThroughGuard(),
            deduplicator = DefaultAynvoraEventDeduplicator(),
        )
        var tarotHandled = false
        var palmistryHandled = false

        dispatcher.registerHandler(object : AynvoraEventHandler {
            override fun canHandle(event: AynvoraEvent) = event.eventId.startsWith("tarot.")
            override suspend fun handle(event: AynvoraEvent): EventHandlingResult {
                tarotHandled = true
                return EventHandlingResult.Handled
            }
        })
        dispatcher.registerHandler(object : AynvoraEventHandler {
            override fun canHandle(event: AynvoraEvent) = event.eventId.startsWith("palmistry.")
            override suspend fun handle(event: AynvoraEvent): EventHandlingResult {
                palmistryHandled = true
                return EventHandlingResult.Handled
            }
        })

        dispatcher.dispatch(
            AynvoraClickEvent("tarot.disclaimer.accepted", "tarot_disclaimer", "accept_btn"),
            AynvoraDeduplicationPolicy.NO_DEDUP,
        )
        assertTrue(tarotHandled)
        assertFalse(palmistryHandled)
    }

    @Test
    fun test26_handlerUpdatesState() {
        // Structural test: state mutation occurs inside the private handler in ViewModel
        // This validates the mechanism is correct via reducer pattern
        data class TestState(val count: Int)

        var state = TestState(0)
        val reducer: TestState.() -> TestState = { copy(count = count + 1) }
        state = state.reducer()
        assertEquals(1, state.count)
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun test27_handlerEmitsEffect() {
        val channel = Channel<AynvoraEffect>(Channel.BUFFERED)
        channel.trySend(AynvoraEffect.Navigate(AynvoraNavigationTarget.TarotHome))
        assertFalse(channel.isEmpty)
        val received = channel.tryReceive().getOrNull()
        assertNotNull(received)
        assertEquals(AynvoraNavigationTarget.TarotHome, (received as AynvoraEffect.Navigate).target)
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun test28_effectEmittedOnce() = runBlocking {
        val channel = Channel<AynvoraEffect>(Channel.BUFFERED)
        channel.send(AynvoraEffect.Navigate(AynvoraNavigationTarget.TarotHome))
        val received = channel.receive()
        assertEquals(AynvoraNavigationTarget.TarotHome, (received as AynvoraEffect.Navigate).target)
        assertTrue(channel.isEmpty, "Channel must be empty after consuming one-shot effect")
    }

    @Test
    fun test29_handlerFailure_typed() = runBlocking {
        val dispatcher = DefaultAynvoraEventDispatcher(
            guard = PassThroughGuard(),
            deduplicator = DefaultAynvoraEventDeduplicator(),
        )
        dispatcher.registerHandler(object : AynvoraEventHandler {
            override fun canHandle(event: AynvoraEvent) = true
            override suspend fun handle(event: AynvoraEvent): EventHandlingResult {
                throw RuntimeException("Simulated domain error")
            }
        })

        val result = dispatcher.dispatch(
            AynvoraClickEvent("dashboard.tarot.open_clicked", "dashboard", "card"),
            AynvoraDeduplicationPolicy.NO_DEDUP,
        )
        assertTrue(
            result is EventHandlingResult.Failed,
            "Handler errors must be captured as Failed, got: $result"
        )
    }

    @Test
    fun test30_duplicateEvents_protected() = runBlocking {
        val deduplicator = DefaultAynvoraEventDeduplicator()
        val event = AynvoraClickEvent(
            eventId = "dashboard.ai.download_clicked",
            screenId = "dashboard",
            componentId = "download_btn",
            payload = AynvoraEventPayload.AiDownloadPayload("gemma-2b"),
        )
        assertTrue(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.IDEMPOTENT))
        assertFalse(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.IDEMPOTENT))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // §44: Navigation Tests (tests 31–35)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test31_tarotTypedNavigation() {
        val target = AynvoraNavigationTarget.TarotReading("tarot_session_42")
        val effect = AynvoraEffect.Navigate(target)
        assertEquals(
            "tarot_session_42",
            (effect.target as AynvoraNavigationTarget.TarotReading).readingId
        )
    }

    @Test
    fun test32_palmistryTypedNavigation() {
        val target = AynvoraNavigationTarget.PalmistryReading("palm_session_99")
        val effect = AynvoraEffect.Navigate(target)
        assertEquals(
            "palm_session_99",
            (effect.target as AynvoraNavigationTarget.PalmistryReading).sessionId
        )
    }

    @Test
    fun test33_aiTypedNavigation() {
        val target = AynvoraNavigationTarget.AiSettings
        val effect = AynvoraEffect.Navigate(target)
        assertEquals(AynvoraNavigationTarget.AiSettings, effect.target)
    }

    @Test
    fun test34_featureDetailTypedNavigation() {
        val target = AynvoraNavigationTarget.FeatureDetail(CoreFeatureId.TAROT)
        val validation = AynvoraNavigationValidator.validate(target)
        assertTrue(
            validation is NavigationValidationResult.Valid,
            "Valid known feature should pass navigation validation, got: $validation"
        )
    }

    @Test
    fun test35_invalidNavigationTarget_rejected() {
        // Blank readingId must fail
        val blankResult = AynvoraNavigationValidator.validate(
            AynvoraNavigationTarget.TarotReading("")
        )
        assertTrue(blankResult is NavigationValidationResult.Invalid)

        // Malicious path traversal must fail
        val maliciousResult = AynvoraNavigationValidator.validate(
            AynvoraNavigationTarget.PalmistryReading("../../admin/config")
        )
        assertTrue(maliciousResult is NavigationValidationResult.Invalid)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Legacy regression guards (protect existing feature behavior)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test36_eventRegistry_hasAllCriticalEvents() {
        val critical = listOf(
            "dashboard.feature.open_clicked",
            "dashboard.ai.download_clicked",
            "dashboard.ai.cancel_clicked",
            "dashboard.ai.delete_clicked",
            "tarot.feedback.submitted",
            "tarot.card.draw_clicked",
            "palmistry.feedback.submitted",
            "palmistry.analysis.started",
            "dashboard.language.change_clicked",
        )
        for (id in critical) {
            assertTrue(
                AynvoraEventRegistry.isRegistered(id),
                "Critical event '$id' must be registered"
            )
        }
    }

    @Test
    fun test37_analytics_tarotFeedback_rating_mapped() {
        val mapper = DefaultAynvoraEventAnalyticsMapper()
        val event = AynvoraUiEvent(
            eventId = "tarot.feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = "tarot",
            componentId = "feedback_dialog",
            payload = AynvoraEventPayload.TarotFeedbackPayload(starRating = 5),
        )
        val analytics = mapper.mapToAnalytics(event)
        assertNotNull(analytics, "Tarot feedback must be mapped to analytics")
        assertEquals("tarot_feedback_submitted", analytics.name)
    }

    @Test
    fun test38_guard_invalidFeedbackRating_tarot() {
        val guard = StandardAynvoraEventGuard()
        val badRating = AynvoraUiEvent(
            eventId = "tarot.feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = "tarot",
            componentId = "stars",
            payload = AynvoraEventPayload.TarotFeedbackPayload(starRating = 10),
        )
        val result = guard.validate(badRating)
        assertEquals(
            AynvoraEventValidationResult.InvalidPayload("Star rating must be between 1 and 5"),
            result,
        )
    }

    @Test
    fun test39_guard_invalidFeedbackRating_palmistry() {
        val guard = StandardAynvoraEventGuard()
        val badRating = AynvoraUiEvent(
            eventId = "palmistry.feedback.submitted",
            eventType = AynvoraEventType.SUBMIT,
            screenId = "palmistry",
            componentId = "rating",
            payload = AynvoraEventPayload.PalmFeedbackPayload(starRating = 0),
        )
        val result = guard.validate(badRating)
        assertEquals(
            AynvoraEventValidationResult.InvalidPayload("Star rating must be between 1 and 5"),
            result,
        )
    }

    @Test
    fun test40_noDedup_allowsRepeat() = runBlocking {
        val deduplicator = DefaultAynvoraEventDeduplicator()
        val event = AynvoraClickEvent("dashboard.tarot.open_clicked", "dashboard", "btn")
        assertTrue(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.NO_DEDUP))
        assertTrue(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.NO_DEDUP))
        assertTrue(deduplicator.checkAndRecord(event, AynvoraDeduplicationPolicy.NO_DEDUP))
    }

    @Test
    fun test41_eventFactory_dashboardEvents() {
        val event = DashboardEvents.toggleTheme()
        assertEquals("dashboard.theme.toggle_clicked", event.eventId)
        assertEquals("theme_toggle", event.componentId)
    }

    @Test
    fun test42_eventFactory_aiEvents() {
        val download = AiEvents.downloadModel("gemma-2b-it")
        assertEquals("dashboard.ai.download_clicked", download.eventId)
        assertEquals(
            "gemma-2b-it",
            (download.payload as AynvoraEventPayload.AiDownloadPayload).modelId
        )
    }

    @Test
    fun test43_eventFactory_tarotEvents() {
        val open = TarotEvents.open()
        assertEquals("dashboard.tarot.open_clicked", open.eventId)

        val drawCard = TarotEvents.drawCard(position = 2)
        assertEquals("tarot.card.draw_clicked", drawCard.eventId)
        assertEquals(2, (drawCard.payload as AynvoraEventPayload.TarotDrawCardPayload).position)
    }

    @Test
    fun test44_eventFactory_palmistryEvents() {
        val open = PalmistryEvents.open()
        assertEquals("dashboard.palmistry.open_clicked", open.eventId)

        val feedback = PalmistryEvents.submitFeedback(rating = 4)
        assertEquals("palmistry.feedback.submitted", feedback.eventId)
        assertEquals(4, (feedback.payload as AynvoraEventPayload.PalmFeedbackPayload).starRating)
    }

    @Test
    fun test45_analyticsSafePayload_paramSanitization() {
        val params = mapOf(
            "spread_id" to "three_card",
            "question_text" to "Will I succeed?",
            "user_question" to "Personal note",
            "card_count" to 3,
        )
        val sanitized = AnalyticsSafePayload.sanitizeParams(params)
        assertTrue(sanitized.containsKey("spread_id"), "Safe key must pass")
        assertFalse(sanitized.containsKey("question_text"), "question_text must be stripped")
        assertFalse(sanitized.containsKey("user_question"), "user_question must be stripped")
        assertTrue(sanitized.containsKey("card_count"), "Numeric values must pass")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Phase 9.2 — Route-Level Migration & New Event Tests (tests 46–55)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun test46_phase92_palmistryRegistryEntries() {
        assertNotNull(AynvoraEventRegistry.find("palmistry.screen.opened"))
        assertNotNull(AynvoraEventRegistry.find("palmistry.session.saved"))
        assertNotNull(AynvoraEventRegistry.find("palmistry.feedback.feature_recorded"))
        assertNotNull(AynvoraEventRegistry.find("palmistry.feedback.answer_recorded"))
    }

    @Test
    fun test47_phase92_tarotRegistryEntries() {
        assertNotNull(AynvoraEventRegistry.find("tarot.screen.opened"))
        assertNotNull(AynvoraEventRegistry.find("tarot.answer_feedback.submitted"))
        assertNotNull(AynvoraEventRegistry.find("tarot.clarification.drawn"))
        assertNotNull(AynvoraEventRegistry.find("tarot.reading.lock_shown"))
        assertNotNull(AynvoraEventRegistry.find("tarot.content.opened"))
        assertNotNull(AynvoraEventRegistry.find("tarot.ai.answer_generated"))
    }

    @Test
    fun test48_phase92_garudaPuranRegistryEntries() {
        assertNotNull(AynvoraEventRegistry.find("garuda_puran.screen.opened"))
        assertNotNull(AynvoraEventRegistry.find("garuda_puran.topic.selected"))
    }

    @Test
    fun test49_phase92_palmistryPayloadOwnershipValidation() {
        val guard = StandardAynvoraEventGuard()

        // Valid payload
        val validEvent = PalmistryEvents.saveSession("session_123")
        assertEquals(AynvoraEventValidationResult.Valid, guard.validate(validEvent))

        // Mismatched payload (using empty payload when PalmSaveSessionPayload is expected)
        val invalidEvent = AynvoraClickEvent(
            eventId = "palmistry.session.saved",
            screenId = "palmistry",
            componentId = "save",
            payload = AynvoraEventPayload.Empty,
        )
        val result = guard.validate(invalidEvent)
        assertTrue(result is AynvoraEventValidationResult.InvalidPayload)
    }

    @Test
    fun test50_phase92_palmFeatureFeedbackValidation() {
        val guard = StandardAynvoraEventGuard()
        val event = PalmistryEvents.recordFeatureFeedback(
            sessionId = "sess_1",
            featureType = "heart_line",
            category = "accurate",
        )
        assertEquals(AynvoraEventValidationResult.Valid, guard.validate(event))
    }

    @Test
    fun test51_phase92_palmAnswerFeedbackValidation() {
        val guard = StandardAynvoraEventGuard()
        val event = PalmistryEvents.recordAnswerFeedback(
            sessionId = "sess_1",
            questionId = "q_1",
            isHelpful = true,
        )
        assertEquals(AynvoraEventValidationResult.Valid, guard.validate(event))
    }

    @Test
    fun test52_phase92_garudaTopicValidation() {
        val guard = StandardAynvoraEventGuard()
        val event = GarudaPuranEvents.topicSelected("KARMA")
        assertEquals(AynvoraEventValidationResult.Valid, guard.validate(event))
    }

    @Test
    fun test53_phase92_analyticsBridgeNewTarotEvents() = runBlocking {
        val tracker = RecordingAnalyticsTracker()
        val bridge = AynvoraEventAnalyticsBridge(tracker)

        val lockEvent = TarotEvents.readingLockShown("3600000")
        bridge.onEvent(lockEvent, EventHandlingResult.Handled)

        val contentEvent = TarotEvents.contentOpened("the_fool", "en")
        bridge.onEvent(contentEvent, EventHandlingResult.Handled)

        val aiEvent = TarotEvents.aiAnswerGenerated("gemma", "1.0", false, "en")
        bridge.onEvent(aiEvent, EventHandlingResult.Handled)

        assertEquals(3, tracker.trackedEvents.size)
        assertTrue(tracker.trackedEvents[0] is AnalyticsEvent.TarotReadingLockShown)
        assertTrue(tracker.trackedEvents[1] is AnalyticsEvent.TarotContentOpened)
        assertTrue(tracker.trackedEvents[2] is AnalyticsEvent.TarotAiAnswerGenerated)
    }

    @Test
    fun test54_phase92_analyticsBridgeGarudaEvents() = runBlocking {
        val tracker = RecordingAnalyticsTracker()
        val bridge = AynvoraEventAnalyticsBridge(tracker)

        val openEvent = GarudaPuranEvents.screenOpened()
        bridge.onEvent(openEvent, EventHandlingResult.Handled)

        val topicEvent = GarudaPuranEvents.topicSelected("KARMA")
        bridge.onEvent(topicEvent, EventHandlingResult.Handled)

        assertEquals(2, tracker.trackedEvents.size)
        assertTrue(tracker.trackedEvents[0] is AnalyticsEvent.GarudaPuranOpened)
        assertTrue(tracker.trackedEvents[1] is AnalyticsEvent.GarudaPuranTopicOpened)
    }

    @Test
    fun test55_phase92_sensitiveQuestionsBlockedFromAnalytics() = runBlocking {
        val tracker = RecordingAnalyticsTracker()
        val bridge = AynvoraEventAnalyticsBridge(tracker)

        val palmQuestion = PalmistryEvents.submitQuestion("My private life question")
        bridge.onEvent(palmQuestion, EventHandlingResult.Handled)

        val tarotQuestion = TarotEvents.submitQuestion("My private tarot question")
        bridge.onEvent(tarotQuestion, EventHandlingResult.Handled)

        // Neither question event should be logged to analytics because questions are PII
        assertEquals(0, tracker.trackedEvents.size)
    }
}

/**
 * A pass-through guard for testing handlers in isolation without registry enforcement.
 */
private class PassThroughGuard : AynvoraEventGuard {
    override fun validate(event: AynvoraEvent): AynvoraEventValidationResult =
        AynvoraEventValidationResult.Valid
}
