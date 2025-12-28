package com.hiendao.presentation.bookDetail

import android.content.Context
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.coreui.utils.TernaryState
import com.hiendao.data.local.dao.ChapterBodyDao
import com.hiendao.data.local.dao.ChapterDao
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.entity.ChapterBodyEntity
import com.hiendao.data.remote.retrofit.book.BookApi
import com.hiendao.data.remote.retrofit.chapter.ChapterApi
import com.hiendao.data.utils.AppCoroutineScope
import com.hiendao.domain.map.toDomain
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.model.Book
import com.hiendao.domain.repository.AppRepository
import com.hiendao.domain.utils.AppFileResolver
import com.hiendao.domain.utils.Response
import com.hiendao.domain.utils.removeCommonTextFromTitles
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChaptersRepository @Inject constructor(
    private val appRepository: AppRepository,
    private val appPreferences: AppPreferences,
    private val bookApi: BookApi,
    @ApplicationContext private val context: Context,
    private val chapterDao: ChapterDao,
    private val chapterBodyDao: ChapterBodyDao,
    private val libraryDao: LibraryDao,
    private val chapterApi: ChapterApi
) {

    suspend fun getBookDetailForAll(
        bookId: String
    ): Flow<Response<Book>> {
        return flow {
            try {
                emit(Response.Loading)
                val bookResponse = bookApi.getBookDetail(bookId)
                val localBook = libraryDao.get(bookId)
                val bookEntity = bookResponse.toEntity()
                var finalEntity = if(localBook != null){
                    bookEntity.copy(
                        inLibrary = localBook.inLibrary,
                        isFavourite = localBook.isFavourite,
                        lastReadEpochTimeMilli = localBook.lastReadEpochTimeMilli,
                        completed = localBook.completed,
                        lastReadChapter = localBook.lastReadChapter
                    )
                } else bookEntity
                libraryDao.upsertBook(finalEntity)
                var chapters = bookResponse.chapters
                chapters?.forEach {
                    if(it.id.isNullOrEmpty()){
                        return@forEach
                    }
                    val local = chapterDao.get(it.id!!)
                    val server = it.toEntity(bookId)
                    val merged = if (local != null) {
                        server.copy(
                            read = local.read,
                            lastReadPosition = local.lastReadPosition,
                            lastReadOffset = local.lastReadOffset
                        )
                    } else {
                        server
                    }
                    chapterDao.insertChapter(merged)
                    chapterBodyDao.insertReplace(
                        ChapterBodyEntity(
                            chapterId = it.id!!,
                            body = it.content?.replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms") ?: ""
                        )
                    )
                }

                val book = bookResponse.toDomain()
                emit(Response.Success(book))
            } catch (e: Exception) {
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    suspend fun getBookDetail(
        bookId: String
    ): Flow<Response<Book>> {
        return flow {
            try {
                emit(Response.Loading)
                val bookResponse = bookApi.getBookDetail(bookId)
                val localBook = libraryDao.get(bookId)
                val bookEntity = bookResponse.toEntity()
                var finalEntity = if(localBook != null){
                    bookEntity.copy(
                        inLibrary = localBook.inLibrary,
                        isFavourite = localBook.isFavourite,
                        lastReadEpochTimeMilli = localBook.lastReadEpochTimeMilli,
                        completed = localBook.completed,
                        lastReadChapter = localBook.lastReadChapter
                    )
                } else bookEntity
                libraryDao.upsertBook(finalEntity)
                var chapters = bookResponse.chapters
                chapters?.forEach {
                    if(it.id.isNullOrEmpty()){
                        return@forEach
                    }
                    val local = chapterDao.get(it.id!!)
                    val server = it.toEntity(bookId)
                    val merged = if (local != null) {
                        server.copy(
                            read = local.read,
                            lastReadPosition = local.lastReadPosition,
                            lastReadOffset = local.lastReadOffset
                        )
                    } else {
                        server
                    }
                    chapterDao.insertChapter(merged)
                    chapterBodyDao.insertReplace(
                        ChapterBodyEntity(
                            chapterId = it.id!!,
                            body = it.content?.replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms") ?: ""
                        )
                    )
                }

                val book = bookResponse.toDomain()
                emit(Response.Success(book))
            } catch (e: Exception) {
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    fun getChaptersSortedFlow(bookUrl: String) = appRepository.bookChapters
        .getChaptersWithContextFlow(bookUrl = bookUrl)
        .map(::removeCommonTextFromTitles)
        // Sort the chapters given the order preference
        .combine(appPreferences.CHAPTERS_SORT_ASCENDING.flow()) { chapters, sorted ->
            when (sorted) {
                TernaryState.Active -> chapters.sortedBy { it.chapter.position }
                TernaryState.Inverse -> chapters.sortedByDescending { it.chapter.position }
                TernaryState.Inactive -> chapters
            }
        }
        .flowOn(Dispatchers.Default)

    suspend fun getLastReadChapter(bookUrl: String): String? =
        appRepository.libraryBooks.get(bookUrl)?.lastReadChapter
            ?: appRepository.bookChapters.getFirstChapter(bookUrl)?.id

    suspend fun getChapterDetail(chapterId: String, bookId: String) {
        try {
            val chapterResponse = chapterApi.getChapterDetail(chapterId)
            if(chapterResponse.id.isNullOrEmpty() || chapterResponse.content.isNullOrEmpty()) {
                return
            }
            val local = chapterDao.get(chapterResponse.id!!)
            val server = chapterResponse.toEntity(bookId)
            val merged = if (local != null) {
                server.copy(
                    read = local.read,
                    lastReadPosition = local.lastReadPosition,
                    lastReadOffset = local.lastReadOffset
                )
            } else {
                server
            }
            chapterDao.insertChapter(merged)
            chapterBodyDao.insertReplace(
                ChapterBodyEntity(
                    chapterId = chapterResponse.id!!,
                    body = chapterResponse.content?.replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms") ?: ""
                )
            )
        } catch (e : Exception){
            // Ignore errors
            Timber.tag("ChaptersRepository").e(e, "getChapterDetail: error: ${e.message}" )
        }
    }
}