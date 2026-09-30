package com.aynvora.ui

import androidx.lifecycle.viewModelScope
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiExecutionMode
import com.aynvora.core.ai.AiInferenceDiagnostics
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.ai.AiModelSelector
import com.aynvora.core.ai.AiModelVariant
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.ai.knowledge.AynvoraKnowledgePack
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.event.AynvoraNavigationEffect
import com.aynvora.core.event.AynvoraNavigationTarget
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureDescriptor
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Observable UI state for the root application container.
 */
data class AynvoraAppState(
    val isDark: Boolean = true,
    val isAstrologyOpen: Boolean = false,
    val isTarotOpen: Boolean = false,
    val isPalmistryOpen: Boolean = false,
    val isNumerologyOpen: Boolean = false,
    val isGemstoneOpen: Boolean = false,
    val isGitaOpen: Boolean = false,
    val isAiDiagnosticsOpen: Boolean = false,
    val selectedFeatureDetail: CoreFeatureDescriptor? = null,
    val isLanguagePickerOpen: Boolean = false,
    val aiLifecycleState: AiModelLifecycleState = AiModelLifecycleState.NotInstalled,
    val selectionResult: AiModelSelectionResult? = null,
    val deviceProfile: AiDeviceProfile? = null,
    val installedModel: AiModelVariant? = null,
    val aiExecutionMode: AiExecutionMode = AiExecutionMode.DETERMINISTIC_FALLBACK,
    val isNativeVerified: Boolean = false,
    val nativeLibraryStatus: String = "NOT_VERIFIED",
    val jniStatus: String = "UNLINKED",
    val inferenceDiagnostics: AiInferenceDiagnostics = AiInferenceDiagnostics(),
    val inferenceStatus: AiInferenceStatus = AiInferenceStatus.UNLOADED,
    val knowledgePacks: List<AynvoraKnowledgePack> = emptyList(),
    val lastTestResult: String? = null,
    val isTestRunning: Boolean = false,
)

/**
 * Root application events emitted by UI user interactions.
 */
