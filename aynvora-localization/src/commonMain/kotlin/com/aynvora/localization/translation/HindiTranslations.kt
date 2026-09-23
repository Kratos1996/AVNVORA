package com.aynvora.localization.translation

/**
 * Hindi (hi-IN) translation catalog.
 *
 * Covers: app strings, all 12 Rashis, all 27 Nakshatras, all 9 celestial bodies,
 * status strings, errors, chart UI, and accessibility strings.
 *
 * Terminology uses standard Vedic Jyotisha Hindi nomenclature.
 * Traditional spellings are preserved; terms are NOT invented.
 * Keys not present here fall back to English via [AynvoraTranslator] fallback chain.
 */
internal object HindiTranslations {

    val table = TranslationTable(
        localeId = "hi",
        entries = mapOf(
            // App
            "app.app_name" to "AYNVORA",
            "app.loading" to "लोड हो रहा है…",
            "app.error" to "एक त्रुटि हुई",
            "app.retry" to "पुनः प्रयास करें",
            "app.cancel" to "रद्द करें",
            "app.save" to "सहेजें",
            "app.delete" to "हटाएँ",
            "app.confirm" to "पुष्टि करें",
            "app.settings" to "सेटिंग्स",
            "app.language" to "भाषा",
            "app.select_language" to "भाषा चुनें",
            "app.theme" to "थीम",
            "app.done" to "हो गया",
            "app.back" to "वापस",

            // Rashis — Hindi names (standard Vedic Jyotisha)
            "astro.rashi.aries" to "मेष",
            "astro.rashi.taurus" to "वृषभ",
            "astro.rashi.gemini" to "मिथुन",
            "astro.rashi.cancer" to "कर्क",
            "astro.rashi.leo" to "सिंह",
            "astro.rashi.virgo" to "कन्या",
            "astro.rashi.libra" to "तुला",
            "astro.rashi.scorpio" to "वृश्चिक",
            "astro.rashi.sagittarius" to "धनु",
            "astro.rashi.capricorn" to "मकर",
            "astro.rashi.aquarius" to "कुम्भ",
            "astro.rashi.pisces" to "मीन",

            // Rashis — Sanskrit names (same as English; preserved)
            "astro.rashi.aries.sanskrit" to "मेषा",
            "astro.rashi.taurus.sanskrit" to "वृषभा",
            "astro.rashi.gemini.sanskrit" to "मिथुना",
            "astro.rashi.cancer.sanskrit" to "कर्का",
            "astro.rashi.leo.sanskrit" to "सिंहा",
            "astro.rashi.virgo.sanskrit" to "कन्या",
            "astro.rashi.libra.sanskrit" to "तुला",
            "astro.rashi.scorpio.sanskrit" to "वृश्चिका",
            "astro.rashi.sagittarius.sanskrit" to "धनुः",
            "astro.rashi.capricorn.sanskrit" to "मकरा",
            "astro.rashi.aquarius.sanskrit" to "कुम्भा",
            "astro.rashi.pisces.sanskrit" to "मीना",

            // Nakshatras — all 27 (standard Vedic Jyotisha Hindi)
            "astro.nakshatra.ashwini" to "अश्विनी",
            "astro.nakshatra.bharani" to "भरणी",
            "astro.nakshatra.krittika" to "कृत्तिका",
            "astro.nakshatra.rohini" to "रोहिणी",
            "astro.nakshatra.mrigashira" to "मृगशिरा",
            "astro.nakshatra.ardra" to "आर्द्रा",
            "astro.nakshatra.punarvasu" to "पुनर्वसु",
            "astro.nakshatra.pushya" to "पुष्य",
            "astro.nakshatra.ashlesha" to "आश्लेषा",
            "astro.nakshatra.magha" to "मघा",
            "astro.nakshatra.purva_phalguni" to "पूर्व फाल्गुनी",
            "astro.nakshatra.uttara_phalguni" to "उत्तर फाल्गुनी",
            "astro.nakshatra.hasta" to "हस्त",
            "astro.nakshatra.chitra" to "चित्रा",
            "astro.nakshatra.swati" to "स्वाती",
            "astro.nakshatra.vishakha" to "विशाखा",
            "astro.nakshatra.anuradha" to "अनुराधा",
            "astro.nakshatra.jyeshtha" to "ज्येष्ठा",
            "astro.nakshatra.mula" to "मूल",
            "astro.nakshatra.purva_ashadha" to "पूर्वाषाढ़ा",
            "astro.nakshatra.uttara_ashadha" to "उत्तराषाढ़ा",
            "astro.nakshatra.shravana" to "श्रवण",
            "astro.nakshatra.dhanishta" to "धनिष्ठा",
            "astro.nakshatra.shatabhisha" to "शतभिषा",
            "astro.nakshatra.purva_bhadrapada" to "पूर्व भाद्रपद",
            "astro.nakshatra.uttara_bhadrapada" to "उत्तर भाद्रपद",
            "astro.nakshatra.revati" to "रेवती",

            // Celestial bodies — all 9
            "astro.body.sun" to "सूर्य",
            "astro.body.moon" to "चन्द्र",
            "astro.body.mercury" to "बुध",
            "astro.body.venus" to "शुक्र",
            "astro.body.mars" to "मंगल",
            "astro.body.jupiter" to "बृहस्पति",
            "astro.body.saturn" to "शनि",
            "astro.body.rahu" to "राहु",
            "astro.body.ketu" to "केतु",

            // Status
            "astro.status.calculating" to "गणना हो रही है…",
            "astro.status.calculated" to "गणना पूर्ण",
            "astro.status.error" to "गणना त्रुटि",
            "astro.retrograde" to "वक्री",
            "astro.direct" to "मार्गी",

            // Ayanamsa / House labels
            "astro.ayanamsa.lahiri" to "लाहिरी (चित्रापक्ष)",
            "astro.ayanamsa.tropical" to "उष्णकटिबंधीय",
            "astro.house.equal" to "समान भाव",
            "astro.profile.standard_vedic" to "मानक वैदिक",

            // Errors
            "error.generic" to "कुछ गलत हो गया। कृपया पुनः प्रयास करें।",
            "error.invalid_input" to "अमान्य इनपुट: {field}",
            "error.not_found" to "नहीं मिला",
            "error.unsupported_config" to "यह कॉन्फ़िगरेशन समर्थित नहीं है।",
            "error.network_unavailable" to "नेटवर्क उपलब्ध नहीं। ऑफलाइन काम कर रहे हैं।",

            // Chart
            "chart.title" to "जन्मपत्री",
            "chart.birth_date" to "जन्म तिथि",
            "chart.birth_time" to "जन्म समय",
            "chart.birth_place" to "जन्म स्थान",
            "chart.calculate" to "गणना करें",
            "chart.julian_day" to "जूलियन दिवस",
            "chart.ayanamsa" to "अयनांश",
            "chart.planetary_positions" to "ग्रह स्थिति",
            "chart.saved_one" to "1 कुंडली सहेजी गई",
            "chart.saved_other" to "{count} कुंडलियाँ सहेजी गईं",

            // Accessibility
            "a11y.language_selector" to "भाषा चयनकर्ता",
            "a11y.selected_language" to "{language} चयनित",
            "a11y.loading" to "लोड हो रहा है, कृपया प्रतीक्षा करें",
            "a11y.error_message" to "त्रुटि: {message}",
            "a11y.retrograde" to "वक्री गति",
        ),
    )
}
