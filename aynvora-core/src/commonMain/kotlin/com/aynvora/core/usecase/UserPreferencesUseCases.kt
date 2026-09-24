package com.aynvora.core.usecase

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to read user preferences.
 */
class GetUserPreferencesUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun execute(): AynvoraResult<UserPreferences> =
        userPreferencesRepository.getPreferences()
}

/**
 * Use case to observe user preferences stream.
 */
class ObserveUserPreferencesUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun execute(): Flow<AynvoraResult<UserPreferences>> =
        userPreferencesRepository.observePreferences()
}

/**
 * Use case to update user preferences.
 */
class UpdateUserPreferencesUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(preferences: UserPreferences): AynvoraResult<UserPreferences> {
        val result = userPreferencesRepository.updatePreferences(preferences)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.PreferenceChanged("all"))
        }
        return result
    }
}
