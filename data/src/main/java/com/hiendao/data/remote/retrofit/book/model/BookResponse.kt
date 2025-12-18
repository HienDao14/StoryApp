package com.hiendao.data.remote.retrofit.book.model

data class BookResponse(
    val results: BookDTO,
    val totalPages: Int,
    val totalResults: Int
)
