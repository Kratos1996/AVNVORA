package com.aynvora.localization.translation

import com.aynvora.core.models.CelestialBody
import com.aynvora.core.models.Nakshatra
import com.aynvora.core.models.Rashi

/**
 * Stable, typed translation key hierarchy for AYNVORA.
 *
 * RULES:
 * - Never use raw UI strings as translation keys (e.g. Text("Calculate")).
 * - Always resolve through [AynvoraTranslator.translate].
 * - Keys are stable across app versions. Renaming a key is a breaking change.
 * - Keys are language-independent.
 *
 * String key derivation:
 *   TranslationKey.App.AppName   → "app.app_name"
 *   TranslationKey.App.Loading   → "app.loading"
 *   TranslationKey.Astro.Rashi(Rashi.ARIES) → "astro.rashi.aries"
 *
 * Variable placeholders use {variable_name} syntax:
 *   "saved_charts_count" → "Saved {count} charts"
 *
 * Plural keys use _one / _other suffix:
 *   "chart.saved_one"   → "1 chart saved"
 *   "chart.saved_other" → "{count} charts saved"
 */
import com.aynvora.core.localization.LocalizationKey

sealed class TranslationKey : LocalizationKey {

    /** Derived string key used for catalog lookup. */
    override abstract val key: String


    // -------------------------------------------------------------------------
    // App-level strings
    // -------------------------------------------------------------------------
    object App {
        object AppName : TranslationKey() { override val key = "app.app_name" }
        object Tagline : TranslationKey() {
            override val key = "app.tagline"
        }
        object Loading : TranslationKey() { override val key = "app.loading" }
        object Error : TranslationKey() { override val key = "app.error" }
        object Retry : TranslationKey() { override val key = "app.retry" }
        object Cancel : TranslationKey() { override val key = "app.cancel" }
        object Save : TranslationKey() { override val key = "app.save" }
        object Delete : TranslationKey() { override val key = "app.delete" }
        object Confirm : TranslationKey() { override val key = "app.confirm" }
        object Settings : TranslationKey() { override val key = "app.settings" }
        object Language : TranslationKey() { override val key = "app.language" }
        object SelectLanguage : TranslationKey() { override val key = "app.select_language" }
        object Theme : TranslationKey() { override val key = "app.theme" }
        object Done : TranslationKey() { override val key = "app.done" }
        object Back : TranslationKey() { override val key = "app.back" }
    }

    // -------------------------------------------------------------------------
    // Feature Detail Sheet strings
    // -------------------------------------------------------------------------
    object FeatureDetail {
        object OperationalStatus : TranslationKey() {
            override val key = "feature_detail.operational_status"
        }

        object StatusAvailable : TranslationKey() {
            override val key = "feature_detail.status_available"
        }

        object StatusOffline : TranslationKey() {
            override val key = "feature_detail.status_offline"
        }

        object StatusSetupRequired : TranslationKey() {
            override val key = "feature_detail.status_setup_required"
        }

        object StatusUpdateRequired : TranslationKey() {
            override val key = "feature_detail.status_update_required"
        }

        object StatusUnsupported : TranslationKey() {
            override val key = "feature_detail.status_unsupported"
        }

        object DomainOverview : TranslationKey() {
            override val key = "feature_detail.domain_overview"
        }

        object ArchitectureFoundation : TranslationKey() {
            override val key = "feature_detail.architecture_foundation"
        }

        object PrivacyGuaranteeTitle : TranslationKey() {
            override val key = "feature_detail.privacy_guarantee_title"
        }

        object PrivacyGuaranteeBody : TranslationKey() {
            override val key = "feature_detail.privacy_guarantee_body"
        }

        object UnderstoodClose : TranslationKey() {
            override val key = "feature_detail.understood_close"
        }

        object FeatureOpen : TranslationKey() {
            override val key = "feature.open"
        }

        object FeatureViewFoundation : TranslationKey() {
            override val key = "feature.view_foundation"
        }

        /** For in-development status: expects {phase} arg */
        object StatusInDevelopment : TranslationKey() {
            override val key = "feature_detail.status_in_development"
        }

        /** For setup required: expects {reason} arg */
        object StatusConfigRequired : TranslationKey() {
            override val key = "feature_detail.status_config_required"
        }

        /** For unsupported: expects {platform} arg */
        object StatusUnsupportedPlatform : TranslationKey() {
            override val key = "feature_detail.status_unsupported_platform"
        }
    }

    // -------------------------------------------------------------------------
    // Astrology domain strings (bodies, signs, nakshatras)
    // -------------------------------------------------------------------------
    object Astro {
        /** Localized display name for a Rashi (Zodiac Sign). */
        class RashiName(val rashi: Rashi) : TranslationKey() {
            override val key = "astro.rashi.${rashi.name.lowercase()}"
        }

        /** Localized Sanskrit name for a Rashi. */
        class RashiSanskrit(val rashi: Rashi) : TranslationKey() {
            override val key = "astro.rashi.${rashi.name.lowercase()}.sanskrit"
        }

        /** Localized display name for a Nakshatra. */
        class NakshatraName(val nakshatra: Nakshatra) : TranslationKey() {
            override val key = "astro.nakshatra.${nakshatra.name.lowercase()}"
        }

