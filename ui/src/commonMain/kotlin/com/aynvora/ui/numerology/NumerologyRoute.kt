package com.aynvora.ui.numerology

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aynvora.core.numerology.LoShuArrowStatus
import com.aynvora.core.numerology.LoShuGrid
import com.aynvora.core.numerology.NUMEROLOGY_DISCLAIMER
import com.aynvora.core.numerology.NumericalDisciplineType
import com.aynvora.core.numerology.NumerologyCalculationType
import com.aynvora.core.numerology.NumerologyInterpretationPackage
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportKeyValue
import com.aynvora.core.report.ReportParagraph
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.core.numerology.NumerologyAiQuestionCategory
import com.aynvora.core.numerology.NumerologyChatMessage
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import org.koin.compose.currentKoinScope

/**
 * Main consumer Composable route for the AYNVORA Numerology experience.
 *
 * Implements strict presentation requirements:
 * - Multi-tradition discovery
 * - Dynamic form inputs per ruleset
 * - Visualizations for Core Numbers, Lo Shu 3x3 Grid, Gematria, Abjad, Katapayadi, Nine Star Ki, Tarot Birth Cards
 * - Transparent calculation trace display
 * - Source provenance attribution
 * - Static reflective interpretations
 * - Cross-tradition comparison
 * - Offline history
 * - Accessible semantics
 * - Event-driven communication
 */
@Composable
fun NumerologyRoute(
    onClose: () -> Unit,
    viewModel: NumerologyViewModel? = null,
    modifier: Modifier = Modifier,
) {
    val koin = currentKoinScope()
    val vm = viewModel ?: remember(koin) {
        koin.getOrNull<NumerologyViewModel>() ?: NumerologyViewModel(
            numerologyRepository = koin.get(),
            historyRepository = koin.get(),
            eventDispatcher = koin.getOrNull(),
        )
    }

    val state by vm.uiState.collectAsState()
    val translator = LocalAynvoraTranslator.current
    val isDark = AynvoraTheme.isDark

    val bgColor = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    val primaryText = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryText =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.sdp, vertical = 12.sdp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ──────────────────────────────────────────────────────────
            NumerologyTopBar(
                onClose = {
                    vm.onEvent(NumerologyUiEvent.Close)
                    onClose()
                },
                onReset = { vm.onEvent(NumerologyUiEvent.ResetForm) },
                onRequestReport = { vm.onEvent(NumerologyUiEvent.RequestReport) },
                isDark = isDark,
                primaryText = primaryText,
            )

            Spacer(modifier = Modifier.height(10.sdp))

            // ── Navigation Tabs ──────────────────────────────────────────────────
            NumerologyTabBar(
                activeTab = state.activeTab,
                onTabSelected = { vm.onEvent(NumerologyUiEvent.SelectTab(it)) },
                isDark = isDark,
            )

            Spacer(modifier = Modifier.height(12.sdp))

            // ── Active Tab Content ───────────────────────────────────────────────
            when (state.activeTab) {
                NumerologyTab.CALCULATE -> {
                    NumerologyCalculateTab(
                        state = state,
                        onEvent = vm::onEvent,
                        isDark = isDark,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }

                NumerologyTab.COMPARE -> {
                    NumerologyCompareTab(
                        state = state,
                        onEvent = vm::onEvent,
                        isDark = isDark,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }

                NumerologyTab.HISTORY -> {
                    NumerologyHistoryTab(
                        state = state,
                        onEvent = vm::onEvent,
                        isDark = isDark,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }
            }
        }

        // Report Overlay Dialog
        state.generatedReport?.let { reportDoc ->
            NumerologyReportOverlay(
                report = reportDoc,
                onDismiss = { vm.onEvent(NumerologyUiEvent.DismissReport) },
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // AI Conversational Explanation Modal
        if (state.showAiModal) {
            NumerologyAiConversationModal(
                state = state,
                onDismiss = { vm.onEvent(NumerologyUiEvent.DismissAiExplanation) },
                onQuestionInputChanged = { vm.onEvent(NumerologyUiEvent.UpdateAiQuestionInput(it)) },
                onSubmitQuestion = { q, cat ->
                    vm.onEvent(
                        NumerologyUiEvent.SubmitAiQuestion(
                            q,
                            cat
                        )
                    )
                },
                onClearConversation = { vm.onEvent(NumerologyUiEvent.ClearAiConversation) },
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }
    }
}

// ============================================================================
// TOP BAR & TAB BAR
// ============================================================================

@Composable
private fun NumerologyTopBar(
    onClose: () -> Unit,
    onReset: () -> Unit,
    onRequestReport: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.sdp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .clickable(onClick = onClose)
                    .semantics { contentDescription = "Close Numerology" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✕",
                    color = primaryText,
                    fontSize = 16.ssp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.sdp))
            Column {
                Text(
                    text = "AYNVORA Numerology",
                    style = AynvoraTheme.typography.title20,
                    color = AynvoraTheme.colors.Gold,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Multi-Tradition • Deterministic • Explainable",
                    style = AynvoraTheme.typography.caption12,
                    color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AynvoraTheme.colors.Gold.copy(alpha = 0.15f))
                    .clickable(onClick = onRequestReport)
                    .padding(horizontal = 10.sdp, vertical = 6.sdp)
                    .semantics { contentDescription = "Generate Numerology Report" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Report",
                    color = AynvoraTheme.colors.Gold,
                    fontSize = 12.ssp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .clickable(onClick = onReset)
                    .padding(horizontal = 10.sdp, vertical = 6.sdp)
                    .semantics { contentDescription = "Reset Form" },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Reset", color = primaryText, fontSize = 12.ssp)
            }
        }
    }
}

@Composable
private fun NumerologyTabBar(
    activeTab: NumerologyTab,
    onTabSelected: (NumerologyTab) -> Unit,
    isDark: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
            .padding(4.sdp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        NumerologyTab.entries.forEach { tab ->
            val isSelected = activeTab == tab
            val tabLabel = when (tab) {
                NumerologyTab.CALCULATE -> "Calculate"
                NumerologyTab.COMPARE -> "Compare Methods"
                NumerologyTab.HISTORY -> "History"
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) AynvoraTheme.colors.Gold
                        else Color.Transparent
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.sdp)
                    .semantics { contentDescription = "Tab $tabLabel" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tabLabel,
                    fontSize = 13.ssp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AynvoraTheme.colors.CosmicBlack else (if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark),
                )
            }
        }
    }
}

