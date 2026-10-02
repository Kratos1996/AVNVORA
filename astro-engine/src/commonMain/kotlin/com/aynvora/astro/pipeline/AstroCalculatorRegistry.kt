package com.aynvora.astro.pipeline

import com.aynvora.astro.varga.VargaEngine
import kotlinx.serialization.Serializable

/** Queryable metadata for a registered calculator. It describes an existing engine; it is not a second registry. */
@Serializable
data class AstroCalculatorDescriptor(
    val calculatorId: String,
    val featureId: String,
    val version: String,
    val dependencies: List<String>,
    val supportedProfiles: Set<String>,
    val inputType: String,
    val outputType: String,
    val status: FeatureStatus,
)

object AstroCalculatorRegistry {
    fun all(): List<AstroCalculatorDescriptor> = all(com.aynvora.astro.varga.DefaultVargaEngine())

    fun all(vargaEngine: VargaEngine): List<AstroCalculatorDescriptor> {
        val statuses = CoreAstroFeatureRegistry.descriptors(vargaEngine).associate { it.featureId to it.status }
        return CoreAstroFeatureRegistry.engines(vargaEngine).map { engine ->
            AstroCalculatorDescriptor(
                calculatorId = calculatorIdFor(engine.featureId),
                featureId = engine.featureId,
                version = engine.featureVersion,
                dependencies = engine.dependencies.map { it.id }.sorted(),
                supportedProfiles = engine.supportedCalculationProfiles,
                inputType = "AstroCalculationContext+FeatureOutputs",
                outputType = "FeatureOutput<${engine.featureId}>",
                status = statuses.getValue(engine.featureId),
            )
        }.sortedBy { it.calculatorId }
    }

    fun find(vargaEngine: VargaEngine, calculatorId: String): AstroCalculatorDescriptor? =
        all(vargaEngine).firstOrNull { it.calculatorId == calculatorId }

    fun find(calculatorId: String): AstroCalculatorDescriptor? = find(com.aynvora.astro.varga.DefaultVargaEngine(), calculatorId)

    fun calculatorIdFor(featureId: String): String =
        featureId.uppercase().replace('.', '_')
}

/** Typed, serializable projection of a feature output for SDK, tools, and report consumers. */
@Serializable
data class AstroCalculationResult<T>(
    val calculatorId: String,
    val featureId: String,
    val inputReference: String,
    val output: T?,
    val dependencies: List<String>,
    val evidenceIds: List<String>,
    val sourceRefs: List<String>,
    val provenance: com.aynvora.astro.provenance.CalculationMetadata,
    val version: String,
    val status: FeatureStatus,
    val warnings: List<FeatureWarning>,
)

fun <T> FeatureOutput<T>.toCalculationResult(inputReference: String, sourceRefs: List<String> = emptyList()) =
    AstroCalculationResult(
        calculatorId = AstroCalculatorRegistry.calculatorIdFor(featureId),
        featureId = featureId,
        inputReference = inputReference,
        output = value,
        dependencies = dependencyIds,
        evidenceIds = evidence,
        sourceRefs = sourceRefs.distinct().sorted(),
        provenance = provenance,
        version = featureVersion,
        status = status,
        warnings = warnings,
    )
