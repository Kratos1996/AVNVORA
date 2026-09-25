package com.aynvora.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aynvora.core.ai.AiDeviceProfile
import com.aynvora.core.ai.AiInferenceDiagnostics
import com.aynvora.core.ai.AiInferenceStatus
import com.aynvora.core.ai.AiModelLifecycleState
import com.aynvora.core.ai.AiModelSelectionResult
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant

/**
 * Developer and debug-only diagnostics screen.
 *
 * Exposes detailed technical telemetry, memory allocation estimates, candidate evaluations,
 * and engine diagnostics without cluttering or leaking into the consumer user experience.
 */
@Composable
fun AiDiagnosticScreen(
    deviceProfile: AiDeviceProfile?,
    selectionResult: AiModelSelectionResult?,
    lifecycleState: AiModelLifecycleState,
    inferenceStatus: AiInferenceStatus,
    inferenceDiagnostics: AiInferenceDiagnostics,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(16.sdp),
    ) {
        // Navigation / Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "AI Technical Diagnostics",
                style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            AynvoraButton(
                text = "Close",
                variant = AynvoraButtonVariant.Outlined,
                onClick = onBackClicked,
            )
        }

        Spacer(modifier = Modifier.height(12.sdp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.sdp),
        ) {
            // 1. Hardware & Platform Profile
            item {
                DiagnosticCard(title = "Device Hardware Profile") {
                    if (deviceProfile != null) {
                        val totalRamMb = deviceProfile.totalRamBytes / (1024L * 1024L)
                        val availRamMb = deviceProfile.availableRamBytes / (1024L * 1024L)
                        val safeRamMb = deviceProfile.maxSafeRamAllocationBytes / (1024L * 1024L)
                        val storageGb = deviceProfile.freeStorageBytes / (1024L * 1024L * 1024L)

                        DiagnosticRow("CPU Architecture", deviceProfile.cpuArchitecture.name)
                        DiagnosticRow(
                            "OS Platform",
                            "${deviceProfile.osPlatform.name} (${deviceProfile.osVersion})"
                        )
                        DiagnosticRow("Total Physical RAM", "$totalRamMb MB")
                        DiagnosticRow("Available RAM", "$availRamMb MB")
                        DiagnosticRow("Safe RAM Allocation Limit", "$safeRamMb MB")
                        DiagnosticRow("Free Internal Storage", "$storageGb GB")
                        DiagnosticRow(
                            "Supported Runtimes",
                            deviceProfile.supportedRuntimes.joinToString(", ")
                        )
                        DiagnosticRow(
                            "Supported Accelerators",
                            deviceProfile.supportedAccelerators.joinToString(", ")
                        )
                    } else {
                        Text(
                            text = "Device profile not yet detected.",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                }
            }

            // 2. Selection Result & Catalog Evaluation
            item {
                DiagnosticCard(title = "Automatic Model Selection Result") {
                    if (selectionResult != null) {
                        DiagnosticRow("Status", selectionResult.status.name)
                        DiagnosticRow(
                            "Selected Model ID",
                            selectionResult.selectedModel?.modelId ?: "None"
                        )
                        DiagnosticRow(
                            "Quantization Format",
                            selectionResult.selectedModel?.quantization?.name ?: "None"
                        )
                        DiagnosticRow(
                            "Model File Size",
                            "${selectionResult.selectedModel?.displaySizeMb ?: 0} MB"
                        )
                        DiagnosticRow(
                            "Default Context Limit",
                            "${selectionResult.selectedModel?.defaultContextLength ?: 0} tokens"
                        )
                        DiagnosticRow(
                            "SHA-256 Checksum",
                            selectionResult.selectedModel?.sha256Checksum?.take(16)?.plus("...")
                                ?: "None"
                        )

                        Spacer(modifier = Modifier.height(8.sdp))
                        Text(
                            text = "Candidate Evaluations:",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.GoldLight,
                        )
                        selectionResult.diagnostics.candidateEvaluations.forEach { eval ->
                            val statusText = if (eval.isCompatible) "COMPATIBLE" else "REJECTED: ${
                                eval.rejectionReasons.joinToString("; ")
                            }"
                            DiagnosticRow(eval.modelId, statusText)
                        }
                    } else {
                        Text(
                            text = "Model selection evaluation pending.",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                }
            }

            // 3. Runtime Lifecycle & Inference Telemetry
            item {
                DiagnosticCard(title = "Local Runtime Execution Telemetry") {
                    DiagnosticRow("Inference Engine Status", inferenceStatus.name)
                    DiagnosticRow("Execution Mode", inferenceDiagnostics.executionMode.name)
                    DiagnosticRow(
                        "Cold Load Latency",
                        "${inferenceDiagnostics.coldLoadDurationMs} ms"
                    )
                    DiagnosticRow(
                        "Warm Load Latency",
                        "${inferenceDiagnostics.warmLoadDurationMs} ms"
                    )
                    DiagnosticRow(
                        "Last Inference Duration",
                        "${inferenceDiagnostics.lastInferenceDurationMs} ms"
                    )
                    DiagnosticRow(
                        "Generation Speed",
                        "${inferenceDiagnostics.tokensPerSecond} tokens/sec"
                    )
                    DiagnosticRow(
                        "Memory Allocated",
                        "${inferenceDiagnostics.memoryAllocatedBytes / (1024L * 1024L)} MB"
                    )
                    DiagnosticRow(
                        "Context Tokens Allocated",
                        "${inferenceDiagnostics.contextTokensAllocated}"
                    )
                    DiagnosticRow(
                        "Last Error Code",
                        inferenceDiagnostics.lastErrorCode?.name ?: "NONE"
                    )

                    val lifecycleStr = when (lifecycleState) {
                        is AiModelLifecycleState.Ready -> "READY (${lifecycleState.localFilePath})"
                        is AiModelLifecycleState.Downloading -> "DOWNLOADING (${lifecycleState.percentage}%)"
                        is AiModelLifecycleState.VerifyingChecksum -> "VERIFYING_CHECKSUM"
                        is AiModelLifecycleState.Installing -> "INSTALLING"
                        is AiModelLifecycleState.NotInstalled -> "NOT_INSTALLED"
                        is AiModelLifecycleState.Error -> "ERROR: ${lifecycleState.message} (Reason: ${lifecycleState.failureReason})"
                    }
                    DiagnosticRow("Lifecycle State", lifecycleStr)
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCard(
    title: String,
    content: @Composable () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.sdp)) {
            Text(
                text = title,
                style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(8.sdp))
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.sdp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
            color = AynvoraTheme.colors.TextLight,
        )
    }
}
