package com.aynvora.gita

import com.aynvora.contracts.AynvoraResult
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Source provenance — never modified, never AI-generated
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Identifies the upstream dataset that backs all Gita scripture in AYNVORA.
 *
 * Source: https://github.com/gita/gita — Public Domain
 * Commit: c6fce39595445768876ddbb8d1268a9c935e1d2b
 */
object GitaDataSource {
    const val REPOSITORY_URL = "https://github.com/gita/gita"
    const val COMMIT_SHA = "c6fce39595445768876ddbb8d1268a9c935e1d2b"
    const val LICENSE = "Public Domain"
    const val ATTRIBUTION = "Source: Bhagavad Gita API (github.com/gita/gita) — Public Domain"
    const val SCHEMA_VERSION = 1
}

// ─────────────────────────────────────────────────────────────────────────────
// Author
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A translator or commentator recognized in the source dataset.
 */
@Serializable
data class GitaAuthor(
    val authorId: Int,
    val name: String,
)

// ─────────────────────────────────────────────────────────────────────────────
// Translation  (scripture-grounded, never AI-generated)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single translator's rendering of one verse into one language.
 *
 * INVARIANT: [description] is canonical published text from [GitaDataSource].
 *            It must NEVER be replaced by or mixed with AI-generated text.
 */
@Serializable
data class GitaTranslation(
    val translationId: Int,
    val verseId: Int,
    val authorId: Int,
    val authorName: String,
    val language: String,         // "english" | "hindi" | "sanskrit"
    val description: String,      // canonical translated text — immutable
)

// ─────────────────────────────────────────────────────────────────────────────
// Commentary  (scripture-grounded, never AI-generated)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single commentator's annotation of one verse.
 *
 * INVARIANT: [description] is canonical commentary text from [GitaDataSource].
 *            It must NEVER be replaced by or mixed with AI-generated text.
 */
@Serializable
data class GitaCommentary(
    val commentaryId: Int,
    val verseId: Int,
    val authorId: Int,
    val authorName: String,
    val language: String,
    val description: String,      // canonical commentary text — immutable
)

