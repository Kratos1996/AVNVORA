package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Public request model encapsulating input birth data and calculation configuration.
 */
@Serializable
data class ChartRequest(
    val birthData: BirthData,
    val config: CalculationConfig = CalculationConfig(),
)
