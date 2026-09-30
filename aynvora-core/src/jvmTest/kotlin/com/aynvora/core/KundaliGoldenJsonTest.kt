package com.aynvora.core

import com.aynvora.core.models.*
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
        val encoded = KundaliSnapshotJson.encode(calculated.value)
        assertEquals(golden, encoded)

        val decoded = KundaliSnapshotJson.decode(golden)
        assertEquals("1", decoded.schemaVersion)
        assertEquals("planetary_positions", decoded.featureResults["planetary_positions"]?.featureId)
        assertEquals(decoded.calculation, decoded.featureResults["planetary_positions"]?.provenance)
        assertEquals(AstroFeatureStatus.AMBIGUOUS, decoded.featureResults["chalit"]?.status)
        assertEquals(AstroFeatureStatus.NOT_VERIFIED, decoded.featureResults["location"]?.status)
        assertEquals(calculated.value, decoded)
        assertEquals(emptyList(), KundaliConsistencyValidator.validate(decoded))
        assertEquals(golden, KundaliSnapshotJson.encode(decoded))
    }
}
