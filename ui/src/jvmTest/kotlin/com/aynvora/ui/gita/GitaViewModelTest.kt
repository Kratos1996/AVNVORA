package com.aynvora.ui.gita

import com.aynvora.core.gita.GitaAuthor
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaCommentary
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaTranslation
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GitaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleChapter = GitaChapter(
        chapterId = 2,
        chapterNumber = 2,
        nameSanskrit = "साङ्ख्ययोग",
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
        wordMeanings = "karmaṇi—in prescribed duties; eva—only; adhikāraḥ—right; te—thy; mā—not; phaleṣu—in the fruits; kadācana—at any time",
        translations = listOf(sampleTranslation),
        commentaries = listOf(sampleCommentary),
    )

    private val fakeRepository = object : GitaRepository {
        var queryRecorded: String? = null
        var lastLanguageRequested: String? = null

        override suspend fun getChapters(): AynvoraResult<List<GitaChapter>> {
            return AynvoraResult.Success(listOf(sampleChapter))
        }

        override suspend fun getChapter(chapterNumber: Int): AynvoraResult<GitaChapter> {
            return AynvoraResult.Success(sampleChapter)
        }

        override suspend fun getVersesForChapter(
            chapterNumber: Int,
            language: String,
        ): AynvoraResult<List<GitaVerse>> {
            lastLanguageRequested = language
            return AynvoraResult.Success(listOf(sampleVerse))
        }

        override suspend fun getVerse(
            chapterNumber: Int,
            verseNumber: Int,
            language: String,
        ): AynvoraResult<GitaVerse> {
            lastLanguageRequested = language
            return AynvoraResult.Success(sampleVerse)
        }

        override suspend fun searchVerses(query: String): AynvoraResult<List<GitaVerse>> {
            queryRecorded = query
            return AynvoraResult.Success(listOf(sampleVerse))
        }

        override suspend fun getAuthors(): AynvoraResult<List<GitaAuthor>> {
            return AynvoraResult.Success(listOf(GitaAuthor(16, "Swami Sivananda")))
        }

        override suspend fun isSeeded(): Boolean = true
        override suspend fun getSeededVerseCount(): Int = 701
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateLoadsChapters() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(GitaDestination.ChapterList, state.destination)
        assertEquals(1, state.chapters.size)
        assertEquals("साङ्ख्ययोग", state.chapters.first().nameSanskrit)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun testSelectChapterLoadsVerses() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.SelectChapter(sampleChapter))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(GitaDestination.VerseList, state.destination)
        assertEquals(sampleChapter, state.selectedChapter)
        assertEquals(1, state.verses.size)
        assertEquals("BG 2.47", state.verses.first().title)
        assertFalse(state.isLoading)
    }

    @Test
    fun testSelectVerseNavigatesToDetail() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.SelectChapter(sampleChapter))
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.SelectVerse(sampleVerse))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(GitaDestination.VerseDetail, state.destination)
        assertNotNull(state.selectedVerse)
        assertEquals("BG 2.47", state.selectedVerse?.title)
        assertEquals(1, state.selectedVerse?.translations?.size)
        assertEquals(1, state.selectedVerse?.commentaries?.size)
    }

    @Test
    fun testBackNavigationFlow() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        // 1. ChapterList -> VerseList
        vm.onEvent(GitaUiEvent.SelectChapter(sampleChapter))
        advanceUntilIdle()
        assertEquals(GitaDestination.VerseList, vm.uiState.value.destination)

        // 2. VerseList -> VerseDetail
        vm.onEvent(GitaUiEvent.SelectVerse(sampleVerse))
        advanceUntilIdle()
        assertEquals(GitaDestination.VerseDetail, vm.uiState.value.destination)

        // 3. VerseDetail -> VerseList
        vm.onEvent(GitaUiEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(GitaDestination.VerseList, vm.uiState.value.destination)
        assertNull(vm.uiState.value.selectedVerse)

        // 4. VerseList -> ChapterList
        vm.onEvent(GitaUiEvent.NavigateBack)
        advanceUntilIdle()
        assertEquals(GitaDestination.ChapterList, vm.uiState.value.destination)
        assertNull(vm.uiState.value.selectedChapter)
    }

    @Test
    fun testSearchAndClearFlow() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.UpdateSearchQuery("karma"))
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(GitaDestination.Search, state.destination)
        assertEquals("karma", state.searchQuery)
        assertEquals(1, state.searchResults.size)

        vm.onEvent(GitaUiEvent.ClearSearch)
        advanceUntilIdle()

        val clearedState = vm.uiState.value
        assertEquals(GitaDestination.ChapterList, clearedState.destination)
        assertEquals("", clearedState.searchQuery)
        assertTrue(clearedState.searchResults.isEmpty())
    }

    @Test
    fun testLanguageToggle() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.SelectChapter(sampleChapter))
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.ChangeLanguage("hindi"))
        advanceUntilIdle()

        assertEquals("hindi", vm.uiState.value.language)
        assertEquals("hindi", fakeRepository.lastLanguageRequested)
    }

    @Test
    fun testToggleCommentaryAndAuthorFilter() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showCommentary)
        vm.onEvent(GitaUiEvent.ToggleCommentary)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showCommentary)

        vm.onEvent(GitaUiEvent.SelectAuthorFilter(16))
        advanceUntilIdle()
        assertEquals(16, vm.uiState.value.selectedAuthorId)
    }

    @Test
    fun testCanonicalScriptureIntegrityInvariant() = runTest {
        val vm = GitaViewModel(fakeRepository)
        advanceUntilIdle()

        vm.onEvent(GitaUiEvent.SelectChapter(sampleChapter))
        advanceUntilIdle()
        vm.onEvent(GitaUiEvent.SelectVerse(sampleVerse))
        advanceUntilIdle()

        val verse = vm.uiState.value.selectedVerse
        assertNotNull(verse)
        // Verify verse text is canonical Sanskrit & transliteration
        assertTrue(verse.sanskritDevanagari.contains("कर्मण्येवाधिकारस्ते"))
        assertTrue(verse.transliteration.contains("karmaṇy-evādhikāras"))
        // Verify translation is from accredited author, not AI-synthesized
        assertEquals("Swami Sivananda", verse.translations.first().authorName)
        assertEquals(16, verse.translations.first().authorId)
    }
}
