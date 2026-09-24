package com.aynvora.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aynvora.core.report.ReportBlock
import com.aynvora.core.report.ReportContentKind
import com.aynvora.core.report.ReportDocument
import com.aynvora.core.report.ReportEvidence
import com.aynvora.core.report.ReportInterpretation
import com.aynvora.core.report.ReportKeyValue
import com.aynvora.core.report.ReportMetric
import com.aynvora.core.report.ReportParagraph
import com.aynvora.core.report.ReportSection
import com.aynvora.core.report.ReportSubsection
import com.aynvora.core.report.ReportTable
import com.aynvora.core.report.ReportTextKey
import com.aynvora.core.report.ReportTextResolver
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import kotlinx.coroutines.launch

/** Reusable renderer for the same immutable document passed to PDF generation. */
@Composable
fun ReportViewer(
    document: ReportDocument,
    resolver: ReportTextResolver,
    onGeneratePdf: () -> Unit,
    onSharePdf: (() -> Unit)? = null,
    pdfStatus: String? = null,
    modifier: Modifier = Modifier,
) {
    require(document.metadata.language == resolver.language) { "Report and viewer languages must match" }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize()) {
        ReportHeader(document, resolver)
        Text(
            text = resolver.text(ReportTextKey.SECTION_NAVIGATION).value,
            style = MaterialTheme.typography.labelLarge,
            color = AynvoraTheme.colors.Gold,
            modifier = Modifier.padding(start = 16.sdp, top = 8.sdp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.sdp, vertical = 4.sdp),
            horizontalArrangement = Arrangement.spacedBy(6.sdp),
        ) {
            itemsIndexed(document.sections, key = { _, section -> section.id }) { index, section ->
                OutlinedButton(onClick = { scope.launch { listState.animateScrollToItem(index) } }) {
                    Text(section.title.value, maxLines = 1)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.sdp),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            Button(onClick = onGeneratePdf) { Text(resolver.text(ReportTextKey.GENERATE_PDF).value) }
            if (onSharePdf != null) OutlinedButton(onClick = onSharePdf) {
                Text(
                    resolver.text(
                        ReportTextKey.SHARE
                    ).value
                )
            }
        }
        pdfStatus?.let {
            Text(
                it,
                color = AynvoraTheme.colors.TextLightSecondary,
                modifier = Modifier.padding(horizontal = 16.sdp, vertical = 4.sdp)
            )
        }
        if (document.sections.isEmpty()) {
            Text(
                resolver.text(ReportTextKey.EMPTY).value,
                modifier = Modifier.padding(20.sdp),
                color = AynvoraTheme.colors.TextLight
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.sdp),
                verticalArrangement = Arrangement.spacedBy(10.sdp),
            ) {
                itemsIndexed(document.sections, key = { _, section -> section.id }) { _, section ->
                    ReportSectionRenderer(section, resolver)
                }
                item(key = "document-disclaimer") { ReportDisclaimerRenderer(document, resolver) }
            }
        }
    }
}

@Composable
private fun ReportHeader(document: ReportDocument, resolver: ReportTextResolver) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.sdp, vertical = 12.sdp)) {
        Text(
            document.title.value,
            style = MaterialTheme.typography.headlineSmall,
            color = AynvoraTheme.colors.Gold
        )
        document.metadata.identity.displayName?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleMedium,
                color = AynvoraTheme.colors.TextLight
            )
        }
        val birth = document.sections.firstOrNull { it.id == "birth_details" }
        val summary = birth?.blocks?.filterIsInstance<ReportKeyValue>()?.filter {
            it.label.key == ReportTextKey.BIRTH_DATE.key || it.label.key == ReportTextKey.BIRTH_TIME.key || it.label.key == ReportTextKey.BIRTH_PLACE.key
        }
        summary?.forEach {
            Text(
                "${it.label.value}: ${it.value}",
                color = AynvoraTheme.colors.TextLightSecondary
            )
        }
        Spacer(Modifier.height(4.sdp))
        Text(
            resolver.text(ReportTextKey.REPORT_METADATA).value,
            style = MaterialTheme.typography.labelLarge,
            color = AynvoraTheme.colors.CelestialBlue
        )
        Text(
            "${resolver.text(ReportTextKey.GENERATED_AT).value}: ${resolver.generatedAtUtc(document.metadata.generatedAtEpochMs)}",
            color = AynvoraTheme.colors.TextLightSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "${resolver.text(ReportTextKey.SCHEMA_VERSION).value}: ${document.metadata.version.reportSchemaVersion} · ${
                resolver.text(
                    ReportTextKey.CALCULATION_VERSION
                ).value
            }: ${document.metadata.version.calculationVersion} · ${resolver.text(ReportTextKey.CONTENT_VERSION).value}: ${document.metadata.version.contentVersion}",
            color = AynvoraTheme.colors.TextLightSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ReportSectionRenderer(
    section: ReportSection,
    resolver: ReportTextResolver,
    modifier: Modifier = Modifier
) {
    var expanded by remember(section.id) { mutableStateOf(true) }
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AynvoraTheme.colors.CosmicNavy),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.sdp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    section.title.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (expanded) "−" else "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = AynvoraTheme.colors.Gold
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.sdp))
                section.blocks.forEach { ReportBlockRenderer(it, resolver) }
                section.subsections.forEach { ReportSubsectionRenderer(it, resolver) }
                ReportEvidenceRenderer(section.evidence, resolver)
            }
        }
    }
}

