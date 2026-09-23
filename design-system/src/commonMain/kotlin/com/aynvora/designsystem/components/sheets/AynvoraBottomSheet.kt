package com.aynvora.designsystem.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import dev.ishant.cottonsheet.CottonSheetController
import dev.ishant.cottonsheet.CottonSheetHost
import dev.ishant.cottonsheet.CottonSheetParams

/**
 * Default styling and parameters for AYNVORA bottom sheets adhering to the Master UI Style Guide.
 */
object AynvoraBottomSheetDefaults {

    val SheetMaxWidth: Dp = 560.dp
    val SheetCornerRadius: Dp = 16.dp

    /**
     * Builds [CottonSheetParams] pre-configured with AYNVORA design tokens.
     */
    @Composable
    fun params(
        skipPartiallyExpanded: Boolean = true,
        sheetMaxWidth: Dp = SheetMaxWidth,
        isFullScreen: Boolean = false,
        isDismissable: Boolean = true,
    ): CottonSheetParams {
        val colors = AynvoraTheme.colors
        val isDark = AynvoraTheme.isDark

        return CottonSheetParams(
            skipPartiallyExpanded = skipPartiallyExpanded,
            sheetMaxWidth = sheetMaxWidth,
            shape = RoundedCornerShape(
                topStart = SheetCornerRadius,
                topEnd = SheetCornerRadius,
                bottomStart = 0.dp,
                bottomEnd = 0.dp,
            ),
            containerColor = if (isDark) colors.CosmicNavy else colors.White,
            contentColor = if (isDark) colors.TextLight else colors.TextDark,
            scrimColor = colors.CosmicBlack.copy(alpha = 0.72f),
            showDragHandle = true,
            customDragHandle = { AynvoraDragHandle() },
            isFullScreen = isFullScreen,
            isDismissable = isDismissable,
        )
    }
}

/**
 * Standard AYNVORA gold-accented drag handle knob for bottom sheets.
 */
@Composable
fun AynvoraDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AynvoraSpacing.space12),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                    color = AynvoraTheme.colors.Gold.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(2.dp),
                ),
        )
    }
}

/**
 * Reusable header for AYNVORA bottom sheets with title, optional subtitle, and close button.
 */
@Composable
fun AynvoraBottomSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onCloseClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = AynvoraSpacing.space24,
                end = AynvoraSpacing.space16,
                top = AynvoraSpacing.space8,
                bottom = AynvoraSpacing.space16,
            ),
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
 * Root host for AYNVORA bottom sheets powered by CottonSheet.
 * Place once at the root of the composition hierarchy inside [AynvoraTheme].
 */
@Composable
fun AynvoraBottomSheetHost(
    content: @Composable () -> Unit,
) {
    CottonSheetHost(content = content)
}

/**
 * Convenience extension to launch an AYNVORA-styled bottom sheet via [CottonSheetController].
 */
fun CottonSheetController.showAynvoraSheet(
    params: CottonSheetParams = CottonSheetParams(),
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    show(params = params, content = content)
}
