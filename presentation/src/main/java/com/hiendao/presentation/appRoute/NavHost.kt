package com.hiendao.presentation.appRoute

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.hiendao.coreui.composableActions.onDoAskForImage
import com.hiendao.presentation.bookDetail.screen.ChaptersScreen
import com.hiendao.presentation.bookDetail.viewModel.ChaptersViewModel
import com.hiendao.presentation.home.HomeRoute
import com.hiendao.presentation.home.MainScaffold
import com.hiendao.presentation.library.LibraryRoute
import com.hiendao.presentation.library.viewmodel.LibraryViewModel
import com.hiendao.presentation.login.LoginRoute
import com.hiendao.presentation.search.SearchRoute
import com.hiendao.presentation.settings.SettingRoute
import com.hiendao.presentation.voice.screen.VoiceRoute
import com.hiendao.presentation.voice.state.VoiceReaderScreenState
import com.hiendao.presentation.voice.viewModel.VoiceViewModel
import com.hiendao.presentation.categoryDetail.CategoryDetailRoute
import com.hiendao.presentation.categoryDetail.viewModel.CategoryDetailViewModel
import com.hiendao.presentation.story.create.CreateStoryRoute

import com.hiendao.presentation.voice.create.CreateVoiceRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val HOME = "home"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val BOOK_DETAIL = "book_detail"
    const val BOOK_VOICE = "book_voice"
    const val BOOK_READING = "book_reading"
    const val SEARCH = "search"
    const val CATEGORY_DETAIL = "category_detail"
    const val CREATE_STORY = "create_story"
    const val CREATE_VOICE = "create_voice"
}

