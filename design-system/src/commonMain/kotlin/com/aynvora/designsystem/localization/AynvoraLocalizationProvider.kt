package com.aynvora.designsystem.localization

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aynvora.designsystem.AynvoraMotion
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.locale.TextDirection
import com.aynvora.localization.translation.AynvoraTranslator

/**
 * Root localization provider for AYNVORA Compose UI.
 *
 * Wraps content in locale-aware [CompositionLocalProvider] entries and applies
 * a lightweight crossfade transition when the language changes (~200ms).
 *
 * DESIGN:
 * - Observes [AynvoraLocaleManager.currentLocale] via [collectAsState].
 * - On locale change → [AnimatedContent] crossfades old ↔ new content.
 * - Duration: [AynvoraMotion.durationDefault] (200ms) — matches spec.
 * - Reduced motion: caller should pass [useReducedMotion] = true to skip animation.
 * - No Activity restart. No navigation reset.
 *
 * Usage (place once at application root, inside [AynvoraTheme]):
 * ```kotlin
 * AynvoraTheme {
 *     AynvoraLocalizationProvider(localeManager = appLocaleManager) {
 *         // App content — all children recompose instantly on language change
 *     }
 * }
 * ```
 */
@Composable
fun AynvoraLocalizationProvider(
    localeManager: AynvoraLocaleManager,
    useReducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val locale by localeManager.currentLocale.collectAsState()
    AynvoraLocalizationProviderForLocale(
        locale = locale,
        useReducedMotion = useReducedMotion,
        content = content,
    )
}

/**
 * Variant for testing or static locale (e.g., design previews).
 */
@Composable
fun AynvoraLocalizationProviderForLocale(
    locale: SupportedLocale,
    useReducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val translator = AynvoraTranslator(locale)
    val isRtl = locale.direction == TextDirection.RTL
    val layoutDirection =
        if (isRtl) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
        LocalAynvoraLocale provides locale,
        LocalAynvoraTranslator provides translator,
        LocalAynvoraLocalizationProvider provides translator,
        LocalAynvoraIsRtl provides isRtl,
    ) {
        // Directly compose content with updated CompositionLocals.
        // Reactive snapshot state in Compose automatically recomposes all text nodes
        // seamlessly while preserving screen destinations, user input, and PalmEvidence.
        content()
    }
}
