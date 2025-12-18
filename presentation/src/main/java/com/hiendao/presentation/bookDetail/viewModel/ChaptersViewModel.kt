package com.hiendao.presentation.bookDetail.viewModel

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.R
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.StateExtra_String
import com.hiendao.coreui.utils.Toasty
import com.hiendao.coreui.utils.isLocalUri
import com.hiendao.coreui.utils.toState
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.local.entity.BookWithContext
import com.hiendao.data.local.entity.ChapterWithContext
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.data.utils.isContentUri
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.repository.AppRepository
import com.hiendao.presentation.bookDetail.ChaptersRepository
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.presentation.bookDetail.state.ChaptersScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.addAll
import kotlin.collections.containsKey
import kotlin.collections.set
import kotlin.compareTo
import kotlin.let
import kotlin.ranges.rangeTo
import kotlin.text.clear
import kotlin.text.set
import kotlin.to

interface ChapterStateBundle {
    val rawBookUrl: String
    val bookTitle: String
}

@HiltViewModel
internal class ChaptersViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val appScope: AppCoroutineScope,
    private val toasty: Toasty,
    appPreferences: AppPreferences,
    appFileResolver: AppFileResolver,
    stateHandle: SavedStateHandle,
    private val chaptersRepository: ChaptersRepository
) : BaseViewModel() {
    @Volatile
    private var loadChaptersJob: Job? = null

    private var _bookUrl = MutableStateFlow<String>("")
    val bookUrl = _bookUrl.asStateFlow()

    private var _bookTitle = MutableStateFlow<String>("")
    val bookTitle = _bookTitle.asStateFlow()

    @Volatile
    private var lastSelectedChapterUrl: String? = null
    private val book = appRepository.libraryBooks.getFlow(bookUrl.value)
        .filterNotNull()
        .map { ChaptersScreenState.BookState(it.toDomain()) }
        .toState(
            viewModelScope,
            ChaptersScreenState.BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null)
        )

    private val _bookState = MutableStateFlow<ChaptersScreenState.BookState>(ChaptersScreenState.BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null))
    val bookState = _bookState.asStateFlow()

    val state = ChaptersScreenState(
        book = bookState.toState(viewModelScope, ChaptersScreenState.BookState(title = bookTitle.value, url = bookUrl.value, coverImageUrl = null)),
        error = mutableStateOf(""),
        chapters = mutableStateListOf(),
        selectedChaptersUrl = mutableStateMapOf(),
        isRefreshing = mutableStateOf(false),
        settingChapterSort = appPreferences.CHAPTERS_SORT_ASCENDING.state(viewModelScope),
        isLocalSource = mutableStateOf(false),
        isRefreshable = mutableStateOf(true)
    )

    fun updateState(bookUrl: String, bookTitle: String){
        _bookUrl.value = bookUrl
        _bookTitle.value = bookTitle
        reload()
    }

    fun reload(){
        viewModelScope.launch {
            launch {
                chaptersRepository.getChaptersSortedFlow(bookUrl = bookUrl.value).collect {
                    state.chapters.clear()
                    state.chapters.addAll(it)
                }
            }
            launch {
                appRepository.libraryBooks.getFlow(bookUrl.value)
                    .filterNotNull()
                    .map { ChaptersScreenState.BookState(it.toDomain()) }.collect {
                        _bookState.value = it
                    }
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val isBookmarked =
                appRepository.toggleBookmark(bookTitle = bookTitle.value, bookUrl = bookUrl.value)
            val msg = if (isBookmarked) R.string.added_to_library else R.string.removed_from_library
            reload()
            toasty.show(msg)
        }
    }

    fun onPullRefresh() {
        if (!state.isRefreshable.value) {
            toasty.show(R.string.local_book_nothing_to_update)
            state.isRefreshing.value = false
            return
        }
        toasty.show(R.string.updating_book_info)
    }

    suspend fun getLastReadChapter(): String? =
        chaptersRepository.getLastReadChapter(bookUrl = bookUrl.value)

    fun setAsUnreadSelected() {
        val list = state.selectedChaptersUrl.toList()
        appScope.launch(Dispatchers.Default) {
            appRepository.bookChapters.setAsUnread(list.map { it.first })
        }
    }

    fun setAsReadSelected() {
        val list = state.selectedChaptersUrl.toList()
        appScope.launch(Dispatchers.Default) {
            appRepository.bookChapters.setAsRead(list.map { it.first })
        }
    }

    fun setAsReadUpToSelected() {
        if (state.selectedChaptersUrl.size > 1) return
        val selectedIndex = state.selectedChaptersUrl.keys.firstOrNull()?.let { selectedUrl ->
            state.chapters.indexOfFirst { it.chapter.id == selectedUrl }
        } ?: return

        if (selectedIndex != -1) {
            val chaptersToMarkAsRead = state.chapters.take(selectedIndex + 1).map { it.chapter.id }
            appScope.launch(Dispatchers.Default) {
                appRepository.bookChapters.setAsRead(chaptersToMarkAsRead)
            }
        }
    }

    fun setAsReadUpToUnSelected() {
        if (state.selectedChaptersUrl.size > 1) return
        val selectedIndex = state.selectedChaptersUrl.keys.firstOrNull()?.let { selectedUrl ->
            state.chapters.indexOfFirst { it.chapter.id == selectedUrl }
        } ?: return

        if (selectedIndex != -1) {
            val chaptersToMarkAsUnread = state.chapters.take(selectedIndex + 1).map { it.chapter.id }
            appScope.launch(Dispatchers.Default) {
                appRepository.bookChapters.setAsUnread(chaptersToMarkAsUnread)
            }
        }
    }

    fun deleteDownloadsSelected() {
        if (state.isLocalSource.value) return
        val list = state.selectedChaptersUrl.toList()
        appScope.launch(Dispatchers.Default) {
            appRepository.chapterBody.removeRows(list.map { it.first })
        }
    }

    fun onSelectionModeChapterClick(chapter: ChapterWithContext) {
        val url = chapter.chapter.id
        if (state.selectedChaptersUrl.containsKey(url)) {
            state.selectedChaptersUrl.remove(url)
        } else {
            state.selectedChaptersUrl[url] = Unit
        }
        lastSelectedChapterUrl = url
    }

    fun saveImageAsCover(uri: Uri) {
        appRepository.libraryBooks.saveImageAsCover(imageUri = uri, bookUrl = bookUrl.value)
    }

    fun onSelectionModeChapterLongClick(chapter: ChapterWithContext) {
        val url = chapter.chapter.id
        if (url != lastSelectedChapterUrl) {
            val indexOld = state.chapters.indexOfFirst { it.chapter.id == lastSelectedChapterUrl }
            val indexNew = state.chapters.indexOfFirst { it.chapter.id == url }
            val min = minOf(indexOld, indexNew)
            val max = maxOf(indexOld, indexNew)
            if (min >= 0 && max >= 0) {
                for (index in min..max) {
                    state.selectedChaptersUrl[state.chapters[index].chapter.id] = Unit
                }
                lastSelectedChapterUrl = state.chapters[indexNew].chapter.id
                return
            }
        }

        if (state.selectedChaptersUrl.containsKey(url)) {
            state.selectedChaptersUrl.remove(url)
        } else {
            state.selectedChaptersUrl[url] = Unit
        }
        lastSelectedChapterUrl = url
    }

    fun onChapterLongClick(chapter: ChapterWithContext) {
        val url = chapter.chapter.id
        state.selectedChaptersUrl[url] = Unit
        lastSelectedChapterUrl = url
    }

    fun unselectAll() {
        state.selectedChaptersUrl.clear()
    }

    fun selectAll() {
        state.chapters
            .toList()
            .map { it.chapter.id to Unit }
            .let { state.selectedChaptersUrl.putAll(it) }
    }

    fun invertSelection() {
        val allChaptersUrl = state.chapters.asSequence().map { it.chapter.id }.toSet()
        val selectedUrl = state.selectedChaptersUrl.asSequence().map { it.key }.toSet()
        val inverse = (allChaptersUrl - selectedUrl).asSequence().associateWith { }
        state.selectedChaptersUrl.clear()
        state.selectedChaptersUrl.putAll(inverse)
    }
}
