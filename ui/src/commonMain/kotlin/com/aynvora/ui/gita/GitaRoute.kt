package com.aynvora.ui.gita

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aynvora.core.gita.GitaChapter
import com.aynvora.core.gita.GitaTranslation
import com.aynvora.core.gita.GitaVerse
import com.aynvora.designsystem.AynvoraColors
import com.aynvora.designsystem.AynvoraShapes
import com.aynvora.designsystem.AynvoraTheme
import com.aynvora.designsystem.adaptive.sdp
import com.aynvora.designsystem.adaptive.ssp
import com.aynvora.designsystem.event.aynvoraClickable
import org.koin.compose.currentKoinScope

/**
 * Root Compose entry for the Bhagavad Gita feature.
 *
 * Displays the full navigation stack: ChapterList → VerseList → VerseDetail.
 * All data is canonical scripture from the gita/gita Public Domain dataset.
 * No AI-generated content is displayed anywhere in this screen hierarchy.
 */
@Composable
fun GitaRoute(
    onClose: () -> Unit,
    viewModel: GitaViewModel? = null,
) {
    val koin = currentKoinScope()
    val vm = viewModel ?: remember(koin) {
        koin.getOrNull<GitaViewModel>() ?: GitaViewModel(
            gitaRepository = koin.get(),
        )
    }
    val state by vm.uiState.collectAsState()
    val isDark = AynvoraTheme.isDark
    val bg = if (isDark) AynvoraTheme.colors.CosmicBlack else AynvoraTheme.colors.Ivory
    val onEvent: (GitaUiEvent) -> Unit = { vm.onEvent(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        AnimatedContent(
            targetState = state.destination,
            transitionSpec = {
                when (targetState) {
                    GitaDestination.ChapterList ->
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())

                    else ->
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                }
            },
            label = "GitaNavigation",
        ) { destination ->
            when (destination) {
                GitaDestination.ChapterList -> GitaChapterListScreen(
                    state = state,
                    onEvent = onEvent,
                    onClose = onClose,
                )

                GitaDestination.VerseList -> GitaVerseListScreen(
                    state = state,
                    onEvent = onEvent,
                )

                GitaDestination.VerseDetail -> GitaVerseDetailScreen(
                    state = state,
                    onEvent = onEvent,
                )

                GitaDestination.Search -> GitaSearchScreen(
                    state = state,
                    onEvent = onEvent,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val isDark = AynvoraTheme.isDark
    val textColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.sdp, vertical = 12.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) AynvoraTheme.colors.CosmicNavy
                        else AynvoraTheme.colors.SoftGold
                    )
                    .aynvoraClickable(
                        event = GitaUiEvent.NavigateBack,
                        onDispatch = { onBack() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "←",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 16.ssp),
                    color = AynvoraColors.Gold,
                )
            }
            Spacer(modifier = Modifier.width(10.sdp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AynvoraTheme.typography.title20.copy(
                    fontSize = 18.ssp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = AynvoraColors.Gold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                    color = if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.sdp))
            trailing()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chapter List Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaChapterListScreen(
    state: GitaUiState,
    onEvent: (GitaUiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val textColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

    Column(modifier = Modifier.fillMaxSize()) {
        GitaTopBar(
            title = "Bhagavad Gita",
            subtitle = "18 Adhyāyas · 701 Ślokas · Public Domain",
            onBack = onClose,
            trailing = {
                // Search icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                        .aynvoraClickable(
                            event = GitaUiEvent.UpdateSearchQuery(""),
                            onDispatch = {
                                onEvent(GitaUiEvent.UpdateSearchQuery("")) // trigger search screen
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🔍", style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp))
                }
            },
        )

        // Attribution banner
        GitaAttributionBanner()

        when {
            state.isLoading && state.chapters.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AynvoraColors.Gold)
                        Spacer(modifier = Modifier.height(16.sdp))
                        Text(
                            text = "Loading sacred verses…",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = secondaryTextColor,
                        )
                    }
                }
            }

            state.chapters.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.sdp),
                    ) {
                        Text("📖", style = AynvoraTheme.typography.title20.copy(fontSize = 40.ssp))
                        Spacer(modifier = Modifier.height(12.sdp))
                        Text(
                            text = state.errorMessage ?: "Initializing scripture data…",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                            color = textColor,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.sdp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AynvoraColors.Gold.copy(alpha = 0.2f))
                                .border(1.dp, AynvoraColors.Gold, RoundedCornerShape(20.dp))
                                .aynvoraClickable(
                                    event = GitaUiEvent.LoadChapters,
                                    onDispatch = { onEvent(GitaUiEvent.LoadChapters) },
                                )
                                .padding(horizontal = 20.sdp, vertical = 8.sdp),
                        ) {
                            Text(
                                text = "Retry",
                                style = AynvoraTheme.typography.caption12.copy(
                                    fontSize = 13.ssp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = AynvoraColors.Gold,
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.sdp, vertical = 8.sdp),
                    verticalArrangement = Arrangement.spacedBy(10.sdp),
                ) {
                    items(state.chapters, key = { it.chapterNumber }) { chapter ->
                        GitaChapterCard(
                            chapter = chapter,
                            isDark = isDark,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { onEvent(GitaUiEvent.SelectChapter(chapter)) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.sdp)) }
                }
            }
        }
    }
}

