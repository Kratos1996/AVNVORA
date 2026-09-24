package com.aynvora.app.analytics

import android.content.Context
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

/**
 * Android production implementation of [AnalyticsTracker] backed by Firebase Analytics.
 *
 * Architecture rules:
 * - This class is confined to `androidApp` — it MUST NOT be imported or referenced from
 *   `:aynvora-core`, `:astro-engine`, `:aynvora-data`, or any KMP shared module.
 * - It receives an [AnalyticsEvent] which is guaranteed PII-free by construction.
 * - No event parameter values are inspected, transformed, or augmented here.
 * - Any Firebase exception is caught and discarded to prevent analytics from
 *   destabilising the host application.
 */
class FirebaseAnalyticsTracker(context: Context) : AnalyticsTracker {

    private val firebase: FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context.applicationContext)

    override fun track(event: AnalyticsEvent) {
        try {
            firebase.logEvent(event.name) {
                event.params.forEach { (key, value) ->
                    when (value) {
                        is String -> param(key, value)
                        is Long   -> param(key, value)
                        is Double -> param(key, value)
                        is Int    -> param(key, value.toLong())
                        is Float  -> param(key, value.toDouble())
                        is Boolean -> param(key, value.toString())
                        else       -> param(key, value.toString())
                    }
                }
            }
        } catch (_: Exception) {
            // Analytics failures must never surface to users or affect calculations.
        }
    }
}