sealed class AynvoraAppUiEvent(
    eventId: String,
    screenId: String = "root",
    componentId: String = "app",
    payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
) : AynvoraClickEvent(
    eventId = eventId,
    screenId = screenId,
    componentId = componentId,
    payload = payload,
) {
    data object ToggleTheme :
        AynvoraAppUiEvent("dashboard.theme.toggle_clicked", componentId = "theme_toggle")

    data object OpenLanguagePicker :
        AynvoraAppUiEvent("dashboard.language.open_clicked", componentId = "language_selector")

    data class SelectLocale(val locale: SupportedLocale) : AynvoraAppUiEvent(
        "dashboard.language.change_clicked",
        componentId = "language_row",
        payload = AynvoraEventPayload.LanguageChangePayload(locale.localeId),
    )

    data object OpenAstrology : AynvoraAppUiEvent(
        "dashboard.astrology.open_clicked",
        componentId = "feature_card_astrology",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.ASTROLOGY),
    )

    data object CloseAstrology : AynvoraAppUiEvent(
        "astrology.close_clicked",
        screenId = "astrology",
        componentId = "close_button"
    )

    data object OpenTarot : AynvoraAppUiEvent(
        "dashboard.tarot.open_clicked",
        componentId = "feature_card_tarot",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.TAROT),
    )

    data object CloseTarot :
        AynvoraAppUiEvent("tarot.close_clicked", screenId = "tarot", componentId = "close_button")

    data object OpenPalmistry : AynvoraAppUiEvent(
        "dashboard.palmistry.open_clicked",
        componentId = "feature_card_palmistry",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.PALMISTRY),
    )

    data object ClosePalmistry : AynvoraAppUiEvent(
        "palmistry.close_clicked",
        screenId = "palmistry",
        componentId = "close_button"
    )

    data object OpenNumerology : AynvoraAppUiEvent(
        "dashboard.numerology.open_clicked",
        componentId = "feature_card_numerology",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.NUMEROLOGY),
    )

    data object CloseNumerology : AynvoraAppUiEvent(
        "numerology.close_clicked",
        screenId = "numerology",
        componentId = "close_button"
    )

    data object OpenGemstone : AynvoraAppUiEvent(
        "dashboard.gemstone.open_clicked",
        componentId = "feature_card_gemstone",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.GEMSTONE),
    )

    data object CloseGemstone : AynvoraAppUiEvent(
        "gemstone.close_clicked",
        screenId = "gemstone",
        componentId = "close_button"
    )

    data object OpenGita : AynvoraAppUiEvent(
        "dashboard.gita.open_clicked",
        componentId = "feature_card_gita",
        payload = AynvoraEventPayload.FeatureOpenPayload(CoreFeatureId.GITA),
    )

    data object CloseGita :
        AynvoraAppUiEvent("gita.close_clicked", screenId = "gita", componentId = "close_button")

    data class SelectFeature(val featureId: CoreFeatureId) : AynvoraAppUiEvent(
        "dashboard.feature.open_clicked",
        componentId = "feature_card_${featureId.name.lowercase()}",
        payload = AynvoraEventPayload.FeatureOpenPayload(featureId),
    )

    data object DownloadAi :
        AynvoraAppUiEvent("dashboard.ai.download_clicked", componentId = "ai_setup")

    data object CancelAi :
        AynvoraAppUiEvent("dashboard.ai.cancel_clicked", componentId = "ai_setup")

    data object DeleteAi :
        AynvoraAppUiEvent("dashboard.ai.delete_clicked", componentId = "ai_setup")

    data object OpenAiDiagnostics :
        AynvoraAppUiEvent("dashboard.ai.diagnostics_clicked", componentId = "ai_system_details")

    data object CloseAiDiagnostics :
        AynvoraAppUiEvent(
            "ai.diagnostics.close_clicked",
            screenId = "ai_system_details",
            componentId = "close_button"
        )

    data object LoadAiModel :
        AynvoraAppUiEvent(
            "ai.diagnostics.load_model",
            screenId = "ai_system_details",
            componentId = "load_button"
        )

    data object UnloadAiModel :
        AynvoraAppUiEvent(
            "ai.diagnostics.unload_model",
            screenId = "ai_system_details",
            componentId = "unload_button"
        )

    data class RunAiTest(val testType: String = "self_test") :
        AynvoraAppUiEvent(
            "ai.diagnostics.run_test",
            screenId = "ai_system_details",
            componentId = "run_test_button"
        )

    data object DismissSheet : AynvoraAppUiEvent("app.sheet.dismiss", componentId = "bottom_sheet")
    data object DismissDialog : AynvoraAppUiEvent("app.dialog.dismiss", componentId = "dialog")
}

/**
 * ViewModel governing application-wide root state, navigation decisions, and feature dispatching.
 */
