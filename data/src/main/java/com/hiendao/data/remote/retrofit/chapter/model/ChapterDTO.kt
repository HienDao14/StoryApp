package com.hiendao.data.remote.retrofit.chapter.model

data class ChapterDTO(
    val id: String,
    val chapterNumber: Int,
    val title: String,
    val content: String,
    val images: List<String>
)
