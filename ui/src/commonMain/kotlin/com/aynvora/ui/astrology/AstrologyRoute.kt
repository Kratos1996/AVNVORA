package com.aynvora.ui.astrology

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.components.AynvoraLogo
import com.aynvora.designsystem.components.AynvoraLogoVariant
import com.aynvora.core.AynvoraSdk
import com.aynvora.core.analytics.AnalyticsEvent
import com.aynvora.core.analytics.AnalyticsTracker
import com.aynvora.core.models.BirthData
import com.aynvora.core.models.BirthDate
import com.aynvora.core.models.BirthPlace
import com.aynvora.core.models.BirthProfile
import com.aynvora.core.models.BirthTime
import com.aynvora.core.models.CalculationConfig
import com.aynvora.core.models.ChartRequest
import com.aynvora.core.models.ChartResult
import com.aynvora.core.models.KundaliSnapshotJson
import com.aynvora.core.models.KundaliIdentity
import com.aynvora.core.models.SavedChart
import com.aynvora.core.repository.BirthProfileRepository
import com.aynvora.core.repository.SavedChartRepository
import com.aynvora.core.result.AynvoraResult
import com.aynvora.core.models.generateKundaliSnapshot
import com.aynvora.astro.time.JulianDayFormatter
import com.aynvora.astro.dasha.DashaPeriod
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.designsystem.generated.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.currentKoinScope

private data class LocalCity(
    val id: String, val city: String, val stateCode: String, val state: String,
    val countryCode: String, val country: String, val lat: Double, val lon: Double, val timezone: String,
)

