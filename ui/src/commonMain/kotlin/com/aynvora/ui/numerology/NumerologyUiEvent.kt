package com.aynvora.ui.numerology

import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.numerology.NumerologyCalculationType

/**
 * Navigation / presentation tabs in the Numerology experience.
 */
enum class NumerologyTab {
    CALCULATE,
    COMPARE,
    HISTORY,
}

/**
 * Typed user interactions for the consumer Numerology experience.
 *
 * Adheres strictly to the Event SDK:
 * - Every business interaction flows through [NumerologyUiEvent]
 * - Typed payloads, no Map<String, Any>
 * - No direct repository calls from Composables
 */
sealed class NumerologyUiEvent(
    eventId: String,
    screenId: String = "numerology",
    componentId: String = "ui",
    payload: AynvoraEventPayload = AynvoraEventPayload.Empty,
) : AynvoraClickEvent(
    eventId = eventId,
    screenId = screenId,
    componentId = componentId,
    payload = payload,
) {
    data class SelectTab(val tab: NumerologyTab) : NumerologyUiEvent(
        eventId = "numerology.tab_selected",
        componentId = "tab_${tab.name.lowercase()}",
    )

    data class SelectRuleset(val rulesetId: String) : NumerologyUiEvent(
        eventId = "numerology.ruleset_selected",
        componentId = "ruleset_item",
        payload = AynvoraEventPayload.NumerologyRulesetSelectPayload(rulesetId),
    )

    data class UpdateDay(val day: String) :
        NumerologyUiEvent("numerology.input_day_changed", componentId = "input_day")

    data class UpdateMonth(val month: String) :
        NumerologyUiEvent("numerology.input_month_changed", componentId = "input_month")

    data class UpdateYear(val year: String) :
        NumerologyUiEvent("numerology.input_year_changed", componentId = "input_year")

    data class UpdateName(val name: String) :
        NumerologyUiEvent("numerology.input_name_changed", componentId = "input_name")

    data class UpdateScriptText(val text: String) :
        NumerologyUiEvent("numerology.input_script_changed", componentId = "input_script")

    data class UpdateTargetYear(val year: String) :
        NumerologyUiEvent("numerology.input_target_year_changed", componentId = "input_target_year")

    data class UpdateTargetMonth(val month: String) : NumerologyUiEvent(
        "numerology.input_target_month_changed",
        componentId = "input_target_month"
    )

    data class UpdateTime(val hour: Int, val minute: Int) :
        NumerologyUiEvent("numerology.input_time_changed", componentId = "input_time")

    data object Calculate : NumerologyUiEvent(
        eventId = "numerology.calculation_started",
        componentId = "btn_calculate",
    )

    data object ResetForm : NumerologyUiEvent("numerology.form_reset", componentId = "btn_reset")

    data class ToggleTrace(val calculationType: NumerologyCalculationType?) : NumerologyUiEvent(
        eventId = "numerology.trace_viewed",
        componentId = "btn_toggle_trace",
        payload = AynvoraEventPayload.NumerologyTraceViewPayload(calculationType?.name ?: "ALL"),
    )

    data object ToggleSourceTransparency : NumerologyUiEvent(
        eventId = "numerology.source_transparency_toggled",
        componentId = "btn_toggle_source",
    )

    data class ToggleCompareRuleset(val rulesetId: String, val isSelected: Boolean) :
        NumerologyUiEvent(
            eventId = "numerology.compare_ruleset_toggled",
            componentId = "checkbox_compare_$rulesetId",
        )

    data object RunComparison : NumerologyUiEvent(
        eventId = "numerology.method_compared",
        componentId = "btn_run_comparison",
    )

    data object RequestReport : NumerologyUiEvent(
        eventId = "numerology.report_requested",
        componentId = "btn_generate_report",
    )

    data object DismissReport :
        NumerologyUiEvent("numerology.report_dismissed", componentId = "btn_dismiss_report")

    data object OpenHistory : NumerologyUiEvent(
        eventId = "numerology.history_opened",
        componentId = "btn_open_history",
    )

    data class SelectHistoryEntry(val entryId: String) : NumerologyUiEvent(
        eventId = "numerology.history_entry_selected",
        componentId = "history_row",
    )

    data object ClearHistory :
        NumerologyUiEvent("numerology.history_cleared", componentId = "btn_clear_history")

    data object RequestAiExplanation :
        NumerologyUiEvent("numerology.ai_explain_requested", componentId = "btn_ai_explain")

    data object DismissAiExplanation :
        NumerologyUiEvent("numerology.ai_explain_dismissed", componentId = "btn_dismiss_ai")

    data class UpdateAiQuestionInput(val text: String) : NumerologyUiEvent(
        eventId = "numerology.ai.input_changed",
        componentId = "input_ai_question",
    )

    data class SubmitAiQuestion(
        val question: String,
        val category: com.aynvora.core.numerology.NumerologyAiQuestionCategory = com.aynvora.core.numerology.NumerologyAiQuestionCategory.REFLECTIVE_QUESTION,
    ) : NumerologyUiEvent(
        eventId = "numerology.ai.question_submitted",
        componentId = "btn_submit_ai_question",
        payload = AynvoraEventPayload.NumerologyAiQuestionSubmittedPayload("", category.name),
    )

    data object ClearAiConversation : NumerologyUiEvent(
        eventId = "numerology.ai.conversation_cleared",
        componentId = "btn_clear_ai_conversation",
    )

    data object Close : NumerologyUiEvent("numerology.close_clicked", componentId = "close_button")
}
