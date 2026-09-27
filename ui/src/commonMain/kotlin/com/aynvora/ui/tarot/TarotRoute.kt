package com.aynvora.ui.tarot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.report.GenerateReportUseCase
import com.aynvora.core.report.PrepareTarotReportUseCase
import com.aynvora.core.report.ReportGenerationRequest
import com.aynvora.core.report.ReportGenerationResult
import com.aynvora.core.report.ReportLanguage
import com.aynvora.core.report.ReportPdfGenerator
import com.aynvora.core.report.ReportShareService
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.core.report.ReportType
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.PerformTarotReadingUseCase
import com.aynvora.core.tarot.TarotAssetRepository
import com.aynvora.core.tarot.TarotCardContent
import com.aynvora.core.tarot.TarotCardDraw
import com.aynvora.core.tarot.TarotCardOrientation
import com.aynvora.core.tarot.TarotDisclaimer
import com.aynvora.core.tarot.TarotReading
import com.aynvora.core.tarot.TarotRepository
import com.aynvora.core.tarot.TarotSpread
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.aynvora.core.tarot.AiImprovementSignal
import com.aynvora.core.tarot.TarotAnswerFeedback
import com.aynvora.core.tarot.TarotAnswerStatus
import com.aynvora.core.tarot.TarotCardFeedback
import com.aynvora.core.tarot.TarotCardFeedbackCategory
import com.aynvora.core.tarot.TarotClarificationCard
import com.aynvora.core.tarot.TarotFeedback
import com.aynvora.core.tarot.TarotQuestion
import com.aynvora.core.tarot.TarotQuestionAnswer
import com.aynvora.core.tarot.TarotQuestionEngine
import com.aynvora.core.tarot.TarotReadingAvailability
import com.aynvora.core.tarot.TarotReadingAvailabilityPolicy
import com.aynvora.core.tarot.TarotReadingSession
import com.aynvora.core.tarot.TarotReadingStatus
import com.aynvora.core.tarot.TarotSessionRepository
import com.aynvora.core.tarot.TarotStandardDeck
import com.aynvora.core.tarot.TarotTimelineEvent
import com.aynvora.core.tarot.TarotTimelineEventType
import com.aynvora.designsystem.localization.LocalAynvoraLocale
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.locale.AynvoraLocaleManager
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.translation.TranslationKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Navigation sealed class for the Tarot sub-flow
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Internal navigation destinations within the Tarot production experience.
 */
sealed class TarotDestination {
    /** Non-predictive disclosure. Must be acknowledged before any reading. */
    data object Disclaimer : TarotDestination()

    /** Spread selection + deck selection + draw trigger. */
    data object Home : TarotDestination()

    /** Animated card reveal for a completed reading. */
    data class Reveal(val reading: TarotReading, val session: TarotReadingSession? = null) :
        TarotDestination()

    /** Full reading result with card meanings, keywords, follow-up questions, AI explanation. */
    data class ReadingResult(val reading: TarotReading, val session: TarotReadingSession? = null) :
        TarotDestination()

    /** Scrollable paginated history of past readings. */
    data object History : TarotDestination()

    /** Detail view for a single past reading. */
    data class HistoryDetail(val reading: TarotReading, val session: TarotReadingSession? = null) :
        TarotDestination()

    /** Card browser — browse all 78 cards and their authentic artwork. */
    data class CardBrowser(
        val language: String,
        val deckId: String = "rider_waite_smith_standard"
    ) : TarotDestination()

    /** Card detail for a specific card from the browser. */
    data class CardDetail(
        val cardId: String,
        val language: String,
        val deckId: String = "rider_waite_smith_standard"
    ) : TarotDestination()

    /** Tarot report viewer. */
    data class TarotReport(val reading: TarotReading, val session: TarotReadingSession? = null) :
        TarotDestination()

    /** Deck attribution and license information screen. */
    data object DeckLicenses : TarotDestination()

    /** Chronological reading timeline. */
    data class Timeline(val sessionId: String) : TarotDestination()
}


// ─────────────────────────────────────────────────────────────────────────────
// Root Tarot Route (Entry Point — replaces TarotScreen)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Production Tarot experience entry composable.
 *
 * Implements the complete Tarot flow:
 * Disclaimer → Home → Spread/Deck Selection → Draw → Reveal → Result → History
 *
 * Governance:
 * - Disclaimer must be acknowledged on every app session start.
 * - Real card artwork displayed throughout (Card Browser, Reveal, Detail, Reading).
 * - AI is strictly an explanation layer — never selects cards.
 * - Strictly non-predictive framing at every screen boundary.
 * - Report generated from approved local content only.
 */