@Composable
private fun ReportSubsectionRenderer(subsection: ReportSubsection, resolver: ReportTextResolver) {
    Text(
        subsection.title.value,
        style = MaterialTheme.typography.titleSmall,
        color = AynvoraTheme.colors.CelestialBlue,
        modifier = Modifier.padding(top = 8.sdp)
    )
    subsection.blocks.forEach { ReportBlockRenderer(it, resolver) }
    ReportEvidenceRenderer(subsection.evidence, resolver)
}

@Composable
private fun ReportBlockRenderer(block: ReportBlock, resolver: ReportTextResolver) {
    when (block) {
        is ReportParagraph -> Column(Modifier.padding(vertical = 4.sdp)) {
            Text(
                contentKindLabel(block.kind, resolver),
                style = MaterialTheme.typography.labelSmall,
                color = AynvoraTheme.colors.CelestialBlue
            )
            Text(
                block.text.value,
                style = MaterialTheme.typography.bodyMedium,
                color = AynvoraTheme.colors.TextLight
            )
        }

        is ReportKeyValue -> Column(Modifier.padding(vertical = 3.sdp)) {
            Text(
                contentKindLabel(block.kind, resolver),
                style = MaterialTheme.typography.labelSmall,
                color = AynvoraTheme.colors.CelestialBlue
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                Text(
                    block.label.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AynvoraTheme.colors.TextLightSecondary,
                    modifier = Modifier.weight(0.42f)
                )
                Text(
                    block.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AynvoraTheme.colors.TextLight,
                    modifier = Modifier.weight(0.58f)
                )
            }
        }

        is ReportMetric -> Column(Modifier.padding(vertical = 3.sdp)) {
            Text(
                contentKindLabel(block.kind, resolver),
                style = MaterialTheme.typography.labelSmall,
                color = AynvoraTheme.colors.CelestialBlue
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.sdp)) {
                Text(
                    block.label.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AynvoraTheme.colors.TextLightSecondary,
                    modifier = Modifier.weight(0.55f)
                )
                Text(block.value + (block.unit?.let { " ${it.value}" } ?: ""),
                    style = MaterialTheme.typography.titleSmall,
                    color = AynvoraTheme.colors.Gold,
                    modifier = Modifier.weight(0.45f))
            }
        }

        is ReportTable -> ReportTableRenderer(block, resolver)
        is ReportInterpretation -> Card(
            colors = CardDefaults.cardColors(containerColor = AynvoraTheme.colors.CosmicBlack),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.sdp)
        ) {
            Column(Modifier.padding(10.sdp)) {
                Text(
                    contentKindLabel(ReportContentKind.INTERPRETATION, resolver),
                    style = MaterialTheme.typography.labelSmall,
                    color = AynvoraTheme.colors.CelestialBlue
                )
                Text(
                    block.text.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AynvoraTheme.colors.TextLight
                )
                Text(
                    "${resolver.text(ReportTextKey.CONTENT_SOURCE).value}: ${block.source.name} · ${
                        resolver.text(
                            ReportTextKey.TRADITION_ID
                        ).value
                    }: ${block.traditionId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AynvoraTheme.colors.TextLightSecondary
                )
                if (block.supportingEvidenceIds.isNotEmpty()) Text(
                    "${resolver.text(ReportTextKey.EVIDENCE).value}: ${block.supportingEvidenceIds.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AynvoraTheme.colors.TextLightSecondary
                )
            }
        }
    }
}

