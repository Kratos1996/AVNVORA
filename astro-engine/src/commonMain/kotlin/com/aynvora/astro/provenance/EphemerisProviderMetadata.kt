package com.aynvora.astro.provenance

import com.aynvora.astro.BodyId
import kotlinx.serialization.Serializable

@Serializable
enum class EphemerisLicenseStatus { VERIFIED, NOT_VERIFIED, REFERENCE_ONLY }

/** Explicit identity and limits for the numerical source behind position calculations. */
@Serializable
data class EphemerisProviderMetadata(
    val providerId: String,
    val version: String,
    val source: String,
    val supportedBodies: List<BodyId>,
    val supportedEpochStartJulianDay: Double?,
    val supportedEpochEndJulianDay: Double?,
    val precisionNotes: String,
    val license: String,
    val redistributionStatus: EphemerisLicenseStatus,
    val provenance: String,
) {
    init {
        require(providerId.isNotBlank() && version.isNotBlank() && source.isNotBlank())
        require((supportedEpochStartJulianDay == null) == (supportedEpochEndJulianDay == null))
        require(supportedEpochStartJulianDay == null || supportedEpochStartJulianDay <= supportedEpochEndJulianDay!!)
    }

    companion object {
        val AnalyticalMeeusSimon = EphemerisProviderMetadata(
            providerId = "ANALYTICAL_MEEUS_SIMON_FORMULAE",
            version = CalculationMetadata.ENGINE_VERSION,
            source = "Meeus analytical formula subsets and Simon et al. orbital elements as implemented in AYNVORA astro-engine",
            supportedBodies = BodyId.entries,
            supportedEpochStartJulianDay = null,
            supportedEpochEndJulianDay = null,
            precisionNotes = "No continuous validated epoch range or end-to-end error bound is established; see Calculation Contract V1.",
            license = "Implementation and source-derived coefficient redistribution rights have not been independently verified.",
            redistributionStatus = EphemerisLicenseStatus.NOT_VERIFIED,
            provenance = "astro-engine source; docs/CALCULATION_CONTRACT_V1.md",
        )
    }
}
