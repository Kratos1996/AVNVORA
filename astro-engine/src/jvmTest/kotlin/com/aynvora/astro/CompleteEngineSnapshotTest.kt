package com.aynvora.astro

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Generates a reproducible engine sample plus a separate timing artifact under build/reports. */
class CompleteEngineSnapshotTest {
    @Test
    fun generateDeterministicMultiEngineSnapshot() = kotlinx.coroutines.runBlocking {
        val engine = AynvoraAstroEngine()
        val request = BirthData(
            dateTimeIso = "2000-01-01T17:30:00",
            latitude = 28.6139,
            longitude = 77.2090,
            timeZoneId = "Asia/Kolkata",
            year = 2000, month = 1, day = 1, hour = 17, minute = 30, second = 0,
            cityId = "IN-DL-DEL", cityName = "New Delhi",
        )
        val config = EngineCalculationConfig()
        val transitObservation = observation("2024-04-08", "18:30:00", 2024, 4, 8, 18, 30, request)
        val panchangObservation = observation("2024-04-08", "12:00:00", 2024, 4, 8, 12, 0, request)
            .copy(panchangVaraConvention = com.aynvora.astro.panchang.VaraConvention.LOCAL_SUNRISE.name)
        val inputMetadata = com.aynvora.astro.provenance.CalculationMetadata(
            calculationProfileId = config.profile,
            calculationModel = "EXPLICIT_OBSERVATION_CONTEXT",
        )
        val seed = com.aynvora.astro.pipeline.FeatureOutputs.Empty
            .withInput(com.aynvora.astro.pipeline.CoreFeatureKeys.TransitObservation, transitObservation, inputMetadata)
            .withInput(com.aynvora.astro.pipeline.CoreFeatureKeys.PanchangObservation, panchangObservation, inputMetadata)
        lateinit var calculated: CalculationResult
        val wallNanos = measureNanoTime {
            calculated = engine.calculateSnapshot(
                request, config, com.aynvora.astro.pipeline.CoreAstroFeatureRegistry.featureIds, seed,
            )
        }
        val trace = assertNotNull(calculated.executionTrace)
        assertEquals(com.aynvora.astro.pipeline.CoreAstroFeatureRegistry.featureIds.size, trace.executedOnceCount)
        assertEquals(trace.executionOrder.size, trace.executionOrder.distinct().size)
        assertEquals(12, calculated.commonChart?.houses?.size)
        assertEquals(9, calculated.grahSthiti?.rows?.size)
        assertEquals("2024-04-08", calculated.transit?.observation?.localDate)
        assertEquals("2024-04-08T13:00:00Z", calculated.transit?.observation?.utcTimestamp)
        assertEquals("2024-04-08", calculated.panchang?.observation?.localDate)
        assertTrue(calculated.panchang?.snapshot?.sunriseJulianDay != null)
        assertTrue(calculated.panchang?.snapshot?.sunsetJulianDay != null)

        val json = Json { prettyPrint = true; encodeDefaults = true; explicitNulls = true }
        val stableResult = calculated.copy(
            executionTrace = trace.copy(executionDurationNanos = 0, engineExecutionDurationNanos = emptyMap()),
        )
        val snapshotPayload = json.encodeToString(CalculationResult.serializer(), stableResult) + "\n"
        val reportDirectory = Path.of("build", "reports", "astro")
        Files.createDirectories(reportDirectory)
        val snapshot = reportDirectory.resolve("complete_engine_snapshot.json")
        Files.writeString(snapshot, snapshotPayload)

        val performance = PerformanceReport(
            engineExecutions = trace.engineExecutionDurationNanos.toSortedMap(),
            combinedPipelineNanos = trace.executionDurationNanos,
            fullCalculationWallNanos = wallNanos,
            dependencyReuseCount = trace.reusedCount,
            duplicateCalculationsAvoided = trace.duplicateCalculationsAvoided,
            executedEngineCount = trace.executedOnceCount,
            serializedSnapshotBytes = snapshotPayload.encodeToByteArray().size,
        )
        Files.writeString(reportDirectory.resolve("multi_engine_performance.json"), json.encodeToString(performance) + "\n")
        assertTrue(Files.size(snapshot) > 1_000)
    }

    private fun observation(
        date: String,
        time: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        location: BirthData,
    ): com.aynvora.astro.context.AstroObservationContext {
        val normalized = com.aynvora.astro.time.TimeNormalizer.normalizeUnambiguous(
            year, month, day, hour, minute, 0, location.timeZoneId,
        )
        return com.aynvora.astro.context.AstroObservationContext(
            date, time,
            "%04d-%02d-%02dT%02d:%02d:%02dZ".format(
                normalized.year, normalized.month, normalized.day,
                normalized.hour, normalized.minute, normalized.second.toInt(),
            ),
            location.timeZoneId, normalized.timezoneOffsetMinutes, normalized.julianDay.value,
            location.latitude, location.longitude, location.cityId, location.cityName,
            com.aynvora.astro.time.TimeNormalizer.TIMEZONE_DATA_VERSION,
        )
    }
}

@Serializable
private data class PerformanceReport(
    val engineExecutions: Map<String, Long>,
    val combinedPipelineNanos: Long,
    val fullCalculationWallNanos: Long,
    val dependencyReuseCount: Int,
    val duplicateCalculationsAvoided: Int,
    val executedEngineCount: Int,
    val serializedSnapshotBytes: Int,
)
