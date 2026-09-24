package com.aynvora.core.gita

import com.aynvora.core.result.AynvoraResult

/**
 * Metadata for a Bhagavad Gita publication/commentary edition.
 */
data class GitaSourceEdition(
    val editionId: String,
    val title: String,
    val translatorOrCommentator: String,
    val language: String,
    val publicationYear: Int? = null,
    val isPublicDomain: Boolean = true,
)

/**
 * Distinct Sanskrit shloka with edition-specific translation and commentary.
 */
data class GitaVerse(
    val chapterNumber: Int,
    val verseNumber: Int,
    val sanskritDevenagari: String,
    val transliterationIast: String,
    val wordMeanings: Map<String, String> = emptyMap(),
    val translation: String,
    val commentary: String? = null,
    val sourceEdition: GitaSourceEdition,
    val themeTags: List<String> = emptyList(),
)

/**
 * Chapter descriptor for Bhagavad Gita.
 */
data class GitaChapter(
    val chapterNumber: Int,
    val titleSanskrit: String,
    val titleEnglish: String,
    val titleHindi: String,
    val totalVerses: Int,
    val summary: String,
)

/**
 * Domain repository contract for Bhagavad Gita text and reflection.
 */
interface GitaRepository {
    suspend fun getChapters(): AynvoraResult<List<GitaChapter>>
    suspend fun getVerse(
        chapterNumber: Int,
        verseNumber: Int,
        language: String = "en"
    ): AynvoraResult<GitaVerse>

    suspend fun getVersesForChapter(
        chapterNumber: Int,
        language: String = "en"
    ): AynvoraResult<List<GitaVerse>>

    suspend fun searchVersesByTheme(
        themeTag: String,
        language: String = "en"
    ): AynvoraResult<List<GitaVerse>>
}
