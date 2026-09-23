package com.aynvora.core

import com.aynvora.core.internal.AstroEngineAdapter
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.EngineMetadata
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.repository.UserProfileRepository
import com.aynvora.core.result.AynvoraResult

/**
 * Primary public SDK facade for AYNVORA.
 *
 * Exposes a stable, platform-independent boundary that decouples user interfaces
 * and consumer applications from underlying calculation engine implementations
 * and persistence infrastructure.
 */
interface AynvoraSdk {

    /**
     * Calculates an astrological chart from a structured, validated request.
     */
    suspend fun calculateChart(request: ChartRequest): AynvoraResult<ChartResult>

    /**
     * Convenience overload calculating chart directly from birth data and optional configuration.
     */
    suspend fun calculateChart(
        birthData: BirthData,
        config: CalculationConfig = CalculationConfig(),
    ): AynvoraResult<ChartResult> = calculateChart(ChartRequest(birthData, config))

    /**
     * Discovers calculation engine version and supported capability domains.
     */
    fun getMetadata(): EngineMetadata

    /**
     * Repositories for data persistence and offline-first state access.
     */
    val userProfiles: UserProfileRepository? get() = null
    val birthProfiles: BirthProfileRepository? get() = null
    val savedCharts: SavedChartRepository? get() = null
    val userPreferences: UserPreferencesRepository? get() = null
}

/**
 * Entry point factory for the AYNVORA SDK.
 */
object Aynvora {
    /**
     * Creates a standard instance of [AynvoraSdk] with optional repository configurations.
     */
    fun create(
        userProfiles: UserProfileRepository? = null,
        birthProfiles: BirthProfileRepository? = null,
        savedCharts: SavedChartRepository? = null,
        userPreferences: UserPreferencesRepository? = null,
    ): AynvoraSdk = DefaultAynvoraSdk(
        adapter = AstroEngineAdapter(),
        userProfiles = userProfiles,
        birthProfiles = birthProfiles,
        savedCharts = savedCharts,
        userPreferences = userPreferences,
    )
}

/**
 * Default implementation of [AynvoraSdk] delegating to [AstroEngineAdapter] and repositories.
 */
internal class DefaultAynvoraSdk(
    private val adapter: AstroEngineAdapter = AstroEngineAdapter(),
    override val userProfiles: UserProfileRepository? = null,
    override val birthProfiles: BirthProfileRepository? = null,
    override val savedCharts: SavedChartRepository? = null,
    override val userPreferences: UserPreferencesRepository? = null,
) : AynvoraSdk {
    override suspend fun calculateChart(request: ChartRequest): AynvoraResult<ChartResult> =
        adapter.execute(request)

    override fun getMetadata(): EngineMetadata =
        adapter.getMetadata()
}
