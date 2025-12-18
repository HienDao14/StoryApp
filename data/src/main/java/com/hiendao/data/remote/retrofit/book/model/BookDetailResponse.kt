package com.hiendao.data.remote.retrofit.book.model

data class BookDetailResponse(
    val result: BookDTO,
    val totalPages: Int,
    val totalResults: Int
)
