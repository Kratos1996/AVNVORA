package com.aynvora.core.usecase

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.models.BirthProfile
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to save or update a validated [BirthProfile].
 */
class SaveBirthProfileUseCase(
    private val birthProfileRepository: BirthProfileRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(profile: BirthProfile): AynvoraResult<BirthProfile> {
        val result = birthProfileRepository.saveBirthProfile(profile)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.BirthProfileSaved)
        }
        return result
    }
}

/**
 * Use case to observe all saved birth profiles.
 */
class ObserveBirthProfilesUseCase(
    private val birthProfileRepository: BirthProfileRepository,
) {
    fun execute(): Flow<AynvoraResult<List<BirthProfile>>> =
        birthProfileRepository.observeAllBirthProfiles()
}

/**
 * Use case to delete a birth profile by ID.
 */
class DeleteBirthProfileUseCase(
    private val birthProfileRepository: BirthProfileRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(id: String): AynvoraResult<Unit> {
        val result = birthProfileRepository.deleteBirthProfile(id)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.BirthProfileDeleted)
        }
        return result
    }
}
