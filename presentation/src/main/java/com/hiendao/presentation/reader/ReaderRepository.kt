package com.hiendao.presentation.reader

import androidx.room.withTransaction
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.model.Chapter
import com.hiendao.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.hiendao.domain.repository.BookChaptersRepository
import com.hiendao.domain.repository.LibraryBooksRepository
import com.hiendao.presentation.reader.domain.ChapterState
import com.hiendao.presentation.reader.domain.InitialPositionChapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ReaderRepository @Inject constructor(
    private val scope: AppCoroutineScope,
    private val database: AppDatabase,
    private val bookChaptersRepository: BookChaptersRepository,
    private val libraryBooksRepository: LibraryBooksRepository,
    private val appRepository: AppRepository,
) {

    fun saveBookLastReadPositionState(
        bookUrl: String,
        newChapter: ChapterState,
        oldChapter: ChapterState? = null
    ) {
        scope.launch(Dispatchers.IO) {
            database.withTransaction {
                libraryBooksRepository.updateLastReadChapter(
                    bookUrl = bookUrl,
                    lastReadChapterUrl = newChapter.chapterUrl
                )

                if (oldChapter?.chapterUrl != null) bookChaptersRepository.updatePosition(
                    chapterUrl = oldChapter.chapterUrl,
                    lastReadPosition = oldChapter.chapterItemPosition,
                    lastReadOffset = oldChapter.offset
                )

                bookChaptersRepository.updatePosition(
                    chapterUrl = newChapter.chapterUrl,
                    lastReadPosition = newChapter.chapterItemPosition,
                    lastReadOffset = newChapter.offset
                )
            }
        }
    }

    suspend fun getInitialChapterItemPosition(
        bookUrl: String,
        chapterIndex: Int,
        chapter: Chapter,
    ): InitialPositionChapter = coroutineScope {
        val titleChapterItemPosition = 0 // Hardcode or no?
        val book = async { appRepository.libraryBooks.get(bookUrl) }
        val position = InitialPositionChapter(
            chapterIndex = chapterIndex,
            chapterItemPosition = chapter.lastReadPosition,
            chapterItemOffset = chapter.lastReadOffset,
        )

        when {
            chapter.id == book.await()?.lastReadChapter -> position
            chapter.isRead -> InitialPositionChapter(
                chapterIndex = chapterIndex,
                chapterItemPosition = titleChapterItemPosition,
                chapterItemOffset = 0,
            )
            else -> position
        }
    }

    suspend fun downloadChapter(chapterUrl: String) =
        appRepository.chapterBody.getBody(chapterUrl)
}