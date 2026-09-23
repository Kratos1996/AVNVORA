package com.aynvora.designsystem.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraSpacing

/**
 * Maximum content constraint on ultra-wide / desktop viewports to preserve visual harmony.
 */
val MAX_DESKTOP_CONTENT_WIDTH = 1200.dp
val MAX_READABLE_CARD_WIDTH = 640.dp

/**
 * Automatically arranging adaptive layout container for multi-device support.
 *
 * Automatically manages:
 * - Mobile (< 600dp): Single-column vertical arrangement.
 * - Foldable (600dp - 839dp): Balanced side-by-side two-column arrangement for unfolded displays.
 * - Tablet & Desktop (>= 840dp): Multi-pane master-detail arrangement centered within max width bounds.
 *
 * @param modifier Root layout modifier.
 * @param primaryContent Main content block (always rendered).
 * @param secondaryContent Optional secondary block (placed below on mobile, beside on foldable/tablet/desktop).
 * @param spacing Spacing between content blocks.
 * @param verticalScrollable Whether the container should automatically scroll when content exceeds viewport.
 */
@Composable
fun AynvoraAdaptiveLayout(
    modifier: Modifier = Modifier,
    spacing: Dp = AynvoraSpacing.space16,
    verticalScrollable: Boolean = true,
    secondaryContent: (@Composable () -> Unit)? = null,
    primaryContent: @Composable () -> Unit,
) {
    val windowInfo = LocalAynvoraWindowInfo.current

    val scrollModifier = if (verticalScrollable && windowInfo.isCompact) {
        Modifier.verticalScroll(rememberScrollState())
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(scrollModifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            // Mobile (Compact): Single column
            windowInfo.isCompact || secondaryContent == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = if (windowInfo.isExpanded) MAX_READABLE_CARD_WIDTH else Dp.Unspecified),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    primaryContent()
                    if (secondaryContent != null) {
                        Spacer(modifier = Modifier.height(spacing))
                        secondaryContent()
                    }
                }
            }

            // Foldable (Medium, 600dp - 839dp): Balanced 50/50 dual pane
            windowInfo.isMedium -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        primaryContent()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        secondaryContent()
                    }
                }
            }

            // Tablet & Desktop (Expanded, >= 840dp): Centered master-detail arrangement
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = MAX_DESKTOP_CONTENT_WIDTH),
                    horizontalArrangement = Arrangement.spacedBy(spacing * 1.5f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        primaryContent()
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        secondaryContent()
                    }
                }
            }
        }
    }
}