// ============================================================================
// CALCULATE TAB
// ============================================================================

@Composable
private fun NumerologyCalculateTab(
    state: NumerologyUiState,
    onEvent: (NumerologyUiEvent) -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.sdp),
    ) {
        // Educational Intro Banner
        item(key = "intro_banner") {
            AynvoraCard(variant = AynvoraCardVariant.Outlined) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = "12 Distinct Historical Traditions",
                        style = AynvoraTheme.typography.body14,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Every tradition employs its own ancient ruleset, authority sources, and reduction methods. AYNVORA never mixes traditions into one unexplained score.",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
            }
        }

        // Tradition / Ruleset Selector
        item(key = "tradition_selector") {
            TraditionSelectorCard(
                selectedRuleset = state.selectedRuleset,
                onSelectRuleset = { onEvent(NumerologyUiEvent.SelectRuleset(it)) },
                isDark = isDark,
                primaryText = primaryText,
            )
        }

        // Dynamic Input Form
        item(key = "dynamic_input_form") {
            DynamicInputFormCard(
                state = state,
                onEvent = onEvent,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // Error State
        if (state.calculationError != null) {
            item(key = "calc_error") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                        .padding(12.sdp),
                ) {
                    Text(
                        text = state.calculationError,
                        color = Color(0xFFE53935),
                        fontSize = 12.ssp,
                    )
                }
            }
        }

        // Results Section
        state.currentResult?.let { result ->
            item(key = "results_overview") {
                ResultsOverviewSection(
                    result = result,
                    ruleset = state.selectedRuleset,
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            // Traditional Interpretation Card
            item(key = "interpretation_card") {
                TraditionalInterpretationCard(
                    result = result,
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            // Calculation Transparency / Trace
            item(key = "trace_card") {
                CalculationTraceCard(
                    result = result,
                    showTrace = state.showTrace,
                    onToggleTrace = { onEvent(NumerologyUiEvent.ToggleTrace(null)) },
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            // Source Provenance Transparency
            item(key = "source_card") {
                SourceProvenanceCard(
                    ruleset = state.selectedRuleset,
                    showSource = state.showSource,
                    onToggleSource = { onEvent(NumerologyUiEvent.ToggleSourceTransparency) },
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            // AI Future Boundary Card
            item(key = "ai_boundary_card") {
                AiFutureBoundaryCard(
                    onExplainClicked = { onEvent(NumerologyUiEvent.RequestAiExplanation) },
                    isDark = isDark,
                    primaryText = primaryText,
                    secondaryText = secondaryText,
                )
            }

            // Mandatory Disclaimer
            item(key = "disclaimer") {
                AynvoraCard(variant = AynvoraCardVariant.Elevated) {
                    Column(modifier = Modifier.padding(14.sdp)) {
                        Text(
                            text = "Contemplative Reflection Notice",
                            style = AynvoraTheme.typography.caption12,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(4.sdp))
                        Text(
                            text = NUMEROLOGY_DISCLAIMER,
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TRADITION SELECTOR
// ============================================================================

@Composable
private fun TraditionSelectorCard(
    selectedRuleset: NumerologyRuleset,
    onSelectRuleset: (String) -> Unit,
    isDark: Boolean,
    primaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Select Tradition & Ruleset",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))
            Text(
                text = "Currently active: ${selectedRuleset.name}",
                style = AynvoraTheme.typography.caption12,
                color = primaryText,
            )
            Spacer(modifier = Modifier.height(10.sdp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                items(NumerologyRuleset.ALL_RULESETS) { ruleset ->
                    val isCurrent = ruleset.id == selectedRuleset.id
                    val disciplineTag = when (ruleset.id) {
                        NumerologyRuleset.LO_SHU_CLASSICAL_V1.id -> "MAGIC SQUARE"
                        NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
                        NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> "GEMATRIA"

                        NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
                        NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> "ABJAD"

                        NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
                        NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id,
                        NumerologyRuleset.TAROT_BIRTH_CARD_V1.id -> "SPECIALIZED"

                        else -> "NUMEROLOGY"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrent) AynvoraTheme.colors.Gold
                                else (if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                            )
                            .clickable { onSelectRuleset(ruleset.id) }
                            .padding(horizontal = 12.sdp, vertical = 8.sdp)
                            .semantics { contentDescription = "Ruleset ${ruleset.name}" },
                    ) {
                        Column {
                            Text(
                                text = disciplineTag,
                                fontSize = 9.ssp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.7f) else AynvoraTheme.colors.Gold,
                            )
                            Spacer(modifier = Modifier.height(2.sdp))
                            Text(
                                text = ruleset.name,
                                fontSize = 12.ssp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) AynvoraTheme.colors.CosmicBlack else primaryText,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DYNAMIC INPUT FORM
// ============================================================================

@Composable
private fun DynamicInputFormCard(
    state: NumerologyUiState,
    onEvent: (NumerologyUiEvent) -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val ruleset = state.selectedRuleset
    val isScriptBased = ruleset.id in setOf(
        NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
        NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id,
        NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
        NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id,
        NumerologyRuleset.INDIAN_KATAPAYADI_V1.id,
    )
    val requiresName = ruleset.id in setOf(
        NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
        NumerologyRuleset.AGRIPPAN_OCCULT_V1.id,
    )
    val isNineStarKi = ruleset.id == NumerologyRuleset.CHINESE_NINE_STAR_KI_V1.id

    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Required Inputs (${ruleset.name})",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(8.sdp))

            if (isScriptBased) {
                val scriptPrompt = when (ruleset.id) {
                    NumerologyRuleset.HEBREW_GEMATRIA_CLASSICAL_V1.id,
                    NumerologyRuleset.HEBREW_MISPAR_GADOL_V1.id -> "Enter Hebrew text (e.g. שלום)"

                    NumerologyRuleset.ARABIC_ABJAD_MASHRIQI_V1.id,
                    NumerologyRuleset.ARABIC_ABJAD_MAGHRIBI_V1.id -> "Enter Arabic text (e.g. بسم الله)"

                    NumerologyRuleset.INDIAN_KATAPAYADI_V1.id -> "Enter Indic/Devanagari text (e.g. गोपीभाग्यमधुव्रात)"
                    else -> "Enter text"
                }

                Text(text = scriptPrompt, fontSize = 12.ssp, color = secondaryText)
                Spacer(modifier = Modifier.height(4.sdp))
                StyledTextInput(
                    value = state.scriptText,
                    onValueChange = { onEvent(NumerologyUiEvent.UpdateScriptText(it)) },
                    placeholder = "Input text here...",
                    isDark = isDark,
                    primaryText = primaryText,
                )
            } else {
                // Date of Birth row
                Text(
                    text = "Date of Birth (DD / MM / YYYY)",
                    fontSize = 12.ssp,
                    color = secondaryText
                )
                Spacer(modifier = Modifier.height(4.sdp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.sdp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        StyledTextInput(
                            value = state.day,
                            onValueChange = { onEvent(NumerologyUiEvent.UpdateDay(it)) },
                            placeholder = "Day",
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StyledTextInput(
                            value = state.month,
                            onValueChange = { onEvent(NumerologyUiEvent.UpdateMonth(it)) },
                            placeholder = "Month",
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        StyledTextInput(
                            value = state.year,
                            onValueChange = { onEvent(NumerologyUiEvent.UpdateYear(it)) },
                            placeholder = "Year",
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                }

                // Name input for name-supporting traditions
                if (requiresName) {
                    Spacer(modifier = Modifier.height(10.sdp))
                    Text(
                        text = "Full Name (Latin script)",
                        fontSize = 12.ssp,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    StyledTextInput(
                        value = state.name,
                        onValueChange = { onEvent(NumerologyUiEvent.UpdateName(it)) },
                        placeholder = "e.g. ISHANT",
                        isDark = isDark,
                        primaryText = primaryText,
                    )
                }

                // Time for Nine Star Ki
                if (isNineStarKi) {
                    Spacer(modifier = Modifier.height(10.sdp))
                    Text(
                        text = "Birth Time (for solar Li Chun transition boundary)",
                        fontSize = 12.ssp,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StyledTextInput(
                                value = state.hour.toString(),
                                onValueChange = {
                                    val h = it.toIntOrNull() ?: 12
                                    onEvent(NumerologyUiEvent.UpdateTime(h, state.minute))
                                },
                                placeholder = "Hour (0..23)",
                                isDark = isDark,
                                primaryText = primaryText,
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StyledTextInput(
                                value = state.minute.toString(),
                                onValueChange = {
                                    val m = it.toIntOrNull() ?: 0
                                    onEvent(NumerologyUiEvent.UpdateTime(state.hour, m))
                                },
                                placeholder = "Minute (0..59)",
                                isDark = isDark,
                                primaryText = primaryText,
                            )
                        }
                    }
                }
            }

            // Validation Error message
            if (state.validationErrorKey != null) {
                Spacer(modifier = Modifier.height(8.sdp))
                Text(
                    text = state.validationErrorKey,
                    color = Color(0xFFE53935),
                    fontSize = 12.ssp,
                )
            }

            Spacer(modifier = Modifier.height(14.sdp))
            AynvoraButton(
                text = if (state.isCalculating) "Calculating..." else "Calculate Results",
                onClick = { onEvent(NumerologyUiEvent.Calculate) },
                variant = AynvoraButtonVariant.Primary,
                enabled = !state.isCalculating,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StyledTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isDark: Boolean,
    primaryText: Color,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, fontSize = 13.ssp) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold,
            unfocusedContainerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold,
            focusedTextColor = primaryText,
            unfocusedTextColor = primaryText,
            focusedIndicatorColor = AynvoraTheme.colors.Gold,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

// ============================================================================
// RESULTS OVERVIEW & SPECIALIZED VISUALIZATIONS
// ============================================================================

@Composable
private fun ResultsOverviewSection(
    result: NumerologyResult,
    ruleset: NumerologyRuleset,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.sdp)) {
        // 1. Classical Core Numbers (Pythagorean, Chaldean, Indian, Agrippan)
        if (result.profile.radical != null || result.profile.destiny != null || result.profile.nameNumber != null) {
            CoreNumbersGridCard(
                result = result,
                ruleset = ruleset,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 2. Lo Shu 3x3 Magic Square
        result.loShuResult?.let { loShu ->
            LoShuGridVisualizerCard(
                loShuResult = loShu,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 3. Hebrew Gematria
        result.gematriaResult?.let { gematria ->
            GematriaVisualizerCard(
                gematria = gematria,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 4. Arabic Abjad
        result.abjadResult?.let { abjad ->
            AbjadVisualizerCard(
                abjad = abjad,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 5. Indian Katapayadi
        result.katapayadiResult?.let { katapayadi ->
            KatapayadiVisualizerCard(
                katapayadi = katapayadi,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 6. Chinese Nine Star Ki
        result.nineStarKiResult?.let { nsk ->
            NineStarKiVisualizerCard(
                nsk = nsk,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }

        // 7. Tarot Birth Cards
        result.tarotBirthCardResult?.let { tarot ->
            TarotBirthCardVisualizerCard(
                tarot = tarot,
                isDark = isDark,
                primaryText = primaryText,
                secondaryText = secondaryText,
            )
        }
    }
}

@Composable
private fun CoreNumbersGridCard(
    result: NumerologyResult,
    ruleset: NumerologyRuleset,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "${ruleset.name} — Core Numbers",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(10.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp)
            ) {
                result.profile.radical?.let { rad ->
                    NumberPill(
                        label = if (ruleset.id == NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id) "Moolank (Day)" else "Radical Number",
                        value = rad.radicalValue.toString(),
                        planet = rad.rulingPlanet,
                        modifier = Modifier.weight(1f),
                        isDark = isDark,
                        primaryText = primaryText,
                    )
                }
                result.profile.destiny?.let { des ->
                    NumberPill(
                        label = if (ruleset.id == NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id) "Bhagyank (Destiny)" else "Life Path Number",
                        value = des.destinyValue.toString(),
                        planet = des.rulingPlanet,
                        modifier = Modifier.weight(1f),
                        isDark = isDark,
                        primaryText = primaryText,
                    )
                }
            }

            if (result.profile.nameNumber != null || result.profile.soulUrge != null) {
                Spacer(modifier = Modifier.height(10.sdp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.sdp)
                ) {
                    result.profile.nameNumber?.let { nam ->
                        NumberPill(
                            label = if (ruleset.id == NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id) "Namank (Name)" else "Expression Number",
                            value = nam.nameValue.toString(),
                            planet = nam.rulingPlanet,
                            modifier = Modifier.weight(1f),
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                    result.profile.soulUrge?.let { su ->
                        NumberPill(
                            label = "Soul Urge (Vowels)",
                            value = su.soulUrgeValue.toString(),
                            planet = su.rulingPlanet,
                            modifier = Modifier.weight(1f),
                            isDark = isDark,
                            primaryText = primaryText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPill(
    label: String,
    value: String,
    planet: String,
    modifier: Modifier = Modifier,
    isDark: Boolean,
    primaryText: Color,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
            .padding(12.sdp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.ssp,
                color = AynvoraTheme.colors.Gold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(text = value, fontSize = 28.ssp, fontWeight = FontWeight.Bold, color = primaryText)
            Spacer(modifier = Modifier.height(2.sdp))
            Text(text = planet, fontSize = 11.ssp, color = primaryText.copy(alpha = 0.7f))
        }
    }
}

// ============================================================================
// LO SHU 3x3 MAGIC SQUARE VISUALIZER
// ============================================================================

@Composable
private fun LoShuGridVisualizerCard(
    loShuResult: com.aynvora.core.numerology.LoShuResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val grid = loShuResult.grid
    val freqMap = grid.frequencies.associate { it.digit to it.count }

    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(
            modifier = Modifier
                .padding(14.sdp)
                .semantics {
                    contentDescription =
                        "Lo Shu 3 by 3 Magic Square grid. Present digits: ${grid.presentDigits.joinToString()}, Missing digits: ${grid.missingDigits.joinToString()}"
                },
        ) {
            Text(
                text = "Lo Shu 3×3 Magic Square Grid",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(
                text = "Classical Luoshu coordinate placement. Numbers 1..9 frequency from birth date.",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
            Spacer(modifier = Modifier.height(12.sdp))

            // 3x3 Grid
            // Row 1: 4, 9, 2
            // Row 2: 3, 5, 7
            // Row 3: 8, 1, 6
            val rows = listOf(
                listOf(4, 9, 2),
                listOf(3, 5, 7),
                listOf(8, 1, 6),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .padding(8.sdp),
                verticalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                rows.forEach { rowDigits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.sdp),
                    ) {
                        rowDigits.forEach { digit ->
                            val count = freqMap[digit] ?: 0
                            val isPresent = count > 0

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.sdp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isPresent) AynvoraTheme.colors.Gold.copy(alpha = 0.25f)
                                        else (if (isDark) AynvoraTheme.colors.CosmicBlack.copy(alpha = 0.5f) else Color.White.copy(
                                            alpha = 0.6f
                                        ))
                                    )
                                    .border(
                                        1.dp,
                                        if (isPresent) AynvoraTheme.colors.Gold else Color.Gray.copy(
                                            alpha = 0.3f
                                        ),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .semantics {
                                        contentDescription = "Cell $digit, count $count"
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = digit.toString(),
                                        fontSize = 18.ssp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPresent) AynvoraTheme.colors.Gold else primaryText.copy(
                                            alpha = 0.3f
                                        ),
                                    )
                                    if (isPresent) {
                                        Text(
                                            text = if (count > 1) "×$count" else "✓",
                                            fontSize = 11.ssp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryText,
                                        )
                                    } else {
                                        Text(
                                            text = "—",
                                            fontSize = 11.ssp,
                                            color = primaryText.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.sdp))

            // Arrow Planes Analysis
            Text(
                text = "Planes & Arrow Patterns",
                style = AynvoraTheme.typography.body14,
                fontWeight = FontWeight.Bold,
                color = primaryText,
            )
            Spacer(modifier = Modifier.height(6.sdp))

            val strengthArrows =
                grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_STRENGTH }
            val weaknessArrows =
                grid.arrows.filter { it.status == LoShuArrowStatus.ARROW_OF_WEAKNESS }

            if (strengthArrows.isNotEmpty()) {
                Text(
                    text = "Arrows of Strength: " + strengthArrows.joinToString { "${it.planeName} (${it.strengthName})" },
                    fontSize = 12.ssp,
                    color = Color(0xFF4CAF50),
                )
            }
            if (weaknessArrows.isNotEmpty()) {
                Text(
                    text = "Arrows of Challenge: " + weaknessArrows.joinToString { "${it.planeName} (${it.weaknessName})" },
                    fontSize = 12.ssp,
                    color = Color(0xFFFF9800),
                )
            }
        }
    }
}

// ============================================================================
// GEMATRIA VISUALIZER
// ============================================================================

@Composable
private fun GematriaVisualizerCard(
    gematria: com.aynvora.core.numerology.GematriaResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Hebrew Gematria (${gematria.variant})",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Absolute Value", fontSize = 11.ssp, color = secondaryText)
                        Text(
                            text = gematria.absoluteValue.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Mispar Katan (Root)", fontSize = 11.ssp, color = secondaryText)
                        Text(
                            text = gematria.reducedValue.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.sdp))
            Text(
                text = "Letter-by-Letter Values",
                fontSize = 12.ssp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(6.sdp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.sdp)) {
                items(gematria.letterValues) { (char, valNum) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                            .padding(horizontal = 8.sdp, vertical = 6.sdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = char,
                                fontSize = 16.ssp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = valNum.toString(),
                                fontSize = 11.ssp,
                                color = AynvoraTheme.colors.Gold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// ABJAD VISUALIZER
// ============================================================================

@Composable
private fun AbjadVisualizerCard(
    abjad: com.aynvora.core.numerology.AbjadResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Arabic Hisab al-Jummal (${abjad.variant})",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Jummal Kabir (Major)",
                            fontSize = 11.ssp,
                            color = secondaryText
                        )
                        Text(
                            text = abjad.kabirValue.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Jummal Saghir (Minor)",
                            fontSize = 11.ssp,
                            color = secondaryText
                        )
                        Text(
                            text = abjad.saghirValue.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.sdp))
            Text(
                text = "Letter Values",
                fontSize = 12.ssp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(6.sdp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.sdp)) {
                items(abjad.letterValues) { (char, valNum) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                            .padding(horizontal = 8.sdp, vertical = 6.sdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = char,
                                fontSize = 16.ssp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = valNum.toString(),
                                fontSize = 11.ssp,
                                color = AynvoraTheme.colors.Gold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// KATAPAYADI VISUALIZER
// ============================================================================

@Composable
private fun KatapayadiVisualizerCard(
    katapayadi: com.aynvora.core.numerology.KatapayadiResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Indian Katapayadi System (कटपयादि)",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))
            Text(
                text = "Rule: 'Ankanam vamato gatih' (Digits read from right to left).",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
            Spacer(modifier = Modifier.height(10.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Forward Digits", fontSize = 11.ssp, color = secondaryText)
                        Text(
                            text = katapayadi.digitSequence,
                            fontSize = 20.ssp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Reversed Number", fontSize = 11.ssp, color = secondaryText)
                        Text(
                            text = katapayadi.finalNumber,
                            fontSize = 20.ssp,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.sdp))
            Text(
                text = "Phoneme Sequence",
                fontSize = 12.ssp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(6.sdp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.sdp)) {
                items(katapayadi.phonemes) { phoneme ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                            .padding(horizontal = 8.sdp, vertical = 6.sdp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = phoneme.akshara,
                                fontSize = 16.ssp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = phoneme.digit.toString(),
                                fontSize = 11.ssp,
                                color = AynvoraTheme.colors.Gold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// NINE STAR KI VISUALIZER
// ============================================================================

@Composable
private fun NineStarKiVisualizerCard(
    nsk: com.aynvora.core.numerology.NineStarKiResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Chinese Nine Star Ki (九星気学)",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))
            Text(
                text = "Calculated via solar Li Chun boundary (315° solar longitude).",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
            Spacer(modifier = Modifier.height(10.sdp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .padding(12.sdp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Principal Star",
                        fontSize = 12.ssp,
                        color = AynvoraTheme.colors.Gold
                    )
                    Spacer(modifier = Modifier.height(2.sdp))
                    Text(
                        text = "${nsk.principalStar.starNumber} — ${nsk.principalStar.starName}",
                        fontSize = 22.ssp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Trigram: ${nsk.principalStar.trigram} • Element: ${nsk.principalStar.element} • Direction: ${nsk.principalStar.direction}",
                        fontSize = 12.ssp,
                        color = secondaryText,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Applicable Solar Year: ${nsk.solarYear}",
                        fontSize = 11.ssp,
                        color = primaryText.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAROT BIRTH CARDS VISUALIZER
// ============================================================================

@Composable
private fun TarotBirthCardVisualizerCard(
    tarot: com.aynvora.core.numerology.TarotBirthCardResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Text(
                text = "Tarot Birth Cards (Major Arcana)",
                style = AynvoraTheme.typography.title18,
                fontWeight = FontWeight.Bold,
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(6.sdp))
            Text(
                text = "Authority: Mary K. Greer, 'Tarot for Your Self' (1984).",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
            Spacer(modifier = Modifier.height(10.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(12.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Personality Card", fontSize = 11.ssp, color = secondaryText)
                        Spacer(modifier = Modifier.height(2.sdp))
                        Text(
                            text = tarot.personalityCardNumber.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = tarot.personalityCardName,
                            fontSize = 12.ssp,
                            fontWeight = FontWeight.Medium,
                            color = AynvoraTheme.colors.Gold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(12.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Soul Card", fontSize = 11.ssp, color = secondaryText)
                        Spacer(modifier = Modifier.height(2.sdp))
                        Text(
                            text = tarot.soulCardNumber.toString(),
                            fontSize = 24.ssp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = tarot.soulCardName,
                            fontSize = 12.ssp,
                            fontWeight = FontWeight.Medium,
                            color = AynvoraTheme.colors.Gold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (tarot.shadowCardNumber != null && tarot.shadowCardName != null) {
                Spacer(modifier = Modifier.height(8.sdp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .padding(10.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Shadow / Teacher Card: ${tarot.shadowCardNumber} — ${tarot.shadowCardName}",
                        fontSize = 12.ssp,
                        color = primaryText,
                    )
                }
            }
        }
    }
}

// ============================================================================
// TRADITIONAL INTERPRETATION
// ============================================================================

@Composable
private fun TraditionalInterpretationCard(
    result: NumerologyResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val translator = LocalAynvoraTranslator.current
    val bundle = remember(result) {
        NumerologyInterpretationPackage.resolveInterpretations(result)
    }
    val primaryInterp = bundle.primaryInterpretation ?: return
    var showSourceDetails by remember { mutableStateOf(false) }

    AynvoraCard(
        variant = AynvoraCardVariant.Elevated,
        modifier = Modifier.semantics {
            contentDescription = "Traditional Reflective Interpretations Card"
        },
    ) {
        Column(modifier = Modifier.padding(14.sdp)) {
            // Header: Title and Ruleset Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = translator.resolve(primaryInterp.titleKey),
                    style = AynvoraTheme.typography.title18,
                    fontWeight = FontWeight.Bold,
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Spacer(modifier = Modifier.height(4.sdp))

            // Non-personality notice if applicable
            if (!bundle.isPersonalityInterpretation && bundle.nonPersonalityNoticeKey != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isDark) AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.7f) else AynvoraTheme.colors.SoftGold.copy(
                                alpha = 0.5f
                            )
                        )
                        .padding(8.sdp),
                ) {
                    Text(
                        text = translator.resolve(bundle.nonPersonalityNoticeKey!!),
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
                Spacer(modifier = Modifier.height(8.sdp))
            }

            // Summary
            Text(
                text = translator.resolve(primaryInterp.summaryKey),
                style = AynvoraTheme.typography.body14,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
            )

            Spacer(modifier = Modifier.height(8.sdp))

            // Detailed Reflection Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .padding(12.sdp),
            ) {
                Text(
                    text = translator.resolve(primaryInterp.reflectionKey),
                    style = AynvoraTheme.typography.body14,
                    color = primaryText,
                )
            }

            // Secondary Interpretations (e.g. arrows, compounds)
            if (bundle.secondaryInterpretations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.sdp))
                for (sec in bundle.secondaryInterpretations) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.sdp),
                    ) {
                        Text(
                            text = translator.resolve(sec.titleKey),
                            style = AynvoraTheme.typography.body14,
                            fontWeight = FontWeight.Medium,
                            color = AynvoraTheme.colors.Gold,
                        )
                        Text(
                            text = translator.resolve(sec.summaryKey),
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.sdp))

            // Method & Source Provenance (Progressive Disclosure)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showSourceDetails = !showSourceDetails }
                    .padding(vertical = 4.sdp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (showSourceDetails) "Hide Method & Source" else "Method & Source Provenance",
                    style = AynvoraTheme.typography.caption12,
                    fontWeight = FontWeight.SemiBold,
                    color = AynvoraTheme.colors.Gold,
                )
                Text(
                    text = if (showSourceDetails) "▲" else "▼",
                    style = AynvoraTheme.typography.caption12,
                    color = AynvoraTheme.colors.Gold,
                )
            }

            AnimatedVisibility(visible = showSourceDetails) {
                Column(modifier = Modifier.padding(top = 6.sdp)) {
                    Text(
                        text = "Tradition: ${bundle.traditionId} | Ruleset: ${bundle.rulesetId} (v${bundle.contentVersion})",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Historical Citations: ${
                            primaryInterp.sourceReferences.joinToString(
                                "; "
                            )
                        }",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "Disclosure: Contemplative framework grounded in documented traditions; zero deterministic, medical, or financial predictions.",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
            }
        }
    }
}

// ============================================================================
// CALCULATION TRANSPARENCY & TRACES
// ============================================================================

@Composable
private fun CalculationTraceCard(
    result: NumerologyResult,
    showTrace: Boolean,
    onToggleTrace: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val traces = result.profile.calculationTraces

    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleTrace),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "How This Was Calculated",
                        style = AynvoraTheme.typography.title18,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "Deterministic, step-by-step reduction traces",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
                Text(
                    text = if (showTrace) "▲ Hide" else "▼ Show",
                    fontSize = 12.ssp,
                    color = AynvoraTheme.colors.Gold,
                )
            }

            AnimatedVisibility(visible = showTrace) {
                Column(
                    modifier = Modifier.padding(top = 10.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp)
                ) {
                    if (traces.isEmpty()) {
                        Text(
                            text = "Trace details computed deterministically in engine.",
                            fontSize = 12.ssp,
                            color = secondaryText
                        )
                    } else {
                        traces.forEach { (type, trace) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                    .padding(10.sdp),
                            ) {
                                Column {
                                    Text(
                                        text = type.name,
                                        fontSize = 12.ssp,
                                        fontWeight = FontWeight.Bold,
                                        color = AynvoraTheme.colors.Gold
                                    )
                                    Spacer(modifier = Modifier.height(2.sdp))
                                    Text(
                                        text = "Raw Input: ${trace.rawInput}",
                                        fontSize = 11.ssp,
                                        color = primaryText
                                    )
                                    Text(
                                        text = "Normalized: ${trace.normalizedInput}",
                                        fontSize = 11.ssp,
                                        color = primaryText
                                    )
                                    Text(
                                        text = "Compound Sum: ${trace.compoundSum}",
                                        fontSize = 11.ssp,
                                        color = primaryText
                                    )
                                    if (trace.reductionSteps.isNotEmpty()) {
                                        trace.reductionSteps.forEach { step ->
                                            Text(
                                                text = "Step ${step.stepNumber}: ${step.equation}",
                                                fontSize = 11.ssp,
                                                color = secondaryText
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Final Value: ${trace.finalValue} (Master: ${trace.isMasterNumber})",
                                        fontSize = 11.ssp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AynvoraTheme.colors.Gold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SOURCE PROVENANCE TRANSPARENCY
// ============================================================================

@Composable
private fun SourceProvenanceCard(
    ruleset: NumerologyRuleset,
    showSource: Boolean,
    onToggleSource: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Elevated) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleSource),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Method & Source Provenance",
                        style = AynvoraTheme.typography.title18,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "Rigorous historical grounding and ruleset metadata",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
                Text(
                    text = if (showSource) "▲ Hide" else "▼ Show",
                    fontSize = 12.ssp,
                    color = AynvoraTheme.colors.Gold,
                )
            }

            AnimatedVisibility(visible = showSource) {
                Column(
                    modifier = Modifier.padding(top = 10.sdp),
                    verticalArrangement = Arrangement.spacedBy(6.sdp)
                ) {
                    Text(text = "Ruleset ID: ${ruleset.id}", fontSize = 11.ssp, color = primaryText)
                    Text(
                        text = "Ruleset Version: ${ruleset.version}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                    Text(
                        text = "Authority: ${ruleset.authorityDescription}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                    Text(
                        text = "Primary Citation: ${ruleset.primarySourceReference}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                    Text(
                        text = "Name Mapping: ${ruleset.nameNumberSystem.name}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                    Text(
                        text = "Master Number Policy: ${ruleset.masterNumberPolicy.name}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                    Text(
                        text = "Reduction Method: ${ruleset.dateReductionMethod.name}",
                        fontSize = 11.ssp,
                        color = primaryText
                    )
                }
            }
        }
    }
}

// ============================================================================
// AI FUTURE BOUNDARY CARD
// ============================================================================

@Composable
private fun AiFutureBoundaryCard(
    onExplainClicked: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(variant = AynvoraCardVariant.Outlined) {
        Column(modifier = Modifier.padding(14.sdp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Explain with AYNVORA AI",
                        style = AynvoraTheme.typography.body14,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(2.sdp))
                    Text(
                        text = "Conversational reflection. AI does not alter calculations.",
                        style = AynvoraTheme.typography.caption12,
                        color = secondaryText,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AynvoraTheme.colors.Gold.copy(alpha = 0.2f))
                        .clickable(onClick = onExplainClicked)
                        .padding(horizontal = 10.sdp, vertical = 6.sdp)
                        .semantics { contentDescription = "Explain with AYNVORA AI" },
                ) {
                    Text(
                        text = "Learn More",
                        fontSize = 11.ssp,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold
                    )
                }
            }
        }
    }
}

// ============================================================================
// COMPARE METHODS TAB
// ============================================================================

@Composable
private fun NumerologyCompareTab(
    state: NumerologyUiState,
    onEvent: (NumerologyUiEvent) -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.sdp),
    ) {
        item(key = "compare_header") {
            AynvoraCard(variant = AynvoraCardVariant.Outlined) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = "Cross-Tradition Comparison",
                        style = AynvoraTheme.typography.title18,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.sdp))
                    Text(
                        text = "These systems use different rules and should not be treated as one combined calculation.",
                        style = AynvoraTheme.typography.body14,
                        color = primaryText,
                    )
                }
            }
        }

        item(key = "compare_picker") {
            AynvoraCard(variant = AynvoraCardVariant.Elevated) {
                Column(modifier = Modifier.padding(14.sdp)) {
                    Text(
                        text = "Select Traditions to Compare",
                        fontSize = 13.ssp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(modifier = Modifier.height(8.sdp))

                    val candidateRulesets = listOf(
                        NumerologyRuleset.PYTHAGOREAN_WESTERN_V1,
                        NumerologyRuleset.CHALDEAN_CHEIRO_V1,
                        NumerologyRuleset.INDIAN_ANK_JYOTISH_V1,
                        NumerologyRuleset.AGRIPPAN_OCCULT_V1,
                    )

                    candidateRulesets.forEach { r ->
                        val isSelected = r.id in state.compareRulesetIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(
                                        NumerologyUiEvent.ToggleCompareRuleset(
                                            r.id,
                                            !isSelected
                                        )
                                    )
                                }
                                .padding(vertical = 6.sdp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isSelected) "☑" else "☐",
                                fontSize = 16.ssp,
                                color = if (isSelected) AynvoraTheme.colors.Gold else primaryText,
                            )
                            Spacer(modifier = Modifier.width(8.sdp))
                            Text(text = r.name, fontSize = 13.ssp, color = primaryText)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.sdp))
                    AynvoraButton(
                        text = if (state.isComparing) "Comparing..." else "Run Comparison",
                        onClick = { onEvent(NumerologyUiEvent.RunComparison) },
                        variant = AynvoraButtonVariant.Primary,
                        enabled = !state.isComparing,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (state.comparisonResults.isNotEmpty()) {
            item(key = "comparison_table") {
                AynvoraCard(variant = AynvoraCardVariant.Elevated) {
                    Column(modifier = Modifier.padding(14.sdp)) {
                        Text(
                            text = "Comparison Matrix",
                            style = AynvoraTheme.typography.title18,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(10.sdp))

                        state.comparisonResults.forEach { compResult ->
                            val rset = NumerologyRuleset.fromId(compResult.profile.rulesetId)
                            val rad = compResult.profile.radical?.radicalValue?.toString() ?: "N/A"
                            val des = compResult.profile.destiny?.destinyValue?.toString() ?: "N/A"
                            val nam = compResult.profile.nameNumber?.nameValue?.toString() ?: "N/A"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                    .padding(10.sdp),
                            ) {
                                Column {
                                    Text(
                                        text = rset?.name ?: compResult.profile.rulesetId,
                                        fontSize = 13.ssp,
                                        fontWeight = FontWeight.Bold,
                                        color = AynvoraTheme.colors.Gold,
                                    )
                                    Spacer(modifier = Modifier.height(4.sdp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Radical: $rad",
                                            fontSize = 12.ssp,
                                            color = primaryText
                                        )
                                        Text(
                                            text = "Destiny: $des",
                                            fontSize = 12.ssp,
                                            color = primaryText
                                        )
                                        Text(
                                            text = "Name: $nam",
                                            fontSize = 12.ssp,
                                            color = primaryText
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.sdp))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HISTORY TAB
// ============================================================================

@Composable
private fun NumerologyHistoryTab(
    state: NumerologyUiState,
    onEvent: (NumerologyUiEvent) -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        item(key = "history_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Saved Calculation Sessions",
                    style = AynvoraTheme.typography.title18,
                    fontWeight = FontWeight.Bold,
                    color = AynvoraTheme.colors.Gold,
                )
                if (state.historyEntries.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        fontSize = 12.ssp,
                        color = Color(0xFFE53935),
                        modifier = Modifier.clickable { onEvent(NumerologyUiEvent.ClearHistory) },
                    )
                }
            }
        }

        if (state.historyEntries.isEmpty()) {
            item(key = "empty_history") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.sdp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No saved numerology calculations yet.",
                        fontSize = 13.ssp,
                        color = secondaryText
                    )
                }
            }
        } else {
            items(state.historyEntries, key = { it.id }) { entry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .clickable { onEvent(NumerologyUiEvent.SelectHistoryEntry(entry.id)) }
                        .padding(12.sdp)
                        .semantics { contentDescription = "History entry: ${entry.traditionName}" },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = entry.traditionName,
                                fontSize = 13.ssp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Text(
                                text = "Input: ${entry.dateOrTextInputDisplay}",
                                fontSize = 11.ssp,
                                color = secondaryText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AynvoraTheme.colors.Gold.copy(alpha = 0.2f))
                                .padding(horizontal = 10.sdp, vertical = 6.sdp),
                        ) {
                            Text(
                                text = entry.summaryResult,
                                fontSize = 13.ssp,
                                fontWeight = FontWeight.Bold,
                                color = AynvoraTheme.colors.Gold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// REPORT OVERLAY & AI MODAL
// ============================================================================

@Composable
private fun NumerologyReportOverlay(
    report: ReportDocument,
    onDismiss: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss)
            .padding(16.sdp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) AynvoraTheme.colors.CosmicBlack else Color.White)
                .clickable(enabled = false) {}
                .padding(16.sdp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = report.title.key,
                        style = AynvoraTheme.typography.title18,
                        fontWeight = FontWeight.Bold,
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "✕",
                        fontSize = 16.ssp,
                        color = primaryText,
                        modifier = Modifier.clickable(onClick = onDismiss),
                    )
                }

                Spacer(modifier = Modifier.height(10.sdp))

                LazyColumn(
                    modifier = Modifier.height(400.sdp),
                    verticalArrangement = Arrangement.spacedBy(10.sdp),
                ) {
                    items(report.sections) { section ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                                .padding(10.sdp),
                        ) {
                            Column {
                                Text(
                                    text = section.title.key,
                                    fontSize = 13.ssp,
                                    fontWeight = FontWeight.Bold,
                                    color = AynvoraTheme.colors.Gold,
                                )
                                Spacer(modifier = Modifier.height(6.sdp))
                                section.blocks.forEach { block ->
                                    when (block) {
                                        is ReportParagraph -> {
                                            Text(
                                                text = block.text.key,
                                                fontSize = 11.ssp,
                                                color = primaryText
                                            )
                                            Spacer(modifier = Modifier.height(4.sdp))
                                        }

                                        is ReportKeyValue -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = block.label.key,
                                                    fontSize = 11.ssp,
                                                    color = secondaryText
                                                )
                                                Text(
                                                    text = block.value,
                                                    fontSize = 11.ssp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryText
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.sdp))
                                        }

                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumerologyAiConversationModal(
    state: NumerologyUiState,
    onDismiss: () -> Unit,
    onQuestionInputChanged: (String) -> Unit,
    onSubmitQuestion: (String, NumerologyAiQuestionCategory) -> Unit,
    onClearConversation: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val messages = state.conversationState?.messages ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(onClick = onDismiss)
            .padding(14.sdp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) AynvoraTheme.colors.CosmicNavy else Color.White)
                .clickable(enabled = false) {}
                .padding(16.sdp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AYNVORA AI Grounded Explanation",
                            style = AynvoraTheme.typography.title18,
                            fontWeight = FontWeight.Bold,
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(2.sdp))
                        Text(
                            text = "Grounded SLM • ${state.selectedRuleset.name} • On-Device",
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText,
                        )
                    }
                    Text(
                        text = "✕",
                        fontSize = 16.ssp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(4.sdp)
                            .semantics { contentDescription = "Close AI Explanation Dialog" },
                    )
                }

                Spacer(modifier = Modifier.height(8.sdp))

                // Non-Authoritative / On-Device Grounding Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AynvoraTheme.colors.Gold.copy(alpha = 0.1f))
                        .border(
                            1.dp,
                            AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.sdp),
                ) {
                    Text(
                        text = "AI is an explanatory synthesis layer. Calculations are authoritative only from AYNVORA engine. Processing is strictly on-device.",
                        fontSize = 10.ssp,
                        color = primaryText,
                    )
                }

                Spacer(modifier = Modifier.height(8.sdp))

                // Suggested Questions Chips
                Text(
                    text = "Suggested Prompts:",
                    fontSize = 11.ssp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                )
                Spacer(modifier = Modifier.height(4.sdp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.sdp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val suggestions = listOf(
                        "What does this number represent?" to NumerologyAiQuestionCategory.EXPLAIN_RESULT,
                        "How was this number calculated?" to NumerologyAiQuestionCategory.EXPLAIN_CALCULATION,
                        "What does the historical source say?" to NumerologyAiQuestionCategory.CLARIFY_SOURCE,
                        "What does this tradition emphasize?" to NumerologyAiQuestionCategory.EXPLAIN_TRADITION,
                        "What does this mean for reflection?" to NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
                    )
                    items(suggestions) { (chipText, cat) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.SoftGold)
                                .clickable { onSubmitQuestion(chipText, cat) }
                                .padding(horizontal = 8.sdp, vertical = 4.sdp)
                                .semantics { contentDescription = "Suggested prompt: $chipText" },
                        ) {
                            Text(
                                text = chipText,
                                fontSize = 10.ssp,
                                color = AynvoraTheme.colors.Gold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.sdp))

                // Messages Scroll Area
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.sdp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Ask a question about your verified numbers, the calculation trace, or traditional sources.",
                                    fontSize = 12.ssp,
                                    color = secondaryText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    items(messages) { msg ->
                        if (msg.isUser) {
                            // User Message Bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 2.dp,
                                                bottomStart = 12.dp,
                                                bottomEnd = 12.dp
                                            )
                                        )
                                        .background(AynvoraTheme.colors.Gold.copy(alpha = 0.25f))
                                        .padding(10.sdp),
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 12.ssp,
                                        color = primaryText,
                                    )
                                }
                            }
                        } else {
                            // Assistant Message Bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 2.dp,
                                                topEnd = 12.dp,
                                                bottomStart = 12.dp,
                                                bottomEnd = 12.dp
                                            )
                                        )
                                        .background(
                                            if (isDark) AynvoraTheme.colors.CosmicBlack else Color(
                                                0xFFF5F5F5
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            AynvoraTheme.colors.Gold.copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(10.sdp),
                                ) {
                                    Column {
                                        if (msg.isFallback) {
                                            Text(
                                                text = "⚡ Deterministic Grounded Fallback",
                                                fontSize = 9.ssp,
                                                fontWeight = FontWeight.Bold,
                                                color = AynvoraTheme.colors.Gold,
                                            )
                                            Spacer(modifier = Modifier.height(2.sdp))
                                        }

                                        Text(
                                            text = msg.text,
                                            fontSize = 12.ssp,
                                            color = primaryText,
                                        )

                                        if (msg.citedSources.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.sdp))
                                            Text(
                                                text = "Sources: ${msg.citedSources.joinToString(", ")}",
                                                fontSize = 10.ssp,
                                                fontWeight = FontWeight.Medium,
                                                color = secondaryText,
                                            )
                                        }

                                        if (msg.referencedEvidenceIds.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.sdp))
                                            Text(
                                                text = "Evidence: ${
                                                    msg.referencedEvidenceIds.joinToString(
                                                        ", "
                                                    )
                                                }",
                                                fontSize = 9.ssp,
                                                color = secondaryText.copy(alpha = 0.8f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.isAiThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.sdp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.sdp),
                                    strokeWidth = 2.dp,
                                    color = AynvoraTheme.colors.Gold,
                                )
                                Spacer(modifier = Modifier.width(8.sdp))
                                Text(
                                    text = "Consulting verified historical evidence...",
                                    fontSize = 11.ssp,
                                    color = secondaryText,
                                )
                            }
                        }
                    }

                    if (state.aiErrorMessage != null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Red.copy(alpha = 0.15f))
                                    .padding(8.sdp),
                            ) {
                                Text(
                                    text = "Error: ${state.aiErrorMessage}",
                                    fontSize = 11.ssp,
                                    color = Color.Red,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.sdp))

                // Bottom Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = state.currentAiQuestionInput,
                        onValueChange = onQuestionInputChanged,
                        placeholder = {
                            Text(
                                "Ask a question...",
                                fontSize = 11.ssp,
                                color = secondaryText
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) AynvoraTheme.colors.CosmicBlack else Color(
                                0xFFF0F0F0
                            ),
                            unfocusedContainerColor = if (isDark) AynvoraTheme.colors.CosmicBlack else Color(
                                0xFFF0F0F0
                            ),
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText,
                        ),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.width(6.sdp))

                    if (messages.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.SoftGold)
                                .clickable(onClick = onClearConversation)
                                .padding(8.sdp)
                                .semantics { contentDescription = "Clear Chat History" },
                        ) {
                            Text(text = "Clear", fontSize = 10.ssp, color = secondaryText)
                        }
                        Spacer(modifier = Modifier.width(4.sdp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (state.currentAiQuestionInput.isNotBlank() && !state.isAiThinking)
                                    AynvoraTheme.colors.Gold
                                else
                                    AynvoraTheme.colors.Gold.copy(alpha = 0.4f)
                            )
                            .clickable(
                                enabled = state.currentAiQuestionInput.isNotBlank() && !state.isAiThinking,
                                onClick = {
                                    onSubmitQuestion(
                                        state.currentAiQuestionInput,
                                        NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
                                    )
                                },
                            )
                            .padding(horizontal = 12.sdp, vertical = 10.sdp)
                            .semantics { contentDescription = "Send AI Question" },
                    ) {
                        Text(
                            text = "Ask",
                            fontSize = 11.ssp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                        )
                    }
                }
            }
        }
    }
}
