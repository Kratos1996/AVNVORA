package com.aynvora.designsystem.components.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aynvora.designsystem.AynvoraBorders
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Side Navigation Rail Component for foldable, tablet, and desktop viewports.
 *
 * Contract:
 * - 48dp minimum item touch target.
 * - Cosmic Navy container with border dividing navigation from primary content.
 * - Gold active indicator with readable text labels.
 */
@Composable
fun AynvoraNavigationRail(
    items: List<AynvoraNavigationItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    NavigationRail(
        modifier = modifier.border(
            BorderStroke(AynvoraBorders.thin, AynvoraColors.CosmicIndigo)
        ),
        containerColor = AynvoraColors.CosmicNavy,
        contentColor = AynvoraColors.TextLight,
        header = if (header != null) {
            {
                header()
                Spacer(modifier = Modifier.height(AynvoraSpacing.space16))
            }
        } else null,
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedId
            NavigationRailItem(
                selected = isSelected,
                onClick = { onSelect(item.id) },
                icon = {
                    if (isSelected && item.selectedIcon != null) {
                        item.selectedIcon()
                    } else {
                        item.icon()
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        style = AynvoraTheme.typography.caption12,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = AynvoraColors.CosmicBlack,
                    selectedTextColor = AynvoraColors.Gold,
                    indicatorColor = AynvoraColors.Gold,
                    unselectedIconColor = AynvoraColors.TextLightSecondary,
                    unselectedTextColor = AynvoraColors.TextLightSecondary,
                ),
            )
        }
    }
}
