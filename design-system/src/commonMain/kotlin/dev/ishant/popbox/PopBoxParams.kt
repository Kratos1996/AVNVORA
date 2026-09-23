package dev.ishant.popbox

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual / behavioural configuration for a single PopBox layer.
 *
 * @param disableOuterTap  If true, clicking outside or back press won't dismiss.
 * @param shape            Corner shape.
 * @param containerColor   PopBox background color.
 * @param horizontalPadding Padding from screen edges.
 * @param contentPadding   Internal padding for content.
 */
data class PopBoxParams(
    val disableOuterTap: Boolean = false,
    val shape: Shape = RoundedCornerShape(16.dp),
    val containerColor: Color? = null,
    val horizontalPadding: Dp = 24.dp,
    val contentPadding: Dp = 20.dp,
    val onDismissRequest: (() -> Unit)? = null,
)
