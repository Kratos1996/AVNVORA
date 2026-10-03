package com.aynvora.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp

/**
 * Information-dense Desktop & Tablet Data Table component.
 * Supports sticky headers, dense row padding, and crisp border grid lines.
 */
@Composable
fun AynvoraDesktopDataTable(
    columns: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AynvoraColors.CosmicIndigo,
                shape = AynvoraShapes.shape8,
            )
            .background(AynvoraColors.CosmicNavy, shape = AynvoraShapes.shape8)
            .padding(AynvoraSpacing.space12),
    ) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = AynvoraTheme.typography.title18.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.ssp,
                ),
                color = AynvoraColors.Gold,
                modifier = Modifier.padding(bottom = AynvoraSpacing.space8),
            )
        }

        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AynvoraColors.CosmicBlack)
                .padding(vertical = AynvoraSpacing.space6, horizontal = AynvoraSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            columns.forEach { col ->
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = col,
                        style = AynvoraTheme.typography.caption12.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.ssp,
                        ),
                        color = AynvoraColors.Gold,
                    )
                }
            }
        }

        // Data Rows
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(rows) { index, row ->
                val rowBg = if (index % 2 == 0) AynvoraColors.CosmicNavy else AynvoraColors.CosmicBlack.copy(alpha = 0.4f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(vertical = AynvoraSpacing.space6, horizontal = AynvoraSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { cell ->
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cell,
                                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                                color = AynvoraColors.TextLightSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
