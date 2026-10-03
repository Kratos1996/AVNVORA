package com.aynvora.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraTheme

/**
 * Enforced AYNVORA Typography System.
 *
 * All typography is IMMUTABLE. Developers do not pass custom [androidx.compose.ui.text.TextStyle] or custom font sizes.
 * Font family, size, line-height, and weight are automatically resolved by the design system across Mobile, Tablet, and Desktop.
 */

@Composable
fun AynvoraDisplay(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraColors.Gold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.display40,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraDisplayLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraColors.Gold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraDisplay(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.title20,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTitleLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraTitle(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraTitleMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.title18,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTitleSmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16.copy(fontWeight = FontWeight.SemiBold),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraRegularText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextRegular(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraRegularText(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraRegularMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16.copy(fontWeight = FontWeight.Medium),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraRegularBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16.copy(fontWeight = FontWeight.Bold),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextRegularBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraRegularBold(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraRegularExtraBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16.copy(fontWeight = FontWeight.ExtraBold),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraRegularUnderline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body16.copy(textDecoration = TextDecoration.Underline),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textSecondary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body14,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextSmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textSecondary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraSmallText(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraSmallMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textSecondary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Medium),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraSmallBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Bold),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextSmallBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraSmallBold(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraExtraSmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textMuted,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.caption12,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextExtraSmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraTheme.colors.textMuted,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraExtraSmallText(text, modifier, color, textAlign, maxLines, overflow)

@Composable
fun AynvoraExtraSmallBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraColors.Gold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
fun AynvoraTextExtraSmallBold(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AynvoraColors.Gold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) = AynvoraExtraSmallBold(text, modifier, color, textAlign, maxLines, overflow)
