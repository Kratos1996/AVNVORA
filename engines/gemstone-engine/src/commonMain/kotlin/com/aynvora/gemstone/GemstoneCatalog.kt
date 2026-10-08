package com.aynvora.gemstone

import com.aynvora.contracts.CelestialBody
import kotlinx.serialization.Serializable

/**
 * Category of gemstone in classical Jyotisha literature.
 */
@Serializable
enum class GemstoneCategory {
    PRIMARY_NAVARATNA,
    UPARATNA_SUBSTITUTE,
}

/**
 * Finger placement in traditional Ratna Dharana guidelines.
 */
@Serializable
enum class TraditionalFinger(val displayName: String, val sanskritName: String) {
    RING("Ring Finger", "Anamika"),
    LITTLE("Little Finger", "Kanishtha"),
    INDEX("Index Finger", "Tarjani"),
    MIDDLE("Middle Finger", "Madhyama"),
}

/**
 * Source citation metadata for traditional gemstone assertions.
 */
@Serializable
data class GemstoneSourceCitation(
    val sourceId: String,
    val title: String,
    val chapterOrSection: String,
    val quoteOrSummary: String,
)

/**
 * Rich domain metadata for an authentic gemstone.
 */
@Serializable
data class GemstoneDescriptor(
    val id: String,
    val type: GemstoneType,
    val commonName: String,
    val sanskritName: String,
    val hindiName: String,
    val primaryPlanet: CelestialBody,
    val category: GemstoneCategory,
    val mineralSpecies: String,
    val colorFamily: String,
    val hardnessMohs: Double,
    val primaryMetal: GemstoneMetal,
    val traditionalFinger: TraditionalFinger,
    val recommendedDayTime: String,
    val traditionalSignificance: String,
    val caveatsAndContraindications: List<String>,
    val sourceCitations: List<GemstoneSourceCitation>,
)

/**
 * Authoritative canonical registry of Vedic Navaratna and recognized Uparatnas.
 *
 * Source grounded in:
 * - Garuda Purana (Saroddhara / Agastya Ratna Pariksha)
 * - Brihat Samhita (Varahamihira, ch. 80-83 Ratnapariksha)
 * - Brihat Parashara Hora Shastra (Graha Sambandha)
 * - Phaladeepika (Mantreswara, ch. 2)
 */
object GemstoneCatalog {

