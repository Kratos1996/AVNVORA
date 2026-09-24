package com.aynvora.ui.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.report.GenerateReportPdfUseCase
import com.aynvora.core.report.GenerateReportUseCase
import com.aynvora.core.report.ReportGenerationRequest
import com.aynvora.core.report.ReportGenerationResult
import com.aynvora.core.report.ReportPdfArtifact
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportPdfOutcome
import com.aynvora.core.report.ReportShareResult
import com.aynvora.core.report.ReportShareService
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ShareReportUseCase
import com.aynvora.designsystem.AynvoraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** End-to-end route: deterministic engine output -> ReportDocument -> Compose -> host PDF/share. */
@Composable
fun ReportRoute(
    request: ReportGenerationRequest,
    sdk: AynvoraSdk,
    resolver: ReportTextResolver,
    pdfGenerator: ReportPdfGenerator,
    shareService: ReportShareService,
    analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
    modifier: Modifier = Modifier,
) {
    val generateReport = remember(sdk, analyticsTracker) {
        GenerateReportUseCase(
            sdk,
            analyticsTracker = analyticsTracker
        )
    }
    val generatePdf = remember(pdfGenerator, analyticsTracker) {
        GenerateReportPdfUseCase(
            pdfGenerator,
            analyticsTracker
        )
    }
    val shareReport = remember(shareService, analyticsTracker) {
        ShareReportUseCase(
            shareService,
            analyticsTracker
        )
    }
    var state by remember { mutableStateOf<ReportGenerationResult?>(null) }
    var unexpectedError by remember { mutableStateOf(false) }
    var artifact by remember { mutableStateOf<ReportPdfArtifact?>(null) }
    var pdfStatus by remember { mutableStateOf<String?>(null) }
    var generatingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(request.reportType.id) {
        analyticsTracker.track(AnalyticsEvent.ReportOpened(request.reportType.id))
    }

    LaunchedEffect(request, resolver.language) {
        state = null
        unexpectedError = false
        artifact = null
        pdfStatus = null
        try {
            state = generateReport.execute(request, resolver)
        } catch (_: Exception) {
            unexpectedError = true
        }
    }

    when {
        state == null && !unexpectedError -> ReportLoading(resolver)
        unexpectedError -> ReportUnavailableText(resolver.text(ReportTextKey.ERROR).value)
        state is ReportGenerationResult.Unavailable -> ReportUnavailableText((state as ReportGenerationResult.Unavailable).reason.value)
        state is ReportGenerationResult.Generated -> {
            val document = (state as ReportGenerationResult.Generated).document
            ReportViewer(
                document = document,
                resolver = resolver,
                onGeneratePdf = {
                    if (!generatingPdf) {
                        generatingPdf = true
                        scope.launch {
                            val result =
                                withContext(Dispatchers.Default) { generatePdf.execute(document) }
                            generatingPdf = false
                            when (result) {
                                is ReportPdfOutcome.Generated -> {
                                    artifact = result.artifact
                                    pdfStatus = resolver.text(ReportTextKey.PDF_READY).value
                                }

                                is ReportPdfOutcome.Failure -> {
                                    artifact = null
                                    pdfStatus = resolver.text(ReportTextKey.PDF_FAILED).value
                                }
                            }
                        }
                    }
                },
                onSharePdf = artifact?.let { pdf ->
                    {
                        when (shareReport.execute(pdf, document.metadata.reportTypeId)) {
                            ReportShareResult.Shared -> pdfStatus =
                                resolver.text(ReportTextKey.PDF_READY).value

                            ReportShareResult.Cancelled -> Unit
                            ReportShareResult.UnsupportedPlatform, is ReportShareResult.Failed -> pdfStatus =
                                resolver.text(ReportTextKey.PDF_FAILED).value
                        }
                    }
                },
                pdfStatus = if (generatingPdf) resolver.text(ReportTextKey.PDF_GENERATING).value else pdfStatus,
                modifier = modifier,
            )
        }
    }
}

/** Backwards-compatible name for the original Kundali entry point. */
@Composable
fun KundaliReportRoute(
    request: ReportGenerationRequest,
    sdk: AynvoraSdk,
    resolver: ReportTextResolver,
    pdfGenerator: ReportPdfGenerator,
    shareService: ReportShareService,
    analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
    modifier: Modifier = Modifier,
) = ReportRoute(request, sdk, resolver, pdfGenerator, shareService, analyticsTracker, modifier)

@Composable
private fun ReportLoading(resolver: ReportTextResolver) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
            Text(resolver.text(ReportTextKey.LOADING).value, color = AynvoraTheme.colors.TextLight)
        }
    }
}

@Composable
private fun ReportUnavailableText(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = AynvoraTheme.colors.TextLight)
    }
}
