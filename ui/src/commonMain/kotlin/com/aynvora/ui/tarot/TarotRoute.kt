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
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Navigation sealed class for the Tarot sub-flow
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Internal navigation destinations within the Tarot production experience.
 */
private sealed class TarotDestination {
    /** Non-predictive disclosure. Must be acknowledged before any reading. */
    data object Disclaimer : TarotDestination()

    /** Spread selection + deck selection + draw trigger. */
    data object Home : TarotDestination()

    /** Animated card reveal for a completed reading. */
    data class Reveal(val reading: TarotReading) : TarotDestination()

    /** Full reading result with card meanings, keywords, SLM explanation. */
    data class ReadingResult(val reading: TarotReading) : TarotDestination()

    /** Scrollable paginated history of past readings. */
    data object History : TarotDestination()

    /** Detail view for a single past reading. */
    data class HistoryDetail(val reading: TarotReading) : TarotDestination()

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
    data class TarotReport(val reading: TarotReading) : TarotDestination()

    /** Deck attribution and license information screen. */
    data object DeckLicenses : TarotDestination()
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
    pdfGenerator: ReportPdfGenerator? = null,
    shareService: ReportShareService? = null,
    resolver: ReportTextResolver? = null,
    modifier: Modifier = Modifier,
    language: String = "en",
    onClose: () -> Unit = {},
) {
    var destination by remember { mutableStateOf<TarotDestination>(TarotDestination.Disclaimer) }
    var selectedDeckId by remember { mutableStateOf("rider_waite_smith_standard") }
    val coroutineScope = rememberCoroutineScope()
    val readingUseCase = remember(tarotRepository, analyticsTracker) {
        PerformTarotReadingUseCase(
            tarotRepository = tarotRepository,
            analyticsTracker = analyticsTracker,
        )
    }

    LaunchedEffect(Unit) {
        analyticsTracker.track(AnalyticsEvent.TarotOpened)
    }

    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 20 } togetherWith
                    fadeOut(tween(200))
        },
        modifier = modifier.fillMaxSize(),
        label = "TarotNavigation",
    ) { dest ->
        when (dest) {
            is TarotDestination.Disclaimer -> TarotDisclaimerScreen(
                language = language,
                onAccept = {
                    analyticsTracker.track(AnalyticsEvent.TarotDisclaimerViewed)
                    destination = TarotDestination.Home
                },
                onClose = onClose,
            )

            is TarotDestination.Home -> TarotHomeScreen(
                language = language,
                analyticsTracker = analyticsTracker,
                readingUseCase = readingUseCase,
                selectedDeckId = selectedDeckId,
                onSelectDeck = { selectedDeckId = it },
                onReadingComplete = { reading ->
                    destination = TarotDestination.Reveal(reading)
                },
                onOpenHistory = { destination = TarotDestination.History },
                onOpenCardBrowser = {
                    destination = TarotDestination.CardBrowser(language, selectedDeckId)
                },
                onOpenDeckLicenses = { destination = TarotDestination.DeckLicenses },
                onClose = onClose,
            )

            is TarotDestination.Reveal -> TarotRevealScreen(
                reading = dest.reading,
                language = language,
                onRevealComplete = {
                    destination = TarotDestination.ReadingResult(dest.reading)
                },
            )

            is TarotDestination.ReadingResult -> TarotReadingResultScreen(
                reading = dest.reading,
                tarotRepository = tarotRepository,
                language = language,
                analyticsTracker = analyticsTracker,
                onNewReading = { destination = TarotDestination.Home },
                onViewReport = { reading ->
                    destination = TarotDestination.TarotReport(reading)
                },
                onClose = onClose,
            )

            is TarotDestination.History -> TarotHistoryScreen(
                tarotRepository = tarotRepository,
                language = language,
                analyticsTracker = analyticsTracker,
                onReadingSelected = { reading ->
                    destination = TarotDestination.HistoryDetail(reading)
                },
                onBack = { destination = TarotDestination.Home },
            )

            is TarotDestination.HistoryDetail -> TarotReadingResultScreen(
                reading = dest.reading,
                tarotRepository = tarotRepository,
                language = language,
                analyticsTracker = analyticsTracker,
                onNewReading = { destination = TarotDestination.Home },
                onViewReport = { reading ->
                    destination = TarotDestination.TarotReport(reading)
                },
                onClose = { destination = TarotDestination.History },
            )

            is TarotDestination.CardBrowser -> TarotCardBrowserScreen(
                tarotRepository = tarotRepository,
                initialDeckId = dest.deckId,
                language = dest.language,
                analyticsTracker = analyticsTracker,
                onCardSelected = { cardId, deckId ->
                    destination = TarotDestination.CardDetail(cardId, language, deckId)
                },
                onBack = { destination = TarotDestination.Home },
            )

            is TarotDestination.CardDetail -> TarotCardDetailScreen(
                cardId = dest.cardId,
                deckId = dest.deckId,
                tarotRepository = tarotRepository,
                language = dest.language,
                analyticsTracker = analyticsTracker,
                onBack = { destination = TarotDestination.CardBrowser(language, dest.deckId) },
            )

            is TarotDestination.DeckLicenses -> TarotDeckLicensesScreen(
                language = language,
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
                        language = language,
                        analyticsTracker = analyticsTracker,
                        pdfGenerator = pdfGenerator,
                        shareService = shareService,
                        onBack = { destination = TarotDestination.ReadingResult(dest.reading) },
                    )
                } else {
                    TarotReadingResultScreen(
                        reading = dest.reading,
                        tarotRepository = tarotRepository,
                        language = language,
                        analyticsTracker = analyticsTracker,
                        onNewReading = { destination = TarotDestination.Home },
                        onViewReport = {},
                        onClose = onClose,
                    )
                }
            }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(24.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.sdp))
            Text(
                text = "✦",
                style = AynvoraTheme.typography.display36.copy(fontSize = 48.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(Modifier.height(16.sdp))
            Text(
                text = if (language == "hi") "टैरो चिंतन" else "Tarot Reflection",
                style = AynvoraTheme.typography.display36.copy(fontSize = 30.ssp),
                color = AynvoraTheme.colors.Gold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = if (language == "hi")
                    "आत्म-चिंतन और मनन के लिए प्रतीकात्मक माध्यम"
                else
                    "Symbolic archetypes for self-reflection & mindfulness",
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
                        text = if (language == "hi")
                            "चिंतन एवं गैर-भविष्यवाणी प्रकटीकरण"
                        else
                            "Reflection & Mindful Use Disclosure",
                        style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(Modifier.height(12.sdp))
                    Text(
                        text = if (language == "hi") TarotDisclaimer.HINDI_TEXT else TarotDisclaimer.ENGLISH_TEXT,
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
                text = if (language == "hi") "मैं समझता हूँ — आगे बढ़ें" else "I Understand — Continue",
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onAccept,
            )
            AynvoraButton(
                text = if (language == "hi") "वापस जाएं" else "Go Back",
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
    onSelectDeck: (String) -> Unit,
    onReadingComplete: (TarotReading) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCardBrowser: () -> Unit,
    onOpenDeckLicenses: () -> Unit,
    onClose: () -> Unit,
) {
    var selectedSpread by remember { mutableStateOf(TarotSpread.SingleCard) }
    var isDrawing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                text = if (language == "hi") "टैरो चिंतन" else "Tarot Reflection",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 24.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            AynvoraButton(
                text = if (language == "hi") "बंद" else "Close",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onClose,
            )
        }

        Spacer(Modifier.height(4.sdp))
        Text(
            text = if (language == "hi")
                "आत्म-चिंतन के लिए प्रतीकात्मक माध्यम।"
            else
                "Symbolic archetypes for mindful self-reflection.",
            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
            color = AynvoraTheme.colors.TextLightSecondary,
        )

        Spacer(Modifier.height(20.sdp))

        // Quick-access row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            AynvoraButton(
                text = if (language == "hi") "पत्ता ब्राउज़र" else "Card Browser",
                variant = AynvoraButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
                onClick = onOpenCardBrowser,
            )
            AynvoraButton(
                text = if (language == "hi") "इतिहास" else "History",
                variant = AynvoraButtonVariant.Outlined,
                modifier = Modifier.weight(1f),
                onClick = onOpenHistory,
            )
        }

        Spacer(Modifier.height(8.sdp))
        AynvoraButton(
            text = if (language == "hi") "डेक लाइसेंस एवं स्रोत" else "Deck Licenses & Attribution",
            variant = AynvoraButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenDeckLicenses,
        )

        Spacer(Modifier.height(20.sdp))

        // Deck Selection
        Text(
            text = if (language == "hi") "डेक चुनें" else "Select Tarot Deck",
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
            text = if (language == "hi") "स्प्रेड चुनें" else "Select a Spread",
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
                        analyticsTracker.track(AnalyticsEvent.TarotSpreadSelected(spread.id))
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
                if (language == "hi") "पत्ते निकाले जा रहे हैं…" else "Drawing Cards…"
            } else {
                if (language == "hi") "पत्ते निकालें" else "Draw Cards"
            },
            variant = AynvoraButtonVariant.Primary,
            enabled = !isDrawing,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (isDrawing) return@AynvoraButton
                isDrawing = true
                errorMessage = null
                scope.launch {
                    val now = System.currentTimeMillis()
                    val result = readingUseCase.execute(
                        spread = selectedSpread,
                        allowReversed = true,
                        deckId = selectedDeckId,
                        timestampEpochMs = now,
                    )
                    when (result) {
                        is AynvoraResult.Success -> onReadingComplete(result.value)
                        is AynvoraResult.Failure -> errorMessage = result.message
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
            text = if (language == "hi")
                "* यह कोई भविष्यवाणी नहीं है। केवल चिंतन और आत्म-विश्लेषण हेतु।"
            else
                "* For reflective contemplation only. Not predictive.",
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
    language: String,
    onRevealComplete: () -> Unit,
) {
    var revealedCount by remember { mutableStateOf(0) }
    val totalCards = reading.draws.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(24.sdp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.sdp))
            Text(
                text = if (language == "hi") "पत्ते उभर रहे हैं" else "Your Cards Emerge",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 24.ssp),
                color = AynvoraTheme.colors.Gold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.sdp))
            Text(
                text = if (language == "hi")
                    "प्रत्येक पत्ते पर टैप करें"
                else
                    "Tap each card to reveal",
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
                text = "$revealedCount / $totalCards ${if (language == "hi") "प्रकट" else "revealed"}",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
            )
            AnimatedVisibility(visible = revealedCount >= totalCards) {
                AynvoraButton(
                    text = if (language == "hi") "अर्थ देखें" else "View Meanings",
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRevealComplete,
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
                    contentDescription = if (language == "hi") "टैप करें" else "Tap to reveal",
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
                    if (language == "hi") "↑ सीधा" else "↑ Upright"
                else
                    if (language == "hi") "↓ उल्टा" else "↓ Reversed",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                    AynvoraTheme.colors.Gold
                else
                    AynvoraTheme.colors.TextLightSecondary,
            )
        } else {
            Text(
                text = if (language == "hi") "प्रकट करने के लिए टैप करें" else "Tap to reveal",
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
    tarotRepository: TarotRepository,
    language: String,
    analyticsTracker: AnalyticsTracker,
    onNewReading: () -> Unit,
    onViewReport: (TarotReading) -> Unit,
    onClose: () -> Unit,
) {
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
                    text = if (language == "hi") "आपका पठन" else "Your Reading",
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                AynvoraButton(
                    text = if (language == "hi") "बंद" else "Close",
                    variant = AynvoraButtonVariant.Ghost,
                    onClick = onClose,
                )
            }
        }

        item {
            Text(
                text = if (language == "hi")
                    "* यह केवल चिंतन के लिए है, भविष्यवाणी नहीं।"
                else
                    "* For reflective contemplation only. Not a prediction.",
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
            )
        }

        item { Spacer(Modifier.height(8.sdp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                AynvoraButton(
                    text = if (language == "hi") "नया पठन" else "New Reading",
                    variant = AynvoraButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    onClick = onNewReading,
                )
                AynvoraButton(
                    text = if (language == "hi") "रिपोर्ट" else "Report",
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
) {
    var content by remember(draw.card.id, language) { mutableStateOf<TarotCardContent?>(null) }

    LaunchedEffect(draw.card.id, language) {
        val result = tarotRepository.getCardContent(draw.card.id, language)
        if (result is AynvoraResult.Success) {
            content = result.value
            analyticsTracker.track(AnalyticsEvent.TarotContentOpened(draw.card.id, language))
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
                            text = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                                if (language == "hi") "↑ सीधा" else "↑ Upright"
                            } else {
                                if (language == "hi") "↓ उल्टा" else "↓ Reversed"
                            },
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
                    text = if (language == "hi") "चिंतनशील दृष्टिकोण:" else "Reflective Perspective:",
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
                        text = (if (language == "hi") "मुख्य शब्द: " else "Keywords: ") +
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
) {
    val historyFlow = remember { tarotRepository.observeRecentReadings(limit = 30) }
    val readings by historyFlow.collectAsState(initial = emptyList())

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
                text = if (language == "hi") "पठन इतिहास" else "Reading History",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            AynvoraButton(
                text = if (language == "hi") "वापस" else "Back",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onBack,
            )
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
                        text = if (language == "hi") "कोई पठन नहीं" else "No readings yet",
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
                            analyticsTracker.track(AnalyticsEvent.TarotHistoryOpened)
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
) {
    var currentDeckId by remember { mutableStateOf(initialDeckId) }
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
                    text = if (language == "hi") "पत्ता ब्राउज़र" else "Card Browser",
                    style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                    color = AynvoraTheme.colors.Gold,
                )
                Text(
                    text = "${filtered.size} / 78 ${if (language == "hi") "पत्ते" else "cards"}",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )
            }
            AynvoraButton(
                text = if (language == "hi") "वापस" else "Back",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onBack,
            )
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
                            analyticsTracker.track(
                                AnalyticsEvent.TarotContentOpened(
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
            AynvoraButton(
                text = if (language == "hi") "वापस" else "Back",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onBack,
            )
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
                    title = if (language == "hi") "↑ सीधा — चिंतनशील परिप्रेक्ष्य" else "↑ Upright — Reflective Perspective",
                    meaning = c.uprightMeaning,
                    accentColor = AynvoraTheme.colors.Gold,
                )

                Spacer(Modifier.height(16.sdp))

                // Reversed meaning
                MeaningSection(
                    title = if (language == "hi") "↓ उल्टा — वैकल्पिक परिप्रेक्ष्य" else "↓ Reversed — Alternative Perspective",
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
                            text = if (language == "hi") "डेक स्रोत एवं लाइसेंस" else "Deck Provenance & License",
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
                text = if (language == "hi") "डेक लाइसेंस एवं श्रेय" else "Deck Licenses & Attribution",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            AynvoraButton(
                text = if (language == "hi") "वापस" else "Back",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onBack,
            )
        }

        Spacer(Modifier.height(8.sdp))
        Text(
            text = if (language == "hi")
                "AYNVORA केवल सत्यापित एवं कानूनी रूप से अनुमत वास्तविक कलाकृतियों का उपयोग करता है।"
            else
                "AYNVORA strictly uses verified, legally licensed, authentic tarot artwork. No AI generation, placeholders, or fake substitutes.",
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
    var reportState by remember { mutableStateOf<ReportGenerationResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val reportLanguage = if (language == "hi") ReportLanguage.HINDI else ReportLanguage.ENGLISH

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
                text = if (language == "hi") "टैरो रिपोर्ट" else "Tarot Report",
                style = AynvoraTheme.typography.headline28.copy(fontSize = 22.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            AynvoraButton(
                text = if (language == "hi") "वापस" else "Back",
                variant = AynvoraButtonVariant.Ghost,
                onClick = onBack,
            )
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
