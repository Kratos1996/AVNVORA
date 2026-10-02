package com.aynvora.core

import com.aynvora.core.models.*
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KundaliGoldenJsonTest {
    @Test
    fun engineOutputMatchesCommittedGoldenAndRoundTrips() = runBlocking {
        val birth = BirthData(
            BirthDate(2000, 1, 1), BirthTime(17, 30),
            BirthPlace("New Delhi", Coordinates(28.6139, 77.2090), "Asia/Kolkata", country = "India", id = "IN-DL-DEL", countryCode = "IN", stateName = "Delhi", stateCode = "DL", cityName = "New Delhi"),
        )
        val calculated = Aynvora.create().generateKundaliSnapshot(
            ChartRequest(birth, CalculationConfig(requestedDivisionalCharts = DivisionalChart.entries.toSet())),
            "golden-j2000-v1", "Golden J2000", "UNSPECIFIED",
        )
        assertIs<AynvoraResult.Success<KundaliSnapshot>>(calculated)
        val golden = checkNotNull(javaClass.getResourceAsStream("/kundali/golden_kundali_v1.json"))
            .bufferedReader().use { it.readText() }

        val decoded = KundaliSnapshotJson.decode(golden)
        assertEquals("2", decoded.schemaVersion)
        assertEquals("planetary_positions", decoded.featureResults["planetary_positions"]?.featureId)
        assertEquals(decoded.calculation, decoded.featureResults["planetary_positions"]?.provenance)
        assertEquals(AstroFeatureStatus.AMBIGUOUS, decoded.featureResults["chalit"]?.status)
        assertEquals(AstroFeatureStatus.NOT_VERIFIED, decoded.featureResults["location"]?.status)
        assertEquals(AstroFeatureStatus.PARTIAL, decoded.featureResults["time"]?.status)
        assertEquals(CalculationAvailability.AMBIGUOUS, decoded.availability.first { it.sectionId == "chalit" }.availability)
        assertEquals(calculated.value.birth, decoded.birth)
        assertEquals(calculated.value.natalChart, decoded.natalChart)
        assertEquals(calculated.value.charts, decoded.charts)
        assertTrue(decoded.events.isEmpty())
        assertEquals(null, decoded.evidenceGraph)
        assertEquals(emptyList(), KundaliConsistencyValidator.validate(decoded))
        val schema2Json = KundaliSnapshotJson.encode(decoded)
        assertEquals(schema2Json, KundaliSnapshotJson.encode(KundaliSnapshotJson.decode(schema2Json)))
    }
}
