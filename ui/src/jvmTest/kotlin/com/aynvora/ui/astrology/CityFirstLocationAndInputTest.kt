package com.aynvora.ui.astrology

import com.aynvora.core.models.LocationLookupStatus
import com.aynvora.core.models.OfflineLocationCatalog
import com.aynvora.designsystem.adaptive.AynvoraDesktopLayoutSpec
import com.aynvora.designsystem.adaptive.AynvoraMobileLayoutSpec
import com.aynvora.designsystem.adaptive.calculateAynvoraWindowInfo
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CityFirstLocationAndInputTest {

    private val sampleData = """
        101	Bikaner	RJ	Rajasthan	IN	India	28.0229	73.3119	Asia/Kolkata
        102	Jaipur	RJ	Rajasthan	IN	India	26.9124	75.7873	Asia/Kolkata
        103	London	ENG	England	GB	United Kingdom	51.5074	-0.1278	Europe/London
        104	New York	NY	New York	US	United States	40.7128	-74.0060	America/New_York
        105	Springfield	IL	Illinois	US	United States	39.7817	-89.6501	America/Chicago
        106	Springfield	MA	Massachusetts	US	United States	42.1015	-72.5898	America/New_York
    """.trimIndent()

    private val catalog = OfflineLocationCatalog.parse(
        sampleData,
        datasetVersion = "test-1.0",
        provenance = "unit-test",
    )

    @Test
    fun cityFirstGlobalSearchReturnsExactAndPrefixMatchesWithoutCountrySelection() {
        val results = catalog.searchCitiesGlobal("Bikan")
        assertEquals(1, results.size)
        val bikaner = results.first()
        assertEquals("Bikaner", bikaner.cityName)
        assertEquals("Rajasthan", bikaner.stateName)
        assertEquals("India", bikaner.countryName)
        assertEquals("Asia/Kolkata", bikaner.timezoneId)
        assertEquals(28.0229, bikaner.latitude)
        assertEquals(73.3119, bikaner.longitude)
    }

    @Test
    fun cityFirstGlobalSearchDisambiguatesSameNameAcrossDifferentStates() {
        val results = catalog.searchCitiesGlobal("Springfield")
        assertEquals(2, results.size)
        val states = results.map { it.stateCode }.toSet()
        assertTrue(states.contains("IL"))
        assertTrue(states.contains("MA"))

        // With optional state filter
        val illinoisOnly = catalog.searchCitiesGlobal("Springfield", stateCode = "IL")
        assertEquals(1, illinoisOnly.size)
        assertEquals("Illinois", illinoisOnly.first().stateName)
    }

    @Test
    fun optionalCountryFilterRestrictsResults() {
        // Blank query with country filter returns popular cities in that country
        val indiaPopular = catalog.searchCitiesGlobal("", countryCode = "IN")
        assertTrue(indiaPopular.all { it.countryCode == "IN" })
        assertEquals(setOf("Jaipur"), indiaPopular.map { it.cityName }.toSet())

        // Query with country filter restricts search to that country
        val indiaMatches = catalog.searchCitiesGlobal("an", countryCode = "IN")
        assertTrue(indiaMatches.all { it.countryCode == "IN" })
        assertEquals(setOf("Bikaner", "Jaipur"), indiaMatches.map { it.cityName }.toSet())

        val ukOnly = catalog.searchCitiesGlobal("London", countryCode = "GB")
        assertEquals(1, ukOnly.size)
        assertEquals("United Kingdom", ukOnly.first().countryName)
    }

    @Test
    fun layoutSpecsMaintainTouchTargetsAndDensity() {
        // Mobile touch target >= 48dp
        assertTrue(AynvoraMobileLayoutSpec.minTouchTarget >= 48.dp)

        // Desktop specifications
        assertTrue(AynvoraDesktopLayoutSpec.sidebarWidth >= 220.dp)
        assertTrue(AynvoraDesktopLayoutSpec.contentPadding >= 20.dp)

        // Window size mapping
        val mobileInfo = calculateAynvoraWindowInfo(width = 390.dp, height = 844.dp)
        assertTrue(mobileInfo.isCompact)
        assertTrue(mobileInfo.isMobile)

        val desktopInfo = calculateAynvoraWindowInfo(width = 1440.dp, height = 900.dp)
        assertTrue(desktopInfo.isExpanded)
        assertTrue(desktopInfo.isDesktop)
    }
}
