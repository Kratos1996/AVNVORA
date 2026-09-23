package com.aynvora.localization

import com.aynvora.core.models.AyanamsaConvention
import com.aynvora.core.models.CalculationProfile
import com.aynvora.core.models.HouseSystem
import com.aynvora.core.models.ThemePreference
import com.aynvora.core.models.UserPreferences
import com.aynvora.core.repository.UserPreferencesRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.localization.locale.AynvoraLocaleManagerImpl
import com.aynvora.localization.locale.LanguageRegistry
import com.aynvora.localization.locale.SupportedLocale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests 5–11: Runtime locale switching, StateFlow, persistence, startup restoration.
 */
class LocaleManagerTest {

    // ── Fake repository ───────────────────────────────────────────────────

    private class FakePreferencesRepository(
        initialLocaleId: String = "en",
    ) : UserPreferencesRepository {

        private val _prefs = MutableStateFlow(
            UserPreferences(
                languageCode = initialLocaleId,
                theme = ThemePreference.SYSTEM,
                defaultCalculationProfile = CalculationProfile.STANDARD_VEDIC,
                defaultAyanamsa = AyanamsaConvention.LAHIRI_CHITRAPAKSHA,
                defaultHouseSystem = HouseSystem.EQUAL_HOUSE,
            ),
        )

        var savedPreferences: UserPreferences? = null
        var saveCount = 0

        override suspend fun getPreferences(): AynvoraResult<UserPreferences> =
            AynvoraResult.Success(_prefs.value)

        override suspend fun updatePreferences(preferences: UserPreferences): AynvoraResult<UserPreferences> {
            _prefs.value = preferences
            savedPreferences = preferences
            saveCount++
            return AynvoraResult.Success(preferences)
        }

        override fun observePreferences(): Flow<AynvoraResult<UserPreferences>> = flow {
            emit(AynvoraResult.Success(_prefs.value))
        }
    }

    private class FailingPreferencesRepository : UserPreferencesRepository {
        override suspend fun getPreferences(): AynvoraResult<UserPreferences> =
            AynvoraResult.Failure.InternalFailure("Simulated IO failure")
        override suspend fun updatePreferences(p: UserPreferences): AynvoraResult<UserPreferences> =
            AynvoraResult.Failure.InternalFailure("Simulated IO failure")
        override fun observePreferences(): Flow<AynvoraResult<UserPreferences>> = flow {
            emit(AynvoraResult.Failure.InternalFailure("Simulated IO failure"))
        }
    }

    // ── Test 5: Runtime locale switching ─────────────────────────────────
    @Test
    fun testSetLocaleUpdatesCurrentLocale() = runTest {
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        assertEquals("en", manager.currentLocale.value.localeId)

        manager.setLocale(LanguageRegistry.HINDI)
        assertEquals("hi", manager.currentLocale.value.localeId)
    }

    // ── Test 6: StateFlow emits new locale ────────────────────────────────
    @Test
    fun testStateFlowEmitsNewValueOnLocaleChange() = runTest {
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        val emittedValues = mutableListOf<String>()
        val initialLocaleId = manager.currentLocale.value.localeId
        emittedValues.add(initialLocaleId)

        manager.setLocale("hi")
        emittedValues.add(manager.currentLocale.value.localeId)

        manager.resetToDefault()
        emittedValues.add(manager.currentLocale.value.localeId)

        assertEquals(listOf("en", "hi", "en"), emittedValues)
    }

    // ── Test 8: No application restart (structural verification) ─────────
    @Test
    fun testLocaleChangeDoesNotRequireNewManagerInstance() = runTest {
        // Verifies that locale can be changed N times on the same manager instance.
        // Application restart would require a new manager instance — which this test
        // deliberately avoids.
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        repeat(5) { i ->
            val target = if (i % 2 == 0) LanguageRegistry.HINDI else LanguageRegistry.ENGLISH
            manager.setLocale(target)
            assertEquals(target.localeId, manager.currentLocale.value.localeId)
        }
    }

    // ── Test 9: Selected locale persistence ───────────────────────────────
    @Test
    fun testLocaleIsPersisted() = runTest {
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        manager.setLocale(LanguageRegistry.HINDI)

        assertEquals("hi", repo.savedPreferences?.languageCode)
        assertEquals(1, repo.saveCount)
    }

    // ── Test 10: Startup locale restoration ──────────────────────────────
    @Test
    fun testStartupRestoresPersistedLocale() = runTest {
        // Simulate app restart by creating a new manager with an existing persisted preference
        val repoWithHindi = FakePreferencesRepository("hi")
        val newManager = AynvoraLocaleManagerImpl(repoWithHindi)
        newManager.initialize()

        assertEquals("hi", newManager.currentLocale.value.localeId,
            "Startup must restore the persisted locale")
    }

    // ── Test 11: Unsupported persisted locale fallback ────────────────────
    @Test
    fun testStartupFallsBackForUnsupportedPersistedLocale() = runTest {
        val repoWithUnknown = FakePreferencesRepository("fr") // French — not yet supported
        val manager = AynvoraLocaleManagerImpl(repoWithUnknown)
        manager.initialize()

        assertEquals("en", manager.currentLocale.value.localeId,
            "Unsupported persisted locale must fall back to English")
    }

    @Test
    fun testStartupFallsBackOnRepositoryFailure() = runTest {
        val manager = AynvoraLocaleManagerImpl(FailingPreferencesRepository())
        manager.initialize() // Must not throw

        assertEquals("en", manager.currentLocale.value.localeId,
            "IO failure during startup must fall back to English")
    }

    @Test
    fun testSetLocaleByStringId() = runTest {
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        manager.setLocale("hi")
        assertEquals("hi", manager.currentLocale.value.localeId)
    }

    @Test
    fun testSetLocaleUnknownIdFallsBackToDefault() = runTest {
        val repo = FakePreferencesRepository("en")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        manager.setLocale("xx_UNKNOWN")
        assertEquals("en", manager.currentLocale.value.localeId,
            "Unknown locale ID must fall back to English default")
    }

    @Test
    fun testResetToDefault() = runTest {
        val repo = FakePreferencesRepository("hi")
        val manager = AynvoraLocaleManagerImpl(repo)
        manager.initialize()

        assertEquals("hi", manager.currentLocale.value.localeId)
        manager.resetToDefault()
        assertEquals("en", manager.currentLocale.value.localeId)
    }

    @Test
    fun testPersistenceFailureIsNonFatal() = runTest {
        val manager = AynvoraLocaleManagerImpl(FailingPreferencesRepository())
        manager.initialize()

        // setLocale must succeed even if persistence fails
        manager.setLocale(LanguageRegistry.HINDI)
        assertEquals("hi", manager.currentLocale.value.localeId,
            "In-memory state must update even if persistence fails")
    }
}
