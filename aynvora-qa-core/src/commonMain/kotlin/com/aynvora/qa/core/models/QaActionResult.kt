package com.aynvora.qa.core.models

import kotlinx.serialization.Serializable

@Serializable
data class QaActionResult(
    val actionId: QaActionId,
    val interactionType: String = "TAP",
    val transition: QaStateTransition,
    val classification: QaClassification,
    val confidence: QaConfidence,
    val failureCapsule: QaFailureCapsule? = null,
    val repeatedCount: Int = 1,
) {
    val isSuccess: Boolean
        get() = classification == QaClassification.ACTION_SUCCESS

    val isNoOp: Boolean
        get() = classification == QaClassification.ACTION_NO_OP || classification == QaClassification.ACTION_REPEATED_NO_OP
}
