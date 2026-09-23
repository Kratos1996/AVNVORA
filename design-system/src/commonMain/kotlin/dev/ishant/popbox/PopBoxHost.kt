package dev.ishant.popbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Root host for PopBoxes. Place this at the top of your composition.
 */
@Composable
fun PopBoxHost(content: @Composable () -> Unit) {
    val controller = remember { PopBoxController() }

    CompositionLocalProvider(LocalPopBoxController provides controller) {
        content()

        val entries = controller.stack.toList()
        entries.forEach { entry ->
            key(entry.id) {
                val p = entry.params
                val dismiss = {
                    p.onDismissRequest?.invoke()
                    controller.dismissById(entry.id)
                }

                Dialog(
                    onDismissRequest = {
                        if (!p.disableOuterTap) {
                            dismiss()
                        }
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = !p.disableOuterTap,
                        dismissOnClickOutside = !p.disableOuterTap,
                        usePlatformDefaultWidth = false,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, shape = p.shape)
                            .padding(horizontal = p.horizontalPadding)
                            .fillMaxWidth()
                            .background(
                                color = p.containerColor ?: MaterialTheme.colorScheme.surface,
                                shape = p.shape,
                            )
                            .padding(p.contentPadding),
                    ) {
                        entry.content(dismiss)
                    }
                }
            }
        }
    }
}
