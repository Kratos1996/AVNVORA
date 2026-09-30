package com.aynvora.astro

import com.aynvora.astro.context.AstroCalculationContext
import com.aynvora.astro.pipeline.AstroFeaturePipeline
import com.aynvora.astro.pipeline.AyanamsaFeatureEngine
import com.aynvora.astro.pipeline.CoreFeatureKeys
import com.aynvora.astro.pipeline.PlanetaryPositionFeatureEngine
import com.aynvora.astro.pipeline.TimeFeatureEngine
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
        )
        val context = AstroCalculationContext.create(birth, EngineCalculationConfig())
        val outputs = AstroFeaturePipeline(
            listOf(PlanetaryPositionFeatureEngine, AyanamsaFeatureEngine, TimeFeatureEngine),
        ).calculate(context)

        assertEquals(2_451_545.0, context.julianDay.value, 0.000001)
        assertEquals(context.normalizedUtc, outputs.get(CoreFeatureKeys.Time).value)
        assertEquals(9, outputs.get(CoreFeatureKeys.PlanetaryPositions).value?.size)
        assertEquals("ANALYTICAL_MEEUS_SIMON_FORMULAE", outputs.get(CoreFeatureKeys.PlanetaryPositions).provenance.ephemerisSourceId)
        assertEquals("planetary_positions", outputs.get(CoreFeatureKeys.PlanetaryPositions).provenance.conventions["feature_id"])
        assertNotNull(outputs.get(CoreFeatureKeys.Ayanamsa).value)
        assertTrue(outputs.get(CoreFeatureKeys.PlanetaryPositions).warnings.isEmpty())
    }

    @Test
    fun requestedFeaturesResolveOnlySharedDependenciesOnceAndEmitTrace() = kotlinx.coroutines.runBlocking {
        val context = AstroCalculationContext.create(
            BirthData("2000-01-01T12:00:00", 28.6139, 77.2090, "Asia/Kolkata"),
            EngineCalculationConfig(),
        )
        val pipeline = AstroFeaturePipeline(listOf(
            TimeFeatureEngine, AyanamsaFeatureEngine, PlanetaryPositionFeatureEngine,
            DignityFeatureEngine, RelationshipFeatureEngine,
        ))
        val result = pipeline.calculateFeatures(context, setOf(CoreFeatureKeys.Dignities, CoreFeatureKeys.Relationships))
        assertEquals(5, result.trace.executedOnceCount)
        assertEquals(result.trace.executionOrder.size, result.trace.executionOrder.distinct().size)
        assertEquals(2, result.trace.requestedFeatures.size)
        assertTrue(result.trace.executionOrder.contains(CoreFeatureKeys.PlanetaryPositions.id))
        assertTrue(result.trace.reusedCount >= 4)
        assertEquals(0, result.trace.skippedUnsupportedFeatures.size)
    }
}
