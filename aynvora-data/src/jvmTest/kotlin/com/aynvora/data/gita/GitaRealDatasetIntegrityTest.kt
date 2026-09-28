package com.aynvora.data.gita

import com.aynvora.core.gita.GitaDataSource
import com.aynvora.core.result.AynvoraResult
import com.aynvora.data.database.dao.GitaDao
import com.aynvora.data.database.entity.GitaAuthorRoomEntity
import com.aynvora.data.database.entity.GitaChapterRoomEntity
import com.aynvora.data.database.entity.GitaCommentaryRoomEntity
import com.aynvora.data.database.entity.GitaSeedStateRoomEntity
import com.aynvora.data.database.entity.GitaTranslationRoomEntity
import com.aynvora.data.database.entity.GitaVerseRoomEntity
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GitaRealDatasetIntegrityTest {

    private class FakeGitaDao : GitaDao {
        val authors = mutableListOf<GitaAuthorRoomEntity>()
        val chapters = mutableListOf<GitaChapterRoomEntity>()
        val verses = mutableListOf<GitaVerseRoomEntity>()
        val translations = mutableListOf<GitaTranslationRoomEntity>()
        val commentaries = mutableListOf<GitaCommentaryRoomEntity>()
        var seedState: GitaSeedStateRoomEntity? = null

        override suspend fun upsertChapters(entities: List<GitaChapterRoomEntity>) {
            chapters.addAll(entities)
        }

        override suspend fun getAllChapters(): List<GitaChapterRoomEntity> =
            chapters.sortedBy { it.chapterNumber }

        override suspend fun getChapterByNumber(chapterNumber: Int): GitaChapterRoomEntity? =
            chapters.find { it.chapterNumber == chapterNumber }

        override suspend fun upsertVerses(entities: List<GitaVerseRoomEntity>) {
            verses.addAll(entities)
        }

        override suspend fun getVersesForChapter(chapterNumber: Int): List<GitaVerseRoomEntity> =
            verses.filter { it.chapterNumber == chapterNumber }.sortedBy { it.verseOrder }

        override suspend fun getVerse(chapterNumber: Int, verseNumber: Int): GitaVerseRoomEntity? =
            verses.find { it.chapterNumber == chapterNumber && it.verseNumber == verseNumber }

        override suspend fun getVerseCount(): Int = verses.size

        override suspend fun searchVersesBySanskrit(query: String): List<GitaVerseRoomEntity> =
            verses.filter {
                it.sanskritDevanagari.contains(query, ignoreCase = true) ||
                    it.transliteration.contains(query, ignoreCase = true) ||
                    it.wordMeanings.contains(query, ignoreCase = true)
            }

        override suspend fun upsertTranslations(entities: List<GitaTranslationRoomEntity>) {
            translations.addAll(entities)
        }

        override suspend fun getTranslationsForVerse(
            verseId: Int,
            language: String,
        ): List<GitaTranslationRoomEntity> =
            translations.filter { it.verseId == verseId && (language.isBlank() || it.language == language) }

        override suspend fun getTranslationsForVerses(
            verseIds: List<Int>,
            language: String,
        ): List<GitaTranslationRoomEntity> =
            translations.filter { verseIds.contains(it.verseId) && (language.isBlank() || it.language == language) }

        override suspend fun getTranslationCount(): Int = translations.size

        override suspend fun searchVersesByTranslation(
            query: String,
            language: String,
        ): List<GitaVerseRoomEntity> {
            val matchingVerseIds = translations.filter {
                it.description.contains(query, ignoreCase = true) &&
                    (language.isBlank() || it.language == language)
            }.map { it.verseId }.toSet()
            return verses.filter { matchingVerseIds.contains(it.verseId) }
        }

        override suspend fun upsertCommentaries(entities: List<GitaCommentaryRoomEntity>) {
            commentaries.addAll(entities)
        }

        override suspend fun getCommentariesForVerse(
            verseId: Int,
            language: String,
        ): List<GitaCommentaryRoomEntity> =
            commentaries.filter { it.verseId == verseId && (language.isBlank() || it.language == language) }

        override suspend fun getCommentariesForVerses(
            verseIds: List<Int>,
            language: String,
        ): List<GitaCommentaryRoomEntity> =
            commentaries.filter { verseIds.contains(it.verseId) && (language.isBlank() || it.language == language) }

        override suspend fun upsertAuthors(entities: List<GitaAuthorRoomEntity>) {
            authors.addAll(entities)
        }

        override suspend fun getAllAuthors(): List<GitaAuthorRoomEntity> = authors

        override suspend fun getSeedState(): GitaSeedStateRoomEntity? = seedState

        override suspend fun upsertSeedState(entity: GitaSeedStateRoomEntity) {
            seedState = entity
        }
    }

    @Test
    fun testRealGitaDataSeedingAndRepositoryIntegrity() = runBlocking {
        val fakeDao = FakeGitaDao()
        val seeder = GitaDataSeeder(dao = fakeDao)

        // 1. First seed execution must parse all canonical JSON resources
        val firstSeed = seeder.seedIfNeeded()
        assertTrue(firstSeed, "Initial seeding must succeed")

        // 2. Second seed must be a no-op (idempotent)
        val secondSeed = seeder.seedIfNeeded()
        assertFalse(secondSeed, "Subsequent seeding must return false (already seeded)")

        // 3. Verify exactly 18 chapters seeded
        assertEquals(18, fakeDao.chapters.size, "Gita must have exactly 18 chapters")
        val totalVersesCount = fakeDao.chapters.sumOf { it.versesCount }
        assertEquals(701, totalVersesCount, "All 18 chapters must sum to 701 verses")

        // 4. Verify chapter 1 and 2 metadata
        val ch1 = fakeDao.getChapterByNumber(1)
        assertNotNull(ch1)
        assertEquals("अर्जुनविषादयोग", ch1.nameSanskrit)
        assertEquals(47, ch1.versesCount)

        val ch2 = fakeDao.getChapterByNumber(2)
        assertNotNull(ch2)
        assertEquals("सांख्ययोग", ch2.nameSanskrit)
        assertEquals(72, ch2.versesCount)

        // 5. Verify exactly 701 verses seeded
        assertEquals(701, fakeDao.verses.size, "Total seeded verses must be 701")

        // 6. Verify famous verse 2.47 (Nishkama Karma)
        val v247 = fakeDao.getVerse(chapterNumber = 2, verseNumber = 47)
        assertNotNull(v247)
        assertTrue(
            v247.sanskritDevanagari.contains("कर्मण्येवाधिकारस्ते"),
            "Verse 2.47 must contain canonical Sanskrit: कर्मण्येवाधिकारस्ते"
        )
        assertTrue(
            v247.transliteration.contains("karmaṇy-evādhikāras"),
            "Verse 2.47 transliteration must be correct"
        )

        // 7. Verify authors, translations, and commentaries
        assertTrue(fakeDao.authors.isNotEmpty(), "Authors list must not be empty")
        assertTrue(fakeDao.translations.isNotEmpty(), "Translations must not be empty")
        assertTrue(fakeDao.commentaries.isNotEmpty(), "Commentaries must not be empty")

        // 8. Test RoomGitaRepository backed by this seeded data
        val repository = RoomGitaRepository(dao = fakeDao)

        val chaptersResult = repository.getChapters()
        assertTrue(chaptersResult is AynvoraResult.Success)
        assertEquals(18, chaptersResult.value.size)

        val chapter2Result = repository.getChapter(2)
        assertTrue(chapter2Result is AynvoraResult.Success)
        assertEquals("सांख्ययोग", chapter2Result.value.nameSanskrit)

        val ch2VersesResult = repository.getVersesForChapter(2, "english")
        assertTrue(ch2VersesResult is AynvoraResult.Success)
        val ch2Verses = ch2VersesResult.value
        assertEquals(72, ch2Verses.size)

        val v247Result = repository.getVerse(2, 47, "english")
        assertTrue(v247Result is AynvoraResult.Success)
        val verseDetail = v247Result.value
        assertTrue(verseDetail.sanskritDevanagari.contains("कर्मण्येवाधिकारस्ते"))
        assertTrue(verseDetail.translations.isNotEmpty(), "Verse 2.47 must have English translations")

        // Search test
        val searchResult = repository.searchVerses("कर्मण्य")
        assertTrue(searchResult is AynvoraResult.Success)
        val searchMatches = searchResult.value
        assertTrue(searchMatches.any { it.chapterNumber == 2 && it.verseNumber == 47 })
    }
}
