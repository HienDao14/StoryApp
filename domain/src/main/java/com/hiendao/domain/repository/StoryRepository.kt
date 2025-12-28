package com.hiendao.domain.repository

import com.hiendao.data.local.dao.ChapterBodyDao
import com.hiendao.data.local.dao.ChapterDao
import com.hiendao.data.local.dao.LibraryDao
import com.hiendao.data.local.entity.ChapterBodyEntity
import com.hiendao.data.remote.retrofit.story.StoryApi
import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import com.hiendao.domain.map.toEntity
import com.hiendao.domain.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface StoryRepository {
    suspend fun createStory(request: CreateStoryRequest): Flow<Response<String>>
    suspend fun deleteStory(id: String): Flow<Response<Unit>>
}

class StoryRepositoryImpl @Inject constructor(
    private val storyApi: StoryApi,
    private val libraryDao: LibraryDao,
    private val chapterDao: ChapterDao,
    private val chapterBodyDao: ChapterBodyDao
) : StoryRepository {
    override suspend fun createStory(request: CreateStoryRequest): Flow<Response<String>> {
        return flow {
            try {
                emit(Response.Loading)
                val bookResponse = storyApi.createStory(request)
                val bookEntity = bookResponse.toEntity(inLibrary = true)
                libraryDao.upsertBook(bookEntity)
                var chapters = bookResponse.chapters
                chapters?.forEach {
                    if(it.id.isNullOrEmpty()){
                        return@forEach
                    }
                    val server = it.toEntity(bookResponse.id!!)
                    chapterDao.insertChapter(server)
                    chapterBodyDao.insertReplace(
                        ChapterBodyEntity(
                            chapterId = it.id!!,
                            body = it.content?.replace("http://127.0.0.1:9000", "https://ctd37qdd-9000.asse.devtunnels.ms") ?: ""
                        )
                    )
                }
                emit(Response.Success(bookResponse.id.toString()))
            } catch (e: Exception) {
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }

    override suspend fun deleteStory(id: String): Flow<Response<Unit>> {
        return flow {
            try {
                emit(Response.Loading)
                // Delete from local DB first to reflect changes immediately (Optimistic UI update or just local consistency)
                libraryDao.remove(id) 
                
                // Call API
                val response = storyApi.deleteStory(id)
                emit(Response.Success(Unit))
            } catch (e: Exception) {
                emit(Response.Error(e.message.toString(), e))
            }
        }
    }
}
