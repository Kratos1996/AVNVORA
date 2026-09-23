package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Switch Toggle Component.
 *
 * Contract:
 * - Minimum 48dp interactive touch target height.
 * - Supports label and supporting text caption.
 * - Uses Gold accent for active state and Cosmic Indigo for track.
 */
@Composable
fun AynvoraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    val clickableModifier = if (onCheckedChange != null && enabled) {
        modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
    } else {
        modifier.defaultMinSize(minHeight = 48.dp)
    }

    Row(
        modifier = clickableModifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!label.isNullOrBlank()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = AynvoraTheme.typography.body14,
                    color = if (enabled) AynvoraColors.TextLight else AynvoraColors.TextLightMuted,
                )
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraColors.TextMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.width(AynvoraSpacing.space16))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AynvoraColors.Gold,
                checkedTrackColor = AynvoraColors.CosmicNavy,
                checkedBorderColor = AynvoraColors.Gold,
                uncheckedThumbColor = AynvoraColors.TextMuted,
                uncheckedTrackColor = AynvoraColors.CosmicBlack,
                uncheckedBorderColor = AynvoraColors.CosmicIndigo,
            ),
        )
    }
}
