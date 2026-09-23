package com.aynvora.designsystem.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import dev.ishant.popbox.PopBoxController
import dev.ishant.popbox.PopBoxHost
import dev.ishant.popbox.PopBoxParams

/**
 * Default styling and parameters for AYNVORA dialogs adhering to the Master UI Style Guide.
 */
object AynvoraDialogDefaults {

    val DialogCornerRadius: Dp = 16.dp
    val DialogHorizontalPadding: Dp = 24.dp
    val DialogContentPadding: Dp = 24.dp

    /**
     * Builds [PopBoxParams] pre-configured with AYNVORA design tokens.
     */
    @Composable
    fun params(
        disableOuterTap: Boolean = false,
        horizontalPadding: Dp = DialogHorizontalPadding,
        contentPadding: Dp = DialogContentPadding,
        onDismissRequest: (() -> Unit)? = null,
    ): PopBoxParams {
        val colors = AynvoraTheme.colors
        val isDark = AynvoraTheme.isDark

        return PopBoxParams(
            disableOuterTap = disableOuterTap,
            shape = RoundedCornerShape(DialogCornerRadius),
            containerColor = if (isDark) colors.CosmicNavy else colors.White,
            horizontalPadding = horizontalPadding,
            contentPadding = contentPadding,
            onDismissRequest = onDismissRequest,
        )
    }
}

/**
 * Reusable header for AYNVORA dialogs with title, optional subtitle, and close button.
 */
@Composable
fun AynvoraDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onCloseClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = AynvoraSpacing.space16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AynvoraTheme.typography.title20.copy(fontSize = 20.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AynvoraSpacing.space4))
                Text(
                    text = subtitle,
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                    color = if (AynvoraTheme.isDark) {
                        AynvoraTheme.colors.TextLightSecondary
                    } else {
                        AynvoraTheme.colors.TextSecondary
                    },
                )
            }
        }

        if (onCloseClick != null) {
            AynvoraButton(
                text = "✕",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onCloseClick,
            )
        }
    }
}

/**
 * Root host for AYNVORA dialogs powered by PopBox.
 * Place once at the root of the composition hierarchy inside [AynvoraTheme].
 */
@Composable
fun AynvoraDialogHost(
    content: @Composable () -> Unit,
) {
    PopBoxHost(content = content)
}

/**
 * Convenience extension to launch an AYNVORA-styled dialog via [PopBoxController].
 */
fun PopBoxController.showAynvoraDialog(
    params: PopBoxParams = PopBoxParams(),
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    show(params = params, content = content)
}
