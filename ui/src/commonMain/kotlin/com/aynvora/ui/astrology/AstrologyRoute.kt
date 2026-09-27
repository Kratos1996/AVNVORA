package com.aynvora.ui.astrology

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aynvora.astro.AstroEngine
import com.aynvora.astro.AynvoraAstroEngine
import com.aynvora.astro.BirthData
import com.aynvora.astro.BodyId
import com.aynvora.astro.BodyPosition
import com.aynvora.astro.CalculationResult
import com.aynvora.astro.EngineCalculationConfig
import com.aynvora.astro.houses.HousePosition
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.qa.android.qaAction
import com.aynvora.qa.core.models.QaActionId
import kotlinx.coroutines.launch

/**
 * Top-level route composable for the Vedic Astrology feature.
 * Connects directly to [AynvoraAstroEngine] to calculate deterministic natal charts,
 * Graha positions, Bhavas (Houses), Lagna, and Nakshatra placements.
 */
@Composable
fun AstrologyRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    engine: AstroEngine = remember { AynvoraAstroEngine() },
) {
    val isDark = AynvoraTheme.isDark
    val translator = LocalAynvoraTranslator.current
    val scope = rememberCoroutineScope()

    val bgColor = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    val primaryText = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryText =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

    // Birth Data Input Form State
    var birthDate by remember { mutableStateOf("1995-10-15") }
    var birthTime by remember { mutableStateOf("14:30:00") }
    var latitudeStr by remember { mutableStateOf("28.6139") }
    var longitudeStr by remember { mutableStateOf("77.2090") }
    var locationName by remember { mutableStateOf("New Delhi, India") }
    var timeZoneId by remember { mutableStateOf("Asia/Kolkata") }

    var isCalculating by remember { mutableStateOf(false) }
    var calculationResult by remember { mutableStateOf<CalculationResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun runCalculation() {
        scope.launch {
            isCalculating = true
            errorMessage = null
            try {
                val dateParts = birthDate.split("-").map { it.trim().toInt() }
                val timeParts = birthTime.split(":").map { it.trim().toInt() }
                val lat = latitudeStr.trim().toDouble()
                val lon = longitudeStr.trim().toDouble()

                val birthData = BirthData(
                    dateTimeIso = "${birthDate}T${birthTime}Z",
                    latitude = lat,
                    longitude = lon,
                    timeZoneId = timeZoneId,
                    year = dateParts[0],
                    month = dateParts[1],
                    day = dateParts[2],
                    hour = timeParts[0],
                    minute = timeParts[1],
                    second = if (timeParts.size > 2) timeParts[2] else 0,
                )

                val result = engine.calculate(birthData, EngineCalculationConfig())
                calculationResult = result
            } catch (e: Exception) {
                errorMessage = "Calculation error: ${e.message ?: "Invalid birth parameters"}"
            } finally {
                isCalculating = false
            }
        }
    }

    // Auto-calculate on initial entry for immediate presentation
    LaunchedEffect(Unit) {
        if (calculationResult == null) {
            runCalculation()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.sdp, vertical = 12.sdp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header Bar ────────────────────────────────────────────────────────
            AstrologyTopBar(
                onClose = onClose,
                isDark = isDark,
                primaryText = primaryText,
            )

            Spacer(modifier = Modifier.height(12.sdp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.sdp),
            ) {
                // ── Birth Input Form Card ─────────────────────────────────────────
                item(key = "input_form") {
                    AstrologyInputCard(
                        birthDate = birthDate,
                        onBirthDateChange = { birthDate = it },
                        birthTime = birthTime,
                        onBirthTimeChange = { birthTime = it },
                        latitudeStr = latitudeStr,
                        onLatitudeChange = { latitudeStr = it },
                        longitudeStr = longitudeStr,
                        onLongitudeChange = { longitudeStr = it },
                        locationName = locationName,
                        isCalculating = isCalculating,
                        onCalculate = { runCalculation() },
                        isDark = isDark,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                    )
                }

                if (errorMessage != null) {
                    item(key = "error_message") {
                        Text(
                            text = errorMessage ?: "",
                            color = AynvoraTheme.colors.Error,
                            style = AynvoraTheme.typography.body14,
                            modifier = Modifier.padding(horizontal = 4.sdp),
                        )
                    }
                }

                // ── Calculation Results Section ───────────────────────────────────
                val result = calculationResult
                if (result != null) {
                    // Lagna (Ascendant) Card
                    item(key = "lagna_card") {
                        LagnaCard(
                            result = result,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                        )
                    }

                    // Planetary Positions (Grahas) Card
                    item(key = "grahas_header") {
                        Text(
                            text = "ग्रह स्थिति • Planetary Positions (Grahas)",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                            color = AynvoraTheme.colors.Gold,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    items(result.positions, key = { it.bodyId.name }) { position ->
                        GrahaPositionRow(
                            position = position,
                            isDark = isDark,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                        )
                    }

                    // Houses (Bhavas) Card
                    if (result.houses.isNotEmpty()) {
                        item(key = "houses_header") {
                            Text(
                                text = "द्वादश भाव • 12 Bhavas (Houses)",
                                style = AynvoraTheme.typography.title18.copy(fontSize = 17.ssp),
                                color = AynvoraTheme.colors.Gold,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        item(key = "houses_grid") {
                            HousesSummaryCard(
                                houses = result.houses,
                                isDark = isDark,
                                primaryText = primaryText,
                                secondaryText = secondaryText,
                            )
                        }
                    }

                    // Classical Provenance & Guarantee Footer
                    item(key = "provenance_footer") {
                        AstrologyProvenanceFooter(
                            engineVersion = result.engineVersion,
                            calculationModel = result.calculationModel,
                            ayanamsaName = result.ayanamsaName,
                            isDark = isDark,
                            secondaryText = secondaryText,
                        )
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(24.sdp))
                }
            }
        }
    }
}

// ── Top Bar Composable ────────────────────────────────────────────────────────

@Composable
private fun AstrologyTopBar(
    onClose: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            AynvoraButton(
                text = "✕",
                variant = AynvoraButtonVariant.Outlined,
                onClick = onClose,
                modifier = Modifier
                    .qaAction(QaActionId.ASTROLOGY_CLOSE)
                    .semantics { contentDescription = "Close Vedic Astrology" },
            )

            Spacer(modifier = Modifier.width(12.sdp))

            Column {
                Text(
                    text = "AYNVORA Vedic Astrology",
                    style = AynvoraTheme.typography.title20,
                    color = AynvoraTheme.colors.Gold,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Deterministic Natal Kundli & Planetary Ephemeris",
                    style = AynvoraTheme.typography.caption12,
                    color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AynvoraTheme.colors.Gold.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    color = AynvoraTheme.colors.Gold.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.sdp, vertical = 4.sdp),
        ) {
            Text(
                text = "🪐 Vedic",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = AynvoraTheme.colors.Gold,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Birth Input Form Card ─────────────────────────────────────────────────────

@Composable
private fun AstrologyInputCard(
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    birthTime: String,
    onBirthTimeChange: (String) -> Unit,
    latitudeStr: String,
    onLatitudeChange: (String) -> Unit,
    longitudeStr: String,
    onLongitudeChange: (String) -> Unit,
    locationName: String,
    isCalculating: Boolean,
    onCalculate: () -> Unit,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.White,
        contentColor = primaryText,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "जन्म विवरण • Birth Details",
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = AynvoraTheme.colors.Gold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.sdp))
            Text(
                text = "Location: $locationName",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )

            Spacer(modifier = Modifier.height(10.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = onBirthDateChange,
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                        focusedLabelColor = AynvoraTheme.colors.Gold,
                    ),
                )

                OutlinedTextField(
                    value = birthTime,
                    onValueChange = onBirthTimeChange,
                    label = { Text("Time (HH:MM:SS)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                        focusedLabelColor = AynvoraTheme.colors.Gold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.sdp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                OutlinedTextField(
                    value = latitudeStr,
                    onValueChange = onLatitudeChange,
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                        focusedLabelColor = AynvoraTheme.colors.Gold,
                    ),
                )

                OutlinedTextField(
                    value = longitudeStr,
                    onValueChange = onLongitudeChange,
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AynvoraTheme.colors.Gold,
                        unfocusedBorderColor = AynvoraTheme.colors.Gold.copy(alpha = 0.3f),
                        focusedLabelColor = AynvoraTheme.colors.Gold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.sdp))

            AynvoraButton(
                text = if (isCalculating) "Calculating Kundli..." else "Calculate Chart • कुंडली गणना करें",
                variant = AynvoraButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .qaAction(QaActionId.ASTROLOGY_CALCULATE),
                enabled = !isCalculating,
                event = AynvoraClickEvent(
                    eventId = "astrology.calculate_clicked",
                    screenId = "astrology",
                    componentId = "calculate_chart_button",
                ),
                onClick = onCalculate,
            )
        }
    }
}

// ── Lagna (Ascendant) Card ────────────────────────────────────────────────────

@Composable
private fun LagnaCard(
    result: CalculationResult,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val lagna = result.lagna

    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
        containerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.White,
        contentColor = primaryText,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "लग्न • Ascendant (Lagna)",
                    style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                    color = AynvoraTheme.colors.Gold,
                    fontWeight = FontWeight.Bold,
                )
                if (lagna != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AynvoraTheme.colors.CelestialBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 8.sdp, vertical = 3.sdp),
                    ) {
                        Text(
                            text = lagna.rashiName,
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.CelestialBlue,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.sdp))

            if (lagna != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Rashi Degree",
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText
                        )
                        Text(
                            text = formatDegree(lagna.degreeInRashi),
                            style = AynvoraTheme.typography.body14,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column {
                        Text(
                            text = "Nakshatra",
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText
                        )
                        Text(
                            text = "${lagna.nakshatraName} (Pada ${lagna.pada})",
                            style = AynvoraTheme.typography.body14,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column {
                        Text(
                            text = "Sidereal Longitude",
                            style = AynvoraTheme.typography.caption12,
                            color = secondaryText
                        )
                        Text(
                            text = formatDegree(lagna.siderealLongitude),
                            style = AynvoraTheme.typography.body14,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            } else {
                Text(
                    text = "Lagna calculated from geographical coordinates and exact local sidereal time.",
                    style = AynvoraTheme.typography.body14,
                    color = secondaryText,
                )
            }
        }
    }
}

// ── Graha Position Row ────────────────────────────────────────────────────────

@Composable
private fun GrahaPositionRow(
    position: BodyPosition,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    val sanskritName = getGrahaSanskritName(position.bodyId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) AynvoraTheme.colors.CosmicNavy.copy(alpha = 0.7f) else AynvoraTheme.colors.White)
            .border(
                width = 1.dp,
                color = AynvoraTheme.colors.Gold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 12.sdp, vertical = 10.sdp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = position.bodyId.name,
                    style = AynvoraTheme.typography.body14,
                    color = AynvoraTheme.colors.Gold,
                    fontWeight = FontWeight.Bold,
                )
                if (position.isRetrograde) {
                    Spacer(modifier = Modifier.width(4.sdp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AynvoraTheme.colors.Error.copy(alpha = 0.2f))
                            .padding(horizontal = 4.sdp, vertical = 1.sdp),
                    ) {
                        Text(
                            text = "R (वक्र)",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 9.ssp),
                            color = AynvoraTheme.colors.Error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = sanskritName,
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
        }

        Column(modifier = Modifier.weight(1.3f)) {
            Text(
                text = position.rashiName,
                style = AynvoraTheme.typography.body14,
                color = primaryText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatDegree(position.degreeInRashi),
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
        }

        Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.End) {
            Text(
                text = position.nakshatraName,
                style = AynvoraTheme.typography.body14,
                color = primaryText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Pada ${position.pada} • ${formatDegree(position.degreeInNakshatra)}",
                style = AynvoraTheme.typography.caption12,
                color = secondaryText,
            )
        }
    }
}

// ── Houses Summary Card ───────────────────────────────────────────────────────

@Composable
private fun HousesSummaryCard(
    houses: List<HousePosition>,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color,
) {
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Outlined,
        containerColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold,
        contentColor = primaryText,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            for (chunk in houses.chunked(3)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.sdp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    for (house in chunk) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bhava ${house.houseNumber}",
                                style = AynvoraTheme.typography.caption12,
                                color = AynvoraTheme.colors.Gold,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = house.rashiName,
                                style = AynvoraTheme.typography.body14.copy(fontSize = 12.ssp),
                                color = primaryText,
                            )
                            Text(
                                text = formatDegree(house.degreeInRashi),
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                                color = secondaryText,
                            )
                        }
                    }
                }
                if (chunk != houses.chunked(3).last()) {
                    HorizontalDivider(
                        color = AynvoraTheme.colors.Gold.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 4.sdp),
                    )
                }
            }
        }
    }
}