        /** Localized display name for a celestial body. */
        class BodyName(val body: CelestialBody) : TranslationKey() {
            override val key = "astro.body.${body.name.lowercase()}"
        }

        // Calculation status labels
        object StatusCalculating : TranslationKey() { override val key = "astro.status.calculating" }
        object StatusCalculated : TranslationKey() { override val key = "astro.status.calculated" }
        object StatusError : TranslationKey() { override val key = "astro.status.error" }

        // Retrograde
        object Retrograde : TranslationKey() { override val key = "astro.retrograde" }
        object Direct : TranslationKey() { override val key = "astro.direct" }

        // Ayanamsa labels
        object AyanamsaLahiri : TranslationKey() { override val key = "astro.ayanamsa.lahiri" }
        object AyanamsaTropical : TranslationKey() { override val key = "astro.ayanamsa.tropical" }

        // House system labels
        object HouseEqual : TranslationKey() { override val key = "astro.house.equal" }

        // Calculation profile
        object ProfileStandardVedic : TranslationKey() { override val key = "astro.profile.standard_vedic" }
    }

    // -------------------------------------------------------------------------
    // Error strings
    // -------------------------------------------------------------------------
    object Errors {
        object GenericError : TranslationKey() { override val key = "error.generic" }
        object InvalidInput : TranslationKey() { override val key = "error.invalid_input" }
        object NotFound : TranslationKey() { override val key = "error.not_found" }
        object UnsupportedConfig : TranslationKey() { override val key = "error.unsupported_config" }
        object NetworkUnavailable : TranslationKey() { override val key = "error.network_unavailable" }
    }

    // -------------------------------------------------------------------------
    // Chart / BirthData strings
    // -------------------------------------------------------------------------
    object Chart {
        object Title : TranslationKey() { override val key = "chart.title" }
        object BirthDate : TranslationKey() { override val key = "chart.birth_date" }
        object BirthTime : TranslationKey() { override val key = "chart.birth_time" }
        object BirthPlace : TranslationKey() { override val key = "chart.birth_place" }
        object Calculate : TranslationKey() { override val key = "chart.calculate" }
        object JulianDay : TranslationKey() { override val key = "chart.julian_day" }
        object Ayanamsa : TranslationKey() { override val key = "chart.ayanamsa" }
        object PlanetaryPositions : TranslationKey() { override val key = "chart.planetary_positions" }
        // Plural: "chart.saved_one" / "chart.saved_other"
        object SavedOne : TranslationKey() { override val key = "chart.saved_one" }
        object SavedOther : TranslationKey() { override val key = "chart.saved_other" }
    }

    // -------------------------------------------------------------------------
    // Tarot domain strings
    // -------------------------------------------------------------------------
    object Tarot {
        object Title : TranslationKey() {
            override val key = "tarot.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "tarot.subtitle"
        }

        object DisclaimerTitle : TranslationKey() {
            override val key = "tarot.disclaimer_title"
        }

        object DisclaimerBody : TranslationKey() {
            override val key = "tarot.disclaimer_body"
        }

        object DisclaimerAcknowledge : TranslationKey() {
            override val key = "tarot.disclaimer_acknowledge"
        }

        object SelectSpread : TranslationKey() {
            override val key = "tarot.select_spread"
        }

        object DrawCards : TranslationKey() {
            override val key = "tarot.draw_cards"
        }

        object DrawAgain : TranslationKey() {
            override val key = "tarot.draw_again"
        }

        object Upright : TranslationKey() {
            override val key = "tarot.upright"
        }

        object Reversed : TranslationKey() {
            override val key = "tarot.reversed"
        }

        object SingleCardTitle : TranslationKey() {
            override val key = "tarot.spread.single_card.title"
        }

        object SingleCardDesc : TranslationKey() {
            override val key = "tarot.spread.single_card.desc"
        }

        object ThreeCardTitle : TranslationKey() {
            override val key = "tarot.spread.three_card.title"
        }

        object ThreeCardDesc : TranslationKey() {
            override val key = "tarot.spread.three_card.desc"
        }

        object ReflectiveMeaning : TranslationKey() {
            override val key = "tarot.reflective_meaning"
        }

        object KeywordsLabel : TranslationKey() {
            override val key = "tarot.keywords_label"
        }

        object ReflectionTitle : TranslationKey() {
            override val key = "tarot.screen.reflection_title"
        }

        object Close : TranslationKey() {
            override val key = "tarot.screen.close"
        }

        object SingleCard : TranslationKey() {
            override val key = "tarot.screen.single_card"
        }

        object ThreeCards : TranslationKey() {
            override val key = "tarot.screen.three_cards"
        }

        object DrawingCards : TranslationKey() {
            override val key = "tarot.screen.drawing_cards"
        }

        object DisclosureTitle : TranslationKey() {
            override val key = "tarot.screen.disclosure_title"
        }

        object DisclosureAccept : TranslationKey() {
            override val key = "tarot.screen.disclosure_accept"
        }

        object DisclosureBack : TranslationKey() {
            override val key = "tarot.screen.disclosure_back"
        }

        object UprightArrow : TranslationKey() {
            override val key = "tarot.screen.upright_arrow"
        }

        object ReversedArrow : TranslationKey() {
            override val key = "tarot.screen.reversed_arrow"
        }

