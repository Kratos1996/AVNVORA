package com.aynvora.core.models

/** Cross-checks duplicated snapshot projections against the authoritative natal calculation. */
object KundaliConsistencyValidator {
    fun validate(snapshot: KundaliSnapshot): List<String> {
        val issues = mutableListOf<String>()
        val source = snapshot.natalChart
        val d1 = snapshot.charts.firstOrNull { it.chartId == "D1" }
        if (d1 == null) {
            issues += "D1 chart is missing from the snapshot."
            return issues
        }
        if (d1.houses.size != 12 || d1.houses.map { it.houseNumber }.toSet() != (1..12).toSet()) {
            issues += "D1 must contain exactly houses 1 through 12."
        }
        val sourceHouses = source.houses.associateBy { it.houseNumber }
        d1.houses.forEach { house ->
            val original = sourceHouses[house.houseNumber]
            if (original == null) issues += "D1 house ${house.houseNumber} has no matching source house."
            else if (house.signIndex != original.rashiPosition.rashi.index) issues += "D1 house ${house.houseNumber} sign differs from source house."
        }
        val projections = d1.placements.associateBy { it.bodyId }
        source.planetaryPositions.forEach { position ->
            val projected = projections[position.body.name]
            if (projected == null) {
                issues += "D1 is missing ${position.body.name}."
                return@forEach
            }
            if (projected.signIndex != position.rashiPosition.rashi.index) issues += "${position.body.name} sign differs between D1 and planetary positions."
            if (projected.houseNumber != position.houseNumber) issues += "${position.body.name} house differs between D1 and planetary positions."
            if (kotlin.math.abs(projected.degreeInSign - position.rashiPosition.degreeInSign) > 1e-7) issues += "${position.body.name} degree differs between D1 and planetary positions."
            if (projected.retrograde != position.isRetrograde) issues += "${position.body.name} retrograde state differs between D1 and planetary positions."
            if (projected.combust != (position.combustionState == CombustionState.COMBUST)) issues += "${position.body.name} combustion state differs between D1 and planetary positions."
        }
        if (snapshot.dasha != null && source.planetaryPositions.none { it.body == CelestialBody.MOON }) {
            issues += "Dasha exists without its Moon position dependency."
        }
        if (snapshot.dasha != null && snapshot.dasha.birthJulianDay != source.julianDay) {
            issues += "Dasha birth Julian Day differs from natal chart."
        }
        return issues
    }
}
