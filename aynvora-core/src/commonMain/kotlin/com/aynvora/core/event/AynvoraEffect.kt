package com.aynvora.core.event

import com.aynvora.core.feature.CoreFeatureId

/**
 * Typed destinations for navigation effects.
 * Arbitrary raw route strings from UI are strictly forbidden.
 */
sealed interface AynvoraNavigationTarget {
    data object Dashboard : AynvoraNavigationTarget
    data object AstrologyHome : AynvoraNavigationTarget
    data object TarotHome : AynvoraNavigationTarget
    data object PalmistryHome : AynvoraNavigationTarget
    data object NumerologyHome : AynvoraNavigationTarget
    data object NumerologyCompare : AynvoraNavigationTarget
    data object NumerologyHistory : AynvoraNavigationTarget
    data class FeatureDetail(val featureId: CoreFeatureId) : AynvoraNavigationTarget
    data class TarotReading(val readingId: String) : AynvoraNavigationTarget
    data class TarotCardDetail(val cardId: String, val deckId: String) : AynvoraNavigationTarget
    data object TarotHistory : AynvoraNavigationTarget
    data object PalmistryHistory : AynvoraNavigationTarget
    data class PalmistryReading(val sessionId: String) : AynvoraNavigationTarget
    data class PalmistryFeatureDetail(val featureName: String) : AynvoraNavigationTarget
    data object AiSettings : AynvoraNavigationTarget
    data object AiDiagnostics : AynvoraNavigationTarget
    data object Settings : AynvoraNavigationTarget
    data object History : AynvoraNavigationTarget
    data object Back : AynvoraNavigationTarget
    data object Close : AynvoraNavigationTarget
}

/**
 * Typed one-shot side-effects emitted by ViewModels and consumed by UI/NavHost.
 * Persistent UI state must never be used to represent one-shot navigation or dialog commands.
 */
sealed interface AynvoraEffect {
    data class Navigate(val target: AynvoraNavigationTarget) : AynvoraEffect
    data class ShowDialog(val dialogId: String, val title: String = "", val message: String = "") :
        AynvoraEffect

    data class ShowBottomSheet(val sheetId: String, val data: Any? = null) : AynvoraEffect
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : AynvoraEffect
    data class Share(val text: String, val title: String? = null) : AynvoraEffect
    data class OpenExternal(val url: String) : AynvoraEffect
    data object DismissDialog : AynvoraEffect
    data object DismissSheet : AynvoraEffect
}

/**
 * Typealias for explicit navigation effects.
 */
typealias AynvoraNavigationEffect = AynvoraEffect.Navigate
