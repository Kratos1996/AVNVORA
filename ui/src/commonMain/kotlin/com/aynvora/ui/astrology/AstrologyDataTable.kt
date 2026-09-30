package com.aynvora.ui.astrology

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.models.AstroTableCell
import com.aynvora.core.models.AstroTableColumn
import com.aynvora.core.models.AstroTableSnapshot
import com.aynvora.core.models.AstroValueType
import com.aynvora.designsystem.AynvoraTheme

/** Formats typed snapshot cells only; the table never derives astrology values. */
object AstrologyTableFormatter {
    fun format(cell: AstroTableCell, column: AstroTableColumn): String = when (cell.semanticType) {
        AstroValueType.ANGLE -> cell.numericValue?.let { "%.3f°".format(it) } ?: cell.textValue.orEmpty().ifEmpty { "—" }
        AstroValueType.INTEGER -> cell.integerValue?.toString() ?: cell.numericValue?.toInt()?.toString() ?: "—"
        AstroValueType.BOOLEAN -> cell.canonicalId?.let {
            when (it.lowercase()) { "true" -> "Yes"; "false" -> "No"; else -> it }
        } ?: "—"
        AstroValueType.BODY, AstroValueType.SIGN, AstroValueType.NAKSHATRA, AstroValueType.ENUM -> cell.canonicalId?.let(::humanize) ?: cell.textValue.orEmpty().ifEmpty { "—" }
        AstroValueType.TEXT -> cell.textValue ?: cell.canonicalId ?: "—"
    }

    private fun humanize(value: String) = value.lowercase().replace('_', ' ').split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
}

@Composable
fun AstrologyDataTable(table: AstroTableSnapshot, modifier: Modifier = Modifier) {
    val ink = AynvoraTheme.colors.TextLight
    val muted = AynvoraTheme.colors.TextLightSecondary
    Surface(modifier.fillMaxWidth(), color = AynvoraTheme.colors.CosmicIndigo.copy(alpha = .72f), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
            Row(Modifier.width((table.columns.size * 122).dp).background(AynvoraTheme.colors.Gold.copy(alpha = .16f))) {
                table.columns.forEach { column ->
                    Text(
                        AstrologyTableFormatter.formatHeader(column.labelKey),
                        Modifier.width(122.dp).padding(horizontal = 8.dp, vertical = 10.dp),
                        color = AynvoraTheme.colors.GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                    )
                }
            }
            table.rows.forEachIndexed { rowIndex, row ->
                Row(
                    Modifier.width((table.columns.size * 122).dp)
                        .background(if (rowIndex % 2 == 0) AynvoraTheme.colors.CosmicIndigo.copy(alpha = .72f) else AynvoraTheme.colors.CosmicBlack.copy(alpha = .25f))
                        .semantics { contentDescription = row.cells.mapIndexed { i, cell ->
                            val column = table.columns.getOrNull(i)
                            "${column?.let { AstrologyTableFormatter.formatHeader(it.labelKey) } ?: "Value"}: ${column?.let { AstrologyTableFormatter.format(cell, it) } ?: "—"}"
                        }.joinToString(", ") },
                ) {
                    table.columns.forEachIndexed { index, column ->
                        val cell = row.cells.getOrNull(index)
                        Text(
                            cell?.let { AstrologyTableFormatter.format(it, column) } ?: "—",
                            Modifier.width(122.dp).padding(horizontal = 8.dp, vertical = 9.dp),
                            color = ink, fontSize = 12.sp, maxLines = 2,
                        )
                    }
                }
            }
            if (table.rows.isEmpty()) Text("No rows available", Modifier.padding(12.dp), color = muted, fontSize = 12.sp)
        }
    }
}

private fun AstrologyTableFormatter.formatHeader(key: String): String = key.substringAfterLast('.').replace('_', ' ').split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
