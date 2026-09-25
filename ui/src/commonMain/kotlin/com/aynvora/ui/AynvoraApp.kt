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
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.ui.features.CoreFeatureDashboard
import com.aynvora.ui.tarot.TarotRoute
import dev.ishant.cottonsheet.LocalCottonSheetController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.currentKoinScope

/**
 * Root Compose Multiplatform entry application for AYNVORA with adaptive multi-device support,
 * official brand logo integration, top-level language switcher (English / हिन्दी),
 * theme controls, and full core product feature catalog.
 */
@Composable
fun AynvoraApp(darkTheme: Boolean = true) {
    var isDark by remember { mutableStateOf(darkTheme) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var isTarotOpen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val koin = currentKoinScope()

    val aiLifecycleManager = remember(koin) { koin.getOrNull<AiModelLifecycleManager>() }
    val aiModelSelector = remember(koin) { koin.getOrNull<AiModelSelector>() }
    val aiCapabilityDetector = remember(koin) { koin.getOrNull<AiDeviceCapabilityDetector>() }

    val aiLifecycleState by (aiLifecycleManager?.state ?: remember {
        MutableStateFlow(
            AiModelLifecycleState.NotInstalled
        )
    })
        .collectAsState()

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
        val backgroundColor = if (isDark) {
            AynvoraTheme.colors.CosmicBlack
        } else {
            AynvoraTheme.colors.Ivory
        }

        val primaryTextColor = if (isDark) {
            AynvoraTheme.colors.TextLight
        } else {
            AynvoraTheme.colors.TextDark
        }

        val secondaryTextColor = if (isDark) {
            AynvoraTheme.colors.TextLightSecondary
        } else {
            AynvoraTheme.colors.TextSecondary
        }

        AynvoraBottomSheetHost {
            AynvoraDialogHost {
                val sheetController = LocalCottonSheetController.current
                val sheetParams = AynvoraBottomSheetDefaults.params()

                if (isTarotOpen) {
                    TarotRoute(
                        language = selectedLanguage,
                        onClose = { isTarotOpen = false },
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .padding(horizontal = 20.sdp, vertical = 16.sdp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Top Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Brand Identity with Official Drawable Logo
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AynvoraLogo(
                                    size = 46.dp,
                                    variant = if (isDark) AynvoraLogoVariant.Transparent else AynvoraLogoVariant.Default,
                                )

                                Spacer(modifier = Modifier.width(12.sdp))

                                Column {
                                    Text(
                                        text = "AYNVORA",
                                        style = AynvoraTheme.typography.display36.copy(fontSize = 24.ssp),
                                        color = AynvoraTheme.colors.Gold,
                                    )
                                    Text(
                                        text = if (selectedLanguage == "hi") {
                                            "प्राचीन ज्ञान। स्पष्ट निर्णय।"
                                        } else {
                                            "Ancient Wisdom. Clearer Choices."
                                        },
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                        color = secondaryTextColor,
                                    )
                                }
                            }

                            // Controls: Language Toggle & Dark/Light Theme Toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.sdp),
                            ) {
                                // Language Switcher Pill (EN | हिन्दी)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                        .border(
                                            width = 1.dp,
                                            color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(20.dp),
                                        )
                                        .padding(2.sdp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // English button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (selectedLanguage == "en") AynvoraTheme.colors.Gold else androidx.compose.ui.graphics.Color.Transparent
                                            )
                                            .clickable { selectedLanguage = "en" }
                                            .padding(horizontal = 10.sdp, vertical = 5.sdp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "EN",
                                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                            color = if (selectedLanguage == "en") AynvoraTheme.colors.CosmicBlack else primaryTextColor,
                                        )
                                    }

                                    // Hindi button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (selectedLanguage == "hi") AynvoraTheme.colors.Gold else androidx.compose.ui.graphics.Color.Transparent
                                            )
                                            .clickable { selectedLanguage = "hi" }
                                            .padding(horizontal = 10.sdp, vertical = 5.sdp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "हिन्दी",
                                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                            color = if (selectedLanguage == "hi") AynvoraTheme.colors.CosmicBlack else primaryTextColor,
                                        )
                                    }
                                }

                                // Theme Switcher Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                        .border(
                                            width = 1.dp,
                                            color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(18.dp),
                                        )
                                        .clickable { isDark = !isDark },
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

                        // Central Feature Dashboard
                        CoreFeatureDashboard(
                            language = selectedLanguage,
                            aiLifecycleState = aiLifecycleState,
                            aiSelectionResult = selectionResult,
                            aiDeviceProfile = deviceProfile,
                            onDownloadAiClicked = {
                                selectionResult?.selectedModel?.let { model ->
                                    coroutineScope.launch {
                                        aiLifecycleManager?.downloadAndInstall(model)
                                    }
                                }
                            },
                            onCancelAiClicked = {
                                coroutineScope.launch {
                                    aiLifecycleManager?.cancelDownload()
                                }
                            },
                            onDeleteAiClicked = {
                                coroutineScope.launch {
                                    aiLifecycleManager?.deleteInstalledModel()
                                }
                            },
                            onFeatureSelected = { featureId ->
                                if (featureId == CoreFeatureId.TAROT) {
                                    isTarotOpen = true
                                } else {
                                    val desc =
                                        CanonicalCoreFeatures.firstOrNull { it.id == featureId }
                                    if (desc != null) {
                                        sheetController.show(sheetParams) { dismiss ->
                                            FeatureFoundationDetailSheet(
                                                descriptor = desc,
                                                language = selectedLanguage,
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
}

/**
 * Bottom sheet displaying the architectural foundation and privacy commitments of a core feature.
 */
@Composable
private fun FeatureFoundationDetailSheet(
    descriptor: com.aynvora.core.feature.CoreFeatureDescriptor,
    language: String,
    onClose: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val translator = AynvoraTranslator(LanguageRegistry.getLocaleOrDefault(language))
    val isHi = language == "hi"

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
            subtitle = if (isHi) "वास्तुकला एवं आधारभूत संरचना" else "Architecture & Foundation",
            onCloseClick = onClose,
        )

        Column(modifier = Modifier.padding(horizontal = 24.sdp)) {
            // Status Card
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Outlined,
                containerColor = if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold,
                contentColor = primaryTextColor,
            ) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = if (isHi) "सक्रिय परिचालन स्थिति" else "Operational Status",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = when (val a = descriptor.availability) {
                            is FeatureAvailability.Available -> if (isHi) "उपलब्ध (पूर्णतः स्थापित)" else "Available (Fully Installed)"
                            is FeatureAvailability.ComingSoon -> if (isHi) "विकासशील — ${a.targetPhase}" else "In Development — ${a.targetPhase}"
                            is FeatureAvailability.OfflineAvailable -> if (isHi) "ऑफ़लाइन उपलब्ध" else "Offline Available"
                            is FeatureAvailability.ConfigurationRequired -> if (isHi) "कॉन्फ़िगरेशन आवश्यक: ${a.reasonKey}" else "Setup Required: ${a.reasonKey}"
                            is FeatureAvailability.UpdateRequired -> if (isHi) "संस्करण अपडेट आवश्यक" else "Update Required"
                            is FeatureAvailability.UnsupportedOnPlatform -> if (isHi) "इस प्लेटफ़ॉर्म पर असमर्थित" else "Unsupported on ${a.platform}"
                        },
                        style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                        color = primaryTextColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.sdp))

            // Architectural Overview
            Text(
                text = if (isHi) "डोमेन अवलोकन" else "Domain Overview",
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

            // Privacy & Governance Guarantee
            Text(
                text = if (isHi) "गोपनीयता एवं ऑफ़लाइन गारंटी" else "Privacy & Local Execution Guarantee",
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = AynvoraTheme.colors.GoldLight,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(
                text = if (isHi) {
                    "AYNVORA के सभी घटक 100% ऑन-डिवाइस कार्य करते हैं। कोई भी व्यक्तिगत डेटा, जन्म विवरण या छवि कभी भी किसी रिमोट सर्वर पर नहीं भेजी जाती।"
                } else {
                    "All AYNVORA domains execute 100% on-device. Zero telemetry, zero cloud telemetry, and zero birth or biometric data ever leave this device."
                },
                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                color = secondaryTextColor,
            )

            Spacer(modifier = Modifier.height(20.sdp))

            AynvoraButton(
                text = if (isHi) "समझ गया — बंद करें" else "Understood — Close",
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClose,
            )
        }
    }
}
