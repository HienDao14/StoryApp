package com.hiendao.data.remote.retrofit.story

import com.hiendao.data.remote.retrofit.book.model.BookResponseDTO
import com.hiendao.data.remote.retrofit.book.model.BooksResponseDTO
import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import com.hiendao.data.remote.retrofit.story.model.CreateStoryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface StoryApi {
    @POST("ai/generate-story")
    suspend fun createStory(@Body request: CreateStoryRequest): BookResponseDTO

    @DELETE("stories/{id}")
    suspend fun deleteStory(@Path("id") id: String)
}
