package com.aynvora.astro.provenance

import kotlinx.serialization.Serializable

/** Immutable provenance shared by deterministic calculation outputs. */
@Serializable
data class CalculationMetadata(
    val contractVersion: String = CURRENT_CONTRACT_VERSION,
    val calculationProfileId: String,
    val engineVersion: String = ENGINE_VERSION,
    val calculationModel: String,
    val ephemerisSourceId: String = CURRENT_EPHEMERIS_SOURCE_ID,
    val ephemerisDataVersion: String? = null,
    val commercialRedistributionStatus: CommercialRedistributionStatus =
        CommercialRedistributionStatus.NOT_VERIFIED,
    val conventions: Map<String, String> = emptyMap(),
) {
    companion object {
        const val CURRENT_CONTRACT_VERSION = "V1"
        const val ENGINE_VERSION = "0.3.0"
        const val CURRENT_EPHEMERIS_SOURCE_ID = "ANALYTICAL_MEEUS_SIMON_FORMULAE"
    }
}

@Serializable
enum class CommercialRedistributionStatus {
    VERIFIED,
    NOT_VERIFIED,
    UNKNOWN,
}
