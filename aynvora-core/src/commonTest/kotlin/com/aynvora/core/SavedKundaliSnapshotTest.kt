package com.aynvora.core

import com.aynvora.core.models.*
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SavedKundaliSnapshotTest {
    @Test
    fun snapshotPersistsAndReopensCachedJsonWithoutRecomputing() = runBlocking {
        val repository = MemorySavedCharts()
        val sdk = Aynvora.create(savedCharts = repository)
        val birth = BirthData(BirthDate(2000, 1, 1), BirthTime(17, 30), BirthPlace("Delhi", Coordinates(28.6139, 77.209), "Asia/Kolkata"))
        val generated = sdk.astrology.calculateKundali(ChartRequest(birth), "birth-1", "Delhi")
        assertIs<AynvoraResult.Success<KundaliSnapshot>>(generated)
        val saved = sdk.persistKundaliSnapshot(generated.value, "birth-1", "chart-1", 1_700_000_000_000)
        assertIs<AynvoraResult.Success<SavedChart>>(saved)
        assertEquals(1, repository.saveCount)
        val opened = sdk.openSavedKundaliSnapshot("chart-1")
        assertIs<AynvoraResult.Success<SavedKundaliSnapshot>>(opened)
        assertEquals(SavedSnapshotOpenStatus.OPEN, opened.value.status)
        assertEquals(generated.value, opened.value.snapshot)
        assertEquals(1, repository.readCount)
    }

    private class MemorySavedCharts : SavedChartRepository {
        private val charts = mutableMapOf<String, SavedChart>()
        var saveCount = 0
        var readCount = 0
        override suspend fun getSavedChart(id: String): AynvoraResult<SavedChart> {
            readCount++
            return charts[id]?.let { AynvoraResult.Success(it) }
                ?: AynvoraResult.Failure.NotFound(id, "Not found")
        }
        override suspend fun getChartsForBirthProfile(birthProfileId: String) = AynvoraResult.Success(charts.values.filter { it.birthProfileId == birthProfileId })
        override suspend fun getAllSavedCharts() = AynvoraResult.Success(charts.values.toList())
        override suspend fun saveChart(chart: SavedChart): AynvoraResult<SavedChart> {
            saveCount++
            charts[chart.id] = chart
            return AynvoraResult.Success(chart)
        }
        override suspend fun deleteChart(id: String): AynvoraResult<Unit> { charts.remove(id); return AynvoraResult.Success(Unit) }
        override fun observeChartsForBirthProfile(birthProfileId: String): Flow<AynvoraResult<List<SavedChart>>> = flowOf(AynvoraResult.Success(charts.values.filter { it.birthProfileId == birthProfileId }))
    }
}
