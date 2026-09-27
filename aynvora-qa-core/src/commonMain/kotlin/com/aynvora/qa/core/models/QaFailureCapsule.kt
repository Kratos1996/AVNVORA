package com.aynvora.qa.core.models

import kotlinx.serialization.Serializable

/**
 * Classification of an observed UI interaction outcome.
 */
@Serializable
enum class QaClassification {
    ACTION_SUCCESS,
    ACTION_NO_OP,
    ACTION_REPEATED_NO_OP,
    ACTION_ERROR,
    ACTION_TIMEOUT,
    ACTION_BLOCKED,
    ACTION_PARTIAL,
    ACTION_UNEXPECTED_STATE,
    ACTION_NAVIGATION_FAILURE,
    ACTION_LOADING_HANG,
    ACTION_RESOURCE_FAILURE,
    ACTION_UNDETERMINED;

    val isFailure: Boolean
        get() = this in listOf(
            ACTION_NO_OP,
            ACTION_REPEATED_NO_OP,
            ACTION_ERROR,
            ACTION_TIMEOUT,
            ACTION_BLOCKED,
            ACTION_UNEXPECTED_STATE,
            ACTION_NAVIGATION_FAILURE,
            ACTION_LOADING_HANG,
            ACTION_RESOURCE_FAILURE
        )
}

@Serializable
enum class QaConfidence {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Detailed diagnostic failure capsule containing exact context to reproduce and fix defects.
 * Strictly avoids PII.
 */
@Serializable
data class QaFailureCapsule(
    val failureId: String,
    val actionId: QaActionId,
    val screen: String,
    val route: String? = null,
    val interactionType: String = "TAP",
    val expected: String,
    val observed: String,
    val repeatedCount: Int = 1,
    val durationMs: Long,
    val classification: QaClassification,
    val confidence: QaConfidence,
    val beforeFingerprint: String,
    val afterFingerprint: String,
    val eventsLogged: List<String> = emptyList(),
    val stackTrace: String? = null,
    val logcatSnippet: String? = null,
    val timestampMs: Long = 0L,
)
