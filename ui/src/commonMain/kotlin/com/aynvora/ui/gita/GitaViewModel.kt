package com.aynvora.ui.gita

import androidx.lifecycle.viewModelScope
import com.aynvora.core.event.AynvoraClickEvent
import com.aynvora.core.event.AynvoraEffect
import com.aynvora.core.event.AynvoraEventDispatcher
import com.aynvora.core.event.AynvoraEventPayload
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaRepository
import com.aynvora.core.gita.GitaVerse
import com.aynvora.core.result.AynvoraResult
import com.aynvora.ui.base.AynvoraBaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

enum class GitaDestination {
    /** Chapter list — 18 chapters with Sanskrit names. */
    ChapterList,

    /** Verse list for a selected chapter. */
    VerseList,

    /** Single verse detail with translations and commentary. */
    VerseDetail,

    /** Full-text search results. */
    Search,
}

data class GitaUiState(
    val destination: GitaDestination = GitaDestination.ChapterList,
    val chapters: List<GitaChapter> = emptyList(),
    val selectedChapter: GitaChapter? = null,
    val verses: List<GitaVerse> = emptyList(),
    val selectedVerse: GitaVerse? = null,
    val searchQuery: String = "",
    val searchResults: List<GitaVerse> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Language used for translations/commentaries: "english" | "hindi" */
    val language: String = "english",
    /** Index of the selected author for translations (null = show all) */
    val selectedAuthorId: Int? = null,
    /** Whether the verse detail shows commentary in addition to translations */
    val showCommentary: Boolean = false,
)

// ─────────────────────────────────────────────────────────────────────────────
// Events
// ─────────────────────────────────────────────────────────────────────────────

sealed class GitaUiEvent(
    eventId: String,
    componentId: String = "gita",
) : AynvoraClickEvent(
    eventId = eventId,
    screenId = "gita",
    componentId = componentId,
    payload = AynvoraEventPayload.Empty,
) {
    data object LoadChapters : GitaUiEvent("gita.chapters.load")
    data class SelectChapter(val chapter: GitaChapter) :
        GitaUiEvent("gita.chapter.select", "chapter_row_${chapter.chapterNumber}")

    data class SelectVerse(val verse: GitaVerse) :
        GitaUiEvent("gita.verse.select", "verse_row_${verse.chapterNumber}_${verse.verseNumber}")

    data object NavigateBack : GitaUiEvent("gita.navigate.back")
    data class UpdateSearchQuery(val query: String) : GitaUiEvent("gita.search.query_changed")
    data object ClearSearch : GitaUiEvent("gita.search.clear")
    data class ChangeLanguage(val language: String) : GitaUiEvent("gita.language.changed")
    data object ToggleCommentary : GitaUiEvent("gita.verse.toggle_commentary")
    data class SelectAuthorFilter(val authorId: Int?) : GitaUiEvent("gita.verse.author_filter")
    data object DismissError : GitaUiEvent("gita.error.dismiss")
}

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for Bhagavad Gita browsing, chapter/verse navigation, and search.
 *
 * Data contract:
 *  - All text displayed comes from [GitaRepository] — canonical gita/gita dataset.
 *  - AI content is NEVER requested or merged here.
 *  - Language controls translation display only; Sanskrit text is always shown.
 */
