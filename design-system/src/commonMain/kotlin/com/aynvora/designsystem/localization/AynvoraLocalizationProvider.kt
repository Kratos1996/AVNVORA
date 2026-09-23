package com.aynvora.designsystem.localization

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.locale.TextDirection
import com.aynvora.localization.translation.AynvoraTranslator
import com.aynvora.designsystem.AynvoraMotion

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

    CompositionLocalProvider(
        LocalAynvoraLocale provides locale,
        LocalAynvoraTranslator provides translator,
        LocalAynvoraIsRtl provides isRtl,
    ) {
        if (useReducedMotion) {
            // Skip animation for reduced-motion users
            content()
        } else {
            AnimatedContent(
                targetState = locale.localeId,
                transitionSpec = {
                    fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = AynvoraMotion.durationDefault,
                        ),
                    ) togetherWith fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = AynvoraMotion.durationDefault,
                        ),
                    )
                },
                label = "locale_transition",
            ) { targetLocaleId ->
                // Content recomposes for any locale change.
                // The targetLocaleId is intentionally unused — we use CompositionLocals instead.
                @Suppress("UNUSED_EXPRESSION")
                targetLocaleId
                content()
            }
        }
    }
}