        object ReflectivePerspective : TranslationKey() {
            override val key = "tarot.screen.reflective_perspective"
        }

        object CardBrowser : TranslationKey() {
            override val key = "tarot.screen.card_browser"
        }

        object History : TranslationKey() {
            override val key = "tarot.screen.history"
        }

        object DeckAttribution : TranslationKey() {
            override val key = "tarot.screen.deck_attribution"
        }

        object SelectDeck : TranslationKey() {
            override val key = "tarot.screen.select_deck"
        }

        object CardsEmerging : TranslationKey() {
            override val key = "tarot.screen.cards_emerging"
        }

        object RevealedSuffix : TranslationKey() {
            override val key = "tarot.screen.revealed_suffix"
        }

        object ViewMeanings : TranslationKey() {
            override val key = "tarot.screen.view_meanings"
        }

        object TapToReveal : TranslationKey() {
            override val key = "tarot.screen.tap_to_reveal"
        }

        object YourReading : TranslationKey() {
            override val key = "tarot.screen.your_reading"
        }

        object Timeline : TranslationKey() {
            override val key = "tarot.screen.timeline"
        }

        object Satisfied : TranslationKey() {
            override val key = "tarot.screen.satisfied"
        }

        object NewReading : TranslationKey() {
            override val key = "tarot.screen.new_reading"
        }

        object Report : TranslationKey() {
            override val key = "tarot.screen.report"
        }

        object DisclaimerText : TranslationKey() {
            override val key = "tarot.screen.disclaimer_text"
        }

        object ExplanationTemplate : TranslationKey() {
            override val key = "tarot.explanation.template"
        }

        object DisclosureSubtitle : TranslationKey() {
            override val key = "tarot.screen.disclosure_subtitle"
        }

        object BrowseCardsSuffix : TranslationKey() {
            override val key = "tarot.screen.browse_cards_suffix"
        }

        object UprightPerspective : TranslationKey() {
            override val key = "tarot.screen.upright_perspective"
        }

        object ReversedPerspective : TranslationKey() {
            override val key = "tarot.screen.reversed_perspective"
        }

        object NoReadingsYet : TranslationKey() {
            override val key = "tarot.screen.no_readings_yet"
        }

        object LicenseNotice : TranslationKey() {
            override val key = "tarot.screen.license_notice"
        }
    }


    // -------------------------------------------------------------------------
    // Core Product Feature Hub & Titles
    // -------------------------------------------------------------------------
    object Features {
        object Astrology : TranslationKey() {
            override val key = "feature.astrology"
        }

        object Palmistry : TranslationKey() {
            override val key = "feature.palmistry"
        }

        object Gemstone : TranslationKey() {
            override val key = "feature.gemstone"
        }

        object Gita : TranslationKey() {
            override val key = "feature.gita"
        }

        object GarudaPuran : TranslationKey() {
            override val key = "feature.garuda_puran"
        }

        object LalKitab : TranslationKey() {
            override val key = "feature.lal_kitab"
        }

        object Tarot : TranslationKey() {
            override val key = "feature.tarot"
        }

        object AiAssistant : TranslationKey() {
            override val key = "feature.ai_assistant"
        }

        object DailyGuidance : TranslationKey() {
            override val key = "feature.daily_guidance"
        }

        object Wallpaper : TranslationKey() {
            override val key = "feature.wallpaper"
        }

        object Numerology : TranslationKey() {
            override val key = "feature.numerology"
        }

        object Rudraksha : TranslationKey() {
            override val key = "feature.rudraksha"
        }

        object Jadi : TranslationKey() {
            override val key = "feature.jadi"
        }

        object Yantra : TranslationKey() {
            override val key = "feature.yantra"
        }

        object ComingSoon : TranslationKey() {
            override val key = "feature.coming_soon"
        }
    }

    object Palmistry {
        object Title : TranslationKey() {
            override val key = "palmistry.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "palmistry.subtitle"
        }

        object ScanPrompt : TranslationKey() {
            override val key = "palmistry.scan_prompt"
        }

        object DisclaimerTitle : TranslationKey() {
            override val key = "palmistry.disclaimer.title"
        }

        object DisclaimerText : TranslationKey() {
            override val key = "palmistry.disclaimer.text"
        }

        object DisclaimerAccept : TranslationKey() {
            override val key = "palmistry.disclaimer.accept"
        }

        object SelectHandTitle : TranslationKey() {
            override val key = "palmistry.hand.select_title"
        }

        object SelectHandSubtitle : TranslationKey() {
            override val key = "palmistry.hand.select_subtitle"
        }

        object LeftHand : TranslationKey() {
            override val key = "palmistry.hand.left"
        }

        object RightHand : TranslationKey() {
            override val key = "palmistry.hand.right"
        }

        object CaptureTitle : TranslationKey() {
            override val key = "palmistry.capture.title"
        }

        object CaptureGuidance : TranslationKey() {
            override val key = "palmistry.capture.guidance"
        }

        object TakePhoto : TranslationKey() {
            override val key = "palmistry.capture.take_photo"
        }

        object PickGallery : TranslationKey() {
            override val key = "palmistry.capture.pick_gallery"
        }

        object SampleHand : TranslationKey() {
            override val key = "palmistry.capture.sample_hand"
        }

