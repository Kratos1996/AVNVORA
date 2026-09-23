package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
 * Standard AYNVORA Checkbox Component.
 *
 * Contract:
 * - Minimum 48dp interactive touch target height.
 * - Supports label and supporting caption text.
 * - Uses Gold accent for checked state and Cosmic Indigo border.
 */
@Composable
fun AynvoraCheckbox(
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
            .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
    } else {
        modifier.defaultMinSize(minHeight = 48.dp)
    }

    Row(
        modifier = clickableModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = AynvoraColors.Gold,
                uncheckedColor = AynvoraColors.CosmicIndigo,
                checkmarkColor = AynvoraColors.CosmicBlack,
                disabledCheckedColor = AynvoraColors.TextMuted,
                disabledUncheckedColor = AynvoraColors.CosmicIndigo.copy(alpha = 0.5f),
            ),
        )

        if (!label.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(AynvoraSpacing.space8))
            Column {
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
        }
    }
}
