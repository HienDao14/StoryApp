package com.hiendao.presentation.library.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.R
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.TernaryState
import com.hiendao.coreui.utils.Toasty
import com.hiendao.coreui.utils.toState
import com.hiendao.data.local.entity.BookWithContext
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.model.Book
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.repository.LibraryBooksRepository
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.domain.utils.Response
import com.hiendao.domain.repository.StoryRepository
import com.hiendao.presentation.bookDetail.ChaptersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.text.get

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val preferences: AppPreferences,
    private val toasty: Toasty,
    private val libraryBooksRepository: LibraryBooksRepository,
    private val storyRepository: StoryRepository
) : BaseViewModel() {

    private var _allBooks = MutableStateFlow<Response<List<BookWithContext>>>(Response.None)
    val allBooks = _allBooks.asStateFlow()

    private var _apiFavorites = MutableStateFlow<List<Book>>(emptyList())

    private var _listReading = MutableStateFlow<List<BookWithContext>>(emptyList())
    val listReading = _listReading.asStateFlow()

    private var _listCompleted = MutableStateFlow<List<BookWithContext>>(emptyList())
    val listCompleted = _listCompleted.asStateFlow()

    private var _listFavourite = MutableStateFlow<List<BookWithContext>>(emptyList())
    val listFavourite = _listFavourite.asStateFlow()

    private var _currentBook = MutableStateFlow<Book?>(null)
    val currentBook = _currentBook.asStateFlow()

    var isPullRefreshing by mutableStateOf(false)
    var readFilter by preferences.LIBRARY_FILTER_READ.state(viewModelScope)
    var readSort by preferences.LIBRARY_SORT_LAST_READ.state(viewModelScope)

    @Suppress("UNUSED_PARAMETER")
    fun onLibraryCategoryRefresh() {
        showLoadingSpinner()
        toasty.show(R.string.updating_library_notice)
        getApiFavorites()
    }

    private fun showLoadingSpinner() {
        viewModelScope.launch {
            // Keep for 3 seconds so the user can notice the refresh has been triggered.
            isPullRefreshing = true
            delay(3000L)
            isPullRefreshing = false
        }
    }

    fun toggleFavourite(book: Book) {
        viewModelScope.launch {
            launch {
                libraryBooksRepository.toggleFavourite(book.id)
            }
            val isBookmarked =
                appRepository.toggleBookmark(bookTitle = book.title, bookUrl = book.url)
            val msg = if (isBookmarked) R.string.added_to_library else R.string.removed_from_library
            getApiFavorites()
            toasty.show(msg)
        }
    }

    init {
        getApiFavorites()
        initBooksFlow(
            onDone = {
                // === Tối ưu listReading Flow ===
                _allBooks
                    .filterIsInstance<Response.Success<List<BookWithContext>>>() // Chỉ xử lý khi Response là Success
                    .map { it.data } // Lấy ra List<BookWithContext>
                    .combine(preferences.LIBRARY_FILTER_READ.flow()) { list, filterRead ->
                        // B1: Lọc sách chưa hoàn thành (book.completed == false)
                        val readingList = list.filter { book -> book.book.completed == false }

                        // B2: Áp dụng filterRead
                        when (filterRead) {
                            TernaryState.Active -> readingList.filter { it.chaptersCount == it.chaptersReadCount }
                            TernaryState.Inverse -> readingList.filter { it.chaptersCount != it.chaptersReadCount }
                            TernaryState.Inactive -> readingList
                        }
                    }.combine(preferences.LIBRARY_SORT_LAST_READ.flow()) { list, sortRead ->
                        // B3: Áp dụng sortRead
                        when (sortRead) {
                            TernaryState.Active -> list.sortedByDescending { it.book.lastReadEpochTimeMilli }
                            TernaryState.Inverse -> list.sortedBy { it.book.lastReadEpochTimeMilli }
                            TernaryState.Inactive -> list
                        }
                    }.flowOn(Dispatchers.Default) // Chuyển việc tính toán sang Dispatchers.Default
                    .collectIn(viewModelScope) {
                        _listReading.value = it // Cập nhật MutableStateFlow
                    }

                _allBooks
                    .filterIsInstance<Response.Success<List<BookWithContext>>>() // Chỉ xử lý khi Response là Success
                    .map { it.data } // Lấy ra List<BookWithContext>
                    .combine(preferences.LIBRARY_FILTER_READ.flow()) { list, filterRead ->
                        // B1: Lọc sách chưa hoàn thành (book.completed == false)
                        val readingList = list.filter { book -> book.book.completed == true }

                        // B2: Áp dụng filterRead
                        when (filterRead) {
                            TernaryState.Active -> readingList.filter { it.chaptersCount == it.chaptersReadCount }
                            TernaryState.Inverse -> readingList.filter { it.chaptersCount != it.chaptersReadCount }
                            TernaryState.Inactive -> readingList
                        }
                    }.combine(preferences.LIBRARY_SORT_LAST_READ.flow()) { list, sortRead ->
                        // B3: Áp dụng sortRead
                        when (sortRead) {
                            TernaryState.Active -> list.sortedByDescending { it.book.lastReadEpochTimeMilli }
                            TernaryState.Inverse -> list.sortedBy { it.book.lastReadEpochTimeMilli }
                            TernaryState.Inactive -> list
                        }
                    }.flowOn(Dispatchers.Default) // Chuyển việc tính toán sang Dispatchers.Default
                    .collectIn(viewModelScope) {
                        _listCompleted.value = it // Cập nhật MutableStateFlow
                    }

                // === List Favourite from API merged with Local ===
                combine(
                    _allBooks,
                    _apiFavorites,
                    preferences.LIBRARY_FILTER_READ.flow(),
                    preferences.LIBRARY_SORT_LAST_READ.flow()
                ) { allBooksResponse, apiFavs, filterRead, sortRead ->
                    val localBooks = (allBooksResponse as? Response.Success)?.data ?: emptyList()
                    val localBooksMap = localBooks.associateBy { it.book.id }

                    val mergedFavList = apiFavs.map { apiBook ->
                        localBooksMap[apiBook.id] ?: BookWithContext(
                            book = apiBook.toEntity().copy(isFavourite = true),
                            chaptersCount = apiBook.totalChapters,
                            chaptersReadCount = 0 // Default to 0 if not in local library
                        )
                    }

                    // Apply filters if needed, passing through for now as favorites are usually just a list
                    // Apply Sort
                    when (sortRead) {
                        TernaryState.Active -> mergedFavList.sortedByDescending { it.book.lastReadEpochTimeMilli }
                        TernaryState.Inverse -> mergedFavList.sortedBy { it.book.lastReadEpochTimeMilli }
                        TernaryState.Inactive -> mergedFavList
                    }
                }.flowOn(Dispatchers.Default)
                .collectIn(viewModelScope) {
                     _listFavourite.value = it
                }

            }
        )
    }

    private fun getApiFavorites() {
        viewModelScope.launch {
            libraryBooksRepository.getFavoriteBooks().collect { response ->
                if (response is Response.Success) {
                    _apiFavorites.value = response.data
                }
            }
        }
    }

    fun initBooksFlow(onDone: () -> Unit = {}) { // Đổi tên để phản ánh mục đích khởi tạo Flow
        viewModelScope.launch(Dispatchers.IO) {
            libraryBooksRepository.getLibraryBooks().collect { response ->
                when(response){
                    is Response.Loading -> {
                        _allBooks.emit(Response.Loading)
                    }
                    is Response.Error -> {
                        _allBooks.emit(Response.Error(response.message ?: "Unknown Error", response.exception))
                        onDone.invoke()
                    }
                    is Response.Success -> {
                        _allBooks.emit(Response.Success(response.data))
                        onDone.invoke()
                    }
                    else -> Unit

                }
            }
        }
    }

    fun readFilterToggle() {
        readFilter = readFilter.next()
    }

    fun readSortToggle() {
        readSort = readSort.next()
    }

    fun bookCompletedToggle(bookUrl: String) {
        viewModelScope.launch {
            val book = appRepository.libraryBooks.get(bookUrl)?.toDomain() ?: return@launch
            appRepository.libraryBooks.update(book.copy(completed = !book.completed))
        }
    }
    fun getBook(bookId: String) = appRepository.libraryBooks.getFlow(bookId)

    fun deleteStory(bookId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(bookId).collect {
                when (it) {
                    is Response.Loading -> showLoadingSpinner()
                    is Response.Success -> {
                        toasty.show(R.string.done)
                    }
                    is Response.Error -> {
                        toasty.show(it.message ?: "Error deleting story")
                    }
                    else -> Unit
                }
            }
        }
    }
}


// Hàm mở rộng để đơn giản hóa việc thu thập
private fun <T> Flow<T>.collectIn(scope: CoroutineScope, action: suspend (T) -> Unit) =
    scope.launch {
        this@collectIn.collect(action)
    }