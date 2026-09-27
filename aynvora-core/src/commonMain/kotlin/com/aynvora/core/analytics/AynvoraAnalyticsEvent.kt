package com.aynvora.core.analytics

/**
 * Strongly typed analytics contract.
 *
 * Direct caller code and ViewModels must use typed domain analytics definitions
 * rather than constructing ad-hoc untyped parameter maps.
 */
interface AynvoraAnalyticsEvent {
    val eventName: String
    val parameters: Map<String, Any>
}
