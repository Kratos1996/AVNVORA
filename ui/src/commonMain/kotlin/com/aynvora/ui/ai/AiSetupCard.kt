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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.core.ai.AiModelSelectionStatus
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant

/**
 * Single-touch UI card for On-Device AI platform management.
 *
 * Implements the core principle: "You use AYNVORA. AYNVORA decides the AI."
 * The user is never prompted for model size, quantization, or runtime parameters.
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
    AynvoraCard(
        modifier = modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "AYNVORA AI",
                        style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "Private On-Device Intelligence",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }

                val statusBadgeText = when (lifecycleState) {
                    is AiModelLifecycleState.Ready -> "100% OFFLINE"
                    is AiModelLifecycleState.Downloading -> "DOWNLOADING"
                    is AiModelLifecycleState.VerifyingChecksum -> "VERIFYING"
                    is AiModelLifecycleState.Installing -> "INSTALLING"
                    is AiModelLifecycleState.Error -> "ATTENTION"
                    is AiModelLifecycleState.NotInstalled -> {
                        if (selectionResult?.status == AiModelSelectionStatus.READY_TO_DOWNLOAD) "READY"
                        else "UNAVAILABLE"
                    }
                }

                val statusBadgeColor = when (lifecycleState) {
                    is AiModelLifecycleState.Ready -> AynvoraTheme.colors.CelestialBlue
                    is AiModelLifecycleState.Downloading,
                    is AiModelLifecycleState.VerifyingChecksum,
                    is AiModelLifecycleState.Installing -> AynvoraTheme.colors.Gold

                    is AiModelLifecycleState.Error -> Color(0xFFEF5350)
                    is AiModelLifecycleState.NotInstalled -> AynvoraTheme.colors.TextLightSecondary
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, statusBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = statusBadgeText,
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                        color = statusBadgeColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.sdp))

            // Zero-Configuration Consumer Device Banner
            if (deviceProfile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            AynvoraTheme.colors.Gold.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.sdp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "AI optimized for your device • 100% Private & On-Device",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.GoldLight,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.sdp))
            }

            // State-Specific Interactive Content
            when (lifecycleState) {
                is AiModelLifecycleState.NotInstalled -> {
                    val summary = selectionResult?.userSummary
                    if (summary != null && selectionResult.status == AiModelSelectionStatus.READY_TO_DOWNLOAD) {
                        Text(
                            text = "AYNVORA has automatically selected the optimal model package (${summary.downloadSizeFormatted}) tuned specifically for your device hardware.",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = AynvoraTheme.colors.TextLight,
                        )
                        Spacer(modifier = Modifier.height(14.sdp))
                        AynvoraButton(
                            text = summary.actionLabel,
                            variant = AynvoraButtonVariant.Primary,
                            onClick = onDownloadClicked,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            text = summary?.description
                                ?: "Checking hardware capability and preparing automatic configuration...",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                }

                is AiModelLifecycleState.Downloading -> {
                    Text(
                        text = "Downloading AYNVORA AI package...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.TextLight,
                    )
                    Spacer(modifier = Modifier.height(8.sdp))
                    LinearProgressIndicator(
                        progress = { lifecycleState.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.sdp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AynvoraTheme.colors.Gold,
                        trackColor = AynvoraTheme.colors.CosmicNavy,
                    )
                    Spacer(modifier = Modifier.height(6.sdp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val downloadedMb = lifecycleState.bytesDownloaded / (1024L * 1024L)
                        val totalMb = lifecycleState.totalBytes / (1024L * 1024L)
                        Text(
                            text = "${lifecycleState.percentage}% ($downloadedMb / $totalMb MB)",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                        Text(
                            text = "Cancel",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.GoldLight,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.sdp))
                    AynvoraButton(
                        text = "Cancel Download",
                        variant = AynvoraButtonVariant.Outlined,
                        onClick = onCancelClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AiModelLifecycleState.VerifyingChecksum -> {
                    Text(
                        text = "Verifying cryptographic SHA-256 integrity...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(8.sdp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.sdp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AynvoraTheme.colors.Gold,
                        trackColor = AynvoraTheme.colors.CosmicNavy,
                    )
                }

                is AiModelLifecycleState.Installing -> {
                    Text(
                        text = "Finalizing atomic installation...",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                }

                is AiModelLifecycleState.Ready -> {
                    Text(
                        text = "AYNVORA AI is installed and fully operational offline. Contemplation for Tarot, Astrology, and Scriptures runs locally without any network requests.",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.TextLight,
                    )
                    Spacer(modifier = Modifier.height(12.sdp))
                    AynvoraButton(
                        text = "Remove AI Package",
                        variant = AynvoraButtonVariant.Outlined,
                        onClick = onDeleteClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is AiModelLifecycleState.Error -> {
                    Text(
                        text = lifecycleState.message,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = Color(0xFFEF5350),
                    )
                    Spacer(modifier = Modifier.height(12.sdp))
                    AynvoraButton(
                        text = "Retry Download",
                        variant = AynvoraButtonVariant.Primary,
                        onClick = onDownloadClicked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
