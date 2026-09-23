package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Text Input Field component.
 *
 * Contract:
 * - Minimum 48dp interactive touch target.
 * - Outlined surface styling with cosmic palette tokens.
 * - Supports label, placeholder, leading/trailing icons, error state, and accessibility semantics.
 */
@Composable
fun AynvoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val errorActive = isError || !errorMessage.isNullOrBlank()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
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
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = AynvoraShapes.shape8,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AynvoraColors.TextLight,
            unfocusedTextColor = AynvoraColors.TextLight,
            disabledTextColor = AynvoraColors.TextLightMuted,
            errorTextColor = AynvoraColors.TextLight,
            focusedContainerColor = AynvoraColors.CosmicNavy,
            unfocusedContainerColor = AynvoraColors.CosmicNavy,
            disabledContainerColor = AynvoraColors.CosmicBlack,
            errorContainerColor = AynvoraColors.CosmicNavy,
            cursorColor = AynvoraColors.Gold,
            errorCursorColor = AynvoraColors.Error,
            focusedBorderColor = AynvoraColors.Gold,
            unfocusedBorderColor = AynvoraColors.CosmicIndigo,
            disabledBorderColor = AynvoraColors.CosmicIndigo.copy(alpha = 0.5f),
            errorBorderColor = AynvoraColors.Error,
            focusedLabelColor = AynvoraColors.Gold,
            unfocusedLabelColor = AynvoraColors.TextLightSecondary,
            errorLabelColor = AynvoraColors.Error,
        ),
    )
}
