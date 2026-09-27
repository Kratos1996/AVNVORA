package com.aynvora.designsystem.event

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraDeduplicationPolicy
import com.aynvora.core.event.AynvoraEvent

/**
 * Event-aware clickable Modifier adhering to Master UI Style Guide and Aynvora Event Architecture.
 *
 * Responsibilities:
 * - Emits typed [AynvoraClickEvent] to the ambient [LocalAynvoraEventDispatcher]
 * - Prevents rapid accidental double-dispatch according to [policy]
 * - Preserves semantic [Role] and accessibility
 * - Contains zero navigation or business logic
 */
@Composable
fun Modifier.aynvoraClickable(
    event: AynvoraClickEvent,
    enabled: Boolean = true,
    policy: AynvoraDeduplicationPolicy = AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW,
    role: Role? = null,
    onDispatch: ((AynvoraEvent) -> Unit)? = null,
): Modifier {
    val ambientDispatcher = LocalAynvoraEventDispatcher.current
    val dispatcher = onDispatch ?: ambientDispatcher

    val timeSourceMark = remember { kotlin.time.TimeSource.Monotonic.markNow() }
    val lastClickTimeState = remember { longArrayOf(-1L) }

    return this.clickable(
        enabled = enabled,
        role = role,
        onClick = {
            if (dispatcher == null) return@clickable

            val now = timeSourceMark.elapsedNow().inWholeMilliseconds
            val lastTime = lastClickTimeState[0]

            val shouldDispatch = when (policy) {
                AynvoraDeduplicationPolicy.NO_DEDUP -> true
                AynvoraDeduplicationPolicy.DEDUP_SHORT_WINDOW -> {
                    if (now - lastTime < 350L) {
                        false
                    } else {
                        lastClickTimeState[0] = now
                        true
                    }
                }

                AynvoraDeduplicationPolicy.IDEMPOTENT -> {
                    if (lastTime != 0L) {
                        false
                    } else {
                        lastClickTimeState[0] = now
                        true
                    }
                }
            }

            if (shouldDispatch) {
                dispatcher(event)
            }
        },
    )
}
