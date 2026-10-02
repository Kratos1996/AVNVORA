package com.aynvora.astro

import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.pipeline.AstroFeaturePipeline
import com.aynvora.astro.pipeline.AyanamsaFeatureEngine
import com.aynvora.astro.pipeline.CoreFeatureKeys
import com.aynvora.astro.pipeline.PlanetaryPositionFeatureEngine
import com.aynvora.astro.pipeline.TimeFeatureEngine
import com.aynvora.astro.pipeline.LocationFeatureEngine
import com.aynvora.astro.pipeline.EphemerisFeatureEngine
import com.aynvora.astro.pipeline.DignityFeatureEngine
import com.aynvora.astro.pipeline.RelationshipFeatureEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import com.aynvora.astro.time.TimeNormalizer
import kotlin.test.assertEquals

class AstroFeaturePipelineTest {
    @Test
    fun fullEngineCalculationNormalizesTimezoneExactlyOnce() = kotlinx.coroutines.runBlocking {
        var calls = 0
        val engine = AynvoraAstroEngine(timeResolver = { y, m, d, h, min, s, zone ->
            calls++
            TimeNormalizer.normalizeUnambiguous(y, m, d, h, min, s, zone)
        })
        val result = engine.calculate(
            BirthData("2000-01-01T17:30:00", 28.6139, 77.2090, "Asia/Kolkata"),
            EngineCalculationConfig(),
        )
        assertEquals(1, calls)
        assertEquals(2_451_545.0, result.julianDay, 0.000001)
        assertEquals("2000-01-01T12:00:00Z", result.utcTimestamp)
        assertEquals(330, result.timezoneOffsetMinutes)
    }

    @Test
    fun timezoneResolverRejectsUsDstGapAndFold() {
        val gap = runCatching { TimeNormalizer.normalizeUnambiguous(2024, 3, 10, 2, 30, 0, "America/New_York") }.exceptionOrNull()
        val fold = runCatching { TimeNormalizer.normalizeUnambiguous(2024, 11, 3, 1, 30, 0, "America/New_York") }.exceptionOrNull()
        assertTrue(gap?.message?.contains("does not exist") == true)
        assertTrue(fold?.message?.contains("ambiguous") == true)
        assertEquals(TimeNormalizer.LocalTimeStatus.INVALID_DST_GAP, TimeNormalizer.localTimeStatus(2024, 3, 10, 2, "America/New_York"))
        assertEquals(TimeNormalizer.LocalTimeStatus.AMBIGUOUS_FOLD, TimeNormalizer.localTimeStatus(2024, 11, 3, 1, "America/New_York"))
    }

    @Test
    fun dependencyOrderSharesOneNormalizedContextAndProducesProvenance() = kotlinx.coroutines.runBlocking {
        val birth = BirthData(
            dateTimeIso = "2000-01-01T17:30:00",
            latitude = 28.6139,
            longitude = 77.2090,
            timeZoneId = "Asia/Kolkata",
            cityId = "IN-DL-DEL",
            cityName = "New Delhi",
            locationDatasetVersion = "test-dataset-v1",
            locationProvenance = "test-catalog",
        )
        val context = AstroCalculationContext.create(birth, EngineCalculationConfig())
        val outputs = AstroFeaturePipeline(
            listOf(LocationFeatureEngine, PlanetaryPositionFeatureEngine, EphemerisFeatureEngine, AyanamsaFeatureEngine, TimeFeatureEngine),
        ).calculate(context)

        assertEquals(2_451_545.0, context.julianDay.value, 0.000001)
        assertEquals(context.normalizedUtc, outputs.get(CoreFeatureKeys.Time).value)
        assertEquals(9, outputs.get(CoreFeatureKeys.PlanetaryPositions).value?.size)
        assertEquals("ANALYTICAL_MEEUS_SIMON_FORMULAE", outputs.get(CoreFeatureKeys.PlanetaryPositions).provenance.ephemerisSourceId)
        assertEquals("planetary_positions", outputs.get(CoreFeatureKeys.PlanetaryPositions).provenance.conventions["feature_id"])
        assertNotNull(outputs.get(CoreFeatureKeys.Ayanamsa).value)
        assertTrue(outputs.get(CoreFeatureKeys.PlanetaryPositions).warnings.isEmpty())
        assertEquals("ANALYTICAL_MEEUS_SIMON_FORMULAE", outputs.get(CoreFeatureKeys.Ephemeris).value?.providerId)
        assertEquals(9, outputs.get(CoreFeatureKeys.Ephemeris).value?.rawValues?.size)
        assertEquals(com.aynvora.astro.provenance.EphemerisLicenseStatus.NOT_VERIFIED, outputs.get(CoreFeatureKeys.Ephemeris).value?.providerMetadata?.redistributionStatus)
        assertEquals(null, outputs.get(CoreFeatureKeys.Ephemeris).value?.providerMetadata?.supportedEpochStartJulianDay)
        assertEquals(com.aynvora.astro.pipeline.FeatureStatus.PARTIAL, outputs.get(CoreFeatureKeys.Location).status)
        assertEquals(28.6139, outputs.get(CoreFeatureKeys.Location).value?.latitude)
        assertEquals("IN-DL-DEL", outputs.get(CoreFeatureKeys.Location).value?.cityId)
        assertEquals("test-dataset-v1", outputs.get(CoreFeatureKeys.Location).value?.locationDatasetVersion)
        assertEquals("test-catalog", outputs.get(CoreFeatureKeys.Location).value?.locationProvenance)
        assertEquals(TimeNormalizer.TIMEZONE_DATA_VERSION, outputs.get(CoreFeatureKeys.Location).value?.timezoneDataVersion)
    }

