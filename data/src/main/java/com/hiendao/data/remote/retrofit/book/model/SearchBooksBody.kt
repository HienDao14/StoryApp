package com.hiendao.data.remote.retrofit.book.model

data class SearchBooksBody(
    val ageRating: Int ?= null,
    val author: String ?= null,
    val categories: List<String> = emptyList(),
    val keyword: String = ""
)