@Composable
private fun GitaChapterCard(
    chapter: GitaChapter,
    isDark: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold
    val borderColor = AynvoraColors.Gold.copy(alpha = 0.22f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AynvoraShapes.shape12)
            .background(cardBg)
            .border(1.dp, borderColor, AynvoraShapes.shape12)
            .aynvoraClickable(
                event = GitaUiEvent.SelectChapter(chapter),
                onDispatch = { onClick() },
            )
            .padding(14.sdp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Chapter number badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AynvoraColors.Gold.copy(alpha = 0.15f))
                .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${chapter.chapterNumber}",
                style = AynvoraTheme.typography.title18.copy(
                    fontSize = 16.ssp,
                    fontWeight = FontWeight.Bold,
                ),
                color = AynvoraColors.Gold,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.width(14.sdp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.nameSanskrit,
                style = AynvoraTheme.typography.title18.copy(
                    fontSize = 16.ssp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = textColor,
            )
            Text(
                text = chapter.nameTranslation,
                style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                color = AynvoraColors.Gold,
            )
            Spacer(modifier = Modifier.height(3.sdp))
            Text(
                text = chapter.nameMeaning,
                style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.sdp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${chapter.versesCount}",
                style = AynvoraTheme.typography.title18.copy(
                    fontSize = 15.ssp,
                    fontWeight = FontWeight.Bold,
                ),
                color = AynvoraColors.Gold,
            )
            Text(
                text = "ślokas",
                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                color = secondaryTextColor,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Verse List Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaVerseListScreen(
    state: GitaUiState,
    onEvent: (GitaUiEvent) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val textColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary
    val chapter = state.selectedChapter

    Column(modifier = Modifier.fillMaxSize()) {
        GitaTopBar(
            title = chapter?.nameSanskrit ?: "Verses",
            subtitle = chapter?.let { "${it.nameTranslation} · ${it.versesCount} ślokas" },
            onBack = { onEvent(GitaUiEvent.NavigateBack) },
            trailing = {
                // Language toggle pill
                GitaLanguagePill(
                    language = state.language,
                    onToggle = {
                        onEvent(
                            GitaUiEvent.ChangeLanguage(
                                if (state.language == "english") "hindi" else "english"
                            )
                        )
                    },
                )
            },
        )

        if (chapter != null) {
            // Chapter summary card
            GitaChapterSummaryCard(
                chapter = chapter,
                language = state.language,
                isDark = isDark,
                textColor = secondaryTextColor
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AynvoraColors.Gold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.sdp, vertical = 8.sdp),
                verticalArrangement = Arrangement.spacedBy(8.sdp),
            ) {
                items(state.verses, key = { "${it.chapterNumber}_${it.verseNumber}" }) { verse ->
                    GitaVerseCard(
                        verse = verse,
                        isDark = isDark,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        language = state.language,
                        onClick = { onEvent(GitaUiEvent.SelectVerse(verse)) },
                    )
                }
                item { Spacer(modifier = Modifier.height(24.sdp)) }
            }
        }
    }
}

@Composable
private fun GitaChapterSummaryCard(
    chapter: GitaChapter,
    language: String,
    isDark: Boolean,
    textColor: Color,
) {
    val summary =
        if (language == "hindi") chapter.chapterSummaryHindi else chapter.chapterSummaryEnglish
    if (summary.isBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.sdp, vertical = 6.sdp)
            .clip(AynvoraShapes.shape10)
            .background(AynvoraColors.Gold.copy(alpha = 0.08f))
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.18f), AynvoraShapes.shape10)
            .padding(12.sdp),
    ) {
        Text(
            text = summary,
            style = AynvoraTheme.typography.body14.copy(
                fontSize = 12.ssp,
                fontStyle = FontStyle.Italic,
            ),
            color = textColor,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GitaVerseCard(
    verse: GitaVerse,
    isDark: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    language: String,
    onClick: () -> Unit,
) {
    val cardBg = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AynvoraShapes.shape10)
            .background(cardBg)
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.18f), AynvoraShapes.shape10)
            .aynvoraClickable(
                event = GitaUiEvent.SelectVerse(verse),
                onDispatch = { onClick() },
            )
            .padding(12.sdp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${verse.chapterNumber}.${verse.verseNumber}",
                style = AynvoraTheme.typography.caption12.copy(
                    fontSize = 11.ssp,
                    fontWeight = FontWeight.Bold,
                ),
                color = AynvoraColors.Gold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = verse.title,
                style = AynvoraTheme.typography.caption12.copy(fontSize = 10.ssp),
                color = secondaryTextColor,
            )
        }
        Spacer(modifier = Modifier.height(6.sdp))
        // Sanskrit snippet
        Text(
            text = verse.sanskritDevanagari.take(120).let {
                if (verse.sanskritDevanagari.length > 120) "$it…" else it
            },
            style = AynvoraTheme.typography.body14.copy(
                fontSize = 14.ssp,
                lineHeight = 22.sp,
            ),
            color = textColor,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Verse Detail Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaVerseDetailScreen(
    state: GitaUiState,
    onEvent: (GitaUiEvent) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val textColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary
    val verse = state.selectedVerse ?: return

    // Filter translations by selected author, or show all
    val displayedTranslations = if (state.selectedAuthorId != null) {
        verse.translations.filter { it.authorId == state.selectedAuthorId }
    } else {
        verse.translations.take(2) // Default: show first 2 translations
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GitaTopBar(
            title = "${verse.chapterNumber}.${verse.verseNumber}",
            subtitle = verse.title,
            onBack = { onEvent(GitaUiEvent.NavigateBack) },
            trailing = {
                GitaLanguagePill(
                    language = state.language,
                    onToggle = {
                        onEvent(
                            GitaUiEvent.ChangeLanguage(
                                if (state.language == "english") "hindi" else "english"
                            )
                        )
                    },
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.sdp, vertical = 8.sdp),
            verticalArrangement = Arrangement.spacedBy(14.sdp),
        ) {
            // Sanskrit text block
            item {
                GitaScriptureBlock(
                    label = "Sanskrit",
                    content = verse.sanskritDevanagari,
                    isDark = isDark,
                    textColor = textColor,
                    fontSize = 18.ssp,
                )
            }

            // Transliteration
            item {
                GitaScriptureBlock(
                    label = "Transliteration",
                    content = verse.transliteration,
                    isDark = isDark,
                    textColor = textColor.copy(alpha = 0.82f),
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.ssp,
                )
            }

            // Author filter chips (if multiple authors)
            if (verse.translations.size > 1) {
                item {
                    GitaAuthorChips(
                        translations = verse.translations,
                        selectedAuthorId = state.selectedAuthorId,
                        isDark = isDark,
                        onSelect = { id -> onEvent(GitaUiEvent.SelectAuthorFilter(id)) },
                    )
                }
            }

            // Translations
            if (displayedTranslations.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.sdp)) {
                        displayedTranslations.forEach { translation ->
                            GitaTranslationBlock(
                                translation = translation,
                                isDark = isDark,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                            )
                        }
                    }
                }
            }

            // Commentary toggle
            if (verse.commentaries.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AynvoraShapes.shape8)
                            .background(AynvoraColors.Gold.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                AynvoraColors.Gold.copy(alpha = 0.25f),
                                AynvoraShapes.shape8
                            )
                            .aynvoraClickable(
                                event = GitaUiEvent.ToggleCommentary,
                                onDispatch = { onEvent(GitaUiEvent.ToggleCommentary) },
                            )
                            .padding(horizontal = 14.sdp, vertical = 10.sdp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (state.showCommentary) "▲ Hide Commentary" else "▼ Show Commentary (${verse.commentaries.size})",
                            style = AynvoraTheme.typography.body14.copy(
                                fontSize = 13.ssp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = AynvoraColors.Gold,
                        )
                    }
                }

                if (state.showCommentary) {
                    val filteredCommentaries = if (state.selectedAuthorId != null) {
                        verse.commentaries.filter { it.authorId == state.selectedAuthorId }
                    } else {
                        verse.commentaries.take(1)
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.sdp)) {
                            filteredCommentaries.forEach { commentary ->
                                GitaCommentaryBlock(
                                    authorName = commentary.authorName,
                                    text = commentary.description,
                                    isDark = isDark,
                                    textColor = textColor,
                                    secondaryTextColor = secondaryTextColor,
                                )
                            }
                        }
                    }
                }
            }

            // Attribution
            item {
                GitaAttributionBanner()
                Spacer(modifier = Modifier.height(24.sdp))
            }
        }
    }
}