        object QualityValidating : TranslationKey() {
            override val key = "palmistry.quality.validating"
        }

        object QualityGood : TranslationKey() {
            override val key = "palmistry.quality.good"
        }

        object QualityLowRes : TranslationKey() {
            override val key = "palmistry.quality.low_res"
        }

        object QualityBlurry : TranslationKey() {
            override val key = "palmistry.quality.blurry"
        }

        object QualityTooDark : TranslationKey() {
            override val key = "palmistry.quality.too_dark"
        }

        object QualityTooBright : TranslationKey() {
            override val key = "palmistry.quality.too_bright"
        }

        object QualityHandNotFound : TranslationKey() {
            override val key = "palmistry.quality.hand_not_found"
        }

        object AnalyzingTitle : TranslationKey() {
            override val key = "palmistry.analysis.title"
        }

        object AnalyzingSubtitle : TranslationKey() {
            override val key = "palmistry.analysis.subtitle"
        }

        object AnalysisResults : TranslationKey() {
            override val key = "palmistry.results.title"
        }

        object DetectedFeatures : TranslationKey() {
            override val key = "palmistry.results.detected_features"
        }

        object TraditionalInterpretation : TranslationKey() {
            override val key = "palmistry.results.traditional_interpretation"
        }

        object AiReflection : TranslationKey() {
            override val key = "palmistry.results.ai_reflection"
        }

        object AskQuestionPrompt : TranslationKey() {
            override val key = "palmistry.question.prompt"
        }

        object AskQuestionPlaceholder : TranslationKey() {
            override val key = "palmistry.question.placeholder"
        }

        object AskButton : TranslationKey() {
            override val key = "palmistry.question.ask_button"
        }

        object InsufficientEvidence : TranslationKey() {
            override val key = "palmistry.question.insufficient_evidence"
        }

        object TimelineTitle : TranslationKey() {
            override val key = "palmistry.timeline.title"
        }

        object FeedbackTitle : TranslationKey() {
            override val key = "palmistry.feedback.title"
        }

        object WasHelpful : TranslationKey() {
            override val key = "palmistry.feedback.was_helpful"
        }

        object ImprovementPrompt : TranslationKey() {
            override val key = "palmistry.feedback.improvement_prompt"
        }

        object SubmitFeedback : TranslationKey() {
            override val key = "palmistry.feedback.submit"
        }

        object CardFeedbackTitle : TranslationKey() {
            override val key = "palmistry.card_feedback.title"
        }

        object CardFeedbackClear : TranslationKey() {
            override val key = "palmistry.card_feedback.clear"
        }

        object CardFeedbackConfusing : TranslationKey() {
            override val key = "palmistry.card_feedback.confusing"
        }

        object CardFeedbackNeedContext : TranslationKey() {
            override val key = "palmistry.card_feedback.need_context"
        }

        object ReportTitle : TranslationKey() {
            override val key = "palmistry.report.title"
        }

        object ViewReport : TranslationKey() {
            override val key = "palmistry.report.view"
        }

        object ExportPdf : TranslationKey() {
            override val key = "palmistry.report.export_pdf"
        }

        object TraditionBadge : TranslationKey() {
            override val key = "palmistry.screen.tradition_badge"
        }

        object HeroTitle : TranslationKey() {
            override val key = "palmistry.screen.hero_title"
        }

        object HeroSubtext : TranslationKey() {
            override val key = "palmistry.screen.hero_subtext"
        }

        object StartButton : TranslationKey() {
            override val key = "palmistry.screen.start_button"
        }

        object RecentReadings : TranslationKey() {
            override val key = "palmistry.screen.recent_readings"
        }

        object DisclosureTitle : TranslationKey() {
            override val key = "palmistry.disclosure.title"
        }

        object DisclosureBadge : TranslationKey() {
            override val key = "palmistry.disclosure.badge"
        }

        object DisclosureBody : TranslationKey() {
            override val key = "palmistry.disclosure.body"
        }

        object DisclosureAccept : TranslationKey() {
            override val key = "palmistry.disclosure.accept"
        }

        object HandSelectTitle : TranslationKey() {
            override val key = "palmistry.hand.select_title"
        }

        object HandSelectSubtitle : TranslationKey() {
            override val key = "palmistry.hand.select_subtitle"
        }

        object RightHandTitle : TranslationKey() {
            override val key = "palmistry.hand.right_title"
        }

        object RightHandDesc : TranslationKey() {
            override val key = "palmistry.hand.right_desc"
        }

        object LeftHandTitle : TranslationKey() {
            override val key = "palmistry.hand.left_title"
        }

        object LeftHandDesc : TranslationKey() {
            override val key = "palmistry.hand.left_desc"
        }

        object CaptureBadge : TranslationKey() {
            override val key = "palmistry.capture.badge"
        }

        object CaptureGuidanceBody : TranslationKey() {
            override val key = "palmistry.capture.guidance_body"
        }

        object QualityTitle : TranslationKey() {
            override val key = "palmistry.quality.title"
        }

        object QualityVerified : TranslationKey() {
            override val key = "palmistry.quality.verified"
        }

        object QualityIssue : TranslationKey() {
            override val key = "palmistry.quality.issue"
        }

        object QualityStatusFormat : TranslationKey() {
            override val key = "palmistry.quality.status_format"
        }

