package com.aynvora.ui.garudapuran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.garudapuran.GarudaPuranCatalog
import com.aynvora.core.garudapuran.GarudaPuranContentStatus
import com.aynvora.core.garudapuran.GarudaPuranTextKey
import com.aynvora.core.garudapuran.GarudaPuranTextResolver
import com.aynvora.core.garudapuran.GarudaPuranTopicAvailability
import com.aynvora.core.garudapuran.GarudaPuranTopicContent
import com.aynvora.core.garudapuran.GarudaPuranTopicId
import com.aynvora.core.garudapuran.GetGarudaPuranCatalogUseCase
import com.aynvora.core.garudapuran.GetGarudaPuranTopicUseCase
import com.aynvora.core.report.PrepareGarudaPuranReportUseCase
import com.aynvora.core.report.ReportGenerationRequest
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportShareService
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ReportType
import com.aynvora.core.result.AynvoraResult
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.ui.report.ReportRoute
import kotlinx.coroutines.launch

/**
 * Offline-first Garuda Puran entry route. Source items are read through Domain use cases and
 * reports are delegated to the shared ReportRoute/ReportDocument/PDF pipeline.
 */
@Composable
fun GarudaPuranRoute(
    sdk: AynvoraSdk,
    catalogUseCase: GetGarudaPuranCatalogUseCase,
    topicUseCase: GetGarudaPuranTopicUseCase,
    prepareReportUseCase: PrepareGarudaPuranReportUseCase,
    garudaText: GarudaPuranTextResolver,
    reportText: ReportTextResolver,
    pdfGenerator: ReportPdfGenerator,
    shareService: ReportShareService,
    nowEpochMillis: () -> Long,
    analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
    modifier: Modifier = Modifier,
) {
    var loading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var catalog by remember { mutableStateOf<GarudaPuranCatalog?>(null) }
    var selectedTopic by remember { mutableStateOf<GarudaPuranTopicId?>(null) }
    var topicLoading by remember { mutableStateOf(false) }
    var topicFailed by remember { mutableStateOf(false) }
    var topicContent by remember { mutableStateOf<GarudaPuranTopicContent?>(null) }
    var reportLoading by remember { mutableStateOf(false) }
    var reportRequest by remember { mutableStateOf<ReportGenerationRequest?>(null) }
    var reportUnavailable by remember { mutableStateOf(false) }
    val language = reportText.language
    val scope = rememberCoroutineScope()

    LaunchedEffect(analyticsTracker) {
        analyticsTracker.track(AnalyticsEvent.GarudaPuranOpened)
    }
    LaunchedEffect(catalogUseCase, garudaText.languageCode, language) {
        loading = true
        loadFailed = garudaText.languageCode != language.code
        catalog = null
        if (!loadFailed) {
            when (val result = catalogUseCase.execute(language.code)) {
                is AynvoraResult.Success -> catalog = result.value
                is AynvoraResult.Failure -> loadFailed = true
            }
        }
        loading = false
    }
    LaunchedEffect(selectedTopic, topicUseCase, language) {
        val topic = selectedTopic ?: return@LaunchedEffect
        topicLoading = true
        topicFailed = false
        topicContent = null
        when (val result = topicUseCase.execute(topic, language.code)) {
            is AynvoraResult.Success -> topicContent = result.value
            is AynvoraResult.Failure -> topicFailed = true
        }
        topicLoading = false
    }

    val request = reportRequest
    if (request != null) {
        Column(modifier.fillMaxSize().padding(16.sdp)) {
            AynvoraButton(
                text = garudaText.text(GarudaPuranTextKey.BACK_TO_TOPICS),
                variant = AynvoraButtonVariant.Outlined,
                onClick = { reportRequest = null },
            )
            Spacer(Modifier.height(8.sdp))
            ReportRoute(
                request = request,
                sdk = sdk,
                resolver = reportText,
                pdfGenerator = pdfGenerator,
                shareService = shareService,
                analyticsTracker = analyticsTracker,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(16.sdp)) {
        Text(
            garudaText.text(GarudaPuranTextKey.PAGE_TITLE),
            style = AynvoraTheme.typography.title20.copy(fontSize = 20.ssp),
            color = AynvoraTheme.colors.Gold,
        )
        Spacer(Modifier.height(4.sdp))
        Text(
            garudaText.text(GarudaPuranTextKey.PAGE_SUBTITLE),
            style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
        )
        Spacer(Modifier.height(12.sdp))

        when {
            loading -> GarudaLoading(garudaText)
            loadFailed || catalog == null -> {
                GarudaMessage(garudaText.text(GarudaPuranTextKey.LOAD_ERROR))
                AynvoraButton(garudaText.text(GarudaPuranTextKey.RETRY), onClick = {
                    loading = true
                    scope.launch {
                        when (val result = catalogUseCase.execute(language.code)) {
                            is AynvoraResult.Success -> {
                                catalog = result.value; loadFailed = false
                            }

                            is AynvoraResult.Failure -> loadFailed = true
                        }
                        loading = false
                    }
                }, variant = AynvoraButtonVariant.Outlined)
            }

            selectedTopic != null -> {
                AynvoraButton(garudaText.text(GarudaPuranTextKey.BACK_TO_TOPICS), onClick = {
                    selectedTopic = null
                    topicContent = null
                }, variant = AynvoraButtonVariant.Outlined)
                Spacer(Modifier.height(8.sdp))
                when {
                    topicLoading -> GarudaLoading(garudaText)
                    topicFailed || topicContent == null -> GarudaMessage(
                        garudaText.text(
                            GarudaPuranTextKey.LOAD_ERROR
                        )
                    )

                    topicContent!!.items.isEmpty() -> GarudaMessage(
                        topicContent!!.availability.unavailableReason?.let(garudaText::unavailableReason)
                            ?: garudaText.text(GarudaPuranTextKey.CONTENT_UNAVAILABLE)
                    )

                    else -> GarudaTopicDetails(topicContent!!, garudaText)
                }
            }

            catalog!!.topics.none { it.status == GarudaPuranContentStatus.AVAILABLE } -> {
                Text(
                    garudaText.text(GarudaPuranTextKey.AVAILABLE_TOPICS),
                    style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                    color = AynvoraTheme.colors.TextLight,
                )
                Spacer(Modifier.height(8.sdp))
                GarudaMessage(
                    catalog!!.topics.firstOrNull()?.unavailableReason?.let(garudaText::unavailableReason)
                        ?: garudaText.text(GarudaPuranTextKey.CONTENT_UNAVAILABLE)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.sdp)) {
                    items(catalog!!.topics, key = { it.topicId.name }) { availability ->
                        TopicCard(availability, garudaText, onClick = {})
                    }
                }
            }

            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        garudaText.text(GarudaPuranTextKey.AVAILABLE_TOPICS),
                        style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                        color = AynvoraTheme.colors.TextLight,
                    )
                    AynvoraButton(
                        text = if (reportLoading) garudaText.text(GarudaPuranTextKey.REPORT_LOADING) else garudaText.text(
                            GarudaPuranTextKey.GENERATE_REPORT
                        ),
                        variant = AynvoraButtonVariant.Primary,
                        onClick = {
                            if (!reportLoading) {
                                reportLoading = true
                                reportUnavailable = false
                                scope.launch {
                                    val timestamp = nowEpochMillis()
                                    val queryId = "garuda_" + timestamp
                                    when (val result = prepareReportUseCase.execute(
                                        language,
                                        timestamp,
                                        queryId
                                    )) {
                                        is AynvoraResult.Success -> {
                                            reportRequest = ReportGenerationRequest(
                                                reportType = ReportType.GARUDA_PURAN,
                                                language = language,
                                                generatedAtEpochMs = timestamp,
                                                generatorInput = result.value,
                                                evidenceGraph = result.value.evidenceGraph,
                                            )
                                        }

                                        is AynvoraResult.Failure -> reportUnavailable = true
                                    }
                                    reportLoading = false
                                }
                            }
                        },
                    )
                }
                if (reportUnavailable) GarudaMessage(garudaText.text(GarudaPuranTextKey.CONTENT_UNAVAILABLE))
                Spacer(Modifier.height(8.sdp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.sdp)) {
                    items(catalog!!.topics, key = { it.topicId.name }) { availability ->
                        TopicCard(availability, garudaText) {
                            if (availability.status == GarudaPuranContentStatus.AVAILABLE) {
                                analyticsTracker.track(
                                    AnalyticsEvent.GarudaPuranTopicOpened(
                                        availability.topicId
                                    )
                                )
                                selectedTopic = availability.topicId
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicCard(
    availability: GarudaPuranTopicAvailability,
    resolver: GarudaPuranTextResolver,
    onClick: () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Outlined,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(Modifier.fillMaxWidth().padding(12.sdp)) {
            Text(
                resolver.topicTitle(availability.topicId),
                color = AynvoraTheme.colors.GoldLight,
                style = AynvoraTheme.typography.title18
            )
            Spacer(Modifier.height(4.sdp))
            if (availability.status == GarudaPuranContentStatus.AVAILABLE) {
                Text(
                    availability.itemCount.toString(),
                    color = AynvoraTheme.colors.TextLightSecondary
                )
                AynvoraButton(
                    text = resolver.text(GarudaPuranTextKey.TOPIC_DETAILS),
                    variant = AynvoraButtonVariant.Outlined,
                    onClick = onClick,
                )
            } else {
                Text(
                    availability.unavailableReason?.let(resolver::unavailableReason)
                        ?: resolver.text(GarudaPuranTextKey.CONTENT_UNAVAILABLE),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }
    }
}

@Composable
private fun GarudaTopicDetails(
    content: GarudaPuranTopicContent,
    resolver: GarudaPuranTextResolver
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.sdp)) {
        item {
            Text(
                resolver.text(GarudaPuranTextKey.TOPIC_DETAILS),
                color = AynvoraTheme.colors.Gold,
                style = AynvoraTheme.typography.title18
            )
        }
        items(content.items, key = { it.contentId }) { item ->
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Outlined,
                containerColor = AynvoraTheme.colors.CosmicNavy,
                contentColor = AynvoraTheme.colors.TextLight,
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(12.sdp),
                    verticalArrangement = Arrangement.spacedBy(5.sdp)
                ) {
                    Text(
                        item.section.title,
                        color = AynvoraTheme.colors.GoldLight,
                        style = AynvoraTheme.typography.title18
                    )
                    Text(
                        resolver.text(GarudaPuranTextKey.REFERENCE) + ": " + item.reference.canonicalReferenceId,
                        color = AynvoraTheme.colors.TextLightSecondary
                    )
                    Text(
                        item.sourceEdition.title + " · v" + item.contentVersion,
                        color = AynvoraTheme.colors.TextLightSecondary
                    )
                    item.text.originalSourceText?.let {
                        Text(
                            it,
                            color = AynvoraTheme.colors.TextLight
                        )
                    }
                    Text(
                        resolver.text(GarudaPuranTextKey.SOURCE_MEANING),
                        color = AynvoraTheme.colors.GoldLight
                    )
                    Text(item.text.sourceMeaning, color = AynvoraTheme.colors.TextLight)
                    Text(
                        item.text.localizedPresentation,
                        color = AynvoraTheme.colors.TextLightSecondary
                    )
                    item.interpretations.forEach { interpretation ->
                        Text(
                            resolver.text(GarudaPuranTextKey.TRADITIONAL_INTERPRETATION),
                            color = AynvoraTheme.colors.GoldLight
                        )
                        Text(interpretation.text, color = AynvoraTheme.colors.TextLightSecondary)
                    }
                    item.practices.forEach { practice ->
                        Text(
                            resolver.text(GarudaPuranTextKey.TRADITIONAL_PRACTICE),
                            color = AynvoraTheme.colors.GoldLight
                        )
                        Text(practice.text, color = AynvoraTheme.colors.TextLightSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GarudaLoading(resolver: GarudaPuranTextResolver) {
    Column(
        Modifier.fillMaxWidth().padding(16.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.sdp),
    ) {
        CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
        Text(resolver.text(GarudaPuranTextKey.LOADING), color = AynvoraTheme.colors.TextLight)
    }
}

@Composable
private fun GarudaMessage(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth().padding(12.sdp),
        color = AynvoraTheme.colors.TextLightSecondary,
        style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
    )
}
