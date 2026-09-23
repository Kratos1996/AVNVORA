package com.aynvora.designsystem.localization

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.locale.TextDirection
import com.aynvora.localization.translation.AynvoraTranslator

/**
 * Composition local providing the currently active [SupportedLocale] to the Compose tree.
 *
 * Changes to this value trigger recomposition throughout the tree, enabling
 * INSTANT language switching without Activity restart.
 *
 * Usage:
 * ```kotlin
 * val locale = LocalAynvoraLocale.current
 * ```
 */
val LocalAynvoraLocale = compositionLocalOf<SupportedLocale> {
    LanguageRegistry.defaultLocale()
}

/**
 * Composition local providing the [AynvoraTranslator] for the current locale.
 *
 * Usage:
 * ```kotlin
 * val translator = LocalAynvoraTranslator.current
 * val text = translator.translate(TranslationKey.App.AppName)
 * ```
 */
val LocalAynvoraTranslator = compositionLocalOf<AynvoraTranslator> {
    AynvoraTranslator(LanguageRegistry.defaultLocale())
}

/**
 * Composition local for the layout direction derived from the current locale.
 *
 * Compose [CompositionLocalProvider] should set this to
 * [androidx.compose.ui.unit.LayoutDirection.Rtl] when [SupportedLocale.isRtl] is true.
 *
 * Usage:
 * ```kotlin
 * val isRtl = LocalAynvoraIsRtl.current
 * ```
 */
val LocalAynvoraIsRtl = staticCompositionLocalOf { false }
