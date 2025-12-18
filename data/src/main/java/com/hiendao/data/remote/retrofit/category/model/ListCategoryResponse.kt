package com.hiendao.data.remote.retrofit.category.model

data class ListCategoryResponse(
    val page: Int,
    val results: List<CategoryDTO>,
    val totalPages: Int,
    val totalResults: Int
)
