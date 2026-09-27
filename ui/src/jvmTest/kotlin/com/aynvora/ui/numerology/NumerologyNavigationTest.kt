package com.aynvora.ui.numerology

import com.aynvora.core.event.AynvoraNavigationEffect
import com.aynvora.core.event.AynvoraNavigationTarget
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.ui.AynvoraAppState
import com.aynvora.ui.AynvoraAppUiEvent
import com.aynvora.ui.AynvoraAppViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NumerologyNavigationTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSelectFeatureNumerologyOpensNumerologyScreen() = runTest {
        val appViewModel = AynvoraAppViewModel()
        advanceUntilIdle()

        assertFalse(appViewModel.uiState.value.isNumerologyOpen)

        appViewModel.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.NUMEROLOGY))
        advanceUntilIdle()

        assertTrue(appViewModel.uiState.value.isNumerologyOpen)
        assertFalse(appViewModel.uiState.value.isTarotOpen)
        assertFalse(appViewModel.uiState.value.isPalmistryOpen)
    }

    @Test
    fun testOpenAndCloseNumerologyEvents() = runTest {
        val appViewModel = AynvoraAppViewModel()
        advanceUntilIdle()

        appViewModel.onEvent(AynvoraAppUiEvent.OpenNumerology)
        advanceUntilIdle()
        assertTrue(appViewModel.uiState.value.isNumerologyOpen)

        appViewModel.onEvent(AynvoraAppUiEvent.CloseNumerology)
        advanceUntilIdle()
        assertFalse(appViewModel.uiState.value.isNumerologyOpen)
    }
}
