package com.hiendao.presentation.home.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.R
import com.hiendao.coreui.utils.Toasty
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Category
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.repository.BooksRepository
import com.hiendao.domain.repository.CategoryRepository
import com.hiendao.domain.repository.LibraryBooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.hiendao.coreui.appPreferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.hiendao.coreui.theme.Themes
import com.hiendao.coreui.theme.toPreferenceTheme
import com.hiendao.domain.utils.Response
import com.hiendao.presentation.voice.ReadingVoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryBooksRepository: LibraryBooksRepository,
    private val categoryRepository: CategoryRepository,
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    private val toasty: Toasty,
    @ApplicationContext private val context: Context,
    private val readingVoiceRepository: ReadingVoiceRepository
): ViewModel()  {

    val currentTheme = appPreferences.THEME_ID.state(viewModelScope)
    val followsSystemTheme = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope)

    private val _books: MutableStateFlow<List<Book>> = MutableStateFlow(emptyList())
    val books = _books.asStateFlow()

    private val _homeState : MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _voiceState: MutableStateFlow<Response<Boolean>> = MutableStateFlow(Response.None)
    val voiceState = _voiceState.asStateFlow()

    init {
        viewModelScope.launch {
            val response = readingVoiceRepository.getAllVoices()
            if (response is Response.Success) {
                _voiceState.emit(Response.Success(true))
            }
        }
    }

    fun resetVoiceState() {
        _voiceState.value = Response.None
    }

    fun reload(){
        getAllBooks()
        getNewestBooks()
        getRecentlyReadBooks()
        getFavouriteBooks()
        getAllCategories()
    }

    fun getAllBooks(page: Int = 0){
        viewModelScope.launch {
            libraryBooksRepository.getAllBooks().collect { response ->
                when(response){
                    is Response.Loading -> {
                        _homeState.update { it.copy(isLoading = true) }
                    }
                    is Response.Success -> {
                        val allBooks = _homeState.value.allBooks
                        val mergedBooks = mergeBooks(allBooks, response.data)
                        _homeState.update { it.copy(isLoading = false, allBooksPage = page, allBooks = mergedBooks, isAllBooksEnd = response.data.isEmpty()) }
                    }
                    is Response.Error -> {
                        _homeState.update { it.copy(isLoading = false, errorMsg = response.message) }
                    }
                    is Response.None -> Unit
                }
            }
        }
    }

    fun getNewestBooks(page: Int = 0){
        viewModelScope.launch {
            libraryBooksRepository.getNewestBooks(page).collect { response ->
                when(response){
                    is Response.Loading -> {
                        _homeState.update { it.copy(isLoading = true) }
                    }
                    is Response.Success -> {
                        val newestBooks = _homeState.value.newestBooks
                        val mergedBooks = mergeBooks(newestBooks, response.data)
                        _homeState.update { it.copy(isLoading = false, newestBooksPage = page, newestBooks = mergedBooks, isNewestBooksEnd = response.data.isEmpty()) }
                    }
                    is Response.Error -> {
                        _homeState.update { it.copy(isLoading = false, errorMsg = response.message) }
                    }
                    is Response.None -> Unit
                }
            }
        }
    }

    fun getFavouriteBooks(page: Int = 0) {
        viewModelScope.launch {
            libraryBooksRepository.getFavoriteBooks(page).collect { response ->
                when(response){
                    is Response.Loading -> {
                        _homeState.update { it.copy(isLoading = true) }
                    }
                    is Response.Success -> {
                        val favoriteBooks = _homeState.value.favouriteBooks
                        val mergedBooks = mergeBooks(favoriteBooks, response.data)
                        _homeState.update { it.copy(isLoading = false, favouriteBooksPage = page, favouriteBooks = mergedBooks, isFavouriteBooksEnd = response.data.isEmpty()) }
                    }
                    is Response.Error -> {
                        _homeState.update { it.copy(isLoading = false, errorMsg = response.message) }
                    }
                    is Response.None -> Unit
                }
            }
        }
    }

    fun getRecentlyReadBooks(page: Int = 0) {
        viewModelScope.launch {
            libraryBooksRepository.getRecentlyReadBooks(page).collect { response ->
                when(response){
                    is Response.Loading -> {
                        _homeState.update { it.copy(isLoading = true) }
                    }
                    is Response.Success -> {
                        val recentlyRead = _homeState.value.recentlyRead
                        val mergedBooks = mergeBooks(recentlyRead, response.data)
                        _homeState.update { it.copy(isLoading = false, recentlyReadPage = page, recentlyRead = mergedBooks, isRecentlyReadEnd = response.data.isEmpty()) }
                    }
                    is Response.Error -> {
                        _homeState.update { it.copy(isLoading = false, errorMsg = response.message) }
                    }
                    is Response.None -> Unit
                }
            }
        }
    }

    private fun getAllCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getAllCategories()
            _homeState.update { it.copy(categories = categories) }
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
            reload()
            toasty.show(msg)
        }
    }

    fun onThemeChange(themes: Themes) {
        appPreferences.THEME_ID.value = themes.toPreferenceTheme
    }

    fun onFollowSystemChange(follow: Boolean) {
        appPreferences.THEME_FOLLOW_SYSTEM.value = follow
    }

    fun importBook(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Resolve file name
                val returnCursor = context.contentResolver.query(uri, null, null, null, null)
                val nameIndex = returnCursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                returnCursor?.moveToFirst()
                val fileName = (nameIndex?.let { returnCursor.getString(it) } ?: "Imported Book").substringBeforeLast(".")
                returnCursor?.close()

                // 2. Create destination file
                val destinationFile = File(context.filesDir, "imported_books/${System.currentTimeMillis()}_$fileName")
                destinationFile.parentFile?.mkdirs()

                // 3. Copy content
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // 4. Add to Library
                val newBook = Book(
                    id = destinationFile.absolutePath, // Use path as ID/URL
                    title = fileName,
                    url = destinationFile.absolutePath,
                    coverImageUrl = "", // No cover for now
                    inLibrary = true,
                    isFavourite = false,
                    completed = false,
                    author = "Imported"
                )
                libraryBooksRepository.insert(newBook)

                withContext(Dispatchers.Main) {
                    toasty.show(R.string.added_to_library) // Ensure string exists or use generic
                    getAllBooks() // Refresh list
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    toasty.show("Import failed: ${e.message}")
                }
            }
        }
    }
    private fun mergeBooks(current: List<Book>, new: List<Book>): MutableList<Book> {
        val newIds = new.map { it.id }.toSet()
        return (current.filter { it.id !in newIds } + new).toMutableList()
    }
}

data class HomeState(
    val isLoading: Boolean = false,
    val isRefresh: Boolean = false,
    val errorMsg: String? = null,
    val allBooks: MutableList<Book> = mutableListOf(),
    val newestBooks: MutableList<Book> = mutableListOf(),
    val favouriteBooks: MutableList<Book> = mutableListOf(),
    val featuredBooks: MutableList<Book> = mutableListOf(),
    val recentlyRead: MutableList<Book> = mutableListOf(),
    val categories: List<Category> = emptyList(),

    var allBooksPage: Int = 0,
    var newestBooksPage: Int = 0,
    var favouriteBooksPage: Int = 0,
    var recentlyReadPage: Int = 0,
    var isNewestBooksEnd: Boolean = false,
    var isAllBooksEnd: Boolean = false,
    var isFavouriteBooksEnd: Boolean = false,
    var isRecentlyReadEnd: Boolean = false
)