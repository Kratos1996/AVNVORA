package com.aynvora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelSelector
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.components.AynvoraLogo
import com.aynvora.designsystem.components.AynvoraLogoVariant
import com.aynvora.designsystem.components.dialogs.AynvoraDialogHost
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHeader
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHost
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
import com.aynvora.ui.features.CoreFeatureDashboard
import com.aynvora.ui.tarot.TarotRoute
import dev.ishant.cottonsheet.LocalCottonSheetController
import org.koin.compose.currentKoinScope

/**
 * Root Compose Multiplatform entry application for AYNVORA.
 *
 * Language is managed entirely by [AynvoraLocaleManager] — the single source of truth.
 * [AynvoraLocalizationProvider] wires [LocalAynvoraLocale] and [LocalAynvoraTranslator]
 * into the Compose tree so every child recomposes instantly on language change,
 * with no Activity restart and no manual if-else blocks anywhere in the UI.
 *
 * Language selection is exposed via a globe button (🌐) that presents a bottom sheet
 * listing all locales from [LanguageRegistry.availableLocales()], with the active
 * one visually highlighted.
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
            localIntelligence = koin.getOrNull<com.aynvora.core.ai.AynvoraLocalIntelligence>(),
            eventDispatcher = koin.getOrNull<com.aynvora.core.event.AynvoraEventDispatcher>(),
            initialDarkTheme = darkTheme,
        )
    }

    val state by appViewModel.uiState.collectAsState()
    val localeManager = remember(koin) { koin.getOrNull<AynvoraLocaleManager>() }

    AynvoraTheme(darkTheme = state.isDark) {
        val content = @Composable {
            androidx.compose.runtime.CompositionLocalProvider(
                com.aynvora.designsystem.event.LocalAynvoraEventDispatcher provides { event ->
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
    // Reactive locale & translator — automatically updated by AynvoraLocalizationProvider
    val locale = LocalAynvoraLocale.current
    val translator = LocalAynvoraTranslator.current

    val backgroundColor = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    val primaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

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
                            translator = translator,
                            isDark = isDark,
                            primaryTextColor = primaryTextColor,
                            secondaryTextColor = secondaryTextColor,
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
                                    com.aynvora.core.feature.CoreFeatureId.ASTROLOGY -> {
                                        onEvent(AynvoraAppUiEvent.OpenAstrology)
                                    }

                                    com.aynvora.core.feature.CoreFeatureId.GEMSTONE -> {
                                        onEvent(AynvoraAppUiEvent.OpenGemstone)
                                    }

                                    com.aynvora.core.feature.CoreFeatureId.GITA -> {
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
                    com.aynvora.ui.astrology.AstrologyRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseAstrology) },
                    )
                } else if (state.isTarotOpen) {
                    TarotRoute(
                        language = locale.localeId,
                        onClose = { onEvent(AynvoraAppUiEvent.CloseTarot) },
                    )
                } else if (state.isPalmistryOpen) {
                    com.aynvora.ui.palmistry.PalmistryRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.ClosePalmistry) },
                    )
                } else if (state.isNumerologyOpen) {
                    com.aynvora.ui.numerology.NumerologyRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseNumerology) },
                    )
                } else if (state.isGemstoneOpen) {
                    com.aynvora.ui.gemstone.GemstoneRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseGemstone) },
                    )
                } else if (state.isGitaOpen) {
                    com.aynvora.ui.gita.GitaRoute(
                        onClose = { onEvent(AynvoraAppUiEvent.CloseGita) },
                    )
                } else if (state.isAiDiagnosticsOpen) {
                    com.aynvora.ui.ai.AiDiagnosticScreen(
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
                        // ── Top Header Bar ────────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Brand Identity with Official Drawable Logo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space10),
                            ) {
                                AynvoraLogo(
                                    size = 38.dp,
                                    variant = if (isDark) AynvoraLogoVariant.Transparent else AynvoraLogoVariant.Default,
                                )
                                Column {
                                    Text(
                                        text = translator.translate(TranslationKey.App.AppName),
                                        style = AynvoraTheme.typography.title20.copy(
                                            fontSize = 20.ssp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp,
                                        ),
                                        color = AynvoraColors.Gold,
                                    )
                                    Text(
                                        text = translator.translate(TranslationKey.App.Tagline),
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                        color = secondaryTextColor,
                                    )
                                }
                            }

                            // Unified Control Group: Language Selector, Theme Toggle, Sentinel QA
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

                                // Language Selector Pill
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
                                        Text(
                                            text = "🌐",
                                            style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                                        )
                                        Text(
                                            text = locale.nativeName,
                                            style = AynvoraTheme.typography.caption12.copy(
                                                fontSize = 11.ssp,
                                                fontWeight = FontWeight.Medium,
                                            ),
                                            color = primaryTextColor,
                                        )
                                    }
                                }

                                // Theme Switcher Button
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
                                    Text(
                                        text = if (isDark) "🌙" else "☀️",
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                                    )
                                }

                                // Interaction Sentinel QA Dashboard Button
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
                                    Text(
                                        text = "🛡️",
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(AynvoraSpacing.space12))

                        // ── Central Feature Dashboard ──────────────────────────────
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

/**
 * Bottom sheet listing all locales from [LanguageRegistry.availableLocales()].
 * The active locale row is highlighted with a gold tinted background,
 * so the user can immediately see which language is currently selected.
 */
