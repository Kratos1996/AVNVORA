package com.aynvora.qa.core.models

import kotlinx.serialization.Serializable

/**
 * Stable, typed action identity for AYNVORA UI components.
 * Independent of localized visible text or screen coordinates.
 */
@Serializable
data class QaActionId(
    val feature: String,
    val screen: String,
    val component: String,
    val action: String,
    val variant: String? = null,
) {
    val identifier: String
        get() = buildString {
            append(feature.uppercase())
            append("_")
            append(screen.uppercase())
            append("_")
            append(component.uppercase())
            append("_")
            append(action.uppercase())
            if (!variant.isNullOrBlank()) {
                append("_")
                append(variant.uppercase())
            }
        }

    override fun toString(): String = identifier

    companion object {
        fun of(
            feature: String,
            screen: String,
            component: String,
            action: String,
            variant: String? = null
        ): QaActionId {
            return QaActionId(feature, screen, component, action, variant)
        }

        // Canonical Dashboard actions
        val DASHBOARD_OPEN_FEATURE = of("dashboard", "home", "feature_card", "open")
        val DASHBOARD_OPEN_LANGUAGE = of("dashboard", "home", "top_bar", "open_language")
        val DASHBOARD_TOGGLE_THEME = of("dashboard", "home", "top_bar", "toggle_theme")
        val DASHBOARD_CLOSE_SHEET = of("dashboard", "sheet", "close_button", "close")

        // Canonical Language sheet actions
        val LANGUAGE_SELECT_LOCALE = of("localization", "language_sheet", "locale_row", "select")
        val LANGUAGE_CLOSE_SHEET = of("localization", "language_sheet", "close_button", "close")

        // Canonical Feature Detail sheet actions
        val FEATURE_DETAIL_CLOSE = of("dashboard", "feature_detail_sheet", "close_button", "close")
        val FEATURE_DETAIL_ACTION =
            of("dashboard", "feature_detail_sheet", "action_button", "proceed")
        val VEDIC_ASTROLOGY_SELECT = of("astrology", "sheet", "option", "select")

        // Canonical Vedic Astrology actions
        val ASTROLOGY_CALCULATE = of("astrology", "form", "calculate_button", "submit")
        val ASTROLOGY_CLOSE = of("astrology", "screen", "close_button", "close")

        // Canonical Numerology actions
        val NUMEROLOGY_CALCULATE = of("numerology", "form", "calculate_button", "submit")
        val NUMEROLOGY_SELECT_TRADITION = of("numerology", "tradition_picker", "chip", "select")
        val NUMEROLOGY_OPEN_AI = of("numerology", "result", "ai_button", "open_slm")
        val NUMEROLOGY_CLOSE = of("numerology", "screen", "close_button", "close")

        // Canonical Tarot actions
        val TAROT_DRAW_CARD = of("tarot", "draw", "deck", "draw_card")
        val TAROT_RESET = of("tarot", "result", "reset_button", "reset")
        val TAROT_CLOSE = of("tarot", "screen", "close_button", "close")

        // Canonical Palmistry actions
        val PALMISTRY_SELECT_HAND = of("palmistry", "setup", "hand_picker", "select")
        val PALMISTRY_OPEN_CAMERA =
            of("palmistry", "input_selection", "camera_button", "open_camera")
        val PALMISTRY_OPEN_GALLERY =
            of("palmistry", "input_selection", "gallery_button", "open_gallery")
        val PALMISTRY_ANALYZE = of("palmistry", "analysis", "analyze_button", "start")
        val PALMISTRY_CLOSE = of("palmistry", "screen", "close_button", "close")

        // Canonical Gemstone actions (Phase 8.2)
        val GEMSTONE_OPEN = of("gemstone", "dashboard", "feature_card", "open")
        val GEMSTONE_SELECT = of("gemstone", "catalog", "gem_card", "select")
        val GEMSTONE_ADD = of("gemstone", "inventory", "add_button", "add")
        val GEMSTONE_REMOVE = of("gemstone", "inventory", "remove_button", "remove")
        val GEMSTONE_CHECK_COMPATIBILITY =
            of("gemstone", "compatibility", "check_button", "evaluate")
        val GEMSTONE_GENERATE_RECOMMENDATION =
            of("gemstone", "recommendation", "generate_button", "generate")
        val GEMSTONE_OPEN_CAMERA = of("gemstone", "certificate", "camera_button", "open_camera")
        val GEMSTONE_OPEN_GALLERY = of("gemstone", "certificate", "gallery_button", "open_gallery")
        val GEMSTONE_RUN_CERTIFICATE_INSPECTION =
            of("gemstone", "certificate", "inspect_button", "inspect")
        val GEMSTONE_GENERATE_REPORT = of("gemstone", "report", "generate_button", "generate")
        val GEMSTONE_CLOSE = of("gemstone", "screen", "close_button", "close")

        // Canonical AI System & Intelligence actions
        val AI_OPEN = of("ai", "dashboard", "card", "open")
        val AI_VIEW_MODEL_DETAILS = of("ai", "card", "link", "view_details")
        val AI_VIEW_SYSTEM_DETAILS = AI_VIEW_MODEL_DETAILS
        val AI_DOWNLOAD_MODEL = of("ai", "setup", "button", "download")
        val AI_CANCEL_DOWNLOAD = of("ai", "setup", "button", "cancel_download")
        val AI_DELETE_MODEL = of("ai", "setup", "button", "delete")
        val AI_LOAD_MODEL = of("ai", "details", "button", "load")
        val AI_UNLOAD_MODEL = of("ai", "details", "button", "unload")
        val AI_ASK_QUESTION = of("ai", "interaction", "button", "ask")
        val AI_ASK = AI_ASK_QUESTION
        val AI_RETRY = of("ai", "setup", "button", "retry")
        val AI_CHANGE_FEATURE_CONTEXT = of("ai", "interaction", "picker", "change_context")
        val AI_CLOSE_DETAILS = of("ai", "details", "close_button", "close")
    }
}
