package com.aynvora.core.models

import com.aynvora.astro.dasha.VimshottariDashaTimeline
import com.aynvora.astro.panchang.PanchangSnapshot
import com.aynvora.astro.transit.TransitSnapshot
import kotlinx.serialization.Serializable

/** Versioned, offline-ready aggregate emitted after one Kundali generation. */
@Serializable
data class KundaliSnapshot(
    val schemaVersion: String = CURRENT_SCHEMA_VERSION,
    val profileId: String,
    val profileName: String,
    val genderId: String? = null,
    val birth: KundaliBirthDetails,
    val calculation: CalculationMetadata,
    val natalChart: ChartResult,
    val charts: List<AstroChartSnapshot>,
    val dasha: VimshottariDashaTimeline?,
    /** Transit positions at the chart's birth timestamp; not a current-date prediction. */
    val transitAtBirth: TransitSnapshot?,
    val panchang: PanchangSnapshot?,
    val tables: List<AstroTableSnapshot>,
    val availability: List<AstrologySectionAvailability>,
    /** Per-feature calculation records persisted alongside the aggregate snapshot. */
    val featureResults: Map<String, AstroFeatureRecord> = emptyMap(),
) {
    init {
        require(schemaVersion == CURRENT_SCHEMA_VERSION)
        require(profileId.isNotBlank())
        require(profileName.isNotBlank())
        require(charts.map { it.chartId }.distinct().size == charts.size) { "Duplicate chart IDs" }
        require(tables.map { it.sectionId }.distinct().size == tables.size) { "Duplicate table section IDs" }
        require(availability.map { it.sectionId }.distinct().size == availability.size) { "Duplicate section IDs" }
    }

    companion object { const val CURRENT_SCHEMA_VERSION = "1" }
}

@Serializable
data class AstroFeatureRecord(
    val featureId: String,
    val featureVersion: String,
    val status: AstroFeatureStatus,
    val dataRef: String?,
    val dependencyIds: List<String>,
    val provenance: CalculationMetadata,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class KundaliBirthDetails(
    val profileId: String,
    val profileName: String,
    val genderId: String? = null,
    val localDate: String,
    val localTime: String,
    val timezoneId: String,
    val timezoneOffsetMinutes: Int,
    val utcTimestamp: String,
    val julianDay: Double,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val countryCode: String? = null,
    val state: String? = null,
    val stateCode: String? = null,
    val city: String,
    val cityId: String? = null,
    val locationSource: String = "CALLER_SUPPLIED",
    val locationResolutionStatus: AstroFeatureStatus = AstroFeatureStatus.NOT_VERIFIED,
    val timezoneDataVersion: String? = null,
    val ayanamsaId: String,
    val houseSystemId: String,
    val calculationProfileId: String,
    val nodeConventionId: String,
)

@Serializable
data class AstroChartSnapshot(
    val chartId: String,
    val chartTypeId: String,
    val titleKey: String,
    val zodiacModeId: String,
    val houseSystemId: String,
    val status: CalculationAvailability,
    val ascendantSignIndex: Int? = null,
    val houses: List<AstroChartHouse>,
    val placements: List<AstroChartPlacement>,
    val sourceMetadata: CalculationMetadata,
)

@Serializable
data class AstroChartHouse(
    val houseNumber: Int,
    val signIndex: Int?,
    val startLongitude: Double? = null,
    val cuspLongitude: Double? = null,
    val endLongitude: Double? = null,
)

@Serializable
data class AstroChartPlacement(
    val bodyId: String,
    val houseNumber: Int?,
    val signIndex: Int,
    val longitude: Double,
    val degreeInSign: Double,
    val nakshatraId: String?,
    val nakshatraIndex: Int?,
    val pada: Int?,
    val retrograde: Boolean,
    val combust: Boolean,
    val exalted: Boolean? = null,
    val debilitated: Boolean? = null,
    val vargottama: Boolean? = null,
    val dignityId: String? = null,
    val sourceId: String,
)

@Serializable
data class AstroTableSnapshot(
    val sectionId: String,
    val titleKey: String,
    val columns: List<AstroTableColumn>,
    val rows: List<AstroTableRow>,
    val status: CalculationAvailability,
)

@Serializable
data class AstroTableColumn(val id: String, val labelKey: String, val semanticType: AstroValueType)

@Serializable
data class AstroTableRow(val id: String, val cells: List<AstroTableCell>)

@Serializable
data class AstroTableCell(
    val semanticType: AstroValueType,
    val canonicalId: String? = null,
    val numericValue: Double? = null,
    val integerValue: Int? = null,
    val textValue: String? = null,
)

@Serializable
enum class AstroValueType { BODY, SIGN, NAKSHATRA, ANGLE, INTEGER, BOOLEAN, TEXT, ENUM }

@Serializable
enum class CalculationAvailability { AVAILABLE, PARTIAL, UNSUPPORTED, COMING_SOON }

@Serializable
data class AstrologySectionAvailability(
    val sectionId: String,
    val titleKey: String,
    val availability: CalculationAvailability,
    val dataRef: String? = null,
)
