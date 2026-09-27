package com.aynvora.qa.core.models

import kotlinx.serialization.Serializable

/**
 * Structured, safe snapshot of UI and runtime state before or after an interaction.
 * Redacts any PII and retains only structural/behavioral indicators.
 */
@Serializable
data class QaStateSnapshot(
    val route: String? = null,
    val screen: String,
    val isSheetOpen: Boolean = false,
    val isDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val activeLocale: String? = null,
    val isDarkTheme: Boolean = true,
    val eventSequence: Long = 0L,
    val semanticFingerprint: String = "",
    val timestampMs: Long = 0L,
) {
    val structuralKey: String
        get() = "$screen|r=${route ?: "none"}|s=$isSheetOpen|d=$isDialogOpen|l=$isLoading|e=$hasError|loc=$activeLocale|thm=$isDarkTheme|fp=$semanticFingerprint"
}

/**
 * Difference between pre-state snapshot and post-state snapshot.
 */
@Serializable
data class QaStateTransition(
    val preState: QaStateSnapshot,
    val postState: QaStateSnapshot,
    val durationMs: Long,
    val observedLoading: Boolean = false,
    val snackbarObserved: Boolean = false,
    val eventsObserved: List<String> = emptyList(),
    val errorObserved: String? = null,
) {
    val routeChanged: Boolean
        get() = preState.route != postState.route

    val screenChanged: Boolean
        get() = preState.screen != postState.screen

    val sheetStateChanged: Boolean
        get() = preState.isSheetOpen != postState.isSheetOpen

    val dialogStateChanged: Boolean
        get() = preState.isDialogOpen != postState.isDialogOpen

    val themeChanged: Boolean
        get() = preState.isDarkTheme != postState.isDarkTheme

    val localeChanged: Boolean
        get() = preState.activeLocale != postState.activeLocale

    val loadingChanged: Boolean
        get() = preState.isLoading != postState.isLoading || observedLoading

    val errorStateChanged: Boolean
        get() = preState.hasError != postState.hasError || errorObserved != null

    val isIdenticalState: Boolean
        get() = !routeChanged &&
                !screenChanged &&
                !sheetStateChanged &&
                !dialogStateChanged &&
                !themeChanged &&
                !localeChanged &&
                !loadingChanged &&
                !errorStateChanged &&
                preState.semanticFingerprint == postState.semanticFingerprint &&
                eventsObserved.isEmpty() &&
                errorObserved == null
}
