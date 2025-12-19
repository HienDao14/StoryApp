package com.hiendao.data.remote.retrofit.book.model

import com.hiendao.data.remote.retrofit.chapter.model.ChapterDTO

data class BookResponseDTO(
    val ageRating: String?=null,
    val author: String?=null,
    val categoryNames: List<String>?=null,
    val chapters: List<ChapterDTO>?=null,
    val coverImageUrl: String?=null,
    val createDate: String?=null,
    val description: String?=null,
    val id: String?=null,
    val isFavorite: Boolean?=null,
    val lastUpdateDate: String?=null,
    val status: String?=null,
    val title: String?=null,
    val uploaderName: String?=null
)