package com.aynvora.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraBorders
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme

/**
 * Visual variant options for [AynvoraButton].
 */
enum class AynvoraButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Ghost,
}

/**
 * Standard AYNVORA Button Component.
 *
 * Contract:
 * - Purpose: Primary and secondary call-to-action button adhering to Master UI Style Guide.
 * - Anatomy: Container, optional leading icon, text label, optional trailing icon, optional loading indicator.
 * - Tokens: AynvoraColors, AynvoraSpacing, AynvoraShapes, AynvoraTypography.
 * - States: Default, pressed, disabled, loading.
 * - Accessibility: 48dp minimum interactive touch target height, semantic button role.
 */
@Composable
fun AynvoraButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    event: com.aynvora.core.event.AynvoraClickEvent? = null,
    variant: AynvoraButtonVariant = AynvoraButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val ambientDispatcher = com.aynvora.designsystem.event.LocalAynvoraEventDispatcher.current
    val effectiveOnClick: () -> Unit = {
        if (event != null) {
            ambientDispatcher?.invoke(event)
        }
        onClick()
    }

    val isActionable = enabled && !loading
    val shape = AynvoraShapes.shape8
    val minHeight = 48.dp
    val contentPadding = PaddingValues(
        horizontal = AynvoraSpacing.space16,
        vertical = AynvoraSpacing.space12,
    )

    when (variant) {
        AynvoraButtonVariant.Primary -> {
            Button(
                onClick = effectiveOnClick,
                modifier = modifier
                    .defaultMinSize(minHeight = minHeight)
                    .semantics { role = Role.Button },
                enabled = isActionable,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AynvoraColors.Gold,
                    contentColor = AynvoraColors.CosmicBlack,
                    disabledContainerColor = AynvoraColors.TextMuted.copy(alpha = 0.3f),
                    disabledContentColor = AynvoraColors.TextLightMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    loading = loading,
                    contentColor = AynvoraColors.CosmicBlack,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            }
        }
        AynvoraButtonVariant.Secondary -> {
            Button(
                onClick = effectiveOnClick,
                modifier = modifier
                    .defaultMinSize(minHeight = minHeight)
                    .semantics { role = Role.Button },
                enabled = isActionable,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AynvoraColors.CelestialBlue,
                    contentColor = AynvoraColors.White,
                    disabledContainerColor = AynvoraColors.TextMuted.copy(alpha = 0.3f),
                    disabledContentColor = AynvoraColors.TextLightMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    loading = loading,
                    contentColor = AynvoraColors.White,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            }
        }
        AynvoraButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = effectiveOnClick,
                modifier = modifier
                    .defaultMinSize(minHeight = minHeight)
                    .semantics { role = Role.Button },
                enabled = isActionable,
                shape = shape,
                border = BorderStroke(
                    width = AynvoraBorders.thin,
                    color = if (isActionable) AynvoraColors.Gold else AynvoraColors.TextMuted.copy(alpha = 0.3f),
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AynvoraColors.Gold,
                    disabledContentColor = AynvoraColors.TextLightMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    loading = loading,
                    contentColor = AynvoraColors.Gold,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            }
        }
        AynvoraButtonVariant.Ghost -> {
            TextButton(
                onClick = effectiveOnClick,
                modifier = modifier
                    .defaultMinSize(minHeight = minHeight)
                    .semantics { role = Role.Button },
                enabled = isActionable,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AynvoraColors.GoldLight,
                    disabledContentColor = AynvoraColors.TextLightMuted,
                ),
                contentPadding = contentPadding,
            ) {
                ButtonContent(
                    text = text,
                    loading = loading,
                    contentColor = AynvoraColors.GoldLight,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    contentColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp,
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.size(18.dp)) {
                    leadingIcon()
                }
                Spacer(modifier = Modifier.width(AynvoraSpacing.space8))
            }
            Text(
                text = text,
                style = AynvoraTheme.typography.body14,
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(AynvoraSpacing.space8))
                Box(modifier = Modifier.size(18.dp)) {
                    trailingIcon()
                }
            }
        }
    }
}
