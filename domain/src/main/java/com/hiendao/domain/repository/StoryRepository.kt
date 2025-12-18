package com.hiendao.domain.repository

import com.hiendao.data.remote.retrofit.story.StoryApi
import com.hiendao.data.remote.retrofit.story.model.CreateStoryRequest
import javax.inject.Inject

interface StoryRepository {
    suspend fun createStory(request: CreateStoryRequest): Result<String>
}

class StoryRepositoryImpl @Inject constructor(
    private val storyApi: StoryApi
) : StoryRepository {
    override suspend fun createStory(request: CreateStoryRequest): Result<String> {
        return try {
            val response = storyApi.createStory(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
