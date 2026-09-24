package com.aynvora.core.report

import com.aynvora.core.feature.CanonicalCoreFeatures
import com.aynvora.core.feature.CoreFeatureId
import com.aynvora.core.feature.FeatureAvailability

data class ReportTypeDescriptor(
    val type: ReportType,
    val titleKey: ReportTextKey,
)

/** Canonical catalog of independently selectable reports and their owning core features. */
object ReportTypeRegistry {
    private val descriptors = listOf(
        ReportTypeDescriptor(ReportType.KUNDALI, ReportTextKey.KUNDALI_TITLE),
        ReportTypeDescriptor(ReportType.GEMSTONE, ReportTextKey.GEMSTONE_TITLE),
        ReportTypeDescriptor(ReportType.NUMEROLOGY, ReportTextKey.NUMEROLOGY_TITLE),
        ReportTypeDescriptor(ReportType.RUDRAKSHA, ReportTextKey.RUDRAKSHA_TITLE),
        ReportTypeDescriptor(ReportType.JADI, ReportTextKey.JADI_TITLE),
        ReportTypeDescriptor(ReportType.YANTRA, ReportTextKey.YANTRA_TITLE),
        ReportTypeDescriptor(ReportType.PALMISTRY, ReportTextKey.PALMISTRY_TITLE),
        ReportTypeDescriptor(ReportType.TAROT, ReportTextKey.TAROT_TITLE),
        ReportTypeDescriptor(ReportType.GITA, ReportTextKey.GITA_TITLE),
        ReportTypeDescriptor(ReportType.LAL_KITAB, ReportTextKey.LAL_KITAB_TITLE),
        ReportTypeDescriptor(ReportType.GARUDA_PURAN, ReportTextKey.GARUDA_PURAN_TITLE),
        ReportTypeDescriptor(ReportType.DAILY_GUIDANCE, ReportTextKey.DAILY_GUIDANCE_TITLE),
    )

    fun all(): List<ReportTypeDescriptor> = descriptors.toList()

    fun find(id: String): ReportTypeDescriptor? = descriptors.firstOrNull { it.type.id == id }

    /** Report availability reflects the authoritative feature registry, not an assumed report implementation. */
    fun featureStatus(feature: CoreFeatureId): ReportFeatureStatus {
        val availability = CanonicalCoreFeatures.firstOrNull { it.id == feature }?.availability
        return when (availability) {
            is FeatureAvailability.ComingSoon, is FeatureAvailability.ConfigurationRequired -> ReportFeatureStatus.FOUNDATION_ONLY
            is FeatureAvailability.UpdateRequired, is FeatureAvailability.UnsupportedOnPlatform -> ReportFeatureStatus.LIMITED
            else -> ReportFeatureStatus.IMPLEMENTED
        }
    }
}
