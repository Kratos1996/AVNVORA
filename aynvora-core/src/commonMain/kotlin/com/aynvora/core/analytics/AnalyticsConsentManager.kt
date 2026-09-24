package com.aynvora.core.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Analytics consent state for AYNVORA.
 *
 * Consent MUST be checked before any analytics data is collected or transmitted.
 * The default state is [PENDING] — analytics must not fire before consent is granted.
 */
enum class AnalyticsConsent {
    /** Consent not yet determined. Analytics must be suppressed. */
    PENDING,
    /** User has granted analytics consent. Firebase may collect events. */
    GRANTED,
    /** User has explicitly declined analytics. All tracking must be suppressed. */
    DENIED,
}

/**
 * Domain abstraction for managing user analytics consent.
 *
 * Architecture rules:
 * - Lives in `:aynvora-core` domain — no Firebase imports.
 * - Platform implementations (Android/iOS) configure Firebase collection via this contract.
 * - Consent state is persisted via [com.aynvora.core.repository.UserPreferencesRepository].
 * - Analytics must NEVER fire if consent is [AnalyticsConsent.DENIED] or [AnalyticsConsent.PENDING].
 *
 * Privacy rules:
 * - Do NOT use Firebase advertising identifiers.
 * - Do NOT enable personalized advertising.
 * - Do NOT collect sensitive spiritual/religious user properties.
 * - Do NOT persist consent state to Firebase — use local preferences only.
 */
interface AnalyticsConsentManager {
    /**
     * Exposes the current consent state as a reactive stream.
     * Collectors must re-evaluate analytics enablement whenever this changes.
     */
    val consentState: StateFlow<AnalyticsConsent>

    /**
     * Grants analytics consent.
     * Platform implementations must enable Firebase data collection when called.
     */
    suspend fun grantConsent()

    /**
     * Denies analytics consent.
     * Platform implementations must disable Firebase data collection and suppress all tracking.
     */
    suspend fun denyConsent()

    /**
     * Returns whether analytics may currently fire events.
     * True only when consent is [AnalyticsConsent.GRANTED].
     */
    fun isAnalyticsEnabled(): Boolean = consentState.value == AnalyticsConsent.GRANTED
}

/**
 * In-memory consent manager for use in tests and Desktop platform.
 *
 * Desktop does not have Firebase Analytics; this provides a safe no-op implementation
 * that allows the rest of the codebase to compile and function without platform-specific stubs.
 *
 * @param initialConsent The starting consent state. Defaults to [AnalyticsConsent.PENDING]
 *   to ensure analytics is safely suppressed until the user is explicitly asked for consent.
 */
class InMemoryAnalyticsConsentManager(
    initialConsent: AnalyticsConsent = AnalyticsConsent.PENDING,
) : AnalyticsConsentManager {

    private val _consentState = MutableStateFlow(initialConsent)
    override val consentState: StateFlow<AnalyticsConsent> = _consentState.asStateFlow()

    override suspend fun grantConsent() {
        _consentState.value = AnalyticsConsent.GRANTED
    }

    override suspend fun denyConsent() {
        _consentState.value = AnalyticsConsent.DENIED
    }
}

/**
 * Consent-aware [AnalyticsTracker] decorator.
 *
 * Wraps a delegate [AnalyticsTracker] and gates all event tracking
 * behind the current [AnalyticsConsentManager] state.
 *
 * Use this to compose a consent-aware tracker from any backing implementation:
 * ```kotlin
 * val tracker = ConsentAwareAnalyticsTracker(
 *     delegate = FirebaseAnalyticsTracker(context),
 *     consentManager = firebaseConsentManager,
 * )
 * ```
 */
class ConsentAwareAnalyticsTracker(
    private val delegate: AnalyticsTracker,
    private val consentManager: AnalyticsConsentManager,
) : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) {
        if (consentManager.isAnalyticsEnabled()) {
            delegate.track(event)
        }
    }
}
