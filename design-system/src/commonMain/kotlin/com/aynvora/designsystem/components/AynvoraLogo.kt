package com.aynvora.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.generated.resources.Res
import com.aynvora.designsystem.generated.resources.logo_avnvora
import com.aynvora.designsystem.generated.resources.logo_avnvora_dark
import com.aynvora.designsystem.generated.resources.logo_avnvora_transparent
import com.aynvora.designsystem.generated.resources.logo_txt_avnvora
import org.jetbrains.compose.resources.painterResource

/**
 * Variants of the official AYNVORA logo drawable from design system.
 */
enum class AynvoraLogoVariant {
    /** Adaptive transparent emblem matching background seamlessly. */
    Transparent,

    /** Dark background emblem with cosmic gold gradients. */
    Dark,

    /** Default full emblem with brand backing. */
    Default,

    /** Logo with text wordmark. */
    Wordmark,
}

/**
 * Official AYNVORA brand logo component powered by Compose Resources.
 *
 * Uses the high-resolution logo assets stored in design system composeResources.
 */
@Composable
fun AynvoraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    variant: AynvoraLogoVariant = AynvoraLogoVariant.Transparent,
    contentDescription: String = "AYNVORA Logo",
) {
    val isDark = AynvoraTheme.isDark

    val drawableResource = when (variant) {
        AynvoraLogoVariant.Transparent -> Res.drawable.logo_avnvora_transparent
        AynvoraLogoVariant.Dark -> Res.drawable.logo_avnvora_dark
        AynvoraLogoVariant.Wordmark -> Res.drawable.logo_txt_avnvora
        AynvoraLogoVariant.Default -> if (isDark) {
            Res.drawable.logo_avnvora_dark
        } else {
            Res.drawable.logo_avnvora
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(drawableResource),
            contentDescription = contentDescription,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    }
}