        object QualityProceed : TranslationKey() {
            override val key = "palmistry.quality.proceed"
        }

        object QualityRetake : TranslationKey() {
            override val key = "palmistry.quality.retake"
        }

        object StepSegmenting : TranslationKey() {
            override val key = "palmistry.analysis.step_segmenting"
        }

        object StepLines : TranslationKey() {
            override val key = "palmistry.analysis.step_lines"
        }

        object StepSamudrika : TranslationKey() {
            override val key = "palmistry.analysis.step_samudrika"
        }

        object AnalysisProgress : TranslationKey() {
            override val key = "palmistry.analysis.analyzing"
        }

        object InsightsTitle : TranslationKey() {
            override val key = "palmistry.results.insights_title"
        }

        object ObservedFeatures : TranslationKey() {
            override val key = "palmistry.results.observed_features"
        }

        object AskPrompt : TranslationKey() {
            override val key = "palmistry.results.ask_prompt"
        }

        object HelpfulLabel : TranslationKey() {
            override val key = "palmistry.results.helpful_label"
        }

        object QuestionPlaceholder : TranslationKey() {
            override val key = "palmistry.results.question_placeholder"
        }

        object GeneratingReflection : TranslationKey() {
            override val key = "palmistry.results.generating_reflection"
        }

        object AskReflectionButton : TranslationKey() {
            override val key = "palmistry.results.ask_button"
        }

        object TimelineButton : TranslationKey() {
            override val key = "palmistry.results.timeline_button"
        }

        object ReportButton : TranslationKey() {
            override val key = "palmistry.results.report_button"
        }

        object SatisfiedButton : TranslationKey() {
            override val key = "palmistry.results.satisfied_button"
        }

        object HandLeftLabel : TranslationKey() {
            override val key = "palmistry.results.hand_left_label"
        }

        object HandRightLabel : TranslationKey() {
            override val key = "palmistry.results.hand_right_label"
        }

        object ObservationSummaryFormat : TranslationKey() {
            override val key = "palmistry.observation.summary_format"
        }

        object ObservationIntro : TranslationKey() {
            override val key = "palmistry.observation.intro"
        }

        object ObservationImportantNote : TranslationKey() {
            override val key = "palmistry.observation.important_note"
        }

        object ObservationReflectionPrefix : TranslationKey() {
            override val key = "palmistry.observation.reflection_prefix"
        }

        object QuestionNoData : TranslationKey() {
            override val key = "palmistry.question.no_data"
        }

        object QuestionInsufficientTitle : TranslationKey() {
            override val key = "palmistry.question.insufficient_title"
        }

        object QuestionUnsupportedLine : TranslationKey() {
            override val key = "palmistry.question.unsupported_line"
        }

        object QuestionLineNotDetectedTitle : TranslationKey() {
            override val key = "palmistry.question.line_not_detected_title"
        }

        object QuestionFailedTitle : TranslationKey() {
            override val key = "palmistry.question.failed_title"
        }

        object StatusPass : TranslationKey() { override val key = "palmistry.status.pass" }
        object StatusWrongHand : TranslationKey() { override val key = "palmistry.status.wrong_hand" }
        object StatusRetry : TranslationKey() { override val key = "palmistry.status.retry" }

        object ShapeSquare : TranslationKey() { override val key = "palmistry.shape.square" }
        object ShapeRectangular : TranslationKey() { override val key = "palmistry.shape.rectangular" }
        object ShapeLong : TranslationKey() { override val key = "palmistry.shape.long" }
        object ShapeWide : TranslationKey() { override val key = "palmistry.shape.wide" }
        object ShapeEarth : TranslationKey() { override val key = "palmistry.shape.earth" }
        object ShapeAir : TranslationKey() { override val key = "palmistry.shape.air" }
        object ShapeFire : TranslationKey() { override val key = "palmistry.shape.fire" }
        object ShapeWater : TranslationKey() { override val key = "palmistry.shape.water" }
        object ShapeStandard : TranslationKey() { override val key = "palmistry.shape.standard" }
        object ShapeUnknown : TranslationKey() { override val key = "palmistry.shape.unknown" }

        object LineHeart : TranslationKey() { override val key = "palmistry.line.heart" }
        object LineHead : TranslationKey() { override val key = "palmistry.line.head" }
        object LineLife : TranslationKey() { override val key = "palmistry.line.life" }
        object LineFate : TranslationKey() { override val key = "palmistry.line.fate" }
        object LineSun : TranslationKey() { override val key = "palmistry.line.sun" }
        object LineMercury : TranslationKey() { override val key = "palmistry.line.mercury" }
        object LineMarriage : TranslationKey() { override val key = "palmistry.line.marriage" }
        object LineIntuition : TranslationKey() { override val key = "palmistry.line.intuition" }
        object LineHealth : TranslationKey() { override val key = "palmistry.line.health" }

