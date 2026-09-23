package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Public metadata describing the underlying calculation engine capabilities and version.
 */
@Serializable
data class EngineMetadata(
    val engineVersion: String,
    val buildNumber: String,
    val isDeterministic: Boolean = true,
    val supportedDomains: List<String> = listOf("PLANETARY_POSITIONS", "ASCENDANT", "HOUSES"),
)
