package com.hiendao.data.remote.retrofit.chapter

import com.hiendao.data.remote.retrofit.chapter.model.ListChapterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChapterApi {
    @GET("")
    suspend fun getAllChapters(
        @Query("bookId") bookId: String
    ): ListChapterResponse




}