package com.aynvora.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraSpacing
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraLogo
import com.aynvora.designsystem.components.AynvoraLogoVariant

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp

data class AynvoraSidebarItem(
    val id: String,
    val title: String,
    val iconGlyph: String,
    val featureId: CoreFeatureId? = null,
    val category: String? = null,
)

/**
 * Information-Dense Persistent Sidebar Navigation for Desktop applications.
 */
@Composable
fun AynvoraDesktopSidebar(
    items: List<AynvoraSidebarItem>,
    selectedItemId: String,
    onItemSelected: (AynvoraSidebarItem) -> Unit,
    modifier: Modifier = Modifier,
    onLanguageClick: () -> Unit = {},
    onThemeToggleClick: () -> Unit = {},
    isDark: Boolean = true,
    currentLanguageName: String = "English",
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(AynvoraColors.CosmicNavy)
            .border(
                width = 1.dp,
                color = AynvoraColors.CosmicIndigo,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            )
            .padding(AynvoraSpacing.space16),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space8),
                modifier = Modifier.padding(bottom = AynvoraSpacing.space16),
            ) {
                AynvoraLogo(
                    size = 32.dp,
                    variant = if (isDark) AynvoraLogoVariant.Transparent else AynvoraLogoVariant.Default,
                )
                Text(
                    text = "AYNVORA",
                    style = AynvoraTheme.typography.title18.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.ssp,
                    ),
                    color = AynvoraColors.Gold,
                )
            }

            // Navigation Items List with Category Sections
            var currentCategory: String? = null
            items.forEach { item ->
                if (item.category != null && item.category != currentCategory) {
                    currentCategory = item.category
                    Text(
                        text = currentCategory.uppercase(),
                        style = AynvoraTheme.typography.caption12.copy(
                            fontSize = 10.ssp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        ),
                        color = AynvoraColors.Gold.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
                    )
                }

                val isSelected = item.id == selectedItemId
                val bg = if (isSelected) AynvoraColors.Gold.copy(alpha = 0.18f) else Color.Transparent
                val border = if (isSelected) AynvoraColors.Gold else Color.Transparent
                val textColor = if (isSelected) AynvoraColors.Gold else AynvoraColors.TextLightSecondary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(AynvoraShapes.shape8)
                        .background(bg)
                        .border(width = 1.dp, color = border, shape = AynvoraShapes.shape8)
                        .clickable { onItemSelected(item) }
                        .padding(horizontal = AynvoraSpacing.space10),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space10),
                    ) {
                        Text(
                            text = item.iconGlyph,
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        )
                        Text(
                            text = item.title,
                            style = AynvoraTheme.typography.body14.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.5.ssp,
                            ),
                            color = textColor,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }

        // Bottom Controls: Language & Theme
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AynvoraSpacing.space8),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(AynvoraShapes.shape8)
                    .background(AynvoraColors.CosmicBlack)
                    .border(1.dp, AynvoraColors.CosmicIndigo, AynvoraShapes.shape8)
                    .clickable { onLanguageClick() }
                    .padding(horizontal = AynvoraSpacing.space10),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space6),
                ) {
                    Text("🌐", style = AynvoraTheme.typography.caption12)
                    Text(
                        text = currentLanguageName,
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                        color = AynvoraColors.TextLight,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(AynvoraShapes.shape8)
                    .background(AynvoraColors.CosmicBlack)
                    .border(1.dp, AynvoraColors.CosmicIndigo, AynvoraShapes.shape8)
                    .clickable { onThemeToggleClick() }
                    .padding(horizontal = AynvoraSpacing.space10),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AynvoraSpacing.space6),
                ) {
                    Text(if (isDark) "🌙" else "☀️", style = AynvoraTheme.typography.caption12)
                    Text(
                        text = if (isDark) "Dark Theme" else "Light Theme",
                        style = AynvoraTheme.typography.caption12.copy(fontSize = 12.ssp),
                        color = AynvoraColors.TextLight,
                    )
                }
            }
        }
    }
}
