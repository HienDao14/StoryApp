package com.hiendao.domain.repository

import android.content.Context
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.domain.model.Book
import com.hiendao.domain.model.Chapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get


@Singleton
class AppRepository @Inject constructor(
    private val db: AppDatabase,
    @ApplicationContext private val context: Context,
    val libraryBooks: LibraryBooksRepository,
    val bookChapters: BookChaptersRepository,
    val chapterBody: ChapterBodyRepository,
    private val appFileResolver: AppFileResolver
) {
    val settings = Settings()
    val eventDataRestored = MutableSharedFlow<Unit>()

    suspend fun toggleBookmark(bookUrl: String, bookTitle: String): Boolean {
        val realUrl = appFileResolver.getLocalIfContentType(bookUrl, bookFolderName = bookTitle)
        return libraryBooks.toggleBookmark(bookUrl = bookUrl, bookTitle = bookTitle)
    }

    inner class Settings {
        suspend fun clearNonLibraryData() = withContext(Dispatchers.IO)
        {
            db.libraryDao.removeAllNonLibraryRows()
            db.chapterDao.removeAllNonLibraryRows()
            db.chapterBodyDao.removeAllNonChapterRows()
        }

        /**
         * Folder where additional book data like images is stored.
         * Each subfolder must be an unique folder for each book.
         * Each book folder can have an arbitrary structure internally.
         */
        val folderBooks = appFileResolver.folderBooks
    }
}

fun isValid(book: Book): Boolean = book.url.matches("""^(https?|local)://.*""".toRegex())
fun isValid(chapter: Chapter): Boolean =
    chapter.id.matches("""^(https?|local)://.*""".toRegex())