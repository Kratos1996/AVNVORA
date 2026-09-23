package com.aynvora.designsystem.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme

/**
 * Standard form field wrapper providing label, required asterisk, supporting text,
 * and accessible error message display adhering to Master UI Style Guide.
 */
@Composable
fun AynvoraFormField(
    modifier: Modifier = Modifier,
    label: String? = null,
    isRequired: Boolean = false,
    supportingText: String? = null,
    errorMessage: String? = null,
    content: @Composable () -> Unit,
) {
    val isError = !errorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
    ) {
        if (!label.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = AynvoraTheme.typography.body14,
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                if (isRequired) {
                    Spacer(modifier = Modifier.width(AynvoraSpacing.space4))
                    Text(
                        text = "*",
                        style = AynvoraTheme.typography.body14,
                        color = AynvoraTheme.colors.Gold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(AynvoraSpacing.space4))
        }

        content()

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AynvoraSpacing.space4))
            Text(
                text = errorMessage,
                style = AynvoraTheme.typography.caption12,
                color = AynvoraTheme.colors.Error,
                modifier = Modifier.semantics { },
            )
        } else if (!supportingText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(AynvoraSpacing.space4))
            Text(
                text = supportingText,
                style = AynvoraTheme.typography.caption12,
                color = AynvoraTheme.colors.TextMuted,
            )
        }
    }
}
