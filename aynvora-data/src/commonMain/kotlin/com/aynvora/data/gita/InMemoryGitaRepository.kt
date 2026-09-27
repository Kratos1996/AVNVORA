package com.aynvora.data.gita

import com.aynvora.core.gita.GitaAuthor
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaSourceEdition
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult

/**
 * Offline-first repository for Bhagavad Gita text.
 * Seeds authentic public domain Sanskrit verses and translations.
 */
class InMemoryGitaRepository : GitaRepository {

    private val publicDomainEdition = GitaSourceEdition(
        editionId = "GITA_PUBLIC_DOMAIN_1920",
        title = "The Bhagavad Gita: The Songs of the Master",
        translatorOrCommentator = "Charles Johnston (1908) & Classical Sanskrit Trad.",
        language = "en",
        publicationYear = 1908,
        isPublicDomain = true,
    )

    private val sampleVerses = listOf(
        GitaVerse(
            chapterNumber = 2,
            verseNumber = 47,
            sanskritDevenagari = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन। मा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            transliterationIast = "karmaṇy-evādhikāras te mā phaleṣu kadācana, mā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi.",
            wordMeanings = mapOf(
                "karmaṇi" to "in duty",
                "eva" to "only",
                "adhikāraḥ" to "right",
                "te" to "your"
            ),
            translation = "Thy right is to work only, but never to its fruits; let not the fruits of action be thy motive, nor let thy longing be for inaction.",
            commentary = "One of the cardinal verses of Nishkama Karma: focus wholeheartedly on conscious right action without being consumed or paralyzed by anticipated rewards or outcome anxiety.",
            sourceEdition = publicDomainEdition,
            themeTags = listOf("ACTION", "FOCUS", "DUTY", "DETACHMENT"),
        ),
        GitaVerse(
            chapterNumber = 2,
            verseNumber = 48,
            sanskritDevenagari = "योगस्थः कुरु कर्माणि सङ्गं त्यक्त्वा धनञ्जय। सिद्ध्यसिद्ध्योः समो भूत्वा समत्वं योग उच्यते॥",
            transliterationIast = "yoga-sthaḥ kuru karmāṇi saṅgaṁ tyaktvā dhanañjaya, siddhy-asiddhyoḥ samo bhūtvā samatvaṁ yoga ucyate.",
            wordMeanings = mapOf(
                "yoga-sthaḥ" to "steadfast in yoga",
                "kuru" to "perform",
                "karmāṇi" to "duties"
            ),
            translation = "Perform work, O Dhananjaya, being steadfast in Yoga, abandoning attachment, and balanced in success and failure. Equanimity of mind is called Yoga.",
            commentary = "Equanimity in victory and setback is the true hallmark of spiritual balance and inner mastery.",
            sourceEdition = publicDomainEdition,
            themeTags = listOf("YOGA", "EQUANIMITY", "BALANCE"),
        )
    )

    override suspend fun getChapters(): AynvoraResult<List<GitaChapter>> {
        return AynvoraResult.Success(
            listOf(
                GitaChapter(
                    chapterNumber = 1,
                    titleSanskrit = "अर्जुनविषादयोग",
                    titleEnglish = "Arjuna's Grief & Despondency",
                    titleHindi = "अर्जुन विषाद योग",
                    totalVerses = 47,
                    summary = "Arjuna observes both armies at Kurukshetra and experiences moral collapse and grief.",
                ),
                GitaChapter(
                    chapterNumber = 2,
                    titleSanskrit = "साङ्ख्ययोग",
                    titleEnglish = "The Yoga of Knowledge",
                    titleHindi = "सांख्य योग",
                    totalVerses = 72,
                    summary = "Krishna reveals the eternal nature of the soul, Nishkama Karma, and the characteristics of the steady-minded sage.",
                ),
            )
        )
    }

    override suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter> {
        val chaptersResult = getChapters()
        if (chaptersResult is AynvoraResult.Success) {
            val ch = chaptersResult.value.find { it.chapterNumber == chapterNumber }
            if (ch != null) return AynvoraResult.Success(ch)
        }
        return AynvoraResult.Failure.NotFound(
            resourceId = "GITA_CHAPTER_$chapterNumber",
            message = "Chapter $chapterNumber not found.",
        )
    }

    override suspend fun getVerse(
        chapterNumber: Int,
        verseNumber: Int,
        language: String
    ): AynvoraResult<GitaVerse> {
        val verse =
            sampleVerses.find { it.chapterNumber == chapterNumber && it.verseNumber == verseNumber }
        return if (verse != null) {
            AynvoraResult.Success(verse)
        } else {
            AynvoraResult.Failure.NotFound(
                resourceId = "GITA_$chapterNumber.$verseNumber",
                message = "Verse $chapterNumber.$verseNumber not present in local starter pack.",
            )
        }
    }

    override suspend fun getVersesForChapter(
        chapterNumber: Int,
        language: String
    ): AynvoraResult<List<GitaVerse>> {
        val verses = sampleVerses.filter { it.chapterNumber == chapterNumber }
        return AynvoraResult.Success(verses)
    }

    override suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>> {
        val q = query.lowercase()
        val matches = sampleVerses.filter {
            it.sanskritDevanagari.contains(q, ignoreCase = true) ||
                    it.transliteration.contains(q, ignoreCase = true) ||
                    it.translation.contains(q, ignoreCase = true)
        }
        return AynvoraResult.Success(matches)
    }

    override suspend fun searchVersesByTheme(
        themeTag: String,
        language: String
    ): AynvoraResult<List<GitaVerse>> {
        val normalized = themeTag.uppercase()
        val verses = sampleVerses.filter { it.themeTags.contains(normalized) }
        return AynvoraResult.Success(verses)
    }

    override suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>> {
        return AynvoraResult.Success(
            listOf(GitaAuthor(authorId = 1, name = "Charles Johnston"))
        )
    }

    override suspend fun isSeeded(): Boolean = true

    override suspend fun getSeededVerseCount(): Int = sampleVerses.size
}
