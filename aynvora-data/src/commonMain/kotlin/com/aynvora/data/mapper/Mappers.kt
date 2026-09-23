package com.aynvora.data.mapper

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
import com.aynvora.data.entity.BirthProfileEntity
import com.aynvora.data.entity.SavedChartEntity
import com.aynvora.data.entity.UserPreferencesEntity
import com.aynvora.data.entity.UserProfileEntity

internal fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        id = id,
        displayName = displayName,
        contextNotes = contextNotes,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

internal fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        displayName = displayName,
        contextNotes = contextNotes,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

internal fun BirthProfileEntity.toDomain(): BirthProfile =
    BirthProfile(
        id = id,
        name = name,
        birthData = BirthData(
            date = BirthDate(year = year, month = month, day = day),
            time = BirthTime(hour = hour, minute = minute, second = second),
            place = BirthPlace(
                name = placeName,
                coordinates = Coordinates(latitude = latitude, longitude = longitude),
                timezoneId = timezoneId,
                country = country,
                id = placeId,
            ),
        ),
        notes = notes,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

internal fun BirthProfile.toEntity(): BirthProfileEntity =
    BirthProfileEntity(
        id = id,
        name = name,
        year = date.year,
        month = date.month,
        day = date.day,
        hour = time.hour,
        minute = time.minute,
        second = time.second,
        placeName = place.name,
        latitude = place.coordinates.latitude,
        longitude = place.coordinates.longitude,
        timezoneId = place.timezoneId,
        country = place.country,
        placeId = place.id,
        notes = notes,
        createdAtEpochMs = createdAtEpochMs,
        updatedAtEpochMs = updatedAtEpochMs,
    )

internal fun SavedChartEntity.toDomain(): SavedChart =
    SavedChart(
        id = id,
        birthProfileId = birthProfileId,
        calculationConfig = CalculationConfig(
            profile = CalculationProfile.valueOf(calculationProfile),
            ayanamsa = AyanamsaConvention.valueOf(ayanamsa),
            houseSystem = HouseSystem.valueOf(houseSystem),
        ),
        engineVersion = engineVersion,
        calculationTimestampEpochMs = calculationTimestampEpochMs,
        schemaVersion = schemaVersion,
        status = ChartCalculationStatus.valueOf(status),
        cachedResultJson = cachedResultJson,
    )

internal fun SavedChart.toEntity(): SavedChartEntity =
    SavedChartEntity(
        id = id,
        birthProfileId = birthProfileId,
        calculationProfile = calculationConfig.profile.name,
        ayanamsa = calculationConfig.ayanamsa.name,
        houseSystem = calculationConfig.houseSystem.name,
        engineVersion = engineVersion,
        calculationTimestampEpochMs = calculationTimestampEpochMs,
        schemaVersion = schemaVersion,
        status = status.name,
        cachedResultJson = cachedResultJson,
    )

internal fun UserPreferencesEntity.toDomain(): UserPreferences =
    UserPreferences(
        theme = ThemePreference.valueOf(theme),
        languageCode = languageCode,
        defaultCalculationProfile = CalculationProfile.valueOf(defaultCalculationProfile),
        defaultAyanamsa = AyanamsaConvention.valueOf(defaultAyanamsa),
        defaultHouseSystem = HouseSystem.valueOf(defaultHouseSystem),
    )

internal fun UserPreferences.toEntity(): UserPreferencesEntity =
    UserPreferencesEntity(
        theme = theme.name,
        languageCode = languageCode,
        defaultCalculationProfile = defaultCalculationProfile.name,
        defaultAyanamsa = defaultAyanamsa.name,
        defaultHouseSystem = defaultHouseSystem.name,
    )