// ── Provenance Footer ─────────────────────────────────────────────────────────

@Composable
private fun AstrologyProvenanceFooter(
    engineVersion: String,
    calculationModel: String,
    ayanamsaName: String,
    isDark: Boolean,
    secondaryText: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDark) AynvoraTheme.colors.CosmicIndigo.copy(alpha = 0.5f) else AynvoraTheme.colors.SoftGold.copy(
                    alpha = 0.5f
                )
            )
            .padding(12.sdp),
    ) {
        Column {
            Text(
                text = "✓ 100% Deterministic Engine • Zero LLM Hallucination",
                style = AynvoraTheme.typography.caption12,
                color = AynvoraTheme.colors.Success,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.sdp))
            Text(
                text = "Engine: $engineVersion | Model: $calculationModel | Ayanamsa: $ayanamsaName | Local Offline Calculation",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                color = secondaryText,
            )
        }
    }
}

private fun formatDegree(deg: Double): String {
    val d = deg.toInt()
    val m = ((deg - d) * 60).toInt()
    val s = ((((deg - d) * 60) - m) * 60).toInt()
    return "$d° $m' $s\""
}

private fun getGrahaSanskritName(bodyId: BodyId): String {
    return when (bodyId) {
        BodyId.SUN -> "सूर्य (Surya)"
        BodyId.MOON -> "चन्द्र (Chandra)"
        BodyId.MARS -> "मङ्गल (Mangala)"
        BodyId.MERCURY -> "बुध (Budha)"
        BodyId.JUPITER -> "गुरु (Guru / Brihaspati)"
        BodyId.VENUS -> "शुक्र (Shukra)"
        BodyId.SATURN -> "शनि (Shani)"
        BodyId.RAHU -> "राहु (Rahu)"
        BodyId.KETU -> "केतु (Ketu)"
    }
}
