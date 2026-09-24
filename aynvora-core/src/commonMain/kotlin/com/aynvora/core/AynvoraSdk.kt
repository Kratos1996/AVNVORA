package com.aynvora.core

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
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
     * Calculates a specific divisional chart from an astrological request.
     */
    suspend fun calculateDivisionalChart(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart,
    ): AynvoraResult<com.aynvora.core.models.DivisionalChartResult>

    /**
     * Calculates multiple requested divisional charts from an astrological request.
     */
    suspend fun calculateDivisionalCharts(
        request: ChartRequest,
        charts: Set<com.aynvora.core.models.DivisionalChart>,
    ): AynvoraResult<Map<com.aynvora.core.models.DivisionalChart, com.aynvora.core.models.DivisionalChartResult>>

    /**
     * Calculates planetary dignities for a specific chart position (D1 or divisional).
     */
    suspend fun calculateDignities(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart = com.aynvora.core.models.DivisionalChart.D1,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryDignity>>

    /**
     * Calculates planetary relationships for a specific chart position (D1 or divisional).
     */
    suspend fun calculateRelationships(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart = com.aynvora.core.models.DivisionalChart.D1,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryRelationship>>

    /**
     * Calculates planetary Shadbala strength foundations from an astrological request.
     */
    suspend fun calculateShadbala(
        request: ChartRequest,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryShadbala>>

    /**
     * Calculates Ashtakavarga (Bhinnashtakavarga and Sarvashtakavarga) from an astrological request.
     */
    suspend fun calculateAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.AshtakavargaResult>

    /**
     * Calculates Shodhita Ashtakavarga (Trikona Shodhana, Ekadhipatya Shodhana, and Reduced SAV) from an astrological request.
     */
    suspend fun calculateShodhitaAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.ShodhitaAshtakavargaResult>

    /**
     * Calculates Ashtakavarga Pindas (Rashi Pinda, Graha Pinda, and Shodhya Pinda) from an astrological request.
     */
    suspend fun calculateAshtakavargaPinda(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.AshtakavargaPinda>

    /**
     * Calculates 120-year Vimshottari Dasha timeline from birth Julian Day and Moon's sidereal longitude.
     */
    fun calculateDasha(
        birthJd: Double,
        moonSiderealLongitude: Double,
        calculateAntardashas: Boolean = true,
        calculatePratyantardashas: Boolean = false,
    ): com.aynvora.astro.dasha.VimshottariDashaTimeline =
        com.aynvora.astro.dasha.VimshottariDashaCalculator.calculate(
            birthJd, moonSiderealLongitude, calculateAntardashas, calculatePratyantardashas,
        )

    /**
     * Calculates planetary transit snapshot for a specific Julian Day.
     */
    fun calculateTransit(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): com.aynvora.astro.transit.TransitSnapshot =
        com.aynvora.astro.transit.TransitCalculator.calculateSnapshot(jd, ayanamsaConvention)

    /**
     * Calculates 5-limb classical Panchang snapshot for an exact moment.
     */
    fun calculatePanchang(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): com.aynvora.astro.panchang.PanchangSnapshot =
        com.aynvora.astro.panchang.PanchangCalculator.calculate(jd, ayanamsaConvention)

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
    val content: com.aynvora.core.repository.ContentRepository? get() = null
    val contentSync: com.aynvora.core.repository.ContentSyncRepository? get() = null
    val tarot: com.aynvora.core.tarot.TarotRepository? get() = null
    val tarotUseCases: com.aynvora.core.tarot.PerformTarotReadingUseCase?
        get() = tarot?.let {
            com.aynvora.core.tarot.PerformTarotReadingUseCase(
                it,
                analyticsTracker = analytics
            )
        }
    val birthProfileUseCases: com.aynvora.core.usecase.SaveBirthProfileUseCase?
        get() = birthProfiles?.let {
            com.aynvora.core.usecase.SaveBirthProfileUseCase(
                it,
                analyticsTracker = analytics
            )
        }
    val chartUseCases: com.aynvora.core.usecase.SaveChartUseCase?
        get() = savedCharts?.let {
            com.aynvora.core.usecase.SaveChartUseCase(
                it,
                analyticsTracker = analytics
            )
        }
    val userPreferencesUseCases: com.aynvora.core.usecase.GetUserPreferencesUseCase?
        get() = userPreferences?.let { com.aynvora.core.usecase.GetUserPreferencesUseCase(it) }

    /**
     * Analytics tracker used to report non-PII usage events.
     * Defaults to [NoOpAnalyticsTracker] when not provided.
     */
    val analytics: AnalyticsTracker get() = NoOpAnalyticsTracker()
}

/**
 * Entry point factory for the AYNVORA SDK.
 */
object Aynvora {
    /**
     * Creates a standard instance of [AynvoraSdk] with optional repository and analytics configurations.
     */
    fun create(
        userProfiles: UserProfileRepository? = null,
        birthProfiles: BirthProfileRepository? = null,
        savedCharts: SavedChartRepository? = null,
        userPreferences: UserPreferencesRepository? = null,
        content: com.aynvora.core.repository.ContentRepository? = null,
        contentSync: com.aynvora.core.repository.ContentSyncRepository? = null,
        tarot: com.aynvora.core.tarot.TarotRepository? = null,
        analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
    ): AynvoraSdk = DefaultAynvoraSdk(
        adapter = AstroEngineAdapter(),
        userProfiles = userProfiles,
        birthProfiles = birthProfiles,
        savedCharts = savedCharts,
        userPreferences = userPreferences,
        content = content,
        contentSync = contentSync,
        tarot = tarot,
        analyticsTrackerImpl = analyticsTracker,
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
    override val content: com.aynvora.core.repository.ContentRepository? = null,
    override val contentSync: com.aynvora.core.repository.ContentSyncRepository? = null,
    override val tarot: com.aynvora.core.tarot.TarotRepository? = null,
    private val analyticsTrackerImpl: AnalyticsTracker = NoOpAnalyticsTracker(),
) : AynvoraSdk {

    override val analytics: AnalyticsTracker get() = analyticsTrackerImpl

    override suspend fun calculateChart(request: ChartRequest): AynvoraResult<ChartResult> {
        val rulesetId = request.config.vargaRulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.ChartCalculationRequested(rulesetId))
        val result = adapter.execute(request)
        when (result) {
            is AynvoraResult.Success ->
                analyticsTrackerImpl.track(AnalyticsEvent.ChartCalculationSucceeded(rulesetId, 0L))
            is AynvoraResult.Failure ->
                analyticsTrackerImpl.track(AnalyticsEvent.ChartCalculationFailed(result.analyticsErrorCode()))
        }
        return result
    }

    override suspend fun calculateDivisionalChart(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart,
    ): AynvoraResult<com.aynvora.core.models.DivisionalChartResult> {
        val division = chart.name
        analyticsTrackerImpl.track(AnalyticsEvent.DivisionalChartRequested(division))
        val result = adapter.executeDivisionalChart(request, chart)
        when (result) {
            is AynvoraResult.Success ->
                analyticsTrackerImpl.track(AnalyticsEvent.DivisionalChartSucceeded(division, 0L))
            is AynvoraResult.Failure ->
                analyticsTrackerImpl.track(AnalyticsEvent.DivisionalChartFailed(division, result.analyticsErrorCode()))
        }
        return result
    }

    override suspend fun calculateDivisionalCharts(
        request: ChartRequest,
        charts: Set<com.aynvora.core.models.DivisionalChart>,
    ): AynvoraResult<Map<com.aynvora.core.models.DivisionalChart, com.aynvora.core.models.DivisionalChartResult>> {
        charts.forEach { analyticsTrackerImpl.track(AnalyticsEvent.DivisionalChartRequested(it.name)) }
        return adapter.executeDivisionalCharts(request, charts)
    }

    override suspend fun calculateDignities(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryDignity>> =
        adapter.executeDignities(request, chart)

    override suspend fun calculateRelationships(
        request: ChartRequest,
        chart: com.aynvora.core.models.DivisionalChart,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryRelationship>> =
        adapter.executeRelationships(request, chart)

    override suspend fun calculateShadbala(
        request: ChartRequest,
    ): AynvoraResult<List<com.aynvora.core.models.PlanetaryShadbala>> {
        val rulesetId = request.config.vargaRulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.ShadbalaCalculationRequested(rulesetId))
        val result = adapter.executeShadbala(request)
        when (result) {
            is AynvoraResult.Success ->
                analyticsTrackerImpl.track(AnalyticsEvent.ShadbalaCalculationSucceeded(rulesetId, 0L))
            is AynvoraResult.Failure ->
                analyticsTrackerImpl.track(AnalyticsEvent.ShadbalaCalculationFailed(result.analyticsErrorCode()))
        }
        return result
    }

    override suspend fun calculateAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.AshtakavargaResult> {
        val rulesetId = request.config.ashtakavargaRulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.AshtakavargaCalculationRequested(rulesetId))
        val result = adapter.executeAshtakavarga(request)
        when (result) {
            is AynvoraResult.Success ->
                analyticsTrackerImpl.track(AnalyticsEvent.AshtakavargaCalculationSucceeded(rulesetId, 0L))
            is AynvoraResult.Failure ->
                analyticsTrackerImpl.track(AnalyticsEvent.AshtakavargaCalculationFailed(result.analyticsErrorCode()))
        }
        return result
    }

    override suspend fun calculateShodhitaAshtakavarga(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.ShodhitaAshtakavargaResult> {
        val rulesetId = request.config.ashtakavargaRulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.ShodhanaCalculationRequested(rulesetId))
        val result = adapter.executeShodhitaAshtakavarga(request)
        if (result is AynvoraResult.Success) {
            analyticsTrackerImpl.track(AnalyticsEvent.ShodhanaCalculationSucceeded(rulesetId, 0L))
        }
        return result
    }

    override suspend fun calculateAshtakavargaPinda(
        request: ChartRequest,
    ): AynvoraResult<com.aynvora.core.models.AshtakavargaPinda> {
        val rulesetId = request.config.ashtakavargaRulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.PindaCalculationRequested(rulesetId))
        val result = adapter.executeAshtakavargaPinda(request)
        if (result is AynvoraResult.Success) {
            analyticsTrackerImpl.track(AnalyticsEvent.PindaCalculationSucceeded(rulesetId, 0L))
        }
        return result
    }

    override fun getMetadata(): EngineMetadata =
        adapter.getMetadata()
}

/**
 * Maps an [AynvoraResult.Failure] to a sanitized analytics error code string.
 * Never leaks internal messages, user data, or stack traces.
 */
private fun AynvoraResult.Failure.analyticsErrorCode(): String = when (this) {
    is AynvoraResult.Failure.InvalidInput -> "invalid_input"
    is AynvoraResult.Failure.UnsupportedConfiguration -> "unsupported_configuration"
    is AynvoraResult.Failure.CalculationFailure -> "calculation_failure"
    is AynvoraResult.Failure.NotFound -> "not_found"
    is AynvoraResult.Failure.StorageFailure -> "storage_failure"
    is AynvoraResult.Failure.CorruptedData -> "corrupted_data"
    is AynvoraResult.Failure.MigrationFailure -> "migration_failure"
    is AynvoraResult.Failure.SyncFailure -> "sync_failure"
    is AynvoraResult.Failure.InternalFailure -> "internal_failure"
}


