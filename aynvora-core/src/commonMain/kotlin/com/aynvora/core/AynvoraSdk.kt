package com.aynvora.core

import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.analytics.NoOpAnalyticsTracker
import com.aynvora.core.internal.AstroEngineAdapter
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.calculateTransitRequest
import com.aynvora.core.models.calculatePanchangRequest
import com.aynvora.core.models.calculateDashaRequest
import com.aynvora.core.models.generateKundaliSnapshot
import com.aynvora.core.models.getChart
import com.aynvora.core.models.exportJson
import com.aynvora.core.models.openSavedKundaliSnapshot
import com.aynvora.core.astrology.prediction.withEventOccurrences
import com.aynvora.core.models.SavedKundaliSnapshot
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
    /** Metadata for calculators exposed through the shared feature engine registry. */
    fun getAstroCalculatorRegistry() = com.aynvora.astro.pipeline.AstroCalculatorRegistry.all()

    /** Returns engine readiness, required pipeline dependencies, and verified implementation blockers. */
    fun getAdvancedAstrologyCapabilities() =
        com.aynvora.core.astrology.prediction.AdvancedAstrologyFeatureRegistry.capabilities

    /** Evaluates explicit feature evidence and appends source-linked occurrences to the snapshot. */
    fun calculateEventOccurrences(
        snapshot: com.aynvora.core.models.KundaliSnapshot,
        requests: List<com.aynvora.core.astrology.prediction.AstroEventEvaluationRequest>,
    ): com.aynvora.core.models.KundaliSnapshot {
        val occurrences = requests.map { request ->
            com.aynvora.core.astrology.prediction.AstroEventEngine.evaluate(
                request.definition, request.period, request.facts, request.provenance, request.featureId,
            )
        }
        return snapshot.withEventOccurrences(occurrences)
    }

    /** Slices a requested observation range while reusing caller-supplied natal facts. */
    fun generateEvents(
        snapshot: com.aynvora.core.models.KundaliSnapshot,
        catalog: com.aynvora.core.astrology.prediction.AstroEventCatalog,
        range: com.aynvora.core.astrology.prediction.AstroEventObservationRange,
        baseFacts: List<com.aynvora.core.astrology.prediction.AstroEventFact>,
        provenance: com.aynvora.astro.provenance.CalculationMetadata,
        factsForSlice: (com.aynvora.core.astrology.prediction.AstroEventDefinition, com.aynvora.core.astrology.prediction.AstroEventTimeSlice) -> List<com.aynvora.core.astrology.prediction.AstroEventFact>,
    ): com.aynvora.core.astrology.prediction.AstroEventSnapshotResult {
        val generation = com.aynvora.core.astrology.prediction.AstroEventGenerator.generateEvents(
            catalog, range, baseFacts, provenance, factsForSlice,
        )
        return com.aynvora.core.astrology.prediction.AstroEventSnapshotResult(
            snapshot.withEventOccurrences(generation.occurrences), generation,
        )
    }

    /** Exact/token search is local and deterministic; semantic search is optional host infrastructure. */
    fun searchKnowledge(
        query: String,
        chunks: List<com.aynvora.core.astrology.prediction.KnowledgeChunk>,
        filters: com.aynvora.core.astrology.prediction.KnowledgeSearchFilters = com.aynvora.core.astrology.prediction.KnowledgeSearchFilters(),
        limit: Int = 20,
        semanticSearch: com.aynvora.core.astrology.prediction.KnowledgeSemanticSearch? = null,
    ) = com.aynvora.core.astrology.prediction.KnowledgeSearchEngine.search(query, chunks, filters, limit, semanticSearch)

    /** Search local source-backed chunks with lexical and optional host semantic retrieval. */
    fun search(query: String, chunks: List<com.aynvora.core.astrology.prediction.KnowledgeChunk>, filters: com.aynvora.core.astrology.prediction.KnowledgeSearchFilters = com.aynvora.core.astrology.prediction.KnowledgeSearchFilters(), limit: Int = 20) =
        searchKnowledge(query, chunks, filters, limit)

    /** Resolve registered source metadata without fetching source content. */
    fun getSource(sourceId: String, registry: com.aynvora.core.astrology.knowledge.AstroKnowledgeSourceRegistry) = registry.find(sourceId)

    /** Build provenance-preserving local and optional remote evidence. */
    fun getEvidence(query: String, chunks: List<com.aynvora.core.astrology.prediction.KnowledgeChunk>, sources: com.aynvora.core.astrology.knowledge.AstroKnowledgeSourceRegistry, rules: List<com.aynvora.core.astrology.prediction.KnowledgeRule> = emptyList(), deterministic: List<com.aynvora.core.astrology.knowledge.AstroEvidenceItem> = emptyList(), web: List<com.aynvora.core.astrology.knowledge.WebEvidence> = emptyList()) =
        com.aynvora.core.astrology.knowledge.KnowledgeRetriever(sources).retrieve(query, chunks, rules, deterministic = deterministic, web = web)

    /** Prepare a page-aware grounded request. Model inference/tool dispatch is host-owned. */
    fun ask(question: String, pageContext: com.aynvora.core.astrology.knowledge.AstroPageContext, mode: com.aynvora.core.astrology.knowledge.AstroAnswerMode, evidence: com.aynvora.core.astrology.knowledge.AstroEvidenceBundle) =
        com.aynvora.core.astrology.knowledge.GroundedAstroAsk(question, pageContext, mode, evidence)

    /** Optional remote provider; returns explicit REMOTE_WEB evidence only when configured. */
    suspend fun searchWeb(query: String, provider: com.aynvora.core.astrology.knowledge.RemoteWebSearchProvider?, retrievedAtEpochMs: Long, traditionId: String? = null) =
        provider?.search(query, retrievedAtEpochMs, traditionId).orEmpty()

    /** The signed-off Tajika V1 rules, page citations, source records, and CC BY-SA attribution. */
    fun getTajikaKnowledgePack() = com.aynvora.core.astrology.knowledge.tajika.TajikaKnowledgePack.v1()

    /** Calculate Muntha from the birth chart and solar-return cycle; never accepts a precomputed Muntha fact. */
    suspend fun getMuntha(request: ChartRequest, targetYear: Int) = calculateVarshaphal(request, targetYear)

    /** Evaluate the sourced Sun-as-Varshesha subset from explicit annual chart facts. */
    fun getVarsheshaSun(strength: String, natalSunStrength: String? = null) =
        com.aynvora.core.astrology.knowledge.tajika.TajikaRuleEngine.evaluateVarsheshaSun(strength, natalSunStrength)

    /** Run the registered-tool, evidence-fusion, and grounded-local-AI pipeline supplied by the host. */
    suspend fun executeTool(
        executor: com.aynvora.core.astrology.knowledge.AynvoraAiToolExecutor,
        question: String,
        context: com.aynvora.core.astrology.knowledge.AstroPageContext,
        locale: String = "en",
        mode: com.aynvora.core.astrology.knowledge.AstroAnswerMode = com.aynvora.core.astrology.knowledge.AstroAnswerMode.NORMAL,
        includeWeb: Boolean = false,
        requestTimestampEpochMs: Long,
    ) = executor.ask(question, context, locale, mode, includeWeb, requestTimestampEpochMs)

    /** Catalog loaded from the host's bundled asset; null means offline search was not configured. */
    val locationCatalog: com.aynvora.core.models.OfflineLocationCatalog? get() = null

    fun searchCountries(query: String = ""): List<com.aynvora.core.models.CanonicalCountry> = locationCatalog?.searchCountries(query).orEmpty()
    fun searchStates(countryCode: String, query: String = ""): List<com.aynvora.core.models.CanonicalState> = locationCatalog?.searchStates(countryCode, query).orEmpty()
    fun searchCities(stateCode: String, countryCode: String? = null, query: String = ""): List<com.aynvora.core.models.CanonicalLocation> = locationCatalog?.searchCities(stateCode, countryCode, query).orEmpty()
    fun findCity(cityName: String, countryCode: String? = null, stateCode: String? = null): com.aynvora.core.models.LocationNameLookup =
        locationCatalog?.findByCityName(cityName, countryCode, stateCode) ?: com.aynvora.core.models.LocationNameLookup(emptyList())
    fun resolveLocation(canonicalId: String): com.aynvora.core.models.CanonicalLocation? = locationCatalog?.resolve(canonicalId)

    /** Grouped astrology request API for date-sensitive and selective operations. */
    val astrology: AstrologySdkFacade get() = AstrologySdkFacade(this)

    /**
     * Calculates an astrological chart from a structured, validated request.
     */
    suspend fun calculateChart(request: ChartRequest): AynvoraResult<ChartResult>

    /** Calculates the solar return and shared annual AstroChart, marking unsupported Tajika parts explicitly. */
    suspend fun calculateVarshaphal(
        request: ChartRequest,
        targetYear: Int,
    ): AynvoraResult<com.aynvora.core.astrology.knowledge.tajika.VarshaphalResult>

    /** Runs only requested registered core features and their shared dependencies. IDs are registry IDs, e.g. `vedic.dignities`. */
    suspend fun calculateFeatures(
        request: ChartRequest,
        featureIds: Set<String>,
    ): AynvoraResult<com.aynvora.core.models.AstrologyFeatureCalculation>

    /** Runs registry features with explicit observation/request inputs through the same pipeline. */
    suspend fun calculateFeaturesWithInputs(
        request: ChartRequest,
        featureIds: Set<String>,
        inputs: com.aynvora.astro.pipeline.FeatureOutputs,
    ): AynvoraResult<com.aynvora.core.models.AstrologyFeatureCalculation> = calculateFeatures(request, featureIds)

    /** Calculates one feature and its required upstream dependencies through the same registry/pipeline. */
    suspend fun calculateFeature(
        request: ChartRequest,
        featureId: String,
    ): AynvoraResult<com.aynvora.core.models.AstrologyFeatureCalculation> =
        calculateFeatures(request, setOf(featureId))

    /** Reads and decodes an already persisted snapshot without invoking calculation engines. */
    suspend fun getSnapshot(chartId: String): AynvoraResult<SavedKundaliSnapshot> = openSavedKundaliSnapshot(chartId)

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

    /** Request-based Dasha calculation sourced from one canonical natal chart calculation. */
    suspend fun calculateDasha(request: com.aynvora.core.models.DashaRequest) = calculateDashaRequest(request)

    /**
     * Calculates planetary transit snapshot for a specific Julian Day.
     */
    fun calculateTransit(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): com.aynvora.astro.transit.TransitSnapshot =
        com.aynvora.astro.transit.TransitCalculator.calculateSnapshot(jd, ayanamsaConvention)

    /** Calculates transit through the shared pipeline using explicit natal and observation contexts. */
    suspend fun calculateTransit(request: com.aynvora.core.models.TransitRequest) = calculateTransitRequest(request)

    /**
     * Calculates 5-limb classical Panchang snapshot for an exact moment.
     */
    fun calculatePanchang(
        jd: Double,
        ayanamsaConvention: String = "LAHIRI_CHITRAPAKSHA",
    ): com.aynvora.astro.panchang.PanchangSnapshot =
        com.aynvora.astro.panchang.PanchangCalculator.calculate(jd, ayanamsaConvention)

    /** Calculates Panchang through the shared pipeline using an explicit observation context. */
    suspend fun calculatePanchang(request: com.aynvora.core.models.PanchangRequest) = calculatePanchangRequest(request)

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
    val numerology: com.aynvora.core.numerology.NumerologyRepository? get() = null
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
     * Calculates a complete, deterministic Numerology profile from a structured request.
     */
    suspend fun calculateNumerology(
        request: com.aynvora.core.numerology.NumerologyRequest
    ): AynvoraResult<com.aynvora.core.numerology.NumerologyResult> =
        numerology?.calculate(request)
            ?: com.aynvora.core.numerology.NumerologyCalculationEngine.calculate(request)

    /**
     * Convenience overload calculating numerology directly from birth date and optional full name and ruleset.
     */
    suspend fun calculateNumerology(
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        fullName: String? = null,
        rulesetId: String = com.aynvora.core.numerology.NumerologyRuleset.CHALDEAN_CHEIRO_V1.id,
    ): AynvoraResult<com.aynvora.core.numerology.NumerologyResult> =
        calculateNumerology(
            com.aynvora.core.numerology.NumerologyRequest(
                birthDay = birthDay,
                birthMonth = birthMonth,
                birthYear = birthYear,
                fullName = fullName,
                rulesetId = rulesetId,
            )
        )

    /**
     * Analytics tracker used to report non-PII usage events.
     * Defaults to [NoOpAnalyticsTracker] when not provided.
     */
    val analytics: AnalyticsTracker get() = NoOpAnalyticsTracker()
}

