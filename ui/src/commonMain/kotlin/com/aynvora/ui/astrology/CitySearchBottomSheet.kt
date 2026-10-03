package com.aynvora.ui.astrology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.models.CanonicalCountry
import com.aynvora.core.models.CanonicalLocation
import com.aynvora.core.models.CanonicalState
import com.aynvora.core.models.OfflineLocationCatalog
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.components.inputs.AynvoraTextField
import com.aynvora.designsystem.components.sheets.AynvoraBottomSheetHeader

/**
 * City-First Global Location Search Bottom Sheet.
 *
 * Allows users to search cities globally across the complete offline 12.1MB locations dataset
 * without requiring a mandatory Country -> State sequence.
 *
 * Features:
 * - Direct city search with 1. exact, 2. starts-with, 3. contains, 4. state/country matches
 * - Optional non-mandatory Country and State filter chips
 * - Local persisted Recent Locations
 * - Current selection highlight when editing
 * - Disambiguation displaying City, State/Region, Country, and Timezone
 * - Automatic metadata extraction on selection
 */
@Composable
fun CitySearchBottomSheet(
    catalog: OfflineLocationCatalog,
    onLocationSelected: (CanonicalLocation) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    recentLocations: List<CanonicalLocation> = emptyList(),
    selectedLocation: CanonicalLocation? = null,
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterCountry by remember { mutableStateOf<CanonicalCountry?>(null) }
    var filterState by remember { mutableStateOf<CanonicalState?>(null) }
    var showCountryFilterDialog by remember { mutableStateOf(false) }
    var showStateFilterDialog by remember { mutableStateOf(false) }

    val results = remember(searchQuery, catalog, filterCountry, filterState) {
        catalog.searchCitiesGlobal(
            query = searchQuery,
            countryCode = filterCountry?.countryCode,
            stateCode = filterState?.stateCode,
            maxResults = 100,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.sdp),
    ) {
        AynvoraBottomSheetHeader(
            title = "Select Birth City",
            subtitle = "Global offline gazetteer (City, State, Country)",
            onCloseClick = onClose,
        )

        Spacer(modifier = Modifier.height(AynvoraSpacing.space12))

        // Global City Search Bar
        AynvoraTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AynvoraSpacing.space16),
            placeholder = "Search city (e.g., Jaipur, London, New York, Bikaner...)",
            leadingIcon = {
                Text(
                    text = "🔍",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                )
            },
        )

        Spacer(modifier = Modifier.height(AynvoraSpacing.space8))

        // Optional Non-Mandatory Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AynvoraSpacing.space16, vertical = AynvoraSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Country Filter Chip (Optional)
            FilterChip(
                label = filterCountry?.countryName ?: "Country (Optional)",
                isActive = filterCountry != null,
                onClick = { showCountryFilterDialog = true },
            )

            // State Filter Chip (Optional)
            if (filterCountry != null) {
                FilterChip(
                    label = filterState?.stateName ?: "State (Optional)",
                    isActive = filterState != null,
                    onClick = { showStateFilterDialog = true },
                )
            }

            // Clear Filters Button
            if (filterCountry != null || filterState != null) {
                Text(
                    text = "Clear Filters ✕",
                    style = AynvoraTheme.typography.caption12.copy(
                        color = AynvoraColors.Error,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier
                        .clip(AynvoraShapes.shape8)
                        .clickable {
                            filterCountry = null
                            filterState = null
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(AynvoraSpacing.space8))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AynvoraSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(AynvoraSpacing.space8),
        ) {
            // If query and filters are blank, show Recent Locations then Suggested
            if (searchQuery.isBlank() && filterCountry == null && filterState == null) {
                if (recentLocations.isNotEmpty()) {
                    item(key = "recents_header") {
                        Text(
                            text = "Recent Locations",
                            style = AynvoraTheme.typography.caption12.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AynvoraColors.Gold,
                            ),
                            modifier = Modifier.padding(bottom = AynvoraSpacing.space4),
                        )
                    }
                    items(recentLocations, key = { "recent_${it.canonicalId}" }) { location ->
                        CityResultCard(
                            location = location,
                            isRecent = true,
                            isSelected = selectedLocation?.canonicalId == location.canonicalId,
                            onClick = {
                                onLocationSelected(location)
                                onClose()
                            },
                        )
                    }
                }

                item(key = "suggested_header") {
                    Spacer(modifier = Modifier.height(AynvoraSpacing.space8))
                    Text(
                        text = "Suggested Cities",
                        style = AynvoraTheme.typography.caption12.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AynvoraColors.TextLightSecondary,
                        ),
                        modifier = Modifier.padding(bottom = AynvoraSpacing.space4),
                    )
                }
            }

            if (results.isEmpty()) {
                item(key = "no_results") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No cities found matching \"$searchQuery\". Try checking the spelling or clearing filters."
                            } else "No locations available.",
                            style = AynvoraTheme.typography.body14,
                            color = AynvoraColors.TextLightSecondary,
                        )
                    }
                }
            } else {
                items(results, key = { it.canonicalId }) { location ->
                    CityResultCard(
                        location = location,
                        isRecent = false,
                        isSelected = selectedLocation?.canonicalId == location.canonicalId,
                        onClick = {
                            onLocationSelected(location)
                            onClose()
                        },
                    )
                }
            }
        }
    }

    // Country Filter Dialog (Optional)
    if (showCountryFilterDialog) {
        val countries = remember(catalog) { catalog.searchCountries() }
        var countryFilterQuery by remember { mutableStateOf("") }
        val filteredCountries = remember(countryFilterQuery, countries) {
            if (countryFilterQuery.isBlank()) countries.take(50)
            else countries.filter { it.countryName.contains(countryFilterQuery, ignoreCase = true) }.take(50)
        }

        AlertDialog(
            onDismissRequest = { showCountryFilterDialog = false },
            title = { Text("Filter by Country (Optional)", color = AynvoraColors.Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AynvoraTextField(
                        value = countryFilterQuery,
                        onValueChange = { countryFilterQuery = it },
                        placeholder = "Search country...",
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        items(filteredCountries, key = { it.countryCode }) { country ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filterCountry = country
                                        filterState = null
                                        showCountryFilterDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(country.countryName, color = AynvoraColors.TextLight)
                                Text("${country.cityCount} cities", color = AynvoraColors.TextLightSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    filterCountry = null
                    filterState = null
                    showCountryFilterDialog = false
                }) { Text("Clear", color = AynvoraColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showCountryFilterDialog = false }) { Text("Close", color = AynvoraColors.TextLightSecondary) }
            },
        )
    }

    // State Filter Dialog (Optional)
    if (showStateFilterDialog && filterCountry != null) {
        val states = remember(catalog, filterCountry) { catalog.searchStates(filterCountry!!.countryCode) }
        var stateFilterQuery by remember { mutableStateOf("") }
        val filteredStates = remember(stateFilterQuery, states) {
            if (stateFilterQuery.isBlank()) states.take(50)
            else states.filter { it.stateName.contains(stateFilterQuery, ignoreCase = true) }.take(50)
        }

        AlertDialog(
            onDismissRequest = { showStateFilterDialog = false },
            title = { Text("Filter by State/Region (Optional)", color = AynvoraColors.Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AynvoraTextField(
                        value = stateFilterQuery,
                        onValueChange = { stateFilterQuery = it },
                        placeholder = "Search state/region...",
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        items(filteredStates, key = { it.stateCode }) { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        filterState = st
                                        showStateFilterDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(st.stateName, color = AynvoraColors.TextLight)
                                Text("${st.cityCount} cities", color = AynvoraColors.TextLightSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    filterState = null
                    showStateFilterDialog = false
                }) { Text("Clear", color = AynvoraColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showStateFilterDialog = false }) { Text("Close", color = AynvoraColors.TextLightSecondary) }
            },
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(AynvoraShapes.shape8)
            .background(if (isActive) AynvoraColors.Gold.copy(alpha = 0.2f) else AynvoraColors.CosmicIndigo)
            .border(
                1.dp,
                if (isActive) AynvoraColors.Gold else AynvoraColors.CosmicIndigo,
                AynvoraShapes.shape8,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AynvoraTheme.typography.caption12.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) AynvoraColors.Gold else AynvoraColors.TextLightSecondary,
            ),
        )
    }
}

@Composable
private fun CityResultCard(
    location: CanonicalLocation,
    isRecent: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    AynvoraCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AynvoraColors.Gold else AynvoraColors.CosmicIndigo,
                shape = RoundedCornerShape(12.dp),
            ),
        variant = if (isRecent) AynvoraCardVariant.Outlined else AynvoraCardVariant.Filled,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AynvoraSpacing.space16, vertical = AynvoraSpacing.space12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = location.cityName,
                        style = AynvoraTheme.typography.body16.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.ssp,
                        ),
                        color = AynvoraColors.Gold,
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.padding(horizontal = AynvoraSpacing.space4))
                        Text(
                            text = "✓ Selected",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraColors.GoldLight,
                        )
                    } else if (isRecent) {
                        Spacer(modifier = Modifier.padding(horizontal = AynvoraSpacing.space4))
                        Text(
                            text = "🕒 Recent",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraColors.TextMuted,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${location.stateName} · ${location.countryName}",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                    color = AynvoraColors.TextLightSecondary,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Timezone: ${location.timezoneId} · ${"%.2f".format(location.latitude)}°, ${"%.2f".format(location.longitude)}°",
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = AynvoraColors.TextMuted,
                )
            }
            Text(
                text = "📍",
                style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
            )
        }
    }
}
