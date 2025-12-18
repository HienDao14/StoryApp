package com.hiendao.data.remote.retrofit.story

import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface StoryApi {
    @POST("story/create")
    suspend fun createStory(@Body request: CreateStoryRequest): String
}