class AstrologySdkFacade internal constructor(private val sdk: AynvoraSdk) {
    fun calculators() = sdk.getAstroCalculatorRegistry()
    fun getAdvancedAstrologyCapabilities() = sdk.getAdvancedAstrologyCapabilities()
    fun calculateEventOccurrences(
        snapshot: com.aynvora.core.models.KundaliSnapshot,
        requests: List<com.aynvora.core.astrology.prediction.AstroEventEvaluationRequest>,
    ) = sdk.calculateEventOccurrences(snapshot, requests)
    fun events(
        snapshot: com.aynvora.core.models.KundaliSnapshot,
        catalog: com.aynvora.core.astrology.prediction.AstroEventCatalog,
        range: com.aynvora.core.astrology.prediction.AstroEventObservationRange,
        baseFacts: List<com.aynvora.core.astrology.prediction.AstroEventFact>,
        provenance: com.aynvora.astro.provenance.CalculationMetadata,
        factsForSlice: (com.aynvora.core.astrology.prediction.AstroEventDefinition, com.aynvora.core.astrology.prediction.AstroEventTimeSlice) -> List<com.aynvora.core.astrology.prediction.AstroEventFact>,
    ) = sdk.generateEvents(snapshot, catalog, range, baseFacts, provenance, factsForSlice)
    fun search(
        query: String,
        chunks: List<com.aynvora.core.astrology.prediction.KnowledgeChunk>,
        filters: com.aynvora.core.astrology.prediction.KnowledgeSearchFilters = com.aynvora.core.astrology.prediction.KnowledgeSearchFilters(),
        limit: Int = 20,
        semanticSearch: com.aynvora.core.astrology.prediction.KnowledgeSemanticSearch? = null,
    ) = sdk.searchKnowledge(query, chunks, filters, limit, semanticSearch)
    suspend fun calculateFeature(request: ChartRequest, featureId: String) = sdk.calculateFeature(request, featureId)
    suspend fun calculateFeatures(request: ChartRequest, featureIds: Set<String>) = sdk.calculateFeatures(request, featureIds)
    suspend fun calculateTransit(request: com.aynvora.core.models.TransitRequest) = sdk.calculateTransit(request)
    suspend fun calculatePanchang(request: com.aynvora.core.models.PanchangRequest) = sdk.calculatePanchang(request)
    suspend fun calculateDasha(request: com.aynvora.core.models.DashaRequest) = sdk.calculateDasha(request)
    suspend fun calculateKundali(
        request: ChartRequest,
        profileId: String,
        profileName: String,
        genderId: String? = null,
    ) = sdk.generateKundaliSnapshot(request, profileId, profileName, genderId)

