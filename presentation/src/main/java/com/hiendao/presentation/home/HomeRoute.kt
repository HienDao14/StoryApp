package com.hiendao.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import kotlinx.coroutines.launch
import com.hiendao.coreui.theme.toTheme
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hiendao.coreui.R
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Category
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.home.autoSwipe.AutoSwipeSection
import com.hiendao.presentation.home.viewModel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onBookClick: (Book) -> Unit = {},
    onReadClick: (Book) -> Unit = {},
    onVoiceClick: (Book) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFavouriteClick: (Book) -> Unit = {},
    onCategoryClick: (String, String) -> Unit = { _, _ -> },
    onCreateStoryClick: () -> Unit = {},
    onCreateVoiceClick: () -> Unit = {},

    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    val homeViewModel: HomeViewModel = hiltViewModel()

    val voiceState = homeViewModel.voiceState.collectAsStateWithLifecycle()
    val homeState = homeViewModel.homeState.collectAsStateWithLifecycle()
    val books = homeState.value.allBooks
    val newestBooks = homeState.value.newestBooks
    val recentlyReadBooks = homeState.value.recentlyRead
    val featureBooks = books.take(5)

    when(voiceState.value){
        is Response.Success -> {
            Toast.makeText(context, "Voice created successfully", Toast.LENGTH_SHORT).show()
            homeViewModel.resetVoiceState()
        }
        else -> Unit
    }

    LaunchedEffect(true) {
        homeViewModel.reload()
    }

    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                ""
            )
        )
    }

    // Import Story Launcher
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        uri?.let { homeViewModel.importBook(it) }
    }

    // Theme Dialog State
    var showThemeDialog by remember { mutableStateOf(false) }
    if (showThemeDialog) {
        com.hiendao.presentation.home.components.ThemeSelectionDialog(
            currentTheme = homeViewModel.currentTheme.value.toTheme,
            currentFollowSystem = homeViewModel.followsSystemTheme.value,
            onFollowSystemChange = homeViewModel::onFollowSystemChange,
            onCurrentThemeChange = homeViewModel::onThemeChange,
            onDismissRequest = { showThemeDialog = false }
        )
    }

    // Logout Confirmation Dialog State
    var showLogoutDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val categories = homeState.value.categories
    val filtered = remember(books, query.text) {
        val q = query.text.trim().lowercase()
        if (q.isBlank()) books
        else books.filter { it.title.lowercase().contains(q) || it.author.lowercase().contains(q) }
    }

    val drawerState =
        androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                Spacer(Modifier.height(12.dp))
                // --- Header ---
                Text(
                    text = "Story App Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Function Group ---
                Text(
                    text = "Functions",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.search)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSearchClick()
                    },
                    icon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.import_story)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        launcher.launch(arrayOf("application/epub+zip", "text/plain")) // Basic filter
                    },
                    icon = { Icon(Icons.Default.FileOpen, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.create_story)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreateStoryClick()
                    },
                    icon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.create_new_voice_title)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCreateVoiceClick()
                    },
                    icon = { Icon(Icons.Default.Mic, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.theme)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showThemeDialog = true
                    },
                    icon = { Icon(Icons.Default.ColorLens, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // --- Navigation Group ---
                Text(
                    text = stringResource(R.string.navigation),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.title_home)) },
                    selected = true, // Currently on Home
                    onClick = {
                        scope.launch { drawerState.close() }
                        // Already home
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.title_library)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate("library")
                    },
                    icon = {
                        Icon(
                            Icons.Default.List,
                            null
                        )
                    }, // Using List icon for Library as placeholder or import LibraryBooks
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.title_settings)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate("settings")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            null
                        )
                    }, // Need to make sure Settings icon is available
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.weight(1f))

                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.log_out)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }, // Need ExitToApp
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(text = stringResource(R.string.logout_confirmation_title)) },
                text = { Text(text = stringResource(R.string.logout_confirmation_message)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogout()
                        }
                    ) {
                        Text(stringResource(R.string.log_out))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = context.getString(R.string.app_name),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu App"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onSearchClick.invoke()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Books"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExpandableFab(
                    onCreateStoryClick = onCreateStoryClick,
                    onCreateVoiceClick = onCreateVoiceClick
                )
            }
        ) { innerPadding ->

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {

                        // ----- Feature Carousel -----
                        item {
                            Spacer(Modifier.height(8.dp))
                            AutoSwipeSection(
                                modifier = Modifier.fillMaxWidth(),
                                sectionType = stringResource(R.string.featured_stories),
                                listMedia = featureBooks,
                                onBookClick = {
                                    onBookClick.invoke(it)
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        // ----- Categories (chips tròn) -----
                        if (categories.isNotEmpty()) {
                            item {
                                CategorySection(
                                    title = stringResource(R.string.category),
                                    categories = categories,
                                    onCategoryClick = { category ->
//                                        onCategoryClick(category.id, category.name)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if(newestBooks.isNotEmpty()) {
                            item {
                                FavouriteSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.newest),
                                    listBooks = newestBooks,
                                    onBookClick = {
                                        onBookClick.invoke(it)
                                    },
                                    onSeeAllClick = {
                                        onCategoryClick("newest", context.getString(R.string.newest))
                                    },
                                    onLoadMore = {
                                        homeViewModel.getNewestBooks(
                                            homeState.value.newestBooksPage + 1
                                        )
                                    },
                                    uiState = homeState.value,
                                    isEndReached = homeState.value.isNewestBooksEnd
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if(recentlyReadBooks.isNotEmpty()) {
                            item {
                                FavouriteSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.recently_read),
                                    listBooks = recentlyReadBooks,
                                    onBookClick = {
                                        onBookClick.invoke(it)
                                    },
                                    onSeeAllClick = {
                                        onCategoryClick("recentlyRead", context.getString(R.string.recently_read))
                                    },
                                    onLoadMore = {
                                        homeViewModel.getRecentlyReadBooks(
                                            homeState.value.recentlyReadPage + 1
                                        )
                                    },
                                    uiState = homeState.value,
                                    isEndReached = homeState.value.isRecentlyReadEnd
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if(homeState.value.favouriteBooks.isNotEmpty()){
                            item {
                                FavouriteSection(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(R.string.favourite),
                                    listBooks = homeState.value.favouriteBooks,
                                    onBookClick = {
                                        onBookClick.invoke(it)
                                    },
                                    onSeeAllClick = {
                                        onCategoryClick("favourite", context.getString(R.string.favourite))
                                    },
                                    onLoadMore = {
                                        homeViewModel.getFavouriteBooks(
                                            homeState.value.favouriteBooksPage + 1
                                        )
                                    },
                                    uiState = homeState.value,
                                    isEndReached = homeState.value.isFavouriteBooksEnd
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        // ----- Grid 2 cột (cuộn cùng màn) -----
                        item {
                            BookGridFlow(
                                title = stringResource(R.string.all_stories),
                                books = homeState.value.allBooks,
                                onBookClick = {
                                    onBookClick.invoke(it)
                                },
                                onPlayClick = {
                                    onVoiceClick.invoke(it)
                                },
                                onFavouriteClick = { book ->
                                    homeViewModel.toggleFavourite(book)
                                },
                                onLoadMore = {
                                    homeViewModel.getAllBooks(homeState.value.allBooksPage + 1)
                                },
                                isLoading = homeState.value.isLoading,
                                isEndReached = homeState.value.isAllBooksEnd
                            )
                            Spacer(Modifier.height(8.dp))
                        }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit,
    placeholder: String,
) {
    TopAppBar(
        title = {
            TextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
fun ExpandableFab(
    onCreateStoryClick: () -> Unit,
    onCreateVoiceClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
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
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalAlignment = Alignment.End
            ) {
                // Generate Story Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(R.string.generate_story),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background((MaterialTheme.colorScheme.background).copy(alpha = 0.9f))
                            .padding(4.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    SmallFloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onCreateStoryClick()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Generate")
                    }
                }

                // Create Voice Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = stringResource(R.string.create_new_voice_title),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background((MaterialTheme.colorScheme.background).copy(alpha = 0.9f))
                            .padding(4.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    SmallFloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onCreateVoiceClick()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Create Voice")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close" else "Add",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}
