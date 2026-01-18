package com.hiendao.data.remote.retrofit.chapter

import com.hiendao.data.remote.retrofit.book.model.BookResponseDTO
import com.hiendao.data.remote.retrofit.chapter.model.ChapterDTO
import com.hiendao.data.remote.retrofit.chapter.model.ListChapterResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChapterApi {
    @GET("")
    suspend fun getAllChapters(
        @Query("bookId") bookId: String
    ): ListChapterResponse

    @GET("stories/chapter/{id}")
    suspend fun getChapterDetail(
        @Path("id") chapterId: String
    ): ChapterDTO
}