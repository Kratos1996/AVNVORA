package com.aynvora.ui.gemstone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aynvora.core.gemstone.GemstoneCatalog
import com.aynvora.core.gemstone.GemstoneCompatibilityStatus
import com.aynvora.core.gemstone.GemstoneDescriptor
import com.aynvora.core.gemstone.GemstoneInventoryItem
import com.aynvora.core.gemstone.GemstoneMetal
import com.aynvora.core.gemstone.GemstoneType
import com.aynvora.core.gemstone.NavaratnaCell
import com.aynvora.core.gemstone.NavaratnaMatrix
import com.aynvora.core.models.Rashi
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportKeyValue
import com.aynvora.core.report.ReportParagraph
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant
import com.aynvora.designsystem.components.AynvoraStatusChip
import com.aynvora.designsystem.components.AynvoraStatusChipVariant
import com.aynvora.designsystem.event.aynvoraClickable
import com.aynvora.designsystem.localization.LocalAynvoraTranslator
import com.aynvora.localization.translation.TranslationKey
import com.aynvora.qa.android.qaAction
import com.aynvora.qa.core.models.QaActionId
import org.koin.compose.currentKoinScope

private fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val fullHex = when (clean.length) {
        6 -> "FF$clean"
        8 -> clean
        else -> "FFFFFFFF"
    }
    val value = fullHex.toLongOrNull(16) ?: 0xFFFFFFFFL
    return Color(value)
}

