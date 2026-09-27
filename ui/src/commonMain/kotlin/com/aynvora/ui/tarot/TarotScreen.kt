package com.aynvora.ui.tarot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.tarot.PerformTarotReadingUseCase
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
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import kotlinx.coroutines.launch

/**
 * Foundational Tarot Reflection Screen.
 *
 * Implements:
 * 1. Explicit ethical disclaimer gate.
 * 2. Spread selection (Single Card, Three Card Timeline).
 * 3. Draw flow and card presentation (clearly separating card identity, orientation, and reflective perspective).
 * 4. Analytics emission via injected [AnalyticsTracker].
 * 5. Clean Architecture presentation boundary (consumes Domain use case and repository).
 */
@Composable
fun TarotScreen(
    tarotRepository: TarotRepository = org.koin.compose.koinInject(),
    analyticsTracker: AnalyticsTracker = org.koin.compose.koinInject(),
    modifier: Modifier = Modifier,
    language: String = "en",
    onClose: () -> Unit = {},
) {
    val translator = LocalAynvoraTranslator.current
    var hasAcceptedDisclaimer by remember { mutableStateOf(false) }
    var selectedSpread by remember { mutableStateOf(TarotSpread.SingleCard) }
    var currentReading by remember { mutableStateOf<TarotReading?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val readingUseCase = remember(tarotRepository, analyticsTracker) {
        PerformTarotReadingUseCase(
            tarotRepository = tarotRepository,
            analyticsTracker = analyticsTracker
        )
    }

    LaunchedEffect(Unit) {
        analyticsTracker.track(AnalyticsEvent.TarotOpened)
    }

    val primaryTextColor = AynvoraTheme.colors.TextLight
    val secondaryTextColor = AynvoraTheme.colors.TextLightSecondary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AynvoraTheme.colors.CosmicBlack)
            .padding(16.sdp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
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
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.Close),
                variant = AynvoraButtonVariant.Ghost,
                onClick = onClose,
            )
        }

        Spacer(modifier = Modifier.height(8.sdp))

        Text(
            text = translator.translate(TranslationKey.Tarot.Subtitle),
            style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
            color = secondaryTextColor,
        )

        Spacer(modifier = Modifier.height(16.sdp))

        // Step 1: Ethical & Non-Predictive Disclaimer Gate
        if (!hasAcceptedDisclaimer) {
            TarotDisclaimerCard(
                onAccept = {
                    analyticsTracker.track(AnalyticsEvent.TarotDisclaimerViewed)
                    hasAcceptedDisclaimer = true
                },
            )
            return@Column
        }

        // Step 2: Spread Selection
        Text(
            text = translator.translate(TranslationKey.Tarot.SelectSpread),
            style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
            color = AynvoraTheme.colors.GoldLight,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.sdp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            TarotSpread.StandardSpreads.forEach { spread ->
                val isSelected = spread.id == selectedSpread.id
                AynvoraButton(
                    text = if (spread.id == "single_card") {
                        translator.translate(TranslationKey.Tarot.SingleCard)
                    } else {
                        translator.translate(TranslationKey.Tarot.ThreeCards)
                    },
                    variant = if (isSelected) AynvoraButtonVariant.Primary else AynvoraButtonVariant.Secondary,
                    onClick = {
                        selectedSpread = spread
                        currentReading = null
                        analyticsTracker.track(AnalyticsEvent.TarotSpreadSelected(spread.id))
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.sdp))

        // Step 3: Draw Trigger with double-tap guard
        AynvoraButton(
            text = if (isLoading) {
                translator.translate(TranslationKey.Tarot.DrawingCards)
            } else if (currentReading == null) {
                translator.translate(TranslationKey.Tarot.DrawCards)
            } else {
                translator.translate(TranslationKey.Tarot.DrawAgain)
            },
            variant = AynvoraButtonVariant.Primary,
            enabled = !isLoading,
            onClick = {
                if (isLoading) return@AynvoraButton
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = readingUseCase.execute(
                        spread = selectedSpread,
                        allowReversed = true,
                        timestampEpochMs = 1_000_000L,
                    )
                    when (result) {
                        is AynvoraResult.Success -> {
                            currentReading = result.value
                        }

                        is AynvoraResult.Failure -> {
                            errorMessage = result.message
                        }
                    }
                    isLoading = false
                }
            },
        )

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.sdp))
            Text(
                text = error,
                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                color = AynvoraTheme.colors.Error,
            )
        }

        Spacer(modifier = Modifier.height(16.sdp))

        // Step 4: Reading Results Display
        currentReading?.let { reading ->
            reading.draws.forEach { draw ->
                TarotDrawResultCard(
                    draw = draw,
                    tarotRepository = tarotRepository,
                    analyticsTracker = analyticsTracker,
                    language = language,
                )
                Spacer(modifier = Modifier.height(12.sdp))
            }
        }
    }
}

