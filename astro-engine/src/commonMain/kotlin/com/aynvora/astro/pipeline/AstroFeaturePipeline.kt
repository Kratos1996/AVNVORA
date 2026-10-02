package com.aynvora.astro.pipeline

import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.provenance.CalculationMetadata

@kotlinx.serialization.Serializable
enum class FeatureStatus { SUPPORTED, PARTIAL, UNSUPPORTED, NOT_VERIFIED, AMBIGUOUS, FAILED }

@kotlinx.serialization.Serializable
data class FeatureWarning(val code: String, val message: String)

@kotlinx.serialization.Serializable
data class FeatureOutput<T>(
    val value: T?,
    val status: FeatureStatus,
    val provenance: CalculationMetadata,
    val warnings: List<FeatureWarning> = emptyList(),
    val featureId: String = provenance.conventions["feature_id"] ?: "unspecified",
    val featureVersion: String = "1",
    val dependencyIds: List<String> = emptyList(),
    val evidence: List<String> = emptyList(),
) {
    val calculationContractVersion: String get() = provenance.contractVersion
    init {
        require(status in setOf(FeatureStatus.UNSUPPORTED, FeatureStatus.NOT_VERIFIED, FeatureStatus.AMBIGUOUS, FeatureStatus.FAILED) || value != null) {
            "A supported feature output must include a value"
        }
    }
}

/** Stable typed key for sharing one upstream calculation between feature engines. */
data class FeatureKey<T>(val id: String)

class FeatureOutputs internal constructor(private val values: Map<String, FeatureOutput<*>>) {
    internal val isEmpty: Boolean get() = values.isEmpty()
    fun asMap(): Map<String, FeatureOutput<*>> = values.toMap()
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: FeatureKey<T>): FeatureOutput<T> =
        values[key.id] as? FeatureOutput<T>
            ?: error("Required feature output '${key.id}' is missing")

    internal fun with(key: FeatureKey<*>, output: FeatureOutput<*>): FeatureOutputs {
        require(key.id !in values) { "Feature output '${key.id}' was already produced" }
        return FeatureOutputs(values + (key.id to output))
    }

    /** Adds an explicit request/context value to seed a selective pipeline run. */
    fun <T> withInput(key: FeatureKey<T>, value: T, provenance: CalculationMetadata): FeatureOutputs =
        with(key, FeatureOutput(value, FeatureStatus.SUPPORTED, provenance, featureId = key.id))

    companion object { val Empty = FeatureOutputs(emptyMap()) }
}

interface AstroFeatureEngine<T> {
    val output: FeatureKey<T>
    val featureId: String get() = output.id
    val featureVersion: String get() = "1"
    val supportedCalculationProfiles: Set<String> get() = setOf("STANDARD_VEDIC")
    val dependencies: Set<FeatureKey<*>>
    suspend fun calculate(context: AstroCalculationContext, inputs: FeatureOutputs): FeatureOutput<T>
}

@kotlinx.serialization.Serializable
data class FeatureExecutionTrace(
    val requestedFeatures: List<String>,
    val resolvedDependencies: List<String>,
    val executionOrder: List<String>,
    val executedOnceCount: Int,
    val reusedCount: Int,
    val skippedUnsupportedFeatures: List<String>,
    val executionDurationNanos: Long,
    val warnings: List<FeatureWarning> = emptyList(),
    val errors: List<String> = emptyList(),
    val engineExecutionDurationNanos: Map<String, Long> = emptyMap(),
    val duplicateCalculationsAvoided: Int = 0,
    val cacheHitCount: Int = 0,
)

data class FeatureCalculation<T>(val outputs: FeatureOutputs, val result: FeatureOutput<T>, val trace: FeatureExecutionTrace)
data class FeatureSetCalculation(val outputs: FeatureOutputs, val trace: FeatureExecutionTrace)

