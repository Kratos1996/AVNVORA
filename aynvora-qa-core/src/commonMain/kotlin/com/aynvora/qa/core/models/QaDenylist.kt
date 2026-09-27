package com.aynvora.qa.core.models

/**
 * Registry of dangerous/destructive actions that autonomous exploration MUST NOT execute.
 */
object QaDenylist {
    private val DANGEROUS_KEYWORDS = listOf(
        "delete",
        "remove",
        "logout",
        "log_out",
        "purchase",
        "payment",
        "pay",
        "buy",
        "send_money",
        "transfer",
        "reset",
        "clear_all",
        "clear_data",
        "share_external",
        "browser",
        "auth",
        "login",
        "sign_in",
        "destructive"
    )

    fun isDangerous(actionId: QaActionId): Boolean {
        val id = actionId.identifier.lowercase()
        return DANGEROUS_KEYWORDS.any { id.contains(it) }
    }

    fun isDangerous(label: String): Boolean {
        val norm = label.lowercase()
        return DANGEROUS_KEYWORDS.any { norm.contains(it) }
    }
}
