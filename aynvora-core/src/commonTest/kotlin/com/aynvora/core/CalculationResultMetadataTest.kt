package com.aynvora.core

import com.aynvora.astro.provenance.CommercialRedistributionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalculationResultMetadataTest {
    private val sdk = Aynvora.create()

    @Test
    fun directDashaTransitAndPanchangOutputsCarryProvenance() {
        val dasha = sdk.calculateDasha(2_451_545.0, 100.0)
        assertEquals("V1", dasha.calculationMetadata.contractVersion)
        assertEquals("PARASHARA_VIMSHOTTARI_120_V1", dasha.calculationMetadata.calculationProfileId)
        assertEquals("PARASHARA_VIMSHOTTARI_RULES_V1", dasha.calculationMetadata.ephemerisSourceId)
        assertEquals("365.25", dasha.calculationMetadata.conventions["days_per_dasha_year"])

        val transit = sdk.calculateTransit(2_451_545.0, "TROPICAL")
        assertEquals("V1", transit.calculationMetadata.contractVersion)
        assertEquals("TROPICAL", transit.calculationMetadata.conventions["ayanamsa_convention"])
        assertNull(transit.calculationMetadata.ephemerisDataVersion)
        assertEquals(CommercialRedistributionStatus.NOT_VERIFIED, transit.calculationMetadata.commercialRedistributionStatus)

        val timeline = com.aynvora.astro.transit.TransitCalculator.calculateTimeline(
            2_451_545.0, 2_451_546.0, ayanamsaConvention = "TROPICAL",
        )
        assertEquals("TROPICAL", timeline.calculationMetadata.conventions["ayanamsa_convention"])
        assertEquals(timeline.calculationMetadata, timeline.snapshots.first().calculationMetadata)

        val panchang = sdk.calculatePanchang(2_451_545.0, "TROPICAL")
        assertEquals("V1", panchang.calculationMetadata.contractVersion)
        assertEquals("TROPICAL", panchang.calculationMetadata.conventions["ayanamsa_convention"])
        assertEquals("CIVIL_UTC", panchang.calculationMetadata.conventions["vara_convention"])
        assertEquals(panchang.profileId, panchang.calculationMetadata.calculationProfileId)
    }
}
