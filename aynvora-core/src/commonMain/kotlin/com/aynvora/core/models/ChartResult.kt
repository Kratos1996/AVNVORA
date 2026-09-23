package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Public calculation result returned by the AYNVORA SDK.
 * Decoupled from internal Astro Engine data structures.
 */
@Serializable
data class ChartResult(
    val engineVersion: String,
    val calculationStatus: String,
    val birthData: BirthData,
    val config: CalculationConfig,
    val calculationModel: String = "MEEUS_VSOP87",
    val julianDay: Double = 0.0,
    val ayanamsaDegrees: Double = 0.0,
    val planetaryPositions: List<PlanetaryPosition> = emptyList(),
)
