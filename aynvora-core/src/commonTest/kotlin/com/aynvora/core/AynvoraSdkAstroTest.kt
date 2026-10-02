package com.aynvora.core

import com.aynvora.core.models.AspectType
import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CalculationProfile
import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.CommercialRedistributionStatus
import com.aynvora.core.models.CombustionState
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.generateKundaliSnapshot
import com.aynvora.core.models.TransitRequest
import com.aynvora.core.models.PanchangRequest
import com.aynvora.core.models.DashaRequest
import com.aynvora.core.models.OfflineLocationCatalog
import com.aynvora.core.models.LocationLookupStatus
import com.aynvora.core.models.AstroReferenceInput
import com.aynvora.core.models.AstroReferenceInputGate
import com.aynvora.core.models.PlanetMotionState
import com.aynvora.core.result.AynvoraResult
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AynvoraSdkAstroTest {

    private val sdk = Aynvora.create()

    private fun sampleBirthData(): BirthData = BirthData(
        date = BirthDate(2000, 1, 1),
        time = BirthTime(17, 30, 0), // 17:30 IST = 12:00 UTC (J2000.0)
        place = BirthPlace(
            name = "New Delhi",
            coordinates = Coordinates(28.6139, 77.2090),
            timezoneId = "Asia/Kolkata",
        ),
    )

    @Test
    fun testRealAstronomicalCalculationViaSdkFacade() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                ayanamsa = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
                houseSystem = HouseSystem.WHOLE_SIGN,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Success<ChartResult>>(result)

        val chart = result.value
        assertEquals("0.3.0", chart.engineVersion)
        assertEquals("CALCULATED", chart.calculationStatus)
        assertEquals("MEEUS_VSOP87", chart.calculationModel)
        assertEquals("V1", chart.calculationMetadata.contractVersion)
        assertEquals("STANDARD_VEDIC", chart.calculationMetadata.calculationProfileId)
        assertEquals(chart.engineVersion, chart.calculationMetadata.engineVersion)
        assertEquals(chart.calculationModel, chart.calculationMetadata.calculationModel)
        assertEquals("ANALYTICAL_MEEUS_SIMON_FORMULAE", chart.calculationMetadata.ephemerisSourceId)
        assertEquals(null, chart.calculationMetadata.ephemerisDataVersion)
        assertEquals(
            CommercialRedistributionStatus.NOT_VERIFIED,
            chart.calculationMetadata.commercialRedistributionStatus,
        )
        assertEquals("LAHIRI_CHITRAPAKSHA", chart.calculationMetadata.conventions["ayanamsa"])
        assertEquals("WHOLE_SIGN", chart.calculationMetadata.conventions["house_system"])

        // J2000.0 epoch check
        assertEquals(2451545.0, chart.julianDay, 1e-4)

        // Lahiri Ayanamsa at J2000.0 is ~23.857°
        assertEquals(23.857, chart.ayanamsaDegrees, 0.01)

        // Lagna verification
        assertNotNull(chart.lagna)
        assertTrue(chart.lagna.tropicalLongitude in 0.0..<360.0)
        assertTrue(chart.lagna.siderealLongitude in 0.0..<360.0)
        assertTrue(chart.lagna.rashiPosition.rashi.index in 0..11)
        assertTrue(chart.lagna.nakshatraPosition.nakshatra.index in 0..26)
        assertTrue(chart.lagna.nakshatraPosition.pada in 1..4)

        // Houses verification
        assertEquals(12, chart.houses.size)
        chart.houses.forEachIndexed { idx, house ->
            assertEquals(idx + 1, house.houseNumber)
            assertEquals(HouseSystem.WHOLE_SIGN, house.system)
            assertTrue(house.cuspLongitude in 0.0..<360.0)
            assertTrue(house.startLongitude in 0.0..<360.0)
            assertTrue(house.endLongitude in 0.0..<360.0)
        }

        // 9 celestial bodies must be calculated
        assertEquals(9, chart.planetaryPositions.size)

        val bodies = chart.planetaryPositions.map { it.body }
        assertTrue(bodies.contains(CelestialBody.SUN))
        assertTrue(bodies.contains(CelestialBody.MOON))
        assertTrue(bodies.contains(CelestialBody.MERCURY))
        assertTrue(bodies.contains(CelestialBody.VENUS))
        assertTrue(bodies.contains(CelestialBody.MARS))
        assertTrue(bodies.contains(CelestialBody.JUPITER))
        assertTrue(bodies.contains(CelestialBody.SATURN))
        assertTrue(bodies.contains(CelestialBody.RAHU))
        assertTrue(bodies.contains(CelestialBody.KETU))

        // Sun checks
        val sun = chart.planetaryPositions.first { it.body == CelestialBody.SUN }
        assertEquals(280.46, sun.tropicalLongitude, 0.1) // Tropical Capricorn
        assertTrue(sun.siderealLongitude in 0.0..<360.0)
        assertEquals(sun.tropicalLongitude - chart.ayanamsaDegrees, sun.siderealLongitude, 1e-6)
        assertTrue(sun.nakshatraPosition.pada in 1..4)
        assertTrue(sun.houseNumber in 1..12)

        // Rahu & Ketu opposition check
        val rahu = chart.planetaryPositions.first { it.body == CelestialBody.RAHU }
        val ketu = chart.planetaryPositions.first { it.body == CelestialBody.KETU }
        val separation = abs(ketu.siderealLongitude - rahu.siderealLongitude)
        assertEquals(180.0, separation, 1e-9)
        assertTrue(rahu.isRetrograde)
        assertTrue(ketu.isRetrograde)
        assertTrue(rahu.houseNumber in 1..12)
        assertTrue(ketu.houseNumber in 1..12)

        // Aspects check
        assertNotNull(chart.aspects)
        assertTrue(chart.aspects.isNotEmpty())
        chart.aspects.forEach { aspect ->
            assertTrue(aspect.firstBody != aspect.secondBody)
            assertTrue(aspect.firstBody.ordinal < aspect.secondBody.ordinal)
            assertTrue(aspect.actualSeparation in 0.0..180.0)
            assertTrue(aspect.orb >= 0.0)
        }
        // Rahu and Ketu must have an OPPOSITION aspect
        val nodeOpp = chart.aspects.firstOrNull { it.firstBody == CelestialBody.RAHU && it.secondBody == CelestialBody.KETU }
        assertNotNull(nodeOpp)
        assertEquals(AspectType.OPPOSITION, nodeOpp.type)
        assertEquals(0.0, nodeOpp.orb, 1e-6)

        // Planet states check
        assertEquals(9, chart.planetStates.size)
        val sunState = chart.planetStates.first { it.body == CelestialBody.SUN }
        assertEquals(CombustionState.NOT_APPLICABLE, sunState.combustionState)
        assertEquals(PlanetMotionState.DIRECT, sunState.motionState)

        val rahuState = chart.planetStates.first { it.body == CelestialBody.RAHU }
        assertEquals(CombustionState.NOT_APPLICABLE, rahuState.combustionState)
        assertEquals(PlanetMotionState.RETROGRADE, rahuState.motionState)
    }

    @Test
    fun transitUsesRequestedInstantRatherThanNatalBirthInstant() = runBlocking {
        val location = sampleBirthData().place
        val requestedDate = BirthDate(2024, 4, 8)
        val requestedTime = BirthTime(18, 30, 0)
        val result = sdk.astrology.calculateTransit(
            TransitRequest(requestedDate, requestedTime, location, natalContext = ChartRequest(sampleBirthData())),
        )
        assertIs<AynvoraResult.Success<com.aynvora.core.models.TransitFeatureResult>>(result)
        assertEquals("2024-04-08", result.value.instant.localDate)
        assertEquals("18:30:00", result.value.instant.localTime)
        assertEquals("2024-04-08T13:00:00Z", result.value.instant.utcTimestamp)
        assertEquals(result.value.instant.julianDay, result.value.snapshot.julianDay, 1e-9)
        assertTrue(result.value.snapshot.julianDay > 2460000.0)
        assertEquals(com.aynvora.core.models.AstroResolutionStatus.RESOLVED, result.value.instant.resolutionStatus)
        assertEquals(com.aynvora.astro.time.TimeNormalizer.TIMEZONE_DATA_VERSION, result.value.instant.timezoneDataVersion)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.PARTIAL, result.value.status)
        assertTrue(result.value.natalHouseByBody.isNotEmpty())
        assertTrue(result.value.interactions.isNotEmpty())
        assertTrue(result.value.executionTrace!!.executionOrder.contains("vedic.transit"))
        assertTrue(result.value.executionTrace!!.executionOrder.contains("core.planetary_positions"))
        assertEquals(1, result.value.executionTrace!!.executionOrder.count { it == "core.planetary_positions" })
        val encoded = kotlinx.serialization.json.Json.encodeToString(
            com.aynvora.core.models.TransitFeatureResult.serializer(), result.value,
        )
        assertTrue(encoded.contains("2024-04-08T13:00:00Z"))
    }

    @Test
    fun panchangUsesRequestedLocalDateAndExplicitNoon() = runBlocking {
        val location = sampleBirthData().place
        val result = sdk.astrology.calculatePanchang(PanchangRequest(BirthDate(2024, 4, 8), location))
        assertIs<AynvoraResult.Success<com.aynvora.core.models.PanchangFeatureResult>>(result)
        assertEquals("2024-04-08", result.value.localDate)
        assertEquals("12:00:00", result.value.evaluatedLocalTime)
        assertEquals(28.6139, result.value.snapshot.observerLatitudeDeg)
        assertEquals(77.2090, result.value.snapshot.observerLongitudeDeg)
        assertEquals(330, result.value.snapshot.timezoneOffsetMinutes)
        assertEquals(listOf("vedic.panchang"), result.value.executionTrace!!.executionOrder)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.SUPPORTED, result.value.status)
        assertNotNull(result.value.snapshot.sunriseJulianDay)
        assertNotNull(result.value.snapshot.sunsetJulianDay)
        assertNotNull(result.value.dayDurationMinutes)
        val encoded = kotlinx.serialization.json.Json.encodeToString(
            com.aynvora.core.models.PanchangFeatureResult.serializer(), result.value,
        )
        assertTrue(encoded.contains("CLASSICAL_LOCAL_SUNRISE_V1"))
    }

    @Test
    fun dashaRequestUsesTheCalculatedNatalMoonAndReturnsCurrentNesting() = runBlocking {
        val result = sdk.astrology.calculateDasha(DashaRequest(ChartRequest(sampleBirthData()), includePratyantardasha = true))
        assertIs<AynvoraResult.Success<com.aynvora.core.models.DashaFeatureResult>>(result)
        val dasha = result.value
        assertEquals("PARASHARA_VIMSHOTTARI_120_V1", dasha.timeline.rulesetId)
        assertEquals("PARASHARA_VIMSHOTTARI_120_V1", dasha.provenance.calculationProfileId)
        assertNotNull(dasha.currentMahadasha)
        assertNotNull(dasha.currentAntardasha)
        assertNotNull(dasha.currentPratyantardasha)
        assertEquals(dasha.timeline.birthJulianDay, dasha.currentMahadasha!!.startJulianDay)
    }

    @Test
    fun offlineLocationCatalogPreservesDuplicateNamesAndResolvesOnlyByCanonicalId() {
        val catalog = OfflineLocationCatalog.parse(
            "101\tSpringfield\tIL\tIllinois\tUS\tUnited States\t39.78\t-89.64\tAmerica/Chicago\n" +
                "202\tSpringfield\tMA\tMassachusetts\tUS\tUnited States\t42.10\t-72.59\tAmerica/New_York\n",
            datasetVersion = "fixture-v1",
            provenance = "test fixture",
        )
        assertEquals(LocationLookupStatus.AMBIGUOUS, catalog.findByCityName("Springfield").status)
        assertEquals(LocationLookupStatus.UNIQUE, catalog.findByCityName("Springfield", stateCode = "IL").status)
        assertEquals("MA", catalog.resolve("202")?.stateCode)
        assertEquals(null, catalog.resolve("Springfield"))

        val catalogSdk = Aynvora.create(locationCatalog = catalog)
        assertEquals(listOf("US"), catalogSdk.searchCountries().map { it.countryCode })
        assertEquals(listOf("IL", "MA"), catalogSdk.searchStates("US").map { it.stateCode }.sorted())
        assertEquals(1, catalogSdk.searchCities("MA", "US", "Spring").size)
        assertEquals(LocationLookupStatus.AMBIGUOUS, catalogSdk.findCity("Springfield").status)
        val selectedPlace = catalogSdk.resolveLocation("202")!!.toBirthPlace()
        assertEquals("202", selectedPlace.id)
        assertEquals("fixture-v1", selectedPlace.locationDatasetVersion)
        assertEquals(42.10, selectedPlace.coordinates.latitude)
    }

    @Test
    fun publicSelectiveApiExecutesSharedRegisteredDependenciesOnce() = runBlocking {
        val result = sdk.calculateFeatures(
            ChartRequest(sampleBirthData()),
            setOf("vedic.dignities", "vedic.relationships", "vedic.vargas", "vedic.dasha", "vedic.chalit"),
        )
        assertIs<AynvoraResult.Success<com.aynvora.core.models.AstrologyFeatureCalculation>>(result)
        assertEquals(5, result.value.trace.requestedFeatures.size)
        assertEquals(result.value.trace.executionOrder.size, result.value.trace.executionOrder.distinct().size)
        assertEquals(result.value.trace.executionOrder.size, result.value.trace.executedOnceCount)
        assertTrue(result.value.trace.executionOrder.contains("core.planetary_positions"))
        assertTrue(result.value.trace.executionOrder.contains("vedic.dasha"))
        assertEquals(com.aynvora.astro.pipeline.FeatureStatus.AMBIGUOUS, result.value.outputs["vedic.chalit"]?.status)
    }

    @Test
    fun canonicalLocationMetadataReachesTheCalculationPipeline() = runBlocking {
        val catalog = OfflineLocationCatalog.parse(
            "202\tSpringfield\tMA\tMassachusetts\tUS\tUnited States\t42.10\t-72.59\tAmerica/New_York",
            datasetVersion = "fixture-v1", provenance = "test catalog",
        )
        val sdk = Aynvora.create(locationCatalog = catalog)
        val selectedPlace = sdk.resolveLocation("202")!!.toBirthPlace()
        val request = ChartRequest(sampleBirthData().copy(place = selectedPlace))
        val result = sdk.calculateFeature(request, "core.location")
        assertIs<AynvoraResult.Success<com.aynvora.core.models.AstrologyFeatureCalculation>>(result)
        val location = result.value.outputs.getValue("core.location").value as com.aynvora.astro.pipeline.ResolvedLocationResult
        assertEquals("202", location.cityId)
        assertEquals("America/New_York", location.timezoneId)
        assertEquals("fixture-v1", location.locationDatasetVersion)
        assertEquals("test catalog", location.locationProvenance)
        assertEquals("offline_canonical_catalog", location.source)
    }

    @Test
    fun referenceInputGateRejectsDifferentInstantsAndCalculationEpochs() {
        val aligned = AstroReferenceInput(
            date = "2024-03-10", localTime = "03:30:00", timezoneId = "America/New_York",
            latitude = 40.71, longitude = -74.0, ayanamsha = "LAHIRI_CHITRAPAKSHA",
            houseSystem = "EQUAL_HOUSE", calculationProfile = "STANDARD_VEDIC", requestedFeature = "sun_longitude",
            requestedTimestamp = "2024-03-10T07:30:00Z", calculationEpoch = "TT", timezoneDataVersion = "tz-v1",
        )
        assertEquals(emptyList(), AstroReferenceInputGate.mismatchFields(aligned, aligned.copy()))
        assertEquals(
            listOf("requestedTimestamp", "calculationEpoch"),
            AstroReferenceInputGate.mismatchFields(aligned, aligned.copy(requestedTimestamp = "2024-03-10T06:30:00Z", calculationEpoch = "UTC")),
        )
    }

    @Test
    fun testMathematicalDeterminism() = runBlocking {
        val request = ChartRequest(birthData = sampleBirthData())

        val result1 = sdk.calculateChart(request)
        val result2 = sdk.calculateChart(request)

        assertIs<AynvoraResult.Success<ChartResult>>(result1)
        assertIs<AynvoraResult.Success<ChartResult>>(result2)

        assertEquals(result1.value.julianDay, result2.value.julianDay)
        assertEquals(result1.value.ayanamsaDegrees, result2.value.ayanamsaDegrees)
        assertEquals(result1.value.lagna?.siderealLongitude, result2.value.lagna?.siderealLongitude)

        for (i in result1.value.planetaryPositions.indices) {
            val p1 = result1.value.planetaryPositions[i]
            val p2 = result2.value.planetaryPositions[i]
            assertEquals(p1.body, p2.body)
            assertEquals(p1.tropicalLongitude, p2.tropicalLongitude)
            assertEquals(p1.siderealLongitude, p2.siderealLongitude)
            assertEquals(p1.rashiPosition, p2.rashiPosition)
            assertEquals(p1.nakshatraPosition, p2.nakshatraPosition)
            assertEquals(p1.houseNumber, p2.houseNumber)
            assertEquals(p1.motionState, p2.motionState)
            assertEquals(p1.combustionState, p2.combustionState)
            assertEquals(p1.isRetrograde, p2.isRetrograde)
            assertEquals(p1.dailyMotionDegrees, p2.dailyMotionDegrees)
        }

        assertEquals(result1.value.aspects.size, result2.value.aspects.size)
        for (i in result1.value.aspects.indices) {
            val a1 = result1.value.aspects[i]
            val a2 = result2.value.aspects[i]
            assertEquals(a1.firstBody, a2.firstBody)
            assertEquals(a1.secondBody, a2.secondBody)
            assertEquals(a1.type, a2.type)
            assertEquals(a1.orb, a2.orb)
        }
    }

    @Test
    fun kundaliSnapshotIsCompleteVersionedAndDeterministicJson() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(requestedDivisionalCharts = com.aynvora.core.models.DivisionalChart.entries.toSet()),
        )
        val first = sdk.generateKundaliSnapshot(request, "fixture-j2000", "J2000 fixture", "UNSPECIFIED")
        val second = sdk.generateKundaliSnapshot(request, "fixture-j2000", "J2000 fixture", "UNSPECIFIED")
        assertIs<AynvoraResult.Success<com.aynvora.core.models.KundaliSnapshot>>(first)
        assertIs<AynvoraResult.Success<com.aynvora.core.models.KundaliSnapshot>>(second)
        val snapshot = first.value
        assertEquals(com.aynvora.core.models.KundaliSnapshot.CURRENT_SCHEMA_VERSION, snapshot.schemaVersion)
        assertEquals("2000-01-01", snapshot.birth.localDate)
        assertEquals("2000-01-01T12:00:00Z", snapshot.birth.utcTimestamp)
        assertEquals(12, snapshot.natalChart.houses.size)
        assertEquals(9, snapshot.natalChart.planetaryPositions.size)
        assertNull(snapshot.natalChart.executionTrace)
        assertEquals(12, snapshot.charts.first { it.chartId == "D1" }.houses.size)
        assertTrue(snapshot.charts.any { it.chartId == "D9" })
        assertNotNull(snapshot.dasha)
        assertNotNull(snapshot.panchang)
        assertTrue(snapshot.tables.any { it.sectionId == "graha_sthiti" })
        assertTrue(snapshot.tables.any { it.sectionId == "chalit_table" })
        assertEquals(com.aynvora.core.models.CalculationAvailability.UNSUPPORTED, snapshot.availability.first { it.sectionId == "kp" }.availability)
        assertEquals(com.aynvora.core.models.CalculationAvailability.UNSUPPORTED, snapshot.availability.first { it.sectionId == "varshaphal" }.availability)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.NOT_VERIFIED, snapshot.featureResults["kp_analysis"]?.status)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.NOT_VERIFIED, snapshot.featureResults["lal_kitab_analysis"]?.status)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.NOT_VERIFIED, snapshot.featureResults["varshaphal"]?.status)
        assertEquals(com.aynvora.core.models.AstroFeatureStatus.UNSUPPORTED, snapshot.featureResults["phaladesh"]?.status)
        val encoded = com.aynvora.core.models.KundaliSnapshotJson.encode(snapshot)
        assertEquals(encoded, com.aynvora.core.models.KundaliSnapshotJson.encode(second.value))
        assertEquals(snapshot, com.aynvora.core.models.KundaliSnapshotJson.decode(encoded))
    }

    @Test
    fun evaluatedEventsAreIntegratedIntoSnapshotJsonAndEvidenceGraph() = runBlocking {
        val generated = sdk.generateKundaliSnapshot(
            ChartRequest(sampleBirthData()), "event-fixture", "Event fixture", "UNSPECIFIED",
        )
        assertIs<AynvoraResult.Success<com.aynvora.core.models.KundaliSnapshot>>(generated)
        val metadata = generated.value.calculation
        val request = com.aynvora.core.astrology.prediction.AstroEventEvaluationRequest(
            definition = com.aynvora.core.astrology.prediction.AstroEventDefinition(
                eventId = "fixture.event", nameKey = "event.fixture", tradition = "FIXTURE_ONLY",
                conditions = emptyList(), evidenceRequirements = listOf("fixture-fact"),
                sourceRefs = listOf("test:fixture"), ruleVersion = "test-1", licenseStatus = "CLEARED",
                status = com.aynvora.core.astrology.prediction.AstroEventDefinitionStatus.SUPPORTED,
                typedConditions = listOf(com.aynvora.core.astrology.prediction.AstroEventCondition(
                    "fixture.value", com.aynvora.core.astrology.prediction.AstroEventComparison.EQUALS,
                    "yes", "fixture-fact",
                )),
            ),
            period = com.aynvora.core.astrology.prediction.AstroEventTimeRange("2027-01-01", "2027-01-31"),
            facts = listOf(com.aynvora.core.astrology.prediction.AstroEventFact(
                "fixture-evidence", "fixture.value", "yes", "fixture-fact", listOf("test:calculation"),
            )),
            provenance = metadata,
            featureId = "vedic.fixture",
        )

        val withEvents = sdk.calculateEventOccurrences(generated.value, listOf(request))
        val encoded = com.aynvora.core.models.KundaliSnapshotJson.encode(withEvents)
        assertEquals(com.aynvora.core.astrology.prediction.AstroEventOccurrenceStatus.SUPPORTED, withEvents.events.single().status)
        assertEquals("vedic.fixture", withEvents.events.single().featureId)
        assertTrue(withEvents.evidenceGraph!!.edges.any { it.relationship == "SUPPORTS_EVENT" })
        assertEquals(withEvents, com.aynvora.core.models.KundaliSnapshotJson.decode(encoded))
        assertEquals(encoded, com.aynvora.core.models.KundaliSnapshotJson.encode(withEvents))
    }

    @Test
    fun testUnsupportedAyanamsaReturnsFailureWithoutSilentFallback() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                ayanamsa = AyanamsaConvention.RAMAN,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
        assertTrue(result.message.contains("RAMAN"))
    }

    @Test
    fun testPlacidusHouseSystemReturnsUnsupportedConfiguration() = runBlocking {
        val request = ChartRequest(
            birthData = sampleBirthData(),
            config = CalculationConfig(
                houseSystem = HouseSystem.PLACIDUS,
            ),
        )

        val result = sdk.calculateChart(request)
        assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(result)
        assertTrue(result.message.contains("Placidus"))
    }

    @Test
    fun unimplementedCalculationProfilesFailExplicitly() = runBlocking {
        for (profile in listOf(CalculationProfile.SURYA_SIDDHANTA, CalculationProfile.DRIG_GANITA)) {
            val outcome = sdk.calculateChart(
                ChartRequest(sampleBirthData(), CalculationConfig(profile = profile)),
            )
            assertIs<AynvoraResult.Failure.UnsupportedConfiguration>(outcome)
            assertTrue(outcome.message.contains(profile.name))
        }
    }

    @Test
    fun sdkAdapterPreservesEngineLongitudesWithoutIndependentRounding() = runBlocking {
        val config = CalculationConfig(ayanamsa = AyanamsaConvention.TROPICAL, houseSystem = HouseSystem.EQUAL_HOUSE)
        val publicResult = sdk.calculateChart(ChartRequest(sampleBirthData(), config))
        assertIs<AynvoraResult.Success<ChartResult>>(publicResult)
        val rawResult = com.aynvora.astro.AynvoraAstroEngine().calculate(
            com.aynvora.astro.BirthData(
                dateTimeIso = sampleBirthData().toIsoDateTimeString(),
                latitude = 28.6139,
                longitude = 77.2090,
                timeZoneId = "Asia/Kolkata",
                year = 2000,
                month = 1,
                day = 1,
                hour = 17,
                minute = 30,
                second = 0,
            ),
            com.aynvora.astro.EngineCalculationConfig(ayanamsa = "TROPICAL", houseSystem = "EQUAL_HOUSE"),
        )
        assertEquals(rawResult.positions.map { it.tropicalLongitude }, publicResult.value.planetaryPositions.map { it.tropicalLongitude })
        assertEquals(rawResult.positions.map { it.siderealLongitude }, publicResult.value.planetaryPositions.map { it.siderealLongitude })
        assertEquals(rawResult.houses.map { it.cuspLongitude }, publicResult.value.houses.map { it.cuspLongitude })
    }
}
