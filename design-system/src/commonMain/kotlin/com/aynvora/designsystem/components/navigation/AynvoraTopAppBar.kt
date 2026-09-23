package com.aynvora.designsystem.components.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aynvora.designsystem.AynvoraBorders
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Top App Bar Component.
 *
 * Contract:
 * - Brand-aligned header with Cormorant Garamond display styling.
 * - Cosmic background with subtle bottom border token.
 * - Supports navigation icon and trailing action slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AynvoraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = AynvoraTheme.typography.title20,
                color = AynvoraColors.Gold,
            )
        },
        modifier = modifier.border(
            BorderStroke(AynvoraBorders.thin, AynvoraColors.CosmicIndigo)
        ),
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AynvoraColors.CosmicNavy,
            navigationIconContentColor = AynvoraColors.TextLight,
            titleContentColor = AynvoraColors.Gold,
            actionIconContentColor = AynvoraColors.TextLight,
        ),
    )
}
