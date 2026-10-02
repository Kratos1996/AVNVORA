package com.aynvora.astro.context

import kotlinx.serialization.Serializable

/** Explicit place and instant for a date-sensitive calculation, separate from the natal context. */
@Serializable
data class AstroObservationContext(
    val localDate: String,
    val localTime: String,
    val utcTimestamp: String,
    val timeZoneId: String,
    val timezoneOffsetMinutes: Int,
    val julianDay: Double,
    val latitude: Double,
    val longitude: Double,
    val locationId: String? = null,
    val locationName: String? = null,
    val timezoneDataVersion: String? = null,
    val panchangVaraConvention: String? = null,
    val requestedRangeStartJulianDay: Double? = null,
    val requestedRangeEndJulianDay: Double? = null,
    val purpose: String? = null,
) {
    init {
        require(localDate.isNotBlank() && localTime.isNotBlank() && utcTimestamp.isNotBlank())
        require(timeZoneId.isNotBlank())
        require(julianDay.isFinite())
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0)
        require((requestedRangeStartJulianDay == null) == (requestedRangeEndJulianDay == null))
        if (requestedRangeStartJulianDay != null && requestedRangeEndJulianDay != null) {
            require(requestedRangeStartJulianDay.isFinite() && requestedRangeEndJulianDay.isFinite())
            require(requestedRangeStartJulianDay <= requestedRangeEndJulianDay)
        }
    }
}