@Composable
private fun GitaScriptureBlock(
    label: String,
    content: String,
    isDark: Boolean,
    textColor: Color,
    fontStyle: FontStyle = FontStyle.Normal,
    fontSize: androidx.compose.ui.unit.TextUnit = 15.ssp,
) {
    val bgColor = if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AynvoraShapes.shape12)
            .background(bgColor)
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.22f), AynvoraShapes.shape12)
            .padding(16.sdp),
    ) {
        Text(
            text = label.uppercase(),
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 9.ssp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
            ),
            color = AynvoraColors.Gold,
        )
        Spacer(modifier = Modifier.height(8.sdp))
        Text(
            text = content.trim(),
            style = AynvoraTheme.typography.body14.copy(
                fontSize = fontSize,
                lineHeight = (fontSize.value * 1.65f).sp,
                fontStyle = fontStyle,
            ),
            color = textColor,
        )
    }
}

@Composable
private fun GitaAuthorChips(
    translations: List<GitaTranslation>,
    selectedAuthorId: Int?,
    isDark: Boolean,
    onSelect: (Int?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.sdp),
        contentPadding = PaddingValues(vertical = 4.sdp),
    ) {
        item {
            GitaChip(
                label = "All",
                isSelected = selectedAuthorId == null,
                isDark = isDark,
                onClick = { onSelect(null) },
            )
        }
        items(translations, key = { it.authorId }) { t ->
            GitaChip(
                label = t.authorName.substringAfterLast(" "),
                isSelected = selectedAuthorId == t.authorId,
                isDark = isDark,
                onClick = { onSelect(t.authorId) },
            )
        }
    }
}

