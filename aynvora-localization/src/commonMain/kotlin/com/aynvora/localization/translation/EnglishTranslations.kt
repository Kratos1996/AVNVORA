package com.aynvora.localization.translation

/**
 * English (en-IN) translation catalog.
 *
 * All 12 Rashis, 27 Nakshatras, and 9 celestial bodies are covered.
 * These are the stable baseline translations; all other locales fall back to this catalog.
 *
 * Terminology follows standard transliteration conventions.
 * Sanskrit names for Rashis are preserved as documented.
 */
internal object EnglishTranslations {

    val table = TranslationTable(
        localeId = "en",
        entries = mapOf(
            // App
            "app.app_name" to "AYNVORA",
            "app.loading" to "Loading…",
            "app.error" to "An error occurred",
            "app.retry" to "Retry",
            "app.cancel" to "Cancel",
            "app.save" to "Save",
            "app.delete" to "Delete",
            "app.confirm" to "Confirm",
            "app.settings" to "Settings",
            "app.language" to "Language",
            "app.select_language" to "Select Language",
            "app.theme" to "Theme",
            "app.done" to "Done",
            "app.back" to "Back",

            // Rashis — English display names
            "astro.rashi.aries" to "Aries",
            "astro.rashi.taurus" to "Taurus",
            "astro.rashi.gemini" to "Gemini",
            "astro.rashi.cancer" to "Cancer",
            "astro.rashi.leo" to "Leo",
            "astro.rashi.virgo" to "Virgo",
            "astro.rashi.libra" to "Libra",
            "astro.rashi.scorpio" to "Scorpio",
            "astro.rashi.sagittarius" to "Sagittarius",
            "astro.rashi.capricorn" to "Capricorn",
            "astro.rashi.aquarius" to "Aquarius",
            "astro.rashi.pisces" to "Pisces",

            // Rashis — Sanskrit names (preserved as documented)
            "astro.rashi.aries.sanskrit" to "Mesha",
            "astro.rashi.taurus.sanskrit" to "Vrishabha",
            "astro.rashi.gemini.sanskrit" to "Mithuna",
            "astro.rashi.cancer.sanskrit" to "Karka",
            "astro.rashi.leo.sanskrit" to "Simha",
            "astro.rashi.virgo.sanskrit" to "Kanya",
            "astro.rashi.libra.sanskrit" to "Tula",
            "astro.rashi.scorpio.sanskrit" to "Vrishchika",
            "astro.rashi.sagittarius.sanskrit" to "Dhanu",
            "astro.rashi.capricorn.sanskrit" to "Makara",
            "astro.rashi.aquarius.sanskrit" to "Kumbha",
            "astro.rashi.pisces.sanskrit" to "Meena",

            // Nakshatras — all 27
            "astro.nakshatra.ashwini" to "Ashwini",
            "astro.nakshatra.bharani" to "Bharani",
            "astro.nakshatra.krittika" to "Krittika",
            "astro.nakshatra.rohini" to "Rohini",
            "astro.nakshatra.mrigashira" to "Mrigashira",
            "astro.nakshatra.ardra" to "Ardra",
            "astro.nakshatra.punarvasu" to "Punarvasu",
            "astro.nakshatra.pushya" to "Pushya",
            "astro.nakshatra.ashlesha" to "Ashlesha",
            "astro.nakshatra.magha" to "Magha",
            "astro.nakshatra.purva_phalguni" to "Purva Phalguni",
            "astro.nakshatra.uttara_phalguni" to "Uttara Phalguni",
            "astro.nakshatra.hasta" to "Hasta",
            "astro.nakshatra.chitra" to "Chitra",
            "astro.nakshatra.swati" to "Swati",
            "astro.nakshatra.vishakha" to "Vishakha",
            "astro.nakshatra.anuradha" to "Anuradha",
            "astro.nakshatra.jyeshtha" to "Jyeshtha",
            "astro.nakshatra.mula" to "Mula",
            "astro.nakshatra.purva_ashadha" to "Purva Ashadha",
            "astro.nakshatra.uttara_ashadha" to "Uttara Ashadha",
            "astro.nakshatra.shravana" to "Shravana",
            "astro.nakshatra.dhanishta" to "Dhanishta",
            "astro.nakshatra.shatabhisha" to "Shatabhisha",
            "astro.nakshatra.purva_bhadrapada" to "Purva Bhadrapada",
            "astro.nakshatra.uttara_bhadrapada" to "Uttara Bhadrapada",
            "astro.nakshatra.revati" to "Revati",

            // Celestial bodies — all 9
            "astro.body.sun" to "Sun",
            "astro.body.moon" to "Moon",
            "astro.body.mercury" to "Mercury",
            "astro.body.venus" to "Venus",
            "astro.body.mars" to "Mars",
            "astro.body.jupiter" to "Jupiter",
            "astro.body.saturn" to "Saturn",
            "astro.body.rahu" to "Rahu",
            "astro.body.ketu" to "Ketu",

            // Status
            "astro.status.calculating" to "Calculating…",
            "astro.status.calculated" to "Calculated",
            "astro.status.error" to "Calculation Error",
            "astro.retrograde" to "Retrograde",
            "astro.direct" to "Direct",

            // Ayanamsa / House labels
            "astro.ayanamsa.lahiri" to "Lahiri (Chitrapaksha)",
            "astro.ayanamsa.tropical" to "Tropical",
            "astro.house.equal" to "Equal House",
            "astro.profile.standard_vedic" to "Standard Vedic",

            // Errors
            "error.generic" to "Something went wrong. Please try again.",
            "error.invalid_input" to "Invalid input: {field}",
            "error.not_found" to "Not found",
            "error.unsupported_config" to "This configuration is not supported.",
            "error.network_unavailable" to "Network unavailable. Working offline.",

            // Chart
            "chart.title" to "Birth Chart",
            "chart.birth_date" to "Birth Date",
            "chart.birth_time" to "Birth Time",
            "chart.birth_place" to "Birth Place",
            "chart.calculate" to "Calculate",
            "chart.julian_day" to "Julian Day",
            "chart.ayanamsa" to "Ayanamsa",
            "chart.planetary_positions" to "Planetary Positions",
            "chart.saved_one" to "1 chart saved",
            "chart.saved_other" to "{count} charts saved",

            // Accessibility
            "a11y.language_selector" to "Language selector",
            "a11y.selected_language" to "{language} selected",
            "a11y.loading" to "Loading, please wait",
            "a11y.error_message" to "Error: {message}",
            "a11y.retrograde" to "Retrograde motion",
        ),
    )
}