/** ----- Nav Host ----- **/
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onBookOpen: ((bookId: String, chapterUrl: String) -> Unit)? = null
) {
    val libraryViewModel = hiltViewModel<LibraryViewModel>()
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.MAIN else Routes.AUTH
    ) {
        navigation(startDestination = Routes.LOGIN, route = Routes.AUTH) {
            composable(Routes.LOGIN) {
                LoginRoute(
                    modifier = modifier,
                    onLoginSuccess = {
                        isLoggedIn = true
                        // Vào main và clear toàn bộ auth khỏi back stack
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        navigation(startDestination = Routes.HOME, route = Routes.MAIN) {
            composable(Routes.HOME) {
                MainScaffold(
                    currentRoute = Routes.HOME,
                    onNavigate = { route -> bottomNavigate(navController, route) },
                    content = {
                        HomeRoute(
                            modifier = Modifier.fillMaxWidth(),
                            onBookClick = { book ->
                                navController.navigate("${Routes.BOOK_DETAIL}?bookId=${book.id}?bookTitle=${book.title}")
                            },
                            onSearchClick = {
                                navController.navigate(Routes.SEARCH)
                            },
                            onVoiceClick = { book ->
                                navController.navigate("${Routes.BOOK_VOICE}?bookId=${book.id}?bookTitle=${book.title}")
                            },
                            onFavouriteClick = {

                            },
                            onCategoryClick = { id, name ->
                                navController.navigate("${Routes.CATEGORY_DETAIL}?categoryId=$id&categoryName=$name")
                            },
                            onCreateStoryClick = {
                                navController.navigate(Routes.CREATE_STORY)
                            },
                            onCreateVoiceClick = {
                                navController.navigate(Routes.CREATE_VOICE)
                            },
                            onNavigate = { route -> bottomNavigate(navController, route) },
                            onLogout = {
                                isLoggedIn = false
                                navController.navigate(Routes.AUTH) {
                                    popUpTo(Routes.MAIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.LIBRARY) {
                MainScaffold(
                    currentRoute = Routes.LIBRARY,
                    onNavigate = { route -> bottomNavigate(navController, route) },
                    content = {
                        LibraryRoute(
                            modifier = Modifier.fillMaxSize(),
                            onBookClick = { book ->
                                navController.navigate("${Routes.BOOK_DETAIL}?bookId=${book.id}?bookTitle=${book.title}")
                            },
                            onCreateStory = {
                                navController.navigate(Routes.CREATE_STORY)
                            }
                        )
                    },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.SETTINGS) {
                MainScaffold(
                    currentRoute = Routes.SETTINGS,
                    onNavigate = { route -> bottomNavigate(navController, route) },
                    content = {
                        SettingRoute(
                            modifier = Modifier.fillMaxSize(),
                            onLogout = {
                                navController.navigate(Routes.AUTH) {
                                    popUpTo(Routes.MAIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    },
                    onLogout = {
                        isLoggedIn = false
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = "${Routes.BOOK_DETAIL}?bookId={bookId}?bookTitle={bookTitle}",
                arguments = listOf(
                    navArgument(name = "bookId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(name = "bookTitle") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
            )) { entry ->
                val chaptersViewModel = hiltViewModel<ChaptersViewModel>()

                val bookId = entry.arguments?.getString("bookId")
                val bookTitle = entry.arguments?.getString("bookTitle")
                requireNotNull(bookId)
                requireNotNull(bookTitle)

                LaunchedEffect(key1 = true) {
                    chaptersViewModel.updateState(bookId, bookTitle)
                    delay(500)
                }

                ChaptersScreen(
                    bookId = bookId,
                    state = chaptersViewModel.state,
                    onFavouriteToggle = chaptersViewModel::toggleBookmark,
                    onResumeReading = {
                        scope.launch {
                            val lastReadChapter = chaptersViewModel.getLastReadChapter()
                                ?: chaptersViewModel.state.chapters.minByOrNull { it.chapter.position }?.chapter?.id
                                ?: return@launch
                            onBookOpen?.invoke(chaptersViewModel.bookUrl.value, lastReadChapter)
                        }
                    },
                    onPressBack = {
                        navController.navigateUp()
                    },
                    onSelectedDeleteDownloads = chaptersViewModel::deleteDownloadsSelected,
                    onSelectedDownload = {},
                    onSelectedSetRead = chaptersViewModel::setAsReadSelected,
                    onSelectedSetReadUpToChapterRead = chaptersViewModel::setAsReadUpToSelected,
                    onSelectedSetReadUpToChapterUnread = chaptersViewModel::setAsReadUpToUnSelected,
                    onSelectedSetUnread = chaptersViewModel::setAsUnreadSelected,
                    onSelectedInvertSelection = chaptersViewModel::invertSelection,
                    onSelectAllChapters = chaptersViewModel::selectAll,
                    onCloseSelectionBar = chaptersViewModel::unselectAll,
                    onChapterClick = { chapter ->
                        onBookOpen?.invoke(chaptersViewModel.bookUrl.value, chapter.chapter.id)
                    },
                    onChapterLongClick = chaptersViewModel::onChapterLongClick,
                    onSelectionModeChapterClick = chaptersViewModel::onSelectionModeChapterClick,
                    onSelectionModeChapterLongClick = chaptersViewModel::onSelectionModeChapterLongClick,
                    onChapterDownload = {},
                    onPullRefresh = chaptersViewModel::onPullRefresh,
                    onCoverLongClick = {},
                    onChangeCover = onDoAskForImage { chaptersViewModel.saveImageAsCover(it) },
                    onGlobalSearchClick = {},
                    onCategoryClick = { id, name ->
                        navController.navigate("${Routes.CATEGORY_DETAIL}?categoryId=$id&categoryName=$name")
                    }
                )

            }

            composable(
                route = "${Routes.BOOK_VOICE}?bookId={bookId}?bookTitle={bookTitle}",
                arguments = listOf(
                    navArgument(name = "bookId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(name = "bookTitle") {
                        type = NavType.StringType
                        defaultValue = ""
                    })
            ) { entry ->

                val bookId = entry.arguments?.getString("bookId")
                val bookTitle = entry.arguments?.getString("bookTitle")
                requireNotNull(bookId)
                requireNotNull(bookTitle)

                val viewModel: VoiceViewModel = hiltViewModel()
                LaunchedEffect(key1 = true) {
                    viewModel.updateState(bookId, bookTitle)
                    delay(500)
                    viewModel.autoPlay()
                }
                VoiceRoute(
                    modifier = Modifier.fillMaxSize(),
                    state = viewModel.state.collectAsStateWithLifecycle().value,
                    onFavouriteToggle = { viewModel::toggleBookmark },
                    onPressBack = {
                        navController.navigateUp()
                    },
                    onChangeCover = onDoAskForImage { viewModel.saveImageAsCover(it) },
                    onChapterSelected = { chapterUrl ->
                        viewModel.playChapterFromStart(chapterUrl)
                    },
                    onPlayClick = { viewModel.play() },
                    onPauseClick = { viewModel.pause() }
                )
//                com.hiendao.presentation.voice.screen.VoiceScreenPart2(
//                    modifier = Modifier.fillMaxSize(),
//                    paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
//                    state = viewModel.state.collectAsStateWithLifecycle().value,
//                    onChapterSelected = { chapterUrl ->
//                        viewModel.playChapterFromStart(chapterUrl)
//                    },
//                    onPlayClick = { viewModel.play() },
//                    onPauseClick = { viewModel.pause() }
//                )
            }

            composable(Routes.SEARCH) {
                SearchRoute(
                    modifier = Modifier.fillMaxSize(),
                   onBookClick = { book ->
                       navController.navigate("${Routes.BOOK_DETAIL}?bookId=${book.id}?bookTitle=${book.title}")
                   },
                    onBackClick = {
                          navController.navigateUp()
                    }
                )
            }

            composable(
                 route = "${Routes.CATEGORY_DETAIL}?categoryId={categoryId}&categoryName={categoryName}",
                 arguments = listOf(
                     navArgument(name = "categoryId") {
                         type = NavType.StringType
                         defaultValue = ""
                     },
                     navArgument(name = "categoryName") {
                         type = NavType.StringType
                         defaultValue = ""
                     }
                 )
            ) { entry ->
                val categoryId = entry.arguments?.getString("categoryId")
                val categoryName = entry.arguments?.getString("categoryName")
                requireNotNull(categoryId)
                requireNotNull(categoryName)

                val viewModel: CategoryDetailViewModel = hiltViewModel()
                LaunchedEffect(key1 = true) {
                    viewModel.updateState(categoryId, categoryName)
                }

                CategoryDetailRoute(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onBookClick = { book ->
                        navController.navigate("${Routes.BOOK_DETAIL}?bookId=${book.id}?bookTitle=${book.title}")
                    }
                )
            }

            composable(Routes.CREATE_STORY) {
                CreateStoryRoute(
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(Routes.CREATE_VOICE) {
                CreateVoiceRoute(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}

/* ---------- Điều hướng khi bấm bottom bar (giữ state từng tab) ---------- */
private fun bottomNavigate(navController: NavHostController, route: String) {
    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
    }
}