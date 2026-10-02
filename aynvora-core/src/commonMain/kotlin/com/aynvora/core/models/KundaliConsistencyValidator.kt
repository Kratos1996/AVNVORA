package com.aynvora.core.models

import kotlin.math.abs

/** Cross-checks serialized projections against the canonical chart calculation with diagnostic paths. */
object KundaliConsistencyValidator {
    private const val ANGLE_EPSILON = 1e-7

    fun validate(snapshot: KundaliSnapshot): List<String> = buildList {
        val source = snapshot.natalChart
        val d1 = snapshot.charts.firstOrNull { it.chartId == "D1" }
        if (d1 == null) {
            add("charts.D1: missing canonical natal chart projection")
            return@buildList
        }
        if (d1.houses.size != 12 || d1.houses.map { it.houseNumber }.toSet() != (1..12).toSet()) {
            add("charts.D1.houses: expected exactly one house numbered 1 through 12")
        }
        if (d1.sourceMetadata != source.calculationMetadata) add("charts.D1.sourceMetadata: differs from natalChart.calculationMetadata")
        if (d1.ascendantSignIndex != source.lagna?.rashiPosition?.rashi?.index) add("charts.D1.ascendantSignIndex: differs from natalChart.lagna")
        if (d1.houseSystemId != source.config.houseSystem.name) add("charts.D1.houseSystemId: differs from natalChart.config")

        val sourceHouses = source.houses.associateBy { it.houseNumber }
        d1.houses.forEach { house ->
            val path = "charts.D1.houses[${house.houseNumber}]"
            val original = sourceHouses[house.houseNumber]
            if (original == null) add("$path: no matching natalChart.houses entry")
            else {
                if (house.signIndex != original.rashiPosition.rashi.index) add("$path.signIndex: differs from natalChart.houses[${house.houseNumber}]")
                if (!same(house.startLongitude, original.startLongitude)) add("$path.startLongitude: differs from natalChart.houses[${house.houseNumber}]")
                if (!same(house.cuspLongitude, original.cuspLongitude)) add("$path.cuspLongitude: differs from natalChart.houses[${house.houseNumber}]")
                if (!same(house.endLongitude, original.endLongitude)) add("$path.endLongitude: differs from natalChart.houses[${house.houseNumber}]")
            }
        }

        val projections = d1.placements.associateBy { it.bodyId }
        source.planetaryPositions.forEach { position ->
            val path = "charts.D1.placements[${position.body.name}]"
            val projected = projections[position.body.name]
            if (projected == null) {
                add("$path: missing natalChart.planetaryPositions entry")
                return@forEach
            }
            if (projected.signIndex != position.rashiPosition.rashi.index) add("$path.signIndex: differs from natalChart.planetaryPositions")
            if (projected.houseNumber != position.houseNumber) add("$path.houseNumber: differs from natalChart.planetaryPositions")
            if (!near(projected.longitude, position.siderealLongitude)) add("$path.longitude: differs from natalChart.planetaryPositions.siderealLongitude")
            if (!near(projected.degreeInSign, position.rashiPosition.degreeInSign)) add("$path.degreeInSign: differs from natalChart.planetaryPositions")
            if (projected.nakshatraIndex != position.nakshatraPosition.nakshatra.index) add("$path.nakshatraIndex: differs from natalChart.planetaryPositions")
            if (projected.pada != position.nakshatraPosition.pada) add("$path.pada: differs from natalChart.planetaryPositions")
            if (projected.retrograde != position.isRetrograde) add("$path.retrograde: differs from natalChart.planetaryPositions")
            if (projected.combust != (position.combustionState == CombustionState.COMBUST)) add("$path.combust: differs from natalChart.planetaryPositions")
            if (projected.dignityId != source.planetaryDignities.firstOrNull { it.body == position.body && it.chart == DivisionalChart.D1 }?.dignityType?.name) {
                add("$path.dignityId: differs from natalChart.planetaryDignities")
            }
            val derivedLongitude = ((projected.signIndex * 30.0 + projected.degreeInSign) % 360.0 + 360.0) % 360.0
            if (!near(projected.longitude, derivedLongitude)) add("$path.longitude: differs from signIndex and degreeInSign")
            val state = source.planetStates.firstOrNull { it.body == position.body }
            if (state != null && (projected.retrograde != (state.motionState == PlanetMotionState.RETROGRADE) || projected.combust != (state.combustionState == CombustionState.COMBUST))) {
                add("$path.state: differs from natalChart.planetStates")
            }
        }
        if (projections.keys != source.planetaryPositions.map { it.body.name }.toSet()) {
            add("charts.D1.placements: body set differs from natalChart.planetaryPositions")
        }

        snapshot.charts.filter { it.chartId.startsWith("D") && it.chartId != "D1" }.forEach { chart ->
            val division = chart.chartId.removePrefix("D").toIntOrNull()?.let(DivisionalChart::fromDivisionNumber)
            val sourceVarga = division?.let(source.divisionalCharts::get)
            if (sourceVarga == null) {
                add("charts.${chart.chartId}: no matching natalChart.divisionalCharts result")
                return@forEach
            }
            if (chart.sourceMetadata != source.calculationMetadata) add("charts.${chart.chartId}.sourceMetadata: differs from natalChart.calculationMetadata")
            val sourcePositions = sourceVarga.positions.filter { !it.isLagna && it.body != null }.associateBy { it.body!!.name }
            val projectedPositions = chart.placements.associateBy { it.bodyId }
            if (chart.ascendantSignIndex != sourceVarga.lagnaPosition?.resultingRashi?.index) {
                add("charts.${chart.chartId}.ascendantSignIndex: differs from natalChart.divisionalCharts[$division].lagnaPosition")
            }
            sourcePositions.forEach { (body, original) ->
                val path = "charts.${chart.chartId}.placements[$body]"
                val projected = projectedPositions[body]
                if (projected == null) add("$path: missing natalChart.divisionalCharts[$division] position")
                else {
                    if (projected.signIndex != original.resultingRashi.index) add("$path.signIndex: differs from source varga result")
                    if (!near(projected.longitude, original.resultingLongitude)) add("$path.longitude: differs from source varga result")
                    if (!near(projected.degreeInSign, original.degreeInResultingRashi)) add("$path.degreeInSign: differs from source varga result")
                }
            }
            if (sourcePositions.keys != projectedPositions.keys) add("charts.${chart.chartId}.placements: body set differs from source varga result")
        }

        val grahaTable = snapshot.tables.firstOrNull { it.sectionId == "graha_sthiti" }
        if (grahaTable == null) add("tables.graha_sthiti: missing planetary-position table")
        else {
            val rows = grahaTable.rows.associateBy { it.id }
            source.planetaryPositions.forEach { position ->
                val path = "tables.graha_sthiti.rows[${position.body.name}]"
                val row = rows[position.body.name]
                if (row == null) add("$path: missing canonical body row")
                else {
                    val cells = row.cells
                    if (cells.getOrNull(1)?.canonicalId != position.rashiPosition.rashi.name) add("$path.sign: differs from natalChart.planetaryPositions")
                    if (!same(cells.getOrNull(2)?.numericValue, position.siderealLongitude)) add("$path.longitude: differs from natalChart.planetaryPositions")
                    if (cells.getOrNull(3)?.integerValue != position.houseNumber) add("$path.house: differs from natalChart.planetaryPositions")
                    if (cells.getOrNull(4)?.canonicalId != position.nakshatraPosition.nakshatra.name) add("$path.nakshatra: differs from natalChart.planetaryPositions")
                    if (cells.getOrNull(5)?.integerValue != position.nakshatraPosition.pada) add("$path.pada: differs from natalChart.planetaryPositions")
                    if (cells.getOrNull(6)?.canonicalId != position.isRetrograde.toString()) add("$path.retrograde: differs from natalChart.planetaryPositions")
                    if (cells.getOrNull(7)?.canonicalId != (position.combustionState == CombustionState.COMBUST).toString()) add("$path.combust: differs from natalChart.planetaryPositions")
                }
            }
        }

        snapshot.dasha?.let { dasha ->
            if (dasha.birthJulianDay != source.julianDay) add("dasha.birthJulianDay: differs from natalChart.julianDay")
            val moon = source.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }
            if (moon == null) add("dasha: exists without natalChart Moon dependency")
            else if (!near(dasha.moonSiderealLongitude, moon.siderealLongitude)) add("dasha.moonSiderealLongitude: differs from natalChart Moon")
            source.dashaTimeline?.let { if (dasha != it) add("dasha: differs from natalChart.dashaTimeline") }
            dasha.mahadashas.forEachIndexed { index, period ->
                validatePeriod(period, expectedLevel = 1, parent = null, path = "dasha.mahadashas[$index]", errors = this)
                if (index > 0 && dasha.mahadashas[index - 1].endJulianDay > period.startJulianDay + ANGLE_EPSILON) {
                    add("dasha.mahadashas[$index].startJulianDay: overlaps or precedes previous Mahadasha")
                }
            }
        }
        snapshot.transitAtBirth?.let { transit ->
            if (!near(transit.julianDay, source.julianDay)) add("transitAtBirth.julianDay: differs from natalChart.julianDay")
            if (transit.calculationMetadata.contractVersion != source.calculationMetadata.contractVersion) add("transitAtBirth.provenance.contractVersion: differs from natal chart contract")
            transit.planetaryPositions.forEach { position ->
                val natal = source.planetaryPositions.firstOrNull { it.body.name == position.bodyId.name }
                if (natal == null) add("transitAtBirth.planetaryPositions[${position.bodyId}]: body missing from natal chart")
                else if (!near(position.siderealLongitude, natal.siderealLongitude)) add("transitAtBirth.planetaryPositions[${position.bodyId}].siderealLongitude: differs at birth instant")
            }
            if (transit.planetaryPositions.map { it.bodyId.name }.toSet() != source.planetaryPositions.map { it.body.name }.toSet()) {
                add("transitAtBirth.planetaryPositions: body set differs from natalChart.planetaryPositions")
            }
        }
        snapshot.panchang?.let { panchang ->
            if (!near(panchang.julianDay, source.julianDay)) add("panchang.julianDay: differs from natalChart.julianDay")
            if (!near(panchang.moonSiderealLongitude, source.planetaryPositions.firstOrNull { it.body == CelestialBody.MOON }?.siderealLongitude)) {
                add("panchang.moonSiderealLongitude: differs from natalChart Moon")
            }
            if (panchang.calculationMetadata.contractVersion != source.calculationMetadata.contractVersion) add("panchang.provenance.contractVersion: differs from natal chart contract")
        }

