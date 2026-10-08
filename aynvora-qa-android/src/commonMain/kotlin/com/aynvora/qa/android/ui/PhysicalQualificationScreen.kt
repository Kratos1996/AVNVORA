package com.aynvora.qa.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.intelligence.PhysicalQualificationStatus
import com.aynvora.core.intelligence.ReleaseGateManager
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant

data class QualificationItemState(
    val title: String,
    val capabilityId: String?,
    val status: String,
    val statusColor: Color,
    val detail: String,
)

/**
 * Developer & QA Physical Device Qualification Status Screen.
 * Reflects real hardware attachment state and capability verification status
 * derived directly from ReleaseGateManager.
 */
@Composable
fun PhysicalQualificationScreen(
    isAndroidDeviceConnected: Boolean,
    isAdbConnected: Boolean,
    deviceModel: String,
    androidVersion: String,
    onRunHarness: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraStatus = ReleaseGateManager.getPhysicalQualificationStatus("palm_camera", isAndroidDeviceConnected)
    val handStatus = ReleaseGateManager.getPhysicalQualificationStatus("palm_hand_detection", isAndroidDeviceConnected)
    val lineStatus = ReleaseGateManager.getPhysicalQualificationStatus("palm_line_detection", isAndroidDeviceConnected)
    val pdfStatus = ReleaseGateManager.getPhysicalQualificationStatus("pdf_android_export", isAndroidDeviceConnected)

    val items = listOf(
        QualificationItemState(
            title = "ANDROID DEVICE",
            capabilityId = null,
            status = if (isAndroidDeviceConnected) "CONNECTED" else "NOT CONNECTED",
            statusColor = if (isAndroidDeviceConnected) Color(0xFF4ADE80) else Color(0xFFF87171),
            detail = if (isAndroidDeviceConnected) "$deviceModel (Android $androidVersion)" else "No physical device attached",
        ),
        QualificationItemState(
            title = "ADB TRANSPORT",
            capabilityId = null,
            status = if (isAdbConnected) "CONNECTED" else "NOT CONNECTED",
            statusColor = if (isAdbConnected) Color(0xFF4ADE80) else Color(0xFFF87171),
            detail = if (isAdbConnected) "TLS / USB link active" else "Daemon waiting for client connection",
        ),
        QualificationItemState(
            title = "CAMERA QUALIFICATION",
            capabilityId = "palm_camera",
            status = cameraStatus.name,
            statusColor = getStatusColor(cameraStatus),
            detail = if (cameraStatus == PhysicalQualificationStatus.VERIFIED) "Physical camera capture qualified" else "Requires physical CameraX live capture",
        ),
        QualificationItemState(
            title = "HAND DETECTION",
            capabilityId = "palm_hand_detection",
            status = handStatus.name,
            statusColor = getStatusColor(handStatus),
            detail = if (handStatus == PhysicalQualificationStatus.VERIFIED) "MediaPipe physical hand inference qualified" else "Requires live hand 21-landmark test",
        ),
        QualificationItemState(
            title = "PALM LINE DETECTION",
            capabilityId = "palm_line_detection",
            status = lineStatus.name,
            statusColor = getStatusColor(lineStatus),
            detail = if (lineStatus == PhysicalQualificationStatus.VERIFIED) "Physical palm creases visually reviewed" else "Requires real palm image crease alignment",
        ),
        QualificationItemState(
            title = "ANDROID PDF EXPORT",
            capabilityId = "pdf_android_export",
            status = pdfStatus.name,
            statusColor = getStatusColor(pdfStatus),
            detail = if (pdfStatus == PhysicalQualificationStatus.VERIFIED) "PdfDocument save & reopen verified" else "Requires physical Android storage & PDF reopen",
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Release Qualification Matrix",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Source of Truth: ReleaseGateManager",
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.detail,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(item.statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, item.statusColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = item.status,
                                color = item.statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AynvoraButton(
            text = if (isAndroidDeviceConnected) "Execute Qualification Harness" else "Awaiting Physical Android Hardware",
            variant = if (isAndroidDeviceConnected) AynvoraButtonVariant.Primary else AynvoraButtonVariant.Ghost,
            onClick = { if (isAndroidDeviceConnected) onRunHarness() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun getStatusColor(status: PhysicalQualificationStatus): Color {
    return when (status) {
        PhysicalQualificationStatus.VERIFIED -> Color(0xFF4ADE80)
        PhysicalQualificationStatus.PENDING -> Color(0xFFFBBF24)
        PhysicalQualificationStatus.BLOCKED -> Color(0xFFF87171)
    }
}