        object HandUnknown : TranslationKey() { override val key = "palmistry.hand.unknown" }
        object ValidationSelectedHandPrefix : TranslationKey() { override val key = "palmistry.validation.selected_hand_prefix" }
        object ValidationDetectedHandPrefix : TranslationKey() { override val key = "palmistry.validation.detected_hand_prefix" }
        object ValidationResultPrefix : TranslationKey() { override val key = "palmistry.validation.validation_result_prefix" }
        object ValidationPassDesc : TranslationKey() { override val key = "palmistry.validation.pass_desc" }
        object ValidationWrongHandDesc : TranslationKey() { override val key = "palmistry.validation.wrong_hand_desc" }
        object ValidationRetryDesc : TranslationKey() { override val key = "palmistry.validation.retry_desc" }
        object ValidationSwitchProceed : TranslationKey() { override val key = "palmistry.validation.switch_proceed" }
        object ValidationRetakeHand : TranslationKey() { override val key = "palmistry.validation.retake_hand" }
        object QualityAssessmentTitle : TranslationKey() { override val key = "palmistry.quality_assessment_title" }
        object ProceedButton : TranslationKey() { override val key = "palmistry.proceed_button" }
        object ObservedTag : TranslationKey() { override val key = "palmistry.tag.observed" }
        object DerivedTag : TranslationKey() { override val key = "palmistry.tag.derived" }
        object TraditionalTag : TranslationKey() { override val key = "palmistry.tag.traditional" }
    }

    object Report {
        object PalmDisclaimerTitle : TranslationKey() {
            override val key = "palmistry.report.disclaimer.title"
        }

        object PalmDisclaimerBody : TranslationKey() {
            override val key = "palmistry.report.disclaimer.body"
        }

        object PalmOverviewTitle : TranslationKey() {
            override val key = "palmistry.report.overview.title"
        }

        object PalmSelectedHand : TranslationKey() {
            override val key = "palmistry.report.selected_hand"
        }

        object PalmShape : TranslationKey() {
            override val key = "palmistry.report.palm_shape"
        }

        object PalmLineClarity : TranslationKey() {
            override val key = "palmistry.report.line_clarity"
        }

        object PalmAnalysisVersion : TranslationKey() {
            override val key = "palmistry.report.analysis_version"
        }

        object TableFeature : TranslationKey() {
            override val key = "palmistry.report.table.feature"
        }

        object TableValue : TranslationKey() {
            override val key = "palmistry.report.table.value"
        }

        object PalmMajorLinesTitle : TranslationKey() {
            override val key = "palmistry.report.lines.title"
        }

        object PalmQnaTitle : TranslationKey() {
            override val key = "palmistry.report.qna.title"
        }

        object PalmAttrTitle : TranslationKey() {
            override val key = "palmistry.report.attr.title"
        }

        object PalmAttrBody : TranslationKey() {
            override val key = "palmistry.report.attr.body"
        }

        object PalmCautionPrefix : TranslationKey() {
            override val key = "palmistry.report.caution_prefix"
        }
    }


    object Gemstone {
        object Title : TranslationKey() {
            override val key = "gemstone.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "gemstone.subtitle"
        }

        object ExploreCatalog : TranslationKey() {
            override val key = "gemstone.explore_catalog"
        }

        object NavaratnaMandala : TranslationKey() {
            override val key = "gemstone.navaratna_mandala"
        }

        object MyInventory : TranslationKey() {
            override val key = "gemstone.my_inventory"
        }

        object Compatibility : TranslationKey() {
            override val key = "gemstone.compatibility"
        }

        object Recommendations : TranslationKey() {
            override val key = "gemstone.recommendations"
        }

        object CertificateInspection : TranslationKey() {
            override val key = "gemstone.certificate_inspection"
        }

        object History : TranslationKey() {
            override val key = "gemstone.history"
        }

        object ReportPreview : TranslationKey() {
            override val key = "gemstone.report_preview"
        }

        object AddGemstone : TranslationKey() {
            override val key = "gemstone.add_gemstone"
        }

        object EditGemstone : TranslationKey() {
            override val key = "gemstone.edit_gemstone"
        }

        object RemoveGemstone : TranslationKey() {
            override val key = "gemstone.remove_gemstone"
        }

        object Carats : TranslationKey() {
            override val key = "gemstone.carats"
        }

        object Metal : TranslationKey() {
            override val key = "gemstone.metal"
        }

        object Finger : TranslationKey() {
            override val key = "gemstone.finger"
        }

        object IsWorn : TranslationKey() {
            override val key = "gemstone.is_worn"
        }

        object Notes : TranslationKey() {
            override val key = "gemstone.notes"
        }

        object Save : TranslationKey() {
            override val key = "gemstone.save"
        }

        object CheckCompatibilityAction : TranslationKey() {
            override val key = "gemstone.check_compat_action"
        }

        object GenerateRecommendationsAction : TranslationKey() {
            override val key = "gemstone.generate_recs_action"
        }

        object CameraAction : TranslationKey() {
            override val key = "gemstone.camera_action"
        }

        object GalleryAction : TranslationKey() {
            override val key = "gemstone.gallery_action"
        }

        object InspectAction : TranslationKey() {
            override val key = "gemstone.inspect_action"
        }

        object DisclaimerTitle : TranslationKey() {
            override val key = "gemstone.disclaimer_title"
        }

        object DisclaimerBody : TranslationKey() {
            override val key = "gemstone.disclaimer_body"
        }
    }

    object Gita {
        object Title : TranslationKey() {
            override val key = "gita.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "gita.subtitle"
        }
    }

    object GarudaPuran {
        object Title : TranslationKey() {
            override val key = "garuda.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "garuda.subtitle"
        }
    }

