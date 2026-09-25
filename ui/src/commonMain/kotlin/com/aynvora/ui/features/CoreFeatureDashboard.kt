package com.aynvora.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureDescriptor
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.ui.ai.AiSetupCard

/**
 * Core Product Dashboard displaying all 14 first-class features with typed availability badges,
 * full Hindi/English bilingual localization, and adaptive theme styling.
 */
@Composable
fun CoreFeatureDashboard(
    onFeatureSelected: (CoreFeatureId) -> Unit,
    language: String = "en",
    aiLifecycleState: AiModelLifecycleState? = null,
    aiSelectionResult: AiModelSelectionResult? = null,
    aiDeviceProfile: AiDeviceProfile? = null,
    onDownloadAiClicked: () -> Unit = {},
    onCancelAiClicked: () -> Unit = {},
    onDeleteAiClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isDark = AynvoraTheme.isDark
    val translator = AynvoraTranslator(LanguageRegistry.getLocaleOrDefault(language))

    val cardContainerColor = if (isDark) {
        AynvoraTheme.colors.CosmicNavy
    } else {
        AynvoraTheme.colors.White
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

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.sdp),
    ) {
        if (aiLifecycleState != null) {
            item(key = "ai_setup_header") {
                AiSetupCard(
                    lifecycleState = aiLifecycleState,
                    selectionResult = aiSelectionResult,
                    deviceProfile = aiDeviceProfile,
                    onDownloadClicked = onDownloadAiClicked,
                    onCancelClicked = onCancelAiClicked,
                    onDeleteClicked = onDeleteAiClicked,
                )
            }
        }

        items(CanonicalCoreFeatures, key = { it.id.name }) { feature ->
            val featureTitle = resolveFeatureTitle(feature, translator, language)
            val featureSubtitle = resolveFeatureSubtitle(feature, translator, language)
            val badgeLabel = resolveBadgeLabel(feature.availability, language)
            val buttonLabel = resolveButtonLabel(feature.availability, language)
            val iconGlyph = getFeatureGlyph(feature.id)

            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
                containerColor = cardContainerColor,
                contentColor = primaryTextColor,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = iconGlyph,
                                    style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                                )
                            }

                            Spacer(modifier = Modifier.width(10.sdp))

                            Text(
                                text = featureTitle,
                                style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                                color = AynvoraTheme.colors.Gold,
                            )
                        }

                        val badgeColor = when (feature.availability) {
                            is FeatureAvailability.Available -> AynvoraTheme.colors.CelestialBlue
                            is FeatureAvailability.OfflineAvailable -> AynvoraTheme.colors.Success
                            else -> AynvoraTheme.colors.GoldLight
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.sdp, vertical = 4.sdp),
                        ) {
                            Text(
                                text = badgeLabel,
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                                color = badgeColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.sdp))

                    Text(
                        text = featureSubtitle,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = secondaryTextColor,
                    )

                    Spacer(modifier = Modifier.height(14.sdp))

                    AynvoraButton(
                        text = buttonLabel,
                        variant = when (feature.availability) {
                            is FeatureAvailability.Available -> AynvoraButtonVariant.Primary
                            else -> AynvoraButtonVariant.Outlined
                        },
                        onClick = { onFeatureSelected(feature.id) },
                    )
                }
            }
        }
    }
}

private fun resolveFeatureTitle(
    feature: CoreFeatureDescriptor,
    translator: AynvoraTranslator,
    language: String,
): String {
    val key = when (feature.id) {
        CoreFeatureId.ASTROLOGY -> "feature.astrology"
        CoreFeatureId.TAROT -> "feature.tarot"
        CoreFeatureId.NUMEROLOGY -> "feature.numerology"
        CoreFeatureId.PALMISTRY -> "feature.palmistry"
        CoreFeatureId.GEMSTONE -> "feature.gemstone"
        CoreFeatureId.GITA -> "feature.gita"
        CoreFeatureId.GARUDA_PURAN -> "feature.garuda_puran"
        CoreFeatureId.LAL_KITAB -> "feature.lal_kitab"
        CoreFeatureId.AI_ASSISTANT -> "feature.ai_assistant"
        CoreFeatureId.DAILY_GUIDANCE -> "feature.daily_guidance"
        CoreFeatureId.WALLPAPER -> "feature.wallpaper"
        CoreFeatureId.RUDRAKSHA -> "feature.rudraksha"
        CoreFeatureId.JADI -> "feature.jadi"
        CoreFeatureId.YANTRA -> "feature.yantra"
    }
    val resolved = translator.resolve(key)
    return if (resolved.startsWith("[MISSING:") || resolved == key) {
        feature.titleKey
    } else {
        resolved
    }
}

