package com.aynvora.qa.core.report

import com.aynvora.qa.core.models.QaActionResult
import com.aynvora.qa.core.models.QaClassification
import com.aynvora.qa.core.models.QaFailureCapsule
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FeatureStats(
    val feature: String,
    val totalActions: Int,
    val success: Int,
    val noOp: Int,
    val repeatedNoOp: Int,
    val error: Int,
    val timeout: Int,
    val loadingHang: Int,
    val undetermined: Int,
)

@Serializable
data class QaSessionSummary(
    val sessionId: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val androidVersion: String,
    val appBuild: String,
    val timestampMs: Long,
    val totalActions: Int,
    val totalSuccess: Int,
    val totalNoOp: Int,
    val totalRepeatedNoOp: Int,
    val totalError: Int,
    val totalTimeout: Int,
    val totalLoadingHang: Int,
    val totalUndetermined: Int,
    val screensVisited: List<String>,
    val featureStats: List<FeatureStats>,
    val failureCapsules: List<QaFailureCapsule>,
)

object QaSessionReporter {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun generateSummary(
        sessionId: String,
        deviceModel: String = "Samsung Galaxy S23 Ultra (SM-S918B)",
        deviceManufacturer: String = "samsung",
        androidVersion: String = "16 (SDK 36)",
        appBuild: String = "1.0.0-qa-sentinel",
        timestampMs: Long,
        screensVisited: Set<String>,
        history: List<QaActionResult>,
        failureCapsules: List<QaFailureCapsule>,
    ): QaSessionSummary {
        val features = history.map { it.actionId.feature }.distinct()

        val featureStatsList = features.map { feat ->
            val featActions = history.filter { it.actionId.feature == feat }
            FeatureStats(
                feature = feat,
                totalActions = featActions.size,
                success = featActions.count { it.classification == QaClassification.ACTION_SUCCESS },
                noOp = featActions.count { it.classification == QaClassification.ACTION_NO_OP },
                repeatedNoOp = featActions.count { it.classification == QaClassification.ACTION_REPEATED_NO_OP },
                error = featActions.count { it.classification == QaClassification.ACTION_ERROR || it.classification == QaClassification.ACTION_NAVIGATION_FAILURE || it.classification == QaClassification.ACTION_RESOURCE_FAILURE },
                timeout = featActions.count { it.classification == QaClassification.ACTION_TIMEOUT },
                loadingHang = featActions.count { it.classification == QaClassification.ACTION_LOADING_HANG },
                undetermined = featActions.count { it.classification == QaClassification.ACTION_UNDETERMINED || it.classification == QaClassification.ACTION_UNEXPECTED_STATE },
            )
        }

        return QaSessionSummary(
            sessionId = sessionId,
            deviceModel = deviceModel,
            deviceManufacturer = deviceManufacturer,
            androidVersion = androidVersion,
            appBuild = appBuild,
            timestampMs = timestampMs,
            totalActions = history.size,
            totalSuccess = history.count { it.classification == QaClassification.ACTION_SUCCESS },
            totalNoOp = history.count { it.classification == QaClassification.ACTION_NO_OP },
            totalRepeatedNoOp = history.count { it.classification == QaClassification.ACTION_REPEATED_NO_OP },
            totalError = history.count { it.classification == QaClassification.ACTION_ERROR || it.classification == QaClassification.ACTION_NAVIGATION_FAILURE || it.classification == QaClassification.ACTION_RESOURCE_FAILURE },
            totalTimeout = history.count { it.classification == QaClassification.ACTION_TIMEOUT },
            totalLoadingHang = history.count { it.classification == QaClassification.ACTION_LOADING_HANG },
            totalUndetermined = history.count { it.classification == QaClassification.ACTION_UNDETERMINED || it.classification == QaClassification.ACTION_UNEXPECTED_STATE },
            screensVisited = screensVisited.toList().sorted(),
            featureStats = featureStatsList,
            failureCapsules = failureCapsules,
        )
    }

    fun toJson(summary: QaSessionSummary): String {
        return json.encodeToString(summary)
    }

    fun toMarkdown(summary: QaSessionSummary): String {
        return buildString {
            appendLine("# AYNVORA INTERACTION SENTINEL VERIFICATION REPORT")
            appendLine()
            appendLine("**Session ID**: `${summary.sessionId}`  ")
            appendLine("**Device**: ${summary.deviceManufacturer} ${summary.deviceModel} (Android ${summary.androidVersion})  ")
            appendLine("**App Build**: `${summary.appBuild}`  ")
            appendLine("**Timestamp**: ${summary.timestampMs}  ")
            appendLine()
            appendLine("## Overall Summary")
            appendLine()
            appendLine("- **Total Actions Tested**: ${summary.totalActions}")
            appendLine("- **Success**: ${summary.totalSuccess}")
            appendLine("- **No-Op Candidates**: ${summary.totalNoOp}")
            appendLine("- **Repeated No-Ops**: ${summary.totalRepeatedNoOp}")
            appendLine("- **Errors / Navigation Failures**: ${summary.totalError}")
            appendLine("- **Timeouts**: ${summary.totalTimeout}")
            appendLine("- **Loading Hangs**: ${summary.totalLoadingHang}")
            appendLine("- **Undetermined**: ${summary.totalUndetermined}")
            appendLine()
            appendLine("## Visited Screens")
            appendLine()
            summary.screensVisited.forEach { appendLine("- `$it`") }
            appendLine()
            appendLine("## Feature Breakdown")
            appendLine()
            appendLine("| Feature | Actions Tested | Success | No-Op | Rep No-Op | Error | Hang | Undetermined |")
            appendLine("|:---|---:|---:|---:|---:|---:|---:|---:|")
            summary.featureStats.forEach { stat ->
                appendLine("| ${stat.feature} | ${stat.totalActions} | ${stat.success} | ${stat.noOp} | ${stat.repeatedNoOp} | ${stat.error} | ${stat.loadingHang} | ${stat.undetermined} |")
            }
            appendLine()
            appendLine("## Failure Evidence Capsules")
            appendLine()
            if (summary.failureCapsules.isEmpty()) {
                appendLine("✅ *No failure capsules recorded during this session.*")
            } else {
                summary.failureCapsules.forEach { capsule ->
                    appendLine("### Failure Capsule: `${capsule.failureId}`")
                    appendLine("- **Action**: `${capsule.actionId.identifier}`")
                    appendLine("- **Screen / Route**: `${capsule.screen}` / `${capsule.route ?: "none"}`")
                    appendLine("- **Classification**: `${capsule.classification}`")
                    appendLine("- **Confidence**: `${capsule.confidence}`")
                    appendLine("- **Repeated Count**: ${capsule.repeatedCount}")
                    appendLine("- **Duration**: ${capsule.durationMs} ms")
                    appendLine("- **Expected**: ${capsule.expected}")
                    appendLine("- **Observed**: ${capsule.observed}")
                    if (capsule.eventsLogged.isNotEmpty()) {
                        appendLine("- **Events Logged**: `${capsule.eventsLogged.joinToString(", ")}`")
                    }
                    if (capsule.stackTrace != null) {
                        appendLine("```")
                        appendLine(capsule.stackTrace)
                        appendLine("```")
                    }
                    appendLine()
                }
            }
        }
    }
}
