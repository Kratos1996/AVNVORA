package com.aynvora.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp

/**
 * Semantic severity/status variants for [AynvoraStatusChip].
 */
enum class AynvoraStatusChipVariant {
    /** Feature is installed, available, or verified operational. */
    Available,

    /** Fully ready and operating 100% offline. */
    Offline,

    /** Feature is in roadmap or development phase. */
    InDevelopment,

    /** Feature requires setup, hardware configuration, or user permission. */
    Warning,

    /** Feature is disabled, unsupported on this platform, or errored. */
    Error,

    /** Neutral informational chip. */
    Neutral,
}

/**
 * Standard AYNVORA Status Chip Component.
 *
 * Contract:
 * - Purpose: Compact status pill indicating operational readiness, availability, or phase.
 * - Anatomy: Rounded pill container, subtle semantic border, optional status dot indicator, localized label.
 * - Tokens: AynvoraColors, AynvoraShapes, AynvoraSpacing, AynvoraTypography.
 * - Visual Quality: Subdued background alpha (0.12f-0.15f), crisp contrast text, compact height.
 */
@Composable
fun AynvoraStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: AynvoraStatusChipVariant = AynvoraStatusChipVariant.Available,
    showDot: Boolean = true,
) {
    val isDark = AynvoraTheme.isDark

    val statusColor: Color = when (variant) {
        AynvoraStatusChipVariant.Available -> AynvoraColors.CelestialBlue
        AynvoraStatusChipVariant.Offline -> AynvoraColors.Success
        AynvoraStatusChipVariant.InDevelopment -> if (isDark) AynvoraColors.GoldLight else AynvoraColors.GoldDeep
        AynvoraStatusChipVariant.Warning -> AynvoraColors.Warning
        AynvoraStatusChipVariant.Error -> AynvoraColors.Error
        AynvoraStatusChipVariant.Neutral -> if (isDark) AynvoraColors.TextLightSecondary else AynvoraColors.TextSecondary
    }

    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(statusColor.copy(alpha = if (isDark) 0.14f else 0.10f))
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = if (isDark) 0.35f else 0.28f),
                shape = shape,
            )
            .padding(horizontal = AynvoraSpacing.space8, vertical = AynvoraSpacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Spacer(modifier = Modifier.width(AynvoraSpacing.space4))
            }
            Text(
                text = text,
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = statusColor,
                maxLines = 1,
            )
        }
    }
}