    @Test
    fun requestedFeaturesResolveOnlySharedDependenciesOnceAndEmitTrace() = kotlinx.coroutines.runBlocking {
        val context = AstroCalculationContext.create(
            BirthData("2000-01-01T12:00:00", 28.6139, 77.2090, "Asia/Kolkata"),
            EngineCalculationConfig(),
        )
        val pipeline = AstroFeaturePipeline(listOf(
            LocationFeatureEngine, TimeFeatureEngine, AyanamsaFeatureEngine, EphemerisFeatureEngine, PlanetaryPositionFeatureEngine,
            DignityFeatureEngine, RelationshipFeatureEngine,
        ))
        val result = pipeline.calculateFeatures(context, setOf(CoreFeatureKeys.Dignities, CoreFeatureKeys.Relationships))
        assertEquals(7, result.trace.executedOnceCount)
        assertEquals(result.trace.executionOrder.size, result.trace.executionOrder.distinct().size)
        assertEquals(2, result.trace.requestedFeatures.size)
        assertTrue(result.trace.executionOrder.contains(CoreFeatureKeys.PlanetaryPositions.id))
        assertTrue(result.trace.reusedCount >= 4)
        assertEquals(0, result.trace.skippedUnsupportedFeatures.size)
    }

    @Test
    fun chartAndGrahSthitiEnginesProduceSerializableSharedPlanetData() = kotlinx.coroutines.runBlocking {
        val context = AstroCalculationContext.create(
            BirthData("2000-01-01T12:00:00", 28.6139, 77.2090, "Asia/Kolkata"),
            EngineCalculationConfig(),
        )
        val engines = com.aynvora.astro.pipeline.CoreAstroFeatureRegistry.engines(com.aynvora.astro.varga.DefaultVargaEngine())
        val pipeline = AstroFeaturePipeline(engines)
        val result = pipeline.calculateFeatures(context, setOf(CoreFeatureKeys.GrahSthiti, CoreFeatureKeys.Chart))
        val chart = result.outputs.get(CoreFeatureKeys.Chart).value ?: error("No chart value")
        val grahSthiti = result.outputs.get(CoreFeatureKeys.GrahSthiti).value ?: error("No Grah Sthiti value")

        assertEquals(12, chart.houses.size)
        assertEquals(9, chart.houses.sumOf { it.planets.size })
        assertEquals(9, grahSthiti.rows.size)
        val moonChart = chart.houses.flatMap { house -> house.planets.map { house.houseNumber to it } }.single { it.second.bodyId == BodyId.MOON }
        val moonTable = grahSthiti.rows.single { it.bodyId == BodyId.MOON }
        assertEquals(moonChart.first, moonTable.houseNumber)
        assertEquals(moonChart.second.rashiIndex, moonTable.signIndex)
        assertEquals(moonChart.second.siderealLongitude, moonTable.degreeInSign + moonTable.signIndex * 30.0, 1e-8)
        assertEquals(result.outputs.get(CoreFeatureKeys.PlanetaryPositions).value?.single { it.bodyId == BodyId.MOON }, moonChart.second)
        assertEquals("chart", chart.provenance.conventions["feature_id"])
        assertEquals("grah_sthiti", grahSthiti.provenance.conventions["feature_id"])
        assertEquals(0, result.trace.executionOrder.count { it == CoreFeatureKeys.PlanetaryPositions.id } - 1)
        assertTrue(result.trace.executionOrder.contains(CoreFeatureKeys.Chart.id))
        assertTrue(result.trace.executionOrder.contains(CoreFeatureKeys.GrahSthiti.id))
        val json = kotlinx.serialization.json.Json
        assertTrue(json.encodeToString(com.aynvora.astro.pipeline.AstroChartFeatureResult.serializer(), chart).contains("\"houses\""))
        assertTrue(json.encodeToString(com.aynvora.astro.pipeline.GrahSthitiResult.serializer(), grahSthiti).contains("\"rows\""))
    }

