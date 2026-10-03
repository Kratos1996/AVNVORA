package com.aynvora.ui.palmistry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.palmistry.HandImageReference
import com.aynvora.core.palmistry.HandType
import com.aynvora.core.palmistry.ImageQualityAssessment
import com.aynvora.core.palmistry.PalmAnswerFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedback
import com.aynvora.core.palmistry.PalmFeatureFeedbackCategory
import com.aynvora.core.palmistry.PalmCaptureError
import com.aynvora.core.palmistry.PalmFinding
import com.aynvora.core.palmistry.PalmImageAnalysisEngine
import com.aynvora.core.palmistry.PalmImageSource
import com.aynvora.core.palmistry.PalmImageSourceType
import com.aynvora.core.palmistry.PalmLineFinding
import com.aynvora.core.palmistry.PalmQuestion
import com.aynvora.core.palmistry.PalmQuestionEngine
import com.aynvora.core.palmistry.PalmReadingSession
import com.aynvora.core.palmistry.PalmReadingStatus
import com.aynvora.core.palmistry.PalmSessionRepository
import com.aynvora.core.palmistry.PalmTimelineEvent
import com.aynvora.core.palmistry.PalmTimelineEventType
import com.aynvora.core.palmistry.PalmistryContentPackage
import com.aynvora.core.palmistry.PalmistryExplanationEngine
import com.aynvora.core.palmistry.PalmistryExplanationRequest
import com.aynvora.core.palmistry.PalmistryMeaning
import com.aynvora.core.report.GenerateReportUseCase
import com.aynvora.core.report.PalmistryReportInput
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportGenerationRequest
import com.aynvora.core.report.ReportGenerationResult
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportText
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ReportType
import com.aynvora.core.result.AynvoraResult
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.localization.LocalAynvoraLocale
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.localization.report.AynvoraReportTextResolver
import com.aynvora.ui.report.ReportViewer
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.qa.android.qaAction
import com.aynvora.qa.core.models.QaActionId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Navigation Destinations
// ─────────────────────────────────────────────────────────────────────────────

private sealed interface PalmDestination {
    data object Home : PalmDestination
    data object Disclaimer : PalmDestination
    data object HandSelection : PalmDestination
    data object Capture : PalmDestination
    data class QualityCheck(val source: PalmImageSource) : PalmDestination
    data class Analyzing(val source: PalmImageSource) : PalmDestination
    data object Result : PalmDestination
    data class FeatureDetail(val line: PalmLineFinding, val meaning: PalmistryMeaning?) :
        PalmDestination

    data object Timeline : PalmDestination
    data object Report : PalmDestination
}