        snapshot.featureResults.values.forEach { record ->
            val path = "featureResults[${record.featureId}].provenance"
            if (record.provenance.contractVersion != snapshot.calculation.contractVersion) add("$path.contractVersion: differs from snapshot calculation")
            if (record.provenance.engineVersion != snapshot.calculation.engineVersion) add("$path.engineVersion: differs from snapshot calculation")
        }
        val birthPlace = source.birthData.place
        if (snapshot.birth.timezoneId != birthPlace.timezoneId) add("birth.timezoneId: differs from natalChart.birthData.place")
        if (!near(snapshot.birth.latitude, birthPlace.coordinates.latitude)) add("birth.latitude: differs from natalChart.birthData.place")
        if (!near(snapshot.birth.longitude, birthPlace.coordinates.longitude)) add("birth.longitude: differs from natalChart.birthData.place")
        if (snapshot.birth.cityId != birthPlace.id) add("birth.cityId: differs from natalChart.birthData.place.id")
        if (snapshot.birth.locationDatasetVersion != birthPlace.locationDatasetVersion) add("birth.locationDatasetVersion: differs from natalChart.birthData.place")
        if (snapshot.birth.locationProvenance != birthPlace.locationProvenance) add("birth.locationProvenance: differs from natalChart.birthData.place")
        val chalit = snapshot.tables.firstOrNull { it.sectionId == "chalit_table" }
        if (snapshot.featureResults["chalit"]?.status == AstroFeatureStatus.AMBIGUOUS && chalit != null && chalit.rows.isNotEmpty()) {
            add("tables.chalit_table.rows: ambiguous Chalit feature must not carry substitute cusp values")
        }
    }

    private fun near(a: Double, b: Double?): Boolean = b != null && abs(a - b) <= ANGLE_EPSILON
    private fun same(a: Double?, b: Double?): Boolean = when {
        a == null || b == null -> a == b
        else -> near(a, b)
    }

    private fun validatePeriod(
        period: com.aynvora.astro.dasha.DashaPeriod,
        expectedLevel: Int,
        parent: com.aynvora.astro.dasha.DashaPeriod?,
        path: String,
        errors: MutableList<String>,
    ) {
        if (period.level != expectedLevel) errors.add("$path.level: expected $expectedLevel")
        if (period.startJulianDay >= period.endJulianDay) errors.add("$path: start must precede end")
        if (parent != null && (period.startJulianDay < parent.startJulianDay - ANGLE_EPSILON || period.endJulianDay > parent.endJulianDay + ANGLE_EPSILON)) {
            errors.add("$path: child period lies outside parent period")
        }
        period.subPeriods.forEachIndexed { index, child ->
            validatePeriod(child, expectedLevel + 1, period, "$path.subPeriods[$index]", errors)
            if (index > 0 && period.subPeriods[index - 1].endJulianDay > child.startJulianDay + ANGLE_EPSILON) {
                errors.add("$path.subPeriods[$index].startJulianDay: overlaps or precedes previous child period")
            }
        }
    }
}
