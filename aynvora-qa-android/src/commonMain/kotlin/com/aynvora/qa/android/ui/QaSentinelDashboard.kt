package com.aynvora.qa.android.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.qa.android.AndroidInteractionSentinelRuntime
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaFailureCapsule
import com.aynvora.qa.core.report.QaSessionSummary

@Composable
fun QaSentinelDashboard(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = remember { AndroidInteractionSentinelRuntime.generateSessionSummary() }
    var selectedCapsule by remember { mutableStateOf<QaFailureCapsule?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "AYNVORA Sentinel QA",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${summary.deviceModel} • Android ${summary.androidVersion}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                )
            }
            AynvoraButton(
                text = "Close",
                variant = AynvoraButtonVariant.Secondary,
                onClick = onClose,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("Actions", "${summary.totalActions}", Color(0xFF38BDF8), Modifier.weight(1f))
            MetricCard("Success", "${summary.totalSuccess}", Color(0xFF4ADE80), Modifier.weight(1f))
            MetricCard("No-Op", "${summary.totalNoOp}", Color(0xFFFBBF24), Modifier.weight(1f))
            MetricCard(
                "Rep No-Op",
                "${summary.totalRepeatedNoOp}",
                Color(0xFFF87171),
                Modifier.weight(1f)
            )
            MetricCard("Errors", "${summary.totalError}", Color(0xFFEF4444), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Breakdown & Capsules
        Text(
            text = "Feature Verification",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(summary.featureStats) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stat.feature.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✓ ${stat.success}", color = Color(0xFF4ADE80), fontSize = 12.sp)
                            Text(
                                "⚠ ${stat.noOp + stat.repeatedNoOp}",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp
                            )
                            Text("✗ ${stat.error}", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            }

            if (summary.failureCapsules.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suspicious Controls / Failure Capsules (${summary.failureCapsules.size})",
                        color = Color(0xFFF87171),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                items(summary.failureCapsules) { capsule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCapsule = capsule },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = capsule.actionId.identifier,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                                Text(
                                    text = capsule.classification.name,
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = "Screen: ${capsule.screen} | Repeated: ${capsule.repeatedCount}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                            )
                            Text(
                                text = "Observed: ${capsule.observed}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }

        selectedCapsule?.let { capsule ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Details: ${capsule.failureId}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Expected: ${capsule.expected}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(
                        "Observed: ${capsule.observed}",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp
                    )
                    if (capsule.stackTrace != null) {
                        Text(
                            "Stacktrace: ${capsule.stackTrace}",
                            color = Color(0xFFF87171),
                            fontSize = 10.sp
                        )
                    }
                    AynvoraButton(
                        text = "Dismiss Details",
                        variant = AynvoraButtonVariant.Ghost,
                        onClick = { selectedCapsule = null },
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}
