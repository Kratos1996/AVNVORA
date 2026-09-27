package com.aynvora.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp

/**
 * Standard AYNVORA Feature Card Component (SDK UI Master Design System).
 *
 * Unified structure applied across all capabilities (Astrology, Tarot, Numerology, Palmistry, etc.):
 * 1. ICON container + FEATURE TITLE + STATUS CHIP
 * 2. SHORT DESCRIPTION
 * 3. PRIMARY CTA BUTTON
 *
 * Contract:
 * - Visually compact, premium SDK card presentation.
 * - Consistent icon container (38dp rounded box with subtle brand backing).
 * - Standardized hierarchy: Gold title (title18), body description (body14), standardized button.
 * - Event-wired interactive CTA supporting analytical tracking.
 */
@Composable
fun AynvoraFeatureCard(
    title: String,
    description: String,
    iconGlyph: String,
    statusLabel: String,
    statusVariant: AynvoraStatusChipVariant,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonVariant: AynvoraButtonVariant = AynvoraButtonVariant.Primary,
    buttonEvent: AynvoraClickEvent? = null,
    onCardClick: (() -> Unit)? = null,
    cardEvent: AynvoraClickEvent? = null,
) {
    val isDark = AynvoraTheme.isDark

    val containerColor = if (isDark) {
        AynvoraTheme.colors.CosmicNavy
    } else {
        AynvoraTheme.colors.White
    }

    val iconBoxBg = if (isDark) {
        AynvoraTheme.colors.CosmicIndigo
    } else {
        AynvoraTheme.colors.SoftGold
    }

    val secondaryTextColor = if (isDark) {
        AynvoraTheme.colors.TextLightSecondary
    } else {
        AynvoraTheme.colors.TextSecondary
    }

    val borderColor = if (isDark) {
        AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.8f)
    } else {
        AynvoraTheme.colors.SoftGold
    }

    AynvoraCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, AynvoraShapes.shape12),
        variant = AynvoraCardVariant.Filled,
        shape = AynvoraShapes.shape12,
        containerColor = containerColor,
        contentColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(AynvoraSpacing.space16),
        event = cardEvent,
        onClick = onCardClick,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Icon + Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(AynvoraShapes.shape8)
                            .background(iconBoxBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = iconGlyph,
                            style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                        )
                    }

                    Spacer(modifier = Modifier.width(AynvoraSpacing.space12))

                    Text(
                        text = title,
                        style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                        color = AynvoraColors.Gold,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.width(AynvoraSpacing.space8))

                AynvoraStatusChip(
                    text = statusLabel,
                    variant = statusVariant,
                )
            }

            Spacer(modifier = Modifier.height(AynvoraSpacing.space10))

            // Short Description
            Text(
                text = description,
                style = AynvoraTheme.typography.body14.copy(
                    fontSize = 13.ssp,
                    lineHeight = 18.ssp,
                ),
                color = secondaryTextColor,
            )

            Spacer(modifier = Modifier.height(AynvoraSpacing.space12))

            // Standardized Primary CTA Button
            AynvoraButton(
                text = buttonLabel,
                variant = buttonVariant,
                event = buttonEvent,
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