@Composable
private fun GitaChip(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) AynvoraColors.Gold.copy(alpha = 0.2f)
    else if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold
    val border =
        if (isSelected) AynvoraColors.Gold.copy(alpha = 0.7f) else AynvoraColors.Gold.copy(alpha = 0.2f)
    val textColor =
        if (isSelected) AynvoraColors.Gold else if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .aynvoraClickable(
                event = GitaUiEvent.SelectAuthorFilter(if (isSelected) null else 0),
                onDispatch = { onClick() },
            )
            .padding(horizontal = 12.sdp, vertical = 6.sdp),
    ) {
        Text(
            text = label,
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 11.ssp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
        )
    }
}

@Composable
private fun GitaTranslationBlock(
    translation: GitaTranslation,
    isDark: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AynvoraShapes.shape10)
            .background(if (isDark) AynvoraTheme.colors.CosmicIndigo else AynvoraTheme.colors.SoftGold)
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.15f), AynvoraShapes.shape10)
            .padding(14.sdp),
    ) {
        Text(
            text = translation.authorName,
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 10.ssp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
            ),
            color = AynvoraColors.Gold,
        )
        Spacer(modifier = Modifier.height(6.sdp))
        Text(
            text = translation.description.trim(),
            style = AynvoraTheme.typography.body14.copy(
                fontSize = 14.ssp,
                lineHeight = 22.sp,
            ),
            color = textColor,
        )
    }
}

