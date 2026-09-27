package com.aynvora.data.gita

import com.aynvora.core.gita.GitaAuthor
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaCommentary
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaTranslation
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.database.dao.GitaDao
import com.aynvora.data.database.entity.GitaChapterRoomEntity
import com.aynvora.data.database.entity.GitaVerseRoomEntity

/**
 * Production offline-first implementation of [GitaRepository], backed by Room.
 *
 * All data comes from the gita/gita dataset (Public Domain) seeded via [GitaDataSeeder].
 *
 * INVARIANT: This repository NEVER generates, modifies, or augments scripture text.
 *            It is a pure read-through layer over the canonical Room store.
 */
class RoomGitaRepository(
    private val dao: GitaDao,
) : GitaRepository {

    // ── Chapters ──────────────────────────────────────────────────────────────

    override suspend fun getChapters(): AynvoraResult<List<GitaChapter>> {
        return try {
            val entities = dao.getAllChapters()
            AynvoraResult.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getAllChapters",
                message = "Failed to load chapters: ${e.message}",
            )
        }
    }

    override suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter> {
        return try {
            val entity = dao.getChapterByNumber(chapterNumber)
                ?: return AynvoraResult.Failure.NotFound(
                    resourceId = "GITA_CHAPTER_$chapterNumber",
                    message = "Chapter $chapterNumber not found in local store.",
                )
            AynvoraResult.Success(entity.toDomain())
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getChapter",
                message = "Failed to load chapter $chapterNumber: ${e.message}",
            )
        }
    }

    // ── Verses ────────────────────────────────────────────────────────────────

    override suspend fun getVersesForChapter(
        chapterNumber: Int,
        language: String,
    ): AynvoraResult<List<GitaVerse>> {
        return try {
            val verseEntities = dao.getVersesForChapter(chapterNumber)
            if (verseEntities.isEmpty()) {
                return AynvoraResult.Failure.NotFound(
                    resourceId = "GITA_CHAPTER_VERSES_$chapterNumber",
                    message = "No verses found for chapter $chapterNumber.",
                )
            }
            val verseIds = verseEntities.map { it.verseId }
            val translations = dao.getTranslationsForVerses(verseIds, language)
                .groupBy { it.verseId }
            val commentaries = dao.getCommentariesForVerses(verseIds, language)
                .groupBy { it.verseId }

            val result = verseEntities.map { v ->
                v.toDomain(
                    translations = translations[v.verseId].orEmpty().map { it.toDomain() },
                    commentaries = commentaries[v.verseId].orEmpty().map { it.toDomain() },
                )
            }
            AynvoraResult.Success(result)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getVersesForChapter",
                message = "Failed to load verses for chapter $chapterNumber: ${e.message}",
            )
        }
    }

    override suspend fun getVerse(
        chapterNumber: Int,
        verseNumber: Int,
        language: String,
    ): AynvoraResult<GitaVerse> {
        return try {
            val verseEntity = dao.getVerse(chapterNumber, verseNumber)
                ?: return AynvoraResult.Failure.NotFound(
                    resourceId = "GITA_$chapterNumber.$verseNumber",
                    message = "Verse $chapterNumber.$verseNumber not found in local store.",
                )
            val translations = dao.getTranslationsForVerse(verseEntity.verseId, language)
            val commentaries = dao.getCommentariesForVerse(verseEntity.verseId, language)
            AynvoraResult.Success(
                verseEntity.toDomain(
                    translations = translations.map { it.toDomain() },
                    commentaries = commentaries.map { it.toDomain() },
                )
            )
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getVerse",
                message = "Failed to load verse $chapterNumber.$verseNumber: ${e.message}",
            )
        }
    }

    // ── Search ────────────────────────────────────────────────────────────────

    override suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>> {
        if (query.isBlank()) return AynvoraResult.Success(emptyList())
        return try {
            val trimmed = query.trim()
            // Search Sanskrit/transliteration first, then augment with English translation matches
            val sanskritMatches = dao.searchVersesBySanskrit(trimmed)
            val translationMatches = dao.searchVersesByTranslation(trimmed, "english")

            // Merge, preserving order, deduplicating by verseId
            val seen = mutableSetOf<Int>()
            val merged = (sanskritMatches + translationMatches).filter { seen.add(it.verseId) }

            val verseIds = merged.map { it.verseId }
            val translations = dao.getTranslationsForVerses(verseIds, "english")
                .groupBy { it.verseId }

            val result = merged.map { v ->
                v.toDomain(
                    translations = translations[v.verseId].orEmpty().map { it.toDomain() },
                    commentaries = emptyList(),
                )
            }
            AynvoraResult.Success(result)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "searchVerses",
                message = "Search failed: ${e.message}",
            )
        }
    }

    // ── Authors ───────────────────────────────────────────────────────────────

    override suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>> {
        return try {
            val authors = dao.getAllAuthors().map { GitaAuthor(it.authorId, it.name) }
            AynvoraResult.Success(authors)
        } catch (e: Exception) {
            AynvoraResult.Failure.StorageFailure(
                operation = "getAuthors",
                message = "Failed to load authors: ${e.message}",
            )
        }
    }

    // ── Seed State ────────────────────────────────────────────────────────────

    override suspend fun isSeeded(): Boolean {
        return dao.getSeedState()?.isSeeded == true
    }

    override suspend fun getSeededVerseCount(): Int {
        return dao.getVerseCount()
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun GitaChapterRoomEntity.toDomain() = GitaChapter(
        chapterId = chapterId,
        chapterNumber = chapterNumber,
        nameSanskrit = nameSanskrit,
        nameTranslation = nameTranslation,
        nameTransliterated = nameTransliterated,
        nameMeaning = nameMeaning,
        chapterSummaryEnglish = chapterSummaryEnglish,
        chapterSummaryHindi = chapterSummaryHindi,
        versesCount = versesCount,
        imageName = imageName,
    )

    private fun GitaVerseRoomEntity.toDomain(
        translations: List<GitaTranslation>,
        commentaries: List<GitaCommentary>,
    ) = GitaVerse(
        verseId = verseId,
        chapterNumber = chapterNumber,
        verseNumber = verseNumber,
        verseOrder = verseOrder,
        title = title,
        sanskritDevanagari = sanskritDevanagari,
        transliteration = transliteration,
        wordMeanings = wordMeanings,
        translations = translations,
        commentaries = commentaries,
    )

    private fun com.aynvora.data.database.entity.GitaTranslationRoomEntity.toDomain() =
        GitaTranslation(
            translationId = translationId,
            verseId = verseId,
            authorId = authorId,
            authorName = authorName,
            language = language,
            description = description,
        )

    private fun com.aynvora.data.database.entity.GitaCommentaryRoomEntity.toDomain() =
        GitaCommentary(
            commentaryId = commentaryId,
            verseId = verseId,
            authorId = authorId,
            authorName = authorName,
            language = language,
            description = description,
        )
}