@Composable
private fun TarotDisclaimerCard(
    onAccept: () -> Unit,
) {
    val translator = LocalAynvoraTranslator.current
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(
            modifier = Modifier.padding(16.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = translator.translate(TranslationKey.Tarot.DisclaimerTitle),
                style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(8.sdp))
            Text(
                text = translator.translate(TranslationKey.Tarot.DisclaimerBody),
                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                color = AynvoraTheme.colors.TextLightSecondary,
            )
            Spacer(modifier = Modifier.height(16.sdp))
            AynvoraButton(
                text = translator.translate(TranslationKey.Tarot.DisclosureAccept),
                variant = AynvoraButtonVariant.Primary,
                onClick = onAccept,
            )
        }
    }
}

@Composable
private fun TarotDrawResultCard(
    draw: TarotCardDraw,
    tarotRepository: TarotRepository,
    analyticsTracker: AnalyticsTracker,
    language: String,
) {
    val translator = LocalAynvoraTranslator.current
    var cardContent by remember(draw.card.id, language) { mutableStateOf<TarotCardContent?>(null) }

    LaunchedEffect(draw.card.id, language) {
        val result = tarotRepository.getCardContent(draw.card.id, language)
        if (result is AynvoraResult.Success) {
            cardContent = result.value
            analyticsTracker.track(AnalyticsEvent.TarotContentOpened(draw.card.id, language))
        }
    }

    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Outlined,
        containerColor = AynvoraTheme.colors.CosmicNavy,
        contentColor = AynvoraTheme.colors.TextLight,
    ) {
        Column(
            modifier = Modifier.padding(16.sdp),
        ) {
            // Position
            Text(
                text = draw.position.name.uppercase(),
                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                color = AynvoraTheme.colors.CelestialBlue,
            )
            Spacer(modifier = Modifier.height(4.sdp))

            // Card name and Orientation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = cardContent?.title ?: draw.card.name,
                    style = AynvoraTheme.typography.title20.copy(fontSize = 18.ssp),
                    color = AynvoraTheme.colors.GoldLight,
                )
                Text(
                    text = if (draw.orientation == TarotCardOrientation.UPRIGHT) {
                        translator.translate(TranslationKey.Tarot.Upright)
                    } else {
                        translator.translate(TranslationKey.Tarot.Reversed)
                    },
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                        AynvoraTheme.colors.Gold
                    else
                        AynvoraTheme.colors.TextLightSecondary,
                )
            }

            Spacer(modifier = Modifier.height(8.sdp))

            // Short description
            cardContent?.let { content ->
                Text(
                    text = content.shortDescription,
                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary,
                )

                Spacer(modifier = Modifier.height(8.sdp))

                // Meaning based on orientation
                val meaning = if (draw.orientation == TarotCardOrientation.UPRIGHT)
                    content.uprightMeaning
                else
                    content.reversedMeaning

                Text(
                    text = translator.translate(TranslationKey.Tarot.ReflectivePerspective),
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = AynvoraTheme.colors.CelestialBlue,
                )
                Spacer(modifier = Modifier.height(4.sdp))
                Text(
                    text = meaning,
                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                    color = AynvoraTheme.colors.TextLight,
                )

                // Keywords
                if (content.keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.sdp))
                    Text(
                        text = translator.translate(TranslationKey.Tarot.KeywordsLabel) + ": " + content.keywords.joinToString(
                            " • "
                        ),
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )
                }

                // Attribution
                Spacer(modifier = Modifier.height(8.sdp))
                Text(
                    text = "Source: ${content.sourceAttribution}",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                    color = AynvoraTheme.colors.TextLightSecondary.copy(alpha = 0.7f),
                )
            }
        }
    }
}
