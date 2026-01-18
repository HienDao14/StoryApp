package com.hiendao.presentation.bookDetail.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiendao.coreui.R
import com.hiendao.coreui.theme.ColorAccent
import com.hiendao.coreui.theme.ColorLike
import com.hiendao.coreui.theme.ColorNotice
import com.hiendao.coreui.theme.colorApp
import com.hiendao.coreui.theme.textPadding
import com.hiendao.coreui.utils.isAtTop
import com.hiendao.coreui.utils.isLocalUri
import com.hiendao.data.local.entity.ChapterWithContext
import com.hiendao.presentation.bookDetail.state.ChaptersScreenState
import my.nanihadesuka.compose.InternalLazyColumnScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChaptersScreen(
    bookId: String,
    state: ChaptersScreenState,
    onFavouriteToggle: () -> Unit,
    onResumeReading: () -> Unit,
    onListenToBook: () -> Unit,
    onPressBack: () -> Unit,
    onSelectedDeleteDownloads: () -> Unit,
    onSelectedDownload: () -> Unit,
    onSelectedSetRead: () -> Unit,
    onSelectedSetUnread: () -> Unit,
    onSelectedSetReadUpToChapterRead: () -> Unit,
    onSelectedSetReadUpToChapterUnread: () -> Unit,
    onSelectedInvertSelection: () -> Unit,
    onSelectAllChapters: () -> Unit,
    onCloseSelectionBar: () -> Unit,
    onChapterClick: (chapter: ChapterWithContext) -> Unit,
    onChapterLongClick: (chapter: ChapterWithContext) -> Unit,
    onSelectionModeChapterClick: (chapter: ChapterWithContext) -> Unit,
    onSelectionModeChapterLongClick: (chapter: ChapterWithContext) -> Unit,
    onChapterDownload: (chapter: ChapterWithContext) -> Unit,
    onPullRefresh: () -> Unit,
    onCoverLongClick: () -> Unit,
    onChangeCover: () -> Unit,
    onGlobalSearchClick: (input: String) -> Unit,
    onCategoryClick: (String, String) -> Unit
) {
    var showDropDown by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val areSelectedChaptersRead = state.selectedChaptersUrl.keys.all { url ->
        state.chapters.find { it.chapter.id == url }?.chapter?.read == true
    }

    if (state.isInSelectionMode.value) BackHandler {
        onCloseSelectionBar()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val isAtTop by lazyListState.isAtTop(threshold = 40.dp)
            val alpha by animateFloatAsState(targetValue = if (isAtTop) 0f else 1f, label = "")
            val backgroundColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.background.copy(alpha = alpha),
                label = ""
            )
            val titleColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                label = ""
            )
            Surface(color = backgroundColor) {
                Column {
                    TopAppBar(
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                        ),
                        title = {
                            Text(
                                text = state.book.value.title,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = titleColor
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onPressBack
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = onFavouriteToggle
                            ) {
                                Icon(
                                    if (state.book.value.isFavourite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                    stringResource(R.string.open_the_web_view),
                                    tint = ColorLike
                                )
                            }
                            IconButton(
                                onClick = { showBottomSheet = !showBottomSheet }
                            ) {
                                Icon(
                                    Icons.Filled.FilterList,
                                    stringResource(R.string.filter),
                                    tint = ColorNotice
                                )
                            }
                            IconButton(onClick = { showDropDown = !showDropDown }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    stringResource(R.string.options_panel)
                                )
                                DropdownMenu(
                                    expanded = showDropDown,
                                    onDismissRequest = { showDropDown = false },
                                    containerColor = MaterialTheme.colorScheme.background) {
                                    ChaptersDropDown(
                                        onResumeReading = onResumeReading,
                                        onChangeCover = onChangeCover,
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider(Modifier.alpha(alpha))
                }
            }
            AnimatedVisibility(
                visible = state.isInSelectionMode.value,
                enter = expandVertically(initialHeight = { it / 2 }, expandFrom = Alignment.Top)
                        + fadeIn(),
                exit = shrinkVertically(targetHeight = { it / 2 }, shrinkTowards = Alignment.Top)
                        + fadeOut(),
            ) {
                Surface(color = MaterialTheme.colorApp.tintedSurface) {
                    TopAppBar(
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorApp.tintedSurface,
                            scrolledContainerColor = MaterialTheme.colorApp.tintedSurface,
                        ),
                        title = {
                            Text(
                                text = state.selectedChaptersUrl.size.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.animateContentSize()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onCloseSelectionBar) {
                                Icon(
                                    Icons.Outlined.Close,
                                    stringResource(id = R.string.close_selection_bar)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onSelectAllChapters) {
                                Icon(
                                    Icons.Outlined.SelectAll,
                                    stringResource(id = R.string.select_all_chapters)
                                )
                            }
                            IconButton(onClick = onSelectedInvertSelection) {
                                Icon(
                                    Icons.Outlined.PublishedWithChanges,
                                    stringResource(id = R.string.close_selection_bar)
                                )
                            }
                        }
                    )
                }
            }
        },
        content = { innerPadding ->
            ChaptersScreenBody(
                state = state,
                lazyListState = lazyListState,
                innerPadding = innerPadding,
                onChapterClick = if (state.isInSelectionMode.value) onSelectionModeChapterClick else onChapterClick,
                onChapterLongClick = if (state.isInSelectionMode.value) onSelectionModeChapterLongClick else onChapterLongClick,
                onChapterDownload = onChapterDownload,
                onPullRefresh = onPullRefresh,
                onCoverLongClick = onCoverLongClick,
                onGlobalSearchClick = onGlobalSearchClick,
                onCategoryClick = onCategoryClick
            )
            Box(Modifier.padding(innerPadding)) {
                InternalLazyColumnScrollbar(state = lazyListState)
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.isInSelectionMode.value,
                enter = expandVertically(initialHeight = { it / 2 }) + fadeIn(),
                exit = shrinkVertically(targetHeight = { it / 2 }) + fadeOut(),
            ) {
                BottomAppBar(
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = MaterialTheme.colorApp.tintedSurface,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        if (!state.isLocalSource.value) {
                            IconButton(onClick = onSelectedDeleteDownloads) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    stringResource(id = R.string.remove_selected_chapters_downloads)
                                )
                            }
                            IconButton(onClick = onSelectedDownload) {
                                Icon(
                                    Icons.Outlined.CloudDownload,
                                    stringResource(id = R.string.download_selected_chapters)
                                )
                            }
                        }
                        if (areSelectedChaptersRead) {
                            IconButton(onClick = onSelectedSetUnread) {
                                Icon(
                                    Icons.Filled.DoneOutline,
                                    stringResource(id = R.string.set_as_not_read_selected_chapters)
                                )
                            }
                        } else {
                            IconButton(onClick = onSelectedSetRead) {
                                Icon(
                                    Icons.Filled.Done,
                                    stringResource(id = R.string.set_as_read_selected_chapters)
                                )
                            }
                        }
                        if (state.selectedChaptersUrl.size <= 1) {
                            if (areSelectedChaptersRead) {
                                IconButton(onClick = onSelectedSetReadUpToChapterUnread) {
                                    Icon(
                                        Icons.Filled.RemoveDone,
                                        stringResource(id = R.string.set_as_Unread_up_to_selected_chapter)
                                    )
                                }
                            } else {
                                IconButton(onClick = onSelectedSetReadUpToChapterRead) {
                                    Icon(
                                        Icons.Filled.DoneAll,
                                        stringResource(id = R.string.set_as_read_up_to_selected_chapter)
                                    )

                                }
                            }
                        }
                    }
                }

            }
        },
        floatingActionButton = {
            ExpandableFab(
                onResumeReading = onResumeReading,
                onListenToBook = onListenToBook
            )
        }
    )

    ChaptersBottomSheet(
        visible = showBottomSheet,
        onDismiss = { showBottomSheet = false },
        state = state
    )
}

@Composable
fun ExpandableFab(
    onResumeReading: () -> Unit,
    onListenToBook: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "fab_rotation"
    )

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Resume Reading Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.continue_reading),
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    androidx.compose.material3.SmallFloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onResumeReading()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Read")
                    }
                }

                // Listen Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.continue_listening),
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    androidx.compose.material3.SmallFloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onListenToBook()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Headphones, contentDescription = "Listen")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = ColorAccent
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.PlayArrow,
                contentDescription = if (isExpanded) "Close" else "Play",
                modifier = Modifier.rotate(rotation),
                tint = Color.White
            )
        }
    }
}