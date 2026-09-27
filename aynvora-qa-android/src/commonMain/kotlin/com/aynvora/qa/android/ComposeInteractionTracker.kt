package com.aynvora.qa.android

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import com.aynvora.qa.core.models.QaActionId

/**
 * Semantics key for QA action identity.
 */
val QaActionIdKey = SemanticsPropertyKey<String>("QaActionId")

var SemanticsPropertyReceiver.qaActionId: String
    get() = throw UnsupportedOperationException("You cannot read qaActionId directly")
    set(value) = set(QaActionIdKey, value)

/**
 * Attaches a stable QA action identity to any Compose component.
 */
fun Modifier.qaAction(actionId: QaActionId): Modifier {
    return this.semantics {
        qaActionId = actionId.identifier
    }
}

fun Modifier.qaAction(
    feature: String,
    screen: String,
    component: String,
    action: String,
    variant: String? = null,
): Modifier {
    return qaAction(QaActionId.of(feature, screen, component, action, variant))
}
