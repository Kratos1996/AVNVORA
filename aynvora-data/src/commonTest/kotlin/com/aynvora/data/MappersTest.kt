package com.aynvora.data

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
import com.aynvora.data.mapper.toDomain
import com.aynvora.data.mapper.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class MappersTest {

    @Test
    fun testUserProfileBidirectionalMapping() {
        val domain = UserProfile(
            id = "user_42",
            displayName = "Bhishma",
            contextNotes = "Senior Consultant",
            createdAtEpochMs = 1700000000000L,
            updatedAtEpochMs = 1700000005000L,
        )

        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.displayName, entity.displayName)
        assertEquals(domain.contextNotes, entity.contextNotes)
        assertEquals(domain.createdAtEpochMs, entity.createdAtEpochMs)
        assertEquals(domain.updatedAtEpochMs, entity.updatedAtEpochMs)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }

    @Test
    fun testBirthProfileBidirectionalMapping() {
        val domain = BirthProfile(
            id = "bp_42",
            name = "Royal Profile",
            birthData = BirthData(
                date = BirthDate(1990, 8, 15),
                time = BirthTime(6, 15, 30),
                place = BirthPlace(
                    name = "Hastinapur",
                    coordinates = Coordinates(29.17, 78.02),
                    timezoneId = "Asia/Kolkata",
                    country = "India",
                    id = "hastinapur_01",
                ),
            ),
            notes = "Primary chart record",
            createdAtEpochMs = 1700000000000L,
            updatedAtEpochMs = 1700000002000L,
        )

        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(1990, entity.year)
        assertEquals(8, entity.month)
        assertEquals(15, entity.day)
        assertEquals(6, entity.hour)
        assertEquals(15, entity.minute)
        assertEquals(30, entity.second)
        assertEquals("Hastinapur", entity.placeName)
        assertEquals(29.17, entity.latitude)
        assertEquals(78.02, entity.longitude)
        assertEquals("Asia/Kolkata", entity.timezoneId)
        assertEquals("India", entity.country)
        assertEquals("hastinapur_01", entity.placeId)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }

    @Test
    fun testSavedChartBidirectionalMapping() {
        val domain = SavedChart(
            id = "chart_42",
            birthProfileId = "bp_42",
            calculationConfig = CalculationConfig(
                profile = CalculationProfile.DRIG_GANITA,
                ayanamsa = AyanamsaConvention.KRISHNAMURTI_KP,
                houseSystem = HouseSystem.PLACIDUS,
            ),
            engineVersion = "0.1.0",
            calculationTimestampEpochMs = 1700000000000L,
            schemaVersion = 1,
            status = ChartCalculationStatus.COMPLETED,
            cachedResultJson = "{\"status\": \"cached\"}",
        )

        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals("DRIG_GANITA", entity.calculationProfile)
        assertEquals("KRISHNAMURTI_KP", entity.ayanamsa)
        assertEquals("PLACIDUS", entity.houseSystem)
        assertEquals("COMPLETED", entity.status)
        assertEquals("{\"status\": \"cached\"}", entity.cachedResultJson)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }

    @Test
    fun testUserPreferencesBidirectionalMapping() {
        val domain = UserPreferences(
            theme = ThemePreference.DARK,
            languageCode = "hi",
            defaultCalculationProfile = CalculationProfile.SURYA_SIDDHANTA,
            defaultAyanamsa = AyanamsaConvention.RAMAN,
            defaultHouseSystem = HouseSystem.SHRIPATI_PORPHYRY,
        )

        val entity = domain.toEntity()
        assertEquals("DARK", entity.theme)
        assertEquals("hi", entity.languageCode)
        assertEquals("SURYA_SIDDHANTA", entity.defaultCalculationProfile)
        assertEquals("RAMAN", entity.defaultAyanamsa)
        assertEquals("SHRIPATI_PORPHYRY", entity.defaultHouseSystem)

        val mappedBack = entity.toDomain()
        assertEquals(domain, mappedBack)
    }
}
