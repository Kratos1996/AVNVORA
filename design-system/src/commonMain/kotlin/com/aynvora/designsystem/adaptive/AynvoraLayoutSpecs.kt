package com.aynvora.designsystem.adaptive

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mobile-specific layout specification.
 * Touch-first, compact, single-column vertical layout with bottom bar navigation.
 */
object AynvoraMobileLayoutSpec {
    val contentPadding: Dp = 16.dp
    val sectionSpacing: Dp = 12.dp
    val cardSpacing: Dp = 10.dp
    val minTouchTarget: Dp = 48.dp
    val topBarHeight: Dp = 56.dp
    val bottomBarHeight: Dp = 64.dp
    val dialogWidth: Dp = 320.dp
}

/**
 * Desktop-specific layout specification.
 * Mouse/keyboard-friendly, information-dense multi-column layout with persistent sidebar navigation.
 */
object AynvoraDesktopLayoutSpec {
    val contentPadding: Dp = 28.dp
    val sectionSpacing: Dp = 20.dp
    val cardSpacing: Dp = 16.dp
    val sidebarWidth: Dp = 240.dp
    val contextPanelWidth: Dp = 320.dp
    val maxContentWidth: Dp = 1440.dp
    val topBarHeight: Dp = 64.dp
    val denseTablePadding: Dp = 8.dp
}
