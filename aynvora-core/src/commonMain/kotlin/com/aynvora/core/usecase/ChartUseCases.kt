package com.aynvora.core.usecase

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.models.SavedChart
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case to persist a calculated astrological chart.
 */
class SaveChartUseCase(
    private val savedChartRepository: SavedChartRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(chart: SavedChart): AynvoraResult<SavedChart> {
        val result = savedChartRepository.saveChart(chart)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.ChartSaved)
        }
        return result
    }
}

/**
 * Use case to observe saved charts for a specific birth profile.
 */
class ObserveSavedChartsUseCase(
    private val savedChartRepository: SavedChartRepository,
) {
    fun execute(birthProfileId: String): Flow<AynvoraResult<List<SavedChart>>> =
        savedChartRepository.observeChartsForBirthProfile(birthProfileId)
}

/**
 * Use case to delete a saved chart by ID.
 */
class DeleteChartUseCase(
    private val savedChartRepository: SavedChartRepository,
    private val analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
) {
    suspend fun execute(id: String): AynvoraResult<Unit> {
        val result = savedChartRepository.deleteChart(id)
        if (result is AynvoraResult.Success) {
            analyticsTracker.track(AnalyticsEvent.ChartDeleted)
        }
        return result
    }
}
