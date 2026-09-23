package com.aynvora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.AynvoraAdaptiveLayout
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant

/**
 * Root Compose Multiplatform entry application for AYNVORA with adaptive multi-device support.
 */
@Composable
fun AynvoraApp(darkTheme: Boolean = true) {
    var isDark by remember { mutableStateOf(darkTheme) }

    AynvoraTheme(darkTheme = isDark) {
        val backgroundColor = if (isDark) {
            AynvoraTheme.colors.CosmicBlack
        } else {
            AynvoraTheme.colors.Ivory
        }

        val primaryTextColor = if (isDark) {
            AynvoraTheme.colors.TextLight
        } else {
            AynvoraTheme.colors.TextDark
        }

        val secondaryTextColor = if (isDark) {
            AynvoraTheme.colors.TextLightSecondary
        } else {
            AynvoraTheme.colors.TextSecondary
        }

        val windowInfo = AynvoraTheme.window

        com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHost {
            com.aynvora.designsystem.components.dialogs.AynvoraDialogHost {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(24.sdp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
            Text(
                text = "AYNVORA",
                style = AynvoraTheme.typography.display36.copy(fontSize = 36.ssp),
                color = AynvoraTheme.colors.Gold,
            )

            Spacer(modifier = Modifier.height(8.sdp))

            Text(
                text = "Ancient Wisdom. Clearer Choices.",
                style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                color = secondaryTextColor,
            )

            Spacer(modifier = Modifier.height(24.sdp))

            AynvoraAdaptiveLayout(
                modifier = Modifier.fillMaxWidth(),
                spacing = 16.sdp,
                primaryContent = {
                    AynvoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = AynvoraCardVariant.Outlined,
                        containerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.White,
                        contentColor = primaryTextColor,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Foundation Ready",
                                style = AynvoraTheme.typography.title20.copy(fontSize = 20.ssp),
                                color = AynvoraTheme.colors.GoldLight,
                            )
                            Spacer(modifier = Modifier.height(8.sdp))
                            Text(
                                text = "Kotlin Multiplatform + Compose Multiplatform SDK",
                                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                                color = secondaryTextColor,
                            )
                            Spacer(modifier = Modifier.height(16.sdp))
                            AynvoraButton(
                                text = if (isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                                variant = AynvoraButtonVariant.Primary,
                                onClick = { isDark = !isDark },
                            )
                        }
                    }
                },
                secondaryContent = {
                    val sheetController = dev.ishant.cottonsheet.LocalCottonSheetController.current
                    val sheetParams = com.aynvora.designsystem.components.sheets.AynvoraBottomSheetDefaults.params()

                    AynvoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = AynvoraCardVariant.Elevated,
                        containerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.White,
                        contentColor = primaryTextColor,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Adaptive Device Profile",
                                style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                                color = AynvoraTheme.colors.CelestialBlue,
                            )
                            Spacer(modifier = Modifier.height(8.sdp))
                            Text(
                                text = "Form Factor: ${windowInfo.deviceType.name} (${windowInfo.widthSizeClass.name})",
                                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                                color = primaryTextColor,
                            )
                            Spacer(modifier = Modifier.height(4.sdp))
                            Text(
                                text = "Viewport: ${windowInfo.width.value.toInt()}dp x ${windowInfo.height.value.toInt()}dp",
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                                color = secondaryTextColor,
                            )
                            Spacer(modifier = Modifier.height(16.sdp))
                            AynvoraButton(
                                text = "Open CottonSheet",
                                variant = AynvoraButtonVariant.Secondary,
                                onClick = {
                                    sheetController.show(sheetParams) { dismiss ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 32.sdp),
                                        ) {
                                            com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHeader(
                                                title = "CottonSheet Integration",
                                                subtitle = "Compose Multiplatform Bottom Sheet",
                                                onCloseClick = dismiss,
                                            )
                                            Column(modifier = Modifier.padding(horizontal = 24.sdp)) {
                                                Text(
                                                    text = "Zero-boilerplate, stackable modal bottom sheets styled with AYNVORA Cosmic Navy surface, Gold accents, and adaptive 560dp max-width.",
                                                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                                                    color = primaryTextColor,
                                                )
                                                Spacer(modifier = Modifier.height(16.sdp))
                                                AynvoraButton(
                                                    text = "Close Sheet",
                                                    variant = AynvoraButtonVariant.Primary,
                                                    onClick = dismiss,
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                            Spacer(modifier = Modifier.height(8.sdp))
                            val dialogController = dev.ishant.popbox.LocalPopBoxController.current
                            val dialogParams = com.aynvora.designsystem.components.dialogs.AynvoraDialogDefaults.params()

                            AynvoraButton(
                                text = "Open PopBox Dialog",
                                variant = AynvoraButtonVariant.Outlined,
                                onClick = {
                                    dialogController.show(dialogParams) { dismiss ->
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            com.aynvora.designsystem.components.dialogs.AynvoraDialogHeader(
                                                title = "PopBox Dialog",
                                                subtitle = "Compose Multiplatform Stackable Dialog",
                                                onCloseClick = dismiss,
                                            )
                                            Text(
                                                text = "Global, zero-boilerplate dialog management styled with AYNVORA Cosmic Navy surface, 16dp radius, and Gold typography.",
                                                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                                                color = primaryTextColor,
                                            )
                                            Spacer(modifier = Modifier.height(16.sdp))
                                            AynvoraButton(
                                                text = "Confirm & Close",
                                                variant = AynvoraButtonVariant.Primary,
                                                onClick = dismiss,
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                },
            )
        }
    }
}
}
}


