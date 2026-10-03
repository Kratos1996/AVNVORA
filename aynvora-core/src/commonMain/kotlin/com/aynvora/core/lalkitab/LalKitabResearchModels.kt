package com.aynvora.core.lalkitab

import kotlinx.serialization.Serializable

/**
 * AYNVORA Lal Kitab Domain — Research & Educational Models
 *
 * STATUS: RESEARCH_ONLY
 *
 * Lal Kitab is a distinct branch of 20th-century folk/Urdu astrology, published between
 * 1939 and 1952 by Pt. Roop Chand Joshi. It differs fundamentally from Parashari Jyotish:
 * - Houses (Bhavas) are fixed with Aries as House 1; Rashi signs are not rotated with Lagna.
 * - Planetary aspects follow unique sight rules rather than classical Parashari Drishtis.
 * - Introduces artificial planets (Masnui Graha), blind horoscopes (Andha Kundali), and sleeping houses (Soe Hue Ghar).
 *
 * STRICT PRODUCT POLICY:
 * - Kept strictly as RESEARCH_ONLY.
 * - NEVER mixed automatically with classical Parashari calculations or charts.
 * - Unsupported destructive or superstition remedies are strictly excluded.
 */

@Serializable
data class LalKitabResearchTopic(
    val id: String,
    val title: String,
    val category: String, // "PHILOSOPHY", "HOUSE_SYSTEM", "MASNUI_GRAHA", "SOE_GHAR", "FARMAN"
    val description: String,
    val sourceReference: String,
    val differenceFromParashari: String,
)

@Serializable
data class LalKitabTraditionOverview(
    val title: String = "Lal Kitab Astrological Tradition (Research Overview)",
    val historicalOrigin: String,
    val primaryTexts: List<String>,
    val author: String,
    val corePrinciples: List<String>,
    val supportedResearchTopics: List<LalKitabResearchTopic>,
    val unsupportedProductionMethods: List<String>,
    val rightsStatus: String = "HISTORICAL_TEXTS_RESEARCH_FAIR_USE",
    val status: String = "RESEARCH_ONLY",
    val researchDisclaimer: String = LAL_KITAB_DISCLAIMER,
)

const val LAL_KITAB_DISCLAIMER =
    "Lal Kitab material is provided exclusively for historical, comparative, and academic research. " +
            "It is NOT verified production astrology and is never combined with Parashari calculations. " +
            "AYNVORA strictly excludes folk remedies claiming miraculous cures, harm, or structural destruction. " +
            "Use for scholarly understanding of 20th-century vernacular astrological systems only."

interface LalKitabResearchRepository {
    suspend fun getTraditionOverview(): LalKitabTraditionOverview
    suspend fun getTopicById(id: String): LalKitabResearchTopic?
}

class DefaultLalKitabResearchRepository : LalKitabResearchRepository {

    private val topics = listOf(
        LalKitabResearchTopic(
            id = "lk_fixed_houses",
            title = "Fixed House-Sign Identification (Pakka Ghar)",
            category = "HOUSE_SYSTEM",
            description = "In Lal Kitab, the 12 houses correspond permanently to the 12 zodiac signs starting with Aries in the 1st House. Regardless of the native's actual ascendant degree, House 1 is treated as Aries' domain.",
            sourceReference = "Lal Kitab 1939 Farman (Tarmeem Shuda)",
            differenceFromParashari = "Parashari rotates zodiac signs based on the precise astronomical degree of Lagna. Lal Kitab fixes the house numbering with Aries as house 1.",
        ),
        LalKitabResearchTopic(
            id = "lk_masnui_graha",
            title = "Synthetic / Artificial Planets (Masnui Graha)",
            category = "MASNUI_GRAHA",
            description = "Certain planets in combination or synthetic pairs are believed to generate the energetic effect of a third body (e.g., Sun + Saturn acting as Venus; Sun + Jupiter acting as Moon).",
            sourceReference = "Lal Kitab 1942 Ilm-e-Samudrik",
            differenceFromParashari = "Classical Parashari evaluates natural and functional relationships (Mitra/Shatru) and conjunction aspects, but does not substitute an artificial planet.",
        ),
        LalKitabResearchTopic(
            id = "lk_soe_hue_ghar",
            title = "Sleeping Houses & Planets (Kismat Soi Hui)",
            category = "SOE_GHAR",
            description = "A house without planets or uninspected by planetary sights is termed 'asleep' (Soe Hue Ghar), requiring activation through specific symbolic deeds (Dharmi deeds).",
            sourceReference = "Lal Kitab 1952 Gutka Edition",
            differenceFromParashari = "Classical Jyotish analyzes empty houses via their house lord (Bhavesh), dispositor, and rashi drishti without calling them inactive or asleep.",
        ),
        LalKitabResearchTopic(
            id = "lk_dharmi_teva",
            title = "Righteous Horoscope (Dharmi Teva)",
            category = "PHILOSOPHY",
            description = "Charts where benefic placements (like Jupiter in ascendant or Saturn with Jupiter) shield the native from severe afflictions, transforming adverse planetary influences into benign outcomes.",
            sourceReference = "Lal Kitab 1940 edition",
            differenceFromParashari = "Parashari recognizes Vipareeta Raja Yogas and Neecha Bhanga, but uses formal mathematical dignity rather than Dharmi Teva rules.",
        ),
        LalKitabResearchTopic(
            id = "lk_varshphal_rotation",
            title = "Annual Progression / Varshphal Rules",
            category = "FARMAN",
            description = "Annual progression in Lal Kitab moves planets to different houses based on fixed age-derived cycles rather than exact Tajika solar return ingress degrees.",
            sourceReference = "Lal Kitab 1952 edition",
            differenceFromParashari = "Tajika Varshaphal is calculated from the exact solar return instant down to the second of arc, using Muntha and Varsheshwara.",
        ),
    )

    private val overview = LalKitabTraditionOverview(
        historicalOrigin = "Emerged in Punjab (undivided India) between 1939 and 1952, written in poetic Urdu-Punjabi idiom with vernacular metaphors.",
        primaryTexts = listOf(
            "Lal Kitab Ke Farman (1939)",
            "Lal Kitab Ke Arman (Ilm-e-Samudrik Ki Lal Kitab, 1940)",
            "Gutka (1941)",
            "Lal Kitab (Tarmeem Shuda, 1942)",
            "Lal Kitab (1952 edition - 1173 pages)",
        ),
        author = "Pt. Roop Chand Joshi (1898–1982), Pharwala, Punjab",
        corePrinciples = listOf(
            "Houses are permanently mapped to natural signs 1 to 12.",
            "Planetary debts (Rina) carry symbolic moral duties.",
            "Emphasis on symbolic remedial gestures (upayas) rather than Vedic fire sacrifices (yajnas).",
            "Strict non-mixing rule: Must not be conflated with Parashari principles.",
        ),
        supportedResearchTopics = topics,
        unsupportedProductionMethods = listOf(
            "Destructive physical remedies (demolitions, burying harmful items).",
            "Superstitious health claims or guaranteed miracle promises.",
            "Automatic application of Lal Kitab rules to Parashari natal charts.",
        ),
    )

    override suspend fun getTraditionOverview(): LalKitabTraditionOverview = overview

    override suspend fun getTopicById(id: String): LalKitabResearchTopic? =
        topics.firstOrNull { it.id == id }
}
