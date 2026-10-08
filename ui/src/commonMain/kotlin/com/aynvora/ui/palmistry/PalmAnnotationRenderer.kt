package com.aynvora.ui.palmistry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.palmistry.HandLandmark
import com.aynvora.core.palmistry.ImageSpaceTransform
import com.aynvora.core.palmistry.PalmEvidence
import com.aynvora.core.palmistry.PalmLineGeometry
import com.aynvora.core.palmistry.PalmLineType
import com.aynvora.core.palmistry.Point2D
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.ui.tarot.toImageBitmap

enum class PalmImageViewMode {
    ORIGINAL,
    NORMALIZED,
    ANNOTATED,
}

data class PalmAnnotationLayers(
    val showHeartLine: Boolean = true,
    val showHeadLine: Boolean = true,
    val showLifeLine: Boolean = true,
    val showFateLine: Boolean = true,
    val showLandmarks: Boolean = true,
    val showPalmBounds: Boolean = false,
    val showWatermark: Boolean = true,
)

/**
 * High-fidelity Palm Line & Landmark overlay canvas for both Mobile and Desktop workspaces.
 */
@Composable
fun PalmAnnotationCanvas(
    imageBytes: ByteArray?,
    evidence: PalmEvidence?,
    layers: PalmAnnotationLayers,
    viewMode: PalmImageViewMode,
    watermarkText: String,
    modifier: Modifier = Modifier,
) {
    val bitmap: ImageBitmap? = remember(imageBytes) {
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            runCatching { imageBytes.toImageBitmap() }.getOrNull()
        } else null
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1117))
            .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val transform = if (bitmap != null) {
                ImageSpaceTransform.fit(bitmap.width, bitmap.height, canvasWidth, canvasHeight)
            } else {
                ImageSpaceTransform.fit(canvasWidth.toInt(), canvasHeight.toInt(), canvasWidth, canvasHeight)
            }

            // 1. Draw image bitmap if available (preserving aspect ratio via transform)
            if (bitmap != null) {
                drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(transform.offsetX.toInt(), transform.offsetY.toInt()),
                    dstSize = IntSize(transform.displayWidth.toInt(), transform.displayHeight.toInt()),
                )
            } else {
                // Geometric palm outline guide fallback
                drawRect(
                    color = Color(0xFF161B22),
                    size = size,
                )
                // Grid guide lines
                val stepX = canvasWidth / 10f
                val stepY = canvasHeight / 10f
                for (i in 1..9) {
                    drawLine(
                        color = Color(0x1A855E1C),
                        start = Offset(stepX * i, 0f),
                        end = Offset(stepX * i, canvasHeight),
                        strokeWidth = 1f,
                    )
                    drawLine(
                        color = Color(0x1A855E1C),
                        start = Offset(0f, stepY * i),
                        end = Offset(canvasWidth, stepY * i),
                        strokeWidth = 1f,
                    )
                }
            }

            // Only draw overlays in ANNOTATED or NORMALIZED mode
            if (viewMode == PalmImageViewMode.ANNOTATED && evidence != null) {
                // 2. Palm Bounds
                if (layers.showPalmBounds) {
                    val b = evidence.palmBounds
                    if (!b.left.isNaN() && !b.top.isNaN() && !b.right.isNaN() && !b.bottom.isNaN() &&
                        !b.left.isInfinite() && !b.top.isInfinite() && !b.right.isInfinite() && !b.bottom.isInfinite()
                    ) {
                        val topLeft = transform.toCanvasPoint(Point2D(b.left.coerceIn(0f, 1f), b.top.coerceIn(0f, 1f)))
                        val bottomRight = transform.toCanvasPoint(Point2D(b.right.coerceIn(0f, 1f), b.bottom.coerceIn(0f, 1f)))
                        val rw = bottomRight.x - topLeft.x
                        val rh = bottomRight.y - topLeft.y
                        if (rw > 0f && rh > 0f) {
                            drawRect(
                                color = Color(0xFFFFD54F).copy(alpha = 0.7f),
                                topLeft = Offset(topLeft.x, topLeft.y),
                                size = Size(rw, rh),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }
                }

                // 3. Hand Landmarks (MediaPipe 21 points)
                if (layers.showLandmarks && evidence.landmarks.isNotEmpty()) {
                    evidence.landmarks.forEach { lm ->
                        if (!lm.x.isNaN() && !lm.y.isNaN() && !lm.x.isInfinite() && !lm.y.isInfinite() &&
                            lm.x in 0.0f..1.0f && lm.y in 0.0f..1.0f
                        ) {
                            val cp = transform.toCanvasPoint(Point2D(lm.x, lm.y))
                            drawCircle(
                                color = Color(0xFF4FC3F7),
                                radius = 3.5.dp.toPx(),
                                center = Offset(cp.x, cp.y),
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = Offset(cp.x, cp.y),
                            )
                        }
                    }
                }

                // 4. Detected Palm Lines
                fun drawLineGeometry(line: PalmLineGeometry?, color: Color, strokeWidth: Float) {
                    if (line == null || !line.detected) return
                    val rawPoints = if (line.geometry.isNotEmpty()) line.geometry else listOf(line.originPoint, line.terminationPoint)
                    // Reject NaN, Infinity, and sanitize coordinates inside normalized [0.0, 1.0] bounds
                    val validPoints = rawPoints.filter { p ->
                        !p.x.isNaN() && !p.y.isNaN() && !p.x.isInfinite() && !p.y.isInfinite() &&
                            p.x in 0.0f..1.0f && p.y in 0.0f..1.0f
                    }
                    if (validPoints.size >= 2) {
                        val firstCp = transform.toCanvasPoint(validPoints.first())
                        val path = Path().apply {
                            moveTo(firstCp.x, firstCp.y)
                            for (i in 1 until validPoints.size) {
                                val nextCp = transform.toCanvasPoint(validPoints[i])
                                lineTo(nextCp.x, nextCp.y)
                            }
                        }
                        // Outer subtle glow
                        drawPath(
                            path = path,
                            color = color.copy(alpha = 0.35f),
                            style = Stroke(
                                width = strokeWidth * 2.2f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                        // Core crisp line
                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }

                if (layers.showHeartLine) {
                    drawLineGeometry(evidence.heartLine, Color(0xFFE57373), 3.5.dp.toPx())
                }
                if (layers.showHeadLine) {
                    drawLineGeometry(evidence.headLine, Color(0xFFFFB74D), 3.5.dp.toPx())
                }
                if (layers.showLifeLine) {
                    drawLineGeometry(evidence.lifeLine, Color(0xFF81C784), 3.5.dp.toPx())
                }
                if (layers.showFateLine) {
                    drawLineGeometry(evidence.fateLine, Color(0xFF9575CD), 3.5.dp.toPx())
                }
                evidence.additionalDetectedLines.forEach { addLine ->
                    drawLineGeometry(addLine, Color(0xFF4DD0E1), 2.5.dp.toPx())
                }
            }
        }

        // 5. Small Image Metadata Watermark Overlay
        if (layers.showWatermark && watermarkText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.sdp)
                    .background(Color(0xB30B0F19), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color(0x40E0E0E0), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.sdp, vertical = 4.sdp),
            ) {
                Text(
                    text = watermarkText,
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                )
            }
        }

        // View Mode Badge in Top-Left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.sdp)
                .background(Color(0xCC111827), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.sdp, vertical = 3.sdp),
        ) {
            Text(
                text = when (viewMode) {
                    PalmImageViewMode.ORIGINAL -> "ORIGINAL"
                    PalmImageViewMode.NORMALIZED -> "NORMALIZED"
                    PalmImageViewMode.ANNOTATED -> "ANNOTATED"
                },
                color = AynvoraTheme.colors.Gold,
                fontSize = 9.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Interactive layer toggle controls for the palm workspace.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PalmLayerControls(
    layers: PalmAnnotationLayers,
    viewMode: PalmImageViewMode,
    onLayersChanged: (PalmAnnotationLayers) -> Unit,
    onViewModeChanged: (PalmImageViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // View Mode Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            PalmImageViewMode.entries.forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AynvoraTheme.colors.Gold else Color(0xFF1E2430))
                        .clickable { onViewModeChanged(mode) }
                        .padding(vertical = 8.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mode.name,
                        color = if (isSelected) Color(0xFF10131A) else Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.sdp))

        // Annotation Layer Chips (active when viewMode == ANNOTATED)
        if (viewMode == PalmImageViewMode.ANNOTATED) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.sdp),
                verticalArrangement = Arrangement.spacedBy(6.sdp),
            ) {
                LayerChip(
                    label = "Heart Line",
                    color = Color(0xFFE57373),
                    selected = layers.showHeartLine,
                    onToggle = { onLayersChanged(layers.copy(showHeartLine = !layers.showHeartLine)) },
                )
                LayerChip(
                    label = "Head Line",
                    color = Color(0xFFFFB74D),
                    selected = layers.showHeadLine,
                    onToggle = { onLayersChanged(layers.copy(showHeadLine = !layers.showHeadLine)) },
                )
                LayerChip(
                    label = "Life Line",
                    color = Color(0xFF81C784),
                    selected = layers.showLifeLine,
                    onToggle = { onLayersChanged(layers.copy(showLifeLine = !layers.showLifeLine)) },
                )
                LayerChip(
                    label = "Fate Line",
                    color = Color(0xFF9575CD),
                    selected = layers.showFateLine,
                    onToggle = { onLayersChanged(layers.copy(showFateLine = !layers.showFateLine)) },
                )
                LayerChip(
                    label = "Landmarks (21)",
                    color = Color(0xFF4FC3F7),
                    selected = layers.showLandmarks,
                    onToggle = { onLayersChanged(layers.copy(showLandmarks = !layers.showLandmarks)) },
                )
                LayerChip(
                    label = "Watermark",
                    color = Color(0xFFE2E8F0),
                    selected = layers.showWatermark,
                    onToggle = { onLayersChanged(layers.copy(showWatermark = !layers.showWatermark)) },
                )
            }
        }
    }
}

@Composable
private fun LayerChip(
    label: String,
    color: Color,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) color.copy(alpha = 0.25f) else Color(0xFF1E2430),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) color else Color(0x33A0AEC0),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.sdp, vertical = 4.sdp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.sdp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.sdp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                color = if (selected) Color.White else Color(0xFFA0AEC0),
                fontSize = 11.sp,
            )
        }
    }
}
