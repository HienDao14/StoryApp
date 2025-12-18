package com.hiendao.domain.repository


import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.data.local.entity.BookEntity
import com.hiendao.data.remote.retrofit.book.BookApi
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.data.utils.fileImporter
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.map.toDomainList
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.model.Book
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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

    suspend fun getAll() = libraryDao.getAll()
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

    suspend fun searchBooks(query: String): Flow<List<Book>>{
        return flow {
            val books = libraryDao.searchBooks(query).map { it.toDomain() }
            emit(books)
        }
    }

    suspend fun getNewestBooks(
        page: Int = 1
    ): List<Book> {
        return try {
            bookApi.getNewestBooks(page).results.toDomainList()
        } catch (e : Exception){
            emptyList()
        }
    }
    suspend fun getFavoriteBooks(
        page: Int = 1
    ): List<Book> {
        return try {
            bookApi.getFavouriteBooks(page).results.toDomainList()
        } catch (e : Exception){
            emptyList()
        }
    }

    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            bookApi.getFeatureBooks().results.toDomainList()
        } catch (e : Exception){
            emptyList()
        }
    }

    suspend fun getBooksByCategory(
        categoryId: String,
        page: Int = 1
    ): List<Book> {
        return try {
            bookApi.getBookOfCategory(categoryId, page).results.toDomainList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}