package com.hiendao.data.remote.retrofit.book.model

data class ListBookResponse(
    val page: Int,
    val results: List<BookDTO>,
    val totalPages: Int,
    val totalResults: Int
)