@Composable
private fun GitaCommentaryBlock(
    authorName: String,
    text: String,
    isDark: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AynvoraShapes.shape10)
            .background(AynvoraColors.Gold.copy(alpha = 0.05f))
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.12f), AynvoraShapes.shape10)
            .padding(14.sdp),
    ) {
        Text(
            text = "Commentary · $authorName",
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 10.ssp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = AynvoraColors.Gold.copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(6.sdp))
        Text(
            text = text.trim(),
            style = AynvoraTheme.typography.body14.copy(
                fontSize = 13.ssp,
                lineHeight = 21.sp,
                fontStyle = FontStyle.Italic,
            ),
            color = secondaryTextColor,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaSearchScreen(
    state: GitaUiState,
    onEvent: (GitaUiEvent) -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val textColor = if (isDark) AynvoraTheme.colors.TextLight else AynvoraTheme.colors.TextDark
    val secondaryTextColor =
        if (isDark) AynvoraTheme.colors.TextLightSecondary else AynvoraTheme.colors.TextSecondary

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.sdp, vertical = 12.sdp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .aynvoraClickable(
                        event = GitaUiEvent.ClearSearch,
                        onDispatch = { onEvent(GitaUiEvent.ClearSearch) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "←",
                    style = AynvoraTheme.typography.body14.copy(fontSize = 16.ssp),
                    color = AynvoraColors.Gold
                )
            }

            Spacer(modifier = Modifier.width(10.sdp))

            BasicTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(GitaUiEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .weight(1f)
                    .clip(AynvoraShapes.shape10)
                    .background(if (isDark) AynvoraTheme.colors.CosmicNavy else AynvoraTheme.colors.SoftGold)
                    .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.3f), AynvoraShapes.shape10)
                    .padding(horizontal = 14.sdp, vertical = 10.sdp),
                textStyle = AynvoraTheme.typography.body14.copy(
                    fontSize = 14.ssp,
                    color = textColor,
                ),
                cursorBrush = SolidColor(AynvoraColors.Gold),
                decorationBox = { inner ->
                    if (state.searchQuery.isBlank()) {
                        Text(
                            text = "Search Sanskrit, transliteration, or English…",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 13.ssp),
                            color = secondaryTextColor,
                        )
                    }
                    inner()
                },
                singleLine = true,
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AynvoraColors.Gold)
            }

            state.searchResults.isEmpty() && state.searchQuery.isNotBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📖", style = AynvoraTheme.typography.title20.copy(fontSize = 36.ssp))
                        Spacer(modifier = Modifier.height(12.sdp))
                        Text(
                            text = "No verses found for\n\"${state.searchQuery}\"",
                            style = AynvoraTheme.typography.body14.copy(fontSize = 14.ssp),
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.sdp, vertical = 8.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp),
                ) {
                    item {
                        Text(
                            text = "${state.searchResults.size} results",
                            style = AynvoraTheme.typography.caption12.copy(fontSize = 11.ssp),
                            color = secondaryTextColor,
                        )
                        Spacer(modifier = Modifier.height(4.sdp))
                    }
                    items(
                        state.searchResults,
                        key = { "${it.chapterNumber}_${it.verseNumber}" }) { verse ->
                        GitaVerseCard(
                            verse = verse,
                            isDark = isDark,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            language = state.language,
                            onClick = { onEvent(GitaUiEvent.SelectVerse(verse)) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.sdp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared micro-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GitaLanguagePill(
    language: String,
    onToggle: () -> Unit,
) {
    val isDark = AynvoraTheme.isDark
    val label = if (language == "hindi") "हिन्दी" else "EN"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AynvoraColors.Gold.copy(alpha = 0.15f))
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .aynvoraClickable(
                event = GitaUiEvent.ChangeLanguage(if (language == "english") "hindi" else "english"),
                onDispatch = { onToggle() },
            )
            .padding(horizontal = 10.sdp, vertical = 5.sdp),
    ) {
        Text(
            text = label,
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 11.ssp,
                fontWeight = FontWeight.SemiBold
            ),
            color = AynvoraColors.Gold,
        )
    }
}

@Composable
private fun GitaAttributionBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.sdp, vertical = 4.sdp)
            .clip(AynvoraShapes.shape8)
            .background(AynvoraColors.Gold.copy(alpha = 0.06f))
            .border(1.dp, AynvoraColors.Gold.copy(alpha = 0.15f), AynvoraShapes.shape8)
            .padding(horizontal = 12.sdp, vertical = 7.sdp),
    ) {
        Text(
            text = "Source: github.com/gita/gita · Public Domain · No AI-generated text",
            style = AynvoraTheme.typography.caption12.copy(
                fontSize = 9.ssp,
                letterSpacing = 0.3.sp
            ),
            color = AynvoraColors.Gold.copy(alpha = 0.65f),
        )
    }
}
