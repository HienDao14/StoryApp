package com.hiendao.domain.repository


import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.local.entity.BookWithContext
import com.hiendao.data.remote.retrofit.book.BookApi
import com.hiendao.data.remote.retrofit.book.model.SearchBooksBody
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.data.utils.fileImporter
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.map.toDomainList
import com.hiendao.domain.map.toDomainListFromContent
import com.hiendao.domain.map.toDomainListFromContentLibrary
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.model.Book
import com.hiendao.domain.utils.Response
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryBooksRepository @Inject constructor(
    private val libraryDao: LibraryDao,
    private val appDatabase: AppDatabase,
    private val bookApi: BookApi,
    @ApplicationContext private val context: Context,
    private val appFileResolver: AppFileResolver,
    private val appCoroutineScope: AppCoroutineScope,
) {
    val getBooksInLibraryWithContextFlow by lazy {
        libraryDao.getBooksInLibraryWithContextFlow()
    }

    fun getFlow(url: String): Flow<BookEntity?>{
        return libraryDao.getFlow(url)
    }
    suspend fun insert(book: Book) = if (isValid(book)) libraryDao.insert(book.toEntity()) else Unit
    @Suppress("unused")
    suspend fun insert(books: List<Book>) = libraryDao.insert(books.filter(::isValid).map { it.toEntity() })
    suspend fun insertReplace(books: List<Book>) =
        libraryDao.insertReplace(books.filter(::isValid).map { it.toEntity() })

    suspend fun remove(bookUrl: String) = libraryDao.remove(bookUrl)
    @Suppress("unused")
    suspend fun remove(book: Book) = libraryDao.remove(book.toEntity())
    suspend fun update(book: Book) = libraryDao.update(book.toEntity())
    suspend fun updateLastReadEpochTimeMilli(bookUrl: String, lastReadEpochTimeMilli: Long) =
        libraryDao.updateLastReadEpochTimeMilli(bookUrl, lastReadEpochTimeMilli)

    suspend fun updateCover(bookUrl: String, coverUrl: String) =
        libraryDao.updateCover(bookUrl, coverUrl)

    suspend fun updateDescription(bookUrl: String, description: String) =
        libraryDao.updateDescription(bookUrl, description)

    suspend fun get(url: String) = libraryDao.get(url)

    suspend fun updateLastReadChapter(bookUrl: String, lastReadChapterUrl: String) =
        libraryDao.updateLastReadChapter(
            bookUrl = bookUrl,
            chapterUrl = lastReadChapterUrl
        )

    suspend fun getAll(
        page: Int = 0
    ): List<Book> {
        return try {
            bookApi.getBooks(page).content.toDomainListFromContent()
        } catch (e : Exception){
            emptyList()
        }
    }

    suspend fun getAllBooks(
        page: Int = 0
    ): Flow<Response<List<Book>>>{
        return flow {
            try {
                emit(Response.Loading)
                val result = bookApi.getBooks(page)
                Timber.tag("LibraryBooksRepository").d("getAllBooks: ${result.toString()}")
                val content = result.content
                Timber.tag("LibraryBooksRepository").d("getAllBooks: content size ${content}")
                val books = content.toDomainListFromContent()
                Timber.tag("LibraryBooksRepository").d("getAllBooks: books size ${books}")
                emit(Response.Success(books))
            } catch (e : Exception){
                Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun getAllInLibrary() = libraryDao.getAllInLibrary()
    suspend fun existInLibrary(url: String) = libraryDao.existInLibrary(url)
    suspend fun toggleBookmark(
        bookUrl: String,
        bookTitle: String
    ): Boolean = appDatabase.withTransaction {
        when (val book = get(bookUrl)) {
            null -> {
                insert(Book(title = bookTitle, url = bookUrl, inLibrary = true, isFavourite = true))
                true
            }
            else -> {
                update(book.copy(isFavourite = !book.isFavourite).toDomain())
                !book.isFavourite
            }
        }
    }

    fun saveImageAsCover(imageUri: Uri, bookUrl: String) {
        appCoroutineScope.launch {
            val imageData = context.contentResolver.openInputStream(imageUri)
                ?.use { it.readBytes() } ?: return@launch
            val bookFolderName = appFileResolver.getLocalBookFolderName(
                bookUrl = bookUrl
            )
            val bookCoverFile = appFileResolver.getStorageBookCoverImageFile(
                bookFolderName = bookFolderName
            )
            fileImporter(targetFile = bookCoverFile, imageData = imageData)
            delay(timeMillis = 1_000)
            updateCover(bookUrl = bookUrl, coverUrl = appFileResolver.getLocalBookCoverPath())
        }
    }

    suspend fun searchBooks(query: String, page: Int = 0): Flow<Response<List<Book>>>{
        return flow {
            try {
                val result = bookApi.searchBooks(
                    page = page,
                    searchBody = SearchBooksBody(
                        keyword = query
                    )
                )
                val books = result.content.toDomainListFromContent()
                emit(Response.Success(books))
            } catch (e : Exception){
                Timber.tag("LibraryBooksRepository").e(e, "searchBooks: error: ${e.message}")
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun toggleFavourite(bookId: String) {
        bookApi.toggleFavorite(bookId)
    }

    suspend fun getNewestBooksNormal(
        page: Int = 0
    ): List<Book>{
        return try {
            bookApi.getNewestBooks(page).content.toDomainListFromContent()
        } catch (e : Exception){
            Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getNewestBooks(
        page: Int = 0
    ): Flow<Response<List<Book>>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = bookApi.getNewestBooks(page)
                Timber.tag("LibraryBooksRepository").d("getAllBooks: ${result.toString()}")
                val content = result.content
                Timber.tag("LibraryBooksRepository").d("getAllBooks: content size ${content}")
                val books = content.toDomainListFromContent()
                Timber.tag("LibraryBooksRepository").d("getAllBooks: books size ${books}")
                emit(Response.Success(books))
            } catch (e : Exception){
                Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }
    suspend fun getFavoriteBooksNormal(
        page: Int = 0
    ): List<Book> {
        return try {
            bookApi.getFavouriteBooks(page).content.toDomainListFromContent()
        } catch (e : Exception){
            Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRecentlyReadBooks(
        page: Int = 0
    ): Flow<Response<List<Book>>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = bookApi.getRecentlyReadBooks(page)
                val content = result.content
                val books = content.toDomainListFromContent()
                emit(Response.Success(books))
            } catch (e : Exception){
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun getRecentlyReadBooksNormal(
        page: Int = 0
    ): List<Book> {
        return try {
            bookApi.getRecentlyReadBooks(page).content.toDomainListFromContent()
        } catch (e : Exception){
            Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFavoriteBooks(
        page: Int = 0
    ): Flow<Response<List<Book>>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = bookApi.getFavouriteBooks(page)
                Timber.tag("LibraryBooksRepository").d("getAllBooks: ${result.toString()}")
                val content = result.content
                Timber.tag("LibraryBooksRepository").d("getAllBooks: content size ${content}")
                val books = content.toDomainListFromContent()
                Timber.tag("LibraryBooksRepository").d("getAllBooks: books size ${books}")
                emit(Response.Success(books))
            } catch (e : Exception){
                Timber.tag("LibraryBooksRepository").e(e, "getAllBooks: error: ${e.message}")
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            bookApi.getNewestBooks().content.toDomainListFromContent()
        } catch (e : Exception){
            emptyList()
        }
    }

    suspend fun getBooksByCategory(
        categoryId: String,
        page: Int = 0
    ): List<Book> {
        return try {
            bookApi.getBookOfCategory(categoryId, page).results.toDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLibraryBooks(
        page: Int = 0
    ): Flow<Response<List<BookWithContext>>> {
        return flow {
            try {
                emit(Response.Loading)
                val result = bookApi.getLibraryBooks(page)
                val content = result.content
                val books = content.toDomainListFromContentLibrary()
                val booksEntity = books.map { it.toEntity() }
                libraryDao.insert(booksEntity)
                val booksWithContext = libraryDao.getBooksInLibraryWithContext()
                emit(Response.Success(booksWithContext))
            } catch (e : Exception){
                Timber.tag("LibraryBooksRepository").e(e, "getLibraryBooks: error: ${e.message}")
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }
}