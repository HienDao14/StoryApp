package com.hiendao.data.remote.retrofit.book.model

data class BookDTO(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverImageUrl: String,
    val ageRating: String,
    val status: String,
    val totalChapter: Int
)