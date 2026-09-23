package com.aynvora.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.localization.locale.SupportedLocale

/**
 * Reusable language selector row item for the AYNVORA design system.
 *
 * Design requirements met:
 * ✅ 48dp minimum touch target (WCAG 2.1 minimum)
 * ✅ Shows native language name (nativeName) as primary label
 * ✅ Shows English name as secondary hint
 * ✅ Selected state via checkmark icon + highlighted border
 * ✅ Keyboard navigable (Surface handles focus)
 * ✅ Light and dark theme compliant
 * ✅ RTL-aware (Row uses Start/End semantics)
 * ✅ Accessibility: content description + role + selected semantic
 * ✅ Disabled state supported
 *
 * Usage:
 * ```kotlin
 * LanguageRegistry.availableLocales().forEach { locale ->
 *     AynvoraLanguageSelectorItem(
 *         locale = locale,
 *         isSelected = locale == currentLocale,
 *         onSelect = { localeManager.setLocale(locale) },
 *     )
 * }
 * ```
 */
@Composable
fun AynvoraLanguageSelectorItem(
    locale: SupportedLocale,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val borderColor = if (isSelected) {
        AynvoraTheme.colors.Gold
    } else {
        AynvoraTheme.colors.CosmicIndigo
    }

    val containerColor = if (isSelected) {
        AynvoraTheme.colors.CosmicIndigo
    } else {
        AynvoraTheme.colors.CosmicNavy
    }

    val contentDesc = if (isSelected) {
        "${locale.nativeName} (${locale.englishName}) — selected"
    } else {
        "${locale.nativeName} (${locale.englishName})"
    }

    Surface(
        onClick = onSelect,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp) // Minimum 48dp touch target
            .semantics {
                contentDescription = contentDesc
                role = Role.Button
                selected = isSelected
            },
        shape = AynvoraTheme.shapes.shape12,
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = borderColor,
        ),
        tonalElevation = if (isSelected) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = locale.nativeName,
                    style = AynvoraTheme.typography.body16,
                    color = if (isSelected) {
                        AynvoraTheme.colors.GoldLight
                    } else {
                        AynvoraTheme.colors.TextLight
                    },
                )
                if (locale.nativeName != locale.englishName) {
                    Text(
                        text = locale.englishName,
                        style = AynvoraTheme.typography.body14,
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                // Selected checkmark indicator
                Text(
                    text = "✓",
                    style = AynvoraTheme.typography.title18,
                    color = AynvoraTheme.colors.Gold,
                )
            }
        }
    }
}

/**
 * Full language selector list component.
 *
 * Renders all [availableLocales] as a vertical list of [AynvoraLanguageSelectorItem] rows.
 *
 * Usage:
 * ```kotlin
 * AynvoraLanguageSelector(
 *     availableLocales = LanguageRegistry.availableLocales(),
 *     selectedLocale = currentLocale,
 *     onLocaleSelected = { localeManager.setLocale(it) },
 * )
 * ```
 */
@Composable
fun AynvoraLanguageSelector(
    availableLocales: List<SupportedLocale>,
    selectedLocale: SupportedLocale,
    onLocaleSelected: (SupportedLocale) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableLocales.forEach { locale ->
            AynvoraLanguageSelectorItem(
                locale = locale,
                isSelected = locale.localeId == selectedLocale.localeId,
                onSelect = { onLocaleSelected(locale) },
            )
        }
    }
}
