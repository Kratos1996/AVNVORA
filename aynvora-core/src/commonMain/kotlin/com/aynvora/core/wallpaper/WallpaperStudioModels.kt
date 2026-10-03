package com.aynvora.core.wallpaper

import kotlinx.serialization.Serializable

@Serializable
enum class WallpaperAspectRatio(val displayName: String, val widthPx: Int, val heightPx: Int) {
    PORTRAIT_9_16("Mobile Portrait (9:16)", 1080, 1920),
    LANDSCAPE_16_9("Desktop Widescreen (16:9)", 2560, 1440),
    TABLET_4_3("Tablet Standard (4:3)", 2048, 1536),
    SQUARE_1_1("Square Avatar / Tile (1:1)", 1080, 1080),
}

@Serializable
data class WallpaperTemplate(
    val id: String,
    val title: String,
    val tradition: String,
    val category: String,
    val palette: List<String>,
    val description: String,
    val sacredMantra: String? = null,
    val previewGlyph: String = "✨",
)

@Serializable
data class WallpaperGenerationRequest(
    val templateId: String,
    val aspectRatio: WallpaperAspectRatio = WallpaperAspectRatio.PORTRAIT_9_16,
    val primaryColorHex: String = "#D4AF37",
    val secondaryColorHex: String = "#121829",
    val includeMantra: Boolean = true,
    val selectedMantra: String? = null,
    val targetPlatform: String = "MOBILE",
)

@Serializable
data class WallpaperSpecification(
    val templateId: String,
    val aspectRatio: WallpaperAspectRatio,
    val canvasWidthPx: Int,
    val canvasHeightPx: Int,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val includeMantra: Boolean,
    val sacredMantra: String?,
    val safeAreaMarginDp: Int,
    val isAiGenerated: Boolean = false,
    val renderingEngine: String = "COMPOSE_VECTOR_CANVAS",
    val disclaimer: String = "Structured local vector wallpaper configuration. No remote AI image generation claimed.",
)

object DefaultWallpaperStudioService {

    private val templates = listOf(
        WallpaperTemplate(
            id = "sri_chakra",
            title = "Sri Chakra Mandala",
            tradition = "Tantric Vedic Geometry",
            category = "Sacred Geometry",
            palette = listOf("#D4AF37", "#121829", "#8E24AA", "#FFD54F"),
            description = "Nine interlocking triangles radiating from supreme Bindu, surrounded by concentric petals.",
            sacredMantra = "Om Shreem Hreem Shreem Kamale Kamalalaye Praseed Praseed",
            previewGlyph = "☸️",
        ),
        WallpaperTemplate(
            id = "surya_mandala",
            title = "Surya Aditya Sunburst",
            tradition = "Solar Vedic",
            category = "Cosmic Constellation",
            palette = listOf("#FF9800", "#FFD54F", "#D84315", "#1A0900"),
            description = "12 radiating solar rays representing the Dwadasha Adityas radiating cosmic vitality.",
            sacredMantra = "Om Hram Hreem Hroum Sah Suryaya Namah",
            previewGlyph = "☀️",
        ),
        WallpaperTemplate(
            id = "navagraha_mandala",
            title = "Navagraha Planetary Harmony",
            tradition = "Classical Jyotish",
            category = "Astrological Mandala",
            palette = listOf("#9C27B0", "#3F51B5", "#009688", "#FFC107"),
            description = "Concentric nine planetary spheres balanced around the cosmic center.",
            sacredMantra = "Om Namah Suryaya Chandraya Mangalaya Budhaya Cha",
            previewGlyph = "🪐",
        ),
        WallpaperTemplate(
            id = "om_pranava",
            title = "Om Pranava Geometry",
            tradition = "Upanishadic",
            category = "Sacred Symbol",
            palette = listOf("#D4AF37", "#0D111A", "#3E2723", "#FFF8E1"),
            description = "The primordial sound OM centered in cosmic concentric rings and aura waves.",
            sacredMantra = "Om Shanti Shanti Shanti",
            previewGlyph = "🕉️",
        ),
        WallpaperTemplate(
            id = "sahasrara_lotus",
            title = "Sahasrara Thousand Petals",
            tradition = "Kundalini Yoga",
            category = "Chakra Art",
            palette = listOf("#7B1FA2", "#BA68C8", "#EDE7F6", "#12002B"),
            description = "Crown chakra lotus blossoming in thousand points of subtle spiritual light.",
            sacredMantra = "Om Aim Hreem Kleem Chamundayai Vichche",
            previewGlyph = "🪷",
        ),
    )

    fun getTemplates(): List<WallpaperTemplate> = templates

    fun findTemplateById(id: String): WallpaperTemplate? = templates.firstOrNull { it.id == id }

    fun generateSpecification(request: WallpaperGenerationRequest): WallpaperSpecification {
        val template = findTemplateById(request.templateId) ?: templates.first()
        val mantra = if (request.includeMantra) {
            request.selectedMantra ?: template.sacredMantra
        } else null

        val marginDp = when (request.aspectRatio) {
            WallpaperAspectRatio.PORTRAIT_9_16 -> 64
            WallpaperAspectRatio.LANDSCAPE_16_9 -> 48
            WallpaperAspectRatio.TABLET_4_3 -> 56
            WallpaperAspectRatio.SQUARE_1_1 -> 40
        }

        return WallpaperSpecification(
            templateId = template.id,
            aspectRatio = request.aspectRatio,
            canvasWidthPx = request.aspectRatio.widthPx,
            canvasHeightPx = request.aspectRatio.heightPx,
            primaryColorHex = request.primaryColorHex,
            secondaryColorHex = request.secondaryColorHex,
            includeMantra = request.includeMantra,
            sacredMantra = mantra,
            safeAreaMarginDp = marginDp,
            isAiGenerated = false,
            renderingEngine = "COMPOSE_VECTOR_CANVAS",
        )
    }
}
