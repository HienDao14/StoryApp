package com.hiendao.domain.repository


import androidx.room.withTransaction
import com.hiendao.data.local.dao.ChapterBodyDao
import com.hiendao.data.local.database.AppDatabase
import com.hiendao.data.local.entity.ChapterBodyEntity
import com.hiendao.domain.utils.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterBodyRepository @Inject constructor(
    private val chapterBodyDao: ChapterBodyDao,
    private val appDatabase: AppDatabase,
    private val bookChaptersRepository: BookChaptersRepository
) {
    suspend fun getAll() = chapterBodyDao.getAll()
    suspend fun insertReplace(chapterBodies: List<ChapterBodyEntity>) =
        chapterBodyDao.insertReplace(chapterBodies)

    private suspend fun insertReplace(chapterBody: ChapterBodyEntity) =
        chapterBodyDao.insertReplace(chapterBody)

    suspend fun removeRows(chaptersUrl: List<String>) =
        chaptersUrl.chunked(500).forEach { chapterBodyDao.removeChapterRows(it) }

    private suspend fun insertWithTitle(chapterBody: ChapterBodyEntity, title: String?) = appDatabase.withTransaction {
        insertReplace(chapterBody)
        if (title != null)
            bookChaptersRepository.updateTitle(chapterBody.chapterId, title)
    }

    suspend fun getBody(id: String): Response<String>{
        chapterBodyDao.get(id)?.let {
            return Response.Success(it.body)
        }
        return Response.Error(
            """
                Unable to load chapter from id:
                $id
                
                Source is local but chapter content missing.
            """.trimIndent(), Exception()
        )
    }
}