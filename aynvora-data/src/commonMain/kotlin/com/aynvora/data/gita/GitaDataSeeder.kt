package com.aynvora.data.gita

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

@Serializable
private data class AuthorDto(val id: Int, val name: String)

@Serializable
private data class ChapterDto(
    val id: Int,
    @SerialName("chapter_number") val chapterNumber: Int,
    val name: String? = null,
    @SerialName("name_translation") val nameTranslation: String? = null,
    @SerialName("name_transliterated") val nameTransliterated: String? = null,
    @SerialName("name_meaning") val nameMeaning: String? = null,
    @SerialName("chapter_summary") val chapterSummary: String? = null,
    @SerialName("chapter_summary_hindi") val chapterSummaryHindi: String? = null,
    @SerialName("verses_count") val versesCount: Int = 0,
    @SerialName("image_name") val imageName: String? = null,
)

@Serializable
private data class VerseDto(
    val id: Int,
    @SerialName("chapter_number") val chapterNumber: Int,
    @SerialName("verse_number") val verseNumber: Int,
    @SerialName("verse_order") val verseOrder: Int,
    val title: String? = null,
    val text: String? = null,
    val transliteration: String? = null,
    @SerialName("word_meanings") val wordMeanings: String? = null,
)

@Serializable
private data class TranslationDto(
    val id: Int,
    @SerialName("verse_id") val verseId: Int,
    @SerialName("author_id") val authorId: Int,
    val authorName: String? = null,
    val lang: String? = null,
    val description: String? = null,
)

@Serializable
private data class CommentaryDto(
    val id: Int,
    @SerialName("verse_id") val verseId: Int,
    @SerialName("author_id") val authorId: Int,
    val authorName: String? = null,
    val lang: String? = null,
    val description: String? = null,
)

/**
 * One-time offline seeder for Bhagavad Gita data using kotlinx.serialization.
 * Supports both Android assets and JVM classpath resources.
 */
class GitaDataSeeder(
    private val inputStreamProvider: (String) -> InputStream,
    private val dao: GitaDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Android constructor convenience
    constructor(context: Any, dao: GitaDao) : this(
        inputStreamProvider = { path ->
            val assetManager = context.javaClass.getMethod("getAssets").invoke(context)
            val openMethod = assetManager.javaClass.getMethod("open", String::class.java)
            openMethod.invoke(assetManager, path) as InputStream
        },
        dao = dao
    )

    // JVM constructor convenience
    constructor(dao: GitaDao) : this(
        inputStreamProvider = { path ->
            Thread.currentThread().contextClassLoader.getResourceAsStream(path)
                ?: throw IllegalStateException("Gita resource not found on classpath: $path")
        },
        dao = dao
    )

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

    private fun parseAuthors(): List<GitaAuthorRoomEntity> {
        val text = inputStreamProvider("gita/authors.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<AuthorDto>>(text)
        return dtos.map { GitaAuthorRoomEntity(authorId = it.id, name = it.name) }
    }

    private fun parseChapters(): List<GitaChapterRoomEntity> {
        val text = inputStreamProvider("gita/chapters.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<ChapterDto>>(text)
        return dtos.map { o ->
            GitaChapterRoomEntity(
                chapterId = o.id,
                chapterNumber = o.chapterNumber,
                nameSanskrit = o.name ?: "",
                nameTranslation = o.nameTranslation ?: "",
                nameTransliterated = o.nameTransliterated ?: "",
                nameMeaning = o.nameMeaning ?: "",
                chapterSummaryEnglish = o.chapterSummary ?: "",
                chapterSummaryHindi = o.chapterSummaryHindi ?: "",
                versesCount = o.versesCount,
                imageName = o.imageName ?: "",
            )
        }
    }

    private fun parseVerses(): List<GitaVerseRoomEntity> {
        val text = inputStreamProvider("gita/verse.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<VerseDto>>(text)
        return dtos.map { o ->
            GitaVerseRoomEntity(
                verseId = o.id,
                chapterNumber = o.chapterNumber,
                verseNumber = o.verseNumber,
                verseOrder = o.verseOrder,
                title = o.title ?: "",
                sanskritDevanagari = o.text ?: "",
                transliteration = o.transliteration ?: "",
                wordMeanings = o.wordMeanings ?: "",
            )
        }
    }

    private fun parseTranslations(): List<GitaTranslationRoomEntity> {
        val text =
            inputStreamProvider("gita/translation.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<TranslationDto>>(text)
        return dtos.map { o ->
            GitaTranslationRoomEntity(
                translationId = o.id,
                verseId = o.verseId,
                authorId = o.authorId,
                authorName = o.authorName ?: "",
                language = o.lang ?: "english",
                description = o.description ?: "",
            )
        }
    }

    private fun parseCommentaries(): List<GitaCommentaryRoomEntity> {
        val text =
            inputStreamProvider("gita/commentary.json").bufferedReader().use { it.readText() }
        val dtos = json.decodeFromString<List<CommentaryDto>>(text)
        return dtos.map { o ->
            GitaCommentaryRoomEntity(
                commentaryId = o.id,
                verseId = o.verseId,
                authorId = o.authorId,
                authorName = o.authorName ?: "",
                language = o.lang ?: "hindi",
                description = o.description ?: "",
            )
        }
    }
}