// ─────────────────────────────────────────────────────────────────────────────
// Root Palmistry Entry Point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PalmistryRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    analysisEngine: PalmImageAnalysisEngine = koinInject(),
    explanationEngine: PalmistryExplanationEngine = koinInject(),
    questionEngine: PalmQuestionEngine = koinInject(),
    analytics: AnalyticsTracker? = null,
    generateReport: GenerateReportUseCase = koinInject(),
    pdfGenerator: ReportPdfGenerator? = null,
    palmistryViewModel: PalmistryViewModel = koinInject(),
) {
    val locale = LocalAynvoraLocale.current
    val language = locale.localeId
    val coroutineScope = rememberCoroutineScope()
    val uiState by palmistryViewModel.uiState.collectAsState()

    var destination by remember { mutableStateOf<PalmDestination>(PalmDestination.Home) }
    var selectedHand by remember { mutableStateOf(HandType.RIGHT) }
    var currentSession by remember { mutableStateOf<PalmReadingSession?>(null) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val pastSessions = uiState.pastSessions

    LaunchedEffect(palmistryViewModel) {
        palmistryViewModel.effects.collect { effect ->
            when (effect) {
                is com.aynvora.core.event.AynvoraEffect.Navigate -> {
                    when (effect.target) {
                        com.aynvora.core.event.AynvoraNavigationTarget.Close -> onClose()
                        com.aynvora.core.event.AynvoraNavigationTarget.Back -> {
                            destination = PalmDestination.Home
                        }

                        is com.aynvora.core.event.AynvoraNavigationTarget.PalmistryReading -> {
                            destination = PalmDestination.Result
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(Unit) {
        palmistryViewModel.onEvent(PalmistryUiEvent.ScreenOpened)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
    ) {
        when (val dest = destination) {
            is PalmDestination.Home -> PalmHomeScreen(
                language = language,
                pastSessions = pastSessions,
                onStartNewReading = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.DisclaimerAccepted)
                    destination = PalmDestination.Disclaimer
                },
                onSelectPastSession = { session ->
                    palmistryViewModel.onEvent(PalmistryUiEvent.SelectPastSession(session.id))
                    currentSession = session
                    destination = PalmDestination.Result
                },
                onClose = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.CloseClicked)
                    onClose()
                },
            )

            is PalmDestination.Disclaimer -> PalmDisclaimerScreen(
                language = language,
                onAccept = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.DisclaimerAccepted)
                    destination = PalmDestination.HandSelection
                },
                onBack = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.BackClicked)
                    destination = PalmDestination.Home
                },
            )

            is PalmDestination.HandSelection -> PalmHandSelectionScreen(
                language = language,
                selectedHand = selectedHand,
                onHandSelected = { hand ->
                    selectedHand = hand
                    palmistryViewModel.onEvent(PalmistryUiEvent.HandSelected(hand))
                    destination = PalmDestination.Capture
                },
                onBack = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.BackClicked)
                    destination = PalmDestination.Home
                },
            )

            is PalmDestination.Capture -> PalmCaptureScreen(
                language = language,
                hand = selectedHand,
                onImageSelected = { source ->
                    palmistryViewModel.onEvent(PalmistryUiEvent.ImageSelected(source))
                    destination = PalmDestination.QualityCheck(source)
                },
                onBack = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.BackClicked)
                    destination = PalmDestination.HandSelection
                },
            )

            is PalmDestination.QualityCheck -> PalmQualityCheckScreen(
                language = language,
                source = dest.source,
                analysisEngine = analysisEngine,
                onProceed = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.StartAnalysis(selectedHand))
                    destination = PalmDestination.Analyzing(dest.source)
                },
                onRetake = { destination = PalmDestination.Capture },
                onBack = {
                    palmistryViewModel.onEvent(PalmistryUiEvent.BackClicked)
                    destination = PalmDestination.Capture
                },
            )

            is PalmDestination.Analyzing -> PalmAnalyzingScreen(
                language = language,
                hand = selectedHand,
                source = dest.source,
                analysisEngine = analysisEngine,
                explanationEngine = explanationEngine,
                onAnalysisCompleted = { session ->
                    palmistryViewModel.onEvent(PalmistryUiEvent.SaveSession(session))
                    palmistryViewModel.onEvent(PalmistryUiEvent.AnalysisCompleted(session.handType))
                    currentSession = session
                    destination = PalmDestination.Result
                },
                onAnalysisFailed = {
                    destination = PalmDestination.Capture
                },
            )

            is PalmDestination.Result -> currentSession?.let { session ->
                PalmResultScreen(
                    language = language,
                    session = session,
                    questionEngine = questionEngine,
                    palmistryViewModel = palmistryViewModel,
                    onOpenFeatureDetail = { line, meaning ->
                        destination = PalmDestination.FeatureDetail(line, meaning)
                    },
                    onOpenTimeline = { destination = PalmDestination.Timeline },
                    onOpenReport = { destination = PalmDestination.Report },
                    onFinishReading = { showFeedbackDialog = true },
                    onSessionUpdated = { updated -> currentSession = updated },
                    onBack = { destination = PalmDestination.Home },
                )
            } ?: run {
                destination = PalmDestination.Home
            }

            is PalmDestination.FeatureDetail -> PalmFeatureDetailScreen(
                language = language,
                line = dest.line,
                meaning = dest.meaning,
                onBack = { destination = PalmDestination.Result },
            )

            is PalmDestination.Timeline -> currentSession?.let { session ->
                PalmTimelineScreen(
                    language = language,
                    session = session,
                    onBack = { destination = PalmDestination.Result },
                )
            } ?: run {
                destination = PalmDestination.Home
            }

            is PalmDestination.Report -> currentSession?.let { session ->
                PalmReportScreen(
                    language = language,
                    session = session,
                    generateReport = generateReport,
                    pdfGenerator = pdfGenerator,
                    palmistryViewModel = palmistryViewModel,
                    onBack = { destination = PalmDestination.Result },
                )
            } ?: run {
                destination = PalmDestination.Home
            }
        }

        // Global Feedback Dialog
        if (showFeedbackDialog && currentSession != null) {
            PalmFeedbackDialog(
                language = language,
                onDismiss = { showFeedbackDialog = false },
                onSubmit = { stars, comment ->
                    val sess = currentSession ?: return@PalmFeedbackDialog
                    coroutineScope.launch {
                        val fb = com.aynvora.core.palmistry.PalmFeedback(
                            readingId = sess.id,
                            ratingStars = stars,
                            improvementComment = comment,
                            timestampEpochMs = System.currentTimeMillis(),
                        )
                        val updated = sess.copy(
                            feedback = fb,
                            status = PalmReadingStatus.SATISFIED,
                            timeline = sess.timeline + PalmTimelineEvent(
                                eventId = "evt_fb_${sess.id}",
                                readingId = sess.id,
                                timestampEpochMs = System.currentTimeMillis(),
                                eventType = PalmTimelineEventType.FEEDBACK_SUBMITTED,
                                summary = "Submitted $stars-star feedback",
                                language = language,
                            )
                        )
                        palmistryViewModel.onEvent(PalmistryUiEvent.SaveSession(updated))
                        palmistryViewModel.onEvent(
                            PalmistryUiEvent.FeedbackSubmitted(
                                stars,
                                comment
                            )
                        )
                        currentSession = updated
                        showFeedbackDialog = false
                    }
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Top-Right Language Switcher Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmistryHeaderBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val locale = LocalAynvoraLocale.current
    var showLanguageSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.sdp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Text(
                    text = "←",
                    style = AynvoraTheme.typography.title18.copy(fontSize = 20.ssp),
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(end = 12.sdp)
                        .semantics { contentDescription = "Back" }
                )
            }
            Text(
                text = title,
                style = AynvoraTheme.typography.headline28.copy(fontSize = 19.ssp),
                color = AynvoraTheme.colors.Gold,
            )
        }

        // Top-right Language Switcher
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AynvoraTheme.colors.CosmicIndigo)
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .clickable { showLanguageSheet = true }
                .padding(horizontal = 10.sdp, vertical = 6.sdp)
                .semantics {
                    contentDescription = "Language switcher: current is ${locale.nativeName}"
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = locale.nativeName,
                    style = AynvoraTheme.typography.caption12.copy(
                        fontSize = 12.ssp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.width(4.sdp))
                Text(
                    text = "▼",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 9.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
            }
        }
    }

    if (showLanguageSheet) {
        PalmLanguagePickerSheet(
            currentLocale = locale,
            onDismiss = { showLanguageSheet = false },
        )
    }
}

