package com.aynvora.data.gita

import android.content.Context
import com.aynvora.core.gita.GitaDataSource
import com.aynvora.data.database.dao.GitaDao
import com.aynvora.data.database.entity.GitaAuthorRoomEntity
import com.aynvora.data.database.entity.GitaChapterRoomEntity
import com.aynvora.data.database.entity.GitaCommentaryRoomEntity
import com.aynvora.data.database.entity.GitaSeedStateRoomEntity
import com.aynvora.data.database.entity.GitaTranslationRoomEntity
import com.aynvora.data.database.entity.GitaVerseRoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * One-time offline seeder for Bhagavad Gita data.
 *
 * SOURCE: https://github.com/gita/gita (Public Domain)
 * COMMIT: c6fce39595445768876ddbb8d1268a9c935e1d2b
 *
 * Reads the six JSON asset files and upserts all records into Room in a single
 * coroutine. Designed to run once at app startup. Re-entrance is guarded by
 * [GitaSeedStateRoomEntity.isSeeded] — if the commit SHA matches, seeding is skipped.
 *
 * INVARIANT: This class writes ONLY canonical source data.
 *            It must NEVER inject or modify scripture text.
 */
class GitaDataSeeder(
    private val context: Context,
    private val dao: GitaDao,
) {

    /**
     * Seeds the Room database from assets if not already seeded.
     * Safe to call on every app start — exits immediately if already up-to-date.
     *
     * @return true if seeding was performed, false if skipped.
     */
    suspend fun seedIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getSeedState()
        if (existing?.isSeeded == true && existing.sourceCommitSha == GitaDataSource.COMMIT_SHA) {
            return@withContext false
        }

        val authors = parseAuthors()
        val chapters = parseChapters()
        val verses = parseVerses()
        val translations = parseTranslations()
        val commentaries = parseCommentaries()

        dao.upsertAuthors(authors)
        dao.upsertChapters(chapters)
        dao.upsertVerses(verses)

        // Insert translations in chunks to avoid SQLite bind limit
        translations.chunked(500).forEach { dao.upsertTranslations(it) }
        commentaries.chunked(500).forEach { dao.upsertCommentaries(it) }

        dao.upsertSeedState(
            GitaSeedStateRoomEntity(
                isSeeded = true,
                seededVerseCount = verses.size,
                seededTranslationCount = translations.size,
                seededCommentaryCount = commentaries.size,
                sourceCommitSha = GitaDataSource.COMMIT_SHA,
                schemaVersion = GitaDataSource.SCHEMA_VERSION,
                seededAtEpochMs = System.currentTimeMillis(),
            )
        )

        return@withContext true
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private fun parseAuthors(): List<GitaAuthorRoomEntity> {
        val arr = readAsset("gita/authors.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            GitaAuthorRoomEntity(
                authorId = o.getInt("id"),
                name = o.getString("name"),
            )
        }
    }

    private fun parseChapters(): List<GitaChapterRoomEntity> {
        val arr = readAsset("gita/chapters.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            GitaChapterRoomEntity(
                chapterId = o.getInt("id"),
                chapterNumber = o.getInt("chapter_number"),
                nameSanskrit = o.optString("name", ""),
                nameTranslation = o.optString("name_translation", ""),
                nameTransliterated = o.optString("name_transliterated", ""),
                nameMeaning = o.optString("name_meaning", ""),
                chapterSummaryEnglish = o.optString("chapter_summary", ""),
                chapterSummaryHindi = o.optString("chapter_summary_hindi", ""),
                versesCount = o.optInt("verses_count", 0),
                imageName = o.optString("image_name", ""),
            )
        }
    }

    private fun parseVerses(): List<GitaVerseRoomEntity> {
        val arr = readAsset("gita/verse.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            GitaVerseRoomEntity(
                verseId = o.getInt("id"),
                chapterNumber = o.getInt("chapter_number"),
                verseNumber = o.getInt("verse_number"),
                verseOrder = o.getInt("verse_order"),
                title = o.optString("title", ""),
                sanskritDevanagari = o.optString("text", ""),
                transliteration = o.optString("transliteration", ""),
                wordMeanings = o.optString("word_meanings", ""),
            )
        }
    }

    private fun parseTranslations(): List<GitaTranslationRoomEntity> {
        val arr = readAsset("gita/translation.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            GitaTranslationRoomEntity(
                translationId = o.getInt("id"),
                verseId = o.getInt("verse_id"),
                authorId = o.getInt("author_id"),
                authorName = o.optString("authorName", ""),
                language = o.optString("lang", "english"),
                description = o.optString("description", ""),
            )
        }
    }

    private fun parseCommentaries(): List<GitaCommentaryRoomEntity> {
        val arr = readAsset("gita/commentary.json")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            GitaCommentaryRoomEntity(
                commentaryId = o.getInt("id"),
                verseId = o.getInt("verse_id"),
                authorId = o.getInt("author_id"),
                authorName = o.optString("authorName", ""),
                language = o.optString("lang", "hindi"),
                description = o.optString("description", ""),
            )
        }
    }

    private fun readAsset(path: String): JSONArray {
        val text = context.assets.open(path).bufferedReader().use { it.readText() }
        return JSONArray(text)
    }
}