    object LalKitab {
        object Title : TranslationKey() {
            override val key = "lalkitab.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "lalkitab.subtitle"
        }
    }

    object AiAssistant {
        object Title : TranslationKey() {
            override val key = "ai.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "ai.subtitle"
        }
    }

    object Guidance {
        object Title : TranslationKey() {
            override val key = "guidance.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "guidance.subtitle"
        }

        object Morning : TranslationKey() {
            override val key = "guidance.morning"
        }

        object Night : TranslationKey() {
            override val key = "guidance.night"
        }
    }

    object Wallpaper {
        object Title : TranslationKey() {
            override val key = "wallpaper.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "wallpaper.subtitle"
        }

        object GeneratePrompt : TranslationKey() {
            override val key = "wallpaper.generate_prompt"
        }

        object CopyPrompt : TranslationKey() {
            override val key = "wallpaper.copy_prompt"
        }
    }

    // -------------------------------------------------------------------------
    // Accessibility strings
    // -------------------------------------------------------------------------
    object Accessibility {
        object LanguageSelectorLabel : TranslationKey() { override val key = "a11y.language_selector" }
        object SelectedLanguage : TranslationKey() { override val key = "a11y.selected_language" }
        object LoadingIndicator : TranslationKey() { override val key = "a11y.loading" }
        object ErrorMessage : TranslationKey() { override val key = "a11y.error_message" }
        object RetrogradeIndicator : TranslationKey() { override val key = "a11y.retrograde" }
        object TarotCardDrawn : TranslationKey() {
            override val key = "a11y.tarot_card_drawn"
        }
    }

    // -------------------------------------------------------------------------
    // Tarot Conversational Experience (Phase 8.9)
    // -------------------------------------------------------------------------
    object TarotSession {
        object ReadingLockTitle : TranslationKey() {
            override val key = "tarot.session.lock.title"
        }

        object ReadingLockMessage : TranslationKey() {
            override val key = "tarot.session.lock.message"
        }

        object ReadingLockRemaining : TranslationKey() {
            override val key = "tarot.session.lock.remaining"
        }

        object ReadingLockNextAvailable : TranslationKey() {
            override val key = "tarot.session.lock.next_available"
        }

        object ContinueExistingReading : TranslationKey() {
            override val key = "tarot.session.continue_existing"
        }

        object ViewHistory : TranslationKey() {
            override val key = "tarot.session.view_history"
        }

        object Close : TranslationKey() {
            override val key = "tarot.session.close"
        }

        object CurrentReadingAvailable : TranslationKey() {
            override val key = "tarot.session.current_available"
        }

        object AskQuestionTitle : TranslationKey() {
            override val key = "tarot.question.title"
        }

        object AskQuestionPlaceholder : TranslationKey() {
            override val key = "tarot.question.placeholder"
        }

        object SendQuestion : TranslationKey() {
            override val key = "tarot.question.send"
        }

        object AnalyzingCards : TranslationKey() {
            override val key = "tarot.question.analyzing"
        }

        object AnswerTitle : TranslationKey() {
            override val key = "tarot.answer.title"
        }

        object QuestionDisclaimer : TranslationKey() {
            override val key = "tarot.question.disclaimer"
        }

        object InsufficientEvidence : TranslationKey() {
            override val key = "tarot.answer.insufficient_evidence"
        }

        object ClarificationOffer : TranslationKey() {
            override val key = "tarot.clarification.offer"
        }

        object DrawClarificationCard : TranslationKey() {
            override val key = "tarot.clarification.draw_card"
        }

        object ContinueWithoutClarification : TranslationKey() {
            override val key = "tarot.clarification.continue_without"
        }

        object ClarificationCardTitle : TranslationKey() {
            override val key = "tarot.clarification.card_title"
        }

        object ClarificationLimitReached : TranslationKey() {
            override val key = "tarot.clarification.limit_reached"
        }

        object TimelineTitle : TranslationKey() {
            override val key = "tarot.timeline.title"
        }

        object ReadingStarted : TranslationKey() {
            override val key = "tarot.timeline.reading_started"
        }

        object CardsDrawn : TranslationKey() {
            override val key = "tarot.timeline.cards_drawn"
        }

        object CardsRevealed : TranslationKey() {
            override val key = "tarot.timeline.cards_revealed"
        }

        object QuestionAsked : TranslationKey() {
            override val key = "tarot.timeline.question_asked"
        }

        object AiAnswer : TranslationKey() {
            override val key = "tarot.timeline.ai_answer"
        }

        object FeedbackSubmitted : TranslationKey() {
            override val key = "tarot.timeline.feedback_submitted"
        }

        object ReadingSatisfied : TranslationKey() {
            override val key = "tarot.timeline.reading_satisfied"
        }

        object FeedbackTitle : TranslationKey() {
            override val key = "tarot.feedback.title"
        }

        object FeedbackPrompt : TranslationKey() {
            override val key = "tarot.feedback.prompt"
        }

        object FeedbackImprovePrompt : TranslationKey() {
            override val key = "tarot.feedback.improve_prompt"
        }

        object SubmitFeedback : TranslationKey() {
            override val key = "tarot.feedback.submit"
        }

        object SkipFeedback : TranslationKey() {
            override val key = "tarot.feedback.skip"
        }

