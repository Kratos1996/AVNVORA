package com.aynvora.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.ai.AiModelSelectionStatus
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.components.AynvoraStatusChip
import com.aynvora.designsystem.components.AynvoraStatusChipVariant

/**
 * Enterprise SDK Intelligence card for On-Device AI platform management.
 *
 * Implements the core principle: "You use AYNVORA. AYNVORA decides the AI."
 * The user is never prompted for model size, quantization, or runtime parameters.
 * Visuals adhere to the SDK design system: unified hierarchy, compact vertical rhythm,
 * token-based spacing, and standardized status chips.
 */
@Composable
fun AiSetupCard(
    lifecycleState: AiModelLifecycleState,
    selectionResult: AiModelSelectionResult?,
    deviceProfile: AiDeviceProfile?,
    onDownloadClicked: () -> Unit,
    onCancelClicked: () -> Unit = {},
    onDeleteClicked: () -> Unit = {},
    onDiagnosticsClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = AynvoraTheme.isDark

    val containerColor = if (isDark) {
        AynvoraTheme.colors.CosmicNavy
    } else {
        AynvoraTheme.colors.White
    }

    val iconBoxBg = if (isDark) {
        AynvoraTheme.colors.CosmicIndigo
    } else {
        AynvoraTheme.colors.SoftGold
    }

    val borderColor = if (isDark) {
        AynvoraColors.Gold.copy(alpha = 0.28f)
    } else {
        AynvoraColors.Gold.copy(alpha = 0.40f)
    }

    val secondaryTextColor = if (isDark) {
        AynvoraTheme.colors.TextLightSecondary
    } else {
        AynvoraTheme.colors.TextSecondary
    }

    val (statusText, statusVariant) = when (lifecycleState) {
        is AiModelLifecycleState.Ready -> "100% OFFLINE" to AynvoraStatusChipVariant.Offline
        is AiModelLifecycleState.Downloading -> "DOWNLOADING" to AynvoraStatusChipVariant.Available
        is AiModelLifecycleState.VerifyingChecksum -> "VERIFYING" to AynvoraStatusChipVariant.InDevelopment
        is AiModelLifecycleState.Installing -> "INSTALLING" to AynvoraStatusChipVariant.InDevelopment
        is AiModelLifecycleState.Error -> "ATTENTION" to AynvoraStatusChipVariant.Error
        is AiModelLifecycleState.NotInstalled -> {
            if (selectionResult?.status == AiModelSelectionStatus.READY_TO_DOWNLOAD) "READY" to AynvoraStatusChipVariant.InDevelopment
            else "UNAVAILABLE" to AynvoraStatusChipVariant.Neutral
        }
    }

    AynvoraCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, AynvoraShapes.shape12),
        variant = AynvoraCardVariant.Filled,
        shape = AynvoraShapes.shape12,
        containerColor = containerColor,
        contentColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AynvoraSpacing.space16),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Icon + Brand AI Title + Status Chip
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
                            .size(38.dp)
                            .clip(AynvoraShapes.shape8)
                            .background(iconBoxBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🧠",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                        )
                    }

                    Spacer(modifier = Modifier.width(AynvoraSpacing.space12))

                    Column {
                        Text(
                            text = "AYNVORA AI",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                            color = AynvoraColors.Gold,
                        )
                        Text(
                            text = "Private On-Device Intelligence",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = secondaryTextColor,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AynvoraSpacing.space8))

                AynvoraStatusChip(
                    text = statusText,
                    variant = statusVariant,
                )
            }

            Spacer(modifier = Modifier.height(AynvoraSpacing.space10))

            // Zero-Configuration Hardware Banner
            if (deviceProfile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AynvoraShapes.shape8)
                        .background(
                            if (isDark) AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.5f)
                            else AynvoraTheme.colors.SoftGold.copy(alpha = 0.7f),
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) AynvoraColors.Gold.copy(alpha = 0.15f) else AynvoraColors.Gold.copy(
                                alpha = 0.25f
                            ),
                            shape = AynvoraShapes.shape8,
                        )
                        .padding(
                            horizontal = AynvoraSpacing.space10,
                            vertical = AynvoraSpacing.space6
                        ),
                ) {
                    Text(
                        text = "AI optimized for your device • 100% Private & On-Device",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                        color = if (isDark) AynvoraColors.GoldLight else AynvoraColors.GoldDeep,
                    )
                }
                Spacer(modifier = Modifier.height(AynvoraSpacing.space10))
            }

            // State-Specific Interactive Content
            when (lifecycleState) {
                is AiModelLifecycleState.NotInstalled -> {
                    val summary = selectionResult?.userSummary
                    if (summary != null && selectionResult.status == AiModelSelectionStatus.READY_TO_DOWNLOAD) {
                        Text(
                            text = "AYNVORA has automatically selected the optimal model package (${summary.downloadSizeFormatted}) tuned specifically for your device hardware.",
                            style = AynvoraTheme.typography.body14.copy(
                                fontSize = 13.ssp,
                                lineHeight = 18.ssp
                            ),
                            color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
                        )
                        Spacer(modifier = Modifier.height(AynvoraSpacing.space12))
                        AynvoraButton(
                            text = summary.actionLabel,
                            variant = AynvoraButtonVariant.Primary,
                            event = AynvoraClickEvent(
                                eventId = "dashboard.ai.download_clicked",
                                screenId = "dashboard",
                                componentId = "ai_setup_download",
                                payload = AynvoraEventPayload.AiDownloadPayload(
                                    modelId = selectionResult.selectedModel?.modelId
                                        ?: "recommended"
                                ),
                            ),
                            onClick = onDownloadClicked,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            text = summary?.description
                                ?: "Checking hardware capability and preparing automatic configuration...",
                            style = AynvoraTheme.typography.body14.copy(
                                fontSize = 13.ssp,
                                lineHeight = 18.ssp
                            ),
                            color = secondaryTextColor,
                        )
                    }
                }

                is AiModelLifecycleState.Downloading -> {
                    Text(
                        text = "Downloading AYNVORA AI package...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
                    )
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space8))
                    LinearProgressIndicator(
                        progress = { lifecycleState.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AynvoraColors.Gold,
                        trackColor = if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold,
                    )
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space6))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val downloadedMb = lifecycleState.bytesDownloaded / (1024L * 1024L)
                        val totalMb = lifecycleState.totalBytes / (1024L * 1024L)
                        Text(
                            text = "${lifecycleState.percentage}% ($downloadedMb / $totalMb MB)",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = secondaryTextColor,
                        )
                        Text(
                            text = "Cancel",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraColors.GoldLight,
                        )
                    }
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space10))
                    AynvoraButton(
                        text = "Cancel Download",
                        variant = AynvoraButtonVariant.Outlined,
                        event = AynvoraClickEvent(
                            eventId = "dashboard.ai.cancel_clicked",
                            screenId = "dashboard",
                            componentId = "ai_setup_cancel",
                            payload = AynvoraEventPayload.AiCancelPayload(),
                        ),
                        onClick = onCancelClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AiModelLifecycleState.VerifyingChecksum -> {
                    Text(
                        text = "Verifying cryptographic SHA-256 integrity...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraColors.Gold,
                    )
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space8))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AynvoraColors.Gold,
                        trackColor = if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold,
                    )
                }

                is AiModelLifecycleState.Installing -> {
                    Text(
                        text = "Finalizing atomic installation...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraColors.Gold,
                    )
                }

                is AiModelLifecycleState.Ready -> {
                    Text(
                        text = "AYNVORA AI is installed and fully operational offline. Contemplation for Tarot, Astrology, and Scriptures runs locally without any network requests.",
                        style = AynvoraTheme.typography.body14.copy(
                            fontSize = 13.ssp,
                            lineHeight = 18.ssp
                        ),
                        color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
                    )
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space12))
                    AynvoraButton(
                        text = "Remove AI Package",
                        variant = AynvoraButtonVariant.Outlined,
                        event = AynvoraClickEvent(
                            eventId = "dashboard.ai.delete_clicked",
                            screenId = "dashboard",
                            componentId = "ai_setup_delete",
                            payload = AynvoraEventPayload.AiDeletePayload(),
                        ),
                        onClick = onDeleteClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AiModelLifecycleState.Error -> {
                    Text(
                        text = lifecycleState.message,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraColors.Error,
                    )
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space12))
                    AynvoraButton(
                        text = "Retry Download",
                        variant = AynvoraButtonVariant.Primary,
                        event = AynvoraClickEvent(
                            eventId = "dashboard.ai.download_clicked",
                            screenId = "dashboard",
                            componentId = "ai_setup_retry",
                            payload = AynvoraEventPayload.AiRetryPayload(),
                        ),
                        onClick = onDownloadClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