class AynvoraAppViewModel(
    private val localeManager: AynvoraLocaleManager? = null,
    private val aiLifecycleManager: AiModelLifecycleManager? = null,
    private val aiModelSelector: AiModelSelector? = null,
    private val aiCapabilityDetector: AiDeviceCapabilityDetector? = null,
    private val localIntelligence: AynvoraLocalIntelligence? = null,
    eventDispatcher: AynvoraEventDispatcher? = null,
    initialDarkTheme: Boolean = true,
) : AynvoraBaseViewModel<AynvoraAppUiEvent, AynvoraAppState, AynvoraEffect>(
    initialState = AynvoraAppState(isDark = initialDarkTheme),
    eventDispatcher = eventDispatcher,
) {

    init {
        registerEventHandler(::handleEvent)

        // Observe and restore AI state across app restarts
        if (aiLifecycleManager != null) {
            viewModelScope.launch {
                aiLifecycleManager.initialize()
            }
            viewModelScope.launch {
                aiLifecycleManager.state.collectLatest { state ->
                    updateState {
                        copy(
                            aiLifecycleState = state,
                            installedModel = if (state is AiModelLifecycleState.Ready) state.installedVariant else null
                        )
                    }
                }
            }
        }

        // Initialize local intelligence runtime state
        if (localIntelligence != null) {
            viewModelScope.launch {
                localIntelligence.initialize()
                updateState {
                    copy(
                        installedModel = localIntelligence.getInstalledModel(),
                        aiExecutionMode = localIntelligence.getExecutionMode(),
                        isNativeVerified = localIntelligence.isNativeVerified(),
                        nativeLibraryStatus = localIntelligence.getNativeLibraryStatus(),
                        jniStatus = localIntelligence.getJniStatus(),
                        inferenceDiagnostics = localIntelligence.getDiagnostics(),
                        inferenceStatus = localIntelligence.runtime.getStatus(),
                        knowledgePacks = localIntelligence.getAllKnowledgePacks(),
                    )
                }
            }
        }

        // Initialize AI capability detection asynchronously
        if (aiCapabilityDetector != null && aiModelSelector != null) {
            viewModelScope.launch {
                try {
                    val profile = aiCapabilityDetector.detectCapability()
                    val selection = aiModelSelector.selectOptimalModel(profile)
                    updateState {
                        copy(deviceProfile = profile, selectionResult = selection)
                    }
                } catch (_: Exception) {
                    // Handled gracefully on unsupported platforms
                }
            }
        }
    }

    private suspend fun handleEvent(event: AynvoraAppUiEvent) {
        when (event) {
            is AynvoraAppUiEvent.ToggleTheme -> {
                updateState { copy(isDark = !isDark) }
            }

            is AynvoraAppUiEvent.OpenLanguagePicker -> {
                updateState { copy(isLanguagePickerOpen = true) }
                emitEffect(AynvoraEffect.ShowBottomSheet("language_picker"))
            }

            is AynvoraAppUiEvent.SelectLocale -> {
                updateState { copy(isLanguagePickerOpen = false) }
                localeManager?.setLocale(event.locale)
                emitEffect(AynvoraEffect.DismissSheet)
            }

            is AynvoraAppUiEvent.OpenAstrology -> {
                updateState {
                    copy(
                        isAstrologyOpen = true,
                        isTarotOpen = false,
                        isPalmistryOpen = false,
                        isNumerologyOpen = false,
                        selectedFeatureDetail = null,
                    )
                }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.AstrologyHome))
            }

            is AynvoraAppUiEvent.CloseAstrology -> {
                updateState { copy(isAstrologyOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.OpenTarot -> {
                updateState {
                    copy(
                        isTarotOpen = true,
                        isPalmistryOpen = false,
                        isAstrologyOpen = false,
                        isNumerologyOpen = false
                    )
                }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.TarotHome))
            }

            is AynvoraAppUiEvent.CloseTarot -> {
                updateState { copy(isTarotOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.OpenPalmistry -> {
                updateState {
                    copy(
                        isPalmistryOpen = true,
                        isTarotOpen = false,
                        isAstrologyOpen = false,
                        isNumerologyOpen = false
                    )
                }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.PalmistryHome))
            }

            is AynvoraAppUiEvent.ClosePalmistry -> {
                updateState { copy(isPalmistryOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.OpenNumerology -> {
                updateState {
                    copy(
                        isNumerologyOpen = true,
                        isTarotOpen = false,
                        isPalmistryOpen = false,
                        isAstrologyOpen = false
                    )
                }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.NumerologyHome))
            }

            is AynvoraAppUiEvent.CloseNumerology -> {
                updateState { copy(isNumerologyOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.OpenGemstone -> {
                updateState {
                    copy(
                        isGemstoneOpen = true,
                        isAstrologyOpen = false,
                        isTarotOpen = false,
                        isPalmistryOpen = false,
                        isNumerologyOpen = false,
                        selectedFeatureDetail = null,
                    )
                }
            }

            is AynvoraAppUiEvent.CloseGemstone -> {
                updateState { copy(isGemstoneOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.OpenGita -> {
                updateState {
                    copy(
                        isGitaOpen = true,
                        isGemstoneOpen = false,
                        isAstrologyOpen = false,
                        isTarotOpen = false,
                        isPalmistryOpen = false,
                        isNumerologyOpen = false,
                        selectedFeatureDetail = null,
                    )
                }
            }

            is AynvoraAppUiEvent.CloseGita -> {
                updateState { copy(isGitaOpen = false) }
                emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.Close))
            }

            is AynvoraAppUiEvent.SelectFeature -> {
                when (event.featureId) {
                    CoreFeatureId.TAROT -> {
                        updateState {
                            copy(
                                isTarotOpen = true,
                                isPalmistryOpen = false,
                                isAstrologyOpen = false,
                                isNumerologyOpen = false,
                                isGemstoneOpen = false
                            )
                        }
                        emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.TarotHome))
                    }

                    CoreFeatureId.PALMISTRY -> {
                        updateState {
                            copy(
                                isPalmistryOpen = true,
                                isTarotOpen = false,
                                isAstrologyOpen = false,
                                isNumerologyOpen = false,
                                isGemstoneOpen = false
                            )
                        }
                        emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.PalmistryHome))
                    }

                    CoreFeatureId.NUMEROLOGY -> {
                        updateState {
                            copy(
                                isNumerologyOpen = true,
                                isTarotOpen = false,
                                isPalmistryOpen = false,
                                isAstrologyOpen = false,
                                isGemstoneOpen = false
                            )
                        }
                        emitEffect(AynvoraNavigationEffect(AynvoraNavigationTarget.NumerologyHome))
                    }

                    CoreFeatureId.GEMSTONE -> {
                        updateState {
                            copy(
                                isGemstoneOpen = true,
                                isTarotOpen = false,
                                isPalmistryOpen = false,
                                isAstrologyOpen = false,
                                isNumerologyOpen = false,
                                isGitaOpen = false,
                                selectedFeatureDetail = null,
                            )
                        }
                    }

                    CoreFeatureId.GITA -> {
                        updateState {
                            copy(
                                isGitaOpen = true,
                                isGemstoneOpen = false,
                                isTarotOpen = false,
                                isPalmistryOpen = false,
                                isAstrologyOpen = false,
                                isNumerologyOpen = false,
                                selectedFeatureDetail = null,
                            )
                        }
                    }

                    else -> {
                        val desc = CanonicalCoreFeatures.firstOrNull { it.id == event.featureId }
                        updateState { copy(selectedFeatureDetail = desc) }
                        if (desc != null) {
                            emitEffect(AynvoraEffect.ShowBottomSheet("feature_detail", desc))
                        }
                    }
                }
            }

            is AynvoraAppUiEvent.DownloadAi -> {
                val selectedModel = currentState.selectionResult?.selectedModel
                if (selectedModel != null && aiLifecycleManager != null) {
                    aiLifecycleManager.downloadAndInstall(selectedModel)
                }
            }

            is AynvoraAppUiEvent.CancelAi -> {
                aiLifecycleManager?.cancelDownload()
            }

            is AynvoraAppUiEvent.DeleteAi -> {
                aiLifecycleManager?.deleteInstalledModel()
            }

            is AynvoraAppUiEvent.OpenAiDiagnostics -> {
                updateState {
                    copy(
                        isAiDiagnosticsOpen = true,
                        installedModel = localIntelligence?.getInstalledModel(),
                        aiExecutionMode = localIntelligence?.getExecutionMode()
                            ?: AiExecutionMode.DETERMINISTIC_FALLBACK,
                        isNativeVerified = localIntelligence?.isNativeVerified() ?: false,
                        nativeLibraryStatus = localIntelligence?.getNativeLibraryStatus()
                            ?: "NOT_VERIFIED",
                        jniStatus = localIntelligence?.getJniStatus() ?: "UNLINKED",
                        inferenceDiagnostics = localIntelligence?.getDiagnostics()
                            ?: AiInferenceDiagnostics(),
                        inferenceStatus = localIntelligence?.runtime?.getStatus()
                            ?: AiInferenceStatus.UNLOADED,
                        knowledgePacks = localIntelligence?.getAllKnowledgePacks() ?: emptyList(),
                    )
                }
            }

            is AynvoraAppUiEvent.CloseAiDiagnostics -> {
                updateState { copy(isAiDiagnosticsOpen = false) }
            }

            is AynvoraAppUiEvent.LoadAiModel -> {
                viewModelScope.launch {
                    localIntelligence?.loadModel()
                    updateState {
                        copy(
                            inferenceStatus = localIntelligence?.runtime?.getStatus()
                                ?: AiInferenceStatus.UNLOADED,
                            inferenceDiagnostics = localIntelligence?.getDiagnostics()
                                ?: AiInferenceDiagnostics(),
                            aiExecutionMode = localIntelligence?.getExecutionMode()
                                ?: AiExecutionMode.DETERMINISTIC_FALLBACK,
                        )
                    }
                }
            }

            is AynvoraAppUiEvent.UnloadAiModel -> {
                viewModelScope.launch {
                    localIntelligence?.unloadModel()
                    updateState {
                        copy(
                            inferenceStatus = localIntelligence?.runtime?.getStatus()
                                ?: AiInferenceStatus.UNLOADED,
                            inferenceDiagnostics = localIntelligence?.getDiagnostics()
                                ?: AiInferenceDiagnostics(),
                            aiExecutionMode = localIntelligence?.getExecutionMode()
                                ?: AiExecutionMode.DETERMINISTIC_FALLBACK,
                        )
                    }
                }
            }

            is AynvoraAppUiEvent.RunAiTest -> {
                viewModelScope.launch {
                    updateState { copy(isTestRunning = true) }
                    try {
                        val intelligence = localIntelligence
                        if (intelligence != null) {
                            if (!intelligence.isModelLoaded()) {
                                intelligence.loadModel()
                            }
                            val response = when (event.testType) {
                                "gita" -> {
                                    val userContext = com.aynvora.core.ai.AynvoraUserContext(
                                        question = "Can you help me reflect on this using the Bhagavad Gita?",
                                        statedSituation = "I am confused about my career direction.",
                                    )
                                    val pack = intelligence.getKnowledgePack(com.aynvora.core.feature.CoreFeatureId.GITA)
                                    val evidence = pack?.retrieveRelevantEvidence(userContext.question, userContext) ?: emptyList()
                                    val req = com.aynvora.core.ai.AynvoraAiRequest(
                                        requestId = "diag_gita_${System.currentTimeMillis()}",
                                        featureId = com.aynvora.core.feature.CoreFeatureId.GITA,
                                        knowledgePackId = pack?.knowledgePackId ?: "kp_gita_canonical_v1",
                                        rulesetId = "CANONICAL_GITA_TRADITION",
                                        evidence = evidence,
                                        userContext = userContext,
                                        question = userContext.question,
                                        locale = "en",
                                        responseMode = com.aynvora.core.ai.AynvoraResponseMode.REFLECTIVE,
                                    )
                                    intelligence.synthesize(req)
                                }
                                else -> {
                                    val userContext = com.aynvora.core.ai.AynvoraUserContext(
                                        question = "Hello! State your contemplative purpose in 5 words:"
                                    )
                                    val req = com.aynvora.core.ai.AynvoraAiRequest(
                                        requestId = "diag_selftest_${System.currentTimeMillis()}",
                                        featureId = com.aynvora.core.feature.CoreFeatureId.GITA,
                                        knowledgePackId = "kp_gita_canonical_v1",
                                        rulesetId = "CANONICAL_GITA_TRADITION",
                                        userContext = userContext,
                                        question = userContext.question,
                                        locale = "en",
                                        responseMode = com.aynvora.core.ai.AynvoraResponseMode.REFLECTIVE,
                                    )
                                    intelligence.synthesize(req)
                                }
                            }
                            val summary = when (response) {
                                is com.aynvora.core.result.AynvoraResult.Success -> {
                                    val res = response.value
                                    "SUCCESS [${res.executionMode.name}] (${res.latencyMs}ms)\nTokens: >0, Validated: ${res.validationStatus.name}\n\n${res.responseText}"
                                }
                                is com.aynvora.core.result.AynvoraResult.Failure -> {
                                    "FAILED: ${response.message}"
                                }
                            }
                            updateState {
                                copy(
                                    lastTestResult = summary,
                                    isTestRunning = false,
                                    aiExecutionMode = intelligence.getExecutionMode(),
                                    inferenceDiagnostics = intelligence.getDiagnostics(),
                                    inferenceStatus = intelligence.runtime.getStatus(),
                                )
                            }
                        } else {
                            updateState { copy(lastTestResult = "FAILED: localIntelligence is null", isTestRunning = false) }
                        }
                    } catch (t: Throwable) {
                        updateState { copy(lastTestResult = "ERROR: ${t.message}", isTestRunning = false) }
                    }
                }
            }

            is AynvoraAppUiEvent.DismissSheet -> {
                updateState { copy(isLanguagePickerOpen = false, selectedFeatureDetail = null) }
                emitEffect(AynvoraEffect.DismissSheet)
            }

            is AynvoraAppUiEvent.DismissDialog -> {
                emitEffect(AynvoraEffect.DismissDialog)
            }
        }
    }
}
