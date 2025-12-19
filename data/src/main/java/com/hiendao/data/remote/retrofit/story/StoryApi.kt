package com.hiendao.data.remote.retrofit.story

import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import com.hiendao.data.remote.retrofit.story.model.CreateStoryResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface StoryApi {
    @POST("ai/generatep-story")
    suspend fun createStory(@Body request: CreateStoryRequest): CreateStoryResponse
}
