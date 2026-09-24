package com.aynvora.app.analytics

import android.content.Context
import com.aynvora.core.analytics.AnalyticsConsent
import com.aynvora.core.analytics.AnalyticsConsentManager
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android production implementation of [AnalyticsConsentManager] backed by Firebase Analytics.
 *
 * Manages [FirebaseAnalytics.setAnalyticsCollectionEnabled] based on user consent.
 *
 * Architecture rules:
 * - Confined to `androidApp` — never imported from shared modules.
 * - Firebase is enabled/disabled at the Firebase SDK level, so even if track() is called
 *   on FirebaseAnalyticsTracker, Firebase itself will suppress collection when disabled.
 * - Consent state is kept in memory; persistence is done via UserPreferencesRepository
 *   at the app level to ensure it survives restarts.
 *
 * Privacy rules:
 * - NO advertising personalization. Advertising IDs are not used.
 * - NO sensitive user properties based on spiritual/religious data.
 * - Firebase collection disabled means no data leaves the device.
 *
 * Required external configuration:
 * - google-services.json must be placed at androidApp/google-services.json.
 *   This file is NOT committed to version control.
 *   Obtain it from the AYNVORA Firebase console (project owner must provision).
 */
class FirebaseAnalyticsConsentManager(context: Context) : AnalyticsConsentManager {

    private val firebase: FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context.applicationContext)

    private val _consentState = MutableStateFlow(AnalyticsConsent.PENDING)
    override val consentState: StateFlow<AnalyticsConsent> = _consentState.asStateFlow()

    override suspend fun grantConsent() {
        _consentState.value = AnalyticsConsent.GRANTED
        try {
            firebase.setAnalyticsCollectionEnabled(true)
        } catch (_: Exception) {
            // Firebase failures must never surface to users.
        }
    }

    override suspend fun denyConsent() {
        _consentState.value = AnalyticsConsent.DENIED
        try {
            firebase.setAnalyticsCollectionEnabled(false)
        } catch (_: Exception) {
            // Firebase failures must never surface to users.
        }
    }
}
