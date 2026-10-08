package com.aynvora.gemstone

import com.aynvora.contracts.CelestialBody
import kotlinx.serialization.Serializable

/**
 * Directional cardinal and intercardinal positioning of the Navaratna layout.
 * Traditionally aligned according to classical Agastya/Garuda Purana Ratnapariksha mandalas.
 */
@Serializable
enum class NavaratnaDirection(val displayName: String, val sanskritName: String) {
    CENTER("Center", "Madhya"),
    EAST("East", "Purva"),
    SOUTH_EAST("South-East", "Agneya"),
    SOUTH("South", "Dakshina"),
    SOUTH_WEST("South-West", "Nairritya"),
    WEST("West", "Pashchima"),
    NORTH_WEST("North-West", "Vayavya"),
    NORTH("North", "Uttara"),
    NORTH_EAST("North-East", "Ishanya"),
}

/**
 * Single cell in the 9-gem Navaratna Mandala.
 */
@Serializable
data class NavaratnaCell(
    val direction: NavaratnaDirection,
    val planet: CelestialBody,
    val gemstoneType: GemstoneType,
    val sanskritName: String,
    val englishName: String,
    val colorHex: String,
    val gridRow: Int,
    val gridCol: Int,
)

/**
 * Deterministic Navaratna Mandala arrangement.
 *
 * Classical layout (North-up):
 * [North-West: Pearl]    [North: Emerald]         [North-East: Yellow Sapphire]
 * [West: Blue Sapphire]  [Center: Ruby]           [East: Cat's Eye / Sun Axis]
 * [South-West: Hessonite][South: Red Coral]       [South-East: Diamond]
 */
object NavaratnaMatrix {

    val CELLS: List<NavaratnaCell> = listOf(
        NavaratnaCell(
            direction = NavaratnaDirection.NORTH_WEST,
            planet = CelestialBody.MOON,
            gemstoneType = GemstoneType.NATURAL_PEARL,
            sanskritName = "Mukta",
            englishName = "Pearl",
            colorHex = "#F5F5F5",
            gridRow = 0,
            gridCol = 0,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.NORTH,
            planet = CelestialBody.MERCURY,
            gemstoneType = GemstoneType.EMERALD,
            sanskritName = "Marakata",
            englishName = "Emerald",
            colorHex = "#2E7D32",
            gridRow = 0,
            gridCol = 1,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.NORTH_EAST,
            planet = CelestialBody.JUPITER,
            gemstoneType = GemstoneType.YELLOW_SAPPHIRE,
            sanskritName = "Pukhraj",
            englishName = "Yellow Sapphire",
            colorHex = "#FBC02D",
            gridRow = 0,
            gridCol = 2,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.WEST,
            planet = CelestialBody.SATURN,
            gemstoneType = GemstoneType.BLUE_SAPPHIRE,
            sanskritName = "Neelam",
            englishName = "Blue Sapphire",
            colorHex = "#1565C0",
            gridRow = 1,
            gridCol = 0,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.CENTER,
            planet = CelestialBody.SUN,
            gemstoneType = GemstoneType.RUBY,
            sanskritName = "Manikya",
            englishName = "Ruby",
            colorHex = "#C62828",
            gridRow = 1,
            gridCol = 1,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.EAST,
            planet = CelestialBody.KETU,
            gemstoneType = GemstoneType.CATS_EYE,
            sanskritName = "Vaidurya",
            englishName = "Cat's Eye",
            colorHex = "#9E9D24",
            gridRow = 1,
            gridCol = 2,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.SOUTH_WEST,
            planet = CelestialBody.RAHU,
            gemstoneType = GemstoneType.HESSONITE,
            sanskritName = "Gomed",
            englishName = "Hessonite",
            colorHex = "#D84315",
            gridRow = 2,
            gridCol = 0,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.SOUTH,
            planet = CelestialBody.MARS,
            gemstoneType = GemstoneType.RED_CORAL,
            sanskritName = "Moonga",
            englishName = "Red Coral",
            colorHex = "#E53935",
            gridRow = 2,
            gridCol = 1,
        ),
        NavaratnaCell(
            direction = NavaratnaDirection.SOUTH_EAST,
            planet = CelestialBody.VENUS,
            gemstoneType = GemstoneType.DIAMOND,
            sanskritName = "Vajra",
            englishName = "Diamond",
            colorHex = "#E0F7FA",
            gridRow = 2,
            gridCol = 2,
        ),
    )

    fun getCellForGem(gemstoneType: GemstoneType): NavaratnaCell? =
        CELLS.firstOrNull { it.gemstoneType == gemstoneType }
}
