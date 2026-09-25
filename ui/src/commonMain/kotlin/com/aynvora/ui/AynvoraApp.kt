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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aynvora.core.ai.AiDeviceCapabilityDetector
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiModelLifecycleManager
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.ai.AiModelSelector
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
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
import com.aynvora.designsystem.localization.AynvoraLocalizationProvider
import com.aynvora.designsystem.localization.LocalAynvoraLocale
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.ui.features.CoreFeatureDashboard
import com.aynvora.ui.tarot.TarotRoute
import dev.ishant.cottonsheet.LocalCottonSheetController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
fun AynvoraApp(darkTheme: Boolean = true) {
    var isDark by remember { mutableStateOf(darkTheme) }
    var isTarotOpen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val koin = currentKoinScope()

    // Locale manager — single source of truth for language
    val localeManager = remember(koin) { koin.getOrNull<AynvoraLocaleManager>() }

    val aiLifecycleManager = remember(koin) { koin.getOrNull<AiModelLifecycleManager>() }
    val aiModelSelector = remember(koin) { koin.getOrNull<AiModelSelector>() }
    val aiCapabilityDetector = remember(koin) { koin.getOrNull<AiDeviceCapabilityDetector>() }

    val aiLifecycleState by (aiLifecycleManager?.state ?: remember {
        MutableStateFlow(AiModelLifecycleState.NotInstalled)
    }).collectAsState()

    var deviceProfile by remember { mutableStateOf<AiDeviceProfile?>(null) }
    var selectionResult by remember { mutableStateOf<AiModelSelectionResult?>(null) }

    LaunchedEffect(aiCapabilityDetector, aiModelSelector) {
        if (aiCapabilityDetector != null && aiModelSelector != null) {
            try {
                val profile = aiCapabilityDetector.detectCapability()
                deviceProfile = profile
                selectionResult = aiModelSelector.selectOptimalModel(profile)
            } catch (_: Exception) {
                // Keep null on unsupported platforms
            }
        }
    }

    AynvoraTheme(darkTheme = isDark) {
        // AynvoraLocalizationProvider observes localeManager.currentLocale StateFlow
        // and provides LocalAynvoraLocale + LocalAynvoraTranslator to the entire tree.
        // If no localeManager is available (e.g. preview), the CompositionLocals use
        // their default English values from LocalLocale.kt.
        if (localeManager != null) {
            AynvoraLocalizationProvider(localeManager = localeManager) {
                AynvoraAppContent(
                    isDark = isDark,
                    isTarotOpen = isTarotOpen,
                    aiLifecycleState = aiLifecycleState,
                    selectionResult = selectionResult,
                    deviceProfile = deviceProfile,
                    onToggleTheme = { isDark = !isDark },
                    onTarotOpen = { isTarotOpen = true },
                    onTarotClose = { isTarotOpen = false },
                    onDownloadAi = {
                        selectionResult?.selectedModel?.let { model ->
                            coroutineScope.launch { aiLifecycleManager?.downloadAndInstall(model) }
                        }
                    },
                    onCancelAi = { coroutineScope.launch { aiLifecycleManager?.cancelDownload() } },
                    onDeleteAi = { coroutineScope.launch { aiLifecycleManager?.deleteInstalledModel() } },
                    onSelectLocale = { locale ->
                        coroutineScope.launch { localeManager.setLocale(locale) }
                    },
                )
            }
        } else {
            // Fallback: no locale manager — CompositionLocals fall back to English defaults
            AynvoraAppContent(
                isDark = isDark,
                isTarotOpen = isTarotOpen,
                aiLifecycleState = aiLifecycleState,
                selectionResult = selectionResult,
                deviceProfile = deviceProfile,
                onToggleTheme = { isDark = !isDark },
                onTarotOpen = { isTarotOpen = true },
                onTarotClose = { isTarotOpen = false },
                onDownloadAi = {
                    selectionResult?.selectedModel?.let { model ->
                        coroutineScope.launch { aiLifecycleManager?.downloadAndInstall(model) }
                    }
                },
                onCancelAi = { coroutineScope.launch { aiLifecycleManager?.cancelDownload() } },
                onDeleteAi = { coroutineScope.launch { aiLifecycleManager?.deleteInstalledModel() } },
                onSelectLocale = { /* no-op: no locale manager */ },
            )
        }
    }
}

