package com.aynvora.ui.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureDescriptor
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraFeatureCard
import com.aynvora.designsystem.components.AynvoraStatusChipVariant
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.ui.ai.AiSetupCard

/**
 * Core Product Dashboard displaying all 14 first-class features with typed availability badges,
 * full multi-locale localization, and standardized SDK feature cards.
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
    onAiDiagnosticsClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val translator = LocalAynvoraTranslator.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AynvoraSpacing.space12),
        contentPadding = PaddingValues(bottom = AynvoraSpacing.space24),
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
                    onDiagnosticsClicked = onAiDiagnosticsClicked,
                )
            }
        }

        items(CanonicalCoreFeatures, key = { it.id.name }) { feature ->
            val featureTitle = resolveFeatureTitle(feature, translator)
            val featureSubtitle = resolveFeatureSubtitle(feature, translator)
            val badgeLabel = resolveBadgeLabel(feature.availability, translator)
            val buttonLabel = resolveButtonLabel(feature.availability, translator)
            val iconGlyph = getFeatureGlyph(feature.id)

            AynvoraFeatureCard(
                title = featureTitle,
                description = featureSubtitle,
                iconGlyph = iconGlyph,
                statusLabel = badgeLabel,
                statusVariant = resolveStatusVariant(feature.availability),
                buttonLabel = buttonLabel,
                buttonVariant = when (feature.availability) {
                    is FeatureAvailability.Available -> AynvoraButtonVariant.Primary
                    else -> AynvoraButtonVariant.Outlined
                },
                buttonEvent = AynvoraClickEvent(
                    eventId = "dashboard.feature.open_clicked",
                    screenId = "dashboard",
                    componentId = "feature_card_${feature.id.name.lowercase()}",
                    payload = AynvoraEventPayload.FeatureOpenPayload(feature.id),
                ),
                onButtonClick = { onFeatureSelected(feature.id) },
            )
        }
    }
}

private fun resolveFeatureTitle(
    feature: CoreFeatureDescriptor,
    translator: AynvoraTranslator,
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
): String {
    val key = when (feature.id) {
        CoreFeatureId.PALMISTRY -> "palmistry.subtitle"
        CoreFeatureId.GEMSTONE -> "gemstone.subtitle"
        CoreFeatureId.GITA -> "gita.subtitle"
        CoreFeatureId.GARUDA_PURAN -> "garuda.subtitle"
        CoreFeatureId.LAL_KITAB -> "lalkitab.subtitle"
        CoreFeatureId.AI_ASSISTANT -> "ai.subtitle"
        CoreFeatureId.DAILY_GUIDANCE -> "guidance.subtitle"
        CoreFeatureId.WALLPAPER -> "wallpaper.subtitle"
        else -> "feature.${feature.id.name.lowercase()}.subtitle"
    }
    val resolved = translator.resolve(key)
    return if (resolved.startsWith("[MISSING:") || resolved == key) {
        feature.subtitleKey
    } else {
        resolved
    }
}

private fun resolveStatusVariant(availability: FeatureAvailability): AynvoraStatusChipVariant {
    return when (availability) {
        is FeatureAvailability.Available -> AynvoraStatusChipVariant.Available
        is FeatureAvailability.OfflineAvailable -> AynvoraStatusChipVariant.Offline
        is FeatureAvailability.ComingSoon -> AynvoraStatusChipVariant.InDevelopment
        is FeatureAvailability.ConfigurationRequired -> AynvoraStatusChipVariant.Warning
        is FeatureAvailability.UpdateRequired -> AynvoraStatusChipVariant.Warning
        is FeatureAvailability.UnsupportedOnPlatform -> AynvoraStatusChipVariant.Error
        is FeatureAvailability.ResearchOnly -> AynvoraStatusChipVariant.InDevelopment
    }
}

private fun resolveBadgeLabel(
    availability: FeatureAvailability,
    translator: AynvoraTranslator
): String {
    return when (availability) {
        is FeatureAvailability.Available -> translator.translate(TranslationKey.FeatureDetail.StatusAvailable)
        is FeatureAvailability.ComingSoon -> translator.translateWithArgs(
            TranslationKey.FeatureDetail.StatusInDevelopment,
            "phase" to availability.targetPhase,
        )

        is FeatureAvailability.OfflineAvailable -> translator.translate(TranslationKey.FeatureDetail.StatusOffline)
        is FeatureAvailability.ConfigurationRequired -> translator.translate(TranslationKey.FeatureDetail.StatusSetupRequired)
        is FeatureAvailability.UpdateRequired -> translator.translate(TranslationKey.FeatureDetail.StatusUpdateRequired)
        is FeatureAvailability.UnsupportedOnPlatform -> translator.translate(TranslationKey.FeatureDetail.StatusUnsupported)
        is FeatureAvailability.ResearchOnly -> "Research Only"
    }
}

private fun resolveButtonLabel(
    availability: FeatureAvailability,
    translator: AynvoraTranslator
): String {
    return when (availability) {
        is FeatureAvailability.Available -> translator.translate(TranslationKey.FeatureDetail.FeatureOpen)
        else -> translator.translate(TranslationKey.FeatureDetail.FeatureViewFoundation)
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
