package com.aynvora.astro.pipeline

import com.aynvora.astro.context.AstroCalculationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Full structural key for deterministic feature outputs. */
data class AstroCalculationCacheKey(
    val calculatorId: String,
    val calculatorVersion: String,
    val contextInputs: List<String>,
    val dependencySignatures: List<String>,
)

interface AstroCalculationCache {
    suspend fun get(key: AstroCalculationCacheKey): FeatureOutput<*>?
    suspend fun put(key: AstroCalculationCacheKey, value: FeatureOutput<*>)
}

/** Bounded process-local cache. It stores calculation structures only, never generated prose. */
class InMemoryAstroCalculationCache(private val maxEntries: Int = 256) : AstroCalculationCache {
    private val mutex = Mutex()
    private val values = linkedMapOf<AstroCalculationCacheKey, FeatureOutput<*>>()

    init { require(maxEntries > 0) }

    override suspend fun get(key: AstroCalculationCacheKey): FeatureOutput<*>? = mutex.withLock { values[key] }

    override suspend fun put(key: AstroCalculationCacheKey, value: FeatureOutput<*>) = mutex.withLock {
        if (value.status == FeatureStatus.FAILED) return@withLock
        values.remove(key)
        values[key] = value
        while (values.size > maxEntries) values.remove(values.keys.first())
    }
}

internal fun AstroCalculationCacheKey(
    context: AstroCalculationContext,
    engine: AstroFeatureEngine<*>,
    inputs: FeatureOutputs,
): AstroCalculationCacheKey {
    val birth = context.birthData
    val config = context.config
    val normalized = context.normalizedUtc
    val contextInputs = listOf(
        birth.dateTimeIso, birth.year?.toString().orEmpty(), birth.month?.toString().orEmpty(),
        birth.day?.toString().orEmpty(), birth.hour?.toString().orEmpty(), birth.minute?.toString().orEmpty(),
        birth.second?.toString().orEmpty(), birth.latitude.toString(), birth.longitude.toString(), birth.timeZoneId,
        birth.countryCode.orEmpty(), birth.stateCode.orEmpty(), birth.cityId.orEmpty(),
        birth.locationDatasetVersion.orEmpty(), birth.locationProvenance.orEmpty(),
        normalized.year.toString(), normalized.month.toString(), normalized.day.toString(),
        normalized.hour.toString(), normalized.minute.toString(), normalized.second.toString(),
        normalized.timezoneOffsetMinutes.toString(), normalized.julianDay.value.toString(),
        com.aynvora.astro.time.TimeNormalizer.TIMEZONE_DATA_VERSION,
        config.profile, config.ayanamsa, config.houseSystem, config.vargaRulesetId,
        config.requestedDivisionalCharts.map { it.name }.sorted().joinToString(","),
        config.ashtakavargaRulesetId, context.providerId, context.providerVersion, context.engineVersion,
        context.calculationContractVersion, context.dataVersion.orEmpty(),
    )
    val dependencies = engine.dependencies.map { dependency ->
        val result = inputs.asMap()[dependency.id]
            ?: error("Cannot build cache key; dependency '${dependency.id}' is missing")
        val provenance = result.provenance
        listOf(
            dependency.id, result.featureVersion, result.status.name,
            provenance.contractVersion, provenance.calculationProfileId, provenance.engineVersion,
            provenance.calculationModel, provenance.ephemerisSourceId,
            provenance.ephemerisDataVersion.orEmpty(), provenance.conventions.toList().sortedBy { it.first }.joinToString(";") { "${it.first}=${it.second}" },
            result.evidence.sorted().joinToString(","),
        ).joinToString("|")
    }.sorted()
    return AstroCalculationCacheKey(
        calculatorId = engine.featureId,
        calculatorVersion = engine.featureVersion,
        contextInputs = contextInputs,
        dependencySignatures = dependencies,
    )
}
