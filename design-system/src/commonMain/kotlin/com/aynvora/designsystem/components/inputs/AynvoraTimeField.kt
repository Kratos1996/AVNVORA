package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard AYNVORA Time Input Field.
 * Generic component for time entry (HH:MM or HH:MM:SS in 24-hour format).
 */
@Composable
fun AynvoraTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Time",
    placeholder: String = "HH:MM",
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    onClockClick: (() -> Unit)? = null,
) {
    val trailingAction: (@Composable () -> Unit)? = if (onClockClick != null) {
        {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onClockClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "⏰",
                    style = AynvoraTheme.typography.body16,
                )
            }
        }
    } else null

    AynvoraTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        trailingIcon = trailingAction,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
