package com.aynvora.designsystem.components.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aynvora.designsystem.AynvoraBorders
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraTheme

/**
 * Navigation item specification for AYNVORA navigation components.
 */
data class AynvoraNavigationItem(
    val id: String,
    val title: String,
    val icon: @Composable () -> Unit,
    val selectedIcon: (@Composable () -> Unit)? = null,
)

/**
 * Standard AYNVORA Bottom Navigation Bar Component for mobile viewports.
 *
 * Contract:
 * - 48dp minimum item touch target height.
 * - Cosmic Navy surface with Cosmic Indigo border.
 * - Gold active indicator with readable text labels.
 */
@Composable
fun AynvoraNavigationBar(
    items: List<AynvoraNavigationItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.border(
            BorderStroke(AynvoraBorders.thin, AynvoraColors.CosmicIndigo)
        ),
        containerColor = AynvoraColors.CosmicNavy,
        contentColor = AynvoraColors.TextLight,
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedId
            NavigationBarItem(
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
                colors = NavigationBarItemDefaults.colors(
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