/** Dedicated, offline-first birth profile entry point. All chart computation remains in AynvoraSdk. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun AstrologyRoute(onClose: () -> Unit, modifier: Modifier = Modifier) {
    val koin = currentKoinScope()
    val sdk = remember(koin) { koin.getOrNull<AynvoraSdk>() }
    val profiles = remember(koin) { koin.getOrNull<BirthProfileRepository>() }
    val charts = remember(koin) { koin.getOrNull<SavedChartRepository>() }
    val analytics = remember(koin) { koin.getOrNull<AnalyticsTracker>() }
    val translator = LocalAynvoraTranslator.current
    val scope = rememberCoroutineScope()
    var cities by remember { mutableStateOf<List<LocalCity>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var hour by remember { mutableStateOf<Int?>(null) }
    var minute by remember { mutableStateOf<Int?>(null) }
    var selected by remember { mutableStateOf<LocalCity?>(null) }
    var gender by remember { mutableStateOf("") }
    var picker by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<VedicAstrologyUiModel?>(null) }
    var savedProfiles by remember { mutableStateOf<List<BirthProfile>>(emptyList()) }
    var deleteCandidate by remember { mutableStateOf<BirthProfile?>(null) }
    var profilesLoaded by remember { mutableStateOf(false) }
    // Saved Kundalis are the entry state; no saved records leads to the create-first screen.
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        analytics?.track(AnalyticsEvent.AstrologyInputOpened)
        val text = Res.readBytes("files/locations.tsv").decodeToString()
        cities = text.lineSequence().mapNotNull { line ->
            val f = line.split('\t')
            if (f.size != 9) return@mapNotNull null
            val lat = f[6].toDoubleOrNull() ?: return@mapNotNull null
            val lon = f[7].toDoubleOrNull() ?: return@mapNotNull null
            LocalCity(f[0], f[1], f[2], f[3], f[4], f[5], lat, lon, f[8])
        }.toList()
        savedProfiles = ((profiles?.getAllBirthProfiles() as? AynvoraResult.Success<*>)?.value as? List<BirthProfile>).orEmpty()
        if (savedProfiles.isEmpty()) showForm = true
        profilesLoaded = true
    }

    val title = when (picker) { "country" -> "Select country"; "state" -> "Select state/region"; "city" -> "Select city"; else -> "" }
    val logoMotion = rememberInfiniteTransition(label = "astrology-logo")
    val logoScale by logoMotion.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1_800), repeatMode = RepeatMode.Reverse),
        label = "logo-pulse",
    )

    PlatformBackHandler {
        when {
            picker.isNotEmpty() -> picker = ""
            deleteCandidate != null -> deleteCandidate = null
            result != null -> { result = null; showForm = false }
            showForm -> showForm = false
            else -> onClose()
        }
    }

    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(AynvoraTheme.colors.CosmicBlack, AynvoraTheme.colors.CosmicNavy, AynvoraTheme.colors.CosmicBlack),
            ),
        ),
    ) {
      Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(46.dp)
                    .clip(CircleShape)
                    .background(AynvoraTheme.colors.CosmicIndigo)
                    .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.55f), CircleShape)
                    .clickable(onClick = { if (result != null) { result = null; showForm = false } else if (showForm) showForm = false else onClose() })
                    .semantics { contentDescription = "Back to home" },
                contentAlignment = Alignment.Center,
            ) {
                Text("‹", color = AynvoraTheme.colors.GoldLight, fontSize = 32.sp, fontWeight = FontWeight.Light)
            }
            Box(
                Modifier.padding(start = 14.dp).size(62.dp)
                    .clip(CircleShape)
                    .background(AynvoraTheme.colors.CosmicIndigo)
                    .border(1.dp, AynvoraTheme.colors.Gold.copy(alpha = 0.25f + (logoScale - 0.96f) * 4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AynvoraLogo(
                    modifier = Modifier.graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                    },
                    size = 46.dp,
                    variant = AynvoraLogoVariant.Transparent,
                    contentDescription = "Animated Aynvora astrology logo",
                )
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("AYNVORA", color = AynvoraTheme.colors.GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(if (result != null) "YOUR KUNDALI" else "VEDIC ASTROLOGY", color = AynvoraTheme.colors.TextLightSecondary, fontSize = 11.sp)
            }
        }
        if (result == null) Column(Modifier.padding(top = 10.dp, bottom = 2.dp)) {
            Text("YOUR COSMIC BLUEPRINT", color = AynvoraTheme.colors.GoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("A map of your\nunique sky.", color = AynvoraTheme.colors.TextLight, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
            Text("Enter your birth details to begin your Vedic chart.", color = AynvoraTheme.colors.TextLightSecondary, fontSize = 14.sp)
        }
        if (result == null && !showForm) {
            Text(if (savedProfiles.isEmpty()) "Create your first Kundali" else "Previous Kundalis", color = AynvoraTheme.colors.TextLight, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (!profilesLoaded) Text("Loading saved Kundalis…", color = AynvoraTheme.colors.TextLightSecondary)
            if (savedProfiles.isEmpty()) Text("Your saved birth charts will appear here.", color = AynvoraTheme.colors.TextLightSecondary)
            Button(onClick = { error = null; showForm = true }, Modifier.fillMaxWidth()) { Text("New Kundali") }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedProfiles, key = { it.id }) { profile ->
                    Surface(color = AynvoraTheme.colors.CosmicIndigo, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(profile.name, color = AynvoraTheme.colors.TextLight, fontWeight = FontWeight.SemiBold)
                                Text("${profile.date.toIsoDateString()} · ${profile.time.toIsoTimeString().take(5)} · ${profile.place.name}", color = AynvoraTheme.colors.TextLightSecondary, fontSize = 12.sp)
                            }
                            Button(enabled = !loading, onClick = {
                                loading = true
                                scope.launch {
                                    val saved = (charts?.getChartsForBirthProfile(profile.id) as? AynvoraResult.Success<*>)?.value as? List<SavedChart>
                                    val chart = saved?.firstOrNull { it.cachedResultJson != null }
                                    if (chart?.cachedResultJson == null) {
                                        error = "This saved Kundali has no stored snapshot. It was not recalculated. Create a new profile to calculate again."
                                    } else {
                                        runCatching { VedicAstrologyJsonUiAdapter.map(chart.cachedResultJson!!) }
                                            .onSuccess { model ->
                                                if (model.snapshot.profileId != profile.id) error = "Saved Kundali profile does not match this record."
                                                else {
                                                    result = model
                                                    name = profile.name
                                                    val now = System.currentTimeMillis()
                                                    profiles?.saveBirthProfile(profile.copy(lastOpenedAtEpochMs = now))
                                                    charts?.saveChart(chart.copy(lastOpenedAtEpochMs = now))
                                                    savedProfiles = ((profiles?.getAllBirthProfiles() as? AynvoraResult.Success<*>)?.value as? List<BirthProfile>).orEmpty()
                                                }
                                            }
                                            .onFailure { error = "Saved Kundali snapshot is incompatible or corrupted. It was not recalculated." }
                                    }
                                    loading = false
                                }
                            }) { Text("Open") }
                            IconButton(onClick = { deleteCandidate = profile }, enabled = !loading) {
                                Text("×", color = AynvoraTheme.colors.Error, fontSize = 22.sp, modifier = Modifier.semantics { contentDescription = "Delete ${profile.name}" })
                            }
                        }
                    }
                }
            }
            error?.let { Text(it, color = AynvoraTheme.colors.Error) }
        } else if (result == null) {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("BIRTH DETAILS  ·  STEP 1 OF 1", color = AynvoraTheme.colors.GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                item { OutlinedTextField(name, { name = it.take(80) }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true) }
                item { Button(onClick = { analytics?.track(AnalyticsEvent.BirthDatePickerOpened); picker = "date" }, Modifier.fillMaxWidth()) { Text(dateMillis?.let(::formatDate) ?: "Date of birth") } }
                item { Button(onClick = { analytics?.track(AnalyticsEvent.BirthTimePickerOpened); picker = "time" }, Modifier.fillMaxWidth()) { Text(if (hour != null && minute != null) "%02d:%02d".format(hour, minute) else "Exact time of birth") } }
                item { Button(onClick = { analytics?.track(AnalyticsEvent.BirthLocationPickerOpened); picker = "country"; query = "" }, Modifier.fillMaxWidth()) { Text(selected?.let { "${it.city}, ${it.state}, ${it.country}" } ?: "Birth location") } }
                item { Text("Gender (optional)", color = AynvoraTheme.colors.TextLight) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Male", "Female", "Prefer not to say").forEach { Button(onClick = { gender = it }) { Text(if (gender == it) "✓ $it" else it) } } } }
                item { Text("Calculation profile: Standard Vedic · Lahiri · Equal House", color = AynvoraTheme.colors.TextLightSecondary) }
                item { error?.let { Text(it, color = AynvoraTheme.colors.Error) } }
                item { Button(enabled = !loading, onClick = {
                    val place = selected
                    val millis = dateMillis
                    val h = hour; val m = minute
                    val validation = when {
                        name.trim().length !in 1..80 -> "Enter a name (1–80 characters)."
                        millis == null -> "Select a birth date."
                        h == null || m == null -> "Select an exact birth time."
                        place == null -> "Select a birth city."
                        place.timezone.isBlank() -> "The selected city has no timezone. Choose a different city."
                        else -> null
                    }
                    if (validation != null) { error = validation; return@Button }
                    if (sdk == null) { error = "Astrology service is unavailable."; return@Button }
                    loading = true; error = null
                    analytics?.track(AnalyticsEvent.KundaliGenerationStarted)
                    scope.launch {
                        try {
                            val date = fromEpochDay((millis!! / 86_400_000L).toInt())
                            val bday = BirthData(BirthDate(date.first, date.second, date.third), BirthTime(h!!, m!!), BirthPlace(
                                name = place!!.city, coordinates = com.aynvora.core.models.Coordinates(place.lat, place.lon),
                                timezoneId = place.timezone, country = place.country, id = place.id,
                                countryCode = place.countryCode, stateName = place.state, stateCode = place.stateCode, cityName = place.city,
                            ))
                            val config = fullHoroscopeConfig()
                            val profileId = KundaliIdentity.profileId(bday, config)
                            val duplicate = savedProfiles.firstOrNull { it.id == profileId }
                                ?: savedProfiles.firstOrNull { it.birthData == bday }
                            if (duplicate != null) {
                                error = "A Kundali with these birth details already exists. Open it from Previous Kundalis."
                                showForm = false
                                loading = false
                                return@launch
                            }
                            when (val calculated = sdk.generateKundaliSnapshot(
                                ChartRequest(bday, config), profileId, name.trim(), gender.ifBlank { null },
                            )) {
                                is AynvoraResult.Success -> {
                                    val json = KundaliSnapshotJson.encode(calculated.value)
                                    val now = System.currentTimeMillis()
                                    val profile = BirthProfile(profileId, name.trim(), bday, createdAtEpochMs = now, updatedAtEpochMs = now, lastOpenedAtEpochMs = now)
                                    val chart = SavedChart(
                                        id = "chart_$profileId", birthProfileId = profileId, calculationConfig = calculated.value.natalChart.config,
                                        engineVersion = calculated.value.natalChart.engineVersion, calculationTimestampEpochMs = now,
                                        cachedResultJson = json, snapshotSchemaVersion = calculated.value.schemaVersion,
                                        calculationContractVersion = calculated.value.calculation.contractVersion,
                                        createdAtEpochMs = now, updatedAtEpochMs = now, lastOpenedAtEpochMs = now,
                                        identityFingerprint = KundaliIdentity.fingerprint(bday, config),
                                    )
                                    val profileSave = profiles?.saveBirthProfile(profile)
                                    val chartSave = charts?.saveChart(chart)
                                    if (profiles == null || charts == null || profileSave !is AynvoraResult.Success<*> || chartSave !is AynvoraResult.Success<*>) {
                                        error = "Kundali was calculated but could not be saved locally. Check storage and try again."
                                    } else {
                                        savedProfiles = ((profiles.getAllBirthProfiles() as? AynvoraResult.Success<*>)?.value as? List<BirthProfile>).orEmpty()
                                        result = VedicAstrologyJsonUiAdapter.map(json)
                                        analytics?.track(AnalyticsEvent.KundaliGenerationSuccess)
                                    }
                                }
                                is AynvoraResult.Failure -> { error = "Unable to generate Kundali: ${calculated.message}"; analytics?.track(AnalyticsEvent.KundaliGenerationFailure) }
                            }
                        } catch (_: Exception) { error = "Unable to generate Kundali. Check the details and try again."; analytics?.track(AnalyticsEvent.KundaliGenerationFailure) }
                        finally { loading = false }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Calculating…" else "GET HOROSCOPE") } }
            }
        } else {
            KundaliWorkspace(model = result!!, translator = translator, modifier = Modifier.weight(1f))
        }
      }
    }

    when (picker) {
        "date" -> {
            val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
            DatePickerDialog(onDismissRequest = { picker = "" }, confirmButton = { TextButton(onClick = { dateMillis = state.selectedDateMillis; picker = "" }) { Text("OK") } }, dismissButton = { TextButton(onClick = { picker = "" }) { Text("Cancel") } }) { DatePicker(state) }
        }
        "time" -> {
            val state = rememberTimePickerState(initialHour = hour ?: 12, initialMinute = minute ?: 0, is24Hour = true)
            AlertDialog(onDismissRequest = { picker = "" }, confirmButton = { TextButton(onClick = { hour = state.hour; minute = state.minute; picker = "" }) { Text("OK") } }, dismissButton = { TextButton(onClick = { picker = "" }) { Text("Cancel") } }, text = { TimePicker(state) })
        }
        "country", "state", "city" -> AlertDialog(onDismissRequest = { picker = "" }, title = { Text(title) }, text = {
            Column {
                OutlinedTextField(query, { query = it }, label = { Text("Search") }, singleLine = true)
                LazyColumn(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    val options = when (picker) {
                        "country" -> cities.map { it.countryCode to it.country }.distinctBy { it.first }.filter { it.second.contains(query, true) }.sortedBy { it.second }.take(500)
                        "state" -> cities.filter { it.countryCode == selected?.countryCode }.map { it.stateCode to it.state }.distinctBy { it.first }.filter { it.second.contains(query, true) }.sortedBy { it.second }.take(500)
                        else -> cities.filter { it.countryCode == selected?.countryCode && it.stateCode == selected?.stateCode && it.city.contains(query, true) }.sortedBy { it.city }.take(100).map { it.id to "${it.city} · ${it.state} · ${it.country}" }
                    }
                    items(options) { option -> Text(option.second, Modifier.fillMaxWidth().clickable {
                        when (picker) {
                            "country" -> { selected = LocalCity("", "", "", "", option.first, option.second, 0.0, 0.0, ""); picker = "state" }
                            "state" -> { selected = selected!!.copy(stateCode = option.first, state = option.second); picker = "city" }
                            else -> {
                                val match = cities.firstOrNull { it.countryCode == selected?.countryCode && it.stateCode == selected?.stateCode && it.id == option.first }
                                if (match != null) { selected = match; analytics?.track(AnalyticsEvent.BirthLocationSelected) }
                                picker = ""
                            }
                        }
                        query = ""
                    }.padding(10.dp)) }
                }
            }
        }, confirmButton = { TextButton(onClick = { picker = "" }) { Text("Close") } })
    }

    deleteCandidate?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete saved Kundali?") },
            text = { Text("${profile.name} and its saved chart snapshot will be deleted from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteCandidate = null
                    loading = true
                    scope.launch {
                        val chartsForProfile = (charts?.getChartsForBirthProfile(profile.id) as? AynvoraResult.Success<*>)?.value as? List<SavedChart>
                        chartsForProfile.orEmpty().forEach { charts?.deleteChart(it.id) }
                        val deleted = profiles?.deleteBirthProfile(profile.id)
                        if (deleted is AynvoraResult.Success<*>) {
                            savedProfiles = ((profiles.getAllBirthProfiles() as? AynvoraResult.Success<*>)?.value as? List<BirthProfile>).orEmpty()
                            error = null
                        } else error = "Unable to delete this saved Kundali."
                        loading = false
                    }
                }) { Text("Delete", color = AynvoraTheme.colors.Error) }
            },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } },
        )
    }
}

private fun formatDate(millis: Long): String {
    val (y, m, d) = fromEpochDay((millis / 86_400_000L).toInt())
    return "%04d-%02d-%02d".format(y, m, d)
}

private fun fullHoroscopeConfig() = CalculationConfig(
    requestedDivisionalCharts = com.aynvora.core.models.DivisionalChart.entries.toSet(),
)

@Composable
private fun KundaliWorkspace(model: VedicAstrologyUiModel, translator: com.aynvora.localization.translation.AynvoraTranslator, modifier: Modifier = Modifier) {
    val snapshot = model.snapshot
    val chart = snapshot.natalChart
    var sectionId by remember(snapshot.profileId) { mutableStateOf("home") }
    val pages = model.sections
    val selectedPage = pages.firstOrNull { it.sectionId == sectionId } ?: pages.first()
    val title: (String) -> String = { key -> translator.get(com.aynvora.core.localization.RawLocalizationKey(key)) }
    val selectedChartId = when (sectionId) {
        "navamsha" -> "D9"
        "chandra" -> "MOON"
        else -> "D1"
    }
    val selectedChart = snapshot.charts.firstOrNull { it.chartId == selectedChartId }
    val ink = AynvoraTheme.colors.TextLight
    val muted = AynvoraTheme.colors.TextLightSecondary
    val gold = AynvoraTheme.colors.GoldLight

    LazyColumn(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(Modifier.padding(top = 5.dp)) {
                Text("YOUR BIRTH CHART", color = gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text(snapshot.profileName, color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("${snapshot.birth.city}  ·  ${snapshot.birth.localDate}", color = muted, fontSize = 13.sp)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.forEach { page ->
                    val active = sectionId == page.sectionId
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { sectionId = page.sectionId },
                        color = if (active) AynvoraTheme.colors.Gold else AynvoraTheme.colors.CosmicIndigo,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(title(page.titleKey), color = if (active) AynvoraTheme.colors.CosmicBlack else ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            if (page.availability == com.aynvora.core.models.CalculationAvailability.UNSUPPORTED || page.availability == com.aynvora.core.models.CalculationAvailability.COMING_SOON)
                                Text("·", color = if (active) AynvoraTheme.colors.CosmicBlack else muted, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        item { Text(title(selectedPage.titleKey), color = ink, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
        when (sectionId) {
            "home" -> {
                item { Text("Choose a Kundali section", color = muted, fontSize = 13.sp) }
                items(pages.filter { it.sectionId != "home" }.chunked(2)) { rowPages ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowPages.forEach { page ->
                            val available = page.availability == com.aynvora.core.models.CalculationAvailability.AVAILABLE || page.availability == com.aynvora.core.models.CalculationAvailability.PARTIAL
                            Surface(
                                modifier = Modifier.weight(1f).clickable { sectionId = page.sectionId },
                                color = if (available) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(title(page.titleKey), color = ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(page.availability.name, color = if (available) gold else muted, fontSize = 10.sp)
                                }
                            }
                        }
                        if (rowPages.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            "lagna", "navamsha", "chandra", "chalit" -> {
                val supported = selectedChart != null && selectedChart.status != com.aynvora.core.models.CalculationAvailability.UNSUPPORTED
                if (sectionId == "chalit" && (selectedPage.availability == com.aynvora.core.models.CalculationAvailability.AMBIGUOUS || snapshot.featureResults["chalit"]?.status == com.aynvora.core.models.AstroFeatureStatus.AMBIGUOUS)) {
                    item { EmptyReport("Chalit is marked AMBIGUOUS because its cusp convention is not verified. The saved snapshot does not substitute an Equal House chart.") }
                } else if (!supported) item { EmptyReport("This chart is not included in the saved calculation snapshot.") }
                else {
                    item {
                        Surface(color = AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.92f), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(selectedChart!!.chartTypeId.replace('_', ' '), color = gold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Text("North Indian · ${selectedChart.zodiacModeId}", color = muted, fontSize = 11.sp)
                                    }
                                    Text(snapshot.birth.localDate, color = muted, fontSize = 10.sp)
                                }
                                NorthIndianKundali(selectedChart!!)
                                SnapshotChartLegend(selectedChart!!)
                            }
                        }
                    }
                    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryCard("ASCENDANT", chart.lagna?.rashiPosition?.rashi?.displayName ?: "Unavailable", chart.lagna?.nakshatraPosition?.nakshatra?.displayName ?: "Lagna", Modifier.weight(1f))
                        SummaryCard("PLANETS", selectedChart!!.placements.size.toString(), "calculated placements", Modifier.weight(1f))
                    } }
                    item { Text("Planetary positions", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    items(selectedChart!!.placements, key = { "${selectedChart.chartId}_${it.bodyId}" }) { p ->
                        DetailRow(bodyShort(p.bodyId), displayCanonical(p.bodyId), displayCanonical(p.nakshatraId), "${"%.2f".format(p.degreeInSign)}° · H${p.houseNumber ?: "—"}${markerSuffix(p)}")
                    }
                }
            }
            "graha_sthiti", "graha_sthiti_all" -> {
                val table = snapshot.tables.firstOrNull { it.sectionId == "graha_sthiti" }
                table?.let { item { AstrologyDataTable(it) } } ?: item { EmptyReport("Planet position rows are unavailable in this snapshot.") }
                if (sectionId == "graha_sthiti_all") item { Text("State, retrograde and combustion markers are carried in each chart placement and natal planet state records.", color = muted, fontSize = 12.sp) }
            }
            "chalit_table" -> {
                val table = snapshot.tables.firstOrNull { it.sectionId == "chalit_table" }
                if (selectedPage.availability == com.aynvora.core.models.CalculationAvailability.AMBIGUOUS || snapshot.featureResults["chalit"]?.status == com.aynvora.core.models.AstroFeatureStatus.AMBIGUOUS) {
                    item { EmptyReport("Chalit house cusps are AMBIGUOUS for this ruleset. No substitute cusps are shown.") }
                } else table?.let { item { AstrologyDataTable(it) } } ?: item { EmptyReport("House cusp rows are unavailable in this snapshot.") }
            }
            "dasha" -> {
                val timeline = snapshot.dasha
                if (timeline == null) item { EmptyReport("Vimshottari Dasha is unsupported for this chart.") }
                else {
                    item { Text("${timeline.rulesetId} · starting lord ${timeline.startingLord.displayName} · balance ${"%.2f".format(timeline.balanceYearsAtBirth)} years", color = muted, fontSize = 12.sp) }
                    item { Text("Tap a period to expand its sub-periods. Dates are UTC.", color = muted, fontSize = 11.sp) }
                    items(timeline.mahadashas, key = { "dasha_${it.planet.name}_${it.startJulianDay}" }) { period -> DashaTreePeriod(period, 0) }
                }
            }
            "panchang" -> {
                val p = snapshot.panchang
                if (p == null) item { EmptyReport("Panchang data is unavailable in this snapshot.") }
                else {
                    item { DetailRow("ति", "Tithi", p.tithi.name, "${"%.1f".format(p.tithiElapsedDegrees)}° elapsed") }
                    item { DetailRow("न", "Nakshatra", p.nakshatraName, "${"%.2f".format(p.nakshatraDegree)}°") }
                    item { DetailRow("यो", "Yoga", p.yoga.name, "${p.profileId}") }
                    item { DetailRow("क", "Karana", p.karana.name, "") }
                    item { DetailRow("वा", "Vara", p.vara.name, p.varaConvention.name) }
                    item { DetailRow("सूर्य", "Sunrise / sunset", "Not returned by current Panchang engine", "—") }
                    item { Text("Paksha: ${p.tithi.paksha} · Vara convention: ${p.varaConvention.name}", color = muted, fontSize = 12.sp) }
                }
            }
            "ashtakavarga", "prastara_ashtakavarga", "shodhita_ashtakavarga" -> {
                val av = chart.ashtakavarga
                val reduced = chart.shodhitaAshtakavarga
                if (sectionId == "shodhita_ashtakavarga" && reduced != null) {
                    item { Text("${reduced.rulesetId} · Trikona and Ekadhipatya Shodhana", color = muted, fontSize = 12.sp) }
                    items(reduced.shodhitaSarvashtakavarga.signScores, key = { "shodhita_${it.rashi.name}" }) { score ->
                        DetailRow((score.rashi.index + 1).toString(), displayCanonical(score.rashi.name), "Raw ${score.rawTotalBindus} · Trikona ${score.trikonaTotalBindus}", "${score.shodhitaTotalBindus}")
                    }
                } else if (av != null) {
                    item { Text("${av.rulesetId} · ${av.sarvashtakavarga.grandTotalBindus} total bindus", color = muted, fontSize = 12.sp) }
                    items(av.sarvashtakavarga.signScores, key = { "sav_${it.rashi.name}" }) { score ->
                        DetailRow((score.rashi.index + 1).toString(), displayCanonical(score.rashi.name), "Sarvashtakavarga · ${score.totalRekhas} rekhas", "${score.totalBindus} bindus")
                    }
                    item { Text("Bhinnashtakavarga contributor charts: ${av.bhinnashtakavarga.size}", color = muted, fontSize = 12.sp) }
                } else item { EmptyReport("Ashtakavarga data is explicitly unsupported for this calculation.") }
            }
            "shadbala", "pindabala" -> {
                if (sectionId == "shadbala") {
                    items(chart.shadbala, key = { "shadbala_${it.body.name}" }) { strength ->
                        DetailRow(bodyShort(strength.body.name), displayCanonical(strength.body.name), strength.completeness.name, strength.totalRupas?.let { "${"%.2f".format(it)} rūpas" } ?: "Unavailable")
                    }
                    if (chart.shadbala.isEmpty()) item { EmptyReport("Shadbala is unavailable in this snapshot.") }
                } else {
                    val pinda = chart.ashtakavargaPinda
                    if (pinda == null) item { EmptyReport("Pinda data is unsupported for this chart.") }
                    else {
                        item { DetailRow("R", "Rāśi Pinda", pinda.completeness.name, "${pinda.totalRasiPinda}") }
                        item { DetailRow("G", "Graha Pinda", pinda.completeness.name, "${pinda.totalGrahaPinda}") }
                        item { DetailRow("S", "Shodhya Pinda", pinda.completeness.name, "${pinda.totalShodhyaPinda}") }
                        items(pinda.planetaryPindas.entries.sortedBy { it.key.ordinal }, key = { "pinda_${it.key.name}" }) { row ->
                            DetailRow(bodyShort(row.key.name), displayCanonical(row.key.name), "Rāśi · Graha · Shodhya", "${row.value.rasiPinda} · ${row.value.grahaPinda} · ${row.value.shodhyaPinda}")
                        }
                    }
                }
            }
            "shodashavarga" -> {
                items(snapshot.charts.filter { it.chartId.startsWith("D") }.sortedBy { it.chartId.drop(1).toIntOrNull() ?: 0 }, key = { it.chartId }) { varga ->
                    Surface(color = AynvoraTheme.colors.CosmicIndigo, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${varga.chartId} · ${varga.chartTypeId}", color = gold, fontWeight = FontWeight.Bold)
                            Text("${varga.status} · ${varga.placements.size} positions", color = muted, fontSize = 11.sp)
                            if (varga.status == com.aynvora.core.models.CalculationAvailability.AVAILABLE) {
                                NorthIndianKundali(varga)
                                SnapshotChartLegend(varga)
                            }
                            Text(varga.placements.joinToString("   ") { "${bodyShort(it.bodyId)} ${displayCanonical(com.aynvora.core.models.Rashi.fromIndex(it.signIndex).name)}" }, color = ink, fontSize = 12.sp)
                        }
                    }
                }
            }
            "relationships" -> {
                item { Text("Planetary aspects", color = gold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                items(chart.aspects, key = { "aspect_${it.firstBody.name}_${it.secondBody.name}_${it.type.name}" }) { a -> DetailRow(a.type.name.take(2), "${displayCanonical(a.firstBody.name)} · ${displayCanonical(a.secondBody.name)}", "${a.type.name} · orb ${"%.2f".format(a.orb)}°", "${"%.2f".format(a.actualSeparation)}°") }
                item { Text("Natural relationships", color = gold, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                items(chart.planetaryRelationships.take(60), key = { "relation_${it.sourceBody.name}_${it.targetBody.name}_${it.chart.name}" }) { r -> DetailRow(bodyShort(r.sourceBody.name), "${displayCanonical(r.sourceBody.name)} → ${displayCanonical(r.targetBody.name)}", "${r.naturalRelationship.name} · ${r.temporaryRelationship.name}", r.compoundRelationship.name) }
            }
            "janam_vivaran", "personal_details" -> {
                item { DetailRow("नाम", "Name", "Birth profile", snapshot.birth.profileName) }
                item { DetailRow("दिन", "Date", "Local birth date", snapshot.birth.localDate) }
                item { DetailRow("समय", "Time", "Local birth time", snapshot.birth.localTime) }
                item { DetailRow("UTC", "Normalized time", snapshot.birth.timezoneId, snapshot.birth.utcTimestamp) }
                item { DetailRow("स्थान", "Place", snapshot.birth.country ?: "", snapshot.birth.city) }
                item { DetailRow("क्षेत्र", "Region", "Latitude · longitude", "${snapshot.birth.state ?: "—"} · ${"%.4f".format(snapshot.birth.latitude)}, ${"%.4f".format(snapshot.birth.longitude)}") }
                item { DetailRow("अयन", "Ayanāṁśa", snapshot.birth.ayanamsaId, "${"%.4f".format(chart.ayanamsaDegrees)}°") }
                item { DetailRow("गृह", "House system", snapshot.birth.houseSystemId, snapshot.birth.calculationProfileId) }
                item { DetailRow("नोड", "Node convention", "Calculation metadata", snapshot.birth.nodeConventionId) }
            }
            "gochar" -> {
                val transit = snapshot.transitAtBirth
                if (transit == null) item { EmptyReport("Transit calculation is unsupported for this snapshot.") }
                else {
                    item { Text("Transit snapshot at recorded birth time · JD ${transit.julianDay}", color = muted, fontSize = 12.sp) }
                    items(transit.planetaryPositions, key = { "transit_${it.bodyId.name}" }) { p -> DetailRow(bodyShort(p.bodyId.name), displayCanonical(p.bodyId.name), "${p.rashiName} · ${p.nakshatraName}", "${"%.3f".format(p.degreeInRashi)}°${if (p.isRetrograde) " ℞" else ""}") }
                    item { Text("This is a historical transit snapshot at birth time, not a current-date forecast.", color = muted, fontSize = 11.sp) }
                }
            }
            "avakahada" -> {
                item { DetailRow("लग्न", "Lagna", "Ascendant sign", chart.lagna?.rashiPosition?.rashi?.name?.let(::displayCanonical) ?: "Unavailable") }
                item { DetailRow("नक्ष", "Janma nakshatra", "Moon mansion", chart.planetaryPositions.firstOrNull { it.body == com.aynvora.core.models.CelestialBody.MOON }?.nakshatraPosition?.nakshatra?.name?.let(::displayCanonical) ?: "Unavailable") }
                item { DetailRow("पद", "Pada", "Moon nakshatra quarter", chart.planetaryPositions.firstOrNull { it.body == com.aynvora.core.models.CelestialBody.MOON }?.nakshatraPosition?.pada?.toString() ?: "—") }
                item { DetailRow("चंद्र", "Moon sign", "Janma rāśi", chart.planetaryPositions.firstOrNull { it.body == com.aynvora.core.models.CelestialBody.MOON }?.rashiPosition?.rashi?.name?.let(::displayCanonical) ?: "Unavailable") }
            }
            "phaladesh", "kp", "lal_kitab", "varshaphal", "karakansha", "swansha", "yogas", "cloud", "pdf_report", "ask_question", "reports" -> {
                val status = selectedPage.availability
                val message = if (status == com.aynvora.core.models.CalculationAvailability.UNSUPPORTED || status == com.aynvora.core.models.CalculationAvailability.COMING_SOON)
                    "This section is marked ${status.name} by the calculation snapshot. No placeholder astrology values are generated."
                else "This report view is partial. Open the available chart and table pages for the source values."
                item { EmptyReport(message) }
                if (sectionId == "reports" || sectionId == "pdf_report") {
                    item { DetailRow("D1", "Rāśi chart", "Primary birth chart", if (chart.lagna != null) "Ready" else "Unavailable") }
                    item { DetailRow("D9", "Navamsha", "Divisional chart", if (snapshot.charts.any { it.chartId == "D9" && it.status == com.aynvora.core.models.CalculationAvailability.AVAILABLE }) "Ready" else "Unsupported") }
                    item { DetailRow("GR", "Graha positions", "${chart.planetaryPositions.size} bodies", "Ready") }
                    item { DetailRow("BH", "Bhava cusps", "${chart.houses.size} houses", "Ready") }
                }
            }
            else -> item { EmptyReport("This section has no data reference in the current snapshot.") }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun NorthIndianKundali(chart: com.aynvora.core.models.AstroChartSnapshot) {
    val grouped = androidx.compose.runtime.remember(chart) { com.aynvora.core.models.AstroChartBuilder.fromSnapshot(chart) }
    AynvoraChart(grouped)
}

@Composable
private fun DashaTreePeriod(period: DashaPeriod, depth: Int) {
    var expanded by remember(period.planet, period.level, period.startJulianDay) { mutableStateOf(false) }
    val hasChildren = period.subPeriods.isNotEmpty()
    Surface(
        modifier = Modifier.fillMaxWidth().padding(start = (depth * 12).dp)
            .clickable(enabled = hasChildren) { expanded = !expanded },
        color = AynvoraTheme.colors.CosmicIndigo.copy(alpha = if (depth == 0) 0.92f else 0.68f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (hasChildren) if (expanded) "▾" else "▸" else "·", color = AynvoraTheme.colors.GoldLight, fontSize = 13.sp)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text("${period.planet.displayName} · ${when (period.level) { 1 -> "Mahadasha"; 2 -> "Antardasha"; else -> "Pratyantardasha" }}", color = AynvoraTheme.colors.TextLight, fontSize = if (depth == 0) 14.sp else 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${JulianDayFormatter.utcTimestamp(period.startJulianDay)}  –  ${JulianDayFormatter.utcTimestamp(period.endJulianDay)}", color = AynvoraTheme.colors.TextLightSecondary, fontSize = 10.sp)
            }
            if (hasChildren) Text("${period.subPeriods.size}", color = AynvoraTheme.colors.GoldLight, fontSize = 10.sp)
        }
    }
    if (expanded && hasChildren) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
            period.subPeriods.forEach { child -> DashaTreePeriod(child, depth + 1) }
        }
    }
}

@Composable
private fun SnapshotChartLegend(chart: com.aynvora.core.models.AstroChartSnapshot) {
    val grouped = androidx.compose.runtime.remember(chart) { com.aynvora.core.models.AstroChartBuilder.fromSnapshot(chart) }
    if (grouped is com.aynvora.core.models.AstroChartBuildResult.Valid) AynvoraChartLegend(grouped.chart)
}

private fun markerSuffix(position: com.aynvora.core.models.AstroChartPlacement): String = buildString {
    if (position.retrograde) append("℞")
    if (position.combust) append("☼")
    if (position.exalted == true) append("↑")
    if (position.debilitated == true) append("↓")
    if (position.vargottama == true) append("◆")
}

private fun displayCanonical(value: String?): String = value?.lowercase()?.replace('_', ' ')?.split(' ')?.joinToString(" ") { it.replaceFirstChar(Char::uppercase) } ?: "Unavailable"

@Composable
private fun EmptyReport(message: String) {
    Surface(color = AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.72f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(message, color = AynvoraTheme.colors.TextLightSecondary, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = AynvoraTheme.colors.CosmicIndigo, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = AynvoraTheme.colors.GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = AynvoraTheme.colors.TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = AynvoraTheme.colors.TextLightSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DetailRow(mark: String, title: String, subtitle: String, value: String) {
    Surface(color = AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.85f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mark, color = AynvoraTheme.colors.GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.size(42.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = AynvoraTheme.colors.TextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = AynvoraTheme.colors.TextLightSecondary, fontSize = 11.sp)
            }
            Text(value, color = AynvoraTheme.colors.TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun bodyShort(body: String): String = when (body) {
    "SUN" -> "Su"; "MOON" -> "Mo"; "MERCURY" -> "Me"; "VENUS" -> "Ve"; "MARS" -> "Ma"
    "JUPITER" -> "Ju"; "SATURN" -> "Sa"; "RAHU" -> "Ra"; "KETU" -> "Ke"; else -> body.take(2)
}

// Proleptic Gregorian civil date conversion from Unix epoch days.
private fun fromEpochDay(days: Int): Triple<Int, Int, Int> {
    val z = days + 719468
    val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    var y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + if (mp < 10) 3 else -9
    if (m <= 2) y++
    return Triple(y, m, d)
}
