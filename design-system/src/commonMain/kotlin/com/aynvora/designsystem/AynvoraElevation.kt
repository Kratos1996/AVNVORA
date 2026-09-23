package com.aynvora.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AYNVORA Elevation & Border Specification (Master UI Style Guide v1.0).
 */
object AynvoraElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 2.dp
    val level4: Dp = 4.dp
    val level8: Dp = 8.dp
}

object AynvoraBorders {
    val thin: Dp = 1.dp
    val medium: Dp = 2.dp

    fun standard(color: Color): BorderStroke = BorderStroke(width = thin, color = color)
}
