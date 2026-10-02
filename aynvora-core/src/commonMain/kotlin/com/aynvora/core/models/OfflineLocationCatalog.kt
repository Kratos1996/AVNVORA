package com.aynvora.core.models

import kotlinx.serialization.Serializable

/** A gazetteer entry identified by its stable source ID, never by city name alone. */
@Serializable
data class CanonicalLocation(
    val canonicalId: String,
    val cityName: String,
    val stateCode: String,
    val stateName: String,
    val countryCode: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val datasetVersion: String,
    val provenance: String,
) {
    /** Build the single calculation-context source used by chart, transit, and Panchang requests. */
    fun toBirthPlace(): BirthPlace = BirthPlace(
        name = cityName,
        coordinates = Coordinates(latitude, longitude),
        timezoneId = timezoneId,
        country = countryName,
        id = canonicalId,
        countryCode = countryCode,
        stateName = stateName,
        stateCode = stateCode,
        cityName = cityName,
        locationDatasetVersion = datasetVersion,
        locationProvenance = provenance,
    )
}

@Serializable
data class CanonicalCountry(val countryCode: String, val countryName: String, val cityCount: Int)

@Serializable
data class CanonicalState(val countryCode: String, val stateCode: String, val stateName: String, val cityCount: Int)

@Serializable
enum class LocationLookupStatus { NOT_FOUND, UNIQUE, AMBIGUOUS }

@Serializable
data class LocationNameLookup(val matches: List<CanonicalLocation>) {
    val status: LocationLookupStatus get() = when (matches.size) {
        0 -> LocationLookupStatus.NOT_FOUND
        1 -> LocationLookupStatus.UNIQUE
        else -> LocationLookupStatus.AMBIGUOUS
    }
}

/** Pure parser for the existing nine-column offline locations.tsv asset. */
class OfflineLocationCatalog private constructor(
    private val locations: List<CanonicalLocation>,
    val datasetVersion: String,
    val provenance: String,
) {
    fun searchCountries(query: String = ""): List<CanonicalCountry> = locations
        .asSequence().filter { query.isBlank() || it.countryName.contains(query, ignoreCase = true) || it.countryCode.contains(query, ignoreCase = true) }
        .groupBy { it.countryCode.uppercase() }
        .map { (_, rows) -> CanonicalCountry(rows.first().countryCode, rows.first().countryName, rows.size) }
        .sortedBy { it.countryName.lowercase() }

    fun searchStates(countryCode: String, query: String = ""): List<CanonicalState> = locations
        .asSequence().filter { it.countryCode.equals(countryCode, ignoreCase = true) && (query.isBlank() || it.stateName.contains(query, ignoreCase = true)) }
        .groupBy { it.countryCode.uppercase() to it.stateCode.uppercase() }
        .map { (_, rows) -> CanonicalState(rows.first().countryCode, rows.first().stateCode, rows.first().stateName, rows.size) }
        .sortedBy { it.stateName.lowercase() }

    fun searchCities(stateCode: String, countryCode: String? = null, query: String = ""): List<CanonicalLocation> = locations
        .filter { it.stateCode.equals(stateCode, ignoreCase = true) && (countryCode == null || it.countryCode.equals(countryCode, ignoreCase = true)) && (query.isBlank() || it.cityName.contains(query, ignoreCase = true)) }
        .sortedWith(compareBy<CanonicalLocation> { it.cityName.lowercase() }.thenBy { it.canonicalId })

    fun resolve(canonicalId: String): CanonicalLocation? = locations.singleOrNull { it.canonicalId == canonicalId }

    /** Name search may return many records; the caller must choose an ID explicitly. */
    fun findByCityName(cityName: String, countryCode: String? = null, stateCode: String? = null): LocationNameLookup {
        val matches = locations.filter {
            it.cityName.equals(cityName, ignoreCase = true) &&
                (countryCode == null || it.countryCode.equals(countryCode, ignoreCase = true)) &&
                (stateCode == null || it.stateCode.equals(stateCode, ignoreCase = true))
        }
        return LocationNameLookup(matches)
    }

    companion object {
        const val CURRENT_ASSET_SHA256 = "b9ef998d8772e66f8f03314f1ad843f2b7d2ab6366eb0e7933a95463c1ae18aa"
        const val CURRENT_ASSET_PATH = "design-system/src/commonMain/composeResources/files/locations.tsv"

        fun parse(
            tsv: String,
            datasetVersion: String = "sha256:$CURRENT_ASSET_SHA256",
            provenance: String = CURRENT_ASSET_PATH,
        ): OfflineLocationCatalog {
            require(datasetVersion.isNotBlank() && provenance.isNotBlank())
            val rows = tsv.lineSequence().filter { it.isNotBlank() }.mapIndexed { index, line ->
                val columns = line.split('\t')
                require(columns.size == 9) { "Location row ${index + 1} must have 9 tab-separated columns" }
                val id = columns[0]
                val city = columns[1]
                val stateCode = columns[2]
                val stateName = columns[3]
                val countryCode = columns[4]
                val countryName = columns[5]
                val lat = columns[6]
                val lon = columns[7]
                val zone = columns[8]
                require(id.isNotBlank() && city.isNotBlank() && countryCode.isNotBlank() && zone.isNotBlank()) {
                    "Location row ${index + 1} is missing an identity or timezone field"
                }
                val latitude = lat.toDoubleOrNull() ?: error("Location row ${index + 1} has invalid latitude")
                val longitude = lon.toDoubleOrNull() ?: error("Location row ${index + 1} has invalid longitude")
                require(latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                    "Location row ${index + 1} coordinates are outside valid ranges"
                }
                CanonicalLocation(id, city, stateCode, stateName, countryCode, countryName, latitude, longitude, zone, datasetVersion, provenance)
            }.toList()
            require(rows.isNotEmpty()) { "Location catalog is empty" }
            require(rows.map { it.canonicalId }.distinct().size == rows.size) { "Location catalog contains duplicate canonical IDs" }
            return OfflineLocationCatalog(rows, datasetVersion, provenance)
        }
    }
}
