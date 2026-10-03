package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Read-Only Input Field component.
 *
 * Designed for Date, Time, City, Country, and State picker fields.
 * Looks visually like a standard [AynvoraTextField] with identical shape, colors, and typography,
 * but disables free text editing and soft keyboard popups.
 * Tapping anywhere on the field invokes [onClick].
 */
@Composable
fun AynvoraReadOnlyField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val errorActive = isError || !errorMessage.isNullOrBlank()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp),
            enabled = enabled,
            readOnly = true,
            singleLine = true,
            textStyle = AynvoraTheme.typography.body16,
            label = if (label != null) {
                { Text(text = label, style = AynvoraTheme.typography.body14) }
            } else null,
            placeholder = if (placeholder != null) {
                { Text(text = placeholder, style = AynvoraTheme.typography.body16, color = AynvoraColors.TextMuted) }
            } else null,
            supportingText = if (!errorMessage.isNullOrBlank()) {
                { Text(text = errorMessage, style = AynvoraTheme.typography.caption12, color = AynvoraColors.Error) }
            } else if (!supportingText.isNullOrBlank()) {
                { Text(text = supportingText, style = AynvoraTheme.typography.caption12, color = AynvoraColors.TextMuted) }
            } else null,
            isError = errorActive,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            shape = AynvoraShapes.shape8,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AynvoraColors.TextLight,
                unfocusedTextColor = AynvoraColors.TextLight,
                disabledTextColor = AynvoraColors.TextLight,
                errorTextColor = AynvoraColors.TextLight,
                focusedContainerColor = AynvoraColors.CosmicNavy,
                unfocusedContainerColor = AynvoraColors.CosmicNavy,
                disabledContainerColor = AynvoraColors.CosmicNavy,
                errorContainerColor = AynvoraColors.CosmicNavy,
                cursorColor = Color.Transparent,
                errorCursorColor = Color.Transparent,
                focusedBorderColor = AynvoraColors.Gold,
                unfocusedBorderColor = AynvoraColors.CosmicIndigo,
                disabledBorderColor = AynvoraColors.CosmicIndigo,
                errorBorderColor = AynvoraColors.Error,
                focusedLabelColor = AynvoraColors.Gold,
                unfocusedLabelColor = AynvoraColors.TextLightSecondary,
                disabledLabelColor = AynvoraColors.TextLightSecondary,
                errorLabelColor = AynvoraColors.Error,
            ),
        )

        // Transparent clickable overlay covering the field completely.
        // Prevents OutlinedTextField from receiving focus or opening virtual keyboard.
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    ),
            )
        }
    }
}
