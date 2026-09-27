package com.aynvora.designsystem.event

import androidx.compose.runtime.staticCompositionLocalOf
import com.aynvora.core.event.AynvoraEvent

/**
 * CompositionLocal providing the ambient event dispatcher throughout the Compose tree.
 */
val LocalAynvoraEventDispatcher = staticCompositionLocalOf<((AynvoraEvent) -> Unit)?> {
    null
}