    suspend fun getSnapshot(chartId: String) = sdk.getSnapshot(chartId)
    fun getChart(snapshot: com.aynvora.core.models.KundaliSnapshot, chartId: String) = snapshot.getChart(chartId)
    fun exportJson(snapshot: com.aynvora.core.models.KundaliSnapshot) = snapshot.exportJson()
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
        numerology: com.aynvora.core.numerology.NumerologyRepository? = null,
        analyticsTracker: AnalyticsTracker = NoOpAnalyticsTracker(),
        locationCatalog: com.aynvora.core.models.OfflineLocationCatalog? = null,
    ): AynvoraSdk = DefaultAynvoraSdk(
        adapter = AstroEngineAdapter(),
        userProfiles = userProfiles,
        birthProfiles = birthProfiles,
        savedCharts = savedCharts,
        userPreferences = userPreferences,
        content = content,
        contentSync = contentSync,
        tarot = tarot,
        numerology = numerology,
        analyticsTrackerImpl = analyticsTracker,
        locationCatalog = locationCatalog,
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
    override val numerology: com.aynvora.core.numerology.NumerologyRepository? = null,
    private val analyticsTrackerImpl: AnalyticsTracker = NoOpAnalyticsTracker(),
    override val locationCatalog: com.aynvora.core.models.OfflineLocationCatalog? = null,
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

    override suspend fun calculateVarshaphal(
        request: ChartRequest,
        targetYear: Int,
    ) = adapter.calculateVarshaphal(request, targetYear)

    override suspend fun calculateFeatures(
        request: ChartRequest,
        featureIds: Set<String>,
    ): AynvoraResult<com.aynvora.core.models.AstrologyFeatureCalculation> = adapter.executeFeatures(request, featureIds)

    override suspend fun calculateFeaturesWithInputs(
        request: ChartRequest,
        featureIds: Set<String>,
        inputs: com.aynvora.astro.pipeline.FeatureOutputs,
    ): AynvoraResult<com.aynvora.core.models.AstrologyFeatureCalculation> = adapter.executeFeatures(request, featureIds, inputs)

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

    override suspend fun calculateNumerology(
        request: com.aynvora.core.numerology.NumerologyRequest
    ): AynvoraResult<com.aynvora.core.numerology.NumerologyResult> {
        val rulesetId = request.rulesetId
        analyticsTrackerImpl.track(AnalyticsEvent.NumerologyCalculationStarted(rulesetId))
        val result = numerology?.calculate(request)
            ?: com.aynvora.core.numerology.NumerologyCalculationEngine.calculate(request)
        when (result) {
            is AynvoraResult.Success ->
                analyticsTrackerImpl.track(AnalyticsEvent.NumerologyCalculationCompleted(rulesetId))

            is AynvoraResult.Failure ->
                analyticsTrackerImpl.track(AnalyticsEvent.NumerologyCalculationFailed(result.analyticsErrorCode()))
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
    is AynvoraResult.Failure.Structured -> "structured_error"
}