@Composable
private fun AynvoraAppContent(
    isDark: Boolean,
    isTarotOpen: Boolean,
    aiLifecycleState: AiModelLifecycleState,
    selectionResult: AiModelSelectionResult?,
    deviceProfile: AiDeviceProfile?,
    onToggleTheme: () -> Unit,
    onTarotOpen: () -> Unit,
    onTarotClose: () -> Unit,
    onDownloadAi: () -> Unit,
    onCancelAi: () -> Unit,
    onDeleteAi: () -> Unit,
    onSelectLocale: (SupportedLocale) -> Unit,
) {
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

            if (isTarotOpen) {
                TarotRoute(
                    language = locale.localeId,
                    onClose = onTarotClose,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(horizontal = 20.sdp, vertical = 16.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // ── Top Header Bar ────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Brand Identity with Official Drawable Logo
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AynvoraLogo(
                                size = 46.dp,
                                variant = if (isDark) AynvoraLogoVariant.Transparent else AynvoraLogoVariant.Default,
                            )
                            Spacer(modifier = Modifier.width(12.sdp))
                            Column {
                                Text(
                                    text = translator.translate(TranslationKey.App.AppName),
                                    style = AynvoraTheme.typography.display36.copy(fontSize = 24.ssp),
                                    color = AynvoraTheme.colors.Gold,
                                )
                                Text(
                                    // Resolves via translation catalog — no if/else needed
                                    text = translator.translate(TranslationKey.App.Tagline),
                                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                    color = secondaryTextColor,
                                )
                            }
                        }

                        // Controls: Language Selector & Theme Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.sdp),
                        ) {
                            // Globe button → opens language picker bottom sheet
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isDark) AynvoraTheme.colors.CosmicNavy
                                        else AynvoraTheme.colors.SoftGold,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable {
                                        sheetController.show(sheetParams) { dismiss ->
                                            LanguagePickerSheet(
                                                currentLocale = locale,
                                                onLocaleSelected = { selected ->
                                                    onSelectLocale(selected)
                                                    dismiss()
                                                },
                                                onClose = dismiss,
                                                translator = translator,
                                                isDark = isDark,
                                                primaryTextColor = primaryTextColor,
                                                secondaryTextColor = secondaryTextColor,
                                            )
                                        }
                                    }
                                    .padding(horizontal = 12.sdp, vertical = 8.sdp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.sdp),
                                ) {
                                    Text(
                                        text = "🌐",
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                                    )
                                    Text(
                                        text = locale.nativeName,
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                        color = primaryTextColor,
                                    )
                                }
                            }

                            // Theme Switcher Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isDark) AynvoraTheme.colors.CosmicNavy
                                        else AynvoraTheme.colors.SoftGold,
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(18.dp),
                                    )
                                    .clickable(onClick = onToggleTheme),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (isDark) "🌙" else "☀️",
                                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.sdp))

                    // ── Central Feature Dashboard ──────────────────────────────
                    CoreFeatureDashboard(
                        language = locale.localeId,
                        aiLifecycleState = aiLifecycleState,
                        aiSelectionResult = selectionResult,
                        aiDeviceProfile = deviceProfile,
                        onDownloadAiClicked = onDownloadAi,
                        onCancelAiClicked = onCancelAi,
                        onDeleteAiClicked = onDeleteAi,
                        onFeatureSelected = { featureId ->
                            if (featureId == CoreFeatureId.TAROT) {
                                onTarotOpen()
                            } else {
                                val desc = CanonicalCoreFeatures.firstOrNull { it.id == featureId }
                                if (desc != null) {
                                    sheetController.show(sheetParams) { dismiss ->
                                        FeatureFoundationDetailSheet(
                                            descriptor = desc,
                                            onClose = dismiss,
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
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
            .clickable(onClick = onClick)
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

            AynvoraButton(
                text = translator.translate(TranslationKey.FeatureDetail.UnderstoodClose),
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClose,
            )
        }
    }
}

