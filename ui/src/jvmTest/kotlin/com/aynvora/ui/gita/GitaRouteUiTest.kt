package com.aynvora.ui.gita

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.aynvora.core.gita.GitaAuthor
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaCommentary
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaTranslation
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult
import com.aynvora.designsystem.AynvoraTheme
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class GitaRouteUiTest {

    private val sampleChapter = GitaChapter(
        chapterId = 2,
        chapterNumber = 2,
        nameSanskrit = "सांख्ययोग",
        nameTranslation = "Sankhya Yoga",
        nameTransliterated = "Sānkhya Yoga",
        nameMeaning = "Yoga of Knowledge",
        chapterSummaryEnglish = "Lord Krishna begins imparting spiritual knowledge to Arjuna.",
        chapterSummaryHindi = "भगवान श्रीकृष्ण अर्जुन को सांख्य ज्ञान प्रदान करते हैं।",
        versesCount = 72,
        imageName = "chapter_2.jpg",
    )

    private val sampleTranslation = GitaTranslation(
        translationId = 1,
        verseId = 47,
        authorId = 16,
        authorName = "Swami Sivananda",
        language = "english",
        description = "Thy right is to work only, but never with its fruits; let not the fruits of action be thy motive, nor let thy attachment be to inaction.",
    )

    private val sampleCommentary = GitaCommentary(
        commentaryId = 1,
        verseId = 47,
        authorId = 16,
        authorName = "Swami Sivananda",
        language = "english",
        description = "Perform action without desiring the fruits. This is Nishkama Karma Yoga.",
    )

    private val sampleVerse = GitaVerse(
        verseId = 47,
        chapterNumber = 2,
        verseNumber = 47,
        verseOrder = 94,
        title = "BG 2.47",
        sanskritDevanagari = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
        transliteration = "karmaṇy-evādhikāras te mā phaleṣu kadācana |\nmā karma-phala-hetur bhūr mā te saṅgo 'stv akarmaṇi ||",
        wordMeanings = "karmaṇi—in prescribed duties; eva—only",
        translations = listOf(sampleTranslation),
        commentaries = listOf(sampleCommentary),
    )

    private val fakeRepository = object : GitaRepository {
        override suspend fun getChapters(): AynvoraResult<List<GitaChapter>> =
            AynvoraResult.Success(listOf(sampleChapter))

        override suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter> =
            AynvoraResult.Success(sampleChapter)

        override suspend fun getVersesForChapter(
            chapterNumber: Int,
            language: String,
        ): AynvoraResult<List<GitaVerse>> =
            AynvoraResult.Success(listOf(sampleVerse))

        override suspend fun getVerse(
            chapterNumber: Int,
            verseNumber: Int,
            language: String,
        ): AynvoraResult<GitaVerse> =
            AynvoraResult.Success(sampleVerse)

        override suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>> =
            AynvoraResult.Success(listOf(sampleVerse))

        override suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>> =
            AynvoraResult.Success(listOf(GitaAuthor(16, "Swami Sivananda")))

        override suspend fun isSeeded(): Boolean = true
        override suspend fun getSeededVerseCount(): Int = 701
    }

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(
                module {
                    single<GitaRepository> { fakeRepository }
                }
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testGitaScreenFullInteractiveFlow() = runComposeUiTest {
        val viewModel = GitaViewModel(fakeRepository)
        var closeCalled = false

        setContent {
            AynvoraTheme(darkTheme = true) {
                GitaRoute(
                    onClose = { closeCalled = true },
                    viewModel = viewModel,
                )
            }
        }

        // ── 1. Chapter List Screen ──
        onNodeWithText("Bhagavad Gita").assertIsDisplayed()
        onNodeWithText("18 Adhyāyas · 701 Ślokas · Public Domain").assertIsDisplayed()
        onNodeWithText("सांख्ययोग").assertIsDisplayed()
        onNodeWithText("Sankhya Yoga").assertIsDisplayed()

        // ── 2. Click Chapter -> Navigate to Verse List ──
        onNodeWithText("सांख्ययोग").performClick()

        // Verify Verse List content
        onNodeWithText("BG 2.47").assertIsDisplayed()
        onNodeWithText("2.47").assertIsDisplayed()

        // ── 3. Click Verse -> Navigate to Verse Detail ──
        onNodeWithText("BG 2.47").performClick()

        // Verify Verse Detail scripture & translation blocks
        onNodeWithText("SANSKRIT").assertIsDisplayed()
        onNodeWithText("TRANSLITERATION").assertIsDisplayed()
        onNodeWithText("Swami Sivananda").assertIsDisplayed()

        // ── 4. Toggle Commentary ──
        onNodeWithText("▼ Show Commentary (1)").assertIsDisplayed()
        onNodeWithText("▼ Show Commentary (1)").performClick()
        onNodeWithText("▲ Hide Commentary").assertIsDisplayed()

        // ── 5. Back Navigation from Detail to Verse List ──
        onNodeWithText("←").performClick()
        onNodeWithText("BG 2.47").assertIsDisplayed()

        // ── 6. Language Toggle (EN <-> हिन्दी) ──
        onNodeWithText("EN").assertIsDisplayed()
        onNodeWithText("EN").performClick()
        onNodeWithText("हिन्दी").assertIsDisplayed()

        // ── 7. Back Navigation from Verse List to Chapter List ──
        onNodeWithText("←").performClick()
        onNodeWithText("Bhagavad Gita").assertIsDisplayed()
        onNodeWithText("सांख्ययोग").assertIsDisplayed()

        // ── 8. Search Flow ──
        onNodeWithText("🔍").assertIsDisplayed()
        onNodeWithText("🔍").performClick()

        // Verify Search Screen placeholder is displayed
        onNodeWithText("Search Sanskrit, transliteration, or English…").assertIsDisplayed()

        // Type search query
        onNodeWithText("Search Sanskrit, transliteration, or English…").performTextInput("karma")

        // In search screen, verify search results are shown
        onNodeWithText("1 results").assertIsDisplayed()
        onNodeWithText("BG 2.47").assertIsDisplayed()

        // Back from search to Chapter List
        onNodeWithText("←").performClick()
        onNodeWithText("Bhagavad Gita").assertIsDisplayed()

        // ── 9. Close Action ──
        onNodeWithText("←").performClick()
        assertTrue(closeCalled, "Closing from ChapterList must trigger onClose callback")
    }
}
