package com.hiendao.presentation.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hiendao.coreui.R
import com.hiendao.coreui.components.BookSettingsDialog
import com.hiendao.coreui.components.BookSettingsDialogState
import com.hiendao.coreui.theme.ColorNotice
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.model.Book
import com.hiendao.presentation.library.screen.LibraryBottomSheet
import com.hiendao.presentation.library.screen.LibraryDropDown
import com.hiendao.presentation.library.screen.LibraryScreenBody
import com.hiendao.presentation.library.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    onBookClick: (Book) -> Unit = {},
    onCreateStory: () -> Unit = {}
) {
    val libraryModel: LibraryViewModel = hiltViewModel()

    val context by rememberUpdatedState(LocalContext.current)
    var showDropDown by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    var bookSettingsDialogState by remember { mutableStateOf<BookSettingsDialogState>(BookSettingsDialogState.Hide) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.title_library),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showBottomSheet = !showBottomSheet }
                    ) {
                        Icon(
                            Icons.Filled.FilterList,
                            stringResource(R.string.filter),
                            tint = ColorNotice
                        )
                    }
                    IconButton(
                        onClick = { showDropDown = !showDropDown }
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            stringResource(R.string.options_panel)
                        )
                        LibraryDropDown(
                            expanded = showDropDown,
                            onDismiss = { showDropDown = false },
                            onImportEpub = {

                            },
                            onGenerateStory = {
                                onCreateStory.invoke()
                            }
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            LibraryScreenBody(
                tabs = listOf(context.getString(R.string.str_default), context.getString(R.string.completed), context.getString(R.string.favourite)),
                innerPadding = innerPadding,
                topAppBarState = scrollBehavior.state,
                onBookClick = { book ->
                    onBookClick.invoke(book.book.toDomain())
                },
                onBookLongClick = {
                    bookSettingsDialogState = BookSettingsDialogState.Show(it.book.toDomain())
                }
            )
        }
    )

    // Book selected options dialog
    when (val state = bookSettingsDialogState) {
        is BookSettingsDialogState.Show -> {
            val book = libraryModel.getBook(state.book.url)
                .collectAsState(initial = state.book.toEntity()).value?.toDomain() ?: return
            BookSettingsDialog(
                book = book,
                onDismiss = { bookSettingsDialogState = BookSettingsDialogState.Hide },
                onToggleCompleted = { libraryModel.bookCompletedToggle(state.book.url) }
            )
        }

        else -> Unit
    }

    LibraryBottomSheet(
        visible = showBottomSheet,
        onDismiss = { showBottomSheet = false }
    )
}
