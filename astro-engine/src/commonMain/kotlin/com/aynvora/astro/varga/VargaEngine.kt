package com.aynvora.astro.varga

import com.aynvora.astro.BodyPosition
import com.aynvora.astro.lagna.LagnaPosition

/**
 * Common calculation engine for Vedic divisional charts (Vargas).
 * Transforms already calculated sidereal longitudes into factual divisional placements.
 */
interface VargaEngine {
    /**
     * Calculates a single divisional chart from existing planetary positions and Lagna.
     * @throws UnsupportedOperationException if the requested chart is unsupported under [profile].
     */
    fun calculate(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        chart: DivisionalChart,
        profile: VargaProfile = VargaProfile.DEFAULT,
    ): VargaChartResult

    /**
     * Calculates multiple requested divisional charts in a single coordinated pass without
     * recalculating underlying planetary positions.
     * @throws UnsupportedOperationException if any requested chart is unsupported under [profile].
     */
    fun calculateMultiple(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        charts: Set<DivisionalChart>,
        profile: VargaProfile = VargaProfile.DEFAULT,
    ): Map<DivisionalChart, VargaChartResult>
}

/**
 * Default implementation of [VargaEngine] using [VargaStrategyRegistry].
 */
class DefaultVargaEngine : VargaEngine {

    override fun calculate(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        chart: DivisionalChart,
        profile: VargaProfile,
    ): VargaChartResult {
        if (!profile.supportedCharts.contains(chart)) {
            throw UnsupportedOperationException(
                "Divisional chart ${chart.name} is not supported under ruleset '${profile.rulesetId}'."
            )
        }

        val strategy = VargaStrategyRegistry.getStrategy(chart)

        // Calculate Lagna divisional position if present
        val lagnaVargaPos = lagna?.let { l ->
            val divResult = strategy.calculate(l.siderealLongitude)
            VargaPosition(
                bodyId = null,
                isLagna = true,
                sourceLongitude = l.siderealLongitude,
                sourceRashiIndex = divResult.sourceRashiIndex,
                divisionIndex = divResult.divisionIndex,
                resultingRashiIndex = divResult.resultingRashiIndex,
                degreeInResultingRashi = divResult.degreeInResultingRashi,
                resultingLongitude = divResult.resultingLongitude,
            )
        }

        // Calculate positions for all celestial bodies
        val bodyVargaPositions = positions.map { body ->
            val divResult = strategy.calculate(body.siderealLongitude)
            VargaPosition(
                bodyId = body.bodyId,
                isLagna = false,
                sourceLongitude = body.siderealLongitude,
                sourceRashiIndex = divResult.sourceRashiIndex,
                divisionIndex = divResult.divisionIndex,
                resultingRashiIndex = divResult.resultingRashiIndex,
                degreeInResultingRashi = divResult.degreeInResultingRashi,
                resultingLongitude = divResult.resultingLongitude,
            )
        }

        return VargaChartResult(
            chart = chart,
            rulesetId = profile.rulesetId,
            isSupported = true,
            lagnaPosition = lagnaVargaPos,
            positions = bodyVargaPositions,
        )
    }

    override fun calculateMultiple(
        positions: List<BodyPosition>,
        lagna: LagnaPosition?,
        charts: Set<DivisionalChart>,
        profile: VargaProfile,
    ): Map<DivisionalChart, VargaChartResult> {
        if (charts.isEmpty()) return emptyMap()

        // Deterministic ordering by chart ordinal
        return charts.sortedBy { it.ordinal }.associateWith { chart ->
            calculate(positions, lagna, chart, profile)
        }
    }
}
