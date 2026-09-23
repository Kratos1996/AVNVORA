package com.aynvora.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.aynvora.designsystem.AynvoraBorders
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraElevation
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing

/**
 * Visual variant options for [AynvoraCard].
 */
enum class AynvoraCardVariant {
    Elevated,
    Outlined,
    Filled,
}

/**
 * Standard AYNVORA Card Component.
 *
 * Contract:
 * - Purpose: Surface container for grouping content, data points, or interactive sections.
 * - Anatomy: Shaped surface container, optional border, content padding, optional click handler.
 * - Tokens: AynvoraColors, AynvoraShapes, AynvoraElevation, AynvoraBorders, AynvoraSpacing.
 * - States: Default, interactive (clickable).
 */
@Composable
fun AynvoraCard(
    modifier: Modifier = Modifier,
    variant: AynvoraCardVariant = AynvoraCardVariant.Filled,
    shape: Shape = AynvoraShapes.shape12,
    onClick: (() -> Unit)? = null,
    containerColor: Color = AynvoraColors.CosmicNavy,
    contentColor: Color = AynvoraColors.TextLight,
    contentPadding: PaddingValues = PaddingValues(AynvoraSpacing.space16),
    content: @Composable () -> Unit,
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    when (variant) {
        AynvoraCardVariant.Elevated -> {
            Card(
                modifier = clickableModifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = AynvoraElevation.level2,
                ),
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
        AynvoraCardVariant.Outlined -> {
            OutlinedCard(
                modifier = clickableModifier,
                shape = shape,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
                border = BorderStroke(
                    width = AynvoraBorders.thin,
                    color = AynvoraColors.CosmicIndigo,
                ),
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
        AynvoraCardVariant.Filled -> {
            Card(
                modifier = clickableModifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = AynvoraElevation.level0,
                ),
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
    }
}