class GitaViewModel(
    private val gitaRepository: GitaRepository,
    eventDispatcher: AynvoraEventDispatcher? = null,
) : AynvoraBaseViewModel<GitaUiEvent, GitaUiState, AynvoraEffect>(
    initialState = GitaUiState(),
    eventDispatcher = eventDispatcher,
) {

    private var searchJob: Job? = null

    init {
        registerEventHandler(::handleEvent)
        // Eagerly load chapters on creation
        viewModelScope.launch { loadChapters() }
    }

    private suspend fun handleEvent(event: GitaUiEvent) {
        when (event) {
            is GitaUiEvent.LoadChapters -> loadChapters()

            is GitaUiEvent.SelectChapter -> {
                val chapter = event.chapter
                updateState {
                    copy(
                        selectedChapter = chapter,
                        destination = GitaDestination.VerseList,
                        verses = emptyList(),
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                loadVersesForChapter(chapter.chapterNumber)
            }

            is GitaUiEvent.SelectVerse -> {
                val verse = event.verse
                // Load full verse with all translations if not already populated
                if (verse.translations.isEmpty()) {
                    updateState { copy(isLoading = true) }
                    val result = gitaRepository.getVerse(
                        chapterNumber = verse.chapterNumber,
                        verseNumber = verse.verseNumber,
                        language = currentState.language,
                    )
                    val fullVerse = (result as? AynvoraResult.Success)?.value ?: verse
                    updateState {
                        copy(
                            selectedVerse = fullVerse,
                            destination = GitaDestination.VerseDetail,
                            isLoading = false,
                        )
                    }
                } else {
                    updateState {
                        copy(
                            selectedVerse = verse,
                            destination = GitaDestination.VerseDetail,
                        )
                    }
                }
            }

            is GitaUiEvent.NavigateBack -> {
                val state = currentState
                when (state.destination) {
                    GitaDestination.VerseDetail -> updateState {
                        copy(destination = GitaDestination.VerseList, selectedVerse = null)
                    }

                    GitaDestination.VerseList -> updateState {
                        copy(
                            destination = GitaDestination.ChapterList,
                            selectedChapter = null,
                            verses = emptyList(),
                        )
                    }

                    GitaDestination.Search -> updateState {
                        copy(
                            destination = GitaDestination.ChapterList,
                            searchQuery = "",
                            searchResults = emptyList(),
                        )
                    }

                    GitaDestination.ChapterList -> { /* handled by root nav */
                    }
                }
            }

            is GitaUiEvent.UpdateSearchQuery -> {
                val query = event.query
                updateState { copy(searchQuery = query) }
                searchJob?.cancel()
                if (query.isBlank()) {
                    updateState {
                        copy(
                            searchResults = emptyList(),
                            destination = if (destination == GitaDestination.Search) GitaDestination.ChapterList else destination,
                        )
                    }
                    return
                }
                searchJob = viewModelScope.launch {
                    updateState { copy(isLoading = true, destination = GitaDestination.Search) }
                    val result = gitaRepository.searchVerses(query)
                    updateState {
                        when (result) {
                            is AynvoraResult.Success -> copy(
                                searchResults = result.value,
                                isLoading = false,
                                errorMessage = null,
                            )

                            else -> copy(
                                isLoading = false,
                                errorMessage = "Search unavailable. Please try again.",
                            )
                        }
                    }
                }
            }

            is GitaUiEvent.ClearSearch -> {
                searchJob?.cancel()
                updateState {
                    copy(
                        searchQuery = "",
                        searchResults = emptyList(),
                        destination = GitaDestination.ChapterList,
                    )
                }
            }

            is GitaUiEvent.ChangeLanguage -> {
                val newLang = event.language
                updateState { copy(language = newLang, isLoading = true) }
                val chapter = currentState.selectedChapter
                if (chapter != null && currentState.destination == GitaDestination.VerseList) {
                    loadVersesForChapter(chapter.chapterNumber)
                } else if (currentState.selectedVerse != null) {
                    val verse = currentState.selectedVerse!!
                    val result =
                        gitaRepository.getVerse(verse.chapterNumber, verse.verseNumber, newLang)
                    updateState {
                        copy(
                            selectedVerse = (result as? AynvoraResult.Success)?.value
                                ?: selectedVerse,
                            isLoading = false,
                        )
                    }
                } else {
                    updateState { copy(isLoading = false) }
                }
            }

            is GitaUiEvent.ToggleCommentary -> {
                updateState { copy(showCommentary = !showCommentary) }
            }

            is GitaUiEvent.SelectAuthorFilter -> {
                updateState { copy(selectedAuthorId = event.authorId) }
            }

            is GitaUiEvent.DismissError -> {
                updateState { copy(errorMessage = null) }
            }
        }
    }

    private suspend fun loadChapters() {
        updateState { copy(isLoading = true, errorMessage = null) }
        val result = gitaRepository.getChapters()
        updateState {
            when (result) {
                is AynvoraResult.Success -> copy(
                    chapters = result.value,
                    isLoading = false,
                )

                else -> copy(
                    isLoading = false,
                    errorMessage = "Unable to load chapters. Please restart the app.",
                )
            }
        }
    }

    private suspend fun loadVersesForChapter(chapterNumber: Int) {
        val result = gitaRepository.getVersesForChapter(chapterNumber, currentState.language)
        updateState {
            when (result) {
                is AynvoraResult.Success -> copy(
                    verses = result.value,
                    isLoading = false,
                    errorMessage = null,
                )

                else -> copy(
                    isLoading = false,
                    errorMessage = "Unable to load verses for chapter $chapterNumber.",
                )
            }
        }
    }
}
