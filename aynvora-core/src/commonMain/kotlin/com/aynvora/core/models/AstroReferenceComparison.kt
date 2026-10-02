package com.aynvora.core.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Comparison evidence is diagnostic; a reference provider is never promoted to calculation authority. */
@Serializable
enum class AstroReferenceComparisonStatus {
    EXACT_MATCH, WITHIN_TOLERANCE, MISMATCH, INPUT_NOT_EQUIVALENT, AMBIGUOUS, UNSUPPORTED, NOT_VERIFIED,
}

@Serializable
data class AstroReferenceInput(
    val date: String,
    val localTime: String,
    val timezoneId: String,
    val latitude: Double,
    val longitude: Double,
    val ayanamsha: String,
    val houseSystem: String,
    val calculationProfile: String,
    val requestedFeature: String,
    /** Explicit normalized instant, when the feature is date/time sensitive. */
    val requestedTimestamp: String? = null,
    /** Ephemeris/calculation epoch or time-scale tag required by the comparison provider. */
    val calculationEpoch: String? = null,
    val timezoneDataVersion: String? = null,
)

@Serializable
enum class AstroMismatchCategory { FORMULA_DIFFERENCE, PROVIDER_DIFFERENCE, CONVENTION_DIFFERENCE, TIME_DIFFERENCE, ROUNDING_DIFFERENCE, UNKNOWN }

/** Gate comparisons before inspecting numbers; any convention/input disagreement is non-comparable. */
object AstroReferenceInputGate {
    fun mismatchFields(left: AstroReferenceInput, right: AstroReferenceInput): List<String> = buildList {
        if (left.date != right.date) add("date")
        if (left.localTime != right.localTime) add("localTime")
        if (left.timezoneId != right.timezoneId) add("timezoneId")
        if (left.latitude != right.latitude) add("latitude")
        if (left.longitude != right.longitude) add("longitude")
        if (left.ayanamsha != right.ayanamsha) add("ayanamsha")
        if (left.houseSystem != right.houseSystem) add("houseSystem")
        if (left.calculationProfile != right.calculationProfile) add("calculationProfile")
        if (left.requestedFeature != right.requestedFeature) add("requestedFeature")
        if (left.requestedTimestamp != right.requestedTimestamp) add("requestedTimestamp")
        if (left.calculationEpoch != right.calculationEpoch) add("calculationEpoch")
        if (left.timezoneDataVersion != right.timezoneDataVersion) add("timezoneDataVersion")
    }
}

@Serializable
data class AstroReferenceComparison(
    val fixtureId: String,
    val input: AstroReferenceInput,
    val calculationProfile: String,
    val ayanamsha: String,
    val houseSystem: String,
    val providerA: String,
    val providerB: String,
    val actualA: JsonElement? = null,
    val actualB: JsonElement? = null,
    val difference: Double? = null,
    val tolerance: Double? = null,
    val status: AstroReferenceComparisonStatus,
    val explanation: String,
    val sourceRefs: List<String>,
    val authority: String = "REFERENCE_ONLY",
    val mismatchCategory: AstroMismatchCategory? = null,
)