@Composable
fun TarotRoute(
    tarotRepository: TarotRepository = org.koin.compose.koinInject(),
    assetRepository: TarotAssetRepository = org.koin.compose.koinInject(),
    analyticsTracker: AnalyticsTracker = org.koin.compose.koinInject(),
    prepareTarotReport: PrepareTarotReportUseCase = org.koin.compose.koinInject(),
    generateReport: GenerateReportUseCase = org.koin.compose.koinInject(),
    availabilityPolicy: TarotReadingAvailabilityPolicy = org.koin.compose.koinInject(),
    sessionRepository: TarotSessionRepository = org.koin.compose.koinInject(),
    questionEngine: TarotQuestionEngine = org.koin.compose.koinInject(),
    pdfGenerator: ReportPdfGenerator? = null,
    shareService: ReportShareService? = null,
    resolver: ReportTextResolver? = null,
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
    modifier: Modifier = Modifier,
    language: String = "en",
    onClose: () -> Unit = {},
) {
    var destination by remember { mutableStateOf<TarotDestination>(TarotDestination.Disclaimer) }
    var selectedDeckId by remember { mutableStateOf("rider_waite_smith_standard") }
    var activeSession by remember { mutableStateOf<TarotReadingSession?>(null) }
    var lockDialogLocked by remember { mutableStateOf<TarotReadingAvailability.Locked?>(null) }
    var feedbackDialogSession by remember { mutableStateOf<TarotReadingSession?>(null) }
    val currentLocale = LocalAynvoraLocale.current
    val activeLanguage = currentLocale.localeId.ifBlank { language }
    val coroutineScope = rememberCoroutineScope()
    val readingUseCase = remember(tarotRepository, analyticsTracker) {
        PerformTarotReadingUseCase(
            tarotRepository = tarotRepository,
            analyticsTracker = analyticsTracker,
        )
    }

    LaunchedEffect(Unit) {
        tarotViewModel.onEvent(TarotUiEvent.ScreenOpened)
        val s = (sessionRepository.getLatestSession() as? AynvoraResult.Success)?.value
        activeSession =
            if (s != null && s.status != TarotReadingStatus.EXPIRED && s.status != TarotReadingStatus.ARCHIVED) s else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 20 } togetherWith
                        fadeOut(tween(200))
            },
            modifier = Modifier.fillMaxSize(),
            label = "TarotNavigation",
        ) { dest ->
            when (dest) {
                is TarotDestination.Disclaimer -> TarotDisclaimerScreen(
                    language = activeLanguage,
                    onAccept = {
                        tarotViewModel.onEvent(TarotUiEvent.DisclaimerAccepted)
                        destination = TarotDestination.Home
                    },
                    onClose = onClose,
                )

                is TarotDestination.Home -> TarotHomeScreen(
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onSpreadSelected = { spreadId ->
                        tarotViewModel.onEvent(TarotUiEvent.SpreadSelected(spreadId))
                    },
                    readingUseCase = readingUseCase,
                    selectedDeckId = selectedDeckId,
                    activeSession = activeSession,
                    availabilityPolicy = availabilityPolicy,
                    sessionRepository = sessionRepository,
                    onSelectDeck = { selectedDeckId = it },
                    onReadingComplete = { reading, session ->
                        activeSession = session
                        destination = TarotDestination.Reveal(reading, session)
                    },
                    onReadingLocked = { locked ->
                        tarotViewModel.onEvent(TarotUiEvent.ReadingLockShown(locked.remainingMs.toString()))
                        lockDialogLocked = locked
                    },
                    onContinueActiveReading = { session ->
                        coroutineScope.launch {
                            val readings = tarotRepository.observeRecentReadings(10).firstOrNull()
                                ?: emptyList()
                            val existingReading =
                                readings.firstOrNull { it.id == session.reading.id }
                            if (existingReading != null) {
                                destination =
                                    TarotDestination.ReadingResult(existingReading, session)
                            } else {
                                destination = TarotDestination.Timeline(session.id)
                            }
                        }
                    },
                    onOpenHistory = { destination = TarotDestination.History },
                    onOpenCardBrowser = {
                        destination = TarotDestination.CardBrowser(activeLanguage, selectedDeckId)
                    },
                    onOpenDeckLicenses = { destination = TarotDestination.DeckLicenses },
                    onClose = onClose,
                )

                is TarotDestination.Reveal -> TarotRevealScreen(
                    reading = dest.reading,
                    session = dest.session,
                    sessionRepository = sessionRepository,
                    language = activeLanguage,
                    onRevealComplete = {
                        destination = TarotDestination.ReadingResult(dest.reading, dest.session)
                    },
                )

                is TarotDestination.ReadingResult -> TarotReadingResultScreen(
                    reading = dest.reading,
                    session = dest.session,
                    tarotRepository = tarotRepository,
                    sessionRepository = sessionRepository,
                    questionEngine = questionEngine,
                    availabilityPolicy = availabilityPolicy,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onNewReading = { destination = TarotDestination.Home },
                    onReadingLocked = { locked ->
                        tarotViewModel.onEvent(TarotUiEvent.ReadingLockShown(locked.remainingMs.toString()))
                        lockDialogLocked = locked
                    },
                    onViewTimeline = { sessionId ->
                        destination = TarotDestination.Timeline(sessionId)
                    },
                    onOpenFeedback = { session ->
                        feedbackDialogSession = session
                    },
                    onViewReport = { reading ->
                        destination = TarotDestination.TarotReport(reading, dest.session)
                    },
                    onClose = onClose,
                )

                is TarotDestination.History -> TarotHistoryScreen(
                    tarotRepository = tarotRepository,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onReadingSelected = { reading ->
                        coroutineScope.launch {
                            val sess = sessionRepository.getSession(reading.id)
                            val s = (sess as? AynvoraResult.Success)?.value
                            destination = TarotDestination.HistoryDetail(reading, s)
                        }
                    },
                    onBack = { destination = TarotDestination.Home },
                )

                is TarotDestination.HistoryDetail -> TarotReadingResultScreen(
                    reading = dest.reading,
                    session = dest.session,
                    tarotRepository = tarotRepository,
                    sessionRepository = sessionRepository,
                    questionEngine = questionEngine,
                    availabilityPolicy = availabilityPolicy,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onNewReading = { destination = TarotDestination.Home },
                    onReadingLocked = { locked ->
                        tarotViewModel.onEvent(TarotUiEvent.ReadingLockShown(locked.remainingMs.toString()))
                        lockDialogLocked = locked
                    },
                    onViewTimeline = { sessionId ->
                        destination = TarotDestination.Timeline(sessionId)
                    },
                    onOpenFeedback = { session ->
                        feedbackDialogSession = session
                    },
                    onViewReport = { reading ->
                        destination = TarotDestination.TarotReport(reading, dest.session)
                    },
                    onClose = { destination = TarotDestination.History },
                )

                is TarotDestination.Timeline -> TarotTimelineScreen(
                    sessionId = dest.sessionId,
                    sessionRepository = sessionRepository,
                    tarotRepository = tarotRepository,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onBack = { destination = TarotDestination.Home },
                )

                is TarotDestination.CardBrowser -> TarotCardBrowserScreen(
                    tarotRepository = tarotRepository,
                    initialDeckId = dest.deckId,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onCardSelected = { cardId, deckId ->
                        destination = TarotDestination.CardDetail(cardId, activeLanguage, deckId)
                    },
                    onBack = { destination = TarotDestination.Home },
                )

                is TarotDestination.CardDetail -> TarotCardDetailScreen(
                    cardId = dest.cardId,
                    deckId = dest.deckId,
                    tarotRepository = tarotRepository,
                    language = activeLanguage,
                    analyticsTracker = analyticsTracker,
                    onBack = {
                        destination = TarotDestination.CardBrowser(activeLanguage, dest.deckId)
                    },
                )

                is TarotDestination.DeckLicenses -> TarotDeckLicensesScreen(
                    language = activeLanguage,
                    onBack = { destination = TarotDestination.Home },
                )

                is TarotDestination.TarotReport -> {
                    val resolverInstance = resolver
                    if (resolverInstance != null) {
                        TarotReportScreen(
                            reading = dest.reading,
                            tarotRepository = tarotRepository,
                            prepareTarotReport = prepareTarotReport,
                            generateReport = generateReport,
                            resolver = resolverInstance,
                            language = activeLanguage,
                            analyticsTracker = analyticsTracker,
                            pdfGenerator = pdfGenerator,
                            shareService = shareService,
                            onBack = {
                                destination =
                                    TarotDestination.ReadingResult(dest.reading, dest.session)
                            },
                        )
                    } else {
                        TarotReadingResultScreen(
                            reading = dest.reading,
                            session = dest.session,
                            tarotRepository = tarotRepository,
                            sessionRepository = sessionRepository,
                            questionEngine = questionEngine,
                            availabilityPolicy = availabilityPolicy,
                            language = activeLanguage,
                            analyticsTracker = analyticsTracker,
                            onNewReading = { destination = TarotDestination.Home },
                            onReadingLocked = { locked ->
                                tarotViewModel.onEvent(TarotUiEvent.ReadingLockShown(locked.remainingMs.toString()))
                                lockDialogLocked = locked
                            },
                            onViewTimeline = { sessionId ->
                                destination = TarotDestination.Timeline(sessionId)
                            },
                            onOpenFeedback = { session ->
                                feedbackDialogSession = session
                            },
                            onViewReport = {},
                            onClose = onClose,
                        )
                    }
                }
            }
        }

        // 24-hour reading lock dialog
        lockDialogLocked?.let { locked ->
            TarotReadingLockDialog(
                locked = locked,
                language = activeLanguage,
                onContinueExisting = { session ->
                    lockDialogLocked = null
                    coroutineScope.launch {
                        val readings =
                            tarotRepository.observeRecentReadings(10).firstOrNull() ?: emptyList()
                        val existingReading = readings.firstOrNull { it.id == session.reading.id }
                        if (existingReading != null) {
                            destination = TarotDestination.ReadingResult(existingReading, session)
                        } else {
                            destination = TarotDestination.Timeline(session.id)
                        }
                    }
                },
                onViewHistory = {
                    lockDialogLocked = null
                    destination = TarotDestination.History
                },
                onDismiss = {
                    lockDialogLocked = null
                },
            )
        }

        // Post-reading satisfaction feedback modal
        feedbackDialogSession?.let { session ->
            TarotFeedbackDialog(
                session = session,
                language = activeLanguage,
                onSubmit = { rating, text ->
                    feedbackDialogSession = null
                    coroutineScope.launch {
                        val feedback = TarotFeedback(
                            id = "fb_${session.id}_${System.currentTimeMillis()}",
                            sessionId = session.id,
                            readingId = session.reading.id,
                            starRating = rating,
                            optionalText = text,
                            language = activeLanguage,
                            submittedAtEpochMs = System.currentTimeMillis(),
                        )
                        sessionRepository.saveFeedback(feedback)
                        val timelineEvent = TarotTimelineEvent(
                            id = "ev_fb_${System.currentTimeMillis()}",
                            readingId = session.reading.id,
                            sessionId = session.id,
                            timestampEpochMs = System.currentTimeMillis(),
                            sequenceIndex = 99,
                            eventType = TarotTimelineEventType.FEEDBACK_SUBMITTED,
                            language = activeLanguage,
                            summaryKey = "User Rating: $rating/5 ★" + (text?.let { " - \"$it\"" }
                                ?: ""),
                        )
                        sessionRepository.appendTimelineEvent(timelineEvent)
                        sessionRepository.updateSessionStatus(
                            session.id,
                            TarotReadingStatus.SATISFIED
                        )
                        val s =
                            (sessionRepository.getLatestSession() as? AynvoraResult.Success)?.value
                        activeSession =
                            if (s != null && s.status != TarotReadingStatus.EXPIRED && s.status != TarotReadingStatus.ARCHIVED) s else null
                        tarotViewModel.onEvent(TarotUiEvent.FeedbackSubmitted(rating, text))
                    }
                },
                onSkip = {
                    feedbackDialogSession = null
                },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Disclaimer Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotDisclaimerScreen(
    language: String,
    onAccept: () -> Unit,
    onClose: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(24.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.Close),
                variant = AynvoraButtonVariant.Ghost,
                onClick = onClose,
            )
            TarotLanguageSwitcher(currentLanguage = language)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.sdp))
            Text(
                text = "✦",
                style = AynvoraTheme.typography.display36.copy(fontSize = 48.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(Modifier.height(16.sdp))
            Text(
                text = translator.translate(TranslationKey.Tarot.ReflectionTitle),
                style = AynvoraTheme.typography.display36.copy(fontSize = 30.ssp),
                color = AynvoraTheme.colors.Gold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = translator.translate(TranslationKey.Tarot.DisclosureSubtitle),
                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.sdp))
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
                containerColor = AynvoraTheme.colors.CosmicNavy,
                contentColor = AynvoraTheme.colors.TextLight,
            ) {
                Column(modifier = Modifier.padding(20.sdp)) {
                    Text(
                        text = translator.translate(TranslationKey.Tarot.DisclosureTitle),
                        style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(Modifier.height(12.sdp))
                    Text(
                        text = translator.translate(TranslationKey.Tarot.DisclaimerText),
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.sdp),
        ) {
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.DisclosureAccept),
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAccept,
            )
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.DisclosureBack),
                variant = AynvoraButtonVariant.Ghost,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClose,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Home Screen (Spread Selection + Draw)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotHomeScreen(
    language: String,
    analyticsTracker: AnalyticsTracker,
    readingUseCase: PerformTarotReadingUseCase,
    selectedDeckId: String,
    activeSession: TarotReadingSession?,
    availabilityPolicy: TarotReadingAvailabilityPolicy,
    sessionRepository: TarotSessionRepository,
    onSelectDeck: (String) -> Unit,
    onReadingComplete: (TarotReading, TarotReadingSession) -> Unit,
    onReadingLocked: (TarotReadingAvailability.Locked) -> Unit,
    onContinueActiveReading: (TarotReadingSession) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCardBrowser: () -> Unit,
    onOpenDeckLicenses: () -> Unit,
    onClose: () -> Unit,
    onSpreadSelected: (String) -> Unit = {},
) {
    var selectedSpread by remember { mutableStateOf(TarotSpread.SingleCard) }
    var isDrawing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = translator.translate(TranslationKey.Tarot.ReflectionTitle),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 24.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
                AynvoraButton(
                    text = translator.translate(TranslationKey.Tarot.Close),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onClose,
                )
            }
        }

        Spacer(Modifier.height(4.sdp))
        Text(
            text = translator.translate(TranslationKey.Tarot.DisclosureSubtitle),
            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
        )

        activeSession?.let { session ->
            Spacer(Modifier.height(14.sdp))
            TarotActiveReadingBanner(
                session = session,
                language = language,
                onContinue = onContinueActiveReading,
            )
        }

        Spacer(Modifier.height(20.sdp))

        // Quick-access row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.CardBrowser),
                variant = AynvoraButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
                onClick = onOpenCardBrowser,
            )
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.History),
                variant = AynvoraButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
                onClick = onOpenHistory,
            )
        }

        Spacer(Modifier.height(8.sdp))
        AynvoraButton(
            text = translator.translate(TranslationKey.Tarot.DeckAttribution),
            variant = AynvoraButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenDeckLicenses,
        )

        Spacer(Modifier.height(20.sdp))

        // Deck Selection
        Text(
            text = translator.translate(TranslationKey.Tarot.SelectDeck),
            style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
            color = AynvoraTheme.colors.GoldLight,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.sdp))

        // Rider-Waite-Smith Standard (Default)
        val isRws = selectedDeckId == "rider_waite_smith_standard"
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDeck("rider_waite_smith_standard") }
                .then(
                    if (isRws) Modifier.border(
                        1.5.dp,
                        AynvoraTheme.colors.Gold,
                        RoundedCornerShape(12.dp),
                    ) else Modifier
                ),
            variant = AynvoraCardVariant.Elevated,
            containerColor = if (isRws)
                AynvoraTheme.colors.CosmicNavy
            else
                AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.8f),
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Row(
                modifier = Modifier.padding(12.sdp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TarotCardBackImage(
                    deckId = "rider_waite_smith_standard",
                    isThumbnail = true,
                    modifier = Modifier
                        .width(36.sdp)
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(12.sdp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Rider-Waite-Smith (1909)",
                        style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                        color = if (isRws) AynvoraTheme.colors.GoldLight else AynvoraTheme.colors.TextLight,
                    )
                    Spacer(Modifier.height(2.sdp))
                    Text(
                        text = "Classic 78-card deck · Public Domain",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
                if (isRws) {
                    Spacer(Modifier.width(8.sdp))
                    Text(
                        text = "✓",
                        color = AynvoraTheme.colors.Gold,
                        style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.sdp))

        // Soimoi Tarot Deck
        val isSoimoi = selectedDeckId == "soimoi_tarot"
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDeck("soimoi_tarot") }
                .then(
                    if (isSoimoi) Modifier.border(
                        1.5.dp,
                        AynvoraTheme.colors.Gold,
                        RoundedCornerShape(12.dp),
                    ) else Modifier
                ),
            variant = AynvoraCardVariant.Elevated,
            containerColor = if (isSoimoi)
                AynvoraTheme.colors.CosmicNavy
            else
                AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.8f),
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Row(
                modifier = Modifier.padding(12.sdp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TarotCardBackImage(
                    deckId = "soimoi_tarot",
                    isThumbnail = true,
                    modifier = Modifier
                        .width(36.sdp)
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(12.sdp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Soimoi Tarot Deck",
                        style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                        color = if (isSoimoi) AynvoraTheme.colors.GoldLight else AynvoraTheme.colors.TextLight,
                    )
                    Spacer(Modifier.height(2.sdp))
                    Text(
                        text = "Contemporary 78-card deck by Mike Koz · CC BY 4.0",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
                if (isSoimoi) {
                    Spacer(Modifier.width(8.sdp))
                    Text(
                        text = "✓",
                        color = AynvoraTheme.colors.Gold,
                        style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.sdp))

        // Spread Selection
        Text(
            text = translator.translate(TranslationKey.Tarot.SelectSpread),
            style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
            color = AynvoraTheme.colors.GoldLight,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.sdp))

        TarotSpread.StandardSpreads.forEach { spread ->
            val isSelected = spread.id == selectedSpread.id
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedSpread = spread
                        errorMessage = null
                        onSpreadSelected(spread.id)
                    }
                    .then(
                        if (isSelected) Modifier.border(
                            1.5.dp,
                            AynvoraTheme.colors.Gold,
                            RoundedCornerShape(12.dp),
                        ) else Modifier
                    ),
                variant = AynvoraCardVariant.Elevated,
                containerColor = if (isSelected)
                    AynvoraTheme.colors.CosmicNavy
                else
                    AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.8f),
                contentColor = AynvoraTheme.colors.TextLight,
            ) {
                Row(
                    modifier = Modifier.padding(16.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (spread.id == "single_card") "✦" else "✦ ✦ ✦",
                        color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary,
                        style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                    )
                    Spacer(Modifier.width(12.sdp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spread.name,
                            style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                            color = if (isSelected) AynvoraTheme.colors.GoldLight else AynvoraTheme.colors.TextLight,
                        )
                        Spacer(Modifier.height(2.sdp))
                        Text(
                            text = spread.description,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isSelected) {
                        Spacer(Modifier.width(8.sdp))
                        Text(
                            text = "✓",
                            color = AynvoraTheme.colors.Gold,
                            style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.sdp))
        }

        Spacer(Modifier.height(16.sdp))

        // Draw button
        AynvoraButton(
            text = if (isDrawing) {
                translator.translate(TranslationKey.Tarot.DrawingCards)
            } else {
                translator.translate(TranslationKey.Tarot.DrawCards)
            },
            variant = AynvoraButtonVariant.Primary,
            enabled = !isDrawing,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (isDrawing) return@AynvoraButton
                isDrawing = true
                errorMessage = null
                scope.launch {
                    val eval = availabilityPolicy.evaluate()
                    if (eval is AynvoraResult.Success) {
                        val avail = eval.value
                        if (avail is TarotReadingAvailability.Locked) {
                            isDrawing = false
                            onReadingLocked(avail)
                            return@launch
                        }
                        if (avail is TarotReadingAvailability.ActiveReadingExists) {
                            isDrawing = false
                            onReadingLocked(
                                TarotReadingAvailability.Locked(
                                    session = avail.session,
                                    remainingMs = (avail.session.expiresAtEpochMs - System.currentTimeMillis()).coerceAtLeast(
                                        0L
                                    ),
                                    nextAvailableAtEpochMs = avail.session.expiresAtEpochMs,
                                )
                            )
                            return@launch
                        }
                    }
                    val now = System.currentTimeMillis()
                    val result = readingUseCase.execute(
                        spread = selectedSpread,
                        allowReversed = true,
                        deckId = selectedDeckId,
                        timestampEpochMs = now,
                    )
                    when (result) {
                        is AynvoraResult.Success -> {
                            val reading = result.value
                            val sessionRes = availabilityPolicy.startNewSession(
                                reading = reading,
                                language = language,
                                deckId = selectedDeckId,
                            )
                            if (sessionRes is AynvoraResult.Success) {
                                val session = sessionRes.value
                                sessionRepository.appendTimelineEvent(
                                    TarotTimelineEvent(
                                        id = "ev_start_${now}",
                                        readingId = session.reading.id,
                                        sessionId = session.id,
                                        timestampEpochMs = now,
                                        sequenceIndex = 0,
                                        eventType = TarotTimelineEventType.READING_STARTED,
                                        language = language,
                                        summaryKey = "Tarot Reading Started",
                                    )
                                )
                                sessionRepository.appendTimelineEvent(
                                    TarotTimelineEvent(
                                        id = "ev_spread_${now + 1}",
                                        readingId = session.reading.id,
                                        sessionId = session.id,
                                        timestampEpochMs = now + 1,
                                        sequenceIndex = 1,
                                        eventType = TarotTimelineEventType.SPREAD_SELECTED,
                                        language = language,
                                        summaryKey = "Spread: ${selectedSpread.name}",
                                    )
                                )
                                sessionRepository.appendTimelineEvent(
                                    TarotTimelineEvent(
                                        id = "ev_shuffled_${now + 2}",
                                        readingId = session.reading.id,
                                        sessionId = session.id,
                                        timestampEpochMs = now + 2,
                                        sequenceIndex = 2,
                                        eventType = TarotTimelineEventType.CARDS_SHUFFLED,
                                        language = language,
                                        summaryKey = "Cards Shuffled",
                                    )
                                )
                                reading.draws.forEachIndexed { idx, draw ->
                                    sessionRepository.appendTimelineEvent(
                                        TarotTimelineEvent(
                                            id = "ev_draw_${now + 10 + idx}",
                                            readingId = session.reading.id,
                                            sessionId = session.id,
                                            timestampEpochMs = now + 10 + idx,
                                            sequenceIndex = 3 + idx,
                                            eventType = TarotTimelineEventType.PRIMARY_CARD_DRAWN,
                                            language = language,
                                            summaryKey = "${draw.card.name} (${draw.orientation.name.lowercase()}) - ${draw.position.name}",
                                        )
                                    )
                                }
                                onReadingComplete(reading, session)
                            }
                        }

                        is AynvoraResult.Failure -> {
                            errorMessage = result.message
                        }
                    }
                    isDrawing = false
                }
            },
        )

        errorMessage?.let { err ->
            Spacer(Modifier.height(8.sdp))
            Text(
                text = err,
                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                color = AynvoraTheme.colors.Error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(32.sdp))

        // Non-predictive footer notice
        Text(
            text = translator.translate(TranslationKey.TarotSession.QuestionDisclaimer),
            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
            color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.sdp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reveal Screen (Animated card-flip reveal)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotRevealScreen(
    reading: TarotReading,
    session: TarotReadingSession? = null,
    sessionRepository: TarotSessionRepository? = null,
    language: String,
    onRevealComplete: () -> Unit,
) {
    var revealedCount by remember { mutableStateOf(0) }
    val totalCards = reading.draws.size
    val coroutineScope = rememberCoroutineScope()
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(24.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TarotLanguageSwitcher(currentLanguage = language)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.sdp))
            Text(
                text = translator.translate(TranslationKey.Tarot.CardsEmerging),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 24.ssp),
                color = AynvoraTheme.colors.Gold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = translator.translate(TranslationKey.Tarot.TapToReveal),
                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
                textAlign = TextAlign.Center,
            )
        }

        // Cards in a row or stacked
        if (reading.draws.size == 1) {
            val draw = reading.draws[0]
            val isRevealed = revealedCount >= 1
            TarotCardRevealItem(
                draw = draw,
                deckId = reading.deckId,
                isRevealed = isRevealed,
                language = language,
                onClick = {
                    if (!isRevealed) revealedCount = 1
                },
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.sdp),
                contentPadding = PaddingValues(horizontal = 16.sdp),
            ) {
                items(reading.draws.indices.toList()) { index ->
                    val draw = reading.draws[index]
                    val isRevealed = revealedCount > index
                    TarotCardRevealItem(
                        draw = draw,
                        deckId = reading.deckId,
                        isRevealed = isRevealed,
                        language = language,
                        onClick = {
                            if (revealedCount == index) revealedCount = index + 1
                        },
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.sdp),
        ) {
            Text(
                text = "$revealedCount / $totalCards ${translator.translate(TranslationKey.Tarot.RevealedSuffix)}",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
            )
            AnimatedVisibility(visible = revealedCount >= totalCards) {
                AynvoraButton(
                    text = translator.translate(TranslationKey.Tarot.ViewMeanings),
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (session != null && sessionRepository != null) {
                            val nowMs = System.currentTimeMillis()
                            coroutineScope.launch {
                                sessionRepository.appendTimelineEvent(
                                    TarotTimelineEvent(
                                        id = "ev_revealed_${nowMs}",
                                        readingId = session.reading.id,
                                        sessionId = session.id,
                                        timestampEpochMs = nowMs,
                                        sequenceIndex = 10,
                                        eventType = TarotTimelineEventType.CARD_REVEALED,
                                        language = language,
                                        summaryKey = "All ${reading.draws.size} cards revealed",
                                    )
                                )
                            }
                        }
                        onRevealComplete()
                    },
                )
            }
        }
    }
}

@Composable
private fun TarotCardRevealItem(
    draw: TarotCardDraw,
    deckId: String,
    isRevealed: Boolean,
    language: String,
    onClick: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    val alpha by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_alpha",
    )
    val rotation by animateFloatAsState(
        targetValue = if (draw.orientation == TarotCardOrientation.REVERSED && isRevealed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_rotation",
    )

    Column(
        modifier = Modifier
            .width(160.sdp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.sdp),
    ) {
        // Position label
        Text(
            text = draw.position.name,
            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
            color = AynvoraTheme.colors.CelestialBlue,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )

        // Card Face (Authentic Card Back when unrevealed, Authentic Card Artwork when revealed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.6f)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    if (isRevealed) 1.5.dp else 1.dp,
                    if (isRevealed) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary.copy(
                        alpha = 0.4f
                    ),
                    RoundedCornerShape(10.dp),
                )
                .alpha(alpha)
                .rotate(rotation),
            contentAlignment = Alignment.Center,
        ) {
            if (isRevealed) {
                TarotCardImage(
                    cardId = draw.card.id,
                    deckId = deckId,
                    isThumbnail = false,
                    isReversed = false, // Rotation handled by Box modifier
                    contentDescription = draw.card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                TarotCardBackImage(
                    deckId = deckId,
                    isThumbnail = false,
                    contentDescription = translator.translate(TranslationKey.Tarot.TapToReveal),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (isRevealed) {
            Text(
                text = draw.card.name,
                style = AynvoraTheme.typography.title18.copy(fontSize = 13.ssp),
                color = AynvoraTheme.colors.GoldLight,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                    translator.translate(TranslationKey.Tarot.UprightArrow)
                else
                    translator.translate(TranslationKey.Tarot.ReversedArrow),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                    AynvoraTheme.colors.Gold
                else
                    AynvoraTheme.colors.TextLightSecondary,
            )
        } else {
            Text(
                text = translator.translate(TranslationKey.Tarot.TapToReveal),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reading Result Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotReadingResultScreen(
    reading: TarotReading,
    session: TarotReadingSession? = null,
    tarotRepository: TarotRepository,
    sessionRepository: TarotSessionRepository,
    questionEngine: TarotQuestionEngine,
    availabilityPolicy: TarotReadingAvailabilityPolicy,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onNewReading: () -> Unit,
    onReadingLocked: (TarotReadingAvailability.Locked) -> Unit = {},
    onViewTimeline: (String) -> Unit = {},
    onOpenFeedback: (TarotReadingSession) -> Unit = {},
    onViewReport: (TarotReading) -> Unit,
    onClose: () -> Unit,
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
) {
    var currentSession by remember(reading.id, session) { mutableStateOf(session) }
    val coroutineScope = rememberCoroutineScope()
    val translator = LocalAynvoraTranslator.current

    LaunchedEffect(reading.id) {
        if (currentSession == null) {
            val sRes = sessionRepository.getSession(reading.id)
            if (sRes is AynvoraResult.Success) {
                currentSession = sRes.value
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack),
        contentPadding = PaddingValues(horizontal = 20.sdp, vertical = 16.sdp),
        verticalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = translator.translate(TranslationKey.Tarot.YourReading),
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    TarotLanguageSwitcher(currentLanguage = language)
                    AynvoraButton(
                        text = translator.translate(TranslationKey.Tarot.Close),
                        variant = AynvoraButtonVariant.Ghost,
                        onClick = onClose,
                    )
                }
            }
        }

        item {
            Text(
                text = translator.translate(TranslationKey.TarotSession.QuestionDisclaimer),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.7f),
            )
        }

        items(reading.draws) { draw ->
            TarotDrawCard(
                draw = draw,
                deckId = reading.deckId,
                tarotRepository = tarotRepository,
                language = language,
                analyticsTracker = analyticsTracker,
                onCardFeedback = { cardId, category ->
                    currentSession?.let { sess ->
                        coroutineScope.launch {
                            val cf = TarotCardFeedback(
                                id = "cfb_${cardId}_${System.currentTimeMillis()}",
                                readingId = sess.reading.id,
                                sessionId = sess.id,
                                cardId = cardId,
                                category = category,
                                submittedAtEpochMs = System.currentTimeMillis(),
                            )
                            sessionRepository.saveCardFeedback(cf)
                            tarotViewModel.onEvent(
                                TarotUiEvent.CardFeedbackSubmitted(
                                    cardId = cardId,
                                    helpful = (category == TarotCardFeedbackCategory.HELPFUL),
                                )
                            )
                        }
                    }
                },
            )
        }

        item { Spacer(Modifier.height(8.sdp)) }

        item {
            TarotQuestionSection(
                session = currentSession,
                reading = reading,
                tarotRepository = tarotRepository,
                sessionRepository = sessionRepository,
                questionEngine = questionEngine,
                language = language,
                analyticsTracker = analyticsTracker,
                tarotViewModel = tarotViewModel,
            )
        }

        currentSession?.let { sess ->
            item {
                Spacer(Modifier.height(8.sdp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    AynvoraButton(
                        text = translator.translate(TranslationKey.Tarot.Timeline),
                        variant = AynvoraButtonVariant.Outlined,
                        modifier = Modifier.weight(1f),
                        onClick = { onViewTimeline(sess.id) },
                    )
                    AynvoraButton(
                        text = translator.translate(TranslationKey.Tarot.Satisfied),
                        variant = AynvoraButtonVariant.Secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenFeedback(sess) },
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.sdp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                AynvoraButton(
                    text = translator.translate(TranslationKey.Tarot.NewReading),
                    variant = AynvoraButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            val eval = availabilityPolicy.evaluate()
                            if (eval is AynvoraResult.Success) {
                                when (val avail = eval.value) {
                                    is TarotReadingAvailability.Locked -> onReadingLocked(avail)
                                    is TarotReadingAvailability.ActiveReadingExists -> onReadingLocked(
                                        TarotReadingAvailability.Locked(
                                            session = avail.session,
                                            remainingMs = (avail.session.expiresAtEpochMs - System.currentTimeMillis()).coerceAtLeast(
                                                0L
                                            ),
                                            nextAvailableAtEpochMs = avail.session.expiresAtEpochMs,
                                        )
                                    )

                                    is TarotReadingAvailability.Available -> onNewReading()
                                }
                            } else {
                                onNewReading()
                            }
                        }
                    },
                )
                AynvoraButton(
                    text = translator.translate(TranslationKey.Tarot.Report),
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onViewReport(reading) },
                )
            }
        }

        item { Spacer(Modifier.height(24.sdp)) }
    }
}

@Composable
private fun TarotDrawCard(
    draw: TarotCardDraw,
    deckId: String,
    tarotRepository: TarotRepository,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onCardFeedback: (String, TarotCardFeedbackCategory) -> Unit = { _, _ -> },
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
) {
    val translator = LocalAynvoraTranslator.current
    var content by remember(draw.card.id, language) { mutableStateOf<TarotCardContent?>(null) }

    LaunchedEffect(draw.card.id, language) {
        val result = tarotRepository.getCardContent(draw.card.id, language)
        if (result is AynvoraResult.Success) {
            content = result.value
            tarotViewModel.onEvent(TarotUiEvent.ContentOpened(draw.card.id, language))
        }
    }

    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(modifier = Modifier.padding(16.sdp)) {
            // Position badge
            Text(
                text = draw.position.name.uppercase(),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = AynvoraTheme.colors.CelestialBlue,
            )
            Spacer(Modifier.height(10.sdp))

            // Artwork + Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.sdp),
                verticalAlignment = Alignment.Top,
            ) {
                TarotCardImage(
                    cardId = draw.card.id,
                    deckId = deckId,
                    isThumbnail = true,
                    isReversed = (draw.orientation == TarotCardOrientation.REVERSED),
                    contentDescription = content?.title ?: draw.card.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(92.sdp)
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(8.dp)),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = content?.title ?: draw.card.name,
                        style = AynvoraTheme.typography.title20.copy(fontSize = 18.ssp),
                        color = AynvoraTheme.colors.GoldLight,
                    )
                    Spacer(Modifier.height(4.sdp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (draw.orientation == TarotCardOrientation.UPRIGHT)
                                    AynvoraTheme.colors.Gold.copy(alpha = 0.15f)
                                else
                                    AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 8.sdp, vertical = 4.sdp),
                    ) {
                        Text(
                            text = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                                translator.translate(TranslationKey.Tarot.UprightArrow)
                            else
                                translator.translate(TranslationKey.Tarot.ReversedArrow),
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                                AynvoraTheme.colors.Gold
                            else
                                AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                    content?.let { c ->
                        if (c.shortDescription.isNotBlank()) {
                            Spacer(Modifier.height(6.sdp))
                            Text(
                                text = c.shortDescription,
                                style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                color = AynvoraTheme.colors.TextLightSecondary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            content?.let { c ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.sdp),
                    color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.15f),
                )

                Text(
                    text = translator.translate(TranslationKey.Tarot.ReflectivePerspective),
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                    color = AynvoraTheme.colors.CelestialBlue,
                )
                Spacer(Modifier.height(4.sdp))

                val meaning = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                    c.uprightMeaning else c.reversedMeaning
                Text(
                    text = meaning,
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = AynvoraTheme.colors.TextLight,
                )

                if (c.keywords.isNotEmpty()) {
                    Spacer(Modifier.height(10.sdp))
                    Text(
                        text = translator.translate(TranslationKey.Tarot.KeywordsLabel) +
                                c.keywords.joinToString(" · "),
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }

                Spacer(Modifier.height(6.sdp))
                Text(
                    text = "↳ ${c.sourceAttribution} · v${c.contentVersion}",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(10.sdp))
                TarotCardFeedbackStrip(
                    cardId = draw.card.id,
                    language = language,
                    onFeedbackSelected = { category ->
                        onCardFeedback(draw.card.id, category)
                    },
                )
            } ?: run {
                Spacer(Modifier.height(8.sdp))
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AynvoraTheme.colors.Gold,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// History Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotHistoryScreen(
    tarotRepository: TarotRepository,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onReadingSelected: (TarotReading) -> Unit,
    onBack: () -> Unit,
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
) {
    val historyFlow = remember { tarotRepository.observeRecentReadings(limit = 30) }
    val readings by historyFlow.collectAsState(initial = emptyList())
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp, vertical = 16.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = translator.translate(TranslationKey.Tarot.History),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
                AynvoraButton(
                    text = translator.translate(TranslationKey.App.Back),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onBack,
                )
            }
        }

        if (readings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    Text(
                        "✦",
                        color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.3f),
                        style = AynvoraTheme.typography.display36
                    )
                    Text(
                        text = translator.translate(TranslationKey.Tarot.NoReadingsYet),
                        style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.sdp, vertical = 8.sdp),
                verticalArrangement = Arrangement.spacedBy(10.sdp),
            ) {
                items(readings) { reading ->
                    TarotHistoryItem(
                        reading = reading,
                        language = language,
                        onClick = {
                            tarotViewModel.onEvent(TarotUiEvent.HistoryOpened)
                            onReadingSelected(reading)
                        },
                    )
                }
                item { Spacer(Modifier.height(24.sdp)) }
            }
        }
    }
}

@Composable
private fun TarotHistoryItem(
    reading: TarotReading,
    language: String,
    onClick: () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        variant = AynvoraCardVariant.Outlined,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Row(
            modifier = Modifier.padding(14.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reading.spreadId.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
                Spacer(Modifier.height(2.sdp))
                Text(
                    text = reading.draws.joinToString(", ") { it.card.name }.let {
                        if (it.length > 48) it.take(45) + "…" else it
                    },
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
            Text(
                text = "›",
                style = AynvoraTheme.typography.title18.copy(fontSize = 20.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card Browser Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotCardBrowserScreen(
    tarotRepository: TarotRepository,
    initialDeckId: String = "rider_waite_smith_standard",
    language: String,
    analyticsTracker: AnalyticsTracker,
    onCardSelected: (String, String) -> Unit,
    onBack: () -> Unit,
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
) {
    var currentDeckId by remember { mutableStateOf(initialDeckId) }
    val translator = LocalAynvoraTranslator.current
    val allContentFlow = remember(language) { tarotRepository.observeAllCardContent(language) }
    val allContentResult by allContentFlow.collectAsState(initial = null)
    val allContent = (allContentResult as? AynvoraResult.Success)?.value ?: emptyList()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(allContent, searchQuery) {
        if (searchQuery.isBlank()) allContent
        else allContent.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.keywords.any { kw -> kw.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp, vertical = 14.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = translator.translate(TranslationKey.Tarot.CardBrowser),
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Text(
                    text = "${filtered.size} / 78 ${translator.translate(TranslationKey.Tarot.BrowseCardsSuffix)}",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
                AynvoraButton(
                    text = translator.translate(TranslationKey.App.Back),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onBack,
                )
            }
        }

        // Deck Switcher Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp, vertical = 4.sdp),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            val isRws = currentDeckId == "rider_waite_smith_standard"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isRws) AynvoraTheme.colors.Gold.copy(alpha = 0.2f) else AynvoraTheme.colors.CosmicNavy)
                    .border(
                        1.dp,
                        if (isRws) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary.copy(
                            alpha = 0.2f
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { currentDeckId = "rider_waite_smith_standard" }
                    .padding(horizontal = 12.sdp, vertical = 6.sdp),
            ) {
                Text(
                    text = "Rider-Waite (1909)",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = if (isRws) AynvoraTheme.colors.GoldLight else AynvoraTheme.colors.TextLightSecondary,
                )
            }

            val isSoimoi = currentDeckId == "soimoi_tarot"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSoimoi) AynvoraTheme.colors.Gold.copy(alpha = 0.2f) else AynvoraTheme.colors.CosmicNavy)
                    .border(
                        1.dp,
                        if (isSoimoi) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary.copy(
                            alpha = 0.2f
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { currentDeckId = "soimoi_tarot" }
                    .padding(horizontal = 12.sdp, vertical = 6.sdp),
            ) {
                Text(
                    text = "Soimoi Tarot (CC BY 4.0)",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = if (isSoimoi) AynvoraTheme.colors.GoldLight else AynvoraTheme.colors.TextLightSecondary,
                )
            }
        }

        Spacer(Modifier.height(8.sdp))

        if (allContent.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = AynvoraTheme.colors.Gold,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.sdp),
                contentPadding = PaddingValues(horizontal = 16.sdp, vertical = 8.sdp),
                horizontalArrangement = Arrangement.spacedBy(10.sdp),
                verticalArrangement = Arrangement.spacedBy(10.sdp),
            ) {
                items(filtered) { content ->
                    TarotCardGridItem(
                        content = content,
                        deckId = currentDeckId,
                        onClick = {
                            tarotViewModel.onEvent(
                                TarotUiEvent.ContentOpened(
                                    content.cardId,
                                    language
                                )
                            )
                            onCardSelected(content.cardId, currentDeckId)
                        },
                    )
                }
                item { Spacer(Modifier.height(24.sdp)) }
                item { Spacer(Modifier.height(24.sdp)) }
            }
        }
    }
}

@Composable
private fun TarotCardGridItem(
    content: TarotCardContent,
    deckId: String,
    onClick: () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(
            modifier = Modifier.padding(8.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Real Card Artwork as Primary Visual Element
            TarotCardImage(
                cardId = content.cardId,
                deckId = deckId,
                isThumbnail = true,
                contentDescription = content.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.6f)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = content.title,
                style = AynvoraTheme.typography.title18.copy(fontSize = 12.ssp),
                color = AynvoraTheme.colors.GoldLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(2.sdp))
            Text(
                text = content.keywords.take(2).joinToString(" · "),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card Detail Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotCardDetailScreen(
    cardId: String,
    deckId: String = "rider_waite_smith_standard",
    tarotRepository: TarotRepository,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onBack: () -> Unit,
) {
    var content by remember(cardId, language) { mutableStateOf<TarotCardContent?>(null) }
    val translator = LocalAynvoraTranslator.current

    LaunchedEffect(cardId, language) {
        val result = tarotRepository.getCardContent(cardId, language)
        if (result is AynvoraResult.Success) content = result.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.sdp, vertical = 16.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = content?.title ?: "…",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
                AynvoraButton(
                    text = translator.translate(TranslationKey.App.Back),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onBack,
                )
            }
        }

        content?.let { c ->
            Column(
                modifier = Modifier.padding(horizontal = 20.sdp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Large Real Card Artwork
                TarotCardImage(
                    cardId = cardId,
                    deckId = deckId,
                    isThumbnail = false,
                    contentDescription = c.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(220.sdp)
                        .aspectRatio(0.6f)
                        .clip(RoundedCornerShape(12.dp)),
                )

                Spacer(Modifier.height(16.sdp))

                // Short description
                Text(
                    text = c.shortDescription,
                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Keywords
                if (c.keywords.isNotEmpty()) {
                    Spacer(Modifier.height(12.sdp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.sdp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        c.keywords.forEach { kw ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AynvoraTheme.colors.Gold.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.sdp, vertical = 4.sdp),
                            ) {
                                Text(
                                    text = kw,
                                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                    color = AynvoraTheme.colors.GoldLight,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.sdp))

                // Upright meaning
                MeaningSection(
                    title = translator.translate(TranslationKey.Tarot.UprightPerspective),
                    meaning = c.uprightMeaning,
                    accentColor = AynvoraTheme.colors.Gold,
                )

                Spacer(Modifier.height(16.sdp))

                // Reversed meaning
                MeaningSection(
                    title = translator.translate(TranslationKey.Tarot.ReversedPerspective),
                    meaning = c.reversedMeaning,
                    accentColor = AynvoraTheme.colors.CelestialBlue,
                )

                Spacer(Modifier.height(20.sdp))

                // Deck Provenance & License Card
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Elevated,
                    containerColor = AynvoraTheme.colors.CosmicNavy,
                    contentColor = AynvoraTheme.colors.TextLight,
                ) {
                    Column(modifier = Modifier.padding(14.sdp)) {
                        Text(
                            text = translator.translate(TranslationKey.Tarot.DeckAttribution),
                            style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(Modifier.height(4.sdp))
                        Text(
                            text = if (deckId.contains("soimoi"))
                                "Deck: Soimoi Tarot · License: CC BY 4.0\nArtist: Mike Koz (https://koz.tv/)\nSource: mixvlad/TarotCards (tarot/soimoi)"
                            else
                                "Deck: Rider-Waite-Smith Standard · License: Public Domain (1909)\nArtist: Pamela Colman Smith & A.E. Waite\nSource: mixvlad/TarotCards (tarot/rider-waite), restored by Steve P.",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                        Spacer(Modifier.height(4.sdp))
                        Text(
                            text = "↳ Content: ${c.sourceAttribution} · v${c.contentVersion}",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                    }
                }

                Spacer(Modifier.height(32.sdp))
            }
        } ?: Box(
            modifier = Modifier.fillMaxWidth().height(200.sdp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = AynvoraTheme.colors.Gold, strokeWidth = 2.dp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deck Attribution & Licenses Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotDeckLicensesScreen(
    language: String,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(horizontal = 20.sdp, vertical = 16.sdp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = translator.translate(TranslationKey.Tarot.DeckAttribution),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
                AynvoraButton(
                    text = translator.translate(TranslationKey.App.Back),
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onBack,
                )
            }
        }

        Spacer(Modifier.height(8.sdp))
        Text(
            text = translator.translate(TranslationKey.Tarot.LicenseNotice),
            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
        )

        Spacer(Modifier.height(20.sdp))

        // Deck 1: Rider-Waite-Smith Standard
        AynvoraCard(
            modifier = Modifier.fillMaxWidth(),
            variant = AynvoraCardVariant.Elevated,
            containerColor = AynvoraTheme.colors.CosmicNavy,
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Column(modifier = Modifier.padding(16.sdp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TarotCardBackImage(
                        deckId = "rider_waite_smith_standard",
                        isThumbnail = true,
                        modifier = Modifier
                            .width(40.sdp)
                            .aspectRatio(0.6f)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.width(12.sdp))
                    Column {
                        Text(
                            text = "Rider-Waite-Smith Standard Deck",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(Modifier.height(2.sdp))
                        Text(
                            text = "License: Public Domain (1909)",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                    }
                }
                Spacer(Modifier.height(10.sdp))
                Text(
                    text = "Original artwork by Pamela Colman Smith (1878–1951) under the direction of Arthur Edward Waite (1857–1942), published December 1909 by William Rider & Son, London. Restored scans by Steve P. (steve-p.org). Source repository: mixvlad/TarotCards.",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = "Card Count: Exactly 78 Cards + Card Back · Bundled Offline",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
            }
        }

        Spacer(Modifier.height(16.sdp))

        // Deck 2: Soimoi Tarot Deck
        AynvoraCard(
            modifier = Modifier.fillMaxWidth(),
            variant = AynvoraCardVariant.Elevated,
            containerColor = AynvoraTheme.colors.CosmicNavy,
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Column(modifier = Modifier.padding(16.sdp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TarotCardBackImage(
                        deckId = "soimoi_tarot",
                        isThumbnail = true,
                        modifier = Modifier
                            .width(40.sdp)
                            .aspectRatio(0.6f)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.width(12.sdp))
                    Column {
                        Text(
                            text = "Soimoi Tarot Deck",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(Modifier.height(2.sdp))
                        Text(
                            text = "License: CC BY 4.0 (Attribution Required)",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                    }
                }
                Spacer(Modifier.height(10.sdp))
                Text(
                    text = "\"Soimoi Tarot\" by Mike Koz (https://koz.tv/), licensed under CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/). Source repository: mixvlad/TarotCards (tarot/soimoi).",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
                Spacer(Modifier.height(6.sdp))
                Text(
                    text = "Card Count: Exactly 78 Cards + Card Back · Bundled Offline",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
            }
        }

        Spacer(Modifier.height(24.sdp))
    }
}

@Composable
private fun MeaningSection(
    title: String,
    meaning: String,
    accentColor: Color,
) {
    Column {
        Text(
            text = title,
            style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp),
            color = accentColor,
        )
        Spacer(Modifier.height(6.sdp))
        Text(
            text = meaning,
            style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
            color = AynvoraTheme.colors.TextLight,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarot Report Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TarotReportScreen(
    reading: TarotReading,
    tarotRepository: TarotRepository,
    prepareTarotReport: PrepareTarotReportUseCase,
    generateReport: GenerateReportUseCase,
    resolver: ReportTextResolver,
    language: String,
    analyticsTracker: AnalyticsTracker,
    pdfGenerator: ReportPdfGenerator?,
    shareService: ReportShareService?,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var reportState by remember { mutableStateOf<ReportGenerationResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val currentLocale = LocalAynvoraLocale.current
    val reportLanguage = if (currentLocale.localeId.lowercase().startsWith("hi")) {
        ReportLanguage.HINDI
    } else {
        ReportLanguage.ENGLISH
    }

    LaunchedEffect(reading.id, language) {
        isLoading = true
        reportState = null

        // Find the spread from the reading's spreadId
        val spread = TarotSpread.StandardSpreads.firstOrNull { it.id == reading.spreadId }
            ?: TarotSpread.SingleCard

        val prepResult = prepareTarotReport.execute(
            reading = reading,
            spread = spread,
            language = reportLanguage,
            generatedAtEpochMs = System.currentTimeMillis(),
        )

        when (prepResult) {
            is AynvoraResult.Success -> {
                val request = ReportGenerationRequest(
                    reportType = ReportType.TAROT,
                    language = reportLanguage,
                    generatedAtEpochMs = System.currentTimeMillis(),
                    generatorInput = prepResult.value,
                )
                reportState =
                    generateReport.generateFrom(ReportType.TAROT, prepResult.value, resolver)
            }

            is AynvoraResult.Failure -> {
                reportState = ReportGenerationResult.Unavailable(
                    reportTypeId = ReportType.TAROT.id,
                    code = com.aynvora.core.report.ReportErrorCode.INSUFFICIENT_DATA,
                    reason = com.aynvora.core.report.ReportText(
                        "tarot.report.error",
                        "Failed to prepare Tarot report: ${prepResult.message}",
                    ),
                    featureStatus = com.aynvora.core.report.ReportFeatureStatus.IMPLEMENTED,
                )
            }
        }
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
                text = translator.translate(TranslationKey.TarotSession.TarotReportTitle),
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                TarotLanguageSwitcher(currentLanguage = language)
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
                com.aynvora.ui.report.ReportViewer(
                    document = document,
                    resolver = resolver,
                    onGeneratePdf = { /* PDF generation handled by platform-specific module */ },
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
// Phase 8.9 — Conversational Experience UI Components
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Global Top-Right Language Switcher.
 * Available on every Tarot screen. Triggers instant recomposition via [AynvoraLocaleManager]
 * without resetting screen navigation, drawn cards, questions, or timeline state.
 */
@Composable
fun TarotLanguageSwitcher(
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
) {
    val locale = LocalAynvoraLocale.current
    val localeManager: AynvoraLocaleManager = org.koin.compose.koinInject()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.9f))
                .border(
                    1.dp,
                    AynvoraTheme.colors.Gold.copy(alpha = 0.6f),
                    RoundedCornerShape(14.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 9.sdp, vertical = 4.sdp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.sdp),
        ) {
            Text(
                text = "${locale.nativeName} ▼",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = AynvoraTheme.colors.GoldLight,
            )
        }

        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AynvoraTheme.colors.CosmicBlack),
            ) {
                LanguageRegistry.availableLocales().forEach { targetLocale ->
                    val isSelected = targetLocale.localeId == locale.localeId
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${targetLocale.englishName} (${targetLocale.nativeName})",
                                color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLight,
                                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            )
                        },
                        modifier = Modifier.background(
                            if (isSelected) AynvoraTheme.colors.CosmicNavy else Color.Transparent
                        ),
                        onClick = {
                            expanded = false
                            scope.launch {
                                localeManager.setLocale(targetLocale)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Banner shown on Tarot Home when an ongoing session exists within the 24h window.
 */
@Composable
private fun TarotActiveReadingBanner(
    session: TarotReadingSession,
    language: String,
    onContinue: (TarotReadingSession) -> Unit,
    modifier: Modifier = Modifier,
) {
    val translator = LocalAynvoraTranslator.current
    AynvoraCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AynvoraTheme.colors.Gold, RoundedCornerShape(12.dp)),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.sdp)
                ) {
                    Text(
                        "✦",
                        color = AynvoraTheme.colors.Gold,
                        style = AynvoraTheme.typography.title18
                    )
                    Text(
                        text = translator.translate(TranslationKey.TarotSession.CurrentReadingAvailable),
                        style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                        color = AynvoraTheme.colors.GoldLight,
                    )
                }
                Text(
                    text = if (session.status == TarotReadingStatus.ACTIVE)
                        translator.translate(TranslationKey.TarotSession.StatusActive)
                    else
                        translator.translate(TranslationKey.TarotSession.StatusSatisfied),
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AynvoraTheme.colors.Gold.copy(alpha = 0.2f))
                        .padding(horizontal = 6.sdp, vertical = 2.sdp),
                )
            }
            Spacer(Modifier.height(6.sdp))
            Text(
                text = translator.translate(TranslationKey.TarotSession.CurrentReadingDesc),
                style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
            )
            Spacer(Modifier.height(10.sdp))
            AynvoraButton(
                text = translator.translate(TranslationKey.TarotSession.ContinueReading),
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onContinue(session) },
            )
        }
    }
}

/**
 * Dialog shown when user attempts a new reading within the 24-hour lock window.
 */
@Composable
private fun TarotReadingLockDialog(
    locked: TarotReadingAvailability.Locked,
    language: String,
    onContinueExisting: (TarotReadingSession) -> Unit,
    onViewHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    val remainingHours = locked.remainingMs / 3_600_000L
    val remainingMins = (locked.remainingMs % 3_600_000L) / 60_000L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .padding(24.sdp),
        contentAlignment = Alignment.Center,
    ) {
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AynvoraTheme.colors.Gold, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent closing when clicking card */ },
                ),
            variant = AynvoraCardVariant.Elevated,
            containerColor = AynvoraTheme.colors.CosmicNavy,
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Column(
                modifier = Modifier.padding(20.sdp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.sdp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = translator.translate(TranslationKey.TarotSession.ReadingLockTitle),
                        style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    TarotLanguageSwitcher(currentLanguage = language)
                }

                Text(
                    text = translator.translate(TranslationKey.TarotSession.ReadingLockMessage),
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                    textAlign = TextAlign.Start,
                )

                AynvoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            AynvoraTheme.colors.CelestialBlue.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        ),
                    variant = AynvoraCardVariant.Outlined,
                    containerColor = AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.6f),
                ) {
                    Column(
                        modifier = Modifier.padding(12.sdp),
                        verticalArrangement = Arrangement.spacedBy(4.sdp)
                    ) {
                        Text(
                            text = translator.translateWithArgs(
                                TranslationKey.TarotSession.ReadingLockRemaining,
                                "remaining" to "${remainingHours}h ${remainingMins}m",
                            ),
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = AynvoraTheme.colors.GoldLight,
                        )
                    }
                }

                Spacer(Modifier.height(4.sdp))

                AynvoraButton(
                    text = translator.translate(TranslationKey.TarotSession.ContinueExistingReading),
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onContinueExisting(locked.session) },
                )

                AynvoraButton(
                    text = translator.translate(TranslationKey.TarotSession.ViewHistory),
                    variant = AynvoraButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onViewHistory,
                )

                AynvoraButton(
                    text = translator.translate(TranslationKey.TarotSession.Close),
                    variant = AynvoraButtonVariant.Ghost,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss,
                )
            }
        }
    }
}

/**
 * Modal dialog for session-level feedback (1 to 5 stars + optional comment).
 */
@Composable
private fun TarotFeedbackDialog(
    session: TarotReadingSession? = null,
    language: String,
    onSubmit: (rating: Int, optionalText: String?) -> Unit,
    onSkip: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var selectedRating by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSkip,
            )
            .padding(24.sdp),
        contentAlignment = Alignment.Center,
    ) {
        AynvoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AynvoraTheme.colors.Gold, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            variant = AynvoraCardVariant.Elevated,
            containerColor = AynvoraTheme.colors.CosmicNavy,
            contentColor = AynvoraTheme.colors.TextLight,
        ) {
            Column(
                modifier = Modifier.padding(20.sdp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.sdp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = translator.translate(TranslationKey.TarotSession.FeedbackTitle),
                        style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    TarotLanguageSwitcher(currentLanguage = language)
                }

                Text(
                    text = translator.translate(TranslationKey.TarotSession.FeedbackPrompt),
                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                    color = AynvoraTheme.colors.TextLight,
                    textAlign = TextAlign.Center,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.sdp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    (1..5).forEach { starIndex ->
                        val isFilled = starIndex <= selectedRating
                        Text(
                            text = if (isFilled) "★" else "☆",
                            color = if (isFilled) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary,
                            style = AynvoraTheme.typography.display36.copy(fontSize = 32.ssp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedRating = starIndex }
                                .padding(4.sdp),
                        )
                    }
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = {
                        Text(
                            text = translator.translate(TranslationKey.TarotSession.FeedbackImprovePrompt),
                            style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.6f),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AynvoraTheme.colors.TextLight,
                        unfocusedTextColor = AynvoraTheme.colors.TextLight,
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.3f),
                        cursorColor = AynvoraTheme.colors.Gold,
                    ),
                    maxLines = 3,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    AynvoraButton(
                        text = translator.translate(TranslationKey.TarotSession.SkipFeedback),
                        variant = AynvoraButtonVariant.Ghost,
                        modifier = Modifier.weight(1f),
                        onClick = onSkip,
                    )
                    AynvoraButton(
                        text = translator.translate(TranslationKey.TarotSession.SubmitFeedback),
                        variant = AynvoraButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSubmit(
                                selectedRating,
                                feedbackText.takeIf { it.isNotBlank() })
                        },
                    )
                }
            }
        }
    }
}

/**
 * Compact swipeable category feedback strip on a card.
 */
@Composable
private fun TarotCardFeedbackStrip(
    cardId: String,
    language: String,
    onFeedbackSelected: (TarotCardFeedbackCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val translator = LocalAynvoraTranslator.current
    var selectedCategory by remember { mutableStateOf<TarotCardFeedbackCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.5f))
            .padding(horizontal = 8.sdp, vertical = 6.sdp),
        verticalArrangement = Arrangement.spacedBy(4.sdp),
    ) {
        Text(
            text = translator.translate(TranslationKey.TarotSession.CardFeedbackTitle),
            style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.sdp)) {
            val categories = listOf<Pair<TarotCardFeedbackCategory, String>>(
                TarotCardFeedbackCategory.HELPFUL to translator.translate(TranslationKey.TarotSession.CardFeedbackHelpful),
                TarotCardFeedbackCategory.CLEAR to translator.translate(TranslationKey.TarotSession.CardFeedbackClear),
                TarotCardFeedbackCategory.CONFUSING to translator.translate(TranslationKey.TarotSession.CardFeedbackConfusing),
                TarotCardFeedbackCategory.NOT_RELEVANT to translator.translate(TranslationKey.TarotSession.CardFeedbackNotRelevant),
                TarotCardFeedbackCategory.NEED_MORE_CONTEXT to translator.translate(TranslationKey.TarotSession.CardFeedbackNeedContext),
            )

            items(categories) { (category, label) ->
                val isSelected = selectedCategory == category
                Text(
                    text = label,
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = if (isSelected) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) AynvoraTheme.colors.Gold.copy(alpha = 0.2f)
                            else AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.7f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) AynvoraTheme.colors.Gold else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable {
                            selectedCategory = category
                            onFeedbackSelected(category)
                        }
                        .padding(horizontal = 10.sdp, vertical = 4.sdp),
                )
            }
        }
    }
}

/**
 * Follow-up question input and interactive conversation thread within active reading.
 */
@Composable
private fun TarotQuestionSection(
    session: TarotReadingSession?,
    reading: TarotReading,
    tarotRepository: TarotRepository,
    sessionRepository: TarotSessionRepository,
    questionEngine: TarotQuestionEngine,
    language: String,
    analyticsTracker: AnalyticsTracker,
    modifier: Modifier = Modifier,
    tarotViewModel: TarotViewModel = org.koin.compose.koinInject(),
) {
    if (session == null) return
    val translator = LocalAynvoraTranslator.current

    val coroutineScope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<TarotQuestion>>(emptyList()) }
    var answers by remember { mutableStateOf<Map<String, TarotQuestionAnswer>>(emptyMap()) }
    var clarifications by remember { mutableStateOf<Map<String, TarotClarificationCard>>(emptyMap()) }
    var questionInputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var isDrawingClarification by remember { mutableStateOf(false) }

    LaunchedEffect(session.id) {
        val qRes = sessionRepository.getQuestionsForSession(session.id)
        if (qRes is AynvoraResult.Success) {
            questions = qRes.value
            val ansMap = mutableMapOf<String, TarotQuestionAnswer>()
            val clarMap = mutableMapOf<String, TarotClarificationCard>()
            for (q in qRes.value) {
                val aRes = sessionRepository.getAnswer(q.id)
                if (aRes is AynvoraResult.Success && aRes.value != null) {
                    ansMap[q.id] = aRes.value!!
                }
                val cRes = sessionRepository.getClarificationCard(q.id)
                if (cRes is AynvoraResult.Success && cRes.value != null) {
                    clarMap[q.id] = cRes.value!!
                }
            }
            answers = ansMap
            clarifications = clarMap
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.sdp)) {
        Text(
            text = translator.translate(TranslationKey.TarotSession.AskQuestionTitle),
            style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
            color = AynvoraTheme.colors.GoldLight,
        )

        questions.sortedBy { it.sequenceNumber }.forEach { question ->
            val answer = answers[question.id]
            val clarCard = clarifications[question.id]

            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        AynvoraTheme.colors.CelestialBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                variant = AynvoraCardVariant.Elevated,
                containerColor = AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.9f),
            ) {
                Column(
                    modifier = Modifier.padding(14.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp)
                ) {
                    Text(
                        text = "Q${question.sequenceNumber}: ${question.questionText}",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )

                    if (answer != null) {
                        Text(
                            text = answer.summary,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = AynvoraTheme.colors.TextLight,
                        )
                        if (answer.interpretation.isNotBlank() && answer.interpretation != answer.summary) {
                            Text(
                                text = answer.interpretation,
                                style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                        }

                        if (answer.clarificationRecommended && clarCard == null && !question.clarificationUsed) {
                            AynvoraCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        AynvoraTheme.colors.Gold.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                variant = AynvoraCardVariant.Outlined,
                                containerColor = AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.6f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.sdp),
                                    verticalArrangement = Arrangement.spacedBy(6.sdp)
                                ) {
                                    Text(
                                        text = translator.translate(TranslationKey.TarotSession.ClarificationOffer),
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                        color = AynvoraTheme.colors.GoldLight,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                                        AynvoraButton(
                                            text = if (isDrawingClarification) {
                                                translator.translate(TranslationKey.TarotSession.DrawingClarification)
                                            } else {
                                                translator.translate(TranslationKey.TarotSession.DrawClarificationCard)
                                            },
                                            variant = AynvoraButtonVariant.Primary,
                                            enabled = !isDrawingClarification,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                if (isDrawingClarification) return@AynvoraButton
                                                isDrawingClarification = true
                                                coroutineScope.launch {
                                                    tarotViewModel.onEvent(TarotUiEvent.ClarificationAccepted)
                                                    val drawnIds =
                                                        reading.draws.map { it.card.id }.toSet()
                                                    val clarResult =
                                                        questionEngine.drawClarificationCard(
                                                            question = question,
                                                            deckCards = TarotStandardDeck.AllCards,
                                                            alreadyDrawnCardIds = drawnIds,
                                                        )
                                                    if (clarResult is AynvoraResult.Success) {
                                                        val card = clarResult.value
                                                        sessionRepository.saveClarificationCard(card)
                                                        val updatedQ =
                                                            question.copy(clarificationCardId = card.id)
                                                        sessionRepository.updateQuestion(updatedQ)

                                                        sessionRepository.appendTimelineEvent(
                                                            TarotTimelineEvent(
                                                                id = "evt_clar_draw_${System.currentTimeMillis()}",
                                                                readingId = reading.id,
                                                                sessionId = session.id,
                                                                questionId = question.id,
                                                                timestampEpochMs = System.currentTimeMillis(),
                                                                sequenceIndex = 10 + question.sequenceNumber,
                                                                eventType = TarotTimelineEventType.CLARIFICATION_CARD_DRAWN,
                                                                language = language,
                                                                metadataJson = "{\"cardId\":\"${card.draw.card.id}\"}",
                                                            )
                                                        )

                                                        tarotViewModel.onEvent(
                                                            TarotUiEvent.ClarificationDrawn(
                                                                cardId = card.draw.card.id,
                                                                orientation = card.draw.orientation.name,
                                                            )
                                                        )

                                                        val cardContents =
                                                            reading.draws.associate { draw ->
                                                                val content =
                                                                    (tarotRepository.getCardContent(
                                                                        draw.card.id,
                                                                        language
                                                                    ) as? AynvoraResult.Success)?.value
                                                                        ?: TarotCardContent(
                                                                            cardId = draw.card.id,
                                                                            language = language,
                                                                            title = draw.card.name,
                                                                            shortDescription = draw.card.name,
                                                                            keywords = listOf(draw.card.arcana.name),
                                                                            uprightMeaning = "Reflection on ${draw.card.name}",
                                                                            reversedMeaning = "Inward reflection on ${draw.card.name}",
                                                                        )
                                                                draw.card.id to content
                                                            }
                                                        val newAnsResult =
                                                            questionEngine.generateAnswer(
                                                                question = updatedQ,
                                                                primaryDraws = reading.draws,
                                                                cardContents = cardContents,
                                                                clarificationDraw = card.draw,
                                                                language = language,
                                                                allowClarificationRecommendation = false,
                                                            )
                                                        if (newAnsResult is AynvoraResult.Success) {
                                                            sessionRepository.saveAnswer(
                                                                newAnsResult.value
                                                            )
                                                            answers =
                                                                answers + (question.id to newAnsResult.value)
                                                            sessionRepository.appendTimelineEvent(
                                                                TarotTimelineEvent(
                                                                    id = "evt_ans_up_${System.currentTimeMillis()}",
                                                                    readingId = reading.id,
                                                                    sessionId = session.id,
                                                                    questionId = question.id,
                                                                    timestampEpochMs = System.currentTimeMillis(),
                                                                    sequenceIndex = 11 + question.sequenceNumber,
                                                                    eventType = TarotTimelineEventType.ANSWER_UPDATED,
                                                                    language = language,
                                                                )
                                                            )
                                                        }
                                                        clarifications =
                                                            clarifications + (question.id to card)
                                                        questions =
                                                            questions.map { if (it.id == question.id) updatedQ else it }
                                                    }
                                                    isDrawingClarification = false
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        if (clarCard != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.sdp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.sdp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(48.sdp)
                                        .aspectRatio(0.6f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            1.dp,
                                            AynvoraTheme.colors.Gold,
                                            RoundedCornerShape(4.dp)
                                        ),
                                ) {
                                    TarotCardImage(
                                        cardId = clarCard.draw.card.id,
                                        deckId = session.deckId,
                                        isThumbnail = true,
                                        isReversed = clarCard.draw.orientation == TarotCardOrientation.REVERSED,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = translator.translateWithArgs(
                                            TranslationKey.TarotSession.ClarificationCardWithName,
                                            "name" to clarCard.draw.card.name,
                                        ),
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                        color = AynvoraTheme.colors.GoldLight,
                                    )
                                    Text(
                                        text = if (clarCard.draw.orientation == TarotCardOrientation.UPRIGHT)
                                            translator.translate(TranslationKey.Tarot.UprightArrow)
                                        else
                                            translator.translate(TranslationKey.Tarot.ReversedArrow),
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                                        color = AynvoraTheme.colors.TextLightSecondary,
                                    )
                                }
                            }
                        }

                        var answerRated by remember { mutableStateOf<Int?>(null) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = translator.translate(TranslationKey.TarotSession.AnswerHelpfulPrompt),
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.sdp)) {
                                (1..5).forEach { star ->
                                    val isStarFilled = answerRated != null && star <= answerRated!!
                                    Text(
                                        text = if (isStarFilled) "★" else "☆",
                                        color = if (isStarFilled) AynvoraTheme.colors.Gold else AynvoraTheme.colors.TextLightSecondary,
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 16.ssp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                answerRated = star
                                                coroutineScope.launch {
                                                    sessionRepository.saveAnswerFeedback(
                                                        TarotAnswerFeedback(
                                                            id = "afb_${System.currentTimeMillis()}",
                                                            answerId = answer.id,
                                                            questionId = question.id,
                                                            readingId = reading.id,
                                                            sessionId = session.id,
                                                            starRating = star,
                                                            language = language,
                                                            submittedAtEpochMs = System.currentTimeMillis(),
                                                        )
                                                    )
                                                    sessionRepository.saveImprovementSignal(
                                                        AiImprovementSignal(
                                                            signalId = "sig_ans_${System.currentTimeMillis()}",
                                                            featureId = "TAROT",
                                                            sessionId = session.id,
                                                            readingId = reading.id,
                                                            questionId = question.id,
                                                            answerId = answer.id,
                                                            feedbackRating = star,
                                                            feedbackType = "ANSWER_STAR",
                                                            language = language,
                                                            promptVersion = answer.promptVersion,
                                                            contentVersion = answer.contentVersion,
                                                            timestampEpochMs = System.currentTimeMillis(),
                                                        )
                                                    )
                                                    tarotViewModel.onEvent(
                                                        TarotUiEvent.AnswerFeedbackSubmitted(star)
                                                    )
                                                }
                                            }
                                            .padding(2.sdp),
                                    )
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator(
                            color = AynvoraTheme.colors.Gold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.sdp),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AynvoraTheme.colors.CosmicNavy)
                .padding(12.sdp),
            verticalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            OutlinedTextField(
                value = questionInputText,
                onValueChange = { questionInputText = it },
                placeholder = {
                    Text(
                        text = translator.translate(TranslationKey.TarotSession.AskQuestionPlaceholder),
                        style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.6f),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AynvoraTheme.colors.TextLight,
                    unfocusedTextColor = AynvoraTheme.colors.TextLight,
                    focusedBorderColor = AynvoraTheme.colors.Gold,
                    unfocusedBorderColor = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.3f),
                    cursorColor = AynvoraTheme.colors.Gold,
                ),
                maxLines = 2,
            )

            if (isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    CircularProgressIndicator(
                        color = AynvoraTheme.colors.Gold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.sdp)
                    )
                    Text(
                        text = translator.translate(TranslationKey.TarotSession.AnalyzingCards),
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.GoldLight,
                    )
                }
            } else {
                AynvoraButton(
                    text = translator.translate(TranslationKey.TarotSession.SendQuestion),
                    variant = AynvoraButtonVariant.Primary,
                    enabled = questionInputText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val text = questionInputText.trim()
                        if (text.isBlank() || isGenerating) return@AynvoraButton
                        isGenerating = true
                        questionInputText = ""
                        coroutineScope.launch {
                            val qId = "q_${System.currentTimeMillis()}"
                            val newQuestion = TarotQuestion(
                                id = qId,
                                readingId = reading.id,
                                sessionId = session.id,
                                askedAtEpochMs = System.currentTimeMillis(),
                                questionText = text,
                                originalLanguage = language,
                                sequenceNumber = questions.size + 1,
                                cardEvidenceIds = reading.draws.map { it.card.id },
                            )
                            sessionRepository.saveQuestion(newQuestion)
                            questions = questions + newQuestion

                            sessionRepository.appendTimelineEvent(
                                TarotTimelineEvent(
                                    id = "evt_q_${System.currentTimeMillis()}",
                                    readingId = reading.id,
                                    sessionId = session.id,
                                    questionId = qId,
                                    timestampEpochMs = System.currentTimeMillis(),
                                    sequenceIndex = 10 + newQuestion.sequenceNumber,
                                    eventType = TarotTimelineEventType.QUESTION_ASKED,
                                    language = language,
                                    metadataJson = "{\"questionText\":\"${text.take(30)}\"}",
                                )
                            )
                            tarotViewModel.onEvent(
                                TarotUiEvent.QuestionSubmitted(text)
                            )

                            val cardContents = reading.draws.associate { draw ->
                                val content = (tarotRepository.getCardContent(
                                    draw.card.id,
                                    language
                                ) as? AynvoraResult.Success)?.value
                                    ?: TarotCardContent(
                                        cardId = draw.card.id,
                                        language = language,
                                        title = draw.card.name,
                                        shortDescription = draw.card.name,
                                        keywords = listOf(draw.card.arcana.name),
                                        uprightMeaning = "Reflection on ${draw.card.name}",
                                        reversedMeaning = "Inward reflection on ${draw.card.name}",
                                    )
                                draw.card.id to content
                            }

                            val ansResult = questionEngine.generateAnswer(
                                question = newQuestion,
                                primaryDraws = reading.draws,
                                cardContents = cardContents,
                                language = language,
                                allowClarificationRecommendation = true,
                            )

                            if (ansResult is AynvoraResult.Success) {
                                val answer = ansResult.value
                                sessionRepository.saveAnswer(answer)
                                answers = answers + (qId to answer)
                                val updatedQ = newQuestion.copy(
                                    answerId = answer.id,
                                    answerStatus = answer.status
                                )
                                sessionRepository.updateQuestion(updatedQ)

                                sessionRepository.appendTimelineEvent(
                                    TarotTimelineEvent(
                                        id = "evt_ans_${System.currentTimeMillis()}",
                                        readingId = reading.id,
                                        sessionId = session.id,
                                        questionId = qId,
                                        timestampEpochMs = System.currentTimeMillis(),
                                        sequenceIndex = 11 + newQuestion.sequenceNumber,
                                        eventType = if (answer.fallbackUsed)
                                            TarotTimelineEventType.AI_ANSWER_FALLBACK
                                        else
                                            TarotTimelineEventType.AI_ANSWER_GENERATED,
                                        language = language,
                                    )
                                )

                                tarotViewModel.onEvent(
                                    TarotUiEvent.AiAnswerGenerated(
                                        modelId = answer.modelId,
                                        promptVersion = answer.promptVersion,
                                        fallbackUsed = answer.fallbackUsed,
                                        language = language,
                                    )
                                )
                            }
                            isGenerating = false
                        }
                    },
                )
            }
        }
    }
}