    val NAVARATNA: List<GemstoneDescriptor> = listOf(
        GemstoneDescriptor(
            id = "gem_ruby",
            type = GemstoneType.RUBY,
            commonName = "Ruby",
            sanskritName = "Manikya",
            hindiName = "माणिक्य",
            primaryPlanet = CelestialBody.SUN,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Natural Corundum",
            colorFamily = "Pigeon Blood Red / Deep Crimson",
            hardnessMohs = 9.0,
            primaryMetal = GemstoneMetal.GOLD,
            traditionalFinger = TraditionalFinger.RING,
            recommendedDayTime = "Sunday morning during Shukla Paksha (waxing moon) at sunrise",
            traditionalSignificance = "Symbol of solar vitality, self-confidence, leadership, and soul authority (Atmakaraka).",
            caveatsAndContraindications = listOf(
                "Never wear alongside Blue Sapphire (Saturn), Diamond (Venus), or Hessonite (Rahu) without explicit chart qualification.",
                "Contraindicated if Sun is functional malefic (Lord of 6th, 8th, or 12th house).",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "garuda_puran_ratna",
                    title = "Garuda Purana",
                    chapterOrSection = "Saroddhara, Ratna Pariksha",
                    quoteOrSummary = "Ruby is the jewel of the Sun; clear, radiant, with the deep luster of the lotus or pomegranate seed.",
                ),
                GemstoneSourceCitation(
                    sourceId = "brihat_samhita_80",
                    title = "Brihat Samhita",
                    chapterOrSection = "Adhyaya 80 (Ratnapariksha)",
                    quoteOrSummary = "Manikya born of the Sun imparts royal authority, health, and destroys ignorance.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_pearl",
            type = GemstoneType.NATURAL_PEARL,
            commonName = "Natural Pearl",
            sanskritName = "Mukta",
            hindiName = "मोती",
            primaryPlanet = CelestialBody.MOON,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Organic (Calcium Carbonate / Nacre)",
            colorFamily = "Lustrous White / Silvery Cream",
            hardnessMohs = 3.5,
            primaryMetal = GemstoneMetal.SILVER,
            traditionalFinger = TraditionalFinger.LITTLE,
            recommendedDayTime = "Monday evening during Shukla Paksha at moonrise",
            traditionalSignificance = "Symbol of lunar tranquility, emotional equanimity, mental clarity, and maternal peace.",
            caveatsAndContraindications = listOf(
                "Do not combine with Hessonite (Rahu) or Cat's Eye (Ketu), which cause Eclipse (Grahana) afflictions.",
                "Avoid pairing with Blue Sapphire to prevent Vish Yoga mental agitation.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "garuda_puran_ratna",
                    title = "Garuda Purana",
                    chapterOrSection = "Saroddhara, Mukta Pariksha",
                    quoteOrSummary = "Pearls of pristine luster calm the mind and soothe sorrow, reflecting the gentle rays of Chandra.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_red_coral",
            type = GemstoneType.RED_CORAL,
            commonName = "Red Coral",
            sanskritName = "Moonga",
            hindiName = "मूँगा",
            primaryPlanet = CelestialBody.MARS,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Organic (Corallium rubrum)",
            colorFamily = "Deep Red / Vermilion Orange",
            hardnessMohs = 3.5,
            primaryMetal = GemstoneMetal.COPPER,
            traditionalFinger = TraditionalFinger.RING,
            recommendedDayTime = "Tuesday morning within one hour after sunrise",
            traditionalSignificance = "Symbol of martial courage, physical stamina, initiative, and protective resilience.",
            caveatsAndContraindications = listOf(
                "Never wear together with Emerald (Mercury) or Blue Sapphire (Saturn) due to mutual planetary enmity.",
                "Contraindicated if Mars is a functional malefic creating intense temper or aggressive impulsivity.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "phaladeepika_2",
                    title = "Phaladeepika",
                    chapterOrSection = "Adhyaya 2, Sloka 21",
                    quoteOrSummary = "Vidruma (Coral) belongs to Mangala, granting courage, strength of blood, and land stewardship.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_emerald",
            type = GemstoneType.EMERALD,
            commonName = "Emerald",
            sanskritName = "Marakata",
            hindiName = "पन्ना",
            primaryPlanet = CelestialBody.MERCURY,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Natural Beryl",
            colorFamily = "Lush Vivid Green",
            hardnessMohs = 7.5,
            primaryMetal = GemstoneMetal.GOLD,
            traditionalFinger = TraditionalFinger.LITTLE,
            recommendedDayTime = "Wednesday morning during Shukla Paksha",
            traditionalSignificance = "Symbol of mercurial intellect (Buddhi), commercial acumen, eloquence, and analytical discernment.",
            caveatsAndContraindications = listOf(
                "Do not combine with Red Coral (Mars) due to sharp planetary conflict between Budha and Mangala.",
                "Do not wear with Natural Pearl if Moon and Mercury are mutually inimical in the birth chart.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "brihat_samhita_81",
                    title = "Brihat Samhita",
                    chapterOrSection = "Adhyaya 81 (Marakata Pariksha)",
                    quoteOrSummary = "Marakata of vivid parrot-green luster bestows wisdom, diplomatic eloquence, and business prosperity.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_yellow_sapphire",
            type = GemstoneType.YELLOW_SAPPHIRE,
            commonName = "Yellow Sapphire",
            sanskritName = "Pukhraj",
            hindiName = "पुखराज",
            primaryPlanet = CelestialBody.JUPITER,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Natural Corundum",
            colorFamily = "Golden Canary Yellow",
            hardnessMohs = 9.0,
            primaryMetal = GemstoneMetal.GOLD,
            traditionalFinger = TraditionalFinger.INDEX,
            recommendedDayTime = "Thursday morning during Shukla Paksha at sunrise",
            traditionalSignificance = "Symbol of spiritual wisdom, divine grace (Guru Kripa), righteous wealth, and scholarly discernment.",
            caveatsAndContraindications = listOf(
                "Classical contraindication against wearing simultaneously with Diamond (Venus), as Brihaspati (Deva Guru) and Shukracharya (Asura Guru) represent opposing spiritual philosophies.",
                "Avoid pairing with Blue Sapphire (Saturn) without deep astrological verification.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "garuda_puran_ratna",
                    title = "Garuda Purana",
                    chapterOrSection = "Saroddhara, Pushparaga Pariksha",
                    quoteOrSummary = "Pushparaga is holy to Guru; it gives knowledge, virtues, progeny, and destroys misfortunes.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_diamond",
            type = GemstoneType.DIAMOND,
            commonName = "Diamond",
            sanskritName = "Vajra",
            hindiName = "हीरा",
            primaryPlanet = CelestialBody.VENUS,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Natural Diamond (Crystalline Carbon)",
            colorFamily = "Colorless / White Brilliance",
            hardnessMohs = 10.0,
            primaryMetal = GemstoneMetal.SILVER,
            traditionalFinger = TraditionalFinger.MIDDLE,
            recommendedDayTime = "Friday morning during Shukla Paksha",
            traditionalSignificance = "Symbol of refined aesthetics, artistic beauty, marital harmony, and aesthetic refinement.",
            caveatsAndContraindications = listOf(
                "Never wear with Ruby (Sun), Natural Pearl (Moon), or Yellow Sapphire (Jupiter) due to classical planetary enmity.",
                "Ensure stone has no structural flaws, inclusions (Doshas), or broken facets.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "brihat_samhita_80",
                    title = "Brihat Samhita",
                    chapterOrSection = "Adhyaya 80 (Vajra Pariksha)",
                    quoteOrSummary = "Vajra is supreme in hardness and brilliance, belonging to Shukra; it bestows luxury and victory.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_blue_sapphire",
            type = GemstoneType.BLUE_SAPPHIRE,
            commonName = "Blue Sapphire",
            sanskritName = "Neelam",
            hindiName = "नीलम",
            primaryPlanet = CelestialBody.SATURN,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Natural Corundum",
            colorFamily = "Royal Cornflower Blue / Deep Velvet Blue",
            hardnessMohs = 9.0,
            primaryMetal = GemstoneMetal.PANCHADHATU,
            traditionalFinger = TraditionalFinger.MIDDLE,
            recommendedDayTime = "Saturday twilight or sunset during Krishna Paksha or Saturday morning",
            traditionalSignificance = "Symbol of karmic discipline, enduring perseverance, spiritual detachment, and swift justice.",
            caveatsAndContraindications = listOf(
                "Highest sensitivity gem in Jyotisha: traditional trial period of 3 days under pillow is recommended to observe dreams and events.",
                "Strictly incompatible with Ruby (Sun), Pearl (Moon), and Red Coral (Mars).",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "phaladeepika_2",
                    title = "Phaladeepika",
                    chapterOrSection = "Adhyaya 2, Sloka 21",
                    quoteOrSummary = "Neela belongs to Shani; its effects are swift and decisive, requiring great purity of intention.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_hessonite",
            type = GemstoneType.HESSONITE,
            commonName = "Hessonite Garnet",
            sanskritName = "Gomed",
            hindiName = "गोमेद",
            primaryPlanet = CelestialBody.RAHU,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Grossular Garnet",
            colorFamily = "Cinnamon Honey Red / Brownish Orange",
            hardnessMohs = 7.25,
            primaryMetal = GemstoneMetal.ASHTADHATU,
            traditionalFinger = TraditionalFinger.MIDDLE,
            recommendedDayTime = "Saturday evening or Wednesday night",
            traditionalSignificance = "Symbol of clarity amidst worldly illusions (Maya), shadow integration, and unconventional innovation.",
            caveatsAndContraindications = listOf(
                "Never wear with Ruby (Sun) or Pearl (Moon) to avoid triggering planetary eclipse dynamics.",
                "Only prescribed during specific Rahu Mahadasha or Antardasha when Rahu acts favorably.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "garuda_puran_ratna",
                    title = "Garuda Purana",
                    chapterOrSection = "Saroddhara, Gomedaka Pariksha",
                    quoteOrSummary = "Gomed possesses the deep color of pure honey or cow's urine; it dissipates the deceptions of Rahu.",
                ),
            ),
        ),
        GemstoneDescriptor(
            id = "gem_cats_eye",
            type = GemstoneType.CATS_EYE,
            commonName = "Cat's Eye Chrysoberyl",
            sanskritName = "Vaidurya",
            hindiName = "लहसुनिया",
            primaryPlanet = CelestialBody.KETU,
            category = GemstoneCategory.PRIMARY_NAVARATNA,
            mineralSpecies = "Chrysoberyl (Chatoyant)",
            colorFamily = "Golden Greenish Yellow with Sharp Chatoyant Band",
            hardnessMohs = 8.5,
            primaryMetal = GemstoneMetal.ASHTADHATU,
            traditionalFinger = TraditionalFinger.MIDDLE,
            recommendedDayTime = "Thursday midnight or Tuesday twilight",
            traditionalSignificance = "Symbol of spiritual liberation (Moksha), intuitive perception, and shielding against unseen influences.",
            caveatsAndContraindications = listOf(
                "Incompatible with Ruby (Sun) and Pearl (Moon).",
                "Chatoyancy band must be sharp, continuous, and unbroken for auspicious traditional use.",
            ),
            sourceCitations = listOf(
                GemstoneSourceCitation(
                    sourceId = "brihat_samhita_82",
                    title = "Brihat Samhita",
                    chapterOrSection = "Adhyaya 82 (Vaidurya Pariksha)",
                    quoteOrSummary = "Vaidurya with the moving white thread like a cat's eye is Ketu's stone, warding off hidden perils.",
                ),
            ),
        ),
    )

    fun findByType(type: GemstoneType): GemstoneDescriptor =
        NAVARATNA.first { it.type == type }

    fun findById(id: String): GemstoneDescriptor? =
        NAVARATNA.firstOrNull { it.id == id }

    fun forPlanet(planet: CelestialBody): GemstoneDescriptor? =
        NAVARATNA.firstOrNull { it.primaryPlanet == planet }
}
