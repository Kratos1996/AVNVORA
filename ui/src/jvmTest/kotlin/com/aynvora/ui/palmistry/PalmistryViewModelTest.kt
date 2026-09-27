package com.aynvora.ui.palmistry

import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmFeedback
import com.aynvora.core.palmistry.PalmFinding
import com.aynvora.core.palmistry.PalmLineFinding
import com.aynvora.core.palmistry.PalmLineType
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmReadingStatus
import com.aynvora.core.palmistry.PalmSessionRepository
import com.aynvora.core.palmistry.PalmShape
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PalmistryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePalmSessionRepository : PalmSessionRepository {
        val sessions = mutableListOf<PalmReadingSession>()

        override suspend fun saveSession(session: PalmReadingSession): AynvoraResult<Unit> {
            val idx = sessions.indexOfFirst { it.id == session.id }
            if (idx >= 0) sessions[idx] = session else sessions.add(session)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getSession(sessionId: String): AynvoraResult<PalmReadingSession?> =
            AynvoraResult.Success(sessions.firstOrNull { it.id == sessionId })

        override suspend fun getLatestSession(): AynvoraResult<PalmReadingSession?> =
            AynvoraResult.Success(sessions.firstOrNull())

        override suspend fun getAllSessions(): AynvoraResult<List<PalmReadingSession>> =
            AynvoraResult.Success(sessions.toList())

        override suspend fun deleteSession(sessionId: String): AynvoraResult<Unit> {
            sessions.removeAll { it.id == sessionId }
            return AynvoraResult.Success(Unit)
        }

        override suspend fun recordFeatureFeedback(feedback: PalmFeatureFeedback): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)

        override suspend fun recordAnswerFeedback(feedback: PalmAnswerFeedback): AynvoraResult<Unit> =
            AynvoraResult.Success(Unit)
    }

    private fun createTestSession(id: String): PalmReadingSession {
        val finding = PalmFinding(
            handType = HandType.RIGHT,
            lines = listOf(
                PalmLineFinding(
                    lineType = PalmLineType.LIFE_LINE,
                    clarityScore = 0.85f,
                    curvatureScore = 0.6f,
                    lengthCategory = "Long",
                    breaksDetected = false,
                )
            ),
            mounts = emptyList(),
            shape = PalmShape.RECTANGULAR,
        )
        return PalmReadingSession(
            id = id,
            startedAtEpochMs = 1000L,
            handType = HandType.RIGHT,
            finding = finding,
            status = PalmReadingStatus.COMPLETED,
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialPalmistryState() = runTest {
        val repo = FakePalmSessionRepository()
        val vm = PalmistryViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(HandType.RIGHT, state.selectedHand)
        assertNull(state.currentSession)
        assertFalse(state.isLoading)
        assertTrue(state.pastSessions.isEmpty())
    }

    @Test
    fun testHandSelection() = runTest {
        val repo = FakePalmSessionRepository()
        val vm = PalmistryViewModel(repo)
        advanceUntilIdle()

        vm.onEvent(PalmistryUiEvent.HandSelected(HandType.LEFT))
        advanceUntilIdle()
        assertEquals(HandType.LEFT, vm.uiState.value.selectedHand)

        vm.onEvent(PalmistryUiEvent.HandSelected(HandType.RIGHT))
        advanceUntilIdle()
        assertEquals(HandType.RIGHT, vm.uiState.value.selectedHand)
    }

    @Test
    fun testAnalysisLoadingState() = runTest {
        val repo = FakePalmSessionRepository()
        val vm = PalmistryViewModel(repo)
        advanceUntilIdle()

        vm.onEvent(PalmistryUiEvent.StartAnalysis(HandType.RIGHT))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLoading)

        vm.onEvent(PalmistryUiEvent.AnalysisCompleted(HandType.RIGHT))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun testSaveAndSelectPastSession() = runTest {
        val repo = FakePalmSessionRepository()
        val vm = PalmistryViewModel(repo)
        advanceUntilIdle()

        val session = createTestSession("palm_001")
        vm.onEvent(PalmistryUiEvent.SaveSession(session))
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.pastSessions.size)
        assertEquals("palm_001", vm.uiState.value.currentSession?.id)

        vm.onEvent(PalmistryUiEvent.SelectPastSession("palm_001"))
        advanceUntilIdle()
        assertEquals("palm_001", vm.uiState.value.currentSession?.id)
    }

    @Test
    fun testStressRapidPalmistryEventsNoCrash() = runTest {
        val repo = FakePalmSessionRepository()
        val vm = PalmistryViewModel(repo)
        advanceUntilIdle()

        val events: List<PalmistryUiEvent> = listOf(
            PalmistryUiEvent.ScreenOpened,
            PalmistryUiEvent.HandSelected(HandType.LEFT),
            PalmistryUiEvent.HandSelected(HandType.RIGHT),
            PalmistryUiEvent.StartAnalysis(HandType.RIGHT),
            PalmistryUiEvent.AnalysisCompleted(HandType.RIGHT),
            PalmistryUiEvent.ReportRequested,
            PalmistryUiEvent.PdfRequested,
            PalmistryUiEvent.ShareRequested,
            PalmistryUiEvent.CloseClicked,
            PalmistryUiEvent.BackClicked,
            PalmistryUiEvent.FeedbackSubmitted(5, "Accurate observation"),
        )

        for (i in 0 until 50) {
            vm.onEvent(events[i % events.size])
        }
        advanceUntilIdle()

        assertNotNull(vm.uiState.value)
    }
}
