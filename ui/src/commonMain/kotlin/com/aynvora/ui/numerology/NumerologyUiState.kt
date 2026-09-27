package com.aynvora.ui.numerology

import com.aynvora.core.numerology.NumerologyCalculationType
import com.aynvora.core.numerology.NumerologyHistoryEntry
import com.aynvora.core.numerology.NumerologyResult
import com.aynvora.core.numerology.NumerologyRuleset
import com.aynvora.core.report.ReportDocument

/**
 * Immutable UI State for the consumer Numerology experience.
 */
data class NumerologyUiState(
    val activeTab: NumerologyTab = NumerologyTab.CALCULATE,
    val selectedRuleset: NumerologyRuleset = NumerologyRuleset.CHALDEAN_CHEIRO_V1,
    val day: String = "11",
    val month: String = "7",
    val year: String = "1996",
    val name: String = "ISHANT",
    val scriptText: String = "",
    val targetYear: String = "2026",
    val targetMonth: String = "9",
    val hour: Int = 12,
    val minute: Int = 0,
    val validationErrorKey: String? = null,
    val isCalculating: Boolean = false,
    val calculationError: String? = null,
    val currentResult: NumerologyResult? = null,
    val selectedTraceType: NumerologyCalculationType? = null,
    val showTrace: Boolean = false,
    val showSource: Boolean = false,
    val showAiModal: Boolean = false,
    val compareRulesetIds: Set<String> = setOf(
        NumerologyRuleset.PYTHAGOREAN_WESTERN_V1.id,
        NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
        NumerologyRuleset.INDIAN_ANK_JYOTISH_V1.id,
    ),
    val comparisonResults: List<NumerologyResult> = emptyList(),
    val isComparing: Boolean = false,
    val historyEntries: List<NumerologyHistoryEntry> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val generatedReport: ReportDocument? = null,
    val isGeneratingReport: Boolean = false,
    val reportError: String? = null,
    val conversationState: com.aynvora.core.numerology.NumerologyConversationState? = null,
    val isAiThinking: Boolean = false,
    val aiErrorMessage: String? = null,
    val currentAiQuestionInput: String = "",
)
