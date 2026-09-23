package dev.ishant.popbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf

/**
 * Internal model for a PopBox entry in the stack.
 */
internal data class PopBoxEntry(
    val id: Long,
    val params: PopBoxParams,
    val content: @Composable (dismiss: () -> Unit) -> Unit,
)

/**
 * Controller to manage showing and dismissing PopBoxes from anywhere.
 */
class PopBoxController {
    private var nextId = 0L
    internal val stack = mutableStateListOf<PopBoxEntry>()

    /**
     * Show a PopBox.
     * @param params Configuration for the PopBox.
     * @param content Composable content, receives a dismiss lambda.
     */
    fun show(
        params: PopBoxParams = PopBoxParams(),
        content: @Composable (dismiss: () -> Unit) -> Unit,
    ) {
        val id = nextId++
        stack.add(PopBoxEntry(id = id, params = params, content = content))
    }

    /**
     * Dismiss the top-most PopBox.
     */
    fun dismiss() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    /**
     * Dismiss all open PopBoxes.
     */
    fun dismissAll() {
        stack.clear()
    }

    internal fun dismissById(id: Long) {
        stack.removeAll { it.id == id }
    }
}

/**
 * CompositionLocal to access the PopBoxController.
 */
val LocalPopBoxController = compositionLocalOf<PopBoxController> {
    error("LocalPopBoxController not found. Wrap your root with PopBoxHost { ... }")
}
