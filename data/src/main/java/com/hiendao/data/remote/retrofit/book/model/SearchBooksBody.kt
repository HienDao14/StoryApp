package com.hiendao.data.remote.retrofit.book.model

data class SearchBooksBody(
    val ageRating: Int = 10,
    val author: String = "",
    val categories: List<String> = emptyList(),
    val keyword: String = ""
)