private fun resolveFeatureSubtitle(
    feature: CoreFeatureDescriptor,
    translator: AynvoraTranslator,
    language: String,
): String {
    if (language != "hi") return feature.subtitleKey

    return when (feature.id) {
        CoreFeatureId.ASTROLOGY -> "निश्चित जन्म कुंडली, दशा समय, गोचर, पंचांग, व्याख्या इंजन और 16 वर्ग।"
        CoreFeatureId.TAROT -> "गहन प्रतीकात्मक चिंतन, 78-पत्तों की प्रामाणिक गड्डी और पूर्णतः ऑफ़लाइन आत्म-निरीक्षण।"
        CoreFeatureId.NUMEROLOGY -> "मूलांक, भाग्यांक, नामांक, व्यक्तिगत वर्ष और जीवन शिखर काल।"
        CoreFeatureId.PALMISTRY -> translator.resolve("palmistry.subtitle")
        CoreFeatureId.GEMSTONE -> translator.resolve("gemstone.subtitle")
        CoreFeatureId.GITA -> translator.resolve("gita.subtitle")
        CoreFeatureId.GARUDA_PURAN -> translator.resolve("garuda.subtitle")
        CoreFeatureId.LAL_KITAB -> translator.resolve("lalkitab.subtitle")
        CoreFeatureId.AI_ASSISTANT -> translator.resolve("ai.subtitle")
        CoreFeatureId.DAILY_GUIDANCE -> translator.resolve("guidance.subtitle")
        CoreFeatureId.WALLPAPER -> translator.resolve("wallpaper.subtitle")
        CoreFeatureId.RUDRAKSHA -> "पारंपरिक मुखी आधारित रुद्राक्ष मार्गदर्शन, ग्रहीय संबंध एवं स्रोत प्रामाणिकता।"
        CoreFeatureId.JADI -> "वैदिक परंपरा से पारंपरिक वनस्पति मूल उपाय। विशुद्ध गैर-चिकित्सीय परामर्श।"
        CoreFeatureId.YANTRA -> "साधना एवं उपासना हेतु पवित्र ज्यामितीय यंत्र आरेख।"
    }
}

private fun resolveBadgeLabel(availability: FeatureAvailability, language: String): String {
    val isHi = language == "hi"
    return when (availability) {
        is FeatureAvailability.Available -> if (isHi) "उपलब्ध" else "AVAILABLE"
        is FeatureAvailability.ComingSoon -> if (isHi) "आधारभूत तैयार" else "FOUNDATION READY"
        is FeatureAvailability.OfflineAvailable -> if (isHi) "ऑफ़लाइन" else "OFFLINE"
        is FeatureAvailability.ConfigurationRequired -> if (isHi) "सेटअप" else "SETUP"
        is FeatureAvailability.UpdateRequired -> if (isHi) "अपडेट" else "UPDATE"
        is FeatureAvailability.UnsupportedOnPlatform -> if (isHi) "असमर्थित" else "UNSUPPORTED"
    }
}

private fun resolveButtonLabel(availability: FeatureAvailability, language: String): String {
    val isHi = language == "hi"
    return when (availability) {
        is FeatureAvailability.Available -> if (isHi) "सुविधा खोलें" else "Open Feature"
        else -> if (isHi) "संरचना देखें" else "View Foundation"
    }
}

private fun getFeatureGlyph(id: CoreFeatureId): String {
    return when (id) {
        CoreFeatureId.ASTROLOGY -> "🪐"
        CoreFeatureId.TAROT -> "🎴"
        CoreFeatureId.NUMEROLOGY -> "🔢"
        CoreFeatureId.PALMISTRY -> "✋"
        CoreFeatureId.GEMSTONE -> "💎"
        CoreFeatureId.GITA -> "📜"
        CoreFeatureId.GARUDA_PURAN -> "🪶"
        CoreFeatureId.LAL_KITAB -> "📕"
        CoreFeatureId.AI_ASSISTANT -> "🧠"
        CoreFeatureId.DAILY_GUIDANCE -> "☀️"
        CoreFeatureId.WALLPAPER -> "🖼️"
        CoreFeatureId.RUDRAKSHA -> "📿"
        CoreFeatureId.JADI -> "🌿"
        CoreFeatureId.YANTRA -> "☸️"
    }
}