    @Test
    fun featureRegistryDescribesEnginesAndLeavesChalitAmbiguous() {
        val descriptors = com.aynvora.astro.pipeline.CoreAstroFeatureRegistry.descriptors(com.aynvora.astro.varga.DefaultVargaEngine()).associateBy { it.featureId }
        assertTrue(descriptors.containsKey(CoreFeatureKeys.Chart.id))
        assertTrue(descriptors.containsKey(CoreFeatureKeys.GrahSthiti.id))
        assertEquals(com.aynvora.astro.pipeline.FeatureStatus.AMBIGUOUS, descriptors[CoreFeatureKeys.Chalit.id]?.status)
        assertEquals(listOf(CoreFeatureKeys.Chart.id, CoreFeatureKeys.Dignities.id, CoreFeatureKeys.PlanetStates.id).sorted(), descriptors[CoreFeatureKeys.GrahSthiti.id]?.dependencies)
    }

    @Test
    fun deterministicFeatureCacheHitsSameInputsAndSeparatesCalculationInputs() = kotlinx.coroutines.runBlocking {
        val birth = BirthData("2000-01-01T12:00:00", 28.6139, 77.2090, "Asia/Kolkata", cityId = "DEL", locationDatasetVersion = "loc-v1")
        val base = AstroCalculationContext.create(birth, EngineCalculationConfig(), providerId = "provider-a", dataVersion = "data-v1")
        val cache = com.aynvora.astro.pipeline.InMemoryAstroCalculationCache()
        val pipeline = AstroFeaturePipeline(listOf(LocationFeatureEngine, TimeFeatureEngine), cache)
        val first = pipeline.calculateFeature(base, CoreFeatureKeys.Time)
        val second = pipeline.calculateFeature(base, CoreFeatureKeys.Time)
        assertEquals(0, first.trace.cacheHitCount)
        assertEquals(2, second.trace.cacheHitCount)

        val key = com.aynvora.astro.pipeline.AstroCalculationCacheKey(base, LocationFeatureEngine, com.aynvora.astro.pipeline.FeatureOutputs.Empty)
        val timezone = AstroCalculationContext.create(birth.copy(timeZoneId = "UTC"), EngineCalculationConfig(), providerId = "provider-a", dataVersion = "data-v1")
        val profile = AstroCalculationContext.create(birth, EngineCalculationConfig(profile = "ALTERNATE"), providerId = "provider-a", dataVersion = "data-v1")
        val location = AstroCalculationContext.create(birth.copy(longitude = 78.0), EngineCalculationConfig(), providerId = "provider-a", dataVersion = "data-v1")
        val provider = AstroCalculationContext.create(birth, EngineCalculationConfig(), providerId = "provider-b", dataVersion = "data-v1")
        val providerVersion = AstroCalculationContext.create(birth, EngineCalculationConfig(), providerId = "provider-a", providerVersion = "provider-v2", dataVersion = "data-v1")
        val data = AstroCalculationContext.create(birth, EngineCalculationConfig(), providerId = "provider-a", dataVersion = "data-v2")
        listOf(timezone, profile, location, provider, providerVersion, data).forEach { changed ->
            assertTrue(key != com.aynvora.astro.pipeline.AstroCalculationCacheKey(changed, LocationFeatureEngine, com.aynvora.astro.pipeline.FeatureOutputs.Empty))
        }
    }

}
