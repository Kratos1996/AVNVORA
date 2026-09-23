package com.aynvora.data

import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthProfile
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CalculationProfile
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.SavedChart
import com.aynvora.core.models.ThemePreference
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.models.UserProfile
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RepositoryTest {

    private lateinit var repos: AynvoraRepositories

    @BeforeTest
    fun setup() {
        repos = AynvoraDataFactory.createInMemory()
    }

    private fun sampleBirthData(): BirthData = BirthData(
        date = BirthDate(1995, 10, 24),
        time = BirthTime(8, 15, 0),
        place = BirthPlace(
            name = "Bengaluru",
            coordinates = Coordinates(12.9716, 77.5946),
            timezoneId = "Asia/Kolkata",
        ),
    )

    @Test
    fun testUserProfileCrudLifecycle() {
        runBlocking {
            val userRepo = repos.userProfiles

            val profile = UserProfile(
                id = "user_101",
                displayName = "Nakula",
                contextNotes = "Consultant",
                createdAtEpochMs = 1700000000000L,
                updatedAtEpochMs = 1700000000000L,
            )

            // 1. Get non-existent
            val missingResult = userRepo.getProfile("user_101")
            assertIs<AynvoraResult.Failure.NotFound>(missingResult)
            assertEquals("user_101", missingResult.resourceId)

            // 2. Save
            val saveResult = userRepo.saveProfile(profile)
            assertIs<AynvoraResult.Success<UserProfile>>(saveResult)
            assertEquals(profile, saveResult.value)

            // 3. Read back
            val readResult = userRepo.getProfile("user_101")
            assertIs<AynvoraResult.Success<UserProfile>>(readResult)
            assertEquals(profile, readResult.value)

            // 4. Update
            val updatedProfile = profile.copy(
                displayName = "Nakula Pandava",
                updatedAtEpochMs = 1700000005000L,
            )
            val updateResult = userRepo.saveProfile(updatedProfile)
            assertIs<AynvoraResult.Success<UserProfile>>(updateResult)

            // 5. List
            val allResult = userRepo.getAllProfiles()
            assertIs<AynvoraResult.Success<List<UserProfile>>>(allResult)
            assertEquals(1, allResult.value.size)
            assertEquals("Nakula Pandava", allResult.value.first().displayName)

            // 6. Delete
            val deleteResult = userRepo.deleteProfile("user_101")
            assertIs<AynvoraResult.Success<Unit>>(deleteResult)

            // 7. Verify deleted
            val afterDelete = userRepo.getProfile("user_101")
            assertIs<AynvoraResult.Failure.NotFound>(afterDelete)
        }
    }

    @Test
    fun testBirthProfileCrudLifecycle() {
        runBlocking {
            val birthRepo = repos.birthProfiles

            val birthProfile = BirthProfile(
                id = "bp_201",
                name = "Sahadeva Profile",
                birthData = sampleBirthData(),
                notes = "Twin profile",
                createdAtEpochMs = 1700000000000L,
                updatedAtEpochMs = 1700000000000L,
            )

            // 1. Get non-existent
            val missing = birthRepo.getBirthProfile("bp_201")
            assertIs<AynvoraResult.Failure.NotFound>(missing)

            // 2. Save
            val saveResult = birthRepo.saveBirthProfile(birthProfile)
            assertIs<AynvoraResult.Success<BirthProfile>>(saveResult)

            // 3. Get
            val getResult = birthRepo.getBirthProfile("bp_201")
            assertIs<AynvoraResult.Success<BirthProfile>>(getResult)
            assertEquals(birthProfile, getResult.value)

            // 4. List
            val listResult = birthRepo.getAllBirthProfiles()
            assertIs<AynvoraResult.Success<List<BirthProfile>>>(listResult)
            assertEquals(1, listResult.value.size)

            // 5. Delete
            val delResult = birthRepo.deleteBirthProfile("bp_201")
            assertIs<AynvoraResult.Success<Unit>>(delResult)

            val afterDel = birthRepo.getBirthProfile("bp_201")
            assertIs<AynvoraResult.Failure.NotFound>(afterDel)
        }
    }

    @Test
    fun testSavedChartLifecycleAndFilter() {
        runBlocking {
            val chartRepo = repos.savedCharts

            val chart1 = SavedChart(
                id = "chart_1",
                birthProfileId = "bp_A",
                calculationConfig = CalculationConfig(
                    profile = CalculationProfile.STANDARD_VEDIC,
                    ayanamsa = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
                    houseSystem = HouseSystem.EQUAL_HOUSE,
                ),
                engineVersion = "0.1.0",
                calculationTimestampEpochMs = 1700000000000L,
            )
            val chart2 = SavedChart(
                id = "chart_2",
                birthProfileId = "bp_B",
                calculationConfig = CalculationConfig(),
                engineVersion = "0.1.0",
                calculationTimestampEpochMs = 1700000002000L,
            )

            // Save both
            chartRepo.saveChart(chart1)
            chartRepo.saveChart(chart2)

            // Query by birthProfileId
            val chartsA = chartRepo.getChartsForBirthProfile("bp_A")
            assertIs<AynvoraResult.Success<List<SavedChart>>>(chartsA)
            assertEquals(1, chartsA.value.size)
            assertEquals("chart_1", chartsA.value.first().id)

            // Query all
            val allCharts = chartRepo.getAllSavedCharts()
            assertIs<AynvoraResult.Success<List<SavedChart>>>(allCharts)
            assertEquals(2, allCharts.value.size)

            // Delete chart1
            chartRepo.deleteChart("chart_1")
            val afterDel = chartRepo.getSavedChart("chart_1")
            assertIs<AynvoraResult.Failure.NotFound>(afterDel)
        }
    }

    @Test
    fun testUserPreferencesGetAndUpdate() {
        runBlocking {
            val prefRepo = repos.userPreferences

            // 1. Initial defaults
            val defaultPref = prefRepo.getPreferences()
            assertIs<AynvoraResult.Success<UserPreferences>>(defaultPref)
            assertEquals(ThemePreference.SYSTEM, defaultPref.value.theme)
            assertEquals("en", defaultPref.value.languageCode)

            // 2. Update preferences
            val newPref = defaultPref.value.copy(
                theme = ThemePreference.DARK,
                languageCode = "sa",
                defaultCalculationProfile = CalculationProfile.SURYA_SIDDHANTA,
            )
            val updateResult = prefRepo.updatePreferences(newPref)
            assertIs<AynvoraResult.Success<UserPreferences>>(updateResult)
            assertEquals(ThemePreference.DARK, updateResult.value.theme)

            // 3. Read back
            val readResult = prefRepo.getPreferences()
            assertIs<AynvoraResult.Success<UserPreferences>>(readResult)
            assertEquals(ThemePreference.DARK, readResult.value.theme)
            assertEquals("sa", readResult.value.languageCode)
            assertEquals(CalculationProfile.SURYA_SIDDHANTA, readResult.value.defaultCalculationProfile)
        }
    }

    @Test
    fun testReactiveFlowObservation() {
        runBlocking {
            val userRepo = repos.userProfiles

            val profile = UserProfile(
                id = "user_obs",
                displayName = "Observer",
                createdAtEpochMs = 1700000000000L,
                updatedAtEpochMs = 1700000000000L,
            )

            // Initial emission should be NotFound
            val initialEmission = userRepo.observeProfile("user_obs").first()
            assertIs<AynvoraResult.Failure.NotFound>(initialEmission)

            // Save profile
            userRepo.saveProfile(profile)

            // Next emission should be Success
            val nextEmission = userRepo.observeProfile("user_obs").first()
            assertIs<AynvoraResult.Success<UserProfile>>(nextEmission)
            assertEquals("Observer", nextEmission.value.displayName)
        }
    }
}
