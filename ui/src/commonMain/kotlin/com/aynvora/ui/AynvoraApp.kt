package com.aynvora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelSelector
import com.aynvora.core.ai.AynvoraLocalIntelligence
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.feature.CoreFeatureDescriptor
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.LocalAynvoraWindowInfo
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.components.AynvoraExtraSmallBold
import com.aynvora.designsystem.components.AynvoraExtraSmallText
import com.aynvora.designsystem.components.AynvoraLogo
import com.aynvora.designsystem.components.AynvoraLogoVariant
import com.aynvora.designsystem.components.AynvoraSmallText
import com.aynvora.designsystem.components.AynvoraTitle
import com.aynvora.designsystem.components.AynvoraTitleMedium
import com.aynvora.designsystem.components.AynvoraTitleSmall
import com.aynvora.designsystem.components.dialogs.AynvoraDialogHost
import com.aynvora.designsystem.components.navigation.AynvoraDesktopSidebar
import com.aynvora.designsystem.components.navigation.AynvoraSidebarItem
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHeader
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHost
import com.aynvora.designsystem.event.LocalAynvoraEventDispatcher
import com.aynvora.designsystem.event.aynvoraClickable
import com.aynvora.designsystem.localization.AynvoraLocalizationProvider
import com.aynvora.designsystem.localization.LocalAynvoraLocale
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.qa.android.AndroidInteractionSentinelRuntime
import com.aynvora.qa.android.qaAction
import com.aynvora.qa.android.ui.QaSentinelDashboard
import com.aynvora.qa.core.models.QaActionId
import com.aynvora.qa.core.models.QaStateSnapshot
import com.aynvora.ui.ai.AiDiagnosticScreen
import com.aynvora.ui.astrology.AstrologyRoute
import com.aynvora.ui.features.CoreFeatureDashboard
import com.aynvora.ui.gemstone.GemstoneRoute
import com.aynvora.ui.gita.GitaRoute
import com.aynvora.ui.numerology.NumerologyRoute
import com.aynvora.ui.palmistry.PalmistryRoute
import com.aynvora.ui.tarot.TarotRoute
import dev.ishant.cottonsheet.LocalCottonSheetController
import org.koin.compose.currentKoinScope

/**
 * Root Compose Multiplatform entry application for AYNVORA.
 */
@Composable
fun AynvoraApp(
    darkTheme: Boolean = true,
    viewModel: AynvoraAppViewModel? = null,
) {
    val koin = currentKoinScope()
    val appViewModel = viewModel ?: remember(koin) {
        koin.getOrNull<AynvoraAppViewModel>() ?: AynvoraAppViewModel(
            localeManager = koin.getOrNull<AynvoraLocaleManager>(),
            aiLifecycleManager = koin.getOrNull<AiModelLifecycleManager>(),
            aiModelSelector = koin.getOrNull<AiModelSelector>(),
            aiCapabilityDetector = koin.getOrNull<AiDeviceCapabilityDetector>(),
            localIntelligence = koin.getOrNull<AynvoraLocalIntelligence>(),
            eventDispatcher = koin.getOrNull<AynvoraEventDispatcher>(),
            initialDarkTheme = darkTheme,
        )
    }

    val state by appViewModel.uiState.collectAsState()
    val localeManager = remember(koin) { koin.getOrNull<AynvoraLocaleManager>() }

    AynvoraTheme(darkTheme = state.isDark) {
        val content = @Composable {
            CompositionLocalProvider(
                LocalAynvoraEventDispatcher provides { event ->
                    if (event is AynvoraAppUiEvent) {
                        appViewModel.onEvent(event)
                    }
                },
            ) {
                AynvoraAppContent(
                    state = state,
                    onEvent = { event -> appViewModel.onEvent(event) },
                )
            }
        }

        if (localeManager != null) {
            AynvoraLocalizationProvider(localeManager = localeManager) {
                content()
            }
        } else {
            content()
        }
    }
}