        object ImSatisfied : TranslationKey() {
            override val key = "tarot.satisfied.button"
        }

        object CardFeedbackTitle : TranslationKey() {
            override val key = "tarot.card_feedback.title"
        }

        object CardFeedbackHelpful : TranslationKey() {
            override val key = "tarot.card_feedback.helpful"
        }

        object CardFeedbackClear : TranslationKey() {
            override val key = "tarot.card_feedback.clear"
        }

        object CardFeedbackConfusing : TranslationKey() {
            override val key = "tarot.card_feedback.confusing"
        }

        object CardFeedbackNotRelevant : TranslationKey() {
            override val key = "tarot.card_feedback.not_relevant"
        }

        object CardFeedbackNeedContext : TranslationKey() {
            override val key = "tarot.card_feedback.need_context"
        }

        object AnswerHelpfulPrompt : TranslationKey() {
            override val key = "tarot.answer_feedback.prompt"
        }

        object AnswerHelpfulYes : TranslationKey() {
            override val key = "tarot.answer_feedback.yes"
        }

        object AnswerHelpfulNo : TranslationKey() {
            override val key = "tarot.answer_feedback.no"
        }

        object TarotReportTitle : TranslationKey() {
            override val key = "tarot.report.title"
        }

        object LanguageSelector : TranslationKey() {
            override val key = "tarot.language.selector"
        }

        object StatusActive : TranslationKey() {
            override val key = "tarot.session.status.active"
        }

        object StatusSatisfied : TranslationKey() {
            override val key = "tarot.session.status.satisfied"
        }

        object CurrentReadingDesc : TranslationKey() {
            override val key = "tarot.session.current_available_desc"
        }

        object ContinueReading : TranslationKey() {
            override val key = "tarot.session.continue_reading"
        }

        object DrawingClarification : TranslationKey() {
            override val key = "tarot.clarification.drawing"
        }

        object ClarificationCardWithName : TranslationKey() {
            override val key = "tarot.clarification.card_with_name"
        }

        object PrimarySpread : TranslationKey() {
            override val key = "tarot.timeline.primary_spread"
        }

        object FollowUpQuestion : TranslationKey() {
            override val key = "tarot.timeline.follow_up"
        }

        object QuestionNumbered : TranslationKey() {
            override val key = "tarot.timeline.question_numbered"
        }

        object SpreadDeckFormat : TranslationKey() {
            override val key = "tarot.timeline.spread_deck_format"
        }
    }

    object NumerologyExperience {
        object Title : TranslationKey() {
            override val key = "numerology.title"
        }

        object Subtitle : TranslationKey() {
            override val key = "numerology.subtitle"
        }

        object CalculateTab : TranslationKey() {
            override val key = "numerology.tab.calculate"
        }

        object CompareTab : TranslationKey() {
            override val key = "numerology.tab.compare"
        }

        object HistoryTab : TranslationKey() {
            override val key = "numerology.tab.history"
        }

        object SelectTradition : TranslationKey() {
            override val key = "numerology.select_tradition"
        }

        object DateOfBirth : TranslationKey() {
            override val key = "numerology.dob"
        }

        object FullName : TranslationKey() {
            override val key = "numerology.full_name"
        }

        object CalculateButton : TranslationKey() {
            override val key = "numerology.calculate_button"
        }

        object ResetButton : TranslationKey() {
            override val key = "numerology.reset_button"
        }

        object ReportButton : TranslationKey() {
            override val key = "numerology.report_button"
        }

        object HowCalculated : TranslationKey() {
            override val key = "numerology.how_calculated"
        }

        object MethodAndSource : TranslationKey() {
            override val key = "numerology.method_and_source"
        }

        object ReflectiveInsight : TranslationKey() {
            override val key = "numerology.reflective_insight"
        }

        object DisclaimerTitle : TranslationKey() {
            override val key = "numerology.disclaimer.title"
        }

        object DisclaimerBody : TranslationKey() {
            override val key = "numerology.disclaimer.body"
        }

        object AiExplainButton : TranslationKey() {
            override val key = "numerology.ai_explain_button"
        }

        // Phase 10.5 Numerology Interpretation Notices & Labels
        object NoticeContemplative : TranslationKey() {
            override val key = "numerology.interp.notice.contemplative"
        }

        object NoticeNoPersonalityMnemonic : TranslationKey() {
            override val key = "numerology.interp.notice.no_personality_mnemonic"
        }

        object NoticeAlphanumeric : TranslationKey() {
            override val key = "numerology.interp.notice.alphanumeric"
        }

        object NoticeHistoricalContext : TranslationKey() {
            override val key = "numerology.interp.notice.historical_context"
        }

        object NoticeEthicalGuidance : TranslationKey() {
            override val key = "numerology.interp.notice.ethical_guidance"
        }

        object StrengthsLabel : TranslationKey() {
            override val key = "numerology.interp.strengths_label"
        }

        object ChallengesLabel : TranslationKey() {
            override val key = "numerology.interp.challenges_label"
        }

        object SourceCitationLabel : TranslationKey() {
            override val key = "numerology.interp.source_citation_label"
        }

        /** Dynamic key wrapper for catalog lookups */
        class DynamicInterpretation(override val key: String) : TranslationKey()
    }
}