@Composable
private fun PalmLanguagePickerSheet(
    currentLocale: SupportedLocale,
    onDismiss: () -> Unit,
) {
    val locales = remember { LanguageRegistry.availableLocales() }
    val localeManager: AynvoraLocaleManager = koinInject()
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AynvoraTheme.colors.CosmicNavy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp)
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(16.sdp)) {
                Text(
                    text = "Select Language / भाषा चुनें",
                    style = AynvoraTheme.typography.title18,
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(12.sdp))
                locales.forEach { loc ->
                    val isSelected = loc.localeId == currentLocale.localeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AynvoraTheme.colors.Gold.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                scope.launch {
                                    localeManager.setLocale(loc)
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 12.sdp, vertical = 10.sdp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${loc.englishName} (${loc.nativeName})",
                            style = AynvoraTheme.typography.body14.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLight,
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                color = AynvoraTheme.colors.Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 1: Home Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmHomeScreen(
    language: String,
    pastSessions: List<PalmReadingSession>,
    onStartNewReading: () -> Unit,
    onSelectPastSession: (PalmReadingSession) -> Unit,
    onClose: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Features.Palmistry),
            onBack = onClose,
        )

        Spacer(Modifier.height(8.sdp))

        // Hero Card
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Text(
                    text = translator.translate(TranslationKey.Palmistry.TraditionBadge),
                    style = AynvoraTheme.typography.caption12,
                    color = AynvoraTheme.colors.CelestialBlue,
                )
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.HeroTitle),
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 20.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(8.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.HeroSubtext),
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                Spacer(Modifier.height(16.sdp))
                AynvoraButton(
                    text = translator.translate(TranslationKey.Palmistry.StartButton),
                    onClick = onStartNewReading,
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(24.sdp))

        // Past Readings Section
        if (pastSessions.isNotEmpty()) {
            Text(
                text = translator.translate(TranslationKey.Palmistry.RecentReadings),
                style = AynvoraTheme.typography.title18,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(Modifier.height(10.sdp))
            pastSessions.take(5).forEach { sess ->
                val dateStr = formatTimestamp(sess.startedAtEpochMs)
                AynvoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.sdp)
                        .clickable { onSelectPastSession(sess) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.sdp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${sess.handType} Hand • ${sess.finding?.shape?.name ?: "Standard"}",
                                style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Bold),
                                color = AynvoraTheme.colors.Gold,
                            )
                            Spacer(Modifier.height(2.sdp))
                            Text(
                                text = dateStr,
                                style = AynvoraTheme.typography.caption12,
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                        }
                        Text(
                            text = "→",
                            style = AynvoraTheme.typography.title18,
                            color = AynvoraTheme.colors.Gold,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 2: Ethical Disclaimer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmDisclaimerScreen(
    language: String,
    onAccept: () -> Unit,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.DisclosureTitle),
            onBack = onBack,
        )

        Spacer(Modifier.height(12.sdp))

        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Text(
                    text = translator.translate(TranslationKey.Palmistry.DisclosureBadge),
                    style = AynvoraTheme.typography.title18,
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(12.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.DisclosureBody),
                    style = AynvoraTheme.typography.body14.copy(lineHeight = 22.ssp),
                    color = AynvoraTheme.colors.TextLight,
                )
                Spacer(Modifier.height(24.sdp))
                AynvoraButton(
                    text = translator.translate(TranslationKey.Palmistry.DisclosureAccept),
                    onClick = onAccept,
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 3: Hand Selection
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmHandSelectionScreen(
    language: String,
    selectedHand: HandType,
    onHandSelected: (HandType) -> Unit,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.HandSelectTitle),
            onBack = onBack,
        )

        Text(
            text = translator.translate(TranslationKey.Palmistry.HandSelectSubtitle),
            style = AynvoraTheme.typography.body14,
            color = AynvoraTheme.colors.TextLightSecondary,
        )

        Spacer(Modifier.height(20.sdp))

        // Right Hand Option
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selectedHand == HandType.RIGHT) 2.dp else 1.dp,
                    color = if (selectedHand == HandType.RIGHT) AynvoraTheme.colors.Gold else AynvoraTheme.colors.Gold.copy(
                        alpha = 0.2f
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable { onHandSelected(HandType.RIGHT) },
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = translator.translate(TranslationKey.Palmistry.RightHandTitle),
                        style = AynvoraTheme.typography.title18,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(text = "✋", fontSize = 24.ssp)
                }
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.RightHandDesc),
                    style = AynvoraTheme.typography.body14,
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }

        Spacer(Modifier.height(16.sdp))

        // Left Hand Option
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (selectedHand == HandType.LEFT) 2.dp else 1.dp,
                    color = if (selectedHand == HandType.LEFT) AynvoraTheme.colors.Gold else AynvoraTheme.colors.Gold.copy(
                        alpha = 0.2f
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable { onHandSelected(HandType.LEFT) },
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = translator.translate(TranslationKey.Palmistry.LeftHandTitle),
                        style = AynvoraTheme.typography.title18,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(text = "🤚", fontSize = 24.ssp)
                }
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.LeftHandDesc),
                    style = AynvoraTheme.typography.body14,
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 4: Image Capture / Selection
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmCaptureScreen(
    language: String,
    hand: HandType,
    onImageSelected: (PalmImageSource) -> Unit,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.CaptureTitle),
            onBack = onBack,
        )

        // Capture Guidance Box
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    AynvoraTheme.colors.CelestialBlue.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(16.sdp)) {
                Text(
                    text = translator.translate(TranslationKey.Palmistry.CaptureBadge),
                    style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                    color = AynvoraTheme.colors.CelestialBlue,
                )
                Spacer(Modifier.height(8.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.CaptureGuidanceBody),
                    style = AynvoraTheme.typography.body14.copy(lineHeight = 20.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }

        var captureError by remember { mutableStateOf<PalmCaptureError?>(null) }
        val imagePicker = rememberPalmImagePicker(
            onImageCaptured = { bytes, isCamera ->
                captureError = null
                // Decode actual pixel dimensions from the normalized image bytes so that
                // PalmImageSource carries truthful resolution metadata (not a hardcoded guess).
                val (imgW, imgH) = decodePalmImageDimensions(bytes)
                val source = PalmImageSource(
                    data = bytes,
                    widthPx = imgW,
                    heightPx = imgH,
                    sourceType = if (isCamera) PalmImageSourceType.CAMERA else PalmImageSourceType.GALLERY,
                    capturedAtEpochMs = System.currentTimeMillis(),
                )
                onImageSelected(source)
            },
            onError = { err ->
                captureError = err
            },
        )

        if (captureError != null) {
            Spacer(Modifier.height(10.sdp))
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AynvoraTheme.colors.Error.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            ) {
                Column(modifier = Modifier.padding(12.sdp)) {
                    Text(
                        text = captureError?.message ?: "",
                        color = AynvoraTheme.colors.Error,
                        style = AynvoraTheme.typography.caption12,
                    )
                    Spacer(Modifier.height(6.sdp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "Dismiss",
                            color = AynvoraTheme.colors.Gold,
                            style = AynvoraTheme.typography.caption12,
                            modifier = Modifier
                                .clickable { captureError = null }
                                .padding(4.sdp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.sdp))

        // Option 1: Real Camera Capture
        AynvoraButton(
            text = translator.translate(TranslationKey.Palmistry.TakePhoto),
            onClick = {
                imagePicker.launchCamera()
            },
            variant = AynvoraButtonVariant.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .qaAction(QaActionId.PALMISTRY_OPEN_CAMERA),
            event = AynvoraClickEvent(
                eventId = "palmistry.camera.open_clicked",
                screenId = "palmistry_input_selection",
                componentId = "camera_button",
            ),
        )

        Spacer(Modifier.height(14.sdp))

        // Option 2: System Photo Picker / Gallery
        AynvoraButton(
            text = translator.translate(TranslationKey.Palmistry.PickGallery),
            onClick = {
                imagePicker.launchGallery()
            },
            variant = AynvoraButtonVariant.Secondary,
            modifier = Modifier
                .fillMaxWidth()
                .qaAction(QaActionId.PALMISTRY_OPEN_GALLERY),
            event = AynvoraClickEvent(
                eventId = "palmistry.gallery.open_clicked",
                screenId = "palmistry_input_selection",
                componentId = "gallery_button",
            ),
        )

        Spacer(Modifier.height(14.sdp))

        // Option 3: Standard Sample Palm (Offline deterministic reference)
        AynvoraButton(
            text = translator.translate(TranslationKey.Palmistry.SampleHand),
            onClick = {
                val sampleBytes = generateSyntheticPalmImageBytes()
                val source = PalmImageSource(
                    data = sampleBytes,
                    widthPx = 480,
                    heightPx = 640,
                    sourceType = PalmImageSourceType.SAMPLE,
                    capturedAtEpochMs = System.currentTimeMillis(),
                )
                onImageSelected(source)
            },
            variant = AynvoraButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 5: Quality Check
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmQualityCheckScreen(
    language: String,
    source: PalmImageSource,
    analysisEngine: PalmImageAnalysisEngine,
    onProceed: () -> Unit,
    onRetake: () -> Unit,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var assessment by remember { mutableStateOf<ImageQualityAssessment?>(null) }

    LaunchedEffect(source) {
        assessment = analysisEngine.validateImageQuality(source)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.QualityTitle),
            onBack = onBack,
        )

        val assess = assessment
        if (assess == null) {
            Box(Modifier.fillMaxWidth().height(200.sdp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
            }
        } else {
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (assess.isAcceptable) AynvoraTheme.colors.Success else AynvoraTheme.colors.Error,
                        shape = RoundedCornerShape(16.dp),
                    ),
            ) {
                Column(modifier = Modifier.padding(18.sdp)) {
                    Text(
                        text = if (assess.isAcceptable) translator.translate(TranslationKey.Palmistry.QualityVerified)
                        else translator.translate(TranslationKey.Palmistry.QualityIssue),
                        style = AynvoraTheme.typography.title18,
                        color = if (assess.isAcceptable) AynvoraTheme.colors.Success else AynvoraTheme.colors.Error,
                    )
                    Spacer(Modifier.height(8.sdp))
                    Text(
                        text = translator.translateWithArgs(
                            TranslationKey.Palmistry.QualityStatusFormat,
                            "state" to assess.state.name,
                            "score" to (assess.score * 100).toInt().toString(),
                        ),
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.Gold,
                    )
                    if (assess.issues.isNotEmpty()) {
                        Spacer(Modifier.height(8.sdp))
                        assess.issues.forEach { issue ->
                            Text(
                                text = "• $issue",
                                style = AynvoraTheme.typography.body14,
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.sdp))
                    if (assess.isAcceptable) {
                        AynvoraButton(
                            text = translator.translate(TranslationKey.Palmistry.QualityProceed),
                            onClick = onProceed,
                            variant = AynvoraButtonVariant.Primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .qaAction(QaActionId.PALMISTRY_PROCEED),
                        )
                    } else {
                        AynvoraButton(
                            text = translator.translate(TranslationKey.Palmistry.QualityRetake),
                            onClick = onRetake,
                            variant = AynvoraButtonVariant.Secondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .qaAction(QaActionId.PALMISTRY_RETAKE),
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 6: Analysis Animation Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmAnalyzingScreen(
    language: String,
    hand: HandType,
    source: PalmImageSource,
    analysisEngine: PalmImageAnalysisEngine,
    explanationEngine: PalmistryExplanationEngine,
    onAnalysisCompleted: (PalmReadingSession) -> Unit,
    onAnalysisFailed: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var stepText by remember { mutableStateOf<String>(translator.translate(TranslationKey.Palmistry.StepSegmenting)) }

    LaunchedEffect(Unit) {
        delay(400)
        stepText = translator.translate(TranslationKey.Palmistry.StepLines)
        delay(600)
        stepText = translator.translate(TranslationKey.Palmistry.StepSamudrika)

        val analysisResult = analysisEngine.analyzePalm(source, hand)
        when (analysisResult) {
            is AynvoraResult.Success -> {
                val finding = analysisResult.value
                val meanings = PalmistryContentPackage.getMeaningsForFindings(finding, language)
                val sessionId = "palm_${System.currentTimeMillis()}"

                // Explain findings
                val explanationReq = PalmistryExplanationRequest(
                    readingId = sessionId,
                    hand = hand,
                    language = language,
                    evidence = finding.lines.filter { it.detected }.map { line ->
                        com.aynvora.core.palmistry.PalmistryEvidence(
                            evidenceId = "ev_${line.lineType.name.lowercase()}",
                            readingId = sessionId,
                            hand = hand,
                            featureType = line.lineType.name,
                            observation = "${line.lineType.name}: Strength ${line.strength}, Clarity ${(line.clarityScore * 100).toInt()}%",
                            confidence = line.clarityScore,
                        )
                    },
                    approvedMeanings = meanings,
                )

                val explanation = explanationEngine.explain(explanationReq).let {
                    if (it is AynvoraResult.Success) it.value else null
                }

                val session = PalmReadingSession(
                    id = sessionId,
                    startedAtEpochMs = System.currentTimeMillis(),
                    handType = hand,
                    imageReference = HandImageReference(
                        imagePath = source.filePath ?: "local://palm_capture_$sessionId",
                        captureTimestampEpochMs = source.capturedAtEpochMs,
                        handType = hand,
                        widthPx = source.widthPx,
                        heightPx = source.heightPx,
                    ),
                    finding = finding,
                    meanings = meanings,
                    timeline = listOf(
                        PalmTimelineEvent(
                            eventId = "evt_start_$sessionId",
                            readingId = sessionId,
                            timestampEpochMs = System.currentTimeMillis(),
                            eventType = PalmTimelineEventType.READING_STARTED,
                            summary = "Palm reading started for $hand hand",
                            language = language,
                        ),
                        PalmTimelineEvent(
                            eventId = "evt_analysis_$sessionId",
                            readingId = sessionId,
                            timestampEpochMs = System.currentTimeMillis(),
                            eventType = PalmTimelineEventType.ANALYSIS_COMPLETED,
                            summary = "Detected ${finding.lines.count { it.detected }} lines (${finding.shape} palm)",
                            language = language,
                        ),
                    ),
                    status = PalmReadingStatus.ACTIVE,
                    language = language,
                )

                onAnalysisCompleted(session)
            }

            is AynvoraResult.Failure -> {
                onAnalysisFailed()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.sdp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AynvoraTheme.colors.Gold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(54.dp),
            )
            Spacer(Modifier.height(24.sdp))
            Text(
                text = translator.translate(TranslationKey.Palmistry.AnalysisProgress),
                style = AynvoraTheme.typography.title18,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = stepText,
                style = AynvoraTheme.typography.body14,
                color = AynvoraTheme.colors.TextLightSecondary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 7: Result Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmResultScreen(
    language: String,
    session: PalmReadingSession,
    questionEngine: PalmQuestionEngine,
    palmistryViewModel: PalmistryViewModel,
    onOpenFeatureDetail: (PalmLineFinding, PalmistryMeaning?) -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenReport: () -> Unit,
    onFinishReading: () -> Unit,
    onSessionUpdated: (PalmReadingSession) -> Unit,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    val coroutineScope = rememberCoroutineScope()
    var questionInput by remember { mutableStateOf("") }
    var isAnswering by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val finding = session.finding
    val meanings = session.meanings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.InsightsTitle),
            onBack = onBack,
        )

        // Summary Card
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(16.sdp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val handLabel = if (session.handType == HandType.LEFT) {
                        translator.translate(TranslationKey.Palmistry.HandLeftLabel)
                    } else {
                        translator.translate(TranslationKey.Palmistry.HandRightLabel)
                    }
                    Text(
                        text = "✋ $handLabel",
                        style = AynvoraTheme.typography.title18,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AynvoraTheme.colors.Gold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.sdp, vertical = 4.sdp)
                    ) {
                        Text(
                            text = finding?.shape?.name ?: "SQUARE",
                            style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
                            color = AynvoraTheme.colors.Gold,
                        )
                    }
                }
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = "Detected Lines: ${finding?.lines?.count { it.detected } ?: 0} • Overall Clarity: ${((finding?.overallClarity ?: 0.8f) * 100).toInt()}%",
                    style = AynvoraTheme.typography.caption12,
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }

        Spacer(Modifier.height(18.sdp))

        // Detected Features List
        Text(
            text = translator.translate(TranslationKey.Palmistry.ObservedFeatures),
            style = AynvoraTheme.typography.title18,
            color = AynvoraTheme.colors.Gold,
        )
        Spacer(Modifier.height(10.sdp))

        finding?.lines?.filter { it.detected }?.forEach { line ->
            val meaning = meanings.firstOrNull { it.featureType == line.lineType.name }
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.sdp)
                    .clickable { onOpenFeatureDetail(line, meaning) },
            ) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = meaning?.title ?: line.lineType.name,
                            style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Bold),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Text(
                            text = "${line.strength} (${(line.clarityScore * 100).toInt()}%)",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                    }
                    if (meaning != null) {
                        Spacer(Modifier.height(4.sdp))
                        Text(
                            text = meaning.traditionalInterpretation,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                            maxLines = 2,
                        )
                    }
                    Spacer(Modifier.height(8.sdp))
                    // Compact Feature Feedback Strip
                    PalmFeatureFeedbackStrip(
                        language = language,
                        featureType = line.lineType.name,
                        onFeedback = { category ->
                            palmistryViewModel.onEvent(
                                PalmistryUiEvent.RecordFeatureFeedback(
                                    PalmFeatureFeedback(
                                        readingId = session.id,
                                        featureType = line.lineType.name,
                                        category = category,
                                        timestampEpochMs = System.currentTimeMillis(),
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(18.sdp))

        // Conversational Q&A Section
        Text(
            text = translator.translate(TranslationKey.Palmistry.AskPrompt),
            style = AynvoraTheme.typography.title18,
            color = AynvoraTheme.colors.Gold,
        )
        Spacer(Modifier.height(8.sdp))

        // Existing Questions
        session.questions.forEach { q ->
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.sdp),
            ) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = "Q: ${q.questionText}",
                        style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Bold),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(Modifier.height(6.sdp))
                    Text(
                        text = q.answerInterpretation ?: q.answerSummary ?: "",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.TextLight,
                    )
                    Spacer(Modifier.height(6.sdp))
                    // Helpful rating
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = translator.translate(TranslationKey.Palmistry.HelpfulLabel),
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                        Spacer(Modifier.width(8.sdp))
                        Text(
                            text = "👍",
                            modifier = Modifier.clickable {
                                palmistryViewModel.onEvent(
                                    PalmistryUiEvent.RecordAnswerFeedback(
                                        PalmAnswerFeedback(
                                            session.id,
                                            q.questionId,
                                            true,
                                            System.currentTimeMillis()
                                        )
                                    )
                                )
                            }
                        )
                        Spacer(Modifier.width(10.sdp))
                        Text(
                            text = "👎",
                            modifier = Modifier.clickable {
                                palmistryViewModel.onEvent(
                                    PalmistryUiEvent.RecordAnswerFeedback(
                                        PalmAnswerFeedback(
                                            session.id,
                                            q.questionId,
                                            false,
                                            System.currentTimeMillis()
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }

        // Question Input
        OutlinedTextField(
            value = questionInput,
            onValueChange = { questionInput = it },
            placeholder = {
                Text(
                    text = translator.translate(TranslationKey.Palmistry.QuestionPlaceholder),
                    style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AynvoraTheme.colors.TextLight,
                unfocusedTextColor = AynvoraTheme.colors.TextLight,
                focusedBorderColor = AynvoraTheme.colors.Gold,
                unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (questionInput.isNotBlank() && !isAnswering) {
                    keyboardController?.hide()
                    val text = questionInput
                    questionInput = ""
                    isAnswering = true
                    coroutineScope.launch {
                        when (val ans = questionEngine.answerQuestion(session, text, language)) {
                            is AynvoraResult.Success -> {
                                val (newQ, newEvt) = ans.value
                                val updated = session.copy(
                                    questions = session.questions + newQ,
                                    timeline = session.timeline + newEvt,
                                )
                                palmistryViewModel.onEvent(PalmistryUiEvent.SaveSession(updated))
                                palmistryViewModel.onEvent(PalmistryUiEvent.QuestionSubmitted(text))
                                onSessionUpdated(updated)
                            }

                            is AynvoraResult.Failure -> Unit
                        }
                        isAnswering = false
                    }
                }
            }),
        )

        Spacer(Modifier.height(10.sdp))

        AynvoraButton(
            text = if (isAnswering) translator.translate(TranslationKey.Palmistry.GeneratingReflection)
            else translator.translate(TranslationKey.Palmistry.AskReflectionButton),
            onClick = {
                if (questionInput.isNotBlank() && !isAnswering) {
                    keyboardController?.hide()
                    val text = questionInput
                    questionInput = ""
                    isAnswering = true
                    coroutineScope.launch {
                        when (val ans = questionEngine.answerQuestion(session, text, language)) {
                            is AynvoraResult.Success -> {
                                val (newQ, newEvt) = ans.value
                                val updated = session.copy(
                                    questions = session.questions + newQ,
                                    timeline = session.timeline + newEvt,
                                )
                                palmistryViewModel.onEvent(PalmistryUiEvent.SaveSession(updated))
                                palmistryViewModel.onEvent(PalmistryUiEvent.QuestionSubmitted(text))
                                onSessionUpdated(updated)
                            }

                            is AynvoraResult.Failure -> Unit
                        }
                        isAnswering = false
                    }
                }
            },
            variant = AynvoraButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.sdp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            AynvoraButton(
                text = translator.translate(TranslationKey.Palmistry.TimelineButton),
                onClick = onOpenTimeline,
                variant = AynvoraButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            AynvoraButton(
                text = translator.translate(TranslationKey.Palmistry.ReportButton),
                onClick = onOpenReport,
                variant = AynvoraButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.sdp))

        AynvoraButton(
            text = translator.translate(TranslationKey.Palmistry.SatisfiedButton),
            onClick = onFinishReading,
            variant = AynvoraButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 8: Feature Detail Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmFeatureDetailScreen(
    language: String,
    line: PalmLineFinding,
    meaning: PalmistryMeaning?,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = meaning?.title ?: line.lineType.name,
            onBack = onBack,
        )

        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Text(
                    text = "🔬 " + translator.translate(TranslationKey.Palmistry.ObservedFeatures),
                    style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(10.sdp))
                Text(
                    text = "• Clarity Score: ${(line.clarityScore * 100).toInt()}%\n" +
                            "• Strength: ${line.strength}\n" +
                            "• Length: ${line.lengthCategory}\n" +
                            "• Continuity: ${(line.continuity * 100).toInt()}%\n" +
                            "• Curvature Index: ${(line.curvatureScore * 100).toInt()}%",
                    style = AynvoraTheme.typography.body14.copy(lineHeight = 22.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                Spacer(Modifier.height(16.sdp))
                Text(
                    text = "📜 " + translator.translate(TranslationKey.Palmistry.TraditionalInterpretation),
                    style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = meaning?.traditionalInterpretation ?: "Content unavailable",
                    style = AynvoraTheme.typography.body14.copy(lineHeight = 22.ssp),
                    color = AynvoraTheme.colors.TextLight,
                )
                if (meaning?.caution?.isNotBlank() == true) {
                    Spacer(Modifier.height(12.sdp))
                    Text(
                        text = "↳ " + translator.translate(TranslationKey.Report.PalmCautionPrefix) + meaning.caution,
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.CelestialBlue,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 9: Timeline Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmTimelineScreen(
    language: String,
    session: PalmReadingSession,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        PalmistryHeaderBar(
            title = translator.translate(TranslationKey.Palmistry.TimelineTitle),
            onBack = onBack,
        )

        Spacer(Modifier.height(10.sdp))

        session.timeline.sortedBy { it.timestampEpochMs }.forEachIndexed { idx, event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.sdp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AynvoraTheme.colors.Gold)
                    )
                    if (idx < session.timeline.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(44.sdp)
                                .background(AynvoraTheme.colors.Gold.copy(alpha = 0.3f))
                        )
                    }
                }
                Spacer(Modifier.width(12.sdp))
                Column {
                    Text(
                        text = formatTimestamp(event.timestampEpochMs),
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.CelestialBlue,
                    )
                    Spacer(Modifier.height(2.sdp))
                    Text(
                        text = event.eventType.name.replace("_", " "),
                        style = AynvoraTheme.typography.body14.copy(fontWeight = FontWeight.Bold),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = event.summary,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen 10: Report Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmReportScreen(
    language: String,
    session: PalmReadingSession,
    generateReport: GenerateReportUseCase,
    pdfGenerator: ReportPdfGenerator?,
    palmistryViewModel: PalmistryViewModel,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var reportState by remember { mutableStateOf<ReportGenerationResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentLocale = LocalAynvoraLocale.current
    val reportLanguage = if (currentLocale.localeId.lowercase().startsWith("hi")) {
        ReportLanguage.HINDI
    } else {
        ReportLanguage.ENGLISH
    }

    val resolver = remember(reportLanguage) {
        AynvoraReportTextResolver(reportLanguage)
    }

    LaunchedEffect(session.id, language) {
        isLoading = true
        reportState = null

        val input = PalmistryReportInput(
            language = reportLanguage,
            generatedAtEpochMs = System.currentTimeMillis(),
            session = session,
        )
        val request = ReportGenerationRequest(
            reportType = ReportType.PALMISTRY,
            language = reportLanguage,
            generatedAtEpochMs = System.currentTimeMillis(),
            generatorInput = input,
        )
        reportState = generateReport.execute(request, resolver)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp, vertical = 16.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = translator.translate(TranslationKey.Palmistry.ReportTitle),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                PalmistryHeaderBar(title = "", onBack = null)
                AynvoraButton(
                    text = translator.translate(TranslationKey.App.Back),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onBack,
                )
            }
        }

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AynvoraTheme.colors.Gold, strokeWidth = 2.dp)
            }

            reportState is ReportGenerationResult.Generated -> {
                val document = (reportState as ReportGenerationResult.Generated).document
                ReportViewer(
                    document = document,
                    resolver = resolver,
                    onGeneratePdf = {
                        pdfGenerator?.generate(document)
                        palmistryViewModel.onEvent(PalmistryUiEvent.PdfRequested)
                    },
                    onSharePdf = null,
                    pdfStatus = null,
                )
            }

            reportState is ReportGenerationResult.Unavailable -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (reportState as ReportGenerationResult.Unavailable).reason.value,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.sdp),
                    )
                }
            }

            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Component: Feature Feedback Strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmFeatureFeedbackStrip(
    language: String,
    featureType: String,
    onFeedback: (PalmFeatureFeedbackCategory) -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var selectedCategory by remember { mutableStateOf<PalmFeatureFeedbackCategory?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf<Pair<PalmFeatureFeedbackCategory, String>>(
            PalmFeatureFeedbackCategory.CLEAR to translator.translate(TranslationKey.Palmistry.CardFeedbackClear),
            PalmFeatureFeedbackCategory.CONFUSING to translator.translate(TranslationKey.Palmistry.CardFeedbackConfusing),
            PalmFeatureFeedbackCategory.NEED_MORE_CONTEXT to translator.translate(TranslationKey.Palmistry.CardFeedbackNeedContext),
        ).forEach { (category, label) ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) AynvoraTheme.colors.Gold.copy(alpha = 0.25f) else AynvoraTheme.colors.CosmicIndigo)
                    .border(
                        1.dp,
                        if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.Gold.copy(
                            alpha = 0.2f
                        ),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        selectedCategory = category
                        onFeedback(category)
                    }
                    .padding(horizontal = 8.sdp, vertical = 4.sdp),
            ) {
                Text(
                    text = label,
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                    color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable Component: Feedback Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PalmFeedbackDialog(
    language: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var selectedStars by remember { mutableStateOf(5) }
    var commentText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AynvoraTheme.colors.CosmicNavy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp)
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                ),
        ) {
            Column(modifier = Modifier.padding(18.sdp)) {
                Text(
                    text = translator.translate(TranslationKey.Palmistry.FeedbackTitle),
                    style = AynvoraTheme.typography.title18,
                    color = AynvoraTheme.colors.Gold,
                )
                Spacer(Modifier.height(8.sdp))
                Text(
                    text = translator.translate(TranslationKey.Palmistry.WasHelpful),
                    style = AynvoraTheme.typography.body14,
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                Spacer(Modifier.height(14.sdp))

                // Star Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    (1..5).forEach { star ->
                        val isFilled = star <= selectedStars
                        Text(
                            text = if (isFilled) "★" else "☆",
                            fontSize = 32.ssp,
                            color = if (isFilled) AynvoraTheme.colors.Gold else AynvoraTheme.colors.GoldLight.copy(
                                alpha = 0.3f
                            ),
                            modifier = Modifier
                                .clickable { selectedStars = star }
                                .padding(horizontal = 4.sdp)
                                .semantics { contentDescription = "$star star" }
                        )
                    }
                }

                Spacer(Modifier.height(14.sdp))

                // Optional comment
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            text = translator.translate(TranslationKey.Palmistry.ImprovementPrompt),
                            style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AynvoraTheme.colors.TextLight,
                        unfocusedTextColor = AynvoraTheme.colors.TextLight,
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                    ),
                )

                Spacer(Modifier.height(18.sdp))

                AynvoraButton(
                    text = translator.translate(TranslationKey.Palmistry.SubmitFeedback),
                    onClick = {
                        onSubmit(selectedStars, commentText.takeIf { it.isNotBlank() })
                    },
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Synthetic Palm Image Generator (100% Offline / No Device Required)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Generates deterministic synthetic palm image byte arrays for testing & verification.
 * Adheres to realistic luminance (140-170) and gradient variances so quality checks pass naturally.
 */
fun generateSyntheticPalmImageBytes(variation: Int = 0): ByteArray {
    val size = 480 * 640 / 4 // 76800 bytes
    val bytes = ByteArray(size)
    for (i in 0 until size) {
        val x = i % 120
        val y = i / 120
        // Central palm skin luminance baseline
        var baseLuminance =
            150 + (sin(x.toDouble() / 15.0) * 15).toInt() + (sin(y.toDouble() / 20.0) * 12).toInt()
        // Simulate anatomical line gradients
        if (abs(y - (x * 0.8 + 20)) < 2.0) baseLuminance -= 45 // Life line arc
        if (abs(y - 65) < 2.0 && x in 25..95) baseLuminance -= 40 // Head line
        if (abs(y - 40 - (sin(x.toDouble() / 20.0) * 5)) < 2.0 && x in 20..100) baseLuminance -= 42 // Heart line
        if (abs(x - 60) < 2.0 && y in 35..110) baseLuminance -= 35 // Fate line
        bytes[i] = baseLuminance.coerceIn(45, 220).toByte()
    }
    return bytes
}

private fun formatTimestamp(epochMs: Long): String {
    return "Session #$epochMs"
}
