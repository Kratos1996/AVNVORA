package com.aynvora.core.report

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker

/** Host implementation renders the exact immutable model consumed by ReportViewer. */
interface ReportPdfGenerator {
    fun generate(document: ReportDocument): ReportPdfResult
}

/** Explicit fallback binding for hosts without a platform PDF implementation (currently iOS). */
class UnsupportedReportPdfGenerator : ReportPdfGenerator {
    override fun generate(document: ReportDocument) =
        ReportPdfResult.Failed(ReportPdfError.UnsupportedPlatform)
}

data class ReportPdfArtifact(
    val fileName: String,
    val mimeType: String = "application/pdf",
    val bytes: ByteArray,
)

sealed interface ReportPdfError {
    data class Failed(val safeCode: String) : ReportPdfError
    data object UnsupportedPlatform : ReportPdfError
}

sealed interface ReportPdfResult {
    data class Generated(val artifact: ReportPdfArtifact) : ReportPdfResult
    data class Failed(val error: ReportPdfError) : ReportPdfResult
}

sealed interface ReportPdfOutcome {
    data class Generated(val artifact: ReportPdfArtifact) : ReportPdfOutcome
    data class Failure(val code: ReportErrorCode) : ReportPdfOutcome
}

class GenerateReportPdfUseCase(
    private val generator: ReportPdfGenerator,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    fun execute(document: ReportDocument): ReportPdfOutcome =
        when (val result = runCatching { generator.generate(document) }.getOrElse {
            ReportPdfResult.Failed(ReportPdfError.Failed("render_failed"))
        }) {
            is ReportPdfResult.Generated -> {
                analyticsTracker.track(AnalyticsEvent.ReportPdfGenerated(document.metadata.reportTypeId))
                if (document.metadata.reportTypeId == ReportType.GARUDA_PURAN.id) analyticsTracker.track(
                    AnalyticsEvent.GarudaPuranPdfGenerated
                )
                ReportPdfOutcome.Generated(result.artifact)
            }

            is ReportPdfResult.Failed -> {
                if (result.error is ReportPdfError.UnsupportedPlatform) {
                    ReportPdfOutcome.Failure(ReportErrorCode.PLATFORM_UNSUPPORTED)
                } else {
                    analyticsTracker.track(
                        AnalyticsEvent.ReportPdfFailed(
                            document.metadata.reportTypeId,
                            (result.error as ReportPdfError.Failed).safeCode
                        )
                    )
                    ReportPdfOutcome.Failure(ReportErrorCode.PDF_GENERATION_FAILED)
                }
            }
        }
}

sealed interface ReportShareResult {
    data object Shared : ReportShareResult
    data object Cancelled : ReportShareResult
    data object UnsupportedPlatform : ReportShareResult
    data class Failed(val safeCode: String) : ReportShareResult
}

interface ReportShareService {
    fun share(artifact: ReportPdfArtifact): ReportShareResult
}

/** Explicit fallback binding for hosts without a platform share/export implementation. */
class UnsupportedReportShareService : ReportShareService {
    override fun share(artifact: ReportPdfArtifact) = ReportShareResult.UnsupportedPlatform
}

class ShareReportUseCase(
    private val shareService: ReportShareService,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    fun execute(artifact: ReportPdfArtifact, reportTypeId: String): ReportShareResult {
        val result =
            runCatching { shareService.share(artifact) }.getOrElse { ReportShareResult.Failed("share_failed") }
        if (result == ReportShareResult.Shared) {
            analyticsTracker.track(AnalyticsEvent.ReportShared(reportTypeId))
            if (reportTypeId == ReportType.GARUDA_PURAN.id) analyticsTracker.track(AnalyticsEvent.GarudaPuranShared)
        }
        return result
    }
}
