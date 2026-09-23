package com.aynvora.core

import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthProfile
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.CalculationProfile
import com.aynvora.core.models.ChartCalculationStatus
import com.aynvora.core.models.Coordinates
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.SavedChart
import com.aynvora.core.models.ThemePreference
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.models.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainContractsTest {

    private fun sampleBirthData(): BirthData = BirthData(
        date = BirthDate(1996, 5, 20),
        time = BirthTime(14, 30, 0),
        place = BirthPlace(
            name = "New Delhi",
            coordinates = Coordinates(28.6139, 77.2090),
            timezoneId = "Asia/Kolkata",
            country = "India",
            id = "delhi_01",
        ),
    )

    @Test
    fun testUserProfileValidCreation() {
        val profile = UserProfile(
            id = "user_001",
            displayName = "Arjuna",
            contextNotes = "Consultation profile",
            createdAtEpochMs = 1700000000000L,
            updatedAtEpochMs = 1700000001000L,
        )
        assertEquals("user_001", profile.id)
        assertEquals("Arjuna", profile.displayName)
        assertEquals("Consultation profile", profile.contextNotes)
        assertEquals(1700000000000L, profile.createdAtEpochMs)
        assertEquals(1700000001000L, profile.updatedAtEpochMs)
    }

    @Test
    fun testUserProfileValidationRejections() {
        assertFailsWith<IllegalArgumentException> {
            UserProfile(id = "", displayName = "Arjuna", createdAtEpochMs = 100L, updatedAtEpochMs = 100L)
        }
        assertFailsWith<IllegalArgumentException> {
            UserProfile(id = "u1", displayName = "   ", createdAtEpochMs = 100L, updatedAtEpochMs = 100L)
        }
        assertFailsWith<IllegalArgumentException> {
            UserProfile(id = "u1", displayName = "Arjuna", createdAtEpochMs = -1L, updatedAtEpochMs = 100L)
        }
        assertFailsWith<IllegalArgumentException> {
            UserProfile(id = "u1", displayName = "Arjuna", createdAtEpochMs = 200L, updatedAtEpochMs = 100L)
        }
    }

    @Test
    fun testBirthProfileValidationAndAccessors() {
        val birthData = sampleBirthData()
        val profile = BirthProfile(
            id = "bp_001",
            name = "Standard Birth Profile",
            birthData = birthData,
            notes = "Primary subject",
            createdAtEpochMs = 1700000000000L,
            updatedAtEpochMs = 1700000000000L,
        )

        assertEquals("bp_001", profile.id)
        assertEquals("Standard Birth Profile", profile.name)
        assertEquals(1996, profile.date.year)
        assertEquals(14, profile.time.hour)
        assertEquals("Asia/Kolkata", profile.timezoneId)
        assertEquals("New Delhi", profile.place.name)
        assertEquals("India", profile.place.country)
        assertEquals("delhi_01", profile.place.id)
    }

    @Test
    fun testBirthProfileValidationRejections() {
        val birthData = sampleBirthData()
        assertFailsWith<IllegalArgumentException> {
            BirthProfile(id = "", name = "Name", birthData = birthData, createdAtEpochMs = 100L, updatedAtEpochMs = 100L)
        }
        assertFailsWith<IllegalArgumentException> {
            BirthProfile(id = "bp1", name = " ", birthData = birthData, createdAtEpochMs = 100L, updatedAtEpochMs = 100L)
        }
        assertFailsWith<IllegalArgumentException> {
            BirthProfile(id = "bp1", name = "Name", birthData = birthData, createdAtEpochMs = 200L, updatedAtEpochMs = 100L)
        }
    }

    @Test
    fun testBirthPlaceValidation() {
        val place = BirthPlace(
            name = "Varanasi",
            coordinates = Coordinates(25.3176, 82.9739),
            timezoneId = "Asia/Kolkata",
        )
        assertEquals("Varanasi", place.name)
        assertNull(place.country)
        assertNull(place.id)

        assertFailsWith<IllegalArgumentException> {
            BirthPlace(
                name = "Invalid",
                coordinates = Coordinates(25.0, 82.0),
                timezoneId = "",
            )
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinates(91.0, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            Coordinates(0.0, 181.0)
        }
    }

    @Test
    fun testSavedChartValidation() {
        val chart = SavedChart(
            id = "chart_101",
            birthProfileId = "bp_001",
            calculationConfig = CalculationConfig(
                profile = CalculationProfile.STANDARD_VEDIC,
                ayanamsa = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
                houseSystem = HouseSystem.EQUAL_HOUSE,
            ),
            engineVersion = "0.1.0",
            calculationTimestampEpochMs = 1700000000000L,
            schemaVersion = 1,
            status = ChartCalculationStatus.COMPLETED,
            cachedResultJson = "{\"sample\": true}",
        )

        assertEquals("chart_101", chart.id)
        assertEquals("bp_001", chart.birthProfileId)
        assertEquals(CalculationProfile.STANDARD_VEDIC, chart.calculationConfig.profile)
        assertEquals(ChartCalculationStatus.COMPLETED, chart.status)
        assertEquals(1, chart.schemaVersion)

        assertFailsWith<IllegalArgumentException> {
            chart.copy(id = "")
        }
        assertFailsWith<IllegalArgumentException> {
            chart.copy(birthProfileId = "   ")
        }
        assertFailsWith<IllegalArgumentException> {
            chart.copy(engineVersion = "")
        }
        assertFailsWith<IllegalArgumentException> {
            chart.copy(calculationTimestampEpochMs = 0L)
        }
        assertFailsWith<IllegalArgumentException> {
            chart.copy(schemaVersion = 0)
        }
    }

    @Test
    fun testUserPreferencesDefaultsAndModifications() {
        val defaults = UserPreferences()
        assertEquals(ThemePreference.SYSTEM, defaults.theme)
        assertEquals("en", defaults.languageCode)
        assertEquals(CalculationProfile.STANDARD_VEDIC, defaults.defaultCalculationProfile)
        assertEquals(AyanamsaConvention.LAHIRI_CHITRAPAKSHA, defaults.defaultAyanamsa)
        assertEquals(HouseSystem.EQUAL_HOUSE, defaults.defaultHouseSystem)

        val custom = defaults.copy(
            theme = ThemePreference.DARK,
            languageCode = "hi",
            defaultAyanamsa = AyanamsaConvention.KRISHNAMURTI_KP,
        )
        assertEquals(ThemePreference.DARK, custom.theme)
        assertEquals("hi", custom.languageCode)
        assertEquals(AyanamsaConvention.KRISHNAMURTI_KP, custom.defaultAyanamsa)

        assertFailsWith<IllegalArgumentException> {
            defaults.copy(languageCode = "")
        }
    }
}