@Composable
fun GemstoneRoute(
    onClose: () -> Unit,
    viewModel: GemstoneViewModel? = null,
    modifier: Modifier = Modifier,
) {
    val koin = currentKoinScope()
    val vm = viewModel ?: remember(koin) {
        koin.getOrNull<GemstoneViewModel>() ?: error(
            "GemstoneViewModel not found in Koin scope. Ensure GemstoneRepository and " +
                    "CertificateImageAnalyzer are registered.",
        )
    }

    val state by vm.uiState.collectAsState()
    val isDark = AynvoraTheme.isDark
    val translator = LocalAynvoraTranslator.current

    val bgColor = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    val primaryText = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryText =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

    val certPicker = rememberGemstoneCertificatePicker(
        onImageCaptured = { bytes, isCamera ->
            vm.onEvent(GemstoneUiEvent.ProcessCertificateImage(bytes, isCamera))
        },
        onError = { /* handled gracefully */ },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
    ) {
        // Top App Bar
        GemstoneTopBar(
            title = translator.translate(TranslationKey.Gemstone.Title),
            subtitle = translator.translate(TranslationKey.Gemstone.Subtitle),
            onClose = onClose,
        )

        // Status or Error notification bar
        AnimatedVisibility(visible = state.statusMessage != null || state.errorMessage != null) {
            val isError = state.errorMessage != null
            val msg = state.errorMessage ?: state.statusMessage ?: ""
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isError) Color(0xFFC62828) else Color(0xFF1B5E20))
                    .padding(horizontal = AynvoraSpacing.space16, vertical = AynvoraSpacing.space8)
                    .clickable { vm.onEvent(GemstoneUiEvent.DismissStatus) },
            ) {
                Text(
                    text = msg,
                    style = AynvoraTheme.typography.caption12,
                    color = Color.White,
                )
            }
        }

        // Horizontal Category Tabs
        GemstoneNavigationTabs(
            currentTab = state.currentTab,
            onTabSelected = { vm.onEvent(GemstoneUiEvent.SelectTab(it)) },
        )

        // Screen Body according to active tab
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.selectedGemstone != null -> {
                    GemstoneDetailView(
                        descriptor = state.selectedGemstone!!,
                        onBack = { vm.onEvent(GemstoneUiEvent.BackToCatalog) },
                    )
                }

                state.currentTab == GemstoneTab.HOME -> {
                    GemstoneHomeView(
                        state = state,
                        onNavigateTab = { vm.onEvent(GemstoneUiEvent.SelectTab(it)) },
                        onSelectGemstone = { vm.onEvent(GemstoneUiEvent.ViewGemstoneDetail(it)) },
                    )
                }

                state.currentTab == GemstoneTab.CATALOG -> {
                    GemstoneCatalogView(
                        catalog = state.catalog,
                        onSelectGemstone = { vm.onEvent(GemstoneUiEvent.ViewGemstoneDetail(it)) },
                    )
                }

                state.currentTab == GemstoneTab.INVENTORY -> {
                    GemstoneInventoryView(
                        state = state,
                        onOpenAdd = { vm.onEvent(GemstoneUiEvent.OpenAddGemstone) },
                        onCancelAdd = { vm.onEvent(GemstoneUiEvent.CancelAddGemstone) },
                        onTypeChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemType(it)) },
                        onCaratsChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemCarats(it)) },
                        onMetalChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemMetal(it)) },
                        onFingerChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemFinger(it)) },
                        onIsWornChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemIsWorn(it)) },
                        onNotesChange = { vm.onEvent(GemstoneUiEvent.UpdateNewGemNotes(it)) },
                        onSubmit = { vm.onEvent(GemstoneUiEvent.SubmitNewGemstone) },
                        onDelete = { vm.onEvent(GemstoneUiEvent.DeleteInventoryItem(it)) },
                        onToggleWorn = { vm.onEvent(GemstoneUiEvent.ToggleWornStatus(it)) },
                    )
                }

                state.currentTab == GemstoneTab.COMPATIBILITY -> {
                    GemstoneCompatibilityView(
                        state = state,
                        onGem1Change = { vm.onEvent(GemstoneUiEvent.SelectCompatGem1(it)) },
                        onGem2Change = { vm.onEvent(GemstoneUiEvent.SelectCompatGem2(it)) },
                        onCheck = { vm.onEvent(GemstoneUiEvent.RunCompatibilityCheck) },
                    )
                }

                state.currentTab == GemstoneTab.RECOMMENDATIONS -> {
                    GemstoneRecommendationsView(
                        state = state,
                        onLagnaChange = { vm.onEvent(GemstoneUiEvent.SelectLagna(it)) },
                        onMoonChange = { vm.onEvent(GemstoneUiEvent.SelectMoon(it)) },
                        onGenerate = { vm.onEvent(GemstoneUiEvent.GenerateRecommendations) },
                        onGenerateReport = { vm.onEvent(GemstoneUiEvent.GenerateReport) },
                    )
                }

                state.currentTab == GemstoneTab.CERTIFICATE -> {
                    GemstoneCertificateView(
                        state = state,
                        onLaunchCamera = { certPicker.launchCamera() },
                        onLaunchGallery = { certPicker.launchGallery() },
                    )
                }

                state.currentTab == GemstoneTab.REPORT_PREVIEW -> {
                    GemstoneReportPreviewView(
                        document = state.reportDocument,
                        onBack = { vm.onEvent(GemstoneUiEvent.SelectTab(GemstoneTab.RECOMMENDATIONS)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GemstoneTopBar(
    title: String,
    subtitle: String,
    onClose: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AynvoraSpacing.space16, vertical = AynvoraSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AynvoraTheme.typography.title18.copy(
                    fontSize = 18.ssp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(2.sdp))
            Text(
                text = subtitle,
                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.sdp))
        Box(
            modifier = Modifier
                .size(40.sdp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (isDark) Color(0xFF333B50) else Color(0xFFD0D5DD),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onClose() }
                .qaAction(QaActionId.GEMSTONE_CLOSE),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                style = AynvoraTheme.typography.title18.copy(fontSize = 16.ssp),
                color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
            )
        }
    }
}

@Composable
private fun GemstoneNavigationTabs(
    currentTab: GemstoneTab,
    onTabSelected: (GemstoneTab) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val tabs = listOf(
        GemstoneTab.HOME to "Home",
        GemstoneTab.RECOMMENDATIONS to "Recommendations",
        GemstoneTab.CATALOG to "Catalog",
        GemstoneTab.INVENTORY to "My Inventory",
        GemstoneTab.COMPATIBILITY to "Conflict Check",
        GemstoneTab.CERTIFICATE to "Lab Cert",
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AynvoraSpacing.space16, vertical = AynvoraSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tabs) { (tab, label) ->
            val isSelected = currentTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) AynvoraTheme.colors.Gold else if (isDark) Color(0xFF1E2433) else Color(
                            0xFFEAEFF5
                        )
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .qaAction(QaActionId.of("gemstone", "tab", tab.name.lowercase(), "select")),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = AynvoraTheme.typography.caption12.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.ssp,
                    ),
                    color = if (isSelected) Color(0xFF11141D) else if (isDark) Color(0xFFE2E8F0) else Color(
                        0xFF334155
                    ),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. HOME SCREEN & NAVARATNA MANDALA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneHomeView(
    state: GemstoneUiState,
    onNavigateTab: (GemstoneTab) -> Unit,
    onSelectGemstone: (GemstoneDescriptor) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            // Recommendation Highlight Banner
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Astrological Recommendation",
                            style = AynvoraTheme.typography.title18.copy(
                                fontSize = 16.ssp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AynvoraTheme.colors.Gold,
                        )
                        AynvoraStatusChip(
                            text = "${state.selectedLagna.name} Lagna",
                            variant = AynvoraStatusChipVariant.Available,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Based on your Ascendant, your primary Life Stone (Jeevan Ratna) and Fortune Stone (Bhagya Ratna) have been calculated deterministically.",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AynvoraButton(
                        text = "View Personalized Recommendations",
                        variant = AynvoraButtonVariant.Primary,
                        onClick = { onNavigateTab(GemstoneTab.RECOMMENDATIONS) },
                    )
                }
            }
        }

        item {
            // Navaratna Mandala Card
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Navaratna Mandala (9 Sacred Gems)",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 16.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Traditional directional arrangement centered around the Sun (Ruby). Tap any gemstone to inspect its properties.",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3x3 Grid for Navaratna Mandala
                    NavaratnaMandalaGrid(
                        cells = NavaratnaMatrix.CELLS,
                        onCellClick = { cell ->
                            val desc = GemstoneCatalog.findByType(cell.gemstoneType)
                            onSelectGemstone(desc)
                        },
                    )
                }
            }
        }

        item {
            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AynvoraCard(
                    modifier = Modifier.weight(1f)
                        .clickable { onNavigateTab(GemstoneTab.INVENTORY) },
                    variant = AynvoraCardVariant.Outlined,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💎 My Gemstones",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.inventory.size} items recorded",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                }
                AynvoraCard(
                    modifier = Modifier.weight(1f)
                        .clickable { onNavigateTab(GemstoneTab.COMPATIBILITY) },
                    variant = AynvoraCardVariant.Outlined,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚖️ Conflict Check",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 14.ssp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Classical Jyotisha rules",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.TextLightSecondary,
                        )
                    }
                }
            }
        }

        item {
            // Lab Certificate OCR Inspection Card
            AynvoraCard(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onNavigateTab(GemstoneTab.CERTIFICATE) },
                variant = AynvoraCardVariant.Outlined,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🔬",
                        style = AynvoraTheme.typography.title18.copy(fontSize = 24.ssp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lab Certificate Optical Inspection",
                            style = AynvoraTheme.typography.title18.copy(
                                fontSize = 14.ssp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Scan or upload gemstone reports (GIA, IGI, GJEPC) for on-device metadata verification.",
                            style = AynvoraTheme.typography.caption12,
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavaratnaMandalaGrid(
    cells: List<NavaratnaCell>,
    onCellClick: (NavaratnaCell) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (col in 0..2) {
                    val cell = cells.first { it.gridRow == row && it.gridCol == col }
                    NavaratnaCellView(
                        cell = cell,
                        modifier = Modifier.weight(1f),
                        onClick = { onCellClick(cell) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavaratnaCellView(
    cell: NavaratnaCell,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val isCenter = cell.direction == com.aynvora.core.gemstone.NavaratnaDirection.CENTER

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCenter) Color(0xFF2A1F1D) else if (isDark) Color(0xFF141926) else Color(
                    0xFFEEF2F6
                )
            )
            .border(
                1.dp,
                if (isCenter) AynvoraTheme.colors.Gold else if (isDark) Color(0xFF283149) else Color(
                    0xFFCBD5E1
                ),
                RoundedCornerShape(12.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val descriptor = GemstoneCatalog.findByType(cell.gemstoneType)
        GemstonePhoto(
            descriptor = descriptor,
            modifier = Modifier.size(42.sdp),
            shape = CircleShape,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = cell.sanskritName,
            style = AynvoraTheme.typography.caption12.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.ssp
            ),
            color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = cell.planet.name,
            style = AynvoraTheme.typography.caption12.copy(fontSize = 9.ssp),
            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. GEMSTONE CATALOG & DETAIL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneCatalogView(
    catalog: List<GemstoneDescriptor>,
    onSelectGemstone: (GemstoneDescriptor) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        items(catalog, key = { it.id }) { desc ->
            AynvoraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGemstone(desc) }
                    .qaAction(QaActionId.of("gemstone", "catalog", desc.id, "select")),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GemstonePhoto(
                        descriptor = desc,
                        modifier = Modifier.size(56.sdp),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = desc.commonName,
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 15.ssp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AynvoraTheme.colors.Gold,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${desc.sanskritName})",
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                                color = AynvoraTheme.colors.TextLightSecondary,
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Planet: ${desc.primaryPlanet.name} • ${desc.mineralSpecies}",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc.traditionalSignificance,
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = AynvoraTheme.colors.TextLightSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GemstoneDetailView(
    descriptor: GemstoneDescriptor,
    onBack: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.sdp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF1E2433) else Color(0xFFE2E8F0))
                        .clickable { onBack() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "←",
                        color = AynvoraTheme.colors.Gold,
                        style = AynvoraTheme.typography.title18
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${descriptor.commonName} (${descriptor.sanskritName})",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 18.ssp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "Hindi: ${descriptor.hindiName} • Planet: ${descriptor.primaryPlanet.name}",
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.CelestialBlue,
                    )
                }
            }
        }

        item {
            GemstonePhoto(
                descriptor = descriptor,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(16.dp),
            )
        }

        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Physical & Traditional Specifications",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 15.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SpecificationRow("Mineral Species", descriptor.mineralSpecies)
                    SpecificationRow("Color Family", descriptor.colorFamily)
                    SpecificationRow("Mohs Hardness", "${descriptor.hardnessMohs} / 10")
                    SpecificationRow("Recommended Metal", descriptor.primaryMetal.name)
                    SpecificationRow(
                        "Traditional Finger",
                        "${descriptor.traditionalFinger.displayName} (${descriptor.traditionalFinger.sanskritName})"
                    )
                    SpecificationRow("Wearing Day & Time", descriptor.recommendedDayTime)
                }
            }
        }

        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Traditional Jyotisha Significance",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 15.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = descriptor.traditionalSignificance,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )
                }
            }
        }

        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Classical Caveats & Incompatible Pairs",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 15.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFEF5350),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    for (caveat in descriptor.caveatsAndContraindications) {
                        Text(
                            text = "• $caveat",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Outlined,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Classical Text Citations (Source Gating)",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 14.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    for (citation in descriptor.sourceCitations) {
                        Text(
                            text = "📜 ${citation.title} — ${citation.chapterOrSection}",
                            style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                        Text(
                            text = "\"${citation.quoteOrSummary}\"",
                            style = AynvoraTheme.typography.caption12.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecificationRow(label: String, value: String) {
    val isDark = AynvoraTheme.isDark
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
        )
        Text(
            text = value,
            style = AynvoraTheme.typography.caption12.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.ssp
            ),
            color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. INVENTORY MANAGEMENT (ROOM / PERSISTENT STORAGE)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneInventoryView(
    state: GemstoneUiState,
    onOpenAdd: () -> Unit,
    onCancelAdd: () -> Unit,
    onTypeChange: (GemstoneType) -> Unit,
    onCaratsChange: (String) -> Unit,
    onMetalChange: (GemstoneMetal) -> Unit,
    onFingerChange: (String) -> Unit,
    onIsWornChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDelete: (String) -> Unit,
    onToggleWorn: (GemstoneInventoryItem) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "My Gemstone Inventory",
                    style = AynvoraTheme.typography.title18.copy(
                        fontSize = 16.ssp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AynvoraTheme.colors.Gold,
                )
                if (!state.isAddingGemstone) {
                    AynvoraButton(
                        text = "+ Add Gemstone",
                        variant = AynvoraButtonVariant.Primary,
                        onClick = onOpenAdd,
                    )
                }
            }
        }

        if (state.isAddingGemstone) {
            item {
                AddGemstoneFormCard(
                    state = state,
                    onTypeChange = onTypeChange,
                    onCaratsChange = onCaratsChange,
                    onMetalChange = onMetalChange,
                    onFingerChange = onFingerChange,
                    onIsWornChange = onIsWornChange,
                    onNotesChange = onNotesChange,
                    onSubmit = onSubmit,
                    onCancel = onCancelAdd,
                )
            }
        }

        if (state.inventory.isEmpty() && !state.isAddingGemstone) {
            item {
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Outlined,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No Gemstones in Inventory",
                            style = AynvoraTheme.typography.title18.copy(fontSize = 15.ssp),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Record your currently worn rings or pendants to enable automatic planetary conflict detection in recommendations.",
                            style = AynvoraTheme.typography.caption12,
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        items(state.inventory, key = { it.id }) { item ->
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${item.type.sanskritName} (${item.type.name})",
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 15.ssp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AynvoraTheme.colors.Gold,
                            )
                            Text(
                                text = "Planet: ${item.type.primaryPlanet.name} • ${item.approximateCaratWeight} cts • ${item.metal.name}",
                                style = AynvoraTheme.typography.caption12,
                                color = AynvoraTheme.colors.CelestialBlue,
                            )
                        }
                        AynvoraStatusChip(
                            text = if (item.isCurrentlyWorn) "Worn" else "Stored",
                            variant = if (item.isCurrentlyWorn) AynvoraStatusChipVariant.Available else AynvoraStatusChipVariant.Offline,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Placement: ${item.fingerOrPlacement}",
                        style = AynvoraTheme.typography.caption12,
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )

                    if (!item.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${item.notes}",
                            style = AynvoraTheme.typography.caption12.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = item.isCurrentlyWorn,
                                onCheckedChange = { onToggleWorn(item) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AynvoraTheme.colors.Gold,
                                    checkedTrackColor = Color(0xFF4A3E1C),
                                ),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (item.isCurrentlyWorn) "Actively Worn" else "Inactive",
                                style = AynvoraTheme.typography.caption12,
                                color = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark,
                            )
                        }

                        AynvoraButton(
                            text = "Remove",
                            variant = AynvoraButtonVariant.Outlined,
                            onClick = { onDelete(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGemstoneFormCard(
    state: GemstoneUiState,
    onTypeChange: (GemstoneType) -> Unit,
    onCaratsChange: (String) -> Unit,
    onMetalChange: (GemstoneMetal) -> Unit,
    onFingerChange: (String) -> Unit,
    onIsWornChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    AynvoraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = AynvoraCardVariant.Elevated,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Record New Gemstone",
                style = AynvoraTheme.typography.title18.copy(
                    fontSize = 16.ssp,
                    fontWeight = FontWeight.Bold
                ),
                color = AynvoraTheme.colors.Gold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Select Gemstone Type
            Text(text = "Gemstone Type:", style = AynvoraTheme.typography.caption12)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(GemstoneType.entries) { gemType ->
                    val isSel = state.newGemType == gemType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                    0xFF1E2433
                                ) else Color(0xFFE2E8F0)
                            )
                            .clickable { onTypeChange(gemType) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GemstonePhoto(
                                descriptor = GemstoneCatalog.findByType(gemType),
                                modifier = Modifier.size(24.sdp),
                                shape = CircleShape,
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = gemType.sanskritName,
                                style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Carats field
            OutlinedTextField(
                value = state.newGemCarats,
                onValueChange = onCaratsChange,
                label = { Text("Carat Weight (cts)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AynvoraTheme.colors.Gold,
                    unfocusedBorderColor = if (isDark) Color(0xFF333B50) else Color(0xFFD0D5DD),
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metal Setting
            Text(text = "Metal Setting:", style = AynvoraTheme.typography.caption12)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(GemstoneMetal.entries) { metal ->
                    val isSel = state.newGemMetal == metal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                    0xFF1E2433
                                ) else Color(0xFFE2E8F0)
                            )
                            .clickable { onMetalChange(metal) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = metal.name,
                            style = AynvoraTheme.typography.caption12,
                            color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Finger Placement
            OutlinedTextField(
                value = state.newGemFinger,
                onValueChange = onFingerChange,
                label = { Text("Finger / Body Placement (e.g. Ring Finger)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AynvoraTheme.colors.Gold,
                    unfocusedBorderColor = if (isDark) Color(0xFF333B50) else Color(0xFFD0D5DD),
                ),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            OutlinedTextField(
                value = state.newGemNotes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional: lab cert, jeweler, origin)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AynvoraTheme.colors.Gold,
                    unfocusedBorderColor = if (isDark) Color(0xFF333B50) else Color(0xFFD0D5DD),
                ),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AynvoraButton(
                    text = "Cancel",
                    variant = AynvoraButtonVariant.Outlined,
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                )
                AynvoraButton(
                    text = "Save Gemstone",
                    variant = AynvoraButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = onSubmit,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. COMPATIBILITY & CONFLICT CHECKER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneCompatibilityView(
    state: GemstoneUiState,
    onGem1Change: (GemstoneType) -> Unit,
    onGem2Change: (GemstoneType) -> Unit,
    onCheck: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemstone Pair Conflict Checker",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 16.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evaluate whether two gemstones can be safely worn together according to Parashara Graha Sambandha (mutual planetary enmity).",
                        style = AynvoraTheme.typography.caption12,
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "First Gemstone:",
                        style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GemstoneType.entries) { gem ->
                            val isSel = state.compatGem1 == gem
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                            0xFF1E2433
                                        ) else Color(0xFFE2E8F0)
                                    )
                                    .clickable { onGem1Change(gem) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GemstonePhoto(
                                        descriptor = GemstoneCatalog.findByType(gem),
                                        modifier = Modifier.size(24.sdp),
                                        shape = CircleShape,
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = gem.sanskritName,
                                        style = AynvoraTheme.typography.caption12,
                                        color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Second Gemstone:",
                        style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GemstoneType.entries) { gem ->
                            val isSel = state.compatGem2 == gem
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                            0xFF1E2433
                                        ) else Color(0xFFE2E8F0)
                                    )
                                    .clickable { onGem2Change(gem) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GemstonePhoto(
                                        descriptor = GemstoneCatalog.findByType(gem),
                                        modifier = Modifier.size(24.sdp),
                                        shape = CircleShape,
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = gem.sanskritName,
                                        style = AynvoraTheme.typography.caption12,
                                        color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    AynvoraButton(
                        text = "Evaluate Combination",
                        variant = AynvoraButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                            .qaAction(QaActionId.GEMSTONE_CHECK_COMPATIBILITY),
                        onClick = onCheck,
                    )
                }
            }
        }

        if (state.compatibilityResult != null) {
            val res = state.compatibilityResult
            item {
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Elevated,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Evaluation Result",
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 15.ssp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = AynvoraTheme.colors.Gold,
                            )
                            AynvoraStatusChip(
                                text = res.status.name,
                                variant = when (res.status) {
                                    GemstoneCompatibilityStatus.COMPATIBLE -> AynvoraStatusChipVariant.Available
                                    GemstoneCompatibilityStatus.CAUTION -> AynvoraStatusChipVariant.Warning
                                    GemstoneCompatibilityStatus.CONFLICT -> AynvoraStatusChipVariant.Error
                                    GemstoneCompatibilityStatus.INSUFFICIENT_DATA -> AynvoraStatusChipVariant.Offline
                                },
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = res.explanation,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )

                        if (res.conflictingFactors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Contraindications:",
                                style = AynvoraTheme.typography.caption12.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF5350)
                                ),
                            )
                            for (factor in res.conflictingFactors) {
                                Text(
                                    text = "• $factor",
                                    style = AynvoraTheme.typography.caption12,
                                    color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                                )
                            }
                        }

                        if (res.triggeredRules.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(
                                color = if (isDark) Color(0xFF283149) else Color(
                                    0xFFCBD5E1
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            for (rule in res.triggeredRules) {
                                Text(
                                    text = "Source Citation: ${rule.sourceCitation.title} (${rule.sourceCitation.chapterOrSection})",
                                    style = AynvoraTheme.typography.caption12.copy(
                                        fontSize = 10.ssp,
                                        color = AynvoraTheme.colors.CelestialBlue
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. ASTROLOGICAL RECOMMENDATIONS (JEEVAN, BHAGYA, PUNYA RATNAS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneRecommendationsView(
    state: GemstoneUiState,
    onLagnaChange: (Rashi) -> Unit,
    onMoonChange: (Rashi) -> Unit,
    onGenerate: () -> Unit,
    onGenerateReport: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val pkg = state.recommendationPackage

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Birth Chart Signs",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 16.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose your Ascendant (Lagna) and Moon Sign to derive classical Jeevan and Bhagya Ratnas.",
                        style = AynvoraTheme.typography.caption12,
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ascendant (Lagna):",
                        style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Rashi.entries) { rashi ->
                            val isSel = state.selectedLagna == rashi
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                            0xFF1E2433
                                        ) else Color(0xFFE2E8F0)
                                    )
                                    .clickable { onLagnaChange(rashi) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = rashi.name,
                                    style = AynvoraTheme.typography.caption12,
                                    color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Moon Sign (Chandra Rashi):",
                        style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Rashi.entries) { rashi ->
                            val isSel = state.selectedMoon == rashi
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) AynvoraTheme.colors.Gold else if (isDark) Color(
                                            0xFF1E2433
                                        ) else Color(0xFFE2E8F0)
                                    )
                                    .clickable { onMoonChange(rashi) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = rashi.name,
                                    style = AynvoraTheme.typography.caption12,
                                    color = if (isSel) Color(0xFF11141D) else if (isDark) Color.White else Color.Black,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    AynvoraButton(
                        text = "Recompute Recommendations",
                        variant = AynvoraButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                            .qaAction(QaActionId.GEMSTONE_GENERATE_RECOMMENDATION),
                        onClick = onGenerate,
                    )
                }
            }
        }

        if (pkg != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Recommended Gemstones (${pkg.primaryRecommendations.size})",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 16.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    AynvoraButton(
                        text = "Generate Report",
                        variant = AynvoraButtonVariant.Outlined,
                        onClick = onGenerateReport,
                    )
                }
            }

            items(pkg.primaryRecommendations) { rec ->
                val desc = GemstoneCatalog.findByType(rec.gemstoneType)
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Elevated,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GemstonePhoto(
                                descriptor = desc,
                                modifier = Modifier.size(48.sdp),
                                shape = RoundedCornerShape(8.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = rec.title,
                                modifier = Modifier.weight(1f),
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 15.ssp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AynvoraTheme.colors.Gold,
                            )
                            AynvoraStatusChip(
                                text = desc.commonName,
                                variant = AynvoraStatusChipVariant.Available,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${desc.sanskritName} • Planet: ${rec.associatedPlanet.name}",
                            style = AynvoraTheme.typography.caption12,
                            color = AynvoraTheme.colors.CelestialBlue,
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = rec.rationale,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        SpecificationRow("Setting Metal", rec.recommendedMetal.name)
                        SpecificationRow(
                            "Finger Placement",
                            "${rec.recommendedFinger.displayName} (${rec.recommendedFinger.sanskritName})"
                        )
                        SpecificationRow("Day & Timing", rec.recommendedDayTime)

                        if (rec.inventoryWarnings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            for (warn in rec.inventoryWarnings) {
                                Text(
                                    text = "⚠️ $warn",
                                    style = AynvoraTheme.typography.caption12.copy(
                                        color = Color(
                                            0xFFEF5350
                                        )
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            if (pkg.cautionedGemstones.isNotEmpty()) {
                item {
                    Text(
                        text = "Cautionary Contraindications",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 15.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFEF5350),
                    )
                }

                items(pkg.cautionedGemstones) { caution ->
                    val desc = GemstoneCatalog.findByType(caution.gemstoneType)
                    AynvoraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = AynvoraCardVariant.Outlined,
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "${desc.commonName} (${desc.sanskritName}) — Planet: ${caution.associatedPlanet.name}",
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 14.ssp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color(0xFFEF5350),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = caution.rationale,
                                style = AynvoraTheme.typography.caption12,
                                color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                            )
                        }
                    }
                }
            }

            item {
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Outlined,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Traditional Jyotisha Ethical Disclosure",
                            style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
                            color = AynvoraTheme.colors.Gold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pkg.ethicalDisclaimer,
                            style = AynvoraTheme.typography.caption12,
                            color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. CERTIFICATE OPTICAL INSPECTION
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneCertificateView(
    state: GemstoneUiState,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val cert = state.certificateResult

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemstone Certificate Optical Inspection",
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 16.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Capture or upload a recognized gemological laboratory report (e.g. GIA, IGI, GJEPC, Gubelin) to extract report number, species, carat weight, and treatment observations on-device.",
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AynvoraButton(
                            text = "📷 Open Camera",
                            variant = AynvoraButtonVariant.Primary,
                            modifier = Modifier.weight(1f)
                                .qaAction(QaActionId.GEMSTONE_OPEN_CAMERA),
                            onClick = onLaunchCamera,
                        )
                        AynvoraButton(
                            text = "🖼️ Select Gallery",
                            variant = AynvoraButtonVariant.Outlined,
                            modifier = Modifier.weight(1f)
                                .qaAction(QaActionId.GEMSTONE_OPEN_GALLERY),
                            onClick = onLaunchGallery,
                        )
                    }
                }
            }
        }

        if (state.isInspectingCertificate) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AynvoraTheme.colors.Gold)
                }
            }
        }

        if (cert != null) {
            item {
                AynvoraCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = AynvoraCardVariant.Elevated,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Extracted Report Findings",
                                style = AynvoraTheme.typography.title18.copy(
                                    fontSize = 15.ssp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AynvoraTheme.colors.Gold,
                            )
                            AynvoraStatusChip(
                                text = "Confidence ${(cert.confidenceScore * 100).toInt()}%",
                                variant = AynvoraStatusChipVariant.Available,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        SpecificationRow("Report Number", cert.certificateNumber)
                        SpecificationRow("Testing Laboratory", cert.labName)
                        SpecificationRow("Identified Species", cert.identifiedSpecies)
                        SpecificationRow(
                            "Reported Carats",
                            "${cert.reportedCaratWeight ?: "—"} cts"
                        )
                        SpecificationRow("Color & Appearance", cert.colorDescription ?: "—")
                        SpecificationRow("Shape & Cut", cert.shapeAndCut ?: "—")
                        SpecificationRow("Enhancement / Treatment", cert.treatmentObservations)

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(
                            color = if (isDark) Color(0xFF283149) else Color(
                                0xFFCBD5E1
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Raw OCR Text Lines (${cert.detectedFieldsCount} fields detected):",
                            style = AynvoraTheme.typography.caption12.copy(fontWeight = FontWeight.Bold),
                            color = AynvoraTheme.colors.CelestialBlue,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        for (line in cert.rawExtractedLines) {
                            Text(
                                text = line,
                                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                                color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notice: ${cert.authenticityDisclaimer}",
                            style = AynvoraTheme.typography.caption12.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = Color(0xFFFFA726),
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. REPORT PREVIEW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GemstoneReportPreviewView(
    document: ReportDocument?,
    onBack: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    if (document == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No report generated yet.", color = AynvoraTheme.colors.TextLightSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = AynvoraSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.sdp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF1E2433) else Color(0xFFE2E8F0))
                        .clickable { onBack() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "←",
                        color = AynvoraTheme.colors.Gold,
                        style = AynvoraTheme.typography.title18
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = document.title.value,
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 17.ssp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Text(
                        text = "Report ID: ${document.metadata.reportId}",
                        style = AynvoraTheme.typography.caption12,
                        color = AynvoraTheme.colors.CelestialBlue,
                    )
                }
            }
        }

        items(document.sections) { section ->
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = section.title.value,
                        style = AynvoraTheme.typography.title18.copy(
                            fontSize = 15.ssp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AynvoraTheme.colors.Gold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    for (block in section.blocks) {
                        when (block) {
                            is ReportKeyValue -> {
                                SpecificationRow(block.label.value, block.value)
                            }

                            is ReportParagraph -> {
                                Text(
                                    text = block.text.value,
                                    style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                                    color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
