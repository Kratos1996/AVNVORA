package com.aynvora.ui.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.components.AynvoraButton
import com.aynvora.designsystem.components.AynvoraButtonVariant
import com.aynvora.designsystem.components.AynvoraCard
import com.aynvora.designsystem.components.AynvoraCardVariant

/**
 * Core Product Dashboard displaying the 10 first-class features with typed availability badges.
 */
@Composable
fun CoreFeatureDashboard(
    onFeatureSelected: (CoreFeatureId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.sdp),
    ) {
        items(CanonicalCoreFeatures, key = { it.id.name }) { feature ->
            AynvoraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = AynvoraCardVariant.Elevated,
                containerColor = AynvoraTheme.colors.CosmicNavy,
                contentColor = AynvoraTheme.colors.TextLight,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = feature.titleKey,
                            style = AynvoraTheme.typography.title18.copy(fontSize = 18.ssp),
                            color = AynvoraTheme.colors.Gold,
                        )

                        val badgeText = when (val avail = feature.availability) {
                            is FeatureAvailability.Available -> "AVAILABLE"
                            is FeatureAvailability.ComingSoon -> "FOUNDATION READY"
                            is FeatureAvailability.OfflineAvailable -> "OFFLINE"
                            is FeatureAvailability.ConfigurationRequired -> "SETUP"
                            is FeatureAvailability.UpdateRequired -> "UPDATE"
                            is FeatureAvailability.UnsupportedOnPlatform -> "UNSUPPORTED"
                        }

                        val badgeColor = when (feature.availability) {
                            is FeatureAvailability.Available -> AynvoraTheme.colors.CelestialBlue
                            else -> AynvoraTheme.colors.GoldLight
                        }

                        Text(
                            text = badgeText,
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = badgeColor,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.sdp))

                    Text(
                        text = feature.subtitleKey,
                        style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                        color = AynvoraTheme.colors.TextLightSecondary,
                    )

                    Spacer(modifier = Modifier.height(12.sdp))

                    AynvoraButton(
                        text = when (feature.availability) {
                            is FeatureAvailability.Available -> "Open Feature"
                            else -> "View Foundation"
                        },
                        variant = when (feature.availability) {
                            is FeatureAvailability.Available -> AynvoraButtonVariant.Primary
                            else -> AynvoraButtonVariant.Outlined
                        },
                        onClick = { onFeatureSelected(feature.id) },
                    )
                }
            }
        }
    }
}