/** Executes independent feature engines in dependency order and shares typed results per calculation. */
class AstroFeaturePipeline(
    engines: List<AstroFeatureEngine<*>>,
    private val cache: AstroCalculationCache? = null,
) {
    private val ordered = order(engines)
    private val byId = ordered.associateBy { it.output.id }

    suspend fun calculate(
        context: AstroCalculationContext,
        seed: FeatureOutputs = FeatureOutputs.Empty,
    ): FeatureOutputs {
        return calculateRequested(context, ordered.map { it.output.id }.toSet(), seed).outputs
    }

    suspend fun <T> calculateFeature(context: AstroCalculationContext, feature: FeatureKey<T>, seed: FeatureOutputs = FeatureOutputs.Empty): FeatureCalculation<T> {
        val calculation = calculateRequested(context, setOf(feature.id), seed)
        return FeatureCalculation(calculation.outputs, calculation.outputs.get(feature), calculation.trace)
    }

    suspend fun calculateFeatures(context: AstroCalculationContext, features: Set<FeatureKey<*>>, seed: FeatureOutputs = FeatureOutputs.Empty): FeatureSetCalculation {
        val calculation = calculateRequested(context, features.map { it.id }.toSet(), seed)
        return FeatureSetCalculation(calculation.outputs, calculation.trace)
    }

    private suspend fun calculateRequested(context: AstroCalculationContext, requested: Set<String>, seed: FeatureOutputs): FeatureExecutionResult {
        require(requested.isNotEmpty()) { "At least one feature must be requested" }
        val required = linkedSetOf<String>()
        fun include(id: String) {
            if (id in required || runCatching { seed.getUntyped(FeatureKey<Any?>(id)) }.isSuccess) return
            val engine = byId[id] ?: error("No engine registered for requested feature '$id'")
            engine.dependencies.forEach { include(it.id) }
            required += id
        }
        requested.forEach(::include)
        val consumerCounts = required.flatMap { id -> byId.getValue(id).dependencies.map { it.id } }
            .groupingBy { it }.eachCount()
        val duplicateCalculationsAvoided = consumerCounts.values.sumOf { (it - 1).coerceAtLeast(0) }
        var outputs = seed
        val execution = mutableListOf<String>()
        val unsupported = mutableListOf<String>()
        val warnings = mutableListOf<FeatureWarning>()
        val engineDurations = linkedMapOf<String, Long>()
        var reused = 0
        var cacheHits = 0
        val allowCache = seed.isEmpty
        val start = kotlin.time.TimeSource.Monotonic.markNow()
        ordered.filter { it.output.id in required }.forEach { engine ->
            reused += engine.dependencies.count { dependency -> runCatching { outputs.getUntyped(dependency) }.isSuccess }
            val missing = engine.dependencies.filterNot { dependency ->
                runCatching { outputs.getUntyped(dependency) }.isSuccess
            }
            require(missing.isEmpty()) { "Feature '${engine.output.id}' has missing dependencies: ${missing.joinToString { it.id }}" }
            val cacheKey = if (allowCache && cache != null) AstroCalculationCacheKey(context, engine, outputs) else null
            val cached = if (cacheKey != null) cache?.get(cacheKey) else null
            if (cached != null) {
                warnings += cached.warnings
                outputs = outputs.with(engine.output, cached)
                execution += engine.output.id
                cacheHits++
                return@forEach
            }
            val engineStart = kotlin.time.TimeSource.Monotonic.markNow()
            val result = engine.calculate(context, outputs).copy(
                featureId = engine.featureId,
                featureVersion = engine.featureVersion,
                dependencyIds = engine.dependencies.map { it.id },
            )
            engineDurations[engine.output.id] = engineStart.elapsedNow().inWholeNanoseconds
            if (result.status == FeatureStatus.UNSUPPORTED) unsupported += engine.output.id
            warnings += result.warnings
            outputs = outputs.with(engine.output, result)
            execution += engine.output.id
            if (cacheKey != null) cache?.put(cacheKey, result)
        }
        return FeatureExecutionResult(outputs, FeatureExecutionTrace(
            requestedFeatures = requested.sorted(),
            resolvedDependencies = required.filterNot { it in requested }.sorted(),
            executionOrder = execution,
            executedOnceCount = execution.size - cacheHits,
            reusedCount = reused,
            skippedUnsupportedFeatures = unsupported,
            executionDurationNanos = start.elapsedNow().inWholeNanoseconds,
            warnings = warnings,
            engineExecutionDurationNanos = engineDurations,
            duplicateCalculationsAvoided = duplicateCalculationsAvoided,
            cacheHitCount = cacheHits,
        ))
    }

    private fun order(engines: List<AstroFeatureEngine<*>>): List<AstroFeatureEngine<*>> {
        require(engines.map { it.output.id }.distinct().size == engines.size) { "Feature output IDs must be unique" }
        val byId = engines.associateBy { it.output.id }
        val ordered = mutableListOf<AstroFeatureEngine<*>>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        fun visit(engine: AstroFeatureEngine<*>) {
            if (engine.output.id in visited) return
            require(visiting.add(engine.output.id)) { "Cycle detected at feature '${engine.output.id}'" }
            engine.dependencies.forEach { dependency ->
                byId[dependency.id]?.let(::visit)
            }
            visiting.remove(engine.output.id)
            visited.add(engine.output.id)
            ordered += engine
        }
        engines.forEach(::visit)
        return ordered
    }
}

private data class FeatureExecutionResult(val outputs: FeatureOutputs, val trace: FeatureExecutionTrace)

private fun FeatureOutputs.getUntyped(key: FeatureKey<*>): FeatureOutput<*> =
    get(FeatureKey<Any?>(key.id))