@Composable
private fun LanguagePickerSheet(
    currentLocale: SupportedLocale,
    onLocaleSelected: (SupportedLocale) -> Unit,
    onClose: () -> Unit,
    translator: AynvoraTranslator,
    isDark: Boolean,
    primaryTextColor: Color,
    secondaryTextColor: Color,
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
                    isDark = isDark,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    onClick = { onLocaleSelected(locale) },
                )
            }

            // Bottom padding inside the list
            item { Spacer(modifier = Modifier.height(8.sdp)) }
        }
    }
}

@Composable
private fun LanguageRow(
    locale: SupportedLocale,
    isSelected: Boolean,
    isDark: Boolean,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit,
) {
    val bgColor = if (isSelected) {
        AynvoraTheme.colors.Gold.copy(alpha = if (isDark) 0.15f else 0.22f)
    } else {
        Color.Transparent
    }
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
            Text(
                text = locale.nativeName,
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = if (isSelected) AynvoraTheme.colors.Gold else primaryTextColor,
            )
            Spacer(modifier = Modifier.height(2.sdp))
            Text(
                text = locale.englishName,
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = secondaryTextColor,
            )
        }

        if (isSelected) {
            Text(
                text = "✓",
                style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                color = AynvoraTheme.colors.Gold,
            )
        }
    }
}

// ── Feature Foundation Detail Sheet ──────────────────────────────────────────

/**
 * Bottom sheet displaying the architectural foundation and privacy commitments of a core feature.
 * All strings are resolved via [LocalAynvoraTranslator] — zero hardcoded language checks.
 */
@Composable
fun FeatureFoundationDetailSheet(
    descriptor: com.aynvora.core.feature.CoreFeatureDescriptor,
    onClose: () -> Unit,
    onOpenFeature: ((com.aynvora.core.feature.CoreFeatureId) -> Unit)? = null,
) {
    val isDark = AynvoraTheme.isDark
    val translator = LocalAynvoraTranslator.current

    val primaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

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
                contentColor = primaryTextColor,
            ) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = translator.translate(TranslationKey.FeatureDetail.OperationalStatus),
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
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
                        },
                        style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                        color = primaryTextColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.sdp))

            // Domain Overview
            Text(
                text = translator.translate(TranslationKey.FeatureDetail.DomainOverview),
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = AynvoraTheme.colors.GoldLight,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(
                text = descriptor.subtitleKey,
                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                color = primaryTextColor,
            )

            Spacer(modifier = Modifier.height(14.sdp))

            // Privacy & Offline Guarantee
            Text(
                text = translator.translate(TranslationKey.FeatureDetail.PrivacyGuaranteeTitle),
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = AynvoraTheme.colors.GoldLight,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(
                text = translator.translate(TranslationKey.FeatureDetail.PrivacyGuaranteeBody),
                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                color = secondaryTextColor,
            )

            Spacer(modifier = Modifier.height(20.sdp))

            val isDirectOpenFeature =
                descriptor.id == com.aynvora.core.feature.CoreFeatureId.ASTROLOGY ||
                        descriptor.id == com.aynvora.core.feature.CoreFeatureId.GEMSTONE
            val openActionId = when (descriptor.id) {
                com.aynvora.core.feature.CoreFeatureId.GEMSTONE -> QaActionId.GEMSTONE_OPEN
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