@Composable
fun ReportTableRenderer(
    table: ReportTable,
    resolver: ReportTextResolver,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().padding(vertical = 6.sdp)) {
        Text(
            contentKindLabel(table.kind, resolver),
            style = MaterialTheme.typography.labelSmall,
            color = AynvoraTheme.colors.CelestialBlue
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            LazyColumn(
                modifier = Modifier.width((132 * table.headers.size).dp).heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(1.sdp),
            ) {
                item(key = "table-header") {
                    Row {
                        table.headers.forEach { cell ->
                            Text(
                                cell.value,
                                modifier = Modifier.width(132.dp).padding(6.sdp),
                                style = MaterialTheme.typography.labelMedium,
                                color = AynvoraTheme.colors.Gold
                            )
                        }
                    }
                }
                itemsIndexed(table.rows, key = { index, _ -> index }) { _, row ->
                    Row {
                        row.forEach { cell ->
                            Text(
                                cell,
                                modifier = Modifier.width(132.dp).padding(6.sdp),
                                style = MaterialTheme.typography.bodySmall,
                                color = AynvoraTheme.colors.TextLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportEvidenceRenderer(evidence: List<ReportEvidence>, resolver: ReportTextResolver) {
    evidence.forEach { item ->
        Column(Modifier.fillMaxWidth().padding(top = 6.sdp)) {
            Text(
                "${resolver.text(ReportTextKey.CONTENT_SOURCE).value}: ${item.source.name} · ${
                    resolver.text(
                        ReportTextKey.SOURCE_ID
                    ).value
                }: ${item.source.sourceId}",
                style = MaterialTheme.typography.labelSmall,
                color = AynvoraTheme.colors.CelestialBlue
            )
            Text(
                "${resolver.text(ReportTextKey.CALCULATION_ID).value}: ${item.calculation.calculationId} · ${
                    resolver.text(
                        ReportTextKey.PROFILE
                    ).value
                }: ${item.calculation.calculationProfile} · ${resolver.text(ReportTextKey.CALCULATION_VERSION).value}: ${item.calculation.calculationVersion}",
                style = MaterialTheme.typography.bodySmall,
                color = AynvoraTheme.colors.TextLightSecondary
            )
            Text(
                "${resolver.text(ReportTextKey.REFERENCE_STATUS).value}: ${resolver.enumLabel(item.calculation.referenceStatus.name)}",
                style = MaterialTheme.typography.bodySmall,
                color = AynvoraTheme.colors.TextLightSecondary
            )
        }
    }
}

@Composable
fun ReportDisclaimerRenderer(
    document: ReportDocument,
    @Suppress("UNUSED_PARAMETER") resolver: ReportTextResolver
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AynvoraTheme.colors.CosmicNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.sdp)) {
            Text(
                document.disclaimer.title.value,
                style = MaterialTheme.typography.titleMedium,
                color = AynvoraTheme.colors.Gold
            )
            Text(
                document.disclaimer.body.value,
                style = MaterialTheme.typography.bodyMedium,
                color = AynvoraTheme.colors.TextLight
            )
        }
    }
}

@Composable
private fun contentKindLabel(kind: ReportContentKind, resolver: ReportTextResolver): String =
    resolver.text(
        when (kind) {
            ReportContentKind.FACT -> ReportTextKey.CONTENT_FACT
            ReportContentKind.CALCULATION -> ReportTextKey.CONTENT_CALCULATION
            ReportContentKind.INTERPRETATION -> ReportTextKey.CONTENT_INTERPRETATION
            ReportContentKind.SOURCE -> ReportTextKey.CONTENT_SOURCE
            ReportContentKind.USER_CONTEXT -> ReportTextKey.CONTENT_USER_CONTEXT
            ReportContentKind.DISCLAIMER -> ReportTextKey.CONTENT_DISCLAIMER
        },
    ).value
