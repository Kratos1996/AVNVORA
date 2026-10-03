package com.aynvora.ui

import com.aynvora.core.ai.AiModelCatalog
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.data.ai.AiModelStorageRepositoryImpl
import com.aynvora.data.storage.InMemoryStorageDriver
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
class AynvoraAppViewModelTest {

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
    fun testInitialAppState() = runTest {
        val vm = AynvoraAppViewModel(initialDarkTheme = true)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isDark)
        assertFalse(state.isTarotOpen)
        assertFalse(state.isPalmistryOpen)
        assertFalse(state.isNumerologyOpen)
        assertFalse(state.isLanguagePickerOpen)
        assertNull(state.selectedFeatureDetail)
        assertEquals(AiModelLifecycleState.NotInstalled, state.aiLifecycleState)
    }

    @Test
    fun testThemeToggle() = runTest {
        val vm = AynvoraAppViewModel(initialDarkTheme = true)
        advanceUntilIdle()

        vm.onEvent(AynvoraAppUiEvent.ToggleTheme)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isDark)

        vm.onEvent(AynvoraAppUiEvent.ToggleTheme)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isDark)
    }

    @Test
    fun testLanguagePickerAndSelection() = runTest {
        val fakePrefsRepo = object : com.aynvora.core.repository.UserPreferencesRepository {
            var current = com.aynvora.core.models.UserPreferences(languageCode = "en")
            override suspend fun getPreferences() =
                com.aynvora.core.result.AynvoraResult.Success(current)

            override suspend fun updatePreferences(preferences: com.aynvora.core.models.UserPreferences): com.aynvora.core.result.AynvoraResult<com.aynvora.core.models.UserPreferences> {
                current = preferences
                return com.aynvora.core.result.AynvoraResult.Success(preferences)
            }

            override fun observePreferences() =
                kotlinx.coroutines.flow.flowOf(com.aynvora.core.result.AynvoraResult.Success(current))
        }
        val localeManager = com.aynvora.localization.locale.AynvoraLocaleManagerImpl(fakePrefsRepo)
        val vm = AynvoraAppViewModel(localeManager = localeManager)
        advanceUntilIdle()

        vm.onEvent(AynvoraAppUiEvent.OpenLanguagePicker)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isLanguagePickerOpen)

        vm.onEvent(AynvoraAppUiEvent.SelectLocale(com.aynvora.localization.locale.LanguageRegistry.HINDI))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLanguagePickerOpen)
        assertEquals(
            com.aynvora.localization.locale.LanguageRegistry.HINDI,
            localeManager.currentLocale.value
        )
    }

    @Test
    fun testFeatureNavigationButtons() = runTest {
        val vm = AynvoraAppViewModel()
        advanceUntilIdle()

        // Tarot
        vm.onEvent(AynvoraAppUiEvent.OpenTarot)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isTarotOpen)
        assertFalse(vm.uiState.value.isPalmistryOpen)

        vm.onEvent(AynvoraAppUiEvent.CloseTarot)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isTarotOpen)

        // Palmistry
        vm.onEvent(AynvoraAppUiEvent.OpenPalmistry)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isPalmistryOpen)
        assertFalse(vm.uiState.value.isTarotOpen)

        vm.onEvent(AynvoraAppUiEvent.ClosePalmistry)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isPalmistryOpen)

        // Numerology
        vm.onEvent(AynvoraAppUiEvent.OpenNumerology)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isNumerologyOpen)

        vm.onEvent(AynvoraAppUiEvent.CloseNumerology)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isNumerologyOpen)

        // Gita
        vm.onEvent(AynvoraAppUiEvent.OpenGita)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isGitaOpen)

        vm.onEvent(AynvoraAppUiEvent.CloseGita)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isGitaOpen)
    }

    @Test
    fun testSelectFeatureCardNavigationAndDetail() = runTest {
        val vm = AynvoraAppViewModel()
        advanceUntilIdle()

        // Direct feature targets
        vm.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.TAROT))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isTarotOpen)

        vm.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.PALMISTRY))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isPalmistryOpen)
        assertFalse(vm.uiState.value.isTarotOpen)

        vm.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.NUMEROLOGY))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isNumerologyOpen)
        assertFalse(vm.uiState.value.isPalmistryOpen)

        vm.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.GITA))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isGitaOpen)
        assertFalse(vm.uiState.value.isNumerologyOpen)

        // Full-screen feature route: Garuda Puran (Phase 10.34)
        vm.onEvent(AynvoraAppUiEvent.SelectFeature(CoreFeatureId.GARUDA_PURAN))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isGarudaPuranOpen)
        assertFalse(vm.uiState.value.isGitaOpen)

        vm.onEvent(AynvoraAppUiEvent.CloseGarudaPuran)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isGarudaPuranOpen)
    }

    @Test
    fun testAiModelDownloadAndPersistenceAcrossAppRestart() = runTest {
        val persistentStorage = InMemoryStorageDriver()
        val variant = AiModelCatalog.allVariants.first()

        // 1. Session 1: User downloads model
        val repo1 = AiModelStorageRepositoryImpl(driver = persistentStorage)
        val lifecycleManager1 = AiModelLifecycleManager(repo1, scope = this)
        val vm1 = AynvoraAppViewModel(aiLifecycleManager = lifecycleManager1)
        advanceUntilIdle()

        assertEquals(AiModelLifecycleState.NotInstalled, vm1.uiState.value.aiLifecycleState)

        // Install model
        val downloadRes = repo1.downloadModel(variant) { _, _ -> }
        val tempPath = (downloadRes as com.aynvora.core.result.AynvoraResult.Success).value
        repo1.commitInstallation(tempPath, variant)
        lifecycleManager1.initialize()
        advanceUntilIdle()

        assertTrue(vm1.uiState.value.aiLifecycleState is AiModelLifecycleState.Ready)
        assertEquals(
            variant.modelId,
            (vm1.uiState.value.aiLifecycleState as AiModelLifecycleState.Ready).installedVariant.modelId
        )

        // 2. SIMULATE APP RESTART (Clean process, new ViewModel, new Repo connected to same storage)
        val repo2 = AiModelStorageRepositoryImpl(driver = persistentStorage)
        val lifecycleManager2 = AiModelLifecycleManager(repo2, scope = backgroundScope)
        val vm2 = AynvoraAppViewModel(aiLifecycleManager = lifecycleManager2)
        advanceUntilIdle()

        // VERIFICATION: After restart, the model MUST automatically show as Ready/Installed!
        val restoredState = vm2.uiState.value.aiLifecycleState
        assertTrue(
            restoredState is AiModelLifecycleState.Ready,
            "Expected Ready state after restart, got $restoredState"
        )
        assertEquals(
            variant.modelId,
            (restoredState as AiModelLifecycleState.Ready).installedVariant.modelId
        )

        // 3. User uninstalls AI model
        vm2.onEvent(AynvoraAppUiEvent.DeleteAi)
        advanceUntilIdle()
        assertEquals(AiModelLifecycleState.NotInstalled, vm2.uiState.value.aiLifecycleState)

        // 4. SIMULATE RESTART AFTER DELETION
        val repo3 = AiModelStorageRepositoryImpl(driver = persistentStorage)
        val lifecycleManager3 = AiModelLifecycleManager(repo3, scope = backgroundScope)
        val vm3 = AynvoraAppViewModel(aiLifecycleManager = lifecycleManager3)
        advanceUntilIdle()

        assertEquals(AiModelLifecycleState.NotInstalled, vm3.uiState.value.aiLifecycleState)
    }

    @Test
    fun testStressRapidInteractionsNoCrash() = runTest {
        val vm = AynvoraAppViewModel()
        advanceUntilIdle()

        val events = listOf(
            AynvoraAppUiEvent.ToggleTheme,
            AynvoraAppUiEvent.OpenLanguagePicker,
            AynvoraAppUiEvent.DismissSheet,
            AynvoraAppUiEvent.OpenTarot,
            AynvoraAppUiEvent.CloseTarot,
            AynvoraAppUiEvent.OpenPalmistry,
            AynvoraAppUiEvent.ClosePalmistry,
            AynvoraAppUiEvent.OpenNumerology,
            AynvoraAppUiEvent.CloseNumerology,
            AynvoraAppUiEvent.SelectFeature(CoreFeatureId.GARUDA_PURAN),
            AynvoraAppUiEvent.DismissSheet,
            AynvoraAppUiEvent.DismissDialog,
        )

        // Fire 100 events rapidly
        for (i in 0 until 100) {
            val event = events[i % events.size]
            vm.onEvent(event)
        }
        advanceUntilIdle()

        // State must remain coherent and uncorrupted
        assertNotNull(vm.uiState.value)
    }
}
