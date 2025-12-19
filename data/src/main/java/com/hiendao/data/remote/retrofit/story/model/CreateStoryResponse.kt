package com.hiendao.data.remote.retrofit.story.model

data class CreateStoryResponse(
    val content: String,
    val createDate: String,
    val id: String,
    val title: String,
    val uploaderId: String
)