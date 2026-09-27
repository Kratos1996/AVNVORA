package com.aynvora.ui.numerology

import com.aynvora.core.numerology.NumerologyCalculationEngine
import com.aynvora.core.numerology.NumerologyHistoryEntry
import com.aynvora.core.numerology.NumerologyHistoryRepository
import com.aynvora.core.numerology.NumerologyRepository
import com.aynvora.core.numerology.NumerologyRequest
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
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
class NumerologyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeHistoryRepo = object : NumerologyHistoryRepository {
        private val list = mutableListOf<NumerologyHistoryEntry>()
        override suspend fun save(entry: NumerologyHistoryEntry): AynvoraResult<Unit> {
            list.add(entry)
            return AynvoraResult.Success(Unit)
        }

        override suspend fun getAll(): AynvoraResult<List<NumerologyHistoryEntry>> {
            return AynvoraResult.Success(list.toList())
        }

        override suspend fun clear(): AynvoraResult<Unit> {
            list.clear()
            return AynvoraResult.Success(Unit)
        }
    }

    private val fakeNumerologyRepo = object : NumerologyRepository {
        override suspend fun getProfile(
            birthDay: Int,
            birthMonth: Int,
            birthYear: Int,
            fullName: String?,
            rulesetId: String,
        ): AynvoraResult<NumerologyResult> {
            val req =
                NumerologyRequest(birthDay, birthMonth, birthYear, fullName, rulesetId = rulesetId)
            return calculate(req)
        }

        override suspend fun calculate(request: NumerologyRequest): AynvoraResult<NumerologyResult> {
            return NumerologyCalculationEngine.calculate(request)
        }
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
    fun testInitialCalculationLoadsDefaultState() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(NumerologyRuleset.CHALDEAN_CHEIRO_V1.id, state.selectedRuleset.id)
        assertEquals(NumerologyTab.CALCULATE, state.activeTab)
        assertNotNull(state.currentResult)
        assertFalse(state.isCalculating)
        assertNull(state.validationErrorKey)
    }

    @Test
    fun testTabSelection() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.SelectTab(NumerologyTab.COMPARE))
        advanceUntilIdle()
        assertEquals(NumerologyTab.COMPARE, vm.uiState.value.activeTab)

        vm.onEvent(NumerologyUiEvent.SelectTab(NumerologyTab.HISTORY))
        advanceUntilIdle()
        assertEquals(NumerologyTab.HISTORY, vm.uiState.value.activeTab)
    }

    @Test
    fun testRulesetSelection() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.SelectRuleset(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id, state.selectedRuleset.id)
        assertNotNull(state.currentResult)
        assertEquals(
            NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
            state.currentResult?.profile?.rulesetId
        )
    }

    @Test
    fun testValidationFailsOnInvalidDay() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.UpdateDay("42"))
        advanceUntilIdle()
        vm.onEvent(NumerologyUiEvent.Calculate)
        advanceUntilIdle()

        assertEquals("numerology.validation.invalid_day", vm.uiState.value.validationErrorKey)
    }

    @Test
    fun testValidationFailsOnInvalidMonth() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.UpdateMonth("15"))
        advanceUntilIdle()
        vm.onEvent(NumerologyUiEvent.Calculate)
        advanceUntilIdle()

        assertEquals("numerology.validation.invalid_month", vm.uiState.value.validationErrorKey)
    }

    @Test
    fun testScriptValidationOnHebrewGematria() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.SelectRuleset(NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id))
        advanceUntilIdle()
        vm.onEvent(NumerologyUiEvent.UpdateScriptText(""))
        advanceUntilIdle()
        vm.onEvent(NumerologyUiEvent.Calculate)
        advanceUntilIdle()

        assertEquals("numerology.validation.script_required", vm.uiState.value.validationErrorKey)
    }

    @Test
    fun testRunComparison() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.RunComparison)
        advanceUntilIdle()

        val compResults = vm.uiState.value.comparisonResults
        assertTrue(compResults.isNotEmpty())
        assertEquals(3, compResults.size)
    }

    @Test
    fun testReportGeneration() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.RequestReport)
        advanceUntilIdle()
        val report = vm.uiState.value.generatedReport
        assertNotNull(report)

        vm.onEvent(NumerologyUiEvent.DismissReport)
        advanceUntilIdle()
        assertNull(vm.uiState.value.generatedReport)
    }

    @Test
    fun testTraceAndSourceToggling() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showTrace)
        vm.onEvent(NumerologyUiEvent.ToggleTrace(null))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showTrace)

        assertFalse(vm.uiState.value.showSource)
        vm.onEvent(NumerologyUiEvent.ToggleSourceTransparency)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showSource)
    }

    @Test
    fun testAiBoundaryModal() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showAiModal)
        vm.onEvent(NumerologyUiEvent.RequestAiExplanation)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showAiModal)

        vm.onEvent(NumerologyUiEvent.DismissAiExplanation)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showAiModal)
    }

    @Test
    fun testHistoryClearing() = runTest {
        val vm = NumerologyViewModel(fakeNumerologyRepo, fakeHistoryRepo)
        advanceUntilIdle()

        vm.onEvent(NumerologyUiEvent.ClearHistory)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.historyEntries.isEmpty())
    }
}
