package com.aynvora.core.models

import kotlinx.serialization.Serializable

/**
 * Validated birth date component.
 */
@Serializable
data class BirthDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(year in 1..9999) { "Year must be between 1 and 9999, got: $year" }
        require(month in 1..12) { "Month must be between 1 and 12, got: $month" }
        require(day in 1..31) { "Day must be between 1 and 31, got: $day" }
    }

    fun toIsoDateString(): String =
        "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

/**
 * Validated birth time component (24-hour).
 */
@Serializable
data class BirthTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23, got: $hour" }
        require(minute in 0..59) { "Minute must be between 0 and 59, got: $minute" }
        require(second in 0..59) { "Second must be between 0 and 59, got: $second" }
    }

    fun toIsoTimeString(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}"
}

/**
 * Geographic coordinates with strict domain validation.
 */
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90.0 and 90.0, got: $latitude" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180.0 and 180.0, got: $longitude" }
    }
}

/**
 * Geographic birthplace specification.
 */
@Serializable
data class BirthPlace(
    val name: String = "",
    val coordinates: Coordinates,
    val timezoneId: String,
    val country: String? = null,
    val id: String? = null,
) {
    init {
        require(timezoneId.isNotBlank()) { "Timezone ID cannot be blank" }
    }
}

/**
 * Immutable, validated birth input model required for astrological calculations.
 */
@Serializable
data class BirthData(
    val date: BirthDate,
    val time: BirthTime,
    val place: BirthPlace,
) {
    fun toIsoDateTimeString(): String =
        "${date.toIsoDateString()}T${time.toIsoTimeString()}"
}
