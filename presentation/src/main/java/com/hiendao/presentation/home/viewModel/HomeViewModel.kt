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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryBooksRepository: LibraryBooksRepository,
    private val categoryRepository: CategoryRepository,
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    private val toasty: Toasty,
    @ApplicationContext private val context: Context
): ViewModel()  {

    val currentTheme = appPreferences.THEME_ID.state(viewModelScope)
    val followsSystemTheme = appPreferences.THEME_FOLLOW_SYSTEM.state(viewModelScope)

    private val _books: MutableStateFlow<List<Book>> = MutableStateFlow(emptyList())
    val books = _books.asStateFlow()

    private val _homeState : MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    fun reload(){
        getAllBooks()
        getNewestBooks()
        getFavouriteBooks()
        getAllCategories()
    }

    fun getAllBooks(page: Int = 1){
        viewModelScope.launch {
            val allBooks = libraryBooksRepository.getAll().map { it.toDomain() }
            _homeState.update { it.copy(allBooksPage = page, allBooks = allBooks) }
        }
    }

    fun getNewestBooks(page: Int = 1){
        viewModelScope.launch {
            val newestBooks = libraryBooksRepository.getNewestBooks(page)
            _homeState.update { it.copy(newestBooksPage = page, newestBooks = newestBooks) }
        }
    }

    fun getFavouriteBooks(page: Int = 1) {
        viewModelScope.launch {
            val favoriteBooks = libraryBooksRepository.getFavoriteBooks(page)
            _homeState.update { it.copy(favouriteBooksPage = page, favouriteBooks = favoriteBooks) }
        }
    }

    private fun getAllCategories() {
        viewModelScope.launch {
            val categories = categoryRepository.getAllCategories()
            _homeState.update { it.copy(categories = categories) }
        }
    }

    fun getFeaturedBooks() {
        viewModelScope.launch {
            val featuredBooks = libraryBooksRepository.getFeaturedBooks()
            _homeState.update { it.copy(featuredBooks = featuredBooks) }
        }
    }

    fun toggleFavourite(book: Book) {
        viewModelScope.launch {
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
}

data class HomeState(
    val isLoading: Boolean = false,
    val isRefresh: Boolean = false,
    val errorMsg: String? = null,
    val allBooks: List<Book> = emptyList(),
    val newestBooks: List<Book> = emptyList(),
    val favouriteBooks: List<Book> = emptyList(),
    val featuredBooks: List<Book> = emptyList(),
    val categories: List<Category> = emptyList(),

    var allBooksPage: Int = 1,
    var newestBooksPage: Int = 1,
    var favouriteBooksPage: Int = 1
)