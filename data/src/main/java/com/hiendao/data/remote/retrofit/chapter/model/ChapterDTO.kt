package com.hiendao.data.remote.retrofit.chapter.model

data class ChapterDTO(
    val id: String?= null,
    val chapterNumber: Int?= null,
    val title: String?= null,
    val content: String?= null,
    val images: List<String>?= null
)
