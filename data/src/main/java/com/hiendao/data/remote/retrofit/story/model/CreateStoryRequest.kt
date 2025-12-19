package com.hiendao.data.remote.retrofit.story.model

data class CreateStoryRequest(
    val title: String,
    val freeText: String,
    val durationSeconds: Int,
    val language: String,
)