// ─────────────────────────────────────────────────────────────────────────────
// Source Edition / Heritage provenance
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class GitaSourceEdition(
    val editionId: String = "GITA_PUBLIC_DOMAIN",
    val title: String = "Bhagavad Gita API (gita/gita) — Public Domain",
    val translatorOrCommentator: String = "Canonical Translators",
    val language: String = "en",
    val publicationYear: Int? = null,
    val isPublicDomain: Boolean = true,
) {
    companion object {
        val PUBLIC_DOMAIN = GitaSourceEdition()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Verse  (core canonical unit)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Distinct Sanskrit śloka with sourced translations and commentary.
 *
 * INVARIANT:
 *  - [sanskritDevanagari] and [transliteration] come directly from [GitaDataSource].
 *  - [translations] and [commentaries] are sourced from [GitaDataSource].
 *  - AI reflection content is NEVER stored in this model.
 */
@Serializable
data class GitaVerse(
    val verseId: Int = 0,
    val chapterNumber: Int = 0,
    val verseNumber: Int = 0,
    val verseOrder: Int = 0,
    val title: String = "",
    val sanskritDevanagari: String = "",    // canonical Devanāgarī text — immutable
    val transliteration: String = "",       // IAST/standard — immutable
    val wordMeanings: String = "",          // raw semicolon-separated string from source
    val translations: List<GitaTranslation> = emptyList(),
    val commentaries: List<GitaCommentary> = emptyList(),
    val themeTags: List<String> = emptyList(),
) {
    val sanskritDevenagari: String get() = sanskritDevanagari
    val transliterationIast: String get() = transliteration
    val translation: String get() = translations.firstOrNull()?.description.orEmpty()
    val commentary: String? get() = commentaries.firstOrNull()?.description
    val sourceEdition: GitaSourceEdition get() = GitaSourceEdition.PUBLIC_DOMAIN

    constructor(
        chapterNumber: Int,
        verseNumber: Int,
        sanskritDevenagari: String,
        transliterationIast: String,
        translation: String,
        sourceEdition: GitaSourceEdition = GitaSourceEdition.PUBLIC_DOMAIN,
        wordMeanings: Map<String, String> = emptyMap(),
        commentary: String? = null,
        themeTags: List<String> = emptyList(),
        verseId: Int = 0,
        verseOrder: Int = 0,
        title: String = "BG $chapterNumber.$verseNumber",
    ) : this(
        verseId = verseId,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        verseOrder = verseOrder,
        title = title,
        sanskritDevanagari = sanskritDevenagari,
        transliteration = transliterationIast,
        wordMeanings = wordMeanings.entries.joinToString("; ") { "${it.key} - ${it.value}" },
        translations = listOf(
            GitaTranslation(
                translationId = 0,
                verseId = verseId,
                authorId = 0,
                authorName = sourceEdition.translatorOrCommentator,
                language = sourceEdition.language,
                description = translation,
            )
        ),
        commentaries = commentary?.let {
            listOf(
                GitaCommentary(
                    commentaryId = 0,
                    verseId = verseId,
                    authorId = 0,
                    authorName = sourceEdition.translatorOrCommentator,
                    language = sourceEdition.language,
                    description = it,
                )
            )
        } ?: emptyList(),
        themeTags = themeTags,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Chapter
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Chapter descriptor for Bhagavad Gita.
 */
@Serializable
data class GitaChapter(
    val chapterId: Int = 0,
    val chapterNumber: Int = 0,
    val nameSanskrit: String = "",           // Devanāgarī name
    val nameTranslation: String = "",        // English yoga name
    val nameTransliterated: String = "",     // e.g. "Arjuna Viṣāda Yoga"
    val nameMeaning: String = "",            // e.g. "Arjuna's Grief"
    val chapterSummaryEnglish: String = "",
    val chapterSummaryHindi: String = "",
    val versesCount: Int = 0,
    val imageName: String = "",
) {
    val titleSanskrit: String get() = nameSanskrit
    val titleEnglish: String get() = nameTranslation
    val titleHindi: String get() = chapterSummaryHindi
    val totalVerses: Int get() = versesCount
    val summary: String get() = chapterSummaryEnglish

    constructor(
        chapterNumber: Int,
        titleSanskrit: String,
        titleEnglish: String,
        titleHindi: String,
        totalVerses: Int,
        summary: String,
        chapterId: Int = chapterNumber,
        nameTransliterated: String = titleEnglish,
        nameMeaning: String = titleEnglish,
        imageName: String = "",
    ) : this(
        chapterId = chapterId,
        chapterNumber = chapterNumber,
        nameSanskrit = titleSanskrit,
        nameTranslation = titleEnglish,
        nameTransliterated = nameTransliterated,
        nameMeaning = nameMeaning,
        chapterSummaryEnglish = summary,
        chapterSummaryHindi = titleHindi,
        versesCount = totalVerses,
        imageName = imageName,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Repository contract
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Domain repository contract for Bhagavad Gita text.
 *
 * Scope rule: This interface returns ONLY sourced, canonical scripture data.
 * AI reflection content is handled separately and must never be merged here.
 */
interface GitaRepository {
    /** All 18 chapters, ordered. */
    suspend fun getChapters(): AynvoraResult<List<GitaChapter>>

    /** A single chapter by number. */
    suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter>

    /** All verses for a chapter, ordered. Translations/commentaries filtered by [language]. */
    suspend fun getVersesForChapter(
        chapterNumber: Int,
        language: String = "english",
    ): AynvoraResult<List<GitaVerse>>

    /** Single verse with all translations and commentaries for [language]. */
    suspend fun getVerse(
        chapterNumber: Int,
        verseNumber: Int,
        language: String = "english",
    ): AynvoraResult<GitaVerse>

    /** Full-text search across Sanskrit, transliteration, and English translations. */
    suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>>

    /** Search verses by theme tag (for MultiFeatureOrchestrator). Default searches across verses. */
    suspend fun searchVersesByTheme(
        themeTag: String,
        language: String = "english",
    ): AynvoraResult<List<GitaVerse>> = searchVerses(themeTag)

    /** All known translators/commentators. */
    suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>>

    /** Whether the local Room database has been seeded with source data. */
    suspend fun isSeeded(): Boolean

    /** Total verse count in local store (integrity check). */
    suspend fun getSeededVerseCount(): Int
}