/**
 * Full chronological reading timeline view with real deck card artwork.
 */
@Composable
private fun TarotTimelineScreen(
    sessionId: String,
    sessionRepository: TarotSessionRepository,
    tarotRepository: TarotRepository,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onBack: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    var timelineEvents by remember { mutableStateOf<List<TarotTimelineEvent>>(emptyList()) }
    var session by remember { mutableStateOf<TarotReadingSession?>(null) }
    var questions by remember { mutableStateOf<List<TarotQuestion>>(emptyList()) }
    var answers by remember { mutableStateOf<Map<String, TarotQuestionAnswer>>(emptyMap()) }
    var clarifications by remember { mutableStateOf<Map<String, TarotClarificationCard>>(emptyMap()) }
    var feedback by remember { mutableStateOf<TarotFeedback?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(sessionId) {
        isLoading = true
        val sessRes = sessionRepository.getSession(sessionId)
        if (sessRes is AynvoraResult.Success) {
            session = sessRes.value
        }
        val timelineRes = sessionRepository.getTimelineForSession(sessionId)
        if (timelineRes is AynvoraResult.Success) {
            timelineEvents = timelineRes.value
        }
        val qRes = sessionRepository.getQuestionsForSession(sessionId)
        if (qRes is AynvoraResult.Success) {
            questions = qRes.value
            val ansMap = mutableMapOf<String, TarotQuestionAnswer>()
            val clarMap = mutableMapOf<String, TarotClarificationCard>()
            for (q in qRes.value) {
                val aRes = sessionRepository.getAnswer(q.id)
                if (aRes is AynvoraResult.Success && aRes.value != null) {
                    ansMap[q.id] = aRes.value!!
                }
                val cRes = sessionRepository.getClarificationCard(q.id)
                if (cRes is AynvoraResult.Success && cRes.value != null) {
                    clarMap[q.id] = cRes.value!!
                }
            }
            answers = ansMap
            clarifications = clarMap
        }
        val fbRes = sessionRepository.getFeedback(sessionId)
        if (fbRes is AynvoraResult.Success) {
            feedback = fbRes.value
        }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack),
        contentPadding = PaddingValues(horizontal = 20.sdp, vertical = 16.sdp),
        verticalArrangement = Arrangement.spacedBy(14.sdp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = translator.translate(TranslationKey.TarotSession.TimelineTitle),
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.sdp)
                ) {
                    TarotLanguageSwitcher()
                    AynvoraButton(
                        text = translator.translate(TranslationKey.App.Back),
                        variant = AynvoraButtonVariant.Ghost,
                        onClick = onBack,
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.sdp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
                }
            }
        } else {
            session?.let { sess ->
                item {
                    TimelineCardItem(
                        title = translator.translate(TranslationKey.TarotSession.ReadingStarted),
                        timestampEpochMs = sess.startedAtEpochMs,
                        badge = translator.translate(TranslationKey.TarotSession.PrimarySpread),
                    ) {
                        Text(
                            text = translator.translateWithArgs(
                                TranslationKey.TarotSession.SpreadDeckFormat,
                                "spread" to sess.reading.spreadId,
                                "deck" to sess.deckId,
                            ),
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                        Spacer(Modifier.height(8.sdp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.sdp)) {
                            items(sess.reading.draws) { draw ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(90.sdp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.6f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(
                                                1.dp,
                                                AynvoraTheme.colors.Gold,
                                                RoundedCornerShape(6.dp)
                                            ),
                                    ) {
                                        TarotCardImage(
                                            cardId = draw.card.id,
                                            deckId = sess.deckId,
                                            isThumbnail = true,
                                            isReversed = draw.orientation == TarotCardOrientation.REVERSED,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                    Spacer(Modifier.height(4.sdp))
                                    Text(
                                        text = draw.card.name,
                                        style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                                        color = AynvoraTheme.colors.GoldLight,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(questions.sortedBy { it.sequenceNumber }) { question ->
                val answer = answers[question.id]
                val clarCard = clarifications[question.id]

                TimelineCardItem(
                    title = translator.translateWithArgs(
                        TranslationKey.TarotSession.QuestionNumbered,
                        "number" to question.sequenceNumber.toString(),
                    ),
                    timestampEpochMs = question.askedAtEpochMs,
                    badge = translator.translate(TranslationKey.TarotSession.FollowUpQuestion),
                ) {
                    Text(
                        text = "\"${question.questionText}\"",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                        color = AynvoraTheme.colors.TextLight,
                    )

                    if (answer != null) {
                        Spacer(Modifier.height(8.sdp))
                        AynvoraCard(
                            modifier = Modifier.fillMaxWidth(),
                            variant = AynvoraCardVariant.Elevated,
                            containerColor = AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.6f),
                        ) {
                            Column(
                                modifier = Modifier.padding(10.sdp),
                                verticalArrangement = Arrangement.spacedBy(4.sdp)
                            ) {
                                Text(
                                    text = translator.translate(TranslationKey.TarotSession.AiAnswer),
                                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                    color = AynvoraTheme.colors.Gold,
                                )
                                Text(
                                    text = answer.summary,
                                    style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                    color = AynvoraTheme.colors.TextLight,
                                )
                                if (answer.interpretation.isNotBlank() && answer.interpretation != answer.summary) {
                                    Text(
                                        text = answer.interpretation,
                                        style = AynvoraTheme.typography.body14.copy(fontSize = 11.ssp),
                                        color = AynvoraTheme.colors.TextLightSecondary,
                                    )
                                }
                            }
                        }
                    }

                    if (clarCard != null) {
                        Spacer(Modifier.height(8.sdp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.8f))
                                .padding(8.sdp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.sdp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(50.sdp)
                                    .aspectRatio(0.6f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        AynvoraTheme.colors.Gold,
                                        RoundedCornerShape(4.dp)
                                    ),
                            ) {
                                TarotCardImage(
                                    cardId = clarCard.draw.card.id,
                                    deckId = session?.deckId ?: "rider_waite_smith_standard",
                                    isThumbnail = true,
                                    isReversed = clarCard.draw.orientation == TarotCardOrientation.REVERSED,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            Column {
                                Text(
                                    text = translator.translateWithArgs(
                                        TranslationKey.TarotSession.ClarificationCardWithName,
                                        "name" to clarCard.draw.card.name,
                                    ),
                                    style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                    color = AynvoraTheme.colors.GoldLight,
                                )
                                Text(
                                    text = clarCard.reason,
                                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                                    color = AynvoraTheme.colors.TextLightSecondary,
                                )
                            }
                        }
                    }
                }
            }

            feedback?.let { fb ->
                item {
                    TimelineCardItem(
                        title = translator.translate(TranslationKey.TarotSession.FeedbackSubmitted),
                        timestampEpochMs = fb.submittedAtEpochMs,
                        badge = "${fb.starRating} ★",
                    ) {
                        Text(
                            text = "★".repeat(fb.starRating) + "☆".repeat(5 - fb.starRating),
                            color = AynvoraTheme.colors.Gold,
                            style = AynvoraTheme.typography.title18,
                        )
                        fb.optionalText?.let { opt ->
                            Spacer(Modifier.height(4.sdp))
                            Text(
                                text = "\"$opt\"",
                                style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                        }
                    }
                }
            }

            if (session?.status == TarotReadingStatus.SATISFIED || session?.status == TarotReadingStatus.COMPLETED) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.5f))
                            .border(
                                1.dp,
                                AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(14.sdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = translator.translate(TranslationKey.TarotSession.ReadingSatisfied),
                            color = AynvoraTheme.colors.GoldLight,
                            style = AynvoraTheme.typography.body14,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineCardItem(
    title: String,
    timestampEpochMs: Long,
    badge: String,
    content: @Composable () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.85f),
    ) {
        Column(
            modifier = Modifier.padding(14.sdp),
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
                Text(
                    text = badge,
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AynvoraTheme.colors.Gold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.sdp, vertical = 2.sdp),
                )
            }
            content()
        }
    }
}