@Composable
private fun AynvoraAppContent(
    state: AynvoraAppState,
    onEvent: (AynvoraAppUiEvent) -> Unit,
) {
    val isDark = state.isDark
    val locale = LocalAynvoraLocale.current
    val translator = LocalAynvoraTranslator.current

    val backgroundColor = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    AynvoraBottomSheetHost {
        AynvoraDialogHost {
            val sheetController = LocalCottonSheetController.current
            val sheetParams = AynvoraBottomSheetDefaults.params()
            var isQaDashboardOpen by remember { mutableStateOf(false) }

            // Sync structural UI state with Interaction Sentinel Runtime
            LaunchedEffect(state, locale, isDark, isQaDashboardOpen) {
                AndroidInteractionSentinelRuntime.stateSnapshotProvider = {
                    QaStateSnapshot(
                        route = when {
                            state.isAiDiagnosticsOpen -> "ai_system_details"
                            isQaDashboardOpen -> "qa_dashboard"
                            state.isAstrologyOpen -> "astrology"
                            state.isTarotOpen -> "tarot"
                            state.isPalmistryOpen -> "palmistry"
                            state.isNumerologyOpen -> "numerology"
                            state.isGemstoneOpen -> "gemstone"
                            state.isGitaOpen -> "gita"
                            else -> "dashboard"
                        },
                        screen = when {
                            state.isAiDiagnosticsOpen -> "AiDiagnosticScreen"
                            isQaDashboardOpen -> "QaSentinelDashboard"
                            state.isAstrologyOpen -> "AstrologyScreen"
                            state.isTarotOpen -> "TarotScreen"
                            state.isPalmistryOpen -> "PalmistryScreen"
                            state.isNumerologyOpen -> "NumerologyScreen"
                            state.isGemstoneOpen -> "GemstoneScreen"
                            state.isGitaOpen -> "GitaScreen"
                            state.isLanguagePickerOpen -> "LanguagePickerSheet"
                            state.selectedFeatureDetail != null -> "FeatureFoundationDetailSheet"
                            else -> "CoreFeatureDashboard"
                        },
                        isSheetOpen = state.isLanguagePickerOpen || state.selectedFeatureDetail != null,
                        activeLocale = locale.localeId,
                        isDarkTheme = isDark,
                        timestampMs = System.currentTimeMillis(),
                    )
                }
            }

            // Observe bottom sheet state from ViewModel with explicit bidirectional dismissal
            LaunchedEffect(state.isLanguagePickerOpen) {
                if (state.isLanguagePickerOpen) {
                    sheetController.show(sheetParams) { dismiss ->
                        LanguagePickerSheet(
                            currentLocale = locale,
                            onLocaleSelected = { selected ->
                                onEvent(AynvoraAppUiEvent.SelectLocale(selected))
                                dismiss()
                            },
                            onClose = {
                                dismiss()
                                onEvent(AynvoraAppUiEvent.DismissSheet)
                            },
                            translator = translator
                        )
                    }
                } else {
                    sheetController.dismiss()
                }
            }

            LaunchedEffect(state.selectedFeatureDetail) {
                val desc = state.selectedFeatureDetail
                if (desc != null) {
                    sheetController.show(sheetParams) { dismiss ->
                        FeatureFoundationDetailSheet(
                            descriptor = desc,
                            onOpenFeature = { featureId ->
                                dismiss()
                                when (featureId) {
                                    CoreFeatureId.ASTROLOGY -> {
                                        onEvent(AynvoraAppUiEvent.OpenAstrology)
                                    }

                                    CoreFeatureId.GEMSTONE -> {
                                        onEvent(AynvoraAppUiEvent.OpenGemstone)
                                    }

                                    CoreFeatureId.GITA -> {
                                        onEvent(AynvoraAppUiEvent.OpenGita)
                                    }

                                    else -> {
                                        onEvent(AynvoraAppUiEvent.SelectFeature(featureId))
                                    }
                                }
                            },
                            onClose = {
                                dismiss()
                                onEvent(AynvoraAppUiEvent.DismissSheet)
                            },
                        )
                    }
                } else {
                    sheetController.dismiss()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                if (isQaDashboardOpen) {
                    QaSentinelDashboard(
                        onClose = { isQaDashboardOpen = false },
                    )
                } else if (state.isAstrologyOpen) {
                    AstrologyRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseAstrology) },
                    )
                } else if (state.isTarotOpen) {
                    TarotRoute(
                        language = locale.localeId,
                        onClose = { onEvent(AynvoraAppUiEvent.CloseTarot) },
                    )
                } else if (state.isPalmistryOpen) {
                    PalmistryRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.ClosePalmistry) },
                    )
                } else if (state.isNumerologyOpen) {
                    NumerologyRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseNumerology) },
                    )
                } else if (state.isGemstoneOpen) {
                    GemstoneRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseGemstone) },
                    )
                } else if (state.isGitaOpen) {
                    GitaRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseGita) },
                    )
                } else if (state.isAiDiagnosticsOpen) {
                    AiDiagnosticScreen(
                        deviceProfile = state.deviceProfile,
                        selectionResult = state.selectionResult,
                        lifecycleState = state.aiLifecycleState,
                        inferenceStatus = state.inferenceStatus,
                        inferenceDiagnostics = state.inferenceDiagnostics,
                        executionMode = state.aiExecutionMode,
                        isNativeVerified = state.isNativeVerified,
                        nativeLibraryStatus = state.nativeLibraryStatus,
                        jniStatus = state.jniStatus,
                        installedModel = state.installedModel,
                        knowledgePacks = state.knowledgePacks,
                        testResult = state.lastTestResult,
                        isTestRunning = state.isTestRunning,
                        onRunTestClicked = { testType -> onEvent(AynvoraAppUiEvent.RunAiTest(testType)) },
                        onLoadModelClicked = { onEvent(AynvoraAppUiEvent.LoadAiModel) },
                        onUnloadModelClicked = { onEvent(AynvoraAppUiEvent.UnloadAiModel) },
                        onBackClicked = { onEvent(AynvoraAppUiEvent.CloseAiDiagnostics) },
                    )
                } else if (LocalAynvoraWindowInfo.current.isExpanded) {
                    val sidebarItems = remember<List<AynvoraSidebarItem>>(translator) {
                        listOf(
                            AynvoraSidebarItem("dashboard", "Dashboard", "🏠"),
                            AynvoraSidebarItem("astrology", "Vedic Astrology", "🪐", CoreFeatureId.ASTROLOGY, category = "Astrology"),
                            AynvoraSidebarItem("daily_guidance", "Daily Guidance", "☀️", CoreFeatureId.DAILY_GUIDANCE, category = "Daily / Practice"),
                            AynvoraSidebarItem("yantra", "Sacred Yantra", "☸️", CoreFeatureId.YANTRA, category = "Daily / Practice"),
                            AynvoraSidebarItem("rudraksha", "Rudraksha", "📿", CoreFeatureId.RUDRAKSHA, category = "Daily / Practice"),
                            AynvoraSidebarItem("jadi", "Sacred Roots", "🌿", CoreFeatureId.JADI, category = "Daily / Practice"),
                            AynvoraSidebarItem("wallpaper", "Wallpaper Studio", "🎨", CoreFeatureId.WALLPAPER, category = "Daily / Practice"),
                            AynvoraSidebarItem("tarot", "Tarot Reflection", "🎴", CoreFeatureId.TAROT, category = "Insights"),
                            AynvoraSidebarItem("numerology", "Numerology", "🔢", CoreFeatureId.NUMEROLOGY, category = "Insights"),
                            AynvoraSidebarItem("palmistry", "Palmistry / Hastrekha", "✋", CoreFeatureId.PALMISTRY, category = "Insights"),
                            AynvoraSidebarItem("gemstone", "Navaratna Gemstones", "💎", CoreFeatureId.GEMSTONE, category = "Insights"),
                            AynvoraSidebarItem("gita", "Bhagavad Gita", "📜", CoreFeatureId.GITA, category = "Insights"),
                            AynvoraSidebarItem("garuda", "Garuda Purana", "🦅", CoreFeatureId.GARUDA_PURAN, category = "Insights"),
                            AynvoraSidebarItem("lalkitab", "Lal Kitab (Research)", "📕", CoreFeatureId.LAL_KITAB, category = "Traditions"),
                            AynvoraSidebarItem("ai", "On-Device AI", "🧠", CoreFeatureId.AI_ASSISTANT, category = "AI Intelligence"),
                        )
                    }

                    Row(modifier = Modifier.fillMaxSize()) {
                        AynvoraDesktopSidebar(
                            items = sidebarItems,
                            selectedItemId = "dashboard",
                            onItemSelected = { item ->
                                val fid = item.featureId
                                if (fid != null) {
                                    onEvent(AynvoraAppUiEvent.SelectFeature(fid))
                                }
                            },
                            onLanguageClick = { onEvent(AynvoraAppUiEvent.OpenLanguagePicker) },
                            onThemeToggleClick = { onEvent(AynvoraAppUiEvent.ToggleTheme) },
                            isDark = isDark,
                            currentLanguageName = locale.nativeName,
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = AynvoraSpacing.space20, vertical = AynvoraSpacing.space16),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    AynvoraTitle(
                                        text = translator.translate(TranslationKey.App.AppName),
                                        color = AynvoraColors.Gold,
                                    )
                                    AynvoraExtraSmallText(
                                        text = translator.translate(TranslationKey.App.Tagline),
                                        color = AynvoraTheme.colors.textSecondary,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space8),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(AynvoraShapes.shape8)
                                            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                            .border(1.dp, if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraColors.Gold.copy(alpha = 0.35f), AynvoraShapes.shape8)
                                            .clickable { isQaDashboardOpen = !isQaDashboardOpen },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AynvoraSmallText(text = "🛡️")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AynvoraSpacing.space16))

                            CoreFeatureDashboard(
                                language = locale.localeId,
                                aiLifecycleState = state.aiLifecycleState,
                                aiSelectionResult = state.selectionResult,
                                aiDeviceProfile = state.deviceProfile,
                                onDownloadAiClicked = { onEvent(AynvoraAppUiEvent.DownloadAi) },
                                onCancelAiClicked = { onEvent(AynvoraAppUiEvent.CancelAi) },
                                onDeleteAiClicked = { onEvent(AynvoraAppUiEvent.DeleteAi) },
                                onAiDiagnosticsClicked = { onEvent(AynvoraAppUiEvent.OpenAiDiagnostics) },
                                onFeatureSelected = { featureId ->
                                    onEvent(AynvoraAppUiEvent.SelectFeature(featureId))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = AynvoraSpacing.space16,
                                vertical = AynvoraSpacing.space12
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Top Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space10),
                            ) {
                                AynvoraLogo(
                                    size = 38.dp,
                                    variant = AynvoraLogoVariant.Transparent
                                )
                                Column {
                                    AynvoraTitle(
                                        text = translator.translate(TranslationKey.App.AppName),
                                        color = AynvoraColors.Gold,
                                    )
                                    AynvoraExtraSmallText(
                                        text = translator.translate(TranslationKey.App.Tagline),
                                        color = AynvoraTheme.colors.textSecondary,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space6),
                            ) {
                                val controlBg =
                                    if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold
                                val controlBorder =
                                    if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraColors.Gold.copy(
                                        alpha = 0.35f
                                    )
                                val controlShape = AynvoraShapes.shape8

                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(controlShape)
                                        .background(controlBg)
                                        .border(
                                            width = 1.dp,
                                            color = controlBorder,
                                            shape = controlShape
                                        )
                                        .aynvoraClickable(
                                            event = AynvoraAppUiEvent.OpenLanguagePicker,
                                            role = Role.Button,
                                        )
                                        .qaAction(QaActionId.DASHBOARD_OPEN_LANGUAGE)
                                        .padding(horizontal = AynvoraSpacing.space10),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space4),
                                    ) {
                                        AynvoraExtraSmallText(text = "🌐")
                                        AynvoraExtraSmallBold(
                                            text = locale.nativeName,
                                            color = AynvoraTheme.colors.textPrimary,
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(controlShape)
                                        .background(controlBg)
                                        .border(
                                            width = 1.dp,
                                            color = controlBorder,
                                            shape = controlShape
                                        )
                                        .aynvoraClickable(
                                            event = AynvoraAppUiEvent.ToggleTheme,
                                            role = Role.Button,
                                        )
                                        .qaAction(QaActionId.DASHBOARD_TOGGLE_THEME),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AynvoraSmallText(text = if (isDark) "🌙" else "☀️")
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(controlShape)
                                        .background(controlBg)
                                        .border(
                                            width = 1.dp,
                                            color = controlBorder,
                                            shape = controlShape
                                        )
                                        .clickable { isQaDashboardOpen = !isQaDashboardOpen }
                                        .qaAction(
                                            QaActionId.of(
                                                "qa",
                                                "dashboard",
                                                "button",
                                                "toggle"
                                            )
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AynvoraSmallText(text = "🛡️")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AynvoraSpacing.space12))

                        CoreFeatureDashboard(
                            language = locale.localeId,
                            aiLifecycleState = state.aiLifecycleState,
                            aiSelectionResult = state.selectionResult,
                            aiDeviceProfile = state.deviceProfile,
                            onDownloadAiClicked = { onEvent(AynvoraAppUiEvent.DownloadAi) },
                            onCancelAiClicked = { onEvent(AynvoraAppUiEvent.CancelAi) },
                            onDeleteAiClicked = { onEvent(AynvoraAppUiEvent.DeleteAi) },
                            onAiDiagnosticsClicked = { onEvent(AynvoraAppUiEvent.OpenAiDiagnostics) },
                            onFeatureSelected = { featureId ->
                                onEvent(AynvoraAppUiEvent.SelectFeature(featureId))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// ── Language Picker Bottom Sheet ──────────────────────────────────────────────

@Composable
private fun LanguagePickerSheet(
    currentLocale: SupportedLocale,
    onLocaleSelected: (SupportedLocale) -> Unit,
    translator: AynvoraTranslator,
    onClose: () -> Unit
) {
    val locales = remember { LanguageRegistry.availableLocales() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.sdp),
    ) {
        AynvoraBottomSheetHeader(
            title = translator.translate(TranslationKey.App.SelectLanguage),
            subtitle = translator.translate(TranslationKey.App.Language),
            onCloseClick = onClose,
        )

        Spacer(modifier = Modifier.height(8.sdp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp),
            verticalArrangement = Arrangement.spacedBy(10.sdp),
        ) {
            items(locales, key = { it.localeId }) { locale ->
                LanguageRow(
                    locale = locale,
                    isSelected = locale.localeId == currentLocale.localeId,
                    onClick = { onLocaleSelected(locale) }
                )
            }

            item { Spacer(modifier = Modifier.height(8.sdp)) }
        }
    }
}

@Composable
private fun LanguageRow(
    locale: SupportedLocale,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) AynvoraTheme.colors.selectedGold else Color.Transparent

    val borderColor = if (isSelected) {
        AynvoraTheme.colors.Gold.copy(alpha = 0.55f)
    } else {
        AynvoraTheme.colors.Gold.copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .aynvoraClickable(
                event = AynvoraAppUiEvent.SelectLocale(locale),
                role = Role.Button,
                onDispatch = { onClick() },
            )
            .padding(horizontal = 16.sdp, vertical = 14.sdp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            AynvoraTitleSmall(
                text = locale.nativeName,
                color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.textPrimary,
            )
            Spacer(modifier = Modifier.height(2.sdp))
            AynvoraExtraSmallText(
                text = locale.englishName,
                color = AynvoraTheme.colors.textSecondary,
            )
        }

        if (isSelected) {
            AynvoraTitleMedium(
                text = "✓",
                color = AynvoraTheme.colors.Gold,
            )
        }
    }
}

// ── Feature Foundation Detail Sheet ──────────────────────────────────────────

@Composable
fun FeatureFoundationDetailSheet(
    descriptor: CoreFeatureDescriptor,
    onClose: () -> Unit,
    onOpenFeature: ((CoreFeatureId) -> Unit)? = null,
) {
    val isDark = AynvoraTheme.isDark
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.sdp),
    ) {
        AynvoraBottomSheetHeader(
            title = descriptor.titleKey,
            subtitle = translator.translate(TranslationKey.FeatureDetail.ArchitectureFoundation),
            onCloseClick = onClose,
        )

        Column(modifier = Modifier.padding(horizontal = 24.sdp)) {
            // Operational Status Card
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Outlined,
                containerColor = if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold,
                contentColor = AynvoraTheme.colors.textPrimary,
            ) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    AynvoraExtraSmallBold(
                        text = translator.translate(TranslationKey.FeatureDetail.OperationalStatus),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    AynvoraTitleSmall(
                        text = when (val a = descriptor.availability) {
                            is FeatureAvailability.Available ->
                                translator.translate(TranslationKey.FeatureDetail.StatusAvailable)

                            is FeatureAvailability.ComingSoon ->
                                translator.translateWithArgs(
                                    TranslationKey.FeatureDetail.StatusInDevelopment,
                                    "phase" to a.targetPhase,
                                )

                            is FeatureAvailability.OfflineAvailable ->
                                translator.translate(TranslationKey.FeatureDetail.StatusOffline)

                            is FeatureAvailability.ConfigurationRequired ->
                                translator.translateWithArgs(
                                    TranslationKey.FeatureDetail.StatusConfigRequired,
                                    "reason" to a.reasonKey,
                                )

                            is FeatureAvailability.UpdateRequired ->
                                translator.translate(TranslationKey.FeatureDetail.StatusUpdateRequired)

                            is FeatureAvailability.UnsupportedOnPlatform ->
                                translator.translateWithArgs(
                                    TranslationKey.FeatureDetail.StatusUnsupportedPlatform,
                                    "platform" to a.platform,
                                )

                            FeatureAvailability.ResearchOnly -> "Research / Classical Reference"
                        },
                        color = AynvoraTheme.colors.textPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.sdp))

            // Domain Overview
            AynvoraTitleSmall(
                text = translator.translate(TranslationKey.FeatureDetail.DomainOverview),
                color = AynvoraTheme.colors.GoldLight,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            AynvoraSmallText(
                text = descriptor.subtitleKey,
                color = AynvoraTheme.colors.textPrimary,
            )

            Spacer(modifier = Modifier.height(14.sdp))

            // Privacy & Offline Guarantee
            AynvoraTitleSmall(
                text = translator.translate(TranslationKey.FeatureDetail.PrivacyGuaranteeTitle),
                color = AynvoraTheme.colors.GoldLight,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            AynvoraSmallText(
                text = translator.translate(TranslationKey.FeatureDetail.PrivacyGuaranteeBody),
                color = AynvoraTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(20.sdp))

            val isDirectOpenFeature =
                descriptor.id == CoreFeatureId.ASTROLOGY ||
                        descriptor.id == CoreFeatureId.GEMSTONE
            val openActionId = when (descriptor.id) {
                CoreFeatureId.GEMSTONE -> QaActionId.GEMSTONE_OPEN
                else -> QaActionId.VEDIC_ASTROLOGY_SELECT
            }

            if (isDirectOpenFeature) {
                AynvoraButton(
                    text = translator.translate(TranslationKey.FeatureDetail.FeatureOpen),
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .qaAction(openActionId),
                    onClick = {
                        onClose()
                        onOpenFeature?.invoke(descriptor.id)
                    },
                )
                Spacer(modifier = Modifier.height(10.sdp))
            }

            AynvoraButton(
                text = translator.translate(TranslationKey.FeatureDetail.UnderstoodClose),
                variant = if (isDirectOpenFeature) AynvoraButtonVariant.Outlined else AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClose,
            )
        }
    }
}
