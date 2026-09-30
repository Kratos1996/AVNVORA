package com.aynvora.core.models

/** Stable identity for a saved chart, derived only from birth and calculation inputs. */
object KundaliIdentity {
    fun profileId(birth: BirthData, config: CalculationConfig): String =
        "kundali_${fingerprint(birth, config)}"

    fun fingerprint(birth: BirthData, config: CalculationConfig): String {
        val identity = listOf(
            birth.date.toIsoDateString(), birth.time.toIsoTimeString(), birth.place.id ?: birth.place.name,
            birth.place.coordinates.latitude.toString(), birth.place.coordinates.longitude.toString(), birth.place.timezoneId,
            config.profile.name, config.ayanamsa.name, config.houseSystem.name,
            config.requestedDivisionalCharts.map { it.name }.sorted().joinToString(","),
            config.vargaRulesetId, config.ashtakavargaRulesetId,
        ).joinToString("|")
        var hash = -3750763034362895579L
        identity.forEach { character ->
            hash = hash xor character.code.toLong()
            hash *= 1099511628211L
        }
        return hash.toULong().toString(16).padStart(16, '0')
    }
}
