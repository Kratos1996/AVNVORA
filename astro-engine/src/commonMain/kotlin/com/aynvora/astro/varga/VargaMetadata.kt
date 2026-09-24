package com.aynvora.astro.varga

import kotlinx.serialization.Serializable

/**
 * Factual metadata representing a Vedic divisional chart in the Astro Engine.
 * Presentation terms and localized strings remain strictly external.
 */
@Serializable
data class VargaMetadata(
    val chart: DivisionalChart,
    val divisionNumber: Int,
    val traditionalName: String,
    val ruleId: String,
    val referenceSource: String,
    val isSupported: Boolean,
    val description: String,
)

/**
 * Central registry of all classical Vedic divisional charts and their authoritative sources.
 */
object VargaRegistry {
    private val metadataMap: Map<DivisionalChart, VargaMetadata> = mapOf(
        DivisionalChart.D1 to VargaMetadata(
            chart = DivisionalChart.D1,
            divisionNumber = 1,
            traditionalName = "Rashi",
            ruleId = "ASTRO-R09",
            referenceSource = "BPHS Ch. 6, slokas 3-4",
            isSupported = true,
            description = "Base physical and general life framework chart (1 division of 30°).",
        ),
        DivisionalChart.D2 to VargaMetadata(
            chart = DivisionalChart.D2,
            divisionNumber = 2,
            traditionalName = "Hora",
            ruleId = "ASTRO-R10",
            referenceSource = "BPHS Ch. 6, slokas 5-6",
            isSupported = true,
            description = "Wealth and resource capacity chart (2 divisions of 15°, ruled by Sun and Moon).",
        ),
        DivisionalChart.D3 to VargaMetadata(
            chart = DivisionalChart.D3,
            divisionNumber = 3,
            traditionalName = "Drekkana",
            ruleId = "ASTRO-R11",
            referenceSource = "BPHS Ch. 6, slokas 7-8",
            isSupported = true,
            description = "Siblings, courage, and vitality chart (3 equal divisions of 10°).",
        ),
        DivisionalChart.D4 to VargaMetadata(
            chart = DivisionalChart.D4,
            divisionNumber = 4,
            traditionalName = "Chaturthamsa",
            ruleId = "ASTRO-R12",
            referenceSource = "BPHS Ch. 6, sloka 9",
            isSupported = true,
            description = "Fixed assets, real estate, and fortune chart (4 divisions of 7°30').",
        ),
        DivisionalChart.D7 to VargaMetadata(
            chart = DivisionalChart.D7,
            divisionNumber = 7,
            traditionalName = "Saptamsa",
            ruleId = "ASTRO-R13",
            referenceSource = "BPHS Ch. 6, slokas 10-11",
            isSupported = true,
            description = "Children and progeny lineage chart (7 divisions of 4°17'08.57\").",
        ),
        DivisionalChart.D9 to VargaMetadata(
            chart = DivisionalChart.D9,
            divisionNumber = 9,
            traditionalName = "Navamsa",
            ruleId = "ASTRO-R14",
            referenceSource = "BPHS Ch. 6, slokas 12-14",
            isSupported = true,
            description = "Dharma, marriage, and essential spiritual strength chart (9 divisions of 3°20').",
        ),
        DivisionalChart.D10 to VargaMetadata(
            chart = DivisionalChart.D10,
            divisionNumber = 10,
            traditionalName = "Dasamsa",
            ruleId = "ASTRO-R15",
            referenceSource = "BPHS Ch. 6, slokas 15-16",
            isSupported = true,
            description = "Profession, career accomplishments, and public status chart (10 divisions of 3°).",
        ),
        DivisionalChart.D12 to VargaMetadata(
            chart = DivisionalChart.D12,
            divisionNumber = 12,
            traditionalName = "Dwadasamsa",
            ruleId = "ASTRO-R16",
            referenceSource = "BPHS Ch. 6, slokas 17-18",
            isSupported = true,
            description = "Lineage, parents, and ancestry chart (12 divisions of 2°30').",
        ),
        DivisionalChart.D16 to VargaMetadata(
            chart = DivisionalChart.D16,
            divisionNumber = 16,
            traditionalName = "Shodasamsa",
            ruleId = "ASTRO-R17",
            referenceSource = "BPHS Ch. 6, slokas 19-21",
            isSupported = true,
            description = "Vehicles, comforts, and inner happiness chart (16 divisions of 1°52'30\").",
        ),
        DivisionalChart.D20 to VargaMetadata(
            chart = DivisionalChart.D20,
            divisionNumber = 20,
            traditionalName = "Vimsamsa",
            ruleId = "ASTRO-R18",
            referenceSource = "BPHS Ch. 6, slokas 22-23",
            isSupported = true,
            description = "Spiritual pursuits, devotion, and upasana chart (20 divisions of 1°30').",
        ),
        DivisionalChart.D24 to VargaMetadata(
            chart = DivisionalChart.D24,
            divisionNumber = 24,
            traditionalName = "Chaturvimsamsa",
            ruleId = "ASTRO-R19",
            referenceSource = "BPHS Ch. 6, slokas 24-25",
            isSupported = true,
            description = "Higher learning, academic knowledge, and skill mastery chart (24 divisions of 1°15').",
        ),
        DivisionalChart.D27 to VargaMetadata(
            chart = DivisionalChart.D27,
            divisionNumber = 27,
            traditionalName = "Bhamsa",
            ruleId = "ASTRO-R20",
            referenceSource = "BPHS Ch. 6, slokas 26-27",
            isSupported = true,
            description = "Physical strengths, weaknesses, and subconscious resilience chart (27 divisions of 1°06'40\").",
        ),
        DivisionalChart.D30 to VargaMetadata(
            chart = DivisionalChart.D30,
            divisionNumber = 30,
            traditionalName = "Trimsamsa",
            ruleId = "ASTRO-R21",
            referenceSource = "BPHS Ch. 6, slokas 28-31",
            isSupported = true,
            description = "Misfortunes, health afflictions, and karmic challenges chart (5 unequal divisions).",
        ),
        DivisionalChart.D40 to VargaMetadata(
            chart = DivisionalChart.D40,
            divisionNumber = 40,
            traditionalName = "Khavedamsa",
            ruleId = "ASTRO-R22",
            referenceSource = "BPHS Ch. 6, slokas 32-33",
            isSupported = true,
            description = "Auspicious and inauspicious karmic effects chart (40 divisions of 0°45').",
        ),
        DivisionalChart.D45 to VargaMetadata(
            chart = DivisionalChart.D45,
            divisionNumber = 45,
            traditionalName = "Akshavedamsa",
            ruleId = "ASTRO-R23",
            referenceSource = "BPHS Ch. 6, slokas 34-35",
            isSupported = true,
            description = "General karmic purity and subtle character chart (45 divisions of 0°40').",
        ),
        DivisionalChart.D60 to VargaMetadata(
            chart = DivisionalChart.D60,
            divisionNumber = 60,
            traditionalName = "Shashtiamsa",
            ruleId = "ASTRO-R24",
            referenceSource = "BPHS Ch. 6, slokas 36-42 & Dr. B. V. Raman Ch. 9",
            isSupported = true,
            description = "Subtle past-life karma and overarching destiny chart (60 divisions of 0°30').",
        ),
    )

    fun getMetadata(chart: DivisionalChart): VargaMetadata =
        metadataMap[chart] ?: error("No metadata registered for divisional chart $chart")

    fun all(): List<VargaMetadata> = metadataMap.values.toList()

    fun supportedCharts(): Set<DivisionalChart> = metadataMap.filterValues { it.isSupported }.keys
}
